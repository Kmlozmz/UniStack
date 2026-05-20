package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    onNewTaskClick: () -> Unit,
    onEditTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    var taskIdPendingDelete by remember { mutableStateOf<String?>(null) }
    var selectedFilter by remember { mutableStateOf(TaskListFilter.ALL) }
    var selectedSubjectId by remember { mutableStateOf<String?>(null) }
    var selectedPriority by remember { mutableStateOf<TaskDifficulty?>(null) }
    var sortOrder by remember { mutableStateOf(TaskSortOrder.DUE_DATE) }
    var showFiltersSheet by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val clearSearchFocus = { focusManager.clearFocus() }
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
        val query = searchQuery.trim().lowercase(Locale.getDefault())
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
                        .any { it.lowercase(Locale.getDefault()).contains(query) }
                }
            }
            .sortFor(sortOrder, subjects)
    }
    val today = TaskDateUtils.today()
    val todayTasks = filteredTasks.filter { TaskDateUtils.fromMillis(it.dueDateMillis) == today }
    val overdueTasks = filteredTasks.filter {
        !it.completed && TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today)
    }
    val upcomingTasks = filteredTasks.filter {
        val dueDate = TaskDateUtils.fromMillis(it.dueDateMillis)
        dueDate.isAfter(today) || (it.completed && dueDate.isBefore(today))
    }
    val selectedSubjectName = subjects.firstOrNull { it.id == selectedSubjectId }?.name

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(clearFocusOnScroll),
            contentPadding = PaddingValues(start = 20.dp, top = 24.dp, end = 20.dp, bottom = 118.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TasksHeader()
            }
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
                    onUserInteraction = clearSearchFocus
                )
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
            }
            when {
                tasks.isEmpty() -> item {
                    TasksEmptyState()
                }
                filteredTasks.isEmpty() -> item {
                    FilteredEmptyState(onOpenFilters = { showFiltersSheet = true })
                }
                else -> {
                    if (todayTasks.isNotEmpty()) {
                        item { SectionTitle("Hoy") }
                        items(todayTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                subjects = subjects,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    clearSearchFocus()
                                    viewModel.setTaskCompleted(task.id, checked)
                                },
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
                        item { SectionTitle("Próximamente") }
                        items(upcomingTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                subjects = subjects,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    clearSearchFocus()
                                    viewModel.setTaskCompleted(task.id, checked)
                                },
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
                    if (overdueTasks.isNotEmpty()) {
                        item { SectionTitle("Vencidas") }
                        items(overdueTasks, key = { it.id }) { task ->
                            TaskCard(
                                task = task,
                                subjects = subjects,
                                onCardClick = clearSearchFocus,
                                onCheckedChange = { checked ->
                                    clearSearchFocus()
                                    viewModel.setTaskCompleted(task.id, checked)
                                },
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
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        singleLine = true,
        shape = RoundedCornerShape(20.dp),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { onSearchDone() },
            onDone = { onSearchDone() }
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        placeholder = {
            Text(
                text = "Buscar tarea, materia o tipo",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold
            )
        }
    )
}

@Composable
private fun TaskStatsRow(
    tasks: List<StudentTask>,
    onUserInteraction: () -> Unit
) {
    val today = TaskDateUtils.today()
    val todayTasks = tasks.filter { TaskDateUtils.isToday(it.dueDateMillis, today) }
    val weekTasks = tasks.filter { task ->
        val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
        !dueDate.isBefore(today) && !dueDate.isAfter(today.plusDays(6))
    }
    val overdueTasks = tasks.filterNot { it.completed }
        .filter { TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(today) }
    var selectedStat by remember { mutableStateOf<TaskStatDetail?>(null) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskStatCard(
            label = "Hoy",
            value = todayTasks.size.toString(),
            supportingText = pendingText(todayTasks),
            accent = UniStackColors.Green,
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Hoy",
                    description = "Aquí verás el número de tareas que tienes para hoy.",
                    tasks = todayTasks
                )
            },
            modifier = Modifier.weight(1f)
        )
        TaskStatCard(
            label = "Semana",
            value = weekTasks.size.toString(),
            supportingText = pendingText(weekTasks),
            accent = UniStackColors.Blue,
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Semana",
                    description = "Aquí verás las tareas que vencen entre hoy y los próximos 6 días.",
                    tasks = weekTasks
                )
            },
            modifier = Modifier.weight(1f)
        )
        TaskStatCard(
            label = "Vencidas",
            value = overdueTasks.size.toString(),
            supportingText = "pendientes",
            accent = UniStackColors.Coral,
            onClick = {
                onUserInteraction()
                selectedStat = TaskStatDetail(
                    title = "Vencidas",
                    description = "Aquí verás las tareas pendientes que ya pasaron de su fecha límite.",
                    tasks = overdueTasks
                )
            },
            modifier = Modifier.weight(1f)
        )
    }

    selectedStat?.let { detail ->
        TaskStatDialog(
            detail = detail,
            onDismiss = { selectedStat = null }
        )
    }
}

