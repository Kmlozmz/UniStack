package com.unistack.app.feature_terms.domain

import java.time.LocalDate

/**
 * Cada cuánto empieza un ciclo en tu universidad.
 *
 * No decide nada por sí solo —las fechas las pone el usuario— pero sirve para sugerir una
 * duración al crear el periodo y para nombrarlo en la interfaz. Son las cinco formas que se
 * usan de verdad; no hay una opción «otro» porque cualquier calendario cabe en alguna de ellas
 * en cuanto se le ponen fechas propias.
 */
enum class AcademicTermType(val label: String, val detail: String, val weeks: Int) {
    SEMESTER("Semestral", "2 al año · unas 16 semanas", 16),
    TRIMESTER("Trimestral", "3 al año · unas 11 semanas", 11),
    QUARTER("Cuatrimestral", "3 al año · unas 14 semanas", 14),
    ANNUAL("Anual", "1 al año", 36),
    BLOCKS("Por bloques", "Módulos cortos seguidos", 8)
}

/** Si el periodo es el que se está cursando o ya se cerró. */
enum class AcademicTermStatus {
    ACTIVE,
    CLOSED
}

/**
 * Un periodo académico: el semestre, con fechas.
 *
 * La app no sabía cuándo empieza ni acaba un ciclo, y eso impedía tres cosas: distinguir una
 * clase anterior al periodo de una que se olvidó marcar, cerrar un semestre, y guardar
 * histórico. Las tres colgaban del mismo dato que faltaba.
 *
 * ## Las fechas
 *
 * [startEpochDay] es **obligatoria**: es exactamente lo que faltaba, y sin ella la asistencia
 * no puede ser honesta. Se pregunta y no se deduce, siguiendo lo que ya dice `Subject`: no hay
 * forma de saberlo sin preguntar, así que no se supone.
 *
 * [plannedEndEpochDay] es una **previsión**, no un compromiso. En agosto nadie sabe el día
 * exacto en que acaba, así que solo sirve para avisar cuando llega. El periodo no se cierra
 * solo ese día.
 *
 * [closedEpochDay] es la fecha real, y solo existe cuando alguien cierra el periodo a mano.
 * Mientras es nulo, el periodo está en curso y «hasta hoy» es su final.
 *
 * ## Cerrar
 *
 * Cerrar es irreversible en un sentido concreto: el periodo **nunca vuelve a ser el activo**.
 * Pero sus datos siguen siendo editables desde el histórico, porque las notas llegan tarde y
 * los profesores corrigen.
 */
data class AcademicTerm(
    val id: String,
    val userId: String,
    val name: String,
    val type: AcademicTermType,
    val startEpochDay: Long,
    val plannedEndEpochDay: Long?,
    val closedEpochDay: Long?,
    val status: AcademicTermStatus,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isActive: Boolean get() = status == AcademicTermStatus.ACTIVE

    val start: LocalDate get() = LocalDate.ofEpochDay(startEpochDay)

    val plannedEnd: LocalDate? get() = plannedEndEpochDay?.let(LocalDate::ofEpochDay)

    /**
     * Hasta dónde llega el periodo hoy.
     *
     * Para uno cerrado es el día en que se cerró. Para el activo es hoy, no la previsión: si
     * el fin previsto ya pasó y todavía no se ha cerrado, el periodo sigue corriendo, y las
     * clases de esta semana pertenecen a él.
     */
    fun endFor(today: LocalDate): LocalDate = when {
        closedEpochDay != null -> LocalDate.ofEpochDay(closedEpochDay)
        else -> today
    }

    /** Si [date] cae dentro del periodo, contando desde el inicio hasta [endFor]. */
    fun contains(date: LocalDate, today: LocalDate): Boolean =
        !date.isBefore(start) && !date.isAfter(endFor(today))

    /** Si el fin previsto ya llegó y nadie ha cerrado todavía. */
    fun isPastPlannedEnd(today: LocalDate): Boolean =
        isActive && plannedEnd?.let { !today.isBefore(it) } == true

    val isValid: Boolean
        get() = id.isNotBlank() &&
            name.isNotBlank() &&
            // Un periodo que acaba antes de empezar no es un periodo.
            (plannedEndEpochDay == null || plannedEndEpochDay > startEpochDay) &&
            (closedEpochDay == null || closedEpochDay >= startEpochDay) &&
            // Cerrado sin fecha de cierre, o activo con ella, son estados que no existen.
            (closedEpochDay != null) == (status == AcademicTermStatus.CLOSED)

    companion object {
        /**
         * El nombre que se propone al crear uno, del estilo «2026-2».
         *
         * Es solo una sugerencia editable: hay universidades que numeran al revés, otras que
         * usan el año académico cruzado. Para las dos primeras formas —semestral y por
         * bloques— el número sale de si la fecha cae en la primera o la segunda mitad del año.
         */
        fun suggestedName(type: AcademicTermType, start: LocalDate): String {
            val ordinal = when (type) {
                AcademicTermType.ANNUAL -> return start.year.toString()
                AcademicTermType.SEMESTER -> if (start.monthValue <= 6) 1 else 2
                AcademicTermType.TRIMESTER -> (start.monthValue - 1) / 4 + 1
                AcademicTermType.QUARTER -> (start.monthValue - 1) / 4 + 1
                AcademicTermType.BLOCKS -> if (start.monthValue <= 6) 1 else 2
            }
            return "${start.year}-$ordinal"
        }

        /** El fin que se propone, contando las semanas típicas de esa forma de periodo. */
        fun suggestedPlannedEnd(type: AcademicTermType, start: LocalDate): LocalDate =
            start.plusWeeks(type.weeks.toLong())
    }
}
