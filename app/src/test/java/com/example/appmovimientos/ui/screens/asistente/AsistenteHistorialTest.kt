package com.example.appmovimientos.ui.screens.asistente

import com.example.appmovimientos.data.repository.Propuesta
import com.example.appmovimientos.ui.screens.asistente.AsistenteViewModel.EstadoPropuesta
import com.example.appmovimientos.ui.screens.asistente.AsistenteViewModel.Mensaje
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AsistenteHistorialTest {

    private val categoria = Propuesta.Categoria("Mascotas", "Gasto")

    @Test
    fun `la IA sabe que decidio el usuario con cada propuesta`() {
        val mensajes = listOf(
            Mensaje(1, esUsuario = true, texto = "crea la categoría mascotas"),
            Mensaje(2, esUsuario = false, texto = "Confírmalo con Guardar", propuesta = categoria, estadoPropuesta = EstadoPropuesta.GUARDADA),
            Mensaje(3, esUsuario = false, texto = "Otra", propuesta = categoria, estadoPropuesta = EstadoPropuesta.CANCELADA),
            Mensaje(4, esUsuario = false, texto = "Error de red", esError = true),
            Mensaje(5, esUsuario = false, texto = "Pendiente", propuesta = categoria)
        )

        val historial = AsistenteViewModel.historialPara(mensajes)

        assertEquals(4, historial.size) // el error no se envía
        assertEquals("crea la categoría mascotas", historial[0].texto)
        assertTrue(historial[1].texto.endsWith("[El usuario confirmó y quedó guardado.]"))
        assertTrue(historial[2].texto.contains("canceló"))
        assertTrue(historial[3].texto.contains("aún no se guardó"))
    }

    @Test
    fun `resumen de la tarjeta de un gasto`() {
        val p = Propuesta.Movimiento("Gasto", 12.5, "Menú", "c1", "Efectivo", null, null, java.time.LocalDate.of(2026, 11, 12))
        val (titulo, filas) = resumenPropuesta(p)
        assertTrue(titulo.contains("gasto"))
        assertEquals("S/ 12.50", filas.first { it.first == "Monto" }.second)
        assertEquals("Sin categoría", filas.first { it.first == "Categoría" }.second)
        assertEquals("12/11/2026", filas.first { it.first == "Fecha" }.second)
    }
}
