package com.unistack.app.feature_user.domain

/**
 * Cómo se mueve la app, gesto a gesto.
 *
 * Hasta ahora el movimiento era **un interruptor de tres posiciones** —[MotionPreference]:
 * completo, reducido o nada— y todo lo demás estaba decidido en el código. Aquí cada momento
 * de la app en el que algo se mueve es una elección propia: cómo entra una pantalla, cómo se
 * tacha una tarea, qué hace la app cuando te pasas del presupuesto.
 *
 * Son veinte gestos con ciento once variantes entre todos, y por eso **no hay un campo por
 * cada uno en el JSON ni una fila escrita a mano en la pantalla**: el catálogo de
 * [MotionCatalog] es el que dice qué gestos hay, qué variantes tiene cada uno y cómo se lee y
 * se escribe la elección. Guardar, cargar y pintar salen todos de ahí, así que añadir una
 * variante es añadir una entrada a un enum y nada más.
 *
 * [MotionPreference] sigue existiendo y sigue mandando: puesto en «reducido» o «nada», lo de
 * aquí queda en pausa. Es lo que hace que quien lo necesite apague todo de un toque sin tener
 * que recorrer veinte ajustes.
 */
data class MotionPreferences(
    // ------------------------------------------------------------------ base
    val speed: MotionSpeed = MotionSpeed.NORMAL,
    val bounce: SpringBounce = SpringBounce.VIVO,
    val press: PressEffect = PressEffect.ONDA,
    val loading: LoadingStyle = LoadingStyle.FORMAS,

    // ------------------------------------------------------------------ transiciones
    val screenTransition: ScreenTransition = ScreenTransition.EJE,
    val listEntry: ListEntry = ListEntry.ESCALONADA,
    val refresh: RefreshStyle = RefreshStyle.ONDA_CIRCULAR,

    // ------------------------------------------------------------------ académico
    val attendance: AttendanceMotion = AttendanceMotion.RELLENO,
    val newGrade: NewGradeMotion = NewGradeMotion.LATERAL,
    val cutSeal: CutSealMotion = CutSealMotion.TINTA,
    val termClose: TermCloseMotion = TermCloseMotion.APILADO,

    // ------------------------------------------------------------------ tareas y notas
    val celebration: CelebrationMotion = CelebrationMotion.CONFETI,
    val strikeThrough: StrikeMotion = StrikeMotion.LINEA,
    val overdueBeat: OverdueBeat = OverdueBeat.RESPIRA,
    val undo: UndoMotion = UndoMotion.REBOTA,
    val autosave: AutosaveMotion = AutosaveMotion.PILDORA,
    val pinNote: PinMotion = PinMotion.SALTA,

    // ------------------------------------------------------------------ gastos y avisos
    /*
     * Pasarse del presupuesto ya no es un gesto con variantes: es **el aviso arriba**, y
     * siempre. Las otras cuatro —contorno, sacudida, parpadeo, seco— se miraron juntas en
     * Gastos y todas se veian igual: una fila con un halo rojo que no decia nada que la
     * franja de arriba no dijera mejor. Se quitaron a proposito, con su apartado.
     */
    val classNow: ClassNowMotion = ClassNowMotion.RESPIRA,

    // ------------------------------------------------------------------ generales
    val fabOnScroll: FabScrollMotion = FabScrollMotion.ENCOGE,

    // ------------------------------------------------------------------ otros
    val animatedBottomBar: Boolean = true,
    val swipeGestures: Boolean = true,
    val countingNumbers: Boolean = true,
    val haptics: HapticStrength = HapticStrength.MEDIA
) {
    companion object {
        fun defaults() = MotionPreferences()
    }
}

/**
 * Una variante de un gesto: lo que se guarda y lo que se lee en la pantalla.
 *
 * El [id] es lo que viaja al disco y a la copia de seguridad, y no cambia aunque cambie el
 * nombre visible. Es lo que deja renombrar «Onda expansiva» sin que a nadie se le pierda la
 * elección que tenía hecha.
 */
