package com.unistack.app.feature_tasks.presentation

import com.unistack.app.core.utils.DayLabels

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Grade
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.theme.UniStackDatePickerColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskScreen(
    onBackClick: () -> Unit,
    onCreateSubjectClick: () -> Unit,
    onEditLinkedGrade: (String, String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = hiltViewModel(),
    taskId: String? = null
) {
    BackHandler(onBack = onBackClick)

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val task = taskId?.let { id -> tasks.firstOrNull { it.id == id } }
    val isEditing = taskId != null

    var title by rememberSaveable(taskId) { mutableStateOf("") }
    var description by rememberSaveable(taskId) { mutableStateOf("") }
    var dueDate by rememberSaveable(taskId) { mutableStateOf("") }
    var dueTime by rememberSaveable(taskId) { mutableStateOf("") }
    var selectedSubjectId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var selectedPeriodId by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var selectedType by rememberSaveable(taskId) { mutableStateOf(TaskType.WORKSHOP) }
    var difficulty by rememberSaveable(taskId) { mutableStateOf(TaskDifficulty.MEDIUM) }
    var gradingChoice by rememberSaveable(taskId) { mutableStateOf<TaskGradingChoice?>(null) }
    var initialized by rememberSaveable(taskId) { mutableStateOf(false) }
    var error by rememberSaveable(taskId) { mutableStateOf<String?>(null) }
    var showDatePicker by rememberSaveable { mutableStateOf(false) }
    var showTimePicker by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    var showUnlinkConfirmation by rememberSaveable { mutableStateOf(false) }
    val estimatedMinutes = "60"

    val titleValidation = TextValidators.validateActivityName(title)
    val isTitleValid = title.isBlank() || titleValidation.isValid
    val parsedDueDate = TaskDateUtils.parseInput(dueDate)
    val parsedDueTime = dueTime.takeIf { it.isNotBlank() }?.let(TaskDateUtils::parseTimeInput)
    val isValid = (!isEditing || task != null) &&
        titleValidation.isValid &&
        parsedDueDate != null &&
        (dueTime.isBlank() || parsedDueTime != null) &&
        gradingChoice != null &&
        (gradingChoice != TaskGradingChoice.YES || selectedSubjectId != null)
    val linkedSubject = task?.linkedGradeId?.let { gradeId ->
        subjects.firstOrNull { subject -> subject.grades.any { it.id == gradeId } }
    }
    val linkedGrade = task?.linkedGradeId?.let { gradeId ->
        linkedSubject?.grades?.firstOrNull { it.id == gradeId }
    }
    val linkedGradeChanged = task != null && linkedGrade != null && (
        title.trim() != task.title ||
            selectedSubjectId != task.subjectId ||
            selectedPeriodId != task.periodId ||
            selectedType != task.type
        )

    LaunchedEffect(task?.id, taskId) {
        if (initialized) return@LaunchedEffect
        if (task != null) {
            title = task.title
            description = task.description
            dueDate = TaskDateUtils.formatInput(TaskDateUtils.fromMillis(task.dueDateMillis))
            dueTime = TaskDateUtils.timeFromMillis(task.dueDateMillis)
                .takeUnless { it == LocalTime.MIDNIGHT }
                ?.let(TaskDateUtils::formatTimeInput)
                .orEmpty()
            selectedSubjectId = task.subjectId
            selectedPeriodId = task.periodId
            selectedType = task.type
            difficulty = task.difficulty
            gradingChoice = when (task.gradingStatus) {
                TaskGradingStatus.NOT_GRADED -> TaskGradingChoice.NO
                TaskGradingStatus.UNDECIDED -> TaskGradingChoice.UNSURE
                TaskGradingStatus.AWAITING_GRADE,
                TaskGradingStatus.GRADED -> TaskGradingChoice.YES
            }
            initialized = true
        } else if (!isEditing) {
            initialized = true
        }
    }

    AddTaskContent(
        title = title,
        description = description,
        isEditing = isEditing,
        taskMissing = isEditing && task == null,
        titleIsValid = isTitleValid,
        titleError = titleValidation.errorMessage,
        dueDateLabel = parsedDueDate?.let { TaskDateUtils.dueText(TaskDateUtils.toMillis(it, parsedDueTime)) }.orEmpty(),
        dueTimeLabel = dueTime.ifBlank { "Sin hora" },
        subjects = subjects,
        selectedSubjectId = selectedSubjectId,
        selectedPeriodId = selectedPeriodId,
        selectedType = selectedType,
        selectedPriority = difficulty,
        gradingChoice = gradingChoice,
        linkedGradeValue = linkedGrade?.let {
            GradingScaleUtils.formatGrade(
                it.value,
                profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE
            )
        },
        linkedGradePeriod = linkedGrade?.let { grade ->
            linkedSubject?.periodScheme?.periodName(grade.periodId)
        },
        linkedGradeWeight = linkedGrade?.let { grade ->
            if (grade.weightStatus == com.unistack.app.feature_grades.domain.GradeWeightStatus.UNKNOWN) {
                "Porcentaje pendiente"
            } else {
                "${(grade.percentage * 100).toInt()}% del corte"
            }
        },
        linkedGradeChanged = linkedGradeChanged,
        isSaveEnabled = isValid,
        error = error,
        onBackClick = onBackClick,
        onDeleteClick = if (isEditing && task != null) {
            { showDeleteConfirmation = true }
        } else {
            null
        },
        onDuplicateClick = if (isEditing && task != null) {
            {
                if (viewModel.duplicateTask(task.id)) onBackClick()
            }
        } else {
            null
        },
        onCompleteClick = if (isEditing && task != null && !task.completed) {
            {
                if (viewModel.completeTaskFromEditor(task.id)) onBackClick()
            }
        } else {
            null
        },
        onEditLinkedGrade = if (linkedSubject != null && linkedGrade != null) {
            { onEditLinkedGrade(linkedSubject.id, linkedGrade.id) }
        } else {
            null
        },
        onUnlinkLinkedGrade = if (linkedGrade != null) {
            { showUnlinkConfirmation = true }
        } else {
            null
        },
        onTitleChange = {
            title = it.take(40)
            error = null
        },
        onDescriptionChange = {
            description = it
            error = null
        },
        onDateClick = {
            showDatePicker = true
            error = null
        },
        onTimeClick = {
            showTimePicker = true
            error = null
        },
        onTypeSelected = {
            selectedType = it
            error = null
        },
        onSubjectSelected = {
            selectedSubjectId = it
            selectedPeriodId = subjects.firstOrNull { subject -> subject.id == it }?.activePeriodId
            error = null
        },
        onPeriodSelected = {
            selectedPeriodId = it
            error = null
        },
        onCreateSubjectClick = onCreateSubjectClick,
        onPrioritySelected = {
            difficulty = it
            error = null
        },
        onGradingChoiceSelected = {
            gradingChoice = it
            error = null
        },
        onSaveClick = {
            val editingTaskId = taskId
            val saved = if (editingTaskId != null) {
                viewModel.updateTask(
                    taskId = editingTaskId,
                    title = title,
                    description = description,
                    subjectId = selectedSubjectId,
                    type = selectedType,
                    dueDateInput = dueDate,
                    dueTimeInput = dueTime,
                    estimatedMinutesInput = estimatedMinutes,
                    difficulty = difficulty,
                    periodId = selectedPeriodId,
                    gradingStatus = gradingChoice.toInitialGradingStatus()
                )
            } else {
                viewModel.addTask(
                    title = title,
                    description = description,
                    subjectId = selectedSubjectId,
                    type = selectedType,
                    dueDateInput = dueDate,
                    dueTimeInput = dueTime,
                    estimatedMinutesInput = estimatedMinutes,
                    difficulty = difficulty,
                    periodId = selectedPeriodId,
                    gradingStatus = gradingChoice.toInitialGradingStatus()
                )
            }

            if (saved) {
                onBackClick()
            } else {
                error = "Revisa la actividad, fecha y hora antes de guardar."
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
    if (showTimePicker) {
        TimePickerSheet(
            selectedTime = parsedDueTime,
            onTimeSelected = { selectedTime ->
                dueTime = TaskDateUtils.formatTimeInput(selectedTime)
                error = null
                showTimePicker = false
            },
            onClearTime = {
                dueTime = ""
                error = null
                showTimePicker = false
            },
            onDismiss = { showTimePicker = false }
        )
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Eliminar tarea") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirmation = false
                        task?.let { viewModel.deleteTask(it.id) }
                        onBackClick()
                    }
                ) {
                    Text("Eliminar", color = UniStackColors.Coral)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    if (showUnlinkConfirmation) {
        AlertDialog(
            onDismissRequest = { showUnlinkConfirmation = false },
            title = { Text("Desvincular nota") },
            text = { Text("La nota seguirá guardada en la materia, pero dejará de estar asociada a esta tarea.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkConfirmation = false
                        task?.let { viewModel.unlinkTaskGrade(it.id) }
                    }
                ) {
                    Text("Desvincular")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun AddTaskContent(
    title: String,
    description: String,
    isEditing: Boolean,
    taskMissing: Boolean,
    titleIsValid: Boolean,
    titleError: String?,
    dueDateLabel: String,
    dueTimeLabel: String,
    subjects: List<Subject>,
    selectedSubjectId: String?,
    selectedPeriodId: String?,
    selectedType: TaskType,
    selectedPriority: TaskDifficulty,
    gradingChoice: TaskGradingChoice?,
    linkedGradeValue: String?,
    linkedGradePeriod: String?,
    linkedGradeWeight: String?,
    linkedGradeChanged: Boolean,
    isSaveEnabled: Boolean,
    error: String?,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    onDuplicateClick: (() -> Unit)?,
    onCompleteClick: (() -> Unit)?,
    onEditLinkedGrade: (() -> Unit)?,
    onUnlinkLinkedGrade: (() -> Unit)?,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onTypeSelected: (TaskType) -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onPeriodSelected: (String) -> Unit,
    onCreateSubjectClick: () -> Unit,
    onPrioritySelected: (TaskDifficulty) -> Unit,
    onGradingChoiceSelected: (TaskGradingChoice) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var descriptionExpanded by rememberSaveable { mutableStateOf(description.isNotBlank()) }
    val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }
    val headerContext = listOfNotNull(
        selectedSubject?.name,
        linkedGradeValue?.let { "Nota $it" }
    ).joinToString(" · ").ifBlank { "Organiza los detalles de la actividad" }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TaskHeader(
                title = if (isEditing) "Editar tarea" else "Nueva tarea",
                subtitle = headerContext,
                onBackClick = onBackClick,
                onDeleteClick = onDeleteClick,
                onDuplicateClick = onDuplicateClick,
                onCompleteClick = onCompleteClick
            )
            FormSection(title = "Detalles principales") {
                BasicInfoCard(
                    title = title,
                    titleIsValid = titleIsValid,
                    titleError = titleError,
                    dueDateLabel = dueDateLabel,
                    dueTimeLabel = dueTimeLabel,
                    subjects = subjects,
                    selectedSubjectId = selectedSubjectId,
                    selectedPeriodId = selectedPeriodId,
                    onTitleChange = onTitleChange,
                    onDateClick = onDateClick,
                    onTimeClick = onTimeClick,
                    onSubjectSelected = onSubjectSelected,
                    onPeriodSelected = onPeriodSelected,
                    onCreateSubjectClick = onCreateSubjectClick
                )
            }
            FormSection(title = "Clasificación") {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    TaskTypeSelector(selected = selectedType, onSelected = onTypeSelected)
                    PrioritySegmentedControl(selected = selectedPriority, onSelected = onPrioritySelected)
                }
            }
            FormSection(title = "Calificación") {
                if (linkedGradeValue != null) {
                    LinkedGradeCard(
                        value = linkedGradeValue,
                        period = linkedGradePeriod,
                        weight = linkedGradeWeight,
                        changed = linkedGradeChanged,
                        onEditClick = onEditLinkedGrade,
                        onUnlinkClick = onUnlinkLinkedGrade
                    )
                } else {
                    GradingIntentSelector(
                        selected = gradingChoice,
                        hasSubject = selectedSubjectId != null,
                        onSelected = onGradingChoiceSelected
                    )
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumCard,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.16f))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .bounceClick { descriptionExpanded = !descriptionExpanded }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (description.isBlank()) "Añadir descripción" else "Descripción",
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            if (descriptionExpanded) Icons.Rounded.KeyboardArrowUp else Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null
                        )
                    }
                    if (descriptionExpanded) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                        Box(modifier = Modifier.padding(14.dp)) {
                            TaskDescriptionField(
                                value = description,
                                onValueChange = onDescriptionChange
                            )
                        }
                    }
                }
            }
            if (taskMissing) {
                Text("Tarea no encontrada.", color = UniStackColors.Coral, fontWeight = FontWeight.Medium)
            }
            error?.let {
                Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Medium)
            }
        }
        // La superficie llega al borde de la pantalla y el margen del sistema va dentro,
        // sobre el botón. Por fuera levantaba la barra entera y dejaba una franja
        // transparente contra el borde por la que se veía pasar el formulario.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 0.dp,
            shadowElevation = 8.dp
        ) {
            CreateTaskButton(
                text = if (isEditing) "Guardar cambios" else "Crear tarea",
                enabled = isSaveEnabled,
                onClick = onSaveClick,
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
    }
}

