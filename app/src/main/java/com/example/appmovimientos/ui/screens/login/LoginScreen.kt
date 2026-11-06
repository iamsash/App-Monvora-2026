package com.example.appmovimientos.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

@Composable
fun MonvoraLoginLogo() {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF60A5FA), // Light blue top-left
                        Color(0xFF9333EA), // Violet
                        Color(0xFFEC4899)  // Pink bottom-right
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(64.dp)) {
            val w = size.width
            val h = size.height

            val stroke = 7.dp.toPx()

            // Draw white M
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(w * 0.16f, h * 0.75f)
                lineTo(w * 0.16f, h * 0.35f)
                lineTo(w * 0.38f, h * 0.65f)
                lineTo(w * 0.60f, h * 0.35f)
            }
            drawPath(
                path = path,
                color = Color.White,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = stroke,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round,
                    join = androidx.compose.ui.graphics.StrokeJoin.Round
                )
            )

            // 3 cyan/light blue financial bars on the right
            val barW = 5.dp.toPx()
            val spacing = 3.dp.toPx()
            val startX = w * 0.67f
            val barHeights = listOf(h * 0.3f, h * 0.5f, h * 0.72f)

            barHeights.forEachIndexed { index, bh ->
                val bx = startX + index * (barW + spacing)
                val by = h * 0.8f - bh
                drawRect(
                    color = Color(0xFF38BDF8), // Bright cyan
                    topLeft = androidx.compose.ui.geometry.Offset(bx, by),
                    size = androidx.compose.ui.geometry.Size(barW, bh)
                )
            }
        }
    }
}

@Composable
fun TechFinancialBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF13092A), // Dark navy purple top
                        Color(0xFF0D061D), // Deep dark center
                        Color(0xFF190B36)  // Rich purple bottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Diagonal and polygonal grid lines
            drawLine(
                color = Color(0xFFC084FC).copy(alpha = 0.25f),
                start = androidx.compose.ui.geometry.Offset(0f, height * 0.2f),
                end = androidx.compose.ui.geometry.Offset(width * 0.7f, 0f),
                strokeWidth = 2f
            )
            drawLine(
                color = Color(0xFF9333EA).copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(width * 0.3f, height),
                end = androidx.compose.ui.geometry.Offset(width, height * 0.6f),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0xFFE879F9).copy(alpha = 0.2f),
                start = androidx.compose.ui.geometry.Offset(width, height * 0.2f),
                end = androidx.compose.ui.geometry.Offset(width * 0.4f, height * 0.8f),
                strokeWidth = 2f
            )

            // Draw subtle ascending financial bar charts on top right
            val barWidth = 18f
            val barSpacing = 12f
            val startX = width - 150f
            val startY = height * 0.25f
            val heights = listOf(40f, 65f, 95f, 130f, 175f)

            heights.forEachIndexed { index, h ->
                val x = startX + (index * (barWidth + barSpacing))
                val y = startY - h
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE879F9).copy(alpha = 0.3f),
                            Color(0xFF9333EA).copy(alpha = 0.15f)
                        )
                    ),
                    topLeft = androidx.compose.ui.geometry.Offset(x, y + 100f),
                    size = androidx.compose.ui.geometry.Size(barWidth, h)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var mostrarRecuperar by remember { mutableStateOf(false) }

    if (mostrarRecuperar) {
        RecuperarContrasenaDialog(
            correoInicial = uiState.correo.trim().takeIf { it.contains("@") } ?: "",
            onEnviar = { correo, onTerminado ->
                viewModel.recuperarContrasena(correo) { exito, mensaje ->
                    onTerminado()
                    Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
                    if (exito) mostrarRecuperar = false
                }
            },
            onCerrar = { mostrarRecuperar = false }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        TechFinancialBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Logo MONVORA ---
            MonvoraLoginLogo()

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "MONVORA",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(28.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    OutlinedTextField(
                        value = uiState.correo,
                        onValueChange = { viewModel.onCorreoChange(it) },
                        label = { Text("Correo electrónico o Usuario") },
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF6366F1)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = uiState.contrasena,
                        onValueChange = { viewModel.onContrasenaChange(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = Color(0xFF6366F1)) },
                        trailingIcon = {
                            IconButton(onClick = { viewModel.toggleMostrarContrasena() }) {
                                Icon(
                                    if (uiState.mostrarContrasena) Icons.Filled.Menu else Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF94A3B8)
                                )
                            }
                        },
                        visualTransformation = if (uiState.mostrarContrasena) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true
                    )

                    TextButton(
                        onClick = { mostrarRecuperar = true },
                        modifier = Modifier.align(Alignment.End),
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text("¿Olvidaste tu contraseña?", fontSize = 13.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Iniciar Sesión con degradado
                    Button(
                        onClick = {
                            viewModel.login(
                                onNavigateMain = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateTerminos = {
                                    navController.navigate("legacyAceptacion") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                            } else {
                                Text("Iniciar Sesión", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botón Continuar con Google
                    OutlinedButton(
                        onClick = {
                            viewModel.loginWithGoogle(
                                context = context,
                                onNavigateMain = {
                                    navController.navigate("main") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                },
                                onNavigateTerminos = {
                                    navController.navigate("legacyAceptacion") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFFE2E8F0))))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Google G symbol representation / Icon
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(Color(0xFFEA4335), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("G", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Continuar con Google", fontSize = 15.sp, color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                        }
                    }

                    uiState.errorMessage?.let {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(it, color = Color.Red, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        TextButton(onClick = { navController.navigate("registroUsuario") }) {
                            Text("¿No tienes cuenta? ", color = Color(0xFF64748B), fontSize = 14.sp)
                            Text("Regístrate aquí", color = Color(0xFF7C3AED), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


/** Diálogo para pedir el enlace de restablecimiento de contraseña. */
@Composable
private fun RecuperarContrasenaDialog(
    correoInicial: String,
    onEnviar: (correo: String, onTerminado: () -> Unit) -> Unit,
    onCerrar: () -> Unit
) {
    var correo by remember { mutableStateOf(correoInicial) }
    var enviando by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { if (!enviando) onCerrar() },
        title = { Text("Recuperar contraseña", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    "Te enviaremos un enlace a tu correo para crear una nueva contraseña.",
                    fontSize = 14.sp,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null, tint = Color(0xFF6366F1)) },
                    singleLine = true,
                    enabled = !enviando,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    enviando = true
                    onEnviar(correo) { enviando = false }
                },
                enabled = !enviando && correo.isNotBlank()
            ) {
                if (enviando) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Enviar enlace")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !enviando) {
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}
