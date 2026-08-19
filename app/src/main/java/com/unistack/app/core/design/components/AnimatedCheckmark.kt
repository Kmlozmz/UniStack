package com.unistack.app.core.design.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import kotlinx.coroutines.launch

import androidx.compose.material3.MaterialTheme
/**
 * Marca de verificación que se dibuja trazo a trazo, en vez de aparecer ya hecha.
 *
 * Se redibuja al tocarla, para poder repetir el gesto. Si el usuario desactivó las
 * animaciones se pinta completa y el toque no hace nada.
 */
@Composable
fun AnimatedCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    color: Color = MaterialTheme.colorScheme.onPrimary,
    strokeWidth: Dp = 4.dp,
    durationMillis: Int = 520
) {
    val motionEnabled = LocalMotionDurationScale.current > 0f
    val progress = remember { Animatable(if (motionEnabled) 0f else 1f) }
    val scope = rememberCoroutineScope()

    suspend fun draw() {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis, easing = FastOutSlowInEasing)
        )
    }

    LaunchedEffect(motionEnabled) {
        if (motionEnabled) draw()
    }

    Canvas(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                enabled = motionEnabled,
                onClickLabel = "Repetir animación"
            ) {
                scope.launch { draw() }
            }
    ) {
        val width = this.size.width
        val height = this.size.height
        // Proporciones del trazo dentro del lienzo: bajada corta a la izquierda y subida
        // larga a la derecha, que es la forma canónica del visto.
        val path = Path().apply {
            moveTo(width * 0.20f, height * 0.52f)
            lineTo(width * 0.42f, height * 0.73f)
            lineTo(width * 0.80f, height * 0.28f)
        }

        val measure = PathMeasure().apply { setPath(path, false) }
        val drawn = Path()
        measure.getSegment(0f, measure.length * progress.value, drawn, true)

        drawPath(
            path = drawn,
            color = color,
            style = Stroke(
                width = strokeWidth.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}
