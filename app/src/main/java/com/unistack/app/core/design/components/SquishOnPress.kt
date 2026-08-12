package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import com.unistack.app.core.design.theme.LocalMotionDurationScale

/** Cuánto se comprime un elemento pulsado. */
const val SQUISH_PRESSED_SCALE = 0.96f

/**
 * El gesto «squishy» de Material 3 Expressive: al pulsar, el elemento se comprime; al
 * soltar, vuelve con un rebote elástico en vez de un corte seco.
 *
 * Vivía dentro de [UniStackButton] y no se podía usar en nada más, así que el resto de la
 * app —barra de navegación, tarjetas pulsables, chips— respondía al toque sin moverse.
 * Aquí es un modificador para que cualquier cosa pulsable pueda tener el mismo tacto.
 *
 * Respeta la preferencia de movimiento: con las animaciones desactivadas no deforma nada,
 * porque un elemento que se encoge bajo el dedo es justo lo que quiere evitar quien pide
 * movimiento reducido.
 *
 * @param interactionSource el mismo que se le pasa al `clickable`, o no habrá nada que oír.
 * @param scale hasta dónde comprime. Los elementos pequeños necesitan menos para notarse.
 */
fun Modifier.squishOnPress(
    interactionSource: InteractionSource,
    enabled: Boolean = true,
    scale: Float = SQUISH_PRESSED_SCALE
): Modifier = composed {
    val pressed by interactionSource.collectIsPressedAsState()
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val squish = pressed && enabled && motionEnabled

    val animatedScale by animateFloatAsState(
        targetValue = if (squish) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "squish"
    )

    graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}

/** Versión para cuando el llamador ya sabe si está pulsado. */
@Composable
fun Modifier.squishOnPress(
    pressed: Boolean,
    enabled: Boolean = true,
    scale: Float = SQUISH_PRESSED_SCALE
): Modifier {
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val squish = pressed && enabled && motionEnabled
    val animatedScale by animateFloatAsState(
        targetValue = if (squish) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "squish"
    )
    return graphicsLayer {
        scaleX = animatedScale
        scaleY = animatedScale
    }
}
