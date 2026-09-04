@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_tasks.presentation

import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Today
import com.unistack.app.core.design.components.celebracionDelDia
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniSearchField
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.MetricCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.unistack.app.core.design.components.UniSwitch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.duracionDeDeshacer
import com.unistack.app.core.design.components.latidoDeVencido
import com.unistack.app.core.design.components.tachadoDe
import com.unistack.app.core.design.theme.motionActual
import com.unistack.app.feature_user.domain.StrikeMotion
import com.unistack.app.core.design.components.FilaDeslizable
import com.unistack.app.core.design.components.entradaDeLista
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.theme.anchoredButtonRoom
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.domain.GradingScale
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import java.util.Locale

import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNewTaskClick: () -> Unit,
    onEditTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onCompleteHistoryClick: (String) -> Unit = {},
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
    var completionPrompt by remember { mutableStateOf<TaskCompletionPrompt?>(null) }
    var historySuggestionSubjectId by remember { mutableStateOf<String?>(null) }
    var pendingGradesExpanded by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    // Se lee aqui y no dentro del `launch`: leer un CompositionLocal desde una corrutina no
    // compila, y ademas asi la duracion es la que habia al pintar la pantalla.
    val duracionDeDeshacer = duracionDeDeshacer()
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val clearSearchFocus = { focusManager.clearFocus() }
    /*
     * **Cerrar la ultima pendiente del dia se celebra.**
     *
     * La app no lo detectaba: completar una tarea era completar una tarea, la sexta o la
     * ultima. Era la mitad que faltaba del ajuste «Celebrar al terminar el dia», que se
     * guardaba sin tener a que aplicarse.
     *
     * Se mira **antes** de marcarla, y con lo que queda vencido tambien: terminar lo de hoy
     * dejando tres cosas atrasadas no es terminar el dia. Y solo al marcar, nunca al
     * desmarcar, que ahi no hay nada que celebrar.
     */
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
                    listOf(task.title, subjectName, task.type.label())
                        .any { it.lowercase(Locale.ROOT).contains(query) }
                }
            }
            .sortFor(sortOrder, subjects)
    }
    val today = TaskDateUtils.today()
    val activeTasks = filteredTasks.filterNot { it.completed }
    val awaitingGradeTasks = tasks.filter {
        it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
    }.sortedByDescending { it.completedAt ?: it.updatedAt }
    val completedTasks = filteredTasks.filter {
        it.completed && it.gradingStatus != TaskGradingStatus.AWAITING_GRADE
    }
    val pendingGradeCount = tasks.count {
        it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
    }
    val todayTasks = activeTasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == today }
    val overdueTasks = activeTasks.filter {
        TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today)
    }
    val upcomingTasks = activeTasks.filter {
        val dueDate = TaskDateUtils.fromMillis(it.dueDateMillis)
        dueDate.isAfter(today) && !dueDate.isAfter(today.plusDays(7))
    }
    val laterTasks = activeTasks.filter {
        TaskDateUtils.fromMillis(it.dueDateMillis).isAfter(today.plusDays(7))
    }
    val selectedSubjectName = subjects.firstOrNull { it.id == selectedSubjectId }?.name

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            // La celebracion se pinta sobre la pantalla entera y sin ocupar sitio: si la lista
            // diera un salto al aparecer, la celebracion seria una molestia.
            .celebracionDelDia(disparada = celebrando, onTerminada = { celebrando = false })
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(clearFocusOnScroll),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = if (embedded) 10.dp else 58.dp,
                end = 20.dp,
                // Misma regla que Materias: lo que tape la barra flotante más el hueco del
                // botón anclado. Con los 118dp fijos la lista podía quedarse justo por debajo
                // del umbral para desplazarse y el botón tapaba el último elemento sin salida.
                bottom = scrollBottomRoom + anchoredButtonRoom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (!embedded) {
                item {
                    TasksHeader()
                }
            }
            // Buscador, cifras y filtro solo aparecen si hay algo que buscar, contar o
            // filtrar. La condición mira `tasks`, no `filteredTasks`: si el usuario tiene
            // tareas pero un filtro las esconde, los controles deben seguir ahí para poder
            // deshacerlo. Lo que sobra es el andamiaje cuando no hay ni una sola tarea.
            if (tasks.isNotEmpty()) {
                item {
                    TaskSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        onSearchDone = clearSearchFocus
                    )
                }
                item {
                    TaskStatsRow(
                        tasks = tasks,
                        onUserInteraction = clearSearchFocus,
                        onTaskClick = onEditTaskClick
                    )
                }
            }
            if (pendingGradeCount > 0) {
                item {
                    PendingGradesBanner(
                        tasks = awaitingGradeTasks,
                        subjects = subjects,
                        expanded = pendingGradesExpanded,
                        onToggle = {
                            clearSearchFocus()
                            pendingGradesExpanded = !pendingGradesExpanded
                        },
                        onRegisterGradeClick = { task -> completionPrompt = TaskCompletionPrompt(task) },
                        onNoGradeClick = { task -> viewModel.markTaskAsNotGraded(task.id) },
                        onEditClick = { task ->
                            clearSearchFocus()
                            onEditTaskClick(task.id)
                        },
                        onDeleteClick = { task ->
                            clearSearchFocus()
                            taskIdPendingDelete = task.id
                        }
                    )
                }
            }
            if (tasks.isNotEmpty()) {
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
                    if (overdueTasks.isNotEmpty()) {
                        item { SectionTitle("Vencidas", overdueTasks.size) }
                        itemsIndexed(overdueTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskCard(
                                modifier = Modifier.entradaDeLista(indice)
                                    .latidoDeVencido(activo = true),
                                task = task,
                                subjects = subjects,
                                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    onTaskChecked(task, checked)
                                },
                                onRegisterGradeClick = {
                                    completionPrompt = TaskCompletionPrompt(task)
                                },
                                onNoGradeClick = { viewModel.markTaskAsNotGraded(task.id) },
                                onUnlinkGradeClick = { viewModel.unlinkTaskGrade(task.id) },
                                onEditClick = {
                                    clearSearchFocus()
                                    onEditTaskClick(task.id)
                                },
                                onDeleteClick = {
                                    clearSearchFocus()
                                    taskIdPendingDelete = task.id
                                }
                            )
                        }
                    }
                    if (todayTasks.isNotEmpty()) {
                        item { SectionTitle("Hoy", todayTasks.size) }
                        itemsIndexed(todayTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskCard(
                                modifier = Modifier.entradaDeLista(indice)
                                    .latidoDeVencido(activo = false),
                                task = task,
                                subjects = subjects,
                                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    onTaskChecked(task, checked)
                                },
                                onRegisterGradeClick = {
                                    completionPrompt = TaskCompletionPrompt(task)
                                },
                                onNoGradeClick = { viewModel.markTaskAsNotGraded(task.id) },
                                onUnlinkGradeClick = { viewModel.unlinkTaskGrade(task.id) },
                                onEditClick = {
                                    clearSearchFocus()
                                    onEditTaskClick(task.id)
                                },
                                onDeleteClick = {
                                    clearSearchFocus()
                                    taskIdPendingDelete = task.id
                                }
                            )
                        }
                    }
                    if (upcomingTasks.isNotEmpty()) {
                        item { SectionTitle("Próximas", upcomingTasks.size) }
                        itemsIndexed(upcomingTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskCard(
                                modifier = Modifier.entradaDeLista(indice)
                                    .latidoDeVencido(activo = false),
                                task = task,
                                subjects = subjects,
                                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    onTaskChecked(task, checked)
                                },
                                onRegisterGradeClick = {
                                    completionPrompt = TaskCompletionPrompt(task)
                                },
                                onNoGradeClick = { viewModel.markTaskAsNotGraded(task.id) },
                                onUnlinkGradeClick = { viewModel.unlinkTaskGrade(task.id) },
                                onEditClick = {
                                    clearSearchFocus()
                                    onEditTaskClick(task.id)
                                },
                                onDeleteClick = {
                                    clearSearchFocus()
                                    taskIdPendingDelete = task.id
                                }
                            )
                        }
                    }
                    if (laterTasks.isNotEmpty()) {
                        item { SectionTitle("Más adelante", laterTasks.size) }
                        itemsIndexed(laterTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskCard(
                                modifier = Modifier.entradaDeLista(indice)
                                    .latidoDeVencido(activo = false),
                                task = task,
                                subjects = subjects,
                                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    onTaskChecked(task, checked)
                                },
                                onRegisterGradeClick = {
                                    completionPrompt = TaskCompletionPrompt(task)
                                },
                                onNoGradeClick = { viewModel.markTaskAsNotGraded(task.id) },
                                onUnlinkGradeClick = { viewModel.unlinkTaskGrade(task.id) },
                                onEditClick = {
                                    clearSearchFocus()
                                    onEditTaskClick(task.id)
                                },
                                onDeleteClick = {
                                    clearSearchFocus()
                                    taskIdPendingDelete = task.id
                                }
                            )
                        }
                    }
                    if (completedTasks.isNotEmpty()) {
                        item { SectionTitle("Completadas", completedTasks.size) }
                        itemsIndexed(completedTasks, key = { _, it -> it.id }) { indice, task ->
                            TaskCard(
                                modifier = Modifier.entradaDeLista(indice)
                                    .latidoDeVencido(activo = false),
                                task = task,
                                subjects = subjects,
                                gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    onTaskChecked(task, checked)
                                },
                                onRegisterGradeClick = {
                                    completionPrompt = TaskCompletionPrompt(task)
                                },
                                onNoGradeClick = { viewModel.markTaskAsNotGraded(task.id) },
                                onUnlinkGradeClick = { viewModel.unlinkTaskGrade(task.id) },
                                onEditClick = {
                                    clearSearchFocus()
                                    onEditTaskClick(task.id)
                                },
                                onDeleteClick = {
                                    clearSearchFocus()
                                    taskIdPendingDelete = task.id
                                }
                            )
                        }
                    }
                }
            }
        }

        if (!embedded) {
            // Dentro de Académico la acción de crear la da el menú flotante, y tener los dos
            // ponía dos formas de crear una tarea encima una de otra.
            NewTaskFab(
                onClick = {
                    clearSearchFocus()
                    onNewTaskClick()
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    // Anclado, no desplazable: sin esto la barra flotante lo tapa siempre.
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
                                    message = "Nota registrada y tarea completada.",
                                    actionLabel = "Deshacer",
                                    // Cinco segundos por defecto, y hasta treinta si se han
                                    // pedido en Accesibilidad: un «Deshacer» que desaparece
                                    // antes de poder pulsarlo deja el borrado hecho.
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
            title = { Text("Tu historial puede mejorar la proyección") },
            text = {
                Text(
                    "Registraste la primera nota de ${subject?.name ?: "esta materia"} en un corte avanzado. " +
                        "Puedes agregar ahora las notas de los cortes anteriores o hacerlo más tarde."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        historySuggestionSubjectId = null
                        onCompleteHistoryClick(subjectId)
                    }
                ) { Text("Completar historial") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        historySuggestionSubjectId = null
                    }
                ) { Text("Más tarde") }
            }
        )
    }

    taskIdPendingDelete?.let { taskId ->
        UniConfirmDeleteDialog(
            title = "¿Eliminar tarea?",
            body = "Esta acción no se puede deshacer.",
            onConfirm = {
                viewModel.deleteTask(taskId)
                taskIdPendingDelete = null
            },
            onDismiss = { taskIdPendingDelete = null }
        )
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
                text = if (enteringGrade) "Registrar resultado" else "Tarea completada",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (enteringGrade) {
                    "${task.title} · ${subject.name}"
                } else {
                    "¿Recibiste una nota por “${task.title}”?"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!enteringGrade) {
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = { enteringGrade = true },
                    modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) { Text("Sí, registrar nota") }
                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Todavía no la recibo")
                }
                TextButton(onClick = onNoGrade, modifier = Modifier.fillMaxWidth()) {
                    Text("Esta tarea no recibe nota")
                }
            } else {
                Text("Corte", fontWeight = FontWeight.SemiBold)
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
                    label = { Text("Nota obtenida") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("No conozco el porcentaje", fontWeight = FontWeight.SemiBold)
                        Text(
                            "La nota se guardará sin alterar la proyección.",
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
                        label = { Text("Peso dentro del corte") },
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
                            error = "Revisa la nota y el porcentaje."
                        } else if (!onSaveGrade(value, percentage, selectedCutId)) {
                            error = "No fue posible guardar. Revisa el rango y el peso acumulado."
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
                ) { Text("Guardar nota") }
            }
        }
    }
}

