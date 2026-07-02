package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.animations.AnimacionFadeIn
import com.cadev.mocaapp.feature.ui.components.GlassCard
import com.cadev.mocaapp.feature.ui.theme.*
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*

/**
 * PANTALLA DE DETALLE DEL DÍA - LÍNEA DE TIEMPO (SECCIÓN 4.2)
 * Recrea la estética de "Day Detail Screen" con Timeline luminosa.
 * Adaptada para soportar Modo Oscuro y con sombras/bordes refinados.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailList(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    fecha: String,
    onRegresar: () -> Unit,
    onIrAlCalendario: () -> Unit,
    onCrearEntrada: (String, String) -> Unit,
    onVerDetalleEntrada: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var mostrarMenuCrear by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val isDark = esModoOscuro()
    
    // Cargar entradas del día al iniciar
    LaunchedEffect(fecha) {
        viewModel.cargarEntradasDelDia(usuarioId, parejaId, fecha)
    }

    val fechaFormateada = remember(fecha) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(fecha) ?: Date()
            SimpleDateFormat("d 'de' MMMM", Locale.forLanguageTag("es-MX")).format(date)
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            fecha
        }
    }

    val esFuturo = remember(fecha) {
        val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        fecha > hoy
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Nuestro Camino", 
                        style = OrganicTypography.headlineMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Regresar", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onIrAlCalendario) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday, 
                            contentDescription = "Calendario", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            if (!esFuturo) {
                FloatingActionButton(
                    onClick = { mostrarMenuCrear = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir")
                }
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        // Bottom Sheet para elegir tipo de entrada
        if (mostrarMenuCrear) {
            ModalBottomSheet(
                onDismissRequest = { mostrarMenuCrear = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 40.dp, start = 24.dp, end = 24.dp)
                ) {
                    Text(
                        "¿Qué quieres añadir?",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    MenuOptionItem(
                        icon = Icons.AutoMirrored.Filled.Notes,
                        title = "Mi Día",
                        subtitle = "Escribe una nota rápida sobre hoy",
                        onClick = {
                            mostrarMenuCrear = false
                            onCrearEntrada(fecha, TipoEntrada.MI_DIA.name)
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    MenuOptionItem(
                        icon = Icons.Default.CameraAlt,
                        title = "Recuerdo",
                        subtitle = "Guarda un momento especial con fotos",
                        onClick = {
                            mostrarMenuCrear = false
                            onCrearEntrada(fecha, TipoEntrada.RECUERDO.name)
                        }
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 40.dp, bottom = 100.dp)
            ) {
                // HEADER DE LA PANTALLA
                item {
                    AnimacionFadeIn {
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)) {
                            Text(
                                text = fechaFormateada,
                                style = OrganicTypography.headlineMedium.copy(fontSize = 40.sp),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Un día de reflexión y conexión.",
                                style = OrganicTypography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // CONTENEDOR DE LA LÍNEA DE TIEMPO
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                }

                itemsIndexed(uiState.entradas) { index, entrada ->
                    AnimacionFadeIn(delayMillis = 100 * (index + 1)) {
                        TimelineItem(
                            entrada = entrada,
                            esUltimo = index == uiState.entradas.lastIndex,
                            onClick = { onVerDetalleEntrada(entrada.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    entrada: EntradaDiario,
    esUltimo: Boolean,
    onClick: () -> Unit
) {
    val isDark = esModoOscuro()
    val hora = remember(entrada.creadaEn) {
        val date = entrada.creadaEn.toDate()
        SimpleDateFormat("hh:mm a", Locale.US).format(date)
    }

    val icono = when {
        entrada.tipo == "RECUERDO" -> Icons.Default.CameraAlt
        entrada.tipo == "CAMINATA" -> Icons.AutoMirrored.Filled.DirectionsWalk
        else -> Icons.AutoMirrored.Filled.Notes
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(bottom = 32.dp)
    ) {
        // COLUMNA DE LA LÍNEA (Izquierda)
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            // Línea luminosa (Luminous Line)
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
                    .padding(top = 24.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = if (esUltimo) {
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), Color.Transparent)
                            } else {
                                listOf(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f), MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                            }
                        ),
                        shape = CircleShape
                    )
            )
            
            // Puntito de la línea de tiempo (Timeline Dot)
            Box(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .size(10.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    .padding(2.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape)
            )
        }

        // CONTENIDO (Tarjeta Glass)
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp)), // Sombra de 2.dp con forma
            bordeRedondeado = 16.dp,
            colorBorde = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Gray.copy(alpha = 0.1f), // Bordes muy tenues
            alHacerClick = onClick
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = hora,
                            style = OrganicTypography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = entrada.titulo,
                        style = OrganicTypography.headlineMedium.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = entrada.detalles,
                        style = OrganicTypography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (entrada.fotos.isNotEmpty()) {
                    AsyncImage(
                        model = entrada.fotos.first(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(MocaShapes.medium)
                            .align(Alignment.CenterVertically),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun MenuOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onPrimaryContainer, 
                    modifier = Modifier.size(24.dp)
                )
            }
            Column {
                Text(
                    text = title, 
                    style = OrganicTypography.labelMedium.copy(fontSize = 16.sp), 
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = subtitle, 
                    style = OrganicTypography.bodySmall, 
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
        }
    }
}
