package com.unistack.app.feature_grades.data.local

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType

fun SubjectEntity.toDomain(grades: List<GradeItem>): Subject {
    val type = runCatching { SubjectVisualType.valueOf(visualType) }
        .getOrDefault(SubjectVisualType.TEAL)
    return Subject(
        id = id,
        name = name,
        targetAverage = targetAverage,
        grades = grades,
        visualType = type
    )
}

fun Subject.toEntity(userId: String): SubjectEntity {
    val now = System.currentTimeMillis()
    return SubjectEntity(
        id = id,
        userId = userId,
        name = name,
        targetAverage = targetAverage,
        visualType = visualType.name,
        createdAt = now,
        updatedAt = now
    )
}

fun GradeEntity.toDomain(): GradeItem {
    val gradeType = runCatching { GradeType.valueOf(type) }
        .getOrDefault(GradeType.WORKSHOP)
    return GradeItem(
        id = id,
        name = name,
        value = value,
        percentage = percentage,
        type = gradeType,
        periodId = periodId.ifBlank { "period-1" }
    )
}

fun GradeItem.toEntity(subjectId: String): GradeEntity {
    return GradeEntity(
        id = id,
        subjectId = subjectId,
        name = name,
        value = value,
        percentage = percentage,
        type = type.name,
        periodId = periodId,
        createdAt = System.currentTimeMillis()
    )
}
