package com.cadev.mocaapp.feature.eventos.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cadev.mocaapp.core.ui.*
import com.cadev.mocaapp.feature.eventos.ui.components.EventoCard
import com.cadev.mocaapp.feature.ui.screens.LoadingTransition
import com.cadev.mocaapp.feature.ui.theme.MocaPrimary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventosScreen(
    viewModel: EventoViewModel,
    relacionId: String,
    onCrearEvento: () -> Unit,
    onVerEvento: (String) -> Unit,
    onRegresar: () -> Unit
) {
    val isDark = isSystemInDarkTheme() || com.cadev.mocaapp.core.utils.ThemeManager.isDarkTheme
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    LaunchedEffect(relacionId) {
        viewModel.iniciarEscucha(context, relacionId)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .then(if (isDark) Modifier.background(Color(0xFF1E1B14)) else Modifier.meshGradientBackground())
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRegresar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = colorPrimary)
                }
                Text(
                    text = "Nuestros planes",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = colorPrimary,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                IconButton(onClick = { /* Acción de favorito */ }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = if (isDark) colorPrimary else MocaPrimary)
                }
            }

            // Tab Row
            Box(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.4f))
                    .padding(4.dp)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    TabButton(
                        text = "Próximos",
                        isSelected = selectedTab == 0,
                        isDark = isDark,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f)
                    )
                    TabButton(
                        text = "Recuerdos",
                        isSelected = selectedTab == 1,
                        isDark = isDark,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (uiState.cargando) {
                LoadingTransition()
            } else {
                val proximos = viewModel.eventosProximos()
                val pasados = viewModel.eventosPassados()
                val listaAMostrar = if (selectedTab == 0) proximos else pasados

                if (listaAMostrar.isEmpty()) {
                    EmptyEventState(isDark)
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(listaAMostrar) { evento ->
                            EventoCard(
                                evento = evento,
                                pasado = selectedTab == 1,
                                onClick = { onVerEvento(evento.id) }
                            )
                        }
                    }
                }
            }
        }

        // FAB
        LargeFloatingActionButton(
            onClick = onCrearEvento,
            containerColor = if (isDark) colorPrimary else StitchPrimaryContainer,
            contentColor = if (isDark) Color.Black else StitchOnPrimaryContainer,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 120.dp, end = 24.dp)
                .size(64.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Evento", modifier = Modifier.size(32.dp))
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    isDark: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimaryContainer
    val colorOnPrimary = if (isDark) Color.Black else StitchOnPrimaryContainer
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(CircleShape)
            .background(if (isSelected) colorPrimary else Color.Transparent)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) colorOnPrimary else colorOnSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun EmptyEventState(isDark: Boolean) {
    val colorPrimary = if (isDark) Color(0xFFE7BBC6) else StitchPrimary
    val colorOnSurfaceVariant = if (isDark) Color(0xFFD3C3C5) else StitchOnSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape)
                .then(if (isDark) Modifier.background(Color.White.copy(alpha = 0.05f)) else Modifier)
        ) {
            AsyncImage(
                model = "https://lh3.googleusercontent.com/aida-public/AB6AXuDuFPUc-FQkvQqLSomXkh7frjZtkvdF534hF_5s8GDNHxgtzM-3rRrueNsOmBI1uWJ_-JsZRB9n_Hnm3S7IyzEjj6SZ1gP13tZxsD9AFB9cThHb1ZN8EttLHZsu9fgfQego1ykGE_ASbAoiaYWmZLXp6ES0TNRBSaZaXqW0XK9KWKxwdyZzZiEHNnSAmxLbhwQIy4IhKuI_6sDvYOOg7F25xIFIK8eQkxGYWl-SFnZxBG_8Cht8py92kf1E5WMuQYJH-axPoqcEU9to",
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = if (isDark) 0.7f else 1f
            )
        }
        Spacer(Modifier.height(32.dp))
        Text(
            text = "¿Cuál será su próxima aventura?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = FontFamily.Serif,
            color = colorPrimary,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Cree un nuevo evento para planificar momentos inolvidables en nuestro santuario.",
            fontSize = 16.sp,
            color = colorOnSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp)
        )
    }
}
