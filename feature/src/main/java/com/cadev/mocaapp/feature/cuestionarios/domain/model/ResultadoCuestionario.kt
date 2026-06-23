package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

/**
 * EL RESULTADO FINAL DEL TEST
 * 
 * Qué hace:
 * Guarda los puntos de cada uno y calcula cuánto se parecen sus respuestas 
 * (porcentaje de compatibilidad) después de que ambos terminan el test.
 */
data class ResultadoCuestionario(
    val id: String = "",                          // ID único del resultado
    val cuestionarioId: String = "",              // Cuál test se evaluó
    val relacionId: String = "",                  // A qué pareja pertenece
    val puntajeUsuario1: Int = 0,                 // Puntos de la primera persona
    val puntajeUsuario2: Int = 0,                 // Puntos de la segunda persona
    val puntajeCompatibilidad: Int = 0,           // % de respuestas que coinciden (0 a 100)
    val completadoPor: List<String> = emptyList(),// Lista de IDs de los que terminaron
    val creadoEn: Timestamp = Timestamp.now()     // Fecha en que se generó el resultado
)
