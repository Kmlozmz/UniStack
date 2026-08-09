package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniStackBrandMark
import com.unistack.app.core.design.components.UniStackBrandPill
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.UniStackColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot

/** Ancho del símbolo mientras se dispersa. Cabe donde estaba el sello del check. */
private val ExitMarkWidth = 132.dp

/**
 * Cierre del onboarding: el sello de confirmación se deshace en las tres píldoras de la
 * marca, salen disparadas a los lados y de donde estaban brota el color que entrega la
 * pantalla siguiente.
 *
 * Se dispersan en el orden inverso al de la animación de arranque, donde las píldoras se
 * juntan para formar el logo. Así el primer arranque queda cerrado por los dos extremos.
 *
 * La onda arranca antes de que salga la última píldora a propósito: encadenadas se leen
 * como un solo gesto —la dispersión libera el color—, mientras que en secuencia limpia
 * parecen dos animaciones pegadas.
 *
 * [onFinished] se invoca cuando la onda ya cubre la pantalla, no al pulsar. Quien llama
 * debe confirmar el setup ahí dentro: hacerlo antes cambia la pantalla bajo los pies de
 * esta animación y la corta a la mitad.
 */
@Composable
fun SetupFinishTransition(
    origin: Offset,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val latestOnFinished by rememberUpdatedState(onFinished)
    val density = LocalDensity.current
    val motionEnabled = LocalMotionDurationScale.current > 0f

    val pillOffsets = remember { List(3) { Animatable(0f) } }
    val pillAlphas = remember { List(3) { Animatable(0f) } }
    val pillScales = remember { List(3) { Animatable(0.5f) } }
    val waveProgress = remember { Animatable(0f) }

    LaunchedEffect(motionEnabled) {
        if (!motionEnabled) {
            // Sin animaciones no tiene sentido esperar mirando una pantalla quieta.
            waveProgress.snapTo(1f)
            latestOnFinished()
            return@LaunchedEffect
        }

        // Deja que la tarjeta y los botones terminen de retirarse antes de empezar.
        delay(260)

        // Las píldoras brotan del sello con un rebote corto.
        pillOffsets.indices.forEach { index ->
            launch {
                pillAlphas[index].animateTo(1f, tween(durationMillis = 140, easing = LinearEasing))
            }
            launch {
                pillScales[index].animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
            }
        }
        delay(160)

        // Dispersión: se acelera igual que la entrada, pero al revés.
        val directions = listOf(1f, -1f, 1f)
        val durations = listOf(300, 270, 240)
        launch {
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
                delay(70)
            }
        }

        delay(280)
        waveProgress.animateTo(1f, tween(durationMillis = 430, easing = FastOutSlowInEasing))
        latestOnFinished()
    }

    Box(modifier = modifier.fillMaxSize()) {
        @Suppress("NAME_SHADOWING")
        val markWidth = ExitMarkWidth
        val markHeight = markWidth * UniStackBrandMark.HeightRatio
        // El símbolo se centra en el sello que acaba de desaparecer.
        val markLeft: Dp
        val markTop: Dp
        with(density) {
            markLeft = origin.x.toDp() - markWidth / 2
            markTop = origin.y.toDp() - markHeight / 2
        }

        Box(modifier = Modifier.offset(x = markLeft, y = markTop)) {
            UniStackBrandMark.Pills.forEachIndexed { index, pill ->
                UniStackBrandPill(
                    pill = pill,
                    markWidth = markWidth,
                    modifier = Modifier.graphicsLayer {
                        // La distancia se mide en anchos de pantalla, para que salgan de
                        // ella en cualquier tamaño de dispositivo.
                        translationX = pillOffsets[index].value * size.width.coerceAtLeast(1f) * 4f
                        alpha = pillAlphas[index].value
                        scaleX = pillScales[index].value
                        scaleY = pillScales[index].value
                    }
                )
            }
        }

        SetupFinishWave(origin = origin, progress = waveProgress.value)
    }
}

/** Círculo del color de marca que crece desde [origin] hasta cubrir la pantalla. */
@Composable
private fun SetupFinishWave(origin: Offset, progress: Float) {
    if (progress <= 0f) return
    val color = UniStackColors.Primary
    Canvas(modifier = Modifier.fillMaxSize()) {
        // Radio necesario para cubrir la esquina más lejana desde el origen.
        val farthest = maxOf(
            hypot(origin.x, origin.y),
            hypot(size.width - origin.x, origin.y),
            hypot(origin.x, size.height - origin.y),
            hypot(size.width - origin.x, size.height - origin.y)
        )
        drawCircle(color = color, radius = farthest * progress, center = origin)
    }
}
