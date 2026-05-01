package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.TaskSummary

import com.unistack.app.feature_user.domain.GradingScale

data class HomeUiState(
    val summary: HomeSummary = emptySummary
) {
    companion object {
        val emptySummary = HomeSummary(
            userName = "Estudiante",
            avatarPhotoUrl = null,
            generalAverage = 0.0,
            subjectsCount = 0,
            tasksToday = 0,
            subjects = emptyList(),
            neededGrade = null,
            nextTask = null,
            weeklyExpenses = null,
            gradingScale = GradingScale.ZERO_TO_FIVE
        )
    }
}
