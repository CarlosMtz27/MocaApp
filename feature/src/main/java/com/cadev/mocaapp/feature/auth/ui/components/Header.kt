package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.auth.ui.utils.glassmorphism
import androidx.compose.ui.graphics.RectangleShape

/**
 * CABECERA DE AUTENTICACIÓN
 */
@Composable
fun CabeceraAutenticacion(
    titulo: String = "Bienvenido",
    alVolver: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(top = 8.dp)
            .height(64.dp)
            .glassmorphism(shape = RectangleShape, alpha = 0.4f),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = alVolver) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF78555E)
                )
            }
            
            Text(
                text = titulo,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFF78555E)
            )
            
            Spacer(modifier = Modifier.width(48.dp))
        }
    }
}
