package com.cadev.mocaapp.feature.cuestionarios.domain.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp

/**
 * CATEGORÍAS DE LOS TESTS
 * 
 * Qué hace:
 * Clasifica los cuestionarios en diferentes temas para que la pareja sepa de qué va cada uno.
 * Cada categoría tiene su propio icono y nombre.
 * 
 * Cómo lo podemos ampliar:
 * Si queremos una categoría para "Planes", debemos añadir:
 * PLANES(Icons.Default.Map, "Planes futuros")
 */
enum class CategoriaCuestionario(val icono: ImageVector, val etiqueta: String) {
    COMPATIBILIDAD(Icons.Default.Favorite, "Compatibilidad"),
    CONOCERSE(Icons.Default.Psychology, "Conócete mejor"),
    DIVERTIDO(Icons.Default.SentimentSatisfied, "Divertido"),
    PROFUNDO(Icons.Default.QuestionAnswer, "Reflexivo"),
    PERSONALIZADO(Icons.Default.Edit, "Personalizado")
}

/**
 * EL MODELO DEL CUESTIONARIO
 * 
 * Qué hace:
 * Define toda la información de un test: su título, descripción, a qué categoría 
 * pertenece y la lista completa de preguntas que lo forman.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los cuestionarios tengan una fecha de caducidad, debemos añadir 
 * una propiedad como `val disponibleHasta: Timestamp? = null`.
 */
data class Cuestionario(
    val id: String = "",                                              // ID único del test
    val titulo: String = "",                                          // Nombre del cuestionario
    val descripcion: String = "",                                     // Breve explicación de qué trata
    val categoria: String = CategoriaCuestionario.COMPATIBILIDAD.name, // El tema del test
    val etiquetas: List<String> = emptyList(),                        // Etiquetas (divertido, hot, etc.)
    val preguntas: List<Pregunta> = emptyList(),                      // Las preguntas que hay que responder
    val creadoPor: String = "sistema",                                // Quién lo inventó ("sistema" o un usuario)
    val relacionId: String = "",                                      // A qué relación pertenece si es personalizado
    val creadoEn: Timestamp = Timestamp.now(),                        // Cuándo se añadió a la app
    val totalPuntos: Int = 0                                          // Suma total de puntos de sus preguntas
)

