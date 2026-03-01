package com.cadev.mocaapp.feature.cuestionarios.domain.model

enum class EstadoCuestionario {
    NINGUNO,// nadie ha respondido
    YO_RESPONDÍ,// solo yo respondi, pareja esperando
    PAREJA_RESPONDIÓ,// solo pareja respondio, mi turno
    AMBOS// completado por ambos
}