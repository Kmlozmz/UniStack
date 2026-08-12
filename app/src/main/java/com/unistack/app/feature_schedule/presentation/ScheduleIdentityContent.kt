package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.utils.NO_DATA
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.domain.SurfaceStyle
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

internal enum class IdentityScheduleView {
    TIMETABLE,
    CALENDAR
}

private enum class IdentityMetricDetail {
    SUBJECTS,
    EVENTS,
    DELIVERIES,
    EXAMS
}

private val IdentityAccent: Color
    get() = UniStackColors.Primary
private val IdentityLocale = Locale.forLanguageTag("es")

@Composable
internal fun ScheduleIdentityContent(
    view: IdentityScheduleView,
    selectedDate: LocalDate,
    uiState: ScheduleUiState,
    onViewChange: (IdentityScheduleView) -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onSessionClick: (LocalDate, ClassSession) -> Unit,
    onTaskClick: (String) -> Unit,
    onAgendaEventClick: (AgendaEvent) -> Unit,
    onAddClass: () -> Unit,
    onAddEvent: () -> Unit,
    onOpenFullSchedule: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalInterfaceSpacing.current
    var metricDetail by remember { mutableStateOf<IdentityMetricDetail?>(null) }
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            top = spacing.cardPadding,
            end = spacing.screenHorizontal,
            bottom = spacing.cardPadding
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.section)
    ) {
        item {
            IdentityHeader(view)
        }
        item {
            IdentityModeSwitch(view = view, onViewChange = onViewChange)
        }

        when (view) {
            IdentityScheduleView.TIMETABLE -> {
                item {
                    TimetableMetrics(
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        use24Hour = uiState.accessibility.use24HourTime,
                        onSubjectsClick = { metricDetail = IdentityMetricDetail.SUBJECTS },
                        onSessionClick = onSessionClick
                    )
                }
                item {
                    FullScheduleLaunchCard(onClick = onOpenFullSchedule)
                }
                item {
                    IdentityWeeklyTimeline(
                        selectedDate = selectedDate,
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        use24Hour = uiState.accessibility.use24HourTime,
                        onSessionClick = onSessionClick
                    )
                }
                item {
                    NextClassPanel(
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        use24Hour = uiState.accessibility.use24HourTime,
                        onSessionClick = onSessionClick
                    )
                }
                item {
                    IdentityPrimaryButton(label = "Agregar clase", onClick = onAddClass)
                }
            }

            IdentityScheduleView.CALENDAR -> {
                item {
                    CalendarMetrics(
                        month = YearMonth.from(selectedDate),
                        tasks = uiState.tasks,
                        agendaEvents = uiState.agendaEvents,
                        onEventsClick = { metricDetail = IdentityMetricDetail.EVENTS },
                        onDeliveriesClick = { metricDetail = IdentityMetricDetail.DELIVERIES },
                        onExamsClick = { metricDetail = IdentityMetricDetail.EXAMS }
                    )
                }
                item {
                    IdentityMonthCalendar(
                        selectedDate = selectedDate,
                        sessions = uiState.sessions,
                        tasks = uiState.tasks,
                        subjects = uiState.subjects,
                        agendaEvents = uiState.agendaEvents,
                        onDateSelected = onDateSelected
                    )
                }
                item {
                    SelectedDayPanel(
                        date = selectedDate,
                        sessions = uiState.sessions,
                        tasks = uiState.tasks,
                        subjects = uiState.subjects,
                        agendaEvents = uiState.agendaEvents,
                        use24Hour = uiState.accessibility.use24HourTime,
                        onSessionClick = onSessionClick,
                        onTaskClick = onTaskClick,
                        onAgendaEventClick = onAgendaEventClick
                    )
                }
                item {
                    IdentityPrimaryButton(label = "Agregar a la agenda", onClick = onAddEvent)
                }
            }
        }
    }

    metricDetail?.let { detail ->
        IdentityMetricDetailsSheet(
            detail = detail,
            selectedDate = selectedDate,
            uiState = uiState,
            onDismiss = { metricDetail = null },
            onSessionClick = { date, session ->
                metricDetail = null
                onSessionClick(date, session)
            },
            onTaskClick = { taskId ->
                metricDetail = null
                onTaskClick(taskId)
            },
            onAgendaEventClick = { event ->
                metricDetail = null
                onAgendaEventClick(event)
            }
        )
    }
}

