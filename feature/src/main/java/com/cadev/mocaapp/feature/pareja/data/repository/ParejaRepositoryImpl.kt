package com.cadev.mocaapp.feature.pareja.data.repository

import com.cadev.mocaapp.feature.pareja.domain.repository.ParejaRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.util.Date

/**
 * EL MOTOR DE LA VINCULACIÓN (FIREBASE)
 * 
 * Qué hace:
 * Aquí escribimos la lógica real para unir dos cuentas. Buscamos el código 
 * en la base de datos y actualizamos ambos perfiles de forma segura usando 
 * una transacción atómica.
 */
class ParejaRepositoryImpl(
    private val firestore: FirebaseFirestore
) : ParejaRepository {

    /**
     * VINCULAR:
     * 1. Buscamos quién tiene el código ingresado.
     * 2. Verificamos que no sea nuestro propio código ni uno ya ocupado.
     * 3. Creamos la "relación" y actualizamos ambos usuarios al mismo tiempo.
     */
    override suspend fun vincularPorCodigo(
        codigoPareja: String,
        miUsuarioId: String
    ): Result<String> {
        return try {
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

            if (parejaId == miUsuarioId) {
                return Result.failure(
                    Exception("No puedes usar tu propio código")
                )
            }

            val parejaData = parejaDoc.data
            val parejaIdRegistrada = parejaData?.get("parejaId") as? String
            
            if (!parejaIdRegistrada.isNullOrBlank()) {
                return Result.failure(
                    Exception("Este código ya pertenece a una pareja vinculada")
                )
            }

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

            // Usamos una transacción para que todo se guarde correctamente o nada.
            firestore.runTransaction { transaction ->
                transaction.set(
                    firestore.collection("relaciones").document(relacionId),
                    relacion
                )

                transaction.update(
                    firestore.collection("usuarios").document(miUsuarioId),
                    mapOf(
                        "parejaId" to parejaId,
                        "relacionId" to relacionId
                    )
                )

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

    /**
     * OBTENER MI CÓDIGO:
     * Trae de nuestra ficha de usuario el código secreto de 6 letras.
     */
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

    /**
     * COMPROBAR VÍNCULO:
     * Mira si ya tenemos el ID de alguien más guardado en nuestro perfil.
     */
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

    /**
     * FECHA DE INICIO:
     * Guarda el día que elegimos como nuestro aniversario en el documento de la relación.
     */
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


