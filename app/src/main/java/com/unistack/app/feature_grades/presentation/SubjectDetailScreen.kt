package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import java.util.Locale
import kotlin.math.round

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val academicWorks by viewModel.academicWorks.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var targetAverageInput by rememberSaveable { mutableStateOf("") }
    var whatIfGradeInput by rememberSaveable { mutableStateOf("") }
    var whatIfPercentageInput by rememberSaveable { mutableStateOf("") }

    if (subject == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text("Materia no encontrada", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    val average = viewModel.currentAverage(subject)
    val evaluated = viewModel.evaluatedPercentage(subject)
    val evaluatedPercentage = subject.grades.sumOf { it.percentage }.coerceIn(0.0, 1.0)
    val remainingPercentage = (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0)
    val subjectWorks = academicWorks.filter { it.subjectId == subject.id }
    val weightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades)
    val targetAverage = parseDecimalInput(targetAverageInput)
    val targetIsValid = targetAverage != null && targetAverage in 0.0..maxGrade
    val needed = if (targetIsValid && remainingPercentage > 0.0) {
        GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = weightedPoints,
            remainingPercentage = remainingPercentage,
            targetAverage = targetAverage ?: subject.targetAverage,
            maxGrade = maxGrade
        )
    } else {
        null
    }
    val quickTargets = quickTargetOptions(
        passingGrade = profile?.passingGrade,
        subjectTarget = subject.targetAverage,
        maxGrade = maxGrade
    )

    LaunchedEffect(subject.id, subject.targetAverage, scale) {
        targetAverageInput = gradeInputText(subject.targetAverage, scale)
        whatIfGradeInput = gradeInputText(maxGrade, scale)
        whatIfPercentageInput = wholePercentInput((remainingPercentage * 100).coerceAtMost(20.0).coerceAtLeast(0.0))
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    subject.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp)
                )
                Box {
                    IconButton(onClick = { showSubjectMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Opciones de materia")
                    }
                    DropdownMenu(
                        expanded = showSubjectMenu,
                        onDismissRequest = { showSubjectMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar materia") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                            onClick = {
                                showSubjectMenu = false
                                onEditSubjectClick(subject.id)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar materia", color = UniStackColors.Coral) },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.Delete,
                                    contentDescription = null,
                                    tint = UniStackColors.Coral
                                )
                            },
                            onClick = {
                                showSubjectMenu = false
                                showDeleteSubjectDialog = true
                            }
                        )
                    }
                }
            }
        }
        item {
            SubjectSummaryCard(
                subject = subject,
                average = average,
                evaluated = evaluated,
                remainingPercentage = remainingPercentage,
                scale = scale
            )
        }
        item {
            if (remainingPercentage <= 0.0) {
                CompletedSubjectInsightCard(
                    average = average,
                    targetAverage = subject.targetAverage,
                    scale = scale
                )
            } else {
                NeededGradePlannerCard(
                    targetAverageInput = targetAverageInput,
                    onTargetAverageChange = { targetAverageInput = it.take(6) },
                    quickTargets = quickTargets,
                    onQuickTargetClick = { targetAverageInput = gradeInputText(it, scale) },
                    targetAverage = targetAverage,
                    targetIsValid = targetIsValid,
                    neededGrade = needed,
                    remainingPercentage = remainingPercentage,
                    maxGrade = maxGrade,
                    scale = scale
                )
            }
        }
        if (remainingPercentage > 0.0) {
            item {
                WhatIfPlannerCard(
                    gradeInput = whatIfGradeInput,
                    onGradeChange = { whatIfGradeInput = it.take(6) },
                    percentageInput = whatIfPercentageInput,
                    onPercentageChange = { whatIfPercentageInput = it.take(5) },
                    weightedPoints = weightedPoints,
                    evaluatedPercentage = evaluatedPercentage,
                    remainingPercentage = remainingPercentage,
                    maxGrade = maxGrade,
                    scale = scale
                )
            }
        }
        if (subjectWorks.isNotEmpty()) {
            item {
                Text(
                    "Trabajos asociados",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            items(subjectWorks, key = { it.id }) { work ->
                SubjectWorkCard(work = work)
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Notas",
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    gradeCountLabel(subject.grades.size),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
        if (subject.grades.isEmpty()) {
            item {
                EmptyNotesCard()
            }
        } else {
            items(subject.grades, key = { it.id }) { grade ->
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                    shape = AppShapes.MediumCard,
                    tonalElevation = 0.dp,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
                    borderWidth = 0.5.dp
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(grade.name, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                            Text(
                                "${String.format(Locale.US, "%.0f", grade.percentage * 100)}% del curso",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(GradingScaleUtils.formatGrade(grade.value, scale), color = subjectAccent(subject.visualType), fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        IconButton(onClick = { onEditGradeClick(subject.id, grade.id) }) {
                            Icon(Icons.Rounded.Edit, contentDescription = "Editar nota")
                        }
                        IconButton(onClick = { gradeIdPendingDelete = grade.id }) {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = "Eliminar nota",
                                tint = UniStackColors.Coral
                            )
                        }
                    }
                }
            }
        }
        item {
            Button(
                onClick = { onAddGradeClick(subject.id) },
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(vertical = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(10.dp))
                Text("Agregar nota", fontWeight = FontWeight.ExtraBold)
            }
        }
    }

    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text("¿Eliminar materia?") },
            text = { Text("También se eliminarán sus notas.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSubjectDialog = false
                        if (viewModel.deleteSubject(subject.id)) {
                            onSubjectDeleted()
                        }
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    gradeIdPendingDelete?.let { gradeId ->
        AlertDialog(
            onDismissRequest = { gradeIdPendingDelete = null },
            title = { Text("¿Eliminar nota?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrade(subject.id, gradeId)
                        gradeIdPendingDelete = null
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gradeIdPendingDelete = null }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun SubjectSummaryCard(
    subject: com.unistack.app.feature_grades.domain.Subject,
    average: Double?,
    evaluated: Double,
    remainingPercentage: Double,
    scale: GradingScale
) {
    val accent = subjectAccent(subject.visualType)
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.LargeCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(136.dp)
                    .background(accent)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(accent.copy(alpha = 0.14f), AppShapes.SmallCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.School, contentDescription = null, tint = accent)
                    }
                    Column(
                        modifier = Modifier
                            .padding(start = 14.dp)
                            .weight(1f)
                    ) {
                        Text("Promedio actual", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            GradingScaleUtils.formatGrade(average, scale),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Meta", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        Text(
                            GradingScaleUtils.formatGrade(subject.targetAverage, scale),
                            color = accent,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SubjectDetailMetric(
                        label = "evaluado",
                        value = "${String.format(Locale.US, "%.0f", evaluated)}%",
                        modifier = Modifier.weight(1f)
                    )
                    SubjectDetailMetric(
                        label = "restante",
                        value = "${String.format(Locale.US, "%.0f", remainingPercentage * 100)}%",
                        modifier = Modifier.weight(1f)
                    )
                    SubjectDetailMetric(
                        label = "notas",
                        value = subject.grades.size.toString(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyNotesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(
                modifier = Modifier.padding(start = 12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text("Aún no has registrado notas.", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Text(
                    "Agrega la primera para empezar a calcular tu promedio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun CompletedSubjectInsightCard(
    average: Double?,
    targetAverage: Double,
    scale: GradingScale
) {
    val targetReached = average != null && average >= targetAverage
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = if (targetReached) UniStackColors.Green else UniStackColors.Coral
                )
                Text(
                    "Materia finalizada",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = if (targetReached) {
                    "Terminaste con ${GradingScaleUtils.formatGrade(average, scale)} y alcanzaste la meta de ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
                } else {
                    "Terminaste con ${GradingScaleUtils.formatGrade(average, scale)}. La meta era ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
                },
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun NeededGradePlannerCard(
    targetAverageInput: String,
    onTargetAverageChange: (String) -> Unit,
    quickTargets: List<Double>,
    onQuickTargetClick: (Double) -> Unit,
    targetAverage: Double?,
    targetIsValid: Boolean,
    neededGrade: Double?,
    remainingPercentage: Double,
    maxGrade: Double,
    scale: GradingScale
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = UniStackColors.Primary)
                Text(
                    "Tu meta",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = goalSummaryMessage(
                    targetAverage = targetAverage,
                    targetIsValid = targetIsValid,
                    neededGrade = neededGrade,
                    remainingPercentage = remainingPercentage,
                    maxGrade = maxGrade,
                    scale = scale
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Meta objetivo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedTextField(
                value = targetAverageInput,
                onValueChange = onTargetAverageChange,
                label = { Text("Meta") },
                singleLine = true,
                shape = AppShapes.MediumCard,
                isError = targetAverageInput.isNotBlank() && !targetIsValid,
                supportingText = {
                    Text("Usa una meta entre 0 y ${GradingScaleUtils.formatGrade(maxGrade, scale)}.")
                },
                modifier = Modifier.fillMaxWidth()
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickTargets, key = { it }) { target ->
                    val selected = targetAverage?.let { roundToOneDecimal(it) == roundToOneDecimal(target) } == true
                    Button(
                        onClick = { onQuickTargetClick(target) },
                        shape = AppShapes.Pill,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                            contentColor = if (selected) Color.White else MaterialTheme.colorScheme.primary
                        ),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(GradingScaleUtils.formatGrade(target, scale), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun WhatIfPlannerCard(
    gradeInput: String,
    onGradeChange: (String) -> Unit,
    percentageInput: String,
    onPercentageChange: (String) -> Unit,
    weightedPoints: Double,
    evaluatedPercentage: Double,
    remainingPercentage: Double,
    maxGrade: Double,
    scale: GradingScale
) {
    val grade = parseDecimalInput(gradeInput)
    val percentage = parseDecimalInput(percentageInput)
    val percentageWeight = ((percentage ?: 0.0) / 100.0).coerceIn(0.0, remainingPercentage)
    val evaluatedAfter = (evaluatedPercentage + percentageWeight).coerceAtMost(1.0)
    val projectedAverage = if (
        grade != null &&
        percentage != null &&
        grade in 0.0..maxGrade &&
        percentageWeight > 0.0 &&
        evaluatedAfter > 0.0
    ) {
        roundToOneDecimal((weightedPoints + grade * percentageWeight) / evaluatedAfter)
    } else {
        null
    }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = UniStackColors.Primary)
                Text(
                    "Simulador rápido",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            Text(
                text = whatIfHeadline(
                    grade = grade,
                    percentage = percentage,
                    projectedAverage = projectedAverage,
                    percentageWeight = percentageWeight,
                    scale = scale
                ),
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = gradeInput,
                    onValueChange = onGradeChange,
                    label = { Text("Nota") },
                    singleLine = true,
                    shape = AppShapes.MediumCard,
                    isError = gradeInput.isNotBlank() && (grade == null || grade !in 0.0..maxGrade),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = percentageInput,
                    onValueChange = onPercentageChange,
                    label = { Text("Peso %") },
                    singleLine = true,
                    shape = AppShapes.MediumCard,
                    isError = percentageInput.isNotBlank() && (percentage == null || percentage <= 0.0 || percentageWeight <= 0.0),
                    modifier = Modifier.weight(1f)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.72f), AppShapes.SmallCard)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                            text = "Promedio proyectado",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = projectedAverage?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "--",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                Text(
                    text = whatIfDetail(
                        projectedAverage = projectedAverage,
                        evaluatedAfter = evaluatedAfter,
                        remainingPercentage = remainingPercentage,
                        percentageWeight = percentageWeight,
                        scale = scale
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            Text(
                text = "Puedes probar hasta ${String.format(Locale.US, "%.0f", remainingPercentage * 100)}% restante.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun SubjectWorkCard(work: AcademicWork) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Rounded.Assignment, contentDescription = null, tint = UniStackColors.Blue)
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(work.title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
                Text(
                    listOfNotNull(
                        work.status.label(),
                        work.dueDateMillis?.let(TaskDateUtils::dueText),
                        "${(work.checklistProgress * 100).toInt()}% listo"
                    ).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
            }
        }
    }
}

private fun AcademicWorkStatus.label(): String {
    return when (this) {
        AcademicWorkStatus.IDEA -> "Idea"
        AcademicWorkStatus.DRAFT -> "Borrador"
        AcademicWorkStatus.REVIEW -> "Revisión"
        AcademicWorkStatus.READY -> "Listo"
        AcademicWorkStatus.SUBMITTED -> "Entregado"
    }
}

private fun gradeCountLabel(count: Int): String =
    if (count == 1) "1 registrada" else "$count registradas"

@Composable
private fun SubjectDetailMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
    }
}

private fun goalSummaryMessage(
    targetAverage: Double?,
    targetIsValid: Boolean,
    neededGrade: Double?,
    remainingPercentage: Double,
    maxGrade: Double,
    scale: GradingScale
): String {
    if (targetAverage == null || !targetIsValid) {
        return "Elige una meta válida."
    }
    if (remainingPercentage <= 0.0) {
        return "La materia ya está completa."
    }
    if (neededGrade != null && neededGrade <= 0.0) {
        return "Ya tienes puntos suficientes para alcanzar ${GradingScaleUtils.formatGrade(targetAverage, scale)}."
    }
    if (neededGrade == null) {
        return "No se puede calcular con el porcentaje restante."
    }
    if (neededGrade > maxGrade) {
        return "No es posible alcanzar ${GradingScaleUtils.formatGrade(targetAverage, scale)} con el porcentaje restante."
    }
    return "Necesitas mantener ${GradingScaleUtils.formatGrade(neededGrade, scale)} en el porcentaje restante."
}

private fun whatIfHeadline(
    grade: Double?,
    percentage: Double?,
    projectedAverage: Double?,
    percentageWeight: Double,
    scale: GradingScale
): String {
    if (grade == null || percentage == null || projectedAverage == null || percentageWeight <= 0.0) {
        return "Prueba una nota futura y mira cómo movería tu promedio."
    }

    val gradeText = GradingScaleUtils.formatGrade(grade, scale)
    val projected = GradingScaleUtils.formatGrade(projectedAverage, scale)
    val percentText = String.format(Locale.US, "%.0f", percentageWeight * 100)
    return "Si sacas $gradeText en una nota de $percentText%, tu promedio quedaría en $projected."
}

private fun whatIfDetail(
    projectedAverage: Double?,
    evaluatedAfter: Double,
    remainingPercentage: Double,
    percentageWeight: Double,
    scale: GradingScale
): String {
    if (projectedAverage == null) {
        return "Escribe una nota y el peso de la próxima evaluación."
    }

    val evaluatedText = String.format(Locale.US, "%.0f", evaluatedAfter * 100)
    return if (percentageWeight >= remainingPercentage) {
        "Ese sería tu promedio final si cubre todo lo que falta."
    } else {
        "Después de esa evaluación tendrías $evaluatedText% del curso evaluado."
    }
}

private fun parseDecimalInput(value: String): Double? {
    return value.trim().replace(',', '.').toDoubleOrNull()
}

private fun gradeInputText(value: Double, scale: GradingScale): String {
    return when (scale) {
        GradingScale.CUSTOM -> String.format(Locale.US, "%.0f", value)
        GradingScale.ZERO_TO_FIVE -> String.format(Locale.US, "%.1f", value)
    }
}

private fun wholePercentInput(value: Double): String =
    String.format(Locale.US, "%.0f", value)

private fun quickTargetOptions(
    passingGrade: Double?,
    subjectTarget: Double,
    maxGrade: Double
): List<Double> {
    return listOfNotNull(passingGrade, subjectTarget, maxGrade)
        .map { it.coerceIn(0.0, maxGrade) }
        .distinctBy { roundToOneDecimal(it) }
}

private fun roundToOneDecimal(value: Double): Double =
    round(value * 10.0) / 10.0
