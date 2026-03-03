package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.cuestionarios.domain.model.CategoriaCuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.Cuestionario
import com.cadev.mocaapp.feature.cuestionarios.domain.model.EstadoCuestionario

// Colores de estado
private val colorYoRespondí = Color(0xFFFFF3E0)
private val colorBordeYoRespondí = Color(0xFFFF9800)
private val colorParejaRespondió = Color(0xFFE8F5E9)
private val colorBordeParejaRespondió = Color(0xFF4CAF50)
private val colorAmbos = Color(0xFFE3F2FD)
private val colorBordeAmbos = Color(0xFF2196F3)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuestionariosScreen(
    viewModel: CuestionarioViewModel,
    usuarioId: String,
    parejaId: String,
    relacionId: String,
    onIniciarCuestionario: (String) -> Unit,
    onVerResultados: (String) -> Unit,
    onCrearCuestionario: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var tabSeleccionado by remember { mutableStateOf(0) }
    LaunchedEffect(relacionId) {
        viewModel.cargarCuestionarios(relacionId, usuarioId, parejaId)
        viewModel.poblarPredefinidos()
    }

    LaunchedEffect(tabSeleccionado) {
        if (tabSeleccionado == 0) {
            viewModel.refrescarEstados(usuarioId, parejaId)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "📋 Cuestionarios",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onCrearCuestionario) {
                        Icon(
                            Icons.Filled.Add,
                            "Crear",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {

            TabRow(selectedTabIndex = tabSeleccionado) {
                Tab(
                    selected = tabSeleccionado == 0,
                    onClick = { tabSeleccionado = 0 },
                    text = { Text("Disponibles") }
                )
                Tab(
                    selected = tabSeleccionado == 1,
                    onClick = { tabSeleccionado = 1 },
                    text = { Text("Completados") }
                )
            }

            if (uiState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
                return@Scaffold
            }

            if (tabSeleccionado == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val porCategoria = uiState.cuestionarios.groupBy { it.categoria }

                    porCategoria.forEach { (categoria, lista) ->
                        item {
                            val cat = try {
                                CategoriaCuestionario.valueOf(categoria)
                            } catch (e: Exception) {
                                CategoriaCuestionario.PERSONALIZADO
                            }
                            Text(
                                text = "${cat.emoji} ${cat.etiqueta}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                            )
                        }
                        items(lista) { cuestionario ->
                            val estado = uiState.estadosCuestionarios[cuestionario.id]
                                ?: EstadoCuestionario.NINGUNO
                            TarjetaCuestionario(
                                cuestionario = cuestionario,
                                estado = estado,
                                onClick = {
                                    when (estado) {
                                        EstadoCuestionario.AMBOS,
                                        EstadoCuestionario.YO_RESPONDÍ ->
                                            onVerResultados(cuestionario.id)
                                        else ->
                                            onIniciarCuestionario(cuestionario.id)
                                    }
                                }
                            )
                        }
                    }

                    if (uiState.cuestionarios.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "No hay cuestionarios disponibles",
                                    color = MaterialTheme.colorScheme.onSurface
                                        .copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.historial.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("📋", fontSize = 48.sp)
                                    Text(
                                        "Aún no has completado ningún cuestionario",
                                        color = MaterialTheme.colorScheme.onSurface
                                            .copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(uiState.historial) { cuestionario ->
                            val estado = uiState.estadosCuestionarios[cuestionario.id]
                                ?: EstadoCuestionario.AMBOS
                            TarjetaCuestionario(
                                cuestionario = cuestionario,
                                estado = estado,
                                onClick = { onVerResultados(cuestionario.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

//Tarjeta con indicador de estado
@Composable
private fun TarjetaCuestionario(
    cuestionario: Cuestionario,
    estado: EstadoCuestionario,
    onClick: () -> Unit
) {
    val categoria = try {
        CategoriaCuestionario.valueOf(cuestionario.categoria)
    } catch (e: Exception) { CategoriaCuestionario.PERSONALIZADO }

    // Color de fondo y borde según estado
    val (colorFondo, colorBorde, etiquetaEstado) = when (estado) {
        EstadoCuestionario.YO_RESPONDÍ -> Triple(
            colorYoRespondí, colorBordeYoRespondí,
            "⏳ Esperando a que tu pareja responda"
        )
        EstadoCuestionario.PAREJA_RESPONDIÓ -> Triple(
            colorParejaRespondió, colorBordeParejaRespondió,
            "💬 ¡Tu pareja ya respondió! Es tu turno"
        )
        EstadoCuestionario.AMBOS -> Triple(
            colorAmbos, colorBordeAmbos,
            "✅ Completado por ambos"
        )
        EstadoCuestionario.NINGUNO -> Triple(
            Color.Transparent,
            Color.Transparent,
            null
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (colorBorde != Color.Transparent)
                    Modifier.border(
                        width = 2.dp,
                        color = colorBorde,
                        shape = RoundedCornerShape(16.dp)
                    )
                else Modifier
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple()
            ) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (colorFondo != Color.Transparent)
                colorFondo
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(categoria.emoji, fontSize = 26.sp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = cuestionario.titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = cuestionario.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 2
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "${cuestionario.preguntas.size} preguntas",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (estado == EstadoCuestionario.AMBOS) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        null,
                        tint = colorBordeAmbos,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            //Banner de estado (solo si hay mensaje)
            if (etiquetaEstado != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            colorBorde.copy(alpha = 0.15f),
                            RoundedCornerShape(
                                bottomStart = 16.dp,
                                bottomEnd = 16.dp
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = etiquetaEstado,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colorBorde
                    )
                }
            }
        }
    }
}