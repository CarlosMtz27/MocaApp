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
    //NavController propio para los tabs
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination

    // Datos del usuario, definidos UNA sola vez
    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    val parejaId = remember(uid) {
        runBlocking { UsuarioHelper.obtenerParejaId(uid) }
    }

    //PerfilViewModel compartido entre todas las pantallas
    val perfilViewModel: PerfilViewModel = viewModel(factory = factory)

    // Precargar perfil al arrancar — cuando el usuario llegue
    //   a Perfil, Ajustes, Chat o PerfilPareja ya están los datos
    LaunchedEffect(uid) {
        perfilViewModel.cargarPerfil(uid, parejaId)
    }

    val perfilState by perfilViewModel.uiState.collectAsState()

    //Tabs de la barra inferior
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
                                // Evita acumular destinos en el back stack
                                popUpTo(
                                    tabNavController.graph
                                        .findStartDestination().id
                                ) { saveState = true }
                                // Evita copias del mismo destino
                                launchSingleTop = true
                                // Restaura el estado al regresar a un tab
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

            // Calendario
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

            // Chat
            composable(NavRoutes.Chat.route) {
                if (parejaId != null) {
                    val chatViewModel: ChatViewModel = viewModel(factory = factory)
                    // perfilViewModel ya cargado — nombre y foto disponibles
                    ChatScreen(
                        viewModel = chatViewModel,
                        usuarioId = uid,
                        parejaId = parejaId,
                        nombrePareja = perfilState.pareja?.nombre ?: "Mi pareja",
                        fotoPareja = perfilState.pareja?.fotoPerfil,
                        onRegresar = { /* tab — sin navegación atrás */ }
                    )
                } else {
                    PlaceholderScreen("💬 Vincula tu pareja primero")
                }
            }

            composable(NavRoutes.Cuestionarios.route) {
                val cuestionarioViewModel: CuestionarioViewModel = viewModel(factory = factory)
                val relacionId = perfilState.usuario?.relacionId ?: ""

                CuestionariosScreen(
                    viewModel = cuestionarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaId ?: "",
                    relacionId = relacionId,
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
                //Misma instancia — datos ya cargados
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
                    }
                )
            }

            // Ajustes
            composable(NavRoutes.Ajustes.route) {
                //Misma instancia — datos ya cargados sin consulta extra
                AjustesScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onRegresar = { navController.popBackStack() }
                )
            }

            // Perfil de pareja
            composable(
                route = NavRoutes.PerfilPareja.route,
                arguments = listOf(
                    navArgument("parejaId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val pId = backStackEntry.arguments
                    ?.getString("parejaId") ?: ""
                //Misma instancia — pareja ya cargada desde el inicio
                PerfilParejaScreen(
                    viewModel = perfilViewModel,
                    parejaId = pId,
                    onRegresar = { navController.popBackStack() }
                )
            }
        }
    }
}