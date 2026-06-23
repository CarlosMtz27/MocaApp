package com.cadev.mocaapp.feature.chat.domain.repository

import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import kotlinx.coroutines.flow.Flow

/**
 * REGLAS DEL CHAT COMPARTIDO
 * 
 * Qué hace:
 * Aquí definimos todas las acciones que nuestra pareja puede hacer en el chat: 
 * enviar textos, fotos, audios, reaccionar con emojis o ver si el otro está escribiendo.
 * 
 * Cómo lo podemos ampliar:
 * Si queremos añadir una función de "llamada", deberíamos añadir aquí la línea:
 * suspend fun iniciarLlamada(conversacionId: String): Result<Unit>
 */
interface ChatRepository {

    /**
     * Nos permite recibir los mensajes nuevos en cuanto lleguen.
     */
    fun escucharMensajes(conversacionId: String): Flow<List<Mensaje>>

    /**
     * Envía un mensaje normal de texto.
     */
    suspend fun enviarMensaje(mensaje: Mensaje): Result<Mensaje>

    /**
     * Envía archivos pesados como fotos, vídeos o grabaciones de voz.
     */
    suspend fun enviarMensajeConMedia(
        mensaje: Mensaje,
        rutaLocal: String
    ): Result<Mensaje>

    /**
     * Avisa a la base de datos que ya leímos los mensajes.
     */
    suspend fun marcarComoLeido(
        conversacionId: String,
        usuarioId: String
    ): Result<Unit>

    /**
     * Dice a la base de datos si estamos pulsando teclas o no.
     */
    suspend fun actualizarEscribiendo(
        conversacionId: String,
        usuarioId: String,
        escribiendo: Boolean
    ): Result<Unit>

    /**
     * Nos avisa si nuestra pareja está escribiendo algo ahora mismo.
     */
    fun escucharEscribiendo(
        conversacionId: String,
        parejaId: String
    ): Flow<Boolean>

    /**
     * Pone un dibujo (emoji) sobre un mensaje específico.
     */
    suspend fun agregarReaccion(
        conversacionId: String,
        mensajeId: String,
        usuarioId: String,
        emoji: String
    ): Result<Unit>

    /**
     * Quita un mensaje de la conversación para los dos.
     */
    suspend fun eliminarMensaje(
        conversacionId: String,
        mensajeId: String
    ): Result<Unit>

    /**
     * Crea un nombre único para la sala de chat de los dos usuarios.
     */
    fun obtenerConversacionId(uid1: String, uid2: String): String
}
