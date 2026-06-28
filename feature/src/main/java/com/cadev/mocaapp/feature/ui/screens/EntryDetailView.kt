package com.cadev.mocaapp.feature.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.feature.diario.domain.model.Comentario
import com.cadev.mocaapp.feature.diario.ui.DiarioViewModel
import com.cadev.mocaapp.feature.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

import com.cadev.mocaapp.feature.ui.components.*

/**
 * VISTA DE LECTURA DE ENTRADA (SECCIÓN 4.4)
 * Fiel al diseño "Memories - Organic Minimalist" con textos en español.
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
                        "Detalle del Recuerdo",
                        style = OrganicTypography.headlineMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        color = MocaOnSurface,
                        modifier = Modifier.fillMaxWidth().padding(end = 48.dp),
                        textAlign = TextAlign.Center
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onRegresar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MocaOnSurface)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MocaSurface)
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
        containerColor = MocaSurface
    ) { padding ->
        if (entrada == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MocaPrimary)
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
                                .shadow(8.dp, RoundedCornerShape(16.dp)) // Sombra para efecto flotante
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
                        color = MocaPrimary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                    Text(
                        text = entrada.titulo,
                        style = OrganicTypography.headlineLarge,
                        color = MocaOnSurface,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 12.dp, end = 12.dp, bottom = 24.dp), // Ajustado para evitar cortes
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusChip(
                            icon = Icons.Outlined.ChatBubbleOutline,
                            text = "${comentarios.size} Comentarios",
                            containerColor = MocaSurfaceContainerHigh
                        )
                        if (entrada.compartida) {
                            val textoCompartido = if (entrada.usuarioId == usuarioId) "Compartido con tu pareja" else "Compartido por tu pareja"
                            Spacer(Modifier.width(8.dp))
                            StatusChip(
                                icon = Icons.Default.Favorite,
                                text = textoCompartido,
                                containerColor = MocaTertiaryContainer,
                                contentColor = MocaOnTertiaryContainer,
                                fillIcon = true
                            )
                        }
                    }
                    
                    HorizontalDivider(color = MocaSurfaceVariant)
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
    contentColor: Color = MocaOnSurfaceVariant,
    fillIcon: Boolean = false
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        modifier = Modifier.height(32.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp), // Reducido de 16 a 12
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp) // Reducido de 8 a 6
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp), // Reducido de 18 a 16
                tint = contentColor
            )
            Text(
                text = text,
                style = OrganicTypography.labelMedium.copy(
                    fontSize = 12.sp, // Reducido de 14 a 12 para que quepa mejor
                    fontWeight = FontWeight.SemiBold
                ),
                color = contentColor,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CommentItem(comentario: Comentario) {
    val tiempoHace = remember(comentario.creadoEn) {
        val date = comentario.creadoEn.toDate()
        val diff = Date().time - date.time
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        when {
            hours < 1 -> "Ahora mismo"
            hours < 24 -> "Hace $hours h"
            else -> SimpleDateFormat("d 'de' MMM", Locale.forLanguageTag("es-MX")).format(date)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = MocaSurfaceContainerHigh
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    comentario.nombreUsuario.take(1).uppercase(),
                    style = OrganicTypography.labelMedium,
                    color = MocaOnSurfaceVariant
                )
            }
        }
        
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = comentario.nombreUsuario,
                    style = OrganicTypography.labelMedium,
                    color = MocaOnSurface
                )
                Text(
                    text = tiempoHace,
                    style = TextStyle(fontSize = 12.sp, color = MocaOutline)
                )
            }
            Text(
                text = comentario.texto,
                style = OrganicTypography.bodyMedium,
                color = MocaOnSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun BottomCommentBar(
    nuevoComentario: String,
    onTextoChange: (String) -> Unit,
    onEnviar: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding(),
        color = MocaSurfaceContainerLowest,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, MocaSurfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = MocaSurfaceVariant
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(20.dp), tint = MocaOnSurfaceVariant)
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(MocaSurfaceContainerLow, CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (nuevoComentario.isEmpty()) {
                    Text(
                        text = "Escribe un comentario...",
                        style = OrganicTypography.bodyMedium.copy(fontSize = 14.sp),
                        color = MocaOutline
                    )
                }
                
                BasicTextField(
                    value = nuevoComentario,
                    onValueChange = onTextoChange,
                    textStyle = OrganicTypography.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = MocaOnSurface
                    ),
                    cursorBrush = SolidColor(MocaAccentPink),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { innerTextField ->
                        innerTextField()
                    }
                )
            }
            
            IconButton(
                onClick = onEnviar,
                enabled = nuevoComentario.isNotBlank(),
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (nuevoComentario.isNotBlank()) MocaAccentPink else MocaSurfaceVariant.copy(alpha = 0.5f),
                        CircleShape
                    )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar",
                    tint = if (nuevoComentario.isNotBlank()) Color.White else MocaOutline,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
