package com.unistack.app.feature_user.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class AppearancePreferencesTest {

    @Test
    fun normalized_restoresMissingHomeSectionsAndClampsDecimals() {
        val normalized = AppearancePreferences(
            decimalPlaces = 7,
            homeSectionOrder = listOf(HomeSection.AGENDA, HomeSection.AGENDA)
        ).normalized()

        assertEquals(2, normalized.decimalPlaces)
        assertEquals(
            listOf(HomeSection.AGENDA, HomeSection.HERO, HomeSection.SNAPSHOT),
            normalized.homeSectionOrder
        )
    }

    @Test
    fun minimalPresetDoesNotOverrideAccessibilityMotion() {
        val preset = AppearancePreferences.preset(VisualPreset.MINIMAL)

        assertEquals(InterfaceDensity.COMPACT, preset.interfaceDensity)
        assertEquals(MotionPreference.FULL, preset.motionPreference)
        assertEquals(BottomBarStyle.ICONS_ONLY, preset.bottomBarStyle)
    }

    @Test
    fun focusPresetKeepsAcademicContentAndHidesSnapshot() {
        val preset = AppearancePreferences.preset(VisualPreset.FOCUS)

        assertFalse(preset.showHomeSnapshot)
        assertFalse(preset.heroShowsExpenses)
        assertEquals(AccentStyle.TEAL, preset.accentStyle)
    }
}
