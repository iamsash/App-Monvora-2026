package com.example.appmovimientos.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Preferencias locales del usuario. Se usa una sola instancia en toda la
 * app ([getInstance]) para que un cambio en Configuración se refleje de
 * inmediato en el resto de pantallas a través de [settingsFlow].
 */
class AppPreferences private constructor(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("econote_prefs", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(
        AppSettings(moneda = leerMoneda(), formatoFecha = leerFormatoFecha())
    )
    /** Moneda y formato de fecha vigentes, para mostrarlos en las pantallas. */
    val settingsFlow: StateFlow<AppSettings> = _settingsFlow.asStateFlow()

    var notificaciones: Boolean
        get() = prefs.getBoolean("notificaciones", true)
        set(value) {
            prefs.edit().putBoolean("notificaciones", value).apply()
        }

    var modoOscuro: Boolean
        get() = prefs.getBoolean("modo_oscuro", false)
        set(value) {
            prefs.edit().putBoolean("modo_oscuro", value).apply()
        }

    var moneda: String
        get() = leerMoneda()
        set(value) {
            prefs.edit().putString("moneda", value).apply()
            _settingsFlow.update { it.copy(moneda = normalizarMoneda(value)) }
        }

    var formatoFecha: String
        get() = leerFormatoFecha()
        set(value) {
            prefs.edit().putString("formato_fecha", value).apply()
            _settingsFlow.update { it.copy(formatoFecha = value) }
        }

    private fun leerMoneda(): String =
        normalizarMoneda(prefs.getString("moneda", AppSettings.MONEDA_PEN) ?: AppSettings.MONEDA_PEN)

    private fun leerFormatoFecha(): String =
        prefs.getString("formato_fecha", AppSettings.FORMATO_FECHA_DEFECTO) ?: AppSettings.FORMATO_FECHA_DEFECTO

    /** Versiones anteriores guardaban "S/."; el símbolo oficial del sol es "S/". */
    private fun normalizarMoneda(valor: String): String = if (valor == "S/.") AppSettings.MONEDA_PEN else valor

    companion object {
        @Volatile
        private var instance: AppPreferences? = null

        fun getInstance(context: Context): AppPreferences =
            instance ?: synchronized(this) {
                instance ?: AppPreferences(context).also { instance = it }
            }
    }
}