@Composable
private fun TaskStatCard(
    label: String,
    value: String,
    supportingText: String,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(104.dp)
            .cleanClickable(onClick),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                color = accent,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = supportingText,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskStatDialog(
    detail: TaskStatDetail,
    onDismiss: () -> Unit
) {
    val done = detail.tasks.count { it.completed }
    val pending = detail.tasks.size - done

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = detail.title,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = detail.description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Total: ${detail.tasks.size}",
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hechas: $done",
                        color = UniStackColors.Green,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pendientes: $pending",
                        color = if (pending > 0) UniStackColors.Coral else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Entendido", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
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
    val label = filterSummaryLabel(
        selectedStatus = selectedStatus,
        selectedSubjectName = selectedSubjectName,
        selectedPriority = selectedPriority,
        sortOrder = sortOrder
    )

    Surface(
        modifier = Modifier.cleanClickable(onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
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
private fun SectionTitle(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.ExtraBold
    )
}

@Composable
private fun TaskCard(
    task: StudentTask,
    subjects: List<Subject>,
    onCardClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val subjectName = task.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name } ?: "Sin materia"
    val titleDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
    val contentAlpha = if (task.completed) 0.62f else 1f
    val priorityColor = task.difficulty.color()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onCardClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline
                )
            )
            Column(
                modifier = Modifier
                    .padding(start = 6.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = task.title,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = titleDecoration,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$subjectName · ${taskDueLabel(task.dueDateMillis)}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = contentAlpha),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
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
            }
            IconButton(onClick = onEditClick) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "Editar tarea",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Eliminar tarea",
                    tint = UniStackColors.Coral
                )
            }
        }
    }
}

@Composable
private fun TasksEmptyState() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
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
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
        text = { Text("Nueva tarea", fontWeight = FontWeight.ExtraBold) },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        shape = RoundedCornerShape(22.dp)
    )
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
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .size(width = 44.dp, height = 4.dp)
                    .clip(RoundedCornerShape(100.dp))
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
        modifier = modifier.cleanClickable(onClick),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.34f),
        border = BorderStroke(
            width = 0.8.dp,
            color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.42f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)
        )
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
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(0.9.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.30f))
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
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f)),
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
            .cleanClickable(onClick)
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
    val options = listOf<Pair<TaskDifficulty?, String>>(
        null to "Todas",
        TaskDifficulty.EASY to "Baja",
        TaskDifficulty.MEDIUM to "Media",
        TaskDifficulty.HARD to "Alta"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.14f))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            options.forEach { (priority, label) ->
                val selected = selectedPriority == priority
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(3.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.88f) else Color.Transparent)
                        .cleanClickable { onPrioritySelected(priority) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        priority?.let {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(it.color())
                            )
                        }
                        Text(
                            text = label,
                            color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
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
        onClick = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Text(
            "Ver resultados",
            color = Color.White,
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

private val shortDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", Locale("es", "CO"))

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

private fun TaskDifficulty.color(): Color {
    return when (this) {
        TaskDifficulty.EASY -> UniStackColors.Green
        TaskDifficulty.MEDIUM -> UniStackColors.Yellow
        TaskDifficulty.HARD -> UniStackColors.Coral
    }
}

private fun TaskType.label(): String {
    return when (this) {
        TaskType.WORKSHOP -> "Taller"
        TaskType.EXAM -> "Parcial"
        TaskType.ESSAY -> "Ensayo"
        TaskType.PRESENTATION -> "Exposición"
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

private fun pendingText(tasks: List<StudentTask>): String {
    val pending = tasks.count { !it.completed }
    return when {
        tasks.isEmpty() -> "sin tareas"
        pending == 0 -> "todo listo"
        pending == 1 -> "1 pendiente"
        else -> "$pending pendientes"
    }
}

private data class TaskStatDetail(
    val title: String,
    val description: String,
    val tasks: List<StudentTask>
)

@Composable
private fun Modifier.cleanClickable(onClick: () -> Unit): Modifier {
    return clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onClick
    )
}

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
