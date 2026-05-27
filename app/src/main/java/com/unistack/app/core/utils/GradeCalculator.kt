package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import kotlin.math.round

object GradeCalculator {
    fun calculateCurrentAverage(grades: List<GradeItem>): Double? {
        val evaluatedPercentage = grades.sumOf { it.percentage }
        if (evaluatedPercentage <= 0.0) return null

        val weightedPoints = grades.sumOf { it.value * it.percentage }
        return roundToOneDecimal(weightedPoints / evaluatedPercentage)
    }

    fun calculateFinalAverage(grades: List<GradeItem>): Double {
        val weightedPoints = grades.sumOf { it.value * it.percentage }
        return roundToOneDecimal(weightedPoints)
    }

    fun calculateEvaluatedPercentage(grades: List<GradeItem>): Double {
        return roundToOneDecimal(grades.sumOf { it.percentage } * 100.0)
    }

    fun calculateWeightedPoints(grades: List<GradeItem>): Double {
        return grades.sumOf { it.value * it.percentage }
    }

    fun calculatePeriodAverage(grades: List<GradeItem>): Double? = calculateCurrentAverage(grades)

    fun calculateFinalAverageByPeriods(
        grades: List<GradeItem>,
        scheme: AcademicPeriodScheme
    ): Double? {
        val weightedPeriods = scheme.periods.mapNotNull { period ->
            val periodAverage = calculatePeriodAverage(grades.filter { it.periodId == period.id }) ?: return@mapNotNull null
            periodAverage * period.weight
        }
        if (weightedPeriods.isEmpty()) return null
        return roundToOneDecimal(weightedPeriods.sum())
    }

    fun calculateEvaluatedSemesterPercentage(
        grades: List<GradeItem>,
        periods: List<AcademicPeriod>
    ): Double {
        val evaluated = periods.sumOf { period ->
            val periodEvaluated = grades
                .filter { it.periodId == period.id }
                .sumOf { it.percentage }
                .coerceIn(0.0, 1.0)
            periodEvaluated * period.weight
        }
        return roundToOneDecimal(evaluated * 100.0)
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

    private fun roundToOneDecimal(value: Double): Double = round(value * 10.0) / 10.0
}
