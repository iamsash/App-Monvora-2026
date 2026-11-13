package com.example.appmovimientos.data.repository

import com.example.appmovimientos.BuildConfig
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/** Mensaje anterior de la conversación que se envía como contexto. */
data class MensajeHistorial(val rol: String, val texto: String) {
    companion object {
        const val USUARIO = "usuario"
        const val ASISTENTE = "asistente"
    }
}

enum class TipoErrorAsistente { SESION, CUENTA_DESHABILITADA, LIMITE, PREGUNTA_INVALIDA, NO_DISPONIBLE, SIN_CONEXION }

sealed class ResultadoAsistente {
    data class Exito(
        val respuesta: String,
        val consultasRestantes: Int?,
        val propuesta: Propuesta? = null
    ) : ResultadoAsistente()
    data class Error(val tipo: TipoErrorAsistente, val mensaje: String, val consultasRestantes: Int? = null) : ResultadoAsistente()
}

/**
 * Formato de las peticiones y respuestas de la API del asistente.
 * Separado de la red para poder probarlo con pruebas unitarias.
 */
object AsistenteProtocolo {
    const val MAX_HISTORIAL = 6
    const val MAX_LARGO_PREGUNTA = 500

    const val MENSAJE_SIN_CONEXION =
        "No pude conectar con el asistente. Revisa tu conexión o que el servidor esté encendido."
    const val MENSAJE_NO_DISPONIBLE =
        "El asistente no está disponible en este momento. Inténtalo más tarde."

    fun cuerpoSolicitud(pregunta: String, historial: List<MensajeHistorial>): String {
        val arreglo = JSONArray()
        historial.takeLast(MAX_HISTORIAL).forEach {
            arreglo.put(JSONObject().put("rol", it.rol).put("texto", it.texto))
        }
        return JSONObject()
            .put("pregunta", pregunta)
            .put("historial", arreglo)
            .toString()
    }

    /** Convierte la respuesta HTTP de la API en un resultado para la pantalla. */
    fun interpretar(codigo: Int, cuerpo: String?): ResultadoAsistente {
        val json = try {
            if (cuerpo.isNullOrBlank()) null else JSONObject(cuerpo)
        } catch (e: Exception) {
            null
        }
        val restantes = json?.takeIf { it.has("consultasRestantes") && !it.isNull("consultasRestantes") }
            ?.optInt("consultasRestantes")
        val mensajeApi = json?.optString("mensaje")?.takeIf { it.isNotBlank() }

        if (codigo == 200) {
            val respuesta = json?.optString("respuesta")?.takeIf { it.isNotBlank() }
                ?: return ResultadoAsistente.Error(TipoErrorAsistente.NO_DISPONIBLE, MENSAJE_NO_DISPONIBLE, restantes)
            return ResultadoAsistente.Exito(respuesta, restantes, Propuesta.desdeJson(json?.optJSONObject("accion")))
        }

        val tipo = when (codigo) {
            401 -> TipoErrorAsistente.SESION
            403 -> TipoErrorAsistente.CUENTA_DESHABILITADA
            429 -> TipoErrorAsistente.LIMITE
            400 -> TipoErrorAsistente.PREGUNTA_INVALIDA
            else -> TipoErrorAsistente.NO_DISPONIBLE
        }
        return ResultadoAsistente.Error(tipo, mensajeApi ?: MENSAJE_NO_DISPONIBLE, restantes)
    }
}

/**
 * Habla con la API del asistente (MONVORA.Asistente.Api). Envía la pregunta junto con el
 * token de sesión de Firebase; la API comprueba quién es y responde solo con sus datos.
 */
class AsistenteRepository(
    private val baseUrl: String = BuildConfig.ASISTENTE_API_URL,
    private val obtenerToken: suspend () -> String? = {
        FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.await()?.token
    }
) {
    suspend fun preguntar(pregunta: String, historial: List<MensajeHistorial>): ResultadoAsistente =
        withContext(Dispatchers.IO) {
            val token = try {
                obtenerToken()
            } catch (e: Exception) {
                null
            } ?: return@withContext ResultadoAsistente.Error(
                TipoErrorAsistente.SESION, "Tu sesión expiró. Vuelve a iniciar sesión."
            )

            var conexion: HttpURLConnection? = null
            try {
                conexion = (URL(baseUrl.trimEnd('/') + "/api/asistente").openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000
                    readTimeout = 60_000 // la IA puede tardar unos segundos
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Authorization", "Bearer $token")
                }
                conexion.outputStream.use {
                    it.write(AsistenteProtocolo.cuerpoSolicitud(pregunta, historial).toByteArray(Charsets.UTF_8))
                }
                val codigo = conexion.responseCode
                val stream = if (codigo in 200..299) conexion.inputStream else conexion.errorStream
                val cuerpo = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }
                AsistenteProtocolo.interpretar(codigo, cuerpo)
            } catch (e: SocketTimeoutException) {
                ResultadoAsistente.Error(TipoErrorAsistente.SIN_CONEXION, "El asistente tardó demasiado en responder. Inténtalo de nuevo.")
            } catch (e: IOException) {
                ResultadoAsistente.Error(TipoErrorAsistente.SIN_CONEXION, AsistenteProtocolo.MENSAJE_SIN_CONEXION)
            } finally {
                conexion?.disconnect()
            }
        }
}
