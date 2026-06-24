package com.cadev.mocaapp.feature.eventos.data.repository

import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.feature.eventos.domain.repository.EventoRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * EL MOTOR DEL CALENDARIO (FIREBASE)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para que nuestros planes se guarden en internet. 
 * Nos conectamos a Firestore para que ambos podamos ver las citas al instante.
 */
class EventoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EventoRepository {

    private val col = firestore.collection("eventos")

    /**
     * TRAER TODOS (FLUJO EN TIEMPO REAL):
     * Escucha cualquier cambio en la colección de eventos y emite la lista actualizada.
     */
    override fun obtenerEventosFlow(relacionId: String): Flow<List<Evento>> = callbackFlow {
        val listener = col
            .whereEqualTo("relacionId", relacionId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val eventos = snapshot?.documents?.mapNotNull { it.toObject(Evento::class.java) }
                    ?.sortedBy { it.fecha + it.hora } ?: emptyList()
                trySend(eventos)
            }
        awaitClose { listener.remove() }
    }

    /**
     * CREAR:
     * Genera un ID automático y guarda el plan en nuestra base de datos.
     */
    override suspend fun crearEvento(evento: Evento): Result<Evento> = try {
        val ref = col.document()
        val nuevo = evento.copy(id = ref.id)
        ref.set(nuevo).await()
        Result.success(nuevo)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * TRAER TODOS:
     * Busca todas las citas de nuestra relación y las ordena para ver la más próxima primero.
     */
    override suspend fun obtenerEventos(relacionId: String): Result<List<Evento>> = try {
        val docs = col
            .whereEqualTo("relacionId", relacionId)
            .get().await()
        val eventos = docs.documents.mapNotNull { it.toObject(Evento::class.java) }
            .sortedBy { it.fecha + it.hora }
        Result.success(eventos)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * BUSCAR POR ID:
     * Obtiene todos los detalles de una cita específica.
     */
    override suspend fun obtenerEvento(eventoId: String): Result<Evento> = try {
        val doc = col.document(eventoId).get().await()
        val evento = doc.toObject(Evento::class.java)
            ?: return Result.failure(Exception("No encontrado"))
        Result.success(evento)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * ACTUALIZAR:
     * Reemplaza la información de una cita vieja con los nuevos datos.
     */
    override suspend fun actualizarEvento(evento: Evento): Result<Unit> = try {
        col.document(evento.id).set(evento).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    /**
     * ELIMINAR:
     * Quita la cita de la nube definitivamente.
     */
    override suspend fun eliminarEvento(eventoId: String): Result<Unit> = try {
        col.document(eventoId).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }
}
