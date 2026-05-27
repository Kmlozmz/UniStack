package com.unistack.app.feature_grades.domain

data class GradeItem(
    val id: String,
    val name: String,
    val value: Double,
    val percentage: Double,
    val type: GradeType = GradeType.WORKSHOP,
    val periodId: String = "period-1"
)

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
