package com.example.appmovimientos.ui.screens.terminos

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosUsoScreen(navController: NavController) {
    PantallaMonvora(titulo = "Términos y condiciones de uso", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Términos y Condiciones de Uso - MONVORA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                "Última actualización: 10 de octubre de 2026",
                fontSize = 12.sp,
                color = Monvora.LilaClaro
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "1. Aceptación de los términos\n" +
                        "Al acceder y utilizar la aplicación MONVORA, usted acepta cumplir y estar sujeto a los presentes términos y condiciones de uso. Si no está de acuerdo con alguna parte de estos términos, deberá abstenerse de utilizar la aplicación.",
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0)
            )

            Text(
                "2. Descripción del servicio\n" +
                        "MONVORA es una aplicación de gestión de finanzas personales diseñada para ayudar a los usuarios a registrar ingresos, gastos, administrar cuentas bancarias y visualizar reportes financieros. La aplicación almacena la información de manera segura utilizando servicios en la nube de Firebase.",
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0)
            )

            Text(
                "3. Registro de cuenta y seguridad\n" +
                        "Para utilizar MONVORA el usuario debe crear una cuenta proporcionando información veraz y actualizada. El usuario es responsable de mantener la confidencialidad de su contraseña y de todas las actividades que ocurran bajo su cuenta.",
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0)
            )

            Text(
                "4. Privacidad de los datos financieros\n" +
                        "Sus datos financieros son estrictamente personales. MONVORA no comparte ni vende información financiera a terceros con fines comerciales.",
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0)
            )

            Text(
                "5. Modificaciones de los términos\n" +
                        "Nos reservamos el derecho de modificar estos términos en cualquier momento. Las modificaciones entrarán en vigor a partir de su publicación en la aplicación.",
                fontSize = 14.sp,
                color = Color(0xFFE2E8F0)
            )
        }
    }
}
