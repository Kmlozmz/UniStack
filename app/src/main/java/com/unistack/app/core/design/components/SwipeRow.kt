package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.core.utils.performSafely
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * Una fila que se arrastra hacia la izquierda para borrar.
 *
 * **«Deslizar en las listas» era un interruptor que no encendía nada.** Se guardaba, se leía en
 * la vista previa y ninguna lista de la app lo miraba: arrastrar una fila no hacía
 * absolutamente nada en Tareas ni en Gastos.
 *
 * Va con el `SwipeToDismissBox` de Material y no con un `pointerInput` propio, por lo de
 * siempre: trae el umbral, la resistencia al final del recorrido y el comportamiento con lector
 * de pantalla, que a mano salen mal.
 *
 * **El arrastre no borra: pregunta.** `confirmValueChange` devuelve `false` a propósito, así que
 * la fila vuelve a su sitio y lo que se abre es el mismo diálogo de confirmación que ya usa el
 * botón de borrar. Borrar de un gesto —sin red— es exactamente el accidente que pasa con el
 * móvil en el bolsillo.
 *
 * @param apagado para las filas que no se pueden borrar; el gesto se desactiva sin que quien
 *   llama tenga que envolver la fila de otra manera.
 */
@Composable
fun FilaDeslizable(
    onBorrar: () -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    containerColor: Color = Color.Unspecified,
    apagado: Boolean = false,
    contenido: @Composable () -> Unit
) {
    val activo = motionActual().swipeGestures && !apagado
    if (!activo) {
        Box(modifier = modifier) { contenido() }
        return
    }

    val haptica = LocalHapticFeedback.current
    val rojo = LocalSectionColors.current.expenses
    val estado = rememberSwipeToDismissBoxState(
        confirmValueChange = { valor ->
            if (valor == SwipeToDismissBoxValue.EndToStart) {
                haptica.performSafely(HapticFeedbackType.LongPress)
                onBorrar()
            }
            false
        }
    )

    SwipeToDismissBox(
        state = estado,
        modifier = modifier.clip(shape),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            // El fondo solo es visible cuando el usuario realmente está deslizando
            val esDeslizando = estado.dismissDirection == SwipeToDismissBoxValue.EndToStart
            val progreso = if (esDeslizando) estado.progress.coerceIn(0f, 1f) else 0f
            if (progreso > 0.05f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(shape)
                        .background(rojo.copy(alpha = 0.20f * progreso))
                        .padding(end = 20.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.common_delete),
                        tint = rojo.copy(alpha = progreso)
                    )
                }
            }
        }
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = shape,
            color = if (containerColor != Color.Unspecified) containerColor else Color.Transparent
        ) {
            contenido()
        }
    }
}
