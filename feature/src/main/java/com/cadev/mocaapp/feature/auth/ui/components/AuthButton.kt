package com.cadev.mocaapp.feature.auth.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cadev.mocaapp.feature.ui.animations.CorazonesOrbitando

@Composable
fun BotonAutenticacion(
    texto: String,
    alHacerClick: () -> Unit,
    estaCargando: Boolean = false,
    habilitado: Boolean = true,
    modificador: Modifier = Modifier,
    icono: ImageVector? = null,
    coloresDegradado: List<Color> = listOf(Color(0xFFFFDAB9), Color(0xFFFFD1DC))
) {
    val degradado = Brush.horizontalGradient(colors = coloresDegradado)

    Button(
        onClick = alHacerClick,
        enabled = habilitado && !estaCargando,
        modifier = modificador
            .fillMaxWidth()
            .height(64.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                spotColor = coloresDegradado.last().copy(alpha = 0.4f)
            ),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(degradado)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (estaCargando) {
                CorazonesOrbitando(modifier = Modifier.size(40.dp))
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (icono != null) {
                        Icon(
                            imageVector = icono,
                            contentDescription = null,
                            tint = Color(0xFF5E3E47),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    Text(
                        text = texto.uppercase(),
                        color = Color(0xFF5E3E47),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}