@Composable
private fun TaskHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    onDeleteClick: (() -> Unit)?,
    onDuplicateClick: (() -> Unit)?,
    onCompleteClick: (() -> Unit)?
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f),
                        shape = AppShapes.Pill
                    )
            ) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            if (onDeleteClick != null || onDuplicateClick != null || onCompleteClick != null) {
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Más acciones")
                    }
                    DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                        onCompleteClick?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Marcar completada") },
                                leadingIcon = { Icon(Icons.Rounded.CheckCircle, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    action()
                                }
                            )
                        }
                        onDuplicateClick?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Duplicar") },
                                leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    action()
                                }
                            )
                        }
                        onDeleteClick?.let { action ->
                            DropdownMenuItem(
                                text = { Text("Eliminar") },
                                leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    action()
                                }
                            )
                        }
                    }
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Normal
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
            text = text,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun FormSectionCard(content: @Composable ColumnScope.() -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
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
    dueTimeLabel: String,
    subjects: List<Subject>,
    selectedSubjectId: String?,
    selectedPeriodId: String?,
    onTitleChange: (String) -> Unit,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
    onSubjectSelected: (String?) -> Unit,
    onPeriodSelected: (String) -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            CompactInfoAction(
                modifier = Modifier.weight(1.35f),
                icon = Icons.Rounded.CalendarMonth,
                label = "Fecha",
                value = dueDateLabel.ifBlank { "Seleccionar" },
                onClick = onDateClick
            )
            CompactInfoAction(
                modifier = Modifier.weight(1f),
                icon = Icons.Rounded.AccessTime,
                label = "Hora",
                value = dueTimeLabel,
                onClick = onTimeClick
            )
        }
        FormDivider()
        SubjectDropdown(
            subjects = subjects,
            selectedSubjectId = selectedSubjectId,
            onCreateSubjectClick = onCreateSubjectClick,
            onSubjectSelected = onSubjectSelected
        )
        val selectedSubject = subjects.firstOrNull { it.id == selectedSubjectId }
        if (selectedSubject != null) {
            FormDivider()
            PeriodDropdown(
                subject = selectedSubject,
                selectedPeriodId = selectedPeriodId ?: selectedSubject.activePeriodId,
                onPeriodSelected = onPeriodSelected
            )
        }
    }
}

