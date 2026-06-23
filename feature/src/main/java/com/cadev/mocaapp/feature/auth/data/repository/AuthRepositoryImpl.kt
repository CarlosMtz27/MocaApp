package com.cadev.mocaapp.feature.auth.data.repository

import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.auth.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * CONEXIÓN REAL CON EL SERVIDOR (FIREBASE)
 * 
 * Qué hace:
 * Aquí es donde escribimos la programación real que habla con Firebase para crear usuarios,
 * validar contraseñas y guardar datos en la nube.
 * 
 * Cómo lo podemos modificar:
 * Si cambiamos algo en AuthRepository (ej: borrarCuenta), aquí debemos escribir
 * la programación de Firebase para que eso funcione de verdad.
 */
class AuthRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : AuthRepository {

    /**
     * REGISTRO:
     * 1. Crea el usuario en Firebase con email y clave.
     * 2. Genera un código de pareja único que nadie más tenga.
     * 3. Guarda la ficha completa en la base de datos "usuarios".
     */
    override suspend fun registrar(
        email: String,
        password: String,
        nombre: String
    ): Result<Usuario> {
        return try {
            val resultado = auth
                .createUserWithEmailAndPassword(email, password)
                .await() 

            val firebaseUser = resultado.user
                ?: return Result.failure(Exception("Error al crear usuario"))

            // Generamos un código que estemos seguros que es único
            val codigoPareja = generarCodigoUnico()

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

    /**
     * INICIO DE SESIÓN:
     * Comprueba las credenciales y luego descarga los datos del usuario
     * (nombre, código, etc.) desde la base de datos de Firestore.
     */
    override suspend fun login(
        email: String,
        password: String
    ): Result<Usuario> {
        return try {
            val resultado = auth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = resultado.user
                ?: return Result.failure(Exception("Error al iniciar sesión"))

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

    /**
     * CERRAR SESIÓN:
     * Le avisa a Firebase que desconecte al usuario actual.
     */
    override fun logout() {
        auth.signOut()
    }

    /**
     * CARGAR SESIÓN:
     * Mira si hay alguien ya conectado al abrir la app para no pedir login otra vez.
     */
    override fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        return Usuario(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: ""
        )
    }

    /**
     * GENERADOR DE CÓDIGOS ÚNICOS:
     * Inventa una clave de 6 letras y números, y verifica en Firestore que 
     * nadie más la tenga asignada. Si ya existe, inventa otra hasta que 
     * encuentre una disponible.
     */
    private suspend fun generarCodigoUnico(): String {
        val caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        var codigo: String
        var existe: Boolean
        
        do {
            codigo = (1..6).map { caracteres.random() }.joinToString("")
            val query = firestore.collection("usuarios")
                .whereEqualTo("codigoPareja", codigo)
                .get()
                .await()
            existe = !query.isEmpty
        } while (existe)
        
        return codigo
    }
}
