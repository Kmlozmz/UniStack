package com.unistack.app.feature_terms.domain

import java.time.LocalDate
import kotlinx.coroutines.flow.StateFlow

interface AcademicTermRepository {
    /** Todos los periodos, del más reciente al más antiguo. */
    val terms: StateFlow<List<AcademicTerm>>

    /**
     * El que se está cursando, o nulo entre uno y otro.
     *
     * Nulo no es un estado de error: es «sin periodo activo», que tiene pantalla propia con el
     * resumen de lo último cerrado y un botón para empezar el siguiente. Cerrar no vacía la
     * app, cambia a este estado.
     */
    val activeTerm: StateFlow<AcademicTerm?>

    suspend fun create(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?
    ): Result<AcademicTerm>

    suspend fun update(term: AcademicTerm): Result<Unit>

    /**
     * Cierra el periodo y lo manda al histórico.
     *
     * Irreversible en un sentido concreto: **nunca vuelve a ser el activo**. Sus notas siguen
     * editándose desde el histórico, porque llegan tarde y los profesores corrigen.
     */
    suspend fun close(termId: String, closedOn: LocalDate): Result<Unit>

    suspend fun delete(termId: String): Result<Unit>
}
