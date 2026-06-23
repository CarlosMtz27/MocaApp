package com.cadev.mocaapp.feature.notas.data.repository

import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import com.cadev.mocaapp.feature.notas.domain.repository.NotaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * EL MOTOR DE LAS NOTAS (FIREBASE)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para que nuestras notas se guarden en internet. 
 * Nos conectamos a Firestore para que los cambios se sincronicen entre ambos móviles.
 */
class NotaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : NotaRepository {

    /**
     * Localiza el documento exacto de la nota dentro de nuestra relación.
     */
    private fun getDocRef(relacionId: String, usuarioId: String): com.google.firebase.firestore.DocumentReference {
        if (relacionId.isBlank()) {
            // Devolvemos una referencia a una carpeta temporal para evitar el error de segments
            return firestore.collection("temp").document("empty")
        }
        return firestore
            .collection("relaciones")
            .document(relacionId)
            .collection("notas")
            .document(usuarioId)
    }

    /**
     * ACTUALIZAR:
     * Guarda la nueva nota en nuestra base de datos.
     */
    override suspend fun actualizarNota(relacionId: String, usuarioId: String, nota: NotaActual): Result<Unit> = try {
        getDocRef(relacionId, usuarioId).set(nota, SetOptions.merge()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * BORRAR:
     * Quita la nota de la nube definitivamente.
     */
    override suspend fun eliminarNota(relacionId: String, usuarioId: String): Result<Unit> = try {
        getDocRef(relacionId, usuarioId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * ESCUCHAR:
     * Se queda vigilando Firebase y nos avisa en cuanto hay un cambio.
     */
    override fun escucharNota(relacionId: String, usuarioId: String): Flow<NotaActual?> = callbackFlow {
        val listener = getDocRef(relacionId, usuarioId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Si el error es por falta de permisos (ej: al cerrar sesión), 
                // simplemente cerramos el canal sin lanzar una excepción fatal.
                if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    close()
                } else {
                    close(error)
                }
                return@addSnapshotListener
            }
            val nota = snapshot?.toObject(NotaActual::class.java)
            trySend(nota)
        }
        awaitClose { listener.remove() }
    }

    /**
     * OBTENER:
     * Descarga la nota actual de una sola vez.
     */
    override suspend fun obtenerNota(relacionId: String, usuarioId: String): Result<NotaActual?> = try {
        val doc = getDocRef(relacionId, usuarioId).get().await()
        Result.success(doc.toObject(NotaActual::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
}

