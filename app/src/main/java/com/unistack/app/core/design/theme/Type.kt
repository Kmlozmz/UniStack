package com.unistack.app.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily

val UniStackTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = FontFamily.SansSerif),
        displayMedium = displayMedium.copy(fontFamily = FontFamily.SansSerif),
        displaySmall = displaySmall.copy(fontFamily = FontFamily.SansSerif),
        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.SansSerif),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif),
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.SansSerif),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif),
        titleSmall = titleSmall.copy(fontFamily = FontFamily.SansSerif),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif),
        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif),
        bodySmall = bodySmall.copy(fontFamily = FontFamily.SansSerif),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif),
        labelMedium = labelMedium.copy(fontFamily = FontFamily.SansSerif),
        labelSmall = labelSmall.copy(fontFamily = FontFamily.SansSerif)
    )
}
