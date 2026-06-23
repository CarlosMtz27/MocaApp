package com.cadev.mocaapp.feature.diario.domain.model

import com.cadev.mocaapp.feature.R

/**
 * NUESTRO MAPA DE SENTIMIENTOS
 * 
 * Qué hace:
 * Define las emociones que podemos elegir al guardar un recuerdo. Cada una 
 * tiene un nombre legible y su propio icono profesional en XML.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir una emoción nueva (ej: "Divertido"), debemos añadir una 
 * línea como: DIVERTIDO("Divertido", R.drawable.ic_reaccion_risa)
 */
enum class Emocion(val etiqueta: String, val iconRes: Int) {
    FELIZ("Feliz", R.drawable.ic_emocion_feliz),
    AMADO("Amado", R.drawable.ic_reaccion_corazon),
    EMOCIONADO("Emocionado", R.drawable.ic_reaccion_fuego),
    TRANQUILO("Tranquilo", R.drawable.ic_reaccion_risa),
    NOSTALGICO("Nostálgico", R.drawable.ic_reaccion_triste),
    TRISTE("Triste", R.drawable.ic_reaccion_triste),
    AGRADECIDO("Agradecido", R.drawable.ic_reaccion_like),
    AVENTURERO("Aventurero", R.drawable.ic_reaccion_chispa)
}


