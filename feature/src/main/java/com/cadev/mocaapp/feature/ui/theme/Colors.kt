package com.cadev.mocaapp.feature.ui.theme

import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.cadev.mocaapp.core.utils.ThemeManager

/**
 * Función de conveniencia para obtener si estamos en modo oscuro
 */
@Composable
fun esModoOscuro(): Boolean = isSystemInDarkTheme() || ThemeManager.isDarkTheme

// COLORES EXTRAÍDOS FIELMENTE DEL HTML/TAILWIND CONFIG
val MocaPrimary = Color(0xFF78555E)
val MocaAccentPink = Color(0xFFA1455A) 
val MocaOnPrimary = Color(0xFFFFFFFF)
val MocaPrimaryContainer = Color(0xFFFFD1DC)
val MocaOnPrimaryContainer = Color(0xFF7A5761)

// ... resto de colores ...
val MocaDarkBackground = Color(0xFF1E1B14)
val MocaDarkSurface = Color(0xFF1E1B14)
val MocaDarkOnSurface = Color(0xFFE0D9CE)
val MocaDarkPrimary = Color(0xFFE7BBC6)
val MocaDarkPrimaryContainer = Color(0xFF5E3E47)
val MocaDarkOnPrimaryContainer = Color(0xFFFFD9E2)


val MocaSecondary = Color(0xFF635F40)
val MocaOnSecondary = Color(0xFFFFFFFF)
val MocaSecondaryContainer = Color(0xFFE8E0BA)
val MocaOnSecondaryContainer = Color(0xFF686344)

val MocaTertiary = Color(0xFF4F644E)
val MocaOnTertiary = Color(0xFFFFFFFF)
val MocaTertiaryContainer = Color(0xFFCBE3C7)
val MocaOnTertiaryContainer = Color(0xFF516650)

val MocaBackground = Color(0xFFFFF8EF)
val MocaOnBackground = Color(0xFF1E1B14)
val MocaSurface = Color(0xFFFFF8EF)
val MocaOnSurface = Color(0xFF1E1B14)
val MocaOnSurfaceVariant = Color(0xFF4F4446)

val MocaError = Color(0xFFBA1A1A)
val MocaOnError = Color(0xFFFFFFFF)

val MocaSurfaceContainer = Color(0xFFF4EDE1)
val MocaSurfaceContainerLow = Color(0xFFFAF3E7)
val MocaSurfaceVariant = Color(0xFFE9E2D6)
val MocaPrimaryFixedDim = Color(0xFFE7BBC6)
val MocaOnPrimaryFixedVariant = Color(0xFF5E3E47)
val MocaTertiaryFixed = Color(0xFFD1E9CD)
val MocaSurfaceContainerHighest = Color(0xFFE9E2D6)
val MocaSurfaceContainerHigh = Color(0xFFEEE7DB)
val MocaSurfaceContainerLowest = Color(0xFFFFFFFF)
val MocaOutline = Color(0xFF817476)
val MocaOutlineVariant = Color(0xFFD3C3C5)
