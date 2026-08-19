package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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

/**
 * El botón ancho de la app: el que ocupa el pie de un formulario o cierra un paso del alta.
 *
 * Cada variante es el botón de Material que le corresponde —[Button], [FilledTonalButton] y
 * [OutlinedButton]—, sin nada encima. Antes esto era un único `Button` al que se le pintaban
 * a mano el relleno, el contorno y el contenido de las tres variantes, más un gesto de
 * compresión con su propia animación y unas esquinas que se cuadraban al pulsar.
 *
 * Todo eso lo hacen ya los botones de Material 3 Expressive: la respuesta al pulsar sale del
 * `MotionScheme` del tema y la forma, de la escala de formas. También se fue la altura fija de
 * 56dp; la de Material se adapta al tamaño de letra del sistema, que es lo que quiere quien
 * pone el texto en grande.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun UniStackButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UniStackButtonVariant = UniStackButtonVariant.Filled,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    /**
     * Color de relleno propio, para secciones con identidad cromática (por ejemplo el rojo de
     * Gastos). El contenido se calcula sobre él, así que sigue siendo legible.
     * Si es null se usa el color que corresponda a [variant].
     */
    containerColor: Color? = null
) {
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (leadingIcon != null) {
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
            }
        }
    }

    val width = modifier.fillMaxWidth()

    when {
        containerColor != null -> Button(
            onClick = onClick,
            modifier = width,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColorOn(containerColor)
            )
        ) { content() }

        variant == UniStackButtonVariant.Filled ->
            Button(onClick = onClick, modifier = width, enabled = enabled) { content() }

        variant == UniStackButtonVariant.Tonal ->
            FilledTonalButton(onClick = onClick, modifier = width, enabled = enabled) { content() }

        else ->
            OutlinedButton(onClick = onClick, modifier = width, enabled = enabled) { content() }
    }
}
