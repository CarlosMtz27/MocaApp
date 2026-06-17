package com.cadev.mocaapp.feature.eventos.data.repository

import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.feature.eventos.domain.repository.EventoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class EventoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EventoRepository {

    private val col = firestore.collection("eventos")

    override suspend fun crearEvento(evento: Evento): Result<Evento> = try {
        val ref = col.document()
        val nuevo = evento.copy(id = ref.id)
        ref.set(nuevo).await()
        Result.success(nuevo)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerEventos(relacionId: String): Result<List<Evento>> = try {
        val docs = col
            .whereEqualTo("relacionId", relacionId)
            .get().await()
        val eventos = docs.documents.mapNotNull { it.toObject(Evento::class.java) }
            .sortedBy { it.fecha + it.hora }
        Result.success(eventos)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun obtenerEvento(eventoId: String): Result<Evento> = try {
        val doc = col.document(eventoId).get().await()
        val evento = doc.toObject(Evento::class.java)
            ?: return Result.failure(Exception("No encontrado"))
        Result.success(evento)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun actualizarEvento(evento: Evento): Result<Unit> = try {
        col.document(evento.id).set(evento).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun eliminarEvento(eventoId: String): Result<Unit> = try {
        col.document(eventoId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}