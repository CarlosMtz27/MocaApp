package com.cadev.mocaapp.feature.perfil.ui


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Navegar al guardar con éxito
    LaunchedEffect(uiState.ajusteExitoso) {
        if (uiState.ajusteExitoso) {
            viewModel.limpiarMensajes()
        }
    }

    // Qué sección está expandida
    var seccionAbierta by remember {
        mutableStateOf<String?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            "Regresar"
                        )
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

            // Mensaje de error
            if (uiState.error != null) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            //Mensaje de éxito
            if (uiState.ajusteExitoso) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme
                            .primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.CheckCircle,
                            null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "✅ Cambios guardados",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Cambiar nombre
            SeccionAjuste(
                icono = Icons.Filled.Person,
                titulo = "Nombre",
                subtitulo = uiState.usuario?.nombre ?: "",
                expandida = seccionAbierta == "nombre",
                onToggle = {
                    seccionAbierta = if (seccionAbierta == "nombre")
                        null else "nombre"
                    viewModel.limpiarMensajes()
                }
            ) {
                CampoNombre(
                    nombreActual = uiState.usuario?.nombre ?: "",
                    cargando = uiState.guardandoAjuste,
                    onGuardar = { nuevo ->
                        viewModel.actualizarNombre(usuarioId, nuevo)
                    }
                )
            }

            //Cambiar email
            SeccionAjuste(
                icono = Icons.Filled.Email,
                titulo = "Correo electrónico",
                subtitulo = uiState.usuario?.email ?: "",
                expandida = seccionAbierta == "email",
                onToggle = {
                    seccionAbierta = if (seccionAbierta == "email")
                        null else "email"
                    viewModel.limpiarMensajes()
                }
            ) {
                CampoEmail(
                    emailActual = uiState.usuario?.email ?: "",
                    cargando = uiState.guardandoAjuste,
                    onGuardar = { nuevoEmail, password ->
                        viewModel.actualizarEmail(
                            usuarioId, nuevoEmail, password
                        )
                    }
                )
            }

            //Cambiar contraseña
            SeccionAjuste(
                icono = Icons.Filled.Lock,
                titulo = "Contraseña",
                subtitulo = "••••••••",
                expandida = seccionAbierta == "password",
                onToggle = {
                    seccionAbierta = if (seccionAbierta == "password")
                        null else "password"
                    viewModel.limpiarMensajes()
                }
            ) {
                CampoPassword(
                    email = uiState.usuario?.email ?: "",
                    cargando = uiState.guardandoAjuste,
                    onGuardar = { actual, nueva, confirmar ->
                        viewModel.actualizarPassword(
                            uiState.usuario?.email ?: "",
                            actual, nueva, confirmar
                        )
                    }
                )
            }

            //Fecha de aniversario
            SeccionAjuste(
                icono = Icons.Filled.Favorite,
                titulo = "Fecha de aniversario",
                subtitulo = uiState.fechaRelacion ?: "No configurada",
                expandida = seccionAbierta == "fecha",
                onToggle = {
                    seccionAbierta = if (seccionAbierta == "fecha")
                        null else "fecha"
                    viewModel.limpiarMensajes()
                }
            ) {
                CampoFecha(
                    fechaActual = uiState.fechaRelacion ?: "",
                    cargando = uiState.guardandoAjuste,
                    onGuardar = { fecha ->
                        viewModel.actualizarFechaRelacion(usuarioId, fecha)
                    }
                )
            }

            //Codigo de pareja
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme
                        .secondaryContainer
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
                            Icons.Filled.Link,
                            null,
                            tint = MaterialTheme.colorScheme
                                .onSecondaryContainer
                        )
                        Column {
                            Text(
                                text = "Código de pareja",
                                style = MaterialTheme.typography
                                    .labelMedium
                            )
                            Text(
                                text = uiState.usuario?.codigoPareja ?: "---",
                                style = MaterialTheme.typography
                                    .headlineSmall,
                                color = MaterialTheme.colorScheme
                                    .onSecondaryContainer
                            )
                        }
                    }
                    IconButton(onClick = { /* Copiar al portapapeles */ }) {
                        Icon(Icons.Filled.ContentCopy, "Copiar")
                    }
                }
            }
        }
    }
}

