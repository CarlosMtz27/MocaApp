package com.cadev.mocaapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import com.cadev.mocaapp.ui.theme.MocaAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MocaAppTheme {
                // aquí irán las pantallas
            }
        }
    }
}