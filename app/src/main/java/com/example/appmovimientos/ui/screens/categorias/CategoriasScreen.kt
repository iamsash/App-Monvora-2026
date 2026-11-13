package com.example.appmovimientos.ui.screens.categorias

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.ui.components.CategoryCard
import com.example.appmovimientos.ui.theme.DialogoMonvora
import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriasScreen(
    navController: NavController,
    viewModel: CategoriasViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var showDeleteDialog by remember { mutableStateOf<Category?>(null) }
    var categoriaAEditar by remember { mutableStateOf<Category?>(null) }
    var filtroSeleccionado by remember { mutableStateOf("Todas") }

    val categorias = uiState.categorias
    val faltantes = viewModel.sugeridasFaltantes()
    var agregando by remember { mutableStateOf(false) }
    val agregarSugeridas: () -> Unit = {
        agregando = true
        viewModel.agregarSugeridas(
            onSuccess = { n ->
                agregando = false
                Toast.makeText(context, if (n == 1) "Se agregó 1 categoría" else "Se agregaron $n categorías", Toast.LENGTH_SHORT).show()
            },
            onError = { err ->
                agregando = false
                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
            }
        )
    }

    PantallaMonvora(
        titulo = "Mis Categorías",
        onVolver = { navController.popBackStack() },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("nuevaCategoria") },
                containerColor = Color.Transparent,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.background(Monvora.DegradadoCategorias, RoundedCornerShape(16.dp))
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Nueva Categoría",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = Monvora.FormaTarjeta,
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Total de Categorías",
                                    fontSize = 14.sp,
                                    color = Color(0xFF64748B),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "${categorias.size}",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    style = TextStyle(brush = Monvora.DegradadoCategorias)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        brush = Monvora.DegradadoCategorias,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🏷️", fontSize = 30.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFECFDF5)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${categorias.count { it.type == "Ingreso" }}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    Text(
                                        "Ingresos",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFEF2F2)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "${categorias.count { it.type == "Gasto" }}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFEF4444)
                                    )
                                    Text(
                                        "Gastos",
                                        fontSize = 12.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = filtroSeleccionado == "Todas",
                        onClick = { filtroSeleccionado = "Todas" },
                        label = {
                            Text(
                                "Todas (${categorias.size})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Menu,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Monvora.Naranja,
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Monvora.LilaClaro,
                            iconColor = Monvora.LilaClaro
                        )
                    )

                    FilterChip(
                        selected = filtroSeleccionado == "Ingreso",
                        onClick = { filtroSeleccionado = "Ingreso" },
                        label = {
                            Text(
                                "Ingresos (${categorias.count { it.type == "Ingreso" }})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF10B981),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Monvora.LilaClaro,
                            iconColor = Monvora.LilaClaro
                        )
                    )

                    FilterChip(
                        selected = filtroSeleccionado == "Gasto",
                        onClick = { filtroSeleccionado = "Gasto" },
                        label = {
                            Text(
                                "Gastos (${categorias.count { it.type == "Gasto" }})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFEF4444),
                            selectedLabelColor = Color.White,
                            selectedLeadingIconColor = Color.White,
                            containerColor = Color.White.copy(alpha = 0.08f),
                            labelColor = Monvora.LilaClaro,
                            iconColor = Monvora.LilaClaro
                        )
                    )
                }

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Monvora.LilaClaro
                        )
                    }
                } else if (categorias.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .background(
                                        color = Color.White.copy(alpha = 0.08f),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Filled.Menu,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Monvora.LilaClaro
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                "No hay categorías aún",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                "Crea tu primera categoría para organizar",
                                fontSize = 14.sp,
                                color = Monvora.LilaClaro,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "mejor tus gastos e ingresos",
                                fontSize = 14.sp,
                                color = Monvora.LilaClaro,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            Button(
                                onClick = { navController.navigate("nuevaCategoria") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Monvora.Naranja
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .height(50.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Crear Primera Categoría",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            if (faltantes.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedButton(
                                    onClick = agregarSugeridas,
                                    enabled = !agregando,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth(0.7f)
                                        .height(50.dp)
                                ) {
                                    Text("✨ Agregar categorías sugeridas", color = Monvora.LilaClaro, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                } else {
                    val categoriasFiltradas = when (filtroSeleccionado) {
                        "Ingreso" -> categorias.filter { it.type == "Ingreso" }
                        "Gasto" -> categorias.filter { it.type == "Gasto" }
                        else -> categorias
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        if (faltantes.isNotEmpty()) {
                            item {
                                AvisoSugeridas(
                                    faltantes = faltantes.size,
                                    vistaPrevia = faltantes.take(6).joinToString(" ") { it.emoji },
                                    agregando = agregando,
                                    onAgregar = agregarSugeridas
                                )
                            }
                        }
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    when (filtroSeleccionado) {
                                        "Ingreso" -> "Categorías de Ingreso (${categoriasFiltradas.size})"
                                        "Gasto" -> "Categorías de Gasto (${categoriasFiltradas.size})"
                                        else -> "Todas las categorías (${categoriasFiltradas.size})"
                                    },
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                if (filtroSeleccionado != "Todas") {
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = when (filtroSeleccionado) {
                                                "Ingreso" -> Color(0xFFECFDF5)
                                                else -> Color(0xFFFEF2F2)
                                            }
                                        )
                                    ) {
                                        Text(
                                            filtroSeleccionado,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = when (filtroSeleccionado) {
                                                "Ingreso" -> Color(0xFF10B981)
                                                else -> Color(0xFFEF4444)
                                            },
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (categoriasFiltradas.isEmpty()) {
                            item {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 32.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            Icons.Filled.List,
                                            contentDescription = null,
                                            modifier = Modifier.size(48.dp),
                                            tint = Color(0xFF94A3B8)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            "No hay categorías de $filtroSeleccionado",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF475569)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Crea una nueva categoría para este tipo",
                                            fontSize = 13.sp,
                                            color = Color(0xFF94A3B8),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            itemsIndexed(categoriasFiltradas) { _, category ->
                                CategoryCard(
                                    category = category,
                                    onEdit = { categoriaAEditar = category },
                                    onDelete = { showDeleteDialog = category }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { category ->
        DialogoMonvora(
            titulo = "Eliminar Categoría",
            subtitulo = category.name,
            encabezado = Monvora.DegradadoRegistrar,
            onCerrar = { showDeleteDialog = null },
            botones = {
                OutlinedButton(
                    onClick = { showDeleteDialog = null },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Monvora.Divisor)
                ) { Text("Cancelar", color = Monvora.TextoSuave) }
                Button(
                    onClick = {
                        viewModel.deleteCategory(
                            category = category,
                            onSuccess = {
                                Toast.makeText(context, "✓ Categoría eliminada", Toast.LENGTH_SHORT).show()
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Monvora.Rojo),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Eliminar", fontWeight = FontWeight.SemiBold) }
            }
        ) {
            Text("¿Estás seguro de que deseas eliminar la categoría '${category.name}'? Esta acción no se puede deshacer.", color = Monvora.TextoMedio)
        }
    }

    categoriaAEditar?.let { categoria ->
        EditarCategoriaDialog(
            categoria = categoria,
            onDismiss = { categoriaAEditar = null },
            onSave = { categoriaActualizada, nombre, tipo, icono ->
                viewModel.updateCategory(
                    category = categoriaActualizada,
                    nombre = nombre,
                    tipo = tipo,
                    icono = icono,
                    onSuccess = {
                        Toast.makeText(context, "✓ Categoría actualizada", Toast.LENGTH_SHORT).show()
                        categoriaAEditar = null
                    },
                    onError = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )
    }
}

/** Aviso para usuarios que aún no tienen las categorías por defecto. */
@Composable
private fun AvisoSugeridas(faltantes: Int, vistaPrevia: String, agregando: Boolean, onAgregar: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Categorías sugeridas", fontWeight = FontWeight.Bold, color = Color(0xFF4C1D95))
                Text(
                    if (faltantes == 1) "Te falta 1 categoría común" else "Te faltan $faltantes categorías comunes",
                    fontSize = 13.sp,
                    color = Color(0xFF6D28D9)
                )
                Text(vistaPrevia, fontSize = 18.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Button(
                onClick = onAgregar,
                enabled = !agregando,
                colors = ButtonDefaults.buttonColors(containerColor = Monvora.Naranja),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (agregando) "Agregando…" else "Agregar")
            }
        }
    }
}
