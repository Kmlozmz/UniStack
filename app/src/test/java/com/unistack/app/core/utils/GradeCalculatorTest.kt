package com.unistack.app.core.utils

import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.GradingScale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GradeCalculatorTest {
    @Test
    fun `format grade follows active numeric scale`() {
        assertEquals("4.3", GradingScaleUtils.formatGrade(4.25, GradingScale.ZERO_TO_FIVE))
        assertEquals("89", GradingScaleUtils.formatGrade(88.6, GradingScale.CUSTOM))
        assertEquals("--", GradingScaleUtils.formatGrade(null, GradingScale.ZERO_TO_FIVE))
    }

    @Test
    fun `max grade matches supported numeric scales`() {
        assertEquals(5.0, GradingScaleUtils.maxGradeFor(GradingScale.ZERO_TO_FIVE), 0.0)
        assertEquals(100.0, GradingScaleUtils.maxGradeFor(GradingScale.CUSTOM), 0.0)
    }

    @Test
    fun `current average is weighted by evaluated percentage`() {
        val grades = listOf(
            GradeItem(id = "1", name = "Parcial", value = 4.0, percentage = 0.5),
            GradeItem(id = "2", name = "Quiz", value = 3.0, percentage = 0.25)
        )

        assertEquals(3.7, GradeCalculator.calculateCurrentAverage(grades)!!, 0.0)
        assertEquals(75.0, GradeCalculator.calculateEvaluatedPercentage(grades), 0.0)
    }

    @Test
    fun `period projection uses internal grade weights and period weights`() {
        val periods = listOf(
            AcademicPeriod(id = "period-1", name = "Corte 1", weight = 0.30, order = 1),
            AcademicPeriod(id = "period-2", name = "Corte 2", weight = 0.40, order = 2),
            AcademicPeriod(id = "period-3", name = "Corte 3", weight = 0.30, order = 3)
        )
        val grades = listOf(
            GradeItem(id = "1", name = "Taller", value = 5.0, percentage = 0.20, periodId = "period-1"),
            GradeItem(id = "2", name = "Exposición", value = 4.0, percentage = 0.20, periodId = "period-1"),
            GradeItem(id = "3", name = "Parcial", value = 4.5, percentage = 0.60, periodId = "period-1")
        )

        assertEquals(4.5, GradeCalculator.calculatePeriodAverage(grades)!!, 0.0)
        assertEquals(1.35, GradeCalculator.calculateWeightedPointsByPeriods(grades, periods), 0.0)
        assertEquals(30.0, GradeCalculator.calculateEvaluatedSemesterPercentage(grades, periods), 0.0)
        assertEquals(4.5, GradeCalculator.calculateProjectedAverageByPeriods(grades, periods)!!, 0.0)
    }

    @Test
    fun `needed grade is calculated for each numeric scale`() {
        assertEquals(
            4.7,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 2.6,
                remainingPercentage = 0.3,
                targetAverage = 4.0,
                maxGrade = 5.0
            )!!,
            0.0
        )
        assertEquals(
            9.3,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 5.2,
                remainingPercentage = 0.3,
                targetAverage = 8.0,
                maxGrade = 10.0
            )!!,
            0.0
        )
        assertEquals(
            93.3,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 52.0,
                remainingPercentage = 0.3,
                targetAverage = 80.0,
                maxGrade = 100.0
            )!!,
            0.0
        )
    }

    @Test
    fun `needed grade returns impossible value uncapped for caller messaging`() {
        assertEquals(
            12.0,
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 4.4,
                remainingPercentage = 0.3,
                targetAverage = 8.0,
                maxGrade = 10.0
            )!!,
            0.0
        )
    }

    @Test
    fun `needed grade is null when no percentage remains`() {
        assertNull(
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = 4.0,
                remainingPercentage = 0.0,
                targetAverage = 4.0,
                maxGrade = 5.0
            )
        )
    }
}
