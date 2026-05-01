package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary

object DemoData {
    val homeSummary = HomeSummary(
        userName = "Pineda",
        avatarPhotoUrl = null,
        generalAverage = 4.1,
        subjectsCount = 5,
        tasksToday = 3,
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
        neededGrade = NeededGradeSummary(
            subjectName = "Contabilidad",
            targetAverage = 4.0,
            neededGrade = 4.6
        ),
        nextTask = TaskSummary(
            title = "Informe de costos",
            dueText = "vence mañana",
            estimatedTimeText = "2 h"
        ),
        weeklyExpenses = ExpenseSummary(
            transport = 42000,
            food = 58000,
            chartValues = listOf(42, 26, 55, 38, 70, 35, 48)
        )
    )

    val homeSummaryNoGrades = homeSummary.copy(
        generalAverage = null,
        subjects = homeSummary.subjects.map { it.copy(average = null, progress = 0f) },
        neededGrade = null
    )

    val homeUiState = HomeUiState(summary = homeSummary)
}
