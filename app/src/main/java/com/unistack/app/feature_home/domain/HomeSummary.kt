package com.unistack.app.feature_home.domain

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AppModule

data class HomeSummary(
    val userName: String,
    val avatarPhotoUrl: String?,
    val dashboardMessage: String,
    val priority: HomePrioritySummary,
    val generalAverage: Double?,
    val subjectsCount: Int,
    val tasksToday: Int,
    val overdueTasks: Int,
    val pendingTasks: Int,
    val openAcademicWorks: Int,
    val subjects: List<SubjectSummary>,
    val riskSubject: SubjectRiskSummary?,
    val neededGrade: NeededGradeSummary?,
    val nextTask: TaskSummary?,
    val nextAcademicWork: AcademicWorkSummary?,
    val todayItems: List<HomeTimelineSummary>,
    val weeklyExpenses: ExpenseSummary?,
    val weeklyExpenseTotal: Int,
    val productivitySummary: String,
    val companionInsight: String,
    val gradingScale: GradingScale = GradingScale.ZERO_TO_FIVE,
    val enabledModules: Set<AppModule> = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
)

data class HomePrioritySummary(
    val title: String,
    val shortDescription: String,
    val fullDescription: String,
    val suggestion: String,
    val action: HomePriorityAction,
    val subjectId: String? = null
)

enum class HomePriorityAction {
    SUBJECT,
    SUBJECTS,
    TASKS,
    TEMPLATES
}

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

data class HomeTimelineSummary(
    val timeText: String,
    val title: String,
    val subtitle: String,
    val kind: HomeTimelineKind,
    val state: HomeTimelineState
)

enum class HomeTimelineKind {
    CLASS,
    TASK,
    WORK,
    EXAM,
    FOCUS
}

enum class HomeTimelineState {
    CURRENT,
    PENDING,
    DONE
}
