package com.unistack.app.feature_home.presentation

import com.unistack.app.core.design.theme.AppShapes

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val accessibility = LocalAccessibilityPreferences.current
    val motionScale = LocalMotionDurationScale.current
    val heroMotionScale = if (accessibility.heroAnimationEnabled) motionScale else 0f
    val heroStar = HomeHeroStar
    // Se lee aquí porque dentro del Canvas ya no hay contexto @Composable.
    val heroOrnament = HomeHeroOrnament

    // Deriva de los dos círculos decorativos. Antes esta transición movía tres destellos con
    // su propia opacidad; al sustituirlos por los círculos quedan dos recorridos y ninguna
    // animación de opacidad. Sigue respetando heroAnimationEnabled y la escala de movimiento:
    // con el ajuste desactivado el factor es 0 y los círculos se quedan quietos.
    val ornamentMotion = rememberInfiniteTransition(label = "heroOrnamentMotion")
    val sparkleOneFloat by ornamentMotion.animateFloat(
        initialValue = 2f * heroMotionScale,
        targetValue = -3f * heroMotionScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroOrnamentOneFloat"
    )
    val sparkleTwoFloat by ornamentMotion.animateFloat(
        initialValue = -1f * heroMotionScale,
        targetValue = 4f * heroMotionScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroOrnamentTwoFloat"
    )
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(heroHeight)
            .cleanClickable(onDetailsClick),
        shape = AppShapes.MediumCard,
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
                // Dos círculos asomando por el borde derecho, del mismo color del contenido
                // apenas insinuado. Sustituyen a la ilustración del cuaderno y a los
                // destellos: sobre una superficie rellena ya no hacía falta decorar tanto, y
                // la ilustración se comía la mitad del ancho que necesita el texto.
                //
                // Se salen del recuadro a propósito; el recorte de la tarjeta los recorta y
                // eso es lo que les da la sensación de estar detrás de ella. Van con una
                // deriva mínima para que la tarjeta no quede del todo quieta.
                drawCircle(
                    color = heroOrnament,
                    radius = size.height * 0.42f,
                    center = Offset(
                        x = size.width * 0.92f,
                        y = size.height * 0.02f + sparkleOneFloat.dp.toPx()
                    )
                )
                drawCircle(
                    color = heroOrnament,
                    radius = size.height * 0.26f,
                    center = Offset(
                        x = size.width * 0.80f,
                        y = size.height * 1.02f + sparkleTwoFloat.dp.toPx()
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(heroPadding)
            ) {
                Column(
                    // Sin ilustración a la derecha, el texto recupera casi todo el ancho.
                    modifier = Modifier
                        .fillMaxWidth(0.86f)
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
                    // Píldora, como pide el lenguaje expresivo para una acción principal. El
                    // relleno es el contenedor del acento y no el acento: sobre una tarjeta
                    // que ya es del color de marca, un botón del mismo color desaparecería.
                    Box(
                        modifier = Modifier
                            .height(if (compact) 36.dp else 38.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.PrimaryLight)
                            .cleanClickable(onOpenClick)
                            .padding(horizontal = if (compact) 16.dp else 18.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                actionLabel,
                                fontSize = if (compact) 12.sp else 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = UniStackColors.OnPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = UniStackColors.OnPrimaryContainer,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

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
        scrimColor = UniStackColors.Scrim.copy(alpha = 0.64f),
        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 4.dp)
                    .size(width = 42.dp, height = 4.dp)
                    .background(UniStackColors.OnPrimary.copy(alpha = 0.18f), AppShapes.Pill)
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
                    .background(HomePrioritySheetSuggestion, AppShapes.MediumCard)
                    .border(
                        width = 0.7.dp,
                        color = HomePrioritySheetCardBorder,
                        shape = AppShapes.MediumCard
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
                    shape = AppShapes.Small,
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
                    shape = AppShapes.Small,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomePurple,
                        contentColor = UniStackColors.OnPrimary
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
    // El token se lee en composición: dentro del Canvas ya no hay contexto @Composable.
    val sunColor = HomePrioritySheetSun
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
                color = sunColor,
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
                    color = sunColor,
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
    // design-tokens-ok: núcleo del destello, es luz blanca por definición
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
