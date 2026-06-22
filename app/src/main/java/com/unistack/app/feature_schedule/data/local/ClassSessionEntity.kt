package com.unistack.app.feature_schedule.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "class_sessions",
    indices = [Index("userId"), Index("subjectId")]
)
data class ClassSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val subjectId: String,
    val daysOfWeekCsv: String,
    val startMinute: Int,
    val endMinute: Int,
    val location: String,
    val reminderMinutes: Int,
    val createdAt: Long,
    val updatedAt: Long,
    val repeatEveryWeeks: Int,
    val recurrenceStartEpochDay: Long
)
