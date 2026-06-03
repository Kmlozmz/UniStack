package com.unistack.app.feature_grades.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.GradingScale
import java.util.Locale
import kotlin.math.round

private val PurpleGradient: Brush
    @Composable get() = Brush.horizontalGradient(listOf(UniStackColors.Primary, UniStackColors.PrimaryDark))
private val LargeCardShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val periodScheme = profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }
    var showAddGradeSheet by remember { mutableStateOf(false) }

    if (subject == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val periodSummaries = remember(subject, periodScheme) {
        periodScheme.periods.map { period ->
            val grades = subject.grades.filter { it.periodId == period.id }
            period.toSummary(grades)
        }
    }

    val evaluatedSubjectPercentage = remember(subject.grades, periodScheme) {
        GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, periodScheme.periods)
    }

    val currentWeightedAverage = remember(subject.grades, periodScheme) {
        GradeCalculator.calculateProjectedAverageByPeriods(subject.grades, periodScheme.periods)
    }

    val activeCuts = remember(periodSummaries) {
        periodSummaries.filter { it.average != null }
    }

    val sumWeightsOfActiveCuts = remember(activeCuts) {
        activeCuts.sumOf { it.period.weight }
    }

    val projectedFinalGrade = remember(activeCuts, sumWeightsOfActiveCuts) {
        if (sumWeightsOfActiveCuts <= 0.0) null else {
            val sumWeightedCuts = activeCuts.sumOf { (it.average ?: 0.0) * it.period.weight }
            val proj = sumWeightedCuts / sumWeightsOfActiveCuts
            round(proj * 10.0) / 10.0
        }
    }

    val remainingSubjectPercentage = (100.0 - evaluatedSubjectPercentage).coerceAtLeast(0.0)

    val neededForTarget = remember(subject.grades, periodScheme, remainingSubjectPercentage, subject.targetAverage, maxGrade) {
        if (remainingSubjectPercentage <= 0.0) null else {
            GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = GradeCalculator.calculateWeightedPointsByPeriods(subject.grades, periodScheme.periods),
                remainingPercentage = remainingSubjectPercentage / 100.0,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(start = 22.dp, top = 8.dp, end = 22.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SubjectHeader(
                    title = subject.name,
                    subtitle = "Materia activa",
                    onBackClick = onBackClick,
                    showMenu = showSubjectMenu,
                    onMenuClick = { showSubjectMenu = true },
                    onDismissMenu = { showSubjectMenu = false },
                    onEditClick = {
                        showSubjectMenu = false
                        onEditSubjectClick(subject.id)
                    },
                    onDeleteClick = {
                        showSubjectMenu = false
                        showDeleteSubjectDialog = true
                    }
                )
            }
            item {
                SubjectOverviewCard(
                    subject = subject,
                    average = currentWeightedAverage,
                    projectedFinal = projectedFinalGrade,
                    evaluated = evaluatedSubjectPercentage,
                    maxGrade = maxGrade,
                    scale = scale
                )
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SubjectMetric(
                        label = "Meta de aprobación",
                        value = GradingScaleUtils.formatGrade(passingGrade, scale),
                        valueColor = UniStackColors.Green,
                        modifier = Modifier.weight(1f)
                    )
                    SubjectMetric(
                        label = "Meta objetivo",
                        value = GradingScaleUtils.formatGrade(subject.targetAverage, scale),
                        valueColor = UniStackColors.Green,
                        modifier = Modifier.weight(1f)
                    )
                    SubjectMetric(
                        label = "Falta evaluar",
                        value = "${formatPercent(remainingSubjectPercentage)}%",
                        valueColor = UniStackColors.TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            item {
                SubjectInsightCard(
                    neededForTarget = neededForTarget,
                    targetGrade = subject.targetAverage,
                    maxGrade = maxGrade,
                    scale = scale
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Cortes del semestre",
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Tu materia está dividida en ${periodScheme.periods.size} cortes",
                            color = UniStackColors.TextSecondary,
                            fontSize = 14.sp
                        )
                        Icon(
                            Icons.Rounded.Lightbulb,
                            contentDescription = null,
                            tint = UniStackColors.TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    periodSummaries.forEach { summary ->
                        PeriodCard(
                            summary = summary,
                            maxGrade = maxGrade,
                            scale = scale,
                            onClick = { onPeriodClick(subject.id, summary.period.id) }
                        )
                    }
                }
            }
        }

        // Sticky bottom button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            UniStackColors.Background.copy(alpha = 0f),
                            UniStackColors.Background.copy(alpha = 0.9f),
                            UniStackColors.Background
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            Button(
                onClick = { showAddGradeSheet = true },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(PurpleGradient, RoundedCornerShape(24.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva nota", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }

    if (showAddGradeSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddGradeSheet = false },
            containerColor = UniStackColors.Card,
            dragHandle = null,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            PeriodPickerSheet(
                summaries = periodSummaries,
                onPeriodSelected = { periodId ->
                    showAddGradeSheet = false
                    onAddGradeClick(subject.id, periodId)
                },
                onCancel = { showAddGradeSheet = false },
                scale = scale
            )
        }
    }

    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text("¿Eliminar materia?", color = UniStackColors.TextPrimary) },
            text = { Text("También se eliminarán sus cortes y notas.", color = UniStackColors.TextSecondary) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSubjectDialog = false
                        if (viewModel.deleteSubject(subject.id)) onSubjectDeleted()
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text("Cancelar", color = UniStackColors.TextSecondary)
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
fun SubjectPeriodDetailScreen(
    subjectId: String,
    periodId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val periodScheme = profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val period = periodScheme.periods.firstOrNull { it.id == periodId }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }

    if (subject == null || period == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val grades = subject.grades.filter { it.periodId == period.id }
    val summary = period.toSummary(grades)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 22.dp, top = 8.dp, end = 22.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PeriodHeader(
                title = periodDisplayName(period),
                subtitle = "${formatPercent(period.weight * 100)}% de la materia",
                onBackClick = onBackClick
            )
        }
        item {
            PeriodOverviewCard(summary = summary, maxGrade = maxGrade, scale = scale)
        }
        
        item {
            Text(
                "Notas del corte",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        if (grades.isEmpty()) {
            item { EmptyPeriodNotesCard() }
        } else {
            item {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = UniStackColors.Card,
                    shape = LargeCardShape,
                    tonalElevation = 0.dp,
                    borderColor = UniStackColors.SoftOutline,
                    borderWidth = 1.dp,
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Column {
                        grades.forEachIndexed { index, grade ->
                            GradeRowItem(
                                grade = grade,
                                scale = scale,
                                onEditClick = { onEditGradeClick(subject.id, grade.id) },
                                onDeleteClick = { gradeIdPendingDelete = grade.id }
                            )
                            if (index < grades.lastIndex) {
                                HorizontalDivider(
                                    color = UniStackColors.SoftOutline,
                                    thickness = 1.dp,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        item {
            Button(
                onClick = { onAddGradeClick(subject.id, period.id) },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.Transparent,
                    contentColor = UniStackColors.Primary
                ),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, UniStackColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = UniStackColors.Primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar nota a este corte", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = UniStackColors.Primary)
            }
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Primary.copy(alpha = 0.08f),
                shape = LargeCardShape,
                tonalElevation = 0.dp,
                borderColor = UniStackColors.Primary.copy(alpha = 0.15f),
                borderWidth = 1.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Rounded.Lightbulb,
                        contentDescription = null,
                        tint = UniStackColors.Primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            "Información",
                            color = UniStackColors.TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Las notas de este corte se combinan según su peso porcentual para obtener la nota del corte.",
                            color = UniStackColors.TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }

    gradeIdPendingDelete?.let { gradeId ->
        AlertDialog(
            onDismissRequest = { gradeIdPendingDelete = null },
            title = { Text("¿Eliminar nota?", color = UniStackColors.TextPrimary) },
            text = { Text("Esta acción no se puede deshacer.", color = UniStackColors.TextSecondary) },
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
                    Text("Cancelar", color = UniStackColors.TextSecondary)
                }
            },
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
private fun MissingSubjectState(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = UniStackColors.TextPrimary)
        }
        Text("Materia no encontrada", color = UniStackColors.TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubjectHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = UniStackColors.TextPrimary
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(UniStackColors.Primary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    subtitle,
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Opciones de materia",
                    tint = UniStackColors.TextPrimary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(UniStackColors.Card)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar materia", color = UniStackColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = UniStackColors.TextSecondary) },
                    onClick = onEditClick
                )
                DropdownMenuItem(
                    text = { Text("Eliminar materia", color = UniStackColors.Coral) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = UniStackColors.Coral) },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun PeriodHeader(title: String, subtitle: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = UniStackColors.TextPrimary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Spacer to balance back button
        Box(modifier = Modifier.size(48.dp))
    }
}

@Composable
private fun SubjectOverviewCard(
    subject: Subject,
    average: Double?,
    projectedFinal: Double?,
    evaluated: Double,
    maxGrade: Double,
    scale: GradingScale
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Proyección actual de la materia",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "--",
                        color = UniStackColors.Primary,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                        color = UniStackColors.TextSecondary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                Text(
                    if (average != null && projectedFinal != null) {
                        "Con el rendimiento actual, terminarías con $projectedFinal"
                    } else {
                        "Registra notas para proyectar tu promedio final."
                    },
                    color = UniStackColors.TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(90.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = UniStackColors.Primary.copy(alpha = 0.1f),
                    strokeWidth = 8.dp,
                    trackColor = Color.Transparent
                )
                CircularProgressIndicator(
                    progress = { (evaluated / 100.0).coerceIn(0.0, 1.0).toFloat() },
                    modifier = Modifier.fillMaxSize(),
                    color = UniStackColors.Primary,
                    strokeWidth = 8.dp,
                    trackColor = Color.Transparent
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${formatPercent(evaluated)}%",
                        color = UniStackColors.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        "evaluado",
                        color = UniStackColors.TextSecondary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectMetric(label: String, value: String, valueColor: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            label,
            color = UniStackColors.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
        Text(
            value,
            color = valueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun SubjectInsightCard(
    neededForTarget: Double?,
    targetGrade: Double,
    maxGrade: Double,
    scale: GradingScale,
    modifier: Modifier = Modifier
) {
    val annotatedText = remember(neededForTarget, targetGrade, maxGrade, scale) {
        buildAnnotatedString {
            if (neededForTarget == null) {
                append("Registra notas en los cortes para estimar lo necesario para tu meta.")
            } else if (neededForTarget <= 0.0) {
                append("¡Excelente! Con tu rendimiento actual ya aseguraste tu meta de ")
                withStyle(style = SpanStyle(color = UniStackColors.Green, fontWeight = FontWeight.Bold)) {
                    append(GradingScaleUtils.formatGrade(targetGrade, scale))
                }
                append(".")
            } else if (neededForTarget > maxGrade) {
                append("La meta de ")
                withStyle(style = SpanStyle(color = UniStackColors.Green, fontWeight = FontWeight.Bold)) {
                    append(GradingScaleUtils.formatGrade(targetGrade, scale))
                }
                append(" está en riesgo: necesitas más de ")
                withStyle(style = SpanStyle(color = UniStackColors.Yellow, fontWeight = FontWeight.Bold)) {
                    append(GradingScaleUtils.formatGrade(maxGrade, scale))
                }
                append(" en lo restante.")
            } else {
                append("Necesitas un promedio de ")
                withStyle(style = SpanStyle(color = UniStackColors.Green, fontWeight = FontWeight.Bold)) {
                    append(GradingScaleUtils.formatGrade(neededForTarget, scale))
                }
                append(" en los cortes restantes para mantener la materia por encima de ")
                withStyle(style = SpanStyle(color = UniStackColors.Green, fontWeight = FontWeight.Bold)) {
                    append(GradingScaleUtils.formatGrade(targetGrade, scale))
                }
                append(".")
            }
        }
    }

    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = UniStackColors.Green.copy(alpha = 0.08f),
        shape = LargeCardShape,
        borderColor = UniStackColors.Green.copy(alpha = 0.2f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.TrendingUp,
                contentDescription = null,
                tint = UniStackColors.Green,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = annotatedText,
                color = UniStackColors.TextPrimary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PeriodCard(
    summary: PeriodSummary,
    maxGrade: Double,
    scale: GradingScale,
    onClick: () -> Unit
) {
    val progress = (summary.evaluated / 100.0).coerceIn(0.0, 1.0).toFloat()
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left status indicator strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(summary.status.color)
            )
            // Main content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 20.dp, vertical = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Texts and progress
                Column(
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                periodDisplayName(summary.period),
                                color = UniStackColors.TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            StatusBadge(status = summary.status)
                        }
                        Text(
                            "${formatPercent(summary.period.weight * 100)}% de la materia",
                            color = UniStackColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                summary.average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "--",
                                color = if (summary.average != null) summary.status.color else UniStackColors.TextSecondary,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                color = UniStackColors.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 2.dp, bottom = 4.dp)
                            )
                        }
                        Text(
                            "${formatPercent(summary.evaluated)}% evaluado",
                            color = UniStackColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.8f)
                                .height(6.dp)
                                .clip(CircleShape),
                            color = summary.status.color,
                            trackColor = UniStackColors.SurfaceVariant
                        )
                    }
                }
                
                // Right side: Icon, notes count, chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(summary.status.color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                summary.status.icon(),
                                contentDescription = null,
                                tint = summary.status.color,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            gradeCountLabel(summary.grades.size),
                            color = UniStackColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = UniStackColors.TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodOverviewCard(summary: PeriodSummary, maxGrade: Double, scale: GradingScale) {
    val progress = (summary.evaluated / 100.0).coerceIn(0.0, 1.0).toFloat()
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Nota del corte",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            summary.average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "--",
                            color = summary.status.color,
                            fontSize = 44.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                            color = UniStackColors.TextSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                        )
                    }
                }
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Estado",
                        color = UniStackColors.TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            summary.status.label,
                            color = summary.status.color,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Icon(
                            summary.status.icon(),
                            contentDescription = null,
                            tint = summary.status.color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${formatPercent(summary.evaluated)}% evaluado",
                    color = UniStackColors.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = summary.status.color,
                    trackColor = UniStackColors.SurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                val notesText = if (summary.evaluated >= 99.9) {
                    "${summary.grades.size} de ${summary.grades.size} notas registradas"
                } else {
                    "${summary.grades.size} ${if (summary.grades.size == 1) "nota registrada" else "notas registradas"}"
                }
                Text(
                    notesText,
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Inline non-card version for use inside a grouped card
@Composable
private fun GradeRowItem(
    grade: GradeItem,
    scale: GradingScale,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(grade.type.colorLocal().copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                grade.type.icon(),
                contentDescription = null,
                tint = grade.type.colorLocal(),
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text(
                grade.name,
                color = UniStackColors.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "${formatPercent(grade.percentage * 100)}% del corte",
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp
            )
        }
        Text(
            GradingScaleUtils.formatGrade(grade.value, scale),
            color = UniStackColors.Green,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Opciones de nota",
                    tint = UniStackColors.TextSecondary
                )
            }
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(UniStackColors.Card)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar", color = UniStackColors.TextPrimary) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = UniStackColors.TextSecondary) },
                    onClick = { showMenu = false; onEditClick() }
                )
                DropdownMenuItem(
                    text = { Text("Eliminar", color = UniStackColors.Coral) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = UniStackColors.Coral) },
                    onClick = { showMenu = false; onDeleteClick() }
                )
            }
        }
    }
}

@Composable
private fun GradeRowCard(
    grade: GradeItem,
    scale: GradingScale,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(grade.type.colorLocal().copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    grade.type.icon(),
                    contentDescription = null,
                    tint = grade.type.colorLocal(),
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    grade.name,
                    color = UniStackColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${formatPercent(grade.percentage * 100)}% del corte",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Text(
                GradingScaleUtils.formatGrade(grade.value, scale),
                color = UniStackColors.Green,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "Opciones de nota",
                        tint = UniStackColors.TextSecondary
                    )
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(UniStackColors.Card)
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar", color = UniStackColors.TextPrimary) },
                        leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = UniStackColors.TextSecondary) },
                        onClick = {
                            showMenu = false
                            onEditClick()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = UniStackColors.Coral) },
                        leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = UniStackColors.Coral) },
                        onClick = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyPeriodNotesCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(UniStackColors.Primary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.Assignment,
                    contentDescription = null,
                    tint = UniStackColors.Primary,
                    modifier = Modifier.size(28.dp)
                )
            }
            Text(
                "Aún no hay notas",
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                "Agrega una actividad para calcular el avance de este corte.",
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
private fun StatusBadge(status: PeriodStatus) {
    Box(
        modifier = Modifier
            .background(status.color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            status.label,
            color = status.color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun PeriodPickerSheet(
    summaries: List<PeriodSummary>,
    onPeriodSelected: (String) -> Unit,
    onCancel: () -> Unit,
    scale: GradingScale
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UniStackColors.Card)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        // Drag handle + title
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(36.dp)
                    .height(4.dp)
                    .background(UniStackColors.SoftOutline, CircleShape)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "¿En qué corte quieres registrar la nota?",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
        }
        
        HorizontalDivider(color = UniStackColors.SoftOutline)
        
        // Period items
        Column {
            summaries.forEachIndexed { index, summary ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .bounceClick { onPeriodSelected(summary.period.id) }
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(summary.status.color.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            summary.status.icon(),
                            contentDescription = null,
                            tint = summary.status.color,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                periodDisplayName(summary.period),
                                color = UniStackColors.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            StatusBadge(status = summary.status)
                        }
                        Text(
                            "${formatPercent(summary.period.weight * 100)}% de la materia • ${gradeCountLabel(summary.grades.size)}",
                            color = UniStackColors.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = UniStackColors.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (index < summaries.lastIndex) {
                    HorizontalDivider(
                        color = UniStackColors.SoftOutline,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        }
        
        HorizontalDivider(color = UniStackColors.SoftOutline)
        
        // Cancel button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick(onCancel)
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "Cancelar",
                color = UniStackColors.TextSecondary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }
    }
}

private data class PeriodSummary(
    val period: AcademicPeriod,
    val grades: List<GradeItem>,
    val average: Double?,
    val evaluated: Double,
    val status: PeriodStatus
)

private enum class PeriodStatus(val label: String) {
    COMPLETED("Completado"),
    IN_PROGRESS("En curso"),
    PENDING("Pendiente")
}

private val PeriodStatus.color: Color
    @Composable get() = when (this) {
        PeriodStatus.COMPLETED -> UniStackColors.Green
        PeriodStatus.IN_PROGRESS -> UniStackColors.Primary
        PeriodStatus.PENDING -> UniStackColors.Yellow
    }

private fun PeriodStatus.icon(): ImageVector {
    return when (this) {
        PeriodStatus.COMPLETED -> Icons.Rounded.CheckCircle
        PeriodStatus.IN_PROGRESS -> Icons.AutoMirrored.Rounded.Assignment
        PeriodStatus.PENDING -> Icons.AutoMirrored.Rounded.Assignment
    }
}

private fun AcademicPeriod.toSummary(grades: List<GradeItem>): PeriodSummary {
    val evaluated = round(grades.sumOf { it.percentage } * 100.0 * 10.0) / 10.0
    val status = when {
        evaluated <= 0.001 -> PeriodStatus.PENDING
        evaluated >= 99.9 -> PeriodStatus.COMPLETED
        else -> PeriodStatus.IN_PROGRESS
    }
    val cortePromedio = GradeCalculator.calculatePeriodAverage(grades)
    return PeriodSummary(
        period = this,
        grades = grades,
        average = cortePromedio,
        evaluated = evaluated,
        status = status
    )
}

private fun periodDisplayName(period: AcademicPeriod): String = "Corte ${period.order}"

private fun gradeCountLabel(count: Int): String = "$count ${if (count == 1) "nota" else "notas"}"

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.0f", value)

private fun GradeType.icon(): ImageVector {
    return when (this) {
        GradeType.WORKSHOP,
        GradeType.PRACTICE -> Icons.AutoMirrored.Rounded.Assignment
        GradeType.PRESENTATION -> Icons.Rounded.School
        GradeType.EXAM,
        GradeType.QUIZ -> Icons.Rounded.CheckCircle
        GradeType.PROJECT,
        GradeType.RESEARCH,
        GradeType.OTHER -> Icons.Rounded.BarChart
    }
}

private fun GradeType.colorLocal(): Color {
    return when (this) {
        GradeType.WORKSHOP,
        GradeType.PRACTICE -> UniStackColors.Green
        GradeType.PRESENTATION -> UniStackColors.Primary
        GradeType.EXAM,
        GradeType.QUIZ -> UniStackColors.Yellow
        GradeType.PROJECT,
        GradeType.RESEARCH -> Color(0xFF38BDF8)
        GradeType.OTHER -> UniStackColors.TextSecondary
    }
}
