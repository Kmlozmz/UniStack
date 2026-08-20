@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.SectionLabelStyle

/*
 * Las piezas con las que están hechas las nueve pantallas de ajustes.
 *
 * Viven aquí y no dentro de una pantalla porque las usan todas: el centro, Apariencia,
 * Accesibilidad, los tres modos de perfil y Actualizaciones. Cuando el encabezado o la fila
 * cambian, cambian en las nueve a la vez, que es justo lo que no pasaba cuando cada pantalla
 * traía su propia versión.
 */

/**
 * El encabezado de una pantalla de ajustes: título grande, apoyo, y la flecha si procede.
 */
@Composable
internal fun SettingsHeader(
    title: String,
    subtitle: String,
    onBackClick: (() -> Unit)?
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sin flecha cuando la pantalla es raíz de pestaña: una flecha que no lleva a
        // ninguna parte es peor que no tenerla.
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(Modifier.width(4.dp))
        } else {
            Spacer(Modifier.width(4.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Un rótulo de sección y, bajo él, el contenedor con sus filas.
 */
@Composable
internal fun SettingsGroup(
    label: String,
    content: @Composable ColumnScope.() -> Unit
) {
    SettingsGroup(label = label, labelColor = MaterialTheme.colorScheme.primary, content = content)
}

/**
 * El mismo grupo, con el rótulo en otro color.
 *
 * Lo usa el grupo de lo irreversible: el rojo del rótulo es lo que lo separa de los grupos de
 * arriba antes de haber leído una sola fila.
 */
@Composable
internal fun SettingsGroup(
    label: String,
    labelColor: Color,
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
 * El cuadrado de color con el icono dentro, a la izquierda de cada fila.
 *
 * Va relleno del color de la sección y el icono en el color del fondo, no al revés: en una
 * lista de ocho filas, ocho iconos teñidos sobre pastillas pálidas se leen como ocho manchas
 * del mismo peso, y el relleno sólido es lo que deja distinguirlos de un vistazo.
 */
@Composable
internal fun SettingsRowIcon(icon: ImageVector, color: Color) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = color,
        contentColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(21.dp))
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
internal fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            SettingsRowIcon(icon = icon, color = iconColor)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
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
internal fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    iconColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable { onCheckedChange(!checked) }
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        SettingsRowIcon(
            icon = icon,
            // Apagado, el icono pierde el color: en una lista de interruptores es lo que
            // deja ver de un vistazo cuáles están encendidos sin leer fila por fila.
            color = if (checked) iconColor else MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/**
 * Un interruptor suelto, sin icono, para las listas donde la fila ya se explica sola.
 */
@Composable
internal fun PreferenceSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .cleanClickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
