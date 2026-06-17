package com.cadev.mocaapp

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.cadev.mocaapp.feature.cuestionarios.ui.CrearCuestionarioScreen
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionarioViewModel
import com.cadev.mocaapp.feature.cuestionarios.ui.ResponderScreen
import com.cadev.mocaapp.feature.cuestionarios.ui.ResultadosScreen
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.CalendarioScreen
import com.cadev.mocaapp.feature.diario.ui.CrearEntradaScreen
import com.cadev.mocaapp.feature.diario.ui.DetalleEntradaScreen
import com.cadev.mocaapp.feature.diario.ui.DetalleDiaScreen
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.diario.ui.EditarEntradaScreen
import com.cadev.mocaapp.feature.eventos.ui.CrearEventoScreen
import com.cadev.mocaapp.feature.eventos.ui.DetalleEventoScreen
import com.cadev.mocaapp.feature.eventos.ui.EditarEventoScreen
import com.cadev.mocaapp.feature.eventos.ui.EventoViewModel
import com.cadev.mocaapp.feature.eventos.ui.EventosScreen
import com.cadev.mocaapp.feature.home.ui.MainScreen
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.pareja.ui.CodigoParejaScreen
import com.cadev.mocaapp.feature.pareja.ui.FechaRelacionScreen
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilParejaScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

@RequiresApi(Build.VERSION_CODES.S)
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

        // Auth
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

        //Pareja
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
            val relacionId =
                backStackEntry.arguments?.getString("relacionId") ?: ""
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

        // Reemplaza el composable de Main
        composable(
            route = "${NavRoutes.Main.route}?tab={tab}",
            arguments = listOf(
                navArgument("tab") {
                    type = NavType.StringType
                    defaultValue = ""
                    nullable = true
                }
            )
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getString("tab") ?: ""
            MainScreen(
                factory = factory,
                navController = navController,
                initialTab = initialTab    // ← nuevo parámetro
            )
        }
        composable(NavRoutes.Calendario.route) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
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
            val entradaId =
                backStackEntry.arguments?.getString("entradaId") ?: ""
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
            val entradaId =
                backStackEntry.arguments?.getString("entradaId") ?: ""
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

        //Perfil, Ajustes
        composable(NavRoutes.Ajustes.route) { backStackEntry ->
            //Compartir ViewModel con Main para no perder datos
            val mainEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(NavRoutes.Main.route)
                } catch (e: Exception) { backStackEntry }
            }
            val viewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = mainEntry,
                factory = factory
            )
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            LaunchedEffect(uid) {
                if (viewModel.uiState.value.usuario == null) {
                    viewModel.cargarPerfil(uid, parejaId)
                }
            }
            AjustesScreen(
                viewModel = viewModel,
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
            val parejaId =
                backStackEntry.arguments?.getString("parejaId") ?: ""
            // Compartir ViewModel con Main para reusar datos ya cargados
            val mainEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(NavRoutes.Main.route)
                } catch (e: Exception) { backStackEntry }
            }
            val viewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = mainEntry,
                factory = factory
            )
            PerfilParejaScreen(
                viewModel = viewModel,
                parejaId = parejaId,
                onRegresar = { navController.popBackStack() }
            )
        }

        //Cuestionarios
        composable(
            route = NavRoutes.ResponderCuestionario.route,
            arguments = listOf(
                navArgument("cuestionarioId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cuestionarioId = backStackEntry.arguments
                ?.getString("cuestionarioId") ?: ""
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            val relacionId = remember(uid) {
                runBlocking {
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .get()
                        .await()
                        .getString("relacionId") ?: ""
                }
            }
            ResponderScreen(
                viewModel = viewModel,
                cuestionarioId = cuestionarioId,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                relacionId = relacionId,
                onCompletado = { id ->
                    navController.navigate(
                        NavRoutes.ResultadosCuestionario.crearRuta(id)
                    ) {
                        //Sacar ResponderScreen del back stack al completar
                        popUpTo(
                            NavRoutes.ResponderCuestionario.crearRuta(id)
                        ) { inclusive = true }
                    }
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.ResultadosCuestionario.route,
            arguments = listOf(
                navArgument("cuestionarioId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cuestionarioId = backStackEntry.arguments
                ?.getString("cuestionarioId") ?: ""
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            //Reusar PerfilViewModel de Main para nombres
            val mainEntry = remember(backStackEntry) {
                try {
                    navController.getBackStackEntry(NavRoutes.Main.route)
                } catch (e: Exception) { backStackEntry }
            }
            val perfilViewModel: PerfilViewModel = viewModel(
                viewModelStoreOwner = mainEntry,
                factory = factory
            )
            val perfilState by perfilViewModel.uiState.collectAsState()

            ResultadosScreen(
                viewModel = viewModel,
                cuestionarioId = cuestionarioId,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                nombreUsuario = perfilState.usuario?.nombre ?: "Tú",
                nombrePareja = perfilState.pareja?.nombre ?: "Tu pareja",
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CrearCuestionario.route) { backStackEntry ->
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {           // ← nuevo
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            val relacionId = remember(uid) {
                runBlocking {
                    FirebaseFirestore.getInstance()
                        .collection("usuarios")
                        .document(uid)
                        .get()
                        .await()
                        .getString("relacionId") ?: ""
                }
            }
            CrearCuestionarioScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId ?: "",           // ← agregar
                relacionId = relacionId,
                onCreado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Eventos.route) {
            val viewModel: EventoViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val relacionId = remember(uid) {
                runBlocking {
                    FirebaseFirestore.getInstance()
                        .collection("usuarios").document(uid)
                        .get().await().getString("relacionId") ?: ""
                }
            }
            EventosScreen(
                viewModel = viewModel,
                relacionId = relacionId,
                onCrearEvento = { navController.navigate(NavRoutes.CrearEvento.route) },
                onVerEvento = { id ->
                    navController.navigate(NavRoutes.DetalleEvento.crearRuta(id))
                },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CrearEvento.route) {
            val viewModel: EventoViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) {
                runBlocking { UsuarioHelper.obtenerParejaId(uid) }
            }
            val relacionId = remember(uid) {
                runBlocking {
                    FirebaseFirestore.getInstance()
                        .collection("usuarios").document(uid)
                        .get().await().getString("relacionId") ?: ""
                }
            }
            CrearEventoScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                relacionId = relacionId,
                onGuardado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DetalleEvento.route,
            arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val viewModel: EventoViewModel = viewModel(factory = factory)
            DetalleEventoScreen(
                viewModel = viewModel,
                eventoId = eventoId,
                usuarioId = uid,
                onRegresar = { navController.popBackStack() },
                onEditar = { id ->
                    navController.navigate(NavRoutes.EditarEvento.crearRuta(id))
                }
            )
        }

        composable(
            route = NavRoutes.EditarEvento.route,
            arguments = listOf(navArgument("eventoId") { type = NavType.StringType })
        ) { backStackEntry ->
            val eventoId = backStackEntry.arguments?.getString("eventoId") ?: ""
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val viewModel: EventoViewModel = viewModel(factory = factory)
            EditarEventoScreen(
                viewModel = viewModel,
                eventoId = eventoId,
                usuarioId = uid,
                onGuardado = { navController.popBackStack() },
                onRegresar = { navController.popBackStack() }
            )
        }
        composable(NavRoutes.Notas.route) {
            PlaceholderScreen("🗒️ Notas")
        }
        composable(NavRoutes.Estados.route) {
            PlaceholderScreen("📊 Estadísticas")
        }
    }
}