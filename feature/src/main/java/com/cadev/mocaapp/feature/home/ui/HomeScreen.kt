package com.cadev.mocaapp.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.NavRoutes
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.ui.CuestionarioViewModel
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.eventos.ui.EventoViewModel
import com.cadev.mocaapp.feature.notificaciones.ui.NotificacionViewModel
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel
import com.cadev.mocaapp.core.model.TipoEvento
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    perfilViewModel: PerfilViewModel,
    eventoViewModel: EventoViewModel,
    diarioViewModel: DiarioViewModel,
    cuestionarioViewModel: CuestionarioViewModel,
    notificacionViewModel: NotificacionViewModel,
    onNavigateToTab: (String) -> Unit,
    onNavigateToScreen: (String) -> Unit
) {
    val perfilState by perfilViewModel.uiState.collectAsState()
    val diarioState by diarioViewModel.uiState.collectAsState()
    val cuestionarioState by cuestionarioViewModel.uiState.collectAsState()

    val usuario = perfilState.usuario
    val pareja = perfilState.pareja
    val tienePareja = pareja != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Saludo y Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Hola, ${usuario?.nombre ?: "Usuario"} 👋",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "¡Qué alegría verte de nuevo!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            AsyncImage(
                model = usuario?.fotoPerfil,
                contentDescription = "Mi perfil",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentScale = ContentScale.Crop
            )
        }

        if (!tienePareja) {
            TarjetaSinPareja(miCodigo = usuario?.codigoPareja ?: "")
        } else {
            // Dashboard con pareja
            CardDiasJuntos(diasJuntos = perfilState.diasJuntos)

            // Accesos rápidos
            AccesosRapidos(
                onNuevaEntrada = { 
                    val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    onNavigateToScreen(NavRoutes.CrearEntrada.crearRuta(hoy, TipoEntrada.MI_DIA.name))
                },
                onNuevoEvento = { onNavigateToScreen(NavRoutes.CrearEvento.route) },
                onChat = { onNavigateToTab(NavRoutes.Chat.route) },
                onCuestionarios = { onNavigateToTab(NavRoutes.Cuestionarios.route) }
            )

            // Próximo evento
            val proximoEvento = eventoViewModel.eventosProximos().firstOrNull()
            SeccionProximoEvento(
                evento = proximoEvento,
                onVerTodos = { onNavigateToScreen(NavRoutes.Eventos.route) },
                onCrear = { onNavigateToScreen(NavRoutes.CrearEvento.route) },
                onVerDetalle = { id -> onNavigateToScreen(NavRoutes.DetalleEvento.crearRuta(id)) }
            )

            // Última actividad del diario
            val ultimaEntrada = diarioState.ultimasEntradas.firstOrNull()
            SeccionUltimaActividad(
                entrada = ultimaEntrada,
                onVerDiario = { onNavigateToTab(NavRoutes.Calendario.route) },
                onVerDetalle = { id -> onNavigateToScreen(NavRoutes.DetalleEntrada.crearRuta(id)) }
            )

            // Resumen de cuestionarios
            SeccionCuestionarios(
                cuestionarios = cuestionarioState.historial,
                estados = cuestionarioState.estadosCuestionarios,
                onVerCuestionarios = { onNavigateToTab(NavRoutes.Cuestionarios.route) },
                onResponder = { id -> onNavigateToScreen(NavRoutes.ResponderCuestionario.crearRuta(id)) }
            )
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CardDiasJuntos(diasJuntos: Long) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "❤️", fontSize = 32.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (diasJuntos >= 0) "$diasJuntos" else "--",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "días compartiendo sueños",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TarjetaSinPareja(miCodigo: String) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Filled.FavoriteBorder, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "¡Vincúlate con tu pareja!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Comparte este código para empezar a crear recuerdos juntos:",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = miCodigo,
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp
                )
            }
        }
    }
}

@Composable
private fun AccesosRapidos(
    onNuevaEntrada: () -> Unit,
    onNuevoEvento: () -> Unit,
    onChat: () -> Unit,
    onCuestionarios: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AccesoItem(icon = Icons.Filled.EditNote, label = "Diario", color = Color(0xFFC2185B), modifier = Modifier.weight(1f), onClick = onNuevaEntrada)
            AccesoItem(icon = Icons.Filled.Event, label = "Evento", color = Color(0xFFE65100), modifier = Modifier.weight(1f), onClick = onNuevoEvento)
            AccesoItem(icon = Icons.Filled.ChatBubbleOutline, label = "Chat", color = Color(0xFF1976D2), modifier = Modifier.weight(1f), onClick = onChat)
            AccesoItem(icon = Icons.AutoMirrored.Filled.Assignment, label = "Test", color = Color(0xFF388E3C), modifier = Modifier.weight(1f), onClick = onCuestionarios)
        }
    }
}

@Composable
private fun AccesoItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SeccionProximoEvento(
    evento: com.cadev.mocaapp.feature.eventos.domain.model.Evento?,
    onVerTodos: () -> Unit,
    onCrear: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Próximo evento", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerTodos) { Text("Ver todos") }
        }

        if (evento == null) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onCrear),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.primary)
                    Text("No hay eventos próximos. ¡Crea uno!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        } else {
            val tipo = try { TipoEvento.valueOf(evento.tipo) } catch (e: Exception) { TipoEvento.OTRO }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onVerDetalle(evento.id) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(tipo.emoji, fontSize = 32.sp)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(evento.titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${evento.fecha} · ${evento.hora}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    }
                    Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
private fun SeccionUltimaActividad(
    entrada: com.cadev.mocaapp.feature.diario.domain.model.EntradaDiario?,
    onVerDiario: () -> Unit,
    onVerDetalle: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Última actividad", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerDiario) { Text("Ir al diario") }
        }

        if (entrada == null) {
            Text(
                "Aún no han escrito nada en su diario. ¡Empiecen hoy!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            val tipo = try { TipoEntrada.valueOf(entrada.tipo) } catch (e: Exception) { TipoEntrada.MI_DIA }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onVerDetalle(entrada.id) },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(tipo.emoji, fontSize = 20.sp)
                        Text(entrada.titulo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    if (entrada.detalles.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(entrada.detalles, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(entrada.fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
private fun SeccionCuestionarios(
    cuestionarios: List<com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario>,
    estados: Map<String, EstadoCuestionario>,
    onVerCuestionarios: () -> Unit,
    onResponder: (String) -> Unit
) {
    val pendientes = estados.filter { it.value == EstadoCuestionario.PAREJA_RESPONDIÓ }.keys
    val completados = cuestionarios.size // Simplificado
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Cuestionarios", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            TextButton(onClick = onVerCuestionarios) { Text("Ver todos") }
        }

        if (pendientes.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onResponder(pendientes.first()) },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Filled.NotificationImportant, null, tint = MaterialTheme.colorScheme.secondary)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¡Tienes uno pendiente!", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        Text("Tu pareja ya respondió, ¡falta tú!", style = MaterialTheme.typography.bodySmall)
                    }
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = MaterialTheme.colorScheme.secondary)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completados", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Completados", style = MaterialTheme.typography.labelSmall)
                }
                VerticalDivider(modifier = Modifier.height(40.dp))
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    // Aquí podrías calcular el promedio real si lo tuvieras
                    Text("85%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Compatibilidad", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
