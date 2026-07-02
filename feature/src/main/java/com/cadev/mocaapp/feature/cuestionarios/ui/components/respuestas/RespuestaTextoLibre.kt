package com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun RespuestaTextoLibre(
    texto: String,
    onTextoChanged: (String) -> Unit,
    placeholder: String = "Escribe tu respuesta aquí...",
    maxChars: Int = 500
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorBg = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLow

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.End
    ) {
        OutlinedTextField(
            value = texto,
            onValueChange = { 
                if (it.length <= maxChars) {
                    onTextoChanged(it)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp),
            placeholder = { 
                Text(
                    text = placeholder,
                    color = colorOnSurface.copy(alpha = 0.6f),
                    fontSize = 15.sp
                ) 
            },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colorBg,
                unfocusedContainerColor = colorBg,
                focusedBorderColor = MocaAccentPink,
                unfocusedBorderColor = MocaOutlineVariant.copy(alpha = 0.5f),
                focusedTextColor = colorOnSurface,
                unfocusedTextColor = colorOnSurface
            ),
            textStyle = MaterialTheme.typography.bodyMedium.copy(fontSize = 15.sp, color = colorOnSurface),
            maxLines = 5
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = "${texto.length} / $maxChars",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = if (texto.length >= maxChars * 0.9) MocaError else MocaOutline,
            modifier = Modifier.padding(end = 8.dp)
        )
    }
}
