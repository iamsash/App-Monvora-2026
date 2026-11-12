package com.example.appmovimientos.ui.screens.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.appmovimientos.ui.components.AvatarAsistente
import com.example.appmovimientos.ui.components.DegradadoAsistente

private val FondoApp = Brush.verticalGradient(
    listOf(Color(0xFF0F0728), Color(0xFF1E103F), Color(0xFF13092A))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenteScreen(
    navController: NavController,
    viewModel: AsistenteViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listaState = rememberLazyListState()

    // Desplaza al último mensaje cuando llega uno nuevo o aparece "escribiendo…"
    LaunchedEffect(uiState.mensajes.size, uiState.enviando) {
        val total = uiState.mensajes.size + if (uiState.enviando) 1 else 0
        if (total > 0) listaState.animateScrollToItem(total - 1)
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarAsistente(tamano = 32.dp)
                        Column(modifier = Modifier.padding(start = 10.dp)) {
                            Text("Asistente IA", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color.White)
                            Text("Tus finanzas en Monvora", fontSize = 12.sp, color = Color(0xFFC4B5FD))
                        }
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
        modifier = Modifier.background(FondoApp)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(FondoApp)
                .padding(padding)
                .imePadding()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                if (uiState.mensajes.isEmpty()) {
                    Bienvenida(onPregunta = viewModel::enviarPregunta, habilitado = !uiState.enviando)
                } else {
                    LazyColumn(
                        state = listaState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.mensajes, key = { it.id }) { mensaje -> Burbuja(mensaje) }
                        if (uiState.enviando) {
                            item(key = "escribiendo") { Escribiendo() }
                        }
                    }
                }
            }

            BarraEntrada(
                texto = uiState.texto,
                onTextoChange = viewModel::onTextoChange,
                onEnviar = viewModel::enviar,
                puedeEnviar = uiState.puedeEnviar,
                consultasRestantes = uiState.consultasRestantes
            )
        }
    }
}

@Composable
private fun Bienvenida(onPregunta: (String) -> Unit, habilitado: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AvatarAsistente(tamano = 64.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Pregúntale a Monvora", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Consulta tus gastos, ingresos y cuentas con tus datos reales.",
            fontSize = 14.sp,
            color = Color(0xFFC4B5FD)
        )
        Spacer(modifier = Modifier.height(24.dp))
        AsistenteViewModel.PREGUNTAS_SUGERIDAS.forEach { pregunta ->
            Surface(
                onClick = { onPregunta(pregunta) },
                enabled = habilitado,
                shape = RoundedCornerShape(14.dp),
                color = Color.White.copy(alpha = 0.08f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Text(
                    pregunta,
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                )
            }
        }
    }
}

@Composable
private fun Burbuja(mensaje: AsistenteViewModel.Mensaje) {
    val alineacion = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = alineacion, verticalAlignment = Alignment.Top) {
        if (!mensaje.esUsuario) {
            AvatarAsistente(tamano = 28.dp)
            Spacer(modifier = Modifier.size(8.dp))
        }
        val forma = if (mensaje.esUsuario)
            RoundedCornerShape(18.dp, 18.dp, 4.dp, 18.dp)
        else
            RoundedCornerShape(4.dp, 18.dp, 18.dp, 18.dp)
        val fondo = when {
            mensaje.esUsuario -> Modifier.background(DegradadoAsistente, forma)
            mensaje.esError -> Modifier.background(Color(0xFFFCEBEB), forma)
            else -> Modifier.background(Color.White, forma)
        }
        Text(
            text = mensaje.texto,
            color = when {
                mensaje.esUsuario -> Color.White
                mensaje.esError -> Color(0xFFA32D2D)
                else -> Color(0xFF1E293B)
            },
            fontSize = 15.sp,
            lineHeight = 21.sp,
            modifier = Modifier
                .widthIn(max = 290.dp)
                .then(fondo)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        )
    }
}

@Composable
private fun Escribiendo() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AvatarAsistente(tamano = 28.dp)
        Spacer(modifier = Modifier.size(8.dp))
        Row(
            modifier = Modifier
                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(18.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color(0xFFC4B5FD))
            Spacer(modifier = Modifier.size(8.dp))
            Text("Monvora está consultando tus datos…", color = Color(0xFFC4B5FD), fontSize = 13.sp)
        }
    }
}

@Composable
private fun BarraEntrada(
    texto: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    puedeEnviar: Boolean,
    consultasRestantes: Int?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F0728))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = texto,
                onValueChange = onTextoChange,
                placeholder = { Text("Escribe tu pregunta…", color = Color(0xFF94A3B8)) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(onSend = { if (puedeEnviar) onEnviar() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color(0xFF8B5CF6),
                    unfocusedBorderColor = Color(0xFF3B2A6B),
                    cursorColor = Color(0xFFC4B5FD),
                    focusedContainerColor = Color(0xFF1E103F),
                    unfocusedContainerColor = Color(0xFF1E103F)
                )
            )
            Spacer(modifier = Modifier.size(8.dp))
            IconButton(
                onClick = onEnviar,
                enabled = puedeEnviar,
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (puedeEnviar) DegradadoAsistente else Brush.linearGradient(listOf(Color(0xFF3B2A6B), Color(0xFF3B2A6B))),
                        CircleShape
                    ),
                colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White, disabledContentColor = Color(0xFF94A3B8))
            ) {
                Icon(Icons.Filled.Send, contentDescription = "Enviar")
            }
        }
        if (consultasRestantes != null) {
            Text(
                text = if (consultasRestantes == 1) "Te queda 1 consulta hoy" else "Te quedan $consultasRestantes consultas hoy",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 8.dp, top = 6.dp)
            )
        }
    }
}
