package com.unistack.app.feature_user.domain

import com.unistack.app.feature_expenses.domain.ExpenseCategory

data class UserProfile(
    val userId: String,
    val preferredName: String,
    val accountProvider: AuthProvider = AuthProvider.LOCAL,
    val accountProviderUserId: String? = null,
    val accountEmail: String? = null,
    val accountPhotoUrl: String? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
    val lastSyncAt: Long? = null,
    val educationLevel: EducationLevel,
    val careerOrProgram: String?,
    val studyArea: StudyArea?,
    val gradeLevel: String?,
    val gradingScale: GradingScale,
    val customGradeMax: Double = 100.0,
    val passingGrade: Double,
    val targetAverage: Double,
    val enabledModules: Set<AppModule>,
    val visualPreference: VisualPreference = VisualPreference.SYSTEM,
    val taskRemindersEnabled: Boolean = true,
    val academicWorkRemindersEnabled: Boolean = true,
    val overdueRemindersEnabled: Boolean = true,
    val reminderLeadHours: Int = 24,
    val weeklyBudget: Int = 0,
    val monthlyBudget: Int = 0,
    val expenseAlertThresholdPercent: Int = 80,
    val enabledExpenseCategories: Set<ExpenseCategory> = ExpenseCategory.entries.toSet(),
    val gradeScenarios: List<SavedGradeScenario> = emptyList(),
    val setupCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

data class SavedGradeScenario(
    val id: String,
    val subjectId: String,
    val subjectName: String,
    val name: String,
    val targetAverage: Double,
    val neededGrade: Double?,
    val createdAt: Long
)

enum class EducationLevel {
    PRIMARY,
    SECONDARY,
    UNIVERSITY,
    OTHER
}

enum class GradingScale {
    ZERO_TO_FIVE,
    CUSTOM
}

enum class StudyArea {
    ENGINEERING_TECHNOLOGY,
    ECONOMICS_BUSINESS,
    LAW_POLITICS,
    HEALTH_SCIENCES,
    EDUCATION,
    ARTS_DESIGN,
    SOCIAL_SCIENCES,
    BASIC_SCIENCES,
    OTHER
}

enum class AppModule {
    GRADES,
    TASKS,
    EXPENSES,
    ACADEMIC_TEMPLATES
}

enum class VisualPreference {
    SYSTEM,
    LIGHT,
    DARK
}
