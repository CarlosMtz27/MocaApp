package com.cadev.mocaapp.feature.pareja.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class EstadoBotonTeAmo {
    REPOSO, CARGANDO, LLENANDO, LATIENDO, LANZAMIENTO, EXPLOSION, MENSAJE
}

data class Particula(
    val id: Int,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val tamano: Float,
    val rotacion: Float,
    val vr: Float,
    val vida: Float = 1f,
    val esCorazon: Boolean = false,
    val esRastro: Boolean = false
)

@Composable
fun BotonTeAmoAnimado(
    onEnviar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var estado by remember { mutableStateOf(EstadoBotonTeAmo.REPOSO) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val config = LocalConfiguration.current
    
    val screenWidth = with(density) { config.screenWidthDp.dp.toPx() }
    val screenHeight = with(density) { config.screenHeightDp.dp.toPx() }
    val size70px = with(density) { 70.dp.toPx() }
    val halfSize = size70px / 2

    var rectBoton by remember { mutableStateOf(androidx.compose.ui.geometry.Rect.Zero) }
    val particulas = remember { mutableStateListOf<Particula>() }
    
    val transition = updateTransition(targetState = estado, label = "TeAmoTransition")
    val anchoBoton by transition.animateDp(
        transitionSpec = { tween(600, easing = CubicBezierEasing(0.65f, -0.1f, 0.25f, 1f)) },
        label = "ancho"
    ) { state -> if (state == EstadoBotonTeAmo.REPOSO) 340.dp else 70.dp }
    
    val radioBoton by transition.animateDp(label = "radio") { state ->
        if (state == EstadoBotonTeAmo.REPOSO) 40.dp else 100.dp
    }

    val alturaLlenado = remember { Animatable(0f) }
    val escalaLatido = remember { Animatable(1f) }
    val offsetLanzamiento = remember { Animatable(Offset.Zero, Offset.VectorConverter) }
    val escalaLanzamiento = remember { Animatable(1f) }

    LaunchedEffect(Unit) {
        while (true) {
            if (particulas.isNotEmpty()) {
                val iterator = particulas.listIterator()
                while (iterator.hasNext()) {
                    val p = iterator.next()
                    val decremento = if (p.esRastro) 0.04f else 0.015f
                    val nuevaVida = p.vida - decremento
                    if (nuevaVida <= 0f) iterator.remove()
                    else iterator.set(p.copy(x = p.x + p.vx, y = p.y + p.vy, rotacion = p.rotacion + p.vr, vida = nuevaVida))
                }
            }
            delay(16)
        }
    }

    Box(
        modifier = modifier
            .onGloballyPositioned { coordinates ->
                val pos = coordinates.positionInWindow()
                rectBoton = androidx.compose.ui.geometry.Rect(pos, androidx.compose.ui.geometry.Size(coordinates.size.width.toFloat(), coordinates.size.height.toFloat()))
            }
            .height(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = {
                if (estado == EstadoBotonTeAmo.REPOSO) {
                    scope.launch {
                        estado = EstadoBotonTeAmo.CARGANDO
                        delay(650)
                        estado = EstadoBotonTeAmo.LLENANDO
                        onEnviar()
                        alturaLlenado.animateTo(1f, tween(1000))
                        estado = EstadoBotonTeAmo.LATIENDO
                        repeat(3) {
                            escalaLatido.animateTo(1.2f, tween(150))
                            escalaLatido.animateTo(0.95f, tween(150))
                            escalaLatido.animateTo(1.15f, tween(150))
                            escalaLatido.animateTo(1f, tween(250))
                        }
                        
                        estado = EstadoBotonTeAmo.LANZAMIENTO
                        val centerBtnX = rectBoton.left + (rectBoton.width / 2)
                        val centerBtnY = rectBoton.top + (rectBoton.height / 2)
                        val targetX = (screenWidth / 2) - centerBtnX
                        val targetY = (screenHeight / 2) - centerBtnY
                        
                        val launchJob = launch {
                            offsetLanzamiento.animateTo(
                                Offset(targetX, targetY),
                                tween(1800, easing = CubicBezierEasing(0.18f, 0.8f, 0.2f, 1f))
                            )
                        }
                        launch { escalaLanzamiento.animateTo(0.45f, tween(1800)) }
                        
                        repeat(50) {
                            val rX = centerBtnX + offsetLanzamiento.value.x
                            val rY = centerBtnY + offsetLanzamiento.value.y
                            particulas.add(Particula(
                                id = Random.nextInt(),
                                x = rX, y = rY,
                                vx = (Random.nextFloat() - 0.5f) * 3f,
                                vy = (Random.nextFloat() - 0.5f) * 3f,
                                color = Color(0xFFFFC3D3),
                                tamano = 8f + Random.nextFloat() * 6f,
                                rotacion = 0f, vr = 0f,
                                esRastro = true
                            ))
                            delay(35)
                        }
                        
                        launchJob.join()
                        estado = EstadoBotonTeAmo.EXPLOSION
                        crearRafaga(particulas, screenWidth / 2, screenHeight / 2, 80, true)
                        delay(300)
                        crearRafaga(particulas, screenWidth / 2, screenHeight / 2, 100, false)
                        estado = EstadoBotonTeAmo.MENSAJE
                        delay(6000)
                        estado = EstadoBotonTeAmo.REPOSO
                        alturaLlenado.snapTo(0f)
                        offsetLanzamiento.snapTo(Offset.Zero)
                        escalaLanzamiento.snapTo(1f)
                    }
                }
            },
            modifier = Modifier
                .width(anchoBoton)
                .height(58.dp)
                .alpha(if (estado == EstadoBotonTeAmo.REPOSO || estado == EstadoBotonTeAmo.CARGANDO) 1f else 0f)
                .shadow(elevation = 10.dp, shape = RoundedCornerShape(radioBoton)),
            shape = RoundedCornerShape(radioBoton),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (estado == EstadoBotonTeAmo.REPOSO) Color(0xFFFFD1DD) else Color.White
            ),
            contentPadding = PaddingValues(0.dp),
            enabled = estado == EstadoBotonTeAmo.REPOSO
        ) {
            if (estado == EstadoBotonTeAmo.REPOSO) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("❤", fontSize = 22.sp, color = Color(0xFF7D6065))
                    Spacer(Modifier.width(12.dp))
                    Text("Enviar un Te Amo", fontSize = 17.sp, color = Color(0xFF7D6065), fontWeight = FontWeight.SemiBold)
                }
            } else {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color(0xFFFFD1DD), strokeWidth = 3.dp)
            }
        }
    }

    if (estado != EstadoBotonTeAmo.REPOSO) {
        Popup(
            alignment = Alignment.TopStart,
            properties = PopupProperties(focusable = false)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (estado == EstadoBotonTeAmo.MENSAJE) {
                    Box(modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.7f)).blur(8.dp))
                }

                particulas.forEach { p ->
                    Text(
                        text = "❤",
                        color = p.color.copy(alpha = p.vida),
                        fontSize = p.tamano.sp,
                        modifier = Modifier
                            .offset { IntOffset(p.x.toInt(), p.y.toInt()) }
                            .rotate(p.rotacion)
                            .graphicsLayer(scaleX = p.vida, scaleY = p.vida)
                    )
                }

                if (estado in listOf(EstadoBotonTeAmo.LLENANDO, EstadoBotonTeAmo.LATIENDO, EstadoBotonTeAmo.LANZAMIENTO)) {
                    val startX = rectBoton.left + (rectBoton.width / 2) - halfSize
                    val startY = rectBoton.top + (rectBoton.height / 2) - halfSize
                    val currentX = startX + offsetLanzamiento.value.x
                    val currentY = startY + offsetLanzamiento.value.y
                    
                    Box(
                        modifier = Modifier
                            .offset { IntOffset(currentX.toInt(), currentY.toInt()) }
                            .scale(escalaLanzamiento.value * escalaLatido.value)
                            .size(70.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val heartPath = Path().apply {
                                moveTo(w / 2f, h * 0.35f)
                                cubicTo(w * 0.1f, h * 0.05f, -w * 0.1f, h * 0.6f, w / 2f, h * 0.95f)
                                cubicTo(w * 1.1f, h * 0.6f, w * 0.9f, h * 0.05f, w / 2f, h * 0.35f)
                                close()
                            }
                            drawPath(heartPath, Color(0xFFFF5B79))
                            clipRect(top = size.height * (1f - alturaLlenado.value)) {
                                drawPath(heartPath, Color(0xFFFF1039))
                            }
                        }
                    }
                }

                AnimatedVisibility(
                    visible = estado == EstadoBotonTeAmo.MENSAJE,
                    enter = fadeIn(tween(1000)) + slideInVertically { 40 },
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "Te amo enviado", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF2D55), textAlign = androidx.compose.ui.text.style.TextAlign.Center, lineHeight = 56.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(text = "Compartir amor es sembrar pequeños instantes de felicidad que florecen en el corazón.", fontSize = 20.sp, color = Color(0xFF4F4446), fontWeight = FontWeight.Medium, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            }
        }
    }
}

private fun crearRafaga(particulas: MutableList<Particula>, x: Float, y: Float, cantidad: Int, corazones: Boolean) {
    repeat(cantidad) {
        val angulo = Random.nextFloat() * 2 * Math.PI.toFloat()
        val dist = 5f + Random.nextFloat() * 15f
        particulas.add(Particula(
            id = Random.nextInt(),
            x = x, y = y,
            vx = cos(angulo) * dist,
            vy = sin(angulo) * dist,
            color = if (corazones) Color(0xFFFF2D55) else Color.White,
            tamano = if (corazones) 16f + Random.nextFloat() * 20f else 4f + Random.nextFloat() * 6f,
            rotacion = Random.nextFloat() * 360f,
            vr = (Random.nextFloat() - 0.5f) * 20f,
            esCorazon = corazones
        ))
    }
}
