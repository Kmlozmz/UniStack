package com.unistack.app.core.design.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.IntOffset
import com.unistack.app.feature_user.domain.AttendanceMotion
import com.unistack.app.feature_user.domain.CelebrationMotion
import com.unistack.app.feature_user.domain.ClassNowMotion
import com.unistack.app.feature_user.domain.FabScrollMotion
import com.unistack.app.feature_user.domain.ListEntry
import com.unistack.app.feature_user.domain.LoadingStyle
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.MotionSpeed
import com.unistack.app.feature_user.domain.OverdueBeat
import com.unistack.app.feature_user.domain.PressEffect
import com.unistack.app.feature_user.domain.RefreshStyle
import com.unistack.app.feature_user.domain.ScreenTransition
import com.unistack.app.feature_user.domain.SpringBounce
import com.unistack.app.feature_user.domain.StrikeMotion

/**
 * El puente entre lo que se elige en Movimiento y lo que hace la app.
 *
 * Sin esto, [MotionPreferences] sería una lista de veinticinco elecciones que se guardan, se
 * enseñan y no pintan un solo píxel, que es exactamente el problema que tenían la mitad de los
 * ajustes de Apariencia antes de esta tanda: `surfaceStyle` guardado y sin usar,
 * `accentIntensity` elegible y sin efecto.
 *
 * Aquí vive **una sola regla**: nadie lee [MotionPreferences] directamente para animar. Se lee
 * [motionActual], que ya trae aplicado el interruptor de accesibilidad —completo, reducido o
 * nada— por encima de cada gesto. Así, apagar el movimiento apaga los veinticinco a la vez sin
 * que ninguna pantalla tenga que acordarse de comprobarlo.
 */
val LocalMotion = staticCompositionLocalOf { MotionPreferences() }

/** Cuánto movimiento se permite ahora mismo, ya con el interruptor de accesibilidad aplicado. */
val LocalMotionAllowance = staticCompositionLocalOf { MotionPreference.FULL }

/**
 * Las preferencias que hay que mirar para animar: las elegidas, o las apagadas.
 *
 * En «nada» devuelve el conjunto quieto —cada gesto en su variante sin movimiento— en vez de
 * pedir a cada sitio que compruebe el permiso antes de moverse. Es la diferencia entre un
 * apagado que funciona siempre y uno que funciona donde alguien se acordó de mirar.
 */
@Composable
@ReadOnlyComposable
fun motionActual(): MotionPreferences {
    val elegidas = LocalMotion.current
    return when (LocalMotionAllowance.current) {
        MotionPreference.FULL -> elegidas
        // Reducido conserva lo que informa —el color que viaja, el latido de lo vencido— y
        // quita lo que solo decora: confeti, sellos que caen, letras que se escriben solas.
        MotionPreference.REDUCED -> elegidas.copy(
            speed = MotionSpeed.RAPIDA,
            bounce = SpringBounce.SUAVE,
            screenTransition = ScreenTransition.FUNDIDO,
            listEntry = ListEntry.FUNDIDO,
            celebration = CelebrationMotion.NINGUNA,
            press = PressEffect.ONDA
        )
        MotionPreference.NONE -> MotionPreferences(
            speed = MotionSpeed.INSTANTANEA,
            bounce = SpringBounce.SUAVE,
            press = PressEffect.NINGUNA,
            loading = LoadingStyle.CIRCULO,
            screenTransition = ScreenTransition.NINGUNA,
            listEntry = ListEntry.NINGUNA,
            refresh = RefreshStyle.CIRCULO,
            attendance = AttendanceMotion.NINGUNA,
            celebration = CelebrationMotion.NINGUNA,
            strikeThrough = StrikeMotion.LINEA,
            overdueBeat = OverdueBeat.NINGUNA,
            classNow = ClassNowMotion.QUIETA,
            fabOnScroll = FabScrollMotion.FIJO,
            animatedBottomBar = false,
            countingNumbers = false,
            haptics = elegidas.haptics
        )
    }
}

/** ¿Se mueve algo ahora mismo? Lo que se pregunta antes de arrancar un bucle infinito. */
@Composable
@ReadOnlyComposable
fun hayMovimiento(): Boolean = LocalMotionAllowance.current != MotionPreference.NONE

/**
 * Una duración en milisegundos, ya multiplicada por la velocidad elegida.
 *
 * Con la velocidad en «nada» devuelve cero, y una animación de cero milisegundos es un salto:
 * no hace falta que quien la pide se entere.
 */
@Composable
@ReadOnlyComposable
fun duracion(baseMs: Int): Int {
    val escala = motionActual().speed.factor
    return (baseMs * escala).toInt()
}

/** El `tween` de siempre, con la velocidad ya aplicada. */
@Composable
fun <T> tweenDeMovimiento(baseMs: Int = 320, retrasoMs: Int = 0): FiniteAnimationSpec<T> =
    tween(durationMillis = duracion(baseMs).coerceAtLeast(1), delayMillis = duracion(retrasoMs))

/**
 * El muelle con el rebote elegido.
 *
 * `dampingRatio` es lo que decide si algo llega y se para o se pasa de largo y vuelve; sale de
 * [SpringBounce] para que «Vivo» se note de verdad y no sea una palabra distinta para lo mismo.
 */
@Composable
fun <T> muelleDeMovimiento(rigidez: Float = Spring.StiffnessMediumLow): AnimationSpec<T> {
    val prefs = motionActual()
    if (prefs.speed == MotionSpeed.INSTANTANEA) return tween(1)
    return spring(dampingRatio = prefs.bounce.damping, stiffness = rigidez / prefs.speed.factor.coerceAtLeast(0.2f))
}

/** El mismo muelle, para lo que se mide en píxeles enteros: posiciones y tamaños. */
@Composable
fun muelleDeDesplazamiento(): AnimationSpec<IntOffset> {
    val prefs = motionActual()
    if (prefs.speed == MotionSpeed.INSTANTANEA) return tween(1)
    return spring(
        dampingRatio = prefs.bounce.damping,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset(1, 1)
    )
}
