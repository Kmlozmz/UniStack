package com.unistack.app.feature_home.domain

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AppModule

data class HomeSummary(
    val userName: String,
    val avatarPhotoUrl: String?,
    val generalAverage: Double?,
    val subjectsCount: Int,
    val tasksToday: Int,
    val subjects: List<SubjectSummary>,
    val neededGrade: NeededGradeSummary?,
    val nextTask: TaskSummary?,
    val weeklyExpenses: ExpenseSummary?,
    val gradingScale: GradingScale = GradingScale.ZERO_TO_FIVE,
    val enabledModules: Set<AppModule> = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
)

data class NeededGradeSummary(
    val subjectName: String,
    val targetAverage: Double,
    val neededGrade: Double
)
