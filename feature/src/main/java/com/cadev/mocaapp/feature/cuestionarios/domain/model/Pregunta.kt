package com.cadev.mocaapp.feature.cuestionarios.domain.model

enum class TipoPregunta {
    OPCION_MULTIPLE,
    TEXTO_LIBRE,
    ESCALA,        // 1-10
    SI_NO
}

data class Pregunta(
    val id: String = "",
    val texto: String = "",
    val tipo: String = TipoPregunta.OPCION_MULTIPLE.name,
    val opciones: List<String> = emptyList(),  // para OPCION_MULTIPLE
    val puntos: Int = 10                       // puntos si coincide con pareja
)