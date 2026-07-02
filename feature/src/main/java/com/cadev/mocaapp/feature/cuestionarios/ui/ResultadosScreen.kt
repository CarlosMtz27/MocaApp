package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.SentimentSatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.meshGradientBackground
import com.cadev.mocaapp.feature.cuestionarios.domain.model.TipoPregunta
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultadosScreen(
    viewModel: CuestionarioViewModel,
    cuestionarioId: String,
    usuarioId: String,
    parejaId: String,
    nombreUsuario: String,
    nombrePareja: String,
    fotoUsuario: String,
    fotoPareja: String,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val colorSurface = if (isDark) Color(0xFF1E1B14) else MocaSurface
    val colorOnSurface = if (isDark) Color(0xFFE7BBC6) else MocaOnSurface
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else MocaOnSurfaceVariant
    val colorCardBackground = if (isDark) Color(0xFF2D2921).copy(alpha = 0.9f) else MocaSurfaceContainerLowest

    LaunchedEffect(cuestionarioId) {
        viewModel.cargarResultado(cuestionarioId, usuarioId, parejaId)
    }

    val cuestionario = uiState.cuestionarioActual
    val resultado = uiState.resultado
    val respuestasUsuario = uiState.respuestas
    val respuestasPareja = uiState.respuestasPareja
    val parejaRespondio = respuestasPareja.isNotEmpty()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(
                color = colorSurface,
                shadowElevation = 0.dp,
                modifier = Modifier.statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver", tint = colorOnSurface)
                    }
                    Text(
                        text = "Resultados",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = colorOnSurface
                    )
                    Spacer(Modifier.size(48.dp))
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().meshGradientBackground()) {
            if (cuestionario == null) {
                LoadingTransition()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!parejaRespondio) {
                        item {
                            EnEsperaCard(nombrePareja, colorCardBackground, colorPrimary, colorOnSurfaceVariant)
                        }
                    } else {
                        // Comparación por pregunta
                        itemsIndexed(cuestionario.preguntas) { _, pregunta ->
                            val rU = respuestasUsuario[pregunta.id] ?: "-"
                            val rP = respuestasPareja[pregunta.id] ?: "-"
                            
                            val coinciden: Boolean? = when (pregunta.tipo) {
                                TipoPregunta.FOTO.name -> null
                                TipoPregunta.TEXTO_LIBRE.name -> {
                                    val rUClean = rU.trim().lowercase()
                                    val rPClean = rP.trim().lowercase()
                                    if (rUClean.isNotEmpty() && rPClean.isNotEmpty()) {
                                        rUClean == rPClean
                                    } else false
                                }
                                TipoPregunta.ESCALA.name -> {
                                    val valU = rU.toIntOrNull() ?: 0
                                    val valP = rP.toIntOrNull() ?: 0
                                    if (valU != 0 && valP != 0) {
                                        kotlin.math.abs(valU - valP) <= 2
                                    } else null
                                }
                                else -> rU == rP
                            }

                            ComparacionCard(
                                pregunta = pregunta.texto,
                                rUsuario = rU,
                                rPareja = rP,
                                fotoUsuario = fotoUsuario,
                                fotoPareja = fotoPareja,
                                nombrePareja = nombrePareja,
                                coinciden = coinciden,
                                cardBg = colorCardBackground,
                                onSurface = colorOnSurface,
                                onSurfaceVariant = colorOnSurfaceVariant,
                                primary = colorPrimary,
                                isDark = isDark
                            )
                        }

                        // Card de Resumen Global
                        if (resultado != null) {
                            item {
                                ResumenGlobalCard(resultado.puntajeCompatibilidad, colorCardBackground, colorOnSurface, colorOnSurfaceVariant, colorPrimary, isDark)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnEsperaCard(nombrePareja: String, cardBg: Color, primary: Color, onSurfaceVariant: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(id = com.cadev.mocaapp.feature.R.drawable.ic_reaccion_espera),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Unspecified
            )
            Text(
                "¡Lo lograste!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = primary
            )
            Text(
                "Ahora le toca a $nombrePareja responder. Los resultados aparecerán aquí en cuanto termine.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = onSurfaceVariant
            )
        }
    }
}

@Composable
fun ComparacionCard(
    pregunta: String,
    rUsuario: String,
    rPareja: String,
    fotoUsuario: String,
    fotoPareja: String,
    nombrePareja: String,
    coinciden: Boolean?,
    cardBg: Color,
    onSurface: Color,
    onSurfaceVariant: Color,
    primary: Color,
    isDark: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heartbeat")
    val heartbeatScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1500
                1f at 0
                1.3f at 210
                1f at 420
                1.3f at 630
                1f at 1050
                1f at 1500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MocaSurfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = pregunta,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                color = onSurface,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarConBorde(url = fotoUsuario, bordeColor = if (coinciden == true) MocaPrimaryContainer else onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text("TÚ", style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(text = rUsuario, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (coinciden == true) primary else onSurfaceVariant, textAlign = TextAlign.Center)
                    }

                    Spacer(Modifier.width(48.dp))

                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        AvatarConBorde(url = fotoPareja, bordeColor = if (coinciden == true) MocaPrimaryContainer else onSurfaceVariant.copy(alpha = 0.3f))
                        Spacer(Modifier.height(12.dp))
                        Text(nombrePareja.uppercase(), style = MaterialTheme.typography.labelSmall, color = onSurfaceVariant, letterSpacing = 1.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(text = rPareja, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = if (coinciden == true) primary else onSurfaceVariant, textAlign = TextAlign.Center)
                    }
                }

                Box(modifier = Modifier.offset(y = 20.dp)) {
                    if (coinciden == true) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Surface(
                                modifier = Modifier.size(48.dp).graphicsLayer { scaleX = heartbeatScale; scaleY = heartbeatScale },
                                shape = CircleShape,
                                color = cardBg,
                                shadowElevation = 4.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, onSurfaceVariant.copy(alpha = 0.2f))
                            ) {
                                Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.Favorite, null, tint = MocaError, modifier = Modifier.size(24.dp)) }
                            }
                            Spacer(Modifier.height(8.dp))
                            Surface(color = MocaPrimaryContainer, shape = CircleShape) {
                                Text("COINCIDENCIA", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MocaPrimary, letterSpacing = 1.sp)
                            }
                        }
                    } else if (coinciden == false) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = cardBg,
                            shadowElevation = 2.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, onSurfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Box(contentAlignment = Alignment.Center) { Icon(Icons.Default.SentimentSatisfied, null, tint = MocaSecondary, modifier = Modifier.size(24.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarConBorde(url: String, bordeColor: Color) {
    Box(modifier = Modifier.size(80.dp).clip(CircleShape).background(bordeColor.copy(alpha = 0.1f)).border(4.dp, bordeColor, CircleShape)) {
        AsyncImage(model = url, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun ResumenGlobalCard(porcentaje: Int, cardBg: Color, onSurface: Color, onSurfaceVariant: Color, primary: Color, isDark: Boolean) {
    val sweepAngle by animateFloatAsState(targetValue = (porcentaje / 100f) * 360f, animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing), label = "sweep")

    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 48.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MocaSurfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(32.dp)) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Conexión Global", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = onSurface)
                Spacer(Modifier.height(32.dp))
                Box(contentAlignment = Alignment.Center, modifier = Modifier.size(192.dp)) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(color = onSurfaceVariant.copy(alpha = 0.2f), style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                        drawArc(color = primary, startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round))
                    }
                    Surface(modifier = Modifier.size(128.dp), shape = CircleShape, color = cardBg, shadowElevation = 4.dp, border = androidx.compose.foundation.BorderStroke(1.dp, onSurfaceVariant.copy(alpha = 0.2f))) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
                            Text(text = porcentaje.toString(), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = primary)
                            Text(text = "%", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = onSurfaceVariant, modifier = Modifier.padding(top = 12.dp, start = 2.dp))
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
                val mensajeResumen = when {
                    porcentaje >= 85 -> "¡Su conexión es mágica! Tienen una sintonía increíble."
                    porcentaje >= 70 -> "Tienen una base sólida, pero aún hay áreas interesantes por descubrir juntos."
                    porcentaje >= 50 -> "Están en buen camino, cada test les ayuda a conocerse mejor."
                    else -> "Este test es una gran oportunidad para hablar de estos temas y fortalecer su vínculo."
                }
                Text(text = mensajeResumen, style = MaterialTheme.typography.bodyLarge, color = onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.widthIn(max = 280.dp))
            }
        }
    }
}
