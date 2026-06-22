package com.unistack.app.feature_grades.presentation

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
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
internal fun SubjectScheduleSection(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalInterfaceSpacing.current
    var startPickerVisible by remember { mutableStateOf(false) }
    var endPickerVisible by remember { mutableStateOf(false) }
    var recurrenceExpanded by remember { mutableStateOf(false) }
    var reminderExpanded by remember { mutableStateOf(false) }

    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(spacing.cardPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(spacing.section)) {
            ScheduleSectionHeader(draft.enabled) {
                onDraftChange(draft.copy(enabled = it))
            }
            if (draft.enabled) {
                SubjectLocationFields(draft, onDraftChange)
                ScheduleDays(draft, onDraftChange)
                ScheduleTimes(draft, { startPickerVisible = true }, { endPickerVisible = true })
                ScheduleOptions(
                    draft,
                    recurrenceExpanded,
                    reminderExpanded,
                    { recurrenceExpanded = it },
                    { reminderExpanded = it },
                    onDraftChange
                )
                if (!draft.isValid) {
                    Text(
                        if (draft.daysOfWeek.isEmpty()) "Selecciona al menos un d\u00eda."
                        else "La hora final debe ser posterior a la inicial.",
                        color = UniStackColors.Coral,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
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

@Composable
private fun ScheduleSectionHeader(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(42.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f), AppShapes.SmallCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text("Horario de clases", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.ExtraBold)
            Text(
                if (enabled) "Se a\u00f1adir\u00e1 a Horario" else "Sin bloques semanales",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Switch(checked = enabled, onCheckedChange = onEnabledChange)
    }
}
@Composable
private fun SubjectLocationFields(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
            value = draft.professor,
            onValueChange = { onDraftChange(draft.copy(professor = it.take(60))) },
            modifier = Modifier.weight(1f),
            label = { Text("Profesor") },
            placeholder = { Text("Prof. P\u00e9rez") },
            singleLine = true,
            shape = AppShapes.SmallCard
        )
        OutlinedTextField(
            value = draft.room,
            onValueChange = { onDraftChange(draft.copy(room = it.take(50))) },
            modifier = Modifier.weight(1f),
            label = { Text("Aula") },
            placeholder = { Text("Aula 301") },
            leadingIcon = { Icon(Icons.Rounded.Place, null) },
            singleLine = true,
            shape = AppShapes.SmallCard
        )
    }
}

@Composable
private fun ScheduleDays(
    draft: SubjectScheduleDraft,
    onDraftChange: (SubjectScheduleDraft) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("D\u00edas", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
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
private fun ScheduleTimes(
    draft: SubjectScheduleDraft,
    onStartClick: () -> Unit,
    onEndClick: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        SchedulePickerField(
            modifier = Modifier.weight(1f),
            label = "Hora inicio",
            value = formatMinute(draft.startMinute),
            icon = Icons.Rounded.Schedule,
            onClick = onStartClick
        )
        SchedulePickerField(
            modifier = Modifier.weight(1f),
            label = "Hora fin",
            value = formatMinute(draft.endMinute),
            icon = Icons.Rounded.Schedule,
            onClick = onEndClick
        )
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
private fun ScheduleOptions(
    draft: SubjectScheduleDraft,
    recurrenceExpanded: Boolean,
    reminderExpanded: Boolean,
    onRecurrenceExpanded: (Boolean) -> Unit,
    onReminderExpanded: (Boolean) -> Unit,
    onDraftChange: (SubjectScheduleDraft) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.weight(1f)) {
            SchedulePickerField(
                label = "Repetici\u00f3n",
                value = if (draft.repeatEveryWeeks == 1) "Cada semana" else "Cada ${draft.repeatEveryWeeks} semanas",
                icon = Icons.Rounded.ExpandMore,
                onClick = { onRecurrenceExpanded(true) }
            )
            DropdownMenu(expanded = recurrenceExpanded, onDismissRequest = { onRecurrenceExpanded(false) }) {
                (1..4).forEach { weeks ->
                    DropdownMenuItem(
                        text = { Text(if (weeks == 1) "Cada semana" else "Cada $weeks semanas") },
                        onClick = {
                            onDraftChange(draft.copy(repeatEveryWeeks = weeks))
                            onRecurrenceExpanded(false)
                        }
                    )
                }
            }
        }
        Box(Modifier.weight(1f)) {
            SchedulePickerField(
                label = "Recordatorio",
                value = if (draft.reminderMinutes == 0) "Sin aviso" else "${draft.reminderMinutes} min antes",
                icon = Icons.Rounded.Alarm,
                onClick = { onReminderExpanded(true) }
            )
            DropdownMenu(expanded = reminderExpanded, onDismissRequest = { onReminderExpanded(false) }) {
                listOf(0, 5, 10, 15, 30, 60).forEach { minutes ->
                    DropdownMenuItem(
                        text = { Text(if (minutes == 0) "Sin recordatorio" else "$minutes minutos antes") },
                        onClick = {
                            onDraftChange(draft.copy(reminderMinutes = minutes))
                            onReminderExpanded(false)
                        }
                    )
                }
            }
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
    val place = location.split('\u2022', limit = 2).map(String::trim)
    return SubjectScheduleDraft(
        enabled = true,
        professor = place.getOrElse(1) { "" },
        daysOfWeek = daysOfWeek,
        startMinute = startMinute,
        endMinute = endMinute,
        room = place.getOrElse(0) { "" },
        reminderMinutes = reminderMinutes,
        repeatEveryWeeks = repeatEveryWeeks,
        recurrenceStartEpochDay = recurrenceStartEpochDay
    )
}

internal fun defaultSubjectScheduleDraft(): SubjectScheduleDraft = SubjectScheduleDraft(
    enabled = true,
    recurrenceStartEpochDay = LocalDate.now().toEpochDay()
)

private fun dayLetter(day: DayOfWeek): String =
    listOf("L", "M", "X", "J", "V", "S", "D")[day.value - 1]

private fun formatMinute(value: Int): String = "%02d:%02d".format(value / 60, value % 60)