@Composable
private fun TasksHeader() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Tareas",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Organiza tus entregas y actividades",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
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
        placeholder = "Buscar tarea, materia o tipo",
        onSearchDone = onSearchDone
    )
}

/**
 * Las tres cifras de Tareas, en las mismas tarjetas que Horario.
 *
 * Eran tres tarjetas de 104dp con tres líneas cada una —rótulo, cifra y un pie que repetía
 * «sin tareas»— y al tocarlas salía un diálogo que decía cuántas había hechas y cuántas
 * pendientes, sin enseñar ninguna. Ahora son la misma `MetricCard` de 58dp que usan Horario
 * e Inicio, y al tocarlas se abre la hoja con las tareas que hay detrás del número, cada una
 * tocable para abrirla, que es lo que hacen las de Horario.
 */
@Composable
private fun TaskStatsRow(
    tasks: List<StudentTask>,
    onUserInteraction: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    val today = TaskDateUtils.today()
    val activeTasks = tasks.filterNot { it.completed }
    val todayTasks = activeTasks.filter { TaskDateUtils.isToday(it.dueDateMillis, today) }
    val weekTasks = activeTasks.filter { task ->
        val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
        !dueDate.isBefore(today) && !dueDate.isAfter(today.plusDays(6))
    }
    val overdueTasks = activeTasks
        .filter { TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today) }
    var selectedStat by remember { mutableStateOf<TaskStatDetail?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Today,
            iconColor = LocalSectionColors.current.onTrack,
            value = todayTasks.size.toString(),
            label = "Hoy",
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Tareas de hoy",
                    description = "Lo que vence hoy y sigue sin marcar.",
                    tasks = todayTasks
                )
            }
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.DateRange,
            iconColor = LocalSectionColors.current.schedule,
            value = weekTasks.size.toString(),
            label = "Semana",
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Tareas de la semana",
                    description = "Lo que vence entre hoy y los próximos seis días.",
                    tasks = weekTasks
                )
            }
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.ErrorOutline,
            iconColor = MaterialTheme.colorScheme.error,
            value = overdueTasks.size.toString(),
            label = "Vencidas",
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Tareas vencidas",
                    description = "Pasó su fecha límite y siguen pendientes.",
                    tasks = overdueTasks
                )
            }
        )
    }

    selectedStat?.let { detail ->
        TaskStatSheet(
            detail = detail,
            onDismiss = { selectedStat = null },
            onTaskClick = { taskId ->
                selectedStat = null
                onTaskClick(taskId)
            }
        )
    }
}

