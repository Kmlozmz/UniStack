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
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Wallet
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.CurrencyFormatter
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_user.domain.AppModule

@Composable
internal fun SemesterSnapshot(
    summary: HomeSummary,
    compact: Boolean,
    onSubjectsClick: () -> Unit,
    onTasksClick: () -> Unit,
    onExpensesClick: () -> Unit,
    onWorksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val averageText = summary.generalAverage
        ?.let { GradingScaleUtils.formatGrade(it, summary.gradingScale) }
        ?: "Sin notas"
    val pendingText = when {
        summary.overdueTasks > 0 -> "${summary.overdueTasks} vencida${if (summary.overdueTasks == 1) "" else "s"}"
        summary.tasksToday > 0 -> "${summary.tasksToday} hoy"
        else -> "${summary.pendingTasks} pendiente${if (summary.pendingTasks == 1) "" else "s"}"
    }
    val worksText = when {
        summary.openAcademicWorks > 0 -> "${summary.openAcademicWorks} trabajo${if (summary.openAcademicWorks == 1) "" else "s"}"
        summary.nextAcademicWork != null -> "1 proximo"
        else -> "Sin trabajos"
    }
    val moneyText = when {
        AppModule.EXPENSES !in summary.enabledModules -> "Inactivo"
        summary.weeklyExpenseTotal > 0 -> CurrencyFormatter.formatCop(summary.weeklyExpenseTotal)
        else -> "Sin gastos"
    }
    val showWorks = AppModule.ACADEMIC_TEMPLATES in summary.enabledModules &&
        (summary.openAcademicWorks > 0 || summary.nextAcademicWork != null)

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(if (compact) 9.dp else 11.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Tu tablero", color = HomeText, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.weight(1f))
            Text("Actualizado", color = HomeMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(17.dp),
            color = HomeCard,
            border = BorderStroke(1.dp, HomeBorder)
        ) {
            Column(modifier = Modifier.padding(horizontal = if (compact) 13.dp else 15.dp, vertical = if (compact) 13.dp else 15.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SnapshotMetric(
                        label = "Materias",
                        value = summary.subjectsCount.toString(),
                        detail = "registradas",
                        icon = Icons.AutoMirrored.Rounded.MenuBook,
                        accent = HomePurple,
                        compact = compact,
                        onClick = onSubjectsClick,
                        modifier = Modifier.weight(1f)
                    )
                    SnapshotMetric(
                        label = "Promedio",
                        value = averageText,
                        detail = summary.neededGrade?.let { "meta ${GradingScaleUtils.formatGrade(it.targetAverage, summary.gradingScale)}" }
                            ?: "general",
                        icon = Icons.AutoMirrored.Rounded.TrendingUp,
                        accent = HomeTeal,
                        compact = compact,
                        onClick = onSubjectsClick,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    SnapshotMetric(
                        label = "Hoy",
                        value = pendingText,
                        detail = "tareas",
                        icon = Icons.AutoMirrored.Rounded.EventNote,
                        accent = HomeYellow,
                        compact = compact,
                        onClick = onTasksClick,
                        modifier = Modifier.weight(1f)
                    )
                    SnapshotMetric(
                        label = if (showWorks) "Trabajos" else "Gastos",
                        value = if (showWorks) worksText else moneyText,
                        detail = if (showWorks) "academicos" else "semana",
                        icon = if (showWorks) Icons.Rounded.Description else Icons.Rounded.Wallet,
                        accent = HomeCoral,
                        compact = compact,
                        onClick = if (showWorks) onWorksClick else onExpensesClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SnapshotMetric(
    label: String,
    value: String,
    detail: String,
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    compact: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = if (UniStackColors.IsDarkTheme) 0.08f else 0.07f))
            .cleanClickable(onClick)
            .padding(horizontal = if (compact) 11.dp else 12.dp, vertical = if (compact) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(if (compact) 30.dp else 34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.17f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(if (compact) 16.dp else 18.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(label, color = HomeMuted, fontSize = 10.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(
                value,
                color = HomeText,
                fontSize = if (compact) 13.sp else 14.sp,
                lineHeight = if (compact) 16.sp else 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(detail, color = HomeSoftText, fontSize = 9.sp, lineHeight = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}
