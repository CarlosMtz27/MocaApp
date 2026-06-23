package com.cadev.mocaapp.feature.cuestionarios.domain.model

/**
 * ESTADOS DE UN TEST PARA LA PAREJA
 * 
 * Qué hace:
 * Nos dice en qué situación se encuentra un test específico: si nadie ha empezado, 
 * si solo uno ha respondido o si ya lo han terminado los dos para ver resultados.
 */
enum class EstadoCuestionario {
    NINGUNO,          // El test está nuevo, nadie ha respondido todavía
    YO_RESPONDÍ,      // Yo ya terminé, ahora estoy esperando a mi pareja
    PAREJA_RESPONDIÓ, // Mi pareja ya terminó, ahora es mi turno de responder
    AMBOS             // ¡Listo! Los dos respondimos y ya podemos ver el resultado
}
