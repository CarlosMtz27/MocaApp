package com.cadev.mocaapp.feature.diario.data.repository

import android.net.Uri
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.domain.repository.DiarioRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class DiarioRepositoryImpl(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : DiarioRepository {

    override suspend fun obtenerEntradasDelMes(
        usuarioId: String,
        anio: Int,
        mes: Int
    ): Result<List<EntradaDiario>> {
        return try {
            // Construimos el prefijo del mes ej: "2024-02"
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"

            val snapshot = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereGreaterThanOrEqualTo("fecha", "$prefijo-01")
                .whereLessThanOrEqualTo("fecha", "$prefijo-31")
                .orderBy("fecha", Query.Direction.DESCENDING)
                .get()
                .await()

            val entradas = snapshot.documents.mapNotNull {
                it.toObject(EntradaDiario::class.java)
            }

            Result.success(entradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ): Result<List<EntradaDiario>> {
        return try {
            val entradas = mutableListOf<EntradaDiario>()

            // Mis entradas del día
            val misEntradas = firestore
                .collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereEqualTo("fecha", fecha)
                .get()
                .await()
                .documents
                .mapNotNull { it.toObject(EntradaDiario::class.java) }

            entradas.addAll(misEntradas)

            // Entradas compartidas de la pareja
            if (parejaId != null) {
                val entradasPareja = firestore
                    .collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("fecha", fecha)
                    .whereEqualTo("compartida", true)
                    .whereEqualTo("parejaId", usuarioId)
                    .get()
                    .await()
                    .documents
                    .mapNotNull { it.toObject(EntradaDiario::class.java) }

                entradas.addAll(entradasPareja)
            }

            Result.success(entradas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun crearEntrada(
        entrada: EntradaDiario,
        fotosLocales: List<String>
    ): Result<EntradaDiario> {
        return try {
            // Subir fotos a Firebase Storage
            val urlsFotos = fotosLocales.map { rutaLocal ->
                subirFoto(rutaLocal, entrada.usuarioId)
            }

            // Crear la entrada con las URLs de las fotos
            val entradaConFotos = entrada.copy(fotos = urlsFotos)
            val entradaId = firestore.collection("entradas").document().id
            val entradaFinal = entradaConFotos.copy(id = entradaId)

            //Guardar en Firestore
            firestore
                .collection("entradas")
                .document(entradaId)
                .set(entradaFinal)
                .await()

            Result.success(entradaFinal)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, List<String>>> {
        return try {
            val mesFormateado = mes.toString().padStart(2, '0')
            val prefijo = "$anio-$mesFormateado"
            val diasConEntrada = mutableMapOf<String, MutableList<String>>()

            // Mis entradas
            firestore.collection("entradas")
                .whereEqualTo("usuarioId", usuarioId)
                .whereGreaterThanOrEqualTo("fecha", "$prefijo-01")
                .whereLessThanOrEqualTo("fecha", "$prefijo-31")
                .get().await()
                .documents.forEach { doc ->
                    val fecha = doc.getString("fecha") ?: return@forEach
                    val tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name
                    diasConEntrada.getOrPut(fecha) { mutableListOf() }.add(tipo)
                }

            // Entradas compartidas de la pareja
            if (parejaId != null) {
                firestore.collection("entradas")
                    .whereEqualTo("usuarioId", parejaId)
                    .whereEqualTo("compartida", true)
                    .whereGreaterThanOrEqualTo("fecha", "$prefijo-01")
                    .whereLessThanOrEqualTo("fecha", "$prefijo-31")
                    .get().await()
                    .documents.forEach { doc ->
                        val fecha = doc.getString("fecha") ?: return@forEach
                        val tipo = doc.getString("tipo") ?: TipoEntrada.MI_DIA.name
                        diasConEntrada.getOrPut(fecha) { mutableListOf() }.add(tipo)
                    }
            }

            Result.success(diasConEntrada)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Sube una foto a Firebase Storage y devuelve su URL
    private suspend fun subirFoto(
        rutaLocal: String,
        usuarioId: String
    ): String {
        val nombreArchivo = UUID.randomUUID().toString()
        val ref = storage
            .reference
            .child("entradas/$usuarioId/$nombreArchivo.jpg")

        ref.putFile(Uri.parse(rutaLocal)).await()
        return ref.downloadUrl.await().toString()
    }
}