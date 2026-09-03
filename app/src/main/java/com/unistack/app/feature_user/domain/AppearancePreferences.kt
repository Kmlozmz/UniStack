package com.unistack.app.feature_user.domain

data class AppearancePreferences(
    /**
     * El tema completo elegido, por su identificador de [AppThemes].
     *
     * Sustituye en la practica a [backgroundStyle] y a la parte de color de [accentStyle]:
     * aquellos guardaban trozos sueltos de una decision que siempre fue conjunta, y de los
     * cinco acentos que ofrecian solo uno llegaba a pintarse.
     */
    val themeId: String = "unistack",
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

    /** Cuanta sombra proyecta una tarjeta. Solo cuenta con [SurfaceStyle.ELEVATED]. */
    val shadowIntensity: ShadowIntensity = ShadowIntensity.MEDIA,

    /** Lo grueso que es el filete. Solo cuenta con [SurfaceStyle.OUTLINED]. */
    val outlineWeight: OutlineWeight = OutlineWeight.FINO,
    val cornerStyle: CornerStyle = CornerStyle.BALANCED,
    val interfaceDensity: InterfaceDensity = InterfaceDensity.BALANCED,
    val motionPreference: MotionPreference = MotionPreference.FULL,

    /**
     * Cada gesto de la app con su variante, en [MotionPreferences].
     *
     * Va como bloque aparte y no como treinta campos sueltos aqui: son veinticinco elecciones
     * y cuatro interruptores, y sueltos convertirian esta clase en una lista de la compra
     * donde ya no se distingue lo que decide el color de lo que decide como se tacha una
     * tarea. [motionPreference] sigue mandando por encima: en «reducido» o «nada», esto queda
     * guardado pero en pausa.
     */
    val motion: MotionPreferences = MotionPreferences(),
    val textScale: TextScalePreference = TextScalePreference.STANDARD,
    val typographyStyle: TypographyStyle = TypographyStyle.UNISTACK,

    /**
     * El tamano del texto, de 85 a 135 por ciento.
     *
     * Sustituye en la practica a [textScale], que solo tenia dos posiciones —normal y grande—
     * y se quedaba corto en los dos extremos: quien queria un pelin mas grande solo podia dar
     * el salto entero, y quien necesita el maximo no llegaba. [textScale] sigue guardandose
     * para no romper las copias de seguridad viejas.
     */
    val textScalePercent: Int = 100,

    /** El aire entre renglones. */
    val lineHeightStyle: LineHeightStyle = LineHeightStyle.NORMAL,
    val decimalPlaces: Int = 1,
    val bottomBarStyle: BottomBarStyle = BottomBarStyle.LABELED,

    /** La forma de los botones de accion. */
    val buttonShape: ButtonShapeStyle = ButtonShapeStyle.MEDIO,

    /** Cuanto ocupan. */
    val buttonSize: ButtonSizeStyle = ButtonSizeStyle.MEDIO,

    /** Como se ven los campos de texto. */
    val textFieldStyle: TextFieldStyle = TextFieldStyle.RELLENO,

    /** Como se ven los chips de filtro. */
    val chipStyle: ChipStyle = ChipStyle.FILETE,

    /** Redondeado, lineal o relleno: los iconos de la barra de abajo. */
    val iconStyle: IconStyle = IconStyle.REDONDEADO,

    /**
     * La forma del distintivo de cada materia.
     *
     * Con [BadgeShape.ALEATORIO] cada materia se queda con una forma propia, sacada de su
     * identificador: no cambia al reabrir la app, y dos materias del mismo color se distinguen
     * de un vistazo por la forma.
     */
    val badgeShape: BadgeShape = BadgeShape.CIRCULO,

    /** La linea fina entre filas de una lista. */
    val listDividers: Boolean = true,

    /** Con que dia empieza la semana en el calendario y en el grafico semanal. */
    val firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.LUNES,

    /**
     * Verde, ambar y rojo en las notas, o todo del color de acento.
     *
     * Apagado, la app deja de decir «bien o mal» con el color y lo dice solo con el numero.
     * Es lo que pide quien no distingue ese par.
     */
    val sectionColorsEnabled: Boolean = true,
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
        textScalePercent = textScalePercent.coerceIn(85, 135),
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

/**
 * La familia de letra de toda la app.
 *
 * Serif y mono se anaden a las dos de siempre: la serif es lo que pide quien lee mejor con
 * remates, y la mono alinea cifras por columnas, que en una lista de notas y de importes se
 * nota. Las cuatro salen de las familias del sistema, asi que ninguna suma peso al APK.
 */
enum class TypographyStyle {
    UNISTACK,
    SYSTEM,
    SERIF,
    MONO
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

/**
 * Cuanta sombra, cuando la superficie es «Sombra».
 *
 * **No hay «nada».** Una sombra de cero es una superficie plana, y plana ya es una de las
 * cuatro superficies: ofrecerla aqui otra vez daba dos caminos al mismo pixel.
 */
enum class ShadowIntensity {
    SUAVE,
    MEDIA,
    FUERTE
}

/** Lo grueso que es el filete, cuando la superficie es «Filete». */
enum class OutlineWeight {
    FINO,
    MEDIO,
    GRUESO
}

/** El aire entre renglones de un parrafo. */
enum class LineHeightStyle {
    COMPACTO,
    NORMAL,
    AMPLIO
}

enum class ButtonShapeStyle {
    RECTO,
    MEDIO,
    PASTILLA
}

enum class ButtonSizeStyle {
    PEQUENO,
    MEDIO,
    GRANDE
}

enum class TextFieldStyle {
    RELLENO,
    FILETE,
    SUBRAYADO
}

enum class ChipStyle {
    FILETE,
    RELLENO,
    TEXTO
}

/**
 * El trazo de los iconos de la barra de abajo.
 *
 * Redondeado y relleno son los dos juegos que Material trae —`Rounded` y `Filled`—; lineal es
 * el contorno fino, que en la barra deja la pestana activa distinguiendose por el color y no
 * por el peso.
 */
enum class IconStyle {
    REDONDEADO,
    LINEAL,
    RELLENO
}

/**
 * La forma del punto de color de cada materia.
 *
 * Las cinco primeras son formas de Material 3 Expressive. [ALEATORIO] no es una sexta forma:
 * reparte las cinco entre las materias de forma estable, sacando la que toca del identificador
 * de cada una, para que dos materias del mismo color no se confundan.
 */
enum class BadgeShape {
    CIRCULO,
    GALLETA,
    TREBOL,
    SOL,
    ROMBO,
    ALEATORIO
}

enum class FirstDayOfWeek(val isoDay: Int) {
    LUNES(1),
    DOMINGO(7)
}
