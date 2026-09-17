package com.unistack.app.feature_user.domain

import com.unistack.app.R
import com.unistack.app.core.utils.Textos

/**
 * Cómo se mueve la app, gesto a gesto.
 *
 * Hasta ahora el movimiento era **un interruptor de tres posiciones** —[MotionPreference]:
 * completo, reducido o nada— y todo lo demás estaba decidido en el código. Aquí cada momento
 * de la app en el que algo se mueve es una elección propia: cómo entra una pantalla, cómo
 * llegan las filas, qué pasa al cerrar un corte.
 *
 * **Quedan siete gestos, de veinticinco que hubo.** Cada uno se probó en su pantalla, variante
 * a variante, y lo que no se distinguía o no aportaba se retiró dejando fija la variante que
 * mejor quedaba: la nota que entra cae y empuja, lo completado se tacha con una línea, lo
 * vencido late, el guardado es un punto, la nota fijada despega, la rueda de asistencia
 * rebota y pasarse del presupuesto avisa arriba. Nada de eso se elige ya; simplemente ocurre.
 *
 * Por eso **no hay un campo por cada gesto en el JSON ni una fila escrita a mano en la
 * pantalla**: el catálogo de [MotionCatalog] es el que dice qué gestos hay, qué variantes tiene
 * cada uno y cómo se lee y se escribe la elección. Guardar, cargar y pintar salen todos de ahí.
 *
 * Los nombres y las variantes salen de `strings.xml`, como el resto de la app: ni el catálogo
 * ni sus enums llevan texto en el código.
 *
 * [MotionPreference] sigue existiendo y sigue mandando: puesto en «reducido» o «nada», lo de
 * aquí queda en pausa. Es lo que hace que quien lo necesite apague todo de un toque.
 */
