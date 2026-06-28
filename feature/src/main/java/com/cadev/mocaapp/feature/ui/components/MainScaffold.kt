package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*
import com.cadev.mocaapp.feature.ui.utils.FondoMeshMoca

/**
 * CONTENEDOR PRINCIPAL CON TRANSICIONES
 * Fiel a la estructura del HTML, integrando Header y Barra Flotante.
 */
@Composable
fun MainScaffold(
    tituloHeader: String,
    alVolver: () -> Unit,
    contenido: @Composable (PaddingValues) -> Unit
) {
    var indiceSeleccionado by remember { mutableStateOf(0) }
    
    val itemsNavegacion = listOf(
        ItemNavegacionData("Inicio", Icons.Default.Home),
        ItemNavegacionData("Calendario", Icons.Default.CalendarToday),
        ItemNavegacionData("Chat", Icons.Default.ChatBubble, mostrarNotificacion = true),
        ItemNavegacionData("Tests", Icons.Default.Quiz, mostrarNotificacion = true),
        ItemNavegacionData("Perfil", Icons.Default.Person)
    )

    FondoMeshMoca {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Como MainScaffold es genérico, le pasamos valores por defecto o vacíos
                // Pero lo ideal es que MainScreen maneje esto directamente
                MocaHeader(
                    titulo = tituloHeader,
                    nombreUsuario = "",
                    nombrePareja = "",
                    urlAvatarUsuario = "",
                    urlAvatarPareja = "",
                    esModoOscuro = false,
                    alHacerClickEnTema = { }
                )
            },
            bottomBar = {
                BarraNavegacionFlotante(
                    indiceSeleccionado = indiceSeleccionado,
                    alSeleccionarItem = { indiceSeleccionado = it },
                    items = itemsNavegacion
                )
            }
        ) { rellenosPadding ->
            // Transición suave al cambiar de contenido/pestaña
            AnimatedContent(
                targetState = indiceSeleccionado,
                transitionSpec = {
                    fadeIn(animationSpec = tween(400)) togetherWith fadeOut(animationSpec = tween(400))
                },
                label = "transicionContenido"
            ) { targetIndice ->
                Box(modifier = Modifier.fillMaxSize()) {
                    // Aquí podrías filtrar el contenido por targetIndice
                    contenido(rellenosPadding)
                }
            }
        }
    }
}

/**
 * VISTA DE DEMOSTRACIÓN (EQUIVALENTE AL CONTENIDO DEL HTML)
 */
@Composable
fun ContenidoDemoMoca(paddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(40.dp)
    ) {
        // Hero Section
        GlassCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "MORNING RITUAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MocaOnSurfaceVariant,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Inner Peace",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MocaOnSurface
                )
                Text(
                    text = "Start your journey with a calming breath exercise. Designed to reduce digital fatigue and restore mental clarity.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MocaOnSurfaceVariant
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {},
                        colors = ButtonDefaults.buttonColors(containerColor = MocaPrimaryContainer),
                        shape = CirculoCompleto
                    ) {
                        Text("Begin Session", color = MocaOnPrimaryContainer)
                    }
                }
            }
        }

        // Bento Grid Stats (Simplificado para el demo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                // ... Contenido stat 1
            }
            GlassCard(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                // ... Contenido stat 2
            }
        }
    }
}
