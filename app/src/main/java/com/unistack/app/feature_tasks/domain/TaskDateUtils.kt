package com.unistack.app.feature_tasks.domain

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

object TaskDateUtils {
    private val inputFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeInputFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("H:mm")
        .toFormatter(Locale.US)
    private val displayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))
    private val timeDisplayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)

    fun today(): LocalDate = LocalDate.now()

    fun parseInput(value: String): LocalDate? {
        return try {
            LocalDate.parse(value.trim(), inputFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatInput(date: LocalDate): String = date.format(inputFormatter)

    fun parseTimeInput(value: String): LocalTime? {
        if (value.isBlank()) return null
        return try {
            LocalTime.parse(value.trim(), timeInputFormatter)
        } catch (_: DateTimeParseException) {
            null
        }
    }

    fun formatTimeInput(time: LocalTime): String = time.format(timeDisplayFormatter)

    fun toMillis(date: LocalDate, time: LocalTime? = null): Long {
        return date.atTime(time ?: LocalTime.MIDNIGHT)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }

    fun fromMillis(value: Long): LocalDate {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun timeFromMillis(value: Long): LocalTime {
        return Instant.ofEpochMilli(value)
            .atZone(ZoneId.systemDefault())
            .toLocalTime()
            .withSecond(0)
            .withNano(0)
    }

    fun hasExplicitTime(value: Long): Boolean = timeFromMillis(value) != LocalTime.MIDNIGHT

    fun dueText(dueDateMillis: Long, today: LocalDate = today()): String {
        val dueDate = fromMillis(dueDateMillis)
        val time = timeFromMillis(dueDateMillis)
        val timeText = if (time == LocalTime.MIDNIGHT) "" else " ${formatTimeInput(time)}"
        val diff = ChronoUnit.DAYS.between(today, dueDate)
        return when (diff) {
            -1L -> "venció ayer$timeText"
            0L -> "vence hoy$timeText"
            1L -> "vence mañana$timeText"
            in Long.MIN_VALUE..-2L -> "venció hace ${-diff} días"
            in 2L..6L -> "vence en $diff días$timeText"
            else -> "vence ${dueDate.format(displayFormatter)}$timeText"
        }
    }

    fun isToday(dueDateMillis: Long, today: LocalDate = today()): Boolean {
        return fromMillis(dueDateMillis) == today
    }

    fun estimatedTimeText(minutes: Int): String {
        if (minutes <= 0) return "--"
        if (minutes < 60) return "$minutes min"

        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return if (remainingMinutes == 0) {
            "$hours h"
        } else {
            "$hours h $remainingMinutes min"
        }
    }
}
