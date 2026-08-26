package com.unistack.app.core.design.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import androidx.compose.runtime.getValue

/**
 * Vaivén continuo entre `-travel` y `+travel`, para que un elemento flote en vez de quedarse
 * clavado. Es la base del movimiento ambiente de los heroes.
 *
 * Devuelve 0 cuando el movimiento está desactivado, y de ahí que quien lo use no tenga que
 * comprobarlo: sumar 0 deja el elemento donde estaba.
 *
 * Vivía dentro del onboarding, que fue donde nació. Se sube aquí al empezar a usarlo también
 * el hero de Inicio: una pantalla no debería importar utilidades de otra para animarse igual.
 */
@Composable
fun floatingOffset(travel: Float, durationMillis: Int, label: String): Float {
    val motionScale = LocalMotionDurationScale.current
    if (motionScale <= 0f) return 0f
    val transition = rememberInfiniteTransition(label = label)
    val animated by transition.animateFloat(
        initialValue = -travel,
        targetValue = travel,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = label
    )
    return animated * motionScale
}
