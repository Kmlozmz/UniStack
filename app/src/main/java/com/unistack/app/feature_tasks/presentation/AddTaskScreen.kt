package com.unistack.app.feature_tasks.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AddTaskScreen(
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
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
    var dueDate by rememberSaveable(taskId) { mutableStateOf("") }
    var selectedSubjectId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var selectedType by rememberSaveable(taskId) { mutableStateOf(TaskType.WORKSHOP) }
    var difficulty by rememberSaveable(taskId) { mutableStateOf(TaskDifficulty.MEDIUM) }
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }
    var error by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    val estimatedMinutes = "60"

    val titleValidation = TextValidators.validateActivityName(title)
    val isTitleValid = title.isBlank() || titleValidation.isValid
    val parsedDueDate = TaskDateUtils.parseInput(dueDate)
    val isValid = (!isEditing || task != null) &&
        titleValidation.isValid &&
        parsedDueDate != null

    LaunchedEffect(task?.id, taskId) {
        if (initialized) return@LaunchedEffect
        if (task != null) {
            title = task.title
            dueDate = TaskDateUtils.formatInput(TaskDateUtils.fromMillis(task.dueDateMillis))
            selectedSubjectId = task.subjectId
            selectedType = task.type
            difficulty = task.difficulty
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    AddTaskContent(
        title = title,
        isEditing = isEditing,
        taskMissing = isEditing && task == null,
        titleIsValid = isTitleValid,
        titleError = titleValidation.errorMessage,
        dueDateLabel = parsedDueDate?.let { TaskDateUtils.dueText(TaskDateUtils.toMillis(it)) }.orEmpty(),
        subjects = subjects,
        selectedSubjectId = selectedSubjectId,
        selectedType = selectedType,
        selectedPriority = difficulty,
        isSaveEnabled = isValid,
        error = error,
        onBackClick = onBackClick,
        onTitleChange = {
            title = it.take(40)
            error = null
        },
        onDateClick = {
            showDatePicker = true
            error = null
        },
        onTypeSelected = {
            selectedType = it
            error = null
        },
        onSubjectSelected = {
            selectedSubjectId = it
            error = null
        },
        onCreateSubjectClick = onCreateSubjectClick,
        onPrioritySelected = {
            difficulty = it
            error = null
        },
        onSaveClick = {
            val editingTaskId = taskId
            val saved = if (editingTaskId != null) {
                viewModel.updateTask(
                    taskId = editingTaskId,
                    title = title,
                    subjectId = selectedSubjectId,
                    type = selectedType,
                    dueDateInput = dueDate,
                    estimatedMinutesInput = estimatedMinutes,
                    difficulty = difficulty
                )
            } else {
                viewModel.addTask(
                    title = title,
                    subjectId = selectedSubjectId,
                    type = selectedType,
                    dueDateInput = dueDate,
                    estimatedMinutesInput = estimatedMinutes,
                    difficulty = difficulty
                )
            }

            if (saved) {
                onBackClick()
            } else {
                error = "Revisa la actividad y la fecha antes de guardar."
            }
        },
        modifier = modifier
    )

    if (showDatePicker) {
        MonthCalendarDialog(
            selectedDate = parsedDueDate,
            onDateSelected = { selectedDate ->
                dueDate = TaskDateUtils.formatInput(selectedDate)
                error = null
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun AddTaskContent(
    title: String,
    isEditing: Boolean,
    taskMissing: Boolean,
    titleIsValid: Boolean,
    titleError: String?,
    dueDateLabel: String,
    subjects: List<Subject>,
    selectedSubjectId: String?,
    selectedType: TaskType,
    selectedPriority: TaskDifficulty,
    isSaveEnabled: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTypeSelected: (TaskType) -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onCreateSubjectClick: () -> Unit,
    onPrioritySelected: (TaskDifficulty) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        TaskHeader(
            title = if (isEditing) "Editar tarea" else "Nueva tarea",
            subtitle = "Agrega los detalles principales de tu actividad.",
            onBackClick = onBackClick
        )
        FormSection(title = "Información básica") {
            BasicInfoCard(
                title = title,
                titleIsValid = titleIsValid,
                titleError = titleError,
                dueDateLabel = dueDateLabel,
                subjects = subjects,
                selectedSubjectId = selectedSubjectId,
                onTitleChange = onTitleChange,
                onDateClick = onDateClick,
                onSubjectSelected = onSubjectSelected,
                onCreateSubjectClick = onCreateSubjectClick
            )
        }
        FormSection(title = "Tipo de tarea") {
            TaskTypeSelector(
                selected = selectedType,
                onSelected = onTypeSelected
            )
        }
        FormSection(title = "Prioridad") {
            PrioritySegmentedControl(
                selected = selectedPriority,
                onSelected = onPrioritySelected
            )
        }
        if (taskMissing) {
            Text("Tarea no encontrada.", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
        }
        error?.let {
            Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
        }
        CreateTaskButton(
            text = if (isEditing) "Guardar tarea" else "Crear tarea",
            enabled = isSaveEnabled,
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun TaskHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                    shape = AppShapes.Pill
                )
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(title)
        content()
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 24.dp)
                .background(MaterialTheme.colorScheme.primary, AppShapes.Pill)
        )
        Text(
            text = text.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.4.sp
        )
    }
}

@Composable
private fun FormSectionCard(content: @Composable ColumnScope.() -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.16f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(content = content)
    }
}

@Composable
private fun BasicInfoCard(
    title: String,
    titleIsValid: Boolean,
    titleError: String?,
    dueDateLabel: String,
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onCreateSubjectClick: () -> Unit
) {
    FormSectionCard {
        TaskNameRow(
            value = title,
            onValueChange = onTitleChange,
            isValid = titleIsValid,
            error = titleError
        )
        FormDivider()
        BasicInfoActionRow(
            icon = Icons.Rounded.CalendarMonth,
            label = "Fecha límite",
            value = dueDateLabel,
            placeholder = "Seleccionar fecha",
            onClick = onDateClick
        )
        FormDivider()
        SubjectDropdown(
            subjects = subjects,
            selectedSubjectId = selectedSubjectId,
            onCreateSubjectClick = onCreateSubjectClick,
            onSubjectSelected = onSubjectSelected
        )
    }
}

@Composable
private fun FormDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 64.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.13f)
    )
}

