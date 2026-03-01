package com.cadev.mocaapp.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
    navController: NavHostController
) {
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination

    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    val parejaId = remember(uid) {
        runBlocking { UsuarioHelper.obtenerParejaId(uid) }
    }

    //ViewModels compartidos
    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)

    //CuestionarioViewModel creado a nivel de MainScreen
    //para que sobreviva cambios de tab y no se recree
    val cuestionarioViewModel: CuestionarioViewModel = viewModel(factory = factory)

    val perfilState by perfilViewModel.uiState.collectAsState()

    // ── Precarga del perfil ───────────────────────────────────
    LaunchedEffect(uid) {
        perfilViewModel.cargarPerfil(uid, parejaId)
    }

    // Precarga de cuestionarios en cuanto tengamos relacionId
    //No esperamos a que el usuario entre al tab
    LaunchedEffect(perfilState.usuario?.relacionId) {
        val relacionId = perfilState.usuario?.relacionId ?: return@LaunchedEffect
        cuestionarioViewModel.cargarCuestionarios(
            relacionId = relacionId,
            usuarioId = uid,
            parejaId = parejaId ?: ""
        )
        // Poblar predefinidos aqui, solo hace algo si no existen
        cuestionarioViewModel.poblarPredefinidos()
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
                            Icon(
                                imageVector = tab.icono,
                                contentDescription = tab.etiqueta
                            )
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

            //Home
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }

            //Calendario
            composable(NavRoutes.Calendario.route) {
                val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
                CalendarioScreen(
                    viewModel = diarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onDiaSeleccionado = { fecha ->
                        navController.navigate(
                            NavRoutes.DetalleDia.crearRuta(fecha)
                        )
                    }
                )
            }

            //Chat
            composable(NavRoutes.Chat.route) {
                if (parejaId != null) {
                    val chatViewModel: ChatViewModel = viewModel(factory = factory)
                    ChatScreen(
                        viewModel = chatViewModel,
                        usuarioId = uid,
                        parejaId = parejaId,
                        nombrePareja = perfilState.pareja?.nombre ?: "Mi pareja",
                        fotoPareja = perfilState.pareja?.fotoPerfil,
                        onRegresar = { /* tab sin navegacion atras */ }
                    )
                } else {
                    PlaceholderScreen("💬 Vincula tu pareja primero")
                }
            }

            //Cuestionarios
            composable(NavRoutes.Cuestionarios.route) {
                // Reusar el viewModel ya cargado, sin nueva instancia
                CuestionariosScreen(
                    viewModel = cuestionarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaId ?: "",
                    relacionId = perfilState.usuario?.relacionId ?: "",
                    onIniciarCuestionario = { id ->
                        navController.navigate(
                            NavRoutes.ResponderCuestionario.crearRuta(id)
                        )
                    },
                    onVerResultados = { id ->
                        navController.navigate(
                            NavRoutes.ResultadosCuestionario.crearRuta(id)
                        )
                    },
                    onCrearCuestionario = {
                        navController.navigate(NavRoutes.CrearCuestionario.route)
                    }
                )
            }

            //Perfil
            composable(NavRoutes.Perfil.route) {
                PerfilScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onIrAjustes = {
                        navController.navigate(NavRoutes.Ajustes.route)
                    },
                    onVerPerfilPareja = { id ->
                        navController.navigate(
                            NavRoutes.PerfilPareja.crearRuta(id)
                        )
                    },
                    onLogout = {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        FirebaseAuth.getInstance().signOut()
                    }
                )
            }

            //Ajustes
            composable(NavRoutes.Ajustes.route) {
                AjustesScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onRegresar = { navController.popBackStack() }
                )
            }

            //Perfil de pareja
            composable(
                route = NavRoutes.PerfilPareja.route,
                arguments = listOf(
                    navArgument("parejaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val pId = backStackEntry.arguments
                    ?.getString("parejaId") ?: ""
                PerfilParejaScreen(
                    viewModel = perfilViewModel,
                    parejaId = pId,
                    onRegresar = { navController.popBackStack() }
                )
            }
        }
    }
}