@Composable
private fun CompactInfoAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.bounceClick(onClick),
        shape = AppShapes.SmallCard,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = value,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
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
        label = "Título"
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.titleMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Normal
            ),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Box {
                    if (value.isBlank()) {
                        Text(
                            text = "Ej: Ensayo sobre Hume",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Normal
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
                fontWeight = FontWeight.Normal
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
                fontWeight = FontWeight.Medium
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
                    fontWeight = FontWeight.Medium
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
                text = "Seleccionar fecha",
                color = UniStackDatePickerColors.Text,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
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
                        Icon(
                            imageVector = Icons.Rounded.ChevronLeft,
                            contentDescription = "Mes anterior",
                            tint = UniStackDatePickerColors.Muted
                        )
                    }
                    Text(
                        text = visibleMonth.month.getDisplayName(TextStyle.FULL, Locale.forLanguageTag("es-CO"))
                            .replaceFirstChar { it.uppercase() } + " ${visibleMonth.year}",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = UniStackDatePickerColors.Text,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        onClick = { visibleMonth = visibleMonth.plusMonths(1) },
                        enabled = canGoForward
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = "Mes siguiente",
                            tint = UniStackDatePickerColors.Muted
                        )
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
                Text(
                    text = "Cancelar",
                    color = UniStackDatePickerColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = UniStackDatePickerColors.Surface,
        shape = AppShapes.MediumCard
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimePickerSheet(
    selectedTime: LocalTime?,
    onTimeSelected: (LocalTime) -> Unit,
    onClearTime: () -> Unit,
    onDismiss: () -> Unit
) {
    val initial = selectedTime ?: LocalTime.now().withSecond(0).withNano(0)
    val pickerState = rememberTimePickerState(
        initialHour = initial.hour,
        initialMinute = initial.minute,
        is24Hour = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Hora límite",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Usaremos esta hora para calcular recordatorios más oportunos.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal
                )
            }
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = pickerState)
            }
            SquishyButton(
                onClick = {
                    onTimeSelected(LocalTime.of(pickerState.hour, pickerState.minute))
                },
                modifier = Modifier.fillMaxWidth(),
                shape = AppShapes.MediumCard
            ) {
                Text("Usar esta hora")
            }
            TextButton(
                onClick = onClearTime,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Dejar sin hora")
            }
        }
    }
}