interface MotionChoice {
    val id: String
    val label: String
    val displayLabel: String get() {
        if (java.util.Locale.getDefault().language != "en") return label
        return when (id) {
            "instant" -> "None"
            "rapida" -> "Fast"
            "normal" -> "Normal"
            "lenta" -> "Slow"
            "suave" -> "Soft"
            "medio" -> "Medium"
            "vivo" -> "Vibrant"
            "ninguna" -> "None"
            "onda" -> "Wave"
            "formas" -> "Shapes"
            "puntos" -> "Dots"
            "circular" -> "Circular"
            "eje" -> "Axis"
            "fundido" -> "Fade"
            "desliza" -> "Slide"
            "escalonada" -> "Staggered"
            "cascada" -> "Waterfall"
            "pulso" -> "Pulse"
            "cae" -> "Drop"
            "rebota" -> "Bounce"
            "salto" -> "Jump"
            "brillo" -> "Glow"
            "viaje" -> "Shift"
            "estampa" -> "Stamp"
            "apilado" -> "Stacked"
            "confeti" -> "Confetti"
            "estrellas" -> "Stars"
            "linea" -> "Line"
            "desvanece" -> "Fade"
            "pildora" -> "Pill"
            "salta" -> "Jump"
            "alerta" -> "Alert"
            "respira" -> "Breathe"
            "sacude" -> "Shake"
            "escalonado" -> "Staggered"
            "encoge" -> "Shrink"
            else -> label
        }
    }
}

// ---------------------------------------------------------------------- base

enum class MotionSpeed(override val id: String, override val label: String, val factor: Float) : MotionChoice {
    /** Todo instantáneo. Es lo que pide quien se marea con el movimiento. */
    INSTANTANEA("instant", "Nada", 0f),
    RAPIDA("rapida", "Rápida", 0.6f),
    NORMAL("normal", "Normal", 1f),
    LENTA("lenta", "Lenta", 1.5f)
}

enum class SpringBounce(override val id: String, override val label: String, val damping: Float) : MotionChoice {
    /** Sin rebote visible: llega y se para. */
    SUAVE("suave", "Suave", 1f),
    VIVO("vivo", "Vivo", 0.45f)
}

enum class PressEffect(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    ONDA("onda", "Onda")
}

enum class LoadingStyle(override val id: String, override val label: String) : MotionChoice {
    CIRCULO("circulo", "Círculo"),

    /** El indicador de M3E que cambia de forma mientras gira. */
    FORMAS("formas", "Formas"),
    ONDA("onda", "Onda"),
    PUNTOS("puntos", "Puntos")
}

// ---------------------------------------------------------------------- transiciones

enum class ScreenTransition(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    FUNDIDO("fundido", "Fundido"),

    /** El empuje lateral de M3E: la que sale va a la izquierda, la que entra viene de la derecha. */
    EJE("eje", "Eje"),
    CONTENEDOR("contenedor", "Contenedor"),
    ABAJO("abajo", "Desde abajo"),
    ZOOM("zoom", "Zoom"),

    /** La de arriba: entra bajando, como un panel que se descuelga. */
    ARRIBA("arriba", "Desde arriba"),

    /** El empuje del eje, pero en vertical: la que sale sube un tercio y espera debajo. */
    EJE_VERTICAL("ejeV", "Eje vertical"),

    /** La nueva sube entera y la anterior se queda detrás, encogida y apagada. */
    TARJETA("tarjeta", "Tarjeta"),

    /** Entra desde la esquina, moviéndose en los dos ejes a la vez. */
    DIAGONAL("diagonal", "Diagonal")
}

enum class ListEntry(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    FUNDIDO("fundido", "Fundido"),

    /** Cada fila entra un poco después que la anterior, desde abajo. */
    ESCALONADA("escalonada", "Escalonada"),

    /** Como la escalonada, pero cayendo desde arriba y con más retardo entre filas. */
    CASCADA("cascada", "Cascada"),

    /** Crece desde el 92%, sin desplazarse. */
    ESCALA("escala", "Escala"),

    /** Entra desde abajo con muelle: se pasa de largo y vuelve. */
    RESORTE("resorte", "Resorte"),

    /** Gira desde el borde izquierdo, como cartas que se abren. */
    ABANICO("abanico", "Abanico")
}

