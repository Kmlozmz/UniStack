package com.unistack.app.feature_home.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.unistack.app.core.design.theme.UniStackColors

internal val HomeBackgroundBrush: Brush
    @Composable get() = androidx.compose.ui.graphics.SolidColor(UniStackColors.Background)

internal val HeroBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.linearGradient(
            listOf(
                HomeHeroStart,
                HomeHeroMid,
                HomeHeroTransition,
                HomeHeroEnd
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                HomeHeroLightModeStart,
                HomeHeroLightModeMid,
                HomeHeroLightModeTransition,
                HomeHeroLightModeEnd
            )
        )
    }

internal val HomeCard: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeCardDark.copy(alpha = 0.96f) else Color.White
internal val HomeText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextPrimary else Color(0xFF171427)
internal val HomeSoftText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextSoft else Color(0xFF575269)
internal val HomeMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeTextMuted else Color(0xFF6B6578)
internal val HomeBorder: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) HomeStroke else Color.Black.copy(alpha = 0.07f)
internal val HomePurple: Color
    @Composable get() = HomeAccentPurple
internal val HomeTeal: Color
    @Composable get() = HomeAccentTeal
internal val HomeCoral: Color
    @Composable get() = HomeAccentCoral
internal val HomeYellow: Color
    @Composable get() = HomeAccentYellow

internal val HomeBgTop = Color(0xFF01040B)
internal val HomeBgMid = Color(0xFF01040B)
internal val HomeBgBottom = Color(0xFF000309)
internal val HomeCardDark = Color(0xFF080D17)
internal val HomeHeroStart = Color(0xFF09051A)
internal val HomeHeroMid = Color(0xFF0E0228)
internal val HomeHeroEnd = Color(0xFF06041C)
internal val HomeHeroTransition = Color(0xFF1A0A48)
internal val HomeHeroVioletDepth = Color(0xFF2A0E72)
internal val HomeHeroVioletWash = Color(0xFF6D28FF)
internal val HomeHeroLightViolet = Color(0xFF8A5FFF)
internal val HomeHeroTitle: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF4F3FF) else Color(0xFF1C1530)
internal val HomeHeroSecondary: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFB8BDD0) else Color(0xFF5F5B73)
internal val HomeHeroLabel: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
internal val HomeHeroStar: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA78BFA) else Color(0xFF7C3AED)
internal val HomeHeroStarSoft: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFC4B5FD) else Color(0xFF9B6CFF)
internal val HomeHeroStroke: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Color(0xFFA78BFA).copy(alpha = 0.13f)
    } else {
        Color(0xFFBDA8FF).copy(alpha = 0.54f)
    }
internal val HomeHeroAssetShadow: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF090018) else Color(0xFF7655D8)
internal val HomeHeroButtonStart = Color(0xFF581DD6)
internal val HomeHeroButtonEnd = Color(0xFF8A5FFF)
internal val HomeAccentPurple = Color(0xFF8F35FF)
internal val HomeAvatarPurpleTop = Color(0xFF9A42FF)
internal val HomeAvatarPurpleBottom = Color(0xFF6E22FF)
internal val HomeAccentTeal = Color(0xFF00E0B8)
internal val HomeAccentCoral = Color(0xFFFF3348)
internal val HomeAccentYellow = Color(0xFFFFB800)
internal val HomeTextPrimary = Color(0xFFF8F4FF)
internal val HomeTextSoft = Color(0xFFD3D0E0)
internal val HomeTextMuted = Color(0xFFA7ADBE)
internal val HomeStroke = Color(0xFF1A2230)
internal val HomeHeroLight = Color(0xFFFFFFFF)
internal val HomeShadow = Color(0xFF000000)
internal val HomeHeroLightModeStart = Color(0xFFFFFEFF)
internal val HomeHeroLightModeMid = Color(0xFFF6F0FF)
internal val HomeHeroLightModeTransition = Color(0xFFEDE3FF)
internal val HomeHeroLightModeEnd = Color(0xFFF8F4FF)
internal val HomeHeroLightModeGlow = Color(0xFFD9C7FF)
internal val HomeHeroLightModeAccent = Color(0xFFB892FF)
internal val HomeHeroLightModeDepth = Color(0xFF8E6AE8)
internal val HomeCompanionHeart = Color(0xFFC08CFF)
internal val HomePrioritySheetSurface: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) UniStackColors.Background else Color(0xFFFBFAFF)
internal val HomePrioritySheetSuggestion: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF15182B) else Color(0xFFF2ECFF)
internal val HomePrioritySheetText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFF4F3FF) else Color(0xFF171427)
internal val HomePrioritySheetBody: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFCDD2E3) else Color(0xFF555267)
internal val HomePrioritySheetMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF9EA6BA) else Color(0xFF747186)
internal val HomePrioritySheetSecondaryButton: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF202232) else Color(0xFFECEAF4)
internal val HomePrioritySheetIconCircle: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFF201044) else Color(0xFFEDE4FF)
internal val HomePrioritySheetCardBorder: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color.White.copy(alpha = 0.07f) else Color(0xFF7C3AED).copy(alpha = 0.14f)
internal val HomePrioritySheetSun = Color(0xFFFFD21F)
internal val HomePrioritySheetAccentSoft: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA855F7) else Color(0xFF8B35E8)
