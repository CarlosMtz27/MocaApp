package com.cadev.mocaapp.feature.diario.ui

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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DE LOS RECUERDOS DE UN DÍA
 * 
 * Qué hace:
 * Aquí mostramos todos los mensajes, fotos y vídeos que hemos guardado en una 
 * fecha específica. Los recuerdos aparecen en forma de burbujas para que se 
 * vea quién escribió cada cosa, como si fuera una conversación.
 * 
 * Cómo lo podemos modificar:
 * Si queremos que los recuerdos se ordenen al revés (del más nuevo al más viejo), 
 * debemos cambiar el `sortedBy` por un `sortedByDescending` en la lista de entradas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDiaScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    fecha: String,
    onRegresar: () -> Unit,
    onEditarEntrada: (entradaId: String) -> Unit,
    onCrearEntrada: (fecha: String, tipo: String) -> Unit,
    onVerDetalle: (entradaId: String) -> Unit
) {
    /**
     * Se descargan los recuerdos guardados para la fecha seleccionada
     */
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(fecha) {
        viewModel.cargarEntradasDelDia(usuarioId, parejaId, fecha)
    }

    /**
     * Se prepara la fecha en un formato bonito para leer en la cabecera
     */
    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoVisible = SimpleDateFormat("EEEE d 'de' MMMM", Locale.forLanguageTag("es-MX"))
    val fechaVisible = try {
        formatoVisible.format(formatoEntrada.parse(fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { fecha }

    var fabExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = fechaVisible,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Capítulo de su historia",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            /**
             * Botón circular que se despliega para elegir qué tipo de recuerdo añadir
             */
            FabMejorado(
                expandido = fabExpandido,
                onToggle = { fabExpandido = !fabExpandido },
                onOpcionSeleccionada = { tipo ->
                    fabExpandido = false
                    onCrearEntrada(fecha, tipo)
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.05f))
                    )
                )
        ) {
            if (uiState.cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.entradas.isEmpty()) {
                /**
                 * Se muestra un mensaje amigable si el día todavía no tiene ningún recuerdo
                 */
                PantallaVacia()
            } else {
                val entradasOrdenadas = uiState.entradas.sortedBy { it.creadaEn }

                /**
                 * Listado vertical con todos los recuerdos ordenados por hora
                 */
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp)
                ) {
                    itemsIndexed(entradasOrdenadas) { index, entrada ->
                        val noVista = entrada.usuarioId != usuarioId && !viewModel.esEntradaVista(entrada.id)
                        
                        /**
                         * Animación para que los recuerdos aparezcan uno tras otro con suavidad
                         */
                        var visible by remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            delay(index * 100L)
                            visible = true
                        }

                        AnimatedVisibility(
                            visible = visible,
                            enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 }
                        ) {
                            /**
                             * Cada tarjeta individual del diario
                             */
                            TarjetaEntradaDiario(
                                entrada = entrada,
                                esMia = entrada.usuarioId == usuarioId,
                                noVista = noVista,
                                onEditar = { onEditarEntrada(entrada.id) },
                                onVerDetalle = {
                                    viewModel.marcarEntradaVista(entrada.id)
                                    onVerDetalle(entrada.id)
                                }
                            )
                        }
                    }
                }
            }

            /**
             * Capa oscura que aparece cuando el botón de añadir está desplegado
             */
            if (fabExpandido) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f))
                        .clickable { fabExpandido = false }
                )
            }
        }
    }
}

/**
 * Función que dibuja el diseño cuando no hay nada escrito en el diario ese día
 */
