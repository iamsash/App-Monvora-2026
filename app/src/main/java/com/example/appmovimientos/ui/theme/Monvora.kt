package com.example.appmovimientos.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

/**
 * Colores y piezas del estilo Monvora, tomados del dashboard (Inicio):
 * fondo morado oscuro, tarjetas blancas y botones con degradado.
 */
object Monvora {
    val FondoOscuro = Color(0xFF0F0728)
    val FondoMedio = Color(0xFF1E103F)
    val FondoProfundo = Color(0xFF13092A)
    val BordeOscuro = Color(0xFF3B2A6B)

    val FondoPantalla = Brush.verticalGradient(listOf(FondoOscuro, FondoMedio, FondoProfundo))

    /** Tarjeta "Balance Total". */
    val DegradadoPrincipal = Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899)))
    /** Botón "Registrar" (movimientos). */
    val DegradadoRegistrar = Brush.linearGradient(listOf(Color(0xFFF43F5E), Color(0xFFEC4899)))
    /** Botón "Categorías". */
    val DegradadoCategorias = Brush.linearGradient(listOf(Color(0xFFFBBF24), Color(0xFFF97316)))
    /** Botón "Ver Reportes Completos" (acciones generales). */
    val DegradadoReportes = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFD946EF)))

    val Morado = Color(0xFF7C3AED)
    val Indigo = Color(0xFF6366F1)
    val Lila = Color(0xFFA78BFA)
    val LilaClaro = Color(0xFFC4B5FD)
    val LilaFondo = Color(0xFFEDE9FE)
    val Naranja = Color(0xFFF97316)

    val Verde = Color(0xFF10B981)
    val VerdeFondo = Color(0xFFECFDF5)
    val Rojo = Color(0xFFEF4444)
    val RojoFondo = Color(0xFFFEE2E2)

    val TextoOscuro = Color(0xFF1E293B)
    val TextoMedio = Color(0xFF475569)
    val TextoSuave = Color(0xFF64748B)
    val Gris = Color(0xFF94A3B8)
    val GrisClaro = Color(0xFFF1F5F9)
    val Divisor = Color(0xFFE2E8F0)

    val FormaTarjeta = RoundedCornerShape(20.dp)
    val FormaBoton = RoundedCornerShape(16.dp)

    val coloresTarjeta: CardColors
        @Composable get() = CardDefaults.cardColors(containerColor = Color.White)

    /** Campos de texto dentro de tarjetas blancas. */
    val coloresCampo: TextFieldColors
        @Composable get() = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF8B5CF6),
            unfocusedBorderColor = Divisor,
            focusedLabelColor = Morado,
            cursorColor = Morado,
            focusedLeadingIconColor = Morado,
            unfocusedLeadingIconColor = Gris,
            focusedTrailingIconColor = Morado,
            unfocusedTrailingIconColor = Gris,
            focusedTextColor = TextoOscuro,
            unfocusedTextColor = TextoOscuro
        )
}

/** Pantalla con el fondo y la barra superior del dashboard. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaMonvora(
    titulo: String,
    onVolver: (() -> Unit)?,
    modifier: Modifier = Modifier,
    acciones: @Composable RowScope.() -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(Monvora.FondoPantalla)) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(titulo, fontWeight = FontWeight.Bold, color = Color.White) },
                    navigationIcon = {
                        if (onVolver != null) {
                            IconButton(onClick = onVolver) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                            }
                        }
                    },
                    actions = acciones,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Monvora.FondoOscuro,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            },
            floatingActionButton = floatingActionButton,
            content = contenido
        )
    }
}

/** Tarjeta blanca redondeada, como las del dashboard. */
@Composable
fun TarjetaMonvora(
    modifier: Modifier = Modifier,
    relleno: PaddingValues = PaddingValues(20.dp),
    contenido: @Composable ColumnScope.() -> Unit
) {
    Surface(modifier = modifier, shape = Monvora.FormaTarjeta, color = Color.White, shadowElevation = 4.dp) {
        Column(modifier = Modifier.padding(relleno), content = contenido)
    }
}

/** Botón con degradado (los de "Registrar", "Categorías" y "Ver Reportes" del dashboard). */
@Composable
fun BotonDegradado(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    degradado: Brush = Monvora.DegradadoReportes,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    icono: ImageVector? = null
) {
    Surface(
        onClick = onClick,
        enabled = habilitado && !cargando,
        shape = Monvora.FormaBoton,
        color = Color.Transparent,
        modifier = modifier.height(54.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(degradado)
                .alpha(if (habilitado) 1f else 0.6f),
            contentAlignment = Alignment.Center
        ) {
            if (!habilitado) Box(Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.35f)))
            if (cargando) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp, color = Color.White)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icono != null) {
                        Icon(icono, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(texto, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }
    }
}

/** Colores para un Button de Material al que se le pone un degradado de fondo con Modifier.background. */
@Composable
fun coloresBotonDegradado() = androidx.compose.material3.ButtonDefaults.buttonColors(
    containerColor = Color.Transparent,
    contentColor = Color.White,
    disabledContainerColor = Color.Black.copy(alpha = 0.35f),
    disabledContentColor = Color.White.copy(alpha = 0.7f)
)

/** Título de sección sobre el fondo oscuro (como "Mis Cuentas (2)"). */
@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(texto, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = modifier)
}

/**
 * Diálogo mixto: encabezado oscuro con degradado y cuerpo blanco.
 * [botones] va al pie, alineado a la derecha.
 */
@Composable
fun DialogoMonvora(
    titulo: String,
    onCerrar: () -> Unit,
    subtitulo: String? = null,
    encabezado: Brush = Monvora.DegradadoPrincipal,
    iconoEncabezado: @Composable (() -> Unit)? = null,
    botones: @Composable RowScope.() -> Unit = {},
    contenido: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onCerrar) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(encabezado)
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (iconoEncabezado != null) {
                    iconoEncabezado()
                    Spacer(Modifier.width(12.dp))
                }
                Column {
                    Text(titulo, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    if (subtitulo != null) Text(subtitulo, color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                }
            }
            // weight(fill = false): si el contenido es largo, se desplaza y los botones siguen visibles
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(start = 22.dp, end = 22.dp, top = 18.dp),
                content = contenido
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
                content = botones
            )
        }
    }
}
