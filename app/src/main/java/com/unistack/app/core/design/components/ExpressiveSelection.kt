package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import androidx.compose.runtime.getValue

/**
 * Reacción de selección de Material 3 Expressive: lo seleccionado crece un poco con un
 * rebote elástico en lugar de limitarse a cambiar de color.
 *
 * Se anula si el usuario desactivó las animaciones.
 */
@Composable
fun Modifier.expressiveSelection(
    selected: Boolean,
    selectedScale: Float = 1.03f
): Modifier {
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val target = if (selected && motionEnabled) selectedScale else 1f
    val scale by animateFloatAsState(
        targetValue = target,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expressive-selection-scale"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Forma que se redondea al seleccionarse, el otro gesto característico de Expressive.
 *
 * Parte del radio de la forma `large` del tema y le suma unos grados al seleccionarse. Antes
 * partía de la preferencia de esquinas del usuario; esa preferencia ya no existe, porque la
 * escala de formas la define el sistema de diseño.
 */
@Composable
fun rememberSelectionShape(
    selected: Boolean,
    extraRadiusWhenSelected: Dp = 8.dp
): RoundedCornerShape {
    val base = 28.dp
    val radius by animateDpAsState(
        targetValue = if (selected) base + extraRadiusWhenSelected else base,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "expressive-selection-shape"
    )
    return RoundedCornerShape(radius)
}
