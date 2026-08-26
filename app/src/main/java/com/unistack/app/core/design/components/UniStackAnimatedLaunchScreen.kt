package com.unistack.app.core.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

private const val UniText = "Uni"
private const val StackText = "Stack"

/** Ancho del símbolo en la pantalla de arranque; el alto sale de su proporción. */
private val LaunchMarkWidth = 168.dp

private fun cascadeSpringEasing(w: Float = 9.6f, d: Float = 0.6f): Easing {
    val wd = w * sqrt(1f - d * d)
    return Easing { t ->
        when {
            t <= 0f -> 0f
            t >= 1f -> 1f
            else -> {
                val decay = exp((-d * w * t).toDouble())
                val osc = cos((wd * t).toDouble()) + (d / sqrt(1f - d * d)) * sin((wd * t).toDouble())
                (1.0 - decay * osc).toFloat()
            }
        }
    }
}

/**
 * @param timeScale factor sobre todos los tiempos. Escala la animación entera en lugar de
 *   recortarle fases: la coreografía es idéntica, solo transcurre más deprisa. Se usa para
 *   que quien ya pasó por el setup no vuelva a esperar la versión larga cada vez que abre.
 */
@Composable
fun UniStackAnimatedLaunchScreen(
    modifier: Modifier = Modifier,
    timeScale: Float = 1f,
    onAnimationFinished: () -> Unit
) {
    val latestOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    val scale = timeScale.coerceIn(0.2f, 1f)
    fun Int.scaled(): Int = (this * scale).toInt().coerceAtLeast(1)
    suspend fun wait(millis: Int) = delay(millis.scaled().toLong())

    val pill1X = remember { Animatable(280f) }
    val pill2X = remember { Animatable(-280f) }
    val pill3X = remember { Animatable(280f) }

    val glow1Alpha = remember { Animatable(0f) }
    val glow2Alpha = remember { Animatable(0f) }
    val glow3Alpha = remember { Animatable(0f) }

    var uniRevealCount by remember { mutableIntStateOf(0) }
    var stackRevealCount by remember { mutableIntStateOf(0) }
    val wordmarkAlpha = remember { Animatable(0f) }
    val cursorAlpha = remember { Animatable(0f) }

    LaunchedEffect(scale) {
        val springEasing = cascadeSpringEasing()

        wait(260)
        pill1X.animateTo(0f, tween(durationMillis = 440.scaled(), easing = springEasing))
        launch {
            glow1Alpha.snapTo(0.7f)
            glow1Alpha.animateTo(0f, tween(durationMillis = 300.scaled(), easing = LinearEasing))
        }

        wait(90)
        pill2X.animateTo(0f, tween(durationMillis = 340.scaled(), easing = springEasing))
        launch {
            glow2Alpha.snapTo(0.7f)
            glow2Alpha.animateTo(0f, tween(durationMillis = 300.scaled(), easing = LinearEasing))
        }

        wait(60)
        pill3X.animateTo(0f, tween(durationMillis = 250.scaled(), easing = springEasing))
        launch {
            glow3Alpha.snapTo(0.7f)
            glow3Alpha.animateTo(0f, tween(durationMillis = 300.scaled(), easing = LinearEasing))
        }

        wait(320)
        wordmarkAlpha.snapTo(1f)
        cursorAlpha.snapTo(1f)

        repeat(UniText.length) {
            uniRevealCount++
            wait(42)
        }
        wait(260)
        repeat(StackText.length) {
            stackRevealCount++
            wait(42)
        }

        repeat(2) {
            wait(220)
            cursorAlpha.snapTo(0f)
            wait(220)
            cursorAlpha.snapTo(1f)
        }
        wait(180)
        cursorAlpha.animateTo(0f, tween(durationMillis = 200.scaled()))

        wait(220)
        latestOnAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            UniStackLaunchLogoStage(
                pill1X = pill1X.value.dp,
                pill2X = pill2X.value.dp,
                pill3X = pill3X.value.dp,
                glow1Alpha = glow1Alpha.value,
                glow2Alpha = glow2Alpha.value,
                glow3Alpha = glow3Alpha.value
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.graphicsLayer { alpha = wordmarkAlpha.value }
            ) {
                UniStackLaunchWordmark(
                    uniRevealCount = uniRevealCount,
                    stackRevealCount = stackRevealCount
                )
                Box(
                    modifier = Modifier
                        .size(width = 3.dp, height = 26.dp)
                        .graphicsLayer { alpha = cursorAlpha.value }
                        .background(MaterialTheme.colorScheme.onSurface)
                )
            }
        }
    }
}

@Composable
private fun UniStackLaunchLogoStage(
    pill1X: Dp,
    pill2X: Dp,
    pill3X: Dp,
    glow1Alpha: Float,
    glow2Alpha: Float,
    glow3Alpha: Float,
    modifier: Modifier = Modifier
) {
    val markWidth = LaunchMarkWidth
    val slides = listOf(pill1X, pill2X, pill3X)
    val glows = listOf(glow1Alpha, glow2Alpha, glow3Alpha)

    Box(
        modifier = modifier.size(
            width = markWidth,
            height = markWidth * UniStackBrandMark.HeightRatio
        )
    ) {
        UniStackBrandMark.Pills.forEachIndexed { index, pill ->
            UniStackLaunchPillGlow(
                pill = pill,
                markWidth = markWidth,
                slideX = slides[index],
                alpha = glows[index]
            )
            UniStackBrandPill(
                pill = pill,
                markWidth = markWidth,
                modifier = Modifier.graphicsLayer { translationX = slides[index].toPx() }
            )
        }
    }
}

/** Contorno que destella al aterrizar la píldora, un poco mayor que ella para rodearla. */
@Composable
private fun UniStackLaunchPillGlow(
    pill: UniStackBrandMark.Pill,
    markWidth: Dp,
    slideX: Dp,
    alpha: Float
) {
    val markHeight = markWidth * UniStackBrandMark.HeightRatio
    val pad = 4.dp
    Box(
        modifier = Modifier
            .offset(x = markWidth * pill.left - pad, y = markHeight * pill.top - pad)
            .size(
                width = markWidth * pill.width + pad * 2,
                height = markHeight * pill.height + pad * 2
            )
            .graphicsLayer { translationX = slideX.toPx() }
            .border(
                width = 2.dp,
                color = pill.gradientEnd.copy(alpha = alpha),
                shape = RoundedCornerShape(percent = 50)
            )
    )
}

@Composable
private fun UniStackLaunchWordmark(
    uniRevealCount: Int,
    stackRevealCount: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onSurface)) {
                append(UniText.take(uniRevealCount))
            }
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                append(StackText.take(stackRevealCount))
            }
        },
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 34.sp,
            lineHeight = 38.sp,
            letterSpacing = 0.sp
        ),
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1
    )
}
