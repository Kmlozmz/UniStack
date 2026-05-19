package com.unistack.app.feature_tasks.domain

data class StudentTask(
    val id: String,
    val title: String,
    val subjectId: String?,
    val type: TaskType,
    val dueDateMillis: Long,
    val difficulty: TaskDifficulty,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)

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
    OTHER
}
