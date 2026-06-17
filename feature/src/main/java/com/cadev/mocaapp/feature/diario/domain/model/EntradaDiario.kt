package com.cadev.mocaapp.feature.diario.domain.model

import java.util.Date

enum class TipoEntrada(
    val etiqueta: String,
    val emoji: String,
    val colorHex: String
) {
    MI_DIA(
        etiqueta = "Mi día",
        emoji = "📝",
        colorHex = "C2185B"    // rosa
    ),
    RECUERDO(
        etiqueta = "Recuerdo",
        emoji = "📸",
        colorHex = "7B1FA2"    // lila
    )
}

data class EntradaDiario(
    val id: String = "",
    val usuarioId: String = "",
    val fecha: String = "",          // formato YYYY-MM-DD
    val tipo: String = TipoEntrada.MI_DIA.name,
    val etiqueta: String = "",       // Aquí guardaremos el TipoEvento (ej. "VIAJE")
    val titulo: String = "",
    val detalles: String = "",
    val emociones: List<String> = emptyList(),
    val fotos: List<String> = emptyList(),
    val videos: List<String> = emptyList(),
    val compartida: Boolean = false,
    val parejaId: String? = null,
    val creadaEn: Date = Date()
)
