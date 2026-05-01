package com.unistack.app.core.utils

import com.unistack.app.feature_user.domain.GradingScale
import java.util.Locale

object GradingScaleUtils {
    fun maxGradeFor(scale: GradingScale): Double {
        return when (scale) {
            GradingScale.ZERO_TO_FIVE -> 5.0
            GradingScale.ZERO_TO_TEN -> 10.0
            GradingScale.ZERO_TO_ONE_HUNDRED -> 100.0
            else -> 5.0 // Default fallback
        }
    }

    fun defaultPassingGradeFor(scale: GradingScale): Double {
        return when (scale) {
            GradingScale.ZERO_TO_FIVE -> 3.0
            GradingScale.ZERO_TO_TEN -> 6.0
            GradingScale.ZERO_TO_ONE_HUNDRED -> 60.0
            else -> 3.0
        }
    }

    fun defaultTargetAverageFor(scale: GradingScale): Double {
        return when (scale) {
            GradingScale.ZERO_TO_FIVE -> 4.0
            GradingScale.ZERO_TO_TEN -> 8.0
            GradingScale.ZERO_TO_ONE_HUNDRED -> 80.0
            else -> 4.0
        }
    }

    fun formatGrade(value: Double?, scale: GradingScale): String {
        if (value == null) return "--"
        return when (scale) {
            GradingScale.ZERO_TO_ONE_HUNDRED -> String.format(Locale.US, "%.0f", value)
            else -> String.format(Locale.US, "%.1f", value)
        }
    }
}
