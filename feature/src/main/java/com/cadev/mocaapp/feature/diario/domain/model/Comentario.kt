package com.cadev.mocaapp.feature.diario.domain.model

import java.util.Date

/**
 * EL MODELO DE UN COMENTARIO
 * 
 * Qué hace:
 * Aquí definimos qué información guardamos de cada comentario que nuestra pareja 
 * deja en los recuerdos del diario: quién lo escribió, qué dice y cuándo.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir reacciones a los comentarios (ej: un corazón), debemos 
 * añadir una propiedad como `val reacciones: Map<String, String> = emptyMap()`.
 */
data class Comentario(
    val id: String = "",            // ID único del comentario
    val entradaId: String = "",     // A qué recuerdo pertenece
    val usuarioId: String = "",     // Quién lo escribió
    val nombreUsuario: String = "", // Nombre de la persona para mostrar rápido
    val texto: String = "",         // El mensaje escrito
    val creadoEn: Date = Date()     // Cuándo se publicó
)
