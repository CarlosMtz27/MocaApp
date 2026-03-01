package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

data class Respuesta(
    val id: String = "",
    val cuestionarioId: String = "",
    val usuarioId: String = "",
    val preguntaId: String = "",
    val valor: String = "",
    val imagenUrl: String = "",   // para respuestas tipo FOTO
    val creadoEn: Timestamp = Timestamp.now()
)