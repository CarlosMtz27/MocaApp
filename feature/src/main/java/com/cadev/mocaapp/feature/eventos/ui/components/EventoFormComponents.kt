package com.cadev.mocaapp.feature.eventos.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.core.ui.*

@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val colorCardBackground = if (isDark) Color(0xFF2D2921) else StitchCardBg
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 4.dp else 20.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = if (isDark) Color.Black else Color(0xFF78555E).copy(alpha = 0.05f),
                spotColor = if (isDark) Color.Black else Color(0xFF78555E).copy(alpha = 0.05f)
            )
            .background(colorCardBackground, RoundedCornerShape(28.dp))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = colorOnSurface
        )
        content()
    }
}

@Composable
fun MinimalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isTitle: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimaryContainer
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant
    val colorBorder = if (isFocused) colorPrimary else (if (isDark) Color.White.copy(alpha = 0.1f) else Color(0xFFD3C3C5).copy(alpha = 0.3f))
    val containerColor = if (isFocused) (if (isDark) Color.White.copy(alpha = 0.05f) else Color.White) else (if (isDark) Color.White.copy(alpha = 0.02f) else Color(0xFFF4EDE1))
    val shadowElevation = if (isFocused && !isDark) 8.dp else 0.dp

    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { 
            Text(
                text = placeholder, 
                style = if (isTitle) TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium) else LocalTextStyle.current,
                color = colorOnSurfaceVariant.copy(alpha = 0.7f)
            ) 
        },
        modifier = modifier
            .fillMaxWidth()
            .shadow(shadowElevation, RoundedCornerShape(if (isTitle) 32.dp else 24.dp))
            .border(1.dp, colorBorder, RoundedCornerShape(if (isTitle) 32.dp else 24.dp))
            .background(containerColor, RoundedCornerShape(if (isTitle) 32.dp else 24.dp)),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedTextColor = colorOnSurface,
            unfocusedTextColor = colorOnSurface
        ),
        textStyle = if (isTitle) TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium) else TextStyle(fontSize = 16.sp),
        singleLine = singleLine,
        minLines = minLines,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(if (isTitle) 32.dp else 24.dp)
    )
}

@Composable
fun DateTimeInput(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false
) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else StitchOnSurface
    val colorSurfaceVariant = if (isDark) Color.White.copy(alpha = 0.05f) else Color(0xFFF4EDE1)

    Box(
        modifier = modifier
            .height(56.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(colorSurfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(icon, contentDescription = null, tint = colorPrimary, modifier = Modifier.size(20.dp))
            Text(
                text = value,
                color = colorOnSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}
