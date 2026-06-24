package com.cadev.mocaapp.feature.diario.domain.model

import com.google.firebase.Timestamp

/**
 * EL MODELO DE UN COMENTARIO
 * 
 * Qué hace:
 * Aquí definimos qué información guardamos de cada comentario que nuestra pareja 
 * deja en los recuerdos del diario: quién lo escribió, qué dice y cuándo.
 */
data class Comentario(
    val id: String = "",            // ID único del comentario
    val entradaId: String = "",     // A qué recuerdo pertenece
    val usuarioId: String = "",     // Quién lo escribió
    val nombreUsuario: String = "", // Nombre de la persona para mostrar rápido
    val texto: String = "",         // El mensaje escrito
    val relacionId: String = "",    // ID de la relación para permisos
    val creadoEn: Timestamp = Timestamp.now() // Cuándo se publicó
)