enum class RefreshStyle(override val id: String, override val label: String) : MotionChoice {
    CIRCULO("circulo", "Círculo"),

    /** El `CircularWavyProgressIndicator` de M3E: un arco cuyo radio ondula. */
    ONDA_CIRCULAR("ondacirc", "Onda circular"),

    /** El `LoadingIndicator` de M3E, que va cambiando de forma. */
    FORMAS("formas", "Formas")
}

// ---------------------------------------------------------------------- académico

/**
 * Como se llena la rueda al marcar una clase.
 *
 * «Trazo» —el visto dibujandose de una linea— se retiro: sobre una rueda de 44 dp el trazo
 * es tan corto que no se distingue de aparecer, y al lado de las otras tres no hacia nada.
 */
enum class AttendanceMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    RELLENO("relleno", "Relleno"),
    REBOTE("rebote", "Rebote"),
    BARRIDO("barrido", "Barrido")
}

enum class NewGradeMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),

    /** La nota cae en la lista y empuja a las de abajo. */
    CAE("cae", "Cae y empuja"),
    LATERAL("lateral", "Desde el lado"),
    DESTELLO("destello", "Destello"),

    /** El promedio sube contando hasta el valor nuevo. */
    CONTAR("contar", "Promedio cuenta"),
    ABRE("abre", "Se abre hueco")
}

enum class CutSealMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    ESTAMPA("estampa", "Estampado"),
    TINTA("tinta", "Tinta"),
    CINTA("cinta", "Cinta")
}

enum class TermCloseMotion(override val id: String, override val label: String) : MotionChoice {
    ENTERO("entero", "Entero"),
    PIEZA("pieza", "Pieza a pieza"),
    CORTINA("cortina", "Cortina"),
    APILADO("apilado", "Apilado")
}

// ---------------------------------------------------------------------- tareas y notas

enum class CelebrationMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    CONFETI("confeti", "Confeti"),
    ONDA("onda", "Onda expansiva"),
    SELLO("sello", "Sello"),
    DESTELLO("destello", "Destello")
}

enum class StrikeMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    LINEA("linea", "Línea"),
    MARCADOR("marcador", "Marcador"),
    VISTO("visto", "Visto encima"),
    TINTA("tinta", "Tinta que cala")
}

enum class OverdueBeat(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    PULSO("pulso", "Pulso"),
    RESPIRA("respira", "Respira"),
    /** Un cerco que late alrededor de la fila, sin moverla. */
    HALO("halo", "Halo"),

    /** Solo late la franja roja del borde: la fila entera se queda quieta. */
    FRANJA("franja", "Solo la franja")
}

enum class UndoMotion(override val id: String, override val label: String) : MotionChoice {
    APARECE("aparece", "Aparece"),
    VUELVE("vuelve", "Vuelve deslizando"),
    CAE("cae", "Cae"),
    DESPLIEGA("despliega", "Se despliega"),
    REBOTA("rebota", "Rebota"),
    GIRA("gira", "Gira al volver"),
    DESTELLO("destello", "Con destello")
}

enum class AutosaveMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    PILDORA("pildora", "Píldora"),
    PUNTO("punto", "Punto"),
    VISTO("visto", "Visto"),
    ANILLO("anillo", "Anillo"),
    FILETE("filete", "Filete arriba"),
    NUBE("nube", "Nube")
}

enum class PinMotion(override val id: String, override val label: String) : MotionChoice {
    SECO("seco", "Seco"),
    SALTA("salta", "Salta"),
    VUELA("vuela", "Vuela en arco"),
    IMAN("iman", "Imán"),
    DESPEGA("despega", "Despega"),
    DESTELLO("destello", "Destello")
}

// ---------------------------------------------------------------------- gastos y avisos

/**
 * La clase que está pasando ahora mismo. **En verde**, que es lo que dice «activo».
 *
 * Estuvo en el color de acento, y ahí competía con todo lo demás que va del color de acento:
 * un morado más entre morados no decía «esto está pasando ahora».
 */
enum class ClassNowMotion(override val id: String, override val label: String) : MotionChoice {
    QUIETA("quieta", "Quieta"),
    RESPIRA("respira", "Respira"),
    PUNTO("punto", "Punto que late"),
    RECORRE("recorre", "Borde que recorre"),
    BARRE("barre", "Brillo que barre")
}

