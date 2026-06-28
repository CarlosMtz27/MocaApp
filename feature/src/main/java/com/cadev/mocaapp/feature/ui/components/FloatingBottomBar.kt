package com.cadev.mocaapp.feature.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import com.cadev.mocaapp.core.ui.BottomNavItem
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.feature.notificaciones.data.ContadoresBadge

@Composable
fun FloatingBottomBar(
    destinoActual: NavDestination?,
    contadores: ContadoresBadge,
    onNavigate: (String) -> Unit
) {
    val tabs = listOf(
        BottomNavItem.Home,
        BottomNavItem.Calendario,
        BottomNavItem.Chat,
        BottomNavItem.Cuestionarios,
        BottomNavItem.Perfil
    )

    Surface(
        modifier = Modifier
            .padding(horizontal = 28.dp, vertical = 24.dp)
            .clip(RoundedCornerShape(32.dp)),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        tonalElevation = 6.dp,
        shadowElevation = 12.dp
    ) {
        NavigationBar(
            containerColor = Color.Transparent,
            modifier = Modifier.height(72.dp),
            windowInsets = WindowInsets(0, 0, 0, 0)
        ) {
            tabs.forEach { tab ->
                val seleccionado = destinoActual?.hierarchy?.any { it.route == tab.route } == true

                val iconSize by animateDpAsState(
                    targetValue = if (seleccionado) 28.dp else 24.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "iconSize"
                )

                val badgeCount = when (tab.route) {
                    NavRoutes.Chat.route -> contadores.chat
                    NavRoutes.Cuestionarios.route -> contadores.cuestionarios
                    else -> 0
                }

                NavigationBarItem(
                    selected = seleccionado,
                    onClick = { onNavigate(tab.route) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (badgeCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary,
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
                                tint = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.6f)
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (seleccionado) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.8f)
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                )
            }
        }
    }
}
