package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary

object DemoData {
    val homeSummary = HomeSummary(
        userName = "Pineda",
        avatarPhotoUrl = null,
        dashboardMessage = "Contabilidad necesita atención académica hoy.",
        generalAverage = 4.1,
        subjectsCount = 5,
        tasksToday = 3,
        overdueTasks = 0,
        subjects = listOf(
            SubjectSummary(
                id = "accounting",
                name = "Contabilidad",
                average = 3.7,
                progress = 0.74f,
                type = SubjectVisualType.TEAL
            ),
            SubjectSummary(
                id = "statistics",
                name = "Estadística",
                average = 4.3,
                progress = 0.86f,
                type = SubjectVisualType.BLUE
            ),
            SubjectSummary(
                id = "english",
                name = "Inglés",
                average = 4.5,
                progress = 0.90f,
                type = SubjectVisualType.CORAL
            )
        ),
        riskSubject = SubjectRiskSummary(
            subjectId = "accounting",
            subjectName = "Contabilidad",
            detail = "Necesitas 4.6 en lo restante.",
            severity = SubjectRiskSeverity.ATTENTION
        ),
        neededGrade = NeededGradeSummary(
            subjectName = "Contabilidad",
            targetAverage = 4.0,
            neededGrade = 4.6
        ),
        nextTask = TaskSummary(
            id = "task-costs",
            title = "Informe de costos",
            dueText = "vence mañana",
            estimatedTimeText = "2 h"
        ),
        nextAcademicWork = AcademicWorkSummary(
            id = "work-essay",
            subjectId = "accounting",
            title = "Ensayo argumentativo",
            dueText = "vence en 3 días",
            progress = 0.42f
        ),
        weeklyExpenses = ExpenseSummary(
            transport = 42000,
            food = 58000,
            total = 100000,
            chartValues = listOf(42, 26, 55, 38, 70, 35, 48)
        ),
        weeklyExpenseTotal = 100000,
        productivitySummary = "5 completadas · 3 pendientes"
    )

    val homeSummaryNoGrades = homeSummary.copy(
        generalAverage = null,
        subjects = homeSummary.subjects.map { it.copy(average = null, progress = 0f) },
        neededGrade = null
    )

    val homeUiState = HomeUiState(summary = homeSummary)
}
