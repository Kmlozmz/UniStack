package com.unistack.app.core.utils

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import java.util.Locale

object GradingScaleUtils {
    fun maxGradeFor(scale: GradingScale): Double {
        return when (scale) {
            GradingScale.ZERO_TO_FIVE -> 5.0
            GradingScale.ZERO_TO_HUNDRED -> 100.0
            GradingScale.CUSTOM -> 100.0
        }
    }

    fun maxGradeFor(profile: UserProfile): Double {
        return if (profile.gradingScale == GradingScale.CUSTOM) {
            profile.customGradeMax.coerceIn(1.0, 100.0)
        } else {
            maxGradeFor(profile.gradingScale)
        }
    }

    fun formatGrade(value: Double?, scale: GradingScale): String {
        if (value == null) return "--"
        return when (scale) {
            GradingScale.ZERO_TO_HUNDRED,
            GradingScale.CUSTOM -> String.format(Locale.US, "%.0f", value)
            GradingScale.ZERO_TO_FIVE -> String.format(Locale.US, "%.1f", value)
        }
    }
}
