package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.auth.ui.components.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * PANTALLA DE ANIVERSARIO
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantallaAniversario(
    viewModel: ParejaViewModel,
    relacionId: String,
    alGuardarFecha: () -> Unit,
    alVolver: () -> Unit
) {
    val estadoUi by viewModel.uiState.collectAsState()
    
    // Forzamos el Locale a Español para el calendario
    val configuracion = LocalConfiguration.current
    val localeEspanol = Locale("es", "ES")
    val contexto = LocalContext.current
    
    val estadoSelectorFecha = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendario.set(Calendar.HOUR_OF_DAY, 23)
                calendario.set(Calendar.MINUTE, 59)
                calendario.set(Calendar.SECOND, 59)
                calendario.set(Calendar.MILLISECOND, 999)
                return utcTimeMillis <= calendario.timeInMillis
            }
        }
    )

    LaunchedEffect(estadoUi.fechaGuardada) {
        if (estadoUi.fechaGuardada) {
            alGuardarFecha()
        }
    }

    // Proveedor de Localización para que el DatePicker hable español
    CompositionLocalProvider(LocalConfiguration provides configuracion.apply { setLocale(localeEspanol) }) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFF8EF)) // Fondo sólido solicitado
        ) {
            DecoracionFondoZen()

            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        IconButton(
                            onClick = alVolver,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.3f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color(0xFF78555E))
                        }
                    }
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
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "¿Cuándo comenzó su historia?",
                        fontSize = 32.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB2AC88),
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp
                    )
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(48.dp),
                        thickness = 1.dp,
                        color = Color(0xFFB2AC88).copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                    ) {
                        DatePicker(
                            state = estadoSelectorFecha,
                            showModeToggle = false,
                            title = null,
                            headline = null,
                            colors = DatePickerDefaults.colors(
                                containerColor = Color(0xFFFCE4EC),
                                selectedDayContainerColor = Color(0xFFFFD1DC),
                                selectedDayContentColor = Color(0xFF5E3E47),
                                todayContentColor = Color(0xFF78555E),
                                todayDateBorderColor = Color(0xFF78555E),
                                weekdayContentColor = Color(0xFF78555E).copy(alpha = 0.6f),
                                navigationContentColor = Color(0xFF78555E),
                                yearContentColor = Color(0xFF78555E),
                                selectedYearContainerColor = Color(0xFFFFD1DC),
                                selectedYearContentColor = Color(0xFF5E3E47)
                            ),
                            modifier = Modifier.padding(8.dp)
                        )
                    }

                    val diasJuntos = remember(estadoSelectorFecha.selectedDateMillis) {
                        val fecha = estadoSelectorFecha.selectedDateMillis ?: System.currentTimeMillis()
                        val diferencia = System.currentTimeMillis() - fecha
                        TimeUnit.MILLISECONDS.toDays(diferencia).coerceAtLeast(0)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Celebrando $diasJuntos días de amor",
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFB2AC88),
                        textAlign = TextAlign.Center
                    )

                    HorizontalDivider(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .width(64.dp),
                        thickness = 1.dp,
                        color = Color(0xFFB2AC88).copy(alpha = 0.3f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    IconoCorazonPalpitante()

                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0xFFFFF8EF).copy(alpha = 0.8f), Color(0xFFFFF8EF))
                        )
                    )
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                BotonAutenticacion(
                    texto = "Comenzar viaje",
                    icono = Icons.Default.FavoriteBorder, // Corazón hueco
                    coloresDegradado = listOf(Color(0xFFFFD1DC), Color(0xFFE7BBC6)),
                    alHacerClick = {
                        val fecha = estadoSelectorFecha.selectedDateMillis
                        if (fecha != null) {
                            viewModel.guardarFechaInicio(relacionId, fecha)
                        }
                    },
                    estaCargando = estadoUi.cargando,
                    modificador = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun IconoCorazonPalpitante() {
    val transicionInfinita = rememberInfiniteTransition(label = "latidoCorazon")
    val escala by transicionInfinita.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escala"
    )

    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = null,
        tint = Color(0xFFFFD1DC),
        modifier = Modifier
            .size(56.dp)
            .graphicsLayer {
                scaleX = escala
                scaleY = escala
                alpha = 0.95f
            }
    )
}
