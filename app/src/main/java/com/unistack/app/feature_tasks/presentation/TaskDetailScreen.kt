@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniConfirmDeleteDialog
import com.unistack.app.core.design.components.UniIconButtonVariant
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.formatTaskDate
import com.unistack.app.feature_tasks.domain.formatTaskTime
import com.unistack.app.feature_user.domain.GradingScale
import java.time.temporal.ChronoUnit

/**
 * «Abrir entera» — la pantalla que antes no existía: tocarla sólo mandaba al formulario de
 * editar. Comparte cabecera con el formulario (atrás + «⋮») en vez de una banda de color, que
 * chocaba con el bloque VENCE (segunda vuelta del artifact). Gana sobre la hoja por la
 * tipografía más grande sin límite de alto y «Más de [materia]» al final.
 */
@Composable
fun TaskDetailScreen(
    taskId: String,
    onBackClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onEditTaskClick: (String) -> Unit,
    onOpenTaskClick: (String) -> Unit,
    viewModel: TasksViewModel = hiltViewModel()
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()

    val task = tasks.firstOrNull { it.id == taskId }
    if (task == null) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background))
        return
    }
    val subject = subjects.firstOrNull { it.id == task.subjectId }
    val taskAttachments = remember(allAttachments, taskId) { allAttachments.filter { it.taskId == taskId } }
    val related = remember(tasks, task.subjectId, taskId) {
        tasks.filter { it.subjectId != null && it.subjectId == task.subjectId && it.id != taskId }.take(2)
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var attachError by remember { mutableStateOf<String?>(null) }
    val attachController = rememberTaskAttachController(task.id, viewModel) { attachError = it }
    val attachmentOpener = rememberTaskAttachmentOpener(viewModel)
    val context = LocalContext.current

    val isDark = LocalIsDarkTheme.current
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSubtle = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.action_back))
            }
            Spacer(modifier = Modifier.weight(1f))
            TaskActionsOverflowMenu(
                onEdit = { onEditTaskClick(task.id) },
                onDuplicate = { viewModel.duplicateTask(task.id); onBackClick() },
                onDelete = { showDeleteConfirm = true }
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textPrimary
                )
                Text(
                    text = listOfNotNull(subject?.name, task.type.label()).joinToString(" · "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSubtle
                )
            }

            TaskDetailStatusBlock(task = task, onPostponeTomorrow = { viewModel.postponeTaskToTomorrow(task.id) })

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder,
                onClick = { subject?.let { onSubjectClick(it.id) } }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 13.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = subject?.name?.firstOrNull()?.uppercase() ?: "?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = contentColorOn(subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.surfaceContainerHigh)
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
                        if (subject != null) {
                            val corteId = subject.defaultCutId
                            val cutName = subject.cutScheme.cutName(corteId)
                            val promedio = GradeCalculator.calculateCutAverage(subject.grades.filter { it.cutId == corteId })
                            Text(
                                text = if (promedio == null) {
                                    stringResource(R.string.tasks_detail_subject_no_grades, cutName)
                                } else {
                                    stringResource(
                                        R.string.tasks_detail_subject_avg_only,
                                        cutName,
                                        GradingScaleUtils.formatGrade(promedio, profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE)
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = textSubtle
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.tasks_detail_no_subject_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = textSubtle
                            )
                        }
                    }
                    if (subject != null) {
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textSubtle)
                    }
                }
            }

            if (task.description.isNotBlank()) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = cardBg, border = cardBorder) {
                    Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = stringResource(R.string.tasks_field_description).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = textSubtle
                        )
                        Text(text = task.description, style = MaterialTheme.typography.bodyMedium, color = textPrimary, lineHeight = 20.sp)
                    }
                }
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = cardBg, border = cardBorder) {
                Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = stringResource(R.string.tasks_field_attachments).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = textSubtle
                    )
                    TaskAttachmentStrip(
                        attachments = taskAttachments,
                        pathFor = { viewModel.taskAttachmentPath(it) },
                        existsFor = { viewModel.taskAttachmentFileExists(it) },
                        onOpen = attachmentOpener.open,
                        onRemove = { viewModel.removeTaskAttachment(it) }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = attachController.openMenu) {
                            Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.tasks_attachment_add), tint = MaterialTheme.colorScheme.primary)
                        }
                        Text(
                            text = stringResource(R.string.tasks_attachment_add),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (task.subtasks.isNotEmpty()) {
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = cardBg, border = cardBorder) {
                    Column(modifier = Modifier.padding(15.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        val done = task.subtasks.count { it.isCompleted }
                        Text(
                            text = stringResource(R.string.tasks_subtasks_title).uppercase() + " · $done/${task.subtasks.size}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = textSubtle
                        )
                        task.subtasks.forEach { sub ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Icon(
                                    imageVector = if (sub.isCompleted) Icons.Rounded.Check else Icons.Rounded.Close,
                                    contentDescription = null,
                                    tint = if (sub.isCompleted) LocalSectionColors.current.onTrack else textSubtle,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(text = sub.title, style = MaterialTheme.typography.bodyMedium, color = if (sub.isCompleted) textSubtle else textPrimary)
                            }
                        }
                    }
                }
            }

            if (related.isNotEmpty() && subject != null) {
                Text(
                    text = stringResource(R.string.tasks_full_related_title, subject.name).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = textSubtle,
                    modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                )
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), color = cardBg, border = cardBorder) {
                    Column(modifier = Modifier.padding(horizontal = 6.dp)) {
                        related.forEach { relatedTask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .cleanClickable { onOpenTaskClick(relatedTask.id) }
                                    .padding(horizontal = 9.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = relatedTask.title,
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = textPrimary
                                )
                                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = textSubtle, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                shape = CircleShape,
                onClick = { viewModel.setTaskCompleted(task.id, !task.completed) },
                modifier = Modifier.weight(1f).height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (task.completed) MaterialTheme.colorScheme.surfaceContainerHigh else LocalSectionColors.current.onTrack,
                    contentColor = if (task.completed) textPrimary else LocalSectionColors.current.onOnTrack
                ),
                // Ya completada, el botón va en superficie y no en color: sin contorno se
                // confundía con el fondo de la barra y no parecía un botón.
                border = if (task.completed) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
            ) {
                Icon(imageVector = if (task.completed) Icons.Rounded.Close else Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(19.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (task.completed) stringResource(R.string.tasks_action_revert_pending) else stringResource(R.string.tasks_action_mark_done),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            // Sin envoltorio propio: `UniIconButton` ya trae su contenedor y su forma, así que
            // meterlo en otro círculo dibujaba dos fondos desalineados, uno saliéndose del otro.
            TaskActionsOverflowMenu(
                onEdit = { onEditTaskClick(task.id) },
                onDuplicate = { viewModel.duplicateTask(task.id); onBackClick() },
                onDelete = { showDeleteConfirm = true },
                variant = UniIconButtonVariant.Surface
            )
        }
    }

    if (attachController.menuOpen) {
        TaskAttachMenuSheet(
            onDismiss = attachController.dismissMenu,
            onPickPhoto = attachController.pickPhoto,
            onTakePhoto = attachController.takePhoto,
            onPickFile = attachController.pickFile,
            onRecordAudio = attachController.openRecorder
        )
    }
    if (attachController.recorderOpen) {
        com.unistack.app.feature_notes.presentation.NoteRecorderSheet(
            createFile = { ext -> viewModel.newTaskAttachmentFile(ext) },
            onDiscard = { viewModel.discardTaskStoredFile(it) },
            onSaved = { storedName, durationMillis ->
                viewModel.attachTaskStoredFile(
                    taskId = task.id,
                    storedName = storedName,
                    displayName = Textos.get(R.string.tasks_attachment_new_recording),
                    mimeType = "audio/mp4",
                    kind = com.unistack.app.feature_notes.domain.AttachmentKind.AUDIO,
                    durationMillis = durationMillis
                )
            },
            onDismiss = attachController.dismissRecorder
        )
    }
    attachError?.let { mensaje ->
        AlertDialog(
            onDismissRequest = { attachError = null },
            title = { Text(stringResource(R.string.tasks_attachment_error_title)) },
            text = { Text(mensaje) },
            confirmButton = { TextButton(onClick = { attachError = null }) { Text(stringResource(R.string.common_understood)) } }
        )
    }
    if (showDeleteConfirm) {
        UniConfirmDeleteDialog(
            title = stringResource(R.string.tasks_delete_dialog_title),
            body = stringResource(R.string.tasks_delete_dialog_body),
            onConfirm = {
                viewModel.deleteTask(task.id)
                showDeleteConfirm = false
                onBackClick()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

/** El mismo bloque VENCE/HOY/posponer de la hoja, en versión de pantalla propia (sin `estadoColor` calculado desde una hoja compartida). */
@Composable
private fun TaskDetailStatusBlock(task: StudentTask, onPostponeTomorrow: () -> Unit) {
    val today = remember { TaskDateUtils.today() }
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val isToday = !task.completed && dueDate == today

    val estadoColor = when {
        task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> LocalSectionColors.current.onTrackContainer
        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> MaterialTheme.colorScheme.secondaryContainer
        isOverdue -> LocalSectionColors.current.expensesContainer
        isToday -> LocalSectionColors.current.atRiskContainer
        else -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val estadoTextColor = when {
        task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> LocalSectionColors.current.onOnTrackContainer
        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> MaterialTheme.colorScheme.onSecondaryContainer
        isOverdue -> LocalSectionColors.current.onExpensesContainer
        isToday -> LocalSectionColors.current.onAtRiskContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    val estadoSubColor = estadoTextColor.copy(alpha = 0.85f)

    Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), color = estadoColor) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val rotulo = when {
                task.gradingStatus == TaskGradingStatus.GRADED -> stringResource(R.string.tasks_detail_graded_label)
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_submitted_label)
                task.completed -> stringResource(R.string.tasks_detail_done_label)
                isOverdue -> stringResource(R.string.tasks_detail_overdue_label)
                else -> stringResource(R.string.tasks_detail_due_label)
            }
            Text(text = rotulo.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.2.sp, color = estadoSubColor)

            val diff = remember(dueDate, today) { ChronoUnit.DAYS.between(today, dueDate) }
            val hasTime = remember(task.dueDateMillis) { TaskDateUtils.hasExplicitTime(task.dueDateMillis) }
            val time = remember(task.dueDateMillis) { TaskDateUtils.timeFromMillis(task.dueDateMillis) }
            val grande = when {
                task.completed -> stringResource(R.string.tasks_pill_done)
                diff < 0 -> if (diff == -1L) stringResource(R.string.tasks_group_yesterday) else "${-diff}d"
                diff == 0L -> stringResource(R.string.tasks_group_today) + (if (hasTime) " · ${formatTaskTime(time)}" else "")
                diff == 1L -> stringResource(R.string.tasks_group_tomorrow) + (if (hasTime) " · ${formatTaskTime(time)}" else "")
                else -> formatTaskDate(dueDate) + (if (hasTime) " · ${formatTaskTime(time)}" else "")
            }
            Text(text = grande, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black, color = estadoTextColor)

            if (!task.completed) {
                Spacer(modifier = Modifier.height(10.dp))
                androidx.compose.material3.HorizontalDivider(color = estadoTextColor.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = stringResource(R.string.tasks_postpone_label).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.1.sp,
                    color = estadoSubColor
                )
                Spacer(modifier = Modifier.height(7.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(999.dp),
                    color = estadoTextColor.copy(alpha = 0.12f),
                    onClick = onPostponeTomorrow
                ) {
                    Row(modifier = Modifier.padding(vertical = 11.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.AutoMirrored.Rounded.TrendingFlat, contentDescription = null, tint = estadoTextColor, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.tasks_postpone_tomorrow),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = estadoTextColor
                        )
                    }
                }
            }
        }
    }
}
