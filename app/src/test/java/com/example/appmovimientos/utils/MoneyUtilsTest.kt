package com.example.appmovimientos.utils

import com.example.appmovimientos.data.model.Expense
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MoneyUtilsTest {

    private val delta = 0.0001

    // --- Defecto 3: montos con coma decimal ---

    @Test
    fun `defecto 3 - el calculo anterior ignoraba montos con coma`() {
        // Así sumaban el dashboard y los reportes antes de la corrección.
        assertNull("12,50".toDoubleOrNull())
    }

    @Test
    fun `defecto 3 - interpreta coma y punto decimal por igual`() {
        assertEquals(12.5, MoneyUtils.parseMonto("12,50")!!, delta)
        assertEquals(12.5, MoneyUtils.parseMonto("12.50")!!, delta)
        assertEquals(12.5, MoneyUtils.parseMonto(" 12,5 ")!!, delta)
        assertEquals(0.5, MoneyUtils.parseMonto(",5")!!, delta)
    }

    @Test
    fun `defecto 3 - las sumas incluyen montos con coma`() {
        val movimientos = listOf(
            Expense(amount = "10,50", type = "Gasto"),
            Expense(amount = "4.50", type = "Gasto")
        )
        assertEquals(15.0, movimientos.sumOf { MoneyUtils.montoDe(it) }, delta)
    }

    @Test
    fun `rechaza textos que no son un numero`() {
        assertNull(MoneyUtils.parseMonto(null))
        assertNull(MoneyUtils.parseMonto(""))
        assertNull(MoneyUtils.parseMonto("abc"))
        assertNull(MoneyUtils.parseMonto("1.234,50"))
        assertNull(MoneyUtils.parseMonto("1,2,3"))
    }

    @Test
    fun `un monto guardado ilegible cuenta como cero en las sumas`() {
        assertEquals(0.0, MoneyUtils.montoDe(Expense(amount = "abc")), delta)
    }

    // --- Defecto 8: validación de lo que escribe el usuario ---

    @Test
    fun `defecto 8 - monto de movimiento valido`() {
        assertEquals(25.9, MoneyUtils.validarMontoMovimiento("25,90")!!, delta)
        assertEquals(100.0, MoneyUtils.validarMontoMovimiento("100")!!, delta)
    }

    @Test
    fun `defecto 8 - monto de movimiento debe ser mayor que cero`() {
        assertNull(MoneyUtils.validarMontoMovimiento("0"))
        assertNull(MoneyUtils.validarMontoMovimiento("-5"))
    }

    @Test
    fun `defecto 8 - monto de movimiento con mas de 2 decimales o invalido se rechaza`() {
        assertNull(MoneyUtils.validarMontoMovimiento("10.999"))
        assertNull(MoneyUtils.validarMontoMovimiento("diez"))
        assertNull(MoneyUtils.validarMontoMovimiento("   "))
    }

    @Test
    fun `defecto 8 - saldo invalido ya no se convierte en cero`() {
        // Antes: "abc".toDoubleOrNull() ?: 0.0 guardaba una cuenta con saldo 0 sin avisar.
        assertNull(MoneyUtils.validarSaldo("abc"))
        assertNull(MoneyUtils.validarSaldo(""))
    }

    @Test
    fun `defecto 8 - saldo admite cero, coma decimal y sobregiro`() {
        assertEquals(0.0, MoneyUtils.validarSaldo("0")!!, delta)
        assertEquals(1500.75, MoneyUtils.validarSaldo("1500,75")!!, delta)
        assertEquals(-20.0, MoneyUtils.validarSaldo("-20")!!, delta)
    }

    // --- Defecto 4: cálculo del cambio de saldo ---

    @Test
    fun `defecto 4 - un gasto resta y un ingreso suma`() {
        assertEquals(-30.0, MoneyUtils.deltaSaldo("Gasto", 30.0), delta)
        assertEquals(30.0, MoneyUtils.deltaSaldo("Ingreso", 30.0), delta)
    }

    @Test
    fun `defecto 4 - eliminar un movimiento revierte su efecto en el saldo`() {
        assertEquals(30.0, MoneyUtils.deltaSaldo("Gasto", 30.0, revertir = true), delta)
        assertEquals(-30.0, MoneyUtils.deltaSaldo("Ingreso", 30.0, revertir = true), delta)
    }

    // --- Formato ---

    @Test
    fun `normaliza el monto con punto decimal para guardarlo`() {
        assertEquals("12.50", MoneyUtils.normalizar(12.5))
    }

    @Test
    fun `defecto 6 - formatea con el simbolo de moneda elegido`() {
        assertEquals("S/ 12.50", MoneyUtils.formatear(12.5, "S/", locale = Locale.US))
        assertEquals("$ 12.50", MoneyUtils.formatear(12.5, "$", locale = Locale.US))
        assertEquals("S/ 13", MoneyUtils.formatear(12.5, "S/", decimales = 0, locale = Locale.US))
    }
}
