package com.cadev.mocaapp.feature.diario.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.model.TipoEvento
import com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PANTALLA DE NUESTRO DIARIO COMPARTIDO
 * 
 * Qué hace:
 * Muestra todos los momentos especiales (recuerdos, eventos y notas diarias) en 
 * un formato de lista elegante o calendario mensual. Permite filtrar y ordenar 
 * para revivir nuestras aventuras juntos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    relacionId: String,
    onRegresar: () -> Unit,
    onDiaSeleccionado: (fecha: String) -> Unit,
    onVerEventos: () -> Unit,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleEvento: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var calendario by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH) + 1

    LaunchedEffect(usuarioId, parejaId, relacionId) {
        viewModel.iniciarEscucha(usuarioId, parejaId, relacionId)
    }

    LaunchedEffect(anio, mes, relacionId) {
        viewModel.cargarMes(usuarioId, parejaId, relacionId, anio, mes)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle, relacionId) {
        snapshotFlow { lifecycle.currentState }
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    viewModel.cargarMes(usuarioId, parejaId, relacionId, anio, mes)
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Nuestro diario",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.toggleVista() }) {
                        Icon(
                            imageVector = if (uiState.verComoLista) Icons.Default.CalendarMonth else Icons.Default.Map,
                            contentDescription = "Cambiar vista",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        onDiaSeleccionado(hoy)
                    }) {
                        Icon(Icons.Default.Add, "Nuevo recuerdo", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            if (!uiState.verComoLista) {
                // VISTA DE CALENDARIO
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    EncabezadoMes(
                        calendario = calendario,
                        onMesAnterior = {
                            val nuevo = calendario.clone() as Calendar
                            nuevo.add(Calendar.MONTH, -1)
                            calendario = nuevo
                        },
                        onMesSiguiente = {
                            val nuevo = calendario.clone() as Calendar
                            nuevo.add(Calendar.MONTH, 1)
                            calendario = nuevo
                        },
                        onVerEventos = onVerEventos
                    )

                    Spacer(Modifier.height(24.dp))
                    DiasDelaSemana()
                    Spacer(Modifier.height(12.dp))

                    Box(modifier = Modifier.weight(1f)) {
                        AnimatedContent(
                            targetState = calendario,
                            transitionSpec = {
                                val isNext = targetState.after(initialState)
                                if (isNext) {
                                    (slideInHorizontally { it } + fadeIn()).togetherWith(slideOutHorizontally { -it } + fadeOut())
                                } else {
                                    (slideInHorizontally { -it } + fadeIn()).togetherWith(slideOutHorizontally { it } + fadeOut())
                                }
                            },
                            label = "MonthTransition"
                        ) { targetCal ->
                            if (uiState.cargando && uiState.diasConEntrada.isEmpty()) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            } else {
                                CuadriculaMes(
                                    calendario = targetCal,
                                    diasConEntrada = uiState.diasConEntrada,
                                    onDiaClick = { fecha -> onDiaSeleccionado(fecha) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    LeyendaCompacta()
                    Spacer(Modifier.height(16.dp))
                }
            } else {
                // VISTA DE LISTA RE-DISEÑADA
                BarraHerramientasLista(
                    filtroActual = uiState.filtro,
                    ordenActual = uiState.orden,
                    onCambiarFiltro = viewModel::cambiarFiltro,
                    onCambiarOrden = viewModel::cambiarOrden
                )

                VistaListaRecuerdosMejorada(
                    uiState = uiState,
                    onVerDetalleEntrada = onVerDetalleEntrada,
                    onVerDetalleEvento = onVerDetalleEvento
                )
            }
        }
    }
}

@Composable
private fun BarraHerramientasLista(
    filtroActual: FiltroListado,
    ordenActual: OrdenListado,
    onCambiarFiltro: (FiltroListado) -> Unit,
    onCambiarOrden: (OrdenListado) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FiltroListado.entries.forEach { filtro ->
                FilterChip(
                    selected = filtroActual == filtro,
                    onClick = { onCambiarFiltro(filtro) },
                    label = { Text(filtro.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (ordenActual == OrdenListado.RECIENTE) "Más recientes primero" else "Más antiguos primero",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            IconButton(onClick = {
                onCambiarOrden(if (ordenActual == OrdenListado.RECIENTE) OrdenListado.ANTIGUO else OrdenListado.RECIENTE)
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Sort,
                    contentDescription = "Ordenar",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun VistaListaRecuerdosMejorada(
    uiState: DiarioUiState,
    onVerDetalleEntrada: (String) -> Unit,
    onVerDetalleEvento: (String) -> Unit
) {
    val items = remember(uiState.entradas, uiState.eventos, uiState.filtro, uiState.orden) {
        val list = mutableListOf<Any>()
        
        if (uiState.filtro == FiltroListado.TODOS || uiState.filtro == FiltroListado.RECUERDOS || uiState.filtro == FiltroListado.DIAS) {
            val entradasFiltradas = if (uiState.filtro == FiltroListado.TODOS) {
                uiState.entradas
            } else if (uiState.filtro == FiltroListado.RECUERDOS) {
                uiState.entradas.filter { it.tipo == TipoEntrada.RECUERDO.name }
            } else {
                uiState.entradas.filter { it.tipo == TipoEntrada.MI_DIA.name }
            }
            list.addAll(entradasFiltradas)
        }
        
        if (uiState.filtro == FiltroListado.TODOS || uiState.filtro == FiltroListado.EVENTOS) {
            list.addAll(uiState.eventos)
        }

        if (uiState.orden == OrdenListado.RECIENTE) {
            list.sortedByDescending { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        } else {
            list.sortedBy { if (it is EntradaDiario) it.fecha else (it as Evento).fecha }
        }
    }

    if (items.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                "No hay nada que mostrar con estos filtros",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            items(items) { item ->
                when (item) {
                    is EntradaDiario -> TarjetaItemDiario(
                        fecha = item.fecha,
                        titulo = item.titulo,
                        tipoLabel = if (item.tipo == TipoEntrada.RECUERDO.name) "Recuerdo" else "Mi día",
                        icon = if (item.tipo == TipoEntrada.RECUERDO.name) Icons.Default.CameraAlt else Icons.Default.EditNote,
                        foto = item.fotos.firstOrNull(),
                        onClick = { onVerDetalleEntrada(item.id) }
                    )
                    is Evento -> {
                        val tipo = try { TipoEvento.valueOf(item.tipo) } catch (_: Exception) { TipoEvento.OTRO }
                        TarjetaItemDiario(
                            fecha = item.fecha,
                            titulo = item.titulo,
                            tipoLabel = tipo.etiqueta,
                            icon = tipo.icono,
                            esEvento = true,
                            onClick = { onVerDetalleEvento(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaItemDiario(
    fecha: String,
    titulo: String,
    tipoLabel: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    foto: String? = null,
    esEvento: Boolean = false,
    onClick: () -> Unit
) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance().apply { time = sdf.parse(fecha) ?: Date() }
    
    val mes = SimpleDateFormat("MMM", Locale.getDefault()).format(cal.time).lowercase()
    val diaNum = cal.get(Calendar.DAY_OF_MONTH).toString()
    val anio = cal.get(Calendar.YEAR).toString()

    val hoy = Calendar.getInstance().apply { 
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
    
    val fechaItem = cal.timeInMillis
    val diff = fechaItem - hoy
    val diasDiff = TimeUnit.MILLISECONDS.toDays(diff).toInt()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // INDICADOR DE FECHA A LA IZQUIERDA
        Column(
            modifier = Modifier.width(60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
            ) {
                Text(
                    text = mes,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = diaNum,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = anio,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }

        Spacer(Modifier.width(16.dp))

        // TARJETA PRINCIPAL
        Card(
            modifier = Modifier
                .weight(1f)
                .height(85.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                // FONDO TENUE (IMAGEN O LOGO)
                if (foto != null) {
                    AsyncImage(
                        model = foto,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize().alpha(0.08f),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_corazon),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(100.dp)
                            .offset(x = 30.dp)
                            .alpha(0.05f),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // CONTENIDO DE LA TARJETA
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // ICONO PRINCIPAL
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    // TEXTOS
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = tipoLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }

                    // INFORMACIÓN DE DÍAS (RESTANTES O PASADOS)
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = Math.abs(diasDiff).toString(),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (diasDiff >= 0) "d. restantes" else "d. pasados",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EncabezadoMes(
    calendario: Calendar,
    onMesAnterior: () -> Unit,
    onMesSiguiente: () -> Unit,
    onVerEventos: () -> Unit
) {
    val formatoMes = SimpleDateFormat("MMMM yyyy", Locale.forLanguageTag("es-MX"))
    val mesActual = formatoMes.format(calendario.time).replaceFirstChar { it.uppercase() }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMesAnterior,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ChevronLeft, "Anterior", tint = MaterialTheme.colorScheme.primary)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = mesActual,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Sus momentos juntos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = onMesSiguiente,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(Icons.Default.ChevronRight, "Siguiente", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = onVerEventos,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp)
        ) {
            Icon(Icons.Filled.Event, null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text("Próximos eventos", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DiasDelaSemana() {
    val dias = listOf("Dom", "Lun", "Mar", "Mié", "Jue", "Vie", "Sáb")
    Row(modifier = Modifier.fillMaxWidth()) {
        dias.forEach { dia ->
            Text(
                text = dia,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun CuadriculaMes(
    calendario: Calendar,
    diasConEntrada: Map<String, DiaCalendarioInfo>,
    onDiaClick: (String) -> Unit
) {
    val formatoFecha = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val hoy = formatoFecha.format(Date())

    val calMes = calendario.clone() as Calendar
    calMes.set(Calendar.DAY_OF_MONTH, 1)
    val primerDiaSemana = calMes.get(Calendar.DAY_OF_WEEK) - 1
    val totalDias = calMes.getActualMaximum(Calendar.DAY_OF_MONTH)
    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH)

    val celdas = List(primerDiaSemana) { null } + List(totalDias) { it + 1 }
    val semanas = celdas.chunked(7)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        semanas.forEach { semana ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                semana.forEach { dia ->
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f)) {
                        if (dia != null) {
                            val fechaCelda = Calendar.getInstance().apply { set(anio, mes, dia) }
                            val fechaStr = formatoFecha.format(fechaCelda.time)
                            val info = diasConEntrada[fechaStr] ?: DiaCalendarioInfo()
                            val esHoy = fechaStr == hoy

                            CeldaDiaMejorada(
                                dia = dia,
                                esHoy = esHoy,
                                info = info,
                                onClick = { onDiaClick(fechaStr) }
                            )
                        }
                    }
                }
                if (semana.size < 7) {
                    repeat(7 - semana.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun CeldaDiaMejorada(
    dia: Int,
    esHoy: Boolean,
    info: DiaCalendarioInfo,
    onClick: () -> Unit
) {
    val tieneEntrada = info.tipos.any { !it.startsWith("EVENTO_") }
    val tieneEvento = info.tipos.any { it.startsWith("EVENTO_") }
    val tieneFoto = !info.primeraFoto.isNullOrBlank()
    val esCompartido = info.autores.size > 1

    val pulseScale by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Scale"
    )

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .then(
                if (esCompartido) Modifier.border(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                tieneFoto -> Color.Transparent
                tieneEntrada -> {
                    val primerTipo = try { 
                        TipoEntrada.valueOf(info.tipos.first { !it.startsWith("EVENTO_") }) 
                    } catch (e: Exception) { TipoEntrada.MI_DIA }
                    Color(android.graphics.Color.parseColor("#${primerTipo.colorHex}")).copy(alpha = 0.12f)
                }
                tieneEvento -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (tieneFoto) 2.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (tieneFoto) {
                AsyncImage(
                    model = info.primeraFoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .then(if (esHoy) Modifier.scale(pulseScale) else Modifier)
                ) {
                    if (esHoy) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Text(
                        text = dia.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (esHoy || tieneEntrada || tieneEvento) FontWeight.ExtraBold else FontWeight.Medium,
                        color = when {
                            tieneFoto -> Color.White
                            esHoy -> MaterialTheme.colorScheme.primary
                            tieneEntrada || tieneEvento -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }

                // PUNTITOS INDICADORES (SIEMPRE VISIBLES ABAJO)
                if (tieneEntrada || tieneEvento) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        info.tipos.distinct().take(3).forEach { tipoNombre ->
                            val color = when {
                                tipoNombre.startsWith("EVENTO_") -> Color(0xFFFFC107) // Amarillo Evento
                                tipoNombre == TipoEntrada.RECUERDO.name -> Color(0xFF2196F3) // Azul Recuerdo
                                tipoNombre == TipoEntrada.MI_DIA.name -> Color(0xFF4CAF50) // Verde Mi Día
                                else -> MaterialTheme.colorScheme.primary
                            }
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            )
                        }
                    }
                }
            }

            if (esCompartido) {
                Icon(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_corazon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun LeyendaCompacta() {
    val scrollState = rememberScrollState()
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            "Tu mapa de recuerdos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EtiquetaLeyenda(
                label = "Mi día",
                icon = Icons.AutoMirrored.Filled.Notes,
                color = Color(0xFF4CAF50) // Verde
            )
            EtiquetaLeyenda(
                label = "Recuerdo",
                icon = Icons.Default.CameraAlt,
                color = Color(0xFF2196F3) // Azul
            )
            EtiquetaLeyenda(
                label = "Evento",
                icon = Icons.Default.Event,
                color = Color(0xFFFFC107) // Amarillo
            )
        }
    }
}

@Composable
private fun EtiquetaLeyenda(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, modifier = Modifier.size(14.dp), tint = color)
            Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}
