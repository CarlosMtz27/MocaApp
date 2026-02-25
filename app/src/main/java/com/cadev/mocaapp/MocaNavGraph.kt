package com.cadev.mocaapp

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.ui.PlaceholderScreen
import com.cadev.mocaapp.feature.auth.ui.AuthViewModel
import com.cadev.mocaapp.feature.auth.ui.LoginScreen
import com.cadev.mocaapp.feature.auth.ui.RegistroScreen


@Composable
fun MocaNavGraph(
    navController: NavHostController,
    factory: ViewModelProvider.Factory   // ← recibe el factory, no crea Firebase
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.Login.route
    ) {

        composable(NavRoutes.Login.route) {
            val viewModel: AuthViewModel = viewModel(factory = factory)
            LoginScreen(
                viewModel = viewModel,
                onLoginExitoso = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onIrARegistro = {
                    navController.navigate(NavRoutes.Registro.route)
                }
            )
        }

        composable(NavRoutes.Registro.route) {
            val viewModel: AuthViewModel = viewModel(factory = factory)
            RegistroScreen(
                viewModel = viewModel,
                onRegistroExitoso = {
                    navController.navigate(NavRoutes.CodigoPareja.route) {
                        // Limpia todo el stack de auth — ya no puede volver atrás
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onIrALogin = {
                    navController.popBackStack() // simplemente regresa a Login
                }
            )
        }

        composable(NavRoutes.CodigoPareja.route) {
            PlaceholderScreen("Código de Pareja")
        }

        composable(NavRoutes.Main.route) {
            PlaceholderScreen("Main")
        }

        composable(NavRoutes.CrearEntrada.route) {
            PlaceholderScreen("Crear Entrada")
        }

        composable(NavRoutes.DetalleEntrada.route) { backStackEntry ->
            val entradaId = backStackEntry.arguments?.getString("entradaId") ?: ""
            PlaceholderScreen("Detalle: $entradaId")
        }

        composable(NavRoutes.Eventos.route) {
            PlaceholderScreen("Eventos")
        }

        composable(NavRoutes.CrearEvento.route) {
            PlaceholderScreen("Crear Evento")
        }

        composable(NavRoutes.Notas.route) {
            PlaceholderScreen("Notas")
        }

        composable(NavRoutes.Estados.route) {
            PlaceholderScreen("Estadísticas")
        }
    }
}