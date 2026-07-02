package com.cadev.mocaapp.feature.perfil.domain.repository

import com.cadev.mocaapp.feature.auth.domain.model.Usuario

/**
 * REGLAS DE NUESTRO PERFIL
 * 
 * Qué hace:
 * Define todas las acciones que podemos realizar sobre nuestra cuenta: 
 * actualizar nuestro nombre, cambiar la foto, modificar el correo o la 
 * contraseña, y gestionar la fecha de nuestra relación.
 */
interface PerfilRepository {

    /**
     * ESCUCHAR MI PERFIL:
     * Vigila en tiempo real cualquier cambio en nuestros datos (como el relacionId).
     */
    fun escucharUsuario(usuarioId: String): kotlinx.coroutines.flow.Flow<Usuario?>

    /**
     * Recupera toda nuestra información personal de la base de datos.
     */
    suspend fun obtenerUsuario(usuarioId: String): Result<Usuario>

    /**
     * Trae la información pública de nuestra pareja.
     */
    suspend fun obtenerPareja(parejaId: String): Result<Usuario>

    /**
     * Guarda nuestro nuevo nombre en nuestro perfil.
     */
    suspend fun actualizarNombre(
        usuarioId: String,
        nuevoNombre: String
    ): Result<Unit>

    /**
     * Trae todas las entradas de diario asociadas a un usuario o pareja.
     */
    suspend fun obtenerEntradas(usuarioId: String, parejaId: String?): Result<List<com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario>>

    /**
     * Cuenta cuántos cuestionarios han sido completados por el usuario.
     */
    suspend fun contarCuestionariosCompletados(usuarioId: String): Result<Int>

    /**
     * Cambia nuestro correo electrónico de acceso de forma segura.
     */
    suspend fun actualizarEmail(
        usuarioId: String,
        nuevoEmail: String,
        passwordActual: String
    ): Result<Unit>

    /**
     * Actualiza nuestra clave secreta tras verificar la actual.
     */
    suspend fun actualizarPassword(
        emailActual: String,
        passwordActual: String,
        nuevoPassword: String
    ): Result<Unit>

    /**
     * Sube una nueva imagen a la nube y la pone como nuestra foto de perfil.
     */
    suspend fun actualizarFotoPerfil(
        usuarioId: String,
        rutaLocal: String
    ): Result<String>

    /**
     * Cuenta cuántos recuerdos hemos guardado en nuestro diario.
     */
    suspend fun contarEntradas(usuarioId: String): Result<Int>

    /**
     * Busca la fecha de nuestro aniversario guardada en la relación.
     */
    suspend fun obtenerFechaRelacion(
        usuarioId: String
    ): Result<String?>

    /**
     * Cambia el día en que celebramos nuestro inicio como pareja.
     */
    suspend fun actualizarFechaRelacion(
        usuarioId: String,
        fecha: String
    ): Result<Unit>
}
