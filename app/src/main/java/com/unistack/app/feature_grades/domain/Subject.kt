package com.unistack.app.feature_grades.domain

data class Subject(
    val id: String,
    val name: String,
    val targetAverage: Double,
    val grades: List<GradeItem>,
    val visualType: SubjectVisualType = SubjectVisualType.TEAL
)
