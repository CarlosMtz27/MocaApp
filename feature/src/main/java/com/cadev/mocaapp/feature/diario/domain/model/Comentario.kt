package com.cadev.mocaapp.feature.diario.domain.model

import java.util.Date

data class Comentario(
    val id: String = "",
    val entradaId: String = "",
    val usuarioId: String = "",
    val nombreUsuario: String = "",
    val texto: String = "",
    val creadoEn: Date = Date()
)