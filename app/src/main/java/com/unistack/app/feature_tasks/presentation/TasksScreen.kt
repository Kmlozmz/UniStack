@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_tasks.presentation

import androidx.annotation.StringRes
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.toPath
import androidx.compose.ui.graphics.drawscope.withTransform
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import com.unistack.app.feature_grades.presentation.formaDeMateria
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.FilaDeslizable
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.UniSearchField
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.celebracionDelDia
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.duracionDeDeshacer
import com.unistack.app.core.design.components.entradaDeLista
import com.unistack.app.core.design.components.latidoDeVencido
import com.unistack.app.core.design.components.reacomodoDeLista
import com.unistack.app.core.design.components.tachadoDe
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.anchoredButtonRoom
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.core.utils.Textos
import androidx.compose.foundation.border
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.isGradable
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.formatTaskDueText
import com.unistack.app.feature_tasks.domain.formatTaskDate
import com.unistack.app.feature_tasks.domain.formatTaskTime
import com.unistack.app.feature_user.domain.GradingScale
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Pantalla de Tareas rediseñada según la Propuesta D («La combinada»).
 *
 * Combina:
 * - El resumen tonal de la semana con el anillo de progreso ondulado animado ([OutcomeWaveRing]).
 * - La tira de 7 días ([SemanaTira]) con puntos de actividad por día y filtro directo al pulsar.
 * - El riel de materias ([RielMaterias]) con conteo por materia y punto de color.
 * - Agrupación por días (Ayer, Hoy, Mañana, fechas próximas, Más adelante y Hechas) con «Entregadas sin nota» fijada arriba.
 * - Fila ligera de tarea ([FilaTarea]) con casilla de esquinas suaves, tachado, metadata, progreso de subtareas y arrastre para borrar.
 * - Hoja de detalle ([HojaTarea]) con pospuesto en 1 toque ([Mañana | Próx. lunes | Elegir día]), fila de materia con salto directo a su detalle, checklist interactiva de subtareas y selector de evaluación.
 * - Hoja de nota ([HojaNota]) para registrar la calificación directamente en el corte de la materia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNewTaskClick: () -> Unit,
    onEditTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCompleteHistoryClick: (String) -> Unit = {},
    onSubjectClick: (String) -> Unit = {},
    viewModel: TasksViewModel = hiltViewModel(),
    embedded: Boolean = false
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    var taskIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(TaskListFilter.ALL) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<TaskDifficulty?>(null) }
    var sortOrder by remember { mutableStateOf(TaskSortOrder.DUE_DATE) }
    var showFiltersSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchExpanded by remember { mutableStateOf(false) }

    var selectedTaskIdForSheet by remember { mutableStateOf<String?>(null) }
    var taskForGradeSheetId by remember { mutableStateOf<String?>(null) }
    var taskForDatePickerId by remember { mutableStateOf<String?>(null) }
    var showDatePickerDialog by remember { mutableStateOf(false) }

    var completionPrompt by remember { mutableStateOf<TaskCompletionPrompt?>(null) }
    var historySuggestionSubjectId by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarGradeSaved = stringResource(R.string.tasks_snackbar_grade_saved)
    val undoLabel = stringResource(R.string.tasks_snackbar_undo)
    val msgAwaitingGrade = stringResource(R.string.tasks_pill_awaiting_grade)
    val msgDone = stringResource(R.string.tasks_pill_done).lowercase()
    val msgRevertPending = stringResource(R.string.tasks_action_revert_pending)
    val duracionDeDeshacer = duracionDeDeshacer()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clearSearchFocus = { focusManager.clearFocus() }

    var celebrando by remember { mutableStateOf(false) }
    var pendingCelebration by remember { mutableStateOf(false) }

    val onTaskChecked: (StudentTask, Boolean) -> Unit = { task, checked ->
        clearSearchFocus()
        val eraLaUltima = checked &&
            tasks.none {
                !it.completed && it.id != task.id &&
                    TaskDateUtils.fromMillis(it.dueDateMillis) <= TaskDateUtils.today()
            }
        val prompt = viewModel.setTaskCompleted(task.id, checked)
        completionPrompt = prompt
        if (eraLaUltima) {
            if (prompt != null) {
                pendingCelebration = true
            } else {
                celebrando = true
            }
        }

        coroutineScope.launch {
            val msg = if (checked) {
                if (task.gradingStatus == TaskGradingStatus.AWAITING_GRADE) {
                    "${task.title} · $msgAwaitingGrade"
                } else {
                    "${task.title} $msgDone"
                }
            } else {
                msgRevertPending
            }
            val result = snackbarHostState.showSnackbar(
                message = msg,
                actionLabel = undoLabel,
                duration = duracionDeDeshacer
            )
            if (result == SnackbarResult.ActionPerformed) {
                pendingCelebration = false
                celebrando = false
                if (checked) {
                    viewModel.revertTaskToPending(task.id)
                } else {
                    viewModel.setTaskCompleted(task.id, true)
                }
            }
        }
    }

    val clearFocusOnScroll = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                clearSearchFocus()
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(subjects) {
        if (selectedSubjectId != null && subjects.none { it.id == selectedSubjectId }) {
            selectedSubjectId = null
        }
    }

    val today = remember { TaskDateUtils.today() }
    val monday = remember(today) { today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)) }
    val sunday = remember(monday) { monday.plusDays(6) }

    val filteredTasks = remember(tasks, subjects, selectedFilter, selectedSubjectId, selectedPriority, sortOrder, searchQuery) {
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        tasks
            .filter { task -> selectedFilter.matches(task) }
            .filter { task -> selectedSubjectId == null || task.subjectId == selectedSubjectId }
            .filter { task -> selectedPriority == null || task.difficulty == selectedPriority }
            .filter { task ->
                if (query.isEmpty()) {
                    true
                } else {
                    val subjectName = task.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name }.orEmpty()
                    listOf(task.title, subjectName)
                        .any { it.lowercase(Locale.ROOT).contains(query) }
                }
            }
            .sortFor(sortOrder, subjects)
    }

    val weekTasks = remember(tasks, selectedSubjectId, monday, sunday) {
        tasks.filter { task ->
            val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
            !dueDate.isBefore(monday) && !dueDate.isAfter(sunday) &&
                (selectedSubjectId == null || task.subjectId == selectedSubjectId)
        }
    }
    val weekPending = weekTasks.filterNot { it.completed }
    val weekDone = weekTasks.filter { it.completed }
    val weekOverdue = weekPending.filter { TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today) }
    val weekEstimatedMinutes = weekPending.sumOf { it.estimatedMinutes }

    val awaitingGradeTasks = filteredTasks.filter {
        it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
    }.sortedByDescending { it.completedAt ?: it.updatedAt }

    val activeTasks = filteredTasks.filterNot { it.completed }
    val completedTasks = filteredTasks.filter {
        it.completed && it.gradingStatus != TaskGradingStatus.AWAITING_GRADE
    }

    val overdueTasks = activeTasks.filter {
        TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today)
    }
    val todayTasks = activeTasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == today }
    val tomorrowTasks = activeTasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == today.plusDays(1) }

    val futureDaysTasks = remember(activeTasks, today) {
        (2..6).mapNotNull { offset ->
            val date = today.plusDays(offset.toLong())
            val dayTasks = activeTasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == date }
            if (dayTasks.isNotEmpty()) date to dayTasks else null
        }
    }

    val laterTasks = activeTasks.filter {
        TaskDateUtils.fromMillis(it.dueDateMillis).isAfter(today.plusDays(6))
    }

    val pendingGradeCount = tasks.count {
        it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
    }

    val selectedSubjectName = subjects.firstOrNull { it.id == selectedSubjectId }?.name
    val selectedTaskForSheet = tasks.firstOrNull { it.id == selectedTaskIdForSheet }
    val taskForGradeSheet = tasks.firstOrNull { it.id == taskForGradeSheetId }
    val taskForDatePicker = tasks.firstOrNull { it.id == taskForDatePickerId }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .celebracionDelDia(
                disparada = celebrando,
                onTerminada = { celebrando = false },
                mensaje = stringResource(R.string.celebration_all_done)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(clearFocusOnScroll),
            contentPadding = PaddingValues(
                start = 18.dp,
                top = if (embedded) 10.dp else 58.dp,
                end = 18.dp,
                bottom = scrollBottomRoom + anchoredButtonRoom
            ),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (!embedded) {
                item {
                    TasksHeader(
                        monday = monday,
                        sunday = sunday,
                        onToggleSearch = { isSearchExpanded = !isSearchExpanded }
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                }
            }

            if (isSearchExpanded || searchQuery.isNotEmpty()) {
                item {
                    TaskSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchDone = clearSearchFocus
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            if (tasks.isNotEmpty()) {
                item {
                    TasksWeekHero(
                        pendingCount = weekPending.size,
                        overdueCount = weekOverdue.size,
                        doneCount = weekDone.size,
                        totalCount = weekTasks.size,
                        awaitingGradeCount = pendingGradeCount,
                        todayPendingCount = todayTasks.size
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                item {
                    TaskFilterSummaryChip(
                        selectedStatus = selectedFilter,
                        selectedSubjectName = selectedSubjectName,
                        selectedPriority = selectedPriority,
                        sortOrder = sortOrder,
                        onClick = {
                            clearSearchFocus()
                            showFiltersSheet = true
                        }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            when {
                tasks.isEmpty() -> item {
                    TasksEmptyState()
                }
                filteredTasks.isEmpty() -> item {
                    FilteredEmptyState(onOpenFilters = { showFiltersSheet = true })
                }
                else -> {
                    // Pinned Entregadas sin nota
                    if (awaitingGradeTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_submitted_waiting),
                                count = awaitingGradeTasks.size
                            )
                        }
                        itemsIndexed(awaitingGradeTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Vencidas
                    if (overdueTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_overdue),
                                count = overdueTasks.size,
                                isOverdue = true
                            )
                        }
                        itemsIndexed(overdueTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Hoy
                    if (todayTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_today),
                                count = todayTasks.size
                            )
                        }
                        itemsIndexed(todayTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Mañana
                    if (tomorrowTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_tomorrow),
                                count = tomorrowTasks.size
                            )
                        }
                        itemsIndexed(tomorrowTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Días futuros de la semana
                    futureDaysTasks.forEach { (date, dayTasks) ->
                        item {
                            SectionTitle(
                                text = formatFutureDayTitle(date),
                                count = dayTasks.size
                            )
                        }
                        itemsIndexed(dayTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Más adelante
                    if (laterTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_later),
                                count = laterTasks.size
                            )
                        }
                        itemsIndexed(laterTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }

                    // Hechas
                    if (completedTasks.isNotEmpty()) {
                        item {
                            SectionTitle(
                                text = stringResource(R.string.tasks_group_done),
                                count = completedTasks.size
                            )
                        }
                        itemsIndexed(completedTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskRowItem(
                                indice = indice,
                                task = task,
                                subjects = subjects,
                                today = today,
                                onCheckedChange = { onTaskChecked(task, it) },
                                onClick = {
                                    clearSearchFocus()
                                    selectedTaskIdForSheet = task.id
                                },
                                onDelete = { taskIdPendingDelete = task.id }
                            )
                        }
                    }
                }
            }
        }

        if (!embedded) {
            NewTaskFab(
                onClick = {
                    clearSearchFocus()
                    onNewTaskClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 20.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 92.dp)
        )
    }

    // Hoja de Tarea (Detalle)
    selectedTaskForSheet?.let { task ->
        val subject = subjects.firstOrNull { it.id == task.subjectId }
        HojaTarea(
            task = task,
            subject = subject,
            gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
            onDismiss = { selectedTaskIdForSheet = null },
            onSubjectClick = { subjectId ->
                selectedTaskIdForSheet = null
                onSubjectClick(subjectId)
            },
            onEditTaskClick = { taskId ->
                selectedTaskIdForSheet = null
                onEditTaskClick(taskId)
            },
            onDuplicateTask = { taskId ->
                val copy = viewModel.duplicateTask(taskId)
                selectedTaskIdForSheet = null
                if (copy != null) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar(
                            message = Textos.get(R.string.tasks_snackbar_duplicated, copy.title),
                            duration = duracionDeDeshacer
                        )
                    }
                }
            },
            onDeleteTask = { taskId ->
                selectedTaskIdForSheet = null
                taskIdPendingDelete = taskId
            },
            onToggleSubtask = { taskId, subtaskId ->
                viewModel.toggleSubtask(taskId, subtaskId)
            },
            onAddSubtask = { taskId, title ->
                viewModel.addSubtask(taskId, title)
            },
            onDeleteSubtask = { taskId, subtaskId ->
                viewModel.deleteSubtask(taskId, subtaskId)
            },
            onPostponeTomorrow = { taskId ->
                val oldDueDateMillis = task.dueDateMillis
                viewModel.postponeTaskToTomorrow(taskId)
                coroutineScope.launch {
                    val res = snackbarHostState.showSnackbar(
                        message = "Ahora vence mañana",
                        actionLabel = undoLabel,
                        duration = duracionDeDeshacer
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        viewModel.postponeTaskToDate(taskId, TaskDateUtils.fromMillis(oldDueDateMillis))
                    }
                }
            },
            onPostponeNextMonday = { taskId ->
                val oldDueDateMillis = task.dueDateMillis
                viewModel.postponeTaskToNextMonday(taskId)
                coroutineScope.launch {
                    val res = snackbarHostState.showSnackbar(
                        message = "Ahora vence el próximo lunes",
                        actionLabel = undoLabel,
                        duration = duracionDeDeshacer
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        viewModel.postponeTaskToDate(taskId, TaskDateUtils.fromMillis(oldDueDateMillis))
                    }
                }
            },
            onOpenDatePicker = { taskId ->
                taskForDatePickerId = taskId
                showDatePickerDialog = true
            },
            onOpenGradeSheet = {
                selectedTaskIdForSheet = null
                taskForGradeSheetId = it.id
            },
            onNoGradeClick = { taskId ->
                viewModel.markTaskAsNotGraded(taskId)
            },
            onUnlinkGradeClick = { taskId ->
                viewModel.unlinkTaskGrade(taskId)
            },
            onSetGradingDecision = { taskId, willAwait ->
                viewModel.setTaskGradingDecision(taskId, willAwait)
            },
            onToggleComplete = { t ->
                onTaskChecked(t, !t.completed)
            }
        )
    }

    // Hoja de Nota
    taskForGradeSheet?.let { task ->
        val subject = subjects.firstOrNull { it.id == task.subjectId }
        if (subject != null) {
            HojaNota(
                task = task,
                subject = subject,
                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                onDismiss = { taskForGradeSheetId = null },
                onSaveGrade = { value, percentage, cutId ->
                    val outcome = viewModel.saveTaskGrade(
                        taskId = task.id,
                        value = value,
                        percentageInput = percentage,
                        cutId = cutId
                    )
                    if (outcome.saved) {
                        taskForGradeSheetId = null
                        val gradeId = outcome.gradeId
                        if (gradeId != null) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = snackbarGradeSaved,
                                    actionLabel = undoLabel,
                                    duration = duracionDeDeshacer
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoTaskGrade(task.id, gradeId)
                                }
                            }
                        }
                        if (outcome.suggestPriorHistory) {
                            historySuggestionSubjectId = outcome.subjectId
                        }
                    }
                    outcome.saved
                }
            )
        } else {
            taskForGradeSheetId = null
        }
    }

    // Date picker dialog
    if (showDatePickerDialog && taskForDatePicker != null) {
        val initialDate = TaskDateUtils.fromMillis(taskForDatePicker.dueDateMillis)
        UniDatePickerDialog(
            selectedDate = initialDate,
            onDateSelected = { newDate ->
                val oldDueDateMillis = taskForDatePicker.dueDateMillis
                viewModel.postponeTaskToDate(taskForDatePicker.id, newDate)
                showDatePickerDialog = false
                taskForDatePickerId = null
                coroutineScope.launch {
                    val res = snackbarHostState.showSnackbar(
                        message = "Ahora vence el ${newDate.format(shortDateFormatter)}",
                        actionLabel = undoLabel,
                        duration = duracionDeDeshacer
                    )
                    if (res == SnackbarResult.ActionPerformed) {
                        viewModel.postponeTaskToDate(taskForDatePicker.id, TaskDateUtils.fromMillis(oldDueDateMillis))
                    }
                }
            },
            onDismiss = {
                showDatePickerDialog = false
                taskForDatePickerId = null
            }
        )
    }

    if (showFiltersSheet) {
        TasksFilterBottomSheet(
            subjects = subjects,
            tasks = tasks,
            selectedStatus = selectedFilter,
            selectedSubjectId = selectedSubjectId,
            selectedPriority = selectedPriority,
            sortOrder = sortOrder,
            onStatusSelected = { selectedFilter = it },
            onSubjectSelected = { selectedSubjectId = it },
            onPrioritySelected = { selectedPriority = it },
            onSortSelected = { sortOrder = it },
            onClear = {
                selectedFilter = TaskListFilter.ALL
                selectedSubjectId = null
                selectedPriority = null
                sortOrder = TaskSortOrder.DUE_DATE
            },
            onDismiss = { showFiltersSheet = false }
        )
    }

    completionPrompt?.let { prompt ->
        val subject = subjects.firstOrNull { it.id == prompt.task.subjectId }
        if (subject == null) {
            completionPrompt = null
            if (pendingCelebration) {
                celebrando = true
                pendingCelebration = false
            }
        } else {
            TaskGradeResultSheet(
                task = prompt.task,
                subject = subject,
                onDismiss = {
                    viewModel.markTaskAwaitingGrade(prompt.task.id)
                    completionPrompt = null
                    if (pendingCelebration) {
                        celebrando = true
                        pendingCelebration = false
                    }
                },
                onNoGrade = {
                    viewModel.markTaskAsNotGraded(prompt.task.id)
                    completionPrompt = null
                    if (pendingCelebration) {
                        celebrando = true
                        pendingCelebration = false
                    }
                },
                onSaveGrade = { value, percentage, cutId ->
                    val outcome = viewModel.saveTaskGrade(
                        taskId = prompt.task.id,
                        value = value,
                        percentageInput = percentage,
                        cutId = cutId
                    )
                    if (outcome.saved) {
                        completionPrompt = null
                        if (pendingCelebration) {
                            celebrando = true
                            pendingCelebration = false
                        }
                        val gradeId = outcome.gradeId
                        if (gradeId != null) {
                            coroutineScope.launch {
                                val result = snackbarHostState.showSnackbar(
                                    message = snackbarGradeSaved,
                                    actionLabel = undoLabel,
                                    duration = duracionDeDeshacer
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    viewModel.undoTaskGrade(prompt.task.id, gradeId)
                                }
                            }
                        }
                        if (outcome.suggestPriorHistory) {
                            historySuggestionSubjectId = outcome.subjectId
                        }
                    }
                    outcome.saved
                }
            )
        }
    }

    historySuggestionSubjectId?.let { subjectId ->
        val subject = subjects.firstOrNull { it.id == subjectId }
        AlertDialog(
            onDismissRequest = {
                viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                historySuggestionSubjectId = null
            },
            title = { Text(stringResource(R.string.tasks_history_prompt_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.tasks_history_prompt_desc_1,
                        subject?.name ?: stringResource(R.string.tasks_field_subject)
                    ) + " " + stringResource(R.string.tasks_history_prompt_desc_2)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        historySuggestionSubjectId = null
                        onCompleteHistoryClick(subjectId)
                    }
                ) { Text(stringResource(R.string.grade_complete_history_button)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        historySuggestionSubjectId = null
                    }
                ) { Text(stringResource(R.string.grade_complete_history_later)) }
            }
        )
    }

    taskIdPendingDelete?.let { taskId ->
        UniConfirmDeleteDialog(
            title = stringResource(R.string.tasks_delete_dialog_title),
            body = stringResource(R.string.tasks_delete_dialog_body),
            onConfirm = {
                viewModel.deleteTask(taskId)
                taskIdPendingDelete = null
            },
            onDismiss = { taskIdPendingDelete = null }
        )
    }
}

