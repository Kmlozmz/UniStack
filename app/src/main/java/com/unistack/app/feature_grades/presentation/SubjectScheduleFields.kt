package com.unistack.app.feature_grades.presentation

import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniTimePickerDialog
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import java.time.DayOfWeek
import java.time.LocalDate

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    // Una hora elegida de madrugada espera confirmación antes de aplicarse.
    var unusualStart by remember { mutableStateOf<Int?>(null) }

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
            shape = MaterialTheme.shapes.medium
        )
        Box {
            SchedulePickerField(
                label = "Repetición",
                value = if (draft.repeatEveryWeeks == 1) "Cada semana" else "Cada ${draft.repeatEveryWeeks} semanas",
                icon = Icons.Rounded.ExpandMore,
                onClick = { recurrenceExpanded = true }
            )
            UniDropdownMenu(expanded = recurrenceExpanded, onDismissRequest = { recurrenceExpanded = false }) {
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
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (startPickerVisible) {
        UniTimePickerDialog(
            selectedTime = java.time.LocalTime.of(draft.startMinute / 60, draft.startMinute % 60),
            onDismiss = { startPickerVisible = false },
            title = "Hora de inicio",
            onTimeSelected = { picked ->
            val chosen = picked.hour * 60 + picked.minute
            // Casi siempre que aparece una hora así es un error al girar la rueda: quien la
            // puso a la 1:00 quería las 13:00. Se pregunta en vez de impedirlo, porque clases
            // de madrugada existen.
            if (isUnusualClassHour(chosen)) unusualStart = chosen else onDraftChange(draft.copy(startMinute = chosen))
        }
        )
    }
    unusualStart?.let { chosen ->
        AlertDialog(
            onDismissRequest = { unusualStart = null },
            title = { Text("¿Clase de madrugada?") },
            text = {
                Text(
                    "Pusiste las ${formatMinute(chosen)}. Si querías las " +
                        "${formatMinute(chosen + 12 * 60)}, vuelve y cámbialo."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDraftChange(draft.copy(startMinute = chosen))
                        unusualStart = null
                    }
                ) { Text("Sí, es a esa hora", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        unusualStart = null
                        startPickerVisible = true
                    }
                ) { Text("Volver") }
            },
            shape = MaterialTheme.shapes.extraLarge
        )
    }
    if (endPickerVisible) {
        UniTimePickerDialog(
            selectedTime = java.time.LocalTime.of(draft.endMinute / 60, draft.endMinute % 60),
            onDismiss = { endPickerVisible = false },
            title = "Hora de fin",
            onTimeSelected = { picked ->
                onDraftChange(draft.copy(endMinute = picked.hour * 60 + picked.minute))
            }
        )
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
        UniDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Días", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..7).forEach { day ->
                val selected = day in draft.daysOfWeek
                // Relleno y ya. El contorno de 0,7 puntos estaba para separar del fondo un chip
                // que se pintaba justo del color del fondo; con contenedor propio, sobra.
                Box(
                    Modifier.weight(1f).height(46.dp).clip(CircleShape)
                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainer)
                        .clickable {
                            focusManager.clearFocus()
                            keyboard?.hide()
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
    // Suelta el campo que tuviera el foco antes de abrir nada.
    //
    // `dismissKeyboardOnTapOutside`, que va en la raíz del formulario, solo ve los toques que
    // ningún hijo consume; esta fila sí lo consume, así que el nombre de la materia se quedaba
    // enfocado por debajo del selector de hora y, al cerrarlo, el teclado volvía a subir solo.
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    Surface(
        onClick = {
            focusManager.clearFocus()
            keyboard?.hide()
            onClick()
        },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
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

/**
 * Si una hora de inicio cae en la madrugada, que en la práctica casi siempre es un error de
 * dedo: la rueda de las horas se pasa de las 13 a la 1 con un gesto.
 */
internal fun isUnusualClassHour(minute: Int): Boolean = minute < 6 * 60

private fun dayLetter(day: DayOfWeek): String =
    DayLabels.short[day.value - 1]

private fun formatMinute(value: Int): String = "%02d:%02d".format(value / 60, value % 60)
