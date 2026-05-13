package com.unistack.app.feature_tasks.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskDateUtilsTest {

    @Test
    fun `parse and format use iso local date`() {
        val date = LocalDate.of(2026, 5, 12)

        assertEquals(date, TaskDateUtils.parseInput(" 2026-05-12 "))
        assertEquals("2026-05-12", TaskDateUtils.formatInput(date))
        assertNull(TaskDateUtils.parseInput("12/05/2026"))
    }

    @Test
    fun `millis round trip preserves local date`() {
        val date = LocalDate.of(2026, 5, 12)

        assertEquals(date, TaskDateUtils.fromMillis(TaskDateUtils.toMillis(date)))
    }

    @Test
    fun `due text describes near dates`() {
        val today = LocalDate.of(2026, 5, 12)

        assertEquals("venció ayer", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.minusDays(1)), today))
        assertEquals("vence hoy", TaskDateUtils.dueText(TaskDateUtils.toMillis(today), today))
        assertEquals("vence mañana", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.plusDays(1)), today))
        assertEquals("vence en 3 días", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.plusDays(3)), today))
        assertEquals("venció hace 2 días", TaskDateUtils.dueText(TaskDateUtils.toMillis(today.minusDays(2)), today))
    }

    @Test
    fun `estimated time text formats minutes and hours`() {
        assertEquals("--", TaskDateUtils.estimatedTimeText(0))
        assertEquals("45 min", TaskDateUtils.estimatedTimeText(45))
        assertEquals("2 h", TaskDateUtils.estimatedTimeText(120))
        assertEquals("2 h 15 min", TaskDateUtils.estimatedTimeText(135))
    }

    @Test
    fun `is today compares against supplied date`() {
        val today = LocalDate.of(2026, 5, 12)

        assertTrue(TaskDateUtils.isToday(TaskDateUtils.toMillis(today), today))
        assertFalse(TaskDateUtils.isToday(TaskDateUtils.toMillis(today.plusDays(1)), today))
    }
}
