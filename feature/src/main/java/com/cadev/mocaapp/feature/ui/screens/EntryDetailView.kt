package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

import com.cadev.mocaapp.feature.ui.components.*

/**
 * VISTA DE LECTURA DE ENTRADA (SECCIÓN 4.4)
 * Fiel al diseño "Memories - Organic Minimalist" con textos en español y soporte Modo Oscuro.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailView(
    viewModel: DiarioViewModel,
    entradaId: String,
    usuarioId: String,
    onRegresar: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(entradaId) {
        viewModel.cargarDetalle(entradaId)
        viewModel.cargarNombreUsuario(usuarioId)
    }

    val entrada = uiState.entradaDetalle
    val comentarios = uiState.comentarios

    val fechaFormateada = remember(entrada?.fecha) {
        if (entrada == null) ""
        else try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(entrada.fecha) ?: Date()
            SimpleDateFormat("d 'de' MMMM, yyyy", Locale.forLanguageTag("es-MX")).format(date)
                .replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            entrada.fecha
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Detalle del Recuerdo",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Regresar", 
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            if (entrada != null) {
                ModernCommentInput(
                    nuevoComentario = uiState.nuevoComentario,
                    fotoUsuario = uiState.fotoUsuario,
                    onTextoChange = { viewModel.actualizarNuevoComentario(it) },
                    onEnviar = {
                        val parejaId = if (entrada.usuarioId == usuarioId) entrada.parejaId else entrada.usuarioId
                        viewModel.publicarComentario(usuarioId, uiState.nombreUsuario, parejaId)
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        if (entrada == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // PAGER DE FOTOS
            if (entrada.fotos.isNotEmpty()) {
                item {
                    val pagerState = rememberPagerState(pageCount = { entrada.fotos.size })
                    HorizontalPager(
                        state = pagerState,
                        contentPadding = PaddingValues(horizontal = 32.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier.padding(vertical = 16.dp)
                    ) { page ->
                        AsyncImage(
                            model = entrada.fotos[page],
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.2f)
                                .shadow(8.dp, RoundedCornerShape(16.dp)) 
                                .clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            // INFO CABECERA
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = fechaFormateada,
                        style = OrganicTypography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Text(
                        text = entrada.titulo,
                        style = OrganicTypography.headlineLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            text = "${comentarios.size} Comentarios",
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        if (entrada.compartida) {
                            val textoCompartido = if (entrada.usuarioId == usuarioId) "Compartido con tu pareja" else "Compartido por tu pareja"
                            Spacer(Modifier.width(8.dp))
                            StatusChip(
                                icon = Icons.Default.Favorite,
                                text = textoCompartido,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                fillIcon = true
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }

            // COMENTARIOS
            itemsIndexed(comentarios) { index, comentario ->
                ModernCommentItem(comentario = comentario, index = index)
            }
        }
    }
}

@Composable
fun StatusChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    fillIcon: Boolean = false
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Text(
                text = text,
                style = OrganicTypography.labelMedium.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
