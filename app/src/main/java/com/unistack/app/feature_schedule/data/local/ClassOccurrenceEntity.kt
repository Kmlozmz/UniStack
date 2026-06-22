package com.unistack.app.feature_schedule.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_occurrences",
    indices = [
        Index("userId"),
        Index("sessionId"),
        Index("dateEpochDay")
    ]
)
data class ClassOccurrenceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val sessionId: String,
    val dateEpochDay: Long,
    val status: String,
    val modality: String,
    val absenceReason: String?,
    val note: String,
    val overrideStartMinute: Int?,
    val overrideEndMinute: Int?,
    val overrideLocation: String?,
    val updatedAt: Long
)
