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
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.anchoredButtonRoom
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.isGradable
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.formatTaskDueText
import com.unistack.app.feature_user.domain.GradingScale
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
    var selectedDateOffset by remember { mutableStateOf<LocalDate?>(null) }
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

    val onTaskChecked: (StudentTask, Boolean) -> Unit = { task, checked ->
        clearSearchFocus()
        val eraLaUltima = checked &&
            tasks.none {
                !it.completed && it.id != task.id &&
                    TaskDateUtils.fromMillis(it.dueDateMillis) <= TaskDateUtils.today()
            }
        completionPrompt = viewModel.setTaskCompleted(task.id, checked)
        if (eraLaUltima) celebrando = true

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
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    val sunday = remember(monday) { monday.plusDays(6) }

    val filteredTasks = remember(tasks, subjects, selectedFilter, selectedSubjectId, selectedDateOffset, selectedPriority, sortOrder, searchQuery) {
        val query = searchQuery.trim().lowercase(Locale.ROOT)
        tasks
            .filter { task -> selectedFilter.matches(task) }
            .filter { task -> selectedSubjectId == null || task.subjectId == selectedSubjectId }
            .filter { task ->
                if (selectedDateOffset == null) true
                else TaskDateUtils.fromMillis(task.dueDateMillis) == selectedDateOffset
            }
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
                start = 20.dp,
                top = if (embedded) 10.dp else 58.dp,
                end = 20.dp,
                bottom = scrollBottomRoom + anchoredButtonRoom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (!embedded) {
                item {
                    TasksHeader(
                        monday = monday,
                        sunday = sunday,
                        onToggleSearch = { isSearchExpanded = !isSearchExpanded }
                    )
                }
            }

            if (isSearchExpanded || searchQuery.isNotEmpty()) {
                item {
                    TaskSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchDone = clearSearchFocus
                    )
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
                        totalPendingCount = tasks.count { !it.completed }
                    )
                }

                item {
                    SemanaTira(
                        weekDays = weekDays,
                        today = today,
                        selectedDate = selectedDateOffset,
                        tasks = tasks,
                        onSelectDate = { date ->
                            clearSearchFocus()
                            selectedDateOffset = if (selectedDateOffset == date) null else date
                        }
                    )
                }

                item {
                    RielMaterias(
                        subjects = subjects,
                        tasks = tasks,
                        selectedSubjectId = selectedSubjectId,
                        onSelectSubject = { subjectId ->
                            clearSearchFocus()
                            selectedSubjectId = subjectId
                        }
                    )
                }

                item {
                    TaskFilterSummaryChip(
                        selectedStatus = selectedFilter,
                        selectedSubjectName = selectedSubjectName,
                        selectedPriority = selectedPriority,
                        sortOrder = sortOrder,
                        selectedDate = selectedDateOffset,
                        onClick = {
                            clearSearchFocus()
                            showFiltersSheet = true
                        }
                    )
                }
            }

            when {
                tasks.isEmpty() -> item {
                    TasksEmptyState()
                }
                filteredTasks.isEmpty() -> item {
                    FilteredEmptyState(onOpenFilters = { showFiltersSheet = true })
                }
                selectedDateOffset != null -> {
                    val dayActive = activeTasks
                    val dayDone = completedTasks
                    if (dayActive.isNotEmpty()) {
                        item { SectionTitle(formatDueDayTitle(selectedDateOffset!!, today), dayActive.size) }
                        itemsIndexed(dayActive, key = { _, it -> it.id }) { indice, task ->
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
                    if (dayDone.isNotEmpty()) {
                        item { SectionTitle(stringResource(R.string.tasks_group_done), dayDone.size) }
                        itemsIndexed(dayDone, key = { _, it -> it.id }) { indice, task ->
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
                selectedDateOffset = null
                sortOrder = TaskSortOrder.DUE_DATE
            },
            onDismiss = { showFiltersSheet = false }
        )
    }

    completionPrompt?.let { prompt ->
        val subject = subjects.firstOrNull { it.id == prompt.task.subjectId }
        if (subject == null) {
            completionPrompt = null
        } else {
            TaskGradeResultSheet(
                task = prompt.task,
                subject = subject,
                onDismiss = {
                    viewModel.markTaskAwaitingGrade(prompt.task.id)
                    completionPrompt = null
                },
                onNoGrade = {
                    viewModel.markTaskAsNotGraded(prompt.task.id)
                    completionPrompt = null
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
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            modifier = Modifier.size(40.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = stringResource(R.string.tasks_search_field_placeholder),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Hero tonal de «Esta semana» con cifra principal, aviso de vencidas y el anillo ondulado.
 */
@Composable
private fun TasksWeekHero(
    pendingCount: Int,
    overdueCount: Int,
    doneCount: Int,
    totalCount: Int,
    awaitingGradeCount: Int,
    totalPendingCount: Int,
    modifier: Modifier = Modifier
) {
    val hasOverdue = overdueCount > 0
    val containerColor = if (hasOverdue) {
        LocalSectionColors.current.atRisk.copy(alpha = 0.16f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
    }
    val contentColor = MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = stringResource(R.string.tasks_hero_this_week),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.5.sp,
                color = if (hasOverdue) LocalSectionColors.current.atRisk else MaterialTheme.colorScheme.primary
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
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "$pendingCount",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Black,
                            color = contentColor,
                            fontSize = 44.sp,
                            lineHeight = 44.sp
                        )
                        Text(
                            text = stringResource(R.string.tasks_hero_to_do),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = contentColor.copy(alpha = 0.85f)
                        )
                    }
                    Text(
                        text = when {
                            overdueCount == 1 -> stringResource(R.string.tasks_hero_overdue_singular, 1)
                            overdueCount > 1 -> stringResource(R.string.tasks_hero_overdue_plural, overdueCount)
                            pendingCount > 0 -> stringResource(R.string.tasks_hero_nothing_overdue)
                            else -> stringResource(R.string.tasks_hero_week_closed)
                        },
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasOverdue) MaterialTheme.colorScheme.error else contentColor.copy(alpha = 0.75f)
                    )
                }

                OutcomeWaveRing(
                    progress = if (totalCount > 0) doneCount.toFloat() / totalCount else 0f,
                    doneCount = doneCount,
                    totalCount = totalCount,
                    color = if (hasOverdue) LocalSectionColors.current.atRisk else MaterialTheme.colorScheme.primary
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$totalPendingCount " + stringResource(R.string.tasks_filter_pending).lowercase(),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
                if (awaitingGradeCount > 0) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_hero_awaiting_grade, awaitingGradeCount),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            style = MaterialTheme.typography.labelSmall,
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
    color: Color = MaterialTheme.colorScheme.primary
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
            val r = size.minDimension * 0.38f
            val amp = 1.2f.dp.toPx() + 2.4f.dp.toPx() * animatedProgress
            val ondas = 12
            val steps = 220
            val strokeWidth = 5.5f.dp.toPx()

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
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 15.sp,
                lineHeight = 16.sp
            )
            Text(
                text = stringResource(R.string.tasks_hero_hechas),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

/**
 * Tira de los 7 días de la semana (Lunes a Domingo) con puntos de actividad.
 */
@Composable
private fun SemanaTira(
    weekDays: List<LocalDate>,
    today: LocalDate,
    selectedDate: LocalDate?,
    tasks: List<StudentTask>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        weekDays.forEach { date ->
            val isToday = date == today
            val isSelected = selectedDate == date
            val dayTasks = tasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == date }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .cleanClickable { onSelectDate(date) },
                shape = RoundedCornerShape(14.dp),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.surfaceContainerHigh
                    else -> MaterialTheme.colorScheme.surfaceContainer
                }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val dayInitial = date.dayOfWeek.getDisplayName(java.time.format.TextStyle.NARROW, Locale.getDefault()).uppercase()
                    Text(
                        text = dayInitial,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                    Text(
                        text = "${date.dayOfMonth}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Row(
                        modifier = Modifier.height(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val dotsToShow = dayTasks.take(3)
                        dotsToShow.forEach { task ->
                            val dotColor = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                !task.completed && date.isBefore(today) -> MaterialTheme.colorScheme.error
                                !task.completed -> MaterialTheme.colorScheme.primary
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            }
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .clip(CircleShape)
                                    .background(dotColor)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Riel horizontal deslizable de materias con chip «Todas» y contadores por materia.
 */
@Composable
private fun RielMaterias(
    subjects: List<Subject>,
    tasks: List<StudentTask>,
    selectedSubjectId: String?,
    onSelectSubject: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val allCount = tasks.count { !it.completed }
        val isAllSelected = selectedSubjectId == null

        Surface(
            modifier = Modifier.cleanClickable { onSelectSubject(null) },
            shape = CircleShape,
            color = if (isAllSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = stringResource(R.string.tasks_rail_all),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$allCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isAllSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }

        subjects.forEach { subject ->
            val isSelected = selectedSubjectId == subject.id
            val count = tasks.count { !it.completed && it.subjectId == subject.id }
            val subjectColor = subjectAccent(subject)
            val shortName = subject.name.split(" ").firstOrNull() ?: subject.name

            Surface(
                modifier = Modifier.cleanClickable {
                    onSelectSubject(if (isSelected) null else subject.id)
                },
                shape = CircleShape,
                color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(subjectColor)
                    )
                    Text(
                        text = shortName,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) MaterialTheme.colorScheme.background.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Fila de tarea de la Propuesta D envuelta en [FilaDeslizable] para borrar.
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

    FilaDeslizable(
        onBorrar = onDelete,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .then(reacomodoDeLista())
            .entradaDeLista(indice)
            .latidoDeVencido(activo = isOverdue)
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

/**
 * La fila de la lista de tareas: casilla cuadrada con esquinas suaves, texto tachado,
 * punto de la materia, tipo, progreso de subtareas y píldora de estado.
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
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val isToday = !task.completed && dueDate == today
    val awaitingGrade = task.completed && task.gradingStatus == TaskGradingStatus.AWAITING_GRADE

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .cleanClickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (awaitingGrade) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        else MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Casilla de M3 con esquinas suaves (6.dp)
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (task.completed) MaterialTheme.colorScheme.primary else Color.Transparent,
                border = BorderStroke(
                    width = 2.dp,
                    color = if (task.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier
                    .size(22.dp)
                    .cleanClickable { onCheckedChange(!task.completed) }
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (task.completed) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f) else MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.tachadoDe(
                        completado = task.completed,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (subject != null) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(subjectAccent(subject))
                        )
                        Text(
                            text = subject.name,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.tasks_no_subject),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = task.type.label(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (task.subtasks.isNotEmpty()) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val doneCount = task.subtasks.count { it.isCompleted }
                        Text(
                            text = "$doneCount/${task.subtasks.size}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                // Píldora de estado
                when {
                    awaitingGrade -> {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_pill_awaiting_grade),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    task.gradingStatus == TaskGradingStatus.GRADED -> {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.onTrack.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_detail_graded_label),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalSectionColors.current.onTrack
                            )
                        }
                    }
                    task.completed -> {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.onTrack.copy(alpha = 0.16f)
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_pill_done),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalSectionColors.current.onTrack
                            )
                        }
                    }
                    isOverdue -> {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text = formatTaskDueText(task.dueDateMillis),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    isToday -> {
                        Surface(
                            shape = CircleShape,
                            color = LocalSectionColors.current.atRisk.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = stringResource(R.string.tasks_due_today),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = LocalSectionColors.current.atRisk
                            )
                        }
                    }
                    else -> {
                        Text(
                            text = formatTaskDueText(task.dueDateMillis),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(task.difficulty.color())
                )
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cabecera: icono tipo, título, metadatos y botón "Abrir entera"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            (subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary)
                                .copy(alpha = 0.16f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.EventNote,
                        contentDescription = null,
                        tint = subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = task.type.label(),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(task.difficulty.color())
                                )
                                Text(
                                    text = task.difficulty.shortLabel(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                        ) {
                            Text(
                                text = "${task.estimatedMinutes} min",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Bloque de Estado (Con botones de pospuesto rápido si está pendiente)
            val estadoColor = when {
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> MaterialTheme.colorScheme.primaryContainer
                task.gradingStatus == TaskGradingStatus.GRADED || task.completed -> LocalSectionColors.current.onTrack.copy(alpha = 0.16f)
                isOverdue -> MaterialTheme.colorScheme.errorContainer
                isToday -> LocalSectionColors.current.atRisk.copy(alpha = 0.16f)
                else -> MaterialTheme.colorScheme.surfaceContainer
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = estadoColor
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val rotulo = when {
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_submitted_label)
                        task.gradingStatus == TaskGradingStatus.GRADED -> stringResource(R.string.tasks_detail_graded_label)
                        task.completed -> stringResource(R.string.tasks_detail_done_label)
                        isOverdue -> stringResource(R.string.tasks_detail_overdue_label)
                        else -> stringResource(R.string.tasks_detail_due_label)
                    }
                    Text(
                        text = rotulo.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )

                    val grande = when {
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_pill_awaiting_grade)
                        task.gradingStatus == TaskGradingStatus.GRADED -> {
                            val grade = subject?.grades?.firstOrNull { it.id == task.linkedGradeId }
                            if (grade != null) {
                                "${GradingScaleUtils.formatGrade(grade.value, gradingScale)} · ${subject.cutScheme.cutName(grade.cutId)}"
                            } else {
                                stringResource(R.string.tasks_grade_recorded)
                            }
                        }
                        task.completed -> stringResource(R.string.tasks_pill_done)
                        isToday -> stringResource(R.string.tasks_due_today)
                        isOverdue -> formatTaskDueText(task.dueDateMillis)
                        else -> formatTaskDueText(task.dueDateMillis)
                    }
                    Text(
                        text = grande,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val peq = when {
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_awaiting_desc, "hoy")
                        task.gradingStatus == TaskGradingStatus.GRADED -> stringResource(R.string.tasks_detail_graded_desc, "hoy")
                        task.completed -> stringResource(R.string.tasks_detail_done_desc, task.type.label().lowercase())
                        isToday -> stringResource(R.string.tasks_detail_anytime_today)
                        isOverdue -> stringResource(R.string.tasks_detail_overdue_desc, formatTaskDueText(task.dueDateMillis))
                        else -> "Vence ${formatTaskDueText(task.dueDateMillis).lowercase()}"
                    }
                    Text(
                        text = peq,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )

                    if (!task.completed) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onPostponeTomorrow(task.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_tomorrow),
                                    modifier = Modifier.padding(vertical = 9.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onPostponeNextMonday(task.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_next_monday),
                                    modifier = Modifier.padding(vertical = 9.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                            }
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .cleanClickable { onOpenDatePicker(task.id) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_postpone_pick_day),
                                    modifier = Modifier.padding(vertical = 9.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
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
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject?.name?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.surface
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = subject?.name ?: stringResource(R.string.tasks_no_subject),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (subject != null) {
                                "Meta: ${GradingScaleUtils.formatGrade(subject.targetAverage, gradingScale)} · ${subject.grades.size} notas"
                            } else {
                                stringResource(R.string.tasks_detail_no_subject_hint)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Subtareas
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_subtasks_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                shape = RoundedCornerShape(5.dp),
                                color = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                                border = BorderStroke(
                                    width = 1.5.dp,
                                    color = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                modifier = Modifier.size(18.dp)
                            ) {
                                if (subtask.isCompleted) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.padding(1.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = subtask.title,
                                modifier = Modifier
                                    .weight(1f)
                                    .tachadoDe(subtask.isCompleted, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (subtask.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                            )

                            IconButton(
                                onClick = { onDeleteSubtask(task.id, subtask.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
                                tint = if (newSubtaskText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }

            // Bloque de Evaluación
            if (subject != null) {
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
                            text = stringResource(R.string.tasks_eval_title).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        when {
                            task.gradingStatus == TaskGradingStatus.GRADED -> {
                                val grade = subject.grades.firstOrNull { it.id == task.linkedGradeId }
                                Text(
                                    text = grade?.let { "${GradingScaleUtils.formatGrade(it.value, gradingScale)} en ${subject.cutScheme.cutName(it.cutId)}" } ?: stringResource(R.string.tasks_grade_recorded),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = LocalSectionColors.current.onTrack
                                )
                                TextButton(onClick = { onUnlinkGradeClick(task.id) }) {
                                    Text(stringResource(R.string.tasks_action_unlink_grade))
                                }
                            }
                            task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> {
                                Button(
                                    shapes = UniStackButtonDefaults.shapes,
                                    onClick = { onOpenGradeSheet(task) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Grade,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.tasks_record_grade_button))
                                }
                                TextButton(
                                    onClick = { onNoGradeClick(task.id) },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(stringResource(R.string.tasks_action_no_grade))
                                }
                            }
                            task.type.isGradable() -> {
                                val isWaiting = task.gradingStatus != TaskGradingStatus.NOT_GRADED
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .cleanClickable { onSetGradingDecision(task.id, true) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isWaiting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tasks_eval_awaiting_grade),
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isWaiting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        modifier = Modifier
                                            .weight(1f)
                                            .cleanClickable { onSetGradingDecision(task.id, false) },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (!isWaiting) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tasks_eval_only_done),
                                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isWaiting) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            else -> {
                                Text(
                                    text = stringResource(R.string.tasks_eval_not_gradable_desc, task.type.label()),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Descripción
            if (task.description.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_field_description).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
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
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = {
                        onDismiss()
                        onToggleComplete(task)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceContainerHighest else LocalSectionColors.current.onTrack,
                        contentColor = if (task.completed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surface
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
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(52.dp)
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(52.dp)
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
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.size(52.dp)
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
                            tint = MaterialTheme.colorScheme.error
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
    selectedDate: LocalDate?,
    onClick: () -> Unit
) {
    val label = filterSummaryLabel(
        selectedStatus = selectedStatus,
        selectedSubjectName = selectedSubjectName,
        selectedPriority = selectedPriority,
        sortOrder = sortOrder,
        selectedDate = selectedDate
    )

    Surface(
        modifier = Modifier.cleanClickable(onClick = onClick),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.tasks_open_filters),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$count",
            color = if (isOverdue) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
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
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FiltersSheetHeader(onClear = onClear)

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
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

                FilterSheetSection(title = stringResource(R.string.tasks_priority_title)) {
                    PrioritySegmentedControl(
                        selectedPriority = selectedPriority,
                        onPrioritySelected = onPrioritySelected
                    )
                }

                FilterSheetSection(title = stringResource(R.string.tasks_sort_title)) {
                    SortRadioGroup(
                        selected = sortOrder,
                        onSelected = onSortSelected
                    )
                }
            }

            FiltersSheetFooter(onDismiss = onDismiss)
        }
    }
}

@Composable
private fun FiltersSheetHeader(onClear: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.tasks_filter_title),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        TextButton(onClick = onClear) {
            Text(
                text = stringResource(R.string.tasks_filter_clear),
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.78f),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterSheetSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold
        )
        content()
    }
}

@Composable
private fun StatusFilterGrid(
    selectedStatus: TaskListFilter,
    onStatusSelected: (TaskListFilter) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        visibleStatusFilters.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowItems.forEach { filter ->
                    StatusFilterOption(
                        label = filter.label,
                        icon = filter.icon(),
                        selected = selectedStatus == filter,
                        onClick = { onStatusSelected(filter) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun StatusFilterOption(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.cleanClickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SubjectDropdownSelector(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onSubjectSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val allSubjectsLabel = stringResource(R.string.tasks_all_subjects)
    val selectedLabel = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: allSubjectsLabel

    FilterSheetSection(title = stringResource(R.string.tasks_field_subject)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .cleanClickable { expanded = !expanded },
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = selectedLabel,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                        modifier = Modifier.size(19.dp)
                    )
                }
            }

            if (expanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    tonalElevation = 0.dp,
                    shadowElevation = 0.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 240.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 6.dp)
                    ) {
                        SubjectDropdownRow(
                            label = allSubjectsLabel,
                            selected = selectedSubjectId == null,
                            onClick = {
                                onSubjectSelected(null)
                                expanded = false
                            }
                        )
                        subjects.forEach { subject ->
                            SubjectDropdownRow(
                                label = subject.name,
                                selected = selectedSubjectId == subject.id,
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
    }
}

@Composable
private fun SubjectDropdownRow(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PrioritySegmentedControl(
    selectedPriority: TaskDifficulty?,
    onPrioritySelected: (TaskDifficulty?) -> Unit
) {
    UniSegmentedControl(
        selected = selectedPriority,
        options = listOf<Pair<TaskDifficulty?, String>>(
            null to stringResource(R.string.tasks_priority_all),
            TaskDifficulty.EASY to stringResource(R.string.tasks_priority_low),
            TaskDifficulty.MEDIUM to stringResource(R.string.tasks_priority_medium),
            TaskDifficulty.HARD to stringResource(R.string.tasks_priority_high)
        ).map { (priority, label) ->
            UniSegmentedOption(
                value = priority,
                label = label,
                dotColor = priority?.color()
            )
        },
        onSelected = onPrioritySelected,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun SortRadioGroup(
    selected: TaskSortOrder,
    onSelected: (TaskSortOrder) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        TaskSortOrder.entries.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .cleanClickable { onSelected(option) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = selected == option,
                    onClick = { onSelected(option) },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = MaterialTheme.colorScheme.primary,
                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Text(
                    text = option.label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun FiltersSheetFooter(onDismiss: () -> Unit) {
    Button(
        shapes = UniStackButtonDefaults.shapes,
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            stringResource(R.string.tasks_filter_apply),
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private fun TaskListFilter.icon(): ImageVector {
    return when (this) {
        TaskListFilter.ALL -> Icons.Rounded.Check
        TaskListFilter.PENDING -> Icons.Rounded.Schedule
        TaskListFilter.COMPLETED -> Icons.Rounded.CheckCircle
        TaskListFilter.OVERDUE -> Icons.Rounded.CalendarMonth
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
    sortOrder: TaskSortOrder,
    selectedDate: LocalDate?
): String {
    val activeFilters = buildList {
        if (selectedStatus != TaskListFilter.ALL) add(selectedStatus.label)
        selectedSubjectName?.let { add(it) }
        selectedDate?.let { add(it.format(shortDateFormatter)) }
        selectedPriority?.let { add(it.shortLabel()) }
        if (sortOrder != TaskSortOrder.DUE_DATE) add(sortOrder.label)
    }
    return if (activeFilters.isEmpty()) {
        stringResource(R.string.tasks_filter_subject_all)
    } else {
        stringResource(R.string.tasks_filters_active, activeFilters.joinToString(" · "))
    }
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
