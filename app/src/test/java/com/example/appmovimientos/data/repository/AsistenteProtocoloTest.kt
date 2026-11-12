package com.example.appmovimientos.data.repository

import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AsistenteProtocoloTest {

    @Test
    fun `envia la pregunta y como maximo los ultimos 6 mensajes`() {
        val historial = (1..10).map {
            MensajeHistorial(if (it % 2 == 1) MensajeHistorial.USUARIO else MensajeHistorial.ASISTENTE, "m$it")
        }
        val json = JSONObject(AsistenteProtocolo.cuerpoSolicitud("¿Cuánto gasté?", historial))

        assertEquals("¿Cuánto gasté?", json.getString("pregunta"))
        val enviados = json.getJSONArray("historial")
        assertEquals(6, enviados.length())
        assertEquals("m5", enviados.getJSONObject(0).getString("texto"))
        assertEquals("asistente", enviados.getJSONObject(5).getString("rol"))
    }

    @Test
    fun `respuesta correcta con consultas restantes`() {
        val r = AsistenteProtocolo.interpretar(200, """{"respuesta":"Gastaste S/ 245.50","consultasRestantes":29}""")
        assertEquals(ResultadoAsistente.Exito("Gastaste S/ 245.50", 29), r)
    }

    @Test
    fun `limite diario alcanzado`() {
        val r = AsistenteProtocolo.interpretar(429, """{"mensaje":"Alcanzaste el límite de 30 consultas por hoy.","consultasRestantes":0}""")
        r as ResultadoAsistente.Error
        assertEquals(TipoErrorAsistente.LIMITE, r.tipo)
        assertEquals(0, r.consultasRestantes)
        assertTrue(r.mensaje.contains("límite"))
    }

    @Test
    fun `cada codigo de error se traduce a su tipo`() {
        val casos = mapOf(
            401 to TipoErrorAsistente.SESION,
            403 to TipoErrorAsistente.CUENTA_DESHABILITADA,
            400 to TipoErrorAsistente.PREGUNTA_INVALIDA,
            503 to TipoErrorAsistente.NO_DISPONIBLE,
            500 to TipoErrorAsistente.NO_DISPONIBLE
        )
        casos.forEach { (codigo, tipo) ->
            val r = AsistenteProtocolo.interpretar(codigo, """{"mensaje":"x"}""") as ResultadoAsistente.Error
            assertEquals("código $codigo", tipo, r.tipo)
        }
    }

    @Test
    fun `usa el mensaje de la API cuando lo hay`() {
        val r = AsistenteProtocolo.interpretar(403, """{"mensaje":"Tu cuenta está deshabilitada. Contacta con soporte."}""")
        assertEquals("Tu cuenta está deshabilitada. Contacta con soporte.", (r as ResultadoAsistente.Error).mensaje)
    }

    @Test
    fun `cuerpo vacio o invalido da un mensaje generico`() {
        listOf(null, "", "<html>Bad gateway</html>").forEach { cuerpo ->
            val r = AsistenteProtocolo.interpretar(502, cuerpo) as ResultadoAsistente.Error
            assertEquals(AsistenteProtocolo.MENSAJE_NO_DISPONIBLE, r.mensaje)
            assertNull(r.consultasRestantes)
        }
    }

    @Test
    fun `200 sin respuesta se trata como no disponible`() {
        val r = AsistenteProtocolo.interpretar(200, """{"respuesta":""}""")
        assertEquals(TipoErrorAsistente.NO_DISPONIBLE, (r as ResultadoAsistente.Error).tipo)
    }
}
