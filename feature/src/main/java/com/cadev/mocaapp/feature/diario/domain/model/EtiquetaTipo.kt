package com.cadev.mocaapp.feature.diario.domain.model

// Cada tipo de entrada tiene sus propias etiquetas
// El usuario también puede escribir una personalizada

enum class EtiquetaEvento(val etiqueta: String, val emoji: String) {
    VIAJE("Viaje", "✈️"),
    SALIDA("Salida", "🚶"),
    CENA("Cena", "🍽️"),
    CITA("Cita", "💑"),
    PICNIC("Picnic", "🧺"),
    PERSONALIZADA("Otra...", "✏️")
}

enum class EtiquetaDiaEspecial(val etiqueta: String, val emoji: String) {
    ANIVERSARIO("Aniversario", "💕"),
    CUMPLEANOS("Cumpleaños", "🎂"),
    LOGRO("Logro", "🏆"),
    PERSONALIZADA("Otra...", "✏️")
}