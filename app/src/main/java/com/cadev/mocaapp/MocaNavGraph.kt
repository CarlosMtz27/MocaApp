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
import com.cadev.mocaapp.feature.auth.ui.PantallaLogin
import com.cadev.mocaapp.feature.auth.ui.PantallaRegistro
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
import com.cadev.mocaapp.feature.ui.screens.*
import com.cadev.mocaapp.feature.ui.screens.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.cadev.mocaapp.feature.home.ui.MainScreen
import com.cadev.mocaapp.feature.notas.ui.NotaViewModel
import com.cadev.mocaapp.feature.notas.ui.NotasScreen
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.pareja.ui.PantallaVincularPareja
import com.cadev.mocaapp.feature.pareja.ui.PantallaAniversario
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.pareja.ui.PerfilParejaScreen
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.screens.PantallaPruebaComponentes
import com.cadev.mocaapp.feature.widgets.ui.WidgetsPreviewScreen
import com.cadev.mocaapp.feature.widgets.ui.WidgetsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

/**
 * ESTE ES EL MAPA DE NAVEGACIÓN
 * 
 * Qué hace
 * Aquí es donde se definen todas las pantallas que existen en la aplicación y cómo se conectan unas con otras. 
 * Cada bloque que empieza por la palabra composable representa una pantalla distinta.
 * 
 * Cómo añadir una nueva pantalla
 * Primero debes registrar la nueva ruta en el archivo de rutas del núcleo de la aplicación. 
 * Después añade un nuevo bloque composable aquí abajo indicando esa ruta y llamando a la función 
 * que dibuja tu pantalla pasándole el gestor de datos o ViewModel que necesite.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MocaNavGraph(
    navController: NavHostController,
    factory: ViewModelProvider.Factory,
    destinoInicial: String = NavRoutes.Login.route
) {
    // Esta pequeña función sirve para volver rápidamente a la pantalla principal de la aplicación
    val irAlInicio = {
        navController.navigate(NavRoutes.Main.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    // El NavHost es el encargado de intercambiar las pantallas según la ruta solicitada
    NavHost(
        navController = navController,
        startDestination = destinoInicial
    ) {

        // PANTALLAS PARA ENTRAR O REGISTRARSE
        composable(NavRoutes.Login.route) {
            val viewModel: AuthViewModel = viewModel(factory = factory)
            PantallaLogin(
                viewModel = viewModel,
                alHacerLoginExitoso = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                alIrARegistro = {
                    navController.navigate(NavRoutes.Registro.route)
                }
            )
        }

        composable(NavRoutes.Registro.route) {
            val viewModel: AuthViewModel = viewModel(factory = factory)
            PantallaRegistro(
                viewModel = viewModel,
                alHacerRegistroExitoso = {
                    navController.navigate(NavRoutes.CodigoPareja.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                alIrALogin = { navController.popBackStack() }
            )
        }

        // PANTALLAS PARA CONECTARSE CON LA PAREJA
        composable(NavRoutes.CodigoPareja.route) {
            val viewModel: ParejaViewModel = viewModel(factory = factory)
            val usuarioId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            PantallaVincularPareja(
                viewModel = viewModel,
                usuarioId = usuarioId,
                alVincular = { relacionId ->
                    navController.navigate(NavRoutes.FechaRelacion.crearRuta(relacionId))
                },
                alVolver = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.FechaRelacion.route,
            arguments = listOf(navArgument("relacionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val relacionId = backStackEntry.arguments?.getString("relacionId") ?: ""
            val viewModel: ParejaViewModel = viewModel(factory = factory)
            PantallaAniversario(
                viewModel = viewModel,
                relacionId = relacionId,
                alGuardarFecha = {
                    navController.navigate(NavRoutes.Main.route) {
                        popUpTo(NavRoutes.Login.route) { inclusive = true }
                    }
                },
                alVolver = { navController.popBackStack() }
            )
        }

        // PANTALLA PRINCIPAL QUE CONTIENE LAS PESTAÑAS INFERIORES
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
                initialTab = initialTab
            )
        }

        // PANTALLAS DEL CALENDARIO Y DEL DIARIO DE LA PAREJA
        composable(NavRoutes.Calendario.route) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val relacionId = remember(uid) { runBlocking { UsuarioHelper.obtenerRelacionId(uid) } }
            val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
            
            CalendarView(
                viewModel = diarioViewModel,
                usuarioId = uid,
                parejaId = parejaId,
                relacionId = relacionId,
                onRegresar = irAlInicio,
                onDiaSeleccionado = { fecha ->
                    navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                },
                onVerListado = { navController.navigate(NavRoutes.Historial.route) },
                onVerEventos = { navController.navigate(NavRoutes.Eventos.route) }
            )
        }

        composable(NavRoutes.Historial.route) {
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val relacionId = remember(uid) { runBlocking { UsuarioHelper.obtenerRelacionId(uid) } }
            val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
            
            TimelineScreen(
                viewModel = diarioViewModel,
                usuarioId = uid,
                parejaId = parejaId,
                relacionId = relacionId,
                onVerDetalleEntrada = { id ->
                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                },
                onVerDetalleEvento = { id ->
                    navController.navigate(NavRoutes.DetalleEvento.crearRuta(id))
                },
                onIrAAjustes = { navController.navigate(NavRoutes.Ajustes.route) },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(
            route = NavRoutes.DetalleDia.route,
            arguments = listOf(navArgument("fecha") { type = NavType.StringType })
        ) { backStackEntry ->
            val fecha = backStackEntry.arguments?.getString("fecha") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            
            DayDetailList(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId,
                fecha = fecha,
                onRegresar = { navController.popBackStack() },
                onIrAlCalendario = {
                    navController.navigate(NavRoutes.Calendario.route) {
                        popUpTo(NavRoutes.Calendario.route) { inclusive = true }
                    }
                },
                onCrearEntrada = { f, t -> 
                    navController.navigate(NavRoutes.CrearEntrada.crearRuta(f, t)) 
                },
                onVerDetalleEntrada = { id -> 
                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id)) 
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
            val tipo = backStackEntry.arguments?.getString("tipo") ?: TipoEntrada.MI_DIA.name
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            
            CreateEntryScreen(
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
            arguments = listOf(navArgument("entradaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entradaId = backStackEntry.arguments?.getString("entradaId") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val parejaId = remember { runBlocking { UsuarioHelper.obtenerParejaId(FirebaseAuth.getInstance().currentUser?.uid ?: "") } }
            EditarEntradaScreen(
                viewModel = viewModel,
                entradaId = entradaId,
                parejaId = parejaId,
                onGuardado = irAlInicio,
                onRegresar = irAlInicio
            )
        }

        composable(
            route = NavRoutes.DetalleEntrada.route,
            arguments = listOf(navArgument("entradaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val entradaId = backStackEntry.arguments?.getString("entradaId") ?: ""
            val viewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            
            EntryDetailView(
                viewModel = viewModel,
                entradaId = entradaId,
                usuarioId = uid,
                onRegresar = { navController.popBackStack() }
            )
        }

        // PANTALLAS DE PERFIL Y CONFIGURACIÓN
        composable(NavRoutes.Ajustes.route) { backStackEntry ->
            val mainEntry = remember(backStackEntry) { try { navController.getBackStackEntry(NavRoutes.Main.route) } catch (e: Exception) { backStackEntry } }
            val viewModel: PerfilViewModel = viewModel(viewModelStoreOwner = mainEntry, factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            AjustesScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId,
                onRegresar = irAlInicio,
                onNavigateToWidgets = { navController.navigate(NavRoutes.WidgetsPreview.route) }
            )
        }

        composable(
            route = NavRoutes.PerfilPareja.route,
            arguments = listOf(navArgument("parejaId") { type = NavType.StringType })
        ) { backStackEntry ->
            val pId = backStackEntry.arguments?.getString("parejaId") ?: ""
            val mainEntry = remember(backStackEntry) { try { navController.getBackStackEntry(NavRoutes.Main.route) } catch (e: Exception) { backStackEntry } }
            val viewModel: PerfilViewModel = viewModel(viewModelStoreOwner = mainEntry, factory = factory)
            PerfilParejaScreen(
                viewModel = viewModel,
                parejaId = pId,

                onRegresar = irAlInicio,
                onVerDetalleEntrada = { id ->
                    navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                },
                onVerDetalleDia = { fecha ->
                    navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                }
            )
        }

        // PANTALLAS PARA RESPONDER Y VER RESULTADOS DE CUESTIONARIOS
        composable(
            route = NavRoutes.ResponderCuestionario.route,
            arguments = listOf(navArgument("cuestionarioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cuestionarioId = backStackEntry.arguments?.getString("cuestionarioId") ?: ""
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val relacionId = remember(uid) {
                runBlocking { FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await().getString("relacionId") ?: "" }
            }
            ResponderScreen(
                viewModel = viewModel,
                cuestionarioId = cuestionarioId,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                relacionId = relacionId,
                onCompletado = { id ->
                    navController.navigate(NavRoutes.ResultadosCuestionario.crearRuta(id)) {
                        popUpTo(NavRoutes.ResponderCuestionario.crearRuta(id)) { inclusive = true }
                    }
                },
                onRegresar = irAlInicio
            )
        }

        composable(
            route = NavRoutes.ResultadosCuestionario.route,
            arguments = listOf(navArgument("cuestionarioId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cuestionarioId = backStackEntry.arguments?.getString("cuestionarioId") ?: ""
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val mainEntry = remember(backStackEntry) { try { navController.getBackStackEntry(NavRoutes.Main.route) } catch (e: Exception) { backStackEntry } }
            val perfilViewModel: PerfilViewModel = viewModel(viewModelStoreOwner = mainEntry, factory = factory)
            val perfilState by perfilViewModel.uiState.collectAsState()

            ResultadosScreen(
                viewModel = viewModel,
                cuestionarioId = cuestionarioId,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                nombreUsuario = perfilState.usuario?.nombre ?: "Tú",
                nombrePareja = perfilState.pareja?.nombre ?: "Tu pareja",
                fotoUsuario = perfilState.usuario?.fotoPerfil ?: "",
                fotoPareja = perfilState.pareja?.fotoPerfil ?: "",
                onRegresar = irAlInicio
            )
        }

        composable(NavRoutes.CrearCuestionario.route) {
            val viewModel: CuestionarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val relacionId = remember(uid) {
                runBlocking { FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await().getString("relacionId") ?: "" }
            }
            CrearCuestionarioScreen(
                viewModel = viewModel,
                usuarioId = uid,
                parejaId = parejaId ?: "",
                relacionId = relacionId,
                onCreado = irAlInicio,
                onRegresar = irAlInicio
            )
        }

        // PANTALLAS PARA LA GESTIÓN DE EVENTOS COMPARTIDOS
        composable(NavRoutes.Eventos.route) {
            val viewModel: EventoViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val relacionId = remember(uid) {
                runBlocking { FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await().getString("relacionId") ?: "" }
            }
            EventosScreen(
                viewModel = viewModel,
                relacionId = relacionId,
                onCrearEvento = { navController.navigate(NavRoutes.CrearEvento.route) },
                onVerEvento = { id -> navController.navigate(NavRoutes.DetalleEvento.crearRuta(id)) },
                onRegresar = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CrearEvento.route) {
            val viewModel: EventoViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val parejaId = remember(uid) { runBlocking { UsuarioHelper.obtenerParejaId(uid) } }
            val relacionId = remember(uid) {
                runBlocking { FirebaseFirestore.getInstance().collection("usuarios").document(uid).get().await().getString("relacionId") ?: "" }
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
                onEditar = { id -> navController.navigate(NavRoutes.EditarEvento.crearRuta(id)) },
                onConvertirEnRecuerdo = { fecha, titulo ->
                    navController.navigate(NavRoutes.CrearEntrada.crearRuta(fecha, TipoEntrada.RECUERDO.name))
                }
            )
        }

        // PANTALLAS PARA EL MURO DE NOTAS COMPARTIDAS
        composable(NavRoutes.Notas.route) {
            val viewModel: NotaViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            val perfilViewModel: PerfilViewModel = viewModel(factory = factory)
            val perfilState by perfilViewModel.uiState.collectAsState()
            
            LaunchedEffect(uid) {
                if (perfilState.usuario == null) {
                    val parejaId = UsuarioHelper.obtenerParejaId(uid)
                    perfilViewModel.cargarPerfil(uid, parejaId)
                }
            }

            val relacionId = perfilState.usuario?.relacionId ?: ""
            val nombreUsuario = perfilState.usuario?.nombre ?: "Alguien"
            val parejaId = perfilState.usuario?.parejaId

            if (relacionId.isNotBlank()) {
                NotasScreen(
                    viewModel = viewModel,
                    relacionId = relacionId,
                    usuarioId = uid,
                    nombreUsuario = nombreUsuario,
                    parejaId = parejaId,
                    onRegresar = irAlInicio
                )
            } else {
                LoadingTransition()
            }
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
        
        composable(NavRoutes.Estados.route) { PlaceholderScreen("Estadísticas") }

        composable(NavRoutes.WidgetsPreview.route) {
            val viewModel: WidgetsViewModel = viewModel(factory = factory)
            WidgetsPreviewScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.PruebaComponentes.route) {
            val diarioViewModel: DiarioViewModel = viewModel(factory = factory)
            val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "test_user"
            
            PantallaPruebaComponentes(
                viewModel = diarioViewModel,
                usuarioId = uid,
                alVolver = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.CargaTransicion.route) {
            LoadingTransition()
        }
    }
}
