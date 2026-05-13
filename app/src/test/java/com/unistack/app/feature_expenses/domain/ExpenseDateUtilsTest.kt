package com.unistack.app.feature_expenses.domain

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpenseDateUtilsTest {

    @Test
    fun `parse and format use iso local date`() {
        val date = LocalDate.of(2026, 5, 12)

        assertEquals(date, ExpenseDateUtils.parseInput(" 2026-05-12 "))
        assertEquals("2026-05-12", ExpenseDateUtils.formatInput(date))
        assertNull(ExpenseDateUtils.parseInput("mayo 12"))
    }

    @Test
    fun `millis round trip preserves local date`() {
        val date = LocalDate.of(2026, 5, 12)

        assertEquals(date, ExpenseDateUtils.fromMillis(ExpenseDateUtils.toMillis(date)))
    }

    @Test
    fun `start of week is monday`() {
        assertEquals(
            LocalDate.of(2026, 5, 11),
            ExpenseDateUtils.startOfWeek(LocalDate.of(2026, 5, 12))
        )
        assertEquals(
            LocalDate.of(2026, 5, 11),
            ExpenseDateUtils.startOfWeek(LocalDate.of(2026, 5, 11))
        )
    }

    @Test
    fun `current week includes monday through sunday only`() {
        val today = LocalDate.of(2026, 5, 12)

        assertTrue(ExpenseDateUtils.isInCurrentWeek(ExpenseDateUtils.toMillis(LocalDate.of(2026, 5, 11)), today))
        assertTrue(ExpenseDateUtils.isInCurrentWeek(ExpenseDateUtils.toMillis(LocalDate.of(2026, 5, 17)), today))
        assertFalse(ExpenseDateUtils.isInCurrentWeek(ExpenseDateUtils.toMillis(LocalDate.of(2026, 5, 10)), today))
        assertFalse(ExpenseDateUtils.isInCurrentWeek(ExpenseDateUtils.toMillis(LocalDate.of(2026, 5, 18)), today))
    }
}
