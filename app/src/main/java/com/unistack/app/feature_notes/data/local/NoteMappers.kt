package com.unistack.app.feature_notes.data.local

import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.QuickNote

fun NoteEntity.toDomain(): QuickNote = QuickNote(
    id = id,
    body = body,
    subjectId = subjectId,
    format = runCatching { NoteFormat.valueOf(format) }.getOrDefault(NoteFormat.PLAIN),
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun QuickNote.toEntity(userId: String): NoteEntity = NoteEntity(
    id = id,
    userId = userId,
    body = body,
    subjectId = subjectId,
    format = format.name,
    pinned = pinned,
    createdAt = createdAt,
    updatedAt = updatedAt
)
