package com.example.appmovimientos.ui.screens.terminos

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
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
import com.example.appmovimientos.ui.screens.registro.RegistroUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AceptacionScreen(
    navController: NavController,
    backStackEntry: androidx.navigation.NavBackStackEntry
) {
    val parentEntry = remember(backStackEntry) {
        try {
            navController.getBackStackEntry("registroUsuario")
        } catch (e: Exception) {
            null
        }
    }
    val viewModel: RegistroUsuarioViewModel = if (parentEntry != null) {
        viewModel(parentEntry)
    } else {
        viewModel()
    }

    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var aceptoTerminos by remember { mutableStateOf(false) }
    var aceptoPrivacidad by remember { mutableStateOf(false) }

    PantallaMonvora(titulo = "Aceptación de Términos", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(
                    "Confirmación final",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    "Para finalizar el registro en MONVORA, por favor marca las casillas para confirmar que has leído y aceptas nuestros términos y condiciones y nuestra política de privacidad.",
                    fontSize = 14.sp,
                    color = Monvora.LilaClaro
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
                        onCheckedChange = { aceptoTerminos = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF8B5CF6), uncheckedColor = Monvora.LilaClaro)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Acepto los Términos y Condiciones de uso",
                        fontSize = 14.sp,
                        color = Color.White
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
                        onCheckedChange = { aceptoPrivacidad = it },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF8B5CF6), uncheckedColor = Monvora.LilaClaro)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Acepto la Política de Privacidad",
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }

                uiState.errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = Color(0xFFF87171), fontSize = 14.sp)
                }
            }

            Button(
                onClick = {
                    viewModel.registrarUsuario {
                        Toast.makeText(context, "¡Cuenta creada exitosamente!", Toast.LENGTH_SHORT).show()
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                .background(Monvora.DegradadoReportes, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = coloresBotonDegradado(),
                enabled = aceptoTerminos && aceptoPrivacidad && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Aceptar y crear cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
