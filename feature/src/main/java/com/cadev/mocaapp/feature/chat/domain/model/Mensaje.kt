package com.cadev.mocaapp.feature.chat.domain.model

import com.google.firebase.Timestamp

enum class TipoMensaje {
    TEXTO, FOTO, VIDEO, AUDIO, VOZ, ENLACE
}

enum class EstadoMensaje {
    ENVIANDO, ENVIADO, ENTREGADO, LEIDO
}

data class Mensaje(
    val id: String = "",
    val conversacionId: String = "",
    val remitenteId: String = "",
    val texto: String = "",
    val tipo: String = TipoMensaje.TEXTO.name,
    val mediaUrl: String = "",
    val duracionSegundos: Int = 0,       // para audio/voz
    val estado: String = EstadoMensaje.ENVIANDO.name,
    val reacciones: Map<String, String> = emptyMap(), // usuarioId, emoji
    val creadoEn: Timestamp = Timestamp.now(),
    val editado: Boolean = false
)