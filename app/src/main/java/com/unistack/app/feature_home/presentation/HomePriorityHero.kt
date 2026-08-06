package com.unistack.app.feature_home.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary

@Composable
internal fun PriorityHero(
    title: String,
    description: String,
    actionLabel: String,
    compact: Boolean,
    onOpenClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroLabel = title.heroLabel()
    val heroHeight = if (compact) 174.dp else 190.dp
    val heroPadding = if (compact) 16.dp else 18.dp
    val isDarkTheme = UniStackColors.IsDarkTheme
    val accessibility = LocalAccessibilityPreferences.current
    val motionScale = LocalMotionDurationScale.current
    val heroMotionScale = if (accessibility.heroAnimationEnabled) motionScale else 0f
    val heroStar = HomeHeroStar
    val heroStarSoft = HomeHeroStarSoft
    val heroAssetShadow = HomeHeroAssetShadow
    val sparkleMotion = rememberInfiniteTransition(label = "heroSparkleMotion")
    val sparkleOneFloat by sparkleMotion.animateFloat(
        initialValue = 2f * heroMotionScale,
        targetValue = -3f * heroMotionScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleOneFloat"
    )
    val sparkleTwoFloat by sparkleMotion.animateFloat(
        initialValue = -1f * heroMotionScale,
        targetValue = 4f * heroMotionScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleTwoFloat"
    )
    val sparkleThreeFloat by sparkleMotion.animateFloat(
        initialValue = 1f * heroMotionScale,
        targetValue = -2.5f * heroMotionScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleThreeFloat"
    )
    val sparkleOneAlpha by sparkleMotion.animateFloat(
        initialValue = 0.78f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleOneAlpha"
    )
    val sparkleTwoAlpha by sparkleMotion.animateFloat(
        initialValue = 1f,
        targetValue = 0.72f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleTwoAlpha"
    )
    val sparkleThreeAlpha by sparkleMotion.animateFloat(
        initialValue = 0.70f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroSparkleThreeAlpha"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight)
            .cleanClickable(onDetailsClick),
        shape = RoundedCornerShape(19.dp),
        color = Color.Transparent,
        border = BorderStroke(1.dp, HomeHeroStroke),
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HeroBrush)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeHeroLightViolet.copy(alpha = 0.24f),
                                HomeHeroVioletWash.copy(alpha = 0.20f),
                                HomeHeroVioletDepth.copy(alpha = 0.08f),
                                Color.Transparent
                            )
                        } else {
                            listOf(
                                HomeHeroLightModeGlow.copy(alpha = 0.70f),
                                HomeHeroLightModeAccent.copy(alpha = 0.34f),
                                Color.Transparent
                            )
                        },
                        center = Offset(size.width * 0.79f, size.height * 0.52f),
                        radius = size.width * if (isDarkTheme) 0.42f else 0.52f
                    )
                )
                drawRect(
                    brush = Brush.linearGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                Color.Transparent,
                                HomeHeroTransition.copy(alpha = 0.10f),
                                HomeHeroVioletDepth.copy(alpha = 0.18f)
                            )
                        } else {
                            listOf(
                                Color.Transparent,
                                HomeHeroLightModeAccent.copy(alpha = 0.24f),
                                HomeHeroLightModeDepth.copy(alpha = 0.18f)
                            )
                        },
                        start = Offset(size.width * 0.36f, size.height * 0.16f),
                        end = Offset(size.width * 1.04f, size.height * 0.88f)
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeHeroLight.copy(alpha = 0.020f),
                                Color.Transparent,
                                HomeShadow.copy(alpha = 0.24f)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.64f),
                                Color.Transparent,
                                HomeHeroLightModeDepth.copy(alpha = 0.12f)
                            )
                        }
                    )
                )
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = if (isDarkTheme) {
                            listOf(
                                HomeShadow.copy(alpha = 0.30f),
                                Color.Transparent,
                                HomeShadow.copy(alpha = 0.18f)
                            )
                        } else {
                            listOf(
                                HomeHeroLightModeDepth.copy(alpha = 0.08f),
                                Color.Transparent,
                                HomeHeroLightModeAccent.copy(alpha = 0.12f)
                            )
                        }
                    )
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            HomeShadow.copy(alpha = if (isDarkTheme) 0.08f else 0.02f),
                            Color.Transparent,
                            HomeShadow.copy(alpha = if (isDarkTheme) 0.22f else 0.04f)
                        )
                    )
                )
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            heroAssetShadow.copy(alpha = if (isDarkTheme) 0.46f else 0.22f),
                            HomeHeroVioletDepth.copy(alpha = if (isDarkTheme) 0.16f else 0.06f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.80f, size.height * 0.77f),
                        radius = size.width * 0.24f
                    ),
                    topLeft = Offset(size.width * 0.64f, size.height * 0.68f),
                    size = Size(size.width * 0.32f, size.height * 0.18f)
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.55f, size.height * 0.37f + sparkleOneFloat.dp.toPx()),
                    radius = size.minDimension * 0.022f,
                    color = heroStar,
                    alpha = (if (isDarkTheme) 0.54f else 0.40f) * sparkleOneAlpha
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.88f, size.height * 0.27f + sparkleTwoFloat.dp.toPx()),
                    radius = size.minDimension * 0.030f,
                    color = heroStarSoft,
                    alpha = (if (isDarkTheme) 0.48f else 0.34f) * sparkleTwoAlpha
                )
                drawSoftSparkle(
                    center = Offset(size.width * 0.68f, size.height * 0.24f + sparkleThreeFloat.dp.toPx()),
                    radius = size.minDimension * 0.014f,
                    color = heroStar,
                    alpha = (if (isDarkTheme) 0.38f else 0.26f) * sparkleThreeAlpha
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(heroPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.56f)
                        .align(Alignment.CenterStart),
                    verticalArrangement = Arrangement.spacedBy(if (compact) 7.dp else 8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PremiumSparkle(tint = HomeHeroStar, modifier = Modifier.size(13.dp))
                        Text(
                            text = heroLabel,
                            color = HomeHeroLabel,
                            fontSize = 8.sp,
                            lineHeight = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.sp
                        )
                    }
                    Text(
                        text = title,
                        color = HomeHeroTitle,
                        fontSize = if (compact) 20.sp else 22.sp,
                        lineHeight = if (compact) 26.sp else 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = description,
                        color = HomeHeroSecondary,
                        fontSize = if (compact) 10.sp else 11.sp,
                        lineHeight = if (compact) 15.sp else 16.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.cleanClickable(onDetailsClick)
                    )
                    Box(
                        modifier = Modifier
                            .width(if (compact) 128.dp else 138.dp)
                            .height(if (compact) 32.dp else 34.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Brush.linearGradient(listOf(HomeHeroButtonStart, HomeHeroButtonEnd)))
                            .cleanClickable(onOpenClick)
                            .padding(horizontal = 13.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                actionLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }
                }

                Image(
                    painter = painterResource(R.drawable.hero_notebook_pen),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(if (compact) 144.dp else 160.dp)
                        .height(if (compact) 154.dp else 176.dp)
                        .offset(x = if (compact) 20.dp else 24.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun PriorityContextSheet(
    priority: HomePrioritySummary,
    actionLabel: String,
    onActionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyParagraphs = priority.sheetBodyParagraphs()
    val suggestionText = priority.sheetSuggestionText()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = HomePrioritySheetSurface,
        contentColor = HomePrioritySheetText,
        scrimColor = Color.Black.copy(alpha = 0.64f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(100.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 30.dp, end = 30.dp, bottom = 42.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PrioritySunBadge()
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = priority.title,
                        color = HomePrioritySheetText,
                        fontSize = 25.sp,
                        lineHeight = 29.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "✦ Tu prioridad de hoy",
                        color = HomePrioritySheetAccentSoft,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                bodyParagraphs.forEach { paragraph ->
                    Text(
                        text = paragraph,
                        color = HomePrioritySheetBody,
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HomePrioritySheetSuggestion, RoundedCornerShape(18.dp))
                    .border(
                        width = 0.7.dp,
                        color = HomePrioritySheetCardBorder,
                        shape = RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                PriorityBulbBadge()
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Sugerencia",
                        color = HomePrioritySheetAccentSoft,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = suggestionText,
                        color = HomePrioritySheetBody,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePrioritySheetSecondaryButton,
                        contentColor = HomePrioritySheetText
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text("Cerrar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = onActionClick,
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePurple,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(actionLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun PrioritySunBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(56.dp)
            .background(HomePrioritySheetIconCircle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(32.dp)) {
            val center = this.center
            val rayStart = size.minDimension * 0.35f
            val rayEnd = size.minDimension * 0.48f
            drawCircle(
                color = HomePrioritySheetSun,
                radius = size.minDimension * 0.20f,
                center = center
            )
            repeat(8) { index ->
                val angle = Math.toRadians((index * 45).toDouble())
                val start = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * rayStart,
                    y = center.y + kotlin.math.sin(angle).toFloat() * rayStart
                )
                val end = Offset(
                    x = center.x + kotlin.math.cos(angle).toFloat() * rayEnd,
                    y = center.y + kotlin.math.sin(angle).toFloat() * rayEnd
                )
                drawLine(
                    color = HomePrioritySheetSun,
                    start = start,
                    end = end,
                    strokeWidth = 2.2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
private fun PriorityBulbBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .background(HomePrioritySheetIconCircle, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Lightbulb,
            contentDescription = null,
            tint = HomePrioritySheetAccentSoft,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
internal fun PremiumSparkle(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        drawSoftSparkle(
            center = Offset(size.width * 0.5f, size.height * 0.5f),
            radius = size.minDimension * 0.42f,
            color = tint,
            alpha = 0.92f
        )
    }
}

internal fun DrawScope.drawSoftSparkle(
    center: Offset,
    radius: Float,
    color: Color,
    alpha: Float
) {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                color.copy(alpha = alpha * 0.22f),
                Color.Transparent
            ),
            center = center,
            radius = radius * 2.25f
        ),
        radius = radius * 2.25f,
        center = center
    )

    val sparkle = Path().apply {
        moveTo(center.x, center.y - radius)
        cubicTo(
            center.x + radius * 0.14f,
            center.y - radius * 0.28f,
            center.x + radius * 0.28f,
            center.y - radius * 0.14f,
            center.x + radius,
            center.y
        )
        cubicTo(
            center.x + radius * 0.28f,
            center.y + radius * 0.14f,
            center.x + radius * 0.14f,
            center.y + radius * 0.28f,
            center.x,
            center.y + radius
        )
        cubicTo(
            center.x - radius * 0.14f,
            center.y + radius * 0.28f,
            center.x - radius * 0.28f,
            center.y + radius * 0.14f,
            center.x - radius,
            center.y
        )
        cubicTo(
            center.x - radius * 0.28f,
            center.y - radius * 0.14f,
            center.x - radius * 0.14f,
            center.y - radius * 0.28f,
            center.x,
            center.y - radius
        )
        close()
    }
    drawPath(sparkle, color.copy(alpha = alpha))
    drawCircle(color = Color.White.copy(alpha = alpha * 0.18f), radius = radius * 0.16f, center = center)
}

internal fun HomePriorityAction.actionLabel(): String {
    return when (this) {
        HomePriorityAction.SUBJECT -> "Abrir materia"
        HomePriorityAction.SUBJECTS -> "Ver materias"
        HomePriorityAction.TASKS -> "Ver mis tareas"
        HomePriorityAction.EXPENSES -> "Ver gastos"
        HomePriorityAction.TEMPLATES -> "Ver trabajos"
        HomePriorityAction.SCHEDULE -> "Ver horario"
    }
}

private fun String.heroLabel(): String {
    val normalized = lowercase()
    return when {
        "espera su nota" in normalized || "resultados esperan" in normalized -> "RESULTADO PENDIENTE"
        "historial" in normalized -> "DATOS POR COMPLETAR"
        "ajusta" in normalized -> "PROYECCIÓN"
        "venc" in normalized || "necesita" in normalized || "sobre el límite" in normalized -> "ALERTA"
        "cerca" in normalized || "atención" in normalized || "limite" in normalized -> "ENFOQUE"
        "gasto" in normalized -> "FINANZAS"
        "primera" in normalized || "materia" in normalized || "nota" in normalized || "semestre" in normalized -> "PRÓXIMO PASO"
        else -> "PULSO DE HOY"
    }
}

private fun HomePrioritySummary.sheetBodyParagraphs(): List<String> {
    if (title == "Día despejado") {
        return listOf(
            "No tienes vencimientos cercanos por ahora.",
            "Es un buen momento para repasar, avanzar en tus materias y dejar listas tus próximas actividades."
        )
    }

    val sentences = fullDescription
        .split(". ")
        .mapIndexed { index, part ->
            val trimmed = part.trim()
            if (trimmed.endsWith(".") || index == fullDescription.split(". ").lastIndex) trimmed else "$trimmed."
        }
        .filter { it.isNotBlank() }

    return when {
        sentences.size >= 2 -> listOf(sentences.first(), sentences.drop(1).joinToString(" "))
        sentences.size == 1 -> listOf(sentences.first())
        else -> listOf(shortDescription)
    }
}

private fun HomePrioritySummary.sheetSuggestionText(): String {
    if (title == "Día despejado") {
        return "Dedica al menos 15 minutos a repasar hoy para mantener el ritmo."
    }
    return suggestion.substringAfter(": ", suggestion)
        .replaceFirstChar { char -> char.uppercase() }
}
