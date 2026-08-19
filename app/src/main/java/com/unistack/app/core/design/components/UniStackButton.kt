package com.unistack.app.core.design.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.UniStackColors

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.contentColorOn
/** Variantes visuales del botón, en la jerarquía de énfasis de Material. */
enum class UniStackButtonVariant {
    /** Relleno con el color de acento. Acción principal de la pantalla. */
    Filled,

    /** Relleno suave sobre el contenedor del acento. Acción secundaria. */
    Tonal,

    /** Solo contorno. Acción terciaria o alternativa. */
    Outlined
}

private val PressedScale = 0.96f
private val RestingRadius = 28.dp
private val PressedRadius = 14.dp

/**
 * Botón único de la app, con el gesto "squishy" de Material 3 Expressive: al pulsar se
 * comprime y sus esquinas se cuadran, y al soltar vuelve con un rebote elástico.
 *
 * Sustituye a la familia de botones duplicados que había por pantalla. El color de
 * contenido nunca se asume: sale de los roles `on*` del tema, así que sigue siendo legible
 * con cualquier acento (incluido el pastel que Monet entrega en modo oscuro).
 */
@Composable
fun UniStackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UniStackButtonVariant = UniStackButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    height: Dp = 56.dp,
    /**
     * Color de relleno propio, para secciones con identidad cromática (por ejemplo el coral
     * de Gastos). El contenido se calcula sobre él, así que sigue siendo legible.
     * Si es null se usa el color que corresponda a [variant].
     */
    containerColor: Color? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    // Con el movimiento desactivado el botón no se deforma, solo cambia de color.
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val squish = pressed && enabled && motionEnabled

    val cornerRadius by animateDpAsState(
        targetValue = if (squish) PressedRadius else RestingRadius,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessMedium),
        label = "button-corner"
    )

    val containerTarget = when {
        !enabled -> MaterialTheme.colorScheme.surfaceContainerHigh
        containerColor != null -> containerColor
        variant == UniStackButtonVariant.Filled -> MaterialTheme.colorScheme.primary
        variant == UniStackButtonVariant.Tonal -> MaterialTheme.colorScheme.primaryContainer
        else -> Color.Transparent
    }
    val contentTarget = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant
        containerColor != null -> contentColorOn(containerColor)
        variant == UniStackButtonVariant.Filled -> MaterialTheme.colorScheme.onPrimary
        variant == UniStackButtonVariant.Tonal -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.primary
    }
    val colorSpec = spring<Color>(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
    val containerColor by animateColorAsState(containerTarget, colorSpec, label = "button-container")
    val contentColor by animateColorAsState(contentTarget, colorSpec, label = "button-content")

    val shape = RoundedCornerShape(cornerRadius)

    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        ),
        border = if (variant == UniStackButtonVariant.Outlined) {
            BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
        } else {
            null
        },
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .squishOnPress(interactionSource, enabled = enabled, scale = PressedScale)
            .clip(shape)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(21.dp)
                )
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    color = contentColor,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingIcon != null) {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
