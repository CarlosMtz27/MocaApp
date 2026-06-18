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

// Cada tab del menu inferior tiene:
// una ruta, un icono y una etiqueta
sealed class BottomNavItem(
    val route: String,
    val iconoSeleccionado: ImageVector,
    val iconoNoSeleccionado: ImageVector,
    val etiqueta: String
) {
    object Home : BottomNavItem(
        route    = NavRoutes.Home.route,
        iconoSeleccionado = Icons.Filled.Favorite,
        iconoNoSeleccionado = Icons.Outlined.FavoriteBorder,
        etiqueta = "Inicio"
    )
    object Calendario : BottomNavItem(
        route    = NavRoutes.Calendario.route,
        iconoSeleccionado = Icons.Filled.CalendarMonth,
        iconoNoSeleccionado = Icons.Outlined.CalendarMonth,
        etiqueta = "Diario"
    )
    object Chat : BottomNavItem(
        route    = NavRoutes.Chat.route,
        iconoSeleccionado = Icons.Filled.Chat,
        iconoNoSeleccionado = Icons.Outlined.Chat,
        etiqueta = "Chat"
    )
    object Cuestionarios : BottomNavItem(
        route    = NavRoutes.Cuestionarios.route,
        iconoSeleccionado = Icons.Filled.Quiz,
        iconoNoSeleccionado = Icons.Outlined.Quiz,
        etiqueta = "Tests"
    )
    object Perfil : BottomNavItem(
        route    = NavRoutes.Perfil.route,
        iconoSeleccionado = Icons.Filled.Person,
        iconoNoSeleccionado = Icons.Outlined.Person,
        etiqueta = "Perfil"
    )
}