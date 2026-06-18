package com.cadev.mocaapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.work.*
import com.cadev.mocaapp.BuildConfig
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.utils.CloudinaryConfig
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

class MainActivity : ComponentActivity() {

    private var navControllerRef: NavHostController? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            triggerUbicacionWorker()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), 1001
                )
            }
        }
    }

    private fun triggerUbicacionWorker() {
        val request = OneTimeWorkRequestBuilder<UbicacionWorker>()
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(this).enqueue(request)
    }

    private fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

        val factory = MocaViewModelFactory()

        setContent {
            val darkTheme = ThemeManager.isDarkTheme
            MocaAppTheme(darkTheme = darkTheme) {
                val navController = rememberNavController()
                navControllerRef = navController

                val auth = FirebaseAuth.getInstance()
                var usuarioActual by remember { mutableStateOf(auth.currentUser) }
                var pendingDeepLink by remember {
                    mutableStateOf(intent?.getStringExtra("deepLink"))
                }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { fa ->
                        usuarioActual = fa.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose { auth.removeAuthStateListener(listener) }
                }

                // Redirigir a login si no hay sesión
                LaunchedEffect(usuarioActual) {
                    if (usuarioActual == null) {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                // Vincular OneSignal al usuario autenticado y guardar Player ID
                LaunchedEffect(usuarioActual) {
                    val uid = usuarioActual?.uid ?: return@LaunchedEffect
                    val repo = NotificacionRepository(
                        firestore = FirebaseFirestore.getInstance(),
                        oneSignalAppId = BuildConfig.ONESIGNAL_APP_ID,
                        oneSignalRestKey = BuildConfig.ONESIGNAL_REST_KEY
                    )

                    // Vincular dispositivo al usuario en OneSignal
                    OneSignal.login(uid)

                    // Disparar actualización del widget de días juntos
                    WorkManager.getInstance(this@MainActivity)
                        .enqueue(OneTimeWorkRequestBuilder<DiasJuntosWorker>().build())

                    val playerId = OneSignal.User.pushSubscription.id
                    if (!playerId.isNullOrBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            repo.guardarOneSignalPlayerId(uid, playerId)
                        }
                    }

                    // Guardar player ID cuando esté disponible
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

                // Navegar al deep link cuando el usuario esté autenticado
                LaunchedEffect(pendingDeepLink, usuarioActual) {
                    val link = pendingDeepLink ?: return@LaunchedEffect
                    if (usuarioActual == null) return@LaunchedEffect

                    kotlinx.coroutines.delay(400)

                    val ruta = when {
                        // Tabs del MainScreen
                        link == "main/chat"          -> "${NavRoutes.Main.route}?tab=chat"
                        link == "main/calendario"    -> "${NavRoutes.Main.route}?tab=calendario"
                        link == "main/cuestionarios" -> "${NavRoutes.Main.route}?tab=cuestionarios"
                        link == "main/perfil"        -> "${NavRoutes.Main.route}?tab=perfil"
                        link == "main/home"          -> "${NavRoutes.Main.route}?tab=home"
                        link == "main/notas"         -> NavRoutes.Notas.route
                        link.startsWith("main/")     -> NavRoutes.Main.route

                        // Pantallas directas
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

                    navController.navigate(ruta) { launchSingleTop = true }
                    pendingDeepLink = null
                }

                val destinoInicial = if (usuarioActual == null)
                    NavRoutes.Login.route
                else
                    NavRoutes.Main.route

                MocaNavGraph(
                    navController  = navController,
                    factory        = factory,
                    destinoInicial = destinoInicial
                )

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val deepLink = intent.getStringExtra("deepLink") ?: return
        navControllerRef?.navigate(deepLink) { launchSingleTop = true }
    }
}