package com.unistack.app.core.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.material3.MotionScheme
import androidx.compose.ui.unit.IntOffset
import com.unistack.app.feature_user.domain.ScreenTransition

/**
 * Cómo se mueve la app al cambiar de pantalla.
 *
 * Lo que quedó, después de probar tres: el empuje. La nueva entra entera desde el borde y la
 * anterior se aparta a un tercio sin llegar a irse. Ese desfase entre las dos es lo que hace
 * que se lean como dos capas, una encima de la otra, y no como dos imágenes que se
 * intercambian.
 *
 * **Las curvas y las duraciones ya no se escriben aquí.** Salían de dos béziers medidas a mano
 * y de tres constantes en milisegundos; ahora salen del [MotionScheme] del tema, que es el
 * mismo del que beben los componentes de Material. Dos motivos:
 *
 *  - Un muelle interrumpido a mitad de camino continúa desde donde está, con la velocidad que
 *    llevaba. Un `tween` interrumpido salta al principio de la curva nueva, y eso es lo que se
 *    veía al cambiar de pestaña a golpes.
 *  - El movimiento de una pantalla entrando y el de un botón respondiendo dejan de estar
 *    afinados por separado. Es la misma física.
 *
 * El movimiento espacial —lo que se desplaza— usa el muelle `spatial`, que en el esquema
 * expresivo lleva algo de rebote. El fundido usa `effects`, que nunca rebota: una opacidad que
 * se pasa de largo y vuelve se ve como un parpadeo.
 */

/** Lo que se aparta la pantalla de atrás mientras la nueva la empuja. */
private const val PARALLAX = 3

/** Entrar en cualquier pantalla, con un fundido que no rebota. */
fun screenFadeIn(motion: MotionScheme): EnterTransition =
    fadeIn(animationSpec = motion.defaultEffectsSpec())

/** Y salir. */
fun screenFadeOut(motion: MotionScheme): ExitTransition =
    fadeOut(animationSpec = motion.fastEffectsSpec())

/** El empuje: la nueva entra entera desde el borde y la de atrás se aparta a un tercio. */
fun screenPushIn(motion: MotionScheme, fromRight: Boolean): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { width -> if (fromRight) width else -width / PARALLAX },
        animationSpec = motion.defaultSpatialSpec<IntOffset>()
    )

fun screenPushOut(motion: MotionScheme, toLeft: Boolean): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { width -> if (toLeft) -width / PARALLAX else width },
        animationSpec = motion.defaultSpatialSpec<IntOffset>()
    )

/**
 * El movimiento que toque, según lo que se haya elegido en Apariencia.
 *
 * El mismo movimiento en todas partes, incluido el cambio de pestaña: es el que se eligió, y
 * partirlo en dos según el destino hacía que la app se moviera de dos maneras sin motivo
 * visible.
 *
 * `motionEnabled` en false es quien tiene el movimiento reducido o apagado en el sistema: no
 * se le mueve nada. Antes esto se hacía escalando las duraciones, que con un muelle ya no son
 * un número que se pueda multiplicar.
 */
fun screenEnter(
    style: ScreenTransition,
    motion: MotionScheme,
    motionEnabled: Boolean,
    fromRight: Boolean
): EnterTransition = when {
    !motionEnabled -> EnterTransition.None
    style == ScreenTransition.NONE -> EnterTransition.None
    style == ScreenTransition.FADE -> screenFadeIn(motion)
    else -> screenPushIn(motion, fromRight)
}

fun screenExit(
    style: ScreenTransition,
    motion: MotionScheme,
    motionEnabled: Boolean,
    toLeft: Boolean
): ExitTransition = when {
    !motionEnabled -> ExitTransition.None
    style == ScreenTransition.NONE -> ExitTransition.None
    style == ScreenTransition.FADE -> screenFadeOut(motion)
    else -> screenPushOut(motion, toLeft)
}
