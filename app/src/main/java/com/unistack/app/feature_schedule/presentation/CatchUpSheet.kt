package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.reacomodoDeLista
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.AttendanceHistoryEntry
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Espanol: Locale = Locale.forLanguageTag("es")
private val DiaCorto = DateTimeFormatter.ofPattern("d MMM", Espanol)

/**
 * Resolver de una vez todo lo que se quedó sin marcar.
 *
 * Los avisos se ignoran —una semana de exámenes y hay seis clases pendientes— y marcarlas una
 * a una, entrando y saliendo de cada panel, es lo que hace que se abandone el registro entero.
 * Aquí cada fila tiene sus dos botones y la lista se vacía sola según se responde.
 *
 * No hay «marcar todas como asistidas»: eso sería la app inventándose el dato, que es justo lo
 * que llevamos toda esta parte quitando.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CatchUpSheet(
    pending: List<AttendanceHistoryEntry>,
    subjects: List<Subject>,
    onMark: (AttendanceHistoryEntry, ClassAttendanceStatus) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptics = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = if (pending.isEmpty()) {
                        stringResource(R.string.schedule_all_caught_up)
                    } else {
                        stringResource(R.string.catch_up_unmarked_count, pending.size)
                    },
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (pending.isEmpty()) {
                        stringResource(R.string.catch_up_empty_title)
                    } else {
                        stringResource(R.string.catch_up_empty_desc)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(bottom = 6.dp)
            ) {
                items(pending, key = { "${it.session.id}:${it.date.toEpochDay()}" }) { entrada ->
                    val materia = subjects.firstOrNull { it.id == entrada.session.subjectId }
                    FilaPendiente(
                        modifier = reacomodoDeLista(),
                        entrada = entrada,
                        nombre = materia?.name ?: stringResource(R.string.schedule_detail_class),
                        onMark = { estado ->
                            haptics.performSafely(HapticFeedbackType.SegmentTick)
                            onMark(entrada, estado)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FilaPendiente(
    entrada: AttendanceHistoryEntry,
    nombre: String,
    onMark: (ClassAttendanceStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = nombre,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Text(
                text = entrada.date.format(DiaCorto),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        BotonRedondo(
            icono = Icons.Rounded.Check,
            descripcion = stringResource(R.string.schedule_status_attended),
            tono = ScheduleAccent,
            onClick = { onMark(ClassAttendanceStatus.ATTENDED) }
        )
        Spacer(Modifier.width(7.dp))
        BotonRedondo(
            icono = Icons.Rounded.Close,
            descripcion = stringResource(R.string.schedule_status_absent),
            tono = MaterialTheme.colorScheme.error,
            onClick = { onMark(ClassAttendanceStatus.ABSENT) }
        )
    }
}

@Composable
private fun BotonRedondo(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    descripcion: String,
    tono: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(tono.copy(alpha = 0.18f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icono, contentDescription = descripcion, tint = tono, modifier = Modifier.size(19.dp))
    }
}
