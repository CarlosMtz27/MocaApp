package com.cadev.mocaapp.core.ui
import android.net.Uri

sealed class NavRoutes(val route: String) {

    object Login         : NavRoutes("login")
    object Registro      : NavRoutes("registro")
    object CodigoPareja  : NavRoutes("codigo_pareja")
    object FechaRelacion : NavRoutes("fecha_relacion/{relacionId}") {
        fun crearRuta(relacionId: String) = "fecha_relacion/$relacionId"
    }
    object Main          : NavRoutes("main")
    object Home          : NavRoutes("home")
    object Calendario    : NavRoutes("calendario")
    object Cuestionarios : NavRoutes("cuestionarios")
    object ResponderCuestionario : NavRoutes("responder/{cuestionarioId}") {
        fun crearRuta(id: String) = "responder/$id"
    }
    object ResultadosCuestionario : NavRoutes("resultados_cuestionario/{cuestionarioId}") {
        fun crearRuta(id: String) = "resultados_cuestionario/$id"
    }
    object CrearCuestionario : NavRoutes("crear_cuestionario")
    object Perfil        : NavRoutes("perfil")
    object DetalleDia    : NavRoutes("detalle_dia/{fecha}") {
        fun crearRuta(fecha: String) = "detalle_dia/$fecha"
    }
    object CrearEntrada  : NavRoutes("crear_entrada/{fecha}/{tipo}") {
        fun crearRuta(fecha: String, tipo: String) = "crear_entrada/$fecha/$tipo"
    }
    object EditarEntrada : NavRoutes("editar_entrada/{entradaId}") {
        fun crearRuta(entradaId: String) = "editar_entrada/$entradaId"
    }
    object DetalleEntrada : NavRoutes("detalle_entrada/{entradaId}") {
        fun crearRuta(entradaId: String) = "detalle_entrada/$entradaId"
    }
    object PerfilPareja  : NavRoutes("perfil_pareja/{parejaId}") {
        fun crearRuta(parejaId: String) = "perfil_pareja/$parejaId"
    }
    object Chat          : NavRoutes("chat_screen/{parejaId}/{nombrePareja}") {
        fun crearRuta(parejaId: String, nombrePareja: String) =
            "chat_screen/$parejaId/${Uri.encode(nombrePareja)}"
    }

    // Eventos ← nuevas rutas
    object Eventos       : NavRoutes("eventos")
    object CrearEvento   : NavRoutes("crear_evento")
    object DetalleEvento : NavRoutes("detalle_evento/{eventoId}") {
        fun crearRuta(eventoId: String) = "detalle_evento/$eventoId"
    }
    object EditarEvento  : NavRoutes("editar_evento/{eventoId}") {
        fun crearRuta(eventoId: String) = "editar_evento/$eventoId"
    }

    object Notas         : NavRoutes("notas")
    object Estados       : NavRoutes("estados")
    object EstadoAnimo   : NavRoutes("estado_animo")
    object Ajustes       : NavRoutes("ajustes")
}