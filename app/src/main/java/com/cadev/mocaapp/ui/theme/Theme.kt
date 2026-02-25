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


// ── Paleta modo claro ────────────────────────────────────────
private val LightColors = lightColorScheme(
    primary          = RosaPrimario,
    onPrimary        = Blanco,
    primaryContainer = RosaClaro,
    onPrimaryContainer = RosaOscuro,
    secondary        = RosaSecundario,
    onSecondary      = Blanco,
    background       = Blanco,
    onBackground     = Negro,
    surface          = GrisClaro,
    onSurface        = Negro,
    surfaceVariant   = RosaClaro,
)

// ── Paleta modo oscuro ───────────────────────────────────────
private val DarkColors = darkColorScheme(
    primary          = RosaSecundario,
    onPrimary        = RosaOscuro,
    primaryContainer = RosaOscuro,
    onPrimaryContainer = RosaClaro,
    secondary        = RosaMedio,
    onSecondary      = Negro,
    background       = Color(0xFF1A1A1A),  // casi negro
    onBackground     = Blanco,
    surface          = Color(0xFF2D2D2D),
    onSurface        = Blanco,
)

@Composable
fun MocaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    // Cambia el color de la barra de estado del teléfono
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MocaTypography,
        content     = content
    )
}