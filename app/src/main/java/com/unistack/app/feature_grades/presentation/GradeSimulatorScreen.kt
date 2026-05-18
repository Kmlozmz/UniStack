package com.unistack.app.feature_grades.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.SavedGradeScenario
import java.util.Locale

@Composable
fun GradeSimulatorScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    initialSubjectId: String? = null,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)

    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: GradingScaleUtils.maxGradeFor(scale)

    var selectedSubjectId by rememberSaveable(initialSubjectId) { mutableStateOf(initialSubjectId) }
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId } ?: subjects.firstOrNull()
    var targetAverageInput by rememberSaveable { mutableStateOf("") }
    var scenarioNameInput by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(subjects, selectedSubjectId) {
        if (selectedSubjectId == null || subjects.none { it.id == selectedSubjectId }) {
            selectedSubjectId = subjects.firstOrNull()?.id
        }
    }

    LaunchedEffect(selectedSubject?.id, selectedSubject?.targetAverage, scale) {
        targetAverageInput = selectedSubject?.targetAverage
            ?.let { GradingScaleUtils.formatGrade(it, scale) }
            .orEmpty()
    }

    val targetAverage = targetAverageInput.toDoubleOrNull()
    val targetIsValid = targetAverage != null && targetAverage in 0.0..maxGrade
    val weightedPoints = selectedSubject?.let { GradeCalculator.calculateWeightedPoints(it.grades) } ?: 0.0
    val remainingPercentage = selectedSubject
        ?.let { (1.0 - it.grades.sumOf { grade -> grade.percentage }).coerceAtLeast(0.0) }
        ?: 0.0
    val currentAverage = selectedSubject?.let { viewModel.currentAverage(it) }
    val evaluatedPercentage = selectedSubject?.let { viewModel.evaluatedPercentage(it) } ?: 0.0
    val neededGrade = if (targetAverage != null && targetAverage in 0.0..maxGrade) {
        GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = weightedPoints,
            remainingPercentage = remainingPercentage,
            targetAverage = targetAverage,
            maxGrade = maxGrade
        )
    } else {
        null
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Nota necesaria",
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Simula cuánto necesitas con tus materias reales.",
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (subjects.isEmpty()) {
            item {
                EmptySimulatorCard(onBackClick = onBackClick)
            }
        } else {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(subjects, key = { it.id }) { subject ->
                        SubjectSelectorChip(
                            subject = subject,
                            selected = subject.id == selectedSubject?.id,
                            gradingScale = scale,
                            average = viewModel.currentAverage(subject),
                            onClick = { selectedSubjectId = subject.id }
                        )
                    }
                }
            }

            selectedSubject?.let { subject ->
                item {
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        color = subjectBackground(subject.visualType),
                        shape = AppShapes.LargeCard,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = subjectAccent(subject.visualType)
                                )
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(
                                        text = subject.name,
                                        color = UniStackColors.TextPrimary,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "${String.format(Locale.US, "%.0f", evaluatedPercentage)}% evaluado",
                                        color = UniStackColors.TextSecondary,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = targetAverageInput,
                                onValueChange = { targetAverageInput = it.take(6) },
                                label = { Text("Promedio objetivo 0 a ${GradingScaleUtils.formatGrade(maxGrade, scale)}") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = AppShapes.MediumCard,
                                isError = targetAverageInput.isNotBlank() && !targetIsValid,
                                supportingText = {
                                    if (targetAverageInput.isNotBlank() && !targetIsValid) {
                                        Text("Ingresa una meta válida para tu escala actual.")
                                    }
                                }
                            )
                            SimulatorMetricsRow(
                                currentAverage = currentAverage,
                                remainingPercentage = remainingPercentage,
                                scale = scale
                            )
                        }
                    }
                }

                item {
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        color = UniStackColors.YellowLight,
                        shape = AppShapes.LargeCard,
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(
                                Icons.Rounded.TrackChanges,
                                contentDescription = null,
                                tint = UniStackColors.Yellow
                            )
                            Text(
                                text = simulatorResultMessage(
                                    targetAverage = targetAverage,
                                    targetIsValid = targetIsValid,
                                    neededGrade = neededGrade,
                                    currentAverage = currentAverage,
                                    remainingPercentage = remainingPercentage,
                                    maxGrade = maxGrade,
                                    scale = scale
                                ),
                                color = UniStackColors.TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            OutlinedTextField(
                                value = scenarioNameInput,
                                onValueChange = {
                                    scenarioNameInput = it.take(28)
                                    feedback = null
                                },
                                label = { Text("Nombre del escenario") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = AppShapes.MediumCard
                            )
                            Button(
                                onClick = {
                                    if (targetAverage != null && viewModel.saveScenario(subject, scenarioNameInput, targetAverage, neededGrade)) {
                                        scenarioNameInput = ""
                                        feedback = "Escenario guardado."
                                    } else {
                                        feedback = "Revisa el nombre y la meta."
                                    }
                                },
                                enabled = targetIsValid,
                                shape = AppShapes.Pill,
                                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Yellow),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Guardar escenario")
                            }
                            feedback?.let { message ->
                                Text(
                                    message,
                                    color = if (message.startsWith("Revisa")) UniStackColors.Coral else UniStackColors.Green,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
                val scenarios = profile?.gradeScenarios
                    ?.filter { it.subjectId == subject.id }
                    .orEmpty()
                if (scenarios.isNotEmpty()) {
                    item {
                        SavedScenariosCard(
                            scenarios = scenarios,
                            scale = scale,
                            onDeleteClick = { scenarioId -> viewModel.deleteScenario(scenarioId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SavedScenariosCard(
    scenarios: List<SavedGradeScenario>,
    scale: GradingScale,
    onDeleteClick: (String) -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Escenarios guardados", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            scenarios.forEach { scenario ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(scenario.name, color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text(
                            "Meta ${GradingScaleUtils.formatGrade(scenario.targetAverage, scale)} · Necesaria ${GradingScaleUtils.formatGrade(scenario.neededGrade, scale)}",
                            color = UniStackColors.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(onClick = { onDeleteClick(scenario.id) }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Eliminar escenario", tint = UniStackColors.Coral)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectSelectorChip(
    subject: Subject,
    selected: Boolean,
    gradingScale: GradingScale,
    average: Double?,
    onClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.bounceClick(onClick),
        color = if (selected) subjectBackground(subject.visualType) else UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = if (selected) 8.dp else 3.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = subject.name,
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Promedio ${GradingScaleUtils.formatGrade(average, gradingScale)}",
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SimulatorMetricsRow(
    currentAverage: Double?,
    remainingPercentage: Double,
    scale: GradingScale
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricPill(
            label = "Actual",
            value = GradingScaleUtils.formatGrade(currentAverage, scale),
            modifier = Modifier.weight(1f)
        )
        MetricPill(
            label = "Restante",
            value = "${String.format(Locale.US, "%.0f", remainingPercentage * 100)}%",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricPill(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier,
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, color = UniStackColors.TextSecondary, fontSize = 12.sp)
            Text(value, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun EmptySimulatorCard(onBackClick: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.PrimaryLight,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.Calculate, contentDescription = null, tint = UniStackColors.Primary)
            Text("Aún no hay materias para simular.", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text("Crea una materia y agrega tus porcentajes para calcular escenarios reales.", color = UniStackColors.TextSecondary)
            Button(
                onClick = onBackClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
            ) {
                Text("Volver a materias")
            }
        }
    }
}

private fun simulatorResultMessage(
    targetAverage: Double?,
    targetIsValid: Boolean,
    neededGrade: Double?,
    currentAverage: Double?,
    remainingPercentage: Double,
    maxGrade: Double,
    scale: GradingScale
): String {
    if (targetAverage == null || !targetIsValid) {
        return "Ingresa una meta válida para calcular la nota necesaria."
    }

    if (neededGrade != null && neededGrade <= 0.0) {
        return "Ya tienes puntos suficientes para alcanzar ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
    }

    if (remainingPercentage <= 0.0) {
        return if (currentAverage != null && currentAverage >= targetAverage) {
            "La materia ya está completa y alcanzaste la meta."
        } else {
            "La materia ya está completa. No queda porcentaje para subir el promedio."
        }
    }

    if (neededGrade == null) {
        return "No se puede calcular una nota necesaria con el porcentaje actual."
    }

    if (neededGrade > maxGrade) {
        return "Con el ${String.format(Locale.US, "%.0f", remainingPercentage * 100)}% restante no es posible alcanzar ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
    }

    return "Necesitas ${GradingScaleUtils.formatGrade(neededGrade, scale)} en el ${String.format(Locale.US, "%.0f", remainingPercentage * 100)}% restante para terminar con ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
}
