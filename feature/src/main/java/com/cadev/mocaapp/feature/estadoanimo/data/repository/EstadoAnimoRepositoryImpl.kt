package com.cadev.mocaapp.feature.estadoanimo.data.repository

import com.cadev.mocaapp.feature.estadoanimo.domain.model.EstadoAnimoActual
import com.cadev.mocaapp.feature.estadoanimo.domain.repository.EstadoAnimoRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

/**
 * MOTOR DEL ESTADO DE ÁNIMO (VERSIÓN ROBUSTA)
 */
class EstadoAnimoRepositoryImpl(
    private val firestore: FirebaseFirestore
) : EstadoAnimoRepository {

    private fun getFechaHoy(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    override suspend fun actualizarEstado(relacionId: String, uid: String, emoji: String) {
        val hoy = getFechaHoy()
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

    /**
     * ESCUCHAR ESTADOS:
     * Obtenemos los estados de la colección sin filtrar rígidamente por fecha 
     * en la consulta, para evitar fallos por desincronización de relojes.
     */
    override fun escucharEstados(relacionId: String, uidPropio: String): Flow<Pair<EstadoAnimoActual?, EstadoAnimoActual?>> = callbackFlow {
        val listener = firestore.collection("relaciones")
            .document(relacionId)
            .collection("estado")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    if (error.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                        close()
                    } else {
                        close(error)
                    }
                    return@addSnapshotListener
                }
                
                val documentos = snapshot?.documents?.mapNotNull { it.toObject(EstadoAnimoActual::class.java) } ?: emptyList()
                
                // Buscamos el estado más reciente de cada uno. 
                // Eliminamos el filtro estricto de 'fecha == hoy' para evitar fallos por 
                // desincronización de relojes entre dispositivos (especialmente cerca de medianoche).
                val propio = documentos.find { it.uid == uidPropio }
                val pareja = documentos.find { it.uid != uidPropio }
                
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
            
        val documentos = snapshot.documents.mapNotNull { it.toObject(EstadoAnimoActual::class.java) }
            
        // Obtenemos los estados sin filtrar rígidamente por la fecha del dispositivo
        val propio = documentos.find { it.uid == uidPropio }
        val pareja = documentos.find { it.uid != uidPropio }
            
        return Pair(propio, pareja)
    }
}
