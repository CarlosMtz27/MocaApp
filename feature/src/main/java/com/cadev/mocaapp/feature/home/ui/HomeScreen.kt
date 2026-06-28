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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
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
import com.cadev.mocaapp.feature.home.ui.components.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Modificador para el fondo Mesh Gradient del diseño de Stitch
 */
fun Modifier.meshGradientBackground() = this.drawBehind {
    val canvasWidth = size.width
    val canvasHeight = size.height
    drawRect(color = Color(0xFFFFF8EF))
    val gradients = listOf(
        Triple(Offset(canvasWidth * 0.4f, canvasHeight * 0.2f), Color(0xFFFFD9E2), 0.5f),
        Triple(Offset(canvasWidth * 0.8f, canvasHeight * 0.0f), Color(0xFFFAF3E7), 0.5f),
        Triple(Offset(canvasWidth * 0.0f, canvasHeight * 0.5f), Color(0xFFF4EDE1), 0.5f),
        Triple(Offset(canvasWidth * 0.8f, canvasHeight * 0.5f), Color(0xFFE7BBC6), 0.5f),
        Triple(Offset(canvasWidth * 0.0f, canvasHeight * 1.0f), Color(0xFFFFD1DC), 0.5f),
        Triple(Offset(canvasWidth * 0.8f, canvasHeight * 1.0f), Color(0xFFE9E2D6), 0.5f),
        Triple(Offset(canvasWidth * 0.0f, canvasHeight * 0.0f), Color(0xFFFFF8EF), 0.5f)
    )
    gradients.forEach { (offset, color, radiusScale) ->
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(color, Color.Transparent),
                center = offset,
                radius = canvasWidth * radiusScale
            ),
            center = offset,
            radius = canvasWidth * radiusScale
        )
    }
}

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
        DailyMoodModal(
            onDismiss = { showMoodSelector = false },
            onMoodSelected = { mood ->
                if (usuario != null) {
                    estadoAnimoViewModel.seleccionarEmoji(
                        context = context,
                        relacionId = usuario.relacionId,
                        uid = usuario.id,
                        nombreUsuario = usuario.nombre,
                        parejaId = usuario.parejaId,
                        emoji = mood.emoji
                    )
                }
                showMoodSelector = false
            },
            currentMood = estadoAnimoState.emojiPropio.ifBlank { "?" },
            nombrePareja = pareja?.nombre ?: "Pareja",
            estadoPareja = estadoAnimoState.emojiPareja.ifBlank { "Desconocido" }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .meshGradientBackground()
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
                    .padding(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (usuario != null && pareja != null) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, 200)) + slideInVertically(initialOffsetY = { 40 })
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Saludo y Frase Inspiradora
                            val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                            val saludo = when (hora) {
                                in 6..12 -> "Buenos días"
                                in 13..19 -> "Buenas tardes"
                                else -> "Buenas noches"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp)
                                    .padding(top = 8.dp),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(
                                    text = "$saludo, ${usuario.nombre}",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E1B14)
                                )
                                Text(
                                    text = "“Tu lo eres todo, todo en mi vida”",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF78555E).copy(alpha = 0.8f),
                                    lineHeight = 18.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                                DaysCounterCard(diasJuntos = perfilState.diasJuntos.toInt())
                            }

                            MoodAndNoteRow(
                                miEmoji = estadoAnimoState.emojiPropio.ifBlank { "?" },
                                parejaEmoji = estadoAnimoState.emojiPareja.ifBlank { "?" },
                                notaPareja = notaState.notaPareja?.texto ?: "No hay notas nuevas por ahora...",
                                nombrePareja = pareja.nombre,
                                onMoodClick = { showMoodSelector = true },
                                onNoteClick = { onNavigateToScreen(NavRoutes.Notas.route) },
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )

                            // MAPEO DE DATOS REALES PARA EL ACTIVITY HUB
                            val todosLosEventos = eventoViewModel.uiState.collectAsState().value.eventos
                            val ahora = Calendar.getInstance().time
                            val formatoCompleto = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                            
                            val proximosFiltrados = todosLosEventos.filter { 
                                val fecha = try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
                                fecha?.after(ahora) == true
                            }.sortedBy { it.fecha }

                            val pendientesFiltrados = todosLosEventos.filter {
                                val fecha = try { formatoCompleto.parse("${it.fecha} ${it.hora}") } catch (e: Exception) { null }
                                (fecha?.before(ahora) == true) && !it.convertidoEnRecuerdo
                            }.sortedByDescending { it.fecha }

                            val proximosMapeados = proximosFiltrados.map { 
                                EventoPlan(it.id, it.titulo, "${it.fecha} · ${it.hora}", "")
                            }

                            val pendientesMapeados = pendientesFiltrados.map { 
                                EventoPlan(it.id, it.titulo, "${it.fecha} · ${it.hora}", "")
                            }
                            
                            val ultimasEntradas = diarioState.ultimasEntradas
                            val recuerdosMapeados = ultimasEntradas.map {
                                RecuerdoMemoria(
                                    id = it.id,
                                    titulo = it.titulo,
                                    detalles = it.fecha, // Quitamos el campo inexistente
                                    urlImagen = it.fotos.firstOrNull() ?: "",
                                    cantidadFotos = it.fotos.size
                                )
                            }

                            HomeActivitySections(
                                proximosEventos = proximosMapeados,
                                pendientesEventos = pendientesMapeados,
                                recuerdosRecientes = recuerdosMapeados,
                                onVerEvento = { id -> onNavigateToScreen(NavRoutes.DetalleEvento.crearRuta(id)) },
                                onVerRecuerdo = { id -> onNavigateToScreen(NavRoutes.DetalleEntrada.crearRuta(id)) }
                            )

                            // SECCIÓN CUESTIONARIOS (ESTADÍSTICAS)
                            QuizStatsCard(
                                completados = cuestionarioState.historial.size,
                                coincidencias = 18, // Podrías calcular esto real si tienes el dato
                                onVerDetalles = { onNavigateToTab(NavRoutes.Cuestionarios.route) }
                            )
                        }
                    }
                }
            }
        }
    }
}
