package com.unistack.app.feature_terms.domain

import java.time.LocalDate
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

/**
 * Cada cuánto empieza un ciclo en tu universidad.
 *
 * No decide nada por sí solo —las fechas las pone el usuario— pero sirve para sugerir una
 * duración al crear el periodo y para nombrarlo en la interfaz. Son las cinco formas que se
 * usan de verdad; no hay una opción «otro» porque cualquier calendario cabe en alguna de ellas
 * en cuanto se le ponen fechas propias.
 */
enum class AcademicTermType(private val labelRes: Int, val perYear: Int, val weeks: Int) {
    SEMESTER(R.string.term_type_semester, 2, 16),
    TRIMESTER(R.string.term_type_trimester, 4, 11),
    QUARTER(R.string.term_type_quarter, 3, 14),
    ANNUAL(R.string.term_type_annual, 1, 36),
    BLOCKS(R.string.term_type_blocks, 6, 8);

    val label: String get() = Textos.get(labelRes)

    /**
     * En cual de los tramos del año cae [date], contando desde 1.
     *
     * La ilustracion del año pintaba siempre el primero como «el que vas a configurar», lo que
     * daba por hecho que todo el mundo se matricula en enero. Quien instala la app en julio
     * esta en el segundo semestre, y la pantalla le decia lo contrario.
     *
     * Es tambien de donde sale el numero del nombre sugerido, para que la barra y el nombre no
     * puedan discrepar: se calculaban por separado y para trimestral no coincidian.
     */
    fun blockFor(date: LocalDate): Int =
        ((date.monthValue - 1) * perYear / 12 + 1).coerceIn(1, perYear)

    /**
     * Cuantos meses dura uno, que es de donde viene su nombre.
     *
     * Sin este dato la pantalla parecia equivocada: «Trimestral · 4 al año» y «Cuatrimestral ·
     * 3 al año» se leen como invertidos, cuando lo cierto es que un trimestre son 3 meses —y
     * por eso caben cuatro— y un cuatrimestre son 4 —y por eso caben tres—. Diciendo los meses,
     * el nombre y la cuenta se explican el uno al otro.
     */
    val months: Int get() = 12 / perYear
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
    val updatedAt: Long,
    /**
     * Los cortes con los que se cerró, o nulo mientras está en curso.
     *
     * El esquema vive en las preferencias y cada periodo nuevo le pone sus fechas. Sin esta
     * copia, cambiar el reparto o las fechas el semestre que viene reescribía en silencio las
     * notas finales y los cortes de todos los cerrados.
     */
    val cutScheme: GradingCutScheme? = null
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
            if (type == AcademicTermType.ANNUAL) return start.year.toString()
            return "${start.year}-${type.blockFor(start)}"
        }

        /** El fin que se propone, contando las semanas típicas de esa forma de periodo. */
        fun suggestedPlannedEnd(type: AcademicTermType, start: LocalDate): LocalDate =
            start.plusWeeks(type.weeks.toLong())
    }
}
