package com.unistack.app.feature_schedule.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "agenda_events",
    indices = [Index("userId"), Index("startMillis"), Index("kind")]
)
data class AgendaEventEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val notes: String,
    val kind: String,
    val startMillis: Long,
    val endMillis: Long?,
    val allDay: Boolean,
    val location: String,
    val reminderMinutes: Int,
    val recurrence: String,
    val recurrenceEndEpochDay: Long?,
    val colorArgb: Int?,
    val createdAt: Long,
    val updatedAt: Long
)
