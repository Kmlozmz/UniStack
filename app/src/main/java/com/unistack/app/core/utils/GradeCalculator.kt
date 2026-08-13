package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_user.domain.AcademicPeriod
import kotlin.math.round

data class PeriodGradeCalculation(
    val average: Double?,
    val evaluatedFraction: Double,
    val weightedPoints: Double,
    val usesOfficialResult: Boolean,
    val unknownWeightCount: Int,
    val recordedGradeCount: Int,
    /**
     * Suma real de los pesos, sin recortar a 1.0. Solo difiere de [evaluatedFraction]
     * cuando el corte está sobreasignado, que es justo lo que detecta [isOverAllocated].
     */
    val allocatedFraction: Double = evaluatedFraction
) {
    val isComplete: Boolean
        get() = evaluatedFraction >= 0.9999

    val isProvisional: Boolean
        get() = average != null && (!isComplete || unknownWeightCount > 0)

    /**
     * Los pesos del corte suman más del 100%. El promedio sigue siendo correcto —es una
     * media ponderada— pero los datos no lo son, y merece avisarse en pantalla.
     */
    val isOverAllocated: Boolean
        get() = allocatedFraction > 1.0001

    /**
     * Lo que este corte aporta a la nota final, ya escalado a la fracción que cubre.
     *
     * Con datos sanos es exactamente [weightedPoints]. Difiere solo cuando el corte está
     * sobreasignado, y ahí está el motivo de que exista: [average] ya se calcula dividiendo
     * por la suma real de pesos, pero [weightedPoints] no, así que sumarlo tal cual a nivel
     * de materia devolvía el número inflado que el corte ya había corregido. Tres actividades
     * del 50% con 5.0 daban 5.0 en la tarjeta del corte y 7.5 en la de la materia.
     */
    val normalizedPoints: Double
        get() = if (allocatedFraction > 1.0) weightedPoints / allocatedFraction else weightedPoints
}

/**
 * Cómo va la meta de la materia, mirando dónde se puede acabar y no solo dónde se está.
 *
 * Sale de comparar la meta con el suelo y el techo —lo que se saca haciendo 0 en lo que falta
 * y lo que se saca haciéndolo perfecto—, así que son estados comprobables y no una suposición
 * sobre el futuro.
 */
enum class TargetOutlook {
    /** Nada evaluado todavía: no hay nada que decir sin inventarlo. */
    NO_DATA,

    /** El suelo ya llega a la meta: no se puede perder ni sacando 0 en todo lo que falta. */
    SECURED,

    /** El promedio va por encima de la meta, pero todavía se puede caer. */
    ON_TRACK,

    /** Se llega, pero hay que sacar más de lo que se lleva. */
    AT_RISK,

    /** Ni con el máximo en todo lo que falta se alcanza la meta. */
    UNREACHABLE
}

data class SubjectGradeCalculation(
    /** Promedio de lo evaluado hasta ahora. No es una previsión del final. */
    val currentAverage: Double?,
    /** Puntos ya asegurados sobre la nota final. Es el suelo. */
    val confirmedWeightedPoints: Double,
    val evaluatedSemesterFraction: Double,
    val remainingSemesterFraction: Double,
    val neededForTarget: Double?,
    val unknownWeightCount: Int,
    val hasIncompleteData: Boolean,
    val targetIsReachable: Boolean?,
    /** Nota final si se saca 0 en todo lo que falta. Null si no hay nada evaluado. */
    val guaranteedMinimum: Double?,
    /** Nota final si se saca el máximo en todo lo que falta. Null si no hay nada evaluado. */
    val bestPossible: Double?,
    /** No queda nada por evaluar: el suelo y el techo son la misma nota, la definitiva. */
    val isFinished: Boolean,
    val outlook: TargetOutlook
)

object GradeCalculator {
    fun calculateCurrentAverage(grades: List<GradeItem>): Double? {
        return calculatePeriod(grades).average
    }

    fun calculateEvaluatedPercentage(grades: List<GradeItem>): Double {
        return roundToOneDecimal(calculatePeriod(grades).evaluatedFraction * 100.0)
    }

    fun calculateWeightedPoints(grades: List<GradeItem>): Double {
        return calculatePeriod(grades).weightedPoints
    }

    fun calculatePeriodAverage(grades: List<GradeItem>): Double? = calculatePeriod(grades).average

    fun calculatePeriod(grades: List<GradeItem>): PeriodGradeCalculation {
        val officialResult = grades
            .filter { it.source == GradeSource.PERIOD_FINAL }
            .maxByOrNull { it.recordedAt }
        if (officialResult != null) {
            return PeriodGradeCalculation(
                average = roundToOneDecimal(officialResult.value),
                evaluatedFraction = 1.0,
                weightedPoints = officialResult.value,
                usesOfficialResult = true,
                unknownWeightCount = grades.count {
                    it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
                },
                recordedGradeCount = grades.size
            )
        }

        val activities = grades.filter { it.source == GradeSource.ACTIVITY }
        val knownActivities = activities.filter {
            it.weightStatus == GradeWeightStatus.KNOWN && it.percentage > 0.0
        }
        // Se divide por lo que realmente se sumó, no por la fracción recortada. Si los
        // pesos pasan del 100% —dato que puede llegar de un respaldo restaurado o de un
        // esquema de cortes editado después— recortar solo el denominador inflaba el
        // resultado: tres actividades del 50% con 5.0 daban 7.5 en una escala de 0 a 5.
        // Dividiendo por la suma real sigue siendo una media ponderada de verdad, así que
        // nunca puede superar la nota más alta que haya entre los datos.
        val allocatedFraction = knownActivities.sumOf { it.percentage }
        val evaluatedFraction = allocatedFraction.coerceIn(0.0, 1.0)
        val weightedPoints = knownActivities.sumOf { it.value * it.percentage }
        val average = if (allocatedFraction <= 0.0) {
            null
        } else {
            roundToOneDecimal(weightedPoints / allocatedFraction)
        }
        return PeriodGradeCalculation(
            average = average,
            evaluatedFraction = evaluatedFraction,
            weightedPoints = weightedPoints,
            usesOfficialResult = false,
            unknownWeightCount = activities.count { it.weightStatus == GradeWeightStatus.UNKNOWN },
            recordedGradeCount = activities.size,
            allocatedFraction = allocatedFraction
        )
    }

