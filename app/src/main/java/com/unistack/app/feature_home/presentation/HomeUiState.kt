package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.TaskSummary

import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale

data class HomeUiState(
    val summary: HomeSummary = emptySummary
) {
    companion object {
        val emptySummary = HomeSummary(
            userName = "Estudiante",
            avatarPhotoUrl = null,
            dashboardMessage = "Configura tu semestre para ver prioridades reales.",
            generalAverage = 0.0,
            subjectsCount = 0,
            tasksToday = 0,
            overdueTasks = 0,
            subjects = emptyList(),
            riskSubject = null,
            neededGrade = null,
            nextTask = null,
            nextAcademicWork = null,
            weeklyExpenses = null,
            weeklyExpenseTotal = 0,
            productivitySummary = "Sin tareas todavía.",
            gradingScale = GradingScale.ZERO_TO_FIVE,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )
    }
}
