package com.cadev.mocaapp.feature.notas.data.repository

import com.cadev.mocaapp.feature.notas.domain.model.NotaActual
import com.cadev.mocaapp.feature.notas.domain.repository.NotaRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class NotaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : NotaRepository {

    private fun getDocRef(relacionId: String, usuarioId: String) = firestore
        .collection("relaciones")
        .document(relacionId)
        .collection("notas")
        .document(usuarioId)

    override suspend fun actualizarNota(relacionId: String, usuarioId: String, nota: NotaActual): Result<Unit> = try {
        getDocRef(relacionId, usuarioId).set(nota, SetOptions.merge()).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun eliminarNota(relacionId: String, usuarioId: String): Result<Unit> = try {
        getDocRef(relacionId, usuarioId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override fun escucharNota(relacionId: String, usuarioId: String): Flow<NotaActual?> = callbackFlow {
        val listener = getDocRef(relacionId, usuarioId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val nota = snapshot?.toObject(NotaActual::class.java)
            trySend(nota)
        }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerNota(relacionId: String, usuarioId: String): Result<NotaActual?> = try {
        val doc = getDocRef(relacionId, usuarioId).get().await()
        Result.success(doc.toObject(NotaActual::class.java))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
