package com.unistack.app.feature_schedule.domain

import java.time.DayOfWeek
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

    /** Cuántas sin marcar caben en «ponerse al día» de una vez. */
    const val CATCH_UP_LIMIT = 12

    /**
     * @param termStart primer día del periodo, o nulo si no hay periodo configurado.
     * @param termEnd último día del periodo, o nulo si no tiene fin previsto.
     * @param breaks tramos en que la universidad estuvo cerrada; sus días no cuentan.
     */
    fun build(
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>,
        today: LocalDate,
        termStart: LocalDate? = null,
        termEnd: LocalDate? = null,
        breaks: List<ClosedRange<LocalDate>> = emptyList()
    ): List<AttendanceHistoryEntry> {
        val desde = maxOf(today.minusDays(LOOKBACK_DAYS), termStart ?: LocalDate.MIN)
        val hasta = minOf(today.plusDays(LOOKAHEAD_DAYS), termEnd ?: LocalDate.MAX)
        // Un periodo que todavía no ha empezado no tiene ni una clase que contar.
        if (desde.isAfter(hasta)) return emptyList()

        val porClave = occurrences.associateBy { it.sessionId to it.dateEpochDay }
        val todas = generateSequence(desde) { dia ->
            dia.plusDays(1).takeIf { !it.isAfter(hasta) }
        }.filter { fecha ->
            /*
             * Un festivo no es una clase que no diste.
             *
             * Sin esto la app apunta pendientes los días que la universidad estaba cerrada, y
             * al no marcarlos se leen igual que los que se te olvidaron. Peor aún con el tope
             * de faltas delante: contaría hacia perder la materia algo que nunca ocurrió.
             */
            breaks.none { fecha in it }
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

    /**
     * Lo que todavía no ha ocurrido, de lo más próximo en adelante.
     *
     * Sale aparte del historial porque **no está pendiente de nada**: una clase del mes que
     * viene no es una que se te olvidó marcar, y la lista las enseñaba con la misma etiqueta.
     */
    fun upcoming(entries: List<AttendanceHistoryEntry>, today: LocalDate): List<AttendanceHistoryEntry> =
        entries.filter { it.date.isAfter(today) }
            .sortedWith(compareBy({ it.date }, { it.session.startMinute }))

    /**
     * Lo que pasó y nadie marcó, de lo más reciente hacia atrás.
     *
     * Existe porque los avisos se ignoran: una semana de exámenes y aparecen seis clases sin
     * marcar. Entrar en cada una es lo que hace que la gente **abandone el registro entero**,
     * así que hay que poder resolverlas juntas.
     *
     * Se corta en [limit] a propósito: una lista de cuarenta pendientes no se pone al día, se
     * cierra. Con las más recientes delante, lo que se recuerda se marca y lo viejo se queda
     * donde estaba, que ya no se acuerda nadie.
     */
    fun pendingToCatchUp(
        entries: List<AttendanceHistoryEntry>,
        today: LocalDate,
        limit: Int = CATCH_UP_LIMIT
    ): List<AttendanceHistoryEntry> = entries
        .filter { it.status == ClassAttendanceStatus.PENDING && !it.date.isAfter(today) }
        .sortedWith(masRecientePrimero)
        .take(limit)

    /**
     * Lo ya ocurrido, en semanas y de la más reciente a la más antigua.
     *
     * Un semestre se piensa por semanas —«la semana pasada falté una»— y no por días sueltos,
     * así que agrupar así hace que la lista se lea sin contar fechas. Las semanas sin ninguna
     * clase no aparecen: una semana de receso deja hueco, que es lo que fue.
     */
    fun byWeek(entries: List<AttendanceHistoryEntry>, today: LocalDate): List<AttendanceWeek> =
        entries.filter { !it.date.isAfter(today) }
            .groupBy { it.date.with(DayOfWeek.MONDAY) }
            .map { (lunes, dentro) ->
                AttendanceWeek(
                    start = lunes,
                    entries = dentro.sortedWith(compareBy({ it.date }, { it.session.startMinute }))
                )
            }
            .sortedByDescending { it.start }

    /**
     * Las cuentas que abren el historial, en un solo sitio.
     *
     * Se calculaban en la pantalla, y por eso el porcentaje podía decir «100 %» apoyado en una
     * sola clase sin que nada obligara a contarlo. Aquí el número de clases decididas viaja
     * junto al porcentaje, así que quien lo pinta no puede enseñar uno sin el otro.
     */
    fun summarize(entries: List<AttendanceHistoryEntry>, absenceLimit: Int? = null): AttendanceSummary {
        val pasadas = entries.filter { it.status != ClassAttendanceStatus.PENDING }
        val asistidas = pasadas.count { it.status == ClassAttendanceStatus.ATTENDED }
        val faltas = pasadas.count { it.status == ClassAttendanceStatus.ABSENT }
        val decididas = asistidas + faltas
        return AttendanceSummary(
            attended = asistidas,
            absent = faltas,
            // Canceladas y reprogramadas no las diste, pero tampoco faltaste: fuera del reparto.
            decided = decididas,
            rate = if (decididas == 0) null else Math.round(asistidas * 100f / decididas),
            absenceLimit = absenceLimit,
            streak = rachaDesdeElFinal(entries)
        )
    }

    /**
     * Cuántas clases seguidas llevas sin faltar, contando hacia atrás desde la última dada.
     *
     * Una cancelada no rompe la racha ni la alarga: ese día no hubo nada a lo que faltar. Una
     * pendiente sí la corta, porque no sabemos qué pasó y dar por buena una clase sin marcar
     * sería inventarse el dato.
     */
    private fun rachaDesdeElFinal(entries: List<AttendanceHistoryEntry>): Int {
        var cuenta = 0
        for (entrada in entries.sortedWith(masRecientePrimero)) {
            when (entrada.status) {
                ClassAttendanceStatus.ATTENDED -> cuenta++
                ClassAttendanceStatus.CANCELLED,
                ClassAttendanceStatus.RESCHEDULED -> Unit
                ClassAttendanceStatus.ABSENT -> return cuenta
                // Las futuras todavía no cuentan; las pasadas sin marcar cortan.
                ClassAttendanceStatus.PENDING -> if (cuenta > 0) return cuenta
            }
        }
        return cuenta
    }
}

/**
 * Una semana del historial, con las clases que cayeron dentro.
 *
 * [start] es siempre el lunes, para que dos materias con horarios distintos agrupen igual.
 */
data class AttendanceWeek(
    val start: LocalDate,
    val entries: List<AttendanceHistoryEntry>
) {
    val end: LocalDate get() = start.plusDays(6)

    /** Si la semana en curso es esta. */
    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)
}

/**
 * Lo que se enseña arriba del historial.
 *
 * [rate] es nulo cuando no hay ni una clase decidida: devolver cero decía «faltaste a todo»
 * cuando lo cierto es que no hay un solo dato. [remainingAbsences] es nulo mientras la materia
 * no tenga tope, porque prometer «te quedan N» sin saber N es peor que no decir nada.
 */
data class AttendanceSummary(
    val attended: Int,
    val absent: Int,
    val decided: Int,
    val rate: Int?,
    val absenceLimit: Int?,
    val streak: Int
) {
    val remainingAbsences: Int?
        get() = absenceLimit?.let { (it - absent).coerceAtLeast(0) }

    /** Si ya no cabe ni una falta más. */
    val atLimit: Boolean get() = remainingAbsences == 0

    /** Si queda una sola: el momento en que este dato cambia lo que haces. */
    val oneLeft: Boolean get() = remainingAbsences == 1

    /**
     * Si el porcentaje se apoya en tan pocas clases que decirlo a secas engaña.
     *
     * «100 %» sobre una clase es ruido con formato de dato.
     */
    val tooFewToTrust: Boolean get() = decided in 1..3
}
