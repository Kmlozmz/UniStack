package com.unistack.app.feature_schedule.domain

data class ClassSession(
    val id: String,
    val subjectId: String,
    val daysOfWeek: Set<Int>,
    val startMinute: Int,
    val endMinute: Int,
    val location: String = "",
    val reminderMinutes: Int = 15,
    val createdAt: Long,
    val updatedAt: Long,
    val repeatEveryWeeks: Int = 1,
    val recurrenceStartEpochDay: Long = 0L
) {
    val isValid: Boolean
        get() = subjectId.isNotBlank() &&
            daysOfWeek.isNotEmpty() &&
            daysOfWeek.all { it in 1..7 } &&
            startMinute in 0 until 24 * 60 &&
            endMinute in 1..24 * 60 &&
            endMinute > startMinute &&
            repeatEveryWeeks in 1..12

    fun occursOn(dateEpochDay: Long, dayOfWeekValue: Int): Boolean {
        if (dayOfWeekValue !in daysOfWeek) return false
        if (repeatEveryWeeks == 1 || recurrenceStartEpochDay <= 0L) return true
        val anchorWeek = recurrenceStartEpochDay - ((recurrenceStartEpochDay + 3L) % 7L)
        val targetWeek = dateEpochDay - ((dateEpochDay + 3L) % 7L)
        val weeks = (targetWeek - anchorWeek) / 7L
        return weeks >= 0L && weeks % repeatEveryWeeks == 0L
    }
}

data class SubjectScheduleDraft(
    val enabled: Boolean = true,
    val professor: String = "",
    val daysOfWeek: Set<Int> = setOf(1, 3, 5),
    val startMinute: Int = 8 * 60,
    val endMinute: Int = 10 * 60,
    val room: String = "",
    val reminderMinutes: Int = 15,
    val repeatEveryWeeks: Int = 1,
    val recurrenceStartEpochDay: Long = 0L
) {
    val isValid: Boolean
        get() = !enabled || (
            daysOfWeek.isNotEmpty() &&
                daysOfWeek.all { it in 1..7 } &&
                startMinute in 0 until 24 * 60 &&
                endMinute in 1..24 * 60 &&
                endMinute > startMinute &&
                reminderMinutes in 0..24 * 60 &&
                repeatEveryWeeks in 1..12
            )

    val location: String
        get() = "${room.trim()}\u2022${professor.trim()}"
}
