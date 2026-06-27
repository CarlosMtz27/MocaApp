package com.cadev.mocaapp.feature.auth.ui

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
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.auth.ui.components.*

/**
 * PANTALLA DE INICIO DE SESIÓN
 */
@Composable
fun PantallaLogin(
    viewModel: AuthViewModel,
    alHacerLoginExitoso: () -> Unit,
    alIrARegistro: () -> Unit
) {
    val estadoUi by viewModel.uiState.collectAsState()
    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }

    LaunchedEffect(estadoUi.exitoso) {
        if (estadoUi.exitoso) alHacerLoginExitoso()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFF8EF),
                        Color(0xFFFFDAB9).copy(alpha = 0.3f),
                        Color(0xFFFFD1DC).copy(alpha = 0.3f)
                    )
                )
            )
    ) {
        DecoracionFondoZen()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CabeceraAutenticacion(alVolver = { /* Cerrar app si se desea */ })
            },
            bottomBar = {
                NavegacionInferiorAutenticacion(esLogin = true, alNavegar = { esLogin ->
                    if (!esLogin) alIrARegistro()
                })
            }
        ) { rellenosPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(rellenosPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                
                SeccionLogo(estaCentrado = true)
                
                Spacer(modifier = Modifier.height(32.dp))
                
                TarjetaCristal(
                    modificador = Modifier.fillMaxWidth().padding(bottom = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CampoTextoCristal(
                            valor = correo,
                            alCambiarValor = { correo = it },
                            etiqueta = "Correo Electrónico",
                            sugerencia = "hola@mocaapp.com"
                        )
                        
                        CampoTextoContrasena(
                            valor = contrasena,
                            alCambiarValor = { contrasena = it }
                        )
                        
                        if (estadoUi.error != null) {
                            Text(
                                text = estadoUi.error!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        BotonAutenticacion(
                            texto = "Entrar",
                            alHacerClick = { viewModel.login(correo, contrasena) },
                            estaCargando = estadoUi.cargando
                        )
                        
                        TextButton(
                            onClick = alIrARegistro,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(
                                "¿Nuevo aquí? Crea una cuenta",
                                color = Color(0xFF78555E).copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
