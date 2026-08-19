package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.utils.DayLabels

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.HourglassBottom
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Repeat
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.ZoomIn
import androidx.compose.material.icons.rounded.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.SessionPlace
import com.unistack.app.feature_tasks.domain.StudentTask
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
/* Estos nombres describían un color («Green», «Purple») pero devolvían un rol del tema,
   así que mentían en cuanto el acento dejaba de ser verde —es decir, siempre—. Ahora
   nombran el papel que cumplen. Se cayeron dos: SchedulePurple, que era un duplicado
   literal de ScheduleAccent, y SchedulePink, que solo alimentaba la lista de muestras. */
private val ScheduleAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary
private val ScheduleRescheduled: Color
    @Composable get() = LocalSectionColors.current.schedule
private val ScheduleCancelled: Color
    @Composable get() = LocalSectionColors.current.atRisk
private val ScheduleShape
    get() = AppShapes.SmallCard

private enum class CalendarMode(val label: String) {
    MONTH("Mes"),
    AGENDA("Agenda"),
    LIST("Lista")
}

private enum class ScheduleView {
    TIMETABLE,
    CALENDAR,
    DAY
}

@Composable
fun CalendarScheduleScreen(
    onTaskClick: (String) -> Unit,
    // Crear y editar una clase abren el formulario de materia, que es la misma pantalla que
    // usa Académico. Antes Horario tenía su propio diálogo para la misma entidad y los dos
    // formularios divergían en todo lo que nadie sincronizaba a mano.
    onAddClassClick: () -> Unit,
    onEditSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScheduleViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var identityView by rememberSaveable { mutableStateOf(IdentityScheduleView.TIMETABLE) }
    var selectedEpochDay by rememberSaveable { mutableStateOf(LocalDate.now().toEpochDay()) }
    var selectedSession by remember { mutableStateOf<ClassSession?>(null) }
    var historySubjectId by rememberSaveable { mutableStateOf<String?>(null) }
    var showFullSchedule by rememberSaveable { mutableStateOf(false) }
    var showAgendaMenu by rememberSaveable { mutableStateOf(false) }
    var agendaCreateKind by remember { mutableStateOf<AgendaCreateKind?>(null) }
    var editingAgendaEvent by remember { mutableStateOf<com.unistack.app.feature_schedule.domain.AgendaEvent?>(null) }

    val selectedDate = LocalDate.ofEpochDay(selectedEpochDay)
    val dayOccurrences = state.occurrences.filter { it.dateEpochDay == selectedEpochDay }

    ScheduleIdentityContent(
        view = identityView,
        selectedDate = selectedDate,
        uiState = state,
        onViewChange = { identityView = it },
        onDateSelected = { selectedEpochDay = it.toEpochDay() },
        onSessionClick = { date, session ->
            selectedEpochDay = date.toEpochDay()
            selectedSession = session
        },
        onTaskClick = onTaskClick,
        onAgendaEventClick = { event ->
            editingAgendaEvent = event
            agendaCreateKind = when (event.kind) {
                com.unistack.app.feature_schedule.domain.AgendaEventKind.REMINDER -> AgendaCreateKind.REMINDER
                com.unistack.app.feature_schedule.domain.AgendaEventKind.PERSONAL,
                com.unistack.app.feature_schedule.domain.AgendaEventKind.MEETING -> AgendaCreateKind.PERSONAL
                com.unistack.app.feature_schedule.domain.AgendaEventKind.CUSTOM -> AgendaCreateKind.CUSTOM
            }
        },
        onAddClass = onAddClassClick,
        onAddEvent = { showAgendaMenu = true },
        onOpenFullSchedule = { showFullSchedule = true },
        modifier = modifier
    )
    if (showAgendaMenu) {
        AgendaCreateMenuSheet(
            onDismiss = { showAgendaMenu = false },
            onSelect = { kind ->
                showAgendaMenu = false
                editingAgendaEvent = null
                agendaCreateKind = kind
            },
            onAddClass = {
                showAgendaMenu = false
                onAddClassClick()
            }
        )
    }
    agendaCreateKind?.let { kind ->
        AgendaComposerSheet(
            kind = kind,
            initialDate = selectedDate,
            subjects = state.subjects,
            existingEvent = editingAgendaEvent,
            onDismiss = {
                agendaCreateKind = null
                editingAgendaEvent = null
            },
            onSaveAcademic = viewModel::saveAcademicAgendaItem,
            onSaveEvent = viewModel::saveAgendaEvent,
            onDeleteEvent = viewModel::deleteAgendaEvent
        )
    }
    if (showFullSchedule) {
        FullScheduleDialog(
            selectedDate = selectedDate,
            sessions = state.sessions,
            subjects = state.subjects,
            use24Hour = state.accessibility.use24HourTime,
            onDismiss = { showFullSchedule = false },
            onWeekChange = { selectedEpochDay = it.toEpochDay() },
            onSessionClick = { date, session ->
                selectedEpochDay = date.toEpochDay()
                showFullSchedule = false
                selectedSession = session
            }
        )
    }
    selectedSession?.let { session ->
        ClassDetailsSheet(
            session = session,
            subject = state.subjects.firstOrNull { it.id == session.subjectId },
            date = selectedDate,
            occurrence = dayOccurrences.firstOrNull { it.sessionId == session.id },
            use24Hour = state.accessibility.use24HourTime,
            onDismiss = { selectedSession = null },
            onEdit = {
                selectedSession = null
                onEditSubjectClick(session.subjectId)
            },
            onHistory = {
                historySubjectId = session.subjectId
                selectedSession = null
            },
            onDelete = {
                viewModel.delete(session.id)
                selectedSession = null
            },
            onStatus = { status ->
                viewModel.saveOccurrence(
                    sessionId = session.id,
                    dateEpochDay = selectedEpochDay,
                    status = status,
                    modality = ClassModality.IN_PERSON,
                    absenceReason = null,
                    note = ""
                )
                selectedSession = null
            }
        )
    }

    historySubjectId?.let { subjectId ->
        val subject = state.subjects.firstOrNull { it.id == subjectId }
        if (subject != null) {
            SubjectHistoryDialog(
                subject = subject,
                sessions = state.sessions.filter { it.subjectId == subjectId },
                occurrences = state.occurrences,
                onDismiss = { historySubjectId = null },
                onMarkAttendance = { date, session ->
                    historySubjectId = null
                    selectedEpochDay = date.toEpochDay()
                    selectedSession = session
                }
            )
        }
    }

}

