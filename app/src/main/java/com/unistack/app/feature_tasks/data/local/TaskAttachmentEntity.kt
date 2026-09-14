package com.unistack.app.feature_tasks.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "task_attachments",
    indices = [
        Index("userId"),
        Index("taskId")
    ]
)
data class TaskAttachmentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val taskId: String,
    val kind: String,
    val displayName: String,
    val storedName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMillis: Long?,
    val createdAt: Long
)
