package com.cadev.mocaapp.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ESTE ARCHIVO DEFINE LOS BOTONES DEL MENÚ INFERIOR
 * 
 * Qué hace
 * Aquí se configuran los botones que aparecen en la parte de abajo de la aplicación. 
 * Cada opción tiene una ruta para saber a dónde ir, un dibujo para cuando está pulsada, 
 * otro para cuando no lo está y un nombre que el usuario puede leer.
 */
sealed class BottomNavItem(
    val route: String,
    val iconoSeleccionado: ImageVector,
    val iconoNoSeleccionado: ImageVector,
    val etiqueta: String
) {
    /**
     * Esta es la configuración para el botón de inicio con forma de corazón
     */
    object Home : BottomNavItem(
        route    = NavRoutes.Home.route,
        iconoSeleccionado = Icons.Filled.Favorite,
        iconoNoSeleccionado = Icons.Outlined.FavoriteBorder,
        etiqueta = "Inicio"
    )
    
    /**
     * Esta es la configuración para el botón que lleva al diario y al calendario
     */
    object Calendario : BottomNavItem(
        route    = NavRoutes.Calendario.route,
        iconoSeleccionado = Icons.Filled.CalendarMonth,
        iconoNoSeleccionado = Icons.Outlined.CalendarMonth,
        etiqueta = "Diario"
    )
    
    /**
     * Esta es la configuración para el botón que abre la mensajería privada
     */
    object Chat : BottomNavItem(
        route    = NavRoutes.Chat.route,
        iconoSeleccionado = Icons.Filled.Chat,
        iconoNoSeleccionado = Icons.Outlined.Chat,
        etiqueta = "Chat"
    )
    
    /**
     * Esta es la configuración para el botón que muestra los tests de pareja
     */
    object Cuestionarios : BottomNavItem(
        route    = NavRoutes.Cuestionarios.route,
        iconoSeleccionado = Icons.Filled.Quiz,
        iconoNoSeleccionado = Icons.Outlined.Quiz,
        etiqueta = "Tests"
    )
    
    /**
     * Esta es la configuración para el botón que permite ver la información personal
     */
    object Perfil : BottomNavItem(
        route    = NavRoutes.Perfil.route,
        iconoSeleccionado = Icons.Filled.Person,
        iconoNoSeleccionado = Icons.Outlined.Person,
        etiqueta = "Perfil"
    )
}
