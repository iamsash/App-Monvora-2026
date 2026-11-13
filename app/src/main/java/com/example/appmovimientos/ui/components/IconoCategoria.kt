package com.example.appmovimientos.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.model.Category

private val FondoGasto = Color(0xFFFFEDE8)
private val FondoIngreso = Color(0xFFE6F6EC)
private val FondoSinIcono = Color(0xFFE2E8F0)

/**
 * Ícono de una categoría: su emoji del catálogo sobre un círculo de color suave,
 * o un círculo gris vacío si la categoría no corresponde a ninguna del catálogo.
 */
@Composable
fun IconoCategoria(categoria: Category?, tamano: Dp = 40.dp, modifier: Modifier = Modifier) {
    IconoEmoji(
        emoji = CategoriasCatalogo.emojiPara(categoria),
        tipo = categoria?.type ?: CategoriasCatalogo.GASTO,
        tamano = tamano,
        modifier = modifier
    )
}

/** Variante para pantallas que solo conocen el emoji (o nada) y el tipo. */
@Composable
fun IconoEmoji(emoji: String?, tipo: String, tamano: Dp = 40.dp, modifier: Modifier = Modifier) {
    val fondo = when {
        emoji == null -> FondoSinIcono
        tipo == CategoriasCatalogo.INGRESO -> FondoIngreso
        else -> FondoGasto
    }
    Box(
        modifier = modifier
            .size(tamano)
            .background(fondo, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (emoji != null) {
            Text(emoji, fontSize = (tamano.value * 0.5f).sp)
        }
    }
}
