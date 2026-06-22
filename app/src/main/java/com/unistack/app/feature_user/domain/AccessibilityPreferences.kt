package com.unistack.app.feature_user.domain

data class AccessibilityPreferences(
    val appLanguage: AppLanguage = AppLanguage.SYSTEM,
    val highContrastEnabled: Boolean = false,
    val use24HourTime: Boolean = true,
    val textScale: TextScalePreference = TextScalePreference.STANDARD,
    val motionPreference: MotionPreference = MotionPreference.FULL,
    val heroAnimationEnabled: Boolean = true
)

enum class AppLanguage {
    SYSTEM,
    SPANISH,
    ENGLISH
}
