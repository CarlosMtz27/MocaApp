package com.cadev.mocaapp.feature.chat.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

import androidx.compose.foundation.isSystemInDarkTheme
import com.cadev.mocaapp.core.utils.ThemeManager

@Composable
fun TypingIndicator(
    nombre: String
) {
    val isDark = isSystemInDarkTheme() || ThemeManager.isDarkTheme
    val surfaceColor = if (isDark) Color(0xFF2D2921) else MocaSurfaceContainerLowest
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurfaceVariant
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (isDark) Color.Black.copy(alpha = 0.3f) else MocaSurfaceVariant)
        )
        
        Spacer(Modifier.width(12.dp))
        
        Surface(
            modifier = Modifier
                .shadow(2.dp, RoundedCornerShape(24.dp))
                .border(
                    width = 1.dp,
                    color = (if (isDark) Color.White.copy(alpha = 0.05f) else MocaTertiaryContainer.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(
                topStart = 24.dp,
                topEnd = 24.dp,
                bottomEnd = 24.dp,
                bottomStart = 4.dp
            ),
            color = surfaceColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$nombre está escribiendo",
                    style = OrganicTypography.bodyMedium.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                )
                
                TypingDots(primaryColor)
            }
        }
    }
}

@Composable
private fun TypingDots(tint: Color) {
    val infiniteTransition = rememberInfiniteTransition(label = "typing")
    
    val dotScale1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.0f at 0 with LinearOutSlowInEasing
                1.0f at 400 with LinearOutSlowInEasing
                0.0f at 800
                0.0f at 1400
            }
        ),
        label = "dot1"
    )
    
    val dotScale2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.0f at 200 with LinearOutSlowInEasing
                1.0f at 600 with LinearOutSlowInEasing
                0.0f at 1000
                0.0f at 1400
            }
        ),
        label = "dot2"
    )
    
    val dotScale3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1400
                0.0f at 400 with LinearOutSlowInEasing
                1.0f at 800 with LinearOutSlowInEasing
                0.0f at 1200
                0.0f at 1400
            }
        ),
        label = "dot3"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.padding(top = 4.dp)
    ) {
        TypingDot(scale = dotScale1, tint = tint)
        TypingDot(scale = dotScale2, tint = tint)
        TypingDot(scale = dotScale3, tint = tint)
    }
}

@Composable
private fun TypingDot(scale: Float, tint: Color) {
    Box(
        modifier = Modifier
            .size(6.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.5f + (scale * 0.5f)))
    )
}
