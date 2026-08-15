package com.unistack.app.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import com.unistack.app.feature_user.domain.ScreenTransition
import kotlin.math.roundToInt

/**
 * Cómo se mueve la app al cambiar de pantalla.
 *
 * Antes todo era el mismo deslizamiento de un tercio de pantalla, fuera lo que fuera el cambio:
 * entrar a un detalle y saltar de una pestaña a otra se veían igual. Y un tercio de recorrido no
 * es ni una cosa ni la otra —la pantalla aparece ya empezada, como si se hubiera perdido el
 * principio del movimiento—, que es lo que hacía que se sintiera desconectado.
 *
 * Lo que quedó, después de probar tres: el empuje. La nueva entra entera desde el borde y la
 * anterior se aparta a un tercio sin llegar a irse. Un tercio de recorrido para la que entra se
 * leía como un movimiento al que le falta el principio, y fundir sin más no contaba de dónde
 * venía nada.
 *
 * Las duraciones salen del escalado de movimiento del sistema, así que quien lo tenga reducido
 * lo ve más rápido, y quien lo apague no ve nada de esto.
 */

/** La curva estándar de Material, para el fundido. */
private val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/**
 * La curva del empuje.
 *
 * Arranca rápido y frena largo, que es lo que hace que la pantalla parezca tener peso en vez de
 * aparecer colocada. Es la misma que usa iOS para su gesto de entrar y salir.
 */
private val Push = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)

private const val FADE_ENTER_MILLIS = 160
private const val FADE_EXIT_MILLIS = 120
private const val PUSH_MILLIS = 380

/** Lo que se aparta la pantalla de atrás mientras la nueva la empuja. */
private const val PARALLAX = 3

/** Y cuánto se apaga mientras se va: no desaparece, se queda detrás. */
private const val DIMMED = 0.65f

private fun Int.scaled(motionScale: Float) = (this * motionScale).roundToInt().coerceAtLeast(1)

/**
 * Entrar en cualquier pantalla: aparece, y no antes de que la otra se haya ido.
 *
 * La espera era la mitad de lo que tarda la anterior en irse, así que durante unas décimas se
 * veían las dos superpuestas: los textos de una encima de los de la otra, como una doble
 * exposición. Con la espera completa, en cada instante hay una sola pantalla.
 */
fun screenFadeIn(motionScale: Float): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = FADE_ENTER_MILLIS.scaled(motionScale),
            delayMillis = FADE_EXIT_MILLIS.scaled(motionScale),
            easing = Standard
        )
    )

/** Y salir: desaparece antes de que la siguiente empiece a llegar. */
fun screenFadeOut(motionScale: Float): ExitTransition =
    fadeOut(
        animationSpec = tween(FADE_EXIT_MILLIS.scaled(motionScale), easing = Standard)
    )

/**
 * El empuje: la nueva entra entera desde el borde y la de atrás se aparta a un tercio.
 *
 * Ese desfase entre las dos —una recorre la pantalla completa, la otra un tercio— es lo que
 * hace que se lean como dos capas, una encima de la otra, y no como dos imágenes que se
 * intercambian. Y la de atrás se apaga en vez de desaparecer, para que se entienda que sigue
 * ahí esperando.
 */
fun screenPushIn(motionScale: Float, fromRight: Boolean): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { width -> if (fromRight) width else -width / PARALLAX },
        animationSpec = tween(PUSH_MILLIS.scaled(motionScale), easing = Push)
    ) + fadeIn(
        initialAlpha = if (fromRight) 1f else DIMMED,
        animationSpec = tween(PUSH_MILLIS.scaled(motionScale), easing = Push)
    )

fun screenPushOut(motionScale: Float, toLeft: Boolean): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { width -> if (toLeft) -width / PARALLAX else width },
        animationSpec = tween(PUSH_MILLIS.scaled(motionScale), easing = Push)
    ) + fadeOut(
        targetAlpha = if (toLeft) DIMMED else 1f,
        animationSpec = tween(PUSH_MILLIS.scaled(motionScale), easing = Push)
    )

/** El movimiento que toque, según lo que se haya elegido en Apariencia. */
fun screenEnter(style: ScreenTransition, motionScale: Float, fromRight: Boolean): EnterTransition =
    when (style) {
        ScreenTransition.NONE -> EnterTransition.None
        ScreenTransition.FADE -> screenFadeIn(motionScale)
        ScreenTransition.PUSH -> screenPushIn(motionScale, fromRight)
    }

fun screenExit(style: ScreenTransition, motionScale: Float, toLeft: Boolean): ExitTransition =
    when (style) {
        ScreenTransition.NONE -> ExitTransition.None
        ScreenTransition.FADE -> screenFadeOut(motionScale)
        ScreenTransition.PUSH -> screenPushOut(motionScale, toLeft)
    }