@Composable
private fun FullScheduleLaunchCard(onClick: () -> Unit) {
    IdentitySurface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumCard,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(IdentityAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Fullscreen,
                    contentDescription = null,
                    tint = IdentityAccent,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Horario completo",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Consulta todas las horas y los 7 días",
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Abrir horario completo",
                tint = UniStackColors.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun IdentitySurface(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.MediumCard,
    color: Color = UniStackColors.Card,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val appearance = LocalAppearancePreferences.current
    val resolvedColor = if (appearance.surfaceStyle == SurfaceStyle.TRANSLUCENT) {
        color.copy(alpha = 0.90f)
    } else {
        color
    }
    val border = if (appearance.surfaceStyle == SurfaceStyle.OUTLINED) {
        BorderStroke(0.7.dp, UniStackColors.SoftOutline.copy(alpha = 0.62f))
    } else {
        null
    }
    val shadowElevation = when (appearance.surfaceStyle) {
        SurfaceStyle.ELEVATED -> 6.dp
        SurfaceStyle.TRANSLUCENT -> 2.dp
        SurfaceStyle.FLAT, SurfaceStyle.OUTLINED -> 0.dp
    }
    if (onClick == null) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = resolvedColor,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = shadowElevation,
            content = content
        )
    } else {
        Surface(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            color = resolvedColor,
            border = border,
            tonalElevation = 0.dp,
            shadowElevation = shadowElevation,
            content = content
        )
    }
}

