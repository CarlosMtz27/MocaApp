package com.cadev.mocaapp

import android.Manifest
import android.util.Log
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.cadev.mocaapp.BuildConfig
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.utils.CloudinaryConfig
import com.cadev.mocaapp.feature.pareja.data.UsuarioHelper
import com.cadev.mocaapp.feature.notificaciones.data.NotificacionRepository
import com.cadev.mocaapp.feature.widgets.diasjuntos.DiasJuntosWorker
import com.cadev.mocaapp.feature.widgets.distancia.UbicacionWorker
import com.cadev.mocaapp.core.utils.ThemeManager
import com.cadev.mocaapp.ui.theme.MocaAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.onesignal.OneSignal
import com.onesignal.user.subscriptions.IPushSubscriptionObserver
import com.onesignal.user.subscriptions.PushSubscriptionChangedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ESTA ES LA ACTIVIDAD PRINCIPAL
 * 
 * Qué hace
 * Es el contenedor de toda la interfaz de la aplicación. Se encarga de gestionar el inicio de sesión, 
 * los permisos del teléfono como la ubicación y decide qué pantalla mostrar según si el usuario 
 * ha tocado una notificación.
 * 
 * Cómo añadir más cosas
 * Si necesitas pedir un nuevo permiso debes añadirlo en la variable locationPermissionRequest. 
 * Si quieres que al tocar una notificación la aplicación abra una pantalla nueva que acabas de crear 
 * debes añadir la lógica dentro del bloque que gestiona los enlaces profundos o deep links.
 */
class MainActivity : ComponentActivity() {

    // Variable para guardar el controlador de navegación y poder usarlo al recibir avisos externos
    private var navControllerRef: NavHostController? = null

