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
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class PerfilRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : PerfilRepository {

    override suspend fun obtenerUsuario(
        usuarioId: String
    ): Result<Usuario> {
        return try {
            val doc = firestore
                .collection("usuarios")
                .document(usuarioId)
                .get()
                .await()

            val usuario = doc.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerPareja(
        parejaId: String
    ): Result<Usuario> = obtenerUsuario(parejaId)

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

    override suspend fun actualizarEmail(
        usuarioId: String,
        nuevoEmail: String,
        passwordActual: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No hay sesión activa"))

            // Reautenticar antes de cambiar email
            val credencial = EmailAuthProvider
                .getCredential(user.email ?: "", passwordActual)
            user.reauthenticate(credencial).await()

            // Actualizar en Firebase Auth
            user.updateEmail(nuevoEmail).await()

            // Actualizar en Firestore
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

    override suspend fun actualizarPassword(
        emailActual: String,
        passwordActual: String,
        nuevoPassword: String
    ): Result<Unit> {
        return try {
            val user = auth.currentUser
                ?: return Result.failure(Exception("No hay sesión activa"))

            // Reautenticar antes de cambiar contraseña
            val credencial = EmailAuthProvider
                .getCredential(emailActual, passwordActual)
            user.reauthenticate(credencial).await()

            // Actualizar contraseña
            user.updatePassword(nuevoPassword).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarFotoPerfil(
    usuarioId: String,
    rutaLocal: String
    ): Result<String> {
        return try {
            // Subir a Cloudinary
            val url = subirFoto(rutaLocal, usuarioId)

            // Guardar URL en Firestore
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

    override suspend fun obtenerFechaRelacion(
        usuarioId: String
    ): Result<String?> {
        return try {
            // Buscar en relaciones donde el usuario es parte
            val relacion = firestore
                .collection("relaciones")
                .whereArrayContains("participantes", usuarioId)
                .get()
                .await()
                .documents
                .firstOrNull()

            val fecha = relacion?.getString("fechaInicio")
            Result.success(fecha)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun actualizarFechaRelacion(
        usuarioId: String,
        fecha: String
    ): Result<Unit> {
        return try {
            val relacionDoc = firestore
                .collection("relaciones")
                .whereArrayContains("participantes", usuarioId)
                .get()
                .await()
                .documents
                .firstOrNull()
                ?: return Result.failure(Exception("Relación no encontrada"))

            firestore
                .collection("relaciones")
                .document(relacionDoc.id)
                .update("fechaInicio", fecha)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun subirFoto(
        rutaLocal: String,
        usuarioId: String
    ): String = suspendCancellableCoroutine { continuation ->

        val requestId = MediaManager.get()
            .upload(Uri.parse(rutaLocal))
            .option("folder", "perfiles/$usuarioId")
            .option("public_id", UUID.randomUUID().toString())
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