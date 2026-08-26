@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.unistack.app.core.utils.performSafely
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class UniSegmentedOption<T>(
    val value: T,
    val label: String,
    /**
     * Opcional: cuatro segmentos con icono y rótulo no caben en el ancho de un teléfono, y
     * en las listas de preferencias el rótulo ya dice todo lo que hay que decir.
     */
    val icon: ImageVector? = null,
    /**
     * Un punto de color delante del rótulo, en vez de un icono.
     *
     * Lo usan los selectores de prioridad y de estado, donde lo que distingue a una opción de
     * otra es el color con el que esa prioridad se pinta en el resto de la pantalla; un icono
     * ahí tendría que inventarse un símbolo para «media» que no existe.
     */
    val dotColor: Color? = null,
    /** Un número al lado del rótulo, si esa vista tiene algo pendiente que contar. */
    val badge: Int? = null,
    /**
     * Cuánto ancho se lleva este segmento respecto a los demás.
     *
     * Por defecto todos valen igual. Se sube cuando un rótulo es mucho más largo que sus
     * vecinos —«Otro» junto a «2», «3» y «4»— y con el reparto a partes iguales quedaría
     * apretado contra sus propios bordes.
     */
    val weight: Float = 1f
)

/**
 * Elegir entre dos o tres vistas de lo mismo: Materias o Tareas, Horario o Calendario.
 *
 * Es un **grupo conectado** de Material: los botones se tocan, el elegido va relleno con el
 * acento y el resto sobre el contenedor. Las esquinas de los extremos se redondean hacia fuera
 * y las interiores quedan casi rectas, que es lo que los hace leerse como una pieza y no como
 * botones sueltos puestos en fila.
 *
 * `animateWidth` añade lo que Material llama la interacción entre vecinos: al mantener pulsado
 * uno, ese se ensancha y los de al lado se comprimen para dejarle sitio.
 *
 * Este control ya se rompió dos veces. La primera con `ButtonGroup` y `toggleableItem`, que se
 * quedaba en blanco; la segunda con `SingleChoiceSegmentedButtonRow`, que dibuja un contorno
 * fino y no el relleno que pide el diseño. El peso va con `Modifier.weight` del propio ámbito
 * del grupo, que es la forma documentada de que ocupen el ancho a partes iguales.
 */
@Composable
fun <T> UniSegmentedControl(
    selected: T,
    options: List<UniSegmentedOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current

    /*
     * Se usa la sobrecarga marcada como obsoleta, y es a propósito.
     *
     * La nueva pide un `overflowIndicator` y cambia el contenido de composable a un ámbito con
     * `customItem`, así que migrar no es cambiar una línea: es reescribir el bucle de segmentos
     * de este control, que dibuja diecinueve sitios de la app.
     *
     * Lo que la nueva resuelve es el desbordamiento: cuando los segmentos no caben, ofrece un
     * menú con los que sobran. Aquí nunca hay más de cuatro y todos caben, así que se estaría
     * pagando una reescritura por un problema que este control no tiene. Cuando la vieja se
     * retire de verdad habrá que hacerlo; hasta entonces, no.
     */
    @Suppress("DEPRECATION")
    ButtonGroup(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
    ) {
        options.forEachIndexed { index, option ->
            val interactionSource = remember { MutableInteractionSource() }
            val isSelected = selected == option.value
            val shapes = when (index) {
                0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
            }

            ToggleButton(
                checked = isSelected,
                onCheckedChange = {
                    // Un toque seco al cambiar de vista, y solo al cambiar: repetirlo al
                    // volver a pulsar la que ya está elegida convierte el aviso en ruido.
                    if (!isSelected) {
                        haptics.performSafely(HapticFeedbackType.SegmentTick)
                    }
                    onSelected(option.value)
                },
                shapes = shapes,
                interactionSource = interactionSource,
                modifier = Modifier
                    .weight(option.weight)
                    .defaultMinSize(minHeight = 48.dp)
                    .animateWidth(interactionSource)
            ) {
                if (option.icon != null) {
                    Icon(option.icon, contentDescription = null, modifier = Modifier.size(ToggleButtonDefaults.IconSize))
                    Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                }
                if (option.dotColor != null) {
                    Box(
                        Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(option.dotColor)
                    )
                    Spacer(Modifier.size(5.dp))
                }
                // Sin ajuste de línea: mientras `animateWidth` estrecha al vecino, el
                // rótulo cabría en menos de lo que mide y `Text` lo partiría en dos.
                Text(text = option.label, maxLines = 1, softWrap = false)
                if (option.badge != null && option.badge > 0) {
                    Spacer(Modifier.size(8.dp))
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    ) {
                        Box(
                            modifier = Modifier
                                .defaultMinSize(minWidth = 20.dp)
                                .padding(horizontal = 6.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = option.badge.toString(),
                                style = MaterialTheme.typography.labelSmallEmphasized
                            )
                        }
                    }
                }
            }
        }
    }
}
