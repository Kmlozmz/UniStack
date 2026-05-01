package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.TaskSummary

data class HomeUiState(
    val summary: HomeSummary = emptySummary
) {
    companion object {
        val emptySummary = HomeSummary(
            userName = "Estudiante",
            avatarPhotoUrl = null,
            generalAverage = 0.0,
            subjectsCount = 0,
            tasksToday = 3,
            subjects = emptyList(),
            neededGrade = null,
            nextTask = TaskSummary(
                title = "Crea tu primera tarea",
                dueText = "cuando quieras",
                estimatedTimeText = "15 min"
            ),
            weeklyExpenses = ExpenseSummary(
                transport = 42000,
                food = 58000,
                chartValues = listOf(42, 26, 55, 38, 70, 35, 48)
            )
        )
    }
}
