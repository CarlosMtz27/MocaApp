package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.components.*
import com.cadev.mocaapp.feature.auth.ui.HeartLogo

/**
 * PANTALLA PARA VINCULAR PAREJA
 */
@Composable
fun PantallaVincularPareja(
    viewModel: ParejaViewModel,
    usuarioId: String,
    alVincular: (relacionId: String) -> Unit,
    alVolver: () -> Unit
) {
    val estadoUi by viewModel.uiState.collectAsState()
    val portapapeles = LocalClipboardManager.current
    var codigoIngresado by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.cargarMiCodigo(usuarioId)
    }

    LaunchedEffect(estadoUi.vinculado) {
        if (estadoUi.vinculado) {
            alVincular(estadoUi.relacionId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF8EF), Color(0xFFFFD9E2))
                )
            )
    ) {
        DecoracionFondoZen()

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CabeceraAutenticacion(titulo = "Vincular Pareja", alVolver = alVolver)
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
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Comparte tu código único con tu pareja para comenzar su viaje juntos.",
                    fontSize = 16.sp,
                    color = Color(0xFF4F4446).copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(40.dp))

                TarjetaCristal(
                    modificador = Modifier.fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "TU CÓDIGO DE VINCULACIÓN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4F4446).copy(alpha = 0.6f),
                            letterSpacing = 2.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = formatearCodigo(estadoUi.miCodigo),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF78555E),
                                letterSpacing = 4.sp
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            IconButton(
                                onClick = { portapapeles.setText(AnnotatedString(estadoUi.miCodigo)) },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFD1DC), CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copiar",
                                    tint = Color(0xFF7A5761),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD3C3C5).copy(alpha = 0.5f))
                    Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                        HeartLogo(size = 48.dp)
                    }
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFD3C3C5).copy(alpha = 0.5f))
                }

                Spacer(modifier = Modifier.height(32.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    CampoTextoCristal(
                        valor = codigoIngresado,
                        alCambiarValor = { 
                            if (it.replace(" ", "").length <= 6) {
                                codigoIngresado = formatearEntradaCodigo(it)
                            }
                        },
                        etiqueta = "Ingresa el código de tu pareja",
                        sugerencia = "XXX XXX",
                        modificador = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Vincularse les permite ver el estado del otro y enviarse recordatorios.",
                        fontSize = 14.sp,
                        color = Color(0xFF4F4446).copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (estadoUi.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = estadoUi.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                BotonAutenticacion(
                    texto = "Vincular Corazones",
                    alHacerClick = { 
                        viewModel.vincularPorCodigo(codigoIngresado.replace(" ", ""), usuarioId)
                    },
                    estaCargando = estadoUi.cargando,
                    habilitado = codigoIngresado.replace(" ", "").length == 6,
                    modificador = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

private fun formatearCodigo(codigo: String): String {
    if (codigo.length != 6) return codigo
    return "${codigo.substring(0, 3)} ${codigo.substring(3, 6)}"
}

private fun formatearEntradaCodigo(entrada: String): String {
    val limpio = entrada.replace(" ", "").uppercase()
    return if (limpio.length > 3) {
        "${limpio.substring(0, 3)} ${limpio.substring(3)}"
    } else {
        limpio
    }
}
