package com.cadev.mocaapp.feature.cuestionarios.domain.model

enum class TipoPregunta {
    OPCION_MULTIPLE,
    TEXTO_LIBRE,
    ESCALA,
    SI_NO,
    FOTO
}

data class Pregunta(
    val id: String = "",
    val texto: String = "",
    val tipo: String = TipoPregunta.OPCION_MULTIPLE.name,
    val opciones: List<String> = emptyList(),
    val imagenUrl: String = "",   // imagen opcional en la pregunta
    val puntos: Int = 10
)