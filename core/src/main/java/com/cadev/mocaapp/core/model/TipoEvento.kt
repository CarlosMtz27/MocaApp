package com.cadev.mocaapp.core.model

enum class TipoEvento(val emoji: String, val etiqueta: String) {
    CITA("💑", "Cita"),
    ANIVERSARIO("💍", "Aniversario"),
    CUMPLEANOS("🎂", "Cumpleaños"),
    VIAJE("✈️", "Viaje"),
    SALIDA("🚶", "Salida"),
    CENA("🍽️", "Cena"),
    PICNIC("🧺", "Picnic"),
    LOGRO("🏆", "Logro"),
    ESPECIAL("⭐", "Especial"),
    OTRO("📅", "Otro")
}
