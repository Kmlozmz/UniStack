package com.unistack.app.feature_home.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.components.SquishyButton

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
import androidx.compose.material.icons.rounded.AutoAwesome
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.graphicsLayer
import com.unistack.app.core.design.components.floatingOffset
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary

@Composable
internal fun PriorityHero(
    title: String,
    description: String,
    action: HomePriorityAction,
    actionLabel: String,
    compact: Boolean,
    onOpenClick: () -> Unit,
    onDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val heroLabel = title.heroLabel(action)
    val heroHeight = if (compact) 174.dp else 190.dp
    val heroPadding = if (compact) 16.dp else 18.dp
    val accessibility = LocalAccessibilityPreferences.current
    val motionScale = LocalMotionDurationScale.current
    val heroMotionScale = if (accessibility.heroAnimationEnabled) motionScale else 0f
    val heroStar = HomeHeroStar
    // Se lee aquí porque dentro del Canvas ya no hay contexto @Composable.
    val heroOrnament = HomeHeroOrnament

    // Mismo movimiento ambiente que los heroes del onboarding: floatingOffset en vez de una
    // transición propia. Con los recorridos y los tiempos desparejados, los dos círculos
    // nunca coinciden en su punto alto y el conjunto no late a compás.
    val circleBigFloat = floatingOffset(travel = 5f, durationMillis = 4200, label = "home-hero-circle-big")
    val circleSmallFloat = floatingOffset(travel = 3.5f, durationMillis = 5600, label = "home-hero-circle-small")
    val sparkleBig = floatingOffset(travel = 0.22f, durationMillis = 1500, label = "home-hero-sparkle-big")
    val sparkleSmall = floatingOffset(travel = 0.28f, durationMillis = 1900, label = "home-hero-sparkle-small")
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
                        y = size.height * 0.02f + circleBigFloat.dp.toPx()
                    )
                )
                drawCircle(
                    color = heroOrnament,
                    radius = size.height * 0.26f,
                    center = Offset(
                        x = size.width * 0.80f,
                        y = size.height * 1.02f + circleSmallFloat.dp.toPx()
                    )
                )
            }

            // Los mismos destellos del onboarding: AutoAwesome girando y latiendo, en vez del
            // trazo dibujado a mano que había aquí. Se colocan sobre los círculos, que es
            // donde hay sitio libre, y no encima del texto.
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = heroStar.copy(alpha = 0.62f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-26).dp, y = 30.dp)
                    .graphicsLayer {
                        scaleX = 1f + sparkleBig
                        scaleY = 1f + sparkleBig
                        rotationZ = sparkleBig * 45f
                        alpha = 0.72f + sparkleBig
                    }
                    .size(18.dp)
            )
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = heroStar.copy(alpha = 0.5f),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-54).dp, y = 54.dp)
                    .graphicsLayer {
                        scaleX = 1f + sparkleSmall
                        scaleY = 1f + sparkleSmall
                        rotationZ = -sparkleSmall * 55f
                        alpha = 0.66f + sparkleSmall
                    }
                    .size(10.dp)
            )

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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = null,
                            tint = HomeHeroStar,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = heroLabel,
                            color = HomeHeroLabel,
                            fontSize = 10.sp,
                            lineHeight = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.6.sp
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
                    // Píldora rellena del acento. Mientras la tarjeta era del color de marca
                    // el botón usaba el contenedor —el mismo color habría desaparecido—, pero
                    // sobre el grafito del hero el acento a plena saturación es justo lo que
                    // hace que la acción se vea antes que nada.
                    Box(
                        modifier = Modifier
                            .height(if (compact) 36.dp else 38.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.Primary)
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
                                color = UniStackColors.OnPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Rounded.ChevronRight,
                                contentDescription = null,
                                tint = UniStackColors.OnPrimary,
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
                    .background(HeroContent.copy(alpha = 0.14f), AppShapes.Pill)
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
                SquishyButton(
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
                SquishyButton(
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

/**
 * Rótulo que encabeza la tarjeta y dice de qué va el aviso.
 *
 * El comodín era «PULSO DE HOY», que no significa nada concreto: ni el usuario sabe qué es
 * un pulso ni la tarjeta está midiendo ninguno. Y caía ahí más de la cuenta, porque las
 * palabras clave no cubren todos los casos —un aviso de clase no contiene ninguna—.
 *
 * Ahora el respaldo sale de [HomePriorityAction], que es un dato real que la prioridad ya
 * traía consigo, en vez de una cadena inventada. Las palabras clave siguen delante porque
 * distinguen matices que la acción no ve: dos avisos que llevan a la misma pantalla pueden
 * ser una alerta o una simple proyección.
 */
private fun String.heroLabel(action: HomePriorityAction): String {
    val normalized = lowercase()
    return when {
        "espera su nota" in normalized || "resultados esperan" in normalized -> "RESULTADO PENDIENTE"
        "historial" in normalized -> "DATOS POR COMPLETAR"
        "ajusta" in normalized -> "PROYECCIÓN"
        "venc" in normalized || "necesita" in normalized || "sobre el límite" in normalized -> "ALERTA"
        "cerca" in normalized || "atención" in normalized || "limite" in normalized -> "ENFOQUE"
        "gasto" in normalized -> "FINANZAS"
        "primera" in normalized || "materia" in normalized || "nota" in normalized || "semestre" in normalized -> "PRÓXIMO PASO"
        else -> when (action) {
            HomePriorityAction.SCHEDULE -> "PRÓXIMA CLASE"
            HomePriorityAction.TASKS -> "PENDIENTES"
            HomePriorityAction.EXPENSES -> "FINANZAS"
            HomePriorityAction.TEMPLATES -> "TRABAJOS"
            HomePriorityAction.SUBJECT, HomePriorityAction.SUBJECTS -> "TUS MATERIAS"
        }
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
