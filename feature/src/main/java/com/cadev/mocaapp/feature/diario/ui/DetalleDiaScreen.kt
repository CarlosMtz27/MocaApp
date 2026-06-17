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
import androidx.compose.ui.text.font.FontWeight
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

    LaunchedEffect(fecha) {
        viewModel.cargarEntradasDelDia(usuarioId, parejaId, fecha)
    }

    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoVisible = SimpleDateFormat("EEEE d 'de' MMMM, yyyy", Locale("es", "MX"))
    val fechaVisible = try {
        formatoVisible.format(formatoEntrada.parse(fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { fecha }

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
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
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.entradas.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📅", fontSize = 56.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "No hay nada registrado\npara este día",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Usa el botón + para agregar\nalgo especial",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            val entradasOrdenadas = uiState.entradas
                .sortedByDescending { it.creadaEn }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(entradasOrdenadas) { entrada ->
                    val noVista = entrada.usuarioId != usuarioId &&
                            !viewModel.esEntradaVista(entrada.id)

                    TarjetaEntrada(
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

        if (fabExpandido) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { fabExpandido = false }
            )
        }
    }
}

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
        if (expandido) {
            OpcionFab("📸", "Recuerdo", Color(0xFF7B1FA2)) {
                onOpcionSeleccionada(TipoEntrada.RECUERDO.name)
            }
            OpcionFab("📝", "Mi día", Color(0xFFC2185B)) {
                onOpcionSeleccionada(TipoEntrada.MI_DIA.name)
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                if (expandido) "✕" else "+",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun OpcionFab(emoji: String, etiqueta: String, color: Color, onClick: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = Color.White, shadowElevation = 4.dp) {
            Text(etiqueta, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
        }
        Spacer(Modifier.width(8.dp))
        SmallFloatingActionButton(onClick = onClick, containerColor = color) {
            Text(emoji, fontSize = 18.sp)
        }
    }
}

@Composable
private fun TarjetaEntrada(
    entrada: EntradaDiario,
    esMia: Boolean,
    noVista: Boolean,
    onEditar: () -> Unit,
    onVerDetalle: () -> Unit
) {
    val tipo = try { TipoEntrada.valueOf(entrada.tipo) } catch (e: Exception) { TipoEntrada.MI_DIA }
    val colorTipo = Color(android.graphics.Color.parseColor("#${tipo.colorHex}"))

    val formatoHora = SimpleDateFormat("HH:mm", Locale.getDefault())
    val hora = try { formatoHora.format(entrada.creadaEn) } catch (e: Exception) { "" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onVerDetalle),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (noVista) 4.dp else 2.dp),
        border = if (noVista)
            androidx.compose.foundation.BorderStroke(
                2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colorTipo.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) { Text(tipo.emoji, fontSize = 16.sp) }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = tipo.etiqueta,
                                style = MaterialTheme.typography.labelSmall,
                                color = colorTipo
                            )
                            Text(
                                text = hora,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                            )
                        }
                        Text(
                            text = entrada.titulo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = if (noVista) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                if (esMia) {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Filled.Edit, "Editar",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            if (entrada.emociones.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    entrada.emociones.take(5).forEach { emocionNombre ->
                        val emocion = try {
                            com.cadev.mocaapp.feature.diario.domain.model
                                .Emocion.valueOf(emocionNombre)
                        } catch (e: Exception) { null }
                        if (emocion != null) Text(emocion.emoji, fontSize = 18.sp)
                    }
                }
            }

            if (entrada.detalles.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = entrada.detalles,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 3
                )
            }

            if (entrada.compartida) {
                Spacer(Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text("💕", fontSize = 12.sp)
                    Text(
                        if (esMia) "Compartida con tu pareja" else "Compartida por tu pareja",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (noVista) {
                Spacer(Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        "● Nuevo",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}
