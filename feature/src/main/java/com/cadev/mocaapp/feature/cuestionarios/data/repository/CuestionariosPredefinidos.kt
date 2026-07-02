package com.cadev.mocaapp.feature.cuestionarios.data.repository

import com.cadev.mocaapp.feature.cuestionarios.domain.model.*

/**
 * LISTA DE TESTS INICIALES
 */
fun cuestionariosPredefinidos(): List<Cuestionario> = listOf(
    // TEST 1: Básicos de la relación
    Cuestionario(
        id = "compatibilidad_basica",
        titulo = "¿Qué tanto nos conocemos?",
        descripcion = "Preguntas básicas para ver qué tan bien se conocen como pareja.",
        categoria = CategoriaCuestionario.COMPATIBILIDAD.name,
        creadoPor = "sistema",
        preguntas = listOf(
            Pregunta(
                id = "p1", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuál es tu lugar favorito para una cita?",
                opciones = listOf("Restaurante", "Cine", "Parque", "Casa")
            ),
            Pregunta(
                id = "p2", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Prefieres quedarte en casa antes que salir?"
            ),
            Pregunta(
                id = "p3", tipo = TipoPregunta.ESCALA.name,
                texto = "Del 1 al 10, ¿qué tan romántico/a te consideras?"
            ),
            Pregunta(
                id = "p4", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuál es tu lenguaje del amor principal?",
                opciones = listOf("Palabras de afirmación", "Tiempo de calidad", "Actos de servicio", "Regalos", "Contacto físico")
            ),
            Pregunta(
                id = "p5", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Crees en los gestos románticos sorpresa?"
            ),
            Pregunta(
                id = "p6", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cómo prefieres resolver un conflicto?",
                opciones = listOf("Hablar de inmediato", "Esperar a calmarse", "Escribir lo que siento", "Pedir tiempo")
            ),
            Pregunta(
                id = "p7", tipo = TipoPregunta.ESCALA.name,
                texto = "¿Qué tan importante es pasar tiempo juntos cada día? (1-10)"
            ),
            Pregunta(
                id = "p8", tipo = TipoPregunta.TEXTO_LIBRE.name,
                texto = "¿Cuál es tu recuerdo favorito juntos?"
            )
        )
    ),

    // TEST 2: Sueños y planes
    Cuestionario(
        id = "futuro_juntos",
        titulo = "Nuestro futuro",
        descripcion = "¿Tienen los mismos sueños y planes para el futuro?",
        categoria = CategoriaCuestionario.PROFUNDO.name,
        creadoPor = "sistema",
        preguntas = listOf(
            Pregunta(
                id = "f1", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Te imaginas viviendo en otra ciudad o país?"
            ),
            Pregunta(
                id = "f2", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuántos hijos te gustaría tener?",
                opciones = listOf("Ninguno", "1", "2", "3 o más", "No sé aún")
            ),
            Pregunta(
                id = "f3", tipo = TipoPregunta.ESCALA.name,
                texto = "¿Qué tan importante es el matrimonio para ti? (1-10)"
            ),
            Pregunta(
                id = "f4", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Dónde te ves en 5 años?",
                opciones = listOf("Casados", "Con hijos", "Viajando", "Enfocados en carrera", "Viviendo juntos")
            ),
            Pregunta(
                id = "f5", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Estarías dispuesto/a a hacer sacrificios por la relación?"
            ),
            Pregunta(
                id = "f6", tipo = TipoPregunta.TEXTO_LIBRE.name,
                texto = "¿Cuál es tu mayor sueño que quieres cumplir junto a tu pareja?"
            )
        )
    ),

    // TEST 3: Humor y gustos
    Cuestionario(
        id = "divertido_trivial",
        titulo = "¿Me conoces bien?",
        descripcion = "Preguntas divertidas para reírse juntos.",
        categoria = CategoriaCuestionario.DIVERTIDO.name,
        creadoPor = "sistema",
        preguntas = listOf(
            Pregunta(
                id = "d1", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuál sería tu superpoder ideal?",
                opciones = listOf("Volar", "Invisibilidad", "Leer la mente", "Teletransportarse")
            ),
            Pregunta(
                id = "d2", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Prefieres dulce sobre salado?"
            ),
            Pregunta(
                id = "d3", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuál es tu plan de viaje soñado?",
                opciones = listOf("Playa tropical", "Ciudad europea", "Aventura en montaña", "Ruta por Asia")
            ),
            Pregunta(
                id = "d4", tipo = TipoPregunta.ESCALA.name,
                texto = "¿Qué tan madrugador/a eres? (1=búho nocturno, 10=ave mañanera)"
            ),
            Pregunta(
                id = "d5", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Qué harías con un millón de dólares?",
                opciones = listOf("Viajar por el mundo", "Invertir", "Comprar casa", "Donarlo")
            ),
            Pregunta(
                id = "d6", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Cantarías karaoke frente a desconocidos?"
            ),
            Pregunta(
                id = "d7", tipo = TipoPregunta.TEXTO_LIBRE.name,
                texto = "¿A qué personaje de serie o película te pareces más?"
            )
        )
    ),

    // TEST 4: Prueba Maestra de Componentes
    Cuestionario(
        id = "prueba_maestra_moca",
        titulo = "Test Maestro: Prueba Total",
        descripcion = "Todas las combinaciones posibles de preguntas e imágenes para testear la UI.",
        categoria = CategoriaCuestionario.PERSONALIZADO.name,
        creadoPor = "sistema",
        preguntas = listOf(
            Pregunta(
                id = "pt1", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Cuál de estos climas prefieres?",
                opciones = listOf("Frío polar", "Calor tropical", "Templado", "Lluvioso")
            ),
            Pregunta(
                id = "pt2", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Te gusta bailar bajo la lluvia?"
            ),
            Pregunta(
                id = "pt3", tipo = TipoPregunta.ESCALA.name,
                texto = "¿Qué tan aventurero/a te sientes hoy?"
            ),
            Pregunta(
                id = "pt4", tipo = TipoPregunta.TEXTO_LIBRE.name,
                texto = "Escribe una frase que resuma nuestra relación."
            ),
            Pregunta(
                id = "pt5", tipo = TipoPregunta.FOTO.name,
                texto = "Sube una foto de tu rincón favorito de la casa."
            ),
            Pregunta(
                id = "pt6", tipo = TipoPregunta.OPCION_MULTIPLE.name,
                texto = "¿Qué animal ves representado en esta imagen?",
                opciones = listOf("Un león", "Un águila", "Un lobo", "Un oso"),
                imagenUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d"
            ),
            Pregunta(
                id = "pt7", tipo = TipoPregunta.SI_NO.name,
                texto = "¿Te gustaría tener una cena romántica en este lugar?",
                imagenUrl = "https://images.unsplash.com/photo-1519671482749-fd09be7ccebf"
            ),
            Pregunta(
                id = "pt8", tipo = TipoPregunta.ESCALA.name,
                texto = "¿Qué tanta paz te transmite este paisaje?",
                imagenUrl = "https://images.unsplash.com/photo-1470770841072-f978cf4d019e"
            ),
            Pregunta(
                id = "pt9", tipo = TipoPregunta.TEXTO_LIBRE.name,
                texto = "Observa la imagen y escribe lo primero que se te venga a la mente.",
                imagenUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470"
            )
        )
    )
)
