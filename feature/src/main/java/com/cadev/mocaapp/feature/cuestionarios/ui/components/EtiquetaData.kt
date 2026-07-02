package com.cadev.mocaapp.feature.cuestionarios.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class EtiquetaData(
    val nombre: String,
    val icono: ImageVector,
    val colorFondo: Color,
    val colorTexto: Color
)

fun obtenerEtiquetaInfo(nombre: String): EtiquetaData {
    return when (nombre.lowercase()) {
        "divertido" -> EtiquetaData(nombre, Icons.Default.SentimentSatisfied, Color(0xFFE8F5E9), Color(0xFF2E7D32))
        "hot" -> EtiquetaData(nombre, Icons.Default.Whatshot, Color(0xFFFFEBEE), Color(0xFFC62828))
        "profundo" -> EtiquetaData(nombre, Icons.Default.AutoAwesome, Color(0xFFF3E5F5), Color(0xFF7B1FA2))
        "romántico" -> EtiquetaData(nombre, Icons.Default.Favorite, Color(0xFFFCE4EC), Color(0xFFC2185B))
        "reto" -> EtiquetaData(nombre, Icons.Default.EmojiEvents, Color(0xFFFFF3E0), Color(0xFFE65100))
        "picante" -> EtiquetaData(nombre, Icons.Default.LocalFireDepartment, Color(0xFFFFFDE7), Color(0xFFF57F17))
        else -> EtiquetaData(nombre, Icons.Default.Tag, Color(0xFFF5F5F5), Color(0xFF616161))
    }
}
