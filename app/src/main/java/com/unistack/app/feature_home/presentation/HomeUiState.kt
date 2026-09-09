package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary
import com.unistack.app.feature_home.domain.HomeSummary

import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale

data class HomeUiState(
    val summary: HomeSummary = emptySummary
) {
    companion object {
        private val isEnglish: Boolean get() = java.util.Locale.getDefault().language == "en"

        fun createEmptySummary() = HomeSummary(
            userName = if (isEnglish) "Student" else "Estudiante",
            avatarPhotoUrl = null,
            dashboardMessage = if (isEnglish) "Set up your semester to see real priorities." else "Configura tu semestre para ver prioridades reales.",
            priority = HomePrioritySummary(
                title = if (isEnglish) "Prepare your semester" else "Prepara tu semestre",
                shortDescription = if (isEnglish) "Add subjects and tasks to activate real priorities." else "Agrega materias y tareas para activar prioridades reales.",
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
            productivitySummary = if (isEnglish) "No tasks yet." else "Sin tareas todavía.",
            companionInsight = if (isEnglish) "Add your subjects so UniStack can support you better." else "Agrega tus materias para que UniStack te acompañe mejor.",
            gradingScale = GradingScale.ZERO_TO_FIVE,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )

        val emptySummary get() = createEmptySummary()
    }
}
