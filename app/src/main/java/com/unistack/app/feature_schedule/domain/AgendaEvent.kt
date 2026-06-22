package com.unistack.app.feature_schedule.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class AgendaEventKind {
    PERSONAL,
    MEETING,
    REMINDER,
    CUSTOM
}

enum class AgendaRecurrence {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}

data class AgendaEvent(
    val id: String,
    val title: String,
    val notes: String,
    val kind: AgendaEventKind,
    val startMillis: Long,
    val endMillis: Long?,
    val allDay: Boolean,
    val location: String,
    val reminderMinutes: Int,
    val recurrence: AgendaRecurrence,
    val recurrenceEndEpochDay: Long?,
    val colorArgb: Int?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isValid: Boolean
        get() = title.isNotBlank() &&
            startMillis > 0L &&
            (endMillis == null || endMillis > startMillis) &&
            reminderMinutes in 0..10_080 &&
            (recurrenceEndEpochDay == null || recurrenceEndEpochDay >= startDate().toEpochDay())

    fun startDate(zoneId: ZoneId = ZoneId.systemDefault()): LocalDate =
        Instant.ofEpochMilli(startMillis).atZone(zoneId).toLocalDate()

    fun occursOn(date: LocalDate, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val start = startDate(zoneId)
        if (date.isBefore(start)) return false
        if (recurrenceEndEpochDay?.let { date.toEpochDay() > it } == true) return false
        return when (recurrence) {
            AgendaRecurrence.NONE -> date == start
            AgendaRecurrence.DAILY -> true
            AgendaRecurrence.WEEKLY -> date.dayOfWeek == start.dayOfWeek
            AgendaRecurrence.MONTHLY -> date.dayOfMonth == start.dayOfMonth
        }
    }
}
