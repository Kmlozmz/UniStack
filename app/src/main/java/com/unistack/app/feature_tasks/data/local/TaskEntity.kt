package com.unistack.app.feature_tasks.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("userId"),
        Index("subjectId"),
        Index("dueDateMillis")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val subjectId: String?,
    val dueDateMillis: Long,
    val difficulty: String,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
