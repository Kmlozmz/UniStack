package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary
import com.unistack.app.feature_home.domain.HomeSummary

import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

data class HomeUiState(
    val summary: HomeSummary = emptySummary
) {
    companion object {

        fun createEmptySummary() = HomeSummary(
            userName = Textos.get(R.string.settings_profile_student),
            avatarPhotoUrl = null,
            dashboardMessage = Textos.get(R.string.home_configura_tu_semestre_para_ver_prioridades),
            priority = HomePrioritySummary(
                title = Textos.get(R.string.home_prepara_tu_semestre),
                shortDescription = Textos.get(R.string.home_agrega_materias_y_tareas_para_activar),
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
            productivitySummary = Textos.get(R.string.home_sin_tareas_todavia),
            companionInsight = Textos.get(R.string.home_agrega_tus_materias_para_que_unistack),
            gradingScale = GradingScale.ZERO_TO_FIVE,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )

        val emptySummary get() = createEmptySummary()
    }
}
