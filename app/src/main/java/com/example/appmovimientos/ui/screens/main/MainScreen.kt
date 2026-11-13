package com.example.appmovimientos.ui.screens.main

import com.example.appmovimientos.ui.components.IconoCategoria
import com.example.appmovimientos.data.preferences.dinero
import com.example.appmovimientos.data.preferences.fechaVisible
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmovimientos.utils.MoneyUtils
import androidx.navigation.NavHostController
import com.example.appmovimientos.R
import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.ui.theme.AppColors
import com.example.appmovimientos.ui.components.IconoAsistente
import com.example.appmovimientos.ui.components.TarjetaAsistente
import kotlinx.coroutines.launch

@Composable
fun MonvoraLogo() {
    Box(
        modifier = Modifier
            .size(42.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF60A5FA), // Light blue top-left
                        Color(0xFF9333EA), // Violet
                        Color(0xFFEC4899)  // Pink bottom-right
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(30.dp)) {
            val w = size.width
            val h = size.height

            val stroke = 3.8.dp.toPx()

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
            val barW = 2.8.dp.toPx()
            val spacing = 2.dp.toPx()
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val cuentas = uiState.cuentas
    val movimientos = uiState.movimientos
    val balanceTotal = uiState.balanceTotal
    val gastoTotal = uiState.gastoTotal
    val ingresoTotal = uiState.ingresoTotal
    var cuentaSeleccionada by remember { mutableStateOf<Account?>(null) }

    val periodoSeleccionado = uiState.periodoSeleccionado
    var expandedPeriodo by remember { mutableStateOf(false) }
    val gastosMensuales = uiState.gastosMensuales
    val categoriasMasGastadas = uiState.categoriasMasGastadas
    val categorias = uiState.categorias
    val movimientosFiltrados = viewModel.getFilteredMovimientos()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = Color.White
            ) {
                EconoteDrawerContent(
                    currentRoute = "main",
                    onItemSelected = { route ->
                        scope.launch { drawerState.close() }
                        if (route != "main") {
                            navController.navigate(route)
                        }
                    },
                    onLogout = {
                        scope.launch { drawerState.close() }
                        viewModel.signOut {
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            MonvoraLogo()
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "MONVORA",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    "TUS FINANZAS, TU CAMINO",
                                    fontSize = 9.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Abrir menú", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate("perfil") }) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(Color(0xFF5B21B6), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = "Perfil", tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
                )
            },
            bottomBar = {
                EconoteBottomBar(
                    currentRoute = "main",
                    onTabSelected = { route ->
                        if (route != "main") {
                            navController.navigate(route) {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF0F0728), // Dark navy purple
                                Color(0xFF1E103F), // Rich purple
                                Color(0xFF13092A)  // Deep dark
                            )
                        )
                    )
                    .padding(padding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 📊 Card de Balance Total
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
                                            colors = listOf(
                                                Color(0xFF6366F1), // Indigo
                                                Color(0xFF8B5CF6), // Purple
                                                Color(0xFFEC4899)  // Pink
                                            )
                                        )
                                    )
                                    .padding(20.dp)
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            "Balance Total",
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Medium
                                        )

                                        // Selector de período estilo pastilla
                                        Surface(
                                            onClick = { expandedPeriodo = true },
                                            shape = RoundedCornerShape(20.dp),
                                            color = Color.White.copy(alpha = 0.2f)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(Icons.Filled.DateRange, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(periodoSeleccionado, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                Spacer(modifier = Modifier.width(4.dp))
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
                                                            viewModel.setPeriodo(periodo)
                                                            expandedPeriodo = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        dinero(balanceTotal),
                                        color = Color.White,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Mini Card Ingresos
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFF10B981), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("Ingresos", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                                    Text(dinero(ingresoTotal), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }

                                        // Mini Card Gastos
                                        Card(
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(14.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(32.dp)
                                                        .background(Color(0xFFEF4444), CircleShape),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                                }
                                                Spacer(modifier = Modifier.width(10.dp))
                                                Column {
                                                    Text("Gastos", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                                                    Text(dinero(gastoTotal), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // ✨ Asistente IA: acceso rápido al chat
                    item {
                        TarjetaAsistente(onClick = { navController.navigate("asistente") })
                    }

                    // 🆕 Top 5 Categorías con Más Gastos
                    if (categoriasMasGastadas.isNotEmpty()) {
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
                                        Text(
                                            "Top 5 Categorías",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1E293B)
                                        )
                                        TextButton(onClick = { navController.navigate("reporteCategoria") }) {
                                            Text("Ver todo →", fontSize = 13.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    categoriasMasGastadas.forEach { (categoriaId, monto) ->
                                        val categoria = categorias[categoriaId]
                                        val porcentaje = if (gastoTotal > 0) (monto / gastoTotal) * 100 else 0.0

                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            IconoCategoria(categoria, tamano = 40.dp)
                                            Spacer(modifier = Modifier.width(12.dp))

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    categoria?.name ?: "Sin categoría",
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1E293B)
                                                )
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(8.dp)
                                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(4.dp))
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .fillMaxWidth(fraction = (porcentaje / 100).toFloat().coerceIn(0f, 1f))
                                                            .height(8.dp)
                                                            .background(
                                                                Color(0xFFEC4899),
                                                                RoundedCornerShape(4.dp)
                                                            )
                                                    )
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(16.dp))

                                            Column(horizontalAlignment = Alignment.End) {
                                                Text(
                                                    dinero(monto),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFEF4444)
                                                )
                                                Text(
                                                    "%.1f%%".format(porcentaje),
                                                    fontSize = 11.sp,
                                                    color = Color(0xFF94A3B8)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 🆕 Gráfico de Tendencia Mensual
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
                                    Text(
                                        "Tendencia de Gastos",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    TextButton(onClick = { navController.navigate("reporteTotal") }) {
                                        Text("Ver detalle →", fontSize = 13.sp, color = Color(0xFF6366F1), fontWeight = FontWeight.SemiBold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                val ultimos6Meses = gastosMensuales.toList()
                                    .takeLast(6) // ya viene en orden cronológico

                                if (ultimos6Meses.isNotEmpty()) {
                                    val maxGasto = ultimos6Meses.maxOfOrNull { it.second } ?: 1.0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(130.dp),
                                        horizontalArrangement = Arrangement.SpaceEvenly,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        ultimos6Meses.forEach { (mesAnio, monto) ->
                                            val altura = ((monto / maxGasto) * 100).toFloat()
                                            val mes = mesAnio.split("/").firstOrNull() ?: ""
                                            val nombreMes = when(mes) {
                                                "1" -> "Ene"
                                                "2" -> "Feb"
                                                "3" -> "Mar"
                                                "4" -> "Abr"
                                                "5" -> "May"
                                                "6" -> "Jun"
                                                "7" -> "Jul"
                                                "8" -> "Ago"
                                                "9" -> "Sep"
                                                "10" -> "Oct"
                                                "11" -> "Nov"
                                                "12" -> "Dic"
                                                else -> mes
                                            }

                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(28.dp)
                                                        .height((altura * 0.9f).coerceAtLeast(10f).dp)
                                                        .background(
                                                            Brush.verticalGradient(
                                                                colors = listOf(Color(0xFFF43F5E), Color(0xFFEC4899))
                                                            ),
                                                            RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                                                        )
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                Text(
                                                    nombreMes,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    color = Color(0xFF64748B)
                                                )
                                            }
                                        }
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(100.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "No hay datos suficientes",
                                            fontSize = 14.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 📱 Botones de Acción Principales
                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Botón Registrar (Rosa)
                                Button(
                                    onClick = { navController.navigate("registro") },
                                    modifier = Modifier
                                        .weight(1f)
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
                                                    colors = listOf(Color(0xFFF43F5E), Color(0xFFEC4899))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Registrar", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                // Botón Categorías (Naranja pastel)
                                Button(
                                    onClick = { navController.navigate("categorias") },
                                    modifier = Modifier
                                        .weight(1f)
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
                                                    colors = listOf(Color(0xFFFBBF24), Color(0xFFF97316))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Menu, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Categorías", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Botón Ver Reportes Completos
                            Button(
                                onClick = { navController.navigate("reportes") },
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
                                                colors = listOf(Color(0xFF8B5CF6), Color(0xFFD946EF))
                                            )
                                        )
                                        .padding(horizontal = 20.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Ver Reportes Completos", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    // 💳 Sección de Cuentas
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Mis Cuentas (${cuentas.size})",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                TextButton(onClick = { navController.navigate("cuentas") }) {
                                    Text("Administrar", fontSize = 14.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (cuentas.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(24.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Filled.MailOutline,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("No hay cuentas", color = Color.Gray, fontSize = 16.sp)
                                            TextButton(onClick = { navController.navigate("nuevaCuenta") }) {
                                                Text("Crear primera cuenta")
                                            }
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    cuentas.take(3).forEach { cuenta ->
                                        CuentaCardCompacta(
                                            cuenta = cuenta,
                                            onClick = { cuentaSeleccionada = cuenta }
                                        )
                                    }
                                    if (cuentas.size > 3) {
                                        TextButton(
                                            onClick = { navController.navigate("cuentas") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Ver todas las cuentas (${cuentas.size})", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 📋 Movimientos Recientes
                    item {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Movimientos Recientes",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                                TextButton(onClick = { navController.navigate("lista") }) {
                                    Text("Ver todos →", fontSize = 13.sp, color = Color(0xFFA78BFA), fontWeight = FontWeight.SemiBold)
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            if (movimientosFiltrados.isEmpty()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                Icons.Filled.DateRange,
                                                contentDescription = null,
                                                modifier = Modifier.size(48.dp),
                                                tint = Color.Gray
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text("No hay movimientos", color = Color.Gray, fontSize = 16.sp)
                                        }
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    movimientosFiltrados.take(5).forEach { movimiento ->
                                        MovimientoCardCompacto(
                                            movimiento = movimiento,
                                            cuenta = cuentas.find { it.id == movimiento.accountId }
                                        )
                                    }
                                    if (movimientosFiltrados.size > 5) {
                                        TextButton(
                                            onClick = { navController.navigate("lista") },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Ver todos los movimientos (${movimientos.size} total)", color = Color.White)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    cuentaSeleccionada?.let { cuenta ->
        MovimientosPorCuentaDialog(
            cuenta = cuenta,
            movimientos = movimientos.filter { it.accountId == cuenta.id },
            onDismiss = { cuentaSeleccionada = null },
            onDeleteMovimiento = { movimiento ->
                viewModel.deleteMovement(
                    expense = movimiento,
                    onSuccess = {
                        Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

// 🧭 Barra de navegación inferior
@Composable
fun EconoteBottomBar(
    currentRoute: String,
    onTabSelected: (String) -> Unit
) {
    NavigationBar(
        containerColor = Color(0xFF0F0728),
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "main",
            onClick = { onTabSelected("main") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Inicio") },
            label = { Text("Inicio") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFC084FC),
                selectedTextColor = Color(0xFFC084FC),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFF1E103F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "lista",
            onClick = { onTabSelected("lista") },
            icon = { Icon(Icons.Filled.List, contentDescription = "Movimientos") },
            label = { Text("Movimientos") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFC084FC),
                selectedTextColor = Color(0xFFC084FC),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFF1E103F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "reportes",
            onClick = { onTabSelected("reportes") },
            icon = { Icon(Icons.Filled.Star, contentDescription = "Reportes") },
            label = { Text("Reportes") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFC084FC),
                selectedTextColor = Color(0xFFC084FC),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFF1E103F)
            )
        )
        NavigationBarItem(
            selected = currentRoute == "perfil",
            onClick = { onTabSelected("perfil") },
            icon = { Icon(Icons.Filled.Person, contentDescription = "Perfil") },
            label = { Text("Perfil") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFFC084FC),
                selectedTextColor = Color(0xFFC084FC),
                unselectedIconColor = Color(0xFF94A3B8),
                unselectedTextColor = Color(0xFF94A3B8),
                indicatorColor = Color(0xFF1E103F)
            )
        )
    }
}

// -------------------------------------------------------------
// Drawer & Cards Helpers
// -------------------------------------------------------------

@Composable
fun EconoteDrawerContent(
    currentRoute: String,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(Color.White)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFEDE9FE), Color(0xFFDDD6FE))
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "MONVORA",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E293B)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(12.dp))

        DrawerItem(
            icon = Icons.Filled.Home,
            label = "Inicio",
            isSelected = currentRoute == "main",
            onClick = { onItemSelected("main") }
        )
        DrawerItem(
            icon = Icons.Filled.List,
            label = "Movimientos",
            isSelected = currentRoute == "lista",
            onClick = { onItemSelected("lista") }
        )
        DrawerItem(
            icon = Icons.Filled.AccountBox,
            label = "Mis cuentas",
            isSelected = currentRoute == "cuentas",
            onClick = { onItemSelected("cuentas") }
        )
        DrawerItem(
            icon = Icons.Filled.Menu,
            label = "Categorías",
            isSelected = currentRoute == "categorias",
            onClick = { onItemSelected("categorias") }
        )
        DrawerItem(
            icon = Icons.Filled.Star,
            label = "Reportes y estadísticas",
            isSelected = currentRoute == "reportes",
            onClick = { onItemSelected("reportes") }
        )
        DrawerItem(
            icon = IconoAsistente,
            label = "Asistente IA",
            isSelected = currentRoute == "asistente",
            onClick = { onItemSelected("asistente") }
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "MI MONVORA",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        DrawerItem(
            icon = Icons.Filled.Person,
            label = "Mi perfil",
            isSelected = currentRoute == "perfil",
            onClick = { onItemSelected("perfil") }
        )
        DrawerItem(
            icon = Icons.Filled.Settings,
            label = "Configuración",
            isSelected = currentRoute == "configuracion",
            onClick = { onItemSelected("configuracion") }
        )
        DrawerItem(
            icon = Icons.Filled.Lock,
            label = "Privacidad y seguridad",
            isSelected = currentRoute == "privacidadSeguridad",
            onClick = { onItemSelected("privacidadSeguridad") }
        )
        DrawerItem(
            icon = Icons.Filled.Info,
            label = "Ayuda y acerca de",
            isSelected = currentRoute == "ayuda",
            onClick = { onItemSelected("ayuda") }
        )

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color(0xFFF1F5F9))
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem(
            icon = Icons.Filled.ExitToApp,
            label = "Cerrar sesión",
            isSelected = false,
            isDestructive = true,
            onClick = onLogout
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) Color(0xFFEDE9FE) else Color.Transparent
    val contentColor = if (isDestructive) Color(0xFFEF4444) else if (isSelected) Color(0xFF7C3AED) else Color(0xFF475569)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun CuentaCardCompacta(
    cuenta: Account,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
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
                    .size(44.dp)
                    .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.AccountBox,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    cuenta.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                if (cuenta.bank.isNotEmpty()) {
                    Text(
                        cuenta.bank,
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
            Text(
                dinero(cuenta.balance),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF10B981)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
    }
}

@Composable
fun MovimientoCardCompacto(
    movimiento: Expense,
    cuenta: Account?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
                    .size(44.dp)
                    .background(
                        if (movimiento.type == "Gasto") Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (movimiento.type == "Gasto") Icons.Filled.ArrowDropDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = if (movimiento.type == "Gasto") Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movimiento.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
                Text(
                    "${cuenta?.name ?: "Cuenta"} • ${fechaVisible(movimiento.date)}",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                )
            }

            Text(
                "${if (movimiento.type == "Gasto") "-" else "+"}${dinero(MoneyUtils.montoDe(movimiento))}",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (movimiento.type == "Gasto") Color(0xFFEF4444) else Color(0xFF10B981)
            )
        }
    }
}
// 🔹 Tarjeta de Cuenta Detallada
@Composable
fun CuentaCardDetallada(
    cuenta: Account,
    gastoTotal: Double,
    cantidadMovimientos: Int,
    onClick: () -> Unit
) {
    val accountGradient = Brush.linearGradient(
        colors = listOf(Color(0xFF8360c3), Color(0xFF2ebf91))
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .background(accountGradient)
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            cuenta.name,
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        if (cuenta.bank.isNotEmpty()) {
                            Text(
                                cuenta.bank,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.AccountBox,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Saldo disponible",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    dinero(cuenta.balance),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Gastado",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            dinero(gastoTotal),
                            color = Color(0xFFFFB74D),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Movimientos",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                        Text(
                            "$cantidadMovimientos",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
// 🔹 Tarjeta de Movimiento
@Composable
fun MovimientoCard(
    movimiento: Expense,
    cuenta: Account?,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
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
                    .background(
                        if (movimiento.type == "Gasto") Color(0xFFFEE2E2) else Color(0xFFECFDF5),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (movimiento.type == "Gasto") Icons.Filled.ArrowDropDown else Icons.Filled.KeyboardArrowUp,
                    contentDescription = null,
                    tint = if (movimiento.type == "Gasto") Color(0xFFEF4444) else Color(0xFF10B981),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    movimiento.description,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                Text(
                    cuenta?.name ?: "Cuenta desconocida",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
                Text(
                    fechaVisible(movimiento.date),
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (movimiento.type == "Gasto") "-" else "+"}${dinero(MoneyUtils.montoDe(movimiento))}",
                    color = if (movimiento.type == "Gasto") Color(0xFFEF4444) else Color(0xFF10B981),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Eliminar",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Movimiento") },
            text = { Text("¿Estás seguro de eliminar este movimiento?") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// 🔹 Diálogo de Movimientos por Cuenta
@Composable
fun MovimientosPorCuentaDialog(
    cuenta: Account,
    movimientos: List<Expense>,
    onDismiss: () -> Unit,
    onDeleteMovimiento: (Expense) -> Unit
) {
    val gastoTotal = movimientos
        .filter { it.type == "Gasto" }
        .sumOf { MoneyUtils.montoDe(it) }
    val ingresoTotal = movimientos
        .filter { it.type == "Ingreso" }
        .sumOf { MoneyUtils.montoDe(it) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(cuenta.name, fontWeight = FontWeight.Bold)
                Text(
                    "Saldo: ${dinero(cuenta.balance)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Ingresos", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                dinero(ingresoTotal),
                                color = Color(0xFF10B981),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Gastos", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                dinero(gastoTotal),
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Total", fontSize = 12.sp, color = Color.Gray)
                            Text(
                                "${movimientos.size}",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                (movimientos.isEmpty())
                if (movimientos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay movimientos", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(movimientos.size) { index ->
                            val mov = movimientos[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            mov.description,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                        Text(
                                            fechaVisible(mov.date),
                                            fontSize = 12.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                    Text(
                                        "${if (mov.type == "Gasto") "-" else "+"}${dinero(MoneyUtils.montoDe(mov))}",
                                        color = if (mov.type == "Gasto") Color(0xFFEF4444) else Color(0xFF10B981),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color(0xFFF8F9FF)
    )
}
