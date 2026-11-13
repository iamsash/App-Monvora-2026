package com.example.appmovimientos.ui.screens.terminos

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TerminosCondicionesScreen(navController: NavController) {
    PantallaMonvora(titulo = "Términos y Condiciones", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icono central con círculo de fondo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .background(Color(0xFFEDE9FE), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Info,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Antes de continuar",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Para crear tu cuenta en MONVORA, es necesario que leas y aceptes nuestros términos y condiciones y nuestra política de privacidad.",
                fontSize = 14.sp,
                color = Monvora.LilaClaro,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Tarjeta 1: Términos y condiciones de uso
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("terminosUso") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Términos y condiciones de uso",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Última actualización:\n10 de octubre de 2026",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tarjeta 2: Política de privacidad
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { navController.navigate("politicaPrivacidad") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Política de privacidad",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "Última actualización:\n10 de octubre de 2026",
                            fontSize = 12.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }

                    Icon(
                        Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón Siguiente para ir a la aceptación explícita
            Button(
                onClick = { navController.navigate("aceptarCuenta") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                .background(Monvora.DegradadoReportes, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = coloresBotonDegradado()
            ) {
                Text("Continuar con la aceptación", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
