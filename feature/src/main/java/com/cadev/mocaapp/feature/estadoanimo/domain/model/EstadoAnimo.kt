package com.cadev.mocaapp.feature.estadoanimo.domain.model

import com.google.firebase.Timestamp
import com.cadev.mocaapp.feature.R

/**
 * ESTADO DE ÁNIMO ACTUAL
 * 
 * Qué hace:
 * Guarda qué emoji eligió el usuario, en qué fecha y a qué hora. 
 * Se usa para mostrar cómo se siente cada uno en la pantalla de inicio.
 */
data class EstadoAnimoActual(
    val uid: String = "",           // ID del usuario
    val emoji: String = "",         // ID del estado de ánimo (ej: "happy")
    val fecha: String = "",         // Día actual (YYYY-MM-DD)
    val actualizadaEn: Timestamp = Timestamp.now()
)

/**
 * INFORMACIÓN VISUAL DEL ESTADO
 * 
 * Qué hace:
 * Asocia un ID de texto con su icono profesional en XML y su nombre legible.
 */
data class MoodInfo(
    val id: String,
    val label: String,
    val iconRes: Int
)

/**
 * LISTA DE ESTADOS DISPONIBLES
 * 
 * Qué hace:
 * Aquí definimos qué estados de ánimo puede elegir la pareja con sus títulos.
 */
val MOODS_DISPONIBLES = listOf(
    MoodInfo("happy", "Feliz", R.drawable.ic_emocion_feliz),
    MoodInfo("love", "Enamorado", R.drawable.ic_reaccion_corazon),
    MoodInfo("sleepy", "Con sueño", R.drawable.ic_mood_sleepy), 
    MoodInfo("sad", "Triste", R.drawable.ic_reaccion_triste),
    MoodInfo("angry", "Enojado", R.drawable.ic_mood_angry), 
    MoodInfo("laugh", "Riendo", R.drawable.ic_reaccion_risa),
    MoodInfo("wow", "Asombrado", R.drawable.ic_reaccion_asombro),
    MoodInfo("cool", "Genial", R.drawable.ic_reaccion_chispa),
    MoodInfo("calm", "Tranquilo", R.drawable.ic_mood_relieved),
    MoodInfo("waiting", "Esperando", R.drawable.ic_reaccion_espera)
)

/**
 * TRADUCTOR DE EMOJIS VIEJOS Y BUSCADOR POR ID
 * 
 * Qué hace:
 * Nos ayuda a encontrar el icono y la etiqueta correspondiente a un ID.
 */
val MAPA_MOODS = mapOf(
    "happy" to MoodInfo("happy", "Feliz", R.drawable.ic_emocion_feliz),
    "love" to MoodInfo("love", "Enamorado", R.drawable.ic_reaccion_corazon),
    "sleepy" to MoodInfo("sleepy", "Con sueño", R.drawable.ic_mood_sleepy),
    "sad" to MoodInfo("sad", "Triste", R.drawable.ic_reaccion_triste),
    "angry" to MoodInfo("angry", "Enojado", R.drawable.ic_mood_angry),
    "laugh" to MoodInfo("laugh", "Riendo", R.drawable.ic_reaccion_risa),
    "wow" to MoodInfo("wow", "Asombrado", R.drawable.ic_reaccion_asombro),
    "cool" to MoodInfo("cool", "Genial", R.drawable.ic_reaccion_chispa),
    "calm" to MoodInfo("calm", "Tranquilo", R.drawable.ic_mood_relieved),
    "waiting" to MoodInfo("waiting", "Esperando", R.drawable.ic_reaccion_espera),
    "unknown" to MoodInfo("unknown", "Sin estado", R.drawable.ic_reaccion_duda),
    "" to MoodInfo("unknown", "Sin estado", R.drawable.ic_reaccion_duda),
    "❓" to MoodInfo("unknown", "Sin estado", R.drawable.ic_reaccion_duda),
    "😊" to MoodInfo("happy", "Feliz", R.drawable.ic_emocion_feliz)
)
