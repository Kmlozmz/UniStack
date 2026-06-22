package com.unistack.app.feature_schedule.data.local

import com.unistack.app.feature_schedule.domain.ClassSession

fun ClassSessionEntity.toDomain() = ClassSession(
    id = id,
    subjectId = subjectId,
    daysOfWeek = daysOfWeekCsv.split(',')
        .mapNotNull(String::toIntOrNull)
        .filter { it in 1..7 }
        .toSet(),
    startMinute = startMinute,
    endMinute = endMinute,
    location = location,
    reminderMinutes = reminderMinutes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    repeatEveryWeeks = repeatEveryWeeks,
    recurrenceStartEpochDay = recurrenceStartEpochDay
)

fun ClassSession.toEntity(userId: String) = ClassSessionEntity(
    id = id,
    userId = userId,
    subjectId = subjectId,
    daysOfWeekCsv = daysOfWeek.sorted().joinToString(","),
    startMinute = startMinute,
    endMinute = endMinute,
    location = location,
    reminderMinutes = reminderMinutes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    repeatEveryWeeks = repeatEveryWeeks,
    recurrenceStartEpochDay = recurrenceStartEpochDay
)
