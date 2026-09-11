package com.unistack.app.feature_tasks.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.utils.NO_DATA
import com.unistack.app.feature_user.domain.DateFormatPreference
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.util.Locale
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

object TaskDateUtils {
    private val inputFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val timeInputFormatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("H:mm")
        .toFormatter(Locale.US)
    private val displayFormatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())
    private val time24Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.US)
    private val time12Formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

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

    fun formatTimeInput(time: LocalTime): String = formatTime(time, use24HourTime = true)

    fun formatTime(time: LocalTime, use24HourTime: Boolean = true): String {
        return if (use24HourTime) time.format(time24Formatter) else time.format(time12Formatter)
    }

    fun formatDate(date: LocalDate, dateFormat: DateFormatPreference = DateFormatPreference.DMY): String {
        return date.format(DateTimeFormatter.ofPattern(dateFormat.pattern))
    }

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

    fun dueText(
        dueDateMillis: Long,
        today: LocalDate = today(),
        dateFormat: DateFormatPreference = DateFormatPreference.DMY,
        use24HourTime: Boolean = true
    ): String {
        val dueDate = fromMillis(dueDateMillis)
        val time = timeFromMillis(dueDateMillis)
        val timeText = if (time == LocalTime.MIDNIGHT) "" else " ${formatTime(time, use24HourTime)}"
        val diff = ChronoUnit.DAYS.between(today, dueDate)
        // Fragmentos para ir dentro de una frase, en minuscula: quien los abra los levanta.
        return when (diff) {
            -1L -> Textos.get(R.string.due_overdue_yesterday)
            0L -> Textos.get(R.string.due_today)
            1L -> Textos.get(R.string.due_tomorrow)
            in Long.MIN_VALUE..-2L -> Textos.get(R.string.due_overdue_days_ago, -diff)
            in 2L..6L -> Textos.get(R.string.due_in_days, diff)
            else -> Textos.get(R.string.due_on_date, formatDate(dueDate, dateFormat))
        } + timeText
    }

    fun isToday(dueDateMillis: Long, today: LocalDate = today()): Boolean {
        return fromMillis(dueDateMillis) == today
    }

    fun estimatedTimeText(minutes: Int): String {
        if (minutes <= 0) return NO_DATA
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

/**
 * Formatea el texto de vencimiento de la tarea adaptado a las preferencias globales
 * de formato de fecha y reloj de 24 horas del usuario.
 */
@Composable
@ReadOnlyComposable
fun formatTaskDueText(dueDateMillis: Long): String {
    val a11y = LocalAccessibilityPreferences.current
    return TaskDateUtils.dueText(
        dueDateMillis = dueDateMillis,
        dateFormat = a11y.dateFormat,
        use24HourTime = a11y.use24HourTime
    )
}

/**
 * Formatea una hora respetando el reloj de 24 horas de Accesibilidad.
 */
@Composable
@ReadOnlyComposable
fun formatTaskTime(time: LocalTime): String {
    return TaskDateUtils.formatTime(time, LocalAccessibilityPreferences.current.use24HourTime)
}

/**
 * Formatea una fecha respetando el orden numérico (DMY, MDY, YMD) de Accesibilidad.
 */
@Composable
@ReadOnlyComposable
fun formatTaskDate(date: LocalDate): String {
    return TaskDateUtils.formatDate(date, LocalAccessibilityPreferences.current.dateFormat)
}
