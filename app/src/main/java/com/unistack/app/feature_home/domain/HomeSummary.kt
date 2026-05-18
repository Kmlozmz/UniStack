package com.unistack.app.feature_home.domain

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AppModule

data class HomeSummary(
    val userName: String,
    val avatarPhotoUrl: String?,
    val dashboardMessage: String,
    val generalAverage: Double?,
    val subjectsCount: Int,
    val tasksToday: Int,
    val overdueTasks: Int,
    val subjects: List<SubjectSummary>,
    val riskSubject: SubjectRiskSummary?,
    val neededGrade: NeededGradeSummary?,
    val nextTask: TaskSummary?,
    val nextAcademicWork: AcademicWorkSummary?,
    val weeklyExpenses: ExpenseSummary?,
    val weeklyExpenseTotal: Int,
    val productivitySummary: String,
    val gradingScale: GradingScale = GradingScale.ZERO_TO_FIVE,
    val enabledModules: Set<AppModule> = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
)

data class NeededGradeSummary(
    val subjectName: String,
    val targetAverage: Double,
    val neededGrade: Double
)

data class SubjectRiskSummary(
    val subjectId: String,
    val subjectName: String,
    val detail: String,
    val severity: SubjectRiskSeverity
)

enum class SubjectRiskSeverity {
    STABLE,
    ATTENTION,
    CRITICAL
}

data class AcademicWorkSummary(
    val id: String,
    val subjectId: String?,
    val title: String,
    val dueText: String,
    val progress: Float
)
