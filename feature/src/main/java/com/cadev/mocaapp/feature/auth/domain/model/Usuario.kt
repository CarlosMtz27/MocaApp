package com.cadev.mocaapp.feature.auth.domain.model

/**
 * FICHA DE DATOS DEL USUARIO
 * 
 * Qué hace:
 * Define toda la información que guardamos de cada usuario.
 */
data class Usuario(
    val id: String = "",           
    val nombre: String = "",       
    val email: String = "",        
    val codigoPareja: String = "", 
    val parejaId: String = "",     
    val relacionId: String = "",   
    val fotoUrl: String? = null,   
    val fotoPerfil: String = "",
    val oneSignalPlayerId: String? = null,
    val ubicacion: Map<String, Any>? = null // Añadido para evitar errores de mapeo con el GPS
)
