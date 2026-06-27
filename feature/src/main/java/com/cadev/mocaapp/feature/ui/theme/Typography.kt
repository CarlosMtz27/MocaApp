package com.cadev.mocaapp.feature.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// NOTA: Para usar Plus Jakarta Sans y Playfair Display deberías añadir las fuentes a res/font.
// Por ahora usaremos familias genéricas equivalentes si no están presentes.
val SerifFontFamily = FontFamily.Serif
val SansFontFamily = FontFamily.Default

val OrganicTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = SerifFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 36.sp, // Para el mes en el calendario
        lineHeight = 40.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 28.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    labelMedium = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.05.sp
    ),
    labelSmall = TextStyle(
        fontFamily = SansFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.03.sp
    )
)
