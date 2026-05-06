package com.unistack.app.feature_tasks.data.local

import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty

fun TaskEntity.toDomain(): StudentTask {
    val parsedDifficulty = runCatching { TaskDifficulty.valueOf(difficulty) }
        .getOrDefault(TaskDifficulty.MEDIUM)

    return StudentTask(
        id = id,
        title = title,
        subjectId = subjectId,
        dueDateMillis = dueDateMillis,
        difficulty = parsedDifficulty,
        estimatedMinutes = estimatedMinutes,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun StudentTask.toEntity(userId: String): TaskEntity {
    return TaskEntity(
        id = id,
        userId = userId,
        title = title,
        subjectId = subjectId,
        dueDateMillis = dueDateMillis,
        difficulty = difficulty.name,
        estimatedMinutes = estimatedMinutes,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
