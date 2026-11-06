package com.example.appmovimientos.ui.screens.cuentas

import com.example.appmovimientos.data.preferences.dinero
import com.example.appmovimientos.data.preferences.simboloMoneda
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
import androidx.navigation.NavController
import com.example.appmovimientos.data.model.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentasScreen(
    navController: NavController,
    viewModel: CuentasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<Account?>(null) }
    var showEditDialog by remember { mutableStateOf<Account?>(null) }
    var nuevoSaldoInput by remember { mutableStateOf("") }

    val totalCuentasBalance = uiState.cuentas.sumOf { it.balance }
    val totalCuentasCount = uiState.cuentas.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Administrar Cuentas", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
                        Text("Gestiona tus cuentas y saldos", fontSize = 11.sp, color = Color.White.copy(alpha = 0.7f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        },
        bottomBar = {
            // Botón inferior «Agregar cuenta» con degradado
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8F9FF))
                    .padding(20.dp)
            ) {
                Button(
                    onClick = { navController.navigate("nuevaCuenta") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(20.dp),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Agregar cuenta", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(padding)
        ) {
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF6366F1))
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 📊 Tarjeta de Resumen con Gradiente y Elemento Gráfico
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(
                                                Color(0xFF6366F1),
                                                Color(0xFF8B5CF6),
                                                Color(0xFFEC4899)
                                            )
                                        )
                                    )
                                    .padding(22.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(Icons.Filled.AccountBox, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text("Total en cuentas", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                        }

                                        Text(
                                            dinero(totalCuentasBalance),
                                            color = Color.White,
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )

                                        Text(
                                            "$totalCuentasCount cuentas registradas",
                                            color = Color.White.copy(alpha = 0.75f),
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Elemento decorativo gráfico geométrico a la derecha
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(36.dp))
                                    }
                                }
                            }
                        }
                    }

                    if (uiState.cuentas.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 60.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Filled.AccountBox,
                                        contentDescription = null,
                                        modifier = Modifier.size(56.dp),
                                        tint = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text("No hay cuentas registradas", fontSize = 16.sp, color = Color.Gray)
                                }
                            }
                        }
                    } else {
                        itemsIndexed(uiState.cuentas) { _, account ->
                            AdminAccountCard(
                                account = account,
                                onEdit = {
                                    showEditDialog = account
                                    nuevoSaldoInput = account.balance.toString()
                                },
                                onDelete = { showDeleteDialog = account }
                            )
                        }
                    }
                }
            }
        }
    }

    showEditDialog?.let { account ->
        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = { Text("Actualizar Saldo") },
            text = {
                Column {
                    Text("Ingresa el nuevo saldo o suma dinero para '${account.name}':")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = nuevoSaldoInput,
                        onValueChange = { nuevoSaldoInput = it },
                        label = { Text("Saldo (${simboloMoneda()})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateAccount(
                            account = account,
                            nuevoSaldoStr = nuevoSaldoInput,
                            onSuccess = {
                                Toast.makeText(context, "Saldo actualizado exitosamente", Toast.LENGTH_SHORT).show()
                                showEditDialog = null
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                ) {
                    Text("Guardar", color = Color(0xFF6366F1), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }

    showDeleteDialog?.let { account ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Eliminar Cuenta") },
            text = {
                Text(
                    "¿Estás seguro de que deseas eliminar la cuenta '${account.name}'?\n\n" +
                        "Solo se pueden eliminar cuentas sin movimientos registrados."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAccount(
                            account = account,
                            onSuccess = {
                                Toast.makeText(context, "Cuenta eliminada", Toast.LENGTH_SHORT).show()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                        showDeleteDialog = null
                    }
                ) {
                    Text("Eliminar", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancelar")
                }
            },
            shape = RoundedCornerShape(20.dp),
            containerColor = Color.White
        )
    }
}

@Composable
fun AdminAccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

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
            // Icono circular sobre fondo lavanda claro
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(Color(0xFFEDE9FE), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (account.type) {
                        "Banco" -> Icons.Filled.DateRange
                        "Efectivo" -> Icons.Filled.Check
                        else -> Icons.Filled.Star
                    },
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (account.bank.isNotEmpty()) "${account.type} • ${account.bank}" else account.type,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dinero(account.balance),
                    color = Color(0xFF10B981),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Botón de lápiz azul para editar
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
            }

            // Menú de tres puntos verticales
            Box {
                IconButton(onClick = { expandedMenu = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Opciones", tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
                }
                DropdownMenu(
                    expanded = expandedMenu,
                    onDismissRequest = { expandedMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Color.Red) },
                        onClick = {
                            expandedMenu = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaCuentaScreen(
    navController: NavController,
    viewModel: CuentasViewModel = viewModel()
) {
    val context = LocalContext.current

    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("Banco") }
    var banco by remember { mutableStateOf("BCP") }
    var saldoInicial by remember { mutableStateOf("") }
    var expandedTipo by remember { mutableStateOf(false) }
    var expandedBanco by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val tiposCuenta = listOf("Banco", "Efectivo", "Ahorros", "Tarjeta de Crédito")
    val bancos = listOf("BCP", "Interbank", "BBVA", "Scotiabank", "Banco de la Nación", "BanBif", "Otro")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Cuenta", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0F0728))
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FF))
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text(
                            "Crear Nueva Cuenta",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it },
                            label = { Text("Nombre de la cuenta") },
                            leadingIcon = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedButton(
                                onClick = { expandedTipo = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Filled.Menu, contentDescription = null)
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
                                tiposCuenta.forEach { tipoItem ->
                                    DropdownMenuItem(
                                        text = { Text(tipoItem) },
                                        onClick = {
                                            tipo = tipoItem
                                            expandedTipo = false
                                            if (tipoItem == "Efectivo") banco = "" else if (banco.isBlank()) banco = "BCP"
                                        },
                                        leadingIcon = { Icon(Icons.Filled.Menu, contentDescription = null) }
                                    )
                                }
                            }
                        }

                        if (tipo != "Efectivo") {
                            Spacer(modifier = Modifier.height(16.dp))

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { expandedBanco = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Filled.Menu, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (banco.isEmpty()) "Seleccionar banco" else banco)
                                    Spacer(modifier = Modifier.weight(1f))
                                    Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
                                }
                                DropdownMenu(
                                    expanded = expandedBanco,
                                    onDismissRequest = { expandedBanco = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    bancos.forEach { bancoItem ->
                                        DropdownMenuItem(
                                            text = { Text(bancoItem) },
                                            onClick = {
                                                banco = bancoItem
                                                expandedBanco = false
                                            },
                                            leadingIcon = { Icon(Icons.Filled.Menu, contentDescription = null) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = saldoInicial,
                            onValueChange = { saldoInicial = it },
                            label = { Text("Saldo inicial (${simboloMoneda()})") },
                            leadingIcon = { Icon(Icons.Filled.Check, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.createAccount(
                                    nombre = nombre,
                                    tipo = tipo,
                                    banco = banco,
                                    saldoInicial = saldoInicial,
                                    onSuccess = {
                                        Toast.makeText(context, "Cuenta creada exitosamente", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    },
                                    onError = { errorMsg ->
                                        errorMessage = errorMsg
                                    }
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            enabled = nombre.isNotBlank() && saldoInicial.isNotBlank()
                        ) {
                            Text("Crear Cuenta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }

                        errorMessage?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}
