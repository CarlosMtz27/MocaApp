package com.cadev.mocaapp.feature.pareja.data.repository

import com.cadev.mocaapp.feature.pareja.domain.repository.ParejaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

class ParejaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ParejaRepository {

    override suspend fun vincularPorCodigo(
        codigoPareja: String,
        miUsuarioId: String
    ): Result<String> {
        return try {
            // Buscamos al usuario que tiene ese código
            val resultado = firestore
                .collection("usuarios")
                .whereEqualTo("codigoPareja", codigoPareja.uppercase())
                .get()
                .await()

            if (resultado.isEmpty) {
                return Result.failure(
                    Exception("Código no encontrado")
                )
            }

            val parejaDoc = resultado.documents.first()
            val parejaId = parejaDoc.id

            // No se puede vincular uno mismo
            if (parejaId == miUsuarioId) {
                return Result.failure(
                    Exception("No puedes usar tu propio código")
                )
            }

            // Verificamos que la pareja no tenga alguna vinculacion
            val parejaData = parejaDoc.data
            if (parejaData?.get("parejaId") != null) {
                return Result.failure(
                    Exception("Este código ya fue usado")
                )
            }

            // Crear documento de relación
            val relacionId = firestore
                .collection("relaciones")
                .document()
                .id

            val relacion = mapOf(
                "id" to relacionId,
                "usuario1Id" to miUsuarioId,
                "usuario2Id" to parejaId,
                "estado" to "activa"
            )

            // Guardamos todo en una transacción atómica
            // Una transacción garantiza que o todo se guarda
            // o nada se guarda, nunca queda a medias
            firestore.runTransaction { transaction ->

                // Guardamos la relación
                transaction.set(
                    firestore.collection("relaciones").document(relacionId),
                    relacion
                )

                // Actualizamos el usuario
                transaction.update(
                    firestore.collection("usuarios").document(miUsuarioId),
                    mapOf(
                        "parejaId" to parejaId,
                        "relacionId" to relacionId
                    )
                )

                // Actualizamos el usuario de la pareja
                transaction.update(
                    firestore.collection("usuarios").document(parejaId),
                    mapOf(
                        "parejaId" to miUsuarioId,
                        "relacionId" to relacionId
                    )
                )
            }.await()

            Result.success(relacionId)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerMiCodigo(
        usuarioId: String
    ): Result<String> {
        return try {
            val doc = firestore
                .collection("usuarios")
                .document(usuarioId)
                .get()
                .await()

            val codigo = doc.getString("codigoPareja")
                ?: return Result.failure(Exception("Código no encontrado"))

            Result.success(codigo)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun tienePareja(usuarioId: String): Boolean {
        return try {
            val doc = firestore
                .collection("usuarios")
                .document(usuarioId)
                .get()
                .await()
            doc.getString("parejaId") != null
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun guardarFechaInicio(
        relacionId: String,
        fecha: Long
    ): Result<Unit> {
        return try {
            firestore
                .collection("relaciones")
                .document(relacionId)
                .update("fechaInicio", Date(fecha))
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

