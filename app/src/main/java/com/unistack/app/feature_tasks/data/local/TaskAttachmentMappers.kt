package com.unistack.app.feature_tasks.data.local

import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_tasks.domain.TaskAttachment

fun TaskAttachmentEntity.toDomain(): TaskAttachment = TaskAttachment(
    id = id,
    taskId = taskId,
    kind = runCatching { AttachmentKind.valueOf(kind) }.getOrDefault(AttachmentKind.FILE),
    displayName = displayName,
    storedName = storedName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMillis = durationMillis,
    createdAt = createdAt
)

fun TaskAttachment.toEntity(userId: String): TaskAttachmentEntity = TaskAttachmentEntity(
    id = id,
    userId = userId,
    taskId = taskId,
    kind = kind.name,
    displayName = displayName,
    storedName = storedName,
    mimeType = mimeType,
    sizeBytes = sizeBytes,
    durationMillis = durationMillis,
    createdAt = createdAt
)
