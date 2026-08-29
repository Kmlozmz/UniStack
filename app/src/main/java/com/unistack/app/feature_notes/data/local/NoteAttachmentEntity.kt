package com.unistack.app.feature_notes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "note_attachments",
    indices = [
        Index("userId"),
        Index("noteId")
    ]
)
data class NoteAttachmentEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val noteId: String,
    val kind: String,
    val displayName: String,
    val storedName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val durationMillis: Long?,
    val createdAt: Long
)
