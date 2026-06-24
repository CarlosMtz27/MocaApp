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
import androidx.compose.ui.platform.LocalContext
import com.cadev.mocaapp.feature.eventos.domain.model.Evento
import androidx.compose.ui.res.painterResource
import com.cadev.mocaapp.core.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.*

/**
 * ESTA ES LA PANTALLA DEL LISTADO DE EVENTOS
 * 
 * Qué hace
 * Muestra todos los planes compartidos de la pareja organizados en dos secciones: los que están 
 * por venir y los que ya han pasado. Permite añadir nuevos planes y ver el detalle de cada uno.
 */
/**
 * ESTA ES NUESTRA AGENDA DE PAREJA
 * 
 * Qué hace:
 * Aquí mostramos todas las citas y planes que tenemos juntos. Dividimos la lista 
 * en "Próximos" para lo que está por venir y "Pasados" para recordar lo que 
 * ya hicimos.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar cómo se ven las tarjetas de las citas, debemos buscar 
 * la función `TarjetaEvento` y ajustar sus colores o bordes.
 */
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
    val context = LocalContext.current

    /**
     * Se activa la escucha en tiempo real de los eventos al entrar en la pantalla
     */
    LaunchedEffect(relacionId) {
        viewModel.iniciarEscucha(context, relacionId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_calendario),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Eventos", fontWeight = FontWeight.Bold)
                    }
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

        /**
         * Muestra una pantalla vacía si no hay ninguna cita registrada
         */
        if (proximos.isEmpty() && pasados.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
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
            /**
             * Listado de las próximas citas con un indicador de cuántos días faltan
             */
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

            /**
             * Listado de eventos antiguos que se muestran con un tono más claro
             */
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

/**
 * Función que crea la tarjeta visual de cada plan indicando su nombre fecha hora y días restantes
 */
@Composable
fun TarjetaEvento(
    evento: Evento,
    pasado: Boolean = false,
    onClick: () -> Unit
) {
    val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }
    val alpha = if (pasado) 0.5f else 1f

    val formatoEntrada  = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoLegible  = SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX"))
    val fechaLegible = try {
        formatoLegible.format(formatoEntrada.parse(evento.fecha)!!)
    } catch (e: Exception) { evento.fecha }

    /**
     * Calcula la diferencia de tiempo entre el día de hoy y la fecha de la cita
     */
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
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = alpha)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tipo.icono,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                )
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

            /**
             * Muestra un aviso de hoy mañana o el número de días que faltan para la cita
             */
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
