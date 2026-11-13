package com.example.appmovimientos.ui.screens.categorias

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appmovimientos.data.model.CategoriaSugerida
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.model.Category
import com.example.appmovimientos.ui.components.IconoEmoji
import com.example.appmovimientos.ui.theme.BotonDegradado
import com.example.appmovimientos.ui.theme.DialogoMonvora
import com.example.appmovimientos.ui.theme.Monvora
import com.example.appmovimientos.ui.theme.PantallaMonvora

private val Morado = Monvora.Morado
private val TextoOscuro = Color(0xFF1E293B)
private val TextoGris = Color(0xFF64748B)

/** Lo elegido en el selector: una categoría sugerida o "Otra" (sin emoji). */
private sealed interface Eleccion {
    data class Sugerida(val categoria: CategoriaSugerida) : Eleccion
    data object Otra : Eleccion
}

/**
 * Crear categoría: se elige una sugerida (emoji y nombre van juntos) o "Otra",
 * que se crea en blanco, sin emoji. Solo se muestran las sugeridas que el usuario aún no tiene.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuevaCategoriaScreen(
    navController: NavController,
    viewModel: CategoriasViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var tipo by remember { mutableStateOf(CategoriasCatalogo.GASTO) }
    var eleccion by remember { mutableStateOf<Eleccion?>(null) }
    var nombre by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val sugeridas = CategoriasCatalogo.faltantes(uiState.categorias, CategoriasCatalogo.deTipo(tipo))

    PantallaMonvora(titulo = "Nueva categoría", onVolver = { navController.popBackStack() }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SelectorTipo(tipo) {
                        if (it != tipo) {
                            tipo = it
                            eleccion = null
                            nombre = ""
                            errorMessage = null
                        }
                    }

                    SelectorSugeridas(
                        sugeridas = sugeridas,
                        eleccion = eleccion,
                        onElegir = {
                            eleccion = it
                            nombre = (it as? Eleccion.Sugerida)?.categoria?.nombre ?: ""
                            errorMessage = null
                        }
                    )
                }
            }

            eleccion?.let { actual ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        val sugerida = (actual as? Eleccion.Sugerida)?.categoria
                        Vista(
                            emoji = sugerida?.emoji,
                            tipo = tipo,
                            nombre = nombre.ifBlank { if (sugerida == null) "Tu categoría" else sugerida.nombre }
                        )
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it.take(30); errorMessage = null },
                            label = { Text(if (sugerida == null) "Nombre de tu categoría" else "Nombre (puedes ajustarlo)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = Monvora.coloresCampo
                        )
                        if (sugerida == null) {
                            Text(
                                "Las categorías personalizadas se crean sin emoji.",
                                fontSize = 12.sp,
                                color = TextoGris
                            )
                        }

                        errorMessage?.let { Text(it, color = Color(0xFFDC2626), fontSize = 13.sp) }

                        BotonDegradado(
                            texto = "Crear categoría",
                            degradado = Monvora.DegradadoCategorias,
                            onClick = {
                                viewModel.createCategory(
                                    nombre = nombre,
                                    tipo = tipo,
                                    icono = sugerida?.id ?: CategoriasCatalogo.SIN_ICONO,
                                    onSuccess = {
                                        Toast.makeText(context, "Categoría creada", Toast.LENGTH_SHORT).show()
                                        navController.popBackStack()
                                    },
                                    onError = { errorMessage = it }
                                )
                            },
                            habilitado = nombre.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/** Editar una categoría: nombre, tipo y emoji (de la lista o sin emoji). */
