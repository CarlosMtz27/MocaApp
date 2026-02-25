package com.cadev.mocaapp.feature.auth.domain.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val fotoUrl: String? = null,
    val codigoPareja: String = "",
    val parejaId: String? = null,
    val relacionId: String? = null
)