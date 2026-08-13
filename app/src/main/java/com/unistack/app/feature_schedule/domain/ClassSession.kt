package com.unistack.app.feature_schedule.domain

import com.unistack.app.core.utils.DayLabels

/** Las dos mitades de [ClassSession.location], que se guarda como `"aula•profesor"`. */
data class SessionPlace(val room: String, val professor: String)

private fun formatMinuteOfDay(value: Int): String = "%02d:%02d".format(value / 60, value % 60)

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

    /**
     * Aula y profesor, sacados de [location].
     *
     * Vive aquí porque el formato lo define el propio modelo. La misma división estaba
     * repetida en Horario y en el formulario de materia, y ahora hace falta también en
     * Académico: tres copias de un `split` que solo el modelo sabe justificar.
     */
    val place: SessionPlace
        get() {
            val parts = location.split('•', limit = 2).map(String::trim)
            return SessionPlace(parts.getOrElse(0) { "" }, parts.getOrElse(1) { "" })
        }

    /** Los días y la hora en una línea: `«L X V · 08:00-10:00»`. Vacío si no hay días. */
    fun daysAndTimeLabel(): String {
        if (daysOfWeek.isEmpty()) return ""
        val days = daysOfWeek.sorted().joinToString(" ") { DayLabels.shortByIsoDay(it) }
        return "$days · ${formatMinuteOfDay(startMinute)}-${formatMinuteOfDay(endMinute)}"
    }

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
    // Sin días marcados de entrada. Venían lunes, miércoles y viernes preseleccionados,
    // que presupone un horario que la app no conoce: quien tenga clase martes y jueves
    // primero tiene que deseleccionar tres días antes de elegir los suyos, y quien no se
    // fije guarda una materia en unos días que nunca escogió. El formulario ya exige al
    // menos un día para guardar, así que el vacío no deja pasar nada roto.
    val daysOfWeek: Set<Int> = emptySet(),
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
