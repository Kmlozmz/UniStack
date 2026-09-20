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
    val cornerStyle: CornerStyle = CornerStyle.BALANCED,
    val interfaceDensity: InterfaceDensity = InterfaceDensity.BALANCED,

    /**
     * Cómo se ven las materias en Académico: tarjeta con su barra, o fila de lista.
     *
     * Con ocho materias, la lista cabe en una pantalla. Es de las pocas cosas de forma que
     * cambian de una persona a otra (20 sep 2026).
     */
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
    val typographyStyle: TypographyStyle = TypographyStyle.SYSTEM,

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

    /** Cuanto ocupan. */

    /** Cómo van los iconos de las filas de Ajustes: de colores o en círculo con el acento. */
    /**
     * Los iconos de Ajustes: de qué color van y qué forma tienen, por separado (20 sep 2026).
     *
     * Eran una sola elección —cuadrados de colores o círculos con el acento— y él quiso poder
     * cruzarlas: el color como estaba, y un interruptor aparte para la forma.
     */
    val settingsIconColor: SettingsIconColor = SettingsIconColor.COLORES,
    val settingsIconRound: Boolean = false,

    /** Un contador sobre Académico con las entregas pendientes. */
    val tabBadges: Boolean = true,

    /** Con que dia empieza la semana en el calendario y en el grafico semanal. */
    val firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.LUNES,
    val academicIndicatorStyle: AcademicIndicatorStyle = AcademicIndicatorStyle.RINGS,
    /**
     * El orden en el que quieres ver tus materias, por identificador.
     *
     * Va aquí y no en la tabla de materias para no pedir una migración de base de datos por un
     * dato que solo mira la pantalla. Las materias que no estén en la lista —recién creadas, o
     * de antes de que esto existiera— van al final; las que estén y ya no existan se ignoran.
     * Vacía significa el orden en que se crearon, que es lo que había hasta ahora.
     */
    val subjectOrder: List<String> = emptyList(),
    val showHomeGreeting: Boolean = true,
    /** «Buenas tardes» según la hora; apagado, un «Hola» a secas. */
    val greetingWithTimeOfDay: Boolean = true,
    val showHomeHero: Boolean = true,
    val showHomeAgenda: Boolean = true,
    val showHomeSnapshot: Boolean = true,
    /*
     * Los cinco bloques que entraron el 20 sep 2026, apagados de fábrica: Inicio no crece
     * para nadie sin pedirlo. Se encienden en Apariencia › Tu inicio.
     */
    val showHomeSubjects: Boolean = false,
    val showHomeWeek: Boolean = false,
    val showHomeAttendance: Boolean = false,
    val showHomeExpenses: Boolean = false,
    val showHomeNotes: Boolean = false,
    val homeSectionOrder: List<HomeSection> = HomeSection.entries,
    val heroAutoRotate: Boolean = true,
    val initialTab: InitialTab = InitialTab.HOME,
    val visualPreset: VisualPreset = VisualPreset.CUSTOM
) {
    /** Si un bloque de Inicio está encendido. El orden va aparte, en [homeSectionOrder]. */
    fun showsSection(section: HomeSection): Boolean = when (section) {
        HomeSection.HERO -> showHomeHero
        HomeSection.AGENDA -> showHomeAgenda
        HomeSection.SNAPSHOT -> showHomeSnapshot
        HomeSection.SUBJECTS -> showHomeSubjects
        HomeSection.WEEK -> showHomeWeek
        HomeSection.ATTENDANCE -> showHomeAttendance
        HomeSection.EXPENSES -> showHomeExpenses
        HomeSection.NOTES -> showHomeNotes
    }

    fun withSection(section: HomeSection, shown: Boolean): AppearancePreferences = when (section) {
        HomeSection.HERO -> copy(showHomeHero = shown)
        HomeSection.AGENDA -> copy(showHomeAgenda = shown)
        HomeSection.SNAPSHOT -> copy(showHomeSnapshot = shown)
        HomeSection.SUBJECTS -> copy(showHomeSubjects = shown)
        HomeSection.WEEK -> copy(showHomeWeek = shown)
        HomeSection.ATTENDANCE -> copy(showHomeAttendance = shown)
        HomeSection.EXPENSES -> copy(showHomeExpenses = shown)
        HomeSection.NOTES -> copy(showHomeNotes = shown)
    }

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
                cornerStyle = CornerStyle.COMPACT,
                interfaceDensity = InterfaceDensity.COMPACT,
                bottomBarStyle = BottomBarStyle.ICONS_ONLY,
                academicIndicatorStyle = AcademicIndicatorStyle.NUMBERS,
                visualPreset = VisualPreset.MINIMAL
            )
            VisualPreset.OLED -> defaults().copy(
                backgroundStyle = BackgroundStyle.PURE,
                accentIntensity = AccentIntensity.VIBRANT,
                visualPreset = VisualPreset.OLED
            )
            VisualPreset.FOCUS -> defaults().copy(
                accentStyle = AccentStyle.TEAL,
                accentIntensity = AccentIntensity.SOFT,
                interfaceDensity = InterfaceDensity.COMFORTABLE,
                showHomeSnapshot = false,
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

/**
 * Lo que queda de la eleccion de acento vieja, solo para leer copias de seguridad antiguas.
 *
 * **Ya no decide nada.** El acento sale del tema, o de [AppearancePreferences.customAccentColor]
 * si se ha elegido uno a mano. `DYNAMIC` —el color del fondo de pantalla, Material You— se
 * retiro entero: con veintiocho temas y un acento a medida no anadia nada, y mientras estaba
 * encendido dejaba los temas en pausa y media pantalla de ajustes sin pintar.
 *
 * El enum se conserva porque el valor viaja en las copias de seguridad y en el JSON del disco;
 * borrarlo haria que una copia de hace dos versiones no se pudiera leer.
 */
@Deprecated("El acento sale del tema o de customAccentColor; esto solo se lee de copias viejas.")
enum class AccentStyle {
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
 * **Ya no se llama «UniStack».** Se llamaba asi la de por defecto y era mentira: no hay ninguna
 * fuente propia. La de por defecto es [SYSTEM], la del telefono, desde el 20 sep 2026.
 *
 * Dos son del sistema ([SYSTEM], [SANS]), una se pide por nombre de dispositivo ([ESTRECHA]:
 * si no esta, cae en la `sans-serif` normal) y las ocho restantes van empaquetadas en
 * `res/font` como fuentes variables bajo la SIL Open Font License: unos dos megas en total,
 * a cambio de que se vean igual en cualquier telefono. Serif, Mono y Redondeada del sistema
 * se fueron con ellas: [LORA], [JETBRAINS_MONO] y [NUNITO] hacen lo mismo sin depender del
 * fabricante.
 */
enum class TypographyStyle {
    /** La que el fabricante haya puesto como suya: Samsung One, MIUI Sans, la que sea. */
    SYSTEM,

    /** La `sans-serif` del sistema. La de siempre. */
    SANS,

    INTER,
    MANROPE,
    DM_SANS,
    OUTFIT,
    SPACE_GROTESK,

    /** De trazo redondeado y amable. */
    NUNITO,

    /** Con remates: se lee bien en parrafos largos. */
    LORA,

    /** Ancho fijo: alinea cifras por columnas, que en notas e importes se nota. */
    JETBRAINS_MONO,

    /** Condensada: cabe mas nombre de materia antes de cortarse. */
    ESTRECHA
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

/**
 * Los bloques de Inicio, en el orden de fábrica.
 *
 * Los tres primeros son los de siempre; los cinco de después entraron el 20 sep 2026 y van
 * apagados hasta que alguien los encienda. Lo guardado con solo tres se completa al final con
 * el resto en [AppearancePreferences.normalized].
 */
enum class HomeSection {
    HERO,
    AGENDA,
    SNAPSHOT,
    SUBJECTS,
    WEEK,
    ATTENDANCE,
    EXPENSES,
    NOTES
}

/**
 * La pestaña en la que arranca la app.
 *
 * [TASKS] ya no se ofrece —Académico es una sola pestaña de la barra— pero sigue existiendo
 * para lo guardado antes del 20 sep 2026, que abre Académico igual.
 */
enum class InitialTab {
    HOME,
    GRADES,
    TASKS,
    SCHEDULE,
    EXPENSES
}



/** El aire entre renglones de un parrafo. */
enum class LineHeightStyle {
    COMPACTO,
    NORMAL,
    AMPLIO
}





/**
 * Los iconos de las filas de Ajustes. Por ahora solo esos; el resto de la app no cambia.
 *
 * Los dos se pidieron el 16 sep 2026, para poder elegir entre ellos.
 */
enum class SettingsIconColor {
    /** Cada uno lleva el color de lo que hace su fila. */
    COLORES,

    /** Todos con el acento. */
    ACENTO
}

/** El valor guardado antes del 20 sep 2026, que solo sirve para migrarlo al arrancar. */
enum class SettingsIconStyle {
    /** Cuadrado relleno, un color por lo que hace cada fila. El de siempre. */
    COLOR,

    /** Círculo del tono de la tarjeta con el icono en el acento, como en la simulación. */
    CIRCULO
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
