package com.unistack.app.core.utils

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import java.util.Locale

object GradingScaleUtils {
    @Volatile
    private var preferredDecimalPlaces: Int? = null

    fun configureDecimalPlaces(decimalPlaces: Int) {
        preferredDecimalPlaces = decimalPlaces.coerceIn(0, 2)
    }

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
        if (value == null) return NO_DATA
        val decimals = preferredDecimalPlaces ?: when (scale) {
            GradingScale.ZERO_TO_HUNDRED,
            GradingScale.CUSTOM -> 0
            GradingScale.ZERO_TO_FIVE -> 1
        }
        return String.format(Locale.US, "%.${decimals}f", value)
    }
}
