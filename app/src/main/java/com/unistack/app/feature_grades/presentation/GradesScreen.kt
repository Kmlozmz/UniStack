package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.UniStackTheme
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale
import androidx.compose.ui.tooling.preview.Preview
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.core.utils.bounceClick

@Composable
fun GradesScreen(
    onAddSubjectClick: () -> Unit,
    onOpenSimulatorClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            FeatureHeader(
                title = "Materias",
                subtitle = "Administra tus materias, notas y porcentajes."
            )
        }
        if (subjects.isEmpty()) {
            item {
                EmptyGradesCard(onAddSubjectClick = onAddSubjectClick)
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
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onAddSubjectClick,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null)
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text("Agregar materia")
                }
                Button(
                    onClick = onOpenSimulatorClick,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Rounded.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.padding(3.dp))
                    Text("Simulador")
                }
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
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick),
        brush = Brush.linearGradient(listOf(subjectBackground(subject.visualType), Color.White)),
        shape = AppShapes.MediumCard
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Rounded.School,
                contentDescription = null,
                tint = subjectAccent(subject.visualType)
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f)
            ) {
                Text(subject.name, fontWeight = FontWeight.ExtraBold, color = UniStackColors.TextPrimary)
                Text(
                    if (average == null) "Sin notas · 0% evaluado"
                    else "Promedio ${GradingScaleUtils.formatGrade(average, gradingScale)} · ${String.format("%.0f", evaluatedPercentage)}% evaluado",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Text(
                GradingScaleUtils.formatGrade(average, gradingScale),
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
private fun EmptyGradesCard(onAddSubjectClick: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.PrimaryLight,
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Aún no tienes materias.", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text("Crea tu primera materia para empezar a calcular tu promedio.", color = UniStackColors.TextSecondary)
            Button(
                onClick = onAddSubjectClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
            ) {
                Text("Agregar materia")
            }
        }
    }
}

@Composable
private fun FeatureHeader(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = subtitle,
            color = UniStackColors.TextSecondary,
            style = MaterialTheme.typography.bodyMedium
        )
    }
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
    SubjectVisualType.ROSE -> Color(0xFFFFE4EF)
    SubjectVisualType.INDIGO -> Color(0xFFE5E8FF)
    SubjectVisualType.ORANGE -> Color(0xFFFFE8D3)
    SubjectVisualType.CYAN -> Color(0xFFDDF7FF)
    SubjectVisualType.LIME -> Color(0xFFEAF7D7)
    SubjectVisualType.SLATE -> Color(0xFFE8EEF2)
}

@Preview(showBackground = true)
@Composable
fun SubjectListCardPreview() {
    UniStackTheme {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            SubjectListCard(
                subject = com.unistack.app.feature_grades.domain.Subject(
                    id = "1",
                    name = "Cálculo I",
                    targetAverage = 3.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE
                ),
                average = 4.2,
                evaluatedPercentage = 40.0,
                gradingScale = GradingScale.ZERO_TO_FIVE,
                onClick = {}
            )
            SubjectListCard(
                subject = com.unistack.app.feature_grades.domain.Subject(
                    id = "2",
                    name = "Física II",
                    targetAverage = 3.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.CORAL
                ),
                average = null,
                evaluatedPercentage = 0.0,
                gradingScale = GradingScale.ZERO_TO_FIVE,
                onClick = {}
            )
        }
    }
}
