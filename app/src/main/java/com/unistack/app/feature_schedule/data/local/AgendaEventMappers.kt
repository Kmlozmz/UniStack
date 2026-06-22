package com.unistack.app.feature_schedule.data.local

import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_schedule.domain.AgendaRecurrence

fun AgendaEventEntity.toDomain() = AgendaEvent(
    id = id,
    title = title,
    notes = notes,
    kind = runCatching { AgendaEventKind.valueOf(kind) }.getOrDefault(AgendaEventKind.CUSTOM),
    startMillis = startMillis,
    endMillis = endMillis,
    allDay = allDay,
    location = location,
    reminderMinutes = reminderMinutes,
    recurrence = runCatching { AgendaRecurrence.valueOf(recurrence) }.getOrDefault(AgendaRecurrence.NONE),
    recurrenceEndEpochDay = recurrenceEndEpochDay,
    colorArgb = colorArgb,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun AgendaEvent.toEntity(userId: String) = AgendaEventEntity(
    id = id,
    userId = userId,
    title = title,
    notes = notes,
    kind = kind.name,
    startMillis = startMillis,
    endMillis = endMillis,
    allDay = allDay,
    location = location,
    reminderMinutes = reminderMinutes,
    recurrence = recurrence.name,
    recurrenceEndEpochDay = recurrenceEndEpochDay,
    colorArgb = colorArgb,
    createdAt = createdAt,
    updatedAt = updatedAt
)
