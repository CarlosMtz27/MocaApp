package com.cadev.mocaapp.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cadev.mocaapp.core.ui.BottomNavItem
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.ui.PlaceholderScreen
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
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilParejaScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

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
    val parejaId = remember(uid) {
        runBlocking { UsuarioHelper.obtenerParejaId(uid) }
    }

    // ViewModels compartidos, todos a nivel de MainScreen para que no se recreen al cambiar tab
    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
    val cuestionarioViewModel: CuestionarioViewModel = viewModel(factory = factory)
    val chatViewModel: ChatViewModel = viewModel(factory = factory)
    val notificacionViewModel: NotificacionViewModel = viewModel(factory = factory)
    val eventoViewModel: EventoViewModel = viewModel(factory = factory)
    val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
    val notaViewModel: NotaViewModel = viewModel(factory = factory)
    val estadoAnimoViewModel: EstadoAnimoViewModel = viewModel(factory = factory)

    val perfilState by perfilViewModel.uiState.collectAsState()
    val contadores by notificacionViewModel.contadores.collectAsState()

    // Inicializar escucha de badges
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            notificacionViewModel.iniciar(uid)
        }
    }

    // Precarga del perfil
    LaunchedEffect(uid) {
        perfilViewModel.cargarPerfil(uid, parejaId)
    }

    // Precarga de eventos, actividad diaria y notas
    LaunchedEffect(uid, parejaId, perfilState.usuario?.relacionId) {
        val relacionId = perfilState.usuario?.relacionId ?: return@LaunchedEffect
        eventoViewModel.cargarEventos(relacionId)
        diarioViewModel.cargarUltimaActividad(uid, parejaId)
        notaViewModel.iniciar(context, relacionId, uid, parejaId)
    }

    // Inicializar chat, ahora solo una vez, no cada vez que se entra al tab
    LaunchedEffect(uid, parejaId) {
        if (uid.isNotBlank() && !parejaId.isNullOrBlank()) {
            chatViewModel.inicializar(uid, parejaId)
        }
    }

    // Precarga cuestionarios
    LaunchedEffect(perfilState.usuario?.relacionId) {
        val relacionId = perfilState.usuario?.relacionId ?: return@LaunchedEffect
        cuestionarioViewModel.cargarCuestionarios(
            relacionId = relacionId,
            usuarioId = uid,
            parejaId = parejaId ?: ""
        )
        cuestionarioViewModel.poblarPredefinidos()
    }

    // Limpiar badge al entrar a cada tab
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

    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendario,
        BottomNavItem.Chat,
        BottomNavItem.Cuestionarios,
        BottomNavItem.Perfil
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { tab ->
                    val seleccionado = destinoActual
                        ?.hierarchy
                        ?.any { it.route == tab.route } == true

                    // Contar badge según tab
                    val badgeCount = when (tab.route) {
                        NavRoutes.Chat.route -> contadores.chat
                        NavRoutes.Calendario.route -> contadores.diario
                        NavRoutes.Cuestionarios.route -> contadores.cuestionarios
                        else -> 0
                    }

                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = {
                            tabNavController.navigate(tab.route) {
                                popUpTo(
                                    tabNavController.graph
                                        .findStartDestination().id
                                ) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            if (badgeCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(
                                                if (badgeCount > 9) "9+"
                                                else badgeCount.toString()
                                            )
                                        }
                                    }
                                ) {
                                    Icon(tab.icono, tab.etiqueta)
                                }
                            } else {
                                Icon(tab.icono, tab.etiqueta)
                            }
                        },
                        label = { Text(tab.etiqueta) }
                    )
                }
            }
        }
    ) { paddingValues ->

        NavHost(
            navController = tabNavController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {

            composable(NavRoutes.Home.route) {
                HomeScreen(
                    perfilViewModel = perfilViewModel,
                    eventoViewModel = eventoViewModel,
                    diarioViewModel = diarioViewModel,
                    cuestionarioViewModel = cuestionarioViewModel,
                    notificacionViewModel = notificacionViewModel,
                    notaViewModel = notaViewModel,
                    estadoAnimoViewModel = estadoAnimoViewModel,
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
                    }
                )
            }

            composable(NavRoutes.Calendario.route) {
                CalendarioScreen(
                    viewModel = diarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onDiaSeleccionado = { fecha ->
                        navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                    },
                    onVerEventos = {
                        navController.navigate(NavRoutes.Eventos.route)
                    }
                )
            }

            composable(NavRoutes.Chat.route) {
                if (parejaId != null) {
                    // Reusamos el chatViewModel ya inicializado
                    ChatScreen(
                        viewModel = chatViewModel,
                        usuarioId = uid,
                        parejaId = parejaId,
                        nombrePareja = perfilState.pareja?.nombre ?: "Mi pareja",
                        fotoPareja = perfilState.pareja?.fotoPerfil,
                        onRegresar = { }
                    )
                } else {
                    PlaceholderScreen("💬 Vincula tu pareja primero")
                }
            }

            composable(NavRoutes.Cuestionarios.route) {
                CuestionariosScreen(
                    viewModel = cuestionarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaId ?: "",
                    relacionId = perfilState.usuario?.relacionId ?: "",
                    onIniciarCuestionario = { id ->
                        navController.navigate(NavRoutes.ResponderCuestionario.crearRuta(id))
                    },
                    onVerResultados = { id ->
                        navController.navigate(NavRoutes.ResultadosCuestionario.crearRuta(id))
                    },
                    onCrearCuestionario = {
                        navController.navigate(NavRoutes.CrearCuestionario.route)
                    }
                )
            }

            composable(NavRoutes.Perfil.route) {
                PerfilScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onIrAjustes = { navController.navigate(NavRoutes.Ajustes.route) },
                    onVerPerfilPareja = { id ->
                        navController.navigate(NavRoutes.PerfilPareja.crearRuta(id))
                    },
                    onLogout = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        FirebaseAuth.getInstance().signOut()
                    }
                )
            }

            composable(NavRoutes.Ajustes.route) {
                AjustesScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onRegresar = { navController.popBackStack() }
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
                    onRegresar = { navController.popBackStack() }
                )
            }
        }
    }
}
