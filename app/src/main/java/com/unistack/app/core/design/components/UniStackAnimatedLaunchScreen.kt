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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

private const val UniText = "Uni"
private const val StackText = "Stack"

// Los gradientes del propio símbolo de marca, medidos del PNG para poder animar cada
// píldora por separado. No salen de los tokens a propósito: son el logo, no interfaz, y
// deben verse igual en claro, oscuro y OLED, como se vería la imagen.
private val Pill1Colors = listOf(Color(0xFF9B7FE6), Color(0xFFC4ABF0)) // design-tokens-ok: color de marca
private val Pill2Colors = listOf(Color(0xFF4E2A8E), Color(0xFF7B5CC0)) // design-tokens-ok: color de marca
private val Pill3Colors = listOf(Color(0xFF6B8BF5), Color(0xFF9B6FF0)) // design-tokens-ok: color de marca

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

@Composable
fun UniStackAnimatedLaunchScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit
) {
    val latestOnAnimationFinished by rememberUpdatedState(onAnimationFinished)

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

    LaunchedEffect(Unit) {
        val springEasing = cascadeSpringEasing()

        delay(260)
        pill1X.animateTo(0f, tween(durationMillis = 440, easing = springEasing))
        launch {
            glow1Alpha.snapTo(0.7f)
            glow1Alpha.animateTo(0f, tween(durationMillis = 300, easing = LinearEasing))
        }

        delay(90)
        pill2X.animateTo(0f, tween(durationMillis = 340, easing = springEasing))
        launch {
            glow2Alpha.snapTo(0.7f)
            glow2Alpha.animateTo(0f, tween(durationMillis = 300, easing = LinearEasing))
        }

        delay(60)
        pill3X.animateTo(0f, tween(durationMillis = 250, easing = springEasing))
        launch {
            glow3Alpha.snapTo(0.7f)
            glow3Alpha.animateTo(0f, tween(durationMillis = 300, easing = LinearEasing))
        }

        delay(320)
        wordmarkAlpha.snapTo(1f)
        cursorAlpha.snapTo(1f)

        repeat(UniText.length) {
            uniRevealCount++
            delay(42)
        }
        delay(260)
        repeat(StackText.length) {
            stackRevealCount++
            delay(42)
        }

        repeat(2) {
            delay(220)
            cursorAlpha.snapTo(0f)
            delay(220)
            cursorAlpha.snapTo(1f)
        }
        delay(180)
        cursorAlpha.animateTo(0f, tween(durationMillis = 200))

        delay(220)
        latestOnAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
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
                        .background(UniStackColors.TextPrimary)
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
    val stageWidth = 168.dp
    val stageHeight = 144.dp

    Box(
        modifier = modifier.size(width = stageWidth, height = stageHeight)
    ) {
        UniStackLaunchPill(
            left = 40.dp,
            top = 2.dp,
            width = 110.dp,
            height = 39.dp,
            gradientColors = Pill1Colors,
            glowColor = Pill1Colors[1],
            slideX = pill1X,
            glowAlpha = glow1Alpha
        )
        UniStackLaunchPill(
            left = 12.dp,
            top = 46.dp,
            width = 122.dp,
            height = 40.dp,
            gradientColors = Pill2Colors,
            glowColor = Pill2Colors[1],
            slideX = pill2X,
            glowAlpha = glow2Alpha
        )
        UniStackLaunchPill(
            left = 31.dp,
            top = 91.dp,
            width = 115.dp,
            height = 40.dp,
            gradientColors = Pill3Colors,
            glowColor = Pill3Colors[1],
            slideX = pill3X,
            glowAlpha = glow3Alpha
        )
    }
}

@Composable
private fun UniStackLaunchPill(
    left: Dp,
    top: Dp,
    width: Dp,
    height: Dp,
    gradientColors: List<Color>,
    glowColor: Color,
    slideX: Dp,
    glowAlpha: Float
) {
    val glowPad = 4.dp
    Box(
        modifier = Modifier
            .offset(x = left - glowPad, y = top - glowPad)
            .size(width = width + glowPad * 2, height = height + glowPad * 2)
            .graphicsLayer { translationX = slideX.toPx() }
            .border(
                width = 2.dp,
                color = glowColor.copy(alpha = glowAlpha),
                shape = RoundedCornerShape(percent = 50)
            )
    )
    Box(
        modifier = Modifier
            .offset(x = left, y = top)
            .size(width = width, height = height)
            .graphicsLayer { translationX = slideX.toPx() }
            .clip(RoundedCornerShape(percent = 50))
            .background(Brush.horizontalGradient(gradientColors))
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
            withStyle(SpanStyle(color = UniStackColors.TextPrimary)) {
                append(UniText.take(uniRevealCount))
            }
            withStyle(SpanStyle(color = UniStackColors.Primary)) {
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
