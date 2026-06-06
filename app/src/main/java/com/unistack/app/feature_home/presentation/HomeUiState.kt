package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary
import com.unistack.app.feature_home.domain.HomeSummary

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
            priority = HomePrioritySummary(
                title = "Prepara tu semestre",
                shortDescription = "Agrega materias y tareas para activar prioridades reales.",
                fullDescription = "Configura tus materias y tareas para que UniStack pueda convertir el Home en una agenda inteligente con prioridades reales.",
                suggestion = "Siguiente paso: crear tu primera materia.",
                action = HomePriorityAction.SUBJECTS
            ),
            dailyFocusItems = emptyList(),
            generalAverage = null,
            subjectsCount = 0,
            tasksToday = 0,
            overdueTasks = 0,
            pendingTasks = 0,
            openAcademicWorks = 0,
            subjects = emptyList(),
            riskSubject = null,
            neededGrade = null,
            nextTask = null,
            nextAcademicWork = null,
            todayItems = emptyList(),
            weeklyExpenses = null,
            weeklyExpenseTotal = 0,
            productivitySummary = "Sin tareas todavía.",
            companionInsight = "Agrega tus materias para que UniStack te acompañe mejor.",
            gradingScale = GradingScale.ZERO_TO_FIVE,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )
    }
}
