package com.cadev.mocaapp.core.ui
import android.net.Uri

/**
 * ESTE ARCHIVO DEFINE LOS CAMINOS DE LA APLICACIÓN
 * 
 * Qué hace
 * Contiene todas las direcciones o rutas que se pueden visitar dentro de la aplicación. Es como un 
 * mapa que indica el nombre de cada pantalla para que el sistema sepa a dónde ir.
 * 
 * Cómo añadir más cosas
 * Si creas una pantalla nueva debes añadir un objeto nuevo aquí con el nombre de la ruta. 
 * Si la pantalla necesita información extra como un identificador se pone entre llaves.
 */
sealed class NavRoutes(val route: String) {

    object Login         : NavRoutes("login")
    object Registro      : NavRoutes("registro")
    object CodigoPareja  : NavRoutes("codigo_pareja")
    
    /**
     * Esta función construye la dirección para la pantalla de fecha de relación usando el identificador
     */
    object FechaRelacion : NavRoutes("fecha_relacion/{relacionId}") {
        fun crearRuta(relacionId: String) = "fecha_relacion/$relacionId"
    }
    
    object Main          : NavRoutes("main")
    object Home          : NavRoutes("home")
    object Calendario    : NavRoutes("calendario")
    object Cuestionarios : NavRoutes("cuestionarios")
    
    /**
     * Esta función genera la ruta necesaria para entrar a responder un test específico
     */
    object ResponderCuestionario : NavRoutes("responder/{cuestionarioId}") {
        fun crearRuta(id: String) = "responder/$id"
    }
    
    /**
     * Esta función crea la dirección para ver los resultados de un test concreto
     */
    object ResultadosCuestionario : NavRoutes("resultados_cuestionario/{cuestionarioId}") {
        fun crearRuta(id: String) = "resultados_cuestionario/$id"
    }
    
    object CrearCuestionario : NavRoutes("crear_cuestionario")
    object Perfil        : NavRoutes("perfil")
    
    /**
     * Esta función construye la ruta para ver lo que pasó en un día específico del calendario
     */
    object DetalleDia    : NavRoutes("detalle_dia/{fecha}") {
        fun crearRuta(fecha: String) = "detalle_dia/$fecha"
    }
    
    /**
     * Esta función genera la dirección para crear una nueva anotación en el diario
     */
    object CrearEntrada  : NavRoutes("crear_entrada/{fecha}/{tipo}") {
        fun crearRuta(fecha: String, tipo: String) = "crear_entrada/$fecha/$tipo"
    }
    
    /**
     * Esta función crea el enlace para modificar una anotación ya existente
     */
    object EditarEntrada : NavRoutes("editar_entrada/{entradaId}") {
        fun crearRuta(entradaId: String) = "editar_entrada/$entradaId"
    }
    
    /**
     * Esta función construye la ruta para leer el contenido completo de una entrada del diario
     */
    object DetalleEntrada : NavRoutes("detalle_entrada/{entradaId}") {
        fun crearRuta(entradaId: String) = "detalle_entrada/$entradaId"
    }
    
    /**
     * Esta función genera la dirección para ver el perfil público de la pareja
     */
    object PerfilPareja  : NavRoutes("perfil_pareja/{parejaId}") {
        fun crearRuta(parejaId: String) = "perfil_pareja/$parejaId"
    }
    
    /**
     * Esta función crea el enlace para entrar al chat privado codificando el nombre para que no haya errores
     */
    object Chat          : NavRoutes("chat_screen/{parejaId}/{nombrePareja}") {
        fun crearRuta(parejaId: String, nombrePareja: String) =
            "chat_screen/$parejaId/${Uri.encode(nombrePareja)}"
    }

    object Eventos       : NavRoutes("eventos")
    object CrearEvento   : NavRoutes("crear_evento")
    
    /**
     * Esta función construye la ruta para ver la información detallada de un evento programado
     */
    object DetalleEvento : NavRoutes("detalle_evento/{eventoId}") {
        fun crearRuta(eventoId: String) = "detalle_evento/$eventoId"
    }
    
    /**
     * Esta función genera la dirección para cambiar los datos de un evento que ya fue creado
     */
    object EditarEvento  : NavRoutes("editar_evento/{eventoId}") {
        fun crearRuta(eventoId: String) = "editar_evento/$eventoId"
    }

    object Notas         : NavRoutes("notas")
    object Estados       : NavRoutes("estados")
    object EstadoAnimo   : NavRoutes("estado_animo")
    object Ajustes       : NavRoutes("ajustes")
    object WidgetsPreview : NavRoutes("widgets_preview")
    object PruebaComponentes : NavRoutes("prueba_componentes")
    object CargaTransicion   : NavRoutes("carga_transicion")
}
