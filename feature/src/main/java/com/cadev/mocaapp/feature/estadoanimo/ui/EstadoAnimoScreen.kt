package com.cadev.mocaapp.feature.estadoanimo.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.estadoanimo.domain.model.EMOJIS_DISPONIBLES
import com.cadev.mocaapp.feature.perfil.ui.PerfilViewModel

@Composable
fun EstadoAnimoScreen(
    viewModel: EstadoAnimoViewModel,
    perfilViewModel: PerfilViewModel,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val perfilState by perfilViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    val usuario = perfilState.usuario
    val pareja = perfilState.pareja
    val relacionId = usuario?.relacionId ?: ""

    LaunchedEffect(usuario, pareja) {
        if (usuario != null && pareja != null) {
            viewModel.cargarEstados(relacionId, usuario.id, pareja.nombre)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "¿Cómo te sientes hoy?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = "Tu pareja verá tu estado en su pantalla de inicio y en su widget",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )

            Spacer(Modifier.height(24.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = 240.dp)
            ) {
                items(EMOJIS_DISPONIBLES) { emoji ->
                    val isSelected = uiState.emojiPropio == emoji
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (isSelected) 2.dp else 0.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable {
                                if (usuario != null && pareja != null) {
                                    viewModel.seleccionarEmoji(
                                        context = context,
                                        relacionId = relacionId,
                                        uid = usuario.id,
                                        nombreUsuario = usuario.nombre,
                                        parejaId = pareja.id,
                                        emoji = emoji
                                    )
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 28.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            if (uiState.emojiPareja.isNotBlank()) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "${uiState.nombrePareja} se siente: ",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(text = uiState.emojiPareja, fontSize = 24.sp)
                    }
                }
                Spacer(Modifier.height(24.dp))
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !uiState.guardando
            ) {
                if (uiState.guardando) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                } else {
                    Text("Cerrar")
                }
            }
        }
    }
}
