package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.utils.glassmorphism

/**
 * NAVEGACIÓN INFERIOR DE AUTENTICACIÓN
 */
@Composable
fun NavegacionInferiorAutenticacion(
    esLogin: Boolean,
    alNavegar: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .glassmorphism(shape = CircleShape, alpha = 0.5f)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ElementoTabAutenticacion(
                etiqueta = "Entrar",
                icono = Icons.AutoMirrored.Filled.Login,
                estaSeleccionado = esLogin,
                alHacerClick = { alNavegar(true) }
            )
            ElementoTabAutenticacion(
                etiqueta = "Registro",
                icono = Icons.Default.PersonAdd,
                estaSeleccionado = !esLogin,
                alHacerClick = { alNavegar(false) }
            )
        }
    }
}

@Composable
private fun ElementoTabAutenticacion(
    etiqueta: String,
    icono: ImageVector,
    estaSeleccionado: Boolean,
    alHacerClick: () -> Unit
) {
    val colorFondo = if (estaSeleccionado) Color(0xFF78555E).copy(alpha = 0.1f) else Color.Transparent
    val colorContenido = if (estaSeleccionado) Color(0xFF78555E) else Color(0xFF4F4446).copy(alpha = 0.7f)

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(colorFondo)
            .clickable(onClick = alHacerClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = etiqueta,
            tint = colorContenido,
            modifier = Modifier.size(20.dp)
        )
        if (estaSeleccionado) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = etiqueta,
                color = colorContenido,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
