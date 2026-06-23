package com.cadev.mocaapp.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * ESTE ARCHIVO DEFINE LAS CATEGORÍAS DE LOS EVENTOS
 * 
 * Qué hace
 * Aquí se listan todos los tipos de eventos que la pareja puede crear en su calendario. 
 * Cada tipo tiene asociado un icono visual y un nombre para que sea fácil de identificar.
 * 
 * Por qué usamos iconos en lugar de emojis
 * Usar iconos vectoriales es una mejor práctica porque se ven igual en todos los teléfonos, 
 * son más elegantes y se adaptan mejor a los colores de la aplicación.
 */
enum class TipoEvento(val icono: ImageVector, val etiqueta: String) {
    CITA(Icons.Default.Favorite, "Cita"),
    ANIVERSARIO(Icons.Default.Stars, "Aniversario"),
    CUMPLEANOS(Icons.Default.Cake, "Cumpleaños"),
    VIAJE(Icons.Default.Flight, "Viaje"),
    SALIDA(Icons.AutoMirrored.Filled.DirectionsWalk, "Salida"),
    CENA(Icons.Default.Restaurant, "Cena"),
    PICNIC(Icons.Default.BakeryDining, "Picnic"),
    LOGRO(Icons.Default.EmojiEvents, "Logro"),
    ESPECIAL(Icons.Default.AutoAwesome, "Especial"),
    OTRO(Icons.Default.Event, "Otro")
}
