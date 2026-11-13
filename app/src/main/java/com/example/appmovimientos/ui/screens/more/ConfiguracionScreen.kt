package com.example.appmovimientos.ui.screens.more

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmovimientos.data.preferences.AppSettings
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracionScreen(
    navController: NavController,
    viewModel: ConfiguracionViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var expandedMoneda by remember { mutableStateOf(false) }
    var expandedFecha by remember { mutableStateOf(false) }

    val monedas = listOf(AppSettings.MONEDA_PEN, AppSettings.MONEDA_USD)
    val formatosFecha = listOf("DD/MM/AAAA", "AAAA-MM-DD")

    PantallaMonvora(titulo = "Configuración", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Grupo: Apariencia
            Text("Apariencia", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Monvora.Lila)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Modo oscuro", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text("Próximamente", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        // Deshabilitado hasta que las pantallas usen los colores del tema.
                        Switch(
                            checked = false,
                            onCheckedChange = null,
                            enabled = false
                        )
                    }
                }
            }

            // Grupo: Notificaciones
            Spacer(modifier = Modifier.height(4.dp))
            Text("Notificaciones", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Monvora.Lila)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Notificaciones push", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text("Recibe alertas de tus gastos y presupuestos", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Switch(
                            checked = uiState.notificaciones,
                            onCheckedChange = { viewModel.setNotificaciones(it) }
                        )
                    }
                }
            }

            // Grupo: Preferencias regionales
            Spacer(modifier = Modifier.height(4.dp))
            Text("Preferencias regionales", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Monvora.Lila)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Moneda
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Moneda de visualización", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text("Símbolo de los montos: S/ (PEN) o $ (USD). No convierte montos.", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Box {
                            OutlinedButton(onClick = { expandedMoneda = true }) {
                                Text(uiState.moneda)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedMoneda,
                                onDismissRequest = { expandedMoneda = false }
                            ) {
                                monedas.forEach { m ->
                                    DropdownMenuItem(
                                        text = { Text(m) },
                                        onClick = {
                                            viewModel.setMoneda(m)
                                            expandedMoneda = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Formato de fechas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Formato de fecha", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                            Text("Cómo se muestran las fechas", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        }
                        Box {
                            OutlinedButton(onClick = { expandedFecha = true }) {
                                Text(uiState.formatoFecha)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedFecha,
                                onDismissRequest = { expandedFecha = false }
                            ) {
                                formatosFecha.forEach { f ->
                                    DropdownMenuItem(
                                        text = { Text(f) },
                                        onClick = {
                                            viewModel.setFormatoFecha(f)
                                            expandedFecha = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
