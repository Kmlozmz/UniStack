package com.unistack.app.feature_home.domain

import com.unistack.app.feature_grades.domain.SubjectVisualType

data class SubjectSummary(
    val id: String,
    val name: String,
    val average: Double?,
    val progress: Float,
    val type: SubjectVisualType
)
