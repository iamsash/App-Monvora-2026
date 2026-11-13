package com.example.appmovimientos.ui.screens.movimientos

import com.example.appmovimientos.ui.components.IconoCategoria
import com.example.appmovimientos.data.preferences.dinero
import com.example.appmovimientos.data.preferences.simboloMoneda
import com.example.appmovimientos.data.preferences.fechaVisible
import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.appmovimientos.utils.MoneyUtils
import androidx.navigation.NavController
import com.example.appmovimientos.data.model.Account
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.data.model.Expense
import com.example.appmovimientos.ui.theme.AppColors
import kotlinx.coroutines.delay
import com.example.appmovimientos.utils.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    // Hoy en Perú, sin depender de la zona horaria configurada en el teléfono
    val hoy = remember { DateUtils.hoy() }

    var monto by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var fecha by remember { mutableStateOf(DateUtils.formatParaGuardar(hoy)) }
    var tipo by remember { mutableStateOf("Gasto") }
    var categoriaSeleccionada by remember { mutableStateOf<Category?>(null) }
    var cuentaSeleccionada by remember { mutableStateOf<Account?>(null) }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedCategoria by remember { mutableStateOf(false) }
    var expandedCuenta by remember { mutableStateOf(false) }
    var showSuccessAnimation by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val categorias = uiState.categorias
    val cuentas = uiState.cuentas

    val categoriasFiltradas = remember(categorias, tipo) {
        categorias.filter { it.type == tipo }
    }

    LaunchedEffect(categoriasFiltradas, tipo) {
        if (categoriasFiltradas.isNotEmpty()) {
            if (categoriaSeleccionada == null || categoriaSeleccionada?.type != tipo) {
                categoriaSeleccionada = categoriasFiltradas.first()
            }
        } else {
            categoriaSeleccionada = null
        }
    }

    LaunchedEffect(cuentas) {
        if (cuentas.isNotEmpty() && cuentaSeleccionada == null) {
            cuentaSeleccionada = cuentas.first()
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> fecha = "$day/${month + 1}/$year" },
        hoy.year,
        hoy.monthValue - 1, // DatePickerDialog cuenta los meses desde 0
        hoy.dayOfMonth
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Movimiento", fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(32.dp)) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            onClick = { expandedTipo = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                if (tipo == "Ingreso") Icons.Filled.KeyboardArrowUp else Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(tipo)
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { expandedTipo = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            DropdownMenuItem(
                                text = { Text("Gasto") },
                                onClick = {
                                    tipo = "Gasto"
                                    expandedTipo = false
                                },
                                leadingIcon = { Icon(Icons.Filled.ArrowDropDown, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Ingreso") },
                                onClick = {
                                    tipo = "Ingreso"
                                    expandedTipo = false
                                },
                                leadingIcon = { Icon(Icons.Filled.KeyboardArrowUp, contentDescription = null) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = monto,
                        onValueChange = { monto = it },
                        label = { Text("Monto (${simboloMoneda()})") },
                        leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    if (categoriasFiltradas.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedCategoria = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                IconoCategoria(categoriaSeleccionada, tamano = 28.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(categoriaSeleccionada?.name ?: "Seleccionar categoría")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedCategoria,
                                onDismissRequest = { expandedCategoria = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categoriasFiltradas.forEach { categoria ->
                                    DropdownMenuItem(
                                        text = { Text(categoria.name) },
                                        onClick = {
                                            categoriaSeleccionada = categoria
                                            expandedCategoria = false
                                        },
                                        leadingIcon = { IconoCategoria(categoria, tamano = 28.dp) }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    if (cuentas.isNotEmpty()) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedCuenta = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(cuentaSeleccionada?.name ?: "Seleccionar cuenta")
                                Spacer(modifier = Modifier.weight(1f))
                                Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                            }
                            DropdownMenu(
                                expanded = expandedCuenta,
                                onDismissRequest = { expandedCuenta = false },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                cuentas.forEach { cuenta ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(cuenta.name)
                                                Text(
                                                    dinero(cuenta.balance),
                                                    fontSize = 12.sp,
                                                    color = Color.Gray
                                                )
                                            }
                                        },
                                        onClick = {
                                            cuentaSeleccionada = cuenta
                                            expandedCuenta = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                when (cuenta.type) {
                                                    "Banco" -> Icons.Filled.DateRange
                                                    "Efectivo" -> Icons.Filled.Check
                                                    "Ahorros" -> Icons.Filled.Check
                                                    else -> Icons.Filled.Done
                                                },
                                                contentDescription = null
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.DateRange, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(fecha)
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    Button(
                        onClick = {
                            viewModel.createMovement(
                                monto = monto,
                                descripcion = descripcion,
                                fecha = fecha,
                                tipo = tipo,
                                categoria = categoriaSeleccionada,
                                cuenta = cuentaSeleccionada,
                                onSuccess = {
                                    Toast.makeText(context, "Movimiento guardado", Toast.LENGTH_SHORT).show()
                                    showSuccessAnimation = true
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                                }
                            )
                        },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = monto.isNotBlank() && descripcion.isNotBlank() &&
                                cuentaSeleccionada != null &&
                                (categoriasFiltradas.isEmpty() || categoriaSeleccionada != null)
                    ) {
                        Text("Guardar Movimiento", fontSize = 16.sp)
                    }
                }
            }
        }
    }

    LaunchedEffect(showSuccessAnimation) {
        if (showSuccessAnimation) {
            delay(1500)
            navController.popBackStack()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListaScreen(
    navController: NavController,
    viewModel: MovimientosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    val movimientos = uiState.movimientos
    val categorias = uiState.categorias.associateBy { it.id }
    val cuentas = uiState.cuentas.associateBy { it.id }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var expenseToEdit by remember { mutableStateOf<Expense?>(null) }
    val isLoading = uiState.isLoading

    val totalIngresos = movimientos.filter { it.type == "Ingreso" }
        .sumOf { MoneyUtils.montoDe(it) }
    val totalGastos = movimientos.filter { it.type == "Gasto" }
        .sumOf { MoneyUtils.montoDe(it) }
    val balance = totalIngresos - totalGastos

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Movimientos",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF3B82F6)
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEFF6FF),
                            Color(0xFFDBEAFE)
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF3B82F6),
                                        Color(0xFF2563EB),
                                        Color(0xFF1E40AF)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Balance Total",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    dinero(balance),
                                    color = Color.White,
                                    fontSize = 38.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3B82F6))
                    }
                } else if (movimientos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay movimientos", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(movimientos) { _, expense ->
                            ExpenseCard(
                                expense = expense,
                                category = categorias[expense.categoryId],
                                account = cuentas[expense.accountId],
                                onEdit = { expenseToEdit = expense }
                            )
                        }
                    }
                }
            }
        }
    }

    expenseToEdit?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToEdit = null },
            title = { Text("Gestionar Movimiento", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Monto: ${dinero(MoneyUtils.montoDe(expense))}")
                    Text("Descripción: ${expense.description}")
                    Text("Tipo: ${expense.type}")
                    Text("Fecha: ${fechaVisible(expense.date)}")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    expenseToEdit = null
                    expenseToDelete = expense
                }) {
                    Text("Eliminar movimiento", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToEdit = null }) {
                    Text("Cerrar")
                }
            }
        )
    }

    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Eliminar Movimiento") },
            text = { Text("¿Estás seguro de eliminar este movimiento?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMovement(
                        expense = expense,
                        cuentasMap = cuentas,
                        onSuccess = {
                            Toast.makeText(context, "Movimiento eliminado", Toast.LENGTH_SHORT).show()
                        },
                        onError = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                        }
                    )
                    expenseToDelete = null
                }) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    category: Category?,
    account: Account?,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (backgroundBrush, amountColor, icon) = when (expense.type) {
        "Ingreso" -> Triple(
            Brush.linearGradient(listOf(Color(0xFF56ab2f), Color(0xFFa8e063))),
            Color(0xFF2E7D32),
            Icons.Filled.ArrowDropDown
        )
        else -> Triple(
            AppColors.ExpenseGradient,
            Color(0xFFD32F2F),
            Icons.Filled.ShoppingCart
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(backgroundBrush, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dinero(MoneyUtils.montoDe(expense)),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = amountColor
                )
                Text(
                    text = expense.description,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                Text(
                    text = "${fechaVisible(expense.date)} • ${expense.type}",
                    color = Color(0xFF666666),
                    fontSize = 12.sp
                )
                if (category != null) {
                    Text(
                        text = "Categoría: ${category.name}",
                        color = Color(0xFF444444),
                        fontSize = 12.sp
                    )
                }
                if (account != null) {
                    Text(
                        text = "Cuenta: ${account.name}",
                        color = Color(0xFF444444),
                        fontSize = 12.sp
                    )
                }
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Gestionar", tint = Color(0xFF3B82F6))
            }
        }
    }
}
