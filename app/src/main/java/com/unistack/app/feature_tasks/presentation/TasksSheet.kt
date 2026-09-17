@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_tasks.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.TrendingFlat
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniIconButtonVariant
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.tachadoDe
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.Textos
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.presentation.subjectAccent
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.formatTaskDate
import com.unistack.app.feature_tasks.domain.formatTaskTime
import com.unistack.app.feature_tasks.domain.isGradable
import com.unistack.app.feature_user.domain.GradingScale
import java.text.DecimalFormatSymbols
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.sin

/**
 * Hoja modal de detalle de la tarea (HojaTarea de Propuesta D).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HojaTarea(
    task: StudentTask,
    subject: Subject?,
    gradingScale: GradingScale,
    viewModel: TasksViewModel,
    onDismiss: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onEditTaskClick: (String) -> Unit,
    onOpenFull: (String) -> Unit,
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
    var subtasksEnabled by remember(task.id) { mutableStateOf(task.subtasks.isNotEmpty()) }
    var showConfirmDisableSubtasks by remember { mutableStateOf(false) }
    var attachError by remember { mutableStateOf<String?>(null) }
    val allAttachments by viewModel.attachments.collectAsStateWithLifecycle()
    val taskAttachments = remember(allAttachments, task.id) { allAttachments.filter { it.taskId == task.id } }
    val attachController = rememberTaskAttachController(task.id, viewModel) { attachError = it }
    val attachmentOpener = rememberTaskAttachmentOpener(viewModel)
    val context = androidx.compose.ui.platform.LocalContext.current
    val today = remember { TaskDateUtils.today() }
    val dueDate = TaskDateUtils.fromMillis(task.dueDateMillis)
    val isOverdue = !task.completed && dueDate.isBefore(today)
    val isToday = !task.completed && dueDate == today

    val isDark = LocalIsDarkTheme.current
    val sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSubtle = MaterialTheme.colorScheme.onSurfaceVariant
    val metaPillBg = MaterialTheme.colorScheme.surfaceContainerHigh
    val metaPillBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        // Sin efecto de borde dentro de la hoja: el gesto se reparte entre desplazar el
        // contenido y mover la hoja entera, así que el estirado llega siempre tarde y se lee
        // como un parón y un tirón. Mismo motivo y misma salida que en la hoja de notas.
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
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
                val typeColor = subject?.let { subjectAccent(it) } ?: MaterialTheme.colorScheme.primary
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
                        color = MaterialTheme.colorScheme.onSurface
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
                                color = textPrimary
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
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }

                IconButton(
                    onClick = {
                        onDismiss()
                        onOpenFull(task.id)
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
            val estadoSubColor = when {
                task.gradingStatus == TaskGradingStatus.GRADED || (task.completed && task.gradingStatus == TaskGradingStatus.NOT_GRADED) -> LocalSectionColors.current.onOnTrackContainer.copy(alpha = 0.85f)
                task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                isOverdue -> LocalSectionColors.current.onExpensesContainer.copy(alpha = 0.85f)
                isToday -> LocalSectionColors.current.onAtRiskContainer.copy(alpha = 0.85f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
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
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> stringResource(R.string.tasks_detail_awaiting_title)
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
                        task.gradingStatus == TaskGradingStatus.AWAITING_GRADE -> {
                            // «Entregada el 9 sep»: fecha real de entrega; el orden día/mes lo pone cada idioma.
                            val entregadaEl = remember(task.completedAt, task.dueDateMillis) {
                                val d = TaskDateUtils.fromMillis(task.completedAt ?: task.dueDateMillis)
                                d.dayOfMonth to d.month
                                    .getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
                                    .replace(".", "").take(3)
                            }
                            stringResource(R.string.tasks_detail_awaiting_desc, entregadaEl.first, entregadaEl.second)
                        }
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
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = estadoTextColor.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = stringResource(R.string.tasks_postpone_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.1.sp,
                            color = estadoSubColor
                        )
                        Spacer(modifier = Modifier.height(7.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            PostponePill(
                                icon = Icons.AutoMirrored.Rounded.TrendingFlat,
                                label = stringResource(R.string.tasks_postpone_tomorrow),
                                color = estadoTextColor,
                                shape = RoundedCornerShape(topStart = 999.dp, bottomStart = 999.dp, topEnd = 10.dp, bottomEnd = 10.dp),
                                onClick = { onPostponeTomorrow(task.id) },
                                modifier = Modifier.weight(1f)
                            )
                            PostponePill(
                                icon = Icons.AutoMirrored.Rounded.TrendingFlat,
                                label = stringResource(R.string.tasks_postpone_next_monday),
                                color = estadoTextColor,
                                shape = RoundedCornerShape(10.dp),
                                onClick = { onPostponeNextMonday(task.id) },
                                modifier = Modifier.weight(1f)
                            )
                            PostponePill(
                                icon = Icons.Rounded.CalendarMonth,
                                label = stringResource(R.string.tasks_postpone_pick_day),
                                color = estadoTextColor,
                                shape = RoundedCornerShape(topStart = 10.dp, bottomStart = 10.dp, topEnd = 999.dp, bottomEnd = 999.dp),
                                onClick = { onOpenDatePicker(task.id) },
                                modifier = Modifier.weight(1f)
                            )
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
                        val subjectSubtitle = if (subject != null) {
                            // «Corte 2 · promedio 4,25 · te falta 3,1»: cuenta del corte con GradeCalculator, sin proyecciones.
                            val corteId = subject.defaultCutId
                            val cutName = subject.cutScheme.cutName(corteId)
                            val notasDelCorte = subject.grades.filter { it.cutId == corteId }
                            val promedioDelCorte = GradeCalculator.calculateCutAverage(notasDelCorte)
                            if (promedioDelCorte == null) {
                                stringResource(R.string.tasks_detail_subject_no_grades, cutName)
                            } else {
                                val promedioStr = GradingScaleUtils.formatGrade(promedioDelCorte, gradingScale)
                                val cuenta = GradeCalculator.calculateCut(notasDelCorte)
                                val falta = GradeCalculator.calculateNeededGrade(
                                    cuenta.weightedPoints,
                                    1.0 - cuenta.evaluatedFraction,
                                    subject.targetAverage,
                                    GradingScaleUtils.maxGradeFor(gradingScale)
                                )
                                if (falta != null && falta > 0.0) {
                                    stringResource(
                                        R.string.tasks_detail_subject_avg,
                                        cutName,
                                        promedioStr,
                                        GradingScaleUtils.formatGrade(falta, gradingScale)
                                    )
                                } else {
                                    stringResource(R.string.tasks_detail_subject_avg_only, cutName, promedioStr)
                                }
                            }
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

            // Adjuntos: fotos, archivos y audio, justo después de la descripción — igual de a mano.
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
                            .cleanClickable(shape = RoundedCornerShape(14.dp), onClick = attachController.openMenu),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.tasks_attachment_add),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
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
                                text = stringResource(R.string.tasks_detail_eval_title).uppercase(),
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
                                        text = stringResource(R.string.tasks_detail_awaiting_hint),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textSubtle,
                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                    )
                                }
                                task.type.isGradable() && !task.completed -> {
                                    Text(
                                        text = stringResource(
                                            R.string.tasks_detail_eval_gradable_hint,
                                            task.type.label().lowercase()
                                        ),
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
                                        color = LocalSectionColors.current.onTrack
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    ) {
                                        Text(
                                            text = stringResource(R.string.tasks_detail_eval_registered_in_subject),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = textSubtle
                                        )
                                        Text(
                                            text = stringResource(R.string.tasks_detail_eval_view_there),
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.cleanClickable { onSubjectClick(subject.id) }
                                        )
                                    }
                                }
                                Text(
                                    text = stringResource(R.string.tasks_detail_eval_unlink_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle,
                                    fontSize = 12.sp
                                )
                                TextButton(
                                    onClick = { onUnlinkGradeClick(task.id) },
                                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
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
                                        .height(52.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                ) {
                                    Text(
                                        text = stringResource(R.string.tasks_record_grade_button),
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Text(
                                    text = stringResource(
                                        R.string.tasks_detail_eval_creates,
                                        subject.cutScheme.cutName(subject.defaultCutId),
                                        subject.name
                                    ),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = stringResource(R.string.tasks_detail_eval_no_grade_hint),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textSubtle,
                                    fontSize = 12.sp
                                )
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
            if (showConfirmDisableSubtasks) {
                AlertDialog(
                    onDismissRequest = { showConfirmDisableSubtasks = false },
                    title = {
                        Text(
                            text = stringResource(R.string.tasks_subtasks_disable_confirm_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.tasks_subtasks_disable_confirm_desc),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                task.subtasks.forEach { onDeleteSubtask(task.id, it.id) }
                                subtasksEnabled = false
                                showConfirmDisableSubtasks = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text(stringResource(R.string.action_delete))
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showConfirmDisableSubtasks = false }) {
                            Text(stringResource(R.string.action_cancel))
                        }
                    }
                )
            }

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
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.tasks_subtasks_title).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.2.sp,
                                    color = textSubtle
                                )
                                if (subtasksEnabled && task.subtasks.isNotEmpty()) {
                                    val doneCount = task.subtasks.count { it.isCompleted }
                                    Surface(
                                        shape = CircleShape,
                                        color = if (doneCount == task.subtasks.size) LocalSectionColors.current.onTrack.copy(alpha = 0.18f) else metaPillBg,
                                        border = metaPillBorder
                                    ) {
                                        Text(
                                            text = "$doneCount/${task.subtasks.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (doneCount == task.subtasks.size) LocalSectionColors.current.onTrack else textPrimary,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (!subtasksEnabled || task.subtasks.isEmpty()) {
                                    stringResource(R.string.tasks_subtasks_enable_desc)
                                } else {
                                    stringResource(
                                        R.string.tasks_subtasks_count,
                                        task.subtasks.count { it.isCompleted },
                                        task.subtasks.size
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = textSubtle
                            )
                        }

                        UniSwitch(
                            checked = subtasksEnabled,
                            onCheckedChange = { checked ->
                                if (!checked && task.subtasks.isNotEmpty()) {
                                    showConfirmDisableSubtasks = true
                                } else {
                                    subtasksEnabled = checked
                                }
                            }
                        )
                    }

                    AnimatedVisibility(visible = subtasksEnabled) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
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
                                        color = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        border = BorderStroke(
                                            width = 1.5.dp,
                                            color = if (subtask.isCompleted) MaterialTheme.colorScheme.primary else (MaterialTheme.colorScheme.outline)
                                        ),
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        if (subtask.isCompleted) {
                                            Icon(
                                                imageVector = Icons.Rounded.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onPrimary,
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
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newSubtaskText,
                                    onValueChange = { newSubtaskText = it },
                                    placeholder = { Text(stringResource(R.string.tasks_subtasks_add_step)) },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(
                                        onDone = {
                                            if (newSubtaskText.isNotBlank()) {
                                                onAddSubtask(task.id, newSubtaskText.trim())
                                                newSubtaskText = ""
                                            }
                                        }
                                    )
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
                                        tint = if (newSubtaskText.isNotBlank()) MaterialTheme.colorScheme.primary else textSubtle.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Acciones al pie: sólo dos zonas de toque — marcar hecha, y un único «⋮» aparte
            // para editar/duplicar/eliminar. Antes eran cuatro botones pegados y era fácil
            // tocar el que no era.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                        containerColor = if (task.completed) (MaterialTheme.colorScheme.surfaceContainerHigh) else LocalSectionColors.current.onTrack,
                        contentColor = if (task.completed) textPrimary else LocalSectionColors.current.onOnTrack
                    ),
                    // Ya completada va en superficie: sin contorno se pierde contra la hoja.
                    border = if (task.completed) BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null
                ) {
                    Icon(
                        imageVector = if (task.completed) Icons.Rounded.Close else Icons.Rounded.Check,
                        contentDescription = null,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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

                // Igual que en la pantalla completa: el botón ya trae su contenedor, envolverlo
                // en otro círculo pintaba dos fondos que no encajan.
                TaskActionsOverflowMenu(
                    onEdit = { onDismiss(); onEditTaskClick(task.id) },
                    onDuplicate = { onDismiss(); onDuplicateTask(task.id) },
                    onDelete = { onDismiss(); onDeleteTask(task.id) },
                    variant = UniIconButtonVariant.Surface
                )
            }
        }
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
            confirmButton = {
                TextButton(onClick = { attachError = null }) { Text(stringResource(R.string.common_understood)) }
            }
        )
    }
}

/**
 * Hoja modal para registrar la nota obtenida en una tarea directamente en el corte de la materia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HojaNota(
    task: StudentTask,
    subject: Subject,
    gradingScale: GradingScale,
    onDismiss: () -> Unit,
    onSaveGrade: (Double, Double?, String) -> Boolean
) {
    val isDark = LocalIsDarkTheme.current
    val sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow
    val cardBg = MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSubtle = MaterialTheme.colorScheme.onSurfaceVariant
    val greenAccent = LocalSectionColors.current.onTrack
    val greenText = LocalSectionColors.current.onOnTrack

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var valueInput by remember(task.id) {
        val initialVal = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) 85.0 else 4.0
        mutableDoubleStateOf(initialVal)
    }
    var selectedCutId by remember(task.id, subject.activeCutId) {
        mutableStateOf(task.cutId ?: subject.defaultCutId)
    }
    var error by remember(task.id) { mutableStateOf<String?>(null) }
    val maxGrade: Double = remember(gradingScale) { GradingScaleUtils.maxGradeFor(gradingScale) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = sheetContainerColor,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        contentWindowInsets = { WindowInsets(0.dp) }
    ) {
        CompositionLocalProvider(LocalOverscrollFactory provides null) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                        color = contentColorOn(subjectAccent(subject))
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.tasks_grade_sheet_title, task.title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = textPrimary
                    )
                    Text(
                        text = stringResource(R.string.tasks_grade_sheet_subtitle, subject.name),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSubtle
                    )
                }
            }

            // Stepper de nota (Tarjeta VALOR)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.tasks_grade_value_label).uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp,
                            color = textSubtle
                        )
                        val maxGradeLabel = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) {
                            "100"
                        } else {
                            GradingScaleUtils.formatGrade(maxGrade, gradingScale).let {
                                it.replace('.', localizedDecimalSeparator())
                            }
                        }
                        Text(
                            text = stringResource(R.string.tasks_grade_scale_label, maxGradeLabel),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = textSubtle
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    valueInput = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) {
                                        (valueInput - 1.0).coerceAtLeast(0.0)
                                    } else {
                                        (Math.round((valueInput - 0.1) * 10.0) / 10.0).coerceAtLeast(0.0)
                                    }
                                }
                            ) {
                                Text(
                                    text = "−",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(32.dp))

                        val formattedGrade = remember(valueInput, gradingScale) {
                            val formatted = GradingScaleUtils.formatGrade(valueInput, gradingScale)
                            formatted.replace('.', localizedDecimalSeparator())
                        }
                        Text(
                            text = formattedGrade,
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Black,
                            color = greenAccent
                        )

                        Spacer(modifier = Modifier.width(32.dp))

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            modifier = Modifier.size(48.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    valueInput = if (gradingScale == GradingScale.ZERO_TO_HUNDRED) {
                                        (valueInput + 1.0).coerceAtMost(maxGrade)
                                    } else {
                                        (Math.round((valueInput + 0.1) * 10.0) / 10.0).coerceAtMost(maxGrade)
                                    }
                                }
                            ) {
                                Text(
                                    text = "+",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Selector de corte (Tarjeta CORTE)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = cardBg,
                border = cardBorder
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.tasks_grade_cut_label).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.2.sp,
                        color = textSubtle
                    )

                    val cutOptions = remember(subject) {
                        subject.cutScheme.cuts.sortedBy { it.order }.map { cut ->
                            UniSegmentedOption(
                                value = cut.id,
                                label = cut.name
                            )
                        }
                    }
                    UniSegmentedControl(
                        selected = selectedCutId,
                        options = cutOptions,
                        onSelected = { selectedCutId = it },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = stringResource(R.string.tasks_grade_cut_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = textSubtle,
                        lineHeight = 16.sp
                    )
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = {
                    if (!onSaveGrade(valueInput, null, selectedCutId)) {
                        error = "Error al guardar la nota"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = greenAccent,
                    contentColor = greenText
                )
            ) {
                Text(
                    text = stringResource(R.string.tasks_grade_save_action),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        }
    }
}

/** El separador decimal del idioma activo, para pintar notas sin depender de comparar el idioma a mano. */
@Composable
private fun PostponePill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    shape: RoundedCornerShape,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El toque se monta aquí dentro y no en quien llama: fuera se pasaba sin forma, así que la
    // onda salía cuadrada y se derramaba por las esquinas redondeadas de la pastilla.
    Surface(
        modifier = modifier.cleanClickable(shape = shape, onClick = onClick),
        shape = shape,
        color = color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 9.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

private fun localizedDecimalSeparator(): Char =
    DecimalFormatSymbols.getInstance(Locale.getDefault()).decimalSeparator
