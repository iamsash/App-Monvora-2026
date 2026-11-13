package com.example.appmovimientos.ui.screens.more

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CambiarPasswordScreen(
    navController: NavController,
    viewModel: CambiarPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    PantallaMonvora(titulo = "Cambiar contraseña", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        "Actualiza tu contraseña para mantener segura tu cuenta",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Contraseña actual
                    OutlinedTextField(
                        value = uiState.currentPass,
                        onValueChange = { viewModel.onCurrentPassChange(it) },
                        label = { Text("Contraseña actual") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleShowCurrent() }) {
                                Icon(
                                    if (uiState.showCurrent) Icons.Filled.Done else Icons.Filled.Lock,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (uiState.showCurrent) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Nueva contraseña
                    OutlinedTextField(
                        value = uiState.newPass,
                        onValueChange = { viewModel.onNewPassChange(it) },
                        label = { Text("Nueva contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleShowNew() }) {
                                Icon(
                                    if (uiState.showNew) Icons.Filled.Done else Icons.Filled.Lock,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (uiState.showNew) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    // Confirmar nueva contraseña
                    OutlinedTextField(
                        value = uiState.confirmPass,
                        onValueChange = { viewModel.onConfirmPassChange(it) },
                        label = { Text("Confirmar nueva contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleShowConfirm() }) {
                                Icon(
                                    if (uiState.showConfirm) Icons.Filled.Done else Icons.Filled.Lock,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (uiState.showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            viewModel.changePassword {
                                Toast.makeText(context, "Contraseña cambiada correctamente", Toast.LENGTH_LONG).show()
                                navController.popBackStack()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                        .background(Monvora.DegradadoReportes, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = coloresBotonDegradado(),
                        enabled = !uiState.isLoading
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Cambiar contraseña", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    uiState.errorMessage?.let {
                        Text(it, color = Color.Red, fontSize = 13.sp)
                    }

                    uiState.successMessage?.let {
                        Text(it, color = Color(0xFF10B981), fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
