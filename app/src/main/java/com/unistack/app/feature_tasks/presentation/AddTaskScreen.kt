package com.unistack.app.feature_tasks.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.EventNote
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty

@Composable
fun AddTaskScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel(),
    taskId: String? = null
) {
    BackHandler(onBack = onBackClick)

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val task = taskId?.let { id -> tasks.firstOrNull { it.id == id } }
    val isEditing = taskId != null

    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var dueDate by rememberSaveable(taskId) { mutableStateOf(TaskDateUtils.formatInput(TaskDateUtils.today())) }
    var estimatedMinutes by rememberSaveable(taskId) { mutableStateOf("60") }
    var selectedSubjectId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var difficulty by rememberSaveable(taskId) { mutableStateOf(TaskDifficulty.MEDIUM) }
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }
    var error by rememberSaveable(taskId) { mutableStateOf<String?>(null) }

    val titleValidation = TextValidators.validateActivityName(title)
    val isTitleValid = title.isBlank() || titleValidation.isValid
    val parsedDueDate = TaskDateUtils.parseInput(dueDate)
    val parsedMinutes = estimatedMinutes.toIntOrNull()
    val isValid = (!isEditing || task != null) &&
        titleValidation.isValid &&
        parsedDueDate != null &&
        parsedMinutes != null &&
        parsedMinutes in 1..1440

    LaunchedEffect(task?.id, taskId) {
        if (initialized) return@LaunchedEffect
        if (task != null) {
            title = task.title
            dueDate = TaskDateUtils.formatInput(TaskDateUtils.fromMillis(task.dueDateMillis))
            estimatedMinutes = task.estimatedMinutes.toString()
            selectedSubjectId = task.subjectId
            difficulty = task.difficulty
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Text(
            if (isEditing) "Editar tarea" else "Nueva tarea",
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.BlueLight,
            shape = AppShapes.LargeCard,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Icon(Icons.AutoMirrored.Rounded.EventNote, contentDescription = null, tint = UniStackColors.Blue)
                if (isEditing && task == null) {
                    Text("Tarea no encontrada.", color = UniStackColors.TextSecondary)
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it.take(40)
                        error = null
                    },
                    label = { Text("Actividad") },
                    placeholder = { Text("Entrega, parcial, lectura...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = !isTitleValid,
                    supportingText = {
                        if (!isTitleValid) {
                            Text(titleValidation.errorMessage ?: "Ingresa una actividad válida")
                        }
                    }
                )
                OutlinedTextField(
                    value = dueDate,
                    onValueChange = {
                        dueDate = it.take(10)
                        error = null
                    },
                    label = { Text("Fecha límite") },
                    placeholder = { Text("YYYY-MM-DD") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = dueDate.isNotBlank() && parsedDueDate == null,
                    supportingText = {
                        if (dueDate.isNotBlank() && parsedDueDate == null) {
                            Text("Usa el formato YYYY-MM-DD")
                        }
                    }
                )
                OutlinedTextField(
                    value = estimatedMinutes,
                    onValueChange = {
                        estimatedMinutes = it.take(4)
                        error = null
                    },
                    label = { Text("Tiempo estimado en minutos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.MediumCard,
                    isError = estimatedMinutes.isNotBlank() && (parsedMinutes == null || parsedMinutes !in 1..1440)
                )
                Text("Materia", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                SubjectSelectorRow(
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    onSubjectSelected = {
                        selectedSubjectId = it
                        error = null
                    }
                )
                Text("Dificultad", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                DifficultySelector(
                    selected = difficulty,
                    onSelected = {
                        difficulty = it
                        error = null
                    }
                )
                error?.let {
                    Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            }
        }
        Button(
            onClick = {
                val saved = if (isEditing && taskId != null) {
                    viewModel.updateTask(
                        taskId = taskId,
                        title = title,
                        subjectId = selectedSubjectId,
                        dueDateInput = dueDate,
                        estimatedMinutesInput = estimatedMinutes,
                        difficulty = difficulty
                    )
                } else {
                    viewModel.addTask(
                        title = title,
                        subjectId = selectedSubjectId,
                        dueDateInput = dueDate,
                        estimatedMinutesInput = estimatedMinutes,
                        difficulty = difficulty
                    )
                }

                if (saved) {
                    onBackClick()
                } else {
                    error = "Revisa la actividad, fecha y tiempo estimado antes de guardar."
                }
            },
            enabled = isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditing) "Guardar cambios" else "Crear tarea")
        }
    }
}

@Composable
private fun SubjectSelectorRow(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onSubjectSelected: (String?) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            SelectionChip(
                text = "General",
                selected = selectedSubjectId == null,
                onClick = { onSubjectSelected(null) }
            )
        }
        items(subjects, key = { it.id }) { subject ->
            SelectionChip(
                text = subject.name,
                selected = selectedSubjectId == subject.id,
                onClick = { onSubjectSelected(subject.id) }
            )
        }
    }
}

@Composable
private fun DifficultySelector(
    selected: TaskDifficulty,
    onSelected: (TaskDifficulty) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TaskDifficulty.values().forEach { difficulty ->
            SelectionChip(
                text = difficulty.label(),
                selected = selected == difficulty,
                onClick = { onSelected(difficulty) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SelectionChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.bounceClick(onClick),
        color = if (selected) UniStackColors.BlueLight else UniStackColors.Card,
        shape = AppShapes.Pill,
        tonalElevation = if (selected) 5.dp else 1.dp,
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            text = text,
            color = if (selected) UniStackColors.Blue else UniStackColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

private fun TaskDifficulty.label(): String {
    return when (this) {
        TaskDifficulty.EASY -> "Baja"
        TaskDifficulty.MEDIUM -> "Media"
        TaskDifficulty.HARD -> "Alta"
    }
}
