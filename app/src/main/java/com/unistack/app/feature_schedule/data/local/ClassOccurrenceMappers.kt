package com.unistack.app.feature_schedule.data.local

import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassOccurrence

fun ClassOccurrenceEntity.toDomain() = ClassOccurrence(
    id = id,
    sessionId = sessionId,
    dateEpochDay = dateEpochDay,
    status = enumValueOrDefault(status, ClassAttendanceStatus.PENDING),
    modality = enumValueOrDefault(modality, ClassModality.IN_PERSON),
    absenceReason = absenceReason?.let { enumValueOrNull<ClassAbsenceReason>(it) },
    note = note,
    overrideStartMinute = overrideStartMinute,
    overrideEndMinute = overrideEndMinute,
    overrideLocation = overrideLocation,
    updatedAt = updatedAt
)

fun ClassOccurrence.toEntity(userId: String) = ClassOccurrenceEntity(
    id = id,
    userId = userId,
    sessionId = sessionId,
    dateEpochDay = dateEpochDay,
    status = status.name,
    modality = modality.name,
    absenceReason = absenceReason?.name,
    note = note,
    overrideStartMinute = overrideStartMinute,
    overrideEndMinute = overrideEndMinute,
    overrideLocation = overrideLocation,
    updatedAt = updatedAt
)

private inline fun <reified T : Enum<T>> enumValueOrNull(value: String): T? =
    enumValues<T>().firstOrNull { it.name == value }

private inline fun <reified T : Enum<T>> enumValueOrDefault(value: String, default: T): T =
    enumValueOrNull<T>(value) ?: default
