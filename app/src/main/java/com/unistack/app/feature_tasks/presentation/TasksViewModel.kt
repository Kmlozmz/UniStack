package com.unistack.app.feature_tasks.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.GradingScaleUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_tasks.data.TaskAttachmentStore
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskAttachment
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_tasks.domain.TaskSubtask
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.util.UUID
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

@HiltViewModel
class TasksViewModel @Inject constructor(
    private val tasksRepository: TasksRepository,
    private val gradesRepository: GradesRepository,
    private val userRepository: UserRepository,
    private val attachmentStore: TaskAttachmentStore
) : ViewModel() {
    val tasks: StateFlow<List<StudentTask>> = tasksRepository.tasks
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects
    val userProfile = userRepository.userProfile
    val attachments: StateFlow<List<TaskAttachment>> = tasksRepository.attachments

    fun attachmentsOf(taskId: String?): List<TaskAttachment> {
        if (taskId == null) return emptyList()
        return attachments.value.filter { it.taskId == taskId }
    }

    /**
     * Copia dentro de UniStack lo que se acaba de elegir y lo cuelga de la tarea.
     * Calcado de `NotesViewModel.attach` — mismo límite (25 MB, 12 por elemento).
     */
    fun attachTask(taskId: String, uri: Uri): Boolean {
        if (attachmentsOf(taskId).size >= Attachments.MAX_PER_NOTE) return false
        val guardado = attachmentStore.import(uri) ?: return false
        val now = System.currentTimeMillis()
        tasksRepository.addAttachment(
            TaskAttachment(
                id = "tatt-" + UUID.randomUUID(),
                taskId = taskId,
                kind = guardado.kind,
                displayName = guardado.displayName,
                storedName = guardado.storedName,
                mimeType = guardado.mimeType,
                sizeBytes = guardado.sizeBytes,
                durationMillis = null,
                createdAt = now
            )
        )
        return true
    }

    /** Lo mismo, para un archivo que ya se escribió dentro (la cámara y el grabador). */
    fun attachTaskStoredFile(
        taskId: String,
        storedName: String,
        displayName: String,
        mimeType: String,
        kind: AttachmentKind,
        durationMillis: Long? = null
    ): Boolean {
        val archivo = attachmentStore.file(storedName)
        if (!archivo.exists() || archivo.length() == 0L) {
            attachmentStore.delete(storedName)
            return false
        }
        if (attachmentsOf(taskId).size >= Attachments.MAX_PER_NOTE) {
            attachmentStore.delete(storedName)
            return false
        }
        val now = System.currentTimeMillis()
        tasksRepository.addAttachment(
            TaskAttachment(
                id = "tatt-" + UUID.randomUUID(),
                taskId = taskId,
                kind = kind,
                displayName = displayName,
                storedName = storedName,
                mimeType = mimeType,
                sizeBytes = archivo.length(),
                durationMillis = durationMillis,
                createdAt = now
            )
        )
        return true
    }

    fun removeTaskAttachment(attachment: TaskAttachment) {
        tasksRepository.deleteAttachment(attachment.id)
        attachmentStore.delete(attachment.storedName)
    }

    /**
     * Tira los adjuntos que se colgaron de un borrador que nunca llegó a guardarse.
     *
     * El formulario de crear reserva el id de la tarea al abrirse para poder adjuntar antes de
     * guardar; si se sale descartando, esas filas quedarían apuntando a una tarea inexistente.
     */
    fun discardTaskAttachments(taskId: String) {
        attachments.value.filter { it.taskId == taskId }.forEach(::removeTaskAttachment)
    }

    fun taskAttachmentFileExists(attachment: TaskAttachment): Boolean =
        attachmentStore.exists(attachment.storedName)

    fun taskAttachmentPath(attachment: TaskAttachment): String =
        attachmentStore.file(attachment.storedName).absolutePath

    fun taskAttachmentUri(attachment: TaskAttachment): Uri? =
        runCatching { attachmentStore.shareUri(attachment.storedName) }.getOrNull()

    fun newTaskAttachmentFile(extension: String): Pair<String, java.io.File> =
        attachmentStore.newFileFor(extension)

    fun discardTaskStoredFile(storedName: String) = attachmentStore.delete(storedName)

    fun taskById(taskId: String): StudentTask? {
        return tasks.value.firstOrNull { it.id == taskId }
    }

    fun subjectName(subjectId: String?): String? {
        if (subjectId == null) return null
        return subjects.value.firstOrNull { it.id == subjectId }?.name
    }

    fun addTask(
        title: String,
        description: String,
        subjectId: String?,
        type: TaskType,
        dueDateInput: String,
        dueTimeInput: String,
        estimatedMinutesInput: String,
        difficulty: TaskDifficulty,
        cutId: String? = null,
        gradingStatus: TaskGradingStatus = TaskGradingStatus.UNDECIDED,
        subtasks: List<TaskSubtask> = emptyList(),
        /**
         * El id que el formulario ya reservó para poder adjuntar antes de guardar. Si no viene,
         * se mina aquí como siempre.
         */
        presetId: String? = null
    ): Boolean {
        val parsed = validatedTaskInput(
            title = title,
            dueDateInput = dueDateInput,
            dueTimeInput = dueTimeInput,
            estimatedMinutesInput = estimatedMinutesInput
        ) ?: return false

        val now = System.currentTimeMillis()
        val taskId = presetId ?: "task-${UUID.randomUUID()}"
        val normalizedSubtasks = subtasks.mapIndexed { index, sub ->
            sub.copy(
                id = if (sub.id.isBlank()) "sub-${UUID.randomUUID()}" else sub.id,
                taskId = taskId,
                position = index
            )
        }
        tasksRepository.addTask(
            StudentTask(
                id = taskId,
                title = TextValidators.normalizeText(title),
                description = description.trim(),
                subjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } },
                type = type,
                dueDateMillis = parsed.dueDateMillis,
                difficulty = difficulty,
                estimatedMinutes = parsed.estimatedMinutes,
                completed = false,
                createdAt = now,
                updatedAt = now,
                cutId = resolvedCutId(subjectId, cutId),
                gradingStatus = gradingStatus,
                subtasks = normalizedSubtasks
            )
        )
        return true
    }

    fun updateTask(
        taskId: String,
        title: String,
        description: String,
        subjectId: String?,
        type: TaskType,
        dueDateInput: String,
        dueTimeInput: String,
        estimatedMinutesInput: String,
        difficulty: TaskDifficulty,
        cutId: String? = null,
        gradingStatus: TaskGradingStatus = existingGradingStatus(taskId),
        subtasks: List<TaskSubtask>? = null
    ): Boolean {
        val existing = taskById(taskId) ?: return false
        val parsed = validatedTaskInput(
            title = title,
            dueDateInput = dueDateInput,
            dueTimeInput = dueTimeInput,
            estimatedMinutesInput = estimatedMinutesInput
        ) ?: return false

        val resolvedSubjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } }
        val resolvedCutId = resolvedCutId(resolvedSubjectId, cutId ?: existing.cutId)
        val linkedGrade = existing.linkedGradeId?.let { gradeId ->
            subjects.value
                .firstOrNull { subject -> subject.grades.any { it.id == gradeId } }
                ?.let { subject -> subject to subject.grades.first { it.id == gradeId } }
        }
        var resolvedGradingStatus = when (existing.gradingStatus) {
            TaskGradingStatus.GRADED,
            TaskGradingStatus.AWAITING_GRADE -> existing.gradingStatus
            else -> gradingStatus
        }
        var linkedGradeId = existing.linkedGradeId
        if (linkedGrade != null) {
            val (oldSubject, grade) = linkedGrade
            when {
                resolvedSubjectId == null -> {
                    gradesRepository.updateGrade(
                        oldSubject.id,
                        grade.copy(
                            name = TextValidators.normalizeText(title),
                            type = type.toGradeType(),
                            taskId = null,
                            recordedAt = System.currentTimeMillis()
                        )
                    )
                    resolvedGradingStatus = TaskGradingStatus.NOT_GRADED
                    linkedGradeId = null
                }
                resolvedSubjectId == oldSubject.id -> {
                    gradesRepository.updateGrade(
                        oldSubject.id,
                        grade.copy(
                            name = TextValidators.normalizeText(title),
                            type = type.toGradeType(),
                            cutId = resolvedCutId ?: grade.cutId,
                            recordedAt = System.currentTimeMillis()
                        )
                    )
                }
                else -> {
                    val newSubject = subjects.value.first { it.id == resolvedSubjectId }
                    val destinationCut = resolvedCutId ?: newSubject.defaultCutId
                    val destinationWeight = newSubject.grades
                        .filter {
                            it.cutId == destinationCut &&
                                it.source == GradeSource.ACTIVITY &&
                                it.weightStatus == GradeWeightStatus.KNOWN
                        }
                        .sumOf { it.percentage }
                    if (
                        grade.weightStatus == GradeWeightStatus.KNOWN &&
                        destinationWeight + grade.percentage > 1.00001
                    ) {
                        return false
                    }
                    gradesRepository.deleteGrade(oldSubject.id, grade.id)
                    gradesRepository.addGrade(
                        newSubject.id,
                        grade.copy(
                            name = TextValidators.normalizeText(title),
                            type = type.toGradeType(),
                            cutId = destinationCut,
                            recordedAt = System.currentTimeMillis()
                        )
                    )
                }
            }
        }

        val updatedSubtasks = subtasks?.mapIndexed { index, sub ->
            sub.copy(
                id = if (sub.id.isBlank()) "sub-${UUID.randomUUID()}" else sub.id,
                taskId = taskId,
                position = index
            )
        } ?: existing.subtasks

        tasksRepository.updateTask(
            existing.copy(
                title = TextValidators.normalizeText(title),
                description = description.trim(),
                subjectId = resolvedSubjectId,
                type = type,
                dueDateMillis = parsed.dueDateMillis,
                difficulty = difficulty,
                estimatedMinutes = parsed.estimatedMinutes,
                updatedAt = System.currentTimeMillis(),
                cutId = resolvedCutId,
                gradingStatus = resolvedGradingStatus,
                linkedGradeId = linkedGradeId,
                subtasks = updatedSubtasks
            )
        )
        return true
    }

    fun deleteTask(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        task.linkedGradeId?.let { gradeId ->
            subjects.value.firstOrNull { subject -> subject.grades.any { it.id == gradeId } }
                ?.let { subject ->
                    val grade = subject.grades.first { it.id == gradeId }
                    gradesRepository.updateGrade(subject.id, grade.copy(taskId = null))
                }
        }
        attachmentsOf(taskId).forEach { attachmentStore.delete(it.storedName) }
        tasksRepository.deleteTask(taskId)
        return true
    }

    fun duplicateTask(taskId: String): StudentTask? {
        val source = taskById(taskId) ?: return null
        val now = System.currentTimeMillis()
        val newTaskId = "task-${UUID.randomUUID()}"
        val duplicatedSubtasks = source.subtasks.map { sub ->
            sub.copy(
                id = "sub-${UUID.randomUUID()}",
                taskId = newTaskId,
                isCompleted = false
            )
        }
        val copy = source.copy(
            id = newTaskId,
            title = Textos.get(R.string.tasks_copia, source.title),
            completed = false,
            completedAt = null,
            gradingStatus = if (source.gradingStatus == TaskGradingStatus.NOT_GRADED) {
                TaskGradingStatus.NOT_GRADED
            } else {
                TaskGradingStatus.UNDECIDED
            },
            subtasks = duplicatedSubtasks,
            linkedGradeId = null,
            createdAt = now,
            updatedAt = now
        )
        tasksRepository.addTask(copy)
        return copy
    }

    fun completeTaskFromEditor(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        val now = System.currentTimeMillis()
        val nextStatus = if (
            task.subjectId != null &&
            task.gradingStatus == TaskGradingStatus.UNDECIDED
        ) {
            TaskGradingStatus.AWAITING_GRADE
        } else {
            task.gradingStatus
        }
        tasksRepository.updateTask(
            task.copy(
                completed = true,
                completedAt = task.completedAt ?: now,
                gradingStatus = nextStatus,
                updatedAt = now
            )
        )
        return true
    }

    fun setTaskCompleted(taskId: String, completed: Boolean): TaskCompletionPrompt? {
        val task = taskById(taskId) ?: return null
        val now = System.currentTimeMillis()
        tasksRepository.updateTask(
            task.copy(
                completed = completed,
                completedAt = if (completed) now else null,
                updatedAt = now
            )
        )
        return if (
            completed &&
            task.subjectId != null &&
            task.gradingStatus == TaskGradingStatus.UNDECIDED
        ) {
            TaskCompletionPrompt(task.copy(completed = true, completedAt = now))
        } else {
            null
        }
    }

    fun markTaskAsNotGraded(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        task.linkedGradeId?.let { gradeId ->
            subjects.value.firstOrNull { subject -> subject.grades.any { it.id == gradeId } }
                ?.let { subject -> gradesRepository.deleteGrade(subject.id, gradeId) }
        }
        tasksRepository.updateTask(
            task.copy(
                gradingStatus = TaskGradingStatus.NOT_GRADED,
                linkedGradeId = null,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun markTaskAwaitingGrade(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        tasksRepository.updateTask(
            task.copy(
                gradingStatus = TaskGradingStatus.AWAITING_GRADE,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean? = null) {
        val task = taskById(taskId)
        val targetSub = task?.subtasks?.firstOrNull { it.id == subtaskId }
        val newCompleted = completed ?: (!(targetSub?.isCompleted ?: false))
        tasksRepository.toggleSubtask(taskId, subtaskId, newCompleted)
    }

    fun addSubtask(taskId: String, title: String): Boolean {
        val clean = title.trim()
        if (clean.isEmpty()) return false
        val task = taskById(taskId) ?: return false
        val newSubtask = TaskSubtask(
            id = "sub-${UUID.randomUUID()}",
            taskId = taskId,
            title = clean,
            isCompleted = false,
            position = task.subtasks.size
        )
        tasksRepository.updateTask(task.copy(subtasks = task.subtasks + newSubtask))
        return true
    }

    fun deleteSubtask(taskId: String, subtaskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        val updated = task.subtasks.filterNot { it.id == subtaskId }
            .mapIndexed { index, sub -> sub.copy(position = index) }
        tasksRepository.updateTask(task.copy(subtasks = updated))
        return true
    }

    fun postponeTaskToTomorrow(taskId: String): Long? {
        val task = taskById(taskId) ?: return null
        val today = TaskDateUtils.today()
        val dueLocalDate = TaskDateUtils.fromMillis(task.dueDateMillis)
        val targetDate = if (dueLocalDate <= today) today.plusDays(1) else dueLocalDate.plusDays(1)
        val dueTime = TaskDateUtils.timeFromMillis(task.dueDateMillis)
        val newMillis = TaskDateUtils.toMillis(targetDate, dueTime)
        tasksRepository.postponeTask(taskId, newMillis)
        return newMillis
    }

    fun postponeTaskToNextMonday(taskId: String): Long? {
        val task = taskById(taskId) ?: return null
        val today = TaskDateUtils.today()
        val dayOfWeek = today.dayOfWeek.value
        val daysUntilMonday = ((8 - dayOfWeek) % 7).let { if (it == 0) 7 else it }
        val targetDate = today.plusDays(daysUntilMonday.toLong())
        val dueTime = TaskDateUtils.timeFromMillis(task.dueDateMillis)
        val newMillis = TaskDateUtils.toMillis(targetDate, dueTime)
        tasksRepository.postponeTask(taskId, newMillis)
        return newMillis
    }

    fun postponeTaskToDate(taskId: String, targetDate: LocalDate): Long? {
        val task = taskById(taskId) ?: return null
        val dueTime = TaskDateUtils.timeFromMillis(task.dueDateMillis)
        val newMillis = TaskDateUtils.toMillis(targetDate, dueTime)
        tasksRepository.postponeTask(taskId, newMillis)
        return newMillis
    }

    fun revertTaskToPending(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        val nextStatus = if (task.gradingStatus == TaskGradingStatus.NOT_GRADED) {
            TaskGradingStatus.NOT_GRADED
        } else {
            TaskGradingStatus.UNDECIDED
        }
        tasksRepository.updateTask(
            task.copy(
                completed = false,
                completedAt = null,
                gradingStatus = nextStatus,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun setTaskGradingDecision(taskId: String, onlyDone: Boolean): Boolean {
        val task = taskById(taskId) ?: return false
        val nextStatus = if (onlyDone) {
            TaskGradingStatus.NOT_GRADED
        } else {
            TaskGradingStatus.UNDECIDED
        }
        tasksRepository.setGradingStatus(taskId, nextStatus)
        return true
    }

    fun saveTaskGrade(
        taskId: String,
        value: Double,
        percentageInput: Double?,
        cutId: String
    ): TaskGradeSaveOutcome {
        val task = taskById(taskId) ?: return TaskGradeSaveOutcome(false)
        val subjectId = task.subjectId ?: return TaskGradeSaveOutcome(false)
        val subject = subjects.value.firstOrNull { it.id == subjectId }
            ?: return TaskGradeSaveOutcome(false)
        val maxGrade = userRepository.userProfile.value?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
        if (value !in 0.0..maxGrade) return TaskGradeSaveOutcome(false)
        val weightStatus = if (percentageInput == null) GradeWeightStatus.UNKNOWN else GradeWeightStatus.KNOWN
        val percentage = percentageInput?.div(100.0) ?: 0.0
        val currentWeight = subject.grades
            .filter {
                it.cutId == cutId &&
                    it.source == GradeSource.ACTIVITY &&
                    it.weightStatus == GradeWeightStatus.KNOWN
            }
            .sumOf { it.percentage }
        if (percentageInput != null && (percentage <= 0.0 || currentWeight + percentage > 1.00001)) {
            return TaskGradeSaveOutcome(false)
        }
        val gradeId = "grade-${UUID.randomUUID()}"
        gradesRepository.addGrade(
            subjectId,
            GradeItem(
                id = gradeId,
                name = task.title,
                value = value,
                percentage = percentage,
                type = task.type.toGradeType(),
                cutId = cutId,
                source = GradeSource.ACTIVITY,
                weightStatus = weightStatus,
                taskId = task.id,
                recordedAt = System.currentTimeMillis()
            )
        )
        tasksRepository.updateTask(
            task.copy(
                cutId = cutId,
                gradingStatus = TaskGradingStatus.GRADED,
                linkedGradeId = gradeId,
                completed = true,
                completedAt = task.completedAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        val shouldSuggestHistory = subject.grades.isEmpty() &&
            subject.historyPromptStatus == PriorHistoryPromptStatus.NOT_SHOWN &&
            subject.cutScheme.cuts.firstOrNull { it.id == cutId }?.order?.let { it > 1 } == true
        if (cutId != subject.activeCutId) {
            gradesRepository.updateSubject(subject.copy(activeCutId = cutId))
        }
        return TaskGradeSaveOutcome(true, shouldSuggestHistory, subjectId, gradeId)
    }

    fun unlinkTaskGrade(taskId: String): Boolean {
        val task = taskById(taskId) ?: return false
        val gradeId = task.linkedGradeId ?: return false
        val subject = subjects.value.firstOrNull { candidate ->
            candidate.grades.any { it.id == gradeId }
        } ?: return false
        val grade = subject.grades.first { it.id == gradeId }
        gradesRepository.updateGrade(subject.id, grade.copy(taskId = null))
        tasksRepository.updateTask(
            task.copy(
                gradingStatus = TaskGradingStatus.NOT_GRADED,
                linkedGradeId = null,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun undoTaskGrade(taskId: String, gradeId: String): Boolean {
        val task = taskById(taskId) ?: return false
        val subject = subjects.value.firstOrNull { candidate ->
            candidate.grades.any { it.id == gradeId }
        } ?: return false
        gradesRepository.deleteGrade(subject.id, gradeId)
        tasksRepository.updateTask(
            task.copy(
                gradingStatus = TaskGradingStatus.AWAITING_GRADE,
                linkedGradeId = null,
                completed = true,
                completedAt = task.completedAt ?: System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun updateHistoryPromptStatus(
        subjectId: String,
        status: PriorHistoryPromptStatus
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        gradesRepository.updateSubject(subject.copy(historyPromptStatus = status))
        return true
    }

    private fun validatedTaskInput(
        title: String,
        dueDateInput: String,
        dueTimeInput: String,
        estimatedMinutesInput: String
    ): ParsedTaskInput? {
        if (!TextValidators.validateActivityName(title).isValid) return null
        val dueDate = TaskDateUtils.parseInput(dueDateInput) ?: return null
        val dueTime = if (dueTimeInput.isBlank()) {
            null
        } else {
            TaskDateUtils.parseTimeInput(dueTimeInput) ?: return null
        }
        val estimatedMinutes = estimatedMinutesInput.toIntOrNull() ?: return null
        if (estimatedMinutes !in 1..1440) return null

        return ParsedTaskInput(
            dueDateMillis = TaskDateUtils.toMillis(dueDate, dueTime),
            estimatedMinutes = estimatedMinutes
        )
    }

    private fun resolvedCutId(subjectId: String?, requestedCutId: String?): String? {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return null
        return requestedCutId
            ?.takeIf { id -> subject.cutScheme.cuts.any { it.id == id } }
            ?: subject.defaultCutId
    }

    private fun existingGradingStatus(taskId: String): TaskGradingStatus {
        return taskById(taskId)?.gradingStatus ?: TaskGradingStatus.UNDECIDED
    }
}

private data class ParsedTaskInput(
    val dueDateMillis: Long,
    val estimatedMinutes: Int
)

data class TaskCompletionPrompt(val task: StudentTask)

data class TaskGradeSaveOutcome(
    val saved: Boolean,
    val suggestPriorHistory: Boolean = false,
    val subjectId: String? = null,
    val gradeId: String? = null
)

private fun TaskType.toGradeType() = when (this) {
    TaskType.WORKSHOP -> com.unistack.app.feature_grades.domain.GradeType.WORKSHOP
    TaskType.EXAM -> com.unistack.app.feature_grades.domain.GradeType.EXAM
    TaskType.ESSAY -> com.unistack.app.feature_grades.domain.GradeType.RESEARCH
    TaskType.PRESENTATION -> com.unistack.app.feature_grades.domain.GradeType.PRESENTATION
    TaskType.RESEARCH -> com.unistack.app.feature_grades.domain.GradeType.RESEARCH
    TaskType.TEST -> com.unistack.app.feature_grades.domain.GradeType.QUIZ
    TaskType.PRACTICE -> com.unistack.app.feature_grades.domain.GradeType.PRACTICE
    TaskType.PROJECT -> com.unistack.app.feature_grades.domain.GradeType.PROJECT
    TaskType.READING, TaskType.OTHER -> com.unistack.app.feature_grades.domain.GradeType.OTHER
}
