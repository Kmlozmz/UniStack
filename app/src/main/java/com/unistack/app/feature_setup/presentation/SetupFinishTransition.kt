package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniStackBrandMark
import com.unistack.app.core.design.components.UniStackBrandPill
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Ancho del símbolo mientras se dispersa. */
private val ExitMarkWidth = 132.dp

/**
 * Cierre del onboarding: el sello de confirmación se deshace en las tres píldoras de la
 * marca, que se muestran un instante formando el logo y salen disparadas a los lados.
 *
 * Se dispersan en el orden inverso al de la animación de arranque, donde las píldoras se
 * juntan para formar el logo. Así el primer arranque queda cerrado por los dos extremos.
 *
 * No hay velo de color al terminar: la pantalla queda en el fondo de la app, que es el
 * mismo del inicio, de modo que el relevo entre una y otra no tiene costura. El inicio se
 * encarga de aparecer con su propio fundido.
 *
 * [onFinished] se invoca cuando ya no queda nada en pantalla, no al pulsar. Quien llama
 * debe confirmar el setup ahí dentro: hacerlo antes cambia la pantalla bajo los pies de
 * esta animación y la corta a la mitad.
 */
@Composable
fun SetupFinishTransition(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnFinished by rememberUpdatedState(onFinished)
    val motionEnabled = LocalMotionDurationScale.current > 0f

    val pillOffsets = remember { List(3) { Animatable(0f) } }
    val pillAlphas = remember { List(3) { Animatable(0f) } }
    val pillScales = remember { List(3) { Animatable(0.55f) } }

    LaunchedEffect(motionEnabled) {
        if (!motionEnabled) {
            // Sin animaciones no tiene sentido esperar mirando una pantalla quieta.
            latestOnFinished()
            return@LaunchedEffect
        }

        // Deja que la tarjeta y los botones terminen de retirarse antes de empezar.
        delay(380)

        // Las píldoras brotan del sello y componen el logo.
        pillOffsets.indices.forEach { index ->
            launch {
                pillAlphas[index].animateTo(1f, tween(durationMillis = 220, easing = LinearEasing))
            }
            launch {
                pillScales[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing)
                )
            }
        }
        delay(280)

        // Un respiro con el logo montado: es lo que se ha estado construyendo, y sin esta
        // pausa pasa tan deprisa que no llega a leerse como el símbolo de la marca.
        delay(200)

        // Dispersión, en el orden inverso al de la entrada.
        val directions = listOf(1f, -1f, 1f)
        val durations = listOf(520, 470, 420)
        directions.indices.forEach { index ->
            launch {
                pillOffsets[index].animateTo(
                    targetValue = directions[index],
                    animationSpec = tween(
                        durationMillis = durations[index],
                        easing = FastOutSlowInEasing
                    )
                )
            }
            launch {
                pillAlphas[index].animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = durations[index])
                )
            }
            delay(110)
        }

        // Lo que queda de la última píldora, más un momento de fondo limpio antes del relevo.
        delay(durations.last().toLong() + 260)
        latestOnFinished()
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(
                width = ExitMarkWidth,
                height = ExitMarkWidth * UniStackBrandMark.HeightRatio
            )
        ) {
            UniStackBrandMark.Pills.forEachIndexed { index, pill ->
                UniStackBrandPill(
                    pill = pill,
                    markWidth = ExitMarkWidth,
                    modifier = Modifier.graphicsLayer {
                        // La distancia se mide en anchos de la propia píldora, para que salga
                        // de la pantalla en cualquier tamaño de dispositivo.
                        translationX = pillOffsets[index].value * size.width.coerceAtLeast(1f) * 4f
                        alpha = pillAlphas[index].value
                        scaleX = pillScales[index].value
                        scaleY = pillScales[index].value
                    }
                )
            }
        }
    }
}
