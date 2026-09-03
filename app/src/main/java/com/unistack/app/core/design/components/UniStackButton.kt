package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
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
 * El botón ancho de la app: el que cierra un formulario o un paso del alta.
 *
 * **Tamaño y forma son los del tamaño «medium» de Material**, que resulta ser exactamente el
 * que tenía la app antes de migrar: 56dp de alto y esquinas de 28dp. Al adoptar Material se
 * quedó con el tamaño por defecto —40dp y forma de píldora— y se veía pequeño y demasiado
 * redondo para lo que es: la acción principal de la pantalla, anclada abajo.
 *
 * **La forma cambia al pulsar.** En reposo lleva la forma «cuadrada» de su tamaño (28dp) y bajo
 * el dedo pasa a la de pulsado (12dp): las esquinas se cierran mientras lo tienes apretado y
 * vuelven a abrirse al soltar. Es el morphing de Material 3 Expressive, y lo hace el propio
 * componente a partir de [ButtonDefaults]; no hay ninguna animación escrita aquí.
 *
 * Nota sobre la dirección: Material aprieta las esquinas al pulsar, no las redondea. Si se
 * quiere al revés —redondear bajo el dedo— basta con intercambiar los dos valores de `shapes`.
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
                Icon(leadingIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.MediumIconSize))
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (trailingIcon != null) {
                Icon(trailingIcon, contentDescription = null, modifier = Modifier.size(ButtonDefaults.MediumIconSize))
            }
        }
    }

    /*
     * La forma y el alto salen de Apariencia, no de aqui.
     *
     * **Estuvieron escritos a mano en este archivo**, asi que los dos ajustes de «Forma de los
     * botones» y «Tamano de los botones» se guardaban y no cambiaban un pixel: el boton pedia
     * siempre `squareShape` y siempre `MediumContainerHeight`. Ahora los dos salen de
     * [UniStackButtonDefaults], que es quien los lee de las preferencias.
     */
    val sized = modifier
        .fillMaxWidth()
        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)

    val shapes = UniStackButtonDefaults.shapes
    val padding = ButtonDefaults.MediumContentPadding

    when {
        containerColor != null -> Button(
            onClick = onClick,
            shapes = shapes,
            modifier = sized,
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = containerColor,
                contentColor = contentColorOn(containerColor)
            ),
            contentPadding = padding
        ) { content() }

        variant == UniStackButtonVariant.Filled -> Button(
            onClick = onClick,
            shapes = shapes,
            modifier = sized,
            enabled = enabled,
            contentPadding = padding
        ) { content() }

        variant == UniStackButtonVariant.Tonal -> FilledTonalButton(
            onClick = onClick,
            shapes = shapes,
            modifier = sized,
            enabled = enabled,
            contentPadding = padding
        ) { content() }

        else -> OutlinedButton(
            onClick = onClick,
            shapes = shapes,
            modifier = sized,
            enabled = enabled,
            contentPadding = padding
        ) { content() }
    }
}
