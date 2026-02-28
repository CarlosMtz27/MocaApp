package com.cadev.mocaapp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.ui.PlaceholderScreen
import com.cadev.mocaapp.feature.auth.ui.AuthViewModel
import com.cadev.mocaapp.feature.auth.ui.LoginScreen
import com.cadev.mocaapp.feature.auth.ui.RegistroScreen
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.CalendarioScreen
import com.cadev.mocaapp.feature.diario.ui.CrearEntradaScreen
import com.cadev.mocaapp.feature.diario.ui.DetalleDiaScreen
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.diario.ui.EditarEntradaScreen
import com.cadev.mocaapp.feature.home.ui.MainScreen
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.pareja.ui.CodigoParejaScreen
import com.cadev.mocaapp.feature.pareja.ui.FechaRelacionScreen
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking
import com.cadev.mocaapp.feature.diario.ui.DetalleEntradaScreen
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel


@Composable
fun MocaNavGraph(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    destinoInicial: String = NavRoutes.Login.route
) {
    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {

        //Auth

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
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                onIrALogin = { navController.popBackStack() }
            )
        }

        // Pareja
        composable(NavRoutes.CodigoPareja.route) {
            val viewModel: ParejaViewModel = viewModel(factory = factory)
            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            CodigoParejaScreen(
                viewModel = viewModel,
                usuarioId = usuarioId,
                onVinculado = { relacionId ->
                    navController.navigate(
                        NavRoutes.FechaRelacion.crearRuta(relacionId)
                    )
                }
            )
        }

        composable(
            route = NavRoutes.FechaRelacion.route,
            arguments = listOf(
                navArgument("relacionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val relacionId = backStackEntry.arguments?.getString("relacionId") ?: ""
            val viewModel: ParejaViewModel = viewModel(factory = factory)
            FechaRelacionScreen(
                viewModel = viewModel,
                relacionId = relacionId,
                onFechaGuardada = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                }
            )
        }

        //Main
        composable(NavRoutes.Main.route) {
            MainScreen(
                factory = factory,
                navController = navController
            )
        }

        //Diario

        composable(
            route = NavRoutes.DetalleDia.route,
            arguments = listOf(
                navArgument("fecha") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            DetalleDiaScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId,
                fecha = fecha,
                onRegresar = { navController.popBackStack() },
                onEditarEntrada = { entradaId ->
                    navController.navigate(
                        NavRoutes.EditarEntrada.crearRuta(entradaId)
                    )
                },
                onCrearEntrada = { f, tipo ->
                    navController.navigate(
                        NavRoutes.CrearEntrada.crearRuta(f, tipo)
                    )
                },
                onVerDetalle = { entradaId ->
                    navController.navigate(
                        NavRoutes.DetalleEntrada.crearRuta(entradaId)
                    )
                }
            )
        }

        composable(
            route = NavRoutes.CrearEntrada.route,
            arguments = listOf(
                navArgument("fecha") { type = NavType.StringType },
                navArgument("tipo") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val tipo = backStackEntry.arguments?.getString("tipo")
                ?: TipoEntrada.MI_DIA.name
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            CrearEntradaScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId,
                fecha = fecha,
                tipo = tipo,
                onEntradaGuardada = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.EditarEntrada.route,
            arguments = listOf(
                navArgument("entradaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entradaId = backStackEntry.arguments?.getString("entradaId") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            EditarEntradaScreen(
                viewModel = viewModel,
                entradaId = entradaId,
                parejaId = parejaId,
                onGuardado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DetalleEntrada.route,
            arguments = listOf(
                navArgument("entradaId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val entradaId = backStackEntry.arguments?.getString("entradaId") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            DetalleEntradaScreen(
                viewModel = viewModel,
                entradaId = entradaId,
                usuarioId = uid,
                onRegresar = { navController.popBackStack() },
                onEditar = { id ->
                    navController.navigate(NavRoutes.EditarEntrada.crearRuta(id))
                }
            )
        }

        composable(NavRoutes.Ajustes.route) {
            val viewModel: PerfilViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            AjustesScreen(
                viewModel = viewModel,
                usuarioId = uid,
                onRegresar = { navController.popBackStack() }
            )
        }

        //Otros
        composable(NavRoutes.Eventos.route) {
            PlaceholderScreen("🗓️ Eventos")
        }

        composable(NavRoutes.CrearEvento.route) {
            PlaceholderScreen("➕ Crear Evento")
        }

        composable(NavRoutes.Notas.route) {
            PlaceholderScreen("🗒️ Notas")
        }

        composable(NavRoutes.Estados.route) {
            PlaceholderScreen("📊 Estadísticas")
        }
    }
}