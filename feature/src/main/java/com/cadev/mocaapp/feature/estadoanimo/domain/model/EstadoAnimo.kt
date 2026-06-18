package com.cadev.mocaapp.feature.estadoanimo.domain.model

import com.google.firebase.Timestamp

data class EstadoAnimoActual(
    val uid: String = "",
    val emoji: String = "",
    val fecha: String = "",   // "yyyy-MM-dd" — para resetear al día siguiente
    val actualizadaEn: Timestamp = Timestamp.now()
)

// Emojis disponibles (lista fija):
val EMOJIS_DISPONIBLES = listOf(
    "😊", "😍", "😴", "😔", "😤",
    "🥰", "😂", "🤗", "😌", "🥺"
)
