@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import com.unistack.app.core.design.components.promedioQueSube
import com.unistack.app.core.design.components.notaRecienRegistrada
import com.unistack.app.core.design.components.UniDivider
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.EvaluationRing
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
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
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
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
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.AcademicIndicatorStyle
import com.unistack.app.feature_user.domain.GradingScale
import java.util.Locale
import kotlin.math.round

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.Flag
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
private val LargeCardShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.medium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    onCutClick: (String, String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onCompleteHistoryClick: (String) -> Unit,
    onNewNoteClick: (String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val classSession = classSessions.firstOrNull { it.subjectId == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val cutScheme = subject?.cutScheme ?: profile?.gradingCutScheme ?: GradingCutScheme.default()
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
    val chosenCut = subject.chosenCutId?.let { id ->
        cutScheme.cuts.firstOrNull { it.id == id }
    }
    val activeCutOrder = chosenCut?.order ?: 1
    val hasIncompletePriorHistory = chosenCut != null &&
        activeCutOrder > 1 &&
        cutScheme.cuts
            .filter { it.order < activeCutOrder }
            .any { cut ->
                subject.grades.none { it.cutId == cut.id } &&
                    cut.id !in subject.unknownCutIds
            }

    val cutSummaries = remember(subject, cutScheme) {
        cutScheme.cuts.map { cut ->
            val grades = subject.grades.filter { it.cutId == cut.id }
            cut.toSummary(grades)
        }
    }
    val orderedCutSummaries = remember(cutSummaries, subject.activeCutId) {
        cutSummaries.sortedWith(
            compareByDescending<CutSummary> { it.cut.id == subject.activeCutId }
                .thenBy { it.cut.order }
        )
    }
    // Los cortes cerrados se apartan: ni se pueden elegir como destino de notas nuevas ni
    // compiten por la atención con los que aún están en juego.
    val openCutSummaries = orderedCutSummaries.filter { it.status != CutStatus.COMPLETED }
    val completedCutSummaries = cutSummaries
        .filter { it.status == CutStatus.COMPLETED }
        .sortedBy { it.cut.order }

    // Una sola cuenta para toda la pantalla. Antes había tres: el promedio salía del
    // calculador, la proyección final se calculaba aquí a mano con otra fórmula —contaba un
    // corte apenas empezado con su peso completo— y lo necesario para la meta volvía a pasar
    // por el porcentaje ya redondeado. La tarjeta llegaba a enseñar dos cifras distintas
    // para lo mismo, una encima de la otra.
    val calculation = remember(subject.grades, cutScheme, subject.targetAverage, maxGrade) {
        GradeCalculator.calculateSubject(
            grades = subject.grades,
            cuts = cutScheme.cuts,
            targetAverage = subject.targetAverage,
            maxGrade = maxGrade
        )
    }
    val evaluatedSubjectPercentage = calculation.evaluatedSemesterFraction * 100.0
    val remainingSubjectPercentage = calculation.remainingSemesterFraction * 100.0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                bottom = 96.dp
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
                    onNewNoteClick = {
                        showSubjectMenu = false
                        onNewNoteClick(subject.id)
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
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    // Sin «Corte N activo»: el selector de justo debajo ya lo marca y la
                    // tarjeta del corte lo repite otra vez. Eran tres formas de decir lo mismo
                    // seguidas.
                    Text(
                        "${cutScheme.cuts.size} cortes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            item {
                CutChooser(
                    // Un corte con el 100% repartido ya no admite más notas, así que sale de
                    // la lista de destinos. Seguir ofreciéndolo era ofrecer un sitio donde
                    // cualquier peso nuevo iba a ser rechazado al guardar.
                    cuts = openCutSummaries.map { it.cut },
                    chosenCutId = subject.chosenCutId,
                    onChoose = { viewModel.setActiveCut(subject.id, it) }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    openCutSummaries.forEach { summary ->
                        val needsHistory = chosenCut != null &&
                            summary.cut.order < activeCutOrder &&
                            summary.grades.isEmpty() &&
                            summary.cut.id !in subject.unknownCutIds
                        CutCard(
                            summary = summary,
                            maxGrade = maxGrade,
                            scale = scale,
                            isActive = summary.cut.id == subject.chosenCutId,
                            needsHistory = needsHistory,
                            onClick = {
                                if (needsHistory) {
                                    onCompleteHistoryClick(subject.id)
                                } else {
                                    onCutClick(subject.id, summary.cut.id)
                                }
                            }
                        )
                    }
                }
            }
            if (completedCutSummaries.isNotEmpty()) {
                item {
                    Text(
                        "Completados",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        completedCutSummaries.forEach { summary ->
                            // Siguen siendo tarjetas normales: se entra a consultarlas y, si
                            // hace falta corregir algo, a editar sus notas desde dentro.
                            CutCard(
                                summary = summary,
                                maxGrade = maxGrade,
                                scale = scale,
                                isActive = false,
                                needsHistory = false,
                                onClick = { onCutClick(subject.id, summary.cut.id) },
                                dimmed = true
                            )
                        }
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
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                            MaterialTheme.colorScheme.background.copy(alpha = 0.9f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                // El degradado ya se dibuja hasta el borde porque va antes que el margen;
                // lo que faltaba era apartarse del teclado.
                .bottomActionInsets()
                .padding(horizontal = 22.dp, vertical = 14.dp)
        ) {
            // Sin corte elegido el botón no lleva a ninguna parte: no hay a qué corte añadir
            // la nota. Queda apagado y dice qué falta, en vez de mandar la nota al primero.
            // Además de «sin elegir», el botón se apaga cuando el corte elegido ya está
            // cerrado y no queda ninguno abierto: no hay dónde meter la nota.
            val addTarget = chosenCut?.takeIf { cut ->
                openCutSummaries.any { it.cut.id == cut.id }
            }
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = { addTarget?.let { onAddGradeClick(subject.id, it.id) } },
                enabled = addTarget != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
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
                    val buttonContent = if (addTarget == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                    Icon(Icons.Rounded.Add, contentDescription = null, tint = buttonContent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        when {
                            addTarget != null -> "Agregar nota a ${cutDisplayName(addTarget)}"
                            openCutSummaries.isEmpty() -> "Todos los cortes están completos"
                            else -> "Elige un corte para agregar notas"
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
            title = { Text("¿Eliminar materia?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("También se eliminarán sus cortes y notas.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSubjectDialog = false
                        if (viewModel.deleteSubject(subject.id)) onSubjectDeleted()
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }
}

@Composable
fun SubjectCutDetailScreen(
    subjectId: String,
    cutId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String, String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val cutScheme = subject?.cutScheme ?: profile?.gradingCutScheme ?: GradingCutScheme.default()
    val cut = cutScheme.cuts.firstOrNull { it.id == cutId }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }

    if (subject == null || cut == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val grades = subject.grades.filter { it.cutId == cut.id }
    val summary = cut.toSummary(grades)
    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
                CutHeader(
                    title = cutDisplayName(cut),
                    subtitle = "${subject.name}  ·  ${formatPercent(cut.weight * 100)}% de la materia",
                    onBackClick = onBackClick
                )
            }
            item {
                CutSummaryCard(summary = summary, maxGrade = maxGrade, scale = scale)
            }
            if (grades.isEmpty()) {
                item {
                    // Una sola tarjeta de estado vacío. Antes había tres bloques seguidos
                    // diciendo casi lo mismo: «Aún no hay notas», el botón, y una tarjeta de
                    // «Información» con una frase fija que nunca cambiaba y ocupaba tanto como
                    // el contenido. La frase explica algo útil solo aquí, así que vive aquí.
                    EmptyCutNotesInline()
                }
            } else {
                item {
                    Text(
                        "Notas del corte",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(LargeCardShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, LargeCardShape)
                    ) {
                        /*
                         * Cual es la nota recien registrada.
                         *
                         * Se recuerda la ultima vista al entrar y se compara: la que no estaba
                         * es la nueva. Sin esa comparacion, todas entrarian animadas cada vez
                         * que se abre la materia y la entrada dejaria de significar «acaba de
                         * pasar algo».
                         */
                        val vistasAntes = remember { mutableStateOf(grades.map { it.id }.toSet()) }
                        val recienLlegada = grades.map { it.id }.firstOrNull { it !in vistasAntes.value }
                        LaunchedEffect(grades.size) {
                            kotlinx.coroutines.delay(1200)
                            vistasAntes.value = grades.map { it.id }.toSet()
                        }

                        grades.forEachIndexed { index, grade ->
                            GradeRowItem(
                                grade = grade,
                                scale = scale,
                                onEditClick = { onEditGradeClick(subject.id, grade.id) },
                                onDeleteClick = { gradeIdPendingDelete = grade.id },
                                modifier = Modifier.notaRecienRegistrada(grade.id == recienLlegada)
                            )
                            if (index < grades.lastIndex) {
                                UniDivider(color = MaterialTheme.colorScheme.outlineVariant)
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
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = { onAddGradeClick(subject.id, cut.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Agregar nota a ${cutDisplayName(cut)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }


    gradeIdPendingDelete?.let { gradeId ->
        AlertDialog(
            onDismissRequest = { gradeIdPendingDelete = null },
            title = { Text("¿Eliminar nota?", color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("Esta acción no se puede deshacer.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrade(subject.id, gradeId)
                        gradeIdPendingDelete = null
                    }
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gradeIdPendingDelete = null }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }
}

@Composable
private fun MissingSubjectState(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        UniIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Volver",
            onClick = onBackClick
        )
        Text("Materia no encontrada", color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
    onNewNoteClick: () -> Unit,
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
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface
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
                color = MaterialTheme.colorScheme.onSurface,
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
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "Opciones de materia",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            UniDropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar materia", color = MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = onEditClick
                )
                DropdownMenuItem(
                    text = { Text("Nueva nota", color = MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(Icons.Rounded.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = onNewNoteClick
                )
                onCompleteHistoryClick?.let { action ->
                    DropdownMenuItem(
                        text = { Text("Completar historial", color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = LocalSectionColors.current.atRisk)
                        },
                        onClick = action
                    )
                }
                // Borrar una materia se lleva por delante sus notas: la línea la separa de
                // las opciones que solo abren otra pantalla.
                UniDivider(Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("Eliminar materia", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
private fun CutHeader(title: String, subtitle: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
private fun CutChooser(
    cuts: List<GradingCut>,
    chosenCutId: String?,
    onChoose: (String) -> Unit
) {
    if (cuts.isEmpty()) {
        Text(
            "Todos los cortes están completos.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (chosenCutId == null) "¿En qué corte vas?" else "Las notas nuevas entran en",
            color = if (chosenCutId == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = if (chosenCutId == null) 15.sp else 13.sp,
            fontWeight = if (chosenCutId == null) FontWeight.ExtraBold else FontWeight.SemiBold
        )
        if (chosenCutId == null) {
            Text(
                "Elígelo para saber dónde entran tus notas y qué cortes ya pasaron.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
        // Elegir uno entre varios es un grupo conectado, igual que Horario/Calendario o
        // Materias/Tareas. Eran tres pastillas sueltas en una fila que rodaba.
        val ordered = cuts.sortedBy { it.order }
        UniSegmentedControl<String>(
            selected = chosenCutId.orEmpty(),
            options = ordered.map {
                UniSegmentedOption(value = it.id, label = cutDisplayName(it))
            },
            onSelected = onChoose,
            modifier = Modifier.fillMaxWidth()
        )
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
    // La tarjeta de arriba va rellena con el color de la app, no en gris sobre gris.
    // Es lo primero que se mira al abrir una materia y era del mismo tono que todo lo
    // demás; ahora pesa lo que le toca, igual que el hero de Inicio y la próxima clase.
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
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
                        (if (calculation.isFinished) "Nota final" else "Promedio de lo evaluado")
                            .uppercase(Locale.forLanguageTag("es")),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = SectionLabelStyle
                    )
                    if (average == null) {
                        Text(
                            "Sin evaluar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                GradingScaleUtils.formatGrade(average, scale),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displaySmallEmphasized,
                                // «Nota que sube»: solo cuando mejora. Bajar no es un logro, y
                                // marcarlo con un salto seria celebrarlo.
                                modifier = Modifier.promedioQueSube(average)
                            )
                            Text(
                                " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                        }
                    }
                }
                val indicatorStyle = LocalAppearancePreferences.current.academicIndicatorStyle
                when (indicatorStyle) {
                    AcademicIndicatorStyle.RINGS -> {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                            // El aro de fondo sale de `trackColor`, no de una segunda copia
                            // del indicador con el progreso al 100 %. Apilar dos era lo que
                            // había, y costaba el doble de nodos para dibujar una pista que
                            // el componente ya sabe pintar solo.
                            EvaluationRing(
                                fraction = evaluated / 100.0,
                                modifier = Modifier.fillMaxSize(),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                trackColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    .copy(alpha = 0.18f)
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
                                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                calculation.isFinished -> {
                    Text(
                        "Ya no queda nada por evaluar: esta es la nota definitiva.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Dónde puedes acabar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
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
                            RangeLegend("Mínimo", GradingScaleUtils.formatGrade(floor, scale), MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
                            RangeLegend("Meta", GradingScaleUtils.formatGrade(targetGrade, scale), MaterialTheme.colorScheme.onPrimaryContainer)
                            RangeLegend("Máximo", GradingScaleUtils.formatGrade(ceiling, scale), MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f))
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
    val bandColor = if (targetIsInside) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error

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
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
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
                .background(MaterialTheme.colorScheme.onPrimaryContainer)
        )
    }
}

@Composable
private fun RangeLegend(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f), fontSize = 11.sp, fontWeight = FontWeight.Medium)
        Text(value, color = valueColor, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun EvaluationValue(evaluated: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "${formatPercent(evaluated)}%",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "evaluado",
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.76f),
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
    // Los cuatro tonos se leen del tema aqui, en contexto composable, y la lambda solo
    // elige entre ellos: leer el tema dentro de la lambda la haria composable a ella.
    val toneUnknown = MaterialTheme.colorScheme.onSurfaceVariant
    val toneSecured = LocalSectionColors.current.onTrack
    val toneUnreachable = MaterialTheme.colorScheme.error
    val toneAtRisk = LocalSectionColors.current.atRisk
    val reachTone: (Double) -> Color = { threshold ->
        val floor = calculation.guaranteedMinimum
        val ceiling = calculation.bestPossible
        when {
            floor == null || ceiling == null -> toneUnknown
            floor >= threshold - 0.0001 -> toneSecured
            ceiling < threshold - 0.0001 -> toneUnreachable
            else -> toneAtRisk
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
    // Las mismas tarjetas que cuentan Materias, Hoy y Semana en Horario. Eran tres columnas
    // dentro de una banda separadas por dos líneas verticales, un recurso que no usa ninguna
    // otra pantalla; con la tarjeta, tres cifras se leen igual en toda la app.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CheckCircle,
            iconColor = passingTone,
            value = passingGrade,
            label = "Aprobación"
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Flag,
            iconColor = targetTone,
            value = targetGrade,
            label = "Objetivo"
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Percent,
            iconColor = MaterialTheme.colorScheme.tertiary,
            value = remainingPercentage,
            label = "Por evaluar"
        )
    }
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
        TargetOutlook.NO_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
        TargetOutlook.SECURED -> LocalSectionColors.current.onTrack
        TargetOutlook.ON_TRACK -> MaterialTheme.colorScheme.tertiary
        TargetOutlook.AT_RISK -> LocalSectionColors.current.atRisk
        TargetOutlook.UNREACHABLE -> MaterialTheme.colorScheme.error
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CutCard(
    summary: CutSummary,
    maxGrade: Double,
    scale: GradingScale,
    isActive: Boolean,
    needsHistory: Boolean,
    onClick: () -> Unit,
    dimmed: Boolean = false
) {
    val progress = (summary.evaluated / 100.0).coerceIn(0.0, 1.0)
    val accent = when {
        needsHistory -> LocalSectionColors.current.atRisk
        isActive -> MaterialTheme.colorScheme.primary
        else -> summary.status.color
    }
    // Un corte cerrado se apaga un poco: sigue ahí para consultarlo, pero ya no compite por la
    // atención con los que están en juego. Con contraste alto no se atenúa nada, que es lo que
    // esa preferencia viene a pedir.
    val highContrast = LocalAccessibilityPreferences.current.highContrastEnabled
    val cardAlpha = if (dimmed && !highContrast) 0.62f else 1f
    UniCard(
        modifier = Modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .bounceClick(onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
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
                                cutDisplayName(summary.cut),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            // La insignia dice siempre el estado del corte. Antes, en el corte
                            // activo la ocupaba «Corte actual», así que de ese corte —el único
                            // que importa ahora mismo— no se podía saber si estaba pendiente,
                            // en curso o completado.
                            if (needsHistory) {
                                CustomStatusBadge("Completar historial", LocalSectionColors.current.atRisk)
                            } else {
                                StatusBadge(status = summary.status)
                            }
                        }
                        Text(
                            buildString {
                                append("${formatPercent(summary.cut.weight * 100)}% de la materia")
                                if (isActive) append("  ·  Corte actual")
                            },
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (summary.average == null) {
                            Text(
                                "Sin evaluar",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                                )
                            }
                        }
                        Text(
                            "${formatPercent(summary.evaluated)}% evaluado",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        EvaluationBar(fraction = progress)
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
                                .background(accent.copy(alpha = 0.15f), MaterialTheme.shapes.small),
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun CutSummaryCard(
    summary: CutSummary,
    maxGrade: Double,
    scale: GradingScale
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
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
                        "NOTA DEL CORTE",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = SectionLabelStyle
                    )
                    if (summary.average == null) {
                        // Sin notas se escribía la raya de «sin dato» a 42sp y en el color del
                        // estado: una mancha ámbar del tamaño de una nota, que se leía como un
                        // valor y no como una ausencia.
                        Text(
                            "Sin evaluar",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                GradingScaleUtils.formatGrade(summary.average, scale),
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                style = MaterialTheme.typography.displaySmallEmphasized
                            )
                            Text(
                                " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(start = 2.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
                StatusBadge(status = summary.status)
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EvaluationBar(
                    fraction = (summary.evaluated / 100.0).coerceIn(0.0, 1.0),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
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
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.80f),
                    style = MaterialTheme.typography.bodySmall
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
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(grade.type.colorLocal().copy(alpha = 0.15f), MaterialTheme.shapes.small),
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                grade.contextLabel(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }
        Text(
            GradingScaleUtils.formatGrade(grade.value, scale),
            color = LocalSectionColors.current.onTrack,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box {
            UniIconButton(
                icon = Icons.Rounded.MoreVert,
                contentDescription = "Opciones de nota",
                onClick = { showMenu = true }
            )
            UniDropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                DropdownMenuItem(
                    text = { Text("Editar", color = MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { showMenu = false; onEditClick() }
                )
                UniDivider(Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = { showMenu = false; onDeleteClick() }
                )
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
private fun EmptyCutNotesInline() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LargeCardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.78f else 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, LargeCardShape)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text("Aún no hay notas", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            "Agrega una actividad para calcular este corte.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}


@Composable
private fun StatusBadge(status: CutStatus) {
    Box(
        modifier = Modifier
            .background(status.color, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            status.label.uppercase(Locale.forLanguageTag("es")),
            color = contentColorOn(status.color),
            style = SectionLabelStyle.copy(fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 0.5.sp)
        )
    }
}

@Composable
private fun CustomStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small)
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

private data class CutSummary(
    val cut: GradingCut,
    val grades: List<GradeItem>,
    val average: Double?,
    val evaluated: Double,
    val status: CutStatus
)

private enum class CutStatus(val label: String) {
    COMPLETED("Completado"),
    IN_PROGRESS("En curso"),
    PENDING("Pendiente")
}

private val CutStatus.color: Color
    @Composable get() = when (this) {
        CutStatus.COMPLETED -> LocalSectionColors.current.onTrack
        CutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        CutStatus.PENDING -> LocalSectionColors.current.atRisk
    }

private fun CutStatus.icon(): ImageVector {
    return when (this) {
        CutStatus.COMPLETED -> Icons.Rounded.CheckCircle
        CutStatus.IN_PROGRESS -> Icons.AutoMirrored.Rounded.Assignment
        CutStatus.PENDING -> Icons.AutoMirrored.Rounded.Assignment
    }
}

private fun GradingCut.toSummary(grades: List<GradeItem>): CutSummary {
    val calculation = GradeCalculator.calculateCut(grades)
    val evaluated = round(calculation.evaluatedFraction * 100.0 * 10.0) / 10.0
    val status = when {
        evaluated <= 0.001 -> CutStatus.PENDING
        evaluated >= 99.9 -> CutStatus.COMPLETED
        else -> CutStatus.IN_PROGRESS
    }
    return CutSummary(
        cut = this,
        grades = grades,
        average = calculation.average,
        evaluated = evaluated,
        status = status
    )
}

private fun cutDisplayName(cut: GradingCut): String = "Corte ${cut.order}"

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

@Composable
@ReadOnlyComposable
private fun GradeType.colorLocal(): Color {
    return when (this) {
        GradeType.WORKSHOP,
        GradeType.PRACTICE -> LocalSectionColors.current.onTrack
        GradeType.PRESENTATION -> MaterialTheme.colorScheme.primary
        GradeType.EXAM,
        GradeType.QUIZ -> LocalSectionColors.current.atRisk
        GradeType.PROJECT,
        GradeType.RESEARCH -> LocalSectionColors.current.schedule
        GradeType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}
