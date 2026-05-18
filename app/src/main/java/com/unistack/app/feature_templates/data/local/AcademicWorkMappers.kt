package com.unistack.app.feature_templates.data.local

import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import org.json.JSONArray

fun AcademicWorkEntity.toDomain(): AcademicWork {
    return AcademicWork(
        id = id,
        templateId = templateId,
        title = title,
        subjectId = subjectId,
        dueDateMillis = dueDateMillis,
        status = runCatching { AcademicWorkStatus.valueOf(status) }.getOrDefault(AcademicWorkStatus.DRAFT),
        priority = runCatching { AcademicWorkPriority.valueOf(priority) }.getOrDefault(AcademicWorkPriority.MEDIUM),
        completedChecklistIds = completedChecklistIdsJson.toStringSet(),
        thesis = thesis,
        outline = outline,
        sources = sources,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun AcademicWork.toEntity(userId: String): AcademicWorkEntity {
    return AcademicWorkEntity(
        id = id,
        userId = userId,
        templateId = templateId,
        title = title,
        subjectId = subjectId,
        dueDateMillis = dueDateMillis,
        status = status.name,
        priority = priority.name,
        completedChecklistIdsJson = completedChecklistIds.toJsonArrayString(),
        thesis = thesis,
        outline = outline,
        sources = sources,
        notes = notes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Set<String>.toJsonArrayString(): String {
    val array = JSONArray()
    sorted().forEach(array::put)
    return array.toString()
}

fun String.toStringSet(): Set<String> {
    if (isBlank()) return emptySet()
    return runCatching {
        val array = JSONArray(this)
        buildSet {
            for (index in 0 until array.length()) {
                val value = array.optString(index)
                if (value.isNotBlank()) add(value)
            }
        }
    }.getOrDefault(emptySet())
}
