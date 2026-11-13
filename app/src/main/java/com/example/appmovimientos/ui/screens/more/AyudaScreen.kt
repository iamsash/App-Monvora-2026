package com.example.appmovimientos.ui.screens.more

import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora
import com.example.appmovimientos.ui.theme.coloresBotonDegradado
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AyudaScreen(navController: NavController) {
    PantallaMonvora(titulo = "Ayuda y acerca de", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Acerca de MONVORA", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E293B))
                    Text("Versión de la aplicación: 1.0.0 (Release)", fontSize = 14.sp, color = Color(0xFF64748B))
                    Text("MONVORA es tu asistente financiero personal diseñado para ayudarte a llevar el control total de tus ingresos, gastos, cuentas y reportes en tiempo real.", fontSize = 14.sp, color = Color(0xFF475569))

                    HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 8.dp))

                    Text("¿Necesitas soporte?", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1E293B))
                    Text("Si tienes dudas, sugerencias o experimentas algún problema con tu cuenta, contáctanos en dmaghimilyc@gmail.com", fontSize = 14.sp, color = Color(0xFF475569))
                }
            }
        }
    }
}
