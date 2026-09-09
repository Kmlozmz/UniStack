package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.utils.performSafely

/**
 * Cuántas faltas admite tu reglamento.
 *
 * Es **uno para todas las materias**, no uno por materia: el número sale del reglamento de la
 * universidad, y tenerlo por asignatura obligaba a escribir siete veces lo mismo. Se pregunta y
 * no se supone —cambia de una universidad a otra— así que mientras no esté puesto, el historial
 * enseña el porcentaje en vez de prometer «te quedan N».
 */
@Composable
internal fun AbsenceLimitDialog(
    actual: Int?,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit
) {
    var valor by remember { mutableStateOf(actual ?: 6) }
    val haptics = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.schedule_absence_limit_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.schedule_absence_limit_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Paso(
                        icono = Icons.Rounded.Remove,
                        descripcion = stringResource(R.string.schedule_absence_limit_decrease),
                        activo = valor > 1
                    ) {
                        haptics.performSafely(HapticFeedbackType.SegmentTick)
                        valor = (valor - 1).coerceAtLeast(1)
                    }
                    Text(
                        text = "$valor",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Paso(
                        icono = Icons.Rounded.Add,
                        descripcion = stringResource(R.string.schedule_absence_limit_increase),
                        activo = valor < 40
                    ) {
                        haptics.performSafely(HapticFeedbackType.SegmentTick)
                        valor = (valor + 1).coerceAtMost(40)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(valor) }) {
                Text(stringResource(R.string.action_save), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            // Quitarlo devuelve la cabecera al porcentaje, que es lo que había antes del tope.
            if (actual != null) {
                TextButton(onClick = { onConfirm(null) }) {
                    Text(stringResource(R.string.schedule_absence_limit_remove), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    )
}

@Composable
private fun Paso(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    activo: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(enabled = activo, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = descripcion,
            tint = if (activo) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            modifier = Modifier.size(21.dp)
        )
    }
}
