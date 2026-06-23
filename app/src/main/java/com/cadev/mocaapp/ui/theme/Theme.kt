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
 * EL TEMA VISUAL DE LA APLICACIÓN
 * 
 * Qué hace
 * Aquí se definen todos los colores que utiliza la aplicación. Hay dos grupos de colores 
 * uno para cuando el teléfono está en modo claro y otro para cuando está en modo oscuro.
 * 
 * Cómo añadir o cambiar cosas
 * Si quieres cambiar el color principal de la aplicación busca la palabra primary en los listados 
 * de abajo y cámbialo por el color que prefieras. Los nombres de los colores como RosaPrimario 
 * se definen en el archivo llamado Color.kt.
 */

// Este es el grupo de colores que se activan durante el día o en modo claro
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

// Este es el grupo de colores que se activan por la noche o en modo oscuro
private val DarkColors = darkColorScheme(
    primary          = RosaSecundario,
    onPrimary        = RosaOscuro,
    primaryContainer = RosaOscuro,
    onPrimaryContainer = RosaClaro,
    secondary        = RosaMedio,
    onSecondary      = Negro,
    background       = Color(0xFF1A1A1A), // Un tono gris muy oscuro casi negro
    onBackground     = Blanco,
    surface          = Color(0xFF2D2D2D),
    onSurface        = Blanco,
)

/**
 * Esta es la función que aplica los colores y el estilo a toda la aplicación
 */
@Composable
fun MocaAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Se elige la paleta de colores según si el móvil está en modo oscuro o no
    val colorScheme = if (darkTheme) DarkColors else LightColors

    // Este bloque sirve para cambiar el color de la barra superior donde sale la hora en el teléfono
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Se aplican los colores y las tipografías elegidas al resto de la aplicación
    MaterialTheme(
        colorScheme = colorScheme,
        typography  = MocaTypography,
        content     = content
    )
}
