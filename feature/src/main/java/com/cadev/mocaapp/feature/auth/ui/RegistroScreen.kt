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

/**
 * ESTA ES LA PANTALLA PARA CREAR UNA CUENTA
 * 
 * Qué hace:
 * Aquí permitimos que los nuevos usuarios se registren. Nos aseguramos de que escriban bien 
 * su nombre, correo y que las contraseñas coincidan antes de enviarlo.
 * 
 * Cómo lo podemos modificar:
 * Si queremos pedir un dato nuevo (ej: fecha de nacimiento), debemos añadir un nuevo 
 * `OutlinedTextField` siguiendo la estructura que ya tenemos.
 */
@Composable
fun RegistroScreen(
    viewModel: AuthViewModel,
    onRegistroExitoso: () -> Unit,
    onIrALogin: () -> Unit
) {
    /**
     * Se vigila la información del gestor para saber si hay errores o si el proceso terminó bien
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * Estas variables guardan lo que el usuario va escribiendo en el formulario
     */
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmarPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorLocal by remember { mutableStateOf<String?>(null) }

    /**
     * Se reinicia cualquier aviso de error anterior al entrar en la pantalla
     */
    LaunchedEffect(Unit) {
        viewModel.limpiarEstado()
    }

    /**
     * Si el registro se completa con éxito se avisa a la aplicación para avanzar
     */
    LaunchedEffect(uiState.exitoso) {
        if (uiState.exitoso) onRegistroExitoso()
    }

    /**
     * Se usa un contenedor que permite deslizar hacia arriba si el teclado tapa los cuadros
     */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp)
            .padding(vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        /**
         * Cabecera visual de la pantalla
         */

        HeartLogo(size = 80.dp)
        
        Spacer(Modifier.height(16.dp))
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

        /**
         * Cuadro para escribir el nombre personal
         */
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Tu nombre") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        /**
         * Cuadro para escribir el correo electrónico
         */
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

        /**
         * Cuadro para elegir una contraseña secreta
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
                /**
                 * Botón para mostrar u ocultar los caracteres de la clave
                 */
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

        /**
         * Cuadro para repetir la contraseña y estar seguros de que no hay errores
         */
        OutlinedTextField(
            value = confirmarPassword,
            onValueChange = { confirmarPassword = it },
            label = { Text("Confirmar contraseña") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password
            ),
            /**
             * Se marca en rojo si las dos contraseñas escritas son distintas
             */
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

        /**
         * Aquí se muestran los avisos si algo ha salido mal durante el registro
         */
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

        /**
         * Botón principal para realizar el registro en la base de datos
         */
        Button(
            onClick = {
                /**
                 * Se comprueba que todo esté bien antes de enviar los datos al servidor
                 */
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
            /**
             * Mientras se crea la cuenta se muestra un indicador de que la aplicación está trabajando
             */
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

        /**
         * Botón para volver atrás si el usuario ya tiene una cuenta activa
         */
        TextButton(onClick = onIrALogin) {
            Text("¿Ya tienes cuenta? Inicia sesión")
        }
    }
}
