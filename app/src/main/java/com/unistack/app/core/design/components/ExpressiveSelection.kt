package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
 * Pulsación expresiva: el elemento se comprime mientras se mantiene pulsado y vuelve con
 * rebote al soltar, el mismo gesto que ya hace el botón principal.
 *
 * Existe porque hay superficies que se tocan y no daban ninguna señal de haberlo notado:
 * usaban un clic sin indicación, pensado para no meter el resalte gris de Material, y al
 * quitarlo se quedaron sin ninguna respuesta. Esto devuelve la respuesta sin ese resalte.
 *
 * Devuelve el modificador ya con el clic puesto para que quien lo use no pueda olvidarse de
 * conectar la fuente de interacción, que es de donde sale el estado de pulsado.
 */
@Composable
fun Modifier.expressivePress(
    pressedScale: Float = 0.97f,
    onClick: () -> Unit
): Modifier {
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && motionEnabled) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "expressive-press-scale"
    )
    return this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
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
