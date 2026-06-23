package com.cadev.mocaapp.feature.perfil.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.perfil.domain.repository.PerfilRepository
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * MOTOR DE NUESTRO PERFIL (FIREBASE Y CLOUDINARY)
 * 
 * Qué hace:
 * Aquí escribimos la programación real para gestionar nuestra cuenta. Usamos 
 * Firebase Auth para la seguridad (correo y clave), Firestore para guardar 
 * nuestros datos y Cloudinary para almacenar nuestra foto de perfil.
 */
class PerfilRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PerfilRepository {

    /**
     * ESCUCHAR USUARIO:
     * Implementación del vigilante en tiempo real usando Firestore.
     */
    override fun escucharUsuario(usuarioId: String): kotlinx.coroutines.flow.Flow<Usuario?> = kotlinx.coroutines.flow.callbackFlow {
        val listener = firestore.collection("usuarios")
            .document(usuarioId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Usuario::class.java))
            }
        awaitClose { listener.remove() }
    }

    /**
     * OBTENER MI PERFIL:
     * Descarga de la base de datos toda la información de nuestra cuenta.
     */
    override suspend fun obtenerUsuario(usuarioId: String): Result<Usuario> {
        return try {
            val doc = firestore
                .collection("usuarios")
                .document(usuarioId)
                .get()
                .await()

            val usuario = doc.toObject(Usuario::class.java)
            if (usuario == null)
                return Result.failure(Exception("Usuario no encontrado"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * OBTENER PERFIL PAREJA:
     * Busca los datos públicos de nuestra pareja para mostrarlos en la app.
     */
    override suspend fun obtenerPareja(parejaId: String): Result<Usuario> {
        return try {
            val doc = firestore
                .collection("usuarios")
                .document(parejaId)
                .get()
                .await()

            val usuario = doc.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Pareja no encontrada"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ACTUALIZAR NOMBRE:
     * Cambia cómo nos llamamos dentro de la aplicación.
     */
    override suspend fun actualizarNombre(
        usuarioId: String,
        nuevoNombre: String
    ): Result<Unit> {
        return try {
            firestore
                .collection("usuarios")
                .document(usuarioId)
                .update("nombre", nuevoNombre)
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ACTUALIZAR CORREO:
     * 1. Verifica nuestra identidad con la clave actual.
     * 2. Cambia el correo en el sistema de seguridad.
     * 3. Actualiza el correo en nuestra ficha de usuario.
     */
    override suspend fun actualizarEmail(
        usuarioId: String,
        nuevoEmail: String,
        passwordActual: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No hay sesión activa"))

            val credencial = EmailAuthProvider
                .getCredential(user.email ?: "", passwordActual)
            user.reauthenticate(credencial).await()

            user.updateEmail(nuevoEmail).await()

            firestore
                .collection("usuarios")
                .document(usuarioId)
                .update("email", nuevoEmail)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * ACTUALIZAR CLAVE:
     * Comprueba nuestra clave vieja y guarda la nueva de forma segura.
     */
    override suspend fun actualizarPassword(
        emailActual: String,
        passwordActual: String,
        nuevoPassword: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No hay sesión activa"))

            val credencial = EmailAuthProvider
                .getCredential(emailActual, passwordActual)
            user.reauthenticate(credencial).await()

            user.updatePassword(nuevoPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CAMBIAR FOTO DE PERFIL:
     * Envía nuestra nueva imagen a la nube y guarda el enlace en nuestro perfil.
     */
    override suspend fun actualizarFotoPerfil(
    usuarioId: String,
    rutaLocal: String
    ): Result<String> {
        return try {
            val url = subirFoto(rutaLocal, usuarioId)

            firestore
                .collection("usuarios")
                .document(usuarioId)
                .update("fotoPerfil", url)
                .await()

            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CONTAR RECUERDOS:
     * Suma cuántas entradas hemos escrito hasta ahora en el diario.
     */
    override suspend fun contarEntradas(
        usuarioId: String
    ): Result<Int> {
        return try {
            val count = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .get()
                .await()
                .size()
            Result.success(count)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * BUSCAR ANIVERSARIO:
     * Busca el documento de nuestra relación para saber cuándo empezamos.
     */
    override suspend fun obtenerFechaRelacion(
        usuarioId: String
    ): Result<String?> {
        return try {
            var snapshot = firestore
                .collection("relaciones")
                .whereEqualTo("usuario1Id", usuarioId)
                .get().await()

            if (snapshot.documents.isEmpty()) {
                snapshot = firestore
                    .collection("relaciones")
                    .whereEqualTo("usuario2Id", usuarioId)
                    .get().await()
            }

            val doc = snapshot.documents.firstOrNull()
            val timestamp = doc?.getTimestamp("fechaInicio")
            val fecha = timestamp?.toDate()?.let { date ->
                java.text.SimpleDateFormat(
                    "yyyy-MM-dd",
                    java.util.Locale.getDefault()
                ).format(date)
            }

            Result.success(fecha)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * CAMBIAR ANIVERSARIO:
     * Guarda en nuestra relación la nueva fecha de inicio elegida.
     */
    override suspend fun actualizarFechaRelacion(
        usuarioId: String,
        fecha: String
    ): Result<Unit> {
        return try {
            val sdf = java.text.SimpleDateFormat(
                "yyyy-MM-dd", java.util.Locale.getDefault()
            )
            val date = sdf.parse(fecha)
                ?: return Result.failure(Exception("Fecha inválida"))
            val timestamp = com.google.firebase.Timestamp(date)

            var snapshot = firestore
                .collection("relaciones")
                .whereEqualTo("usuario1Id", usuarioId)
                .get().await()

            if (snapshot.documents.isEmpty()) {
                snapshot = firestore
                    .collection("relaciones")
                    .whereEqualTo("usuario2Id", usuarioId)
                    .get().await()
            }

            val relacionDoc = snapshot.documents.firstOrNull()
                ?: return Result.failure(Exception("Relación no encontrada"))

            firestore
                .collection("relaciones")
                .document(relacionDoc.id)
                .update("fechaInicio", timestamp)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * SUBIDA A LA NUBE (CLOUDINARY):
     * Gestiona el envío de nuestra imagen personal al servidor de fotos.
     */
    private suspend fun subirFoto(
        rutaLocal: String,
        usuarioId: String
    ): String = suspendCancellableCoroutine { continuation ->

        val requestId = MediaManager.get()
            .upload(Uri.parse(rutaLocal))
            .option("folder", "perfiles/$usuarioId")
            .option("public_id", UUID.randomUUID().toString())
            .option("resource_type", "image")
            .callback(object : UploadCallback {
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}

                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    val url = resultData["secure_url"] as? String
                    if (url != null) continuation.resume(url)
                    else continuation.resumeWithException(
                        Exception("No se obtuvo URL")
                    )
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(
                        Exception(error.description)
                    )
                }

                override fun onReschedule(requestId: String, error: ErrorInfo) {
                    continuation.resumeWithException(
                        Exception(error.description)
                    )
                }
            })
            .dispatch()

        continuation.invokeOnCancellation {
            MediaManager.get().cancelRequest(requestId)
        }
    }
}
