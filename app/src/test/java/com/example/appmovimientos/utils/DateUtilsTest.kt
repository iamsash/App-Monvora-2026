package com.example.appmovimientos.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class DateUtilsTest {

    @Test
    fun `hoy usa la hora de Peru aunque el telefono este en UTC`() {
        // 12/11/2026 22:36 en Lima = 13/11/2026 03:36 en UTC
        val relojUtc = Clock.fixed(Instant.parse("2026-11-13T03:36:00Z"), ZoneOffset.UTC)
        assertEquals(LocalDate.of(2026, 11, 12), DateUtils.hoy(relojUtc))
        assertEquals("12/11/2026", DateUtils.formatParaGuardar(DateUtils.hoy(relojUtc)))
    }

    @Test
    fun `interpreta fechas sin ceros a la izquierda como las guarda la app`() {
        assertEquals(LocalDate.of(2025, 1, 5), DateUtils.parse("5/1/2025"))
    }

    @Test
    fun `interpreta fechas con ceros a la izquierda`() {
        assertEquals(LocalDate.of(2025, 1, 5), DateUtils.parse("05/01/2025"))
    }

    @Test
    fun `ignora espacios alrededor`() {
        assertEquals(LocalDate.of(2025, 12, 20), DateUtils.parse("  20/12/2025 "))
    }

    @Test
    fun `devuelve null para fechas invalidas o vacias`() {
        assertNull(DateUtils.parse(null))
        assertNull(DateUtils.parse(""))
        assertNull(DateUtils.parse("Hoy"))
        assertNull(DateUtils.parse("31/2/2025"))
        assertNull(DateUtils.parse("2025-01-05"))
    }

    @Test
    fun `guarda en el mismo formato que usa el selector de fecha`() {
        assertEquals("5/1/2025", DateUtils.formatParaGuardar(LocalDate.of(2025, 1, 5)))
    }

    @Test
    fun `muestra la fecha segun la preferencia del usuario`() {
        assertEquals("05/01/2025", DateUtils.formatParaMostrar("5/1/2025", DateUtils.FORMATO_DMY))
        assertEquals("2025-01-05", DateUtils.formatParaMostrar("5/1/2025", DateUtils.FORMATO_ISO))
    }

    @Test
    fun `muestra tal cual una fecha que no se puede interpretar`() {
        assertEquals("Hoy", DateUtils.formatParaMostrar("Hoy", DateUtils.FORMATO_ISO))
    }
}
