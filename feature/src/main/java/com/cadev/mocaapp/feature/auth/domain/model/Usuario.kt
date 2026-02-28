package com.cadev.mocaapp.feature.auth.domain.model

data class Usuario(
    val id: String = "",
    val nombre: String = "",
    val email: String = "",
    val codigoPareja: String = "",
    val parejaId: String = "",
    val relacionId: String = "",
    val fotoUrl: String? = null,
    val fotoPerfil: String = ""
)