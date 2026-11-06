package com.example.appmovimientos.ui.screens.more

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.appmovimientos.data.preferences.AppPreferences
import com.example.appmovimientos.data.preferences.AppSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = AppPreferences.getInstance(application)

    data class ConfiguracionUiState(
        val notificaciones: Boolean = true,
        val modoOscuro: Boolean = false,
        val moneda: String = AppSettings.MONEDA_PEN,
        val formatoFecha: String = "DD/MM/AAAA"
    )

    private val _uiState = MutableStateFlow(
        ConfiguracionUiState(
            notificaciones = prefs.notificaciones,
            modoOscuro = prefs.modoOscuro,
            moneda = prefs.moneda,
            formatoFecha = prefs.formatoFecha
        )
    )
    val uiState: StateFlow<ConfiguracionUiState> = _uiState.asStateFlow()

    fun setNotificaciones(enabled: Boolean) {
        prefs.notificaciones = enabled
        _uiState.update { it.copy(notificaciones = enabled) }
    }

    fun setModoOscuro(enabled: Boolean) {
        prefs.modoOscuro = enabled
        _uiState.update { it.copy(modoOscuro = enabled) }
    }

    fun setMoneda(moneda: String) {
        prefs.moneda = moneda
        _uiState.update { it.copy(moneda = moneda) }
    }

    fun setFormatoFecha(formato: String) {
        prefs.formatoFecha = formato
        _uiState.update { it.copy(formatoFecha = formato) }
    }
}
