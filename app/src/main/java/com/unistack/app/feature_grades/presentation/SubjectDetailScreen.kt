package com.unistack.app.feature_grades.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.components.SquishyButton

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PriorityHigh
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.LocalBottomBarOverlay
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.SubjectGradeCalculation
import com.unistack.app.core.utils.TargetOutlook
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.AcademicIndicatorStyle
import com.unistack.app.feature_user.domain.GradingScale
import java.util.Locale
import kotlin.math.round

private val LargeCardShape = AppShapes.SmallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    onPeriodClick: (String, String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onCompleteHistoryClick: (String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val classSession = classSessions.firstOrNull { it.subjectId == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val periodScheme = subject?.periodScheme ?: profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    if (subject == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    // Mientras el usuario no diga en qué corte va, la pantalla no lo supone: no marca ningún
    // corte como actual y no reclama el historial de los anteriores. Reclamarlo exige saber
    // que esos cortes ya pasaron, y eso solo lo sabe él.
    val chosenPeriod = subject.chosenPeriodId?.let { id ->
        periodScheme.periods.firstOrNull { it.id == id }
    }
    val activePeriodOrder = chosenPeriod?.order ?: 1
    val hasIncompletePriorHistory = chosenPeriod != null &&
        activePeriodOrder > 1 &&
        periodScheme.periods
            .filter { it.order < activePeriodOrder }
            .any { period ->
                subject.grades.none { it.periodId == period.id } &&
                    period.id !in subject.unknownPeriodIds
            }

    val periodSummaries = remember(subject, periodScheme) {
        periodScheme.periods.map { period ->
            val grades = subject.grades.filter { it.periodId == period.id }
            period.toSummary(grades)
        }
    }
    val orderedPeriodSummaries = remember(periodSummaries, subject.activePeriodId) {
        periodSummaries.sortedWith(
            compareByDescending<PeriodSummary> { it.period.id == subject.activePeriodId }
                .thenBy { it.period.order }
        )
    }

    // Una sola cuenta para toda la pantalla. Antes había tres: el promedio salía del
    // calculador, la proyección final se calculaba aquí a mano con otra fórmula —contaba un
    // corte apenas empezado con su peso completo— y lo necesario para la meta volvía a pasar
    // por el porcentaje ya redondeado. La tarjeta llegaba a enseñar dos cifras distintas
    // para lo mismo, una encima de la otra.
    val calculation = remember(subject.grades, periodScheme, subject.targetAverage, maxGrade) {
        GradeCalculator.calculateSubject(
            grades = subject.grades,
            periods = periodScheme.periods,
            targetAverage = subject.targetAverage,
            maxGrade = maxGrade
        )
    }
    val evaluatedSubjectPercentage = calculation.evaluatedSemesterFraction * 100.0
    val remainingSubjectPercentage = calculation.remainingSemesterFraction * 100.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 22.dp,
                top = 8.dp,
                end = 22.dp,
                // 96dp para el botón pegado, más lo que tape la barra flotante encima
                // de él. Sin lo segundo, la última tarjeta de cortes quedaba debajo.
                bottom = 96.dp + LocalBottomBarOverlay.current
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SubjectHeader(
                    title = subject.name,
                    // «Materia activa» era una etiqueta fija que no distinguía nada: no
                    // existen materias inactivas. La meta sí dice algo y no se repite en
                    // ningún otro sitio de la cabecera.
                    subtitle = "Meta ${GradingScaleUtils.formatGrade(subject.targetAverage, scale)}",
                    onBackClick = onBackClick,
                    showMenu = showSubjectMenu,
                    onMenuClick = { showSubjectMenu = true },
                    onDismissMenu = { showSubjectMenu = false },
                    onEditClick = {
                        showSubjectMenu = false
                        onEditSubjectClick(subject.id)
                    },
                    onCompleteHistoryClick = if (hasIncompletePriorHistory) {
                        {
                            showSubjectMenu = false
                            onCompleteHistoryClick(subject.id)
                        }
                    } else {
                        null
                    },
                    onDeleteClick = {
                        showSubjectMenu = false
                        showDeleteSubjectDialog = true
                    }
                )
            }
            classSession?.let { session ->
                item { SubjectClassFacts(session) }
            }
            item {
                SubjectOverviewCard(
                    calculation = calculation,
                    targetGrade = subject.targetAverage,
                    evaluated = evaluatedSubjectPercentage,
                    maxGrade = maxGrade,
                    scale = scale
                )
            }
            item {
                SubjectMetricsBand(
                    calculation = calculation,
                    passingGrade = passingGrade,
                    targetGrade = subject.targetAverage,
                    remainingPercentage = remainingSubjectPercentage,
                    scale = scale
                )
            }
            // Sin notas, la tarjeta de resumen ya dice que hay que registrar la primera. Esta
            // repetía el mismo encargo con otras palabras dos tarjetas más abajo.
            if (calculation.outlook != TargetOutlook.NO_DATA) {
                item {
                    SubjectInsightCard(
                        calculation = calculation,
                        targetGrade = subject.targetAverage,
                        maxGrade = maxGrade,
                        scale = scale
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Cortes del semestre",
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    // Sin «Corte N activo»: el selector de justo debajo ya lo marca y la
                    // tarjeta del corte lo repite otra vez. Eran tres formas de decir lo mismo
                    // seguidas.
                    Text(
                        "${periodScheme.periods.size} cortes",
                        color = UniStackColors.TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
            item {
                PeriodChooser(
                    periods = periodScheme.periods,
                    chosenPeriodId = subject.chosenPeriodId,
                    onChoose = { viewModel.setActivePeriod(subject.id, it) }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    orderedPeriodSummaries.forEach { summary ->
                        val needsHistory = chosenPeriod != null &&
                            summary.period.order < activePeriodOrder &&
                            summary.grades.isEmpty() &&
                            summary.period.id !in subject.unknownPeriodIds
                        PeriodCard(
                            summary = summary,
                            maxGrade = maxGrade,
                            scale = scale,
                            isActive = summary.period.id == subject.chosenPeriodId,
                            needsHistory = needsHistory,
                            onClick = {
                                if (needsHistory) {
                                    onCompleteHistoryClick(subject.id)
                                } else {
                                    onPeriodClick(subject.id, summary.period.id)
                                }
                            }
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
                // El degradado ya se dibuja hasta el borde porque va antes que el margen;
                // lo que faltaba era apartarse del teclado.
                .bottomActionInsets()
                .padding(bottom = LocalBottomBarOverlay.current)
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Sin corte elegido el botón no lleva a ninguna parte: no hay a qué corte añadir
            // la nota. Queda apagado y dice qué falta, en vez de mandar la nota al primero.
            SquishyButton(
                onClick = { chosenPeriod?.let { onAddGradeClick(subject.id, it.id) } },
                enabled = chosenPeriod != null,
                shape = AppShapes.LargeCard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UniStackColors.Primary,
                    contentColor = UniStackColors.OnPrimary,
                    disabledContainerColor = UniStackColors.SurfaceVariant,
                    disabledContentColor = UniStackColors.TextSecondary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    val buttonContent = if (chosenPeriod == null) {
                        UniStackColors.TextSecondary
                    } else {
                        UniStackColors.OnPrimary
                    }
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = buttonContent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (chosenPeriod == null) {
                            "Elige un corte para agregar notas"
                        } else {
                            "Agregar nota a ${periodDisplayName(chosenPeriod)}"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = buttonContent
                    )
                }
            }
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
    viewModel: GradesViewModel = hiltViewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val periodScheme = subject?.periodScheme ?: profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val period = periodScheme.periods.firstOrNull { it.id == periodId }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }

    if (subject == null || period == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val grades = subject.grades.filter { it.periodId == period.id }
    val summary = period.toSummary(grades)
    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = saveBarHeight + scrollBottomRoom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                PeriodHeader(
                    title = periodDisplayName(period),
                    subtitle = "${subject.name}  ·  ${formatPercent(period.weight * 100)}% de la materia",
                    onBackClick = onBackClick
                )
            }
            item {
                PeriodSummaryCard(summary = summary, maxGrade = maxGrade, scale = scale)
            }
            if (grades.isEmpty()) {
                item {
                    // Una sola tarjeta de estado vacío. Antes había tres bloques seguidos
                    // diciendo casi lo mismo: «Aún no hay notas», el botón, y una tarjeta de
                    // «Información» con una frase fija que nunca cambiaba y ocupaba tanto como
                    // el contenido. La frase explica algo útil solo aquí, así que vive aquí.
                    EmptyPeriodNotesInline()
                }
            } else {
                item {
                    Text(
                        "Notas del corte",
                        color = UniStackColors.TextPrimary,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LargeCardShape)
                            .background(UniStackColors.Card)
                            .border(1.dp, UniStackColors.SoftOutline, LargeCardShape)
                    ) {
                        grades.forEachIndexed { index, grade ->
                            GradeRowItem(
                                grade = grade,
                                scale = scale,
                                onEditClick = { onEditGradeClick(subject.id, grade.id) },
                                onDeleteClick = { gradeIdPendingDelete = grade.id }
                            )
                            if (index < grades.lastIndex) {
                                HorizontalDivider(color = UniStackColors.SoftOutline, thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }

        // Anclado, igual que en el detalle de materia. Antes iba dentro del contenido, entre
        // el estado vacío y una tarjeta informativa, y había que desplazarse para encontrarlo.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { saveBarHeight = with(density) { it.height.toDp() } },
            color = UniStackColors.Background,
            shadowElevation = 8.dp
        ) {
            SquishyButton(
                onClick = { onAddGradeClick(subject.id, period.id) },
                shape = AppShapes.LargeCard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UniStackColors.Primary,
                    contentColor = UniStackColors.OnPrimary
                ),
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(bottom = LocalBottomBarOverlay.current)
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = UniStackColors.OnPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar nota a ${periodDisplayName(period)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = UniStackColors.OnPrimary
                )
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
    onCompleteHistoryClick: (() -> Unit)?,
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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
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
                        tint = UniStackColors.OnPrimary,
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
                onCompleteHistoryClick?.let { action ->
                    DropdownMenuItem(
                        text = { Text("Completar historial", color = UniStackColors.TextPrimary) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = UniStackColors.Yellow)
                        },
                        onClick = action
                    )
                }
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


/**
 * Profesor, aula y horario de la materia.
 *
 * Se piden al crear la materia y hasta ahora solo se veían en Horario, así que quien los
 * escribía no volvía a encontrarlos donde los busca: en la propia materia. Se omite lo que
 * esté vacío y la fila entera desaparece si no hay clase.
 */
@Composable
private fun SubjectClassFacts(session: ClassSession) {
    val facts = listOfNotNull(
        session.place.professor.takeIf { it.isNotBlank() }?.let { Icons.Rounded.Person to it },
        session.place.room.takeIf { it.isNotBlank() }?.let { Icons.Rounded.Place to it },
        session.daysAndTimeLabel().takeIf { it.isNotBlank() }?.let { Icons.Rounded.CalendarMonth to it }
    )
    if (facts.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.SmallCard,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            facts.forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = UniStackColors.TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text, color = UniStackColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * En qué corte va la materia.
 *
 * Mientras no haya elección, es una pregunta con todas las opciones apagadas y no un ajuste
 * con una ya marcada. La app venía dando por hecho el primer corte y presentándolo como
 * «Corte actual» en materias recién creadas; de ahí salía además que reclamara el historial
 * de unos cortes anteriores que el usuario nunca dijo haber cursado.
 */
@Composable
private fun PeriodChooser(
    periods: List<AcademicPeriod>,
    chosenPeriodId: String?,
    onChoose: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (chosenPeriodId == null) "¿En qué corte vas?" else "Las notas nuevas entran en",
            color = if (chosenPeriodId == null) UniStackColors.TextPrimary else UniStackColors.TextSecondary,
            fontSize = if (chosenPeriodId == null) 15.sp else 13.sp,
            fontWeight = if (chosenPeriodId == null) FontWeight.ExtraBold else FontWeight.SemiBold
        )
        if (chosenPeriodId == null) {
            Text(
                "Elígelo para saber dónde entran tus notas y qué cortes ya pasaron.",
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            periods.sortedBy { it.order }.forEach { period ->
                val selected = chosenPeriodId == period.id
                Surface(
                    modifier = Modifier.bounceClick { onChoose(period.id) },
                    shape = AppShapes.Small,
                    color = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    border = if (chosenPeriodId == null) {
                        androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                        )
                    } else {
                        null
                    }
                ) {
                    Text(
                        periodDisplayName(period),
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        color = if (selected) UniStackColors.OnPrimary else UniStackColors.TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

/**
 * Dónde está la materia y entre qué dos notas puede acabar.
 *
 * La segunda línea decía «con el rendimiento actual, terminarías con X» a partir de un
 * cálculo propio que contaba los cortes empezados con su peso entero: una sola nota del 5%
 * en un corte del 40% movía la cifra como si el corte estuviera cerrado. En su lugar van los
 * dos extremos reales —sacar 0 en lo que falta y sacarlo todo—, que no suponen nada.
 */
@Composable
private fun SubjectOverviewCard(
    calculation: SubjectGradeCalculation,
    targetGrade: Double,
    evaluated: Double,
    maxGrade: Double,
    scale: GradingScale
) {
    val average = calculation.currentAverage
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
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
                        if (calculation.isFinished) "Nota final" else "Promedio de lo evaluado",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (average == null) {
                        Text(
                            "Sin evaluar",
                            color = UniStackColors.TextSecondary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                GradingScaleUtils.formatGrade(average, scale),
                                color = UniStackColors.Primary,
                                fontSize = 42.sp,
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
                    }
                }
                val indicatorStyle = LocalAppearancePreferences.current.academicIndicatorStyle
                when (indicatorStyle) {
                    AcademicIndicatorStyle.RINGS -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            CircularProgressIndicator(
                                progress = { 1f },
                                modifier = Modifier.fillMaxSize(),
                                color = UniStackColors.Primary.copy(alpha = 0.1f),
                                strokeWidth = 7.dp,
                                trackColor = Color.Transparent
                            )
                            CircularProgressIndicator(
                                progress = { (evaluated / 100.0).coerceIn(0.0, 1.0).toFloat() },
                                modifier = Modifier.fillMaxSize(),
                                color = UniStackColors.Primary,
                                strokeWidth = 7.dp,
                                trackColor = Color.Transparent
                            )
                            EvaluationValue(evaluated)
                        }
                    }
                    AcademicIndicatorStyle.BARS -> {
                        Column(
                            modifier = Modifier.width(88.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EvaluationValue(evaluated)
                            EvaluationBar(
                                fraction = evaluated / 100.0,
                                height = 7.dp,
                                color = UniStackColors.Primary
                            )
                        }
                    }
                    AcademicIndicatorStyle.NUMBERS -> EvaluationValue(evaluated)
                }
            }

            val floor = calculation.guaranteedMinimum
            val ceiling = calculation.bestPossible
            when {
                floor == null || ceiling == null -> {
                    Text(
                        "Registra tu primera nota para saber entre qué notas puedes acabar.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                calculation.isFinished -> {
                    Text(
                        "Ya no queda nada por evaluar: esta es la nota definitiva.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Dónde puedes acabar",
                            color = UniStackColors.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutcomeRangeBar(
                            floor = floor,
                            ceiling = ceiling,
                            target = targetGrade,
                            maxGrade = maxGrade
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            RangeLegend("Mínimo", GradingScaleUtils.formatGrade(floor, scale), UniStackColors.TextSecondary)
                            RangeLegend("Meta", GradingScaleUtils.formatGrade(targetGrade, scale), UniStackColors.TextPrimary)
                            RangeLegend("Máximo", GradingScaleUtils.formatGrade(ceiling, scale), UniStackColors.TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

/**
 * La franja de notas finales todavía posibles, sobre la escala completa, con la meta marcada.
 *
 * El extremo izquierdo es sacar 0 en todo lo que falta y el derecho sacarlo todo, así que la
 * franja solo se estrecha según se van registrando notas. Si la marca de la meta queda fuera
 * de la franja, la meta ya no se puede alcanzar y se ve sin leer ningún texto.
 */
@Composable
private fun OutcomeRangeBar(
    floor: Double,
    ceiling: Double,
    target: Double,
    maxGrade: Double
) {
    if (maxGrade <= 0.0) return
    val start = (floor / maxGrade).coerceIn(0.0, 1.0).toFloat()
    val end = (ceiling / maxGrade).coerceIn(0.0, 1.0).toFloat()
    val targetAt = (target / maxGrade).coerceIn(0.0, 1.0).toFloat()
    val targetIsInside = target in floor..ceiling
    val bandColor = if (targetIsInside) UniStackColors.Primary else UniStackColors.Coral

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(14.dp)
    ) {
        val fullWidth = maxWidth
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(UniStackColors.SurfaceVariant)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = fullWidth * start)
                .width((fullWidth * (end - start)).coerceAtLeast(3.dp))
                .height(10.dp)
                .clip(CircleShape)
                .background(bandColor)
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = (fullWidth * targetAt - 1.5.dp).coerceAtLeast(0.dp))
                .width(3.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(UniStackColors.TextPrimary)
        )
    }
}

@Composable
private fun RangeLegend(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = UniStackColors.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EvaluationValue(evaluated: Double) {
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

/**
 * Aprobación, objetivo y lo que queda por evaluar.
 *
 * Los dos primeros iban en verde fijo, dijeran lo que dijeran los datos: la nota de
 * aprobación se pintaba igual de verde estando ya perdida. Ahora cada una se colorea según
 * siga estando a tiro con lo que falta.
 */
@Composable
private fun SubjectMetricsBand(
    calculation: SubjectGradeCalculation,
    passingGrade: Double,
    targetGrade: Double,
    remainingPercentage: Double,
    scale: GradingScale
) {
    val reachTone: (Double) -> Color = { threshold ->
        val floor = calculation.guaranteedMinimum
        val ceiling = calculation.bestPossible
        when {
            floor == null || ceiling == null -> UniStackColors.TextSecondary
            floor >= threshold - 0.0001 -> UniStackColors.Green
            ceiling < threshold - 0.0001 -> UniStackColors.Coral
            else -> UniStackColors.Yellow
        }
    }
    SubjectMetricsBandContent(
        passingGrade = GradingScaleUtils.formatGrade(passingGrade, scale),
        passingTone = reachTone(passingGrade),
        targetGrade = GradingScaleUtils.formatGrade(targetGrade, scale),
        targetTone = reachTone(targetGrade),
        remainingPercentage = "${formatPercent(remainingPercentage)}%"
    )
}

@Composable
private fun SubjectMetricsBandContent(
    passingGrade: String,
    passingTone: Color,
    targetGrade: String,
    targetTone: Color,
    remainingPercentage: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.SmallCard,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MetricBandItem("Aprobación", passingGrade, passingTone, Modifier.weight(1f))
            MetricDivider()
            MetricBandItem("Objetivo", targetGrade, targetTone, Modifier.weight(1f))
            MetricDivider()
            MetricBandItem("Por evaluar", remainingPercentage, UniStackColors.TextPrimary, Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricBandItem(label: String, value: String, color: Color, modifier: Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = UniStackColors.TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Normal)
        Text(value, color = color, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MetricDivider() {
    Box(Modifier.fillMaxHeight().width(1.dp).background(UniStackColors.SoftOutline))
}

@Composable
private fun SubjectInsightCard(
    calculation: SubjectGradeCalculation,
    targetGrade: Double,
    maxGrade: Double,
    scale: GradingScale,
    modifier: Modifier = Modifier
) {
    // El color sale del mismo sitio que el mensaje. Antes la tarjeta era verde siempre,
    // así que «tu meta está en riesgo» se leía sobre fondo verde y con el número
    // resaltado también en verde: el color decía lo contrario que el texto.
    val tone = when (calculation.outlook) {
        TargetOutlook.NO_DATA -> UniStackColors.TextSecondary
        TargetOutlook.SECURED -> UniStackColors.Green
        TargetOutlook.ON_TRACK -> UniStackColors.Teal
        TargetOutlook.AT_RISK -> UniStackColors.Yellow
        TargetOutlook.UNREACHABLE -> UniStackColors.Coral
    }

    val annotatedText = remember(calculation, targetGrade, maxGrade, scale, tone) {
        val target = GradingScaleUtils.formatGrade(targetGrade, scale)
        val bold = SpanStyle(color = tone, fontWeight = FontWeight.Bold)
        buildAnnotatedString {
            when (calculation.outlook) {
                TargetOutlook.NO_DATA ->
                    append("Registra notas en los cortes para saber qué te hace falta para tu meta.")

                TargetOutlook.SECURED -> {
                    // Con la materia cerrada, el mensaje habla en pasado. Antes «lo que falta
                    // para la meta» era null tanto sin notas como con todo evaluado, así que
                    // una materia terminada pedía registrar notas.
                    if (calculation.isFinished) {
                        append("Materia cerrada con ")
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(": cumpliste tu meta de $target.")
                    } else {
                        append("Tu meta de ")
                        withStyle(bold) { append(target) }
                        append(" ya está asegurada: aunque saques 0 en todo lo que falta, terminas con ")
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(".")
                    }
                }

                TargetOutlook.ON_TRACK -> {
                    append("Vas por encima de tu meta de $target. Te basta con ")
                    withStyle(bold) {
                        append(GradingScaleUtils.formatGrade(calculation.neededForTarget, scale))
                    }
                    append(" en el ")
                    append("${formatPercent(calculation.remainingSemesterFraction * 100)}% que falta por evaluar")
                    append(" para no bajar de ahí.")
                }

                TargetOutlook.AT_RISK -> {
                    append("Necesitas ")
                    withStyle(bold) {
                        append(GradingScaleUtils.formatGrade(calculation.neededForTarget, scale))
                    }
                    // «en los cortes restantes» era inexacto: lo que falta suele ser parte de
                    // un corte ya empezado, no cortes enteros.
                    append(" en el ${formatPercent(calculation.remainingSemesterFraction * 100)}% que falta por evaluar")
                    append(" para llegar a tu meta de $target.")
                }

                TargetOutlook.UNREACHABLE -> {
                    if (calculation.isFinished) {
                        append("Materia cerrada con ")
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(", por debajo de tu meta de $target.")
                    } else {
                        append("Tu meta de $target ya no es alcanzable: aun sacando ")
                        append("${GradingScaleUtils.formatGrade(maxGrade, scale)} en todo lo que falta, terminarías con ")
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.bestPossible, scale))
                        }
                        append(".")
                    }
                }
            }
        }
    }

    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = tone.copy(alpha = 0.08f),
        shape = LargeCardShape,
        borderColor = tone.copy(alpha = 0.2f),
        borderWidth = 1.dp,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                // El icono también sigue al estado: una flecha al alza junto a un aviso
                // de meta en riesgo contradecía lo que decía el texto.
                when (calculation.outlook) {
                    TargetOutlook.SECURED -> Icons.Rounded.CheckCircle
                    TargetOutlook.AT_RISK, TargetOutlook.UNREACHABLE -> Icons.Rounded.PriorityHigh
                    else -> Icons.AutoMirrored.Rounded.TrendingUp
                },
                contentDescription = null,
                tint = tone,
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
    isActive: Boolean,
    needsHistory: Boolean,
    onClick: () -> Unit
) {
    val progress = (summary.evaluated / 100.0).coerceIn(0.0, 1.0)
    val accent = when {
        needsHistory -> UniStackColors.Yellow
        isActive -> UniStackColors.Primary
        else -> summary.status.color
    }
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick),
        color = UniStackColors.Card,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        borderColor = if (isActive) accent.copy(alpha = 0.48f) else UniStackColors.SoftOutline,
        borderWidth = 1.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left status indicator strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            // Main content
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Texts and progress
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                periodDisplayName(summary.period),
                                color = UniStackColors.TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            // La insignia dice siempre el estado del corte. Antes, en el corte
                            // activo la ocupaba «Corte actual», así que de ese corte —el único
                            // que importa ahora mismo— no se podía saber si estaba pendiente,
                            // en curso o completado.
                            if (needsHistory) {
                                CustomStatusBadge("Completar historial", UniStackColors.Yellow)
                            } else {
                                StatusBadge(status = summary.status)
                            }
                        }
                        Text(
                            buildString {
                                append("${formatPercent(summary.period.weight * 100)}% de la materia")
                                if (isActive) append("  ·  Corte actual")
                            },
                            color = if (isActive) UniStackColors.Primary else UniStackColors.TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (summary.average == null) {
                            Text(
                                "Sin evaluar",
                                color = UniStackColors.TextSecondary,
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    GradingScaleUtils.formatGrade(summary.average, scale),
                                    // La cifra sigue al estado del corte, no a si es el corte
                                    // activo: ser el corte en curso no dice nada de la nota.
                                    color = summary.status.color,
                                    fontSize = 24.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                    color = UniStackColors.TextSecondary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                                )
                            }
                        }
                        Text(
                            "${formatPercent(summary.evaluated)}% evaluado",
                            color = UniStackColors.TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        EvaluationBar(fraction = progress, height = 5.dp)
                    }
                }

                // Right side: Icon, notes count, chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accent.copy(alpha = 0.15f), AppShapes.Small),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (needsHistory) Icons.Rounded.Lightbulb else summary.status.icon(),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            gradeCountLabel(summary.grades.size),
                            color = UniStackColors.TextSecondary,
                            fontSize = 11.sp,
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



/**
 * El resumen del corte: qué nota lleva, en qué estado está y cuánto de su peso está repartido.
 *
 * Sin envoltorio teñido. La pantalla entera iba dentro de una tarjeta pintada con el color del
 * estado, así que un corte pendiente teñía de ámbar todo lo que había debajo y parecía un aviso.
 * El color del estado se queda donde significa algo: la insignia y la cifra.
 */
@Composable
private fun PeriodSummaryCard(
    summary: PeriodSummary,
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
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Nota del corte",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (summary.average == null) {
                        // Sin notas se escribía la raya de «sin dato» a 42sp y en el color del
                        // estado: una mancha ámbar del tamaño de una nota, que se leía como un
                        // valor y no como una ausencia.
                        Text(
                            "Sin evaluar",
                            color = UniStackColors.TextSecondary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                GradingScaleUtils.formatGrade(summary.average, scale),
                                color = summary.status.color,
                                fontSize = 42.sp,
                                lineHeight = 46.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                color = UniStackColors.TextSecondary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
                StatusBadge(status = summary.status)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EvaluationBar(fraction = (summary.evaluated / 100.0).coerceIn(0.0, 1.0))
                // Antes decía «3 de 3 notas registradas», comparando un número consigo mismo.
                // Lo que falta por saber cuando el corte no está cerrado es cuánto peso queda
                // libre, que es justo lo que hay que repartir en la siguiente nota.
                val remaining = (100.0 - summary.evaluated).coerceAtLeast(0.0)
                Text(
                    buildString {
                        append("${formatPercent(summary.evaluated)}% evaluado")
                        append("  ·  ")
                        append(gradeCountLabel(summary.grades.size))
                        if (remaining > 0.05) {
                            append("  ·  queda ${formatPercent(remaining)}% por repartir")
                        }
                    },
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
                .background(grade.type.colorLocal().copy(alpha = 0.15f), AppShapes.Small),
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
                grade.contextLabel(),
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
                    grade.contextLabel(),
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

private fun GradeItem.contextLabel(): String {
    return when {
        source == GradeSource.PERIOD_FINAL -> "Nota oficial del corte"
        weightStatus == GradeWeightStatus.UNKNOWN -> "Peso pendiente por definir"
        else -> "${formatPercent(percentage * 100)}% del corte"
    }
}

@Composable
private fun EmptyPeriodNotesInline() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LargeCardShape)
            .background(UniStackColors.Card.copy(alpha = if (UniStackColors.IsDarkTheme) 0.78f else 0.92f))
            .border(1.dp, UniStackColors.SoftOutline, LargeCardShape)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(UniStackColors.Primary.copy(alpha = 0.12f), AppShapes.Small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Assignment,
                contentDescription = null,
                tint = UniStackColors.Primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text("Aún no hay notas", color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            "Agrega una actividad para calcular este corte.",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
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
            .background(status.color.copy(alpha = 0.12f), AppShapes.Small)
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
private fun CustomStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), AppShapes.Small)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
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
    val calculation = GradeCalculator.calculatePeriod(grades)
    val evaluated = round(calculation.evaluatedFraction * 100.0 * 10.0) / 10.0
    val status = when {
        evaluated <= 0.001 -> PeriodStatus.PENDING
        evaluated >= 99.9 -> PeriodStatus.COMPLETED
        else -> PeriodStatus.IN_PROGRESS
    }
    return PeriodSummary(
        period = this,
        grades = grades,
        average = calculation.average,
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
        GradeType.RESEARCH -> UniStackColors.Blue
        GradeType.OTHER -> UniStackColors.TextSecondary
    }
}
