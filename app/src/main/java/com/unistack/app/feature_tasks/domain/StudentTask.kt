package com.unistack.app.feature_tasks.domain

data class StudentTask(
    val id: String,
    val title: String,
    val subjectName: String?,
    val dueDateText: String,
    val difficulty: TaskDifficulty,
    val estimatedMinutes: Int,
    val completed: Boolean
)

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}
