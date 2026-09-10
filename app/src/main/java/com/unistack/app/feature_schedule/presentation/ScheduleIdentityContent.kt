@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.utils.DayLabels
import com.unistack.app.core.utils.mediumDesde
import com.unistack.app.core.utils.huecosAntesDelUno
import com.unistack.app.core.design.theme.LocalAppearancePreferences

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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import com.unistack.app.core.design.components.PildoraEnCurso
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.claseEnCurso
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskType
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
private val IdentityLocale: Locale get() = Locale.getDefault()

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
    /** Cuántas clases pasadas están sin marcar, en todas las materias. */
    pendingCount: Int = 0,
    onCatchUp: () -> Unit = {},
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

        /*
         * Lo que se te paso, avisado donde vas a estar.
         *
         * Ponerse al dia vivia solo dentro del historial de una materia, asi que para
         * enterarte de que llevabas cinco clases sin marcar tenias que entrar a mirar. Aqui
         * cuenta las de todas y esta en la primera pantalla del modulo.
         *
         * Sale mientras quede una sola sin responder. Antes pedia dos, y con dos pendientes
         * marcar una hacia desaparecer el aviso con la otra todavia sin marcar: el recado se
         * daba por dado a mitad de camino. Una lista de una fila es poca cosa, pero menos aun
         * es enterarte de que te faltaba una cuando ya no hay forma de acordarse.
         */
        if (pendingCount > 0) {
            item {
                CatchUpBanner(count = pendingCount, onClick = onCatchUp)
            }
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
                        onDateSelected = onDateSelected,
                        onSessionClick = onSessionClick
                    )
                }
                item {
                    WeekDayClassList(
                        selectedDate = selectedDate,
                        sessions = uiState.sessions,
                        subjects = uiState.subjects,
                        occurrences = uiState.occurrences,
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
        shape = MaterialTheme.shapes.medium,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Fullscreen,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "Horario completo",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Las 24 horas, los 7 días, semana a semana",
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
    shape: Shape = MaterialTheme.shapes.large,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val resolvedColor = color
    val border: BorderStroke? = null
    val shadowElevation = 0.dp

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
    SectionHeader(
        title = if (view == IdentityScheduleView.TIMETABLE) "Horario" else "Calendario",
        subtitle = if (view == IdentityScheduleView.TIMETABLE) {
            "Clases y bloques de tu semana en un mismo lugar."
        } else {
            "Fechas, eventos y entregas de tu mes."
        }
    )
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
            iconColor = LocalSectionColors.current.schedule,
            value = subjectCount.toString(),
            label = if (subjectCount == 1) "Materia" else "Materias",
            onClick = onSubjectsClick
        )
        // Etiquetas de una palabra: en tres columnas, «Clases hoy» y «Esta semana» salían
        // cortadas con puntos suspensivos. Lo que no cabe aquí lo cuenta el detalle.
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Today,
            iconColor = MaterialTheme.colorScheme.tertiary,
            value = todayCount.toString(),
            label = stringResource(R.string.schedule_identity_today),
            onClick = onTodayClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Schedule,
            iconColor = LocalSectionColors.current.onTrack,
            value = weeklyHoursLabel(weekMinutes),
            label = stringResource(R.string.schedule_identity_week),
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
            iconColor = LocalSectionColors.current.schedule,
            value = eventCount.toString(),
            label = stringResource(R.string.schedule_identity_events),
            onClick = onEventsClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Rounded.Assignment,
            iconColor = MaterialTheme.colorScheme.tertiary,
            value = deliveries.toString(),
            label = stringResource(R.string.schedule_identity_due_dates),
            onClick = onDeliveriesClick
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.School,
            iconColor = LocalSectionColors.current.atRisk,
            value = exams.toString(),
            label = stringResource(R.string.schedule_identity_exams),
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
        IdentityMetricDetail.SUBJECTS -> stringResource(R.string.schedule_identity_subjects_in_schedule)
        IdentityMetricDetail.TODAY -> "Clases de hoy"
        IdentityMetricDetail.WEEK -> "Horas de clase"
        IdentityMetricDetail.EVENTS -> stringResource(R.string.schedule_identity_events_of_month, month.format(DateTimeFormatter.ofPattern("MMMM", IdentityLocale)).identityCapitalized())
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
            today.format(DateTimeFormatter.ofPattern(if (IdentityLocale.language == "en") "EEEE, MMMM d" else "EEEE, d 'de' MMMM", IdentityLocale)).identityCapitalized()
        IdentityMetricDetail.WEEK -> {
            val weekStart = selectedDate.weekStartIdentity()
            val range = "${weekStart.format(DateTimeFormatter.ofPattern("d MMM", IdentityLocale))} - " +
                weekStart.plusDays(6).format(DateTimeFormatter.ofPattern("d MMM", IdentityLocale))
            "${weeklyHoursLabel(weekMinutes)} en total  •  $range"
        }
        else -> "$count ${if (count == 1) "elemento" else "elementos"}"
    }
    val emptyMessage = when (detail) {
        IdentityMetricDetail.TODAY -> stringResource(R.string.schedule_identity_no_classes_today)
        IdentityMetricDetail.WEEK -> stringResource(R.string.schedule_identity_no_classes_this_week)
        else -> stringResource(R.string.schedule_identity_no_info)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = MaterialTheme.shapes.extraLarge
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
                    Modifier.size(42.dp).clip(MaterialTheme.shapes.medium).background(IdentityAccent.copy(alpha = 0.13f)),
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
                IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
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
                            IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = subject?.name ?: "Materia",
                                    detail = "$days  \u2022  ${formatIdentityMinute(session.startMinute, uiState.accessibility.use24HourTime)} - ${formatIdentityMinute(session.endMinute, uiState.accessibility.use24HourTime)}  \u2022  ${session.identityPlace().room.ifBlank { stringResource(R.string.schedule_detail_no_room) }}",
                                    onClick = { onSessionClick(occurrenceDate, session) }
                                )
                            }
                        }

                        IdentityMetricDetail.TODAY -> items(
                            items = todaySessions,
                            key = ClassSession::id
                        ) { session ->
                            val subject = uiState.subjects.firstOrNull { it.id == session.subjectId }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = subject?.name ?: "Clase",
                                    detail = "${formatIdentityMinute(session.startMinute, uiState.accessibility.use24HourTime)} - ${formatIdentityMinute(session.endMinute, uiState.accessibility.use24HourTime)}  •  ${session.identityPlace().room.ifBlank { stringResource(R.string.schedule_detail_no_room) }}",
                                    onClick = { onSessionClick(today, session) }
                                )
                            }
                        }

                        IdentityMetricDetail.WEEK -> items(
                            items = weekDays.filter { (_, daySessions) -> daySessions.isNotEmpty() },
                            key = { (date, _) -> date.toEpochDay() }
                        ) { (date, daySessions) ->
                            val dayMinutes = daySessions.sumOf { it.endMinute - it.startMinute }
                            IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
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
                            IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
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
                            IdentitySurface(Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.large) {
                                IdentityEventRow(
                                    color = subject.identityColor(),
                                    title = task.title,
                                    detail = "${task.dueLocalDate().format(DateTimeFormatter.ofPattern(if (IdentityLocale.language == "en") "EEE, MMM d" else "EEE d 'de' MMM", IdentityLocale)).identityCapitalized()}${subject?.name?.let { "  \u2022  $it" }.orEmpty()}",
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
/**
 * La semana entera de un vistazo, y con un día elegido dentro.
 *
 * La rejilla no era más que un dibujo: se veía la semana y ahí acababa. Ahora cada columna es
 * un objetivo táctil que elige el día, la columna elegida se pinta y debajo aparece la lista de
 * esas clases con su nombre y su aula completos, que es lo que la rejilla no puede dar —a esta
 * escala, un bloque de media hora no tiene sitio ni para una línea de texto.
 */
@Composable
private fun IdentityWeeklyTimeline(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onDateSelected: (LocalDate) -> Unit,
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

    // Las mismas horas que cuenta la métrica «Semana», para que las dos cifras no se
    // contradigan cuando el fin de semana está fuera de la rejilla.
    val weekMinutes = remember(sessions, weekStart) {
        (0L..6L).sumOf { offset ->
            val date = weekStart.plusDays(offset)
            sessions.filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                .sumOf { it.endMinute - it.startMinute }
        }
    }

    Column {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(
                text = weekRangeLabel(weekStart, visibleDays.last),
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = weeklyHoursLabel(weekMinutes) + " de clase",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(10.dp))
        // El día elegido se marca aquí arriba y solo aquí. Estuvo tintada la columna entera
        // de la rejilla y no se leía como una selección: parecía una sombra suelta detrás de
        // una clase. Una píldora debajo de la letra dice lo mismo sin ambigüedad, y deja al
        // anillo la otra pregunta, que es cuál de los días es hoy.
        val today = LocalDate.now()
        Row(Modifier.padding(start = axisWidth)) {
            visibleDays.forEach { day ->
                val date = weekStart.plusDays((day - 1).toLong())
                val picked = date == selectedDate
                val isToday = date == today
                Box(
                    Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clickable { onDateSelected(date) },
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (picked) IdentityAccent else Color.Transparent)
                            .then(
                                if (isToday && !picked) {
                                    Modifier.border(1.5.dp, IdentityAccent, CircleShape)
                                } else {
                                    Modifier
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = identityDayLetter(DayOfWeek.of(day)),
                            textAlign = TextAlign.Center,
                            color = when {
                                picked -> MaterialTheme.colorScheme.onPrimary
                                isToday -> IdentityAccent
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
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

            // Las columnas van debajo de todo y no pintan nada: solo recogen el toque en los
            // huecos. Ni las líneas de hora ni el texto consumen el puntero, así que tocar el
            // vacío de una columna sigue llegando aquí; encima de un bloque manda el bloque,
            // que abre esa clase.
            visibleDays.forEach { day ->
                val date = weekStart.plusDays((day - 1).toLong())
                Box(
                    Modifier
                        .offset(x = axisWidth + dayWidth * (day - visibleDays.first))
                        .width(dayWidth)
                        .fillMaxHeight()
                        .clickable { onDateSelected(date) }
                )
            }
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
                                text = "⋮",
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
                                name = subject?.name ?: stringResource(R.string.schedule_detail_class),
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

/** «18 - 23 de agosto», y con los dos meses cuando la semana los cruza. */
private fun weekRangeLabel(weekStart: LocalDate, lastVisibleDay: Int): String {
    val weekEnd = weekStart.plusDays((lastVisibleDay - 1).toLong())
    val month = DateTimeFormatter.ofPattern("MMMM", IdentityLocale)
    val startMonth = weekStart.format(month)
    val endMonth = weekEnd.format(month)
    return if (weekStart.month == weekEnd.month) {
        "${weekStart.dayOfMonth} - ${weekEnd.dayOfMonth} de $startMonth"
    } else {
        "${weekStart.dayOfMonth} de $startMonth - ${weekEnd.dayOfMonth} de $endMonth"
    }
}

/**
 * Las clases del día elegido, escritas enteras.
 *
 * La rejilla dice cuándo y la lista dice qué: el nombre completo de la materia y el aula, que
 * en un bloque de doce píxeles de alto no caben. Tocar una fila abre esa clase, igual que
 * tocar su bloque arriba.
 */
@Composable
private fun WeekDayClassList(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    occurrences: List<ClassOccurrence>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val daySessions = sessions
        .filter { it.occursOn(selectedDate.toEpochDay(), selectedDate.dayOfWeek.value) }
        .sortedBy(ClassSession::startMinute)
    val dayName = selectedDate.format(DateTimeFormatter.ofPattern("EEEE", IdentityLocale))

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (daySessions.isEmpty()) {
            IdentitySurface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Text(
                    text = stringResource(R.string.schedule_identity_no_classes_on_day, dayName),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        /*
         * **Que clase esta pasando ahora mismo.**
         *
         * La app no lo sabia: el horario ensenaba las clases del dia todas iguales, sin
         * distinguir la que esta ocurriendo de la que fue esta manana o la de esta tarde. Es
         * la mitad que faltaba del ajuste «Clase en curso» de Movimiento, que se guardaba sin
         * tener a que aplicarse.
         *
         * `ahora` se recalcula por minuto —no en cada recomposicion— porque una clase que
         * empieza a las diez tiene que marcarse a las diez, no cuando alguien toque la
         * pantalla.
         */
        val hoy = LocalDate.now()
        var minutoActual by remember { mutableIntStateOf(LocalTime.now().let { it.hour * 60 + it.minute }) }
        LaunchedEffect(Unit) {
            while (true) {
                minutoActual = LocalTime.now().let { it.hour * 60 + it.minute }
                kotlinx.coroutines.delay(30_000)
            }
        }
        val verde = LocalSectionColors.current.onTrack

        daySessions.forEach { session ->
            val subject = subjects.firstOrNull { it.id == session.subjectId }
            val detail = formatIdentityMinute(session.startMinute, use24Hour) + " - " +
                formatIdentityMinute(session.endMinute, use24Hour) + "  •  " +
                session.identityPlace().room.ifBlank { stringResource(R.string.schedule_detail_no_room) }
            val enCurso = selectedDate == hoy &&
                minutoActual >= session.startMinute &&
                minutoActual < session.endMinute
            IdentityOutlinedRow(
                onClick = { onSessionClick(selectedDate, session) },
                modifier = Modifier.claseEnCurso(enCurso, verde),
                bordeColor = if (enCurso) verde else null
            ) {
                Box(
                    Modifier
                        .width(4.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(subject.identityColor())
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = subject?.name ?: "Clase",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = detail,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                /*
                 * **La píldora a la derecha, fuera de la columna del nombre.**
                 *
                 * Estuvo delante del nombre y despues encima, y las dos veces le cambiaba
                 * el ancho al entrar y salir de clase: el titulo se recolocaba solo, a
                 * mitad de hora, sin que nadie hubiera tocado nada. En su propia celda a
                 * la derecha, el nombre mide siempre lo mismo.
                 */
                if (enCurso) {
                    Spacer(Modifier.width(10.dp))
                    PildoraEnCurso()
                }
                /*
                 * Como quedo la clase, en la propia fila.
                 *
                 * Habia que abrir cada una para saber si estaba marcada, asi que enterarse de
                 * que se te habian pasado tres costaba tres toques. Con el estado a la vista,
                 * el dia entero se lee de un vistazo.
                 *
                 * Una clase que todavia no ha llegado no lleva estado: no hay nada que
                 * decir de ella, y «pendiente» significaria lo que no es.
                 */
                val termina = selectedDate.atStartOfDay().plusMinutes(session.endMinute.toLong())
                val estado = occurrences.firstOrNull {
                    it.sessionId == session.id && it.dateEpochDay == selectedDate.toEpochDay()
                }?.status
                // Si aun no ha terminado, solo se dice algo cuando ya hay respuesta.
                val aEnsenar = if (termina.isAfter(LocalDateTime.now())) {
                    estado?.takeIf { it != ClassAttendanceStatus.PENDING }
                } else {
                    estado ?: ClassAttendanceStatus.PENDING
                }
                if (aEnsenar != null) {
                    Spacer(Modifier.width(8.dp))
                    AttendanceDot(aEnsenar)
                }
            }
        }
    }
}

/** El estado de una clase, del tamaño justo para caber en la fila sin robarle sitio. */
@Composable
private fun AttendanceDot(status: ClassAttendanceStatus) {
    val (tono, texto) = when (status) {
        ClassAttendanceStatus.ATTENDED -> LocalSectionColors.current.schedule to "Asistí"
        ClassAttendanceStatus.ABSENT -> MaterialTheme.colorScheme.error to "Falta"
        ClassAttendanceStatus.CANCELLED -> MaterialTheme.colorScheme.tertiary to "Cancelada"
        ClassAttendanceStatus.RESCHEDULED -> MaterialTheme.colorScheme.secondary to "Movida"
        ClassAttendanceStatus.PENDING -> MaterialTheme.colorScheme.onSurfaceVariant to "Sin marcar"
    }
    Surface(shape = CircleShape, color = tono.copy(alpha = 0.16f)) {
        Text(
            text = texto,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            color = tono,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )
    }
}

/** La fila con contorno del diseño: sin relleno, el borde justo y las esquinas de la maqueta. */
@Composable
private fun IdentityOutlinedRow(
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    /** El borde, para que la clase en curso pueda marcarse en verde sin tocar el resto. */
    bordeColor: Color? = null,
    content: @Composable RowScope.() -> Unit
) {
    val shape = MaterialTheme.shapes.medium
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, bordeColor ?: MaterialTheme.colorScheme.outlineVariant, shape)
            .then(modifier)
            .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/**
 * Lo que se viene, con el peso visual que le corresponde.
 *
 * Es la única tarjeta rellena de la pantalla, y va con el color del horario, no con el acento
 * general: en una lista de rectángulos claros, el bloque de color es lo que el ojo encuentra
 * primero, y esto es justo lo que se viene a mirar cuando se abre la pestaña.
 */
@Composable
private fun NextClassPanel(
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val next = remember(sessions) { findUpcomingClass(LocalDate.now(), sessions) }
    val section = LocalSectionColors.current

    if (next == null) {
        IdentitySurface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "PRÓXIMA CLASE",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = SectionLabelStyle
                    )
                    Text(
                        "No hay clases programadas",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    val date = next.first
    val session = next.second
    val subject = subjects.firstOrNull { it.id == session.subjectId }
    IdentitySurface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = section.scheduleContainer,
        onClick = { onSessionClick(date, session) }
    ) {
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(section.schedule),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Schedule,
                    contentDescription = null,
                    tint = section.scheduleContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    "PRÓXIMA CLASE",
                    color = section.onScheduleContainer,
                    style = SectionLabelStyle
                )
                Text(
                    subject?.name ?: "Clase",
                    color = section.onScheduleContainer,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nextClassDetail(date, session, use24Hour),
                    color = section.onScheduleContainer.copy(alpha = 0.88f),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(12.dp))
            // La pastilla no lleva su propio clic: está dentro del área de la tarjeta, que ya
            // abre la clase. Dos objetivos superpuestos solo servirían para que el lector de
            // pantalla anunciara la misma acción dos veces.
            Box(
                Modifier
                    .heightIn(min = 34.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(section.schedule)
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Abrir",
                    color = section.scheduleContainer,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * «En 40 min · 10:00 - 11:40 · Aula 302»: cuánto falta, a qué hora y dónde, en una línea.
 *
 * Lo que falta va delante porque es lo que decide si hay que moverse ya. La hora exacta sigue
 * ahí para quien la quiera; lo que no estaba antes es el «en cuánto», que es la pregunta que
 * uno se hace de verdad al mirar esta tarjeta.
 */
private fun nextClassDetail(
    date: LocalDate,
    session: ClassSession,
    use24Hour: Boolean
): String {
    val time = "${formatIdentityMinute(session.startMinute, use24Hour)} - " +
        formatIdentityMinute(session.endMinute, use24Hour)
    // El aula solo si la hay. «Sin aula» ocupaba el mismo sitio que un aula de verdad y
    // empujaba la línea hasta los puntos suspensivos para no decir nada.
    val room = session.identityPlace().room.takeIf(String::isNotBlank)
    val today = LocalDate.now()
    val lead = when {
        date == today -> {
            val now = LocalTime.now()
            val minutesAway = session.startMinute - (now.hour * 60 + now.minute)
            when {
                minutesAway <= 0 -> "Ahora"
                minutesAway < 60 -> "En $minutesAway min"
                minutesAway < 120 -> "En 1 h"
                else -> "En ${minutesAway / 60} h"
            }
        }
        date == today.plusDays(1) -> "Mañana"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE", IdentityLocale)).identityCapitalized()
    }
    return listOfNotNull(lead, time, room).joinToString("  •  ")
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
    /*
     * La rejilla arranca en el dia que diga el ajuste, no siempre en lunes.
     *
     * Estaba clavado en `previousOrSame(MONDAY)`, asi que el ajuste de «primer dia de la
     * semana» no tenia por donde llegar aqui: se elegia domingo y el calendario seguia
     * empezando en lunes.
     */
    val primerDia = DayOfWeek.of(LocalAppearancePreferences.current.firstDayOfWeek.isoDay)
    val firstCell = month.atDay(1).with(TemporalAdjusters.previousOrSame(primerDia))
    val leadingDays = huecosAntesDelUno(month.atDay(1).dayOfWeek, primerDia)
    val cellCount = ((leadingDays + month.lengthOfMonth() + 6) / 7) * 7

    // Sin caja alrededor. La rejilla ya es una forma cerrada por sí misma, y el contenedor
    // que la envolvía solo servía para meter la cuadrícula dentro de otra cuadrícula.
    Column {
        Row(
            Modifier.fillMaxWidth().height(48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilledTonalIconButton(
                onClick = { onDateSelected(selectedDate.minusMonths(1).withDayOfMonth(1)) },
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.schedule_identity_prev_month),
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = selectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy", IdentityLocale)).identityCapitalized(),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            FilledTonalIconButton(
                onClick = { onDateSelected(selectedDate.plusMonths(1).withDayOfMonth(1)) },
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize()),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.schedule_identity_next_month),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Row(Modifier.fillMaxWidth().padding(bottom = 6.dp)) {
            DayLabels.mediumDesde(primerDia).forEach { label ->
                Text(
                    text = label.uppercase(IdentityLocale),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = SectionLabelStyle.copy(fontSize = 10.sp, letterSpacing = 0.5.sp)
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

/**
 * Un día del mes: el número y, debajo, una barra si ese día tiene algo.
 *
 * Eran tres puntos y ahora es una barra, que es lo que dice la maqueta. Lo que no se pierde es
 * cuánto hay: la barra crece en tres tramos según sean una, dos o tres cosas o más. Un punto
 * de seis píxeles y una barra corta ocupan lo mismo, pero la barra se lee a la primera y no
 * obliga a contar puntos dentro de una casilla de cuarenta píxeles.
 */
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
            .height(46.dp)
            .padding(2.dp)
            .clip(RoundedCornerShape(13.dp))
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
                else -> MaterialTheme.colorScheme.outline
            },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
        Spacer(Modifier.height(4.dp))
        Box(
            Modifier
                .width(
                    when (colors.size) {
                        0 -> 0.dp
                        1 -> 8.dp
                        2 -> 12.dp
                        else -> 16.dp
                    }
                )
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(
                    when {
                        colors.isEmpty() -> Color.Transparent
                        selected -> MaterialTheme.colorScheme.onPrimary
                        else -> colors.first()
                    }
                )
        )
    }
}

/**
 * El día elegido del calendario, con el tipo de cada cosa dicho por delante.
 *
 * Antes cada fila empezaba por un punto de color, que distingue una materia de otra pero no
 * dice si eso es una clase, un examen o una entrega —y en un calendario esa es justamente la
 * diferencia que importa—. Ahora el tipo va escrito en una etiqueta, y el color sigue estando
 * dentro de ella, así que no se pierde nada de lo que el punto contaba.
 */
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

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = date.format(DateTimeFormatter.ofPattern(if (IdentityLocale.language == "en") "EEEE, MMMM d" else "EEEE, d 'de' MMMM", IdentityLocale)).identityCapitalized(),
            modifier = Modifier.padding(start = 4.dp),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        if (daySessions.isEmpty() && dayTasks.isEmpty() && dayAgendaEvents.isEmpty()) {
            IdentitySurface(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
                Text(
                    stringResource(R.string.schedule_identity_no_events_day),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        dayAgendaEvents.take(2).forEach { event ->
            IdentityAgendaRow(
                kind = event.identityKindLabel(),
                color = event.identityColor(),
                title = event.title,
                detail = event.identityTimeText(use24Hour) +
                    event.location.takeIf(String::isNotBlank)?.let { "  •  $it" }.orEmpty(),
                onClick = { onAgendaEventClick(event) }
            )
        }
        val remainingAfterEvents = (2 - dayAgendaEvents.size).coerceAtLeast(0)
        daySessions.take(remainingAfterEvents).forEach { session ->
            val subject = subjects.firstOrNull { it.id == session.subjectId }
            IdentityAgendaRow(
                kind = "CLASE",
                color = subject.identityColor(),
                title = subject?.name ?: "Clase",
                detail = formatIdentityMinute(session.startMinute, use24Hour) + " - " +
                    formatIdentityMinute(session.endMinute, use24Hour) + "  •  " +
                    session.identityPlace().room.ifBlank { stringResource(R.string.schedule_detail_no_room) },
                onClick = { onSessionClick(date, session) }
            )
        }
        val remainingAfterClasses = (remainingAfterEvents - daySessions.size).coerceAtLeast(0)
        dayTasks.take(remainingAfterClasses).forEach { task ->
            val subject = subjects.firstOrNull { it.id == task.subjectId }
            val isExam = task.type == TaskType.EXAM || task.type == TaskType.TEST
            IdentityAgendaRow(
                kind = if (isExam) "EXAMEN" else "ENTREGA",
                color = subject.identityColor(),
                title = task.title,
                detail = subject?.name ?: if (isExam) "Evaluación" else "Entrega",
                onClick = { onTaskClick(task.id) }
            )
        }
        val hidden = dayAgendaEvents.size + daySessions.size + dayTasks.size - 2
        if (hidden > 0) {
            Text(
                stringResource(R.string.schedule_identity_more_events, hidden),
                modifier = Modifier.padding(start = 6.dp),
                color = IdentityAccent,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Una fila de la agenda: la etiqueta del tipo, el nombre y el detalle. */
@Composable
private fun IdentityAgendaRow(
    kind: String,
    color: Color,
    title: String,
    detail: String,
    onClick: () -> Unit
) {
    IdentityOutlinedRow(onClick = onClick) {
        Box(
            Modifier
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = kind,
                color = contentColorOn(color),
                style = SectionLabelStyle.copy(fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 0.5.sp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/** Cómo se llama cada tipo de evento cuando cabe en una etiqueta de nueve píxeles. */
private fun AgendaEvent.identityKindLabel(): String = when (kind) {
    AgendaEventKind.PERSONAL -> "EVENTO"
    AgendaEventKind.MEETING -> "REUNIÓN"
    AgendaEventKind.REMINDER -> "AVISO"
    AgendaEventKind.CUSTOM -> "OTRO"
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
            .clip(MaterialTheme.shapes.medium)
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
    Button(
        shapes = UniStackButtonDefaults.shapes,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight).height(LocalInterfaceSpacing.current.controlHeight),
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
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) (if (IdentityLocale.language == "en") "AM" else "a. m.") else (if (IdentityLocale.language == "en") "PM" else "p. m."))
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

/**
 * «Tienes N clases sin marcar», con el atajo para resolverlas juntas.
 */
/**
 * Los pares de clases que se pisan en un mismo dia.
 *
 * Dos tramos se cruzan si uno empieza antes de que el otro acabe: basta con ordenarlos por hora
 * de inicio y mirar cada uno con el siguiente que le quede solapado. Se devuelven los pares y no
 * un `true`, porque el aviso dice **cuales** son.
 */
private fun crucesDelDia(sesiones: List<ClassSession>): List<Pair<ClassSession, ClassSession>> {
    val ordenadas = sesiones.sortedBy { it.startMinute }
    val pares = mutableListOf<Pair<ClassSession, ClassSession>>()
    for (i in ordenadas.indices) {
        for (j in i + 1 until ordenadas.size) {
            // Ordenadas por inicio: en cuanto una empieza despues del final de la de fuera,
            // las siguientes tambien, y no hace falta seguir mirando.
            if (ordenadas[j].startMinute >= ordenadas[i].endMinute) break
            pares += ordenadas[i] to ordenadas[j]
        }
    }
    return pares
}

/** El aviso de que hay clases pisandose, con el mismo aire que el de «sin marcar». */
@Composable
private fun AvisoDeCruce(cuantos: Int, detalle: String, onClick: () -> Unit) {
    val rojo = MaterialTheme.colorScheme.error
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(rojo.copy(alpha = 0.12f))
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.WarningAmber,
            contentDescription = null,
            tint = rojo,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = if (cuantos == 1) {
                    stringResource(R.string.schedule_overlap_one)
                } else {
                    stringResource(R.string.schedule_overlap_many, cuantos)
                },
                color = rojo,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            if (detalle.isNotBlank()) {
                Text(
                    text = detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun CatchUpBanner(count: Int, onClick: () -> Unit) {
    /*
     * Una linea, no una tarjeta.
     *
     * La primera version era del tamaño de las metricas que tiene debajo —icono en circulo,
     * dos lineas de texto y una flecha— y con eso pesaba lo mismo que el contenido de la
     * pantalla. Es un recado, no una seccion: se dice y se quita de en medio.
     */
    val tono = LocalSectionColors.current.schedule
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = CircleShape,
        color = tono.copy(alpha = 0.12f)
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, top = 9.dp, bottom = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(tono)
            )
            Text(
                text = if (count == 1) {
                    "Tienes 1 clase sin marcar"
                } else {
                    "Tienes $count clases sin marcar"
                },
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Ponerse al día",
                color = tono,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = tono,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

