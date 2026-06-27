package com.cadev.mocaapp.feature.auth.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.components.*

/**
 * PANTALLA DE REGISTRO
 */
@Composable
fun PantallaRegistro(
    viewModel: AuthViewModel,
    alHacerRegistroExitoso: () -> Unit,
    alIrALogin: () -> Unit
) {
    val estadoUi by viewModel.uiState.collectAsState()
    
    var nombre by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var confirmarContrasena by remember { mutableStateOf("") }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(estadoUi.exitoso) {
        if (estadoUi.exitoso) alHacerRegistroExitoso()
    }

    // Animación de flotación para la tarjeta
    val transicionInfinita = rememberInfiniteTransition(label = "flotacion")
    val desplazamientoY by transicionInfinita.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "desplazamientoY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8EF),
                        Color(0xFFFFDAB9).copy(alpha = 0.3f),
                        Color(0xFFD1E9CD).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        DecoracionFondoZen()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CabeceraAutenticacion(alVolver = alIrALogin)
            },
            bottomBar = {
                NavegacionInferiorAutenticacion(esLogin = false, alNavegar = { esLogin ->
                    if (esLogin) alIrALogin()
                })
            }
        ) { rellenosPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(rellenosPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                SeccionLogo(
                    titulo = "Únete a nosotros",
                    subtitulo = "Comienza tu viaje hacia la tranquilidad digital",
                    estaCentrado = false
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TarjetaCristal(
                    modificador = Modifier
                        .fillMaxWidth()
                        .offset(y = desplazamientoY.dp)
                        .padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CampoTextoCristal(
                            valor = nombre,
                            alCambiarValor = { nombre = it },
                            etiqueta = "Nombre Completo",
                            sugerencia = "Janice Miller"
                        )

                        CampoTextoCristal(
                            valor = correo,
                            alCambiarValor = { correo = it },
                            etiqueta = "Correo Electrónico",
                            sugerencia = "hola@mocaapp.com"
                        )
                        
                        CampoTextoContrasena(
                            valor = contrasena,
                            alCambiarValor = { contrasena = it },
                            etiqueta = "Contraseña"
                        )

                        CampoTextoContrasena(
                            valor = confirmarContrasena,
                            alCambiarValor = { confirmarContrasena = it },
                            etiqueta = "Confirmar Contraseña"
                        )

                        SeccionValidacion(
                            contrasena = contrasena,
                            confirmarContrasena = confirmarContrasena
                        )
                        
                        val errorAMostrar = errorLocal ?: estadoUi.error
                        if (errorAMostrar != null) {
                            Text(
                                text = errorAMostrar,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        BotonAutenticacion(
                            texto = "Crear Cuenta",
                            alHacerClick = {
                                errorLocal = when {
                                    contrasena != confirmarContrasena -> "Las contraseñas no coinciden"
                                    contrasena.length < 8 -> "La contraseña debe tener al menos 8 caracteres"
                                    else -> null
                                }
                                if (errorLocal == null) {
                                    viewModel.registrar(correo, contrasena, nombre)
                                }
                            },
                            estaCargando = estadoUi.cargando
                        )
                        
                        Text(
                            text = "Al registrarte, aceptas nuestros Términos de Servicio.",
                            fontSize = 12.sp,
                            color = Color(0xFF4F4446).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }
        }
    }
}
