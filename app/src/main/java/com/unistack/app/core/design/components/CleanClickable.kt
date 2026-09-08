package com.unistack.app.core.design.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import com.unistack.app.core.design.theme.hayMovimiento

/**
 * Clic limpio con recorte de onda según la forma dada.
 *
 * Aplica el recorte [shape] tanto a la superficie como a la onda táctil (ripple)
 * para garantizar que la indicación respete fielmente el contorno del componente.
 */
@Composable
fun Modifier.cleanClickable(shape: Shape = RectangleShape, onClick: () -> Unit): Modifier {
    val fuente = remember { MutableInteractionSource() }
    val enMovimiento = hayMovimiento()

    return this
        .clip(shape)
        .clickable(
            interactionSource = fuente,
            indication = if (enMovimiento) LocalIndication.current else null,
            onClick = onClick
        )
}

