package com.example.appmovimientos.utils

import com.example.appmovimientos.data.model.Expense
import java.util.Locale

/**
 * Lectura, validación y formato de montos en un solo lugar.
 *
 * Los montos de los movimientos se guardan como texto; algunos usuarios
 * escriben coma decimal ("12,50"). Todas las sumas y cálculos de saldo
 * deben usar [parseMonto] o [montoDe] para obtener el mismo resultado.
 */
object MoneyUtils {

    /** Número con como máximo un separador decimal (punto o coma). */
    private val numeroLeniente = Regex("^-?(\\d+([.,]\\d*)?|[.,]\\d+)$")

    /** Número con como máximo 2 decimales, para lo que el usuario escribe. */
    private val numeroEntrada = Regex("^-?(\\d+([.,]\\d{1,2})?|[.,]\\d{1,2})$")

    /**
     * Interpreta un monto guardado. Acepta coma o punto decimal y espacios.
     * Devuelve null si el texto no es un número.
     */
    fun parseMonto(texto: String?): Double? {
        val limpio = limpiar(texto) ?: return null
        if (!numeroLeniente.matches(limpio)) return null
        return limpio.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    /** Monto de un movimiento para sumas; los montos ilegibles cuentan como 0. */
    fun montoDe(movimiento: Expense): Double = parseMonto(movimiento.amount) ?: 0.0

    /** Monto de un movimiento escrito por el usuario: mayor que 0 y con máximo 2 decimales. */
    fun validarMontoMovimiento(texto: String?): Double? {
        val valor = parseEntrada(texto) ?: return null
        return valor.takeIf { it > 0.0 }
    }

    /** Saldo de una cuenta escrito por el usuario: máximo 2 decimales; admite 0 y negativos (sobregiro). */
    fun validarSaldo(texto: String?): Double? = parseEntrada(texto)

    /** Texto normalizado con punto decimal para guardar en Firebase ("12.50"). */
    fun normalizar(valor: Double): String = String.format(Locale.US, "%.2f", valor)

    /** Cambio en el saldo de la cuenta al registrar (o revertir) un movimiento. */
    fun deltaSaldo(tipo: String, monto: Double, revertir: Boolean = false): Double {
        val delta = if (tipo == "Gasto") -monto else monto
        return if (revertir) -delta else delta
    }

    /** Monto con símbolo de moneda, por ejemplo "S/ 12.50". */
    fun formatear(
        valor: Double,
        simbolo: String,
        decimales: Int = 2,
        locale: Locale = Locale.getDefault()
    ): String = "$simbolo ${String.format(locale, "%.${decimales}f", valor)}"

    private fun parseEntrada(texto: String?): Double? {
        val limpio = limpiar(texto) ?: return null
        if (!numeroEntrada.matches(limpio)) return null
        return limpio.replace(',', '.').toDoubleOrNull()?.takeIf { it.isFinite() }
    }

    private fun limpiar(texto: String?): String? =
        texto?.trim()?.replace(" ", "")?.takeIf { it.isNotEmpty() }
}
