package com.cadev.mocaapp.feature.chat.domain.model

import com.google.firebase.Timestamp

/**
 * TIPOS DE REACCIONES OFICIALES
 * 
 * Qué hace:
 * Define los identificadores fijos para las reacciones. Usamos texto (id) 
 * en lugar de emojis directos para que el código sea más robusto y profesional.
 */
enum class ReaccionType(val id: String) {
    CORAZON("heart"),
    RISA("laugh"),
    ASOMBRO("wow"),
    TRISTE("sad"),
    LIKE("like"),
    FUEGO("fire");

    companion object {
        /**
         * Busca una reacción por su ID (útil al leer de la base de datos).
         */
        fun fromId(id: String): ReaccionType? = values().find { it.id == id }
    }
}

/**
 * TIPOS DE MENSAJES QUE PODEMOS ENVIAR
 */
enum class TipoMensaje {
    TEXTO, FOTO, VIDEO, AUDIO, VOZ, ENLACE
}

/**
 * ESTADOS POR LOS QUE PASA UN MENSAJE
 */
enum class EstadoMensaje {
    ENVIANDO,  // El mensaje está saliendo del móvil
    ENVIADO,   // Ya llegó a la base de datos
    ENTREGADO, // Ya llegó al móvil de la pareja
    LEIDO      // La pareja ya abrió el chat y lo vio
}

/**
 * NUESTRO MODELO DE MENSAJE
 * 
 * Qué hace:
 * Aquí definimos qué información guardamos de cada burbuja de chat: quién lo envió, 
 * qué dice, si tiene una foto o audio, y cómo reaccionó la pareja.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir una función nueva (ej: mensajes que se borran solos), 
 * deberíamos añadir aquí una propiedad como `val caducaEn: Long? = null`.
 */
data class Mensaje(
    val id: String = "",                             // ID único del mensaje
    val conversacionId: String = "",                 // ID del chat de la pareja
    val remitenteId: String = "",                    // Quién envió el mensaje
    val texto: String = "",                          // El contenido escrito
    val tipo: String = TipoMensaje.TEXTO.name,       // Si es texto, foto, audio...
    val mediaUrl: String = "",                       // Enlace a la foto o vídeo en la nube
    val duracionSegundos: Int = 0,                   // Tiempo que dura el audio
    val estado: String = EstadoMensaje.ENVIANDO.name,// Situación del envío
    val reacciones: Map<String, String> = emptyMap(),// usuarioId, reaccionId (ReaccionType.id)
    val creadoEn: Timestamp = Timestamp.now(),       // Fecha y hora del envío
    val editado: Boolean = false                     // Si se cambió después de enviar
)

