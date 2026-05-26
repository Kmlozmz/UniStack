package com.unistack.app.feature_tasks.data.local

import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType

fun TaskEntity.toDomain(): StudentTask {
    val parsedDifficulty = runCatching { TaskDifficulty.valueOf(difficulty) }
        .getOrDefault(TaskDifficulty.MEDIUM)
    val parsedType = runCatching { TaskType.valueOf(type) }
        .getOrDefault(TaskType.WORKSHOP)

    return StudentTask(
        id = id,
        title = title,
        description = description,
        subjectId = subjectId,
        type = parsedType,
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
        description = description,
        subjectId = subjectId,
        type = type.name,
        dueDateMillis = dueDateMillis,
        difficulty = difficulty.name,
        estimatedMinutes = estimatedMinutes,
        completed = completed,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