//Seccion colapsable
@Composable
private fun SeccionAjuste(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    titulo: String,
    subtitulo: String,
    expandida: Boolean,
    onToggle: () -> Unit,
    contenido: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onToggle)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        icono,
                        null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = titulo,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = subtitulo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                                .copy(alpha = 0.5f)
                        )
                    }
                }
                Icon(
                    if (expandida) Icons.Filled.ExpandLess
                    else Icons.Filled.ExpandMore,
                    null,
                    tint = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.4f)
                )
            }

            if (expandida) {
                HorizontalDivider()
                Box(modifier = Modifier.padding(16.dp)) {
                    contenido()
                }
            }
        }
    }
}

//Campos de ajuste

@Composable
private fun CampoNombre(
    nombreActual: String,
    cargando: Boolean,
    onGuardar: (String) -> Unit
) {
    var nombre by remember(nombreActual) { mutableStateOf(nombreActual) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nuevo nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onGuardar(nombre) },
            enabled = !cargando && nombre.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar nombre")
            }
        }
    }
}

@Composable
private fun CampoEmail(
    emailActual: String,
    cargando: Boolean,
    onGuardar: (String, String) -> Unit
) {
    var email by remember(emailActual) { mutableStateOf(emailActual) }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Nuevo correo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña actual (para confirmar)") },
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Ocultar" else "Ver")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onGuardar(email, password) },
            enabled = !cargando && email.isNotBlank() && password.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar correo")
            }
        }
    }
}

@Composable
private fun CampoPassword(
    email: String,
    cargando: Boolean,
    onGuardar: (String, String, String) -> Unit
) {
    var passwordActual by remember { mutableStateOf("") }
    var passwordNuevo by remember { mutableStateOf("") }
    var passwordConfirmar by remember { mutableStateOf("") }
    var verActual by remember { mutableStateOf(false) }
    var verNuevo by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = passwordActual,
            onValueChange = { passwordActual = it },
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
            value = passwordNuevo,
            onValueChange = { passwordNuevo = it },
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
            value = passwordConfirmar,
            onValueChange = { passwordConfirmar = it },
            label = { Text("Confirmar nueva contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            isError = passwordConfirmar.isNotEmpty() &&
                    passwordNuevo != passwordConfirmar,
            supportingText = {
                if (passwordConfirmar.isNotEmpty() &&
                    passwordNuevo != passwordConfirmar) {
                    Text("Las contraseñas no coinciden")
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onGuardar(passwordActual, passwordNuevo, passwordConfirmar)
            },
            enabled = !cargando &&
                    passwordActual.isNotBlank() &&
                    passwordNuevo.isNotBlank() &&
                    passwordConfirmar.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Cambiar contraseña")
            }
        }
    }
}

@Composable
private fun CampoFecha(
    fechaActual: String,
    cargando: Boolean,
    onGuardar: (String) -> Unit
) {
    // Parsear fecha actual
    val partes = fechaActual.split("-")
    var anio by remember(fechaActual) {
        mutableStateOf(partes.getOrNull(0) ?: "")
    }
    var mes by remember(fechaActual) {
        mutableStateOf(partes.getOrNull(1) ?: "")
    }
    var dia by remember(fechaActual) {
        mutableStateOf(partes.getOrNull(2) ?: "")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = dia,
                onValueChange = {
                    if (it.length <= 2) dia = it.filter { c -> c.isDigit() }
                },
                label = { Text("Día") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = mes,
                onValueChange = {
                    if (it.length <= 2) mes = it.filter { c -> c.isDigit() }
                },
                label = { Text("Mes") },
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = anio,
                onValueChange = {
                    if (it.length <= 4) anio = it.filter { c -> c.isDigit() }
                },
                label = { Text("Año") },
                singleLine = true,
                modifier = Modifier.weight(2f)
            )
        }
        Button(
            onClick = {
                val fecha = "$anio-${mes.padStart(2, '0')}-${dia.padStart(2, '0')}"
                onGuardar(fecha)
            },
            enabled = !cargando &&
                    anio.length == 4 &&
                    mes.isNotBlank() &&
                    dia.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar fecha")
            }
        }
    }
}