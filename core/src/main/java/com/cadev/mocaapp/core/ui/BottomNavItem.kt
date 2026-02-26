package com.cadev.mocaapp.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.ui.graphics.vector.ImageVector

// Cada tab del menu inferior tiene:
// una ruta, un icono y una etiqueta
sealed class BottomNavItem(
    val route: String,
    val icono: ImageVector,
    val etiqueta: String
) {
    object Home : BottomNavItem(
        route    = NavRoutes.Home.route,
        icono    = Icons.Filled.Favorite,
        etiqueta = "Inicio"
    )
    object Calendario : BottomNavItem(
        route    = NavRoutes.Calendario.route,
        icono    = Icons.Filled.CalendarMonth,
        etiqueta = "Calendario"
    )
    object Chat : BottomNavItem(
        route    = NavRoutes.Chat.route,
        icono    = Icons.Filled.Chat,
        etiqueta = "Chat"
    )
    object Cuestionarios : BottomNavItem(
        route    = NavRoutes.Cuestionarios.route,
        icono    = Icons.Filled.Quiz,
        etiqueta = "Quiz"
    )
    object Perfil : BottomNavItem(
        route    = NavRoutes.Perfil.route,
        icono    = Icons.Filled.Person,
        etiqueta = "Perfil"
    )
}