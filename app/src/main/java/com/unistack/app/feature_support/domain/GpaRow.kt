package com.unistack.app.feature_support.domain

import java.util.UUID

/**
 * Una fila de la calculadora: una materia con su nota y su peso.
 *
 * Los tres campos son texto y no números porque la fila existe mientras se escribe, y «3,» o
 * «» son estados legítimos a medio teclear. El cálculo se hace con lo que sí sea un número.
 */
data class GpaRow(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val grade: String = "",
    val credits: String = ""
) {
    val gradeValue: Double? get() = grade.replace(',', '.').toDoubleOrNull()

    /**
     * El peso de la fila. Sin créditos escritos vale uno: quien no los usa espera un promedio
     * simple, no que su materia no cuente.
     */
    val creditsValue: Double
        get() = credits.replace(',', '.').toDoubleOrNull()?.takeIf { it > 0.0 } ?: 1.0

    val isUsable: Boolean get() = gradeValue != null
}

/** Promedio ponderado de las filas que tengan nota. Nulo si no hay ninguna. */
fun weightedAverage(rows: List<GpaRow>): Double? {
    val usable = rows.filter(GpaRow::isUsable)
    if (usable.isEmpty()) return null
    val weight = usable.sumOf(GpaRow::creditsValue)
    if (weight <= 0.0) return null
    return usable.sumOf { (it.gradeValue ?: 0.0) * it.creditsValue } / weight
}
