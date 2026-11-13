package com.example.appmovimientos.ui.screens.asistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.repository.AsistenteProtocolo
import com.example.appmovimientos.data.repository.AsistenteRepository
import com.example.appmovimientos.data.repository.GuardadorFirebase
import com.example.appmovimientos.data.repository.GuardadorPropuestas
import com.example.appmovimientos.data.repository.MensajeHistorial
import com.example.appmovimientos.data.repository.Propuesta
import com.example.appmovimientos.data.repository.ResultadoAsistente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AsistenteViewModel(
    private val repository: AsistenteRepository = AsistenteRepository(),
    private val guardador: GuardadorPropuestas = GuardadorFirebase()
) : ViewModel() {

    /** Qué pasó con un registro propuesto por el asistente. */
    enum class EstadoPropuesta { PENDIENTE, GUARDANDO, GUARDADA, CANCELADA, REEMPLAZADA }

    data class Mensaje(
        val id: Long,
        val esUsuario: Boolean,
        val texto: String,
        val esError: Boolean = false,
        val propuesta: Propuesta? = null,
        val estadoPropuesta: EstadoPropuesta = EstadoPropuesta.PENDIENTE,
        val errorPropuesta: String? = null
    )

    data class UiState(
        val mensajes: List<Mensaje> = emptyList(),
        val texto: String = "",
        val enviando: Boolean = false,
        val consultasRestantes: Int? = null
    ) {
        val puedeEnviar: Boolean get() = texto.isNotBlank() && !enviando
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()
    private var siguienteId = 0L

    fun onTextoChange(nuevo: String) {
        _uiState.update { it.copy(texto = nuevo.take(AsistenteProtocolo.MAX_LARGO_PREGUNTA)) }
    }

    /** Texto dictado por voz: se coloca en la caja para que el usuario lo revise antes de enviar. */
    fun onTextoDictado(dictado: String) {
        val limpio = dictado.trim()
        if (limpio.isEmpty()) return
        val actual = _uiState.value.texto.trim()
        onTextoChange(if (actual.isEmpty()) limpio else "$actual $limpio")
    }

    /** Envía lo escrito en la caja de texto. */
    fun enviar() = enviarPregunta(_uiState.value.texto)

    /** Envía una pregunta (también se usa con las preguntas sugeridas). */
    fun enviarPregunta(pregunta: String) {
        val limpia = pregunta.trim().take(AsistenteProtocolo.MAX_LARGO_PREGUNTA)
        if (limpia.isEmpty() || _uiState.value.enviando) return

        // Contexto: los mensajes anteriores válidos (sin errores), antes de agregar la nueva pregunta
        val historial = historialPara(_uiState.value.mensajes)

        _uiState.update {
            it.copy(
                mensajes = it.mensajes + Mensaje(siguienteId++, esUsuario = true, texto = limpia),
                texto = "",
                enviando = true
            )
        }

        viewModelScope.launch {
            val resultado = repository.preguntar(limpia, historial)
            _uiState.update { estado ->
                when (resultado) {
                    is ResultadoAsistente.Exito -> {
                        // Una propuesta nueva reemplaza a la que seguía pendiente (p. ej. tras una corrección)
                        val anteriores = if (resultado.propuesta == null) estado.mensajes else estado.mensajes.map {
                            if (it.propuesta != null && it.estadoPropuesta == EstadoPropuesta.PENDIENTE)
                                it.copy(estadoPropuesta = EstadoPropuesta.REEMPLAZADA, errorPropuesta = null)
                            else it
                        }
                        estado.copy(
                            mensajes = anteriores + Mensaje(
                                siguienteId++, esUsuario = false, texto = resultado.respuesta, propuesta = resultado.propuesta
                            ),
                            enviando = false,
                            consultasRestantes = resultado.consultasRestantes ?: estado.consultasRestantes
                        )
                    }
                    is ResultadoAsistente.Error -> estado.copy(
                        mensajes = estado.mensajes + Mensaje(siguienteId++, esUsuario = false, texto = resultado.mensaje, esError = true),
                        enviando = false,
                        consultasRestantes = resultado.consultasRestantes ?: estado.consultasRestantes
                    )
                }
            }
        }
    }

    /** El usuario pulsó "Guardar": se guarda con las validaciones de la app. */
    fun confirmarPropuesta(mensajeId: Long) {
        val mensaje = _uiState.value.mensajes.firstOrNull { it.id == mensajeId } ?: return
        val propuesta = mensaje.propuesta ?: return
        if (mensaje.estadoPropuesta != EstadoPropuesta.PENDIENTE) return

        actualizar(mensajeId) { it.copy(estadoPropuesta = EstadoPropuesta.GUARDANDO, errorPropuesta = null) }
        viewModelScope.launch {
            try {
                guardador.guardar(propuesta)
                actualizar(mensajeId) { it.copy(estadoPropuesta = EstadoPropuesta.GUARDADA) }
            } catch (e: Exception) {
                val motivo = if (e is IllegalArgumentException) e.message else null
                actualizar(mensajeId) {
                    it.copy(
                        estadoPropuesta = EstadoPropuesta.PENDIENTE,
                        errorPropuesta = motivo ?: "No se pudo guardar. Revisa tu conexión e inténtalo de nuevo."
                    )
                }
            }
        }
    }

    fun cancelarPropuesta(mensajeId: Long) {
        actualizar(mensajeId) {
            if (it.estadoPropuesta == EstadoPropuesta.PENDIENTE)
                it.copy(estadoPropuesta = EstadoPropuesta.CANCELADA, errorPropuesta = null)
            else it
        }
    }

    private fun actualizar(mensajeId: Long, cambio: (Mensaje) -> Mensaje) {
        _uiState.update { estado ->
            estado.copy(mensajes = estado.mensajes.map { if (it.id == mensajeId) cambio(it) else it })
        }
    }

    companion object {
        val PREGUNTAS_SUGERIDAS = listOf(
            "¿Cuánto gasté esta semana?",
            "¿En qué gasté más este mes?",
            "¿Cuánto tengo en mis cuentas?",
            "Registra un gasto de S/ 15 en comida"
        )

        /**
         * Historial que se envía a la IA. A los mensajes con propuesta se les añade qué
         * decidió el usuario, para que no vuelva a proponer lo mismo ni diga que se guardó algo cancelado.
         */
        fun historialPara(mensajes: List<Mensaje>): List<MensajeHistorial> =
            mensajes.filterNot { it.esError }.map {
                val nota = if (it.propuesta == null) "" else when (it.estadoPropuesta) {
                    EstadoPropuesta.GUARDADA -> " [El usuario confirmó y quedó guardado.]"
                    EstadoPropuesta.CANCELADA -> " [El usuario canceló; no se guardó.]"
                    EstadoPropuesta.REEMPLAZADA -> " [Propuesta reemplazada; no se guardó.]"
                    EstadoPropuesta.PENDIENTE, EstadoPropuesta.GUARDANDO -> " [Pendiente de confirmar; aún no se guardó.]"
                }
                MensajeHistorial(
                    if (it.esUsuario) MensajeHistorial.USUARIO else MensajeHistorial.ASISTENTE,
                    it.texto + nota
                )
            }
    }
}
