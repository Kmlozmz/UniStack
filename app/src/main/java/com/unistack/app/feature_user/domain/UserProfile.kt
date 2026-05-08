package com.unistack.app.feature_user.domain

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
    val passingGrade: Double,
    val targetAverage: Double,
    val enabledModules: Set<AppModule>,
    val visualPreference: VisualPreference = VisualPreference.SYSTEM,
    val setupCompleted: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

enum class EducationLevel {
    SCHOOL,
    UNIVERSITY,
    TECHNICAL,
    INDEPENDENT_COURSE,
    OTHER
}

enum class GradingScale {
    ZERO_TO_FIVE,
    ZERO_TO_TEN,
    ZERO_TO_ONE_HUNDRED,
    LETTERS,
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
