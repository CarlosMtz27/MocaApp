package com.cadev.mocaapp.feature.cuestionarios.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cadev.mocaapp.feature.cuestionarios.domain.model.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrearCuestionarioScreen(
    viewModel: CuestionarioViewModel,
    usuarioId: String,
    relacionId: String,
    onCreado: () -> Unit,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.creadoExitoso) {
        if (uiState.creadoExitoso) {
            viewModel.resetearCreacion()
            onCreado()
        }
    }

    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var preguntas by remember { mutableStateOf(
        listOf(Pregunta(id = UUID.randomUUID().toString(),
            tipo = TipoPregunta.OPCION_MULTIPLE.name))
    )}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Crear cuestionario") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (titulo.isNotBlank() && preguntas.isNotEmpty()) {
                                viewModel.crearCuestionario(
                                    Cuestionario(
                                        titulo = titulo,
                                        descripcion = descripcion,
                                        categoria = CategoriaCuestionario
                                            .PERSONALIZADO.name,
                                        preguntas = preguntas,
                                        creadoPor = usuarioId,
                                        relacionId = relacionId
                                    )
                                )
                            }
                        },
                        enabled = titulo.isNotBlank() &&
                                preguntas.isNotEmpty() &&
                                !uiState.creando
                    ) {
                        if (uiState.creando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "Guardar",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            //Info básica
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Información básica",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = titulo,
                            onValueChange = { titulo = it },
                            label = { Text("Título del cuestionario") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = descripcion,
                            onValueChange = { descripcion = it },
                            label = { Text("Descripción (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            //Preguntas
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Preguntas (${preguntas.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalButton(
                        onClick = {
                            preguntas = preguntas + Pregunta(
                                id = UUID.randomUUID().toString(),
                                tipo = TipoPregunta.OPCION_MULTIPLE.name
                            )
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            Icons.Filled.Add, null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar")
                    }
                }
            }

            itemsIndexed(preguntas) { index, pregunta ->
                EditorPregunta(
                    numero = index + 1,
                    pregunta = pregunta,
                    onCambiar = { nueva ->
                        preguntas = preguntas.toMutableList()
                            .also { it[index] = nueva }
                    },
                    onEliminar = if (preguntas.size > 1) {
                        { preguntas = preguntas.toMutableList()
                            .also { it.removeAt(index) } }
                    } else null
                )
            }
        }
    }
}

//Editor de pregunta individual

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorPregunta(
    numero: Int,
    pregunta: Pregunta,
    onCambiar: (Pregunta) -> Unit,
    onEliminar: (() -> Unit)?
) {
    var expandido by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Pregunta $numero",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (onEliminar != null) {
                    IconButton(
                        onClick = onEliminar,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Filled.Delete, null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Texto de la pregunta
            OutlinedTextField(
                value = pregunta.texto,
                onValueChange = { onCambiar(pregunta.copy(texto = it)) },
                label = { Text("Texto de la pregunta") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            // Tipo de pregunta
            var expandidoTipo by remember { mutableStateOf(false) }
            val tipoActual = try {
                TipoPregunta.valueOf(pregunta.tipo)
            } catch (e: Exception) { TipoPregunta.OPCION_MULTIPLE }

            ExposedDropdownMenuBox(
                expanded = expandidoTipo,
                onExpandedChange = { expandidoTipo = it }
            ) {
                OutlinedTextField(
                    value = when (tipoActual) {
                        TipoPregunta.OPCION_MULTIPLE -> "Opción múltiple"
                        TipoPregunta.TEXTO_LIBRE -> "Texto libre"
                        TipoPregunta.ESCALA -> "Escala 1-10"
                        TipoPregunta.SI_NO -> "Sí / No"
                    },
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandidoTipo)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandidoTipo,
                    onDismissRequest = { expandidoTipo = false }
                ) {
                    listOf(
                        TipoPregunta.OPCION_MULTIPLE to "Opción múltiple",
                        TipoPregunta.TEXTO_LIBRE to "Texto libre",
                        TipoPregunta.ESCALA to "Escala 1-10",
                        TipoPregunta.SI_NO to "Sí / No"
                    ).forEach { (tipo, etiqueta) ->
                        DropdownMenuItem(
                            text = { Text(etiqueta) },
                            onClick = {
                                onCambiar(
                                    pregunta.copy(
                                        tipo = tipo.name,
                                        opciones = if (tipo ==
                                            TipoPregunta.OPCION_MULTIPLE)
                                            listOf("", "") else emptyList()
                                    )
                                )
                                expandidoTipo = false
                            }
                        )
                    }
                }
            }

            // Opciones (solo para OPCION_MULTIPLE)
            if (tipoActual == TipoPregunta.OPCION_MULTIPLE) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Opciones:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    pregunta.opciones.forEachIndexed { i, opcion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = opcion,
                                onValueChange = { nueva ->
                                    val nuevasOpciones = pregunta.opciones
                                        .toMutableList().also { it[i] = nueva }
                                    onCambiar(pregunta.copy(opciones = nuevasOpciones))
                                },
                                label = { Text("Opción ${i + 1}") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )
                            if (pregunta.opciones.size > 2) {
                                IconButton(
                                    onClick = {
                                        val nuevasOpciones = pregunta.opciones
                                            .toMutableList()
                                            .also { it.removeAt(i) }
                                        onCambiar(
                                            pregunta.copy(opciones = nuevasOpciones)
                                        )
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Delete, null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                    TextButton(
                        onClick = {
                            onCambiar(
                                pregunta.copy(
                                    opciones = pregunta.opciones + ""
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Agregar opción")
                    }
                }
            }
        }
    }
}