@Composable
private fun PantallaVacia() {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(
            "Un lienzo en blanco",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Cada día es una oportunidad para\nguardar algo que amen.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Función que crea la tarjeta visual de un recuerdo. Coloca el texto a la derecha si es tuyo 
 * y a la izquierda si es de tu pareja similar a una conversación.
 */
@Composable
private fun TarjetaEntradaDiario(
    entrada: EntradaDiario,
    esMia: Boolean,
    noVista: Boolean,
    onEditar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val tipo = try { TipoEntrada.valueOf(entrada.tipo) } catch (e: Exception) { TipoEntrada.MI_DIA }
    val colorTipo = Color(android.graphics.Color.parseColor("#${tipo.colorHex}"))
    val formatoHora = SimpleDateFormat("h:mm a", Locale.getDefault())
    val hora = try { formatoHora.format(entrada.creadaEn) } catch (e: Exception) { "" }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (esMia) Alignment.End else Alignment.Start
    ) {
        /**
         * Pequeña etiqueta con la hora y quién escribió el recuerdo
         */
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            if (!esMia) {
                Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text("Pareja · ", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
            Text(hora, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
            if (esMia) {
                Spacer(Modifier.width(4.dp))
                Text(" · Tú", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomStart = if (esMia) 20.dp else 4.dp,
                    bottomEnd = if (esMia) 4.dp else 20.dp
                ))
                .clickable(onClick = onVerDetalle),
            shape = RoundedCornerShape(
                topStart = 20.dp,
                topEnd = 20.dp,
                bottomStart = if (esMia) 20.dp else 4.dp,
                bottomEnd = if (esMia) 4.dp else 20.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (esMia) MaterialTheme.colorScheme.surface else colorTipo.copy(alpha = 0.08f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (noVista) 6.dp else 2.dp),
            border = if (noVista) androidx.compose.foundation.BorderStroke(2.dp, colorTipo) else null
        ) {
            Column {
                /**
                 * Se muestra la primera foto del recuerdo como portada
                 */
                if (entrada.fotos.isNotEmpty()) {
                    AsyncImage(
                        model = entrada.fotos.first(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                        contentScale = ContentScale.Crop
                    )
                }

                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(
                                imageVector = tipo.icono,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (esMia) MaterialTheme.colorScheme.primary else colorTipo
                            )
                            Text(
                                text = entrada.titulo,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (esMia) MaterialTheme.colorScheme.onSurface else colorTipo
                            )
                        }
                        
                        /**
                         * Botón para modificar el recuerdo solo si es propiedad del usuario
                         */
                        if (esMia) {
                            IconButton(onClick = onEditar, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    if (entrada.detalles.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = entrada.detalles,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            maxLines = 4,
                            fontStyle = FontStyle.Italic
                        )
                    }

                    /**
                     * Fila de dibujos de sentimientos elegidos para este momento
                     */
                    if (entrada.emociones.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape).padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            entrada.emociones.take(4).forEach { em ->
                                val emotion = try { com.cadev.mocaapp.feature.diario.domain.model.Emocion.valueOf(em) } catch (e: Exception) { null }
                                if (emotion != null) {
                                    Icon(
                                        painter = painterResource(id = emotion.iconRes),
                                        contentDescription = emotion.etiqueta,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(18.dp)
                                    )
                                } else {
                                    Icon(
                                        painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_chispa),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    /**
                     * Mensaje llamativo si este recuerdo acaba de ser compartido por la pareja
                     */
                    if (noVista) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            color = colorTipo,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "¡Nuevo recuerdo!",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Función que crea el botón circular de añadir y gestiona su animación de giro y apertura
 */
@Composable
private fun FabMejorado(
    expandido: Boolean,
    onToggle: () -> Unit,
    onOpcionSeleccionada: (tipo: String) -> Unit
) {
    val rotation by animateFloatAsState(if (expandido) 45f else 0f, label = "Rotate")

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * Se muestran las opciones de creación solo cuando el botón principal está pulsado
         */
        if (expandido) {
            OpcionFabMejorada(
                icono = Icons.Default.CameraAlt,
                label = "Nuevo Recuerdo",
                color = Color(0xFF7B1FA2),
                description = "Fotos y videos",
                onClick = { onOpcionSeleccionada(TipoEntrada.RECUERDO.name) }
            )
            OpcionFabMejorada(
                icono = Icons.Default.Edit,
                label = "Mi día",
                color = Color(0xFFC2185B),
                description = "¿Cómo fue hoy?",
                onClick = { onOpcionSeleccionada(TipoEntrada.MI_DIA.name) }
            )
        }
        
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.size(56.dp)
        ) {
            Icon(
                Icons.Default.Add,
                null,
                modifier = Modifier
                    .scale(if (expandido) 1.2f else 1f)
                    .rotate(rotation)
            )
        }
    }
}

/**
 * Función auxiliar para dibujar cada una de las opciones que salen del botón añadir
 */
@Composable
private fun OpcionFabMejorada(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                    Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Text(description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }
        }
        Spacer(Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
