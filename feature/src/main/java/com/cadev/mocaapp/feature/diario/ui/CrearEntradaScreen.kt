package com.cadev.mocaapp.feature.diario.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.diario.domain.model.Emocion
import com.cadev.mocaapp.feature.diario.domain.model.TipoEntrada
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearEntradaScreen(
    viewModel: DiarioViewModel,
    usuarioId: String,
    parejaId: String?,
    fecha: String,
    tipo: String = TipoEntrada.MI_DIA.name,
    onEntradaGuardada: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Configurar valores iniciales según el tipo
    val tipoEntrada = try {
        TipoEntrada.valueOf(tipo)
    } catch (e: Exception) {
        TipoEntrada.MI_DIA
    }

    // Limpiar formulario al entrar
    LaunchedEffect(Unit) {
        viewModel.limpiarFormulario()
        // Los recuerdos se comparten por defecto
        if (tipoEntrada == TipoEntrada.RECUERDO && parejaId != null) {
            viewModel.toggleCompartir()
        }
    }

    // Navegar cuando se guarda
    LaunchedEffect(uiState.entradaCreada) {
        if (uiState.entradaCreada) onEntradaGuardada()
    }

    // Formato de fecha legible
    val formatoEntrada = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatoVisible = SimpleDateFormat("EEEE d 'de' MMMM", Locale("es", "MX"))
    val fechaVisible = try {
        formatoVisible.format(formatoEntrada.parse(fecha)!!)
            .replaceFirstChar { it.uppercase() }
    } catch (e: Exception) { fecha }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${tipoEntrada.emoji} ${tipoEntrada.etiqueta}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = fechaVisible,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    // Botón guardar en la barra superior
                    IconButton(
                        onClick = {
                            viewModel.guardarEntrada(usuarioId, parejaId, fecha)
                        },
                        enabled = !uiState.cargando
                    ) {
                        if (uiState.cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Guardar",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            //Título
            OutlinedTextField(
                value = uiState.titulo,
                onValueChange = { viewModel.actualizarTitulo(it) },
                label = { Text("¿Cómo fue tu día?") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Emocione
            Text(
                text = "¿Cómo te sentiste?",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Chips de emociones en filas
            // Dividimos las emociones en filas de 4
            val emociones = Emocion.entries.chunked(4)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                emociones.forEach { fila ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        fila.forEach { emocion ->
                            val seleccionada = uiState.emocionesSeleccionadas
                                .contains(emocion)
                            Box(modifier = Modifier.weight(1f)) {
                                ChipEmocion(
                                    emocion = emocion,
                                    seleccionada = seleccionada,
                                    onClick = { viewModel.toggleEmocion(emocion) }
                                )
                            }
                        }
                        // Rellenar espacios vacíos en la última fila
                        repeat(4 - fila.size) {
                            Box(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
            //Detalles
            OutlinedTextField(
                value = uiState.detalles,
                onValueChange = { viewModel.actualizarDetalles(it) },
                label = { Text("Cuéntame más...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                maxLines = 8
            )

            //Compartir con pareja
            if (parejaId != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { viewModel.toggleCompartir() }
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "💕 Compartir con mi pareja",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tu pareja podrá ver esta entrada",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = uiState.compartir,
                        onCheckedChange = { viewModel.toggleCompartir() }
                    )
                }
            }

            //Error
            if (uiState.error != null) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ChipEmocion(
    emocion: Emocion,
    seleccionada: Boolean,
    onClick: () -> Unit
) {
    val colorFondo = if (seleccionada)
        MaterialTheme.colorScheme.primaryContainer
    else
        MaterialTheme.colorScheme.surface

    val colorBorde = if (seleccionada)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outline

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(colorFondo)
            .border(1.dp, colorBorde, RoundedCornerShape(50.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = emocion.emoji, fontSize = 16.sp)
        Text(
            text = emocion.etiqueta,
            style = MaterialTheme.typography.bodyMedium,
            color = if (seleccionada)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}
