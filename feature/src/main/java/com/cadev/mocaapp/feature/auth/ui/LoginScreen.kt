package com.cadev.mocaapp.feature.auth.ui
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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

/**
 * ESTA ES LA PANTALLA DE INICIO DE SESIÓN
 * 
 * Qué hace:
 * Aquí diseñamos el formulario donde el usuario escribe sus datos para entrar. 
 * También pusimos botones para navegar a otras pantallas como la de registro.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar el diseño o los textos, debemos buscar los componentes como `Button` 
 * o `Text` y ajustar sus propiedades.
 */
@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onLoginExitoso: () -> Unit,
    onIrARegistro: () -> Unit
) {
    /**
     * Se observa la información que viene del gestor de datos
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * Estas variables guardan lo que el usuario escribe en cada cuadro de texto
     */
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    /**
     * Se asegura de que al entrar en esta pantalla no queden avisos de errores previos
     */
    LaunchedEffect(Unit) {
        viewModel.limpiarEstado()
    }

    /**
     * Si la entrada es correcta se avisa a la aplicación para que cambie de pantalla
     */
    LaunchedEffect(uiState.exitoso) {
        if (uiState.exitoso) onLoginExitoso()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        /**
         * Título y presentación de la aplicación
         */
        HeartLogo(size = 100.dp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "MocaApp",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Tu diario de pareja",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(48.dp))

        /**
         * Cuadro de texto para escribir el correo electrónico
         */
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Cuadro de texto para escribir la contraseña con opción de ver lo que se escribe
         */
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        /**
         * Si ocurre algún problema se muestra el aviso de error aquí
         */
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        /**
         * Botón principal para intentar entrar en la aplicación
         */
        Button(
            onClick = { viewModel.login(email, password) },
            enabled = !uiState.cargando,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            /**
             * Si la aplicación está comprobando los datos se muestra un círculo de carga
             */
            if (uiState.cargando) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Iniciar sesión")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /**
         * Botón secundario por si el usuario todavía no tiene una cuenta creada
         */
        TextButton(onClick = onIrARegistro) {
            Text("¿No tienes cuenta? Regístrate")
        }
    }
}
