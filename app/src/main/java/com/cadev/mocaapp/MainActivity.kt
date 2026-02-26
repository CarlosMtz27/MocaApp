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
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.ui.theme.MocaAppTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = MocaViewModelFactory()

        setContent {
            MocaAppTheme {
                val navController = rememberNavController()

                // Detecta cambios de sesión en tiempo real
                val auth = FirebaseAuth.getInstance()
                var usuarioActual by remember {
                    mutableStateOf(auth.currentUser)
                }

                // Listener que se activa cada vez que cambia la sesión
                DisposableEffect(Unit) {
                    val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                        usuarioActual = firebaseAuth.currentUser
                    }
                    auth.addAuthStateListener(listener)

                    // Se limpia cuando el Composable sale de la pantalla
                    onDispose {
                        auth.removeAuthStateListener(listener)
                    }
                }

                val destinoInicial = when {
                    usuarioActual == null -> NavRoutes.Login.route
                    else -> NavRoutes.Main.route
                }

                // Cuando cambia el usuario, navega al destino correcto
                LaunchedEffect(usuarioActual) {
                    if (usuarioActual == null) {
                        navController.navigate(NavRoutes.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }

                MocaNavGraph(
                    navController = navController,
                    factory = factory,
                    destinoInicial = destinoInicial
                )
            }
        }
    }
}