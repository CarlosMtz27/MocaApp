package com.cadev.mocaapp.feature.auth.data.repository

import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    override suspend fun registrar(
        email: String,
        password: String,
        nombre: String
    ): Result<Usuario> {
        return try {
            // Creamos la cuenta en Firebase Auth
            val resultado = auth
                .createUserWithEmailAndPassword(email, password)
                .await()  // await() convierte el callback de Firebase en suspend

            val firebaseUser = resultado.user
                ?: return Result.failure(Exception("Error al crear usuario"))

            // Generamos código único de pareja (6 caracteres)
            val codigoPareja = generarCodigo()

            // Guardamos datos extra en Firestore
            val usuario = Usuario(
                id = firebaseUser.uid,
                nombre = nombre,
                email = email,
                codigoPareja = codigoPareja
            )

            firestore
                .collection("usuarios")
                .document(firebaseUser.uid)
                .set(usuario)
                .await()

            Result.success(usuario)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): Result<Usuario> {
        return try {
            // Iniciar sesión en Firebase Auth
            val resultado = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = resultado.user
                ?: return Result.failure(Exception("Error al iniciar sesión"))

            // Obtener datos del usuario desde Firestore
            val documento = firestore
                .collection("usuarios")
                .document(firebaseUser.uid)
                .get()
                .await()

            val usuario = documento.toObject(Usuario::class.java)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            Result.success(usuario)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun logout() {
        auth.signOut()
    }

    override fun obtenerUsuarioActual(): Usuario? {
        // Solo verificamos si hay sesión — los datos completos
        // los cargamos desde Firestore cuando sea necesario
        val firebaseUser = auth.currentUser ?: return null
        return Usuario(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: ""
        )
    }

    // Genera un código único de 6 caracteres para vincularse con pareja
    private fun generarCodigo(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6).map { caracteres.random() }.joinToString("")
    }
}