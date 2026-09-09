package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_user.domain.Corte

/**
 * Cómo se lee el historial.
 *
 * La pantalla usa color como dato —verde es que fuiste, rojo que no— y eso hay que decirlo una
 * vez. La leyenda de la tira lo cubre a medias, pero solo enseña los estados que ya han
 * ocurrido, así que quien empieza no ve el rojo hasta que falta por primera vez.
 *
 * Se abre a mano desde la interrogación y no sale sola: nadie quiere un tutorial cada vez que
 * mira sus faltas.
 */
@Composable
internal fun AttendanceHelpDialog(
    hasLimit: Boolean,
    hasWeekNumbers: Boolean,
    hasCuts: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.attendance_help_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Parrafo(
                    if (hasLimit) {
                        stringResource(R.string.attendance_help_limit_desc)
                    } else {
                        stringResource(R.string.attendance_help_no_limit_desc)
                    }
                )

                Parrafo(stringResource(R.string.attendance_help_legend_title))

                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Fila(ClassAttendanceStatus.ATTENDED, stringResource(R.string.attendance_help_legend_attended))
                    Fila(ClassAttendanceStatus.ABSENT, stringResource(R.string.attendance_help_legend_absent))
                    Fila(ClassAttendanceStatus.CANCELLED, stringResource(R.string.attendance_help_legend_canceled))
                    Fila(ClassAttendanceStatus.PENDING, stringResource(R.string.attendance_help_legend_unmarked))
                }

                if (hasCuts) {
                    Parrafo(
                        stringResource(R.string.attendance_help_corte_desc, Corte.Singular.lowercase(), Corte.Plural.lowercase())
                    )
                }

                if (hasWeekNumbers) {
                    Parrafo(
                        stringResource(R.string.attendance_help_weeks_desc)
                    )
                }

                Parrafo(
                    stringResource(R.string.attendance_help_upcoming_desc)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_understood), fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun Parrafo(texto: String) {
    Text(
        text = texto,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 13.sp,
        lineHeight = 18.sp
    )
}

@Composable
private fun Fila(status: ClassAttendanceStatus, que: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            Modifier
                .size(11.dp)
                .clip(CircleShape)
                .background(
                    status.attendanceColor() ?: MaterialTheme.colorScheme.surfaceContainerHighest
                )
        )
        Text(
            text = status.legendName().replaceFirstChar { it.uppercase() } + " — $que",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 12.5.sp,
            lineHeight = 16.sp
        )
    }
}
