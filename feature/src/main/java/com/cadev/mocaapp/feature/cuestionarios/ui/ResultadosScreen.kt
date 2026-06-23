package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.cuestionarios.domain.model.TipoPregunta

/**
 * ESTA ES LA PANTALLA DE RESULTADOS DEL TEST
 * 
 * Qué hace:
 * Muestra el porcentaje de match que sacamos con nuestra pareja. Compara las 
 * respuestas de ambos lado a lado para ver en qué coincidimos y en qué no, 
 * usando colores verdes para los aciertos y rojos para las diferencias.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar los mensajes de compatibilidad (ej: "¡Son almas gemelas!"), 
 * debemos editar la lógica del `when` donde se calcula el `iconRes` y el `mensaje`.
 */
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
        // Cargar cuestionario si viene directo de ResponderScreen
        if (uiState.cuestionarioActual?.id != cuestionarioId) {
            viewModel.iniciarCuestionario(cuestionarioId)
        }
        viewModel.cargarResultado(cuestionarioId, usuarioId, parejaId)
    }

    val cuestionario = uiState.cuestionarioActual
    val resultado = uiState.resultado
    val respuestasUsuario = uiState.respuestas
    val respuestasPareja = uiState.respuestasPareja
    val fotoUsuario = uiState.respuestasFoto
    val fotoPareja = uiState.respuestasFotoPareja
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

            //Header
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
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            cuestionario.titulo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )

                        if (!parejaRespondio) {
                            //Pareja aún no ha respondido
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_espera),
                                        contentDescription = null,
                                        tint = Color.Unspecified,
                                        modifier = Modifier.size(48.dp)
                                    )
                                    Text(
                                        "¡Lo lograste! Respondiste el cuestionario",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFFE65100)
                                    )
                                    Text(
                                        "Ahora le toca a $nombrePareja responder.\nLos resultados aparecerán aquí en cuanto termine.",
                                        style = MaterialTheme.typography.bodySmall,
                                        textAlign = TextAlign.Center,
                                        color = Color(0xFFBF360C)
                                    )
                                }
                            }
                        } else if (resultado != null) {
                            // ← Resultado con porcentaje
                            val puntaje = resultado.puntajeCompatibilidad
                            val (iconRes, mensaje) = when {
                                puntaje >= 80 -> com.cadev.mocaapp.feature.R.drawable.ic_corazon to "¡Son muy compatibles!"
                                puntaje >= 60 -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_corazon to "¡Buena compatibilidad!"
                                puntaje >= 40 -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_risa to "Se complementan bien"
                                else -> com.cadev.mocaapp.feature.R.drawable.ic_reaccion_semilla to "¡Hay mucho por descubrir!"
                            }

                            Box(
                                modifier = Modifier
                                    .size(130.dp)
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

                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(48.dp)
                            )
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
                    val esFoto = pregunta.tipo == TipoPregunta.FOTO.name
                    val esTexto = pregunta.tipo == TipoPregunta.TEXTO_LIBRE.name

                    val coinciden: Boolean? = when {
                        esFoto || esTexto -> null
                        pregunta.tipo == TipoPregunta.ESCALA.name -> {
                            kotlin.math.abs(
                                (rUsuario.toIntOrNull() ?: 0) -
                                        (rPareja.toIntOrNull() ?: 0)
                            ) <= 2
                        }
                        else -> rUsuario == rPareja
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (coinciden) {
                                true -> Color(0xFF4CAF50).copy(alpha = 0.08f)
                                false -> MaterialTheme.colorScheme.errorContainer
                                    .copy(alpha = 0.2f)
                                null -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Texto de la pregunta mas imagen opcional
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        pregunta.texto,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (pregunta.imagenUrl.isNotBlank()) {
                                        Spacer(Modifier.height(8.dp))
                                        AsyncImage(
                                            model = pregunta.imagenUrl,
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(100.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    }
                                }
                                if (coinciden != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(
                                        imageVector = if (coinciden) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                        contentDescription = null,
                                        tint = if (coinciden) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            //Respuestas lado a lado
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
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        "Tú",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    if (esFoto) {
                                        val url = fotoUsuario[pregunta.id]
                                        if (url != null) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        } else {
                                            Text(
                                                "Sin foto",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme
                                                    .onPrimaryContainer
                                                    .copy(alpha = 0.5f)
                                            )
                                        }
                                    } else {
                                        Text(
                                            rUsuario,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme
                                                .onPrimaryContainer,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }

                                // Respuesta pareja
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        nombrePareja,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(Modifier.height(6.dp))
                                    if (esFoto) {
                                        val url = fotoPareja[pregunta.id]
                                        if (url != null) {
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(100.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                            )
                                        } else {
                                            Text(
                                                "Sin foto",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme
                                                    .onSecondaryContainer
                                                    .copy(alpha = 0.5f)
                                            )
                                        }
                                    } else {
                                        Text(
                                            rPareja,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme
                                                .onSecondaryContainer,
                                            textAlign = TextAlign.Center
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
}