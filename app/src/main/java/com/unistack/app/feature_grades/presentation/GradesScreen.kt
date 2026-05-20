package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
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
    viewModel: GradesViewModel = viewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val averages = subjects.mapNotNull(viewModel::currentAverage)
    val generalAverage = averages.takeIf { it.isNotEmpty() }?.average()
    val evaluatedSubjects = subjects.count { it.grades.isNotEmpty() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 22.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            FeatureHeader(
                title = "Materias",
                subtitle = "Administra tus materias, notas y porcentajes."
            )
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
                fontWeight = FontWeight.ExtraBold
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
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                AddSubjectButton(
                    onClick = onAddSubjectClick,
                    modifier = Modifier.width(214.dp)
                )
            }
        }
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
        SubjectStatCard(
            value = subjectCount.toString(),
            label = if (subjectCount == 1) "materia" else "materias",
            icon = Icons.Rounded.Book,
            modifier = Modifier.weight(1f)
        )
        SubjectStatCard(
            value = generalAverage?.let { GradingScaleUtils.formatGrade(it, gradingScale) } ?: "--",
            label = "promedio",
            icon = Icons.Rounded.Grade,
            modifier = Modifier.weight(1f)
        )
        SubjectStatCard(
            value = evaluatedSubjects.toString(),
            label = if (evaluatedSubjects == 1) "evaluada" else "evaluadas",
            icon = Icons.Rounded.BarChart,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectStatCard(
    value: String,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(25.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                Text(
                    value,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Clip
                )
            }
        }
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
    val subjectColor = subjectAccent(subject.visualType)
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .bounceClick(onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        shape = RoundedCornerShape(10.dp),
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(80.dp)
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
                    .weight(1f, fill = true)
            ) {
                Text(
                    subject.name,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if (average == null) "Sin notas · 0% evaluado"
                    else "Promedio ${GradingScaleUtils.formatGrade(average, gradingScale)} · ${String.format(Locale.US, "%.0f", evaluatedPercentage)}% evaluado",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
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
        fontWeight = FontWeight.ExtraBold,
        fontSize = 13.sp,
        maxLines = 1,
        softWrap = false
    )
}

@Composable
private fun EmptyGradesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Text("Aún no tienes materias.", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
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
            fontWeight = FontWeight.ExtraBold
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
    ExtendedFloatingActionButton(
        onClick = onClick,
        shape = AppShapes.Pill,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = Color.White,
        icon = {
            Icon(Icons.Rounded.Add, contentDescription = null)
        },
        text = {
            Text("Agregar materia", fontWeight = FontWeight.ExtraBold)
        },
        modifier = modifier.height(56.dp)
    )
}

fun subjectAccent(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> UniStackColors.Teal
    SubjectVisualType.BLUE -> UniStackColors.Blue
    SubjectVisualType.CORAL -> UniStackColors.Coral
    SubjectVisualType.PURPLE -> UniStackColors.Primary
    SubjectVisualType.GREEN -> UniStackColors.Green
    SubjectVisualType.YELLOW -> UniStackColors.Yellow
    SubjectVisualType.ROSE -> Color(0xFFE84A8A)
    SubjectVisualType.INDIGO -> Color(0xFF4D5BD7)
    SubjectVisualType.ORANGE -> Color(0xFFF57C00)
    SubjectVisualType.CYAN -> Color(0xFF00A6D6)
    SubjectVisualType.LIME -> Color(0xFF7CB342)
    SubjectVisualType.SLATE -> Color(0xFF607D8B)
}

fun subjectBackground(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> UniStackColors.TealLight
    SubjectVisualType.BLUE -> UniStackColors.BlueLight
    SubjectVisualType.CORAL -> UniStackColors.CoralLight
    SubjectVisualType.PURPLE -> UniStackColors.PrimaryLight
    SubjectVisualType.GREEN -> UniStackColors.GreenLight
    SubjectVisualType.YELLOW -> UniStackColors.YellowLight
    SubjectVisualType.ROSE -> if (UniStackColors.IsDarkTheme) Color(0xFF3B1F2D) else Color(0xFFFFE4EF)
    SubjectVisualType.INDIGO -> if (UniStackColors.IsDarkTheme) Color(0xFF20274A) else Color(0xFFE5E8FF)
    SubjectVisualType.ORANGE -> if (UniStackColors.IsDarkTheme) Color(0xFF3D2817) else Color(0xFFFFE8D3)
    SubjectVisualType.CYAN -> if (UniStackColors.IsDarkTheme) Color(0xFF123444) else Color(0xFFDDF7FF)
    SubjectVisualType.LIME -> if (UniStackColors.IsDarkTheme) Color(0xFF243719) else Color(0xFFEAF7D7)
    SubjectVisualType.SLATE -> if (UniStackColors.IsDarkTheme) Color(0xFF25313A) else Color(0xFFE8EEF2)
}
