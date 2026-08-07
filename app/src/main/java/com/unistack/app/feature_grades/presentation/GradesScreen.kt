package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.CategoricalSubjectAccents
import com.unistack.app.core.design.theme.CategoricalSubjectBackgrounds
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.core.utils.bounceClick
import java.util.Locale

@Composable
fun GradesScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    embedded: Boolean = false
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val averages = subjects.mapNotNull(viewModel::currentAverage)
    val generalAverage = averages.takeIf { it.isNotEmpty() }?.average()
    val evaluatedSubjects = subjects.count { it.grades.isNotEmpty() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = 22.dp,
                top = if (embedded) 10.dp else 58.dp,
                end = 22.dp,
                bottom = 118.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!embedded) {
                item {
                    FeatureHeader(
                        title = "Materias",
                        subtitle = "Administra tus materias, notas y porcentajes."
                    )
                }
            }
            item {
                SubjectsStatsRow(
                    subjectCount = subjects.size,
                    generalAverage = generalAverage,
                    evaluatedSubjects = evaluatedSubjects,
                    gradingScale = scale
                )
            }
            item {
                Text(
                    text = "Tus materias",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (subjects.isEmpty()) {
                item {
                    EmptyGradesCard()
                }
            } else {
                items(subjects, key = { it.id }) { subject ->
                    SubjectListCard(
                        subject = subject,
                        average = viewModel.currentAverage(subject),
                        evaluatedPercentage = viewModel.evaluatedPercentage(subject),
                        gradingScale = scale,
                        onClick = { onSubjectClick(subject.id) }
                    )
                }
            }
        }

        AddSubjectButton(
            onClick = onAddSubjectClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
        )
    }
}

