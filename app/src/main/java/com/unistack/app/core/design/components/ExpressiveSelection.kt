package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.AppearanceRuntime
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.cardRadius

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
 * Parte del radio de tarjeta que corresponda a la preferencia `cornerStyle`, así que sigue
 * respetando el ajuste de esquinas del usuario.
 */
@Composable
fun rememberSelectionShape(
    selected: Boolean,
    extraRadiusWhenSelected: Dp = 8.dp
): RoundedCornerShape {
    val base = AppearanceRuntime.cornerStyle.cardRadius()
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
