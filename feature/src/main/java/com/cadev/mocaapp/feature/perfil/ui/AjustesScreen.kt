package com.cadev.mocaapp.feature.perfil.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.ripple
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(usuarioId) {
        if (uiState.usuario == null) {
            viewModel.cargarPerfil(usuarioId, parejaId)
        }
    }

    val context = LocalContext.current

    //Estados de edición por campo
    var editandoNombre by remember { mutableStateOf(false) }
    var editandoEmail by remember { mutableStateOf(false) }
    var editandoPassword by remember { mutableStateOf(false) }
    var editandoFecha by remember { mutableStateOf(false) }

    // Valores locales de edicion
    var valorNombre by remember(uiState.usuario?.nombre) {
        mutableStateOf(uiState.usuario?.nombre ?: "")
    }
    var valorEmail by remember(uiState.usuario?.email) {
        mutableStateOf(uiState.usuario?.email ?: "")
    }
    var valorPasswordActual by remember { mutableStateOf("") }
    var valorPasswordNuevo by remember { mutableStateOf("") }
    var valorPasswordConfirmar by remember { mutableStateOf("") }

    //Fecha
    val partesF = (uiState.fechaRelacion ?: "").split("-")
    var valorAnio by remember(uiState.fechaRelacion) {
        mutableStateOf(partesF.getOrNull(0) ?: "")
    }
    var valorMes by remember(uiState.fechaRelacion) {
        mutableStateOf(partesF.getOrNull(1) ?: "")
    }
    var valorDia by remember(uiState.fechaRelacion) {
        mutableStateOf(partesF.getOrNull(2) ?: "")
    }

    //Cerrar edición y limpiar al guardar con éxito
    LaunchedEffect(uiState.ajusteExitoso) {
        if (uiState.ajusteExitoso) {
            editandoNombre = false
            editandoEmail = false
            editandoPassword = false
            editandoFecha = false
            valorPasswordActual = ""
            valorPasswordNuevo = ""
            valorPasswordConfirmar = ""
            viewModel.limpiarMensajes()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            //Banner de error
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Error, null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            //Banner de exito
            if (uiState.ajusteExitoso) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle, null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "Cambios guardados",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            Text(
                "Información personal",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            // Nombre
            CampoEditable(
                icono = Icons.Filled.Person,
                etiqueta = "Nombre",
                valor = valorNombre,
                editando = editandoNombre,
                cargando = uiState.guardandoAjuste,
                onValorChange = { valorNombre = it },
                onEditar = {
                    editandoNombre = true
                    editandoEmail = false
                    editandoPassword = false
                    editandoFecha = false
                    viewModel.limpiarMensajes()
                },
                onCancelar = {
                    editandoNombre = false
                    valorNombre = uiState.usuario?.nombre ?: ""
                },
                onGuardar = {
                    viewModel.actualizarNombre(usuarioId, valorNombre)
                }
            )

            //Email
            CampoEditable(
                icono = Icons.Filled.Email,
                etiqueta = "Correo electrónico",
                valor = valorEmail,
                editando = editandoEmail,
                cargando = uiState.guardandoAjuste,
                onValorChange = { valorEmail = it },
                onEditar = {
                    editandoEmail = true
                    editandoNombre = false
                    editandoPassword = false
                    editandoFecha = false
                    viewModel.limpiarMensajes()
                },
                onCancelar = {
                    editandoEmail = false
                    valorEmail = uiState.usuario?.email ?: ""
                },
                onGuardar = {
                    viewModel.actualizarEmail(
                        usuarioId, valorEmail, valorPasswordActual
                    )
                },
                contenidoExtra = if (editandoEmail) {
                    {
                        OutlinedTextField(
                            value = valorPasswordActual,
                            onValueChange = { valorPasswordActual = it },
                            label = { Text("Contraseña actual") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else null
            )

            // Contraseña
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Lock, null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Contraseña",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                        .copy(alpha = 0.5f)
                                )
                                Text(
                                    "••••••••",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(onClick = {
                            editandoPassword = !editandoPassword
                            editandoNombre = false
                            editandoEmail = false
                            editandoFecha = false
                            valorPasswordActual = ""
                            valorPasswordNuevo = ""
                            valorPasswordConfirmar = ""
                            viewModel.limpiarMensajes()
                        }) {
                            Icon(
                                if (editandoPassword) Icons.Filled.Close
                                else Icons.Filled.Edit,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (editandoPassword) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        var verActual by remember { mutableStateOf(false) }
                        var verNuevo by remember { mutableStateOf(false) }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = valorPasswordActual,
                                onValueChange = { valorPasswordActual = it },
                                label = { Text("Contraseña actual") },
                                singleLine = true,
                                visualTransformation = if (verActual)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                trailingIcon = {
                                    TextButton(onClick = { verActual = !verActual }) {
                                        Text(if (verActual) "Ocultar" else "Ver")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = valorPasswordNuevo,
                                onValueChange = { valorPasswordNuevo = it },
                                label = { Text("Nueva contraseña") },
                                singleLine = true,
                                visualTransformation = if (verNuevo)
                                    VisualTransformation.None
                                else PasswordVisualTransformation(),
                                trailingIcon = {
                                    TextButton(onClick = { verNuevo = !verNuevo }) {
                                        Text(if (verNuevo) "Ocultar" else "Ver")
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = valorPasswordConfirmar,
                                onValueChange = { valorPasswordConfirmar = it },
                                label = { Text("Confirmar nueva contraseña") },
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                isError = valorPasswordConfirmar.isNotEmpty() &&
                                        valorPasswordNuevo != valorPasswordConfirmar,
                                supportingText = {
                                    if (valorPasswordConfirmar.isNotEmpty() &&
                                        valorPasswordNuevo != valorPasswordConfirmar
                                    ) Text("Las contraseñas no coinciden")
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { editandoPassword = false },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Cancelar") }
                                Button(
                                    onClick = {
                                        viewModel.actualizarPassword(
                                            uiState.usuario?.email ?: "",
                                            valorPasswordActual,
                                            valorPasswordNuevo,
                                            valorPasswordConfirmar
                                        )
                                    },
                                    enabled = !uiState.guardandoAjuste &&
                                            valorPasswordActual.isNotBlank() &&
                                            valorPasswordNuevo.isNotBlank() &&
                                            valorPasswordConfirmar.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (uiState.guardandoAjuste) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            //Fecha aniversario
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Filled.Favorite, null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Column {
                                Text(
                                    "Fecha de aniversario",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                        .copy(alpha = 0.5f)
                                )
                                Text(
                                    text = uiState.fechaRelacion ?: "No configurada",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        IconButton(onClick = {
                            editandoFecha = !editandoFecha
                            editandoNombre = false
                            editandoEmail = false
                            editandoPassword = false
                            viewModel.limpiarMensajes()
                        }) {
                            Icon(
                                if (editandoFecha) Icons.Filled.Close
                                else Icons.Filled.Edit,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (editandoFecha) {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = valorDia,
                                    onValueChange = {
                                        if (it.length <= 2)
                                            valorDia = it.filter { c -> c.isDigit() }
                                    },
                                    label = { Text("Día") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = valorMes,
                                    onValueChange = {
                                        if (it.length <= 2)
                                            valorMes = it.filter { c -> c.isDigit() }
                                    },
                                    label = { Text("Mes") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = valorAnio,
                                    onValueChange = {
                                        if (it.length <= 4)
                                            valorAnio = it.filter { c -> c.isDigit() }
                                    },
                                    label = { Text("Año") },
                                    singleLine = true,
                                    modifier = Modifier.weight(2f)
                                )
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { editandoFecha = false },
                                    modifier = Modifier.weight(1f)
                                ) { Text("Cancelar") }
                                Button(
                                    onClick = {
                                        val fecha = "$valorAnio-${
                                            valorMes.padStart(2, '0')
                                        }-${valorDia.padStart(2, '0')}"
                                        viewModel.actualizarFechaRelacion(
                                            usuarioId, fecha
                                        )
                                    },
                                    enabled = !uiState.guardandoAjuste &&
                                            valorAnio.length == 4 &&
                                            valorMes.isNotBlank() &&
                                            valorDia.isNotBlank(),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    if (uiState.guardandoAjuste) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onPrimary
                                        )
                                    } else Text("Guardar")
                                }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                "Cuenta",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.padding(start = 4.dp)
            )

            //Código de pareja
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            Icons.Filled.Link, null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Column {
                            Text(
                                "Código de pareja",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                    .copy(alpha = 0.7f)
                            )
                            Text(
                                text = uiState.usuario?.codigoPareja ?: "---",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    IconButton(onClick = {
                        val codigo =
                            uiState.usuario?.codigoPareja ?: return@IconButton
                        val clipboard = context.getSystemService(
                            Context.CLIPBOARD_SERVICE
                        ) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText("Código pareja", codigo)
                        )
                    }) {
                        Icon(
                            Icons.Filled.ContentCopy, "Copiar",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

//Campo editable genérico

@Composable
private fun CampoEditable(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    etiqueta: String,
    valor: String,
    editando: Boolean,
    cargando: Boolean,
    onValorChange: (String) -> Unit,
    onEditar: () -> Unit,
    onCancelar: () -> Unit,
    onGuardar: () -> Unit,
    contenidoExtra: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // clickable con ripple explícito para evitar conflicto M2,M3
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple()
                ) { if (!editando) onEditar() }
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        icono, null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            etiqueta,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.5f)
                        )
                        if (!editando) {
                            Text(
                                text = valor.ifBlank { "No configurado" },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                IconButton(onClick = if (editando) onCancelar else onEditar) {
                    Icon(
                        if (editando) Icons.Filled.Close else Icons.Filled.Edit,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (editando) {
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = valor,
                        onValueChange = onValorChange,
                        label = { Text(etiqueta) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    contenidoExtra?.invoke()
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onCancelar,
                            modifier = Modifier.weight(1f)
                        ) { Text("Cancelar") }
                        Button(
                            onClick = onGuardar,
                            enabled = !cargando && valor.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            if (cargando) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}