@Composable
private fun TaskNameRow(
    value: String,
    onValueChange: (String) -> Unit,
    isValid: Boolean,
    error: String?
) {
    BasicInfoRowShell(
        icon = Icons.Rounded.Edit,
        label = "Nombre de la tarea"
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isBlank()) {
                        Text(
                            text = "Ej: Ensayo sobre Hume",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        if (!isValid) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = error ?: "Ingresa una actividad válida",
                color = UniStackColors.Coral,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun BasicInfoActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    BasicInfoRowShell(
        modifier = Modifier
            .heightIn(min = 72.dp)
            .bounceClick(onClick),
        icon = icon,
        label = label
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value.ifBlank { placeholder },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BasicInfoRowShell(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), AppShapes.SmallCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(23.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 18.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp),
            content = {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                content()
            }
        )
    }
}

@Composable
private fun MonthCalendarDialog(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = TaskDateUtils.today()
    val minMonth = YearMonth.from(today)
    val maxMonth = minMonth.plusMonths(18)
    var visibleMonth by androidx.compose.runtime.remember(selectedDate) {
        mutableStateOf(YearMonth.from(selectedDate ?: today))
    }
    val canGoBack = visibleMonth > minMonth
    val canGoForward = visibleMonth < maxMonth

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Fecha límite",
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { visibleMonth = visibleMonth.minusMonths(1) },
                        enabled = canGoBack
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior")
                    }
                    Text(
                        text = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale("es", "CO"))
                            .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = { visibleMonth = visibleMonth.plusMonths(1) },
                        enabled = canGoForward
                    ) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = "Mes siguiente")
                    }
                }
                CalendarMonthGrid(
                    month = visibleMonth,
                    selectedDate = selectedDate,
                    minDate = today,
                    maxDate = maxMonth.atEndOfMonth(),
                    onDateSelected = onDateSelected
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate?,
    minDate: LocalDate,
    maxDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDay = month.atDay(1)
    val leadingEmptyCells = firstDay.dayOfWeek.isoIndex() - 1
    val days = (1..month.lengthOfMonth()).map { month.atDay(it) }
    val cells = List(leadingEmptyCells) { null } + days
    val weeks = cells.chunked(7)
    val dayLabels = listOf("L", "M", "M", "J", "V", "S", "D")

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                (0 until 7).forEach { index ->
                    val date = week.getOrNull(index)
                    val enabled = date != null && !date.isBefore(minDate) && !date.isAfter(maxDate)
                    val selected = date != null && date == selectedDate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 38.dp)
                            .then(
                                if (enabled) Modifier.bounceClick { onDateSelected(date!!) } else Modifier
                            )
                            .background(
                                color = when {
                                    selected -> MaterialTheme.colorScheme.primary
                                    enabled -> MaterialTheme.colorScheme.surfaceVariant
                                    else -> Color.Transparent
                                },
                                shape = AppShapes.Pill
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString().orEmpty(),
                            color = when {
                                selected -> MaterialTheme.colorScheme.onPrimary
                                enabled -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
                            },
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectDropdown(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onCreateSubjectClick: () -> Unit,
    onSubjectSelected: (String?) -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val selectedLabel = selectedSubjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        BasicInfoActionRow(
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            label = "Materia",
            value = selectedLabel.orEmpty(),
            placeholder = "Seleccionar materia",
            onClick = { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface, AppShapes.SmallCard)
        ) {
            DropdownMenuItem(
                text = {
                    DropdownOptionText(
                        text = "Sin materia asignada",
                        selected = selectedSubjectId == null
                    )
                },
                onClick = {
                    onSubjectSelected(null)
                    expanded = false
                }
            )
            subjects.forEach { subject ->
                DropdownMenuItem(
                    text = {
                        DropdownOptionText(
                            text = subject.name,
                            selected = selectedSubjectId == subject.id
                        )
                    },
                    onClick = {
                        onSubjectSelected(subject.id)
                        expanded = false
                    }
                )
            }
            DropdownMenuItem(
                text = {
                    Text(
                        "Crear nueva materia",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                onClick = {
                    expanded = false
                    onCreateSubjectClick()
                }
            )
        }
    }
}

@Composable
private fun DropdownOptionText(
    text: String,
    selected: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TaskTypeSelector(
    selected: TaskType,
    onSelected: (TaskType) -> Unit
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(
            TaskType.WORKSHOP,
            TaskType.EXAM,
            TaskType.ESSAY,
            TaskType.PRESENTATION,
            TaskType.OTHER
        ).forEach { type ->
            TaskChoiceChip(
                text = type.label(),
                selected = selected == type,
                onClick = { onSelected(type) }
            )
        }
    }
}

@Composable
private fun TaskChoiceChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    val border = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
    val textColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Row(
        modifier = modifier
            .height(46.dp)
            .background(background, AppShapes.Pill)
            .border(1.dp, border, AppShapes.Pill)
            .bounceClick(onClick)
            .padding(horizontal = if (selected) 18.dp else 22.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Rounded.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(17.dp)
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
        Text(
            text = text,
            color = textColor,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun PrioritySegmentedControl(
    selected: TaskDifficulty,
    onSelected: (TaskDifficulty) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f), AppShapes.Pill)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f), AppShapes.Pill)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TaskDifficulty.entries.forEach { priority ->
            val isSelected = selected == priority
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = AppShapes.Pill
                    )
                    .bounceClick { onSelected(priority) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = priority.label(),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CreateTaskButton(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.13f),
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
        ),
        contentPadding = PaddingValues(vertical = 0.dp),
        modifier = modifier.height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.TaskAlt,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.size(10.dp))
            Text(text, fontWeight = FontWeight.ExtraBold)
        }
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

private fun TaskDifficulty.label(): String {
    return when (this) {
        TaskDifficulty.EASY -> "Baja"
        TaskDifficulty.MEDIUM -> "Media"
        TaskDifficulty.HARD -> "Alta"
    }
}

private fun DayOfWeek.isoIndex(): Int = value
