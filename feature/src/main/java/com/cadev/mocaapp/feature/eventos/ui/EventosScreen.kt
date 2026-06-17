package com.cadev.mocaapp.feature.eventos.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
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
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import com.cadev.mocaapp.core.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    viewModel: EventoViewModel,
    relacionId: String,
    onCrearEvento: () -> Unit,
    onVerEvento: (String) -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(relacionId) {
        viewModel.cargarEventos(relacionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("📅 Eventos", fontWeight = FontWeight.Bold) },
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
            FloatingActionButton(onClick = onCrearEvento) {
                Icon(Icons.Filled.Add, "Crear evento")
            }
        }
    ) { padding ->
        if (uiState.cargando) {
            Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val proximos = viewModel.eventosProximos()
        val pasados  = viewModel.eventosPassados()

        if (proximos.isEmpty() && pasados.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("📅", fontSize = 56.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Sin eventos todavía",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Crea el primero con el botón +",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    textAlign = TextAlign.Center
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (proximos.isNotEmpty()) {
                item {
                    Text(
                        "Próximos",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(proximos) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        onClick = { onVerEvento(evento.id) }
                    )
                }
            }

            if (pasados.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Pasados",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
                items(pasados) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        pasado = true,
                        onClick = { onVerEvento(evento.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TarjetaEvento(
    evento: Evento,
    pasado: Boolean = false,
    onClick: () -> Unit
) {
    val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }
    val alpha = if (pasado) 0.5f else 1f

    // Formatear fecha legible
    val formatoEntrada  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoLegible  = SimpleDateFormat("d 'de' MMMM, yyyy", Locale("es", "MX"))
    val fechaLegible = try {
        formatoLegible.format(formatoEntrada.parse(evento.fecha)!!)
    } catch (e: Exception) { evento.fecha }

    // Días restantes
    val diasRestantes = try {
        val hoy = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val eventTime = formatoEntrada.parse(evento.fecha)!!.time
        ((eventTime - hoy) / (1000 * 60 * 60 * 24)).toInt()
    } catch (e: Exception) { 0 }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(if (pasado) 0.dp else 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (pasado)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono tipo
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(tipo.emoji, fontSize = 26.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evento.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$fechaLegible · ${evento.hora}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (evento.descripcion.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = evento.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        maxLines = 1
                    )
                }
            }

            // Badge días restantes
            if (!pasado) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    when {
                        diasRestantes == 0 -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    "HOY",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        diasRestantes == 1 -> {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.secondary
                            ) {
                                Text(
                                    "MAÑANA",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        else -> {
                            Text(
                                text = "$diasRestantes",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "días",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}