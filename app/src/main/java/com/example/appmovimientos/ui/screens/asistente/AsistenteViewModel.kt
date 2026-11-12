package com.example.appmovimientos.ui.screens.asistente

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.appmovimientos.data.repository.AsistenteProtocolo
import com.example.appmovimientos.data.repository.AsistenteRepository
import com.example.appmovimientos.data.repository.MensajeHistorial
import com.example.appmovimientos.data.repository.ResultadoAsistente
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AsistenteViewModel(
    private val repository: AsistenteRepository = AsistenteRepository()
) : ViewModel() {

    data class Mensaje(
        val id: Long,
        val esUsuario: Boolean,
        val texto: String,
        val esError: Boolean = false
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

    /** Envía lo escrito en la caja de texto. */
    fun enviar() = enviarPregunta(_uiState.value.texto)

    /** Envía una pregunta (también se usa con las preguntas sugeridas). */
    fun enviarPregunta(pregunta: String) {
        val limpia = pregunta.trim().take(AsistenteProtocolo.MAX_LARGO_PREGUNTA)
        if (limpia.isEmpty() || _uiState.value.enviando) return

        // Contexto: los mensajes anteriores válidos (sin errores), antes de agregar la nueva pregunta
        val historial = _uiState.value.mensajes
            .filterNot { it.esError }
            .map {
                MensajeHistorial(
                    if (it.esUsuario) MensajeHistorial.USUARIO else MensajeHistorial.ASISTENTE,
                    it.texto
                )
            }

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
                    is ResultadoAsistente.Exito -> estado.copy(
                        mensajes = estado.mensajes + Mensaje(siguienteId++, esUsuario = false, texto = resultado.respuesta),
                        enviando = false,
                        consultasRestantes = resultado.consultasRestantes ?: estado.consultasRestantes
                    )
                    is ResultadoAsistente.Error -> estado.copy(
                        mensajes = estado.mensajes + Mensaje(siguienteId++, esUsuario = false, texto = resultado.mensaje, esError = true),
                        enviando = false,
                        consultasRestantes = resultado.consultasRestantes ?: estado.consultasRestantes
                    )
                }
            }
        }
    }

    companion object {
        val PREGUNTAS_SUGERIDAS = listOf(
            "¿Cuánto gasté esta semana?",
            "¿En qué gasté más este mes?",
            "¿Cuánto tengo en mis cuentas?",
            "¿Cuál fue mi gasto más grande del mes?"
        )
    }
}
