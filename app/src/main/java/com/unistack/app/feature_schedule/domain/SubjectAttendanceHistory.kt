package com.unistack.app.feature_schedule.domain

import java.time.LocalDate

/** Una clase concreta de una materia en un día concreto, con lo que se marcó ese día. */
data class AttendanceHistoryEntry(
    val date: LocalDate,
    val session: ClassSession,
    val status: ClassAttendanceStatus
)

/**
 * El historial de asistencia de una materia, sin inventarse clases.
 *
 * La lista no sale de lo que ocurrió sino de la **regla de repetición**: si una materia es los
 * martes, cualquier martes del calendario es candidato. Eso obliga a acotar hasta dónde se
 * mira, y el corte estaba puesto a ojo en 120 días hacia atrás.
 *
 * Ahí estaba la deshonestidad. Una materia creada esta semana aparecía con cuatro meses de
 * clases «pendientes» que nunca existieron, y la app no tenía forma de distinguir **una clase
 * anterior al periodo** de **una que se olvidó marcar**: las dos se veían igual. Con las fechas
 * del periodo, la ventana se recorta a lo que de verdad se ha cursado, y lo que queda
 * pendiente lo está porque lo está.
 *
 * Sin periodo configurado —quien viene de una versión anterior— se mantiene la ventana de
 * antes: es lo único que se puede hacer sin el dato, y no empeora nada.
 */
object SubjectAttendanceHistory {

    /** Hasta dónde se mira hacia atrás cuando no hay periodo que acote. */
    const val LOOKBACK_DAYS = 120L

    /** Cuántos días por delante se enseñan, para saber qué viene. */
    const val LOOKAHEAD_DAYS = 30L

    /** Cuántas clases pasadas caben en el historial. */
    const val RECENT_LIMIT = 22

    /** Cuántas clases futuras se anuncian. */
    const val UPCOMING_LIMIT = 2

    /**
     * @param termStart primer día del periodo, o nulo si no hay periodo configurado.
     * @param termEnd último día del periodo, o nulo si no tiene fin previsto.
     */
    fun build(
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>,
        today: LocalDate,
        termStart: LocalDate? = null,
        termEnd: LocalDate? = null
    ): List<AttendanceHistoryEntry> {
        val desde = maxOf(today.minusDays(LOOKBACK_DAYS), termStart ?: LocalDate.MIN)
        val hasta = minOf(today.plusDays(LOOKAHEAD_DAYS), termEnd ?: LocalDate.MAX)
        // Un periodo que todavía no ha empezado no tiene ni una clase que contar.
        if (desde.isAfter(hasta)) return emptyList()

        val porClave = occurrences.associateBy { it.sessionId to it.dateEpochDay }
        val todas = generateSequence(desde) { dia ->
            dia.plusDays(1).takeIf { !it.isAfter(hasta) }
        }.flatMap { fecha ->
            sessions
                .filter { it.occursOn(fecha.toEpochDay(), fecha.dayOfWeek.value) }
                .map { sesion ->
                    AttendanceHistoryEntry(
                        date = fecha,
                        session = sesion,
                        status = porClave[sesion.id to fecha.toEpochDay()]?.status
                            ?: ClassAttendanceStatus.PENDING
                    )
                }
        }.toList()

        val futuras = todas.filter { it.date.isAfter(today) }
            .sortedBy(AttendanceHistoryEntry::date)
            .take(UPCOMING_LIMIT)
        val recientes = todas.filter { !it.date.isAfter(today) }
            .sortedWith(masRecientePrimero)
            .take(RECENT_LIMIT)
        return (futuras + recientes).sortedWith(masRecientePrimero)
    }

    private val masRecientePrimero =
        compareByDescending<AttendanceHistoryEntry> { it.date }
            .thenByDescending { it.session.startMinute }
}