enum class FabScrollMotion(override val id: String, override val label: String) : MotionChoice {
    FIJO("fijo", "Fijo"),

    /** Pierde el texto y se queda redondo. */
    ENCOGE("encoge", "Se encoge")
}

enum class HapticStrength(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    MEDIA("media", "Media")
}

/**
 * Un gesto del catálogo: lo que hace falta para guardarlo, leerlo y pintarlo.
 *
 * Lleva las funciones de leer y escribir dentro porque es lo que permite que la pantalla de
 * Movimiento y el guardado en disco recorran la misma lista en vez de repetir veinte veces el
 * mismo `when`. Sin esto, añadir un gesto serían cuatro sitios que tocar.
 */
data class MotionGesture(
    val id: String,
    val group: String,
    val name: String,
    val detail: String,
    val options: List<MotionChoice>,
    val read: (MotionPreferences) -> MotionChoice,
    val write: (MotionPreferences, MotionChoice) -> MotionPreferences
) {
    val displayName: String get() {
        if (java.util.Locale.getDefault().language != "en") return name
        return when (id) {
            "velocidad" -> "Speed"
            "carga" -> "Loading indicator"
            "transicion" -> "Between screens"
            "listas" -> "List entry"
            "asistencia" -> "Mark attendance"
            "notaNueva" -> "Log a grade"
            "subeNota" -> "Grade increase"
            "recupera" -> "Subject recovery"
            "sello" -> "Term close seal"
            "celebracion" -> "Day completion celebration"
            "tachar" -> "Cross off on complete"
            "latido" -> "Overdue heartbeat"
            "guardado" -> "Autosave"
            "fijar" -> "Pin note"
            "claseAhora" -> "Class in session"
            "errorShake" -> "Error alert"
            "saludo" -> "Greeting on open"
            else -> name
        }
    }

    val displayDetail: String get() {
        if (java.util.Locale.getDefault().language != "en") return detail
        return when (id) {
            "velocidad" -> "Multiplies all animation durations."
            "carga" -> "Material 3 Expressive morphs while spinning."
            "transicion" -> "How a new screen enters."
            "listas" -> "How rows appear when opening."
            "asistencia" -> "When confirming attendance to class."
            "notaNueva" -> "When a new grade is entered for the term."
            "subeNota" -> "When your average improves."
            "recupera" -> "When rising out of the red."
            "sello" -> "When closing a term."
            "celebracion" -> "When completing the last pending item."
            "tachar" -> "Before disappearing from the list."
            "latido" -> "For items open for multiple days."
            "guardado" -> "Notice that changes were saved."
            "fijar" -> "When moving note to pinned section."
            "claseAhora" -> "Class currently ongoing. Green indicates «active»."
            "errorShake" -> "When a field is filled incorrectly."
            "saludo" -> "Greeting and your name on Home."
            else -> detail
        }
    }
}

/** Un interruptor de movimiento, que no tiene variantes sino sí o no. */
data class MotionToggle(
    val id: String,
    val name: String,
    val detail: String,
    val read: (MotionPreferences) -> Boolean,
    val write: (MotionPreferences, Boolean) -> MotionPreferences
)

object MotionCatalog {
    const val GROUP_BASE = "BASE"
    const val GROUP_TRANSITIONS = "TRANSICIONES"
    const val GROUP_ACADEMIC = "ACADÉMICO"
    const val GROUP_TASKS = "TAREAS Y NOTAS"
    const val GROUP_ALERTS = "GASTOS Y AVISOS"
    const val GROUP_GENERAL = "GENERALES"

    private inline fun <reified T> gesto(
        id: String,
        group: String,
        name: String,
        detail: String,
        noinline read: (MotionPreferences) -> T,
        noinline write: (MotionPreferences, T) -> MotionPreferences
    ): MotionGesture where T : Enum<T>, T : MotionChoice = MotionGesture(
        id = id,
        group = group,
        name = name,
        detail = detail,
        options = enumValues<T>().toList(),
        read = read,
        write = { prefs, choice -> write(prefs, choice as T) }
    )

