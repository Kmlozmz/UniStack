package com.unistack.app.core.design.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object UniStackDatePickerColors {
    val Surface: Color
        @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF080B13) else Color.White

    val DayCell: Color
        @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF10131B) else Color(0xFFF4F0FA)

    val Accent: Color
        @Composable get() = Color(0xFFFF746D)

    val Text: Color
        @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF8F7FC) else UniStackColors.TextPrimary

    val Muted: Color
        @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA9A7B7) else UniStackColors.TextSecondary
}
