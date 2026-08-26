@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.ui.unit.IntSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.Spring
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.unistack.app.core.utils.performSafely
import androidx.compose.runtime.getValue

/**
 * Elegir una opción entre varias, con contorno y un visto en la elegida.
 *
 * **Es el hermano de [UniSegmentedControl], y la diferencia no es estética.** El relleno dice
 * «estás viendo esto»: cambiar de Materias a Tareas cambia lo que hay debajo, y el segmento
 * relleno es el sitio donde estás. El contorno dice «has marcado esto»: filtrar, elegir el
 * motivo de un mensaje o el tipo de algo no te lleva a otro sitio, acota el que ya estás
 * mirando. Usar el relleno para las dos cosas hacía que un filtro pareciera una pestaña.
 *
 * Está escrito a mano y no con el segmentado de Material por una razón concreta: aquel sube al
 * frente el segmento que estás pulsando, y como los de en medio son rectángulos, al mantener
 * pulsado aparecía un contorno cuadrado encima de un control redondeado. Aquí el recorte lo pone
 * la fila entera, así que nada puede dibujarse fuera de ella.
 *
 * El gesto que lo hace legible: al elegir uno, el visto **entra por la izquierda** empujando el
 * rótulo, que se recoloca en el centro.
 */
@Composable
fun <T> UniChoiceRow(
    selected: T,
    options: List<UniSegmentedOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptics = LocalHapticFeedback.current
    val shape = RoundedCornerShape(percent = 50)
    val line = MaterialTheme.colorScheme.outline

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(shape)
            .border(width = 1.dp, color = line, shape = shape)
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) {
                VerticalDivider(color = line, thickness = 1.dp)
            }
            val chosen = option.value == selected
            val fill by animateColorAsState(
                targetValue = if (chosen) {
                    MaterialTheme.colorScheme.secondaryContainer
                } else {
                    Color.Transparent
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "fondo de la opción"
            )
            val ink by animateColorAsState(
                targetValue = if (chosen) {
                    MaterialTheme.colorScheme.onSecondaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
                label = "tinta de la opción"
            )

            Row(
                modifier = Modifier
                    .weight(option.weight)
                    .fillMaxHeight()
                    .background(fill)
                    .selectable(
                        selected = chosen,
                        role = Role.RadioButton,
                        onClick = {
                            // Un toque seco al cambiar, y solo al cambiar: repetirlo sobre el
                            // que ya está elegido convierte el aviso en ruido.
                            if (!chosen) haptics.performSafely(HapticFeedbackType.SegmentTick)
                            onSelected(option.value)
                        }
                    ),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(
                    visible = chosen,
                    enter = expandHorizontally(
                        // Un muelle con rebote de verdad, no el del tema.
                        //
                        // `defaultSpatialSpec` está amortiguado casi al máximo: entra recto y
                        // no se nota que sea un muelle. Bajando la amortiguación, el rótulo se
                        // pasa un pelo de su sitio y vuelve, que es el gesto que se pedía.
                        animationSpec = spring(
                            dampingRatio = 0.45f,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        expandFrom = Alignment.Start
                    ) + fadeIn(MaterialTheme.motionScheme.defaultEffectsSpec()),
                    exit = shrinkHorizontally(
                        animationSpec = spring(
                            dampingRatio = 0.45f,
                            stiffness = Spring.StiffnessMediumLow,
                            visibilityThreshold = IntSize.VisibilityThreshold
                        ),
                        shrinkTowards = Alignment.Start
                    ) + fadeOut(MaterialTheme.motionScheme.defaultEffectsSpec())
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = ink,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                    }
                }
                // Sin ajuste de línea: mientras el visto entra, el rótulo dispone de menos
                // ancho del que mide y `Text` lo partiría en dos a mitad de la animación.
                Text(
                    text = option.label,
                    color = ink,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}
