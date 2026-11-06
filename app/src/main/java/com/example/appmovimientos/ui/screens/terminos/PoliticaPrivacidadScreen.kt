package com.example.appmovimientos.ui.screens.terminos

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
fun PoliticaPrivacidadScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Política de privacidad", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Política de Privacidad - MONVORA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
            Text(
                "Última actualización: 10 de octubre de 2026",
                fontSize = 12.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "1. Información que recopilamos\n" +
                        "Recopilamos información personal que usted nos proporciona al registrarse en MONVORA, como su nombre completo, correo electrónico, nombre de usuario y los datos financieros (cuentas, categorías y movimientos) que decida registrar en la aplicación.",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )

            Text(
                "2. Uso de la información\n" +
                        "La información recopilada se utiliza exclusivamente para proporcionar, mantener y mejorar nuestros servicios, autenticar su cuenta mediante Firebase Authentication y almacenar sus registros financieros en Firebase Realtime Database.",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )

            Text(
                "3. Protección de los datos\n" +
                        "Implementamos medidas de seguridad técnicas y organizativas adecuadas para proteger sus datos personales y financieros contra acceso no autorizado, alteración, divulgación o destrucción.",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )

            Text(
                "4. Proveedores de servicios (Firebase)\n" +
                        "Utilizamos Google Firebase como infraestructura de autenticación y base de datos en la nube. Los datos almacenados están sujetos a las políticas de seguridad y privacidad de Google.",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )

            Text(
                "5. Contacto\n" +
                        "Si tiene alguna duda sobre esta política de privacidad, puede ponerse en contacto con el equipo de soporte de MONVORA a través de la aplicación.",
                fontSize = 14.sp,
                color = Color(0xFF475569)
            )
        }
    }
}
