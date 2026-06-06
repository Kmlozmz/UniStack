package com.unistack.app.feature_expenses.domain

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object ExpenseDateUtils {
    private val inputFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))

    fun today(): LocalDate = LocalDate.now()

    fun parseInput(value: String): LocalDate? {
        return try {
            LocalDate.parse(value.trim(), inputFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatInput(date: LocalDate): String = date.format(inputFormatter)

    fun formatDisplay(dateMillis: Long): String {
        return fromMillis(dateMillis).format(displayFormatter)
    }

    fun toMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun fromMillis(value: Long): LocalDate {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun startOfWeek(today: LocalDate = today()): LocalDate {
        return today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    }

    fun isInCurrentWeek(dateMillis: Long, today: LocalDate = today()): Boolean {
        val date = fromMillis(dateMillis)
        val start = startOfWeek(today)
        val end = start.plusDays(6)
        return date in start..end
    }
}
