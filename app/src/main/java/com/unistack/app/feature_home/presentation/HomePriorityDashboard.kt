package com.unistack.app.feature_home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.unistack.app.feature_home.domain.SubjectRiskSeverity

@Composable
internal fun PriorityDashboardSection(
    summary: HomeSummary,
    showGrades: Boolean,
    showTasks: Boolean,
    showExpenses: Boolean,
    showTemplates: Boolean,
    onSubjectClick: (String) -> Unit,
    onSeeTasksClick: () -> Unit,
    onSeeExpensesClick: () -> Unit,
    onOpenTemplatesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cards = buildList {
        if (showGrades && summary.riskSubject != null) {
            val severityColor = when (summary.riskSubject.severity) {
                SubjectRiskSeverity.CRITICAL -> UniStackColors.Coral
                SubjectRiskSeverity.ATTENTION -> UniStackColors.Yellow
                SubjectRiskSeverity.STABLE -> UniStackColors.Green
            }
            add(
                PriorityDashboardItem(
                    title = when (summary.riskSubject.severity) {
                        SubjectRiskSeverity.CRITICAL -> "Materia crítica"
                        SubjectRiskSeverity.ATTENTION -> "Materia en foco"
                        SubjectRiskSeverity.STABLE -> "Materia estable"
                    },
                    body = "${summary.riskSubject.subjectName}: ${summary.riskSubject.detail}",
                    icon = Icons.Rounded.TrackChanges,
                    accent = severityColor,
                    actionText = "Abrir",
                    onActionClick = { onSubjectClick(summary.riskSubject.subjectId) }
                )
            )
        }
        if (showTasks && summary.nextTask != null) {
            add(
                PriorityDashboardItem(
                    title = if (summary.overdueTasks > 0) "Tareas vencidas" else "Próxima tarea",
                    body = "${summary.nextTask.title}: ${summary.nextTask.dueText} · ${summary.nextTask.estimatedTimeText}",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    accent = if (summary.overdueTasks > 0) UniStackColors.Coral else UniStackColors.Green,
                    actionText = "Ver tareas",
                    onActionClick = onSeeTasksClick
                )
            )
        }
        if (showExpenses && summary.weeklyExpenseTotal > 0) {
            add(
                PriorityDashboardItem(
                    title = "Gasto semanal",
                    body = "Has registrado ${CurrencyFormatter.formatCop(summary.weeklyExpenseTotal)} esta semana.",
                    icon = Icons.Rounded.AccountBalanceWallet,
                    accent = UniStackColors.Coral,
                    actionText = "Ver gastos",
                    onActionClick = onSeeExpensesClick
                )
            )
        }
        if (showTemplates && summary.nextAcademicWork != null) {
            add(
                PriorityDashboardItem(
                    title = "Próximo trabajo",
                    body = "${summary.nextAcademicWork.title}: ${summary.nextAcademicWork.dueText}",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    accent = UniStackColors.Blue,
                    actionText = "Abrir",
                    progress = summary.nextAcademicWork.progress,
                    onActionClick = onOpenTemplatesClick
                )
            )
        } else if (showTemplates) {
            add(
                PriorityDashboardItem(
                    title = "Trabajos académicos",
                    body = "Checklist, estructura y APA están listos para tu próximo borrador.",
                    icon = Icons.AutoMirrored.Rounded.Assignment,
                    accent = UniStackColors.Blue,
                    actionText = "Abrir",
                    onActionClick = onOpenTemplatesClick
                )
            )
        }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader(title = "Prioridades")
        Text(
            text = summary.dashboardMessage,
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        BoxWithConstraints {
            if (maxWidth >= 600.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    cards.chunked(2).forEach { rowCards ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            rowCards.forEach { card ->
                                PriorityActionCard(card = card, modifier = Modifier.weight(1f))
                            }
                            if (rowCards.size == 1) {
                                Box(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cards.forEach { card ->
                        PriorityActionCard(card = card)
                    }
                }
            }
        }
    }
}

private data class PriorityDashboardItem(
    val title: String,
    val body: String,
    val icon: ImageVector,
    val accent: Color,
    val actionText: String,
    val progress: Float? = null,
    val onActionClick: () -> Unit
)

@Composable
private fun PriorityActionCard(
    card: PriorityDashboardItem,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 2.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(card.accent.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(card.icon, contentDescription = null, tint = card.accent, modifier = Modifier.size(20.dp))
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(card.title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                Text(card.body, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
            }
            Button(
                onClick = card.onActionClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = card.accent),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(card.actionText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
        card.progress?.let { progress ->
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                color = card.accent,
                trackColor = UniStackColors.SurfaceVariant
            )
        }
    }
}
