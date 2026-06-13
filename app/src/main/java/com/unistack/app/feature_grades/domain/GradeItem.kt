package com.unistack.app.feature_grades.domain

data class GradeItem(
    val id: String,
    val name: String,
    val value: Double,
    val percentage: Double,
    val type: GradeType = GradeType.WORKSHOP,
    val periodId: String = "period-1",
    val source: GradeSource = GradeSource.ACTIVITY,
    val weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN,
    val taskId: String? = null,
    val recordedAt: Long = 0L
)

enum class GradeSource {
    ACTIVITY,
    PERIOD_FINAL
}

enum class GradeWeightStatus {
    KNOWN,
    UNKNOWN
}

enum class GradeType {
    WORKSHOP,
    PRESENTATION,
    QUIZ,
    EXAM,
    PROJECT,
    RESEARCH,
    PRACTICE,
    OTHER
}
