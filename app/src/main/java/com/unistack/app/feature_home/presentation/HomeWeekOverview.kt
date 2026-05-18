package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.SectionHeader
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.feature_home.domain.HomeSummary

@Composable
internal fun WeekOverviewSection(
    summary: HomeSummary,
    showTasks: Boolean,
    showExpenses: Boolean,
    showTemplates: Boolean,
    modifier: Modifier = Modifier
) {
    val cards = buildList {
        if (showTasks) {
            add(
                WeekMetric(
                    icon = Icons.Rounded.CheckCircle,
                    title = "Hoy",
                    value = "${summary.tasksToday}",
                    detail = if (summary.overdueTasks > 0) "${summary.overdueTasks} vencidas" else "tareas",
                    accent = if (summary.overdueTasks > 0) UniStackColors.Coral else UniStackColors.Green
                )
            )
            add(
                WeekMetric(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Tarea",
                    value = summary.nextTask?.dueText ?: "--",
                    detail = summary.nextTask?.title ?: "sin pendientes",
                    accent = UniStackColors.Primary
                )
            )
        }
        if (showTemplates) {
            add(
                WeekMetric(
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    title = "Trabajo",
                    value = summary.nextAcademicWork?.dueText ?: "--",
                    detail = summary.nextAcademicWork?.title ?: "sin entregas",
                    accent = UniStackColors.Blue
                )
            )
        }
        if (showExpenses) {
            add(
                WeekMetric(
                    icon = Icons.Rounded.AccountBalanceWallet,
                    title = "Gastos",
                    value = CurrencyFormatter.formatCop(summary.weeklyExpenseTotal),
                    detail = "semana actual",
                    accent = UniStackColors.Coral
                )
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Esta semana")
        cards.chunked(2).forEach { rowCards ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                rowCards.forEach { card ->
                    WeekMetricCard(
                        icon = card.icon,
                        title = card.title,
                        value = card.value,
                        detail = card.detail,
                        accent = card.accent,
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowCards.size == 1) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

private data class WeekMetric(
    val icon: ImageVector,
    val title: String,
    val value: String,
    val detail: String,
    val accent: Color
)

@Composable
private fun WeekMetricCard(
    icon: ImageVector,
    title: String,
    value: String,
    detail: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 2.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = accent)
                Text(
                    title,
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text(value, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Text(detail, color = UniStackColors.TextSecondary, fontSize = 11.sp, maxLines = 2)
        }
    }
}
