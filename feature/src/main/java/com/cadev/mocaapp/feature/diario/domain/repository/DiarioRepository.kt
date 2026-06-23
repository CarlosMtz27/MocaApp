package com.cadev.mocaapp.feature.diario.domain.repository

import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario

/**
 * REGLAS DEL DIARIO COMPARTIDO
 * 
 * Qué hace:
 * Aquí definimos todas las acciones que podemos hacer con nuestro diario: 
 * guardar momentos, subir fotos y vídeos, ver el calendario o comentar lo 
 * que nuestra pareja ha escrito.
 * 
 * Cómo lo podemos ampliar:
 * Si queremos que los recuerdos se puedan "archivar", debemos añadir aquí:
 * suspend fun archivarEntrada(entradaId: String): Result<Unit>
 */
interface DiarioRepository {

    /**
     * Trae todos los recuerdos guardados por nosotros en un mes específico.
     */
    suspend fun obtenerEntradasDelMes(
        usuarioId: String,
        anio: Int,
        mes: Int
    ): Result<List<EntradaDiario>>

    /**
     * Busca los recuerdos (nuestros y de nuestra pareja) de un día concreto.
     */
    suspend fun obtenerEntradasDelDia(
        usuarioId: String,
        parejaId: String?,
        fecha: String
    ): Result<List<EntradaDiario>>

    /**
     * Obtiene los últimos recuerdos guardados para verlos en la pantalla de inicio.
     */
    suspend fun obtenerUltimasEntradas(
        usuarioId: String,
        parejaId: String?,
        limite: Int
    ): Result<List<EntradaDiario>>

    /**
     * Crea un nuevo recuerdo, subiendo primero las fotos y vídeos locales a la nube.
     */
    suspend fun crearEntrada(
        entrada: EntradaDiario,
        fotosLocales: List<String>,
        videosLocales: List<String>
    ): Result<EntradaDiario>

    /**
     * Nos dice qué días del mes tienen algo escrito para marcarlos en el calendario.
     */
    suspend fun obtenerDiasConEntrada(
        usuarioId: String,
        parejaId: String?,
        anio: Int,
        mes: Int
    ): Result<Map<String, com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo>>

    /**
     * Obtiene toda la información de un solo recuerdo usando su ID.
     */
    suspend fun obtenerEntradaPorId(entradaId: String): Result<EntradaDiario>

    /**
     * Modifica un recuerdo existente, gestionando las fotos nuevas y las que queremos borrar.
     */
    suspend fun actualizarEntrada(
        entrada: EntradaDiario,
        fotosNuevas: List<String>,
        videosNuevos: List<String>,
        fotosEliminar: List<String>,
        videosEliminar: List<String>
    ): Result<EntradaDiario>

    /**
     * Trae todos los comentarios de nuestra pareja en un recuerdo.
     */
    suspend fun obtenerComentarios(
        entradaId: String
    ): Result<List<Comentario>>

    /**
     * Guarda un nuevo comentario en un recuerdo.
     */
    suspend fun agregarComentario(
        comentario: Comentario
    ): Result<Comentario>

    /**
     * Borra un comentario que hayamos escrito.
     */
    suspend fun eliminarComentario(
        comentarioId: String
    ): Result<Unit>
}
