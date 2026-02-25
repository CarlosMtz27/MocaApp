package com.cadev.mocaapp.feature.pareja.domain.model

import java.util.Date

data class Relacion(
    val id: String = "",
    val usuario1Id: String = "",
    val usuario2Id: String = "",
    val fechaInicio: Date = Date(),
    val estado: String = "activa"
)