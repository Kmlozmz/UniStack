package com.unistack.app.feature_rooms.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** «hoy», «mañana», «en 3 días», «hace 2 días». */
@Composable
fun relDay(days: Long): String = when {
    days == 0L -> stringResource(R.string.rooms_today)
    days == 1L -> stringResource(R.string.rooms_tomorrow)
    days == -1L -> stringResource(R.string.rooms_yesterday)
    days < 0 -> stringResource(R.string.rooms_days_ago, (-days).toInt())
    else -> stringResource(R.string.rooms_in_days, days.toInt())
}

/** «27 sep». */
fun shortDate(epochDay: Long, locale: Locale = Locale.getDefault()): String {
    val d = LocalDate.ofEpochDay(epochDay)
    return "${d.dayOfMonth} ${d.month.getDisplayName(TextStyle.SHORT, locale).trimEnd('.').lowercase(locale)}"
}

@Composable
@ReadOnlyComposable
fun subjectColor(subject: Subject?): Color = subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary

fun gradeText(g: Double): String = String.format(Locale.getDefault(), "%.1f", g)
