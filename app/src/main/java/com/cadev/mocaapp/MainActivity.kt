package com.cadev.mocaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.cadev.mocaapp.ui.theme.MocaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Factory creado una sola vez en el punto de entrada
        val factory = MocaViewModelFactory()

        setContent {
            MocaAppTheme {
                val navController = rememberNavController()
                MocaNavGraph(
                    navController = navController,
                    factory = factory
                )
            }
        }
    }
}