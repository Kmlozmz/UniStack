package com.unistack.app.feature_templates.domain

data class AcademicWork(
    val id: String,
    val templateId: String,
    val title: String,
    val subjectId: String?,
    val dueDateMillis: Long?,
    val status: AcademicWorkStatus,
    val priority: AcademicWorkPriority,
    val completedChecklistIds: Set<String>,
    val thesis: String,
    val outline: String,
    val sources: String,
    val notes: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    val checklistProgress: Float
        get() {
            val total = AcademicTemplateLibrary.checklist.size
            return if (total == 0) 0f else completedChecklistIds.size.coerceAtMost(total).toFloat() / total
        }

    val isFinished: Boolean
        get() = status == AcademicWorkStatus.SUBMITTED
}

enum class AcademicWorkStatus {
    IDEA,
    DRAFT,
    REVIEW,
    READY,
    SUBMITTED
}

enum class AcademicWorkPriority {
    LOW,
    MEDIUM,
    HIGH
}
