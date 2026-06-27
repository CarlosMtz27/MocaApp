package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.utils.glassmorphism

/**
 * CAMPO DE TEXTO CON EFECTO CRISTAL
 */
@Composable
fun CampoTextoCristal(
    valor: String,
    alCambiarValor: (String) -> Unit,
    etiqueta: String,
    sugerencia: String,
    modificador: Modifier = Modifier,
    iconoFinal: @Composable (() -> Unit)? = null,
    transformacionVisual: VisualTransformation = VisualTransformation.None
) {
    val forma = RoundedCornerShape(28.dp)
    
    Column(modifier = modificador) {
        Text(
            text = etiqueta,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF78555E),
            modifier = Modifier.padding(start = 16.dp)
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = valor,
            onValueChange = alCambiarValor,
            placeholder = { Text(sugerencia, color = Color(0xFF78555E).copy(alpha = 0.4f)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .glassmorphism(shape = forma, alpha = 0.2f)
                .border(1.dp, Color(0xFF78555E).copy(alpha = 0.3f), forma),
            shape = forma,
            visualTransformation = transformacionVisual,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color(0xFF78555E),
                focusedTextColor = Color(0xFF78555E),
                unfocusedTextColor = Color(0xFF78555E)
            ),
            trailingIcon = iconoFinal,
            singleLine = true
        )
    }
}
