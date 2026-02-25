package com.cadev.mocaapp.core.ui

// Cada objeto representa una "dirección" de pantalla
// Usamos sealed class para que sea imposible agregar rutas
// accidentalmente desde otro lugar — solo aquí se definen

sealed class NavRoutes(val route: String) {

    // ── Auth ─────────────────────────────────────────────────
    object Login       : NavRoutes("login")
    object Registro    : NavRoutes("registro")

    // ── Pareja ───────────────────────────────────────────────
    object CodigoPareja : NavRoutes("codigo_pareja")

    // ── Principal (con bottom navigation) ────────────────────
    object Main        : NavRoutes("main")

    // ── Tabs del Main ────────────────────────────────────────
    object Home        : NavRoutes("home")
    object Calendario  : NavRoutes("calendario")
    object Chat        : NavRoutes("chat")
    object Cuestionarios : NavRoutes("cuestionarios")
    object Perfil      : NavRoutes("perfil")

    // ── Diario ───────────────────────────────────────────────
    object CrearEntrada  : NavRoutes("crear_entrada")
    object DetalleEntrada : NavRoutes("detalle_entrada/{entradaId}") {
        // Función para construir la ruta con el ID real
        fun crearRuta(entradaId: String) = "detalle_entrada/$entradaId"
    }

    // ── Eventos ──────────────────────────────────────────────
    object Eventos     : NavRoutes("eventos")
    object CrearEvento : NavRoutes("crear_evento")

    // ── Notas ────────────────────────────────────────────────
    object Notas       : NavRoutes("notas")

    // ── Estados ──────────────────────────────────────────────
    object Estados     : NavRoutes("estados")
}