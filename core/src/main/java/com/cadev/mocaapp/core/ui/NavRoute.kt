package com.cadev.mocaapp.core.ui

sealed class NavRoutes(val route: String) {

    //Auth
    object Login         : NavRoutes("login")
    object Registro      : NavRoutes("registro")

    //Pareja
    object CodigoPareja  : NavRoutes("codigo_pareja")
    object FechaRelacion : NavRoutes("fecha_relacion/{relacionId}") {
        fun crearRuta(relacionId: String) = "fecha_relacion/$relacionId"
    }

    //Main
    object Main          : NavRoutes("main")

    //Tabs
    object Home          : NavRoutes("home")
    object Calendario    : NavRoutes("calendario")
    object Chat          : NavRoutes("chat")
    object Cuestionarios : NavRoutes("cuestionarios")
    object Perfil        : NavRoutes("perfil")

    //Diario
    object DetalleDia : NavRoutes("detalle_dia/{fecha}") {
        fun crearRuta(fecha: String) = "detalle_dia/$fecha"
    }

    object CrearEntrada : NavRoutes("crear_entrada/{fecha}/{tipo}") {
        fun crearRuta(fecha: String, tipo: String) = "crear_entrada/$fecha/$tipo"
    }

    object EditarEntrada : NavRoutes("editar_entrada/{entradaId}") {
        fun crearRuta(entradaId: String) = "editar_entrada/$entradaId"
    }

    //Eventos
    object Eventos     : NavRoutes("eventos")
    object CrearEvento : NavRoutes("crear_evento")

    //Otros
    object Notas   : NavRoutes("notas")
    object Estados : NavRoutes("estados")
}