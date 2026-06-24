package com.cadev.mocaapp.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Spring
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.perfil.ui.AjustesScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilParejaScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilScreen
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.runBlocking

/**
 * ESTA ES LA PANTALLA CONTENEDORA PRINCIPAL
 * 
 * Qué hace:
 * Se encarga de mostrar nuestro menú de navegación inferior y de intercambiar 
 * las pantallas principales de la aplicación: Inicio, Diario, Chat, Tests y Perfil. 
 * También gestionamos aquí la carga inicial de toda nuestra información.
 * 
 * Cómo lo podemos modificar:
 * Si queremos añadir una nueva pestaña al menú inferior, debemos añadir un 
 * nuevo `BottomNavItem` en la lista `tabs`.
 */
@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MainScreen(
    factory: ViewModelProvider.Factory,
    navController: NavHostController,
    initialTab: String = ""
) {
    /**
     * NAVEGACIÓN INTERNA:
     * Creamos un controlador de navegación interno para manejar las pestañas 
     * de nuestro menú inferior de forma independiente.
     */
    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val destinoActual = navBackStackEntry?.destination
    val rutaActual = destinoActual?.route
    val context = LocalContext.current

    /**
     * IDENTIFICACIÓN:
     * Identificamos al usuario conectado y buscamos quién es su pareja 
     * para cargar toda nuestra información compartida.
     */
    val uid = remember {
        FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }

    /**
     * GESTORES DE DATOS:
     * Inicializamos todos los ViewModel que nos ayudarán a gestionar 
     * la información en cada una de nuestras pestañas.
     */
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

    /**
     * Función rápida para volver siempre a la pestaña de inicio
     */
    val irAlInicio = {
        tabNavController.navigate(NavRoutes.Home.route) {
            popUpTo(tabNavController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    /**
     * NOTIFICACIONES:
     * Activamos nuestro sistema de avisos en cuanto la aplicación está lista 
     * para recibir mensajes y alertas push.
     */
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            notificacionViewModel.iniciar(uid)
        }
    }

    /**
     * PERFIL EN TIEMPO REAL:
     * Activamos el vigilante de nuestro perfil. Esto nos permite detectar al 
     * instante si nuestra pareja nos vincula o si cambia algo en su perfil.
     */
    LaunchedEffect(uid) {
        if (uid.isNotBlank()) {
            perfilViewModel.iniciarEscucha(uid)
        }
    }

    val usuario = perfilState.usuario
    val parejaIdActual = usuario?.parejaId
    val relacionIdActual = usuario?.relacionId

    // Pantalla de carga mientras se recupera el perfil del usuario
    if (usuario == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
        return
    }

    /**
     * CONTENIDO COMPARTIDO EN TIEMPO REAL:
     * Cargamos nuestras citas próximas, recuerdos del diario y notas 
     * rápidas. Activamos la escucha activa para que cualquier cambio se 
     * refleje al instante en todas las pantallas sin refrescar.
     */
    LaunchedEffect(uid, parejaIdActual, relacionIdActual) {
        if (!relacionIdActual.isNullOrBlank()) {
            eventoViewModel.iniciarEscucha(relacionIdActual)
            diarioViewModel.iniciarEscucha(uid, parejaIdActual, relacionIdActual)
            notaViewModel.iniciar(context, relacionIdActual, uid, parejaIdActual)
        }
    }

    /**
     * CHAT EN VIVO:
     * Preparamos nuestro chat privado para que podamos enviarnos mensajes, 
     * fotos y audios al instante.
     */
    LaunchedEffect(uid, parejaIdActual) {
        if (uid.isNotBlank() && !parejaIdActual.isNullOrBlank()) {
            chatViewModel.inicializar(uid, parejaIdActual)
        }
    }

    /**
     * TESTS DE PAREJA EN TIEMPO REAL:
     * Activamos la escucha de retos y tests para detectar si la pareja 
     * ha respondido alguno al instante.
     */
    LaunchedEffect(relacionIdActual, uid, parejaIdActual) {
        if (!relacionIdActual.isNullOrBlank()) {
            cuestionarioViewModel.iniciarEscucha(
                relacionId = relacionIdActual,
                usuarioId = uid,
                parejaId = parejaIdActual ?: ""
            )
            cuestionarioViewModel.poblarPredefinidos()
        }
    }

    /**
     * LIMPIEZA DE AVISOS:
     * Cada vez que entramos en una sección, limpiamos los avisos de mensajes 
     * nuevos de esa parte específica.
     */
    LaunchedEffect(rutaActual) {
        if (uid.isBlank()) return@LaunchedEffect
        when (rutaActual) {
            NavRoutes.Chat.route -> notificacionViewModel.limpiarChat(uid)
            NavRoutes.Calendario.route -> notificacionViewModel.limpiarDiario(uid)
            NavRoutes.Cuestionarios.route -> notificacionViewModel.limpiarCuestionarios(uid)
        }
    }

    /**
     * ATAJOS DE INICIO:
     * Si abrimos la app desde una notificación o un widget, nos encargamos 
     * de llevarnos directamente a la sección correcta.
     */
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

    /**
     * MENÚ INFERIOR:
     * Estas son las cinco pestañas principales que tenemos disponibles 
     * en nuestra barra de navegación.
     */
    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendario,
        BottomNavItem.Chat,
        BottomNavItem.Cuestionarios,
        BottomNavItem.Perfil
    )

    Scaffold(
        bottomBar = {
            /**
             * BARRA DE NAVEGACIÓN:
             * Diseñamos una barra inferior flotante y moderna que nos permite 
             * movernos cómodamente por toda la app.
             */
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .clip(RoundedCornerShape(32.dp)),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(72.dp),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                ) {
                    tabs.forEach { tab ->
                        val seleccionado = destinoActual
                            ?.hierarchy
                            ?.any { it.route == tab.route } == true

                        /**
                         * ANIMACIÓN:
                         * Añadimos una pequeña animación para que el icono crezca 
                         * suavemente cuando lo seleccionamos.
                         */
                        val iconSize by animateDpAsState(
                            targetValue = if (seleccionado) 28.dp else 24.dp,
                            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                            label = "iconSize"
                        )

                        /**
                         * CONTADOR DE NOVEDADES:
                         * Calculamos si tenemos mensajes nuevos o retos de pareja 
                         * pendientes por responder.
                         */
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
                                    popUpTo(tabNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                BadgedBox(
                                    badge = {
                                        /**
                                         * AVISO ROJO:
                                         * Si tenemos novedades, mostramos un círculo rojo 
                                         * con el número para no olvidarnos de nada.
                                         */
                                        if (badgeCount > 0) {
                                            Badge(
                                                containerColor = MaterialTheme.colorScheme.error,
                                                contentColor = MaterialTheme.colorScheme.onError,
                                                modifier = Modifier.offset(x = (-4).dp, y = 4.dp)
                                            ) {
                                                Text(if (badgeCount > 9) "9+" else badgeCount.toString(), fontSize = 10.sp)
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (seleccionado) tab.iconoSeleccionado else tab.iconoNoSeleccionado,
                                        contentDescription = tab.etiqueta,
                                        modifier = Modifier.size(iconSize),
                                        tint = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = tab.etiqueta,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (seleccionado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        )
                    }
                }
            }
        }
    ) { paddingValues ->

        /**
         * CONTENEDOR DE PANTALLAS:
         * Este es el lugar donde van cambiando nuestras pantallas (Inicio, 
         * Diario, Chat, etc.) según lo que toquemos en el menú.
         */
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
                CalendarioScreen(
                    viewModel = diarioViewModel,
                    usuarioId = uid,
                    parejaId = parejaIdActual,
                    relacionId = relacionIdActual ?: "",
                    onRegresar = irAlInicio,
                    onDiaSeleccionado = { fecha ->
                        navController.navigate(NavRoutes.DetalleDia.crearRuta(fecha))
                    },
                    onVerEventos = {
                        navController.navigate(NavRoutes.Eventos.route)
                    },
                    onVerDetalleEntrada = { id ->
                        navController.navigate(NavRoutes.DetalleEntrada.crearRuta(id))
                    },
                    onVerDetalleEvento = { id ->
                        navController.navigate(NavRoutes.DetalleEvento.crearRuta(id))
                    }
                )
            }

            composable(NavRoutes.Chat.route) {
                if (!parejaIdActual.isNullOrBlank()) {
                    ChatScreen(
                        viewModel = chatViewModel,
                        usuarioId = uid,
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
                    onRegresar = irAlInicio,
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
                    }
                )
            }

            composable(NavRoutes.Ajustes.route) {
                AjustesScreen(
                    viewModel = perfilViewModel,
                    usuarioId = uid,
                    parejaId = parejaIdActual,
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
