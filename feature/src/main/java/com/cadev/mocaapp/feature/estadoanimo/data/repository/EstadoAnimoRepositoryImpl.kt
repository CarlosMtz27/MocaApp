package com.cadev.mocaapp.feature.estadoanimo.data.repository

import com.cadev.mocaapp.feature.estadoanimo.domain.model.EstadoAnimoActual
import com.cadev.mocaapp.feature.estadoanimo.domain.repository.EstadoAnimoRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

class EstadoAnimoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EstadoAnimoRepository {

    override suspend fun actualizarEstado(relacionId: String, uid: String, emoji: String) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val estado = EstadoAnimoActual(
            uid = uid,
            emoji = emoji,
            fecha = hoy,
            actualizadaEn = Timestamp.now()
        )
        
        firestore.collection("relaciones")
            .document(relacionId)
            .collection("estado")
            .document(uid)
            .set(estado)
            .await()
    }

    override fun escucharEstados(relacionId: String, uidPropio: String): Flow<Pair<EstadoAnimoActual?, EstadoAnimoActual?>> = callbackFlow {
        val listener = firestore.collection("relaciones")
            .document(relacionId)
            .collection("estado")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close()
                    return@addSnapshotListener
                }
                
                val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val estados = snapshot?.documents?.mapNotNull { it.toObject(EstadoAnimoActual::class.java) } ?: emptyList()
                
                val propio = estados.find { it.uid == uidPropio && it.fecha == hoy }
                val pareja = estados.find { it.uid != uidPropio && it.fecha == hoy }
                
                trySend(Pair(propio, pareja))
            }
        awaitClose { listener.remove() }
    }

    override suspend fun obtenerEstados(relacionId: String, uidPropio: String): Pair<EstadoAnimoActual?, EstadoAnimoActual?> {
        val snapshot = firestore.collection("relaciones")
            .document(relacionId)
            .collection("estado")
            .get()
            .await()
            
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val estados = snapshot.documents.mapNotNull { it.toObject(EstadoAnimoActual::class.java) }
            
        val propio = estados.find { it.uid == uidPropio && it.fecha == hoy }
        val pareja = estados.find { it.uid != uidPropio && it.fecha == hoy }
            
        return Pair(propio, pareja)
    }
}
