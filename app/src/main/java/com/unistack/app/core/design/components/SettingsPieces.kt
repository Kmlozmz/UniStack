@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.feature_user.domain.SettingsIconStyle

/*
 * Las piezas con las que están hechas las nueve pantallas de ajustes.
 *
 * Viven aquí y no dentro de una pantalla porque las usan todas: el centro, Apariencia,
 * Accesibilidad, los tres modos de perfil y Actualizaciones. Cuando la fila
 * cambia, cambia en las nueve a la vez, que es justo lo que no pasaba cuando cada pantalla
 * traía su propia versión.
 */

/**
 * Dónde vive una fila dentro de su grupo.
 *
 * De esto depende la forma: la primera redondea arriba, la última abajo, y las de en medio van
 * casi rectas. Con una sola fila, redondea las cuatro esquinas.
 */
enum class SettingsRowPosition { First, Middle, Last, Only }

/**
 * El ámbito de un grupo de ajustes.
 *
 * Existe para que cada fila sepa si es la primera, una del medio o la última **sin que quien
 * escribe la pantalla tenga que contarlas**. Se le pasa el número de filas al abrir el grupo y
 * el ámbito reparte las posiciónes según se van pidiendo.
 *
 * Hizo falta porque `SegmentedListItem` de Material necesita esa posición para elegir su forma,
 * y las filas llegaban dentro de una lambda de contenido donde no hay forma de contarlas.
 */
class SettingsGroupScope internal constructor(private val total: Int) {
    private var index = 0

    /** La posición de la siguiente fila. Se llama una vez por fila, en orden. */
    fun nextPosition(): SettingsRowPosition {
        val position = when {
            total <= 1 -> SettingsRowPosition.Only
            index == 0 -> SettingsRowPosition.First
            index == total - 1 -> SettingsRowPosition.Last
            else -> SettingsRowPosition.Middle
        }
        index++
        return position
    }
}

/**
 * El grupo de siempre: una tarjeta con lo que le metas dentro.
 *
 * Lo usan los grupos cuyas filas no son [SettingsRow] sino piezas propias de su pantalla —los
 * avisos de notificaciones, el bloque de «no molestar», la cuenta—. Esas filas no pintan su
 * propio fondo, así que sin el contenedor se quedarían flotando sobre el fondo de la app.
 *
 * Los grupos hechos de [SettingsRow] usan el otro [SettingsGroup], el segmentado.
 */
@Composable
fun SettingsGroupCard(
    label: String,
    labelColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = SectionLabelStyle,
            color = labelColor,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Column(content = content)
        }
    }
}

/**
 * Una fila sola, sin grupo alrededor.
 *
 * La usan las pantallas donde el ajuste es uno y no hay lista: redondea las cuatro esquinas,
 * que es lo que hacía el `Surface` que estas pantallas se pintaban por su cuenta.
 */
@Composable
fun SettingsSoloRow(content: @Composable SettingsGroupScope.() -> Unit) {
    SettingsGroupScope(1).content()
}

/** Las formas que le tocan a una fila según dónde esté. */
@Composable
internal fun shapeFor(position: SettingsRowPosition): RoundedCornerShape {
    val big = 18.dp
    val small = 4.dp
    return when (position) {
        SettingsRowPosition.Only -> RoundedCornerShape(big)
        SettingsRowPosition.First -> RoundedCornerShape(topStart = big, topEnd = big, bottomStart = small, bottomEnd = small)
        SettingsRowPosition.Middle -> RoundedCornerShape(small)
        SettingsRowPosition.Last -> RoundedCornerShape(topStart = small, topEnd = small, bottomStart = big, bottomEnd = big)
    }
}

@Composable
internal fun shapesFor(position: SettingsRowPosition): ListItemShapes {
    val s = shapeFor(position)
    return ListItemDefaults.shapes(
        shape = s,
        selectedShape = s,
        focusedShape = s,
        pressedShape = s
    )
}

/**
 * Un rótulo de sección y, bajo él, sus filas.
 *
 * **Las filas son piezas sueltas, no una tarjeta con líneas dentro.** Antes iban las cuatro o
 * cinco metidas en un mismo contenedor redondeado; ahora cada una tiene su propio fondo y su
 * propia forma, separadas por un hilo de aire. Es el patrón segmentado de Material 3
 * Expressive: se ve dónde acaba una fila y empieza la siguiente sin necesidad de pintar una
 * línea divisoria, y al pulsar una, la que se hunde es esa y no el bloque entero.
 *
 * @param rowCount cuántas filas van dentro. Hay que decirlo porque de ahí sale que la primera
 *   redondee arriba y la última abajo.
 */
