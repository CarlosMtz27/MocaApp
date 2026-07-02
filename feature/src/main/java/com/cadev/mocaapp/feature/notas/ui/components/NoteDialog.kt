package com.cadev.mocaapp.feature.notas.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

/**
 * DIÁLOGO DE NOTAS COMPARTIDAS (GLASSMORPHIC PREMIUM)
 * Fiel al diseño del HTML, con efecto de nota adhesiva y glassmorphism.
 */
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.isSystemInDarkTheme
import com.cadev.mocaapp.core.utils.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDialog(
    onDismiss: () -> Unit,
    notaPareja: String?,
    miNotaBorrador: String,
    onBorradorChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onNudge: () -> Unit = {}
) {
    val isDark = isSystemInDarkTheme() || ThemeManager.isDarkTheme
    val surfaceColor = if (isDark) Color(0xFF1E1B14) else MocaSurface
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = surfaceColor.copy(alpha = 0.95f),
        scrimColor = MocaOnSurface.copy(alpha = 0.32f),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 8.dp)
                    .width(48.dp)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color.White.copy(alpha = 0.1f) else MocaOutlineVariant.copy(alpha = 0.6f))
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(horizontal = 24.dp)
        ) {
            // Cabecera
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = onSurfaceColor)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Notas de Pareja",
                        style = OrganicTypography.headlineMedium.copy(
                            fontSize = 20.sp, 
                            fontWeight = FontWeight.SemiBold,
                            color = primaryColor
                        )
                    )
                    Text(
                        text = "Espacio Compartido",
                        style = OrganicTypography.labelSmall.copy(
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                    )
                }
                IconButton(onClick = { /* Acción de favorito */ }) {
                    Icon(Icons.Default.Favorite, contentDescription = "Favorito", tint = primaryColor)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Sección 1: Nota de la pareja
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically, 
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Favorite, 
                            contentDescription = null, 
                            tint = primaryColor, 
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Nota de tu pareja",
                            style = OrganicTypography.headlineMedium.copy(
                                fontSize = 18.sp, 
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                        )
                    }

                    PartnerNoteCard(texto = notaPareja, onNudge = onNudge, isDark = isDark)
                }

                // Divisor decorativo
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    HorizontalDivider(color = (if (isDark) Color.White.copy(alpha = 0.1f) else MocaOutlineVariant.copy(alpha = 0.5f)))
                    Surface(
                        color = surfaceColor.copy(alpha = 0.9f),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .border(1.dp, Color.White.copy(alpha = 0.1f), CircleShape),
                        shape = CircleShape
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = null,
                            tint = primaryColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp).padding(4.dp)
                        )
                    }
                }

                // Sección 2: Tu editor de notas
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Tu nota",
                        style = OrganicTypography.headlineMedium.copy(
                            fontSize = 18.sp, 
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    )

                    StickyNoteEditor(
                        texto = miNotaBorrador,
                        onTextoChange = onBorradorChange,
                        isDark = isDark
                    )
                }
                
                Spacer(Modifier.height(24.dp))
            }

            // Footer
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = CircleShape,
                    border = BorderStroke(1.dp, (if (isDark) Color.White.copy(alpha = 0.2f) else MocaOutline.copy(alpha = 0.5f)))
                ) {
                    Text(
                        "Cancelar", 
                        style = OrganicTypography.labelMedium.copy(color = onSurfaceColor)
                    )
                }
                Button(
                    onClick = onGuardar,
                    modifier = Modifier
                        .weight(2f)
                        .height(56.dp)
                        .shadow(8.dp, CircleShape, spotColor = primaryColor),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDark) listOf(primaryColor, Color(0xFF865364)) else listOf(MocaPrimary, MocaAccentPink),
                                    start = Offset(0f, 0f),
                                    end = Offset(400f, 400f)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, 
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp), tint = (if (isDark) Color.Black else Color.White))
                            Text(
                                "Guardar Nota", 
                                style = OrganicTypography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = (if (isDark) Color.Black else Color.White)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PartnerNoteCard(texto: String?, onNudge: () -> Unit, isDark: Boolean) {
    val gradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(Color(0xFF2D342D).copy(alpha = 0.6f), Color(0xFF1E1B14).copy(alpha = 0.3f))
        } else {
            listOf(Color(0xFFC8E6FF).copy(alpha = 0.4f), Color(0xFFB4D2F0).copy(alpha = 0.1f))
        }
    )
    
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurfaceVariant

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 120.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(gradient)
            .border(1.dp, (if (isDark) Color.White.copy(alpha = 0.05f) else Color.White.copy(alpha = 0.6f)), RoundedCornerShape(20.dp))
            .clickable { if (texto.isNullOrBlank()) onNudge() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (texto.isNullOrBlank()) "Tu pareja aún no ha dejado una nota." else texto,
                style = OrganicTypography.bodyLarge.copy(
                    fontSize = 17.sp,
                    color = onSurfaceColor,
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            )
            
            if (texto.isNullOrBlank()) {
                Text(
                    text = "Avisarle".uppercase(),
                    style = OrganicTypography.labelSmall.copy(
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun StickyNoteEditor(
    texto: String,
    onTextoChange: (String) -> Unit,
    isDark: Boolean
) {
    val noteBgColor = if (isDark) Color(0xFF374C37).copy(alpha = 0.6f) else Color(0xFFFCF4CD).copy(alpha = 0.8f)
    val primaryColor = if (isDark) Color(0xFFE7BBC6) else MocaPrimary
    val onSurfaceColor = if (isDark) Color(0xFFEEE7DB) else MocaOnSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(noteBgColor)
            .border(1.dp, if (isDark) Color.White.copy(alpha = 0.05f) else Color.Transparent, RoundedCornerShape(16.dp))
    ) {
        BasicTextField(
            value = texto,
            onValueChange = onTextoChange,
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            textStyle = OrganicTypography.bodyLarge.copy(
                color = onSurfaceColor,
                fontSize = 17.sp,
                lineHeight = 24.sp
            ),
            cursorBrush = SolidColor(primaryColor),
            decorationBox = { innerTextField ->
                if (texto.isEmpty()) {
                    Text(
                        text = "Escribe algo lindo para que lo encuentre...",
                        style = OrganicTypography.bodyLarge.copy(
                            color = onSurfaceColor.copy(alpha = 0.4f),
                            fontSize = 17.sp
                        )
                    )
                }
                innerTextField()
            }
        )
    }
}
