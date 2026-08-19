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
import androidx.compose.material.icons.rounded.Today
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
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
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

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.runtime.ReadOnlyComposable
internal enum class IdentityScheduleView {
    TIMETABLE,
    CALENDAR
}

private enum class IdentityMetricDetail {
    SUBJECTS,
    TODAY,
    WEEK,
    EVENTS,
    DELIVERIES,
    EXAMS
}

private val IdentityAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.primary
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
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
        // Se suma lo que tape la barra flotante, que se dibuja encima del contenido. Con
        // la barra acoplada el valor es cero y esto queda igual que antes.
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
                        selectedDate = selectedDate,
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        onSubjectsClick = { metricDetail = IdentityMetricDetail.SUBJECTS },
                        onTodayClick = { metricDetail = IdentityMetricDetail.TODAY },
                        onWeekClick = { metricDetail = IdentityMetricDetail.WEEK }
                    )
                }
                // La próxima clase va antes de la rejilla y el acceso al horario completo
                // después: lo primero es lo que se viene a mirar, y lo segundo es una salida
                // hacia otra pantalla, que se ofrece cuando ya has visto la semana.
                item {
                    NextClassPanel(
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        use24Hour = uiState.accessibility.use24HourTime,
                        onSessionClick = onSessionClick
                    )
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
                    FullScheduleLaunchCard(onClick = onOpenFullSchedule)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Consulta todas las horas y los 7 días",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = "Abrir horario completo",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun IdentitySurface(
    modifier: Modifier = Modifier,
    shape: Shape = AppShapes.MediumCard,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
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
        BorderStroke(0.7.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
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
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = if (view == IdentityScheduleView.TIMETABLE) {
                "Clases y bloques de tu semana en un mismo lugar."
            } else {
                "Fechas, eventos y entregas de tu mes."
            },
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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

/**
 * Las tres cifras de la cabecera del horario.
 *
 * Ninguna repite lo que ya hay debajo. Aquí estuvieron la hora de la próxima clase y su aula,
 * que es exactamente lo que dice el panel de «Próxima clase» unos centímetros más abajo, y con
 * más detalle: sobraban. En su sitio van dos cosas que la pantalla no cuenta en ninguna parte
 * —cuántas clases hay hoy y cuánta clase tiene la semana—, que es lo que se mira de un vistazo
 * antes de ponerse a leer la rejilla.
 */
@Composable
private fun TimetableMetrics(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    onSubjectsClick: () -> Unit,
    onTodayClick: () -> Unit,
    onWeekClick: () -> Unit
) {
    val subjectCount = subjects.count { subject -> sessions.any { it.subjectId == subject.id } }
    val today = LocalDate.now()
    val todayCount = remember(sessions, today) {
        sessions.count { it.occursOn(today.toEpochDay(), today.dayOfWeek.value) }
    }
    // Se recorre la semana día a día en vez de sumar cada clase por sus días marcados: así
    // una materia quincenal cuenta solo en la semana en que toca, que es lo que enseña la
    // rejilla de abajo.
    val weekMinutes = remember(sessions, selectedDate) {
        val weekStart = selectedDate.weekStartIdentity()
        (0L..6L).sumOf { offset ->
            val date = weekStart.plusDays(offset)
            sessions
                .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                .sumOf { it.endMinute - it.startMinute }
        }
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
        // Etiquetas de una palabra: en tres columnas, «Clases hoy» y «Esta semana» salían
        // cortadas con puntos suspensivos. Lo que no cabe aquí lo cuenta el detalle.
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Today,
            iconColor = IdentityAccent,
            value = todayCount.toString(),
            label = "Hoy",
            onClick = onTodayClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Schedule,
            iconColor = IdentityAccent,
            value = weeklyHoursLabel(weekMinutes),
            label = "Semana",
            onClick = onWeekClick
        )
    }
}

/** Las horas de clase de la semana, con media hora de resolución: «18 h», «17,5 h». */
private fun weeklyHoursLabel(minutes: Int): String {
    if (minutes == 0) return "0 h"
    val hours = minutes / 60f
    return if (minutes % 60 == 0) {
        "${minutes / 60} h"
    } else {
        String.format(IdentityLocale, "%.1f h", hours)
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
    val today = LocalDate.now()
    val todaySessions = remember(uiState.sessions, today) {
        uiState.sessions
            .filter { it.occursOn(today.toEpochDay(), today.dayOfWeek.value) }
            .sortedBy(ClassSession::startMinute)
    }
    // Los siete días con lo que tiene cada uno, para que la cifra de la tarjeta se pueda
    // desglosar: «24 h» no dice si son cuatro días de seis o seis de cuatro.
    val weekDays = remember(uiState.sessions, selectedDate) {
        val weekStart = selectedDate.weekStartIdentity()
        (0L..6L).map { offset ->
            val date = weekStart.plusDays(offset)
            date to uiState.sessions
                .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                .sortedBy(ClassSession::startMinute)
        }
    }
    val weekMinutes = weekDays.sumOf { (_, daySessions) ->
        daySessions.sumOf { it.endMinute - it.startMinute }
    }

    val title = when (detail) {
        IdentityMetricDetail.SUBJECTS -> "Materias del horario"
        IdentityMetricDetail.TODAY -> "Clases de hoy"
        IdentityMetricDetail.WEEK -> "Horas de clase"
        IdentityMetricDetail.EVENTS -> "Eventos de ${month.format(DateTimeFormatter.ofPattern("MMMM", IdentityLocale)).identityCapitalized()}"
        IdentityMetricDetail.DELIVERIES -> "Entregas pendientes"
        IdentityMetricDetail.EXAMS -> "Exámenes pendientes"
    }
    val icon = when (detail) {
        IdentityMetricDetail.SUBJECTS -> Icons.AutoMirrored.Rounded.MenuBook
        IdentityMetricDetail.TODAY -> Icons.Rounded.Today
        IdentityMetricDetail.WEEK -> Icons.Rounded.Schedule
        IdentityMetricDetail.EVENTS -> Icons.Rounded.CalendarMonth
        IdentityMetricDetail.DELIVERIES -> Icons.AutoMirrored.Rounded.Assignment
        IdentityMetricDetail.EXAMS -> Icons.Rounded.School
    }
    val count = when (detail) {
        IdentityMetricDetail.SUBJECTS -> groupedSubjects.size
        IdentityMetricDetail.TODAY -> todaySessions.size
        IdentityMetricDetail.WEEK -> weekDays.count { (_, daySessions) -> daySessions.isNotEmpty() }
        IdentityMetricDetail.EVENTS -> monthAgendaEvents.size
        IdentityMetricDetail.DELIVERIES, IdentityMetricDetail.EXAMS -> pendingTasks.size
    }

    /*
     * Cada detalle dice lo suyo debajo del título.
     *
     * «N elementos» valía mientras todos fueran listas de cosas; para las horas de la semana
     * no dice nada, y es justo el dato que la tarjeta no puede enseñar entero.
     */
    val subtitle = when (detail) {
        IdentityMetricDetail.TODAY ->
            today.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", IdentityLocale)).identityCapitalized()
        IdentityMetricDetail.WEEK -> {
            val weekStart = selectedDate.weekStartIdentity()
            val range = "${weekStart.format(DateTimeFormatter.ofPattern("d MMM", IdentityLocale))} - " +
                weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM", IdentityLocale))
            "${weeklyHoursLabel(weekMinutes)} en total  •  $range"
        }
        else -> "$count ${if (count == 1) "elemento" else "elementos"}"
    }
    val emptyMessage = when (detail) {
        IdentityMetricDetail.TODAY -> "Hoy no tienes clases."
        IdentityMetricDetail.WEEK -> "Esta semana no tienes clases."
        else -> "No hay información para mostrar."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
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

            if (count == 0) {
                IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                    Text(
                        emptyMessage,
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

                        IdentityMetricDetail.TODAY -> items(
                            items = todaySessions,
                            key = ClassSession::id
                        ) { session ->
                            val subject = uiState.subjects.firstOrNull { it.id == session.subjectId }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = subject?.name ?: "Clase",
                                    detail = "${formatIdentityMinute(session.startMinute, uiState.accessibility.use24HourTime)} - ${formatIdentityMinute(session.endMinute, uiState.accessibility.use24HourTime)}  •  ${session.identityPlace().room.ifBlank { "Sin aula" }}",
                                    onClick = { onSessionClick(today, session) }
                                )
                            }
                        }

                        IdentityMetricDetail.WEEK -> items(
                            items = weekDays.filter { (_, daySessions) -> daySessions.isNotEmpty() },
                            key = { (date, _) -> date.toEpochDay() }
                        ) { (date, daySessions) ->
                            val dayMinutes = daySessions.sumOf { it.endMinute - it.startMinute }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = AppShapes.MediumCard) {
                                IdentityEventRow(
                                    color = IdentityAccent,
                                    title = date.format(DateTimeFormatter.ofPattern("EEEE d", IdentityLocale)).identityCapitalized(),
                                    detail = "${weeklyHoursLabel(dayMinutes)}  •  ${daySessions.size} ${if (daySessions.size == 1) "clase" else "clases"}  •  ${formatIdentityMinute(daySessions.first().startMinute, uiState.accessibility.use24HourTime)} - ${formatIdentityMinute(daySessions.maxOf { it.endMinute }, uiState.accessibility.use24HourTime)}",
                                    onClick = { onSessionClick(date, daySessions.first()) }
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

    // La rejilla se queda en L-V mientras no haya nada el fin de semana, y se estira a los
    // siete días en cuanto lo hay. Antes el rango era (1..5) fijo, así que una clase de
    // sábado —que el selector de días deja crear sin problema— no aparecía por ninguna
    // parte: quedaba guardada y era invisible.
    val hasWeekendSession = sessions.any { session ->
        (6..7).any { day ->
            val date = weekStart.plusDays((day - 1).toLong())
            session.occursOn(date.toEpochDay(), day)
        }
    }
    val visibleDays = if (hasWeekendSession) 1..7 else 1..5

    val weekdaySessions = sessions.filter { session ->
        visibleDays.any { day ->
            val date = weekStart.plusDays((day - 1).toLong())
            session.occursOn(date.toEpochDay(), day)
        }
    }
    // La rejilla enseña las horas que tienen clase y pliega los huecos, en vez de una ventana
    // de siete horas anclada a la más temprana: con eso, una clase suelta a la 1:00 escondía
    // todo lo demás y un día repartido no cabía. El detalle, en TimelineRows.kt.
    val timelineRows = remember(weekdaySessions) {
        buildTimelineRows(weekdaySessions.map { it.startMinute..it.endMinute })
    }
    val hourHeight = 36.dp
    val breakHeight = 16.dp
    val axisWidth = 42.dp
    val gridHeight = timelineHeight(timelineRows, hourHeight.value, breakHeight.value).dp

    Column {
        Row(Modifier.padding(start = axisWidth)) {
            visibleDays.forEach { day ->
                Text(
                    text = identityDayLetter(DayOfWeek.of(day)),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().height(gridHeight)) {
            // El ancho de columna y las líneas se derivan de visibleDays, no de un 5 fijo.
            // La cabecera reparte con weight(1f) entre los días visibles, así que en cuanto
            // la rejilla se estira a siete la aritmética tiene que estirarse con ella o los
            // bloques se dibujan con el paso de cinco columnas y se salen por la derecha.
            val dayCount = visibleDays.count()
            val dayWidth = (maxWidth - axisWidth) / dayCount
            Column {
                timelineRows.forEach { row ->
                    when (row) {
                        is TimelineRow.Hour -> Row(
                            Modifier.height(hourHeight),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = formatIdentityMinute(row.hour * 60, use24Hour),
                                modifier = Modifier.width(axisWidth).offset(y = (-7).dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                        }

                        // El corte se ve: si no, dos bloques separados por horas parecerían
                        // seguidos y la rejilla estaría mintiendo sobre el tiempo.
                        is TimelineRow.Break -> Row(
                            Modifier.height(breakHeight).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "\u22ee",
                                modifier = Modifier.width(axisWidth),
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                        }
                    }
                }
            }
            (0..dayCount).forEach { line ->
                Box(
                    Modifier
                        .offset(x = axisWidth + dayWidth * line)
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
                )
            }
            sessions.forEach { session ->
                visibleDays.forEach { day ->
                    val date = weekStart.plusDays((day - 1).toLong())
                    if (session.occursOn(date.toEpochDay(), day)) {
                        val top = offsetForMinute(
                            timelineRows, session.startMinute, hourHeight.value, breakHeight.value
                        )
                        val bottom = offsetForMinute(
                            timelineRows, session.endMinute, hourHeight.value, breakHeight.value
                        )
                        if (top != null && bottom != null && bottom > top) {
                            val subject = subjects.firstOrNull { it.id == session.subjectId }
                            val y = top.dp
                            // 26dp es una clase de media hora con esta escala: por debajo el
                            // nombre no cabe ni en una línea.
                            val cardHeight = (bottom - top).dp.coerceAtLeast(26.dp)
                            ClassBlock(
                                modifier = Modifier
                                    .offset(x = axisWidth + dayWidth * (day - visibleDays.first) + 3.dp, y = y)
                                    .width(dayWidth - 6.dp)
                                    .height(cardHeight),
                                height = cardHeight,
                                color = subject.identityColor(),
                                name = subject?.name ?: "Clase",
                                room = session.identityPlace().room,
                                startLabel = formatIdentityMinute(session.startMinute, use24Hour),
                                onClick = { onSessionClick(date, session) }
                            )
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        "${formatIdentityMinute(session.startMinute, use24Hour)} - ${formatIdentityMinute(session.endMinute, use24Hour)}  •  ${session.identityPlace().room.ifBlank { "Sin aula" }}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Abrir clase",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = MaterialTheme.colorScheme.onSurface)
                }
                Text(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", IdentityLocale)).identityCapitalized(),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { onDateSelected(selectedDate.plusMonths(1).withDayOfMonth(1)) }) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Mes siguiente", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
            Row(Modifier.fillMaxWidth().padding(bottom = 4.dp)) {
                DayLabels.medium.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                dayTasks.map { IdentityAccent } + dayAgendaEvents.map { event -> event.identityColor() },
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
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
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
                selected -> MaterialTheme.colorScheme.onPrimary
                inMonth -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
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
                            .background(if (selected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.78f) else color)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = titleMaxLines,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = detailMaxLines,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = "Abrir evento",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun IdentityPrimaryButton(label: String, onClick: () -> Unit) {
    SquishyButton(
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

@Composable
@ReadOnlyComposable
private fun Subject?.identityColor(): Color = scheduleBlockColor(IdentityAccent)

@Composable
@ReadOnlyComposable
private fun AgendaEvent.identityColor(): Color = colorArgb?.let(::Color) ?: when (kind) {
    AgendaEventKind.PERSONAL -> MaterialTheme.colorScheme.tertiary
    AgendaEventKind.MEETING -> LocalSectionColors.current.schedule
    AgendaEventKind.REMINDER -> LocalSectionColors.current.atRisk
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

private fun identityDayLetter(day: DayOfWeek): String = DayLabels.short[day.value - 1]

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
