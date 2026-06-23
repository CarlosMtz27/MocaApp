package com.cadev.mocaapp.feature.chat.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.chat.domain.model.EstadoMensaje
import com.cadev.mocaapp.feature.chat.domain.model.Mensaje
import com.cadev.mocaapp.feature.chat.domain.repository.ChatRepository
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * EL MOTOR DEL CHAT (FIREBASE Y CLOUDINARY)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para que los mensajes viajen por internet. Usamos 
 * Firebase para los textos y Cloudinary para guardar las fotos y audios en la nube.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar dónde guardamos las fotos, debemos modificar la función 
 * `subirArchivo` para que apunte a otro servicio (ej: Firebase Storage).
 */
class ChatRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ChatRepository {

    /**
     * Crea un nombre para la sala de chat uniendo los IDs de la pareja en orden alfabético.
     */
    override fun obtenerConversacionId(uid1: String, uid2: String): String {
        return listOf(uid1, uid2).sorted().joinToString("_")
    }

    /**
     * SECCIÓN DE MENSAJES EN VIVO:
     * Nos conectamos a Firestore y pedimos que nos avise cada vez que aparezca un mensaje nuevo.
     */
    override fun escucharMensajes(conversacionId: String): Flow<List<Mensaje>> = callbackFlow {
        val listener = firestore
            .collection("conversaciones")
            .document(conversacionId)
            .collection("mensajes")
            .orderBy("creadoEn", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                val mensajes = snapshot?.documents
                    ?.mapNotNull { it.toObject(Mensaje::class.java) }
                    ?: emptyList()
                trySend(mensajes)
            }
        awaitClose { listener.remove() }
    }

    /**
     * ENVÍO DE TEXTO:
     * Guardamos el mensaje en la base de datos y actualizamos la vista previa del chat.
     */
    override suspend fun enviarMensaje(
        mensaje: Mensaje
    ): Result<Mensaje> {
        return try {
            val id = firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .collection("mensajes")
                .document()
                .id

            val mensajeFinal = mensaje.copy(
                id = id,
                estado = EstadoMensaje.ENVIADO.name,
                creadoEn = Timestamp.now()
            )

            firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .collection("mensajes")
                .document(id)
                .set(mensajeFinal)
                .await()

            // Actualizamos el resumen de la conversación para que aparezca en la lista
            firestore
                .collection("conversaciones")
                .document(mensaje.conversacionId)
                .set(
                    mapOf(
                        "ultimoMensaje" to mensaje.texto.ifBlank { "Archivo" },
                        "ultimaActividad" to Timestamp.now(),
                        "participantes" to listOf(
                            mensaje.remitenteId,
                            mensaje.conversacionId
                                .replace(mensaje.remitenteId, "")
                                .replace("_", "")
                        )
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()

            Result.success(mensajeFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ENVÍO DE ARCHIVOS (FOTOS/AUDIOS):
     * 1. Primero subimos el archivo a Cloudinary.
     * 2. Cuando nos dan el enlace web, enviamos el mensaje al chat.
     */
    override suspend fun enviarMensajeConMedia(
        mensaje: Mensaje,
        rutaLocal: String
    ): Result<Mensaje> {
        return try {
            val carpeta = when (mensaje.tipo) {
                "FOTO" -> "fotos"
                "VIDEO" -> "videos"
                "AUDIO", "VOZ" -> "audios"
                else -> "otros"
            }
            val url = subirArchivo(rutaLocal, carpeta)
            val mensajeConMedia = mensaje.copy(mediaUrl = url)
            enviarMensaje(mensajeConMedia)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CONFIRMACIÓN DE LECTURA:
     * Buscamos los mensajes de la pareja que todavía no hayamos leído y les cambiamos el estado.
     */
    override suspend fun marcarComoLeido(
        conversacionId: String,
        usuarioId: String
    ): Result<Unit> {
        return try {
            val mensajes = firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .whereNotEqualTo("remitenteId", usuarioId)
                .whereEqualTo("estado", EstadoMensaje.ENTREGADO.name)
                .get()
                .await()

            val batch = firestore.batch()
            mensajes.documents.forEach { doc ->
                batch.update(doc.reference, "estado", EstadoMensaje.LEIDO.name)
            }
            batch.commit().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ESTADO ESCRIBIENDO:
     * Avisamos a la base de datos si estamos tecleando algo ahora mismo.
     */
    override suspend fun actualizarEscribiendo(
        conversacionId: String,
        usuarioId: String,
        escribiendo: Boolean
    ): Result<Unit> {
        return try {
            firestore
                .collection("typing")
                .document(conversacionId)
                .set(
                    mapOf(usuarioId to escribiendo),
                    com.google.firebase.firestore.SetOptions.merge()
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ESCUCHA DE PAREJA ESCRIBIENDO:
     * Vigila la base de datos para avisarnos en cuanto la pareja empiece a escribir.
     */
    override fun escucharEscribiendo(
        conversacionId: String,
        parejaId: String
    ): Flow<Boolean> = callbackFlow {
        if (parejaId.isBlank()) {
            trySend(false)
            return@callbackFlow
        }
        val listener = firestore
            .collection("typing")
            .document(conversacionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.getBoolean(parejaId) ?: false)
            }
        awaitClose { listener.remove() }
    }

    /**
     * REACCIONES:
     * Guarda el dibujo (emoji) que elegimos para un mensaje en particular.
     */
    override suspend fun agregarReaccion(
        conversacionId: String,
        mensajeId: String,
        usuarioId: String,
        emoji: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .document(mensajeId)
                .update("reacciones.$usuarioId", emoji)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * BORRADO:
     * Quita el mensaje de la base de datos para que desaparezca de ambos móviles.
     */
    override suspend fun eliminarMensaje(
        conversacionId: String,
        mensajeId: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("conversaciones")
                .document(conversacionId)
                .collection("mensajes")
                .document(mensajeId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * SUBIDA A LA NUBE (CLOUDINARY):
     * Envía el archivo local (foto, vídeo o audio) al servidor web de Cloudinary.
     */
    private suspend fun subirArchivo(
        rutaLocal: String,
        carpeta: String
    ): String = suspendCancellableCoroutine { continuation ->

        val resourceType = when (carpeta) {
            "fotos" -> "image"
            "videos", "audios" -> "video"
            else -> "auto"
        }

        val requestId = MediaManager.get()
            .upload(Uri.parse(rutaLocal))
            .option("folder", "chat/$carpeta")
            .option("public_id", UUID.randomUUID().toString())
            .option("resource_type", resourceType)
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) continuation.resume(url)
                    else continuation.resumeWithException(Exception("Sin URL"))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(Exception(error.description))
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}
