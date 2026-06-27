package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation

/**
 * CAMPO DE TEXTO PARA CONTRASEÑA
 */
@Composable
fun CampoTextoContrasena(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String = "Contraseña",
    sugerencia: String = "••••••••"
) {
    var contrasenaVisible by remember { mutableStateOf(false) }

    CampoTextoCristal(
        valor = valor,
        alCambiarValor = alCambiarValor,
        etiqueta = etiqueta,
        sugerencia = sugerencia,
        transformacionVisual = if (contrasenaVisible) VisualTransformation.None else PasswordVisualTransformation(),
        iconoFinal = {
            IconButton(onClick = { contrasenaVisible = !contrasenaVisible }) {
                Icon(
                    imageVector = if (contrasenaVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                    contentDescription = null,
                    tint = Color(0xFF78555E).copy(alpha = 0.6f)
                )
            }
        }
    )
}
