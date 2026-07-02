package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.core.ui.meshGradientBackground
import com.cadev.mocaapp.feature.cuestionarios.domain.model.CategoriaCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.ui.components.TarjetaCuestionarioMejorada
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.theme.*

import androidx.compose.foundation.isSystemInDarkTheme
// ... (keep other imports)

@Composable
fun CuestionariosScreen(
    viewModel: CuestionarioViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    nombrePareja: String = "Pareja",
    onRegresar: () -> Unit,
    onIniciarCuestionario: (String) -> Unit,
    onVerResultados: (String) -> Unit,
    onCrearCuestionario: () -> Unit,
    onConfiguracion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorSurfaceContainer = if (isDark) Color(0xFF333028) else MocaSurfaceContainerLow

    LaunchedEffect(relacionId, usuarioId, parejaId) {
        viewModel.iniciarEscucha(relacionId, usuarioId, parejaId)
        viewModel.poblarPredefinidos()
    }

    LaunchedEffect(selectedTab) {
        if (selectedTab == 0) {
            viewModel.refrescarEstados(usuarioId, parejaId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .meshGradientBackground()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            
            // Tab Row Estilo Imagen
            CustomTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                isDark = isDark,
                containerColor = colorSurfaceContainer,
                primaryColor = colorPrimary
            )

            if (uiState.cargando) {
                LoadingTransition()
            } else {
                val listaAMostrar = if (selectedTab == 0) {
                    uiState.cuestionarios.filter { uiState.estadosCuestionarios[it.id] != EstadoCuestionario.AMBOS }
                } else {
                    uiState.cuestionarios.filter { uiState.estadosCuestionarios[it.id] == EstadoCuestionario.AMBOS }
                }

                if (listaAMostrar.isEmpty()) {
                    EmptyState(isHistory = selectedTab == 1, colorPrimary = colorPrimary)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (selectedTab == 0) {
                            val porCategoria = listaAMostrar.groupBy { it.categoria }
                            porCategoria.forEach { (categoria, lista) ->
                                item {
                                    val cat = try { CategoriaCuestionario.valueOf(categoria) } catch (e: Exception) { CategoriaCuestionario.PERSONALIZADO }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = cat.icono,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = colorPrimary
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text(
                                            text = cat.etiqueta,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = colorPrimary
                                        )
                                    }
                                }
                                itemsIndexed(lista) { _, cuestionario ->
                                    val estado = uiState.estadosCuestionarios[cuestionario.id] ?: EstadoCuestionario.NINGUNO
                                    TarjetaCuestionarioMejorada(
                                        cuestionario = cuestionario,
                                        estado = estado,
                                        onClick = {
                                            if (estado == EstadoCuestionario.AMBOS) {
                                                onVerResultados(cuestionario.id)
                                            } else {
                                                onIniciarCuestionario(cuestionario.id)
                                            }
                                        }
                                    )
                                }
                            }
                        } else {
                            itemsIndexed(listaAMostrar) { _, cuestionario ->
                                TarjetaCuestionarioMejorada(
                                    cuestionario = cuestionario,
                                    estado = EstadoCuestionario.AMBOS,
                                    onClick = { onVerResultados(cuestionario.id) }
                                )
                            }
                        }
                        
                        item { Spacer(Modifier.height(100.dp)) }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCrearCuestionario,
            containerColor = if (isDark) Color(0xFF5E3E47) else MocaPrimaryContainer,
            contentColor = if (isDark) Color.White else MocaOnPrimaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 24.dp, end = 24.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Crear Cuestionario", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
private fun CustomTabRow(
    selectedTabIndex: Int, 
    onTabSelected: (Int) -> Unit, 
    isDark: Boolean,
    containerColor: Color,
    primaryColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(containerColor.copy(alpha = if (isDark) 0.8f else 0.5f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabItem(
            modifier = Modifier.weight(1f),
            selected = selectedTabIndex == 0,
            text = "Disponibles",
            icon = Icons.Default.Favorite,
            isDark = isDark,
            primaryColor = primaryColor,
            onClick = { onTabSelected(0) }
        )
        TabItem(
            modifier = Modifier.weight(1f),
            selected = selectedTabIndex == 1,
            text = "Completados",
            icon = Icons.Default.History,
            isDark = isDark,
            primaryColor = primaryColor,
            onClick = { onTabSelected(1) }
        )
    }
}

@Composable
private fun TabItem(
    modifier: Modifier, 
    selected: Boolean, 
    text: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isDark: Boolean,
    primaryColor: Color,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        if (selected) (if (isDark) Color(0xFF4B472B) else MocaSurface) else Color.Transparent, 
        label = "bg"
    )
    val contentColor by animateColorAsState(
        if (selected) primaryColor else (if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant), 
        label = "content"
    )

    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = backgroundColor,
        shadowElevation = if (selected && !isDark) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(18.dp), tint = contentColor)
            Spacer(Modifier.width(8.dp))
            Text(text, fontSize = 14.sp, fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold, color = contentColor)
        }
    }
}

@Composable
fun EmptyState(isHistory: Boolean, colorPrimary: Color) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isHistory) Icons.Default.History else Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MocaOutline.copy(alpha = 0.2f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = if (isHistory) "Aún no hay historia" else "¡Todo al día!",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = colorPrimary
        )
        Text(
            text = if (isHistory) "Completen su primer cuestionario juntos." else "Pronto tendremos más retos para ustedes.",
            style = MaterialTheme.typography.bodyMedium,
            color = if (colorPrimary == MocaPrimary) MocaOnSurfaceVariant else Color(0xFFD3C3C5),
            textAlign = TextAlign.Center
        )
    }
}
