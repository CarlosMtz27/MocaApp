package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.HeartLogo

/**
 * SECCIÓN DEL LOGO
 *
 * Muestra el logo del corazón junto con el título y subtítulo de la aplicación.
 * Permite alternar entre un diseño centrado (para Login) o en fila (para Registro).
 */
@Composable
fun SeccionLogo(
    titulo: String = "MocaApp",
    subtitulo: String = "Tu diario de pareja",
    estaCentrado: Boolean = false
) {
    if (estaCentrado) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeartLogo(size = 96.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = titulo,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF78555E),
                letterSpacing = (-0.5).sp,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitulo,
                fontSize = 16.sp,
                color = Color(0xFF4F4446).copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            HeartLogo(size = 72.dp)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = titulo,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF78555E),
                    letterSpacing = (-0.5).sp,
                    lineHeight = 28.sp
                )
                Text(
                    text = subtitulo,
                    fontSize = 14.sp,
                    color = Color(0xFF4F4446).copy(alpha = 0.7f),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}
