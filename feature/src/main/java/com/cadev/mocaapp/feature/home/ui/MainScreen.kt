package com.cadev.mocaapp.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.ui.PlaceholderScreen
import com.cadev.mocaapp.core.utils.ThemeManager
import com.cadev.mocaapp.feature.chat.ui.ChatScreen
import com.cadev.mocaapp.feature.chat.ui.ChatViewModel
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionarioViewModel
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionariosScreen
import com.cadev.mocaapp.feature.diario.ui.CalendarioScreen
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.eventos.ui.EventoViewModel
import com.cadev.mocaapp.feature.estadoanimo.ui.EstadoAnimoViewModel
import com.cadev.mocaapp.feature.notas.ui.NotaViewModel
import com.cadev.mocaapp.feature.notificaciones.ui.NotificacionViewModel
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.pareja.ui.PerfilParejaScreen
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.feature.ui.screens.*
import com.cadev.mocaapp.feature.ui.components.*
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.utils.FondoMeshMoca
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

/**
 * ESTA ES LA PANTALLA CONTENEDORA PRINCIPAL (ESTILO ZEN)
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainScreen(
    factory: ViewModelProvider.Factory,
    navController: NavHostController,
    initialTab: String = ""
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination
    val rutaActual = destinoActual?.route
    val context = LocalContext.current

    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
    val cuestionarioViewModel: CuestionarioViewModel = viewModel(factory = factory)
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val notificacionViewModel: NotificacionViewModel = viewModel(factory = factory)
    val eventoViewModel: EventoViewModel = viewModel(factory = factory)
    val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
    val notaViewModel: NotaViewModel = viewModel(factory = factory)
    val estadoAnimoViewModel: EstadoAnimoViewModel = viewModel(factory = factory)
    val parejaViewModel: ParejaViewModel = viewModel(factory = factory)

    val perfilState by perfilViewModel.uiState.collectAsState()
    val contadores by notificacionViewModel.contadores.collectAsState()

    val irAlInicio = {
        tabNavController.navigate(NavRoutes.Home.route) {
            popUpTo(tabNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            notificacionViewModel.iniciar(uid)
            perfilViewModel.iniciarEscucha(uid)
        }
    }

    val usuario = perfilState.usuario
    val parejaIdActual = usuario?.parejaId
    val relacionIdActual = usuario?.relacionId

    // Pantalla de carga Zen
    if (usuario == null) {
        LoadingTransition()
        return
    }

    LaunchedEffect(uid, parejaIdActual, relacionIdActual) {
        if (!relacionIdActual.isNullOrBlank()) {
            eventoViewModel.iniciarEscucha(context, relacionIdActual)
            diarioViewModel.iniciarEscucha(uid, parejaIdActual, relacionIdActual)
            notaViewModel.iniciar(context, relacionIdActual, uid, parejaIdActual)
        }
    }

    LaunchedEffect(uid, parejaIdActual) {
        if (uid.isNotBlank() && !parejaIdActual.isNullOrBlank()) {
            chatViewModel.inicializar(uid, usuario.nombre, usuario.fotoPerfil, parejaIdActual)
        }
    }

    LaunchedEffect(relacionIdActual, uid, parejaIdActual) {
        if (!relacionIdActual.isNullOrBlank()) {
            cuestionarioViewModel.iniciarEscucha(relacionIdActual, uid, parejaIdActual ?: "")
            cuestionarioViewModel.poblarPredefinidos()
        }
    }

    LaunchedEffect(rutaActual) {
        if (uid.isBlank()) return@LaunchedEffect
        when (rutaActual) {
            NavRoutes.Chat.route -> notificacionViewModel.limpiarChat(uid)
            NavRoutes.Calendario.route -> notificacionViewModel.limpiarDiario(uid)
            NavRoutes.Cuestionarios.route -> notificacionViewModel.limpiarCuestionarios(uid)
        }
    }

    LaunchedEffect(initialTab) {
        if (initialTab.isBlank()) return@LaunchedEffect
        val ruta = when (initialTab) {
            "home"          -> NavRoutes.Home.route
            "chat"          -> NavRoutes.Chat.route
            "calendario"    -> NavRoutes.Calendario.route
            "cuestionarios" -> NavRoutes.Cuestionarios.route
            "perfil"        -> NavRoutes.Perfil.route
            else            -> return@LaunchedEffect
        }
        kotlinx.coroutines.delay(300)
        tabNavController.navigate(ruta) {
            popUpTo(tabNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    // Definición de items para la barra flotante
    val itemsNavegacion = listOf(
        ItemNavegacionData("Inicio", Icons.Default.Home),
        ItemNavegacionData("Diario", Icons.Default.CalendarToday, mostrarNotificacion = contadores.diario > 0),
        ItemNavegacionData("Chat", Icons.Default.ChatBubble, mostrarNotificacion = contadores.chat > 0),
        ItemNavegacionData("Tests", Icons.Default.Quiz, mostrarNotificacion = contadores.cuestionarios > 0),
        ItemNavegacionData("Perfil", Icons.Default.Person)
    )

    val indiceSeleccionado = when (rutaActual) {
        NavRoutes.Home.route -> 0
        NavRoutes.Calendario.route -> 1
        NavRoutes.Chat.route -> 2
        NavRoutes.Cuestionarios.route -> 3
        NavRoutes.Perfil.route -> 4
        else -> 0
    }

    FondoMeshMoca {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                if (rutaActual != NavRoutes.Chat.route) {
                    val titulo = when (rutaActual) {
                        NavRoutes.Home.route -> "Inicio"
                        NavRoutes.Calendario.route -> "Nuestro Diario"
                        NavRoutes.Historial.route -> "Nuestra Historia"
                        NavRoutes.Chat.route -> "Chat Privado"
                        NavRoutes.Cuestionarios.route -> "Retos y Tests"
                        NavRoutes.Perfil.route -> "Mi Perfil"
                        else -> "Moca"
                    }
                    MocaHeader(
                        titulo = titulo,
                        nombreUsuario = usuario.nombre,
                        nombrePareja = perfilState.pareja?.nombre ?: "",
                        urlAvatarUsuario = usuario.fotoPerfil,
                        urlAvatarPareja = perfilState.pareja?.fotoPerfil ?: "",
                        esModoOscuro = ThemeManager.isDarkTheme,
                        alHacerClickEnTema = { ThemeManager.isDarkTheme = !ThemeManager.isDarkTheme }
                    )
                }
            },
            bottomBar = {
                BarraNavegacionFlotante(
                    indiceSeleccionado = indiceSeleccionado,
                    alSeleccionarItem = { i ->
                        val ruta = when (i) {
                            0 -> NavRoutes.Home.route
                            1 -> NavRoutes.Calendario.route
                            2 -> NavRoutes.Chat.route
                            3 -> NavRoutes.Cuestionarios.route
                            4 -> NavRoutes.Perfil.route
                            else -> NavRoutes.Home.route
                        }
                        tabNavController.navigate(ruta) {
                            popUpTo(tabNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    items = itemsNavegacion
                )
            }
        ) { paddingValues ->
            AnimatedContent(
                targetState = rutaActual,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "transicionPestana"
            ) { targetRuta ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    NavHost(
                        navController = tabNavController,
                        startDestination = NavRoutes.Home.route
                    ) {
                        composable(NavRoutes.Home.route) {
                            HomeScreen(
                                perfilViewModel = perfilViewModel,
                                eventoViewModel = eventoViewModel,
                                diarioViewModel = diarioViewModel,
                                cuestionarioViewModel = cuestionarioViewModel,
                                notaViewModel = notaViewModel,
                                estadoAnimoViewModel = estadoAnimoViewModel,
                                parejaViewModel = parejaViewModel,
                                onNavigateToTab = { route ->
                                    tabNavController.navigate(route) {
                                        popUpTo(tabNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                onNavigateToScreen = { route ->
                                    navController.navigate(route)
                                },
                                onIrAVincular = {
                                    navController.navigate(NavRoutes.CodigoPareja.route)
                                },
                                onVinculado = { relacionId ->
                                    navController.navigate(NavRoutes.FechaRelacion.crearRuta(relacionId))
                                }
                            )
                        }

                        composable(NavRoutes.Calendario.route) {
                            CalendarView(
                                viewModel = diarioViewModel,
                                usuarioId = uid,
                                parejaId = parejaIdActual,
                                relacionId = relacionIdActual ?: "",
                                onRegresar = irAlInicio,
                                onDiaSeleccionado = { fecha ->
                                    navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                                },
                                onVerListado = {
                                    tabNavController.navigate(NavRoutes.Historial.route)
                                },
                                onVerEventos = {
                                    navController.navigate(NavRoutes.Eventos.route)
                                }
                            )
                        }

                        composable(NavRoutes.Historial.route) {
                            TimelineScreen(
                                viewModel = diarioViewModel,
                                usuarioId = uid,
                                parejaId = parejaIdActual,
                                relacionId = relacionIdActual ?: "",
                                onVerDetalleEntrada = { id ->
                                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                                },
                                onVerDetalleEvento = { id ->
                                    navController.navigate(NavRoutes.DetalleEvento.crearRuta(id))
                                },
                                onIrAAjustes = { navController.navigate(NavRoutes.Ajustes.route) },
                                onRegresar = { tabNavController.popBackStack() }
                            )
                        }

                        composable(NavRoutes.Chat.route) {
                            if (!parejaIdActual.isNullOrBlank()) {
                                ChatScreen(
                                    viewModel = chatViewModel,
                                    usuarioId = uid,
                                    usuarioNombre = usuario.nombre,
                                    usuarioFoto = usuario.fotoPerfil,
                                    parejaId = parejaIdActual,
                                    nombrePareja = perfilState.pareja?.nombre ?: "Mi pareja",
                                    fotoPareja = perfilState.pareja?.fotoPerfil,
                                    onRegresar = irAlInicio
                                )
                            } else {
                                PlaceholderScreen("Vincula tu pareja primero")
                            }
                        }

                        composable(NavRoutes.Cuestionarios.route) {
                            CuestionariosScreen(
                                viewModel = cuestionarioViewModel,
                                usuarioId = uid,
                                parejaId = parejaIdActual ?: "",
                                relacionId = relacionIdActual ?: "",
                                nombrePareja = perfilState.pareja?.nombre ?: "Pareja",
                                onRegresar = irAlInicio,
                                onIniciarCuestionario = { id ->
                                    navController.navigate(NavRoutes.ResponderCuestionario.crearRuta(id))
                                },
                                onVerResultados = { id ->
                                    navController.navigate(NavRoutes.ResultadosCuestionario.crearRuta(id))
                                },
                                onCrearCuestionario = {
                                    navController.navigate(NavRoutes.CrearCuestionario.route)
                                },
                                onConfiguracion = {
                                    navController.navigate(NavRoutes.Ajustes.route)
                                }
                            )
                        }

                        composable(NavRoutes.Perfil.route) {
                            PerfilScreen(
                                viewModel = perfilViewModel,
                                usuarioId = uid,
                                parejaId = parejaIdActual,
                                onRegresar = irAlInicio,
                                onIrAjustes = { navController.navigate(NavRoutes.Ajustes.route) },
                                onVerPerfilPareja = { id ->
                                    navController.navigate(NavRoutes.PerfilPareja.crearRuta(id))
                                },
                                onLogout = {
                                    navController.navigate(NavRoutes.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    FirebaseAuth.getInstance().signOut()
                                },
                                onVerDetalleEntrada = { id ->
                                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                                },
                                onVerDetalleDia = { fecha ->
                                    navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                                },
                                onIrATests = {
                                    tabNavController.navigate(NavRoutes.Cuestionarios.route) {
                                        popUpTo(tabNavController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }

                        composable(NavRoutes.Ajustes.route) {
                            AjustesScreen(
                                viewModel = perfilViewModel,
                                usuarioId = uid,
                                parejaId = parejaIdActual,
                                onRegresar = { navController.popBackStack() },
                                onNavigateToWidgets = { navController.navigate(NavRoutes.WidgetsPreview.route) },
                                onLogout = {
                                    navController.navigate(NavRoutes.Login.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    FirebaseAuth.getInstance().signOut()
                                }
                            )
                        }

                        composable(
                            route = NavRoutes.PerfilPareja.route,
                            arguments = listOf(
                                navArgument("parejaId") { type = NavType.StringType }
                            )
                        ) { backStackEntry ->
                            val pId = backStackEntry.arguments?.getString("parejaId") ?: ""
                            PerfilParejaScreen(
                                viewModel = perfilViewModel,
                                parejaId = pId,
                                onRegresar = { navController.popBackStack() },
                                onVerDetalleEntrada = { id ->
                                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                                },
                                onVerDetalleDia = { fecha ->
                                    navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
