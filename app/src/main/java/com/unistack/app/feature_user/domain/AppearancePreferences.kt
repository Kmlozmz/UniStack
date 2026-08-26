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
    val academicIndicatorStyle: AcademicIndicatorStyle = AcademicIndicatorStyle.RINGS,
    val switchIconStyle: SwitchIconStyle = SwitchIconStyle.BOTH,
    /**
     * El orden en el que quieres ver tus materias, por identificador.
     *
     * Va aquí y no en la tabla de materias para no pedir una migración de base de datos por un
     * dato que solo mira la pantalla. Las materias que no estén en la lista —recién creadas, o
     * de antes de que esto existiera— van al final; las que estén y ya no existan se ignoran.
     * Vacía significa el orden en que se crearon, que es lo que había hasta ahora.
     */
    val subjectOrder: List<String> = emptyList(),
    val academicProgressShape: ProgressShape = ProgressShape.FLAT,
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
 * Si el pulgar del interruptor lleva un icono dentro, y en qué estados.
 *
 * Material 3 Expressive lo permite con `thumbContent`, y es lo que deja leer un interruptor sin
 * depender solo del color: quien no distingue el violeta encendido del gris apagado sí distingue
 * un visto de un aspa.
 *
 * Es preferencia y no decisión cerrada porque el icono añade ruido a una lista larga de
 * interruptores, y hay a quien le estorba. Por defecto va en los dos estados.
 */
enum class SwitchIconStyle {
    /** Visto al encender, aspa al apagar. */
    BOTH,

    /** Visto solo al encender; apagado, el pulgar va liso. */
    CHECKED_ONLY,

    /** Sin icono, como estuvo hasta ahora. */
    NONE
}

/**
 * La forma de las barras y anillos de progreso **académico**.
 *
 * Solo el académico: semestre, materia, checklist de un trabajo, presupuesto. El progreso del
 * sistema —descargas, guardado, carga de una imagen— se queda fijo en ondulado y no se ofrece,
 * porque nadie quiere configurar cómo se ve una descarga.
 */
enum class ProgressShape {
    /** La onda de Material 3 Expressive. */
    WAVY,

    /**
     * Línea recta con indicador de parada al final. Es la de por defecto: la onda tiene
     * gracia la primera vez y estorba en un dato que se mira todos los días.
     */
    FLAT
}

/**
 * Cómo cambia una pantalla por otra.
 *
 * Es una preferencia y no una decisión cerrada porque no hay una respuesta buena: el
 * deslizamiento cuenta jerarquía y molesta a quien entra y sale cincuenta veces al día, y el
 * fundido no cuenta nada y por eso no se equivoca. Cada quien nota una cosa distinta.
 */
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
