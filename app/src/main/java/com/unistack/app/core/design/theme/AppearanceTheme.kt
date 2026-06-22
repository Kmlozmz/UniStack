package com.unistack.app.core.design.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.MotionPreference

val LocalAppearancePreferences = staticCompositionLocalOf { AppearancePreferences.defaults() }
val LocalAccessibilityPreferences = staticCompositionLocalOf { AccessibilityPreferences() }

val LocalMotionDurationScale = staticCompositionLocalOf { 1f }

val LocalInterfaceSpacing = staticCompositionLocalOf { InterfaceSpacing() }

object AppearanceRuntime {
    var cornerStyle: CornerStyle = CornerStyle.BALANCED
        internal set
}

data class InterfaceSpacing(
    val screenHorizontal: Dp = 20.dp,
    val section: Dp = 16.dp,
    val cardPadding: Dp = 16.dp,
    val controlHeight: Dp = 52.dp
)

internal fun AccessibilityPreferences.motionScale(): Float = when (motionPreference) {
    MotionPreference.FULL -> 1f
    MotionPreference.REDUCED -> 0.55f
    MotionPreference.NONE -> 0f
}

internal fun AppearancePreferences.interfaceSpacing(): InterfaceSpacing = when (interfaceDensity) {
    InterfaceDensity.COMPACT -> InterfaceSpacing(
        screenHorizontal = 16.dp,
        section = 12.dp,
        cardPadding = 12.dp,
        controlHeight = 46.dp
    )
    InterfaceDensity.BALANCED -> InterfaceSpacing()
    InterfaceDensity.COMFORTABLE -> InterfaceSpacing(
        screenHorizontal = 22.dp,
        section = 20.dp,
        cardPadding = 18.dp,
        controlHeight = 56.dp
    )
}

internal fun CornerStyle.cardRadius(): Dp = when (this) {
    CornerStyle.COMPACT -> 8.dp
    CornerStyle.BALANCED -> 16.dp
    CornerStyle.SOFT -> 24.dp
}
