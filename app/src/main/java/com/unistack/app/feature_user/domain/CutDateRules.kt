package com.unistack.app.feature_user.domain

import java.time.LocalDate

/** Lo que le pasa a una tanda de fechas de corte cuando no sirve. */
enum class CutDateProblem {
    /** Unas puestas y otras no. Media tabla no ordena nada. */
    INCOMPLETAS,

    /** Una fecha que no es posterior a la anterior: el corte no tendría días. */
    DESORDENADAS,

    /** La primera cae antes de que empiece el periodo. */
    ANTES_DEL_INICIO,

    /** La última no deja sitio al corte final antes de que acabe el periodo. */
    DESPUES_DEL_FINAL
}

/**
 * Cuándo una tanda de fechas de corte se puede guardar.
 *
 * Las reglas ya existían escritas a mano dentro del onboarding, y al hacerlas editables desde
 * Ajustes habrían tenido que existir dos veces. Aquí están una sola vez y sin depender de
 * Compose ni de un `ViewModel`, que es lo que permite probarlas.
 *
 * Todas parten de lo mismo: se escribe **el último día** de cada corte menos el del último, que
 * acaba con el periodo. Por eso solo se miran las `cutCount - 1` primeras posiciones.
 */
object CutDateRules {
    /** Qué impide guardar estas fechas, o nulo si no hay nada que lo impida. */
    fun problemFor(
        cutEndDates: List<LocalDate?>,
        cutCount: Int,
        termStart: LocalDate? = null,
        termPlannedEnd: LocalDate? = null
    ): CutDateProblem? {
        val esperadas = (cutCount - 1).coerceAtLeast(0)
        val intermedias = List(esperadas) { cutEndDates.getOrNull(it) }
        val puestas = intermedias.filterNotNull()
        // Ninguna puesta es un estado legítimo: el corte de cada nota se elige a mano.
        if (puestas.isEmpty()) return null
        if (puestas.size != esperadas) return CutDateProblem.INCOMPLETAS
        if (puestas.zipWithNext().any { (a, b) -> !b.isAfter(a) }) return CutDateProblem.DESORDENADAS
        if (termStart != null && puestas.first().isBefore(termStart)) {
            return CutDateProblem.ANTES_DEL_INICIO
        }
        // El ultimo corte va del dia siguiente al final del periodo: sin dias no es un corte.
        if (termPlannedEnd != null && !termPlannedEnd.isAfter(puestas.last())) {
            return CutDateProblem.DESPUES_DEL_FINAL
        }
        return null
    }

    /** Si estas fechas se pueden guardar tal cual están. */
    fun areSound(
        cutEndDates: List<LocalDate?>,
        cutCount: Int,
        termStart: LocalDate? = null,
        termPlannedEnd: LocalDate? = null
    ): Boolean = problemFor(cutEndDates, cutCount, termStart, termPlannedEnd) == null

    /**
     * La lista con el tamaño que le toca a [cutCount] cortes, conservando lo que ya había.
     *
     * Cambiar de tres cortes a cuatro no debería borrar las dos fechas que ya estaban puestas,
     * y bajar de cuatro a tres no debería dejar una fecha suelta que ya no pinta nada.
     */
    fun resize(cutEndDates: List<LocalDate?>, cutCount: Int): List<LocalDate?> {
        val esperadas = (cutCount - 1).coerceAtLeast(0)
        return List(esperadas) { cutEndDates.getOrNull(it) }
    }
}
