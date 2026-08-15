package com.unistack.app.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import kotlin.math.roundToInt

/**
 * Cómo se mueve la app al cambiar de pantalla.
 *
 * Antes todo era el mismo deslizamiento de un tercio de pantalla, fuera lo que fuera el cambio:
 * entrar a un detalle y saltar de una pestaña a otra se veían igual. Y un tercio de recorrido no
 * es ni una cosa ni la otra —la pantalla aparece ya empezada, como si se hubiera perdido el
 * principio del movimiento—, que es lo que hacía que se sintiera desconectado.
 *
 * Ahora hay dos gestos, y cada uno dice algo distinto:
 *
 * - **En profundidad** (abrir un detalle, entrar a un ajuste): la nueva entra desde el borde
 *   recorriendo la pantalla entera, y la anterior se va despacio hacia el lado contrario, a un
 *   cuarto de velocidad. Ese desfase es lo que hace que se lean como dos capas y no como dos
 *   imágenes intercambiadas.
 * - **Entre pestañas** (Inicio ↔ Horario ↔ Gastos): nada se desliza, porque no hay ni un antes
 *   ni un después entre ellas. Se cruzan fundiéndose, con un pellizco de escala.
 *
 * Las duraciones salen del escalado de movimiento del sistema, así que quien lo tenga reducido
 * lo ve más rápido, y quien lo apague no ve nada de esto.
 */

/** La curva estándar de Material: sale rápido y frena al llegar. */
private val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Entrar y salir no duran lo mismo: lo que llega se mira, lo que se va estorba. */
private const val ENTER_MILLIS = 320
private const val EXIT_MILLIS = 240
private const val FADE_THROUGH_OUT_MILLIS = 90
private const val FADE_THROUGH_IN_MILLIS = 220

private fun Int.scaled(motionScale: Float) = (this * motionScale).roundToInt().coerceAtLeast(1)

/** La pantalla nueva entra desde el borde. [fromRight] es avanzar; falso, volver. */
fun depthEnter(fromRight: Boolean, motionScale: Float): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { width -> if (fromRight) width else -width },
        animationSpec = tween(ENTER_MILLIS.scaled(motionScale), easing = Standard)
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = (ENTER_MILLIS / 2).scaled(motionScale),
            easing = Standard
        )
    )

/**
 * La anterior se aparta un cuarto de pantalla, no entera.
 *
 * Es el desfase que da la sensación de capas: si las dos recorrieran lo mismo, parecerían una
 * sola tira moviéndose de lado.
 */
fun depthExit(toLeft: Boolean, motionScale: Float): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { width -> if (toLeft) -width / 4 else width / 4 },
        animationSpec = tween(EXIT_MILLIS.scaled(motionScale), easing = Standard)
    ) + fadeOut(
        animationSpec = tween(EXIT_MILLIS.scaled(motionScale), easing = Standard)
    )

/**
 * Cambio entre pestañas: se funden, sin dirección.
 *
 * Deslizar entre secciones obliga a inventarse un orden —¿Gastos está a la derecha de Horario?—
 * y ese orden cambia según los módulos que tengas encendidos. Fundir no miente.
 */
fun lateralEnter(motionScale: Float): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = FADE_THROUGH_IN_MILLIS.scaled(motionScale),
            delayMillis = FADE_THROUGH_OUT_MILLIS.scaled(motionScale),
            easing = Standard
        )
    ) + scaleIn(
        initialScale = 0.94f,
        animationSpec = tween(
            durationMillis = FADE_THROUGH_IN_MILLIS.scaled(motionScale),
            delayMillis = FADE_THROUGH_OUT_MILLIS.scaled(motionScale),
            easing = Standard
        )
    )

fun lateralExit(motionScale: Float): ExitTransition =
    fadeOut(
        animationSpec = tween(FADE_THROUGH_OUT_MILLIS.scaled(motionScale), easing = Standard)
    ) + scaleOut(
        targetScale = 0.96f,
        animationSpec = tween(FADE_THROUGH_OUT_MILLIS.scaled(motionScale), easing = Standard)
    )

/**
 * La que se va al volver atrás recorre la pantalla entera.
 *
 * Es la mitad que el dedo arrastra en el gesto predictivo: si solo se apartara un cuarto, el
 * sistema dibujaría la pantalla saliendo y volviendo a su sitio en cuanto sueltas, que es
 * justo la sensación de que el gesto «no responde».
 */
fun depthPopExit(motionScale: Float): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { width -> width },
        animationSpec = tween(EXIT_MILLIS.scaled(motionScale), easing = Standard)
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = EXIT_MILLIS.scaled(motionScale),
            delayMillis = (EXIT_MILLIS / 3).scaled(motionScale),
            easing = Standard
        )
    )

/**
 * Si el cambio es entre secciones y no hacia dentro de una.
 *
 * Se mide por la pestaña a la que pertenece cada ruta: dos rutas de pestañas distintas son un
 * cambio lateral aunque una esté más adentro que la otra.
 */
internal fun isLateralNavigation(initialRoute: String?, targetRoute: String?): Boolean {
    val from = bottomRouteFor(initialRoute) ?: return false
    val to = bottomRouteFor(targetRoute) ?: return false
    return from != to
}
