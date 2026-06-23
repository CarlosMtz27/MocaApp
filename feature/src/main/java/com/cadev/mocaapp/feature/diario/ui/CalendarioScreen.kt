package com.cadev.mocaapp.feature.diario.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.DiaCalendarioInfo
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DEL CALENDARIO DE RECUERDOS
 * 
 * Qué hace:
 * Aquí mostramos un calendario mensual donde podemos ver qué días hemos 
 * guardado fotos o textos. Podemos navegar entre los meses y tocar cualquier 
 * día para ver qué pasó en esa fecha especial.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los fines de semana tengan un color distinto, debemos 
 * modificar la función `CeldaDiaMejorada`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarioScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    onRegresar: () -> Unit,
    onDiaSeleccionado: (fecha: String) -> Unit,
    onVerEventos: () -> Unit
) {
    /**
     * Se obtiene el estado actual del diario y se gestiona la navegación por meses
     */
    val uiState by viewModel.uiState.collectAsState()

    var calendario by remember {
        mutableStateOf(Calendar.getInstance())
    }

    val anio = calendario.get(Calendar.YEAR)
    val mes = calendario.get(Calendar.MONTH) + 1

    /**
     * Cada vez que se cambia de mes se descarga la información de qué días tienen contenido
     */
    LaunchedEffect(anio, mes) {
        viewModel.cargarMes(usuarioId, parejaId, anio, mes)
    }

    /**
     * Se asegura de refrescar el calendario si el usuario vuelve a la aplicación después de usar otra
     */
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        snapshotFlow { lifecycle.currentState }
            .collect { state ->
                if (state == Lifecycle.State.RESUMED) {
                    viewModel.cargarMes(usuarioId, parejaId, anio, mes)
                }
            }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Calendario", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar al inicio")
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
                .padding(horizontal = 16.dp)
        ) {
            /**
             * Parte superior para cambiar de mes y botón para ir a la lista de eventos
             */
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

            /**
             * Letras de los días de la semana de lunes a domingo
             */
            DiasDelaSemana()

            Spacer(Modifier.height(12.dp))

            Box(modifier = Modifier.weight(1f)) {
                /**
                 * Animación suave para cuando el usuario cambia de mes deslizando
                 */
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        }
                    } else {
                        /**
                         * Dibujo de todos los números del mes actual
                         */
                        CuadriculaMes(
                            calendario = targetCal,
                            diasConEntrada = uiState.diasConEntrada,
                            onDiaClick = { fecha -> onDiaSeleccionado(fecha) }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            /**
             * Pequeña guía de colores en la parte inferior
             */
            LeyendaCompacta()
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * Función que dibuja el nombre del mes y los botones de navegación lateral
 */
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

        /**
         * Botón de acceso directo a la planificación de eventos futuros
         */
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

/**
 * Muestra los nombres abreviados de los siete días de la semana
 */
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

/**
 * Organiza los números del mes en filas y columnas asegurando que el día uno caiga en el día de la semana correcto
 */
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
                /**
                 * Rellena con espacios vacíos si la última semana no tiene los siete días
                 */
                if (semana.size < 7) {
                    repeat(7 - semana.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Dibuja cada cuadrado individual del calendario. Si el día tiene una foto la muestra de fondo. 
 * Si es el día de hoy le aplica una pequeña animación de latido.
 */
@Composable
private fun CeldaDiaMejorada(
    dia: Int,
    esHoy: Boolean,
    info: DiaCalendarioInfo,
    onClick: () -> Unit
) {
    val tieneEntrada = info.tipos.isNotEmpty()
    val tieneFoto = !info.primeraFoto.isNullOrBlank()
    val esCompartido = info.autores.size > 1

    /**
     * Animación para resaltar suavemente el día actual
     */
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
            containerColor = if (tieneEntrada && !tieneFoto) {
                val primerTipo = try { TipoEntrada.valueOf(info.tipos.first()) } catch (e: Exception) { TipoEntrada.MI_DIA }
                Color(android.graphics.Color.parseColor("#${primerTipo.colorHex}")).copy(alpha = 0.12f)
            } else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (tieneFoto) 2.dp else 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            /**
             * Si hay un recuerdo con imagen se pone la foto como fondo del día
             */
            if (tieneFoto) {
                AsyncImage(
                    model = info.primeraFoto,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.8f
                )
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.15f)))
            }

            Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(28.dp)
                        .then(if (esHoy) Modifier.scale(pulseScale) else Modifier)
                ) {
                    if (esHoy) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = dia.toString(),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = if (esHoy || tieneEntrada) FontWeight.ExtraBold else FontWeight.Medium,
                        color = when {
                            tieneFoto -> Color.White
                            esHoy -> MaterialTheme.colorScheme.primary
                            tieneEntrada -> MaterialTheme.colorScheme.onSurface
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        }
                    )
                }

                /**
                 * Se ponen pequeños puntos de colores para indicar el tipo de recuerdos que hay ese día
                 */
                if (!tieneFoto && tieneEntrada) {
                    Row(
                        modifier = Modifier.padding(top = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        info.tipos.distinct().take(3).forEach { tipoNombre ->
                            val color = try {
                                Color(android.graphics.Color.parseColor("#${TipoEntrada.valueOf(tipoNombre).colorHex}"))
                            } catch (e: Exception) { MaterialTheme.colorScheme.primary }
                            Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(color))
                        }
                    }
                }
            }

            /**
             * Muestra un pequeño símbolo si los dos miembros de la pareja han escrito algo ese día
             */
            if (esCompartido) {
                Icon(
                    painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_corazon),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(10.dp)
                )
            }
        }
    }
}

/**
 * Sección horizontal en la parte de abajo que explica qué significa cada color de los recuerdos
 */
@Composable
private fun LeyendaCompacta() {
    val scrollState = rememberScrollState()
    
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            "Tu mapa de recuerdos",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TipoEntrada.entries.forEach { tipo ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(android.graphics.Color.parseColor("#${tipo.colorHex}")).copy(alpha = 0.1f),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        Color(android.graphics.Color.parseColor("#${tipo.colorHex}")).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = tipo.icono,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = Color(android.graphics.Color.parseColor("#${tipo.colorHex}"))
                        )
                        Text(
                            text = tipo.etiqueta,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(android.graphics.Color.parseColor("#${tipo.colorHex}")),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
