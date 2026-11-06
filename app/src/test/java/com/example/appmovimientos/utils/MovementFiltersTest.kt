package com.example.appmovimientos.utils

import com.example.appmovimientos.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test
import java.time.LocalDate

class MovementFiltersTest {

    private val hoy = LocalDate.of(2026, 11, 6)

    private fun mov(id: String, fecha: String, monto: String = "10", tipo: String = "Gasto") =
        Expense(id = id, amount = monto, date = fecha, type = tipo)

    // --- Defecto 1: orden por fecha ---

    @Test
    fun `defecto 1 - ordenar la fecha como texto daba un orden incorrecto`() {
        val movimientos = listOf(mov("enero", "5/1/2025"), mov("diciembre", "20/12/2025"))
        // Comportamiento anterior: sortedByDescending { it.date } pone "5/1" antes que "20/12"
        val ordenAnterior = movimientos.sortedByDescending { it.date }.map { it.id }
        assertNotEquals(listOf("diciembre", "enero"), ordenAnterior)
        assertEquals(listOf("diciembre", "enero"), MovementFilters.ordenarPorFechaDesc(movimientos).map { it.id })
    }

    @Test
    fun `defecto 1 - ordena del mas reciente al mas antiguo segun la fecha real`() {
        val movimientos = listOf(
            mov("enero", "20/1/2025"),
            mov("diciembre", "5/12/2025"),
            mov("marzo2026", "1/3/2026"),
            mov("septiembre", "9/9/2025")
        )
        val orden = MovementFilters.ordenarPorFechaDesc(movimientos).map { it.id }
        assertEquals(listOf("marzo2026", "diciembre", "septiembre", "enero"), orden)
    }

    @Test
    fun `defecto 1 - los movimientos con fecha invalida quedan al final`() {
        val movimientos = listOf(mov("invalido", "Hoy"), mov("valido", "1/1/2025"))
        val orden = MovementFilters.ordenarPorFechaDesc(movimientos).map { it.id }
        assertEquals(listOf("valido", "invalido"), orden)
    }

    // --- Defecto 2: filtro por periodo ---

    @Test
    fun `defecto 2 - ultimos 3 meses incluye el mes actual y los dos anteriores`() {
        val movimientos = listOf(
            mov("nov", "1/11/2026"),
            mov("oct", "15/10/2026"),
            mov("sep", "1/9/2026"),
            mov("ago", "31/8/2026"),
            mov("anioPasado", "10/11/2025")
        )
        val ids = MovementFilters.filtrarPorPeriodo(movimientos, MovementFilters.ULTIMOS_3_MESES, hoy).map { it.id }
        assertEquals(listOf("nov", "oct", "sep"), ids)
    }

    @Test
    fun `defecto 2 - ultimos 3 meses funciona al cruzar de anio`() {
        val enero = LocalDate.of(2027, 1, 10)
        val movimientos = listOf(mov("nov", "5/11/2026"), mov("dic", "5/12/2026"), mov("oct", "5/10/2026"))
        val ids = MovementFilters.filtrarPorPeriodo(movimientos, MovementFilters.ULTIMOS_3_MESES, enero).map { it.id }
        assertEquals(listOf("nov", "dic"), ids)
    }

    @Test
    fun `defecto 2 - ya no depende de la cantidad de movimientos`() {
        // Antes "Últimos 3 Meses" era movimientos.take(100), sin mirar la fecha.
        val antiguos = (1..150).map { mov("viejo$it", "1/1/2020") }
        val resultado = MovementFilters.filtrarPorPeriodo(antiguos, MovementFilters.ULTIMOS_3_MESES, hoy)
        assertEquals(0, resultado.size)
    }

    @Test
    fun `mes actual solo incluye el mes en curso`() {
        val movimientos = listOf(mov("nov", "30/11/2026"), mov("oct", "31/10/2026"), mov("nov2025", "6/11/2025"))
        val ids = MovementFilters.filtrarPorPeriodo(movimientos, MovementFilters.MES_ACTUAL, hoy).map { it.id }
        assertEquals(listOf("nov"), ids)
    }

    @Test
    fun `ultimo anio incluye el anio calendario en curso`() {
        val movimientos = listOf(mov("ene", "1/1/2026"), mov("dic2025", "31/12/2025"))
        val ids = MovementFilters.filtrarPorPeriodo(movimientos, MovementFilters.ULTIMO_ANIO, hoy).map { it.id }
        assertEquals(listOf("ene"), ids)
    }

    @Test
    fun `todo no filtra nada, ni siquiera fechas invalidas`() {
        val movimientos = listOf(mov("a", "Hoy"), mov("b", "1/1/2020"))
        assertEquals(2, MovementFilters.filtrarPorPeriodo(movimientos, MovementFilters.TODO, hoy).size)
    }

    // --- Gráfico de gastos mensuales ---

    @Test
    fun `gastos por mes en orden cronologico y solo gastos`() {
        val movimientos = listOf(
            mov("1", "5/12/2025", "100"),
            mov("2", "9/9/2025", "50"),
            mov("3", "10/12/2025", "20,50"),
            mov("4", "1/1/2026", "30"),
            mov("5", "2/1/2026", "999", tipo = "Ingreso")
        )
        val resultado = MovementFilters.gastosPorMes(movimientos)
        assertEquals(listOf("9/2025", "12/2025", "1/2026"), resultado.keys.toList())
        assertEquals(120.5, resultado["12/2025"]!!, 0.0001)
        assertEquals(30.0, resultado["1/2026"]!!, 0.0001)
    }

    @Test
    fun `movimientos de un anio concreto`() {
        val movimientos = listOf(mov("a", "1/1/2025"), mov("b", "1/1/2026"))
        assertEquals(listOf("b"), MovementFilters.delAnio(movimientos, 2026).map { it.id })
    }
}
