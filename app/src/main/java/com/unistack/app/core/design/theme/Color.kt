package com.unistack.app.core.design.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

object UniStackColors {
    private val lightPalette = UniStackColorPalette(
        primary = Color(0xFF6750F5),
        primaryDark = Color(0xFF2E1A78),
        primaryLight = Color(0xFFE9DDFF),
        blue = Color(0xFF1E7BEA),
        blueLight = Color(0xFFDDEBFF),
        teal = Color(0xFF00AFA5),
        tealLight = Color(0xFFE0F8F5),
        green = Color(0xFF4CAF50),
        greenLight = Color(0xFFE8F6E8),
        coral = Color(0xFFEF5B4D),
        coralLight = Color(0xFFFFE1DC),
        yellow = Color(0xFFE2A900),
        yellowLight = Color(0xFFFFF4CC),
        background = Color(0xFFFCFBFF),
        card = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF4F0FA),
        textPrimary = Color(0xFF171427),
        textSecondary = Color(0xFF5F5B6B),
        softOutline = Color(0xFFDCD2EA),
        gradientEnd = Color(0xFFFFFFFF),
        bottomBar = Color(0xFFFFFCFF),
        bottomBarSelected = Color(0xFFF0EAFF)
    )

    private val darkPalette = UniStackColorPalette(
        primary = Color(0xFF8F35FF),
        primaryDark = Color(0xFFF1E7FF),
        primaryLight = Color(0xFF24105C),
        blue = Color(0xFF8F35FF),
        blueLight = Color(0xFF1D1048),
        teal = Color(0xFF00E0B8),
        tealLight = Color(0xFF063D35),
        green = Color(0xFF74C981),
        greenLight = Color(0xFF193420),
        coral = Color(0xFFFF3348),
        coralLight = Color(0xFF421522),
        yellow = Color(0xFFFFB800),
        yellowLight = Color(0xFF4A3308),
        background = Color(0xFF070B14),
        card = Color(0xFF080D17),
        surfaceVariant = Color(0xFF0B111D),
        textPrimary = Color(0xFFF8F4FF),
        textSecondary = Color(0xFFD3D0E0),
        softOutline = Color(0xFF1A2230),
        gradientEnd = Color(0xFF000309),
        bottomBar = Color(0xFF050913),
        bottomBarSelected = Color(0xFF24105C)
    )

    private var appliedDarkTheme = false

    var IsDarkTheme by mutableStateOf(false)
        private set
    var Primary by mutableStateOf(lightPalette.primary)
        private set
    var PrimaryDark by mutableStateOf(lightPalette.primaryDark)
        private set
    var PrimaryLight by mutableStateOf(lightPalette.primaryLight)
        private set
    var Blue by mutableStateOf(lightPalette.blue)
        private set
    var BlueLight by mutableStateOf(lightPalette.blueLight)
        private set
    var Teal by mutableStateOf(lightPalette.teal)
        private set
    var TealLight by mutableStateOf(lightPalette.tealLight)
        private set
    var Green by mutableStateOf(lightPalette.green)
        private set
    var GreenLight by mutableStateOf(lightPalette.greenLight)
        private set
    var Coral by mutableStateOf(lightPalette.coral)
        private set
    var CoralLight by mutableStateOf(lightPalette.coralLight)
        private set
    var Yellow by mutableStateOf(lightPalette.yellow)
        private set
    var YellowLight by mutableStateOf(lightPalette.yellowLight)
        private set
    var Background by mutableStateOf(lightPalette.background)
        private set
    var Card by mutableStateOf(lightPalette.card)
        private set
    var SurfaceVariant by mutableStateOf(lightPalette.surfaceVariant)
        private set
    var TextPrimary by mutableStateOf(lightPalette.textPrimary)
        private set
    var TextSecondary by mutableStateOf(lightPalette.textSecondary)
        private set
    var SoftOutline by mutableStateOf(lightPalette.softOutline)
        private set
    var GradientEnd by mutableStateOf(lightPalette.gradientEnd)
        private set
    var BottomBar by mutableStateOf(lightPalette.bottomBar)
        private set
    var BottomBarSelected by mutableStateOf(lightPalette.bottomBarSelected)
        private set

    internal fun applyTheme(darkTheme: Boolean) {
        if (appliedDarkTheme == darkTheme) return

        val palette = if (darkTheme) darkPalette else lightPalette
        appliedDarkTheme = darkTheme
        IsDarkTheme = darkTheme
        Primary = palette.primary
        PrimaryDark = palette.primaryDark
        PrimaryLight = palette.primaryLight
        Blue = palette.blue
        BlueLight = palette.blueLight
        Teal = palette.teal
        TealLight = palette.tealLight
        Green = palette.green
        GreenLight = palette.greenLight
        Coral = palette.coral
        CoralLight = palette.coralLight
        Yellow = palette.yellow
        YellowLight = palette.yellowLight
        Background = palette.background
        Card = palette.card
        SurfaceVariant = palette.surfaceVariant
        TextPrimary = palette.textPrimary
        TextSecondary = palette.textSecondary
        SoftOutline = palette.softOutline
        GradientEnd = palette.gradientEnd
        BottomBar = palette.bottomBar
        BottomBarSelected = palette.bottomBarSelected
    }
}

private data class UniStackColorPalette(
    val primary: Color,
    val primaryDark: Color,
    val primaryLight: Color,
    val blue: Color,
    val blueLight: Color,
    val teal: Color,
    val tealLight: Color,
    val green: Color,
    val greenLight: Color,
    val coral: Color,
    val coralLight: Color,
    val yellow: Color,
    val yellowLight: Color,
    val background: Color,
    val card: Color,
    val surfaceVariant: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val softOutline: Color,
    val gradientEnd: Color,
    val bottomBar: Color,
    val bottomBarSelected: Color
)