@Composable
private fun ScheduleHeader(
    title: String,
    showBack: Boolean,
    onBack: () -> Unit,
    onCalendarClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(44.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBack) {
            IconButton(onClick = onBack, modifier = Modifier.size(36.dp)) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Spacer(Modifier.width(4.dp))
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.sp
        )
        IconButton(onClick = onCalendarClick) {
            Icon(Icons.Rounded.CalendarMonth, contentDescription = "Abrir calendario", tint = MaterialTheme.colorScheme.onSurface)
        }
        IconButton(onClick = onMenuClick) {
            Icon(Icons.Rounded.MoreVert, contentDescription = "M\u00e1s opciones", tint = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun WeekPicker(
    selectedDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val weekStart = selectedDate.weekStart()
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Semana anterior")
            }
            Text(
                "${weekStart.dayOfMonth} - ${weekStart.plusDays(6).dayOfMonth} de ${weekStart.format(DateTimeFormatter.ofPattern("MMMM", SpanishLocale))}",
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
            IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Semana siguiente")
            }
        }
        Surface(
            shape = ScheduleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
        ) {
            Row(Modifier.fillMaxWidth().padding(4.dp)) {
                (0L..6L).forEach { offset ->
                    val date = weekStart.plusDays(offset)
                    val selected = date == selectedDate
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(ScheduleShape)
                            .background(if (selected) ScheduleAccent else Color.Transparent)
                            .clickable { onSelect(date) }
                            .padding(vertical = 5.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(dayLetter(date.dayOfWeek), fontSize = 10.sp, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(date.dayOfMonth.toString(), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun TimetableRangeControls(
    visibleHours: Int,
    onVisibleHoursChange: (Int) -> Unit,
    onOpenList: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(36.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        IconButton(
            onClick = { onVisibleHoursChange((visibleHours + 2).coerceAtMost(14)) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.ZoomOut, contentDescription = "Mostrar m\u00e1s horas", tint = ScheduleAccent, modifier = Modifier.size(19.dp))
        }
        Slider(
            value = visibleHours.toFloat(),
            onValueChange = { onVisibleHoursChange(it.roundToInt().coerceIn(4, 14)) },
            modifier = Modifier.weight(1f),
            valueRange = 4f..14f,
            steps = 4,
            colors = SliderDefaults.colors(
                thumbColor = ScheduleAccent,
                activeTrackColor = ScheduleAccent,
                inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant,
                activeTickColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                inactiveTickColor = ScheduleAccent.copy(alpha = 0.55f)
            )
        )
        IconButton(
            onClick = { onVisibleHoursChange((visibleHours - 2).coerceAtLeast(4)) },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(Icons.Rounded.ZoomIn, contentDescription = "Acercar horario", tint = ScheduleAccent, modifier = Modifier.size(19.dp))
        }
        IconButton(onClick = onOpenList, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Rounded.ViewList, contentDescription = "Abrir lista del mes", tint = ScheduleAccent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun WeekSummary(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val weekStart = selectedDate.weekStart()
    val entries = (0L..6L).flatMap { offset ->
        val date = weekStart.plusDays(offset)
        sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
            .map { date to it }
    }.sortedWith(compareBy<Pair<LocalDate, ClassSession>> { it.first }.thenBy { it.second.startMinute })
    val upcoming = entries.filter { !it.first.isBefore(selectedDate) }.take(2)
        .ifEmpty { entries.takeLast(2) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Esta semana", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        if (upcoming.isEmpty()) {
            CalendarEmptyState("No hay clases programadas esta semana")
        }
        upcoming.forEach { (date, session) ->
            val subject = subjects.firstOrNull { it.id == session.subjectId }
            val color = subject.scheduleColor()
            Surface(
                onClick = { onSessionClick(date, session) },
                modifier = Modifier.fillMaxWidth(),
                shape = ScheduleShape,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
            ) {
                Row(Modifier.height(70.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.width(3.dp).fillMaxHeight().background(color))
                    Box(Modifier.padding(start = 12.dp).size(8.dp).clip(CircleShape).background(color))
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            date.format(DateTimeFormatter.ofPattern("EEE, d 'de' MMMM", SpanishLocale)).capitalized(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                        Text(subject?.name ?: "Clase", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${formatMinute(session.startMinute, use24Hour)} - ${formatMinute(session.endMinute, use24Hour)}  \u2022  ${session.place.room.ifBlank { "Sin aula" }}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Abrir clase", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp))
                }
            }
        }
    }
}

@Composable
private fun CalendarToolbar(
    selectedDate: LocalDate,
    mode: CalendarMode,
    onMode: (CalendarMode) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onPrevious, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior")
            }
            Text(
                selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", SpanishLocale)).capitalized(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente")
            }
        }
        Surface(shape = ScheduleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
            Row(Modifier.fillMaxWidth()) {
                CalendarMode.entries.forEach { option ->
                    Text(
                        option.label,
                        modifier = Modifier
                            .weight(1f)
                            .clip(ScheduleShape)
                            .background(if (mode == option) ScheduleAccent.copy(alpha = 0.16f) else Color.Transparent)
                            .clickable { onMode(option) }
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        color = if (mode == option) ScheduleAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (mode == option) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthGrid(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    tasks: List<StudentTask>,
    subjects: List<Subject>,
    expanded: Boolean,
    onSelect: (LocalDate) -> Unit
) {
    val month = YearMonth.from(selectedDate)
    val firstCell = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val leadingDays = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
    val cellCount = ((leadingDays + month.lengthOfMonth() + 6) / 7) * 7
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Row(Modifier.fillMaxWidth()) {
            DayLabels.medium.forEach { label ->
                Text(label, Modifier.weight(1f), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
        Surface(
            shape = ScheduleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.7f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
        ) {
            Column {
                repeat(cellCount / 7) { row ->
                    Row(Modifier.fillMaxWidth()) {
                        repeat(7) { column ->
                            val date = firstCell.plusDays((row * 7 + column).toLong())
                            val dateSessions = sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                            val dateTasks = tasks.filter { !it.completed && it.dueDateMillis.asLocalDate() == date }
                            MonthCell(
                                modifier = Modifier.weight(1f),
                                date = date,
                                inMonth = YearMonth.from(date) == month,
                                selected = date == selectedDate,
                                sessions = dateSessions,
                                tasks = dateTasks,
                                subjects = subjects,
                                expanded = expanded,
                                onClick = { onSelect(date) }
                            )
                        }
                    }
                }
            }
        }
        if (expanded) MonthLegend(sessions, subjects)
    }
}

@Composable
private fun MonthCell(
    modifier: Modifier,
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    sessions: List<ClassSession>,
    tasks: List<StudentTask>,
    subjects: List<Subject>,
    expanded: Boolean,
    onClick: () -> Unit
) {
    val height = if (expanded) 70.dp else 54.dp
    Column(
        modifier = modifier
            .height(height)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            .padding(3.dp)
            .then(
                if (selected) Modifier
                    .clip(ScheduleShape)
                    .background(ScheduleAccent.copy(alpha = if (expanded) 0.12f else 0.92f))
                    .border(1.dp, ScheduleAccent, ScheduleShape)
                else Modifier
            )
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            date.dayOfMonth.toString(),
            color = when {
                selected && !expanded -> MaterialTheme.colorScheme.onPrimary
                !inMonth -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.onSurface
            },
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
        if (expanded) {
            sessions.take(3).forEach { session ->
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                Text(
                    subject?.name?.shortName() ?: "Clase",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(subject.scheduleColor())
                        .padding(horizontal = 2.dp, vertical = 1.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 7.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Row(
                modifier = Modifier.padding(top = 7.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                sessions.take(3).forEach { session ->
                    val subject = subjects.firstOrNull { it.id == session.subjectId }
                    Box(Modifier.size(6.dp).clip(CircleShape).background(subject.scheduleColor()))
                }
                if (tasks.isNotEmpty()) Box(Modifier.size(6.dp).clip(CircleShape).background(ScheduleCancelled))
            }
        }
    }
}

@Composable
private fun MonthLegend(sessions: List<ClassSession>, subjects: List<Subject>) {
    val used = subjects.filter { subject -> sessions.any { it.subjectId == subject.id } }.take(4)
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        used.forEach { subject ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(subject.scheduleColor()))
                Spacer(Modifier.width(6.dp))
                Text(subject.name, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun DayAgenda(
    date: LocalDate,
    sessions: List<ClassSession>,
    tasks: List<StudentTask>,
    occurrences: List<ClassOccurrence>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    rich: Boolean,
    onSessionClick: (ClassSession) -> Unit,
    onTaskClick: (String) -> Unit,
    onOpenDay: () -> Unit,
    onAddClass: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onOpenDay),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(date.longTitle(), Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Abrir d\u00eda", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (sessions.isEmpty() && tasks.isEmpty()) {
            Surface(shape = ScheduleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
                Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Event, contentDescription = null, tint = ScheduleAccent, modifier = Modifier.size(30.dp))
                    Text("No hay clases para este d\u00eda", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
                    TextButton(onClick = onAddClass) { Text("A\u00f1adir clase", color = ScheduleAccent) }
                }
            }
        } else {

            sessions.forEach { session ->
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                val occurrence = occurrences.firstOrNull { it.sessionId == session.id }
                AgendaClassCard(session, subject, occurrence, use24Hour, rich) { onSessionClick(session) }
            }
            tasks.forEach { task -> AgendaTaskCard(task, subjects.firstOrNull { it.id == task.subjectId }, onTaskClick) }
        }
    }
}

@Composable
private fun AgendaClassCard(
    session: ClassSession,
    subject: Subject?,
    occurrence: ClassOccurrence?,
    use24Hour: Boolean,
    rich: Boolean,
    onClick: () -> Unit
) {
    val color = subject.scheduleColor()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = ScheduleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Row(modifier = Modifier.height(if (rich) 88.dp else 76.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(color))
            if (rich) {
                Box(
                    Modifier.padding(start = 14.dp).size(48.dp).clip(ScheduleShape).background(color.copy(alpha = 0.86f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                }
            }
            Column(Modifier.padding(start = 14.dp).width(64.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatMinute(session.startMinute, use24Hour), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(formatMinute(session.endMinute, use24Hour), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(subject?.name ?: "Clase", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(listOf(session.place.room, session.place.professor).filter(String::isNotBlank).joinToString("  \u2022  "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            if (occurrence != null && occurrence.status != ClassAttendanceStatus.PENDING) {
                Box(Modifier.size(8.dp).clip(CircleShape).background(occurrence.status.color()))
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp))
        }
    }
}

@Composable
private fun AgendaTaskCard(task: StudentTask, subject: Subject?, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(task.id) },
        modifier = Modifier.fillMaxWidth(),
        shape = ScheduleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f))
    ) {
        Row(Modifier.height(70.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.width(4.dp).fillMaxHeight().background(ScheduleCancelled))
            Icon(Icons.Rounded.Event, contentDescription = null, tint = ScheduleCancelled, modifier = Modifier.padding(horizontal = 14.dp))
            Column(Modifier.weight(1f)) {
                Text(task.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(subject?.name ?: "Tarea", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(end = 10.dp))
        }
    }
}

@Composable
private fun CalendarAgendaView(
    month: YearMonth,
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onDateClick: (LocalDate) -> Unit,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val dates = monthScheduleDates(month, sessions)
    Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
        if (dates.isEmpty()) {
            CalendarEmptyState("No hay clases programadas este mes")
        }
        dates.forEach { (date, dateSessions) ->
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onDateClick(date) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (date == LocalDate.now()) "Hoy  \u00b7  ${date.longTitle()}" else date.longTitle(),
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Abrir d\u00eda", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            }
            dateSessions.forEach { session ->
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                val occurrence = occurrences.firstOrNull { it.sessionId == session.id && it.dateEpochDay == date.toEpochDay() }
                AgendaClassCard(session, subject, occurrence, use24Hour, rich = false) {
                    onSessionClick(date, session)
                }
            }
        }
    }
}

@Composable
private fun CalendarListView(
    month: YearMonth,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val dates = monthScheduleDates(month, sessions)
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(
            month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy", SpanishLocale)).capitalized(),
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
        if (dates.isEmpty()) {
            CalendarEmptyState("No hay clases programadas este mes")
        }
        dates.forEach { (date, dateSessions) ->
            dateSessions.forEach { session ->
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(ScheduleShape)
                        .clickable { onSessionClick(date, session) }
                        .padding(vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        date.format(DateTimeFormatter.ofPattern("dd MMM (EEE)", SpanishLocale)),
                        modifier = Modifier.width(92.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Box(Modifier.size(7.dp).clip(CircleShape).background(subject.scheduleColor()))
                    Spacer(Modifier.width(9.dp))
                    Text(formatMinute(session.startMinute, use24Hour), modifier = Modifier.width(48.dp), color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text(subject?.name ?: "Clase", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
            }
        }
    }
}

@Composable
private fun DayScheduleView(
    date: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (ClassSession) -> Unit,
    onAddClass: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(date.longTitle(), color = MaterialTheme.colorScheme.onSurface, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
        if (sessions.isEmpty()) {
            CalendarEmptyState("No hay clases para este d\u00eda")
        }
        sessions.forEach { session ->
            val subject = subjects.firstOrNull { it.id == session.subjectId }
            val color = subject.scheduleColor()
            Surface(
                onClick = { onSessionClick(session) },
                modifier = Modifier.fillMaxWidth(),
                shape = ScheduleShape,
                color = color.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
            ) {
                Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.width(58.dp)) {
                        Text(formatMinute(session.startMinute, use24Hour), color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(formatMinute(session.endMinute, use24Hour), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    }
                    Box(Modifier.width(3.dp).height(42.dp).background(color))
                    Spacer(Modifier.width(11.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(subject?.name ?: "Clase", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(listOf(session.place.room, session.place.professor).filter(String::isNotBlank).joinToString("  \u2022  "), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (sessions.isEmpty()) {
            TextButton(onClick = onAddClass, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(Modifier.width(5.dp))
                Text("A\u00f1adir clase", color = ScheduleAccent)
            }
        }
    }
}

@Composable
private fun CalendarEmptyState(message: String) {
    Surface(shape = ScheduleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)) {
        Text(message, Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

@Composable
private fun SubjectHistoryDialog(
    subject: Subject,
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    onDismiss: () -> Unit,
    onMarkAttendance: (LocalDate, ClassSession) -> Unit
) {
    val entries = remember(sessions, occurrences) {
        buildSubjectHistory(sessions, occurrences, LocalDate.now())
    }
    val attended = entries.count { it.status == ClassAttendanceStatus.ATTENDED }
    val absent = entries.count { it.status == ClassAttendanceStatus.ABSENT }
    val decided = attended + absent
    val rate = if (decided == 0) 0 else (attended.toFloat() / decided * 100).roundToInt()
    val pending = entries
        .filter { it.status == ClassAttendanceStatus.PENDING && !it.date.isAfter(LocalDate.now()) }
        .maxByOrNull(HistoryEntry::date)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(88.dp).background(ScheduleAccent)
                ) {
                    IconButton(onClick = onDismiss, modifier = Modifier.align(Alignment.TopStart).padding(4.dp)) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    Box(
                        modifier = Modifier.align(Alignment.BottomCenter).offset(y = 22.dp).size(50.dp)
                            .clip(CircleShape).background(MaterialTheme.colorScheme.background)
                            .border(1.dp, ScheduleAccent.copy(alpha = 0.35f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = ScheduleAccent)
                    }
                }
                Spacer(Modifier.height(30.dp))
                Text(
                    subject.name,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 18.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(14.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = ScheduleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("Asistencia general", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            Text("$rate%", color = MaterialTheme.colorScheme.onSurface, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                            Text("$attended asistencias  \u2022  $absent faltas", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                        Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { rate / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = ScheduleAccent,
                                trackColor = MaterialTheme.colorScheme.outlineVariant,
                                strokeWidth = 4.dp
                            )
                            Text("$rate%", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Text(
                    "Historial",
                    modifier = Modifier.padding(start = 18.dp, top = 16.dp, bottom = 7.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Surface(
                    modifier = Modifier.fillMaxWidth().weight(1f).padding(horizontal = 16.dp),
                    shape = ScheduleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    LazyColumn(contentPadding = PaddingValues(vertical = 5.dp)) {
                        if (entries.isEmpty()) {
                            item {
                                Text("A\u00fan no hay clases en el historial", Modifier.fillMaxWidth().padding(20.dp), textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        items(entries, key = { "${it.session.id}:${it.date.toEpochDay()}" }) { entry ->
                            HistoryRow(entry)
                        }
                    }
                }
                Button(
                    onClick = { pending?.let { onMarkAttendance(it.date, it.session) } },
                    enabled = pending != null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = ScheduleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = ScheduleAccent),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    Text("Marcar asistencia", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(entry: HistoryEntry) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Event, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(
            entry.date.format(DateTimeFormatter.ofPattern("d MMM (EEE)", SpanishLocale)),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 11.sp
        )
        Box(Modifier.size(7.dp).clip(CircleShape).background(entry.status.color()))
        Spacer(Modifier.width(7.dp))
        Text(entry.status.label(), color = entry.status.color(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ClassDetailsSheet(
    session: ClassSession,
    subject: Subject?,
    date: LocalDate,
    occurrence: ClassOccurrence?,
    use24Hour: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onHistory: () -> Unit,
    onDelete: () -> Unit,
    onStatus: (ClassAttendanceStatus) -> Unit
) {
    // Abierto del todo desde el principio. Con la altura a medias \u2014lo que hace un
    // ModalBottomSheet por defecto\u2014 las acciones del final quedaban fuera de la pantalla y
    // hab\u00eda que arrastrar el sheet hacia arriba para descubrir que estaban ah\u00ed.
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val accent = subject.scheduleColor()
    val status = occurrence?.status ?: ClassAttendanceStatus.PENDING

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.background
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                // Y aun as\u00ed el contenido rueda: en una pantalla baja, o con la letra del
                // sistema en grande, el sheet completo tampoco da para todo.
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 18.dp, end = 18.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(46.dp).clip(ScheduleShape).background(accent), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = contentColorOn(accent))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        subject?.name ?: "Clase",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 19.sp,
                        lineHeight = 22.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(date.longTitle(), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
                StatusPill(status)
            }

            // Los datos, en rejilla de dos. Antes iban en una l\u00ednea de texto pegados con
            // puntos: si faltaba el aula y el profesor, la l\u00ednea quedaba vac\u00eda y el hueco
            // parec\u00eda un fallo de la app en vez de un dato que nadie hab\u00eda rellenado.
            ClassInfoGrid(
                listOf(
                    ClassInfo(Icons.Rounded.Schedule, "Horario", "${formatMinute(session.startMinute, use24Hour)} - ${formatMinute(session.endMinute, use24Hour)}"),
                    ClassInfo(Icons.Rounded.HourglassBottom, "Duraci\u00f3n", durationLabel(session.endMinute - session.startMinute)),
                    ClassInfo(Icons.Rounded.Place, "Aula", session.place.room.ifBlank { "Sin aula" }),
                    ClassInfo(Icons.Rounded.Person, "Profesor", session.place.professor.ifBlank { "Sin profesor" }),
                    ClassInfo(Icons.Rounded.Repeat, "Repetici\u00f3n", repeatLabel(session.repeatEveryWeeks)),
                    ClassInfo(Icons.Rounded.NotificationsNone, "Recordatorio", reminderLabel(session.reminderMinutes))
                )
            )

            Text("Registrar asistencia", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ClassAttendanceStatus.ATTENDED, ClassAttendanceStatus.ABSENT, ClassAttendanceStatus.CANCELLED).forEach { option ->
                    val selected = occurrence?.status == option
                    Surface(
                        // Volver a tocar el estado marcado lo deshace: si te equivocas de
                        // bot\u00f3n, antes no hab\u00eda forma de volver a \u00abpendiente\u00bb.
                        onClick = { onStatus(if (selected) ClassAttendanceStatus.PENDING else option) },
                        modifier = Modifier.weight(1f),
                        shape = ScheduleShape,
                        color = if (selected) option.color().copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                        border = BorderStroke(1.dp, if (selected) option.color() else MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Column(
                            Modifier.padding(vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                option.icon(),
                                contentDescription = null,
                                tint = if (selected) option.color() else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                option.label(),
                                textAlign = TextAlign.Center,
                                color = if (selected) option.color() else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            DetailActionRow(Icons.Rounded.CalendarMonth, "Ver historial", MaterialTheme.colorScheme.onSurface, onHistory)
            DetailActionRow(Icons.Rounded.Edit, "Editar clase", MaterialTheme.colorScheme.onSurface, onEdit)
            DetailActionRow(Icons.Rounded.DeleteOutline, "Eliminar clase", MaterialTheme.colorScheme.error, onDelete)
        }
    }
}

/** El estado de hoy, al lado del nombre: es lo primero que se viene a mirar. */
@Composable
private fun StatusPill(status: ClassAttendanceStatus) {
    Surface(
        shape = CircleShape,
        color = status.color().copy(alpha = 0.16f),
        border = BorderStroke(1.dp, status.color().copy(alpha = 0.5f))
    ) {
        Text(
            status.label(),
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            color = status.color(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private data class ClassInfo(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
    val value: String
)

@Composable
private fun ClassInfoGrid(items: List<ClassInfo>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items.chunked(2).forEach { pair ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pair.forEach { info ->
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = ScheduleShape,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh
                    ) {
                        Column(
                            Modifier.padding(horizontal = 11.dp, vertical = 9.dp),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(info.icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(13.dp))
                                Spacer(Modifier.width(5.dp))
                                Text(info.label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                            Text(
                                info.value,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                lineHeight = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private fun durationLabel(minutes: Int): String {
    val hours = minutes / 60
    val rest = minutes % 60
    return when {
        hours == 0 -> "$rest min"
        rest == 0 -> "$hours h"
        else -> "$hours h $rest min"
    }
}

private fun repeatLabel(everyWeeks: Int): String =
    if (everyWeeks <= 1) "Cada semana" else "Cada $everyWeeks semanas"

private fun reminderLabel(minutes: Int): String = when {
    minutes <= 0 -> "Sin recordatorio"
    minutes % 60 == 0 -> "${minutes / 60} h antes"
    else -> "$minutes min antes"
}

private fun ClassAttendanceStatus.icon(): androidx.compose.ui.graphics.vector.ImageVector = when (this) {
    ClassAttendanceStatus.ATTENDED -> Icons.Rounded.Check
    ClassAttendanceStatus.ABSENT -> Icons.Rounded.Close
    ClassAttendanceStatus.CANCELLED -> Icons.Rounded.EventBusy
    ClassAttendanceStatus.RESCHEDULED -> Icons.Rounded.Schedule
    ClassAttendanceStatus.PENDING -> Icons.Rounded.Schedule
}

@Composable
private fun DetailActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(ScheduleShape).clickable(onClick = onClick).padding(vertical = 9.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(11.dp))
        Text(label, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun OptionsSheet(
    onDismiss: () -> Unit,
    onToday: () -> Unit,
    onTimetable: () -> Unit,
    onCalendarMode: (CalendarMode) -> Unit,
    onAddClass: () -> Unit,
    onAddTask: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 18.dp, end = 18.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OptionRow(Icons.Rounded.CalendarMonth, "Ir a hoy", onToday)
            OptionRow(Icons.Rounded.Schedule, "Horario semanal", onTimetable)
            OptionRow(Icons.Rounded.CalendarMonth, "Calendario mensual") { onCalendarMode(CalendarMode.MONTH) }
            OptionRow(Icons.Rounded.Event, "Vista agenda") { onCalendarMode(CalendarMode.AGENDA) }
            OptionRow(Icons.Rounded.ExpandMore, "Lista del mes") { onCalendarMode(CalendarMode.LIST) }
            OptionRow(Icons.AutoMirrored.Rounded.MenuBook, "A\u00f1adir clase", onAddClass)
            OptionRow(Icons.Rounded.Event, "A\u00f1adir tarea", onAddTask)
        }
    }
}

@Composable
private fun OptionRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clip(ScheduleShape).clickable(onClick = onClick).padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = ScheduleAccent)
        Spacer(Modifier.width(12.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
    }
}

private data class HistoryEntry(
    val date: LocalDate,
    val session: ClassSession,
    val status: ClassAttendanceStatus
)

private val SpanishLocale: Locale = Locale.forLanguageTag("es")

@Composable
@ReadOnlyComposable
private fun Subject?.scheduleColor(): Color = this?.customColor?.let(::Color) ?: this?.let { subject -> subjectAccent(subject) } ?: ScheduleAccent

private fun LocalDate.weekStart(): LocalDate = minusDays((dayOfWeek.value - 1).toLong())

private fun Long.asLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun LocalDate.weekdayName(): String = format(DateTimeFormatter.ofPattern("EEEE", SpanishLocale)).capitalized()

private fun LocalDate.longTitle(): String = format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", SpanishLocale)).capitalized()

private fun LocalDate.shortDate(): String = format(DateTimeFormatter.ofPattern("d MMM yyyy", SpanishLocale))

private fun String.capitalized(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(SpanishLocale) else it.toString() }

private fun String.shortName(): String = split(' ').filter(String::isNotBlank).take(2).joinToString(" ") { word ->
    if (word.length <= 5) word else word.take(5) + "."
}

private fun dayLetter(day: DayOfWeek): String = DayLabels.short[day.value - 1]

private fun formatMinute(value: Int, use24Hour: Boolean): String {
    val hour = value / 60
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) "a. m." else "p. m.")
}

private fun findNextSession(fromDate: LocalDate, sessions: List<ClassSession>): Pair<LocalDate, ClassSession>? {
    if (sessions.isEmpty()) return null
    return (0L..13L).asSequence().mapNotNull { offset ->
        val date = fromDate.plusDays(offset)
        sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
            .minByOrNull(ClassSession::startMinute)
            ?.let { date to it }
    }.firstOrNull()
}

private fun monthScheduleDates(
    month: YearMonth,
    sessions: List<ClassSession>
): List<Pair<LocalDate, List<ClassSession>>> = (1..month.lengthOfMonth()).mapNotNull { day ->
    val date = month.atDay(day)
    val dateSessions = sessions
        .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
        .sortedBy(ClassSession::startMinute)
    if (dateSessions.isNotEmpty()) date to dateSessions else null
}

private fun buildSubjectHistory(
    sessions: List<ClassSession>,
    occurrences: List<ClassOccurrence>,
    today: LocalDate
): List<HistoryEntry> {
    val occurrenceByKey = occurrences.associateBy { it.sessionId to it.dateEpochDay }
    val allEntries = (-120L..30L).flatMap { offset ->
        val date = today.plusDays(offset)
        sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }.map { session ->
            HistoryEntry(
                date = date,
                session = session,
                status = occurrenceByKey[session.id to date.toEpochDay()]?.status ?: ClassAttendanceStatus.PENDING
            )
        }
    }
    val future = allEntries.filter { it.date.isAfter(today) }
        .sortedBy(HistoryEntry::date)
        .take(2)
    val recent = allEntries.filter { !it.date.isAfter(today) }
        .sortedWith(compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.session.startMinute })
        .take(22)
    return (future + recent).sortedWith(
        compareByDescending<HistoryEntry> { it.date }.thenByDescending { it.session.startMinute }
    )
}

private fun ClassAttendanceStatus.label(): String = when (this) {
    ClassAttendanceStatus.PENDING -> "Pendiente"
    ClassAttendanceStatus.ATTENDED -> "Asist\u00ed"
    ClassAttendanceStatus.ABSENT -> "Falta"
    ClassAttendanceStatus.CANCELLED -> "Cancelada"
    ClassAttendanceStatus.RESCHEDULED -> "Reprogramada"
}

@Composable
private fun ClassAttendanceStatus.color(): Color = when (this) {
    ClassAttendanceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant
    ClassAttendanceStatus.ATTENDED -> ScheduleAccent
    ClassAttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error
    ClassAttendanceStatus.CANCELLED -> ScheduleCancelled
    ClassAttendanceStatus.RESCHEDULED -> ScheduleRescheduled
}
