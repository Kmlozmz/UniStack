package com.unistack.app.core.design.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

/**
 * Un clic sin la onda de Material.
 *
 * Se usa donde el propio elemento ya responde al toque —una tarjeta que se rellena, un chip
 * que cambia de color— y la onda encima sería una segunda respuesta a la misma acción.
 *
 * Vivía copiado, palabra por palabra, en Gastos, en el formulario de gasto, en Materias y en
 * Tareas: cuatro copias idénticas de seis líneas que nadie iba a mantener a la vez.
 */
@Composable
fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
