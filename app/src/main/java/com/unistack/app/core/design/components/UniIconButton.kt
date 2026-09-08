@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

/** Cuánto pesa visualmente un botón de icono, en la jerarquía de Material. */
enum class UniIconButtonVariant {
    /** Sin fondo. El de una barra superior: volver, buscar, más opciones. */
    Standard,

    /** Fondo suave del acento. Una acción que quieres que se vea, sin ser la principal. */
    Tonal,

    /** Solo contorno. Alterna con el tonal cuando dos van juntos y hay que distinguirlos. */
    Outlined
}

/**
 * Un botón que es solo un icono, con el tamaño y la forma que manda el sistema de diseño.
 *
 * **El tamaño sale de Material, no de un número escrito a mano.** Antes había cinco medidas
 * distintas repartidas por la app —38, 40, 48, 34 y 32 dp— y ninguna era un token: eran ajustes
 * a ojo para que cuadrara cada pantalla. El resultado es que el mismo botón de «más opciones»
 * medía distinto en Tareas que en Materias.
 *
 * Aquí va el tamaño `small` de Material: 40dp de contenedor con el icono a la medida que le
 * corresponde. Es lo bastante grande para el dedo —Android pide 48dp de área táctil, y el
 * componente la añade por fuera del contenedor visible— sin ocupar como un botón de verdad.
 *
 * La forma es redonda. La cuadrada existe en Material y se ve bien en un grupo de varios
 * pegados; suelto, un cuadrado redondeado en una barra parece un botón a medio pintar.
 */
@Composable
fun UniIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: UniIconButtonVariant = UniIconButtonVariant.Standard,
    enabled: Boolean = true
) {
    /*
     * El dibujo se queda igual; lo que crece es lo que responde al dedo.
     *
     * `sizeIn` va despues del `size`, asi que impone un minimo sin cambiar la forma pintada:
     * el boton se ve del mismo tamano y acierta mas. Con «Maximo» son 64dp, que es lo que pide
     * quien tiene temblor o usa el telefono en movimiento.
     */
    val sized = Modifier.size(IconButtonDefaults.smallContainerSize()).areaDeToqueMinima()
    val shapes = IconButtonDefaults.shapes()
    val glyph: @Composable () -> Unit = {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(IconButtonDefaults.smallIconSize)
        )
    }

    /*
     * El rótulo aparece al mantener pulsado.
     *
     * Un botón que es solo un dibujo depende de que se adivine que significa. `contentDescription`
     * ya lo decía, pero solo en voz alta: quien no usa lector de pantalla no tenía forma de
     * comprobarlo sin pulsarlo y ver qué pasaba. El tooltip enseña ese mismo texto, así que no
     * hay un segundo rótulo que mantener al día.
     *
     * Es «plain» y no «rich»: aquí solo hace falta el nombre de la acción. El rico trae título,
     * párrafo y botónes, y para eso ya están los díalogos de ayuda.
     */
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(
            TooltipAnchorPosition.Above
        ),
        tooltip = {
            if (contentDescription != null) {
                PlainTooltip { Text(contentDescription) }
            }
        },
        state = rememberTooltipState(),
        // Sin descripción no hay nada que enseñar, y un recuadro vacío al mantener pulsado
        // parece que la app se ha roto.
        enableUserInput = contentDescription != null,
        modifier = modifier
    ) {
    when (variant) {
        UniIconButtonVariant.Standard -> IconButton(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shapes = shapes
        ) { glyph() }

        UniIconButtonVariant.Tonal -> FilledTonalIconButton(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shapes = shapes
        ) { glyph() }

        UniIconButtonVariant.Outlined -> OutlinedIconButton(
            onClick = onClick,
            modifier = sized,
            enabled = enabled,
            shapes = shapes
        ) { glyph() }
    }
    }
}

/**
 * Botón de retroceso estándar con contorno (Outlined) de Material 3 Expressive.
 *
 * Enmarca la flecha dentro de una pastilla o círculo con borde sutil, evitando que quede
 * como una flecha flotante sin contenedor visual.
 */
@Composable
fun UniBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    contentDescription: String = "Volver"
) {
    UniIconButton(
        icon = icon,
        contentDescription = contentDescription,
        onClick = onClick,
        modifier = modifier,
        variant = UniIconButtonVariant.Outlined
    )
}