@Composable
private fun TasksHeader(
    monday: LocalDate,
    sunday: LocalDate,
    onToggleSearch: () -> Unit
) {
    val monthName = monday.format(DateTimeFormatter.ofPattern("MMMM", Locale.getDefault()))
    val isDark = LocalIsDarkTheme.current
    val searchBtnBg = if (isDark) Color(0xFF1C222D) else MaterialTheme.colorScheme.surfaceContainerHigh
    val searchBtnBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val searchBtnTint = if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.tasks_header_title),
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Semana del ${monday.dayOfMonth} al ${sunday.dayOfMonth} de $monthName",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
        Surface(
            onClick = onToggleSearch,
            shape = CircleShape,
            color = searchBtnBg,
            border = searchBtnBorder,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.tasks_search_field_placeholder),
                    tint = searchBtnTint,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Hero tonal de «Esta semana» con cifra principal, aviso de vencidas y el anillo ondulado.
 * Propuesta D: #6B4E00 / #FFE29E cuando hay vencidas, #2E2A6B / #E2DFFF cuando no.
 * Forma de 30dp, píldoras con blanco al 14%, anillo y tipografía 1:1.
 */
@Composable
private fun TasksWeekHero(
    pendingCount: Int,
    overdueCount: Int,
    doneCount: Int,
    totalCount: Int,
    awaitingGradeCount: Int,
    todayPendingCount: Int,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val hasOverdue = overdueCount > 0
    val containerColor = when {
        hasOverdue -> if (isDark) Color(0xFF6B4E00) else LocalSectionColors.current.atRiskContainer
        else -> if (isDark) Color(0xFF2E2A6B) else MaterialTheme.colorScheme.primaryContainer
    }
    val contentColor = when {
        hasOverdue -> if (isDark) Color(0xFFFFE29E) else LocalSectionColors.current.onAtRiskContainer
        else -> if (isDark) Color(0xFFE2DFFF) else MaterialTheme.colorScheme.onPrimaryContainer
    }
    val pillBg = if (isDark) Color.White.copy(alpha = 0.14f) else contentColor.copy(alpha = 0.10f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.tasks_hero_this_week).uppercase(Locale.getDefault()),
                fontSize = 10.5.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.4.sp,
                color = contentColor.copy(alpha = 0.8f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$pendingCount",
                            fontSize = 46.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-0.045).em,
                            lineHeight = 46.sp,
                            color = contentColor
                        )
                        Text(
                            text = stringResource(R.string.tasks_hero_to_do),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.01).em,
                            color = contentColor
                        )
                    }
                    Text(
                        text = when {
                            overdueCount == 1 -> stringResource(R.string.tasks_hero_overdue_singular, 1)
                            overdueCount > 1 -> stringResource(R.string.tasks_hero_overdue_plural, overdueCount)
                            pendingCount > 0 -> stringResource(R.string.tasks_hero_nothing_overdue)
                            else -> stringResource(R.string.tasks_hero_week_closed)
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }

                OutcomeWaveRing(
                    progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f,
                    doneCount = doneCount,
                    totalCount = totalCount,
                    color = contentColor
                )
            }

            if (awaitingGradeCount > 0 || todayPendingCount > 0 || (todayPendingCount == 0 && pendingCount > 0)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (awaitingGradeCount > 0) {
                        Surface(
                            shape = CircleShape,
                            color = pillBg
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_hero_awaiting_grade, awaitingGradeCount),
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = contentColor
                            )
                        }
                    }
                    val todayText = if (todayPendingCount > 0) {
                        stringResource(R.string.tasks_hero_due_today, todayPendingCount)
                    } else {
                        stringResource(R.string.tasks_hero_today_clear)
                    }
                    Surface(
                        shape = CircleShape,
                        color = pillBg
                    ) {
                        Text(
                            text = todayText,
                            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    }
                }
            }
        }
    }
}