@Composable
fun SettingsGroup(
    label: String,
    rowCount: Int,
    content: @Composable SettingsGroupScope.() -> Unit
) {
    SettingsGroup(
        label = label,
        labelColor = MaterialTheme.colorScheme.primary,
        rowCount = rowCount,
        content = content
    )
}

/**
 * El mismo grupo, con el rótulo en otro color.
 *
 * Lo usa el grupo de lo irreversible: el rojo del rótulo es lo que lo separa de los grupos de
 * arriba antes de haber leído una sola fila.
 */
@Composable
fun SettingsGroup(
    label: String,
    labelColor: Color,
    rowCount: Int,
    content: @Composable SettingsGroupScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = SectionLabelStyle,
            color = labelColor,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp)
        )
        // Dos puntos de aire entre filas: lo justo para que se lean como piezas y no tanto
        // como para que dejen de leerse como un grupo.
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            SettingsGroupScope(rowCount).content()
        }
    }
}

/**
 * El icono a la izquierda de cada fila, en la forma que se haya elegido en Apariencia.
 *
 * - **De colores**: cuadrado relleno del color de lo que hace la fila y el icono en el color del
 *   fondo, no al revés. En una lista de ocho filas, ocho iconos teñidos sobre pastillas pálidas
 *   se leen como ocho manchas del mismo peso, y el relleno sólido es lo que deja distinguirlos.
 * - **Circulares**: círculo un tono por encima de la fila y el icono en el acento, como en la
 *   simulación del histórico. Todos del mismo color, así que ahí manda el dibujo.
 *
 * Apagado —un interruptor sin encender— pierde el color en las dos, que es lo que deja ver de
 * un vistazo cuáles están encendidos sin leer fila a fila.
 */
@Composable
fun SettingsRowIcon(icon: ImageVector, color: Color, activo: Boolean = true) {
    when (LocalAppearancePreferences.current.settingsIconStyle) {
        SettingsIconStyle.COLOR -> Surface(
            shape = MaterialTheme.shapes.small,
            color = if (activo) color else MaterialTheme.colorScheme.surfaceContainerHighest,
            contentColor = MaterialTheme.colorScheme.surface,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp))
            }
        }
        SettingsIconStyle.CIRCULO -> Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (activo) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Una entrada de ajustes que lleva a otra pantalla.
 *
 * Van varias dentro de un mismo contenedor y no una tarjeta por entrada. Con una tarjeta cada
 * una, ocho ajustes son ocho bloques del mismo peso y nada dice cuáles se parecen entre sí;
 * agrupadas, el contenedor es el que agrupa y el rótulo de arriba dice de qué va el grupo.
 */
@Composable
fun SettingsGroupScope.SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = shapesFor(nextPosition()),
        supportingContent = {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = { SettingsRowIcon(icon = icon, color = iconColor) },
        trailingContent = {
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        // El titular va en el `content` final: `SegmentedListItem` no tiene `headlineContent`,
        // el hueco principal es la lambda de la cola.
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
    }
}

/**
 * Una entrada de ajustes que se enciende y se apaga.
 *
 * El interruptor conserva su propio `onCheckedChange` aunque la fila entera ya sea tocable:
 * un interruptor desactivado para que el toque «pase» a la fila no deja de recibir el toque,
 * se lo traga, y el ajuste deja de responder justo donde el dedo va primero.
 */
@Composable
fun SettingsGroupScope.SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    iconColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    SegmentedListItem(
        onClick = { onCheckedChange(!checked) },
        shapes = shapesFor(nextPosition()),
        supportingContent = {
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        },
        leadingContent = {
            // Apagado, el icono pierde el color: en una lista de interruptores es lo que deja
            // ver de un vistazo cuáles están encendidos sin leer fila a fila.
            SettingsRowIcon(icon = icon, color = iconColor, activo = checked)
        },
        trailingContent = { UniSwitch(checked = checked, onCheckedChange = onCheckedChange) },
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
    }
}

/**
 * Una fila de ajustes que aloja un bloque personalizado (como un selector segmentado).
 *
 * Mantiene la misma forma morfológica (18dp/4dp) y color de contenedor que el resto de las
 * filas del grupo de Material 3 Expressive.
 */
@Composable
fun SettingsGroupScope.SettingsCustomRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    trailingAction: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = shapeFor(nextPosition())
    Surface(
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SettingsRowIcon(icon = icon, color = iconColor)
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized)
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                trailingAction?.invoke()
            }
            content()
        }
    }
}
