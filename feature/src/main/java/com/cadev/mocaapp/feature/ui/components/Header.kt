package com.cadev.mocaapp.feature.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.theme.*

/**
 * BARRA SUPERIOR (HEADER) GLASSMORPHIC
 * Adaptación fiel del header HTML.
 */
@Composable
fun MocaHeader(
    titulo: String,
    alVolver: () -> Unit,
    alClickNotificaciones: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(Color.White.copy(alpha = 0.2f))
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = MocaPrimary,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = alVolver)
                    .padding(4.dp)
            )
            
            Text(
                text = titulo,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MocaPrimary
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MocaPrimaryContainer)
                .clickable(onClick = alClickNotificaciones),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "Notificaciones",
                tint = MocaOnPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
