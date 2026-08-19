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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
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
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.CategoricalSubjectAccents
import com.unistack.app.core.design.theme.CategoricalSubjectBackgrounds
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.anchoredButtonRoom
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.SubjectGradeCalculation
import com.unistack.app.core.utils.TargetOutlook
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.core.utils.bounceClick
import java.util.Locale

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
@Composable
fun GradesScreen(
    onAddSubjectClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    embedded: Boolean = false
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
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
                // El margen del final sale de la regla, no de un número a ojo: lo que tape la
                // barra flotante más el hueco del botón anclado. Con los 118dp fijos de antes
                // la lista se quedaba a unos pocos dp de poder desplazarse, así que no había
                // scroll y el botón «Agregar materia» tapaba para siempre la última tarjeta.
                bottom = scrollBottomRoom + anchoredButtonRoom
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
                        calculation = viewModel.calculationFor(subject),
                        classSession = classSessions.firstOrNull { it.subjectId == subject.id },
                        gradingScale = scale,
                        maxGrade = maxGrade,
                        onClick = { onSubjectClick(subject.id) }
                    )
                }
            }
        }

        AddSubjectButton(
            onClick = onAddSubjectClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                // Anclado, no desplazable: sin esto la barra flotante lo tapa siempre.
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
            value = GradingScaleUtils.formatGrade(generalAverage, gradingScale),
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
    calculation: SubjectGradeCalculation,
    classSession: ClassSession?,
    gradingScale: GradingScale,
    maxGrade: Double,
    onClick: () -> Unit
) {
    val tone = subjectTone(calculation, subject.targetAverage, maxGrade)
    val subjectColor = subjectAccent(subject)
    val evaluated = calculation.evaluatedSemesterFraction * 100.0
    val targetText = GradingScaleUtils.formatGrade(subject.targetAverage, gradingScale)
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.30f),
        shape = AppShapes.SmallCard,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        // Dos columnas: a la izquierda quién y cuándo, a la derecha cómo va. Con los datos
        // sueltos en tres filas la tarjeta crecía y no se leía en diagonal; así ocupa lo
        // mismo que cuando solo llevaba el nombre y la cifra.
        Row(
            modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
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
                    .padding(start = 12.dp)
                    .size(38.dp)
                    .background(
                        subjectColor.copy(alpha = if (LocalIsDarkTheme.current) 0.18f else 0.12f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.School,
                    contentDescription = null,
                    tint = subjectColor,
                    modifier = Modifier.size(19.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 12.dp, end = 8.dp, top = 11.dp, bottom = 11.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        subject.name,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        // Sin notas no hay nota que enseñar, así que el hueco lo ocupa la meta.
                        calculation.currentAverage
                            ?.let { GradingScaleUtils.formatGrade(it, gradingScale) }
                            ?: "Meta $targetText",
                        color = tone.color,
                        fontSize = if (calculation.currentAverage == null) 14.sp else 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profesor y horario, que se piden al crear la materia y hasta ahora solo
                    // se veían en Horario. Comparte fila con el estado en vez de abrir otra.
                    val classLine = classSession?.let { session ->
                        listOfNotNull(
                            session.place.professor.takeIf { it.isNotBlank() },
                            session.daysAndTimeLabel().takeIf { it.isNotBlank() }
                        ).joinToString("  ·  ")
                    }?.takeIf { it.isNotBlank() }
                    Text(
                        classLine ?: "${String.format(Locale.US, "%.0f", evaluated)}% evaluado",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        tone.label,
                        color = tone.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
                EvaluationBar(
                    fraction = calculation.evaluatedSemesterFraction,
                    height = 4.dp,
                    // El color de la materia, el mismo de la franja y del avatar. En gris
                    // parecía una barra apagada que no avanzaba.
                    color = subjectColor
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(20.dp)
            )
        }
    }
}

private data class SubjectTone(
    val color: Color,
    val label: String
)

/**
 * Color y rótulo de una materia, a partir de dónde puede acabar y no solo de dónde está.
 *
 * El margen para el aviso sale de la escala. Estaba escrito como 0.5 fijo, que en la escala
 * de 0 a 100 es medio punto: el estado ámbar solo existía entre 79.5 y 80.
 */
@Composable
private fun subjectTone(
    calculation: SubjectGradeCalculation,
    target: Double,
    maxGrade: Double
): SubjectTone {
    return when (calculation.outlook) {
        TargetOutlook.NO_DATA -> SubjectTone(
            MaterialTheme.colorScheme.primary.copy(alpha = if (LocalIsDarkTheme.current) 0.72f else 0.62f),
            "Sin notas"
        )
        TargetOutlook.SECURED -> SubjectTone(
            LocalSectionColors.current.onTrack,
            if (calculation.isFinished) "Meta cumplida" else "Meta asegurada"
        )
        TargetOutlook.ON_TRACK -> SubjectTone(MaterialTheme.colorScheme.tertiary, "Sobre meta")
        TargetOutlook.AT_RISK -> {
            val current = calculation.currentAverage
            val closeToTarget = current != null &&
                target - current <= GradeCalculator.closeToTargetMargin(maxGrade)
            SubjectTone(
                if (closeToTarget) LocalSectionColors.current.atRisk else MaterialTheme.colorScheme.error,
                "Por subir"
            )
        }
        TargetOutlook.UNREACHABLE -> SubjectTone(
            MaterialTheme.colorScheme.error,
            if (calculation.isFinished) "Bajo la meta" else "Fuera de alcance"
        )
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
                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = null,
                    tint = contentColorOn(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.size(18.dp)
                )
            }
            Text(
                text = "Agregar materia",
                color = contentColorOn(MaterialTheme.colorScheme.primary),
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

@Composable
@ReadOnlyComposable
fun subjectAccent(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> MaterialTheme.colorScheme.tertiary
    SubjectVisualType.BLUE -> LocalSectionColors.current.schedule
    SubjectVisualType.CORAL -> MaterialTheme.colorScheme.error
    SubjectVisualType.PURPLE -> MaterialTheme.colorScheme.primary
    SubjectVisualType.GREEN -> LocalSectionColors.current.onTrack
    SubjectVisualType.YELLOW -> LocalSectionColors.current.atRisk
    SubjectVisualType.ROSE -> CategoricalSubjectAccents.Rose
    SubjectVisualType.INDIGO -> CategoricalSubjectAccents.Indigo
    SubjectVisualType.ORANGE -> CategoricalSubjectAccents.Orange
    SubjectVisualType.CYAN -> CategoricalSubjectAccents.Cyan
    SubjectVisualType.LIME -> CategoricalSubjectAccents.Lime
    SubjectVisualType.SLATE -> CategoricalSubjectAccents.Slate
}

@Composable
@ReadOnlyComposable
fun subjectAccent(subject: Subject): Color {
    return subject.customColor?.let { Color(it) } ?: subjectAccent(subject.visualType)
}

@Composable
@ReadOnlyComposable
fun subjectBackground(type: SubjectVisualType): Color = when (type) {
    SubjectVisualType.TEAL -> MaterialTheme.colorScheme.tertiaryContainer
    SubjectVisualType.BLUE -> LocalSectionColors.current.scheduleContainer
    SubjectVisualType.CORAL -> MaterialTheme.colorScheme.errorContainer
    SubjectVisualType.PURPLE -> MaterialTheme.colorScheme.primaryContainer
    SubjectVisualType.GREEN -> LocalSectionColors.current.onTrackContainer
    SubjectVisualType.YELLOW -> LocalSectionColors.current.atRiskContainer
    SubjectVisualType.ROSE -> CategoricalSubjectBackgrounds.rose(LocalIsDarkTheme.current)
    SubjectVisualType.INDIGO -> CategoricalSubjectBackgrounds.indigo(LocalIsDarkTheme.current)
    SubjectVisualType.ORANGE -> CategoricalSubjectBackgrounds.orange(LocalIsDarkTheme.current)
    SubjectVisualType.CYAN -> CategoricalSubjectBackgrounds.cyan(LocalIsDarkTheme.current)
    SubjectVisualType.LIME -> CategoricalSubjectBackgrounds.lime(LocalIsDarkTheme.current)
    SubjectVisualType.SLATE -> CategoricalSubjectBackgrounds.slate(LocalIsDarkTheme.current)
}

@Composable
private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}
