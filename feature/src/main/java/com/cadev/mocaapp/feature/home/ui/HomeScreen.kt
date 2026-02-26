package com.cadev.mocaapp.feature.home.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun HomeScreen() {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // Estado local para los datos del usuario
    var nombreUsuario by remember { mutableStateOf("") }
    var diasJuntos by remember { mutableStateOf(0L) }
    var miCodigo by remember { mutableStateOf("") }
    var tienePareja by remember { mutableStateOf(false) }

    // Cargar datos al entrar a la pantalla
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        val doc = firestore.collection("usuarios").document(uid).get().await()

        nombreUsuario = doc.getString("nombre") ?: ""
        miCodigo = doc.getString("codigoPareja") ?: ""
        tienePareja = doc.getString("parejaId") != null

        // Calcular días juntos si tiene pareja
        val relacionId = doc.getString("relacionId")
        if (relacionId != null) {
            val relacion = firestore
                .collection("relaciones")
                .document(relacionId)
                .get()
                .await()

            val fechaInicio = relacion.getDate("fechaInicio")
            if (fechaInicio != null) {
                val diff = System.currentTimeMillis() - fechaInicio.time
                diasJuntos = diff / (1000 * 60 * 60 * 24)
            } else {
                // para cuando todavia no se ha confirmado la fecha
                diasJuntos = 0
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 32.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        //Saludo
        Text(
            text = "Hola, $nombreUsuario 👋",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        //Tarjeta días juntos
        if (tienePareja) {
            TarjetaDiasJuntos(diasJuntos = diasJuntos)
        } else {
            TarjetaSinPareja(miCodigo = miCodigo)
        }

        //Accesos rapidos
        Text(
            text = "Accesos rápidos",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccesoRapido(
                emoji = "📝",
                titulo = "Nuevo día",
                modifier = Modifier.weight(1f)
            )
            AccesoRapido(
                emoji = "📅",
                titulo = "Evento",
                modifier = Modifier.weight(1f)
            )
            AccesoRapido(
                emoji = "🗒️",
                titulo = "Nota",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))

        HorizontalDivider()

        Spacer(Modifier.height(16.dp))

        // Botón temporal de logout
        TextButton(
            onClick = {
                FirebaseAuth.getInstance().signOut()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Cerrar sesión (temporal)",
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}



//Componentes privados

@Composable
private fun TarjetaDiasJuntos(diasJuntos: Long) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "💕", fontSize = 40.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "$diasJuntos",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 64.sp
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "días juntos",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TarjetaSinPareja(miCodigo: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Aún no tienes pareja vinculada",
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Tu código: $miCodigo",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 4.sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Compártelo con tu pareja para vincularse",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun AccesoRapido(
    emoji: String,
    titulo: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 28.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = titulo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
