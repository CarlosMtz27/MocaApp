package com.cadev.mocaapp.feature.cuestionarios.domain.model

import com.google.firebase.Timestamp

enum class CategoriaCuestionario(val emoji: String, val etiqueta: String) {
    COMPATIBILIDAD("💑", "Compatibilidad"),
    CONOCERSE("🧠", "Conócete mejor"),
    DIVERTIDO("😄", "Divertido"),
    PROFUNDO("💭", "Reflexivo"),
    PERSONALIZADO("✏️", "Personalizado")
}

data class Cuestionario(
    val id: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val categoria: String = CategoriaCuestionario.COMPATIBILIDAD.name,
    val preguntas: List<Pregunta> = emptyList(),
    val creadoPor: String = "sistema",  // "sistema" o uid del usuario
    val relacionId: String = "",
    val creadoEn: Timestamp = Timestamp.now(),
    val totalPuntos: Int = 0
)