@Composable
private fun IdentityHeader(view: IdentityScheduleView) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text = if (view == IdentityScheduleView.TIMETABLE) "Horario" else "Calendario",
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (view == IdentityScheduleView.TIMETABLE) {
                "Clases y bloques de tu semana en un mismo lugar."
            } else {
                "Fechas, eventos y entregas de tu mes."
            },
            color = UniStackColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun IdentityModeSwitch(
    view: IdentityScheduleView,
    onViewChange: (IdentityScheduleView) -> Unit
) {
    UniSegmentedControl(
        selected = view,
        options = listOf(
            UniSegmentedOption(IdentityScheduleView.TIMETABLE, "Horario", Icons.AutoMirrored.Rounded.MenuBook),
            UniSegmentedOption(IdentityScheduleView.CALENDAR, "Calendario", Icons.Rounded.CalendarMonth)
        ),
        onSelected = onViewChange,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TimetableMetrics(
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSubjectsClick: () -> Unit,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val next = remember(sessions) { findUpcomingClass(LocalDate.now(), sessions) }
    val subjectCount = subjects.count { subject -> sessions.any { it.subjectId == subject.id } }
    val room = next?.second?.identityPlace()?.room.orEmpty().ifBlank { NO_DATA }
    val openUpcoming = next?.let { upcoming ->
        { onSessionClick(upcoming.first, upcoming.second) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            iconColor = IdentityAccent,
            value = subjectCount.toString(),
            label = if (subjectCount == 1) "Materia" else "Materias",
            onClick = onSubjectsClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Schedule,
            iconColor = IdentityAccent,
            value = next?.second?.let { formatIdentityMinute(it.startMinute, use24Hour) } ?: NO_DATA,
            label = "Próxima",
            onClick = openUpcoming
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Place,
            iconColor = IdentityAccent,
            value = room,
            label = "Aula",
            onClick = openUpcoming
        )
    }
}

@Composable
private fun CalendarMetrics(
    month: YearMonth,
    tasks: List<StudentTask>,
    agendaEvents: List<AgendaEvent>,
    onEventsClick: () -> Unit,
    onDeliveriesClick: () -> Unit,
    onExamsClick: () -> Unit
) {
    val monthTasks = remember(month, tasks) {
        tasks.filter { !it.completed && YearMonth.from(it.dueLocalDate()) == month }
    }
    val eventCount = remember(month, agendaEvents) {
        (1..month.lengthOfMonth()).sumOf { day ->
            val date = month.atDay(day)
            agendaEvents.count { it.occursOn(date) }
        }
    }
    val exams = monthTasks.count { it.type == TaskType.EXAM || it.type == TaskType.TEST }
    val deliveries = monthTasks.size - exams
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CalendarMonth,
            iconColor = IdentityAccent,
            value = eventCount.toString(),
            label = "Eventos",
            onClick = onEventsClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Rounded.Assignment,
            iconColor = IdentityAccent,
            value = deliveries.toString(),
            label = "Entregas",
            onClick = onDeliveriesClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.School,
            iconColor = IdentityAccent,
            value = exams.toString(),
            label = "Exámenes",
            onClick = onExamsClick
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IdentityMetricDetailsSheet(
    detail: IdentityMetricDetail,
    selectedDate: LocalDate,
    uiState: ScheduleUiState,
    onDismiss: () -> Unit,
    onSessionClick: (LocalDate, ClassSession) -> Unit,
    onTaskClick: (String) -> Unit,
    onAgendaEventClick: (AgendaEvent) -> Unit
) {
    val month = YearMonth.from(selectedDate)
    val groupedSubjects = remember(uiState.sessions) {
        uiState.sessions.groupBy(ClassSession::subjectId).values
            .map { it.sortedBy(ClassSession::startMinute) }
            .sortedBy { sessions ->
                uiState.subjects.firstOrNull { it.id == sessions.first().subjectId }?.name.orEmpty()
            }
    }
    val monthAgendaEvents = remember(month, uiState.agendaEvents) {
        buildList {
            (1..month.lengthOfMonth()).forEach { day ->
                val date = month.atDay(day)
                uiState.agendaEvents.filter { it.occursOn(date) }.forEach { add(date to it) }
            }
        }
    }
    val pendingTasks = remember(month, uiState.tasks, detail) {
        uiState.tasks
            .filter { task ->
                !task.completed &&
                    YearMonth.from(task.dueLocalDate()) == month &&
                    when (detail) {
                        IdentityMetricDetail.EXAMS -> task.type == TaskType.EXAM || task.type == TaskType.TEST
                        IdentityMetricDetail.DELIVERIES -> task.type != TaskType.EXAM && task.type != TaskType.TEST
                        else -> false
                    }
            }
            .sortedBy(StudentTask::dueDateMillis)
    }
    val title = when (detail) {
        IdentityMetricDetail.SUBJECTS -> "Materias del horario"
        IdentityMetricDetail.EVENTS -> "Eventos de ${month.format(DateTimeFormatter.ofPattern("MMMM", IdentityLocale)).identityCapitalized()}"
        IdentityMetricDetail.DELIVERIES -> "Entregas pendientes"
        IdentityMetricDetail.EXAMS -> "Exámenes pendientes"
    }
    val icon = when (detail) {
        IdentityMetricDetail.SUBJECTS -> Icons.AutoMirrored.Rounded.MenuBook
        IdentityMetricDetail.EVENTS -> Icons.Rounded.CalendarMonth
        IdentityMetricDetail.DELIVERIES -> Icons.AutoMirrored.Rounded.Assignment
        IdentityMetricDetail.EXAMS -> Icons.Rounded.School
    }
    val count = when (detail) {
        IdentityMetricDetail.SUBJECTS -> groupedSubjects.size
        IdentityMetricDetail.EVENTS -> monthAgendaEvents.size
        IdentityMetricDetail.DELIVERIES, IdentityMetricDetail.EXAMS -> pendingTasks.size
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = UniStackColors.Background,
        shape = AppShapes.LargeCard
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = LocalInterfaceSpacing.current.screenHorizontal)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(42.dp).clip(AppShapes.SmallCard).background(IdentityAccent.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = IdentityAccent)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "$count ${if (count == 1) "elemento" else "elementos"}",
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (count == 0) {
                IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                    Text(
                        "No hay información para mostrar.",
                        modifier = Modifier.padding(18.dp),
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (detail) {
                        IdentityMetricDetail.SUBJECTS -> items(
                            items = groupedSubjects,
                            key = { sessions -> sessions.first().subjectId }
                        ) { subjectSessions ->
                            val session = subjectSessions.first()
                            val subject = uiState.subjects.firstOrNull { it.id == session.subjectId }
                            val occurrenceDate = nextOccurrenceDate(selectedDate, session) ?: selectedDate
                            val days = session.daysOfWeek.sorted()
                                .joinToString(" · ") { identityDayLetter(DayOfWeek.of(it)) }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = subject?.name ?: "Materia",
                                    detail = "$days  \u2022  ${formatIdentityMinute(session.startMinute, uiState.accessibility.use24HourTime)} - ${formatIdentityMinute(session.endMinute, uiState.accessibility.use24HourTime)}  \u2022  ${session.identityPlace().room.ifBlank { "Sin aula" }}",
                                    onClick = { onSessionClick(occurrenceDate, session) }
                                )
                            }
                        }

                        IdentityMetricDetail.EVENTS -> items(
                            items = monthAgendaEvents,
                            key = { (date, event) -> "${date.toEpochDay()}-${event.id}" }
                        ) { (date, event) ->
                            IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                                IdentityEventRow(
                                    color = event.identityColor(),
                                    title = event.title,
                                    detail = "${date.format(DateTimeFormatter.ofPattern("EEE d", IdentityLocale)).identityCapitalized()}  •  ${event.identityTimeText(uiState.accessibility.use24HourTime)}${event.location.takeIf(String::isNotBlank)?.let { "  •  $it" }.orEmpty()}",
                                    onClick = { onAgendaEventClick(event) }
                                )
                            }
                        }

                        IdentityMetricDetail.DELIVERIES, IdentityMetricDetail.EXAMS -> items(
                            items = pendingTasks,
                            key = StudentTask::id
                        ) { task ->
                            val subject = uiState.subjects.firstOrNull { it.id == task.subjectId }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = task.title,
                                    detail = "${task.dueLocalDate().format(DateTimeFormatter.ofPattern("EEE d 'de' MMM", IdentityLocale)).identityCapitalized()}${subject?.name?.let { "  \u2022  $it" }.orEmpty()}",
                                    onClick = { onTaskClick(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun IdentityWeeklyTimeline(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val weekStart = selectedDate.weekStartIdentity()
    val weekdaySessions = sessions.filter { session ->
        (1..5).any { day ->
            val date = weekStart.plusDays((day - 1).toLong())
            session.occursOn(date.toEpochDay(), day)
        }
    }
    val earliest = weekdaySessions.minOfOrNull(ClassSession::startMinute) ?: 6 * 60
    val startHour = (earliest / 60).coerceIn(0, 17)
    val visibleHours = 7
    val endHour = startHour + visibleHours
    val hourHeight = 36.dp
    val axisWidth = 42.dp

    Column {
        Row(Modifier.padding(start = axisWidth)) {
            (1..5).forEach { day ->
                Text(
                    text = identityDayLetter(DayOfWeek.of(day)),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().height(hourHeight * visibleHours)) {
            val dayWidth = (maxWidth - axisWidth) / 5
            Column {
                (startHour until endHour).forEach { hour ->
                    Row(Modifier.height(hourHeight), verticalAlignment = Alignment.Top) {
                        Text(
                            text = formatIdentityMinute(hour * 60, use24Hour),
                            modifier = Modifier.width(axisWidth).offset(y = (-7).dp),
                            color = UniStackColors.TextSecondary,
                            style = MaterialTheme.typography.labelSmall
                        )
                        HorizontalDivider(color = UniStackColors.SoftOutline.copy(alpha = 0.7f))
                    }
                }
            }
            (0..5).forEach { line ->
                Box(
                    Modifier
                        .offset(x = axisWidth + dayWidth * line)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(UniStackColors.SoftOutline.copy(alpha = 0.62f))
                )
            }
            sessions.forEach { session ->
                (1..5).forEach { day ->
                    val date = weekStart.plusDays((day - 1).toLong())
                    if (session.occursOn(date.toEpochDay(), day)) {
                        val visibleStart = session.startMinute.coerceAtLeast(startHour * 60)
                        val visibleEnd = session.endMinute.coerceAtMost(endHour * 60)
                        if (visibleEnd > visibleStart) {
                            val subject = subjects.firstOrNull { it.id == session.subjectId }
                            val y = hourHeight * ((visibleStart - startHour * 60) / 60f)
                            val cardHeight = (hourHeight * ((visibleEnd - visibleStart) / 60f)).coerceAtLeast(42.dp)
                            Column(
                                modifier = Modifier
                                    .offset(x = axisWidth + dayWidth * (day - 1) + 3.dp, y = y)
                                    .width(dayWidth - 6.dp)
                                    .height(cardHeight)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(subject.identityColor())
                                    .clickable { onSessionClick(date, session) }
                                    .padding(5.dp),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = "${formatIdentityMinute(session.startMinute, use24Hour)}\n${formatIdentityMinute(session.endMinute, use24Hour)}",
                                    color = UniStackColors.OnPrimary,
                                    fontSize = 8.sp,
                                    lineHeight = 9.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = subject?.name ?: "Clase",
                                    color = UniStackColors.OnPrimary,
                                    fontSize = 8.sp,
                                    lineHeight = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = session.identityPlace().room.ifBlank { "Sin aula" },
                                    color = UniStackColors.OnPrimary.copy(alpha = 0.9f),
                                    fontSize = 8.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NextClassPanel(
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val next = remember(sessions) { findUpcomingClass(LocalDate.now(), sessions) }
    IdentitySurface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumCard,
        onClick = next?.let { { onSessionClick(it.first, it.second) } }
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(32.dp).clip(CircleShape).background(IdentityAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Schedule, contentDescription = null, tint = IdentityAccent, modifier = Modifier.size(19.dp))
            }
            Spacer(Modifier.width(11.dp))
            if (next == null) {
                Column(Modifier.weight(1f)) {
                    Text(
                        "Próxima clase",
                        color = IdentityAccent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "No hay clases programadas",
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            } else {
                val session = next.second
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        "Próxima clase",
                        color = IdentityAccent,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(7.dp).clip(CircleShape).background(subject.identityColor()))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            subject?.name ?: "Clase",
                            color = UniStackColors.TextPrimary,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "${formatIdentityMinute(session.startMinute, use24Hour)} - ${formatIdentityMinute(session.endMinute, use24Hour)}  •  ${session.identityPlace().room.ifBlank { "Sin aula" }}",
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Abrir clase",
                    tint = UniStackColors.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun IdentityMonthCalendar(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    tasks: List<StudentTask>,
    subjects: List<Subject>,
    agendaEvents: List<AgendaEvent>,
    onDateSelected: (LocalDate) -> Unit
) {
    val month = YearMonth.from(selectedDate)
    val firstCell = month.atDay(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val leadingDays = month.atDay(1).dayOfWeek.value - 1
    val cellCount = ((leadingDays + month.lengthOfMonth() + 6) / 7) * 7

    IdentitySurface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumCard
    ) {
        Column {
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onDateSelected(selectedDate.minusMonths(1).withDayOfMonth(1)) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = UniStackColors.TextPrimary)
                }
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", IdentityLocale)).identityCapitalized(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDateSelected(selectedDate.plusMonths(1).withDayOfMonth(1)) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = UniStackColors.TextPrimary)
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                listOf("LUN", "MAR", "MIÉ", "JUE", "VIE", "SÁB", "DOM").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            repeat(cellCount / 7) { row ->
                Row(Modifier.fillMaxWidth()) {
                    repeat(7) { column ->
                        val date = firstCell.plusDays((row * 7 + column).toLong())
                        val daySessions = sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                        val dayTasks = tasks.filter { !it.completed && it.dueLocalDate() == date }
                        val dayAgendaEvents = agendaEvents.filter { it.occursOn(date) }
                        IdentityMonthCell(
                            modifier = Modifier.weight(1f),
                            date = date,
                            inMonth = YearMonth.from(date) == month,
                            selected = date == selectedDate,
                            colors = daySessions.map { session -> subjects.firstOrNull { it.id == session.subjectId }.identityColor() } +
                                dayTasks.map { IdentityAccent } + dayAgendaEvents.map(AgendaEvent::identityColor),
                            onClick = { onDateSelected(date) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IdentityMonthCell(
    modifier: Modifier,
    date: LocalDate,
    inMonth: Boolean,
    selected: Boolean,
    colors: List<Color>,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .height(47.dp)
            .border(0.5.dp, UniStackColors.SoftOutline.copy(alpha = 0.62f))
            .padding(3.dp)
            .clip(AppShapes.SmallCard)
            .background(if (selected) IdentityAccent else Color.Transparent)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = when {
                selected -> UniStackColors.OnPrimary
                inMonth -> UniStackColors.TextPrimary
                else -> UniStackColors.TextSecondary.copy(alpha = 0.45f)
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        if (colors.isNotEmpty()) {
            Spacer(Modifier.height(5.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                colors.take(3).forEach { color ->
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (selected) UniStackColors.OnPrimary.copy(alpha = 0.78f) else color)
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectedDayPanel(
    date: LocalDate,
    sessions: List<ClassSession>,
    tasks: List<StudentTask>,
    subjects: List<Subject>,
    agendaEvents: List<AgendaEvent>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit,
    onTaskClick: (String) -> Unit,
    onAgendaEventClick: (AgendaEvent) -> Unit
) {
    val daySessions = sessions
        .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
        .sortedBy(ClassSession::startMinute)
    val dayTasks = tasks.filter { !it.completed && it.dueLocalDate() == date }.sortedBy(StudentTask::dueDateMillis)
    val dayAgendaEvents = agendaEvents.filter { it.occursOn(date) }.sortedBy(AgendaEvent::startMillis)

    IdentitySurface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.MediumCard
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(
                text = date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", IdentityLocale)).identityCapitalized(),
                color = IdentityAccent,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            if (daySessions.isEmpty() && dayTasks.isEmpty() && dayAgendaEvents.isEmpty()) {
                Text(
                    "No hay eventos para este día",
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            dayAgendaEvents.take(2).forEach { event ->
                IdentityEventRow(
                    color = event.identityColor(),
                    title = event.title,
                    detail = "${event.identityTimeText(use24Hour)}${event.location.takeIf(String::isNotBlank)?.let { "  •  $it" }.orEmpty()}",
                    onClick = { onAgendaEventClick(event) }
                )
            }
            val remainingAfterEvents = (2 - dayAgendaEvents.size).coerceAtLeast(0)
            daySessions.take(remainingAfterEvents).forEach { session ->
                val subject = subjects.firstOrNull { it.id == session.subjectId }
                IdentityEventRow(
                    color = subject.identityColor(),
                    title = subject?.name ?: "Clase",
                    detail = "${formatIdentityMinute(session.startMinute, use24Hour)} - ${formatIdentityMinute(session.endMinute, use24Hour)}  •  ${session.identityPlace().room.ifBlank { "Sin aula" }}",
                    onClick = { onSessionClick(date, session) }
                )
            }
            val remainingAfterClasses = (remainingAfterEvents - daySessions.size).coerceAtLeast(0)
            dayTasks.take(remainingAfterClasses).forEach { task ->
                val subject = subjects.firstOrNull { it.id == task.subjectId }
                IdentityEventRow(
                    color = subject.identityColor(),
                    title = task.title,
                    detail = "${if (task.type == TaskType.EXAM || task.type == TaskType.TEST) "Examen" else "Entrega"}${subject?.name?.let { "  •  $it" }.orEmpty()}",
                    onClick = { onTaskClick(task.id) }
                )
            }
            val hidden = dayAgendaEvents.size + daySessions.size + dayTasks.size - 2
            if (hidden > 0) {
                Text(
                    "+$hidden eventos más",
                    color = IdentityAccent,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun IdentityEventRow(
    color: Color,
    title: String,
    detail: String,
    onClick: () -> Unit,
    titleMaxLines: Int = 2,
    detailMaxLines: Int = 2,
    contentPadding: PaddingValues = PaddingValues(horizontal = 10.dp, vertical = 8.dp)
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.SmallCard)
            .clickable(onClick = onClick)
            .padding(contentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                title,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                detail,
                color = UniStackColors.TextSecondary,
                style = MaterialTheme.typography.bodySmall,
                maxLines = detailMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Abrir evento",
            tint = UniStackColors.TextSecondary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun IdentityPrimaryButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(LocalInterfaceSpacing.current.controlHeight),
        shape = AppShapes.MediumCard,
        colors = ButtonDefaults.buttonColors(
            containerColor = IdentityAccent,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(21.dp))
        Spacer(Modifier.width(9.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.ExtraBold)
    }
}

private data class IdentityPlace(val room: String, val professor: String)

private fun ClassSession.identityPlace(): IdentityPlace {
    val parts = location.split('•', limit = 2).map(String::trim)
    return IdentityPlace(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
}

private fun Subject?.identityColor(): Color = this?.customColor?.let(::Color) ?: this?.let(::subjectAccent) ?: IdentityAccent

private fun AgendaEvent.identityColor(): Color = colorArgb?.let(::Color) ?: when (kind) {
    AgendaEventKind.PERSONAL -> UniStackColors.Teal
    AgendaEventKind.MEETING -> UniStackColors.Blue
    AgendaEventKind.REMINDER -> UniStackColors.Yellow
    AgendaEventKind.CUSTOM -> IdentityAccent
}

private fun AgendaEvent.identityTimeText(use24Hour: Boolean): String {
    if (allDay) return "Todo el día"
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(startMillis).atZone(zone).toLocalTime()
    val startText = formatIdentityMinute(start.hour * 60 + start.minute, use24Hour)
    val endText = endMillis?.let {
        val end = Instant.ofEpochMilli(it).atZone(zone).toLocalTime()
        formatIdentityMinute(end.hour * 60 + end.minute, use24Hour)
    }
    return if (endText == null) startText else "$startText - $endText"
}

private fun LocalDate.weekStartIdentity(): LocalDate = minusDays((dayOfWeek.value - 1).toLong())

private fun Long.dueLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate()

private fun StudentTask.dueLocalDate(): LocalDate = dueDateMillis.dueLocalDate()

private fun String.identityCapitalized(): String = replaceFirstChar {
    if (it.isLowerCase()) it.titlecase(IdentityLocale) else it.toString()
}

private fun identityDayLetter(day: DayOfWeek): String = listOf("L", "M", "X", "J", "V", "S", "D")[day.value - 1]

private fun formatIdentityMinute(value: Int, use24Hour: Boolean): String {
    val hour = value / 60
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) "a. m." else "p. m.")
}

private fun nextOccurrenceDate(fromDate: LocalDate, session: ClassSession): LocalDate? =
    (0L..84L).asSequence()
        .map(fromDate::plusDays)
        .firstOrNull { date -> session.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
private fun findUpcomingClass(
    fromDate: LocalDate,
    sessions: List<ClassSession>
): Pair<LocalDate, ClassSession>? {
    if (sessions.isEmpty()) return null
    val now = LocalTime.now()
    val nowMinute = now.hour * 60 + now.minute
    return (0L..20L).asSequence().mapNotNull { offset ->
        val date = fromDate.plusDays(offset)
        sessions
            .filter { session ->
                session.occursOn(date.toEpochDay(), date.dayOfWeek.value) &&
                    (offset > 0L || session.startMinute >= nowMinute)
            }
            .minByOrNull(ClassSession::startMinute)
            ?.let { date to it }
    }.firstOrNull()
}
