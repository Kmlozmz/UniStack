@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.PresentToAll
import androidx.compose.material.icons.rounded.Quiz
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_schedule.domain.AgendaRecurrence
import com.unistack.app.feature_tasks.domain.TaskType
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

internal enum class AgendaCreateKind(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val academic: Boolean
) {
    TASK("Tarea o entrega", "Taller, lectura, proyecto o práctica", Icons.AutoMirrored.Rounded.Assignment, true),
    EVALUATION("Evaluación", "Quiz, parcial o examen", Icons.Rounded.Quiz, true),
    PRESENTATION("Presentación", "Exposición o sustentación", Icons.Rounded.PresentToAll, true),
    PERSONAL("Evento personal", "Cita, reunión o actividad", Icons.Rounded.Event, false),
    REMINDER("Recordatorio", "Algo que no quieres olvidar", Icons.Rounded.Alarm, false),
    CUSTOM("Tipo personalizado", "Crea una categoría flexible", Icons.Rounded.Tune, false)
}

@Composable
internal fun AgendaCreateMenuSheet(
    onDismiss: () -> Unit,
    onSelect: (AgendaCreateKind) -> Unit,
    onAddClass: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background, shape = AppShapes.LargeCard) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Agregar a la agenda", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Elige el tipo y se abre el formulario correcto.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            AgendaSectionLabel("Académico")
            listOf(
                AgendaCreateKind.TASK,
                AgendaCreateKind.EVALUATION,
                AgendaCreateKind.PRESENTATION
            ).forEach { kind ->
                AgendaKindRow(kind = kind, onClick = { onSelect(kind) })
            }
            AgendaKindRow(
                kind = null,
                onClick = onAddClass,
                title = "Clase recurrente",
                subtitle = "Añade una materia al horario",
                icon = Icons.AutoMirrored.Rounded.MenuBook
            )

            AgendaSectionLabel("Personal")
            listOf(
                AgendaCreateKind.PERSONAL,
                AgendaCreateKind.REMINDER,
                AgendaCreateKind.CUSTOM
            ).forEach { kind ->
                AgendaKindRow(kind = kind, onClick = { onSelect(kind) })
            }
        }
    }
}

@Composable
private fun AgendaSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 6.dp)
    )
}

