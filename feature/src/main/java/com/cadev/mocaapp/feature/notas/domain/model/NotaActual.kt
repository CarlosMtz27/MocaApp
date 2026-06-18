package com.cadev.mocaapp.feature.notas.domain.model

import com.google.firebase.Timestamp

data class NotaActual(
    val texto: String = "",
    val autorId: String = "",
    val nombreAutor: String = "",
    val actualizadaEn: Timestamp = Timestamp.now()
)
