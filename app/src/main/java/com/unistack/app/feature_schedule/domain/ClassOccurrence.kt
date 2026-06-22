package com.unistack.app.feature_schedule.domain

enum class ClassAttendanceStatus {
    PENDING,
    ATTENDED,
    ABSENT,
    CANCELLED,
    RESCHEDULED
}

enum class ClassModality {
    IN_PERSON,
    VIRTUAL,
    HYBRID
}

enum class ClassAbsenceReason {
    HEALTH,
    TRANSPORT,
    PERSONAL,
    ACADEMIC_CONFLICT,
    OTHER
}

data class ClassOccurrence(
    val id: String,
    val sessionId: String,
    val dateEpochDay: Long,
    val status: ClassAttendanceStatus = ClassAttendanceStatus.PENDING,
    val modality: ClassModality = ClassModality.IN_PERSON,
    val absenceReason: ClassAbsenceReason? = null,
    val note: String = "",
    val overrideStartMinute: Int? = null,
    val overrideEndMinute: Int? = null,
    val overrideLocation: String? = null,
    val updatedAt: Long
) {
    val isValid: Boolean
        get() = sessionId.isNotBlank() &&
            (overrideStartMinute == null || overrideStartMinute in 0 until 24 * 60) &&
            (overrideEndMinute == null || overrideEndMinute in 1..24 * 60) &&
            (overrideStartMinute == null || overrideEndMinute == null || overrideEndMinute > overrideStartMinute)

    companion object {
        fun idFor(sessionId: String, dateEpochDay: Long): String = "$sessionId:$dateEpochDay"
    }
}