@Composable
private fun LinkedGradeCard(
    value: String,
    period: String?,
    weight: String?,
    changed: Boolean,
    onEditClick: (() -> Unit)?,
    onUnlinkClick: (() -> Unit)?
) {
    FormSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), AppShapes.SmallCard),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Grade,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Nota vinculada",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = listOfNotNull(period, weight).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Normal
                )
            }
            Text(
                text = value,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        }
        if (changed) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = AppShapes.SmallCard,
                color = UniStackColors.Yellow.copy(alpha = 0.10f)
            ) {
                Row(
                    modifier = Modifier.padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Icon(
                        Icons.Rounded.WarningAmber,
                        contentDescription = null,
                        tint = UniStackColors.Yellow,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = "Al guardar, estos cambios también actualizarán la nota vinculada.",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
        if (onEditClick != null || onUnlinkClick != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                onEditClick?.let { action ->
                    TextButton(onClick = action) {
                        Icon(Icons.Rounded.Edit, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Editar nota")
                    }
                }
                onUnlinkClick?.let { action ->
                    TextButton(onClick = action) {
                        Text("Desvincular")
                    }
                }
            }
        }
    }
}

@Composable
private fun GradingIntentSelector(
    selected: TaskGradingChoice?,
    hasSubject: Boolean,
    onSelected: (TaskGradingChoice) -> Unit
) {
    FormSectionCard {
        Text(
            "¿Esta tarea tendrá nota?",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            "Así sabremos si debemos pedirte el resultado al completarla.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskGradingChoice.entries.forEach { choice ->
                val isSelected = selected == choice
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .bounceClick { onSelected(choice) },
                    shape = AppShapes.MediumCard,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.22f)
                    )
                ) {
                    Text(
                        text = choice.label,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 12.dp),
                        textAlign = TextAlign.Center,
                        color = if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        if (selected == TaskGradingChoice.YES && !hasSubject) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                "Selecciona una materia para poder registrar la nota.",
                color = UniStackColors.Coral,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private enum class TaskGradingChoice(val label: String) {
    YES("Calificable"),
    NO("Sin nota"),
    UNSURE("Aún no sé")
}

private fun TaskGradingChoice?.toInitialGradingStatus(): TaskGradingStatus {
    return when (this) {
        TaskGradingChoice.NO -> TaskGradingStatus.NOT_GRADED
        TaskGradingChoice.YES,
        TaskGradingChoice.UNSURE,
        null -> TaskGradingStatus.UNDECIDED
    }
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
    val dayLabels = DayLabels.short

    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            dayLabels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = UniStackDatePickerColors.Muted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        weeks.forEach { week ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                (0 until 7).forEach { index ->
                    val date = week.getOrNull(index)
                    val enabledDate = date?.takeUnless { it.isBefore(minDate) || it.isAfter(maxDate) }
                    val enabled = enabledDate != null
                    val selected = date != null && date == selectedDate
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 34.dp)
                            .then(
                                if (enabledDate != null) {
                                    Modifier.bounceClick { onDateSelected(enabledDate) }
                                } else {
                                    Modifier
                                }
                            )
                            .background(
                                color = when {
                                    selected -> UniStackDatePickerColors.Accent
                                    enabled -> UniStackDatePickerColors.DayCell
                                    else -> Color.Transparent
                                },
                                shape = AppShapes.Small
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString().orEmpty(),
                            color = when {
                                selected -> UniStackColors.OnPrimary
                                enabled -> UniStackDatePickerColors.Text
                                else -> UniStackDatePickerColors.Muted.copy(alpha = 0.35f)
                            },
                            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectDropdown(
    subjects: List<Subject>,
    selectedSubjectId: String?,
    onCreateSubjectClick: () -> Unit,
    onSubjectSelected: (String?) -> Unit
) {
    var showSheet by rememberSaveable { mutableStateOf(false) }
    var subjectQuery by rememberSaveable { mutableStateOf("") }
    val selectedLabel = selectedSubjectId?.let { id -> subjects.firstOrNull { it.id == id }?.name }
    val filteredSubjects = remember(subjects, subjectQuery) {
        val query = subjectQuery.trim()
        if (query.isBlank()) {
            subjects
        } else {
            subjects.filter { subject -> subject.name.contains(query, ignoreCase = true) }
        }
    }

    BasicInfoActionRow(
        icon = Icons.AutoMirrored.Rounded.MenuBook,
        label = "Materia",
        value = selectedLabel.orEmpty(),
        placeholder = "Seleccionar materia",
        onClick = { showSheet = true }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = {
                showSheet = false
                subjectQuery = ""
            },
            containerColor = SubjectSheetSurface,
            contentColor = UniStackColors.OnPrimary,
            scrimColor = UniStackColors.Scrim.copy(alpha = 0.62f),
            shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
            contentWindowInsets = { WindowInsets(0.dp, 0.dp, 0.dp, 0.dp) },
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 42.dp, height = 4.dp)
                        .background(
                            UniStackColors.OnPrimary.copy(alpha = 0.20f),
                            AppShapes.Pill
                        )
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 22.dp, end = 22.dp, bottom = 24.dp)
            ) {
                Text(
                    text = "Seleccionar materia",
                    color = SubjectSheetText,
                    fontSize = 23.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Elige la materia que corresponda a esta tarea.",
                    color = SubjectSheetMuted,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.Normal
                )
                Spacer(modifier = Modifier.height(18.dp))
                SubjectSearchField(
                    value = subjectQuery,
                    onValueChange = { subjectQuery = it }
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 430.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 2.dp)
                ) {
                    item(key = "none") {
                        SubjectSheetOption(
                            title = "Sin materia asignada",
                            subtitle = "Tarea general",
                            selected = selectedSubjectId == null,
                            onClick = {
                                onSubjectSelected(null)
                                subjectQuery = ""
                                showSheet = false
                            }
                        )
                    }
                    items(filteredSubjects, key = { it.id }) { subject ->
                        SubjectSheetOption(
                            title = subject.name,
                            subtitle = "Materia disponible",
                            selected = selectedSubjectId == subject.id,
                            onClick = {
                                onSubjectSelected(subject.id)
                                subjectQuery = ""
                                showSheet = false
                            }
                        )
                    }
                    if (filteredSubjects.isEmpty() && subjectQuery.isNotBlank()) {
                        item(key = "empty") {
                            Text(
                                text = "No encontramos materias con ese nombre.",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                color = SubjectSheetMuted,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    item(key = "create") {
                        CreateSubjectSheetAction {
                            subjectQuery = ""
                            showSheet = false
                            onCreateSubjectClick()
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodDropdown(
    subject: Subject,
    selectedPeriodId: String,
    onPeriodSelected: (String) -> Unit
) {
    var showSheet by rememberSaveable(subject.id) { mutableStateOf(false) }
    val selected = subject.periodScheme.periods.firstOrNull { it.id == selectedPeriodId }
        ?: subject.periodScheme.periods.firstOrNull()

    BasicInfoActionRow(
        icon = Icons.Rounded.CalendarMonth,
        label = "Corte",
        value = selected?.name.orEmpty(),
        placeholder = "Seleccionar corte",
        onClick = { showSheet = true }
    )

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            containerColor = SubjectSheetSurface,
            contentColor = UniStackColors.OnPrimary,
            scrimColor = UniStackColors.Scrim.copy(alpha = 0.62f),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 22.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "Seleccionar corte",
                    color = SubjectSheetText,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Las notas obtenidas por esta tarea se registrarán aquí por defecto.",
                    color = SubjectSheetMuted,
                    style = MaterialTheme.typography.bodySmall
                )
                subject.periodScheme.periods.sortedBy { it.order }.forEach { period ->
                    SubjectSheetOption(
                        title = period.name,
                        subtitle = "${(period.weight * 100).toInt()}% de la materia",
                        selected = period.id == selected?.id,
                        onClick = {
                            onPeriodSelected(period.id)
                            showSheet = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectSearchField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(SubjectSheetField, AppShapes.MediumCard)
            .border(
                width = 0.5.dp,
                color = UniStackColors.SoftOutline,
                shape = AppShapes.MediumCard
            )
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = SubjectSheetMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
                color = SubjectSheetText,
                fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(SubjectSheetAccent),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (value.isBlank()) {
                        Text(
                            text = "Buscar materia...",
                            color = SubjectSheetMuted.copy(alpha = 0.78f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    innerTextField()
                }
            },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SubjectSheetOption(
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = AppShapes.MediumCard
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 76.dp)
            .background(
                color = if (selected) SubjectSheetSelectedSurface else SubjectSheetItemSurface,
                shape = shape
            )
            .border(
                width = 0.8.dp,
                color = if (selected) SubjectSheetAccent.copy(alpha = 0.62f) else UniStackColors.SoftOutline,
                shape = shape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = if (selected) SubjectSheetAccent.copy(alpha = 0.22f) else SubjectSheetAccent.copy(alpha = 0.14f),
                    shape = AppShapes.SmallCard
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                contentDescription = null,
                tint = if (selected) SubjectSheetSelectedIcon else SubjectSheetAccentSoft,
                modifier = Modifier.size(23.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                color = SubjectSheetText,
                fontSize = 15.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                color = SubjectSheetMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(
                    color = if (selected) SubjectSheetAccent.copy(alpha = 0.18f) else Color.Transparent,
                    shape = AppShapes.Pill
                )
                .border(
                    width = 1.dp,
                    color = if (selected) SubjectSheetAccent else SubjectSheetMuted.copy(alpha = 0.36f),
                    shape = AppShapes.Pill
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SubjectSheetSelectedIcon,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun CreateSubjectSheetAction(onClick: () -> Unit) {
    val shape = AppShapes.MediumCard
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 74.dp)
            .background(SubjectSheetCreateSurface, shape)
            .border(0.7.dp, UniStackColors.SoftOutline, shape)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(SubjectSheetAccent, AppShapes.SmallCard),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Add,
                contentDescription = null,
                tint = UniStackColors.OnPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = "Crear nueva materia",
            modifier = Modifier.weight(1f),
            color = SubjectSheetText,
            fontSize = 15.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.SemiBold
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = SubjectSheetMuted,
            modifier = Modifier.size(23.dp)
        )
    }
}

@Composable
private fun TaskDescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = false,
        minLines = 3,
        maxLines = 8,
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Normal
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        decorationBox = { innerTextField ->
            Box {
                if (value.isBlank()) {
                    Text(
                        text = "Añade contexto, instrucciones o enlaces (opcional)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Normal
                    )
                }
                innerTextField()
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun TaskTypeSelector(
    selected: TaskType,
    onSelected: (TaskType) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .bounceClick { expanded = true },
            shape = AppShapes.MediumCard,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.20f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.TaskAlt,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Tipo de tarea",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = selected.label(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.88f)
        ) {
            TaskType.entries.forEach { type ->
                DropdownMenuItem(
                    text = { Text(type.label()) },
                    trailingIcon = {
                        if (selected == type) {
                            Icon(Icons.Rounded.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        expanded = false
                        onSelected(type)
                    }
                )
            }
        }
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
                    fontWeight = FontWeight.Medium
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
    SquishyButton(
        onClick = onClick,
        enabled = enabled,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = UniStackColors.OnPrimary,
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
            Text(text, fontWeight = FontWeight.Medium)
        }
    }
}

private val SubjectSheetSurface: Color
    @Composable get() = UniStackColors.Background
private val SubjectSheetField: Color
    @Composable get() = UniStackColors.SurfaceVariant
private val SubjectSheetItemSurface: Color
    @Composable get() = UniStackColors.Card
private val SubjectSheetSelectedSurface: Color
    @Composable get() = UniStackColors.PrimaryLight
private val SubjectSheetCreateSurface: Color
    @Composable get() = UniStackColors.SurfaceVariant
private val SubjectSheetAccent: Color
    @Composable get() = UniStackColors.Primary
private val SubjectSheetAccentSoft: Color
    @Composable get() = UniStackColors.PrimaryDark
private val SubjectSheetSelectedIcon: Color
    @Composable get() = UniStackColors.Primary
private val SubjectSheetText: Color
    @Composable get() = UniStackColors.TextPrimary
private val SubjectSheetMuted: Color
    @Composable get() = UniStackColors.TextSecondary

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

private fun TaskDifficulty.label(): String {
    return when (this) {
        TaskDifficulty.EASY -> "Baja"
        TaskDifficulty.MEDIUM -> "Media"
        TaskDifficulty.HARD -> "Alta"
    }
}

private fun DayOfWeek.isoIndex(): Int = value
