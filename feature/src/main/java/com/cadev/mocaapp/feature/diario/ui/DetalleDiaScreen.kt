package com.cadev.mocaapp.feature.diario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.text.SimpleDateFormat
import java.util.*

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
    val uiState by viewModel.uiState.collectAsState()

    // Cargar entradas del día al entrar
    LaunchedEffect(fecha) {
        viewModel.cargarEntradasDelDia(usuarioId, parejaId, fecha)
    }

    // Formato legible de la fecha
    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoVisible = SimpleDateFormat(
        "EEEE d 'de' MMMM, yyyy", Locale("es", "MX")
    )
    val fechaVisible = try {
        formatoVisible.format(formatoEntrada.parse(fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { fecha }

    // FAB expandible
    var fabExpandido by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = fechaVisible,
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FabExpandible(
                expandido = fabExpandido,
                onToggle = { fabExpandido = !fabExpandido },
                onOpcionSeleccionada = { tipo ->
                    fabExpandido = false
                    onCrearEntrada(fecha, tipo)
                }
            )
        }
    ) { padding ->

        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        } else if (uiState.entradas.isEmpty()) {
            // Estado vacío
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "📅", fontSize = 56.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "No hay nada registrado\npara este día",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Usa el botón + para agregar\nalgo especial",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(uiState.entradas) { entrada ->
                    TarjetaEntrada(
                        entrada = entrada,
                        esMia = entrada.usuarioId == usuarioId,
                        onEditar = { onEditarEntrada(entrada.id) },
                        onVerDetalle = { onVerDetalle(entrada.id) }
                    )
                }
            }
        }

        // Overlay oscuro al expandir el FAB
        if (fabExpandido) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
            )
        }
    }
}

//FAB Expandible

@Composable
private fun FabExpandible(
    expandido: Boolean,
    onToggle: () -> Unit,
    onOpcionSeleccionada: (tipo: String) -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Opciones que aparecen al expandir
        if (expandido) {
            OpcionFab(
                emoji = "⭐",
                etiqueta = "Día especial",
                color = Color(0xFFF9A825),
                onClick = {
                    onOpcionSeleccionada(TipoEntrada.DIA_ESPECIAL.name)
                }
            )
            OpcionFab(
                emoji = "🗓️",
                etiqueta = "Evento",
                color = Color(0xFFE65100),
                onClick = {
                    onOpcionSeleccionada(TipoEntrada.EVENTO.name)
                }
            )
            OpcionFab(
                emoji = "📸",
                etiqueta = "Recuerdo",
                color = Color(0xFF7B1FA2),
                onClick = {
                    onOpcionSeleccionada(TipoEntrada.RECUERDO.name)
                }
            )
            OpcionFab(
                emoji = "📝",
                etiqueta = "Mi día",
                color = Color(0xFFC2185B),
                onClick = {
                    onOpcionSeleccionada(TipoEntrada.MI_DIA.name)
                }
            )
        }

        // Botón principal
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = if (expandido) "✕" else "+",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun OpcionFab(
    emoji: String,
    etiqueta: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Etiqueta
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Text(
                text = etiqueta,
                modifier = Modifier.padding(
                    horizontal = 12.dp, vertical = 6.dp
                ),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.width(8.dp))

        // Mini FAB
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
    }
}

//Tarjeta de entrada

@Composable
private fun TarjetaEntrada(
    entrada: EntradaDiario,
    esMia: Boolean,
    onEditar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val tipo = try {
        TipoEntrada.valueOf(entrada.tipo)
    } catch (e: Exception) {
        TipoEntrada.MI_DIA
    }

    val colorTipo = Color(
        android.graphics.Color.parseColor("#${tipo.colorHex}")
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle),  // ← agregar esto
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header de la tarjeta
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Indicador de color del tipo
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorTipo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = tipo.emoji, fontSize = 16.sp)
                    }

                    Column {
                        Text(
                            text = tipo.etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = colorTipo
                        )
                        Text(
                            text = entrada.titulo,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                // Botón editar, solo si es mía
                if (esMia) {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Filled.Edit,
                            contentDescription = "Editar",
                            tint = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            //Emociones
            if (entrada.emociones.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    entrada.emociones.take(5).forEach { emocionNombre ->
                        val emocion = try {
                            com.cadev.mocaapp.feature.diario.domain.model
                                .Emocion.valueOf(emocionNombre)
                        } catch (e: Exception) { null }
                        if (emocion != null) {
                            Text(text = emocion.emoji, fontSize = 18.sp)
                        }
                    }
                }
            }

            //Detalles
            if (entrada.detalles.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entrada.detalles,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.7f),
                    maxLines = 3
                )
            }

            //Badge compartida
            if (entrada.compartida) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "💕", fontSize = 12.sp)
                    Text(
                        text = if (esMia) "Compartida con tu pareja"
                        else "Compartida por tu pareja",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}