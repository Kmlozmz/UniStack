package com.unistack.app.feature_tasks.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    indices = [
        Index("userId"),
        Index("subjectId"),
        Index("dueDateMillis"),
        Index("periodId"),
        Index("gradingStatus")
    ]
)
data class TaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val subjectId: String?,
    val type: String,
    val dueDateMillis: Long,
    val difficulty: String,
    val estimatedMinutes: Int,
    val completed: Boolean,
    val periodId: String? = null,
    val gradingStatus: String = "UNDECIDED",
    val linkedGradeId: String? = null,
    val completedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)
