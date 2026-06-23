package com.cadev.mocaapp.feature.pareja.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * ASISTENTE DE USUARIO
 * 
 * Qué hace:
 * Es una herramienta rápida que nos ayuda a encontrar el ID de nuestra pareja 
 * consultando directamente en nuestra ficha de usuario en Firebase.
 * 
 * Cómo lo podemos modificar:
 * Si en el futuro guardamos más datos vinculados (ej: ID del chat), podemos 
 * añadir una función similar aquí para recuperarlos rápidamente.
 */
object UsuarioHelper {

    /**
     * BUSCAR PAREJA:
     * Mira en nuestro perfil de la base de datos y nos devuelve el identificador 
     * único de la persona con la que estamos conectados.
     */
    suspend fun obtenerParejaId(uid: String): String? {
        return try {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .await()
                .getString("parejaId")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
