package com.example.appmovimientos.data.preferences

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import com.example.appmovimientos.utils.DateUtils
import com.example.appmovimientos.utils.MoneyUtils

/** Preferencias de visualización que usan todas las pantallas. */
data class AppSettings(
    val moneda: String = MONEDA_PEN,
    val formatoFecha: String = FORMATO_FECHA_DEFECTO
) {
    companion object {
        const val MONEDA_PEN = "S/"
        const val MONEDA_USD = "$"
        const val FORMATO_FECHA_DEFECTO = DateUtils.FORMATO_DMY
    }
}

/** Preferencias vigentes; se proveen en MainActivity. */
val LocalAppSettings = compositionLocalOf { AppSettings() }

/** Monto con el símbolo de moneda elegido por el usuario, por ejemplo "S/ 12.50". */
@Composable
fun dinero(valor: Double, decimales: Int = 2): String =
    MoneyUtils.formatear(valor, LocalAppSettings.current.moneda, decimales)

/** Símbolo de la moneda elegida, para etiquetas como "Monto (S/)". */
@Composable
fun simboloMoneda(): String = LocalAppSettings.current.moneda

/** Fecha de un movimiento en el formato elegido por el usuario. */
@Composable
fun fechaVisible(fecha: String): String =
    DateUtils.formatParaMostrar(fecha, LocalAppSettings.current.formatoFecha)
