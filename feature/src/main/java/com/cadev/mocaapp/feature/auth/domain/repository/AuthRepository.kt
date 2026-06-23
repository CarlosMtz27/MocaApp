package com.cadev.mocaapp.feature.auth.domain.repository
import com.cadev.mocaapp.feature.auth.domain.model.Usuario

/**
 * REGLAS DE LA AUTENTICACIÓN
 * 
 * Qué hace:
 * Aquí definimos qué cosas podemos hacer en nuestra app (login, registro, etc.).
 * Es como un contrato que nos dice qué funciones tenemos disponibles sin entrar en detalles técnicos.
 * 
 * Cómo lo podemos ampliar:
 * Si necesitamos una función nueva (ej: borrar cuenta), debemos añadirla aquí primero:
 * suspend fun borrarCuenta(): Result<Unit>
 */
interface AuthRepository {

    /**
     * Crea una cuenta nueva usando correo y clave.
     */
    suspend fun registrar(
        email: String,
        password: String,
        nombre: String
    ): Result<Usuario>

    /**
     * Entra en la cuenta si el correo y clave coinciden.
     */
    suspend fun login(
        email: String,
        password: String
    ): Result<Usuario>

    /**
     * Sale de la cuenta actual.
     */
    fun logout()

    /**
     * Comprueba si el usuario ya tiene la sesión abierta de antes.
     */
    fun obtenerUsuarioActual(): Usuario?
}
