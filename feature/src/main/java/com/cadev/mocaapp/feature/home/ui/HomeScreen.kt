package com.cadev.mocaapp.feature.home.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.core.utils.ThemeManager
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionarioViewModel
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.eventos.ui.EventoViewModel
import com.cadev.mocaapp.feature.estadoanimo.domain.model.MAPA_MOODS
import com.cadev.mocaapp.feature.notas.ui.NotaViewModel
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.feature.pareja.ui.ParejaViewModel
import com.cadev.mocaapp.feature.pareja.ui.ParejaUiState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import com.cadev.mocaapp.feature.estadoanimo.ui.EstadoAnimoViewModel
import com.cadev.mocaapp.feature.estadoanimo.ui.EstadoAnimoScreen
import com.cadev.mocaapp.core.model.TipoEvento
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    perfilViewModel: PerfilViewModel,
    eventoViewModel: EventoViewModel,
    diarioViewModel: DiarioViewModel,
    cuestionarioViewModel: CuestionarioViewModel,
    notaViewModel: NotaViewModel,
    estadoAnimoViewModel: EstadoAnimoViewModel,
    parejaViewModel: ParejaViewModel,
    onNavigateToTab: (String) -> Unit,
    onNavigateToScreen: (String) -> Unit,
    onIrAVincular: () -> Unit,
    onVinculado: (String) -> Unit
) {
    val perfilState by perfilViewModel.uiState.collectAsState()
    val diarioState by diarioViewModel.uiState.collectAsState()
    val cuestionarioState by cuestionarioViewModel.uiState.collectAsState()
    val notaState by notaViewModel.uiState.collectAsState()
    val estadoAnimoState by estadoAnimoViewModel.uiState.collectAsState()
    val parejaState by parejaViewModel.uiState.collectAsState()

    var showMoodSelector by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    
    var isRefreshing by remember { mutableStateOf(false) }
    val refreshState = rememberPullToRefreshState()
    val coroutineScope = rememberCoroutineScope()

    val usuario = perfilState.usuario
    val pareja = perfilState.pareja
    val context = LocalContext.current

    val onRefresh = {
        isRefreshing = true
        coroutineScope.launch {
            if (usuario != null) {
                perfilViewModel.cargarPerfil(usuario.id, usuario.parejaId)
                if (usuario.relacionId.isNotBlank()) {
                    eventoViewModel.iniciarEscucha(context, usuario.relacionId)
                    diarioViewModel.iniciarEscucha(usuario.id, usuario.parejaId, usuario.relacionId)
                    notaViewModel.iniciar(context, usuario.relacionId, usuario.id, usuario.parejaId)
                }
            }
            kotlinx.coroutines.delay(1000)
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        visible = true
    }

    LaunchedEffect(usuario?.id, pareja?.id, usuario?.relacionId) {
        if (usuario != null && pareja != null) {
            estadoAnimoViewModel.cargarEstados(context, usuario.relacionId, usuario.id, pareja.nombre)
            if (usuario.relacionId.isNotBlank()) {
                eventoViewModel.iniciarEscucha(context, usuario.relacionId)
                diarioViewModel.iniciarEscucha(usuario.id, usuario.parejaId, usuario.relacionId)
                notaViewModel.iniciar(context, usuario.relacionId, usuario.id, usuario.parejaId)
            }
        }
    }

    LaunchedEffect(parejaState.vinculado) {
        if (parejaState.vinculado) {
            onVinculado(parejaState.relacionId)
        }
    }

    if (showMoodSelector) {
        Dialog(
            onDismissRequest = { showMoodSelector = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            EstadoAnimoScreen(
                viewModel = estadoAnimoViewModel,
                perfilViewModel = perfilViewModel,
                onDismiss = { showMoodSelector = false }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f)
                    )
                )
            )
    ) {
        PullToRefreshBox(
            state = refreshState,
            isRefreshing = isRefreshing,
            onRefresh = { onRefresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + expandVertically()
                ) {
                    HomeHeader(usuario = usuario, pareja = pareja)
                }

                if (usuario != null && pareja != null) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, 200)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            CardDiasJuntos(diasJuntos = perfilState.diasJuntos)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                SeccionEstadoAnimo(
                                    modifier = Modifier.weight(1.2f),
                                    miEmoji = estadoAnimoState.emojiPropio,
                                    parejaEmoji = estadoAnimoState.emojiPareja,
                                    nombrePareja = pareja.nombre,
                                    onClick = { showMoodSelector = true }
                                )
                                
                                SeccionNotaPareja(
                                    modifier = Modifier.weight(1f),
                                    nota = notaState.notaPareja,
                                    nombrePareja = pareja.nombre,
                                    onClick = { onNavigateToScreen(NavRoutes.Notas.route) }
                                )
                            }

                            AccesosRapidos(
                                onNuevaEntrada = {
                                    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                                    onNavigateToScreen(NavRoutes.CrearEntrada.crearRuta(hoy, TipoEntrada.MI_DIA.name))
                                },
                                onNuevoEvento = { onNavigateToScreen(NavRoutes.CrearEvento.route) },
                                onChat = { onNavigateToTab(NavRoutes.Chat.route) },
                                onCuestionarios = { onNavigateToTab(NavRoutes.Cuestionarios.route) }
                            )

                            // SECCIÓN DE EVENTOS FILTRADA
                            val todosLosEventos = eventoViewModel.uiState.collectAsState().value.eventos
                            val ahora = Calendar.getInstance().time
                            val formatoCompleto = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            
                            val eventosProximos = todosLosEventos.filter { 
                                val fecha = try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
                                fecha?.after(ahora) == true
                            }.sortedBy { it.fecha }

                            val eventosPasadosPendientes = todosLosEventos.filter {
                                val fecha = try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
                                (fecha?.before(ahora) == true) && !it.convertidoEnRecuerdo
                            }.sortedByDescending { it.fecha }

                            SeccionEventosHome(
                                proximos = eventosProximos,
                                pendientes = eventosPasadosPendientes,
                                onVerTodos = { onNavigateToScreen(NavRoutes.Eventos.route) },
                                onCrear = { onNavigateToScreen(NavRoutes.CrearEvento.route) },
                                onVerDetalle = { id -> onNavigateToScreen(NavRoutes.DetalleEvento.crearRuta(id)) }
                            )

                            val ultimasEntradas = diarioState.ultimasEntradas
                            SeccionUltimaActividad(
                                entradas = ultimasEntradas,
                                onVerDiario = { onNavigateToTab(NavRoutes.Calendario.route) },
                                onVerDetalle = { id -> onNavigateToScreen(NavRoutes.DetalleEntrada.crearRuta(id)) }
                            )

                            SeccionCuestionarios(
                                cuestionarios = cuestionarioState.historial,
                                estados = cuestionarioState.estadosCuestionarios,
                                onVerCuestionarios = { onNavigateToTab(NavRoutes.Cuestionarios.route) },
                                onResponder = { id -> onNavigateToScreen(NavRoutes.ResponderCuestionario.crearRuta(id)) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(usuario: Usuario?, pareja: Usuario?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hola, ${usuario?.nombre ?: "Usuario"}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_hola),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = if (pareja != null) "Tú y ${pareja.nombre} están conectados" else "¡Qué alegría verte de nuevo!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { ThemeManager.isDarkTheme = !ThemeManager.isDarkTheme }) {
                Icon(
                    imageVector = if (ThemeManager.isDarkTheme) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Tema",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            Box(contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = usuario?.fotoPerfil,
                        contentDescription = "Mi perfil",
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .shadow(1.dp, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    if (pareja != null) {
                        Spacer(Modifier.width((-12).dp))
                        AsyncImage(
                            model = pareja.fotoPerfil,
                            contentDescription = "Pareja",
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .shadow(4.dp, CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CardDiasJuntos(diasJuntos: Long) {
    val brush = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.secondaryContainer
        )
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(28.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(brush)
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Llevan juntos",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (diasJuntos >= 0) "$diasJuntos" else "--",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "días",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "creando una historia increíble",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(6.dp))
                    Icon(
                        painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_chispa),
                        contentDescription = null,
                        tint = Color.Unspecified,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionEstadoAnimo(
    modifier: Modifier = Modifier,
    miEmoji: String,
    parejaEmoji: String,
    nombrePareja: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "¿Cómo están hoy?",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoodBubble(emoji = miEmoji.ifBlank { "unknown" }, label = "Tú")
                Icon(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon), 
                    null, 
                    tint = Color.Unspecified, 
                    modifier = Modifier.size(16.dp)
                )
                MoodBubble(emoji = parejaEmoji.ifBlank { "unknown" }, label = nombrePareja)
            }
        }
    }
}

@Composable
private fun MoodBubble(emoji: String, label: String) {
    val mood = MAPA_MOODS[emoji] ?: MAPA_MOODS["unknown"]!!
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = mood.iconRes),
                contentDescription = mood.label,
                tint = Color.Unspecified,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = mood.label, 
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
        Text(
            text = label, 
            style = MaterialTheme.typography.labelSmall, 
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SeccionNotaPareja(
    modifier: Modifier = Modifier,
    nota: com.cadev.mocaapp.feature.notas.domain.model.NotaActual?,
    nombrePareja: String,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_pin), 
                    null, 
                    tint = Color.Unspecified, 
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Nota de $nombrePareja",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFF57F17),
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = nota?.texto ?: "No hay notas nuevas por ahora...",
                style = MaterialTheme.typography.bodySmall,
                color = Color.DarkGray,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun AccesosRapidos(
    onNuevaEntrada: () -> Unit,
    onNuevoEvento: () -> Unit,
    onChat: () -> Unit,
    onCuestionarios: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            "Rincón compartido", 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccesoItem(
                    icon = Icons.Filled.ChatBubble,
                    label = "Chat",
                    color = Color(0xFF64B5F6),
                    modifier = Modifier.weight(1f).height(90.dp),
                    onClick = onChat
                )
                AccesoItem(
                    icon = Icons.Filled.EditNote,
                    label = "Diario",
                    color = Color(0xFFF06292),
                    modifier = Modifier.weight(1f).height(90.dp),
                    onClick = onNuevaEntrada
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AccesoItem(
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    label = "Tests",
                    color = Color(0xFF81C784),
                    modifier = Modifier.weight(1f).height(90.dp),
                    onClick = onCuestionarios
                )
                AccesoItem(
                    icon = Icons.Filled.Event,
                    label = "Evento",
                    color = Color(0xFFFFB74D),
                    modifier = Modifier.weight(1f).height(90.dp),
                    onClick = onNuevoEvento
                )
            }
        }
    }
}

@Composable
private fun AccesoItem(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = color.copy(alpha = 0.15f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SeccionEventosHome(
    proximos: List<com.cadev.mocaapp.feature.eventos.domain.model.Evento>,
    pendientes: List<com.cadev.mocaapp.feature.eventos.domain.model.Evento>,
    onVerTodos: () -> Unit,
    onCrear: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Planes de pareja", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerTodos) { Text("Ver todos") }
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                Text("Próximos", modifier = Modifier.padding(vertical = 12.dp), style = MaterialTheme.typography.labelLarge)
            }
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                    Text("Pendientes", style = MaterialTheme.typography.labelLarge)
                    if (pendientes.isNotEmpty()) {
                        Badge(containerColor = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp)) {
                            Text(pendientes.size.toString(), color = Color.White)
                        }
                    }
                }
            }
        }

        val listaAMostrar = if (selectedTab == 0) proximos else pendientes

        if (listaAMostrar.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = if(selectedTab == 0) onCrear else ({})),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(if (selectedTab == 0) Icons.Filled.Add else Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Text(
                        if (selectedTab == 0) "Planeen su próxima aventura juntos" else "¡Todo al día! No hay eventos pendientes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                listaAMostrar.take(3).forEach { evento ->
                    val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (_: Exception) { TipoEvento.OTRO }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onVerDetalle(evento.id) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Icon(imageVector = tipo.icono, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AccessTime, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("${evento.fecha} · ${evento.hora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                }
                            }
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionUltimaActividad(
    entradas: List<com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario>,
    onVerDiario: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Recuerdos recientes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerDiario) { Text("Ver diario") }
        }

        if (entradas.isEmpty()) {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))) {
                Text("Aún no han escrito nada en su diario. ¡Empiecen hoy!", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), modifier = Modifier.padding(20.dp), textAlign = TextAlign.Center)
            }
        } else {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                entradas.forEach { entrada ->
                    val tipo = try { com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada.valueOf(entrada.tipo) } catch (_: Exception) { com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada.MI_DIA }
                    Card(modifier = Modifier.width(280.dp).clickable { onVerDetalle(entrada.id) }, shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(android.graphics.Color.parseColor("#${tipo.colorHex}")).copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = tipo.icono, contentDescription = null, modifier = Modifier.size(24.dp), tint = Color(android.graphics.Color.parseColor("#${tipo.colorHex}")))
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = entrada.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Text(text = entrada.fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                            if (entrada.detalles.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Text(text = entrada.detalles, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                            }
                            if (entrada.fotos.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                AsyncImage(model = entrada.fotos.first(), contentDescription = null, modifier = Modifier.fillMaxWidth().height(120.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionCuestionarios(
    cuestionarios: List<com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario>,
    estados: Map<String, EstadoCuestionario>,
    onVerCuestionarios: () -> Unit,
    onResponder: (String) -> Unit
) {
    val pendientes = estados.filter { it.value == EstadoCuestionario.PAREJA_RESPONDIÓ }.keys
    Column(verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("¿Cuánto se conocen?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, textAlign = TextAlign.Center)
            Text("Fortalezcan su vínculo respondiendo tests juntos", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), textAlign = TextAlign.Center)
        }
        if (pendientes.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth().clickable { onResponder(pendientes.first()) }, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer), shape = RoundedCornerShape(24.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(Icons.Filled.Bolt, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(36.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¡Pareja respondió!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Es tu turno de completar el test", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${cuestionarios.size}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                        Text("Completados", style = MaterialTheme.typography.labelMedium)
                    }
                    Box(modifier = Modifier.height(50.dp).width(1.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Tests", style = MaterialTheme.typography.labelMedium)
                        Text("Juntos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Button(onClick = onVerCuestionarios, shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Text("Explorar tests")
                }
            }
        }
    }
}
