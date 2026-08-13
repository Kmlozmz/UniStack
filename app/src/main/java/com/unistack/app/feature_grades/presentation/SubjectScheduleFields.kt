package com.unistack.app.feature_grades.presentation

import com.unistack.app.core.utils.DayLabels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Campos del bloque «Cuándo» del formulario de materia: qué días, a qué hora, dónde y cada
 * cuánto se repite.
 *
 * Son campos sueltos y no una tarjeta con cabecera propia: el bloque que los envuelve es
 * quien pone el título, el interruptor y el marco. Antes esto era una tarjeta entera con su
 * icono y su encabezado, y por eso el formulario acababa siendo una pila de tarjetas grises
 * todas del mismo peso.
 */
@Composable
internal fun SubjectWhenFields(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var startPickerVisible by remember { mutableStateOf(false) }
    var endPickerVisible by remember { mutableStateOf(false) }
    var recurrenceExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ScheduleDays(draft, onDraftChange)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SchedulePickerField(
                modifier = Modifier.weight(1f),
                label = "Hora inicio",
                value = formatMinute(draft.startMinute),
                icon = Icons.Rounded.Schedule,
                onClick = { startPickerVisible = true }
            )
            SchedulePickerField(
                modifier = Modifier.weight(1f),
                label = "Hora fin",
                value = formatMinute(draft.endMinute),
                icon = Icons.Rounded.Schedule,
                onClick = { endPickerVisible = true }
            )
        }
        OutlinedTextField(
            value = draft.room,
            onValueChange = { onDraftChange(draft.copy(room = it.take(50))) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Aula") },
            placeholder = { Text("Aula 301") },
            leadingIcon = { Icon(Icons.Rounded.Place, null) },
            singleLine = true,
            shape = AppShapes.SmallCard
        )
        Box {
            SchedulePickerField(
                label = "Repetición",
                value = if (draft.repeatEveryWeeks == 1) "Cada semana" else "Cada ${draft.repeatEveryWeeks} semanas",
                icon = Icons.Rounded.ExpandMore,
                onClick = { recurrenceExpanded = true }
            )
            DropdownMenu(expanded = recurrenceExpanded, onDismissRequest = { recurrenceExpanded = false }) {
                (1..4).forEach { weeks ->
                    DropdownMenuItem(
                        text = { Text(if (weeks == 1) "Cada semana" else "Cada $weeks semanas") },
                        onClick = {
                            onDraftChange(draft.copy(repeatEveryWeeks = weeks))
                            recurrenceExpanded = false
                        }
                    )
                }
            }
        }
        if (!draft.isValid) {
            Text(
                if (draft.daysOfWeek.isEmpty()) "Selecciona al menos un día."
                else "La hora final debe ser posterior a la inicial.",
                color = UniStackColors.Coral,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (startPickerVisible) {
        SubjectTimePicker("Hora de inicio", draft.startMinute, { startPickerVisible = false }) {
            onDraftChange(draft.copy(startMinute = it))
            startPickerVisible = false
        }
    }
    if (endPickerVisible) {
        SubjectTimePicker("Hora de fin", draft.endMinute, { endPickerVisible = false }) {
            onDraftChange(draft.copy(endMinute = it))
            endPickerVisible = false
        }
    }
}

/**
 * Aviso previo a la clase. Vive con los ajustes académicos y no con los días y las horas
 * porque es una preferencia de quien estudia, no un dato del bloque de clase.
 */
@Composable
internal fun SubjectReminderField(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        SchedulePickerField(
            label = "Recordatorio de clase",
            value = if (draft.reminderMinutes == 0) "Sin aviso" else "${draft.reminderMinutes} min antes",
            icon = Icons.Rounded.Alarm,
            onClick = { expanded = true }
        )
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf(0, 5, 10, 15, 30, 60).forEach { minutes ->
                DropdownMenuItem(
                    text = { Text(if (minutes == 0) "Sin recordatorio" else "$minutes minutos antes") },
                    onClick = {
                        onDraftChange(draft.copy(reminderMinutes = minutes))
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ScheduleDays(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Días", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..7).forEach { day ->
                val selected = day in draft.daysOfWeek
                Box(
                    Modifier.weight(1f).height(42.dp).clip(AppShapes.SmallCard)
                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            0.7.dp,
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f),
                            AppShapes.SmallCard
                        )
                        .clickable {
                            onDraftChange(draft.copy(daysOfWeek = if (selected) draft.daysOfWeek - day else draft.daysOfWeek + day))
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        dayLetter(DayOfWeek.of(day)),
                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun SchedulePickerField(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.SmallCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.7.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.22f))
    ) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
                Text(
                    value,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(6.dp))
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun SubjectTimePicker(
    title: String,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    val state = rememberTimePickerState(initialHour = minute / 60, initialMinute = minute % 60, is24Hour = true)
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { TimePicker(state) },
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour * 60 + state.minute) }) { Text("Aceptar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        shape = AppShapes.LargeCard
    )
}

internal fun ClassSession.toSubjectScheduleDraft(): SubjectScheduleDraft {
    return SubjectScheduleDraft(
        enabled = true,
        professor = place.professor,
        daysOfWeek = daysOfWeek,
        startMinute = startMinute,
        endMinute = endMinute,
        room = place.room,
        reminderMinutes = reminderMinutes,
        repeatEveryWeeks = repeatEveryWeeks,
        recurrenceStartEpochDay = recurrenceStartEpochDay
    )
}

internal fun defaultSubjectScheduleDraft(): SubjectScheduleDraft = SubjectScheduleDraft(
    enabled = true,
    recurrenceStartEpochDay = LocalDate.now().toEpochDay()
)

/**
 * Resumen de una línea del bloque de clase, para que plegarlo esconda los controles y no la
 * información.
 */
internal fun SubjectScheduleDraft.whenSummary(): String {
    if (!enabled) return "Sin clases"
    if (daysOfWeek.isEmpty()) return "Sin días"
    val days = daysOfWeek.sorted().joinToString(" ") { DayLabels.shortByIsoDay(it) }
    return "$days  ·  ${formatMinute(startMinute)}"
}

private fun dayLetter(day: DayOfWeek): String =
    DayLabels.short[day.value - 1]

private fun formatMinute(value: Int): String = "%02d:%02d".format(value / 60, value % 60)
