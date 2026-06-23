package com.cadev.mocaapp.feature.pareja.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
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

/**
 * ESTA ES NUESTRA PANTALLA DE VINCULACIÓN
 * 
 * Qué hace:
 * Aquí es donde nos unimos. Mostramos nuestro código para compartirlo y 
 * también tenemos un espacio para poner el código de nuestra pareja. 
 * Es el paso más importante para empezar a compartir todo.
 * 
 * Cómo lo podemos modificar:
 * Si queremos cambiar el icono de enlace, debemos buscar el componente `Icon` 
 * con `Icons.Default.Link`.
 */
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

    /**
     * Se carga el código personal del usuario en cuanto se abre la pantalla
     */
    LaunchedEffect(Unit) {
        viewModel.cargarMiCodigo(usuarioId)
    }

    /**
     * Si la unión se realiza con éxito se avisa a la aplicación para pasar a la siguiente fase
     */
    LaunchedEffect(uiState.vinculado) {
        if (uiState.vinculado) {
            onVinculado(uiState.relacionId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Icon(
            imageVector = Icons.Default.Link,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.primary
        )
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

        Text(
            text = "Tu código",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        /**
         * Espacio visual donde se muestra el código personal del usuario
         */
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
                letterSpacing = 8.sp
            )
        }

        Spacer(Modifier.height(8.dp))

        /**
         * Botón para copiar el código al portapapeles del teléfono automáticamente
         */
        TextButton(
            onClick = {
                clipboard.setText(AnnotatedString(uiState.miCodigo))
                copiado = true
            }
        ) {
            Icon(
                imageVector = if (copiado) Icons.Default.Check else Icons.Default.ContentCopy,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(if (copiado) "Copiado" else "Copiar código")
        }

        Spacer(Modifier.height(40.dp))
        HorizontalDivider()
        Spacer(Modifier.height(40.dp))

        Text(
            text = "Código de tu pareja",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))

        /**
         * Cuadro de texto para introducir el código que la pareja ha compartido
         */
        OutlinedTextField(
            value = codigoIngresado,
            onValueChange = { nuevo ->
                if (nuevo.length <= 6) {
                    codigoIngresado = nuevo.uppercase()
                }
            },
            label = { Text("Ejemplo: XK92AB") },
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

        Text(
            text = "${codigoIngresado.length}/6",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.align(Alignment.End)
        )

        Spacer(Modifier.height(16.dp))

        /**
         * Muestra avisos si el código introducido no es válido o ya está en uso
         */
        if (uiState.error != null) {
            Text(
                text = uiState.error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
        }

        /**
         * Botón principal para realizar la unión definitiva de las dos cuentas
         */
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
                Text("Vincularme con mi pareja")
            }
        }
    }
}
