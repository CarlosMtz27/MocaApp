package com.cadev.mocaapp.feature.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val MocaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(16.dp), // 1rem
    large = RoundedCornerShape(32.dp),  // 2rem
    extraLarge = RoundedCornerShape(48.dp) // 3rem
)

val CirculoCompleto = RoundedCornerShape(9999.dp)
