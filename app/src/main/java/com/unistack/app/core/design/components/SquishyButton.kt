package com.unistack.app.core.design.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.MaterialTheme

/**
 * El [Button] de Material con el gesto «squishy» ya puesto: se comprime bajo el dedo y
 * vuelve con un rebote en vez de quedarse quieto.
 *
 * Reproduce la firma del original a propósito, para que adoptarlo en una pantalla sea
 * cambiar el nombre y nada más. [UniStackButton] ya traía el gesto, pero impone su propio
 * aspecto —relleno, altura, forma— y estas pantallas llevan colores y medidas propias que
 * se perderían al migrarlas; aquí solo se añade el tacto y se respeta el resto.
 *
 * El [MutableInteractionSource] se crea aquí y se pasa al botón: sin compartir el mismo,
 * el gesto no se enteraría de las pulsaciones.
 */
@Composable
fun SquishyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = ButtonDefaults.shape,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    border: BorderStroke? = null,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource? = null,
    content: @Composable RowScope.() -> Unit
) {
    val source = interactionSource ?: remember { MutableInteractionSource() }
    Button(
        onClick = onClick,
        modifier = modifier.squishOnPress(source, enabled = enabled),
        enabled = enabled,
        shape = shape,
        colors = colors,
        elevation = elevation,
        border = border,
        contentPadding = contentPadding,
        interactionSource = source,
        content = content
    )
}
