package com.unistack.app.feature_schedule.domain

import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AgendaEventTest {
    private val zone = ZoneId.systemDefault()
    private val start = LocalDate.of(2026, 6, 22)

    @Test
    fun weeklyEventOnlyOccursOnMatchingWeekday() {
        val event = event(AgendaRecurrence.WEEKLY)

        assertTrue(event.occursOn(start))
        assertTrue(event.occursOn(start.plusWeeks(1)))
        assertFalse(event.occursOn(start.plusDays(1)))
    }

    @Test
    fun nonRecurringEventOnlyOccursOnItsDate() {
        val event = event(AgendaRecurrence.NONE)

        assertTrue(event.occursOn(start))
        assertFalse(event.occursOn(start.plusDays(1)))
    }

    private fun event(recurrence: AgendaRecurrence) = AgendaEvent(
        id = "event-1",
        title = "Reunión",
        notes = "",
        kind = AgendaEventKind.MEETING,
        startMillis = start.atTime(10, 0).atZone(zone).toInstant().toEpochMilli(),
        endMillis = start.atTime(11, 0).atZone(zone).toInstant().toEpochMilli(),
        allDay = false,
        location = "Biblioteca",
        reminderMinutes = 15,
        recurrence = recurrence,
        recurrenceEndEpochDay = null,
        colorArgb = null,
        createdAt = 1L,
        updatedAt = 1L
    )
}
