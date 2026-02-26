package com.cadev.mocaapp.feature.pareja.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object UsuarioHelper {
    suspend fun obtenerParejaId(uid: String): String? {
        return try {
            FirebaseFirestore.getInstance()
                .collection("usuarios")
                .document(uid)
                .get()
                .await()
                .getString("parejaId")
        } catch (e: Exception) {
            null
        }
    }
}