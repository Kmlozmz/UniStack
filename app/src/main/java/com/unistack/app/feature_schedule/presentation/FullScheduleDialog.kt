package com.unistack.app.feature_schedule.presentation

import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.utils.DayLabels

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassSession
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.unistack.app.core.utils.Textos

private val FullScheduleLocale: Locale get() = Locale.getDefault()
private val FullScheduleHourHeight = 64.dp
private val FullScheduleAxisWidth = 54.dp
private val FullScheduleTopInset = 12.dp
private val FullScheduleBottomInset = 16.dp

@Composable
internal fun FullScheduleDialog(
    selectedDate: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    use24Hour: Boolean,
    onDismiss: () -> Unit,
    onWeekChange: (LocalDate) -> Unit,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val weekStart = selectedDate.minusDays((selectedDate.dayOfWeek.value - 1).toLong())
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()
    val startHour = 0
    val endHour = 24
    val initialHour = remember(sessions) {
        ((sessions.minOfOrNull(ClassSession::startMinute) ?: 7 * 60) / 60 - 1).coerceIn(0, 23)
    }
    val initialScrollOffset = with(LocalDensity.current) {
        (FullScheduleHourHeight * initialHour).roundToPx()
    }

    LaunchedEffect(weekStart) {
        horizontalScroll.scrollTo(0)
    }
    LaunchedEffect(verticalScroll.maxValue, initialScrollOffset) {
        if (verticalScroll.maxValue > 0 && verticalScroll.value == 0) {
            verticalScroll.scrollTo(initialScrollOffset.coerceAtMost(verticalScroll.maxValue))
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                FullScheduleHeader(
                    weekStart = weekStart,
                    onDismiss = onDismiss,
                    onPreviousWeek = { onWeekChange(selectedDate.minusWeeks(1)) },
                    onNextWeek = { onWeekChange(selectedDate.plusWeeks(1)) }
                )
                FullScheduleDayHeader(
                    weekStart = weekStart,
                    horizontalScroll = horizontalScroll
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(verticalScroll)
                ) {
                    FullScheduleTimeAxis(
                        startHour = startHour,
                        endHour = endHour,
                        use24Hour = use24Hour
                    )
                    BoxWithConstraints(
                        modifier = Modifier
                            .weight(1f)
                            .height(
                                FullScheduleHourHeight * (endHour - startHour) +
                                    FullScheduleTopInset + FullScheduleBottomInset
                            )
                    ) {
                        val dayWidth = maxOf(maxWidth / 5f, 68.dp)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .horizontalScroll(horizontalScroll)
                        ) {
                            FullScheduleGrid(
                                modifier = Modifier.requiredWidth(dayWidth * 7),
                                weekStart = weekStart,
                                sessions = sessions,
                                subjects = subjects,
                                startHour = startHour,
                                endHour = endHour,
                                dayWidth = dayWidth,
                                use24Hour = use24Hour,
                                onSessionClick = onSessionClick
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScheduleHeader(
    weekStart: LocalDate,
    onDismiss: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val weekEnd = weekStart.plusDays(6)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniBackButton(
            contentDescription = stringResource(R.string.full_schedule_close),
            onClick = onDismiss
        )
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(R.string.full_schedule_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                fullScheduleWeekLabel(weekStart, weekEnd),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        UniIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
            contentDescription = stringResource(R.string.full_schedule_prev_week),
            onClick = onPreviousWeek
        )
        UniIconButton(
            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = stringResource(R.string.full_schedule_next_week),
            onClick = onNextWeek
        )
    }
}

@Composable
private fun FullScheduleDayHeader(
    weekStart: LocalDate,
    horizontalScroll: androidx.compose.foundation.ScrollState
) {
    Row(Modifier.fillMaxWidth().height(58.dp)) {
        Spacer(Modifier.width(FullScheduleAxisWidth))
        BoxWithConstraints(Modifier.weight(1f)) {
            val dayWidth = maxOf(maxWidth / 5f, 68.dp)
            Row(
                modifier = Modifier
                    .fillMaxHeight()
                    .horizontalScroll(horizontalScroll)
                    .requiredWidth(dayWidth * 7)
            ) {
                repeat(7) { index ->
                    val date = weekStart.plusDays(index.toLong())
                    val isToday = date == LocalDate.now()
                    Column(
                        modifier = Modifier
                            .width(dayWidth)
                            .fillMaxHeight()
                            .padding(vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            fullScheduleDayLetter(date.dayOfWeek),
                            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .size(25.dp)
                                .clip(CircleShape)
                                .background(if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                date.dayOfMonth.toString(),
                                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullScheduleTimeAxis(
    startHour: Int,
    endHour: Int,
    use24Hour: Boolean
) {
    Box(
        modifier = Modifier
            .width(FullScheduleAxisWidth)
            .height(
                FullScheduleHourHeight * (endHour - startHour) +
                    FullScheduleTopInset + FullScheduleBottomInset
            )
            .background(MaterialTheme.colorScheme.background)
    ) {
        (startHour..endHour).forEach { hour ->
            Text(
                text = fullScheduleTime(hour * 60, use24Hour),
                modifier = Modifier
                    .offset(
                        y = FullScheduleTopInset +
                            FullScheduleHourHeight * (hour - startHour) - 8.dp
                    )
                    .fillMaxWidth()
                    .padding(end = 7.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun FullScheduleGrid(
    modifier: Modifier,
    weekStart: LocalDate,
    sessions: List<ClassSession>,
    subjects: List<Subject>,
    startHour: Int,
    endHour: Int,
    dayWidth: Dp,
    use24Hour: Boolean,
    onSessionClick: (LocalDate, ClassSession) -> Unit
) {
    val gridHeight = FullScheduleHourHeight * (endHour - startHour)
    val contentHeight = gridHeight + FullScheduleTopInset + FullScheduleBottomInset
    Box(
        modifier = modifier
            .height(contentHeight)
            .background(MaterialTheme.colorScheme.background)
    ) {
        repeat((endHour - startHour) * 2 + 1) { line ->
            val isFullHour = line % 2 == 0
            HorizontalDivider(
                modifier = Modifier.offset(
                    y = FullScheduleTopInset + FullScheduleHourHeight * (line / 2f)
                ),
                thickness = if (isFullHour) 1.dp else 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isFullHour) 0.72f else 0.34f)
            )
        }
        repeat(8) { line ->
            Box(
                Modifier
                    .offset(x = dayWidth * line, y = FullScheduleTopInset)
                    .width(1.dp)
                    .height(gridHeight)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
            )
        }

        sessions.forEach { session ->
            repeat(7) { dayIndex ->
                val date = weekStart.plusDays(dayIndex.toLong())
                if (session.occursOn(date.toEpochDay(), date.dayOfWeek.value)) {
                    val blockHeight =
                        (FullScheduleHourHeight * ((session.endMinute - session.startMinute) / 60f) - 6.dp)
                            .coerceAtLeast(30.dp)
                    FullScheduleSession(
                        modifier = Modifier
                            .offset(
                                x = dayWidth * dayIndex + 3.dp,
                                y = FullScheduleTopInset +
                                    FullScheduleHourHeight * ((session.startMinute - startHour * 60) / 60f) + 3.dp
                            )
                            .width(dayWidth - 6.dp)
                            .height(blockHeight),
                        height = blockHeight,
                        session = session,
                        subject = subjects.firstOrNull { it.id == session.subjectId },
                        use24Hour = use24Hour,
                        onClick = { onSessionClick(date, session) }
                    )
                }
            }
        }

        if (sessions.none { session ->
                (0..6).any { dayIndex ->
                    val date = weekStart.plusDays(dayIndex.toLong())
                    session.occursOn(date.toEpochDay(), date.dayOfWeek.value)
                }
            }
        ) {
            Text(
                stringResource(R.string.full_schedule_no_classes),
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun FullScheduleSession(
    modifier: Modifier,
    height: Dp,
    session: ClassSession,
    subject: Subject?,
    use24Hour: Boolean,
    onClick: () -> Unit
) {
    ClassBlock(
        modifier = modifier,
        height = height,
        color = subject.scheduleBlockColor(MaterialTheme.colorScheme.primary),
        name = subject?.name ?: stringResource(R.string.schedule_detail_class),
        room = session.place.room,
        startLabel = fullScheduleTime(session.startMinute, use24Hour),
        onClick = onClick
    )
}

private fun fullScheduleDayLetter(day: DayOfWeek): String =
    DayLabels.short[day.value - 1]

private fun fullScheduleTime(value: Int, use24Hour: Boolean): String {
    val hour = (value / 60) % 24
    val minute = value % 60
    if (use24Hour) return "%02d:%02d".format(hour, minute)
    val displayHour = (hour % 12).takeIf { it != 0 } ?: 12
    return "%d:%02d %s".format(displayHour, minute, if (hour < 12) (Textos.get(R.string.schedule_a_m)) else (Textos.get(R.string.schedule_p_m)))
}

private fun fullScheduleWeekLabel(start: LocalDate, end: LocalDate): String {
    val formatter = DateTimeFormatter.ofPattern("d MMM", FullScheduleLocale)
    return "${start.format(formatter)} – ${end.format(formatter)}"
}
