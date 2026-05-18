package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniEmptyStateCard
import com.unistack.app.core.design.components.UniFilterChipRow
import com.unistack.app.core.design.components.UniFilterOption
import com.unistack.app.core.design.components.UniScreenHeader
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty

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

    LaunchedEffect(subjects) {
        if (selectedSubjectId != null && subjects.none { it.id == selectedSubjectId }) {
            selectedSubjectId = null
        }
    }

    val filteredTasks = remember(tasks, selectedFilter, selectedSubjectId) {
        tasks
            .filter { task -> selectedFilter.matches(task) }
            .filter { task -> selectedSubjectId == null || task.subjectId == selectedSubjectId }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            UniScreenHeader(
                title = "Tareas",
                subtitle = "Organiza entregas, parciales y actividades académicas."
            )
        }
        item {
            UniFilterChipRow(
                options = TaskListFilter.entries.map { UniFilterOption(it, it.label) },
                selected = selectedFilter,
                onSelected = { selectedFilter = it }
            )
        }
        if (subjects.isNotEmpty()) {
            item {
                UniFilterChipRow(
                    options = listOf(UniFilterOption<String?>(null, "Todas")) +
                        subjects.map { UniFilterOption<String?>(it.id, it.name) },
                    selected = selectedSubjectId,
                    onSelected = { selectedSubjectId = it }
                )
            }
        }
        if (tasks.isEmpty()) {
            item {
                UniEmptyStateCard(
                    title = "Aún no tienes tareas reales.",
                    body = "Crea tu primera tarea para ver entregas, tiempos estimados y pendientes desde Home.",
                    icon = Icons.AutoMirrored.Rounded.EventNote,
                    actionText = "Nueva tarea",
                    onActionClick = onNewTaskClick,
                    color = UniStackColors.BlueLight,
                    iconColor = UniStackColors.Blue
                )
            }
        } else if (filteredTasks.isEmpty()) {
            item {
                UniEmptyStateCard(
                    title = "No hay tareas con este filtro.",
                    body = "Cambia el estado o la materia para revisar otros pendientes.",
                    icon = Icons.AutoMirrored.Rounded.EventNote,
                    color = UniStackColors.Card,
                    iconColor = UniStackColors.Blue
                )
            }
        } else {
            items(filteredTasks, key = { it.id }) { task ->
                TaskCard(
                    task = task,
                    subjects = subjects,
                    onCheckedChange = { checked -> viewModel.setTaskCompleted(task.id, checked) },
                    onEditClick = { onEditTaskClick(task.id) },
                    onDeleteClick = { taskIdPendingDelete = task.id }
                )
            }
        }
        item {
            Button(
                onClick = onNewTaskClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Nueva tarea")
            }
        }
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
private fun EmptyTasksCard() {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.BlueLight,
        shape = AppShapes.LargeCard,
        contentPadding = PaddingValues(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.AutoMirrored.Rounded.EventNote, contentDescription = null, tint = UniStackColors.Blue)
            Text("Aún no tienes tareas reales.", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(
                "Crea tu primera tarea para ver entregas, tiempos estimados y pendientes desde Home.",
                color = UniStackColors.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: StudentTask,
    subjects: List<Subject>,
    onCheckedChange: (Boolean) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val subjectName = task.subjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name } ?: "General"
    val titleDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
    val titleColor = if (task.completed) UniStackColors.TextSecondary else UniStackColors.TextPrimary

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.MediumCard
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = task.completed,
                onCheckedChange = onCheckedChange
            )
            Column(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    task.title,
                    color = titleColor,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = titleDecoration
                )
                Text(
                    "$subjectName · ${TaskDateUtils.dueText(task.dueDateMillis)} · ${TaskDateUtils.estimatedTimeText(task.estimatedMinutes)}",
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp
                )
                Text(
                    task.difficulty.label(),
                    color = task.difficulty.color(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onEditClick) {
                Icon(Icons.Rounded.Edit, contentDescription = "Editar tarea")
            }
            IconButton(onClick = onDeleteClick) {
                Icon(Icons.Rounded.Delete, contentDescription = "Eliminar tarea", tint = UniStackColors.Coral)
            }
        }
    }
}

private fun TaskDifficulty.label(): String {
    return when (this) {
        TaskDifficulty.EASY -> "Dificultad baja"
        TaskDifficulty.MEDIUM -> "Dificultad media"
        TaskDifficulty.HARD -> "Dificultad alta"
    }
}

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

private fun TaskDifficulty.color(): androidx.compose.ui.graphics.Color {
    return when (this) {
        TaskDifficulty.EASY -> UniStackColors.Green
        TaskDifficulty.MEDIUM -> UniStackColors.Yellow
        TaskDifficulty.HARD -> UniStackColors.Coral
    }
}
