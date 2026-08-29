package com.unistack.app.feature_notes.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "notes",
    indices = [
        Index("userId"),
        Index("subjectId"),
        Index("updatedAt"),
        Index("reminderAt")
    ]
)
data class NoteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val body: String,
    val subjectId: String?,
    val format: String,
    val pinned: Boolean,
    val reminderAt: Long?,
    val colorArgb: Int?,
    val createdAt: Long,
    val updatedAt: Long
)
