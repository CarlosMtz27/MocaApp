package com.cadev.mocaapp.feature.perfil.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.auth.domain.model.Usuario
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilParejaScreen(
    viewModel: PerfilViewModel,
    parejaId: String,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val pareja = uiState.pareja

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pareja?.nombre ?: "Perfil de pareja") },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Regresar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { padding ->

        if (pareja == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        "Cargando perfil...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.5f)
                    )
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
        ) {
            //Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(top = 32.dp, bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Foto de perfil
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .border(
                                3.dp,
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .background(
                                MaterialTheme.colorScheme.primary
                                    .copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!pareja.fotoPerfil.isNullOrBlank()) {
                            AsyncImage(
                                model = pareja.fotoPerfil,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = pareja.nombre.firstOrNull()
                                    ?.uppercase() ?: "?",
                                fontSize = 40.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = pareja.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Filled.Favorite,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Mi pareja",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            //Stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Dias juntos (compartido)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("💕", fontSize = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.diasJuntos.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Días juntos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }

                // Entradas compartidas con el usuario actual
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📖", fontSize = 28.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = uiState.entradasPareja.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "Recuerdos",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                            .copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            //Fecha aniversario
            if (uiState.fechaRelacion != null) {
                val formatoLegible = SimpleDateFormat(
                    "d 'de' MMMM, yyyy", Locale("es", "MX")
                )
                val fechaVisible = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    formatoLegible.format(sdf.parse(uiState.fechaRelacion!!)!!)
                        .replaceFirstChar { it.uppercase() }
                } catch (e: Exception) { uiState.fechaRelacion!! }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🗓️", fontSize = 24.sp)
                    Column {
                        Text(
                            "Juntos desde",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                .copy(alpha = 0.7f)
                        )
                        Text(
                            text = fechaVisible,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}