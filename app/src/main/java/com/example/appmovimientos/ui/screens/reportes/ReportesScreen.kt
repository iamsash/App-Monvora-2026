package com.example.appmovimientos.ui.screens.reportes

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmovimientos.utils.MoneyUtils
import androidx.navigation.NavController
import com.example.appmovimientos.ui.components.AnimatedButton
import com.example.appmovimientos.ui.theme.AppColors
import com.example.appmovimientos.utils.PdfReportGenerator
import com.example.appmovimientos.utils.ReportShareUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportesScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0728), Color(0xFF1E103F))
                    )
                )
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AnimatedButton(
                "Gastos e ingresos por Categoría",
                Icons.Filled.CheckCircle,
                AppColors.PrimaryGradient,
                { navController.navigate("reporteCategoria") },
                Modifier.fillMaxWidth()
            )
            AnimatedButton(
                "Movimientos por Mes",
                Icons.Filled.DateRange,
                AppColors.SecondaryGradient,
                { navController.navigate("reporteMes") },
                Modifier.fillMaxWidth()
            )
            AnimatedButton(
                "Reporte Total Mensual",
                Icons.Filled.Star,
                AppColors.CategoryGradient,
                { navController.navigate("reporteTotal") },
                Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DonutChart(categoryStats: List<Triple<String, Double, Double>>, colors: List<Color>) {
    val total = categoryStats.sumOf { it.second }
    Box(modifier = Modifier.size(85.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(85.dp)) {
            val strokeWidth = 14f
            var startAngle = -90f
            if (total > 0) {
                categoryStats.forEachIndexed { index, stat ->
                    val sweepAngle = ((stat.second / total) * 360).toFloat()
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round)
                    )
                    startAngle += sweepAngle
                }
            } else {
                drawArc(
                    color = Color(0xFFE2E8F0),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("100%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
            Text("Gastos", fontSize = 8.sp, color = Color(0xFF64748B))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteCategoriaScreen(
    navController: NavController,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val movimientosFiltrados = viewModel.getFilteredMovimientos().filter { it.type == "Gasto" }
    val categoriasMap = uiState.categorias.associateBy { it.id }

    var expandedPeriodo by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    val gastosPorCategoria = mutableMapOf<String, Double>()
    for (mov in movimientosFiltrados) {
        val monto = MoneyUtils.montoDe(mov)
        gastosPorCategoria[mov.categoryId] = (gastosPorCategoria[mov.categoryId] ?: 0.0) + monto
    }

    val totalGastos = gastosPorCategoria.values.sum()

    val categoryColors = listOf(
        Color(0xFFF43F5E), Color(0xFF3B82F6), Color(0xFF8B5CF6), Color(0xFFF97316), Color(0xFF64748B)
    )

    val categoryStats = gastosPorCategoria.entries.map { entry ->
        val cat = categoriasMap[entry.key]
        val percentage = if (totalGastos > 0) (entry.value / totalGastos) * 100 else 0.0
        Triple(cat?.name ?: "Sin categoría", entry.value, percentage)
    }.let { list ->
        if (uiState.sortOrder == "Mayor a menor") {
            list.sortedByDescending { it.second }
        } else {
            list.sortedBy { it.second }
        }
    }

    val topCategory = categoryStats.maxByOrNull { it.second }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reporte por Categoría", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Visualiza el detalle de tus gastos por categoría", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    val context = LocalContext.current
                    var expandedExport by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { expandedExport = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedExport,
                                onDismissRequest = { expandedExport = false },
                                modifier = Modifier.background(Color(0xFF1E103F))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Descargar PDF", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val file = PdfReportGenerator.generateCategoryReportPdf(context, uiState.selectedPeriod, totalGastos, categoryStats)
                                        if (file != null) {
                                            ReportShareUtils.openPdfFile(context, file)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Enviar por WhatsApp", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val file = PdfReportGenerator.generateCategoryReportPdf(context, uiState.selectedPeriod, totalGastos, categoryStats)
                                        if (file != null) {
                                            ReportShareUtils.sharePdfViaWhatsApp(context, file, "Reporte por Categoría (${uiState.selectedPeriod})")
                                        }
                                    }
                                )
                            }
                        }

                        Surface(
                            onClick = { expandedPeriodo = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(uiState.selectedPeriod, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedPeriodo,
                                onDismissRequest = { expandedPeriodo = false }
                            ) {
                                listOf("Mes Actual", "Últimos 3 Meses", "Último Año", "Todo").forEach { periodo ->
                                    DropdownMenuItem(
                                        text = { Text(periodo) },
                                        onClick = {
                                            viewModel.setPeriod(periodo)
                                            expandedPeriodo = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0728), Color(0xFF1E103F), Color(0xFF13092A))
                    )
                )
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Total Gastos", fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("S/ %.2f".format(totalGastos), fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1E293B))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                        Text("+12.5% ", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                        Text("vs. periodo anterior", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    DonutChart(categoryStats = categoryStats, colors = categoryColors)

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        categoryStats.take(4).forEachIndexed { index, stat ->
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(8.dp)
                                                        .background(categoryColors[index % categoryColors.size], CircleShape)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(stat.first, fontSize = 11.sp, color = Color(0xFF475569), maxLines = 1)
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("%.1f%%".format(stat.third), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Detalle por Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                                    Surface(
                                        onClick = { expandedSort = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(uiState.sortOrder, fontSize = 12.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Medium)
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = expandedSort,
                                            onDismissRequest = { expandedSort = false }
                                        ) {
                                            listOf("Mayor a menor", "Menor a mayor").forEach { order ->
                                                DropdownMenuItem(
                                                    text = { Text(order) },
                                                    onClick = {
                                                        viewModel.setSortOrder(order)
                                                        expandedSort = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (categoryStats.isEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No hay gastos registrados en este periodo", color = Color.Gray, fontSize = 14.sp)
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                        categoryStats.forEachIndexed { index, stat ->
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(categoryColors[index % categoryColors.size].copy(alpha = 0.15f), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Filled.Star, contentDescription = null, tint = categoryColors[index % categoryColors.size], modifier = Modifier.size(18.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(stat.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("S/ %.2f".format(stat.second), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = categoryColors[index % categoryColors.size])
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text("%.1f%%".format(stat.third), fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction = (stat.third / 100).toFloat().coerceIn(0f, 1f))
                                                            .height(8.dp)
                                                            .background(categoryColors[index % categoryColors.size], RoundedCornerShape(4.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Evolución de Gastos por Categoría", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1f), maxLines = 1)
                                    Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFFF1F5F9)) {
                                        Row(modifier = Modifier.padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                            listOf("Mes", "Trimestre", "Año").forEach { tab ->
                                                val isSelected = uiState.selectedTab == tab
                                                Surface(
                                                    onClick = { viewModel.setTab(tab) },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = if (isSelected) Color(0xFF6366F1) else Color.Transparent
                                                ) {
                                                    Text(tab, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isSelected) Color.White else Color(0xFF64748B), modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(20.dp))

                                Box(
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (movimientosFiltrados.isEmpty()) {
                                        Text("No hay datos para graficar", color = Color.Gray, fontSize = 13.sp)
                                    } else {
                                        Row(
                                            modifier = Modifier.fillMaxSize(),
                                            horizontalArrangement = Arrangement.SpaceEvenly,
                                            verticalAlignment = Alignment.Bottom
                                        ) {
                                            val maxVal = if (totalGastos > 0) totalGastos else 1.0
                                            repeat(5) { i ->
                                                val alturaFrac = ((totalGastos * (0.4 + (i * 0.15))) / maxVal).toFloat().coerceIn(0.2f, 0.95f)
                                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                                    Box(
                                                        modifier = Modifier
                                                            .width(28.dp)
                                                            .height((alturaFrac * 100).dp)
                                                            .background(
                                                                Brush.verticalGradient(
                                                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFFEC4899))
                                                                ),
                                                                RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                            )
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text("P${i + 1}", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.size(44.dp).background(Color(0xFFEDE9FE), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Análisis del período", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val insightText = if (topCategory != null) {
                                            "La categoría ${topCategory.first} representa el %.1f%% de tus gastos totales este mes.".format(topCategory.third)
                                        } else {
                                            "Aún no hay suficientes movimientos registrados para generar un análisis."
                                        }
                                        Text(insightText, fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { navController.navigate("lista") },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE9FE)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text("Ver movimientos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

data class MesStat(val nombre: String, val monto: Double, val count: Int, val porcentaje: Double)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteMesScreen(
    navController: NavController,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val movimientos = uiState.movimientos

    var expandedAnio by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    val aniosDisponibles = listOf("2024", "2025", "2026")
    val anioSeleccionado = uiState.selectedYear

    val movimientosAnio = movimientos.filter { mov ->
        try {
            val partes = mov.date.split("/")
            if (partes.size == 3) partes[2] == anioSeleccionado else false
        } catch (e: Exception) { false }
    }

    val totalAnual = movimientosAnio.sumOf { MoneyUtils.montoDe(it) }
    val totalMovimientos = movimientosAnio.size
    val promedioMensual = if (totalAnual > 0) totalAnual / 12.0 else 0.0

    val movimientosPorMes = mutableMapOf<Int, Double>()
    val conteoPorMes = mutableMapOf<Int, Int>()
    for (i in 1..12) {
        movimientosPorMes[i] = 0.0
        conteoPorMes[i] = 0
    }

    for (mov in movimientosAnio) {
        try {
            val partes = mov.date.split("/")
            if (partes.size == 3) {
                val mes = partes[1].toInt()
                val monto = MoneyUtils.montoDe(mov)
                if (mes in 1..12) {
                    movimientosPorMes[mes] = movimientosPorMes[mes]!! + monto
                    conteoPorMes[mes] = conteoPorMes[mes]!! + 1
                }
            }
        } catch (e: Exception) {}
    }

    val nombresMeses = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")

    val mesStats = (1..12).map { mes ->
        val monto = movimientosPorMes[mes] ?: 0.0
        val count = conteoPorMes[mes] ?: 0
        val porcentaje = if (totalAnual > 0) (monto / totalAnual) * 100 else 0.0
        MesStat(nombresMeses[mes], monto, count, porcentaje)
    }.let { list ->
        if (uiState.sortOrderTotal == "Mayor a menor") {
            list.sortedByDescending { it.monto }
        } else {
            list.sortedBy { it.monto }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Movimientos por Mes", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Desglose de tus movimientos mensuales", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    val context = LocalContext.current
                    var expandedExport by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { expandedExport = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedExport,
                                onDismissRequest = { expandedExport = false },
                                modifier = Modifier.background(Color(0xFF1E103F))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Descargar PDF", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val statsList = mesStats.map { "${it.nombre}: ${it.count} movs, S/ ${it.monto} (${it.porcentaje}%)" }
                                        val file = PdfReportGenerator.generateMonthlyMovementsPdf(context, anioSeleccionado, totalAnual, totalMovimientos, promedioMensual, statsList)
                                        if (file != null) {
                                            ReportShareUtils.openPdfFile(context, file)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Enviar por WhatsApp", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val statsList = mesStats.map { "${it.nombre}: ${it.count} movs, S/ ${it.monto} (${it.porcentaje}%)" }
                                        val file = PdfReportGenerator.generateMonthlyMovementsPdf(context, anioSeleccionado, totalAnual, totalMovimientos, promedioMensual, statsList)
                                        if (file != null) {
                                            ReportShareUtils.sharePdfViaWhatsApp(context, file, "Movimientos por Mes ($anioSeleccionado)")
                                        }
                                    }
                                )
                            }
                        }

                        Surface(
                            onClick = { expandedAnio = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(anioSeleccionado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedAnio,
                                onDismissRequest = { expandedAnio = false }
                            ) {
                                aniosDisponibles.forEach { anio ->
                                    DropdownMenuItem(
                                        text = { Text(anio) },
                                        onClick = {
                                            viewModel.setSelectedYear(anio)
                                            expandedAnio = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0728), Color(0xFF1E103F), Color(0xFF13092A))
                    )
                )
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF10B981), Color(0xFF059669), Color(0xFF047857))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Total Movimientos ($anioSeleccionado)", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("S/ %.2f".format(totalAnual), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .size(50.dp)
                                                .background(Color.White.copy(alpha = 0.2f), shape = RoundedCornerShape(14.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text("Movimientos", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                            Text("$totalMovimientos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Column {
                                            Text("Promedio Mensual", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                                            Text("S/ %.2f".format(promedioMensual), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Detalle por Mes", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Surface(
                                onClick = { expandedSort = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(uiState.sortOrderTotal, color = Color.White, fontSize = 11.sp)
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                                DropdownMenu(
                                    expanded = expandedSort,
                                    onDismissRequest = { expandedSort = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Mayor a menor") },
                                        onClick = {
                                            viewModel.setSortOrderTotal("Mayor a menor")
                                            expandedSort = false
                                        }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Menor a mayor") },
                                        onClick = {
                                            viewModel.setSortOrderTotal("Menor a mayor")
                                            expandedSort = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    items(mesStats) { stat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E103F))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Box(
                                            modifier = Modifier
                                                .size(40.dp)
                                                .background(Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(10.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(20.dp))
                                        }
                                        Column {
                                            Text(stat.nombre, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                            Text("${stat.count} movimientos", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
                                        }
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("S/ %.2f".format(stat.monto), color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                        Text("%.1f%% del total".format(stat.porcentaje), color = Color(0xFF10B981), fontSize = 12.sp)
                                    }
                                }

                                // Progress bar
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .background(Color.White.copy(alpha = 0.1f), shape = RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(fraction = (stat.porcentaje / 100f).coerceIn(0.0, 1.0).toFloat())
                                            .height(6.dp)
                                            .background(
                                                Brush.horizontalGradient(
                                                    colors = listOf(Color(0xFF10B981), Color(0xFF3B82F6))
                                                ),
                                                shape = RoundedCornerShape(3.dp)
                                            )
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReporteTotalScreen(
    navController: NavController,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val movimientos = uiState.movimientos.filter { it.type == "Gasto" }

    var expandedAnio by remember { mutableStateOf(false) }
    var expandedSort by remember { mutableStateOf(false) }

    val aniosDisponibles = listOf("2024", "2025", "2026")
    val anioSeleccionado = uiState.selectedYear

    val movimientosAnio = movimientos.filter { mov ->
        try {
            val partes = mov.date.split("/")
            if (partes.size == 3) partes[2] == anioSeleccionado else false
        } catch (e: Exception) { false }
    }

    val totalAnual = movimientosAnio.sumOf { MoneyUtils.montoDe(it) }
    val totalMovimientos = movimientosAnio.size
    val promedioMensual = if (totalAnual > 0) totalAnual / 12.0 else 0.0

    val gastosPorMes = mutableMapOf<Int, Double>()
    for (i in 1..12) gastosPorMes[i] = 0.0

    for (mov in movimientosAnio) {
        try {
            val partes = mov.date.split("/")
            if (partes.size == 3) {
                val mes = partes[1].toInt()
                val monto = MoneyUtils.montoDe(mov)
                if (mes in 1..12) {
                    gastosPorMes[mes] = gastosPorMes[mes]!! + monto
                }
            }
        } catch (e: Exception) {}
    }

    val mesMasAlto = gastosPorMes.maxByOrNull { it.value }
    val nombresMeses = listOf("", "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
    val nombresMesesCortos = listOf("", "Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

    val mesMasAltoNombre = if ((mesMasAlto?.value ?: 0.0) > 0) nombresMeses[mesMasAlto!!.key] else "Ninguno"

    val mesStats = (1..12).map { mes ->
        val monto = gastosPorMes[mes] ?: 0.0
        val porcentaje = if (totalAnual > 0) (monto / totalAnual) * 100 else 0.0
        Triple(nombresMeses[mes], monto, porcentaje)
    }.let { list ->
        if (uiState.sortOrderTotal == "Mayor a menor") {
            list.sortedByDescending { it.second }
        } else {
            list.sortedBy { it.second }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reporte Total Mensual", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Visualiza el detalle de tus gastos mes a mes", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                actions = {
                    val context = LocalContext.current
                    var expandedExport by remember { mutableStateOf(false) }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { expandedExport = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.Share, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Exportar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedExport,
                                onDismissRequest = { expandedExport = false },
                                modifier = Modifier.background(Color(0xFF1E103F))
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Descargar PDF", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val statsList = mesStats.map { "${it.first}: S/ ${it.second} (${it.third}%)" }
                                        val file = PdfReportGenerator.generateTotalMonthlyPdf(context, anioSeleccionado, totalAnual, totalMovimientos, promedioMensual, statsList)
                                        if (file != null) {
                                            ReportShareUtils.openPdfFile(context, file)
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Enviar por WhatsApp", color = Color.White) },
                                    onClick = {
                                        expandedExport = false
                                        val statsList = mesStats.map { "${it.first}: S/ ${it.second} (${it.third}%)" }
                                        val file = PdfReportGenerator.generateTotalMonthlyPdf(context, anioSeleccionado, totalAnual, totalMovimientos, promedioMensual, statsList)
                                        if (file != null) {
                                            ReportShareUtils.sharePdfViaWhatsApp(context, file, "Reporte Total Mensual ($anioSeleccionado)")
                                        }
                                    }
                                )
                            }
                        }

                        Surface(
                            onClick = { expandedAnio = true },
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(anioSeleccionado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                            DropdownMenu(
                                expanded = expandedAnio,
                                onDismissRequest = { expandedAnio = false }
                            ) {
                                aniosDisponibles.forEach { anio ->
                                    DropdownMenuItem(
                                        text = { Text(anio) },
                                        onClick = {
                                            viewModel.setSelectedYear(anio)
                                            expandedAnio = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFF0F0728), Color(0xFF1E103F), Color(0xFF13092A))
                    )
                )
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899))
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text("Total Gastos", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text("S/ %.2f".format(totalAnual), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                                                Text("+18.3% ", color = Color(0xFF10B981), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                Text("vs. mes anterior", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(22.dp).background(Color(0xFFF43F5E), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("$totalMovimientos", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Movimientos", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(22.dp).background(Color(0xFF8B5CF6), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text("S/ %.0f".format(promedioMensual), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Promedio", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                                            }
                                        }

                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Column(modifier = Modifier.padding(10.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier.size(22.dp).background(Color(0xFF10B981), CircleShape),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                                    }
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text("S/ %.0f".format(mesMasAlto?.value ?: 0.0), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text("Más alto ($mesMasAltoNombre)", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, maxLines = 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Evolución de Gastos Mensuales", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                    Text("Año $anioSeleccionado", fontSize = 12.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val maxGastoAnual = gastosPorMes.values.maxOrNull() ?: 1.0

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxSize(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        (1..12).forEach { mes ->
                                            val monto = gastosPorMes[mes] ?: 0.0
                                            val alturaFrac = if (maxGastoAnual > 0) (monto / maxGastoAnual).toFloat() else 0f
                                            val isHighest = mesMasAlto?.key == mes && monto > 0

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                if (isHighest) {
                                                    Surface(
                                                        shape = RoundedCornerShape(8.dp),
                                                        color = Color.White,
                                                        shadowElevation = 4.dp
                                                    ) {
                                                        Text(
                                                            "S/ %.0f".format(monto),
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFF43F5E),
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .width(18.dp)
                                                        .height((alturaFrac * 90).coerceAtLeast(6f).dp)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(Color(0xFFF43F5E), Color(0xFFEC4899))
                                                            ),
                                                            RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                                        )
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    nombresMesesCortos[mes],
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Detalle por Mes", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))

                                    Surface(
                                        onClick = { expandedSort = true },
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFF1F5F9)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(uiState.sortOrderTotal, fontSize = 12.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.Medium)
                                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color(0xFF6366F1), modifier = Modifier.size(16.dp))
                                        }
                                        DropdownMenu(
                                            expanded = expandedSort,
                                            onDismissRequest = { expandedSort = false }
                                        ) {
                                            listOf("Mayor a menor", "Menor a mayor").forEach { order ->
                                                DropdownMenuItem(
                                                    text = { Text(order) },
                                                    onClick = {
                                                        viewModel.setSortOrderTotal(order)
                                                        expandedSort = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                if (totalAnual == 0.0) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("No hay gastos registrados en el año $anioSeleccionado", color = Color.Gray, fontSize = 14.sp)
                                    }
                                } else {
                                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                                        mesStats.forEach { stat ->
                                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(36.dp)
                                                                .background(Color(0xFFEDE9FE), CircleShape),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(18.dp))
                                                        }
                                                        Spacer(modifier = Modifier.width(10.dp))
                                                        Text(stat.first, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                                    }

                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Text("S/ %.2f".format(stat.second), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF43F5E))
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Text("%.1f%%".format(stat.third), fontSize = 13.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
                                                    }
                                                }

                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction = (stat.third / 100).toFloat().coerceIn(0f, 1f))
                                                            .height(8.dp)
                                                            .background(Color(0xFFF43F5E), RoundedCornerShape(4.dp))
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(20.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .background(Color(0xFFEDE9FE), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(24.dp))
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text("Análisis del período", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val insightText = if (totalAnual > 0) {
                                            "$mesMasAltoNombre fue el mes con mayor gasto, representando el %.1f%% del total anual.".format(mesMasAlto?.let { if (totalAnual > 0) (it.value / totalAnual) * 100 else 0.0 } ?: 0.0)
                                        } else {
                                            "Aún no hay suficientes movimientos de gasto registrados para este año."
                                        }
                                        Text(insightText, fontSize = 12.sp, color = Color(0xFF64748B))
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Button(
                                    onClick = { navController.navigate("lista") },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEDE9FE)),
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                                ) {
                                    Text("Ver movimientos", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C3AED))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF7C3AED), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }
                }
            }
        }
    }
}