/**
 * Anillo de progreso ondulado sinusoidal animado.
 * Formula de Propuesta D: r(θ) = R + A * sin(12 * θ) con A = 1.2 + 2.4 * p.
 */
@Composable
fun OutcomeWaveRing(
    progress: Float,
    doneCount: Int,
    totalCount: Int,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFE2DFFF)
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "OutcomeWaveRing"
    )

    Box(
        modifier = modifier.size(88.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val r = 34.dp.toPx()
            val amp = 1.2f.dp.toPx() + 2.4f.dp.toPx() * animatedProgress
            val ondas = 12
            val steps = 220
            val strokeWidth = 6.dp.toPx()

            fun computePoint(t: Float): Offset {
                val a = (t * 2.0 * Math.PI - Math.PI / 2.0).toFloat()
                val rr = r + sin(t * 2.0 * Math.PI * ondas).toFloat() * amp
                return Offset(cx + cos(a) * rr, cy + sin(a) * rr)
            }

            val pathTrack = Path().apply {
                for (i in 0..steps) {
                    val pt = computePoint(i.toFloat() / steps)
                    if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                }
            }
            drawPath(
                path = pathTrack,
                color = color.copy(alpha = 0.22f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val hasta = (steps * animatedProgress).roundToInt()
            if (hasta >= 2) {
                val pathActive = Path().apply {
                    for (i in 0..hasta) {
                        val pt = computePoint(i.toFloat() / steps)
                        if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                    }
                }
                drawPath(
                    path = pathActive,
                    color = color,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$doneCount/$totalCount",
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color,
                lineHeight = 15.sp
            )
            Text(
                text = stringResource(R.string.tasks_hero_hechas),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = color.copy(alpha = 0.75f),
                letterSpacing = 0.1.sp
            )
        }
    }
}




/**
 * Fila de tarea de la Propuesta D envuelta en [FilaDeslizable] para borrar.
 * Si es «Entregadas sin nota» renderiza [TarjetaAwaitingGrade] (tarjetaA con acento-c y píldora blanca).
 * Si es cualquier otra tarea renderiza [FilaTarea] (filaBC transparente sobre #0A0C11 con divisor fino).
 */
@Composable
private fun LazyItemScope.TaskRowItem(
    indice: Int,
    task: StudentTask,
    subjects: List<Subject>,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val subject = task.subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val awaitingGrade = task.completed && task.gradingStatus == TaskGradingStatus.AWAITING_GRADE

    if (awaitingGrade) {
        FilaDeslizable(
            onBorrar = onDelete,
            shape = RoundedCornerShape(20.dp),
            modifier = modifier
                .then(reacomodoDeLista())
                .entradaDeLista(indice)
                .padding(bottom = 8.dp)
        ) {
            TarjetaAwaitingGrade(
                task = task,
                subject = subject,
                onCheckedChange = onCheckedChange,
                onClick = onClick
            )
        }
    } else {
        FilaDeslizable(
            onBorrar = onDelete,
            shape = RoundedCornerShape(18.dp),
            modifier = modifier
                .then(reacomodoDeLista())
                .entradaDeLista(indice)
                .latidoDeVencido(activo = isOverdue)
                .padding(bottom = 8.dp)
        ) {
            FilaTarea(
                task = task,
                subject = subject,
                today = today,
                onCheckedChange = onCheckedChange,
                onClick = onClick
            )
        }
    }
}

/**
 * Tarjeta destacada exclusivamente para «Entregadas sin nota» (tarjetaA).
 * Contenedor tonal acento-c (#2E2A6B), esquinas 20dp, píldora blanca semitransparente (#FFFFFF al 14%)
 * y punto de prioridad.
 */
@Composable
private fun TarjetaAwaitingGrade(
    task: StudentTask,
    subject: Subject?,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val cardBg = if (isDark) Color(0xFF2E2A6B) else MaterialTheme.colorScheme.secondaryContainer
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val checkmarkBoxBg = if (isDark) Color(0xFF7F77DD) else MaterialTheme.colorScheme.primary
    val checkmarkTint = if (isDark) Color(0xFF171040) else MaterialTheme.colorScheme.onPrimary
    val titleColor = if (isDark) Color(0xFFE8EBF3) else MaterialTheme.colorScheme.onSecondaryContainer
    val subtitleColor = if (isDark) Color(0xFFE2DFFF).copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
    val pillBg = if (isDark) Color.White.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
    val pillTextColor = if (isDark) Color(0xFFE2DFFF) else MaterialTheme.colorScheme.onSecondaryContainer
    val progressTrack = if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
    val progressFill = if (isDark) Color(0xFFE2DFFF) else MaterialTheme.colorScheme.primary
    val progressText = if (isDark) Color(0xFFE2DFFF).copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSecondaryContainer

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cleanClickable(shape = RoundedCornerShape(20.dp), onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = cardBg,
        border = cardBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 13.dp, end = 14.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = checkmarkBoxBg,
                border = BorderStroke(2.dp, checkmarkBoxBg),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(22.dp)
                    .cleanClickable(shape = RoundedCornerShape(6.dp)) { onCheckedChange(false) }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = checkmarkTint,
                    modifier = Modifier.padding(2.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.01).em,
                    color = titleColor,
                    lineHeight = 18.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (subject != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(subjectAccent(subject))
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    val subtitleText = buildString {
                        append(subject?.name ?: stringResource(R.string.tasks_no_subject))
                        append(" · ")
                        append(task.type.label())
                        if (task.estimatedMinutes > 0) {
                            append(" · ")
                            append(TaskDateUtils.estimatedTimeText(task.estimatedMinutes))
                        }
                    }
                    Text(
                        text = subtitleText,
                        fontSize = 11.5.sp,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (task.subtasks.isNotEmpty()) {
                        val doneCount = task.subtasks.count { it.isCompleted }
                        val fraction = (doneCount.toFloat() / task.subtasks.size).coerceIn(0f, 1f)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(progressTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .clip(CircleShape)
                                    .background(progressFill)
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$doneCount/${task.subtasks.size}",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = progressText,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = pillBg,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Text(
                        text = stringResource(R.string.tasks_pill_awaiting_grade),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = pillTextColor,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(task.difficulty.color())
                )
            }
        }
    }
}

/**
 * Tarjeta individual para tareas (Propuesta D con separación y tarjeta individual).
 * Fondo #171C24, esquinas 18dp, casilla cuadrada 6dp, subtítulo fluido y columna de estado y prioridad a la derecha.
 */
@Composable
private fun FilaTarea(
    task: StudentTask,
    subject: Subject?,
    today: LocalDate,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val isToday = !task.completed && dueDate == today
    val isGraded = task.gradingStatus == TaskGradingStatus.GRADED

    val cardBg = if (isDark) Color(0xFF171C24) else MaterialTheme.colorScheme.surfaceContainerLow
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val checkboxCheckedBg = if (isDark) Color(0xFF7F77DD) else MaterialTheme.colorScheme.primary
    val checkboxUncheckedBorder = if (isDark) Color(0xFF6C7689) else MaterialTheme.colorScheme.outline
    val checkmarkTint = if (isDark) Color(0xFF171040) else MaterialTheme.colorScheme.onPrimary

    val titleBase = if (isDark) Color(0xFFE8EBF3) else MaterialTheme.colorScheme.onSurface
    val titleColor = if (task.completed) titleBase.copy(alpha = 0.5f) else titleBase
    val subtitleBase = if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant
    val subtitleColor = if (task.completed) subtitleBase.copy(alpha = 0.55f) else subtitleBase
    val progressTrack = if (isDark) Color(0xFF232B37) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val progressFill = if (isDark) Color(0xFF7F77DD) else MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cleanClickable(shape = RoundedCornerShape(18.dp), onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = cardBg,
        border = cardBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 12.dp, top = 13.dp, end = 14.dp, bottom = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Casilla de M3 cuadrada suave (6dp)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (task.completed) checkboxCheckedBg else Color.Transparent,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (task.completed) checkboxCheckedBg else checkboxUncheckedBorder
                ),
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
                modifier = Modifier
                    .size(22.dp)
                    .cleanClickable(shape = RoundedCornerShape(6.dp)) { onCheckedChange(!task.completed) }
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = checkmarkTint,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    fontSize = 14.5.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.01).em,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.tachadoDe(
                        completado = task.completed,
                        color = titleBase.copy(alpha = 0.6f)
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (subject != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(subjectAccent(subject))
                        )
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    val subtitleText = buildString {
                        if (subject != null) {
                            append(subject.name)
                            append(" · ")
                        } else {
                            append(stringResource(R.string.tasks_no_subject))
                            append(" · ")
                        }
                        append(task.type.label())
                        if (task.estimatedMinutes > 0) {
                            append(" · ")
                            append(TaskDateUtils.estimatedTimeText(task.estimatedMinutes))
                        }
                    }
                    Text(
                        text = subtitleText,
                        fontSize = 11.5.sp,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (task.subtasks.isNotEmpty()) {
                        val doneCount = task.subtasks.count { it.isCompleted }
                        val fraction = (doneCount.toFloat() / task.subtasks.size).coerceIn(0f, 1f)
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(progressTrack)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(fraction)
                                    .clip(CircleShape)
                                    .background(progressFill)
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$doneCount/${task.subtasks.size}",
                            fontSize = 10.5.sp,
                            fontFamily = FontFamily.Monospace,
                            color = subtitleColor,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Trailing column: Pill on top, priority dot below
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                when {
                    isGraded -> {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.onTrackContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_detail_graded_label),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocalSectionColors.current.onOnTrackContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    task.completed -> {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.onTrackContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_pill_done),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocalSectionColors.current.onOnTrackContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    isOverdue -> {
                        val diffDays = ChronoUnit.DAYS.between(dueDate, today)
                        val text = when (diffDays) {
                            1L -> stringResource(R.string.due_overdue_yesterday)
                            else -> stringResource(R.string.due_overdue_days_ago, diffDays)
                        }
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.expensesContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = text,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = LocalSectionColors.current.onExpensesContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    isToday -> {
                        val timeStr = if (TaskDateUtils.hasExplicitTime(task.dueDateMillis)) {
                            " " + formatTaskTime(TaskDateUtils.timeFromMillis(task.dueDateMillis))
                        } else ""
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) Color(0xFF6B4E00) else LocalSectionColors.current.atRiskContainer,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_due_today) + timeStr,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDark) Color(0xFFFFE29E) else LocalSectionColors.current.onAtRiskContainer,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    else -> {
                        val diffDays = ChronoUnit.DAYS.between(today, dueDate)
                        val timeStr = if (TaskDateUtils.hasExplicitTime(task.dueDateMillis)) {
                            " " + formatTaskTime(TaskDateUtils.timeFromMillis(task.dueDateMillis))
                        } else ""
                        val text = when {
                            diffDays == 1L -> "Mañana$timeStr"
                            diffDays in 2L..6L -> {
                                val dayName = dueDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).replaceFirstChar { it.uppercase() }
                                "$dayName$timeStr"
                            }
                            else -> {
                                val monthName = dueDate.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                                "${dueDate.dayOfMonth} $monthName$timeStr"
                            }
                        }
                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                if (!task.completed) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(task.difficulty.color())
                    )
                }
            }
        }
    }
}

