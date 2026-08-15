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
 * Se probaron dos versiones con deslizamiento: un tercio de pantalla, que se leía como un
 * movimiento al que le falta el principio, y el ancho completo con la anterior apartándose, que
 * se sintió peor todavía. Las dos llamaban la atención sobre el marco en vez de sobre lo que
 * hay dentro.
 *
 * Queda un fundido corto. No cuenta nada —ni jerarquía, ni dirección— y por eso no se equivoca:
 * lo que se mueve es el contenido de cada pantalla, que ya tiene su propio movimiento.
 *
 * Las duraciones salen del escalado de movimiento del sistema, así que quien lo tenga reducido
 * lo ve más rápido, y quien lo apague no ve nada de esto.
 */

/** La curva estándar de Material: sale rápido y frena al llegar. */
private val Standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Entrar y salir no duran lo mismo: lo que llega se mira, lo que se va estorba. */
private const val ENTER_MILLIS = 160
private const val EXIT_MILLIS = 120

private fun Int.scaled(motionScale: Float) = (this * motionScale).roundToInt().coerceAtLeast(1)

/**
 * Entrar en cualquier pantalla: aparece.
 *
 * Corto a propósito —bajo doscientos milisegundos—: una transición que se nota es una
 * transición que estorba cuando se pasa por ella cincuenta veces al día.
 */
fun screenFadeIn(motionScale: Float): EnterTransition =
    fadeIn(
        animationSpec = tween(
            durationMillis = ENTER_MILLIS.scaled(motionScale),
            delayMillis = (EXIT_MILLIS / 2).scaled(motionScale),
            easing = Standard
        )
    )

/** Y salir: desaparece antes de que la siguiente empiece a llegar. */
fun screenFadeOut(motionScale: Float): ExitTransition =
    fadeOut(
        animationSpec = tween(EXIT_MILLIS.scaled(motionScale), easing = Standard)
    )

/**
 * El movimiento que toque, según lo que se haya elegido en Apariencia.
 *
 * El deslizamiento es corto —un sexto de pantalla— a propósito: recorrer el ancho completo se
 * probó y se sentía ajeno, como si la pantalla llegara de otro sitio en vez de abrirse encima.
 */
fun screenEnter(style: ScreenTransition, motionScale: Float, fromRight: Boolean): EnterTransition =
    when (style) {
        ScreenTransition.NONE -> EnterTransition.None
        ScreenTransition.FADE -> screenFadeIn(motionScale)
        ScreenTransition.SLIDE -> slideInHorizontally(
            initialOffsetX = { width -> if (fromRight) width / 6 else -width / 6 },
            animationSpec = tween(ENTER_MILLIS.scaled(motionScale), easing = Standard)
        ) + screenFadeIn(motionScale)
    }

fun screenExit(style: ScreenTransition, motionScale: Float, toLeft: Boolean): ExitTransition =
    when (style) {
        ScreenTransition.NONE -> ExitTransition.None
        ScreenTransition.FADE -> screenFadeOut(motionScale)
        ScreenTransition.SLIDE -> slideOutHorizontally(
            targetOffsetX = { width -> if (toLeft) -width / 6 else width / 6 },
            animationSpec = tween(EXIT_MILLIS.scaled(motionScale), easing = Standard)
        ) + screenFadeOut(motionScale)
    }

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
