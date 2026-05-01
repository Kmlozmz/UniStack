package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import kotlin.math.round

object GradeCalculator {
    fun calculateCurrentAverage(grades: List<GradeItem>): Double {
        val evaluatedPercentage = grades.sumOf { it.percentage }
        if (evaluatedPercentage == 0.0) return 0.0

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

    fun calculateNeededGrade(
        currentWeightedPoints: Double,
        remainingPercentage: Double,
        targetAverage: Double
    ): Double {
        if (remainingPercentage == 0.0) return Double.NaN

        val needed = (targetAverage - currentWeightedPoints) / remainingPercentage
        return roundToOneDecimal(needed.coerceIn(0.0, 5.0))
    }

    private fun roundToOneDecimal(value: Double): Double = round(value * 10.0) / 10.0
}
