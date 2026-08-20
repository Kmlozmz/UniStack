@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.unistack.app.feature_schedule.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import com.unistack.app.core.design.theme.SectionLabelStyle
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
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
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults

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
    onSelect: (AgendaCreateKind) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 4.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AgendaSheetHeader(
                icon = Icons.Rounded.CalendarMonth,
                title = "Agregar a la agenda",
                subtitle = "Elige el tipo y se abre el formulario correcto."
            )

            AgendaSectionLabel("ACADÉMICO")
            listOf(
                AgendaCreateKind.TASK,
                AgendaCreateKind.EVALUATION,
                AgendaCreateKind.PRESENTATION
            ).forEach { kind ->
                AgendaKindRow(kind = kind, onClick = { onSelect(kind) })
            }

            AgendaSectionLabel("PERSONAL")
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

/** La cabecera común de los sheets de agenda: el icono en su cuadrado, qué es y para qué. */
@Composable
private fun AgendaSheetHeader(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/** El rótulo de un grupo: versales pequeñas del color del acento, como en el resto de sheets. */
@Composable
private fun AgendaSectionLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.primary,
        style = SectionLabelStyle,
        modifier = Modifier.padding(top = 6.dp, start = 4.dp)
    )
}

@Composable
private fun AgendaKindRow(
    kind: AgendaCreateKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
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
                    .clip(RoundedCornerShape(13.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(kind.icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    kind.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    kind.subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Un campo del formulario, con su rótulo fuera y arriba en vez de flotando dentro. */
@Composable
private fun AgendaFieldLabel(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 4.dp)
    )
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
    // Los pasos de fecha y los chips se tragan el toque, así que el campo que estuviera
    // escrito se quedaba enfocado y el teclado tapaba media hoja mientras se elegía.
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val releaseFocus = {
        focusManager.clearFocus()
        keyboard?.hide()
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AgendaSheetHeader(
                icon = kind.icon,
                title = if (existingEvent == null) kind.title else "Editar evento",
                subtitle = if (academic) "También aparecerá en Académico" else "Evento independiente de tus materias"
            )

            OutlinedTextField(
                title,
                { title = it.take(100) },
                Modifier.fillMaxWidth(),
                label = { Text("Título") },
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            // La fecha, en su propia tarjeta. Suelta sobre el fondo, las dos flechas y el texto
            // no se leían como un mismo control sino como tres cosas puestas en fila.
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = { releaseFocus(); dateEpochDay-- },
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, "Día anterior", modifier = Modifier.size(20.dp))
                    }
                    Text(
                        date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", AgendaLocale)).replaceFirstChar(Char::uppercase),
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FilledTonalIconButton(
                        onClick = { releaseFocus(); dateEpochDay++ },
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, "Día siguiente", modifier = Modifier.size(20.dp))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    startText,
                    { startText = it.take(5) },
                    Modifier.weight(1f),
                    label = { Text(if (academic) "Hora límite" else "Inicio") },
                    placeholder = { Text("08:00") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )
                if (!academic) {
                    OutlinedTextField(
                        endText,
                        { endText = it.take(5) },
                        Modifier.weight(1f),
                        label = { Text("Fin") },
                        placeholder = { Text("09:00") },
                        singleLine = true,
                        shape = MaterialTheme.shapes.medium
                    )
                }
            }

            if (academic) {
                // Los chips se envuelven en varias líneas en vez de rodar en horizontal.
                //
                // Rodando había siempre uno cortado en el borde derecho —había que adivinar
                // que seguía habiendo opciones— y al soltar el desplazamiento aparecía el
                // estirado del borde con un segundo de retraso, cuando la fila ya llevaba
                // rato quieta: la fila terminaba su recorrido y la velocidad que le sobraba
                // seguía subiendo por el desplazamiento anidado de la hoja, que la devolvía
                // tarde. Envueltos se ven todos a la vez y no hay borde que estirar.
                AgendaSectionLabel("TIPO ACADÉMICO")
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    taskTypesFor(kind).forEach { type ->
                        FilterChip(
                            selectedTaskType == type,
                            { releaseFocus(); selectedTaskType = type },
                            label = { Text(type.agendaLabel(), maxLines = 1, softWrap = false) }
                        )
                    }
                }

                AgendaSectionLabel("MATERIA (OPCIONAL)")
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selectedSubjectId == null,
                        { releaseFocus(); selectedSubjectId = null },
                        label = { Text("Sin materia", maxLines = 1, softWrap = false) }
                    )
                    subjects.forEach { subject ->
                        FilterChip(
                            selectedSubjectId == subject.id,
                            { releaseFocus(); selectedSubjectId = subject.id },
                            label = { Text(subject.name, maxLines = 1, softWrap = false) }
                        )
                    }
                }

                // El interruptor va dentro de una tarjeta: es un ajuste, y suelto entre
                // rótulos parecía un párrafo con un mando al lado.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "Genera calificación",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Quedará vinculada al seguimiento de notas.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Switch(generatesGrade, { releaseFocus(); generatesGrade = it })
                    }
                }
            } else {
                OutlinedTextField(
                    location,
                    { location = it.take(80) },
                    Modifier.fillMaxWidth(),
                    label = { Text("Ubicación (opcional)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium
                )

                AgendaSectionLabel("REPETICIÓN")
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AgendaRecurrence.entries.forEach { option ->
                        FilterChip(
                            recurrence == option,
                            { releaseFocus(); recurrence = option },
                            label = { Text(option.agendaLabel(), maxLines = 1, softWrap = false) }
                        )
                    }
                }

                AgendaSectionLabel("RECORDATORIO")
                FlowRow(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0, 5, 15, 30, 60, 1440).forEach { minutes ->
                        FilterChip(
                            reminder == minutes,
                            { releaseFocus(); reminder = minutes },
                            label = { Text(minutes.reminderLabel(), maxLines = 1, softWrap = false) }
                        )
                    }
                }
            }

            OutlinedTextField(
                notes,
                { notes = it.take(500) },
                Modifier.fillMaxWidth(),
                label = { Text("Notas (opcional)") },
                minLines = 2,
                shape = MaterialTheme.shapes.medium
            )
            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Guardar ocupa el ancho, como el botón principal de cualquier otra pantalla.
            // Estaba en una esquina, del tamaño de un botón secundario, siendo la única cosa
            // que había que hacer en toda la hoja.
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    releaseFocus()
                    val start = parseAgendaMinute(startText)
                    val end = parseAgendaMinute(endText)
                    val saved = if (academic) {
                        onSaveAcademic(title, notes, selectedSubjectId, selectedTaskType, date, start, generatesGrade)
                    } else {
                        onSaveEvent(existingEvent, title, notes, kind.toEventKind(), date, start, end, location, reminder, recurrence)
                    }
                    if (saved) onDismiss() else error = "Revisa el título, las horas y la materia seleccionada."
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Icon(Icons.Rounded.Save, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(9.dp))
                Text("Guardar", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
            }
            if (existingEvent != null && onDeleteEvent != null) {
                TextButton(
                    onClick = { onDeleteEvent(existingEvent.id); onDismiss() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Eliminar evento",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

private val AgendaLocale: Locale = Locale.forLanguageTag("es")

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
