package com.cadev.mocaapp.feature.diario.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

/**
 * TIPOS DE ENTRADAS DEL DIARIO
 * 
 * Qué hace:
 * Define si lo que estamos guardando es un resumen de "Mi día" o un "Recuerdo" 
 * especial con fotos y vídeos. Cada uno tiene su propio icono y color.
 */
enum class TipoEntrada(
    val etiqueta: String,
    val icono: ImageVector,
    val colorHex: String
) {
    MI_DIA(
        etiqueta = "Mi día",
        icono = Icons.Default.EditNote,
        colorHex = "4CAF50"    // Verde
    ),
    RECUERDO(
        etiqueta = "Recuerdo",
        icono = Icons.Default.CameraAlt,
        colorHex = "2196F3"    // Azul
    )
}

/**
 * EL MODELO DEL RECUERDO (ENTRADA)
 * 
 * Qué hace:
 * Es la estructura principal que guarda todo lo que pasó en un momento: el título, 
 * la historia, las fotos, los vídeos y si se compartió con nuestra pareja.
 */
data class EntradaDiario(
    val id: String = "",                     // ID único del recuerdo
    val usuarioId: String = "",              // Quién lo creó
    val fecha: String = "",                  // Día en formato YYYY-MM-DD
    val tipo: String = TipoEntrada.MI_DIA.name, // Si es "Mi día" o "Recuerdo"
    val etiqueta: String = "",               // Categoría (ej. "VIAJE", "CITA")
    val titulo: String = "",                 // Nombre corto del momento
    val detalles: String = "",               // La historia completa escrita
    val emociones: List<String> = emptyList(),// Lista de sentimientos (IDs de Emocion)
    val fotos: List<String> = emptyList(),   // Enlaces a las imágenes en la nube
    val videos: List<String> = emptyList(),  // Enlaces a los vídeos en la nube
    val compartida: Boolean = false,         // Si nuestra pareja puede verlo
    val parejaId: String? = null,            // ID de la pareja que puede verlo
    val creadaEn: Timestamp = Timestamp.now() // Fecha y hora exacta de creación
)

/**
 * RESUMEN PARA EL CALENDARIO
 */
data class DiaCalendarioInfo(
    val tipos: List<String> = emptyList(), // Qué tipos de recuerdos hay ese día
    val primeraFoto: String? = null,       // La imagen de portada para el cuadrito
    val autores: Set<String> = emptySet()  // Quiénes escribieron ese día
)