data class MotionPreferences(
    // ------------------------------------------------------------------ base
    val speed: MotionSpeed = MotionSpeed.NORMAL,
    val bounce: SpringBounce = SpringBounce.VIVO,
    val loading: LoadingStyle = LoadingStyle.FORMAS,

    // ------------------------------------------------------------------ transiciones
    val screenTransition: ScreenTransition = ScreenTransition.EJE,
    val listEntry: ListEntry = ListEntry.ESCALONADA,
    val refresh: RefreshStyle = RefreshStyle.ONDA_CIRCULAR,

    // ------------------------------------------------------------------ momentos
    val cutSeal: CutSealMotion = CutSealMotion.TINTA,
    val celebration: CelebrationMotion = CelebrationMotion.CONFETI,
    val undo: UndoMotion = UndoMotion.REBOTA,
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
 * elección que tenía hecha. El nombre visible es un recurso, en el idioma de la app.
 */
interface MotionChoice {
    val id: String
    val labelRes: Int
    val label: String get() = Textos.get(labelRes)
    val displayLabel: String get() = label
}

// ---------------------------------------------------------------------- base

enum class MotionSpeed(override val id: String, override val labelRes: Int, val factor: Float) : MotionChoice {
    /** Todo instantáneo. Es lo que pide quien se marea con el movimiento. */
    INSTANTANEA("instant", R.string.motion_v_none, 0f),
    RAPIDA("rapida", R.string.motion_v_fast, 0.6f),
    NORMAL("normal", R.string.motion_v_normal, 1f),
    LENTA("lenta", R.string.motion_v_slow, 1.5f)
}

enum class SpringBounce(override val id: String, override val labelRes: Int, val damping: Float) : MotionChoice {
    /** Sin rebote visible: llega y se para. */
    SUAVE("suave", R.string.motion_v_soft, 1f),
    VIVO("vivo", R.string.motion_v_vivid, 0.45f)
}

enum class LoadingStyle(override val id: String, override val labelRes: Int) : MotionChoice {
    CIRCULO("circulo", R.string.motion_v_circle),

    /** El indicador de M3E que cambia de forma mientras gira. */
    FORMAS("formas", R.string.motion_v_shapes),
    ONDA("onda", R.string.motion_v_wave),
    PUNTOS("puntos", R.string.motion_v_dots)
}

// ---------------------------------------------------------------------- transiciones

enum class ScreenTransition(override val id: String, override val labelRes: Int) : MotionChoice {
    NINGUNA("ninguna", R.string.motion_v_none),
    FUNDIDO("fundido", R.string.motion_v_fade),

    /** El empuje lateral de M3E: la que sale va a la izquierda, la que entra viene de la derecha. */
    EJE("eje", R.string.motion_v_axis),
    CONTENEDOR("contenedor", R.string.motion_v_container),
    ABAJO("abajo", R.string.motion_v_from_below),
    ZOOM("zoom", R.string.motion_v_zoom),

    /** La de arriba: entra bajando, como un panel que se descuelga. */
    ARRIBA("arriba", R.string.motion_v_from_above),

    /** El empuje del eje, pero en vertical: la que sale sube un tercio y espera debajo. */
    EJE_VERTICAL("ejeV", R.string.motion_v_vertical_axis),

    /** La nueva sube entera y la anterior se queda detrás, encogida y apagada. */
    TARJETA("tarjeta", R.string.motion_v_card),

    /** Entra desde la esquina, moviéndose en los dos ejes a la vez. */
    DIAGONAL("diagonal", R.string.motion_v_diagonal)
}

enum class ListEntry(override val id: String, override val labelRes: Int) : MotionChoice {
    NINGUNA("ninguna", R.string.motion_v_none),
    FUNDIDO("fundido", R.string.motion_v_fade),

    /** Cada fila entra un poco después que la anterior, desde abajo. */
    ESCALONADA("escalonada", R.string.motion_v_staggered),

    /** Como la escalonada, pero cayendo desde arriba y con más retardo entre filas. */
    CASCADA("cascada", R.string.motion_v_waterfall),

    /** Crece desde el 92%, sin desplazarse. */
    ESCALA("escala", R.string.motion_v_scale),

    /** Entra desde abajo con muelle: se pasa de largo y vuelve. */
    RESORTE("resorte", R.string.motion_v_spring),

    /** Gira desde el borde izquierdo, como cartas que se abren. */
    ABANICO("abanico", R.string.motion_v_fan)
}

enum class RefreshStyle(override val id: String, override val labelRes: Int) : MotionChoice {
    CIRCULO("circulo", R.string.motion_v_circle),

    /** El `CircularWavyProgressIndicator` de M3E: un arco cuyo radio ondula. */
    ONDA_CIRCULAR("ondacirc", R.string.motion_v_circular_wave),

    /** El `LoadingIndicator` de M3E, que va cambiando de forma. */
    FORMAS("formas", R.string.motion_v_shapes)
}

// ---------------------------------------------------------------------- momentos

enum class CutSealMotion(override val id: String, override val labelRes: Int) : MotionChoice {
    NINGUNA("ninguna", R.string.motion_v_none),
    ESTAMPA("estampa", R.string.motion_v_stamp),
    TINTA("tinta", R.string.motion_v_ink),
    CINTA("cinta", R.string.motion_v_tape)
}

enum class CelebrationMotion(override val id: String, override val labelRes: Int) : MotionChoice {
    NINGUNA("ninguna", R.string.motion_v_none),
    CONFETI("confeti", R.string.motion_v_confetti),
    ONDA("onda", R.string.motion_v_expanding_wave),
    SELLO("sello", R.string.motion_v_seal),
    DESTELLO("destello", R.string.motion_v_sparkle)
}

enum class UndoMotion(override val id: String, override val labelRes: Int) : MotionChoice {
    APARECE("aparece", R.string.motion_v_appears),
    VUELVE("vuelve", R.string.motion_v_slides_back),
    CAE("cae", R.string.motion_v_drops),
    DESPLIEGA("despliega", R.string.motion_v_unfolds),
    REBOTA("rebota", R.string.motion_v_bounces),
    GIRA("gira", R.string.motion_v_spins_back),
    DESTELLO("destello", R.string.motion_v_with_sparkle)
}

/**
 * La clase que está pasando ahora mismo. **En verde**, que es lo que dice «activo».
 *
 * Estuvo en el color de acento, y ahí competía con todo lo demás que va del color de acento:
 * un morado más entre morados no decía «esto está pasando ahora».
 */
enum class ClassNowMotion(override val id: String, override val labelRes: Int) : MotionChoice {
    QUIETA("quieta", R.string.motion_v_still),
    RESPIRA("respira", R.string.motion_v_breathes),
    PUNTO("punto", R.string.motion_v_beating_dot),
    RECORRE("recorre", R.string.motion_v_running_border),
    BARRE("barre", R.string.motion_v_sweeping_glow)
}

enum class FabScrollMotion(override val id: String, override val labelRes: Int) : MotionChoice {
    FIJO("fijo", R.string.motion_v_fixed),

    /** Pierde el texto y se queda redondo. */
    ENCOGE("encoge", R.string.motion_v_shrinks)
}

enum class HapticStrength(override val id: String, override val labelRes: Int) : MotionChoice {
    NINGUNA("ninguna", R.string.motion_v_none),
    MEDIA("media", R.string.motion_v_medium)
}

/**
 * Un gesto del catálogo: lo que hace falta para guardarlo, leerlo y pintarlo.
 *
 * Lleva las funciones de leer y escribir dentro porque es lo que permite que la pantalla de
 * Movimiento y el guardado en disco recorran la misma lista en vez de repetir siete veces el
 * mismo `when`. Sin esto, añadir un gesto serían cuatro sitios que tocar.
 */
data class MotionGesture(
    val id: String,
    val group: String,
    val nameRes: Int,
    val detailRes: Int,
    val options: List<MotionChoice>,
    val read: (MotionPreferences) -> MotionChoice,
    val write: (MotionPreferences, MotionChoice) -> MotionPreferences
) {
    val name: String get() = Textos.get(nameRes)
    val detail: String get() = Textos.get(detailRes)
    val displayName: String get() = name
    val displayDetail: String get() = detail
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
    const val GROUP_MOMENTS = "MOMENTOS"

    /** El nombre del grupo en el idioma de la app; la constante es solo la clave. */
    fun groupNameRes(group: String): Int = when (group) {
        GROUP_BASE -> R.string.motion_group_base
        GROUP_TRANSITIONS -> R.string.motion_group_transitions
        else -> R.string.motion_group_moments
    }

    private inline fun <reified T> gesto(
        id: String,
        group: String,
        nameRes: Int,
        detailRes: Int,
        noinline read: (MotionPreferences) -> T,
        noinline write: (MotionPreferences, T) -> MotionPreferences
    ): MotionGesture where T : Enum<T>, T : MotionChoice = MotionGesture(
        id = id,
        group = group,
        nameRes = nameRes,
        detailRes = detailRes,
        options = enumValues<T>().toList(),
        read = read,
        write = { prefs, choice -> write(prefs, choice as T) }
    )

    /*
     * **«Tirar para refrescar» no esta en la lista, y es a proposito.**
     *
     * La app lee de una base local: no hay nada que recargar, asi que el gesto no existe en
     * ninguna pantalla. Ofrecer variantes de una animacion que nunca se va a ver es enganar
     * a quien elige. El ajuste sigue guardandose —[MotionPreferences.refresh] y sus variantes
     * se quedan— para que el dia que haya sincronizacion con la nube baste con devolver esta
     * entrada a la lista y no haya que rehacer nada.
     */
    val gestures: List<MotionGesture> = listOf(
        gesto<MotionSpeed>(
            "velocidad", GROUP_BASE, R.string.motion_g_speed_name, R.string.motion_g_speed_detail,
            { it.speed }, { p, v -> p.copy(speed = v) }
        ),
        gesto<LoadingStyle>(
            "carga", GROUP_BASE, R.string.motion_g_loading_name, R.string.motion_g_loading_detail,
            { it.loading }, { p, v -> p.copy(loading = v) }
        ),

        gesto<ScreenTransition>(
            "transicion", GROUP_TRANSITIONS, R.string.motion_g_transition_name, R.string.motion_g_transition_detail,
            { it.screenTransition }, { p, v -> p.copy(screenTransition = v) }
        ),
        gesto<ListEntry>(
            "listas", GROUP_TRANSITIONS, R.string.motion_g_lists_name, R.string.motion_g_lists_detail,
            { it.listEntry }, { p, v -> p.copy(listEntry = v) }
        ),

        gesto<CutSealMotion>(
            "sello", GROUP_MOMENTS, R.string.motion_g_seal_name, R.string.motion_g_seal_detail,
            { it.cutSeal }, { p, v -> p.copy(cutSeal = v) }
        ),
        gesto<CelebrationMotion>(
            "celebracion", GROUP_MOMENTS, R.string.motion_g_celebration_name, R.string.motion_g_celebration_detail,
            { it.celebration }, { p, v -> p.copy(celebration = v) }
        ),
        gesto<ClassNowMotion>(
            "claseAhora", GROUP_MOMENTS, R.string.motion_g_class_now_name, R.string.motion_g_class_now_detail,
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