/** Lo que hay detrás del número, en la misma hoja que usa Horario para lo mismo. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TaskStatSheet(
    detail: TaskStatDetail,
    onDismiss: () -> Unit,
    onTaskClick: (String) -> Unit
) {
    val ordered = remember(detail) { detail.tasks.sortedBy(StudentTask::dueDateMillis) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.Assignment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        detail.title,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        detail.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (ordered.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainer
                ) {
                    Text(
                        "Nada aquí. Cuando algo entre en este grupo, aparecerá en esta lista.",
                        modifier = Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = ordered, key = { it.id }) { task ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            onClick = { onTaskClick(task.id) }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(task.difficulty.color())
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(
                                    Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        task.title,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        TaskDateUtils.dueText(task.dueDateMillis),
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskFilterSummaryChip(
    selectedStatus: TaskListFilter,
    selectedSubjectName: String?,
    selectedPriority: TaskDifficulty?,
    sortOrder: TaskSortOrder,
    onClick: () -> Unit
) {
    val label = filterSummaryLabel(
        selectedStatus = selectedStatus,
        selectedSubjectName = selectedSubjectName,
        selectedPriority = selectedPriority,
        sortOrder = sortOrder
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
                contentDescription = "Abrir filtros",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$count",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PendingGradesBanner(
    tasks: List<StudentTask>,
    subjects: List<Subject>,
    expanded: Boolean,
    onToggle: () -> Unit,
    onRegisterGradeClick: (StudentTask) -> Unit,
    onNoGradeClick: (StudentTask) -> Unit,
    onEditClick: (StudentTask) -> Unit,
    onDeleteClick: (StudentTask) -> Unit
) {
    val count = tasks.size
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.cleanClickable(onClick = onToggle),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Grade,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(8.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (count == 1) "1 resultado pendiente" else "$count resultados pendientes",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!expanded) {
                        Text(
                            text = "Toca para registrar o descartar resultados",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                Text(
                    text = if (expanded) "Ocultar" else "Revisar",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = if (expanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                    contentDescription = if (expanded) "Ocultar resultados pendientes" else "Ver resultados pendientes",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (expanded) {
                tasks.forEachIndexed { index, task ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 46.dp, top = 6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f)
                        )
                    }
                    PendingGradeTaskRow(
                        task = task,
                        subjectName = task.subjectId
                            ?.let { id -> subjects.firstOrNull { it.id == id }?.name }
                            ?: "Sin materia",
                        onRegisterGradeClick = { onRegisterGradeClick(task) },
                        onNoGradeClick = { onNoGradeClick(task) },
                        onEditClick = { onEditClick(task) },
                        onDeleteClick = { onDeleteClick(task) }
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingGradeTaskRow(
    task: StudentTask,
    subjectName: String,
    onRegisterGradeClick: () -> Unit,
    onNoGradeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    var menuExpanded by remember(task.id) { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 9.dp, bottom = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = task.title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$subjectName · ${TaskDateUtils.dueText(task.dueDateMillis)}",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        TextButton(onClick = onRegisterGradeClick) {
            Text("Registrar", fontWeight = FontWeight.Bold)
        }
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(IconButtonDefaults.smallContainerSize())) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones")
            }
            UniDropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Editar tarea") },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onEditClick()
                    }
                )
                DropdownMenuItem(
                    text = { Text("No tuvo nota") },
                    leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                    onClick = {
                        menuExpanded = false
                        onNoGradeClick()
                    }
                )
                // Eliminar va detrás de una línea, y no seguido de lo demás.
                //
                // Las tres opciones tenían el mismo peso y estaban pegadas, así que el dedo
                // que iba a «No tuvo nota» caía en «Eliminar» con un centímetro de error. La
                // línea no impide el toque, pero rompe la lista en dos y obliga a mirar.
                HorizontalDivider(Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text("Eliminar") },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                    colors = MenuDefaults.itemColors(
                        textColor = MaterialTheme.colorScheme.error,
                        leadingIconColor = MaterialTheme.colorScheme.error
                    ),
                    onClick = {
                        menuExpanded = false
                        onDeleteClick()
                    }
                )
            }
        }
    }
}

/**
 * La tarea de la lista, con el arrastre para borrar por delante.
 *
 * La envoltura va aqui y no en cada seccion de la pantalla: son cinco listas —vencidas, de hoy,
 * proximas, sin fecha y completadas— y con el gesto puesto en cada una habria cinco sitios que
 * mantener y cuatro donde olvidarse.
 */
