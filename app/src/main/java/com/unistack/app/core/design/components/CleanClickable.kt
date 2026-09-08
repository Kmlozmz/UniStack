package com.unistack.app.core.design.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import com.unistack.app.core.design.theme.hayMovimiento

/**
 * Clic con el gesto de pulsación de Material 3 Expressive.
 *
 * Al mantener presionado un elemento con esquinas redondeadas, sus esquinas se aprietan
 * temporalmente hacia una geometría más rectangular (morphing de pressedShape), recuperando
 * su curvatura de reposo con muelle al soltarlo, acompañado de la onda de Material (ripple).
 */
@Composable
fun Modifier.cleanClickable(shape: Shape = RectangleShape, onClick: () -> Unit): Modifier {
    val fuente = remember { MutableInteractionSource() }
    val pulsado by fuente.collectIsPressedAsState()
    val enMovimiento = hayMovimiento()

    val factorForma by animateFloatAsState(
        targetValue = if (pulsado && enMovimiento) 0.5f else 1f,
        animationSpec = spring(dampingRatio = 0.60f, stiffness = Spring.StiffnessMediumLow),
        label = "pressCornerMorph"
    )

    val formaEfectiva = remember(shape, factorForma) {
        if (shape is RoundedCornerShape && enMovimiento && factorForma < 0.999f) {
            RoundedCornerShape(
                topStart = shape.topStart.escala(factorForma),
                topEnd = shape.topEnd.escala(factorForma),
                bottomEnd = shape.bottomEnd.escala(factorForma),
                bottomStart = shape.bottomStart.escala(factorForma)
            )
        } else {
            shape
        }
    }

    return this
        .clip(formaEfectiva)
        .clickable(
            interactionSource = fuente,
            indication = if (enMovimiento) LocalIndication.current else null,
            onClick = onClick
        )
}

private fun CornerSize.escala(factor: Float): CornerSize = object : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float =
        this@escala.toPx(shapeSize, density) * factor
    override fun toString(): String = "ScaledCornerSize($factor)"
}

