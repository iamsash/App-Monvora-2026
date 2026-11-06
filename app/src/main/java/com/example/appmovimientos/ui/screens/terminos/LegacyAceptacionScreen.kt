package com.example.appmovimientos.ui.screens.terminos

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegacyAceptacionScreen(
    navController: NavController,
    viewModel: LegacyAceptacionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var aceptoTerminos by remember { mutableStateOf(false) }
    var aceptoPrivacidad by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Actualización de Términos", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
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
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    "Términos actualizados requeridos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )

                Text(
                    "Hemos actualizado nuestros términos y condiciones y nuestra política de privacidad. Para continuar usando MONVORA, por favor lee y acepta las casillas correspondientes.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { aceptoTerminos = !aceptoTerminos },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptoTerminos,
                        onCheckedChange = { aceptoTerminos = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Acepto los Términos y Condiciones de uso",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { aceptoPrivacidad = !aceptoPrivacidad },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = aceptoPrivacidad,
                        onCheckedChange = { aceptoPrivacidad = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Acepto la Política de Privacidad",
                        fontSize = 14.sp,
                        color = Color(0xFF1E293B)
                    )
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color.Red, fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    viewModel.aceptarTerminos(
                        onSuccess = {
                            Toast.makeText(context, "¡Términos aceptados correctamente!", Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                enabled = aceptoTerminos && aceptoPrivacidad && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Aceptar y continuar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
