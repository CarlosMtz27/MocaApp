package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.cadev.mocaapp.feature.cuestionarios.domain.model.TipoPregunta

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosScreen(
    viewModel: CuestionarioViewModel,
    cuestionarioId: String,
    usuarioId: String,
    parejaId: String,
    nombreUsuario: String,
    nombrePareja: String,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(cuestionarioId) {
        viewModel.iniciarCuestionario(cuestionarioId)
        viewModel.cargarResultado(cuestionarioId, usuarioId, parejaId)
    }

    val cuestionario = uiState.cuestionarioActual
    val resultado = uiState.resultado
    val respuestasUsuario = uiState.respuestas
    val respuestasPareja = uiState.respuestasPareja
    val parejaRespondio = respuestasPareja.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->

        if (cuestionario == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            // Header con puntaje
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(cuestionario.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (!parejaRespondio) {
                            // Pareja no ha respondido
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme
                                        .secondaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("⏳", fontSize = 36.sp)
                                    Text(
                                        "¡Respondiste! Esperando a $nombrePareja...",
                                        style = MaterialTheme.typography.bodyMedium,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme
                                            .onSecondaryContainer
                                    )
                                    Text(
                                        "Los resultados aparecerán cuando ambos completen el cuestionario",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme
                                            .onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        } else if (resultado != null) {
                            // Puntaje de compatibilidad
                            val puntaje = resultado.puntajeCompatibilidad
                            val (emoji, mensaje) = when {
                                puntaje >= 80 -> "💑" to "¡Son muy compatibles!"
                                puntaje >= 60 -> "💕" to "¡Buena compatibilidad!"
                                puntaje >= 40 -> "😊" to "Se complementan bien"
                                else -> "🌱" to "¡Hay mucho por descubrir!"
                            }

                            // Círculo de puntaje
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "$puntaje%",
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    Text(
                                        "match",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimary
                                            .copy(alpha = 0.8f)
                                    )
                                }
                            }

                            Text(emoji, fontSize = 32.sp)
                            Text(
                                mensaje,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            //Comparación pregunta por pregunta
            if (parejaRespondio) {
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Comparación de respuestas",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }

                itemsIndexed(cuestionario.preguntas) { _, pregunta ->
                    val rUsuario = respuestasUsuario[pregunta.id] ?: "-"
                    val rPareja = respuestasPareja[pregunta.id] ?: "-"

                    val coinciden = when (pregunta.tipo) {
                        TipoPregunta.ESCALA.name -> {
                            kotlin.math.abs(
                                (rUsuario.toIntOrNull() ?: 0) -
                                        (rPareja.toIntOrNull() ?: 0)
                            ) <= 2
                        }
                        TipoPregunta.TEXTO_LIBRE.name -> null // sin comparar
                        else -> rUsuario == rPareja
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (coinciden) {
                                true -> Color(0xFF4CAF50).copy(alpha = 0.1f)
                                false -> MaterialTheme.colorScheme.errorContainer
                                    .copy(alpha = 0.3f)
                                null -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    pregunta.texto,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                if (coinciden != null) {
                                    Text(
                                        if (coinciden) "✅" else "❌",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Tu respuesta
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        "Tú",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        rUsuario,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme
                                            .onPrimaryContainer
                                    )
                                }

                                // Respuesta pareja
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        nombrePareja,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        rPareja,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme
                                            .onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}