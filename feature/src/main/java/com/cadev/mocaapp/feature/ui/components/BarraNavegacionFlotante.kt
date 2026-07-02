package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * DATOS DE ITEM DE NAVEGACIÓN
 * Representa la información necesaria para cada botón de la barra.
 */
data class ItemNavegacionData(
    val titulo: String,
    val icono: ImageVector,
    val mostrarNotificacion: Boolean = false
)

/**
 * BARRA DE NAVEGACIÓN FLOTANTE (ESTILO GLASSMORPHISM)
 * Adaptación fiel del diseño HTML a Jetpack Compose.
 */
@Composable
fun BarraNavegacionFlotante(
    indiceSeleccionado: Int,
    alSeleccionarItem: (Int) -> Unit,
    items: List<ItemNavegacionData>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Contenedor principal con efecto de vidrio (glassmorphism)
        Row(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { indice, item ->
                ItemNavegacion(
                    item = item,
                    estaActivo = indice == indiceSeleccionado,
                    alHacerClick = { alSeleccionarItem(indice) }
                )
            }
        }
    }
}

/**
 * COMPONENTE INDIVIDUAL DE LA BARRA
 */
@Composable
private fun ItemNavegacion(
    item: ItemNavegacionData,
    estaActivo: Boolean,
    alHacerClick: () -> Unit
) {
    val colorEsquema = MaterialTheme.colorScheme
    
    // Animaciones de color y escala para feedback visual
    val colorIcono by animateColorAsState(
        targetValue = if (estaActivo) colorEsquema.primary else colorEsquema.outline,
        label = "colorIcono"
    )
    
    val escalaIcono by animateFloatAsState(
        targetValue = if (estaActivo) 1.1f else 1.0f,
        label = "escalaIcono"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = alHacerClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        // Fondo suave rosa para el ítem seleccionado (similar al ::before de CSS)
        if (estaActivo) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(colorEsquema.primaryContainer.copy(alpha = 0.35f), CircleShape)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box {
                Icon(
                    imageVector = item.icono,
                    contentDescription = item.titulo,
                    tint = colorIcono,
                    modifier = Modifier
                        .size(24.dp)
                        .graphicsLayer {
                            scaleX = escalaIcono
                            scaleY = escalaIcono
                        }
                )
                
                // Punto de notificación (Badge)
                if (item.mostrarNotificacion) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 2.dp, y = (-2).dp)
                            .size(9.dp)
                            .background(MaterialTheme.colorScheme.error, CircleShape)
                            .border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape)
                    )
                }
            }
            
            Text(
                text = item.titulo,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = colorIcono
            )
        }
    }
}

/**
 * PREVIA DEL COMPONENTE
 */
@Composable
fun PreviaBarraNavegacion() {
    val items = listOf(
        ItemNavegacionData("Inicio", Icons.Default.Home),
        ItemNavegacionData("Calendario", Icons.Default.CalendarToday),
        ItemNavegacionData("Chat", Icons.Default.ChatBubble, mostrarNotificacion = true),
        ItemNavegacionData("Tests", Icons.Default.Quiz, mostrarNotificacion = true),
        ItemNavegacionData("Perfil", Icons.Default.Person)
    )
    
    BarraNavegacionFlotante(
        indiceSeleccionado = 0,
        alSeleccionarItem = {},
        items = items
    )
}
