package com.cadev.mocaapp.feature.auth.domain.repository
import com.cadev.mocaapp.feature.auth.domain.model.Usuario

// Esta interface es el "contrato" — define que puede hacer
// la autenticación, sin decir como lo hace.
// El ViewModel solo conoce esta interface, nunca Firebase directamente.
// Esto nos permite cambiar Firebase por otro servicio en el futuro
// sin tocar el ViewModel ni la UI. pincipio Open/Closed

interface AuthRepository {

    // Registrar usuario nuevo — devuelve el usuario creado o un error
    //suspend es una funcion que puede pausarse mientras espera algo, sin bloquear  el hilo principal
    //La app no se congela mientras se haven llmadas a internet
    suspend fun registrar(
        email: String,
        password: String,
        nombre: String
    ): Result<Usuario>

    // Iniciar sesión — Result devuelve el usuario o un error
    suspend fun login(
        email: String,
        password: String
    ): Result<Usuario>

    // Cerrar sesión
    fun logout()

    // Verificar si hay sesión activa al abrir la app
    fun obtenerUsuarioActual(): Usuario?
}