@Composable
fun EditarCategoriaDialog(
    categoria: Category,
    onDismiss: () -> Unit,
    onSave: (Category, String, String, String) -> Unit
) {
    val actual = CategoriasCatalogo.coincidencia(categoria)
    var tipo by remember { mutableStateOf(categoria.type) }
    var eleccion by remember {
        mutableStateOf<Eleccion?>(if (actual != null) Eleccion.Sugerida(actual) else Eleccion.Otra)
    }
    var nombre by remember { mutableStateOf(categoria.name) }

    DialogoMonvora(
        titulo = "Editar categoría",
        subtitulo = categoria.name,
        encabezado = Monvora.DegradadoCategorias,
        onCerrar = onDismiss,
        botones = {
            OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(12.dp)) { Text("Cancelar", color = TextoGris) }
            Button(
                onClick = {
                    val icono = (eleccion as? Eleccion.Sugerida)?.categoria?.id ?: CategoriasCatalogo.SIN_ICONO
                    onSave(categoria, nombre, tipo, icono)
                },
                enabled = nombre.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Monvora.Naranja),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Guardar", fontWeight = FontWeight.SemiBold) }
        }
    ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                val sugerida = (eleccion as? Eleccion.Sugerida)?.categoria
                Vista(emoji = sugerida?.emoji, tipo = tipo, nombre = nombre.ifBlank { "Sin nombre" })
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it.take(30) },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = Monvora.coloresCampo
                )
                SelectorTipo(tipo) {
                    if (it != tipo) {
                        tipo = it
                        eleccion = Eleccion.Otra // el emoji de un tipo no vale para el otro
                    }
                }
                SelectorSugeridas(
                    sugeridas = CategoriasCatalogo.deTipo(tipo),
                    eleccion = eleccion,
                    onElegir = { eleccion = it },
                    textoOtra = "Sin emoji"
                )
            }
    }
}

@Composable
private fun SelectorTipo(tipo: String, onCambiar: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        listOf(CategoriasCatalogo.GASTO to "💸 Gasto", CategoriasCatalogo.INGRESO to "💰 Ingreso").forEach { (valor, texto) ->
            val activo = tipo == valor
            Surface(
                onClick = { onCambiar(valor) },
                shape = RoundedCornerShape(12.dp),
                color = if (activo) Morado else Color(0xFFF1F5F9),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    texto,
                    color = if (activo) Color.White else TextoOscuro,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectorSugeridas(
    sugeridas: List<CategoriaSugerida>,
    eleccion: Eleccion?,
    onElegir: (Eleccion) -> Unit,
    textoOtra: String = "Otra"
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Elige una categoría", fontWeight = FontWeight.Bold, color = TextoOscuro, fontSize = 15.sp)
        if (sugeridas.isEmpty()) {
            Text("Ya tienes todas las categorías sugeridas de este tipo. Puedes crear una propia con \"Otra\".", fontSize = 13.sp, color = TextoGris)
        }
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            sugeridas.forEach { s ->
                Chip(
                    texto = "${s.emoji}  ${s.nombre}",
                    seleccionado = (eleccion as? Eleccion.Sugerida)?.categoria?.id == s.id,
                    onClick = { onElegir(Eleccion.Sugerida(s)) }
                )
            }
            Chip(
                texto = "✏️  $textoOtra",
                seleccionado = eleccion == Eleccion.Otra,
                onClick = { onElegir(Eleccion.Otra) }
            )
        }
    }
}

@Composable
private fun Chip(texto: String, seleccionado: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (seleccionado) Color(0xFFEDE9FE) else Color.White,
        border = BorderStroke(1.dp, if (seleccionado) Morado else Color(0xFFE2E8F0))
    ) {
        Text(
            texto,
            fontSize = 14.sp,
            color = if (seleccionado) Color(0xFF4C1D95) else TextoOscuro,
            fontWeight = if (seleccionado) FontWeight.SemiBold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}

/** Vista previa: cómo se verá la categoría en la app. */
@Composable
private fun Vista(emoji: String?, tipo: String, nombre: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconoEmoji(emoji = emoji, tipo = tipo, tamano = 48.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(nombre, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = TextoOscuro)
            Text(tipo, fontSize = 13.sp, color = if (tipo == CategoriasCatalogo.INGRESO) Color(0xFF10B981) else Color(0xFFEF4444))
        }
    }
}
