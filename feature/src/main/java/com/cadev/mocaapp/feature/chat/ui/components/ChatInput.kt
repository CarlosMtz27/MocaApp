package com.cadev.mocaapp.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

import androidx.compose.foundation.isSystemInDarkTheme
import com.cadev.mocaapp.core.utils.ThemeManager

@Composable
fun ChatInput(
    texto: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onMediaClick: () -> Unit,
    onMicClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme() || ThemeManager.isDarkTheme
    val containerColor = if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val primaryContainer = if (isDark) Color(0xFF5E3E47) else MocaPrimaryContainer

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .imePadding(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, CircleShape, spotColor = primaryColor.copy(alpha = 0.1f))
                    .background(containerColor, CircleShape)
                    .border(1.dp, (if (isDark) Color.White.copy(alpha = 0.05f) else MocaSurfaceVariant.copy(alpha = 0.5f)), CircleShape)
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onMediaClick,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Multimedia",
                        tint = onSurfaceColor.copy(alpha = 0.6f)
                    )
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (texto.isEmpty()) {
                        Text(
                            text = "Escribe un mensaje...",
                            style = OrganicTypography.bodyMedium.copy(
                                fontSize = 14.sp,
                                color = onSurfaceColor.copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    BasicTextField(
                        value = texto,
                        onValueChange = onTextoChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = OrganicTypography.bodyMedium.copy(
                            fontSize = 14.sp,
                            color = onSurfaceColor
                        ),
                        cursorBrush = SolidColor(primaryColor),
                        decorationBox = { innerTextField ->
                            innerTextField()
                        }
                    )
                }
                
                IconButton(
                    onClick = onMicClick,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Nota de voz",
                        tint = onSurfaceColor.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(Modifier.width(4.dp))
                
                IconButton(
                    onClick = onEnviar,
                    enabled = texto.isNotBlank(),
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (texto.isNotBlank()) primaryContainer else (if (isDark) Color.White.copy(alpha = 0.05f) else MocaSurfaceVariant.copy(alpha = 0.3f)),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Enviar",
                        tint = if (texto.isNotBlank()) (if (isDark) Color.White else MocaOnPrimaryContainer) else onSurfaceColor.copy(alpha = 0.3f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
