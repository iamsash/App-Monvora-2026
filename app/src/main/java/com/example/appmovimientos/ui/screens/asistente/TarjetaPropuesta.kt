package com.example.appmovimientos.ui.screens.asistente

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appmovimientos.data.model.CategoriasCatalogo
import com.example.appmovimientos.data.repository.Propuesta
import com.example.appmovimientos.ui.screens.asistente.AsistenteViewModel.EstadoPropuesta
import com.example.appmovimientos.utils.MoneyUtils
import java.time.format.DateTimeFormatter

private val Morado = Color(0xFF7C3AED)
private val TextoOscuro = Color(0xFF1E293B)
private val TextoSuave = Color(0xFF64748B)
private val formatoFecha = DateTimeFormatter.ofPattern("dd/MM/yyyy")

/** Ícono de micrófono (Material "mic"), sin depender de la librería de íconos extendida. */
val IconoMicrofono: ImageVector = ImageVector.Builder(
    name = "Microfono", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f
).addPath(
    pathData = addPathNodes(
        "M12,14c1.66,0 2.99,-1.34 2.99,-3L15,5c0,-1.66 -1.34,-3 -3,-3S9,3.34 9,5v6c0,1.66 1.34,3 3,3z" +
            "M17.3,11c0,3 -2.54,5.1 -5.3,5.1S6.7,14 6.7,11L5,11c0,3.41 2.72,6.23 6,6.72L11,21h2v-3.28c3.28,-0.48 6,-3.3 6,-6.72h-1.7z"
    ),
    fill = SolidColor(Color.Black)
).build()

/** Lo que muestra la tarjeta: título y filas etiqueta/valor. */
internal fun resumenPropuesta(p: Propuesta): Pair<String, List<Pair<String, String>>> = when (p) {
    is Propuesta.Movimiento -> {
        val titulo = if (p.tipo == CategoriasCatalogo.INGRESO) "💰 Nuevo ingreso" else "💸 Nuevo gasto"
        titulo to listOf(
            "Monto" to "S/ ${MoneyUtils.normalizar(p.monto)}",
            "Descripción" to p.descripcion,
            "Cuenta" to p.cuentaNombre,
            "Categoría" to (p.categoriaNombre ?: "Sin categoría"),
            "Fecha" to formatoFecha.format(p.fecha)
        )
    }
    is Propuesta.Cuenta -> "🏦 Nueva cuenta" to buildList {
        add("Nombre" to p.nombre)
        add("Tipo" to p.tipo)
        if (p.banco.isNotBlank()) add("Banco" to p.banco)
        add("Saldo inicial" to "S/ ${MoneyUtils.normalizar(p.saldoInicial)}")
    }
    is Propuesta.Categoria -> {
        val emoji = CategoriasCatalogo.coincidenciaPorNombre(p.nombre, p.tipo)?.emoji
        "🏷️ Nueva categoría" to listOf(
            "Nombre" to (if (emoji != null) "$emoji ${p.nombre}" else p.nombre),
            "Tipo" to p.tipo
        )
    }
}

/**
 * Borrador que preparó el asistente. Nada se guarda hasta que el usuario pulsa "Guardar";
 * para corregir algo basta con decírselo al asistente (prepara un borrador nuevo).
 */
@Composable
fun TarjetaPropuesta(
    propuesta: Propuesta,
    estado: EstadoPropuesta,
    error: String?,
    onGuardar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (titulo, filas) = resumenPropuesta(propuesta)
    val activa = estado == EstadoPropuesta.PENDIENTE || estado == EstadoPropuesta.GUARDANDO

    Column(
        modifier = modifier
            .widthIn(max = 300.dp)
            .fillMaxWidth()
            .background(if (activa) Color.White else Color.White.copy(alpha = 0.75f), RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Text(titulo, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TextoOscuro)
        Spacer(modifier = Modifier.height(8.dp))
        filas.forEach { (etiqueta, valor) ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                Text(etiqueta, color = TextoSuave, fontSize = 13.sp, modifier = Modifier.weight(0.4f))
                Text(valor, color = TextoOscuro, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(0.6f))
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFE2E8F0))
        Spacer(modifier = Modifier.height(10.dp))

        when (estado) {
            EstadoPropuesta.PENDIENTE, EstadoPropuesta.GUARDANDO -> {
                if (error != null) {
                    Text(error, color = Color(0xFFA32D2D), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = onCancelar,
                        enabled = estado == EstadoPropuesta.PENDIENTE,
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar", color = TextoSuave) }
                    Button(
                        onClick = onGuardar,
                        enabled = estado == EstadoPropuesta.PENDIENTE,
                        colors = ButtonDefaults.buttonColors(containerColor = Morado),
                        modifier = Modifier.weight(1f)
                    ) {
                        if (estado == EstadoPropuesta.GUARDANDO) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = Color.White)
                        } else {
                            Text("Guardar")
                        }
                    }
                }
                Text(
                    "¿Algo no está bien? Díselo al asistente y lo corrige.",
                    color = TextoSuave,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            EstadoPropuesta.GUARDADA -> Estado("✓ Guardado", Color(0xFF15803D))
            EstadoPropuesta.CANCELADA -> Estado("Cancelado · no se guardó", TextoSuave)
            EstadoPropuesta.REEMPLAZADA -> Estado("Reemplazado por la corrección · no se guardó", TextoSuave)
        }
    }
}

@Composable
private fun Estado(texto: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(texto, color = color, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}
