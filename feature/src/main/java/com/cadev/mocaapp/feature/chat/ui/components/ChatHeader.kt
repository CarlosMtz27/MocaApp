package com.cadev.mocaapp.feature.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.ui.theme.*

import androidx.compose.foundation.isSystemInDarkTheme
import com.cadev.mocaapp.core.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHeader(
    nombre: String,
    foto: String?,
    estaActivo: Boolean,
    onBack: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme() || ThemeManager.isDarkTheme
    val surfaceColor = if (isDark) Color(0xFF1E1B14) else MocaSurface
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = surfaceColor.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = onSurfaceColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    Spacer(Modifier.width(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(primaryColor.copy(alpha = 0.2f))
                        ) {
                            if (!foto.isNullOrBlank()) {
                                AsyncImage(
                                    model = foto,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = nombre.take(1).uppercase(),
                                        style = OrganicTypography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = primaryColor
                                        )
                                    )
                                }
                            }
                        }
                        
                        Spacer(Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                text = nombre,
                                style = OrganicTypography.headlineMedium.copy(
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = primaryColor
                                )
                            )
                            Text(
                                text = if (estaActivo) "Activa ahora" else "Desconectada",
                                style = OrganicTypography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = primaryColor.copy(alpha = 0.8f),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
                
                IconButton(onClick = onMoreClick) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Más opciones",
                        tint = primaryColor
                    )
                }
            }
            HorizontalDivider(color = (if (isDark) Color.White.copy(alpha = 0.1f) else MocaSurfaceVariant.copy(alpha = 0.5f)))
        }
    }
}
