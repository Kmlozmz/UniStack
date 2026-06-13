package com.unistack.app.feature_tasks.domain

data class StudentTask(
    val id: String,
    val title: String,
    val description: String,
    val subjectId: String?,
    val type: TaskType,
    val dueDateMillis: Long,
    val difficulty: TaskDifficulty,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
    val periodId: String? = null,
    val gradingStatus: TaskGradingStatus = TaskGradingStatus.UNDECIDED,
    val linkedGradeId: String? = null,
    val completedAt: Long? = null
)

enum class TaskGradingStatus {
    UNDECIDED,
    NOT_GRADED,
    AWAITING_GRADE,
    GRADED
}

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}

enum class TaskType {
    WORKSHOP,
    EXAM,
    ESSAY,
    PRESENTATION,
    RESEARCH,
    TEST,
    PRACTICE,
    PROJECT,
    READING,
    OTHER
}
