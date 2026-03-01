package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

data class ResultadoCuestionario(
    val id: String = "",
    val cuestionarioId: String = "",
    val relacionId: String = "",
    val puntajeUsuario1: Int = 0,
    val puntajeUsuario2: Int = 0,
    val puntajeCompatibilidad: Int = 0,  // % de respuestas que coinciden
    val completadoPor: List<String> = emptyList(),  // uids que completaron
    val creadoEn: Timestamp = Timestamp.now()
)