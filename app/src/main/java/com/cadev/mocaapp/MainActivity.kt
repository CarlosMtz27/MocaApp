package com.cadev.mocaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.cadev.mocaapp.BuildConfig
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.utils.CloudinaryConfig
import com.cadev.mocaapp.ui.theme.MocaAppTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : ComponentActivity() {
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
            MocaAppTheme {
                val navController = rememberNavController()
                val auth = FirebaseAuth.getInstance()

                var usuarioActual by remember {
                    mutableStateOf(auth.currentUser)
                }

                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        usuarioActual = firebaseAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                LaunchedEffect(usuarioActual) {
                    if (usuarioActual == null) {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                val destinoInicial = if (usuarioActual == null)
                    NavRoutes.Login.route
                else
                    NavRoutes.Main.route

                MocaNavGraph(
                    navController = navController,
                    factory = factory,
                    destinoInicial = destinoInicial
                )
            }
        }
    }
}