package com.cadev.mocaapp.feature.auth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    // Limpia errores previos al entrar a esta pantalla
    LaunchedEffect(Unit) {
        viewModel.limpiarEstado()
    }

    // Cuando el registro es exitoso, navegamos afuera
    LaunchedEffect(uiState.exitoso) {
        if (uiState.exitoso) onRegistroExitoso()
    }

    // Scroll por si el teclado empuja el contenido
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // Título
        Text(
            text = "💕",
            fontSize = 48.sp
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Crear cuenta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Empieza tu diario de pareja",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(Modifier.height(40.dp))

        // Campo Nombre
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        //Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Campo Contraseña
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (passwordVisible)
                VisualTransformation.None
            else
                PasswordVisualTransformation(),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(if (passwordVisible) "Ocultar" else "Ver")
                }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Confirmar Contraseña
        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            // Borde rojo si las contraseñas no coinciden
            isError = confirmarPassword.isNotEmpty()
                    && password != confirmarPassword,
            supportingText = {
                if (confirmarPassword.isNotEmpty()
                    && password != confirmarPassword) {
                    Text(
                        text = "Las contraseñas no coinciden",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(28.dp))

        // Errores
        val errorMostrar = errorLocal ?: uiState.error
        if (errorMostrar != null) {
            Text(
                text = errorMostrar,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        // Botón Registrarse
        Button(
            onClick = {
                // Validacion local antes de llamar al ViewModel
                errorLocal = when {
                    password != confirmarPassword ->
                        "Las contraseñas no coinciden"
                    password.length < 6 ->
                        "La contraseña debe tener al menos 6 caracteres"
                    else -> null
                }
                if (errorLocal == null) {
                    viewModel.registrar(email, password, nombre)
                }
            },
            enabled = !uiState.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.cargando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Crear cuenta")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Ir a Login
        TextButton(onClick = onIrALogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}