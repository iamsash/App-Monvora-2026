package com.example.appmovimientos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Ícono de destellos del asistente (estrella de cuatro puntas con una chispa pequeña). */
val IconoAsistente: ImageVector by lazy {
    ImageVector.Builder(
        name = "IconoAsistente",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(fill = SolidColor(Color.Black)) {
            moveTo(10f, 3f)
            curveTo(10.6f, 8.2f, 12.8f, 10.4f, 18f, 11f)
            curveTo(12.8f, 11.6f, 10.6f, 13.8f, 10f, 19f)
            curveTo(9.4f, 13.8f, 7.2f, 11.6f, 2f, 11f)
            curveTo(7.2f, 10.4f, 9.4f, 8.2f, 10f, 3f)
            close()
        }
        path(fill = SolidColor(Color.Black)) {
            moveTo(18.5f, 14f)
            curveTo(18.8f, 16.3f, 19.7f, 17.2f, 22f, 17.5f)
            curveTo(19.7f, 17.8f, 18.8f, 18.7f, 18.5f, 21f)
            curveTo(18.2f, 18.7f, 17.3f, 17.8f, 15f, 17.5f)
            curveTo(17.3f, 17.2f, 18.2f, 16.3f, 18.5f, 14f)
            close()
        }
    }.build()
}

/** Degradado de la marca, el mismo de la tarjeta "Balance Total". */
val DegradadoAsistente = Brush.linearGradient(listOf(Color(0xFF8B5CF6), Color(0xFFEC4899)))

/** Círculo con degradado y el ícono del asistente. */
@Composable
fun AvatarAsistente(tamano: Dp = 40.dp) {
    Box(
        modifier = Modifier
            .size(tamano)
            .background(DegradadoAsistente, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = IconoAsistente,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(tamano * 0.55f)
        )
    }
}

/**
 * Tarjeta compacta del Dashboard: ícono, "Pregúntale a Monvora",
 * "Consulta tus finanzas con IA" y una flecha. Al tocarla abre el chat.
 */
@Composable
fun TarjetaAsistente(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarAsistente(tamano = 40.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(
                    "Pregúntale a Monvora",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B)
                )
                Text(
                    "Consulta tus finanzas con IA",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Abrir asistente",
                tint = Color(0xFF7C3AED),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}
