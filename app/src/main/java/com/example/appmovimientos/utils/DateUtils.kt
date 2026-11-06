package com.example.appmovimientos.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle

/**
 * Manejo centralizado de las fechas de los movimientos.
 *
 * Las fechas se guardan en Firebase como texto con el formato "d/M/yyyy"
 * (por ejemplo "5/1/2025" o "05/01/2025"). Compararlas como texto da
 * resultados incorrectos, por eso toda comparación debe pasar por [parse].
 */
object DateUtils {

    const val FORMATO_DMY = "DD/MM/AAAA"
    const val FORMATO_ISO = "AAAA-MM-DD"

    private val formatoAlmacenado: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d/M/uuuu").withResolverStyle(ResolverStyle.STRICT)

    private val formatoDmy: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/uuuu")
    private val formatoIso: DateTimeFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd")

    /** Convierte la fecha guardada en una fecha real, o null si no es válida. */
    fun parse(fecha: String?): LocalDate? {
        if (fecha.isNullOrBlank()) return null
        return try {
            LocalDate.parse(fecha.trim(), formatoAlmacenado)
        } catch (e: DateTimeParseException) {
            null
        }
    }

    /** Formato con el que se guardan las fechas (compatible con los datos existentes). */
    fun formatParaGuardar(fecha: LocalDate): String = formatoAlmacenado.format(fecha)

    /**
     * Fecha lista para mostrar según la preferencia del usuario
     * ([FORMATO_DMY] o [FORMATO_ISO]). Si la fecha guardada no se puede
     * interpretar, se muestra tal cual para no ocultar información.
     */
    fun formatParaMostrar(fecha: String, formato: String): String {
        val fechaReal = parse(fecha) ?: return fecha
        return when (formato) {
            FORMATO_ISO -> formatoIso.format(fechaReal)
            else -> formatoDmy.format(fechaReal)
        }
    }
}
