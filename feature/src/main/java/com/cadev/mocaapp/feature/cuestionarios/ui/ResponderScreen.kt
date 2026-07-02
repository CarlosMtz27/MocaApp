package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.meshGradientBackground
import com.cadev.mocaapp.feature.cuestionarios.domain.model.TipoPregunta
import com.cadev.mocaapp.feature.cuestionarios.ui.components.ExitoCuestionario
import com.cadev.mocaapp.feature.cuestionarios.ui.components.respuestas.*
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.animations.CorazonesOrbitando
import com.cadev.mocaapp.feature.ui.theme.*

@Composable
fun ResponderScreen(
    viewModel: CuestionarioViewModel,
    cuestionarioId: String,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onCompletado: (cuestionarioId: String) -> Unit,
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
        viewModel.iniciarCuestionario(cuestionarioId)
    }

    if (uiState.completado) {
        ExitoCuestionario(
            onVerResumen = { onCompletado(cuestionarioId) },
            onRegresarInicio = onRegresar
        )
        return
    }

    val cuestionario = uiState.cuestionarioActual

    if (uiState.cargando || cuestionario == null) {
        LoadingTransition()
        return
    }

    val preguntas = cuestionario.preguntas
    val indice = uiState.preguntaActual
    val pregunta = preguntas.getOrNull(indice) ?: return
    val respuestaActual = uiState.respuestas[pregunta.id] ?: ""
    val progreso = (indice + 1).toFloat() / preguntas.size.toFloat()
    val esUltima = indice == preguntas.size - 1

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            Surface(color = colorSurface, shadowElevation = 2.dp, modifier = Modifier.statusBarsPadding()) {
                Row(
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = colorPrimary)
                    }
                    Text(
                        text = "Cuestionario",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = colorPrimary
                    )
                    Icon(Icons.Default.Favorite, null, tint = MocaAccentPink, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 12.dp,
                color = colorSurface,
                modifier = Modifier.navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.preguntaAnterior() },
                        enabled = indice > 0,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, colorPrimary.copy(alpha = 0.3f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = colorPrimary)
                    ) {
                        Icon(Icons.Default.ChevronLeft, null)
                        Text("Anterior", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (esUltima) {
                                viewModel.enviarRespuestas(usuarioId, parejaId, relacionId)
                            } else {
                                viewModel.siguientePregunta()
                            }
                        },
                        enabled = (respuestaActual.isNotBlank() || (pregunta.tipo == TipoPregunta.FOTO.name && uiState.respuestasFoto[pregunta.id] != null)) &&
                                !uiState.enviando && !uiState.subiendoFoto,
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MocaAccentPink),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                    ) {
                        if (uiState.enviando) {
                            CorazonesOrbitando(modifier = Modifier.size(40.dp))
                        } else {
                            Text(if (esUltima) "Finalizar" else "Siguiente", fontSize = 16.sp, fontWeight = FontWeight.Black)
                            if (!esUltima) {
                                Spacer(Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().meshGradientBackground()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Barra de Progreso Estilizada
                Column(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "Pregunta ${indice + 1} de ${preguntas.size}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = colorOnSurfaceVariant
                        )
                        Text(
                            "${(progreso * 100).toInt()}%",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colorPrimary
                        )
                    }
                    LinearProgressIndicator(
                        progress = { progreso },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = MocaAccentPink,
                        trackColor = colorPrimary.copy(alpha = 0.2f)
                    )
                }

                // Card de la Pregunta
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .shadow(
                            elevation = if (isDark) 0.dp else 16.dp, 
                            shape = RoundedCornerShape(28.dp), 
                            spotColor = Color.Black.copy(alpha = 0.08f)
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(colorCardBackground)
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(28.dp))
                        .padding(24.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                        Text(
                            text = pregunta.texto,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorOnSurface,
                            lineHeight = 30.sp
                        )

                        if (pregunta.imagenUrl.isNotBlank()) {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                AsyncImage(
                                    model = pregunta.imagenUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxWidth().aspectRatio(16/9f)
                                )
                            }
                        }

                        // Componente de Respuesta con Animación de Deslizamiento
                        AnimatedContent(
                            targetState = pregunta,
                            transitionSpec = {
                                (slideInHorizontally(animationSpec = tween(400)) { it } + fadeIn(animationSpec = tween(400)))
                                    .togetherWith(slideOutHorizontally(animationSpec = tween(400)) { -it } + fadeOut(animationSpec = tween(400)))
                            },
                            label = "RespuestaAnim"
                        ) { targetPregunta ->
                            when (targetPregunta.tipo) {
                                TipoPregunta.OPCION_MULTIPLE.name -> {
                                    RespuestaOpcionMultiple(
                                        opciones = targetPregunta.opciones,
                                        respuestaSeleccionada = respuestaActual,
                                        onRespuestaSelected = { viewModel.responderPregunta(targetPregunta.id, it) }
                                    )
                                }
                                TipoPregunta.TEXTO_LIBRE.name -> {
                                    RespuestaTextoLibre(
                                        texto = respuestaActual,
                                        onTextoChanged = { viewModel.responderPregunta(targetPregunta.id, it) }
                                    )
                                }
                                TipoPregunta.SI_NO.name -> {
                                    RespuestaSiNo(
                                        respuesta = respuestaActual,
                                        onRespuestaSelected = { viewModel.responderPregunta(targetPregunta.id, it) }
                                    )
                                }
                                TipoPregunta.ESCALA.name -> {
                                    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                                        RespuestaEscala(
                                            valor = respuestaActual.toIntOrNull() ?: 0,
                                            onValorChanged = { viewModel.responderPregunta(targetPregunta.id, it.toString()) }
                                        )
                                        
                                        // Texto Opcional Mejorado
                                        RespuestaTextoLibre(
                                            texto = uiState.comentarios[targetPregunta.id] ?: "",
                                            onTextoChanged = { viewModel.guardarComentario(targetPregunta.id, it) },
                                            placeholder = "¿Algún pensamiento adicional? (Opcional)",
                                            maxChars = 300
                                        )
                                    }
                                }
                                TipoPregunta.FOTO.name -> {
                                    RespuestaFoto(
                                        fotoUrl = uiState.respuestasFoto[targetPregunta.id],
                                        subiendo = uiState.subiendoFoto,
                                        onFotoSeleccionada = { viewModel.subirFotoRespuesta(targetPregunta.id, it) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(Modifier.height(100.dp))
            }
        }
    }
}
