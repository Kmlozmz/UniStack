package com.unistack.app.core.design.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

val UniStackTypography = Typography().run {
    copy(
        displayLarge = displayLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        displayMedium = displayMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        displaySmall = displaySmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        headlineLarge = headlineLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.ExtraBold),
        headlineMedium = headlineMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        headlineSmall = headlineSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        titleSmall = titleSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold),
        bodyLarge = bodyLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        bodyMedium = bodyMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        bodySmall = bodySmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelLarge = labelLarge.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelMedium = labelMedium.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal),
        labelSmall = labelSmall.copy(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal)
    )
}

fun appearanceTypography(
    scale: Float,
    useSystemFont: Boolean
): Typography {
    val family = if (useSystemFont) FontFamily.Default else FontFamily.SansSerif
    fun TextUnit.scaled() = this * scale
    return UniStackTypography.run {
        copy(
            displayLarge = displayLarge.copy(fontFamily = family, fontSize = displayLarge.fontSize.scaled()),
            displayMedium = displayMedium.copy(fontFamily = family, fontSize = displayMedium.fontSize.scaled()),
            displaySmall = displaySmall.copy(fontFamily = family, fontSize = displaySmall.fontSize.scaled()),
            headlineLarge = headlineLarge.copy(fontFamily = family, fontSize = headlineLarge.fontSize.scaled()),
            headlineMedium = headlineMedium.copy(fontFamily = family, fontSize = headlineMedium.fontSize.scaled()),
            headlineSmall = headlineSmall.copy(fontFamily = family, fontSize = headlineSmall.fontSize.scaled()),
            titleLarge = titleLarge.copy(fontFamily = family, fontSize = titleLarge.fontSize.scaled()),
            titleMedium = titleMedium.copy(fontFamily = family, fontSize = titleMedium.fontSize.scaled()),
            titleSmall = titleSmall.copy(fontFamily = family, fontSize = titleSmall.fontSize.scaled()),
            bodyLarge = bodyLarge.copy(fontFamily = family, fontSize = bodyLarge.fontSize.scaled()),
            bodyMedium = bodyMedium.copy(fontFamily = family, fontSize = bodyMedium.fontSize.scaled()),
            bodySmall = bodySmall.copy(fontFamily = family, fontSize = bodySmall.fontSize.scaled()),
            labelLarge = labelLarge.copy(fontFamily = family, fontSize = labelLarge.fontSize.scaled()),
            labelMedium = labelMedium.copy(fontFamily = family, fontSize = labelMedium.fontSize.scaled()),
            labelSmall = labelSmall.copy(fontFamily = family, fontSize = labelSmall.fontSize.scaled())
        )
    }
}