    fun calculateWeightedPointsByPeriods(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double {
        return periods.sumOf { period ->
            calculatePeriod(grades.filter { it.periodId == period.id }).normalizedPoints * period.weight
        }
    }

    /**
     * Promedio de lo que ya está evaluado.
     *
     * Se llamaba «projected» y no proyecta nada: divide los puntos confirmados entre la
     * fracción evaluada, así que responde «cómo voy», no «cómo voy a acabar». El nombre
     * antiguo invitaba a colocar al lado una segunda proyección calculada de otra forma, que
     * es exactamente lo que había en el detalle de materia dando un número distinto.
     */
    fun calculateCurrentAverageByPeriods(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double? {
        val evaluatedWeight = evaluatedSemesterFraction(grades, periods)
        if (evaluatedWeight <= 0.0) return null
        return roundToOneDecimal(calculateWeightedPointsByPeriods(grades, periods) / evaluatedWeight)
    }

    /**
     * Margen para dar por «rozando» la meta: un 10% de la escala.
     *
     * Estaba escrito como 0.5 fijo en la lista de materias. En la escala de 0 a 5 eso es el
     * 10%, pero en la de 0 a 100 es medio punto: el estado de aviso solo aparecía entre 79.5
     * y 80, así que en la práctica se saltaba de «sobre meta» a «revisar» de golpe.
     */
    fun closeToTargetMargin(maxGrade: Double): Double = maxGrade * 0.1

    fun calculateEvaluatedSemesterPercentage(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double {
        return roundToOneDecimal(evaluatedSemesterFraction(grades, periods) * 100.0)
    }

    fun calculateSubject(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>,
        targetAverage: Double,
        maxGrade: Double
    ): SubjectGradeCalculation {
        val evaluated = evaluatedSemesterFraction(grades, periods)
        val remaining = (1.0 - evaluated).coerceAtLeast(0.0)
        val weightedPoints = calculateWeightedPointsByPeriods(grades, periods)
        val current = if (evaluated > 0.0) roundToOneDecimal(weightedPoints / evaluated) else null
        val needed = calculateNeededGrade(weightedPoints, remaining, targetAverage, maxGrade)
        val unknownWeights = grades.count {
            it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
        }
        // El suelo y el techo: qué nota final queda si en lo que falta se saca 0, y qué queda
        // si se saca el máximo. Son los dos extremos reales, sin suponer nada sobre cómo irá
        // lo que todavía no se ha hecho.
        val floor = if (evaluated > 0.0) roundToOneDecimal(weightedPoints) else null
        val ceiling = if (evaluated > 0.0) roundToOneDecimal(weightedPoints + remaining * maxGrade) else null
        val outlook = when {
            floor == null || ceiling == null -> TargetOutlook.NO_DATA
            floor >= targetAverage - 0.0001 -> TargetOutlook.SECURED
            ceiling < targetAverage - 0.0001 -> TargetOutlook.UNREACHABLE
            current != null && current >= targetAverage - 0.0001 -> TargetOutlook.ON_TRACK
            else -> TargetOutlook.AT_RISK
        }
        return SubjectGradeCalculation(
            currentAverage = current,
            confirmedWeightedPoints = weightedPoints,
            evaluatedSemesterFraction = evaluated,
            remainingSemesterFraction = remaining,
            neededForTarget = needed,
            unknownWeightCount = unknownWeights,
            hasIncompleteData = unknownWeights > 0 || remaining > 0.0001,
            targetIsReachable = needed?.let { it <= maxGrade },
            guaranteedMinimum = floor,
            bestPossible = ceiling,
            isFinished = remaining <= 0.0001,
            outlook = outlook
        )
    }

    fun calculateNeededGrade(
        currentWeightedPoints: Double,
        remainingPercentage: Double,
        targetAverage: Double,
        maxGrade: Double = 5.0
    ): Double? {
        if (remainingPercentage <= 0.0 || maxGrade <= 0.0) return null
        val needed = (targetAverage - currentWeightedPoints) / remainingPercentage
        if (needed.isNaN() || needed.isInfinite()) return null
        return roundToOneDecimal(needed)
    }

    private fun evaluatedSemesterFraction(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double {
        return periods.sumOf { period ->
            calculatePeriod(grades.filter { it.periodId == period.id }).evaluatedFraction * period.weight
        }.coerceIn(0.0, 1.0)
    }

    private fun roundToOneDecimal(value: Double): Double = round(value * 10.0) / 10.0
}
