package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
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
    
    // Cargar entradas del día al iniciar
    LaunchedEffect(fecha) {
        viewModel.cargarEntradasDelDia(usuarioId, parejaId, fecha)
    }

    val fechaFormateada = remember(fecha) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(fecha) ?: Date()
            SimpleDateFormat("MMMM d'th'", Locale.forLanguageTag("en-US")).format(date)
        } catch (e: Exception) {
            fecha
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Our Journey", 
                        style = OrganicTypography.headlineMedium.copy(fontSize = 24.sp),
                        color = MocaPrimary
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MocaOnSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onIrAlCalendario) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Calendario", tint = MocaOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MocaSurface.copy(alpha = 0.8f)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onCrearEntrada(fecha, TipoEntrada.MI_DIA.name) },
                containerColor = MocaPrimary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Entry")
            }
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MocaBackground) // Simulación simple del mesh gradient
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
                                color = MocaPrimary
                            )
                            Text(
                                text = "A day of reflection and connection.",
                                style = OrganicTypography.bodyLarge,
                                color = MocaOnSurfaceVariant
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
    val hora = remember(entrada.creadaEn) {
        val date = entrada.creadaEn?.toDate() ?: Date()
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
            .height(IntrinsicSize.Min) // IMPORTANTE: Permite que la línea sepa su altura
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
                                listOf(MocaPrimaryContainer.copy(alpha = 0.8f), Color.Transparent)
                            } else {
                                listOf(MocaPrimaryContainer.copy(alpha = 0.8f), MocaPrimaryContainer.copy(alpha = 0.8f))
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
                    .background(MocaPrimaryContainer, CircleShape)
                    .padding(2.dp)
                    .background(MocaSurface, CircleShape)
            )
        }

        // CONTENIDO (Tarjeta Glass)
        GlassCard(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            bordeRedondeado = 16.dp,
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
                            tint = MocaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = hora,
                            style = OrganicTypography.labelMedium,
                            color = MocaPrimary
                        )
                    }

                    Text(
                        text = entrada.titulo,
                        style = OrganicTypography.headlineMedium.copy(fontSize = 24.sp),
                        color = MocaOnSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Text(
                        text = entrada.detalles,
                        style = OrganicTypography.bodyMedium,
                        color = MocaOnSurfaceVariant,
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