    /*
     * **«Tirar para refrescar» no esta en la lista, y es a proposito.**
     *
     * La app lee de una base local: no hay nada que recargar, asi que el gesto no existe en
     * ninguna pantalla. Ofrecer seis variantes de una animacion que nunca se va a ver es
     * enganar a quien elige. El ajuste sigue guardandose —[MotionPreferences.refresh] y sus
     * variantes se quedan— para que el dia que haya sincronizacion con la nube baste con
     * devolver esta entrada a la lista y no haya que rehacer nada.
     */
    val gestures: List<MotionGesture> = listOf(
        gesto<MotionSpeed>(
            "velocidad", GROUP_BASE, "Velocidad",
            "Multiplica todas las duraciones.",
            { it.speed }, { p, v -> p.copy(speed = v) }
        ),
        gesto<LoadingStyle>(
            "carga", GROUP_BASE, "Indicador de carga",
            "El de M3E cambia de forma mientras gira.",
            { it.loading }, { p, v -> p.copy(loading = v) }
        ),

        gesto<ScreenTransition>(
            "transicion", GROUP_TRANSITIONS, "Entre pantallas",
            "Cómo entra una pantalla nueva.",
            { it.screenTransition }, { p, v -> p.copy(screenTransition = v) }
        ),
        gesto<ListEntry>(
            "listas", GROUP_TRANSITIONS, "Entrada de las listas",
            "Cómo aparecen las filas al abrir.",
            { it.listEntry }, { p, v -> p.copy(listEntry = v) }
        ),

        gesto<AttendanceMotion>(
            "asistencia", GROUP_ACADEMIC, "Marcar asistencia",
            "Al confirmar que fuiste a clase.",
            { it.attendance }, { p, v -> p.copy(attendance = v) }
        ),
        gesto<NewGradeMotion>(
            "notaNueva", GROUP_ACADEMIC, "Registrar una nota",
            "Cuando entra una nota nueva al corte.",
            { it.newGrade }, { p, v -> p.copy(newGrade = v) }
        ),
        gesto<CutSealMotion>(
            "sello", GROUP_ACADEMIC, "Sello al cerrar un corte",
            "Al dar un corte por cerrado.",
            { it.cutSeal }, { p, v -> p.copy(cutSeal = v) }
        ),

        gesto<CelebrationMotion>(
            "celebracion", GROUP_TASKS, "Celebrar al terminar el día",
            "Al cerrar la última pendiente.",
            { it.celebration }, { p, v -> p.copy(celebration = v) }
        ),
        gesto<StrikeMotion>(
            "tachar", GROUP_TASKS, "Tachar al completar",
            "Antes de irse de la lista.",
            { it.strikeThrough }, { p, v -> p.copy(strikeThrough = v) }
        ),
        gesto<OverdueBeat>(
            "latido", GROUP_TASKS, "Latido en lo vencido",
            "Lo que lleva días abierto.",
            { it.overdueBeat }, { p, v -> p.copy(overdueBeat = v) }
        ),
        gesto<AutosaveMotion>(
            "guardado", GROUP_TASKS, "Guardado automático",
            "El aviso de que quedó guardado.",
            { it.autosave }, { p, v -> p.copy(autosave = v) }
        ),
        gesto<PinMotion>(
            "fijar", GROUP_TASKS, "Fijar una nota",
            "Al subirla a las fijadas.",
            { it.pinNote }, { p, v -> p.copy(pinNote = v) }
        ),

        gesto<ClassNowMotion>(
            "claseAhora", GROUP_ALERTS, "Clase en curso",
            "La clase que está pasando ahora. En verde: dice «activo».",
            { it.classNow }, { p, v -> p.copy(classNow = v) }
        )
    )

    val toggles: List<MotionToggle> = emptyList()

    /** Los grupos en el orden en que se enseñan, con sus gestos dentro. */
    fun grouped(): List<Pair<String, List<MotionGesture>>> =
        gestures.groupBy(MotionGesture::group).toList()

    /** Cuántas variantes hay entre todos los gestos: lo que dice el subtítulo de la pantalla. */
    val variantCount: Int get() = gestures.sumOf { it.options.size }
}
