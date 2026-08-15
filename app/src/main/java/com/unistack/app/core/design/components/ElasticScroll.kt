package com.unistack.app.core.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.sign

/*
 * Los tres números que deciden cómo se siente.
 *
 * La primera tanda iba con 0,32 de resistencia, 220 de tope y 0,14 de empuje, y con un
 * desplazamiento fuerte el rebote se iba tan lejos que parecía otra cosa: no una lista que
 * frena contra el final, sino algo que se suelta. Estos valores dejan el gesto en un acuse de
 * recibo —se nota que hay borde— sin llegar a ser un salto.
 */

/** Cuánto se resiste el contenido al pasarse del borde. Uno sería seguir al dedo sin más. */
private const val RESISTANCE = 0.14f

/** El tope: por mucho que se arrastre, no se despega más que esto. */
private const val MAX_OVERSCROLL = 72f

/** Lo que empuja el rebote cuando la lista llega al final lanzada. */
private const val FLING_PUSH = 0.03f

/** El empuje del lanzamiento, con techo propio: es el que se iba de madre. */
private const val MAX_FLING_PUSH = 36f

/**
 * El rebote al llegar al final de una lista.
 *
 * Android trae un estirón —el contenido se deforma contra el borde— y no un rebote: al llegar
 * arriba o abajo la lista se planta en seco. Esto la deja despegarse del borde con resistencia
 * creciente, para que arrastrar de más cueste cada vez más, y la devuelve con un muelle al
 * soltar. Es el gesto que hace que una lista se sienta un objeto físico y no una imagen que se
 * corta.
 *
 * La resistencia es lo que evita que parezca un fallo: sin ella, el contenido se iría con el
 * dedo hasta el infinito y no habría forma de notar dónde está el final de verdad.
 *
 * Se apaga solo si el sistema tiene el movimiento reducido: quien pide menos animación no
 * quiere justamente esto.
 */
fun Modifier.elasticScroll(): Modifier = composed {
    val motionScale = LocalMotionDurationScale.current
    if (motionScale <= 0f) return@composed this

    val scope = rememberCoroutineScope()
    val offset = remember { Animatable(0f) }

    val connection = remember(scope) {
        object : NestedScrollConnection {
            /**
             * Si la lista está despegada, el dedo primero la devuelve a su sitio.
             *
             * Sin esto, arrastrar en sentido contrario haría rodar la lista mientras sigue
             * separada del borde, y el rebote se quedaría colgado.
             */
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val current = offset.value
                if (current == 0f || source != NestedScrollSource.UserInput) return Offset.Zero
                val opposesOffset = sign(available.y) != sign(current)
                if (!opposesOffset) return Offset.Zero

                val consumed = if (abs(available.y) > abs(current)) -current else available.y
                scope.launch { offset.snapTo(current + consumed) }
                return Offset(0f, consumed)
            }

            /** Lo que la lista no pudo usar es que ya está en el borde: eso la despega. */
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                /*
                 * Si la lista consumió el gesto, es que ya no está en el borde: lo que quede
                 * despegado vuelve a cero.
                 *
                 * Sin esto, un desvío podía quedarse puesto —la cabecera que se encoge también
                 * consume desplazamiento, y entre las dos el gesto se cortaba a medias— y la
                 * lista se quedaba pegada arriba sin forma de recolocarla.
                 */
                if (consumed.y != 0f && offset.value != 0f) {
                    scope.launch {
                        offset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                    }
                    return Offset.Zero
                }
                if (available.y == 0f || source != NestedScrollSource.UserInput) return Offset.Zero

                // La resistencia crece con lo que ya se ha estirado: cuanto más lejos, más
                // cuesta, hasta quedarse quieto en el tope.
                val current = offset.value
                val room = 1f - (abs(current) / MAX_OVERSCROLL).coerceIn(0f, 1f)
                val delta = available.y * RESISTANCE * room
                scope.launch { offset.snapTo(current + delta) }
                return Offset(0f, available.y)
            }

            /** Al soltar, vuelve con un muelle. */
            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offset.value == 0f) return Velocity.Zero
                offset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    initialVelocity = available.y
                )
                return available
            }

            /**
             * Llegar al final lanzado también rebota.
             *
             * Es la mitad del efecto que se nota sin buscarlo: la lista frena contra el borde y
             * devuelve algo del impulso en vez de tragárselo.
             */
            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                if (available.y == 0f) return Velocity.Zero
                val push = (available.y * FLING_PUSH).coerceIn(-MAX_FLING_PUSH, MAX_FLING_PUSH)
                offset.snapTo(push)
                offset.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
                return available
            }
        }
    }

    this
        .nestedScroll(connection)
        .graphicsLayer { translationY = offset.value }
}