@Composable
private fun AgendaKindRow(
    kind: AgendaCreateKind?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = kind?.title.orEmpty(),
    subtitle: String = kind?.subtitle.orEmpty(),
    icon: ImageVector = kind?.icon ?: Icons.Rounded.Event
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        tonalElevation = 0.dp,
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
internal fun AgendaComposerSheet(
    kind: AgendaCreateKind,
    initialDate: LocalDate,
    subjects: List<Subject>,
    existingEvent: AgendaEvent? = null,
    onDismiss: () -> Unit,
    onSaveAcademic: (String, String, String?, TaskType, LocalDate, Int?, Boolean) -> Boolean,
    onSaveEvent: (AgendaEvent?, String, String, AgendaEventKind, LocalDate, Int?, Int?, String, Int, AgendaRecurrence) -> Boolean,
    onDeleteEvent: ((String) -> Unit)? = null
) {
    val existingDate = existingEvent?.let { Instant.ofEpochMilli(it.startMillis).atZone(ZoneId.systemDefault()).toLocalDate() }
    var title by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.title.orEmpty()) }
    var notes by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.notes.orEmpty()) }
    var dateEpochDay by rememberSaveable(existingEvent?.id) { mutableStateOf((existingDate ?: initialDate).toEpochDay()) }
    var startText by rememberSaveable(existingEvent?.id) {
        mutableStateOf(existingEvent?.takeUnless(AgendaEvent::allDay)?.startMillis?.let(::agendaTimeText).orEmpty())
    }
    var endText by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.endMillis?.let(::agendaTimeText).orEmpty()) }
    var location by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.location.orEmpty()) }
    var selectedSubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTaskType by rememberSaveable { mutableStateOf(defaultTaskType(kind)) }
    var generatesGrade by rememberSaveable { mutableStateOf(kind == AgendaCreateKind.EVALUATION) }
    var reminder by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.reminderMinutes ?: 15) }
    var recurrence by rememberSaveable(existingEvent?.id) { mutableStateOf(existingEvent?.recurrence ?: AgendaRecurrence.NONE) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }
    val date = LocalDate.ofEpochDay(dateEpochDay)
    val academic = kind.academic

    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background, shape = AppShapes.LargeCard) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(kind.icon, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (existingEvent == null) kind.title else "Editar evento", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    Text(if (academic) "También aparecerá en Académico" else "Evento independiente de tus materias", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                }
            }
            OutlinedTextField(title, { title = it.take(100) }, Modifier.fillMaxWidth(), label = { Text("Título") }, singleLine = true, shape = AppShapes.SmallCard)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton({ dateEpochDay-- }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Día anterior") }
                Text(
                    date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es"))).replaceFirstChar(Char::uppercase),
                    modifier = Modifier.weight(1f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
                IconButton({ dateEpochDay++ }) { Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Día siguiente") }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(startText, { startText = it.take(5) }, Modifier.weight(1f), label = { Text(if (academic) "Hora límite" else "Inicio") }, placeholder = { Text("08:00") }, singleLine = true, shape = AppShapes.SmallCard)
                if (!academic) OutlinedTextField(endText, { endText = it.take(5) }, Modifier.weight(1f), label = { Text("Fin") }, placeholder = { Text("09:00") }, singleLine = true, shape = AppShapes.SmallCard)
            }
            if (academic) {
                Text("Tipo académico", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    taskTypesFor(kind).forEach { type ->
                        FilterChip(selectedTaskType == type, { selectedTaskType = type }, label = { Text(type.agendaLabel()) })
                    }
                }
                Text("Materia (opcional)", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selectedSubjectId == null, { selectedSubjectId = null }, label = { Text("Sin materia") })
                    subjects.forEach { subject ->
                        FilterChip(selectedSubjectId == subject.id, { selectedSubjectId = subject.id }, label = { Text(subject.name, maxLines = 1) })
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Genera calificación", fontWeight = FontWeight.Bold)
                        Text("Quedará vinculada al seguimiento de notas.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(generatesGrade, { generatesGrade = it })
                }
            } else {
                OutlinedTextField(location, { location = it.take(80) }, Modifier.fillMaxWidth(), label = { Text("Ubicación (opcional)") }, singleLine = true, shape = AppShapes.SmallCard)
                Text("Repetición", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AgendaRecurrence.entries.forEach { option ->
                        FilterChip(recurrence == option, { recurrence = option }, label = { Text(option.agendaLabel()) })
                    }
                }
                Text("Recordatorio", fontWeight = FontWeight.Bold)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 5, 15, 30, 60, 1440).forEach { minutes ->
                        FilterChip(reminder == minutes, { reminder = minutes }, label = { Text(minutes.reminderLabel()) })
                    }
                }
            }
            OutlinedTextField(notes, { notes = it.take(500) }, Modifier.fillMaxWidth(), label = { Text("Notas (opcional)") }, minLines = 2, shape = AppShapes.SmallCard)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                if (existingEvent != null && onDeleteEvent != null) {
                    TextButton(onClick = { onDeleteEvent(existingEvent.id); onDismiss() }) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        val start = parseAgendaMinute(startText)
                        val end = parseAgendaMinute(endText)
                        val saved = if (academic) {
                            onSaveAcademic(title, notes, selectedSubjectId, selectedTaskType, date, start, generatesGrade)
                        } else {
                            onSaveEvent(existingEvent, title, notes, kind.toEventKind(), date, start, end, location, reminder, recurrence)
                        }
                        if (saved) onDismiss() else error = "Revisa el título, las horas y la materia seleccionada."
                    },
                    shape = AppShapes.MediumCard,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp)
                ) {
                    Icon(Icons.Rounded.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun defaultTaskType(kind: AgendaCreateKind) = when (kind) {
    AgendaCreateKind.EVALUATION -> TaskType.EXAM
    AgendaCreateKind.PRESENTATION -> TaskType.PRESENTATION
    else -> TaskType.WORKSHOP
}

private fun taskTypesFor(kind: AgendaCreateKind) = when (kind) {
    AgendaCreateKind.EVALUATION -> listOf(TaskType.TEST, TaskType.EXAM)
    AgendaCreateKind.PRESENTATION -> listOf(TaskType.PRESENTATION)
    else -> listOf(TaskType.WORKSHOP, TaskType.PROJECT, TaskType.READING, TaskType.PRACTICE, TaskType.ESSAY, TaskType.RESEARCH, TaskType.OTHER)
}

private fun TaskType.agendaLabel() = when (this) {
    TaskType.WORKSHOP -> "Taller"
    TaskType.EXAM -> "Parcial"
    TaskType.TEST -> "Examen / quiz"
    TaskType.PRESENTATION -> "Presentación"
    TaskType.PROJECT -> "Proyecto"
    TaskType.READING -> "Lectura"
    TaskType.PRACTICE -> "Práctica"
    TaskType.ESSAY -> "Ensayo"
    TaskType.RESEARCH -> "Investigación"
    TaskType.OTHER -> "Otro"
}

private fun AgendaCreateKind.toEventKind() = when (this) {
    AgendaCreateKind.PERSONAL -> AgendaEventKind.PERSONAL
    AgendaCreateKind.REMINDER -> AgendaEventKind.REMINDER
    else -> AgendaEventKind.CUSTOM
}

private fun AgendaRecurrence.agendaLabel() = when (this) {
    AgendaRecurrence.NONE -> "No repetir"
    AgendaRecurrence.DAILY -> "Diario"
    AgendaRecurrence.WEEKLY -> "Semanal"
    AgendaRecurrence.MONTHLY -> "Mensual"
}

private fun Int.reminderLabel() = when (this) {
    0 -> "Sin aviso"
    1440 -> "1 día antes"
    else -> "$this min"
}

private fun parseAgendaMinute(value: String): Int? {
    if (value.isBlank()) return null
    val parts = value.trim().split(':')
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun agendaTimeText(millis: Long): String {
    val time = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalTime()
    return "%02d:%02d".format(time.hour, time.minute)
}
