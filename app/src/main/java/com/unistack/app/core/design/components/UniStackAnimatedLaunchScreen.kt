package com.unistack.app.core.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun UniStackAnimatedLaunchScreen(
    modifier: Modifier = Modifier,
    onAnimationFinished: () -> Unit
) {
    val latestOnAnimationFinished by rememberUpdatedState(onAnimationFinished)
    val logoAlpha = remember { Animatable(0.35f) }
    val logoScale = remember { Animatable(0.94f) }
    val logoOffsetY = remember { Animatable(3f) }
    val wordmarkRevealProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 260, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1.04f,
                animationSpec = tween(durationMillis = 300, easing = LinearOutSlowInEasing)
            )
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoOffsetY.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }

        delay(600)
        wordmarkRevealProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1_050, easing = FastOutSlowInEasing)
        )

        delay(650)
        latestOnAnimationFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentAlignment = Alignment.Center
    ) {
        UniStackLaunchBrandLockup(
            logoAlpha = logoAlpha.value,
            logoScale = logoScale.value,
            logoOffsetY = logoOffsetY.value,
            wordmarkRevealProgress = wordmarkRevealProgress.value
        )
    }
}

@Composable
private fun UniStackLaunchBrandLockup(
    logoAlpha: Float,
    logoScale: Float,
    logoOffsetY: Float,
    wordmarkRevealProgress: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val revealProgress = wordmarkRevealProgress.coerceIn(0f, 1f)
    val initialLogoSize = 96.dp
    val finalLogoSize = 76.dp
    val logoSize = initialLogoSize + ((finalLogoSize - initialLogoSize) * revealProgress)
    val wordmarkGap = 2.dp
    val wordmarkWidth = 154.dp
    val wordmarkHeight = 48.dp
    val lockupWidth = logoSize + ((wordmarkGap + wordmarkWidth) * revealProgress)
    val lockupHeight = 108.dp
    val logoX = 0.dp
    val logoY = (lockupHeight - logoSize) / 2
    val wordmarkX = logoSize + (wordmarkGap * revealProgress)
    val wordmarkY = logoY + ((logoSize - wordmarkHeight) / 2) - 7.dp
    val wordmarkAlpha = ((revealProgress - 0.08f) / 0.92f).coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .offset(y = (-14).dp)
            .size(width = lockupWidth, height = lockupHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        UniStackLogoMark(
            size = logoSize,
            modifier = Modifier
                .offset(x = logoX, y = logoY)
                .size(logoSize)
                .graphicsLayer {
                    alpha = logoAlpha
                    scaleX = logoScale
                    scaleY = logoScale
                    translationY = with(density) { logoOffsetY.dp.toPx() }
                }
        )
        Box(
            modifier = Modifier
                .offset(x = wordmarkX, y = wordmarkY)
                .size(width = wordmarkWidth * revealProgress, height = wordmarkHeight)
                .clipToBounds(),
            contentAlignment = Alignment.CenterStart
        ) {
            UniStackLaunchWordmark(
                modifier = Modifier
                    .requiredWidth(wordmarkWidth)
                    .graphicsLayer {
                        alpha = wordmarkAlpha
                    }
            )
        }
    }
}

@Composable
private fun UniStackLaunchWordmark(modifier: Modifier = Modifier) {
    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = UniStackColors.TextPrimary)) {
                append("Uni")
            }
            withStyle(SpanStyle(color = UniStackColors.Primary)) {
                append("Stack")
            }
        },
        modifier = modifier,
        style = MaterialTheme.typography.headlineMedium.copy(
            fontSize = 36.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        fontWeight = FontWeight.ExtraBold,
        maxLines = 1,
        overflow = TextOverflow.Clip,
        softWrap = false
    )
}
