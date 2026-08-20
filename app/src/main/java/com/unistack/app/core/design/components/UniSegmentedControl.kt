@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class UniSegmentedOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector,
    /** Un número al lado del rótulo, si esa vista tiene algo pendiente que contar. */
    val badge: Int? = null
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
                onCheckedChange = { onSelected(option.value) },
                shapes = shapes,
                interactionSource = interactionSource,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .animateWidth(interactionSource)
            ) {
                Icon(option.icon, contentDescription = null, modifier = Modifier.size(ToggleButtonDefaults.IconSize))
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text(option.label)
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
