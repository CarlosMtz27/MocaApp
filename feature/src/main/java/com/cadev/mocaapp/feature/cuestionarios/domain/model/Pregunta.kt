package com.cadev.mocaapp.feature.cuestionarios.domain.model

/**
 * TIPOS DE PREGUNTAS QUE PODEMOS CREAR
 */
enum class TipoPregunta {
    OPCION_MULTIPLE, // Elegir entre varias respuestas dadas
    TEXTO_LIBRE,     // Escribir lo que uno quiera
    ESCALA,          // Puntuar del 1 al 10 (o similar)
    SI_NO,           // Respuesta cerrada
    FOTO             // Responder subiendo una imagen
}

/**
 * EL MODELO DE UNA PREGUNTA
 * 
 * Qué hace:
 * Define la estructura de cada pregunta de un cuestionario: su texto, el tipo de respuesta 
 * que espera y si tiene alguna imagen de apoyo.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir un tiempo límite para responder cada pregunta, debemos añadir 
 * una propiedad como `val tiempoSegundos: Int = 30`.
 */
data class Pregunta(
    val id: String = "",                                // ID único de la pregunta
    val texto: String = "",                             // Lo que preguntamos
    val tipo: String = TipoPregunta.OPCION_MULTIPLE.name, // El formato de la respuesta
    val opciones: List<String> = emptyList(),           // Lista de opciones si es de selección
    val imagenUrl: String = "",                         // Foto que ayuda a entender la pregunta
    val puntos: Int = 10                                // Cuánto vale esta pregunta para el total
)
