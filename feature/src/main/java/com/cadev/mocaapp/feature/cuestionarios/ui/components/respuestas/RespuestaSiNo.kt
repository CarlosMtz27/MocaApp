package com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun RespuestaSiNo(
    respuesta: String,
    onRespuestaSelected: (String) -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorCardBg = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLowest

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Opción SÍ
        val esSi = respuesta == "si"
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = if (isDark) 0.dp else 4.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                .clip(RoundedCornerShape(20.dp))
                .background(if (esSi) colorPrimary.copy(alpha = 0.2f) else colorCardBg)
                .border(
                    width = 2.dp,
                    color = if (esSi) colorPrimary else MocaOutlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onRespuestaSelected("si") }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.ThumbUp,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = if (esSi) colorPrimary else MocaOutline
                )
                Text(
                    text = "Sí",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (esSi) colorPrimary else colorOnSurface
                )
            }
            
            if (esSi) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 12.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isDark) Color.Black else MocaOnPrimary
                    )
                }
            }
        }

        // Opción NO
        val esNo = respuesta == "no"
        Box(
            modifier = Modifier
                .weight(1f)
                .shadow(elevation = if (isDark) 0.dp else 4.dp, shape = RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.04f))
                .clip(RoundedCornerShape(20.dp))
                .background(if (esNo) colorPrimary.copy(alpha = 0.2f) else colorCardBg)
                .border(
                    width = 2.dp,
                    color = if (esNo) colorPrimary else MocaOutlineVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                )
                .clickable { onRespuestaSelected("no") }
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(
                    imageVector = Icons.Default.ThumbDown,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = if (esNo) colorPrimary else MocaOutline
                )
                Text(
                    text = "No",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (esNo) colorPrimary else colorOnSurface
                )
            }
            
            if (esNo) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 12.dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(colorPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isDark) Color.Black else MocaOnPrimary
                    )
                }
            }
        }
    }
}
