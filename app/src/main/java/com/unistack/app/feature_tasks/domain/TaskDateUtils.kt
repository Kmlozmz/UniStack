package com.unistack.app.feature_tasks.domain

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale

object TaskDateUtils {
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

    fun toMillis(date: LocalDate): Long {
        return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }

    fun fromMillis(value: Long): LocalDate {
        return Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault()).toLocalDate()
    }

    fun dueText(dueDateMillis: Long, today: LocalDate = today()): String {
        val dueDate = fromMillis(dueDateMillis)
        val diff = ChronoUnit.DAYS.between(today, dueDate)
        return when (diff) {
            -1L -> "venció ayer"
            0L -> "vence hoy"
            1L -> "vence mañana"
            in Long.MIN_VALUE..-2L -> "venció hace ${-diff} días"
            in 2L..6L -> "vence en $diff días"
            else -> "vence ${dueDate.format(displayFormatter)}"
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