@Composable
private fun SubjectsStatsRow(
    subjectCount: Int,
    generalAverage: Double?,
    evaluatedSubjects: Int,
    gradingScale: GradingScale
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(
            value = subjectCount.toString(),
            label = if (subjectCount == 1) "Materia" else "Materias",
            icon = Icons.Rounded.Book,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = generalAverage?.let { GradingScaleUtils.formatGrade(it, gradingScale) } ?: "--",
            label = "Promedio",
            icon = Icons.Rounded.Grade,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
        MetricCard(
            value = evaluatedSubjects.toString(),
            label = if (evaluatedSubjects == 1) "Evaluada" else "Evaluadas",
            icon = Icons.Rounded.BarChart,
            iconColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectListCard(
    subject: Subject,
    average: Double?,
    evaluatedPercentage: Double,
    gradingScale: GradingScale,
    onClick: () -> Unit
) {
    val progressVisual = rememberSubjectProgressVisual(
        subject = subject,
        average = average,
        gradingScale = gradingScale
    )
    val subjectColor = subjectAccent(subject)
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(112.dp)
            .bounceClick(onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(subjectColor)
            )
            Box(
                modifier = Modifier
                    .padding(start = 18.dp)
                    .size(46.dp)
                    .background(subjectColor.copy(alpha = if (UniStackColors.IsDarkTheme) 0.18f else 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    tint = subjectColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 14.dp, end = 8.dp)
                    .weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subject.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    SubjectStatusPill(
                        text = subjectStatusText(average, subject.targetAverage, evaluatedPercentage),
                        color = progressVisual.color
                    )
                }
                Text(
                    subjectNextAction(subject, average, evaluatedPercentage, gradingScale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                SubjectProgressBar(
                    evaluatedPercentage = evaluatedPercentage,
                    color = progressVisual.color
                )
                Text(
                    "${String.format(Locale.US, "%.0f", evaluatedPercentage)}% evaluado de la materia",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            SubjectProgressMetric(progressVisual)
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 4.dp, end = 10.dp)
                    .size(22.dp)
            )
        }
    }
}

private data class SubjectProgressVisual(
    val color: Color,
    val text: String
)

@Composable
private fun rememberSubjectProgressVisual(
    subject: Subject,
    average: Double?,
    gradingScale: GradingScale
): SubjectProgressVisual {
    val target = subject.targetAverage
    val targetText = GradingScaleUtils.formatGrade(target, gradingScale)
    val color = when {
        average == null -> MaterialTheme.colorScheme.primary.copy(alpha = if (UniStackColors.IsDarkTheme) 0.72f else 0.62f)
        average >= target -> UniStackColors.Teal
        target - average <= 0.5 -> UniStackColors.Yellow
        else -> UniStackColors.Coral
    }

    return if (average == null) {
        SubjectProgressVisual(
            color = color,
            text = "Meta $targetText"
        )
    } else {
        SubjectProgressVisual(
            color = color,
            text = "${GradingScaleUtils.formatGrade(average, gradingScale)} / $targetText"
        )
    }
}

@Composable
private fun SubjectProgressMetric(progressVisual: SubjectProgressVisual) {
    Text(
        text = progressVisual.text,
        color = progressVisual.color,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun SubjectStatusPill(text: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(AppShapes.Pill)
            .background(color.copy(alpha = if (UniStackColors.IsDarkTheme) 0.16f else 0.12f))
            .padding(horizontal = 9.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
    }
}

@Composable
private fun SubjectProgressBar(
    evaluatedPercentage: Double,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(AppShapes.Pill)
            .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth((evaluatedPercentage / 100.0).coerceIn(0.0, 1.0).toFloat())
                .fillMaxHeight()
                .background(color)
        )
    }
}

private fun subjectStatusText(
    average: Double?,
    targetAverage: Double,
    evaluatedPercentage: Double
): String {
    return when {
        average == null -> "Sin notas"
        evaluatedPercentage >= 99.9 -> "Completa"
        average >= targetAverage -> "Sobre meta"
        targetAverage - average <= 0.5 -> "Atención"
        else -> "Revisar"
    }
}

private fun subjectNextAction(
    subject: Subject,
    average: Double?,
    evaluatedPercentage: Double,
    gradingScale: GradingScale
): String {
    if (average == null) return "Agrega la primera nota para activar la proyección."
    val remaining = (100.0 - evaluatedPercentage).coerceAtLeast(0.0)
    return if (remaining > 0.0) {
        "Promedio ${GradingScaleUtils.formatGrade(average, gradingScale)}. Falta ${String.format(Locale.US, "%.0f", remaining)}% por evaluar."
    } else {
        "Promedio final ${GradingScaleUtils.formatGrade(average, gradingScale)} en ${subject.name}."
    }
}

@Composable
private fun EmptyGradesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Aún no tienes materias.", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text("Crea tu primera materia para empezar a calcular tu promedio.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun FeatureHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun AddSubjectButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .cleanClickable(onClick),
        shape = AppShapes.LargeCard,
        color = MaterialTheme.colorScheme.primary,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(UniStackColors.OnPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = UniStackColors.contentColorOn(UniStackColors.Primary),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Agregar materia",
                color = UniStackColors.contentColorOn(UniStackColors.Primary),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

fun subjectAccent(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> UniStackColors.Teal
    SubjectVisualType.BLUE -> UniStackColors.Blue
    SubjectVisualType.CORAL -> UniStackColors.Coral
    SubjectVisualType.PURPLE -> UniStackColors.Primary
    SubjectVisualType.GREEN -> UniStackColors.Green
    SubjectVisualType.YELLOW -> UniStackColors.Yellow
    SubjectVisualType.ROSE -> CategoricalSubjectAccents.Rose
    SubjectVisualType.INDIGO -> CategoricalSubjectAccents.Indigo
    SubjectVisualType.ORANGE -> CategoricalSubjectAccents.Orange
    SubjectVisualType.CYAN -> CategoricalSubjectAccents.Cyan
    SubjectVisualType.LIME -> CategoricalSubjectAccents.Lime
    SubjectVisualType.SLATE -> CategoricalSubjectAccents.Slate
}

fun subjectAccent(subject: Subject): Color {
    return subject.customColor?.let { Color(it) } ?: subjectAccent(subject.visualType)
}

fun subjectBackground(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> UniStackColors.TealLight
    SubjectVisualType.BLUE -> UniStackColors.BlueLight
    SubjectVisualType.CORAL -> UniStackColors.CoralLight
    SubjectVisualType.PURPLE -> UniStackColors.PrimaryLight
    SubjectVisualType.GREEN -> UniStackColors.GreenLight
    SubjectVisualType.YELLOW -> UniStackColors.YellowLight
    SubjectVisualType.ROSE -> CategoricalSubjectBackgrounds.rose(UniStackColors.IsDarkTheme)
    SubjectVisualType.INDIGO -> CategoricalSubjectBackgrounds.indigo(UniStackColors.IsDarkTheme)
    SubjectVisualType.ORANGE -> CategoricalSubjectBackgrounds.orange(UniStackColors.IsDarkTheme)
    SubjectVisualType.CYAN -> CategoricalSubjectBackgrounds.cyan(UniStackColors.IsDarkTheme)
    SubjectVisualType.LIME -> CategoricalSubjectBackgrounds.lime(UniStackColors.IsDarkTheme)
    SubjectVisualType.SLATE -> CategoricalSubjectBackgrounds.slate(UniStackColors.IsDarkTheme)
}

@Composable
private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
