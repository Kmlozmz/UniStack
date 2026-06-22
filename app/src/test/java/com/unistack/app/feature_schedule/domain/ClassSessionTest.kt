package com.unistack.app.feature_schedule.domain

import java.time.LocalDate
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ClassSessionTest {

    @Test
    fun validSessionRequiresSubjectDaysAndOrderedTimes() {
        val valid = session()

        assertTrue(valid.isValid)
        assertFalse(valid.copy(subjectId = "").isValid)
        assertFalse(valid.copy(daysOfWeek = emptySet()).isValid)
        assertFalse(valid.copy(daysOfWeek = setOf(0)).isValid)
        assertFalse(valid.copy(endMinute = valid.startMinute).isValid)
        assertFalse(valid.copy(repeatEveryWeeks = 0).isValid)
    }

    @Test
    fun customRecurrenceOnlyOccursOnMatchingWeeks() {
        val anchor = LocalDate.of(2026, 6, 1)
        val session = session().copy(
            daysOfWeek = setOf(1),
            repeatEveryWeeks = 2,
            recurrenceStartEpochDay = anchor.toEpochDay()
        )

        assertTrue(session.occursOn(anchor.toEpochDay(), anchor.dayOfWeek.value))
        assertFalse(session.occursOn(anchor.plusWeeks(1).toEpochDay(), anchor.dayOfWeek.value))
        assertTrue(session.occursOn(anchor.plusWeeks(2).toEpochDay(), anchor.dayOfWeek.value))
        assertFalse(session.occursOn(anchor.plusDays(1).toEpochDay(), anchor.plusDays(1).dayOfWeek.value))
    }

    private fun session() = ClassSession(
        id = "class-1",
        subjectId = "subject-1",
        daysOfWeek = setOf(1, 3, 5),
        startMinute = 8 * 60,
        endMinute = 9 * 60 + 30,
        location = "Aula 204",
        reminderMinutes = 15,
        createdAt = 1L,
        updatedAt = 1L
    )
}
