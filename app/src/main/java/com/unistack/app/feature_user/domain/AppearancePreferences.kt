package com.unistack.app.feature_user.domain

data class AppearancePreferences(
    val backgroundStyle: BackgroundStyle = BackgroundStyle.DEFAULT,
    val customBackgroundColor: Int? = null,
    val customThemeBase: CustomThemeBase = CustomThemeBase.SYSTEM,
    // El acento de marca manda por defecto. Monet queda a un toque de distancia en
    // Apariencia, pero dejarlo de serie hacía que la app se viera del color del fondo
    // de pantalla de cada quien: UniStack no tenía identidad propia en su propia app.
    val accentStyle: AccentStyle = AccentStyle.VIOLET,
    val customAccentColor: Int? = null,
    val accentIntensity: AccentIntensity = AccentIntensity.BALANCED,
    val surfaceStyle: SurfaceStyle = SurfaceStyle.OUTLINED,
    val cornerStyle: CornerStyle = CornerStyle.BALANCED,
    val interfaceDensity: InterfaceDensity = InterfaceDensity.BALANCED,
    val motionPreference: MotionPreference = MotionPreference.FULL,
    val textScale: TextScalePreference = TextScalePreference.STANDARD,
    val typographyStyle: TypographyStyle = TypographyStyle.UNISTACK,
    val decimalPlaces: Int = 1,
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.LABELED,
    val screenTransition: ScreenTransition = ScreenTransition.PUSH,
    val academicIndicatorStyle: AcademicIndicatorStyle = AcademicIndicatorStyle.RINGS,
    val showHomeGreeting: Boolean = true,
    val showHomeHero: Boolean = true,
    val showHomeAgenda: Boolean = true,
    val showHomeSnapshot: Boolean = true,
    val homeSectionOrder: List<HomeSection> = HomeSection.entries,
    val heroAutoRotate: Boolean = true,
    val heroShowsGrades: Boolean = true,
    val heroShowsTasks: Boolean = true,
    val heroShowsExpenses: Boolean = true,
    val initialTab: InitialTab = InitialTab.HOME,
    val visualPreset: VisualPreset = VisualPreset.CUSTOM
) {
    fun normalized(): AppearancePreferences = copy(
        decimalPlaces = decimalPlaces.coerceIn(0, 2),
        homeSectionOrder = homeSectionOrder
            .distinct()
            .let { current -> current + HomeSection.entries.filterNot(current::contains) }
    )

    companion object {
        fun defaults() = AppearancePreferences()

        fun preset(preset: VisualPreset): AppearancePreferences = when (preset) {
            VisualPreset.DEFAULT -> defaults().copy(visualPreset = VisualPreset.DEFAULT)
            VisualPreset.MINIMAL -> defaults().copy(
                surfaceStyle = SurfaceStyle.FLAT,
                cornerStyle = CornerStyle.COMPACT,
                interfaceDensity = InterfaceDensity.COMPACT,
                bottomBarStyle = BottomBarStyle.ICONS_ONLY,
                academicIndicatorStyle = AcademicIndicatorStyle.NUMBERS,
                visualPreset = VisualPreset.MINIMAL
            )
            VisualPreset.OLED -> defaults().copy(
                backgroundStyle = BackgroundStyle.PURE,
                surfaceStyle = SurfaceStyle.OUTLINED,
                accentIntensity = AccentIntensity.VIBRANT,
                visualPreset = VisualPreset.OLED
            )
            VisualPreset.FOCUS -> defaults().copy(
                accentStyle = AccentStyle.TEAL,
                accentIntensity = AccentIntensity.SOFT,
                interfaceDensity = InterfaceDensity.COMFORTABLE,
                showHomeSnapshot = false,
                heroShowsExpenses = false,
                visualPreset = VisualPreset.FOCUS
            )
            VisualPreset.CUSTOM -> defaults()
        }
    }
}

enum class BackgroundStyle {
    DEFAULT,
    PURE,
    COOL,
    VIOLET,
    CUSTOM
}

enum class CustomThemeBase {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AccentStyle {
    /** Toma el color del fondo de pantalla del sistema (Material You / Monet, Android 12+). */
    DYNAMIC,
    VIOLET,
    BLUE,
    TEAL,
    GREEN,
    PINK,
    CUSTOM
}

enum class AccentIntensity {
    SOFT,
    BALANCED,
    VIBRANT
}

enum class SurfaceStyle {
    FLAT,
    OUTLINED,
    ELEVATED,
    TRANSLUCENT
}

enum class CornerStyle {
    COMPACT,
    BALANCED,
    SOFT
}

enum class InterfaceDensity {
    COMPACT,
    BALANCED,
    COMFORTABLE
}

enum class MotionPreference {
    FULL,
    REDUCED,
    NONE
}

enum class TextScalePreference {
    STANDARD,
    LARGE
}

enum class TypographyStyle {
    UNISTACK,
    SYSTEM
}

enum class BottomBarStyle {
    LABELED,
    ICONS_ONLY
}

/**
 * Cómo cambia una pantalla por otra.
 *
 * Es una preferencia y no una decisión cerrada porque no hay una respuesta buena: el
 * deslizamiento cuenta jerarquía y molesta a quien entra y sale cincuenta veces al día, y el
 * fundido no cuenta nada y por eso no se equivoca. Cada quien nota una cosa distinta.
 */
enum class ScreenTransition {
    /** La nueva entra entera desde el borde y empuja a la anterior. Lo que viene puesto. */
    PUSH,

    /** Aparece y desaparece, sin recorrido. */
    FADE,

    /** Sin transición: la pantalla se sustituye. */
    NONE
}


enum class AcademicIndicatorStyle {
    RINGS,
    BARS,
    NUMBERS
}

enum class VisualPreset {
    DEFAULT,
    MINIMAL,
    OLED,
    FOCUS,
    CUSTOM
}

enum class HomeSection {
    HERO,
    AGENDA,
    SNAPSHOT
}

enum class InitialTab {
    HOME,
    GRADES,
    TASKS,
    EXPENSES
}
