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
    val attendance: AttendanceMotion = AttendanceMotion.TRAZO,
    val newGrade: NewGradeMotion = NewGradeMotion.CAE,
    val gradeUp: GradeUpMotion = GradeUpMotion.SALTO,
    val recovery: RecoveryMotion = RecoveryMotion.VIAJE,
    val cutSeal: CutSealMotion = CutSealMotion.ESTAMPA,
    val termClose: TermCloseMotion = TermCloseMotion.APILADO,

    // ------------------------------------------------------------------ tareas y notas
    val celebration: CelebrationMotion = CelebrationMotion.CONFETI,
    val strikeThrough: StrikeMotion = StrikeMotion.LINEA,
    val overdueBeat: OverdueBeat = OverdueBeat.PULSO,
    val undo: UndoMotion = UndoMotion.REBOTA,
    val autosave: AutosaveMotion = AutosaveMotion.PILDORA,
    val pinNote: PinMotion = PinMotion.SALTA,

    // ------------------------------------------------------------------ gastos y avisos
    val overBudget: OverBudgetMotion = OverBudgetMotion.ALERTA,
    val classNow: ClassNowMotion = ClassNowMotion.RESPIRA,
    val errorHint: ErrorMotion = ErrorMotion.SACUDE,

    // ------------------------------------------------------------------ generales
    val greeting: GreetingMotion = GreetingMotion.ESCALONADO,
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
    MEDIO("medio", "Medio", 0.75f),
    VIVO("vivo", "Vivo", 0.45f)
}

enum class PressEffect(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    ONDA("onda", "Onda"),
    HUNDIR("hundir", "Hundir"),
    REBOTE("rebote", "Rebote")
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
    FORMAS("formas", "Formas"),
    ELASTICO("elastico", "Elástico"),
    BARRA("barra", "Barra"),
    GOTA("gota", "Gota")
}

// ---------------------------------------------------------------------- académico

enum class AttendanceMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),

    /** El visto se dibuja de un trazo. */
    TRAZO("trazo", "Trazo"),
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

enum class GradeUpMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    SALTO("salto", "Salto"),
    FLECHA("flecha", "Flecha"),
    BRILLO("brillo", "Brillo")
}

enum class RecoveryMotion(override val id: String, override val label: String) : MotionChoice {
    /** Cambia de color y ya. */
    SECO("seco", "Seco"),

    /** El color viaja del rojo al verde pasando por el ámbar. */
    VIAJE("viaje", "Viaje de color"),
    PULSO("pulso", "Pulso verde"),

    /** Una franja verde barre la fila de izquierda a derecha. */
    BARRIDO("barrido", "Barrido"),

    /** El rojo se encoge por la izquierda mientras el verde crece por la derecha. */
    RELEVO("relevo", "Relevo")
}

enum class CutSealMotion(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    ESTAMPA("estampa", "Estampado"),
    TINTA("tinta", "Tinta"),
    LACRE("lacre", "Lacre"),
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
    DOBLE("doble", "Doble línea"),
    TINTA("tinta", "Tinta que cala")
}

enum class OverdueBeat(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    PULSO("pulso", "Pulso"),
    RESPIRA("respira", "Respira"),
    BORDE("borde", "Borde"),

    /** Un golpe corto y seco cada pocos segundos, no un latido continuo. */
    TIC("tic", "Tic")
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
 * Pasarse del presupuesto **es un aviso**, no un adorno.
 *
 * Por eso ninguna variante es decorativa: la más callada cambia el color y ya, y el resto
 * suben desde ahí hasta ocupar la parte de arriba de la pantalla.
 */
enum class OverBudgetMotion(override val id: String, override val label: String) : MotionChoice {
    SECO("seco", "Seco"),
    ALERTA("alerta", "Alerta"),
    SACUDE("sacude", "Sacude"),
    PARPADEO("parpadeo", "Parpadeo"),

    /** La barra se pasa del final y lo que sobra se derrama por debajo. */
    DESBORDA("desborda", "Se desborda"),
    GRIETA("grieta", "Se llena de rojo"),
    BANNER("banner", "Aviso arriba")
}

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

enum class ErrorMotion(override val id: String, override val label: String) : MotionChoice {
    ROJO("rojo", "Solo rojo"),
    SACUDE("sacude", "Sacude"),
    PARPADEA("parpadea", "Parpadea"),
    ENTRA("entra", "Texto que entra")
}

// ---------------------------------------------------------------------- generales

enum class GreetingMotion(override val id: String, override val label: String) : MotionChoice {
    GOLPE("golpe", "De golpe"),

    /** El rótulo entra, y el nombre justo después. */
    ESCALONADO("escalonado", "Escalonado"),
    MAQUINA("maquina", "Máquina de escribir"),
    CORTINA("cortina", "Cortina"),
    LATERAL("lateral", "Lateral"),
    DESENFOQUE("desenfoque", "Desenfoque"),
    LETRAS("letras", "Letra a letra")
}

enum class FabScrollMotion(override val id: String, override val label: String) : MotionChoice {
    FIJO("fijo", "Fijo"),

    /** Pierde el texto y se queda redondo. */
    ENCOGE("encoge", "Se encoge"),
    BAJA("baja", "Se esconde"),
    DESVANECE("desvanece", "Se desvanece")
}

enum class HapticStrength(override val id: String, override val label: String) : MotionChoice {
    NINGUNA("ninguna", "Nada"),
    SUAVE("suave", "Suave"),
    MEDIA("media", "Media"),
    FUERTE("fuerte", "Fuerte")
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
)

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
        gesto<GradeUpMotion>(
            "subeNota", GROUP_ACADEMIC, "Nota que sube",
            "Cuando el promedio mejora.",
            { it.gradeUp }, { p, v -> p.copy(gradeUp = v) }
        ),
        gesto<RecoveryMotion>(
            "recupera", GROUP_ACADEMIC, "Materia que se recupera",
            "Cuando sale del rojo.",
            { it.recovery }, { p, v -> p.copy(recovery = v) }
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

        gesto<OverBudgetMotion>(
            "presupuesto", GROUP_ALERTS, "Pasarse del presupuesto",
            "Al cruzar el límite. Es un aviso, no un adorno.",
            { it.overBudget }, { p, v -> p.copy(overBudget = v) }
        ),
        gesto<ClassNowMotion>(
            "claseAhora", GROUP_ALERTS, "Clase en curso",
            "La clase que está pasando ahora. En verde: dice «activo».",
            { it.classNow }, { p, v -> p.copy(classNow = v) }
        ),
        gesto<ErrorMotion>(
            "errorShake", GROUP_ALERTS, "Aviso de error",
            "Un campo mal rellenado.",
            { it.errorHint }, { p, v -> p.copy(errorHint = v) }
        ),

        gesto<GreetingMotion>(
            "saludo", GROUP_GENERAL, "Saludo al abrir",
            "El saludo y tu nombre en Inicio.",
            { it.greeting }, { p, v -> p.copy(greeting = v) }
        )
    )

    val toggles: List<MotionToggle> = emptyList()

    /** Los grupos en el orden en que se enseñan, con sus gestos dentro. */
    fun grouped(): List<Pair<String, List<MotionGesture>>> =
        gestures.groupBy(MotionGesture::group).toList()

    /** Cuántas variantes hay entre todos los gestos: lo que dice el subtítulo de la pantalla. */
    val variantCount: Int get() = gestures.sumOf { it.options.size }
}