/**
 * Hoja modal de detalle de la tarea (HojaTarea de Propuesta D).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaTarea(
    task: StudentTask,
    subject: Subject?,
    gradingScale: GradingScale,
    onDismiss: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onEditTaskClick: (String) -> Unit,
    onDuplicateTask: (String) -> Unit,
    onDeleteTask: (String) -> Unit,
    onToggleSubtask: (String, String) -> Unit,
    onAddSubtask: (String, String) -> Unit,
    onDeleteSubtask: (String, String) -> Unit,
    onPostponeTomorrow: (String) -> Unit,
    onPostponeNextMonday: (String) -> Unit,
    onOpenDatePicker: (String) -> Unit,
    onOpenGradeSheet: (StudentTask) -> Unit,
    onNoGradeClick: (String) -> Unit,
    onUnlinkGradeClick: (String) -> Unit,
    onSetGradingDecision: (String, Boolean) -> Unit,
    onToggleComplete: (StudentTask) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newSubtaskText by remember { mutableStateOf("") }
    val today = remember { TaskDateUtils.today() }
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val isToday = !task.completed && dueDate == today

    val isDark = LocalIsDarkTheme.current
    val sheetContainerColor = if (isDark) Color(0xFF0D1017) else MaterialTheme.colorScheme.surfaceContainerLow
    val cardBg = if (isDark) Color(0xFF171C24) else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val textPrimary = if (isDark) Color(0xFFE8EBF3) else MaterialTheme.colorScheme.onSurface
    val textSubtle = if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant
    val metaPillBg = if (isDark) Color(0xFF1C222D) else MaterialTheme.colorScheme.surfaceContainerHigh
    val metaPillBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: icono tipo, título, metadatos y botón "Abrir entera"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                val taskIcon = taskTypeIcon(task.type)
                val typeColor = subject?.let { subjectAccent(it) } ?: Color(0xFF7F77DD)
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(typeColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = taskIcon,
                        contentDescription = null,
                        tint = typeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = metaPillBg,
                            border = metaPillBorder,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Text(
                                text = task.type.label(),
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = textSubtle
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = metaPillBg,
                            border = metaPillBorder,
                            tonalElevation = 0.dp,
                            shadowElevation = 0.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(task.difficulty.color())
                                )
                                Text(
                                    text = task.difficulty.shortLabel(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                        if (task.estimatedMinutes > 0) {
                            val durationText = formatEstimatedDuration(task.estimatedMinutes)
                            Surface(
                                shape = CircleShape,
                                color = metaPillBg,
                                border = metaPillBorder,
                                tonalElevation = 0.dp,
                                shadowElevation = 0.dp
                            ) {
                                Text(
                                    text = durationText,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSubtle
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        onDismiss()
                        onEditTaskClick(task.id)
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                        contentDescription = stringResource(R.string.tasks_detail_open_full),
                        tint = Color(0xFF7F77DD)
                    )
                }
            }

            // Bloque de Estado (Con botones de pospuesto rápido si está pendiente)
            val estadoColor = when {
                task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> if (isDark) Color(0xFF0A5C23) else LocalSectionColors.current.onTrackContainer
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> if (isDark) Color(0xFF2E2A6B) else MaterialTheme.colorScheme.secondaryContainer
                isOverdue -> if (isDark) Color(0xFF8C1F0A) else LocalSectionColors.current.expensesContainer
                isToday -> if (isDark) Color(0xFF6B4E00) else LocalSectionColors.current.atRiskContainer
                else -> if (isDark) Color(0xFF1C222D) else MaterialTheme.colorScheme.surfaceContainerHigh
            }
            val estadoTextColor = when {
                task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> if (isDark) Color(0xFFB4F2C4) else LocalSectionColors.current.onOnTrackContainer
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> if (isDark) Color(0xFFE2DFFF) else MaterialTheme.colorScheme.onSecondaryContainer
                isOverdue -> if (isDark) Color(0xFFFFDCD5) else LocalSectionColors.current.onExpensesContainer
                isToday -> if (isDark) Color(0xFFFFE29E) else LocalSectionColors.current.onAtRiskContainer
                else -> if (isDark) Color(0xFFE8EBF3) else MaterialTheme.colorScheme.onSurface
            }
            val estadoSubColor = when {
                task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> if (isDark) Color(0xFFB4F2C4).copy(alpha = 0.85f) else LocalSectionColors.current.onOnTrackContainer.copy(alpha = 0.85f)
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> if (isDark) Color(0xFFE2DFFF).copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                isOverdue -> if (isDark) Color(0xFFFFDCD5).copy(alpha = 0.85f) else LocalSectionColors.current.onExpensesContainer.copy(alpha = 0.85f)
                isToday -> if (isDark) Color(0xFFFFE29E).copy(alpha = 0.85f) else LocalSectionColors.current.onAtRiskContainer.copy(alpha = 0.85f)
                else -> if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = estadoColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rotulo = when {
                        task.gradingStatus == TaskGradingStatus.GRADED -> stringResource(R.string.tasks_detail_graded_label)
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_submitted_label)
                        task.completed -> stringResource(R.string.tasks_detail_done_label)
                        isOverdue -> stringResource(R.string.tasks_detail_overdue_label)
                        else -> stringResource(R.string.tasks_detail_due_label)
                    }
                    Text(
                        text = rotulo.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = estadoSubColor
                    )

                    val diff = remember(dueDate, today) { ChronoUnit.DAYS.between(today, dueDate) }
                    val hasTime = remember(task.dueDateMillis) { TaskDateUtils.hasExplicitTime(task.dueDateMillis) }
                    val time = remember(task.dueDateMillis) { TaskDateUtils.timeFromMillis(task.dueDateMillis) }

                    val grande = when {
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_pill_awaiting_grade)
                        task.gradingStatus == TaskGradingStatus.GRADED -> {
                            val grade = subject?.grades?.firstOrNull { it.id == task.linkedGradeId }
                            if (grade != null) {
                                "${GradingScaleUtils.formatGrade(grade.value, gradingScale)} en ${subject.cutScheme.cutName(grade.cutId)}"
                            } else {
                                stringResource(R.string.tasks_grade_recorded)
                            }
                        }
                        task.completed -> stringResource(R.string.tasks_pill_done)
                        diff < 0 -> if (diff == -1L) "Ayer" else "Hace ${-diff} días"
                        diff == 0L -> "Hoy" + (if (hasTime) " a las ${formatTaskTime(time)}" else "")
                        diff == 1L -> "Mañana" + (if (hasTime) " a las ${formatTaskTime(time)}" else "")
                        diff in 2L..6L -> {
                            val dayName = dueDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault()).replaceFirstChar { it.uppercase() }
                            "$dayName" + (if (hasTime) " a las ${formatTaskTime(time)}" else "")
                        }
                        else -> formatTaskDate(dueDate) + (if (hasTime) " a las ${formatTaskTime(time)}" else "")
                    }
                    Text(
                        text = grande,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = estadoTextColor
                    )

                    val peq = when {
                        task.gradingStatus == TaskGradingStatus.GRADED -> {
                            val deliveredDateStr = task.completedAt?.let { d ->
                                TaskDateUtils.fromMillis(d).let { "${it.dayOfMonth} de ${it.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())}" }
                            } ?: "hoy"
                            "Entregada el $deliveredDateStr · la nota ya está en la materia"
                        }
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_awaiting_desc, "hoy")
                        task.completed -> stringResource(R.string.tasks_detail_done_desc, task.type.label().lowercase())
                        diff < 0 -> stringResource(R.string.tasks_detail_overdue_desc, formatTaskDate(dueDate) + if (hasTime) " ${formatTaskTime(time)}" else "")
                        diff == 0L -> {
                            if (hasTime) {
                                val now = LocalTime.now()
                                val remainingHours = (time.hour - now.hour).coerceAtLeast(0)
                                val remainingMins = ((time.minute - now.minute) + 60) % 60
                                stringResource(R.string.tasks_detail_time_left, remainingHours, remainingMins)
                            } else {
                                stringResource(R.string.tasks_detail_anytime_today)
                            }
                        }
                        diff == 1L -> stringResource(R.string.due_tomorrow) + " · " + formatTaskDate(dueDate)
                        else -> stringResource(R.string.tasks_detail_in_days, diff, formatTaskDate(dueDate))
                    }
                    Text(
                        text = peq,
                        style = MaterialTheme.typography.bodySmall,
                        color = estadoSubColor
                    )

                    if (!task.completed) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onPostponeTomorrow(task.id) },
                                shape = RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.14f) else estadoTextColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_tomorrow),
                                    modifier = Modifier.padding(vertical = 11.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = estadoTextColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onPostponeNextMonday(task.id) },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.14f) else estadoTextColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_next_monday),
                                    modifier = Modifier.padding(vertical = 11.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = estadoTextColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onOpenDatePicker(task.id) },
                                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 999.dp, bottomEnd = 999.dp),
                                color = if (isDark) Color.White.copy(alpha = 0.14f) else estadoTextColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_pick_day),
                                    modifier = Modifier.padding(vertical = 11.dp, horizontal = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = estadoTextColor,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Fila de Materia: tocar lleva a SubjectDetailScreen
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .cleanClickable {
                        if (subject != null) {
                            onSubjectClick(subject.id)
                        } else {
                            onEditTaskClick(task.id)
                        }
                    },
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subject?.let { subjectAccent(it) } ?: Color(0xFF232B37)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject?.name?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0A0C11)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject?.name ?: stringResource(R.string.tasks_no_subject),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textPrimary
                        )
                        val subjectSubtitle = if (subject != null) {
                            val cutName = subject.cutScheme.cutName(subject.defaultCutId)
                            val targetStr = GradingScaleUtils.formatGrade(subject.targetAverage, gradingScale)
                            "$cutName · Meta $targetStr · ${subject.grades.size} notas"
                        } else {
                            stringResource(R.string.tasks_detail_no_subject_hint)
                        }
                        Text(
                            text = subjectSubtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = textSubtle
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = textSubtle
                    )
                }
            }

            // Descripción (ubicada justo debajo de la materia para acceso directo y práctico)
            if (task.description.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_field_description).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = textSubtle
                        )
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textPrimary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }

            // Bloque de Evaluación
            if (subject != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    color = cardBg,
                    border = cardBorder
                ) {
                    Column(
                        modifier = Modifier.padding(15.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (task.type.isGradable() && task.gradingStatus == TaskGradingStatus.UNDECIDED) "AL MARCARLA" else stringResource(R.string.tasks_eval_title).uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = textSubtle
                            )
                            when {
                                task.gradingStatus == TaskGradingStatus.GRADED -> {
                                    val cutName = subject.cutScheme.cutName(subject.grades.firstOrNull { it.id == task.linkedGradeId }?.cutId ?: subject.defaultCutId)
                                    Text(
                                        text = cutName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textSubtle,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> {
                                    Text(
                                        text = "espera nota",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textSubtle,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                task.type.isGradable() && !task.completed -> {
                                    Text(
                                        text = "${task.type.label().lowercase()} · se califica",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textSubtle,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                            }
                        }

                        when {
                            task.gradingStatus == TaskGradingStatus.GRADED -> {
                                val grade = subject.grades.firstOrNull { it.id == task.linkedGradeId }
                                val gradeStr = grade?.let { GradingScaleUtils.formatGrade(it.value, gradingScale) } ?: "—"
                                Row(
                                    verticalAlignment = Alignment.Bottom,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = gradeStr,
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-1).sp,
                                        color = Color(0xFF11C045)
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = "registrada en la materia · ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSubtle
                                        )
                                        Text(
                                            text = "verla allí",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF7F77DD),
                                            modifier = Modifier.cleanClickable { onSubjectClick(subject.id) }
                                        )
                                    }
                                }
                                Text(
                                    text = "Desvincular la nota la deja en la materia y la tarea queda como hecha.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle,
                                    fontSize = 12.sp
                                )
                                TextButton(
                                    onClick = { onUnlinkGradeClick(task.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = if (isDark) Color(0xFFFF5340) else MaterialTheme.colorScheme.error)
                                ) {
                                    Text(stringResource(R.string.tasks_action_unlink_grade), fontWeight = FontWeight.Bold)
                                }
                            }
                            task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> {
                                Button(
                                    shapes = UniStackButtonDefaults.shapes,
                                    onClick = { onOpenGradeSheet(task) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF7F77DD),
                                        contentColor = Color(0xFF171040)
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Grade,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.tasks_record_grade_button),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = "Se crea en ${subject.cutScheme.cutName(subject.defaultCutId)} de ${subject.name} y queda enlazada.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle,
                                    fontSize = 12.sp
                                )
                                TextButton(
                                    onClick = { onNoGradeClick(task.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.tasks_action_no_grade), color = textSubtle)
                                }
                            }
                            task.type.isGradable() -> {
                                val isWaiting = task.gradingStatus != TaskGradingStatus.NOT_GRADED
                                UniSegmentedControl(
                                    selected = isWaiting,
                                    options = listOf(
                                        UniSegmentedOption(
                                            value = true,
                                            label = stringResource(R.string.tasks_eval_awaiting_grade)
                                        ),
                                        UniSegmentedOption(
                                            value = false,
                                            label = stringResource(R.string.tasks_eval_only_done)
                                        )
                                    ),
                                    onSelected = { onSetGradingDecision(task.id, it) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            else -> {
                                Text(
                                    text = stringResource(R.string.tasks_eval_not_gradable_desc, task.type.label()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle
                                )
                            }
                        }
                    }
                }
            }

            // Subtareas
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_subtasks_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = textSubtle
                        )
                        val countText = if (task.subtasks.isNotEmpty()) {
                            stringResource(
                                R.string.tasks_subtasks_count,
                                task.subtasks.count { it.isCompleted },
                                task.subtasks.size
                            )
                        } else {
                            stringResource(R.string.tasks_subtasks_none)
                        }
                        Text(
                            text = countText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = textSubtle
                        )
                    }

                    task.subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .cleanClickable { onToggleSubtask(task.id, subtask.id) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (subtask.isCompleted) Color(0xFF7F77DD) else Color.Transparent,
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (subtask.isCompleted) Color(0xFF7F77DD) else (if (isDark) Color(0xFF6C7689) else MaterialTheme.colorScheme.outline)
                                ),
                                modifier = Modifier.size(20.dp)
                            ) {
                                if (subtask.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF171040),
                                        modifier = Modifier.padding(2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = subtask.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .tachadoDe(subtask.isCompleted, textSubtle),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (subtask.isCompleted) textSubtle else textPrimary
                            )

                            IconButton(
                                onClick = { onDeleteSubtask(task.id, subtask.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = textSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    // Añadir subtarea
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newSubtaskText,
                            onValueChange = { newSubtaskText = it },
                            placeholder = { Text(stringResource(R.string.tasks_subtasks_add_step)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        IconButton(
                            onClick = {
                                if (newSubtaskText.isNotBlank()) {
                                    onAddSubtask(task.id, newSubtaskText.trim())
                                    newSubtaskText = ""
                                }
                            },
                            enabled = newSubtaskText.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = stringResource(R.string.tasks_subtasks_add_step),
                                tint = if (newSubtaskText.isNotBlank()) Color(0xFF7F77DD) else textSubtle.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // Línea de vida
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder
            ) {
                Column(
                    modifier = Modifier.padding(15.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "LÍNEA DE VIDA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = textSubtle
                    )

                    val createdDate = remember(task.createdAt) { TaskDateUtils.fromMillis(task.createdAt) }
                    val dueDateShort = remember(dueDate) {
                        "${dueDate.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${dueDate.dayOfMonth}"
                    }
                    val deliveredDate = remember(task.completedAt) {
                        task.completedAt?.let {
                            val d = TaskDateUtils.fromMillis(it)
                            "${d.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())} ${d.dayOfMonth}"
                        } ?: "—"
                    }
                    val gradeText = remember(task.linkedGradeId, subject) {
                        val g = subject?.grades?.firstOrNull { it.id == task.linkedGradeId }
                        g?.let { GradingScaleUtils.formatGrade(it.value, gradingScale) } ?: "—"
                    }

                    data class PasoVida(
                        val titulo: String,
                        val sub: String,
                        val completado: Boolean,
                        val actual: Boolean
                    )

                    val isWaiting = task.gradingStatus == TaskGradingStatus.AWAITING_GRADE
                    val isGraded = task.gradingStatus == TaskGradingStatus.GRADED
                    val isHecha = task.completed && !isWaiting && !isGraded

                    val pasos = if (isHecha || !task.type.isGradable()) {
                        listOf(
                            PasoVida("Creada", "${createdDate.dayOfMonth} ${createdDate.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())}", completado = true, actual = false),
                            PasoVida("Vence", dueDateShort, completado = task.completed, actual = !task.completed),
                            PasoVida("Hecha", if (task.completed) "Lista" else "—", completado = task.completed, actual = task.completed)
                        )
                    } else {
                        listOf(
                            PasoVida("Creada", "${createdDate.dayOfMonth} ${createdDate.month.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())}", completado = true, actual = false),
                            PasoVida("Vence", dueDateShort, completado = task.completed, actual = !task.completed),
                            PasoVida("Entregada", deliveredDate, completado = isWaiting || isGraded, actual = isWaiting),
                            PasoVida("Con nota", gradeText, completado = isGraded, actual = false)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        pasos.forEach { paso ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                paso.completado -> Color(0xFF11C045)
                                                paso.actual -> Color(0xFF7F77DD)
                                                else -> if (isDark) Color(0xFF232B37) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                                            }
                                        )
                                        .then(
                                            if (paso.actual) Modifier.border(2.dp, Color(0xFF7F77DD).copy(alpha = 0.5f), CircleShape)
                                             else Modifier
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = paso.titulo,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (paso.completado || paso.actual) textPrimary else textSubtle
                                )
                                Text(
                                    text = paso.sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = textSubtle
                                )
                            }
                        }
                    }
                }
            }

            // Acciones al pie: botón principal + editar + duplicar + borrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    shape = CircleShape,
                    onClick = {
                        onDismiss()
                        onToggleComplete(task)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.completed) (if (isDark) Color(0xFF232B37) else MaterialTheme.colorScheme.surfaceContainerHigh) else Color(0xFF11C045),
                        contentColor = if (task.completed) textPrimary else Color(0xFF00320F)
                    )
                ) {
                    Text(
                        text = if (task.completed) {
                            stringResource(R.string.tasks_action_revert_pending)
                        } else if (task.type.isGradable() && task.gradingStatus != TaskGradingStatus.NOT_GRADED) {
                            stringResource(R.string.tasks_action_mark_submitted)
                        } else {
                            stringResource(R.string.tasks_action_mark_done)
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = metaPillBg,
                    border = metaPillBorder,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onEditTaskClick(task.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.action_edit),
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = metaPillBg,
                    border = metaPillBorder,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onDuplicateTask(task.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ContentCopy,
                            contentDescription = stringResource(R.string.tasks_action_duplicate),
                            tint = textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = metaPillBg,
                    border = metaPillBorder,
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            onDismiss()
                            onDeleteTask(task.id)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Delete,
                            contentDescription = stringResource(R.string.action_delete),
                            tint = if (isDark) Color(0xFFFF5340) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Hoja modal para registrar la nota obtenida en una tarea directamente en el corte de la materia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HojaNota(
    task: StudentTask,
    subject: Subject,
    gradingScale: GradingScale,
    onDismiss: () -> Unit,
    onSaveGrade: (Double, Double?, String) -> Boolean
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var valueInput by remember(task.id) {
        val initialVal = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) 85.0 else 4.0
        mutableDoubleStateOf(initialVal)
    }
    var percentageInput by remember(task.id) { mutableStateOf("") }
    var weightUnknown by remember(task.id) { mutableStateOf(false) }
    var selectedCutId by remember(task.id, subject.activeCutId) {
        mutableStateOf(task.cutId ?: subject.defaultCutId)
    }
    var error by remember(task.id) { mutableStateOf<String?>(null) }
    val maxGrade: Double = remember(gradingScale) { GradingScaleUtils.maxGradeFor(gradingScale) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Cabecera
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(subjectAccent(subject)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.name.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.surface
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tasks_grade_sheet_title, task.title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.tasks_grade_sheet_subtitle, subject.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Stepper de nota
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tasks_grade_value_label).uppercase() + " · " + stringResource(R.string.tasks_grade_scale_label, maxGrade.toInt()),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val step = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) 1.0 else 0.1
                                    valueInput = (valueInput - step).coerceAtLeast(0.0)
                                }
                            ) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Text(
                            text = GradingScaleUtils.formatGrade(valueInput, gradingScale),
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = LocalSectionColors.current.onTrack
                        )

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    val step = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) 1.0 else 0.1
                                    valueInput = (valueInput + step).coerceAtMost(maxGrade)
                                }
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            // Selector de corte
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tasks_grade_cut_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        subject.cutScheme.cuts.sortedBy { it.order }.forEach { cut ->
                            val isSelected = selectedCutId == cut.id
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { selectedCutId = cut.id },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                            ) {
                                Text(
                                    text = cut.name,
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.tasks_grade_cut_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Peso opcional
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.tasks_unknown_weight),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = stringResource(R.string.tasks_unknown_weight_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        UniSwitch(
                            checked = weightUnknown,
                            onCheckedChange = { weightUnknown = it }
                        )
                    }

                    if (!weightUnknown) {
                        OutlinedTextField(
                            value = percentageInput,
                            onValueChange = { percentageInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text(stringResource(R.string.tasks_weight_in_cut)) },
                            suffix = { Text("%") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
            }

            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    val percentage = if (weightUnknown) null else percentageInput.toDoubleOrNull()
                    if (!onSaveGrade(valueInput, percentage, selectedCutId)) {
                        error = "Error al guardar la nota"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = LocalSectionColors.current.onTrack,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text(
                    text = stringResource(R.string.tasks_grade_save_action),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskGradeResultSheet(
    task: StudentTask,
    subject: Subject,
    onDismiss: () -> Unit,
    onNoGrade: () -> Unit,
    onSaveGrade: (Double, Double?, String) -> Boolean
) {
    var enteringGrade by remember(task.id) { mutableStateOf(false) }
    var valueInput by remember(task.id) { mutableStateOf("") }
    var percentageInput by remember(task.id) { mutableStateOf("") }
    var weightUnknown by remember(task.id) { mutableStateOf(false) }
    var selectedCutId by remember(task.id, subject.activeCutId) {
        mutableStateOf(task.cutId ?: subject.defaultCutId)
    }
    var error by remember(task.id) { mutableStateOf<String?>(null) }
    val validationErrorMsg = stringResource(R.string.tasks_validation_error)
    val saveErrorMsg = stringResource(R.string.tasks_save_error)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (enteringGrade) stringResource(R.string.tasks_record_grade_title) else stringResource(R.string.tasks_completed_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (enteringGrade) {
                    "${task.title} · ${subject.name}"
                } else {
                    stringResource(R.string.tasks_record_grade_question, task.title)
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!enteringGrade) {
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { enteringGrade = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) { Text(stringResource(R.string.tasks_record_grade_yes)) }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.tasks_record_grade_not_yet))
                }
                TextButton(onClick = onNoGrade, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.tasks_record_grade_never))
                }
            } else {
                Text(stringResource(R.string.tasks_field_cut), fontWeight = FontWeight.SemiBold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    subject.cutScheme.cuts.sortedBy { it.order }.forEach { cut ->
                        Surface(
                            onClick = { selectedCutId = cut.id },
                            color = if (selectedCutId == cut.id) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            },
                            shape = MaterialTheme.shapes.small
                        ) {
                            Text(
                                cut.name,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = valueInput,
                    onValueChange = {
                        valueInput = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.tasks_grade_obtained)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.tasks_unknown_weight), fontWeight = FontWeight.SemiBold)
                        Text(
                            stringResource(R.string.tasks_unknown_weight_hint),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    UniSwitch(
                        checked = weightUnknown,
                        onCheckedChange = {
                            weightUnknown = it
                            error = null
                        }
                    )
                }
                if (!weightUnknown) {
                    OutlinedTextField(
                        value = percentageInput,
                        onValueChange = {
                            percentageInput = it.filter { char -> char.isDigit() || char == '.' }
                            error = null
                        },
                        label = { Text(stringResource(R.string.tasks_weight_in_cut)) },
                        suffix = { Text("%") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                error?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {
                        val value = valueInput.toDoubleOrNull()
                        val percentage = if (weightUnknown) null else percentageInput.toDoubleOrNull()
                        if (value == null || (!weightUnknown && percentage == null)) {
                            error = validationErrorMsg
                        } else if (!onSaveGrade(value, percentage, selectedCutId)) {
                            error = saveErrorMsg
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) { Text(stringResource(R.string.tasks_save_grade)) }
            }
        }
    }
}

@Composable
private fun TaskSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearchDone: () -> Unit
) {
    UniSearchField(
        query = query,
        onQueryChange = onQueryChange,
        placeholder = stringResource(R.string.tasks_search_field_placeholder),
        onSearchDone = onSearchDone
    )
}

@Composable
private fun TaskFilterSummaryChip(
    selectedStatus: TaskListFilter,
    selectedSubjectName: String?,
    selectedPriority: TaskDifficulty?,
    sortOrder: TaskSortOrder,
    onClick: () -> Unit
) {
    val isFilterActive = selectedStatus != TaskListFilter.ALL ||
        selectedSubjectName != null ||
        selectedPriority != null ||
        sortOrder != TaskSortOrder.DUE_DATE

    val label = filterSummaryLabel(
        selectedStatus = selectedStatus,
        selectedSubjectName = selectedSubjectName,
        selectedPriority = selectedPriority,
        sortOrder = sortOrder
    )

    val isDark = LocalIsDarkTheme.current
    val chipBg = if (isFilterActive) {
        if (isDark) Color(0xFF2E2A6B) else MaterialTheme.colorScheme.primaryContainer
    } else {
        if (isDark) Color(0xFF1C222D) else MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val chipContent = if (isFilterActive) {
        if (isDark) Color(0xFFE2DFFF) else MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant
    }
    val chipBorder = if (!isFilterActive && !isDark) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    } else null

    Surface(
        modifier = Modifier.cleanClickable(shape = CircleShape, onClick = onClick),
        shape = CircleShape,
        color = chipBg,
        border = chipBorder,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Tune,
                contentDescription = stringResource(R.string.tasks_open_filters),
                tint = chipContent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                color = chipContent,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SectionTitle(
    text: String,
    count: Int,
    isOverdue: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val titleColor = if (isOverdue) {
        if (isDark) Color(0xFFFF5340) else MaterialTheme.colorScheme.error
    } else {
        if (isDark) Color(0xFF98A2B7) else MaterialTheme.colorScheme.onSurfaceVariant
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text.uppercase(Locale.getDefault()),
            color = titleColor,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$count",
            color = titleColor.copy(alpha = 0.85f),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.sp
        )
    }
}

@Composable
private fun TasksEmptyState() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.EventNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.tasks_empty_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.tasks_empty_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FilteredEmptyState(onOpenFilters: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.tasks_empty_filtered_title),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = stringResource(R.string.tasks_empty_filtered_subtitle),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onOpenFilters) {
                Text(stringResource(R.string.tasks_change_filters), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun NewTaskFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(56.dp)
            .cleanClickable(onClick = onClick),
        shape = MaterialTheme.shapes.extraLarge,
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
                text = stringResource(R.string.tasks_new_task),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TasksFilterBottomSheet(
    subjects: List<Subject>,
    tasks: List<StudentTask>,
    selectedStatus: TaskListFilter,
    selectedSubjectId: String?,
    selectedPriority: TaskDifficulty?,
    sortOrder: TaskSortOrder,
    onStatusSelected: (TaskListFilter) -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onPrioritySelected: (TaskDifficulty?) -> Unit,
    onSortSelected: (TaskSortOrder) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val activeFiltersCount = (if (selectedStatus != TaskListFilter.ALL) 1 else 0) +
        (if (selectedSubjectId != null) 1 else 0) +
        (if (selectedPriority != null) 1 else 0) +
        (if (sortOrder != TaskSortOrder.DUE_DATE) 1 else 0)

    val filteredCount = tasks.count { task ->
        selectedStatus.matches(task) &&
            (selectedSubjectId == null || task.subjectId == selectedSubjectId) &&
            (selectedPriority == null || task.difficulty == selectedPriority)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0D1017) else MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 16.dp, bottom = 12.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = if (isDark) 0.35f else 0.45f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FiltersSheetHeader(
                activeFiltersCount = activeFiltersCount,
                onClear = onClear
            )

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                FilterSheetSection(title = stringResource(R.string.tasks_status_title)) {
                    StatusFilterGrid(
                        selectedStatus = selectedStatus,
                        onStatusSelected = onStatusSelected
                    )
                }

                SubjectDropdownSelector(
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    onSubjectSelected = onSubjectSelected
                )

                PriorityFilterSection(
                    selectedPriority = selectedPriority,
                    onPrioritySelected = onPrioritySelected
                )

                SortFilterSection(
                    sortOrder = sortOrder,
                    onSortSelected = onSortSelected
                )
            }

            FiltersSheetFooter(
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun FiltersSheetHeader(
    activeFiltersCount: Int,
    onClear: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 2.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.tasks_filter_title),
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = stringResource(R.string.tasks_filter_subtitle),
                color = if (isDark) Color(0xFF8F97A8) else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.5.sp,
                lineHeight = 16.sp
            )
        }
        TextButton(
            onClick = onClear,
            shape = CircleShape
        ) {
            Text(
                text = stringResource(R.string.tasks_filter_clear),
                color = if (activeFiltersCount > 0) Color(0xFF7C6EE6) else (if (isDark) Color(0xFF98A2B7).copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterSheetSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val sectionBg = if (isDark) Color(0xFF141722) else MaterialTheme.colorScheme.surface
    val titleColor = if (isDark) Color(0xFF727A8C) else MaterialTheme.colorScheme.onSurfaceVariant
    val border = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = sectionBg,
        border = border,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title.uppercase(Locale.getDefault()),
                color = titleColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
            content()
        }
    }
}

@Composable
private fun StatusFilterGrid(
    selectedStatus: TaskListFilter,
    onStatusSelected: (TaskListFilter) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val options = listOf(
        Pair(TaskListFilter.ALL, stringResource(R.string.tasks_filter_all)),
        Pair(TaskListFilter.PENDING, stringResource(R.string.tasks_filter_pending)),
        Pair(TaskListFilter.COMPLETED, stringResource(R.string.tasks_filter_done)),
        Pair(TaskListFilter.OVERDUE, stringResource(R.string.tasks_filter_overdue))
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        options.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { (filter, label) ->
                    val isSelected = selectedStatus == filter
                    val cardBg = if (isSelected) Color(0xFF7C6EE6) else (if (isDark) Color(0xFF1C202C) else MaterialTheme.colorScheme.surfaceContainerHigh)
                    val contentColor = if (isSelected) Color(0xFF15112B) else (if (isDark) Color(0xFFD8DCE8) else MaterialTheme.colorScheme.onSurface)
                    val iconTint = if (isSelected) Color(0xFF15112B) else (if (isDark) Color(0xFF8F97A8) else MaterialTheme.colorScheme.onSurfaceVariant)
                    val itemBorder = if (!isSelected && !isDark) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)) else null

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .cleanClickable(shape = RoundedCornerShape(16.dp)) { onStatusSelected(filter) },
                        shape = RoundedCornerShape(16.dp),
                        color = cardBg,
                        border = itemBorder,
                        tonalElevation = 0.dp,
                        shadowElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = filter.icon(),
                                contentDescription = null,
                                tint = iconTint,
                                modifier = Modifier.size(19.dp)
                            )
                            Text(
                                text = label,
                                color = contentColor,
                                fontSize = 13.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Icono de forma Material 3 Expressive para la materia seleccionada o en lista.
 * Sincronizado 1:1 con la forma (formaDeMateria) y el color de Académico.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SubjectShapeIcon(
    color: Color,
    seed: String,
    modifier: Modifier = Modifier,
    size: Dp = 18.dp
) {
    val estilo = LocalAppearancePreferences.current.badgeShape
    val polygon = remember(seed, estilo) { formaDeMateria(estilo, seed) }
    val path = polygon.toPath()
    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            withTransform({ scale(this.size.width, this.size.height, pivot = Offset.Zero) }) {
                drawPath(path, color)
            }
        }
    }
}

@Composable
private fun SubjectDropdownSelector(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onSubjectSelected: (String?) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    var expanded by remember { mutableStateOf(false) }
    val allSubjectsLabel = stringResource(R.string.tasks_all_subjects)
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }
    val selectedLabel = selectedSubject?.name ?: allSubjectsLabel

    val triggerBg = if (isDark) Color(0xFF1C202C) else MaterialTheme.colorScheme.surfaceContainerHigh
    val triggerBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))
    val triggerTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
    val triggerArrowColor = if (isDark) Color(0xFF8F97A8) else MaterialTheme.colorScheme.onSurfaceVariant
    val menuBg = if (isDark) Color(0xFF171B26) else MaterialTheme.colorScheme.surfaceContainer

    FilterSheetSection(title = stringResource(R.string.tasks_field_subject)) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .cleanClickable(shape = RoundedCornerShape(20.dp)) { expanded = !expanded },
                shape = RoundedCornerShape(20.dp),
                color = triggerBg,
                border = triggerBorder,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedSubject != null) {
                        SubjectShapeIcon(
                            color = subjectAccent(selectedSubject),
                            seed = selectedSubject.id,
                            size = 20.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = Color(0xFF7C6EE6),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = selectedLabel,
                        modifier = Modifier.weight(1f),
                        color = triggerTextColor,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = triggerArrowColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(24.dp),
                containerColor = menuBg,
                modifier = Modifier
                    .clip(RoundedCornerShape(24.dp))
                    .then(
                        if (!isDark) Modifier.border(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            RoundedCornerShape(24.dp)
                        ) else Modifier
                    )
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            text = allSubjectsLabel,
                            fontWeight = if (selectedSubjectId == null) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (selectedSubjectId == null) Color(0xFF7C6EE6) else (if (isDark) Color(0xFFD8DCE8) else MaterialTheme.colorScheme.onSurface),
                            fontSize = 13.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            tint = if (selectedSubjectId == null) Color(0xFF7C6EE6) else (if (isDark) Color(0xFF8F97A8) else MaterialTheme.colorScheme.onSurfaceVariant),
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = if (selectedSubjectId == null) {
                        {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = Color(0xFF7C6EE6),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null,
                    onClick = {
                        onSubjectSelected(null)
                        expanded = false
                    }
                )
                subjects.forEach { subject ->
                    val isSelected = selectedSubjectId == subject.id
                    val subjectColor = subjectAccent(subject)
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = subject.name,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = if (isSelected) Color(0xFF7C6EE6) else (if (isDark) Color(0xFFD8DCE8) else MaterialTheme.colorScheme.onSurface),
                                fontSize = 13.5.sp
                            )
                        },
                        leadingIcon = {
                            SubjectShapeIcon(
                                color = subjectColor,
                                seed = subject.id,
                                size = 20.dp
                            )
                        },
                        trailingIcon = if (isSelected) {
                            {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF7C6EE6),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else null,
                        onClick = {
                            onSubjectSelected(subject.id)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PriorityFilterSection(
    selectedPriority: TaskDifficulty?,
    onPrioritySelected: (TaskDifficulty?) -> Unit
) {
    FilterSheetSection(title = stringResource(R.string.tasks_priority_title)) {
        val options = listOf(
            UniSegmentedOption<TaskDifficulty?>(
                value = null,
                label = stringResource(R.string.tasks_priority_all)
            ),
            UniSegmentedOption<TaskDifficulty?>(
                value = TaskDifficulty.HARD,
                label = stringResource(R.string.tasks_priority_high)
            ),
            UniSegmentedOption<TaskDifficulty?>(
                value = TaskDifficulty.MEDIUM,
                label = stringResource(R.string.tasks_priority_medium)
            ),
            UniSegmentedOption<TaskDifficulty?>(
                value = TaskDifficulty.EASY,
                label = stringResource(R.string.tasks_priority_low)
            )
        )

        UniSegmentedControl(
            selected = selectedPriority,
            options = options,
            onSelected = onPrioritySelected,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SortFilterSection(
    sortOrder: TaskSortOrder,
    onSortSelected: (TaskSortOrder) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    FilterSheetSection(title = stringResource(R.string.tasks_order_title)) {
        val options = listOf(
            Pair(TaskSortOrder.DUE_DATE, stringResource(R.string.tasks_order_due_date)),
            Pair(TaskSortOrder.PRIORITY, stringResource(R.string.tasks_order_priority)),
            Pair(TaskSortOrder.SUBJECT, stringResource(R.string.tasks_order_subject))
        )

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            options.forEach { (option, label) ->
                val isSelected = sortOrder == option
                val textColor = if (isSelected) {
                    if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                } else {
                    if (isDark) Color(0xFFD8DCE8) else MaterialTheme.colorScheme.onSurfaceVariant
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .cleanClickable(shape = RoundedCornerShape(12.dp)) { onSortSelected(option) }
                        .padding(vertical = 6.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    RadioButton(
                        selected = isSelected,
                        onClick = null,
                        colors = RadioButtonDefaults.colors(
                            selectedColor = Color(0xFF7C6EE6),
                            unselectedColor = if (isDark) Color(0xFF52596A) else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = label,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FiltersSheetFooter(
    onDismiss: () -> Unit
) {
    Button(
        shapes = UniStackButtonDefaults.shapes,
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF7C6EE6),
            contentColor = Color(0xFF15112B)
        )
    ) {
        Text(
            text = stringResource(R.string.tasks_filter_ready),
            color = Color(0xFF15112B),
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun TaskListFilter.icon(): ImageVector {
    return when (this) {
        TaskListFilter.ALL -> Icons.Rounded.Menu
        TaskListFilter.PENDING -> Icons.Rounded.CalendarMonth
        TaskListFilter.COMPLETED -> Icons.Rounded.Check
        TaskListFilter.OVERDUE -> Icons.Rounded.Info
    }
}

private fun List<StudentTask>.sortFor(
    sortOrder: TaskSortOrder,
    subjects: List<Subject>
): List<StudentTask> {
    return when (sortOrder) {
        TaskSortOrder.DUE_DATE -> sortedWith(compareBy<StudentTask> { it.completed }.thenBy { it.dueDateMillis })
        TaskSortOrder.PRIORITY -> sortedWith(
            compareBy<StudentTask> { it.completed }
                .thenByDescending { it.difficulty.rank() }
                .thenBy { it.dueDateMillis }
        )
        TaskSortOrder.SUBJECT -> sortedWith(
            compareBy<StudentTask> { task -> subjects.firstOrNull { it.id == task.subjectId }?.name.orEmpty() }
                .thenBy { it.dueDateMillis }
        )
        TaskSortOrder.RECENT -> sortedByDescending { it.createdAt }
    }
}

@Composable
private fun formatDueDayTitle(date: LocalDate, today: LocalDate): String {
    return when (date) {
        today -> stringResource(R.string.tasks_group_today)
        today.plusDays(1) -> stringResource(R.string.tasks_group_tomorrow)
        today.minusDays(1) -> stringResource(R.string.tasks_group_yesterday)
        else -> date.format(DateTimeFormatter.ofPattern("EEEE d", Locale.getDefault())).replaceFirstChar { it.uppercase() }
    }
}

private fun formatFutureDayTitle(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("EEEE d", Locale.getDefault())).replaceFirstChar { it.uppercase() }
}

private val shortDateFormatter: DateTimeFormatter get() = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault())

@Composable
private fun TaskDifficulty.shortLabel(): String {
    return when (this) {
        TaskDifficulty.EASY -> stringResource(R.string.tasks_priority_low)
        TaskDifficulty.MEDIUM -> stringResource(R.string.tasks_priority_medium)
        TaskDifficulty.HARD -> stringResource(R.string.tasks_priority_high)
    }
}

private fun TaskDifficulty.rank(): Int {
    return when (this) {
        TaskDifficulty.EASY -> 1
        TaskDifficulty.MEDIUM -> 2
        TaskDifficulty.HARD -> 3
    }
}

@Composable
@ReadOnlyComposable
private fun TaskDifficulty.color(): Color {
    return when (this) {
        TaskDifficulty.EASY -> LocalSectionColors.current.onTrack
        TaskDifficulty.MEDIUM -> LocalSectionColors.current.atRisk
        TaskDifficulty.HARD -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun TaskType.label(): String {
    return when (this) {
        TaskType.WORKSHOP -> stringResource(R.string.tasks_type_workshop)
        TaskType.EXAM -> stringResource(R.string.tasks_type_midterm)
        TaskType.ESSAY -> stringResource(R.string.tasks_type_essay)
        TaskType.PRESENTATION -> stringResource(R.string.tasks_type_presentation)
        TaskType.RESEARCH -> stringResource(R.string.tasks_type_research)
        TaskType.TEST -> stringResource(R.string.tasks_type_exam)
        TaskType.PRACTICE -> stringResource(R.string.tasks_type_practice)
        TaskType.PROJECT -> stringResource(R.string.tasks_type_project)
        TaskType.READING -> stringResource(R.string.tasks_type_reading)
        TaskType.OTHER -> stringResource(R.string.tasks_type_other)
    }
}

@Composable
private fun filterSummaryLabel(
    selectedStatus: TaskListFilter,
    selectedSubjectName: String?,
    selectedPriority: TaskDifficulty?,
    sortOrder: TaskSortOrder
): String {
    val activeFilters = buildList {
        if (selectedStatus != TaskListFilter.ALL) add(selectedStatus.label)
        selectedSubjectName?.let { add(it) }
        selectedPriority?.let { add(it.shortLabel()) }
        if (sortOrder != TaskSortOrder.DUE_DATE) add(sortOrder.label)
    }
    return if (activeFilters.isEmpty()) {
        stringResource(R.string.tasks_chip_all_by_date)
    } else {
        activeFilters.joinToString(" · ")
    }
}

private fun formatEstimatedDuration(minutes: Int): String {
    if (minutes <= 0) return ""
    val h = minutes / 60
    val m = minutes % 60
    return when {
        h > 0 && m > 0 -> "$h h $m min"
        h > 0 -> "$h h"
        else -> "$m min"
    }
}

private fun taskTypeIcon(type: TaskType): ImageVector = when (type) {
    TaskType.PROJECT -> Icons.Rounded.Category
    TaskType.WORKSHOP -> Icons.AutoMirrored.Rounded.MenuBook
    TaskType.EXAM, TaskType.TEST -> Icons.Rounded.Grade
    TaskType.ESSAY, TaskType.READING -> Icons.AutoMirrored.Rounded.MenuBook
    else -> Icons.AutoMirrored.Rounded.MenuBook
}


private val visibleStatusFilters = listOf(
    TaskListFilter.ALL,
    TaskListFilter.PENDING,
    TaskListFilter.COMPLETED,
    TaskListFilter.OVERDUE
)

private enum class TaskListFilter(@StringRes val labelRes: Int) {
    ALL(R.string.tasks_status_all),
    PENDING(R.string.tasks_tab_pending),
    OVERDUE(R.string.tasks_tab_overdue),
    COMPLETED(R.string.tasks_tab_completed);

    val label: String
        @Composable get() = stringResource(labelRes)

    fun matches(task: StudentTask): Boolean {
        return when (this) {
            ALL -> true
            PENDING -> !task.completed
            OVERDUE -> !task.completed && TaskDateUtils.fromMillis(task.dueDateMillis).isBefore(TaskDateUtils.today())
            COMPLETED -> task.completed
        }
    }
}

private enum class TaskSortOrder(@StringRes val labelRes: Int) {
    DUE_DATE(R.string.tasks_sort_due_date),
    PRIORITY(R.string.tasks_sort_priority),
    SUBJECT(R.string.tasks_sort_subject),
    RECENT(R.string.tasks_sort_recent);

    val label: String
        @Composable get() = stringResource(labelRes)
}
