package com.cadev.mocaapp.core.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * ESTE ARCHIVO CONTIENE UNA PANTALLA DE ESPERA O PRUEBA
 * 
 * Qué hace
 * Sirve para mostrar un mensaje temporal en el centro de la pantalla. Se usa cuando 
 * una parte de la aplicación todavía se está construyendo o no tiene contenido que mostrar.
 */
@Composable
fun PlaceholderScreen(nombre: String) {
    /**
     * Este bloque coloca el texto justo en la mitad de la pantalla del móvil
     */
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        /**
         * Aquí se dibuja el texto con el nombre que se le haya pasado a la función
         */
        Text(
            text = nombre,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
