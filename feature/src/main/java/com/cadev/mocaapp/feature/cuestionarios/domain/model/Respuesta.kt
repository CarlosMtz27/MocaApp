package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

data class Respuesta(
    val id: String = "",
    val cuestionarioId: String = "",
    val usuarioId: String = "",
    val preguntaId: String = "",
    val valor: String = "",       // texto, "si"/"no", "7", o opción elegida
    val creadoEn: Timestamp = Timestamp.now()
)