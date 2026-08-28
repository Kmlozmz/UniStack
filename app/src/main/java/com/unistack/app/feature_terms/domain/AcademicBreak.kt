package com.unistack.app.feature_terms.domain

import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate

/**
 * Un tramo del calendario en que no hubo clase para nadie.
 *
 * Festivos, paros, semanas de receso. Sin esto la app **registra algo falso**: los días que la
 * universidad estaba cerrada aparecen como clases pendientes, y al no marcarlas se leen igual
 * que las que se te olvidaron. Es el mismo problema que resolvió acotar el historial al
 * periodo, un nivel más abajo.
 *
 * Es un **rango y no un día suelto** porque una semana de receso es una decisión, no siete: si
 * hubiera que apuntar día a día, nadie lo haría. [start] y [end] son ambos inclusive, y para un
 * festivo de un día coinciden.
 */
data class AcademicBreak(
    val id: String,
    val userId: String,
    val name: String,
    val startEpochDay: Long,
    val endEpochDay: Long,
    val createdAt: Long,
    val updatedAt: Long
) {
    val start: LocalDate get() = LocalDate.ofEpochDay(startEpochDay)

    val end: LocalDate get() = LocalDate.ofEpochDay(endEpochDay)

    /** El rango completo, para preguntarle si contiene una fecha. */
    val range: ClosedRange<LocalDate> get() = start..end

    /** Cuántos días naturales dura, contando los dos extremos. */
    val days: Int get() = (endEpochDay - startEpochDay + 1).toInt()

    fun contains(date: LocalDate): Boolean = !date.isBefore(start) && !date.isAfter(end)

    val isValid: Boolean
        get() = id.isNotBlank() && name.isNotBlank() && endEpochDay >= startEpochDay
}

interface AcademicBreakRepository {
    /** Todos los tramos sin clase, del más reciente al más antiguo. */
    val breaks: StateFlow<List<AcademicBreak>>

    suspend fun save(
        id: String?,
        name: String,
        start: LocalDate,
        end: LocalDate
    ): Result<AcademicBreak>

    suspend fun delete(breakId: String): Result<Unit>
}
