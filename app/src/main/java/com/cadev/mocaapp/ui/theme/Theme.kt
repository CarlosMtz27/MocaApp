package com.cadev.mocaapp.ui.theme

import androidx.compose.ui.graphics.Color
import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * PALETA ZEN (MODO CLARO)
 */
private val LightZenColors = lightColorScheme(
    primary = Color(0xFF78555E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD1DC),
    onPrimaryContainer = Color(0xFF7A5761),
    secondary = Color(0xFF635F40),
    onSecondary = Color.White,
    background = Color(0xFFFFF8EF),
    onBackground = Color(0xFF1E1B14),
    surface = Color(0xFFFFF8EF),
    onSurface = Color(0xFF1E1B14),
    surfaceVariant = Color(0xFFE9E2D6),
    onSurfaceVariant = Color(0xFF4F4446),
    outline = Color(0xFF817476),
    inverseSurface = Color(0xFF333028),
    inverseOnSurface = Color(0xFFF7F0E4),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

/**
 * PALETA ZEN (MODO OSCURO)
 */
private val DarkZenColors = darkColorScheme(
    primary = Color(0xFFE7BBC6),
    onPrimary = Color(0xFF452730),
    primaryContainer = Color(0xFF5E3E47),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFCEC7A2),
    onSecondary = Color(0xFF343116),
    background = Color(0xFF1E1B14),
    onBackground = Color(0xFFE0D9CE),
    surface = Color(0xFF1E1B14),
    onSurface = Color(0xFFE0D9CE),
    surfaceVariant = Color(0xFF4F4446),
    onSurfaceVariant = Color(0xFFD3C3C5),
    outline = Color(0xFF9C8D8F),
    inverseSurface = Color(0xFFE0D9CE),
    inverseOnSurface = Color(0xFF1E1B14),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

@Composable
fun MocaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkZenColors else LightZenColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MocaTypography,
        content     = content
    )
}
