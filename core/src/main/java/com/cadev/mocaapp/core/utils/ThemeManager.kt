package com.cadev.mocaapp.core.utils

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * ESTE OBJETO GESTIONA LA APARIENCIA DE LA APLICACIÓN
 * 
 * Qué hace
 * Se encarga de guardar y recordar si el usuario prefiere ver la aplicación con colores 
 * claros o con el modo oscuro activado.
 */
object ThemeManager {
    /**
     * Esta variable indica si el modo oscuro está encendido o apagado en este momento
     */
    var isDarkTheme by mutableStateOf(false)
}
