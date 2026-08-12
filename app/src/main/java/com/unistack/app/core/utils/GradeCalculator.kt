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
}

data class SubjectGradeCalculation(
    val projectedAverage: Double?,
    val confirmedWeightedPoints: Double,
    val evaluatedSemesterFraction: Double,
    val remainingSemesterFraction: Double,
    val neededForTarget: Double?,
    val unknownWeightCount: Int,
    val hasIncompleteData: Boolean,
    val targetIsReachable: Boolean?
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
            calculatePeriod(grades.filter { it.periodId == period.id }).weightedPoints * period.weight
        }
    }

    fun calculateProjectedAverageByPeriods(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double? {
        val evaluatedWeight = evaluatedSemesterFraction(grades, periods)
        if (evaluatedWeight <= 0.0) return null
        return roundToOneDecimal(calculateWeightedPointsByPeriods(grades, periods) / evaluatedWeight)
    }

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
        val projected = if (evaluated > 0.0) roundToOneDecimal(weightedPoints / evaluated) else null
        val needed = calculateNeededGrade(weightedPoints, remaining, targetAverage, maxGrade)
        val unknownWeights = grades.count {
            it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
        }
        return SubjectGradeCalculation(
            projectedAverage = projected,
            confirmedWeightedPoints = weightedPoints,
            evaluatedSemesterFraction = evaluated,
            remainingSemesterFraction = remaining,
            neededForTarget = needed,
            unknownWeightCount = unknownWeights,
            hasIncompleteData = unknownWeights > 0 || remaining > 0.0001,
            targetIsReachable = needed?.let { it <= maxGrade }
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
