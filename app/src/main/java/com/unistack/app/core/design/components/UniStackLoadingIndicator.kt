package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Que algo está en marcha.
 *
 * Lo dibuja [ContainedLoadingIndicator], el indicador de Material 3 Expressive: una forma que
 * va transformándose en otra —de siete lados a nueve, a estrella— dentro de un contenedor.
 * Antes era una rueda dibujada a mano con su propia animación de periodo fijo; la de Material
 * usa las formas del sistema y el `MotionScheme` del tema.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UniStackLoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    ContainedLoadingIndicator(
        modifier = modifier.size(size),
        indicatorColor = color,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    )
}
