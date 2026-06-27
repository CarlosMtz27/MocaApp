package com.cadev.mocaapp.feature.home.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun QuickActionsGrid(
    onChatClick: () -> Unit,
    onDiarioClick: () -> Unit,
    onTestsClick: () -> Unit,
    onEventoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                icon = Icons.Rounded.ChatBubble,
                label = "Chat",
                iconColor = Color(0xFF2196F3),
                backgroundColor = Color(0xFF2196F3).copy(alpha = 0.15f),
                onClick = onChatClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Rounded.Book,
                label = "Diario",
                iconColor = Color(0xFFE91E63),
                backgroundColor = Color(0xFFE91E63).copy(alpha = 0.15f),
                onClick = onDiarioClick,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QuickActionButton(
                icon = Icons.Rounded.Quiz,
                label = "Tests",
                iconColor = Color(0xFF4CAF50),
                backgroundColor = Color(0xFF4CAF50).copy(alpha = 0.15f),
                onClick = onTestsClick,
                modifier = Modifier.weight(1f)
            )
            QuickActionButton(
                icon = Icons.Rounded.Event,
                label = "Evento",
                iconColor = Color(0xFFFF9800),
                backgroundColor = Color(0xFFFF9800).copy(alpha = 0.15f),
                onClick = onEventoClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    iconColor: Color,
    backgroundColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animación "Squishy" (Escala 0.92 al presionar)
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = tween(durationMillis = 400),
        label = "SquishyAnimation"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .height(100.dp)
            // Aplicamos el efecto neumórfico exacto del HTML
            .neumorphicShadow(
                shape = RoundedCornerShape(24.dp),
                offset = 8.dp,
                blur = 16.dp
            )
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, // Desactivamos el ripple nativo para usar el efecto neumórfico
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4F4446),
                letterSpacing = 0.7.sp
            )
        }
    }
}
