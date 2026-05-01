package com.unistack.app.feature_home.domain

data class HomeSummary(
    val userName: String,
    val avatarPhotoUrl: String?,
    val generalAverage: Double,
    val subjectsCount: Int,
    val tasksToday: Int,
    val subjects: List<SubjectSummary>,
    val neededGrade: NeededGradeSummary?,
    val nextTask: TaskSummary,
    val weeklyExpenses: ExpenseSummary
)

data class NeededGradeSummary(
    val subjectName: String,
    val targetAverage: Double,
    val neededGrade: Double
)
