package com.unistack.app.feature_grades.domain

data class GradeResult(
    val currentAverage: Double,
    val finalAverage: Double,
    val neededGrade: Double?
)
