package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CodigoParejaScreen(
    viewModel: ParejaViewModel,
    usuarioId: String,
    onVinculado: (relacionId: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val clipboard = LocalClipboardManager.current
    var codigoIngresado by remember { mutableStateOf("") }
    var copiado by remember { mutableStateOf(false) }

    // Cargamos el codigo propio al entrar
    LaunchedEffect(Unit) {
        viewModel.cargarMiCodigo(usuarioId)
    }

    // Navegamos cuando se vincule exitosamente
    LaunchedEffect(uiState.vinculado) {
        if (uiState.vinculado) {
            onVinculado(uiState.relacionId)  //pasa el ID
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "🔗", fontSize = 56.sp)
        Spacer(Modifier.height(12.dp))

        Text(
            text = "Vincular pareja",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Comparte tu código o ingresa el de tu pareja",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(40.dp))

        // Campo Tu código
        Text(
            text = "Tu código",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        // Caja con el código que se puede copiar
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (uiState.miCodigo.isEmpty()) "..." else uiState.miCodigo,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 8.sp  // espaciado para que sea facil de leer
            )
        }

        Spacer(Modifier.height(8.dp))

        // Botón copiar
        TextButton(
            onClick = {
                clipboard.setText(AnnotatedString(uiState.miCodigo))
                copiado = true
            }
        ) {
            Text(if (copiado) "✅ Copiado" else "📋 Copiar código")
        }

        Spacer(Modifier.height(40.dp))
        HorizontalDivider()
        Spacer(Modifier.height(40.dp))

        // Campo para ingresar codigo de pareja
        Text(
            text = "Código de tu pareja",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = codigoIngresado,
            onValueChange = { nuevo ->
                // Solo letras y numeros, máximo 6 caracteres
                if (nuevo.length <= 6) {
                    codigoIngresado = nuevo.uppercase()
                }
            },
            label = { Text("Ej: XK92AB") },
            singleLine = true,
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                textAlign = TextAlign.Center,
                letterSpacing = 6.sp
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Characters
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        // Contador de caracteres
        Text(
            text = "${codigoIngresado.length}/6",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(Modifier.height(16.dp))

        // Error
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        // Botón vincular
        Button(
            onClick = {
                viewModel.vincularPorCodigo(codigoIngresado, usuarioId)
            },
            enabled = !uiState.cargando && codigoIngresado.length == 6,
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
                Text("Vincularme con mi pareja 💕")
            }
        }
    }
}