@Composable
private fun TaskCard(
    task: StudentTask,
    subjects: List<Subject>,
    gradingScale: GradingScale,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    onRegisterGradeClick: () -> Unit,
    onNoGradeClick: () -> Unit,
    onUnlinkGradeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    FilaDeslizable(onBorrar = onDeleteClick, modifier = modifier) {
        TarjetaDeTarea(
            task = task,
            subjects = subjects,
            gradingScale = gradingScale,
            onCardClick = onCardClick,
            onCheckedChange = onCheckedChange,
            onRegisterGradeClick = onRegisterGradeClick,
            onNoGradeClick = onNoGradeClick,
            onUnlinkGradeClick = onUnlinkGradeClick,
            onEditClick = onEditClick,
            onDeleteClick = onDeleteClick
        )
    }
}

@Composable
private fun TarjetaDeTarea(
    task: StudentTask,
    subjects: List<Subject>,
    gradingScale: GradingScale,
    onCardClick: () -> Unit,
    /** La entrada de la lista y, si esta vencida, su latido. Sale de Movimiento. */
    modifier: Modifier = Modifier,
    onCheckedChange: (Boolean) -> Unit,
    onRegisterGradeClick: () -> Unit,
    onNoGradeClick: () -> Unit,
    onUnlinkGradeClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val subject = task.subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val subjectName = subject?.name ?: "Sin materia"
    val linkedGrade = task.linkedGradeId?.let { gradeId ->
        subject?.grades?.firstOrNull { it.id == gradeId }
    }
    val gradeSummary = linkedGrade?.let { grade ->
        val cutName = subject?.cutScheme?.cutName(grade.cutId) ?: "Corte"
        "${GradingScaleUtils.formatGrade(grade.value, gradingScale)} · $cutName"
    }
    val awaitingGrade = task.completed && task.gradingStatus == TaskGradingStatus.AWAITING_GRADE
    val titleDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
    val contentAlpha = if (task.completed && !awaitingGrade) 0.78f else 1f
    val priorityColor = task.difficulty.color()
    var menuExpanded by remember(task.id) { mutableStateOf(false) }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (awaitingGrade) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        tonalElevation = 0.dp,
        onClick = onCardClick,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            if (awaitingGrade) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(4.dp)
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(start = if (awaitingGrade) 6.dp else 10.dp, top = 12.dp),
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Column(
                modifier = Modifier
                    .padding(start = 4.dp, top = 15.dp, bottom = 15.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (awaitingGrade) {
                    Surface(
                        shape = RoundedCornerShape(7.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "ENTREGADA · ESPERANDO NOTA",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                /*
                 * **El tachado, con la forma elegida en Movimiento.**
                 *
                 * `tachadoDe` existía desde que se hizo el catálogo y no lo llamaba nadie: la
                 * tarea completada salía con la raya de siempre eligieras lo que eligieras.
                 *
                 * Con un estilo puesto se quita la `textDecoration`, porque si no se pintan
                 * las dos: la raya del sistema **y** la del gesto, cruzadas.
                 */
                val estiloTachado = motionActual().strikeThrough
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (estiloTachado == StrikeMotion.NINGUNA) {
                        titleDecoration
                    } else {
                        TextDecoration.None
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.tachadoDe(
                        completado = task.completed,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                )
                if (task.description.isNotBlank()) {
                    Text(
                        text = task.description,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha * 0.85f),
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$subjectName · ${taskDueLabel(task.dueDateMillis)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Text(
                        text = task.difficulty.shortLabel(),
                        color = priorityColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "· ${task.type.label()}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                when (task.gradingStatus) {
                    TaskGradingStatus.AWAITING_GRADE -> {
                        if (awaitingGrade) {
                            Button(
                                shapes = UniStackButtonDefaults.shapes,
                                onClick = onRegisterGradeClick,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Grade,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Registrar nota",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    TaskGradingStatus.GRADED -> {
                        Text(
                            text = gradeSummary?.let { "Nota $it" } ?: "Nota registrada",
                            color = LocalSectionColors.current.onTrack,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    else -> Unit
                }
            }
            Box(modifier = Modifier.padding(top = 8.dp, end = 6.dp)) {
                UniIconButton(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = "Más opciones de ${task.title}",
                    onClick = { menuExpanded = true }
                )
                UniDropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = {
                            Icon(Icons.Rounded.Edit, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onEditClick()
                        }
                    )
                    if (task.gradingStatus == TaskGradingStatus.AWAITING_GRADE) {
                        DropdownMenuItem(
                            text = { Text("No tuvo nota") },
                            leadingIcon = {
                                Icon(Icons.Rounded.CheckCircle, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onNoGradeClick()
                            }
                        )
                    }
                    if (task.gradingStatus == TaskGradingStatus.GRADED) {
                        DropdownMenuItem(
                            text = { Text("Desvincular nota") },
                            leadingIcon = {
                                Icon(Icons.Rounded.Grade, contentDescription = null)
                            },
                            onClick = {
                                menuExpanded = false
                                onUnlinkGradeClick()
                            }
                        )
                    }
                    HorizontalDivider(Modifier.padding(vertical = 4.dp))
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
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
                text = "Aún no tienes tareas.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Crea tu primera tarea para organizar fechas, materias y prioridades.",
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
                text = "No hay tareas con estos filtros.",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Ajusta estado, materia o prioridad para revisar otros pendientes.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
            TextButton(onClick = onOpenFilters) {
                Text("Cambiar filtros", fontWeight = FontWeight.Bold)
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
                text = "Nueva tarea",
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
            FiltersSheetHeader(
                onClear = onClear
            )

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                FilterSheetSection(title = "Estado") {
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

                FilterSheetSection(title = "Prioridad") {
                    PrioritySegmentedControl(
                        selectedPriority = selectedPriority,
                        onPrioritySelected = onPrioritySelected
                    )
                }

                FilterSheetSection(title = "Ordenar por") {
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
            text = "Filtros",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.ExtraBold
        )
        TextButton(onClick = onClear) {
            Text(
                text = "Limpiar",
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
    val selectedLabel = subjects.firstOrNull { it.id == selectedSubjectId }?.name ?: "Todas las materias"

    FilterSheetSection(title = "Materia") {
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
                            label = "Todas las materias",
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
    // Grupo conectado, como los demás selectores de uno entre varios. Era una caja con
    // cuatro cajas dentro, y la elegida se pintaba con el acento al 88 %.
    UniSegmentedControl(
        selected = selectedPriority,
        options = listOf<Pair<TaskDifficulty?, String>>(
            null to "Todas",
            TaskDifficulty.EASY to "Baja",
            TaskDifficulty.MEDIUM to "Media",
            TaskDifficulty.HARD to "Alta"
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
            "Ver resultados",
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
            compareBy<StudentTask> { task -> subjects.firstOrNull { it.id == task.subjectId }?.name ?: "Sin materia" }
                .thenBy { it.dueDateMillis }
        )
        TaskSortOrder.RECENT -> sortedByDescending { it.createdAt }
    }
}

private fun taskDueLabel(dueDateMillis: Long): String {
    val dueDate = TaskDateUtils.fromMillis(dueDateMillis)
    val today = TaskDateUtils.today()
    return when (dueDate) {
        today -> "Hoy"
        today.plusDays(1) -> "Mañana"
        else -> dueDate.format(shortDateFormatter)
    }
}

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es-CO"))

private fun TaskDifficulty.shortLabel(): String {
    return when (this) {
        TaskDifficulty.EASY -> "Baja"
        TaskDifficulty.MEDIUM -> "Media"
        TaskDifficulty.HARD -> "Alta"
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

private fun TaskType.label(): String {
    return when (this) {
        TaskType.WORKSHOP -> "Taller"
        TaskType.EXAM -> "Parcial"
        TaskType.ESSAY -> "Ensayo"
        TaskType.PRESENTATION -> "Exposición"
        TaskType.RESEARCH -> "Investigación"
        TaskType.TEST -> "Examen"
        TaskType.PRACTICE -> "Práctica"
        TaskType.PROJECT -> "Proyecto"
        TaskType.READING -> "Lectura"
        TaskType.OTHER -> "Otro"
    }
}

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
        "Materia: Todas"
    } else {
        "Filtros: ${activeFilters.joinToString(" · ")}"
    }
}

private data class TaskStatDetail(
    val title: String,
    val description: String,
    val tasks: List<StudentTask>
)

private val visibleStatusFilters = listOf(
    TaskListFilter.ALL,
    TaskListFilter.PENDING,
    TaskListFilter.COMPLETED,
    TaskListFilter.OVERDUE
)

private enum class TaskListFilter(val label: String) {
    ALL("Todas"),
    PENDING("Pendientes"),
    OVERDUE("Vencidas"),
    COMPLETED("Completadas");

    fun matches(task: StudentTask): Boolean {
        return when (this) {
            ALL -> true
            PENDING -> !task.completed
            OVERDUE -> !task.completed && TaskDateUtils.fromMillis(task.dueDateMillis).isBefore(TaskDateUtils.today())
            COMPLETED -> task.completed
        }
    }
}

private enum class TaskSortOrder(val label: String) {
    DUE_DATE("Fecha de vencimiento"),
    PRIORITY("Prioridad"),
    SUBJECT("Materia"),
    RECENT("Más recientes")
}
