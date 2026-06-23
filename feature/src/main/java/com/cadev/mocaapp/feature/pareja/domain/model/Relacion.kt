package com.cadev.mocaapp.feature.pareja.domain.model

import java.util.Date

/**
 * NUESTRO MODELO DE RELACIÓN
 * 
 * Qué hace:
 * Define la estructura de la unión entre dos personas. Guarda quiénes son, 
 * cuándo empezaron y si la relación está activa.
 */
data class Relacion(
    val id: String = "",           // ID único de nuestra relación
    val usuario1Id: String = "",    // ID de uno de nosotros
    val usuario2Id: String = "",    // ID del otro
    val fechaInicio: Date = Date(), // El día que empezó nuestra historia
    val estado: String = "activa"   // Situación actual (activa, pausada, etc.)
)
