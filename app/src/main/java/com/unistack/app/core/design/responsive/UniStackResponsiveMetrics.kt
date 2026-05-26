package com.unistack.app.core.design.responsive

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class UniStackWindowSize {
    Compact,
    Medium,
    Expanded
}

@Immutable
data class UniStackResponsiveMetrics(
    val windowSize: UniStackWindowSize,
    val horizontalPadding: Dp,
    val cardPadding: Dp,
    val cardRadius: Dp,
    val sectionSpacing: Dp,
    val itemSpacing: Dp,
    val titleFontSize: TextUnit,
    val bodyFontSize: TextUnit,
    val isCompact: Boolean,
    val isMedium: Boolean,
    val isExpanded: Boolean
)

fun uniStackResponsiveMetrics(maxWidth: Dp): UniStackResponsiveMetrics {
    val windowSize = when {
        maxWidth < 380.dp -> UniStackWindowSize.Compact
        maxWidth < 430.dp -> UniStackWindowSize.Medium
        else -> UniStackWindowSize.Expanded
    }

    return when (windowSize) {
        UniStackWindowSize.Compact -> UniStackResponsiveMetrics(
            windowSize = windowSize,
            horizontalPadding = 16.dp,
            cardPadding = 16.dp,
            cardRadius = 16.dp,
            sectionSpacing = 24.dp,
            itemSpacing = 18.dp,
            titleFontSize = 32.sp,
            bodyFontSize = 15.sp,
            isCompact = true,
            isMedium = false,
            isExpanded = false
        )

        UniStackWindowSize.Medium -> UniStackResponsiveMetrics(
            windowSize = windowSize,
            horizontalPadding = 20.dp,
            cardPadding = 16.dp,
            cardRadius = 18.dp,
            sectionSpacing = 26.dp,
            itemSpacing = 18.dp,
            titleFontSize = 32.sp,
            bodyFontSize = 15.sp,
            isCompact = false,
            isMedium = true,
            isExpanded = false
        )

        UniStackWindowSize.Expanded -> UniStackResponsiveMetrics(
            windowSize = windowSize,
            horizontalPadding = 24.dp,
            cardPadding = 20.dp,
            cardRadius = 20.dp,
            sectionSpacing = 28.dp,
            itemSpacing = 20.dp,
            titleFontSize = 36.sp,
            bodyFontSize = 16.sp,
            isCompact = false,
            isMedium = false,
            isExpanded = true
        )
    }
}
