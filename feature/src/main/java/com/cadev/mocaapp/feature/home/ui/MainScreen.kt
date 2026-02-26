package com.cadev.mocaapp.feature.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.cadev.mocaapp.core.ui.BottomNavItem
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.ui.PlaceholderScreen
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.diario.ui.CalendarioScreen
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking


@Composable
fun MainScreen(factory: ViewModelProvider.Factory,
               navController: NavHostController
) {

    // NavController propio para los tabs, independiente del principal
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination

    // Lista de tabs en el orden que aparecen
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
                                    tabNavController.graph.findStartDestination().id
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

        // NavHost interno de los tabs
        NavHost(
            navController = tabNavController,
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen()
            }
            composable(NavRoutes.Calendario.route) {
                val viewModel: DiarioViewModel = viewModel(factory = factory)
                val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
                val parejaId = remember(uid) {
                    runBlocking { UsuarioHelper.obtenerParejaId(uid) }
                }
                CalendarioScreen(
                    viewModel = viewModel,
                    usuarioId = uid,
                    parejaId = parejaId,
                    onDiaSeleccionado = { fecha ->
                        navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                    }
                )
            }
            composable(NavRoutes.Chat.route) {
                PlaceholderScreen("💬 Chat")
            }
            composable(NavRoutes.Cuestionarios.route) {
                PlaceholderScreen("📋 Cuestionarios")
            }
            composable(NavRoutes.Perfil.route) {
                PlaceholderScreen("👤 Perfil")
            }
        }
    }
}