    // Este bloque gestiona la respuesta del usuario cuando se le piden los permisos de ubicación
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            // Si el usuario acepta se activa la actualización de ubicación de inmediato
            triggerUbicacionWorker()
            // En versiones modernas de Android se pide permiso para rastrear en segundo plano
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1001
                )
            }
        }
    }

    /**
     * Esta función activa una actualización de ubicación única y rápida
     */
    private fun triggerUbicacionWorker() {
        val request = OneTimeWorkRequestBuilder<UbicacionWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(this).enqueue(request)
    }

    /**
     * Esta función comprueba si ya tenemos permiso para acceder a la ubicación del dispositivo
     */
    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Esta es la función que construye la aplicación cuando se abre
     */
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Se configuran las herramientas de subida de imágenes y la base de datos de Google
        try {
            CloudinaryConfig.inicializar(
                context   = this,
                cloudName = BuildConfig.CLOUDINARY_CLOUD_NAME,
                apiKey    = BuildConfig.CLOUDINARY_API_KEY,
                apiSecret = BuildConfig.CLOUDINARY_API_SECRET
            )
            FirebaseFirestore.getInstance().firestoreSettings =
                FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .setCacheSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
        } catch (e: Exception) { }

        // Se prepara el creador de modelos de datos para las pantallas
        val factory = MocaViewModelFactory(application)

        // Aquí empezamos a dibujar la interfaz visual
        setContent {
            // Se comprueba si el usuario prefiere el modo oscuro o claro
            val darkTheme = ThemeManager.isDarkTheme
            MocaAppTheme(darkTheme = darkTheme) {
                // Se crea el gestor de navegación entre pantallas
                val navController = rememberNavController()
                navControllerRef = navController

                val auth = FirebaseAuth.getInstance()
                var usuarioActual by remember { mutableStateOf(auth.currentUser) }
                var estaVinculado by remember { mutableStateOf<Boolean?>(null) }

                // VIGILANCIA DE VINCULACIÓN EN TIEMPO REAL
                DisposableEffect(usuarioActual) {
                    val uid = usuarioActual?.uid
                    if (uid == null) {
                        estaVinculado = null
                        onDispose {}
                    } else {
                        // Escuchamos cambios en nuestro perfil para saber si nuestra pareja nos vinculó
                        val listener = FirebaseFirestore.getInstance()
                            .collection("usuarios")
                            .document(uid)
                            .addSnapshotListener { snapshot, _ ->
                                val pId = snapshot?.getString("parejaId")
                                estaVinculado = !pId.isNullOrBlank()
                            }
                        
                        // Al cerrar sesión o destruir la actividad, quitamos el vigilante
                        onDispose { listener.remove() }
                    }
                }

                // NAVEGACIÓN AUTOMÁTICA AL VINCULAR
                LaunchedEffect(estaVinculado) {
                    if (estaVinculado == true && navController.runCatching { graph }.isSuccess) {
                        val currentRoute = navController.currentDestination?.route
                        if (currentRoute == NavRoutes.CodigoPareja.route) {
                            // Si estábamos esperando el código y de repente nos vinculan (desde el otro móvil), 
                            // vamos al inicio directamente.
                            navController.navigate(NavRoutes.Main.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }
                // Se comprueba si venimos de tocar una notificación
                var pendingDeepLink by remember {
                    mutableStateOf(intent?.getStringExtra("deepLink"))
                }

                // Se vigila si el usuario entra o sale de su cuenta para actualizar la aplicación
                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { fa ->
                        usuarioActual = fa.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                // Si no hay ningún usuario con sesión abierta se le envía directamente a la pantalla de entrar
                LaunchedEffect(usuarioActual) {
                    if (usuarioActual == null) {
                        // Solo navegamos si el grafo ya está configurado
                        if (navController.runCatching { graph }.isSuccess) {
                            navController.navigate(NavRoutes.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }
                }

                // Cuando el usuario entra con su cuenta se sincronizan las notificaciones push
                LaunchedEffect(usuarioActual) {
                    val uid = usuarioActual?.uid ?: return@LaunchedEffect
                    val repo = NotificacionRepository(
                        firestore = FirebaseFirestore.getInstance(),
                        oneSignalAppId = BuildConfig.ONESIGNAL_APP_ID,
                        oneSignalRestKey = BuildConfig.ONESIGNAL_REST_KEY
                    )

                    // Se identifica al usuario en el sistema de notificaciones
                    OneSignal.login(uid)

                    // Se actualiza el contador de días de la pareja
                    WorkManager.getInstance(this@MainActivity)
                        .enqueue(OneTimeWorkRequestBuilder<DiasJuntosWorker>().build())

                    // Intentamos guardar el PlayerID de forma persistente
                    launch {
                        repeat(5) { 
                            val playerId = OneSignal.User.pushSubscription.id
                            if (!playerId.isNullOrBlank()) {
                                Log.d("MOCA_PUSH", "Guardando PlayerID: $playerId")
                                repo.guardarOneSignalPlayerId(uid, playerId)
                                return@launch
                            }
                            delay(2000)
                        }
                    }

                    // Vigilante de cambios en la suscripción
                    val observer = object : IPushSubscriptionObserver {
                        override fun onPushSubscriptionChange(state: PushSubscriptionChangedState) {
                            val id = state.current.id ?: return
                            CoroutineScope(Dispatchers.IO).launch {
                                repo.guardarOneSignalPlayerId(uid, id)
                            }
                        }
                    }
                    OneSignal.User.pushSubscription.addObserver(observer)
                }

                // GESTIÓN DE LA NAVEGACIÓN DESDE LAS NOTIFICACIONES
                LaunchedEffect(pendingDeepLink, usuarioActual) {
                    val link = pendingDeepLink ?: return@LaunchedEffect
                    if (usuarioActual == null) return@LaunchedEffect

                    // Se espera un poco para que la aplicación esté lista antes de saltar de pantalla
                    kotlinx.coroutines.delay(400)

                    // Se decide a qué pantalla ir dependiendo del texto que traiga la notificación
                    val ruta = when {
                        link == "main/chat"          -> "${NavRoutes.Main.route}?tab=chat"
                        link == "main/calendario"    -> "${NavRoutes.Main.route}?tab=calendario"
                        link == "main/cuestionarios" -> "${NavRoutes.Main.route}?tab=cuestionarios"
                        link == "main/perfil"        -> "${NavRoutes.Main.route}?tab=perfil"
                        link == "main/home"          -> "${NavRoutes.Main.route}?tab=home"
                        link == "main/notas"         -> NavRoutes.Notas.route
                        link.startsWith("main/")     -> NavRoutes.Main.route

                        link.startsWith("resultados_cuestionario/") -> {
                            val id = link.removePrefix("resultados_cuestionario/")
                            NavRoutes.ResultadosCuestionario.crearRuta(id)
                        }
                        link.startsWith("responder_cuestionario/") -> {
                            val id = link.removePrefix("responder_cuestionario/")
                            NavRoutes.ResponderCuestionario.crearRuta(id)
                        }
                        else -> NavRoutes.Main.route
                    }

                    // Se realiza el salto a la pantalla correspondiente
                    if (navController.runCatching { graph }.isSuccess) {
                        navController.navigate(ruta) { launchSingleTop = true }
                        pendingDeepLink = null
                    }
                }

                // Se decide cuál es la primera pantalla que verá el usuario
                var destinoInicial by remember { mutableStateOf<String?>(null) }
                
                LaunchedEffect(usuarioActual, estaVinculado) {
                    if (usuarioActual == null) {
                        destinoInicial = NavRoutes.Login.route
                    } else if (estaVinculado != null) {
                        destinoInicial = if (estaVinculado == false) {
                            NavRoutes.CodigoPareja.route
                        } else {
                            NavRoutes.Main.route
                        }
                    }
                }

                if (destinoInicial != null) {
                    // Se carga el mapa de navegación con todas las pantallas disponibles
                    MocaNavGraph(
                        navController  = navController,
                        factory        = factory,
                        destinoInicial = destinoInicial!!
                    )
                } else {
                    // Pantalla de carga mientras se decide el destino inicial
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 3.dp
                        )
                    }
                }

                // Al abrir la aplicación se comprueban los permisos de ubicación
                LaunchedEffect(Unit) {
                    if (!hasLocationPermission()) {
                        locationPermissionRequest.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        triggerUbicacionWorker()
                    }
                }
            }
        }
    }

    /**
     * Esta función se activa si la aplicación ya estaba abierta y llega un aviso nuevo de navegación
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLink = intent.getStringExtra("deepLink") ?: return
        
        navControllerRef?.let { nav ->
            if (nav.runCatching { graph }.isSuccess) {
                nav.navigate(deepLink) { launchSingleTop = true }
            }
        }
    }
}
