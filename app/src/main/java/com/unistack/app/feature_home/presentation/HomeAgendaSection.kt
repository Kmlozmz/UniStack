package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.feature_home.domain.DailyFocusItem
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.HomeTimelineKind
import com.unistack.app.feature_home.domain.HomeTimelineState
import com.unistack.app.feature_home.domain.HomeTimelineSummary
import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors

@Composable
internal fun TodayAgenda(
    summary: HomeSummary,
    compact: Boolean,
    onTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timelineItems = summary.todayItems.take(3)
    if (summary.subjectsCount == 0 && timelineItems.isEmpty()) return

    val countText = when {
        timelineItems.isNotEmpty() -> "${timelineItems.size} acción${if (timelineItems.size == 1) "" else "es"}"
        else -> "Sin pendientes"
    }

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Agenda", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text(countText, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(vertical = if (compact) 6.dp else 8.dp)) {
                when {
                    timelineItems.isNotEmpty() -> {
                        timelineItems.forEachIndexed { index, item ->
                            AgendaTimelineRow(item = item.toTimelineItem(), compact = compact, onClick = onTasksClick)
                            if (index < timelineItems.lastIndex) AgendaDivider()
                        }
                    }
                    else -> TimelineEmptyRow(compact = compact)
                }
            }
        }
    }
}

@Composable
private fun AgendaTimelineRow(
    item: TimelineItem,
    compact: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onClick)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 10.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgendaIcon(icon = item.icon, accent = item.accent, compact = compact)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.time, color = item.accent, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

@Composable
internal fun AgendaFocusRow(
    item: DailyFocusItem,
    compact: Boolean,
    onClick: () -> Unit
) {
    val accent = item.action.agendaAccent()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onClick)
            .padding(
                horizontal = if (compact) 14.dp else 16.dp,
                vertical = if (compact) 10.dp else 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AgendaIcon(icon = item.action.focusIcon(), accent = accent, compact = compact)
        Spacer(modifier = Modifier.width(13.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(item.slotLabel, color = accent, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                item.title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                item.detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                lineHeight = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(item.minutesText, color = accent, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun AgendaIcon(
    icon: ImageVector,
    accent: Color,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .size(if (compact) 34.dp else 38.dp)
            .clip(CircleShape)
            .background(accent.copy(alpha = 0.16f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 18.dp else 20.dp))
    }
}

@Composable
internal fun AgendaDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 63.dp, end = 14.dp)
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant)
    )
}

@Composable
private fun TimelineEmptyRow(compact: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (compact) 76.dp else 86.dp)
            .padding(horizontal = if (compact) 14.dp else 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 34.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text("Día despejado", color = MaterialTheme.colorScheme.onSurface, fontSize = if (compact) 12.sp else 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold)
            Text("Sin vencimientos cercanos por ahora", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, lineHeight = 13.sp)
        }
    }
}

private data class TimelineItem(
    val time: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accent: Color,
    val active: Boolean
)

@Composable
private fun HomeTimelineSummary.toTimelineItem(): TimelineItem {
    return TimelineItem(
        time = timeText,
        title = title,
        subtitle = subtitle,
        icon = when (kind) {
            HomeTimelineKind.CLASS -> Icons.Rounded.CalendarMonth
            HomeTimelineKind.TASK -> Icons.AutoMirrored.Rounded.EventNote
            HomeTimelineKind.WORK -> Icons.Rounded.Description
            HomeTimelineKind.EXAM -> Icons.AutoMirrored.Rounded.Assignment
            HomeTimelineKind.FOCUS -> Icons.Rounded.Star
        },
        accent = when (kind) {
            HomeTimelineKind.CLASS -> MaterialTheme.colorScheme.primary
            HomeTimelineKind.TASK -> MaterialTheme.colorScheme.tertiary
            HomeTimelineKind.WORK -> LocalSectionColors.current.atRisk
            HomeTimelineKind.EXAM -> MaterialTheme.colorScheme.error
            HomeTimelineKind.FOCUS -> MaterialTheme.colorScheme.primary
        },
        active = state == HomeTimelineState.CURRENT
    )
}

internal fun HomePriorityAction.focusIcon(): ImageVector {
    return when (this) {
        HomePriorityAction.SUBJECT,
        HomePriorityAction.SUBJECTS -> Icons.Rounded.School
        HomePriorityAction.TASKS -> Icons.AutoMirrored.Rounded.EventNote
        HomePriorityAction.EXPENSES -> Icons.Rounded.Wallet
        HomePriorityAction.TEMPLATES -> Icons.Rounded.Description
        HomePriorityAction.SCHEDULE -> Icons.Rounded.CalendarMonth
    }
}

@Composable
internal fun HomePriorityAction.agendaAccent(): Color {
    return when (this) {
        HomePriorityAction.SUBJECT,
        HomePriorityAction.SUBJECTS -> MaterialTheme.colorScheme.primary
        HomePriorityAction.TASKS -> MaterialTheme.colorScheme.tertiary
        HomePriorityAction.EXPENSES -> MaterialTheme.colorScheme.error
        HomePriorityAction.TEMPLATES -> LocalSectionColors.current.atRisk
        HomePriorityAction.SCHEDULE -> MaterialTheme.colorScheme.tertiary
    }
}
