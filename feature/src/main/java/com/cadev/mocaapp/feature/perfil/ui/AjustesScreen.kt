package com.cadev.mocaapp.feature.perfil.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.core.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AjustesScreen(
    viewModel: PerfilViewModel,
    usuarioId: String,
    parejaId: String?,
    onRegresar: () -> Unit,
    onNavigateToWidgets: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estados de edición
    var editandoNombre by remember { mutableStateOf(false) }
    var editandoEmail by remember { mutableStateOf(false) }
    var editandoPassword by remember { mutableStateOf(false) }
    var editandoNotificaciones by remember { mutableStateOf(false) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }

    var valorNombre by remember(uiState.usuario?.nombre) { mutableStateOf(uiState.usuario?.nombre ?: "") }
    var valorEmail by remember(uiState.usuario?.email) { mutableStateOf(uiState.usuario?.email ?: "") }
    var valorPasswordActual by remember { mutableStateOf("") }
    var valorPasswordNuevo by remember { mutableStateOf("") }

    // Preferencias de notificaciones (simuladas)
    var notifChat by remember { mutableStateOf(true) }
    var notifDiario by remember { mutableStateOf(true) }
    var notifTests by remember { mutableStateOf(true) }
    var notifNotas by remember { mutableStateOf(true) }

    LaunchedEffect(uiState.ajusteExitoso) {
        if (uiState.ajusteExitoso) {
            editandoNombre = false
            editandoEmail = false
            editandoPassword = false
            valorPasswordActual = ""
            valorPasswordNuevo = ""
            viewModel.limpiarMensajes()
        }
    }

    if (mostrarAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarAcercaDe = false },
            confirmButton = { TextButton(onClick = { mostrarAcercaDe = false }) { Text("Cerrar") } },
            title = { Text("Moca App", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Versión 1.0.0", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                    Text("Una aplicación diseñada para fortalecer el vínculo de pareja a través de recuerdos, retos y momentos compartidos.")
                    Spacer(Modifier.height(16.dp))
                    Text("Hecho con ❤️ para ti.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Ajustes",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            letterSpacing = (-0.02).sp
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Sección General
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Modo Oscuro
                ItemAjusteSwitch(
                    icon = Icons.Default.DarkMode,
                    label = "Modo Oscuro",
                    checked = ThemeManager.isDarkTheme,
                    onCheckedChange = { ThemeManager.isDarkTheme = it }
                )

                // Notificaciones (Expandible con checkboxes)
                ItemAjusteExpandible(
                    icon = Icons.Default.Notifications,
                    label = "Notificaciones",
                    value = if (editandoNotificaciones) "Configurando..." else "Gestionar avisos",
                    isExpanded = editandoNotificaciones,
                    onExpandClick = { editandoNotificaciones = !editandoNotificaciones }
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        NotificacionCheckItem("Chat Privado", notifChat) { notifChat = it }
                        NotificacionCheckItem("Recuerdos del Diario", notifDiario) { notifDiario = it }
                        NotificacionCheckItem("Nuevos Tests", notifTests) { notifTests = it }
                        NotificacionCheckItem("Notas en el Muro", notifNotas) { notifNotas = it }
                    }
                }

                // Privacidad
                ItemAjusteClickable(
                    icon = Icons.Default.Lock,
                    label = "Privacidad",
                    onClick = { }
                )
            }

            // Sección Cuenta
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "CUENTA",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 2.sp
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Nombre
                CampoEditableZen(
                    icon = Icons.Default.Person,
                    label = "Nombre",
                    value = valorNombre,
                    isEditing = editandoNombre,
                    onEditClick = { editandoNombre = true },
                    onCancelClick = { editandoNombre = false; valorNombre = uiState.usuario?.nombre ?: "" },
                    onSaveClick = { viewModel.actualizarNombre(usuarioId, valorNombre) },
                    onValueChange = { valorNombre = it }
                )

                // Email
                CampoEditableZen(
                    icon = Icons.Default.Email,
                    label = "Correo electrónico",
                    value = valorEmail,
                    isEditing = editandoEmail,
                    onEditClick = { editandoEmail = true },
                    onCancelClick = { editandoEmail = false; valorEmail = uiState.usuario?.email ?: "" },
                    onSaveClick = { viewModel.actualizarEmail(usuarioId, valorEmail, valorPasswordActual) },
                    onValueChange = { valorEmail = it },
                    extraContent = if (editandoEmail) {
                        {
                            OutlinedTextField(
                                value = valorPasswordActual,
                                onValueChange = { valorPasswordActual = it },
                                label = { Text("Contraseña actual") },
                                visualTransformation = PasswordVisualTransformation(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    } else null
                )

                // Seguridad (Contraseña)
                ItemAjusteExpandible(
                    icon = Icons.Default.Security,
                    label = "Seguridad",
                    value = "Cambiar contraseña",
                    isExpanded = editandoPassword,
                    onExpandClick = { editandoPassword = !editandoPassword }
                ) {
                    var verActual by remember { mutableStateOf(false) }
                    var verNuevo by remember { mutableStateOf(false) }

                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = valorPasswordActual,
                            onValueChange = { valorPasswordActual = it },
                            label = { Text("Contraseña actual") },
                            visualTransformation = if (verActual) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { verActual = !verActual }) { Icon(if (verActual) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        OutlinedTextField(
                            value = valorPasswordNuevo,
                            onValueChange = { valorPasswordNuevo = it },
                            label = { Text("Nueva contraseña") },
                            visualTransformation = if (verNuevo) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = { IconButton(onClick = { verNuevo = !verNuevo }) { Icon(if (verNuevo) Icons.Default.VisibilityOff else Icons.Default.Visibility, null) } },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Button(
                            onClick = { viewModel.actualizarPassword(uiState.usuario?.email ?: "", valorPasswordActual, valorPasswordNuevo, valorPasswordNuevo) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            if (uiState.guardandoAjuste) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                            else Text("Actualizar Contraseña")
                        }
                    }
                }

                // Acerca de (Reemplaza Ayuda)
                ItemAjusteClickable(
                    icon = Icons.Default.Info,
                    label = "Acerca de",
                    onClick = { mostrarAcercaDe = true }
                )
            }

            // Sección Extra
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Código de pareja", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(uiState.usuario?.codigoPareja ?: "---", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Moca Code", uiState.usuario?.codigoPareja))
                        }) {
                            Icon(Icons.Default.ContentCopy, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

                ItemAjusteClickable(
                    icon = Icons.Default.Widgets,
                    label = "Widgets de inicio",
                    onClick = onNavigateToWidgets
                )
            }

            // Botón Cerrar Sesión
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                TextButton(
                    onClick = onLogout,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Cerrar Sesión", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun NotificacionCheckItem(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun ItemAjusteSwitch(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color.LightGray
                )
            )
        }
    }
}

@Composable
fun ItemAjusteClickable(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                Text(label, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ItemAjusteExpandible(
    icon: ImageVector,
    label: String,
    value: String,
    isExpanded: Boolean,
    onExpandClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().shadow(20.dp, RoundedCornerShape(16.dp), spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
                    Column {
                        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(value, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
                IconButton(onClick = onExpandClick) {
                    Icon(if (isExpanded) Icons.Default.Close else Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    content()
                }
            }
        }
    }
}

@Composable
fun CampoEditableZen(
    icon: ImageVector,
    label: String,
    value: String,
    isEditing: Boolean,
    onEditClick: () -> Unit,
    onCancelClick: () -> Unit,
    onSaveClick: () -> Unit,
    onValueChange: (String) -> Unit,
    extraContent: (@Composable () -> Unit)? = null
) {
    ItemAjusteExpandible(
        icon = icon,
        label = label,
        value = value,
        isExpanded = isEditing,
        onExpandClick = if (isEditing) onCancelClick else onEditClick
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(label) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            extraContent?.invoke()
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onCancelClick, modifier = Modifier.weight(1f)) { Text("Cancelar") }
                Button(onClick = onSaveClick, modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                    Text("Guardar")
                }
            }
        }
    }
}
