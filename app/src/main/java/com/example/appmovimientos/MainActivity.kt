package com.example.appmovimientos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.appmovimientos.data.preferences.AppPreferences
import com.example.appmovimientos.data.preferences.LocalAppSettings
import com.example.appmovimientos.navigation.AppNavigation
import com.example.appmovimientos.ui.theme.AppMovimientosTheme
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        val preferencias = AppPreferences.getInstance(this)

        setContent {
            val ajustes by preferencias.settingsFlow.collectAsState()
            CompositionLocalProvider(LocalAppSettings provides ajustes) {
                // Las pantallas usan colores claros fijos; el modo oscuro aún no está
                // disponible, así que se fija el tema claro para que se vean coherentes.
                AppMovimientosTheme(darkTheme = false) {
                    AppNavigation()
                }
            }
        }
    }

}


