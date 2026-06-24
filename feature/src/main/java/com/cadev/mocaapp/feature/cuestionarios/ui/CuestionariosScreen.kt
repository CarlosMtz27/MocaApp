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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.cuestionarios.domain.model.CategoriaCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import kotlinx.coroutines.delay

/**
 * ESTA ES LA PANTALLA DE SELECCIÓN DE TESTS
 * 
 * Qué hace:
 * Muestra todos los cuestionarios y dinámicas de pareja disponibles. Se divide en 
 * dos pestañas: los tests que podemos responder ahora y el historial de los que 
 * ya terminamos para ver qué tal nos fue.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los tests se vean más grandes o con colores distintos por 
 * categoría, debemos ir a la función `TarjetaCuestionarioMejorada` y ajustar el diseño.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuestionariosScreen(
    viewModel: CuestionarioViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onRegresar: () -> Unit,
    onIniciarCuestionario: (String) -> Unit,
    onVerResultados: (String) -> Unit,
    onCrearCuestionario: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabSeleccionado by remember { mutableIntStateOf(0) }

    /**
     * Se cargan los cuestionarios disponibles y se asegura de que existan los predefinidos
     */
    LaunchedEffect(relacionId, usuarioId, parejaId) {
        viewModel.iniciarEscucha(relacionId, usuarioId, parejaId)
        viewModel.poblarPredefinidos()
    }

    /**
     * Se refresca el estado cada vez que el usuario vuelve a la pestaña de disponibles
     */
    LaunchedEffect(tabSeleccionado) {
        if (tabSeleccionado == 0) {
            viewModel.refrescarEstados(usuarioId, parejaId)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("¿Cuánto se conocen?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                        Text("Tests para parejas", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar al inicio")
                    }
                },
                actions = {
                    /**
                     * Botón para abrir la pantalla de creación de un test propio
                     */
                    IconButton(
                        onClick = onCrearCuestionario,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Add, "Crear", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                    )
                )
        ) {
            /**
             * Barra de pestañas para cambiar entre disponibles y completados
             */
            CustomTabRow(
                selectedTabIndex = tabSeleccionado,
                onTabSelected = { tabSeleccionado = it }
            )

            if (uiState.cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(strokeCap = androidx.compose.ui.graphics.StrokeCap.Round)
                }
            } else {
                /**
                 * Contenido animado que cambia según la pestaña seleccionada
                 */
                AnimatedContent(
                    targetState = tabSeleccionado,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                        } else {
                            (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                        }
                    },
                    label = "TabContent"
                ) { targetTab ->
                    val listaAMostrar = if (targetTab == 0) uiState.cuestionarios else uiState.historial
                    
                    if (listaAMostrar.isEmpty()) {
                        EmptyCuestionariosState(isHistory = targetTab == 1)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp, 8.dp, 16.dp, 100.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (targetTab == 0) {
                                val porCategoria = listaAMostrar.groupBy { it.categoria }
                                porCategoria.forEach { (categoria, lista) ->
                                    item {
                                        val cat = try { CategoriaCuestionario.valueOf(categoria) } catch (e: Exception) { CategoriaCuestionario.PERSONALIZADO }
                                        /**
                                         * Título de la categoría con su icono correspondiente
                                         */
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 4.dp, top = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = cat.icono,
                                                contentDescription = null,
                                                modifier = Modifier.size(20.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = cat.etiqueta,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    itemsIndexed(lista) { index, cuestionario ->
                                        val estado = uiState.estadosCuestionarios[cuestionario.id] ?: EstadoCuestionario.NINGUNO
                                        AnimatedTarjetaCuestionario(
                                            index = index,
                                            cuestionario = cuestionario,
                                            estado = estado,
                                            onClick = {
                                                /**
                                                 * Si el test ya ha sido respondido por ambos se ven los resultados
                                                 */
                                                if (estado == EstadoCuestionario.AMBOS || estado == EstadoCuestionario.YO_RESPONDÍ) {
                                                    onVerResultados(cuestionario.id)
                                                } else {
                                                    onIniciarCuestionario(cuestionario.id)
                                                }
                                            }
                                        )
                                    }
                                }
                            } else {
                                /**
                                 * Listado simple de todos los tests ya finalizados
                                 */
                                itemsIndexed(listaAMostrar) { index, cuestionario ->
                                    AnimatedTarjetaCuestionario(
                                        index = index,
                                        cuestionario = cuestionario,
                                        estado = EstadoCuestionario.AMBOS,
                                        onClick = { onVerResultados(cuestionario.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Función que crea la barra de pestañas personalizada con iconos y nombres
 */
@Composable
private fun CustomTabRow(selectedTabIndex: Int, onTabSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TabItem(
            modifier = Modifier.weight(1f),
            selected = selectedTabIndex == 0,
            text = "Disponibles",
            icon = Icons.Default.Favorite,
            onClick = { onTabSelected(0) }
        )
        TabItem(
            modifier = Modifier.weight(1f),
            selected = selectedTabIndex == 1,
            text = "Completados",
            icon = Icons.Default.History,
            onClick = { onTabSelected(1) }
        )
    }
}

/**
 * Función para dibujar cada una de las opciones de la barra de pestañas
 */
@Composable
private fun TabItem(modifier: Modifier, selected: Boolean, text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    val backgroundColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.surface else Color.Transparent, label = "bg")
    val contentColor by animateColorAsState(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, label = "content")

    Surface(
        modifier = modifier.fillMaxHeight().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        shadowElevation = if (selected) 2.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, modifier = Modifier.size(16.dp), tint = contentColor)
            Spacer(Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium, color = contentColor)
        }
    }
}

/**
 * Encapsula la tarjeta del cuestionario con una pequeña animación de entrada
 */
@Composable
private fun AnimatedTarjetaCuestionario(index: Int, cuestionario: Cuestionario, estado: EstadoCuestionario, onClick: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(index * 100L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
    ) {
        TarjetaCuestionarioMejorada(cuestionario, estado, onClick)
    }
}

/**
 * Función que crea la tarjeta visual para cada test mostrando su progreso y estado
 */
@Composable
private fun TarjetaCuestionarioMejorada(cuestionario: Cuestionario, estado: EstadoCuestionario, onClick: () -> Unit) {
    val categoria = try { CategoriaCuestionario.valueOf(cuestionario.categoria) } catch (e: Exception) { CategoriaCuestionario.PERSONALIZADO }
    
    val colorBase = when (estado) {
        EstadoCuestionario.YO_RESPONDÍ -> Color(0xFFFF9800)
        EstadoCuestionario.PAREJA_RESPONDIÓ -> Color(0xFF4CAF50)
        EstadoCuestionario.AMBOS -> MaterialTheme.colorScheme.primary
        EstadoCuestionario.NINGUNO -> MaterialTheme.colorScheme.secondary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = categoria.icono,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(cuestionario.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
                    Text(cuestionario.descripcion, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 2)
                    
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "${cuestionario.preguntas.size} preguntas",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onClick,
                    modifier = Modifier.background(colorBase.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(
                        if (estado == EstadoCuestionario.AMBOS) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                        null,
                        tint = colorBase
                    )
                }
            }

            /**
             * Muestra una etiqueta de estado si hay un proceso pendiente o finalizado
             */
            if (estado != EstadoCuestionario.NINGUNO) {
                val textoEstado = when (estado) {
                    EstadoCuestionario.YO_RESPONDÍ -> "⏳ Esperando a tu pareja..."
                    EstadoCuestionario.PAREJA_RESPONDIÓ -> "✨ ¡Tu pareja ya respondió! Te toca"
                    EstadoCuestionario.AMBOS -> "✅ ¡Resultados listos para ver!"
                    else -> ""
                }
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = colorBase.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = textoEstado,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = colorBase
                    )
                }
            }
        }
    }
}

/**
 * Función que muestra un mensaje informativo cuando no hay ningún test en la lista
 */
@Composable
private fun EmptyCuestionariosState(isHistory: Boolean) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isHistory) Icons.Default.History else Icons.Default.Favorite,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (isHistory) "Aún no hay trofeos" else "¡Todo listo!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Black
        )
        Text(
            if (isHistory) "Completen su primer test juntos para ver sus resultados aquí." else "No hay tests nuevos por ahora. ¡Vuelve pronto!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}
