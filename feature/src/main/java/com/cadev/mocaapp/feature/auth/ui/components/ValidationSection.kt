package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * SECCIÓN DE VALIDACIÓN DE CONTRASEÑA
 */
@Composable
fun SeccionValidacion(
    contrasena: String,
    confirmarContrasena: String
) {
    val tieneLongitudMin = contrasena.length >= 8
    val tieneNumero = contrasena.any { it.isDigit() }
    val tieneCaracterEspecial = contrasena.any { !it.isLetterOrDigit() }
    val coincidenContrasenas = contrasena.isNotEmpty() && contrasena == confirmarContrasena

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4EDE1).copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ElementoValidacion(texto = "8+ caracteres", esValido = tieneLongitudMin)
                ElementoValidacion(texto = "Contiene número", esValido = tieneNumero)
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ElementoValidacion(texto = "Carácter especial", esValido = tieneCaracterEspecial)
                ElementoValidacion(texto = "Coinciden", esValido = coincidenContrasenas)
            }
        }
    }
}

@Composable
private fun ElementoValidacion(
    texto: String,
    esValido: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (esValido) Icons.Default.CheckCircle else Icons.Default.Circle,
            contentDescription = null,
            tint = if (esValido) Color(0xFF4F644E) else Color(0xFF4F4446).copy(alpha = 0.3f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = texto,
            fontSize = 11.sp,
            color = Color(0xFF4F4446).copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}
