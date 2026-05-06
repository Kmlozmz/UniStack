package com.unistack.app.feature_tasks.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TasksRepository
import kotlinx.coroutines.flow.StateFlow

class TasksViewModel(
    private val tasksRepository: TasksRepository = AppContainer.tasksRepository,
    private val gradesRepository: GradesRepository = AppContainer.gradesRepository
) : ViewModel() {
    val tasks: StateFlow<List<StudentTask>> = tasksRepository.tasks
    val subjects: StateFlow<List<Subject>> = gradesRepository.subjects

    fun taskById(taskId: String): StudentTask? {
        return tasks.value.firstOrNull { it.id == taskId }
    }

    fun subjectName(subjectId: String?): String? {
        if (subjectId == null) return null
        return subjects.value.firstOrNull { it.id == subjectId }?.name
    }

    fun addTask(
        title: String,
        subjectId: String?,
        dueDateInput: String,
        estimatedMinutesInput: String,
        difficulty: TaskDifficulty
    ): Boolean {
        val parsed = validatedTaskInput(
            title = title,
            dueDateInput = dueDateInput,
            estimatedMinutesInput = estimatedMinutesInput
        ) ?: return false

        val now = System.currentTimeMillis()
        tasksRepository.addTask(
            StudentTask(
                id = "task-$now",
                title = TextValidators.normalizeText(title),
                subjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } },
                dueDateMillis = parsed.dueDateMillis,
                difficulty = difficulty,
                estimatedMinutes = parsed.estimatedMinutes,
                completed = false,
                createdAt = now,
                updatedAt = now
            )
        )
        return true
    }

    fun updateTask(
        taskId: String,
        title: String,
        subjectId: String?,
        dueDateInput: String,
        estimatedMinutesInput: String,
        difficulty: TaskDifficulty
    ): Boolean {
        val existing = taskById(taskId) ?: return false
        val parsed = validatedTaskInput(
            title = title,
            dueDateInput = dueDateInput,
            estimatedMinutesInput = estimatedMinutesInput
        ) ?: return false

        tasksRepository.updateTask(
            existing.copy(
                title = TextValidators.normalizeText(title),
                subjectId = subjectId.takeIf { id -> subjects.value.any { it.id == id } },
                dueDateMillis = parsed.dueDateMillis,
                difficulty = difficulty,
                estimatedMinutes = parsed.estimatedMinutes,
                updatedAt = System.currentTimeMillis()
            )
        )
        return true
    }

    fun deleteTask(taskId: String): Boolean {
        val exists = tasks.value.any { it.id == taskId }
        if (!exists) return false
        tasksRepository.deleteTask(taskId)
        return true
    }

    fun setTaskCompleted(taskId: String, completed: Boolean): Boolean {
        val exists = tasks.value.any { it.id == taskId }
        if (!exists) return false
        tasksRepository.setTaskCompleted(taskId, completed)
        return true
    }

    private fun validatedTaskInput(
        title: String,
        dueDateInput: String,
        estimatedMinutesInput: String
    ): ParsedTaskInput? {
        if (!TextValidators.validateActivityName(title).isValid) return null
        val dueDate = TaskDateUtils.parseInput(dueDateInput) ?: return null
        val estimatedMinutes = estimatedMinutesInput.toIntOrNull() ?: return null
        if (estimatedMinutes !in 1..1440) return null

        return ParsedTaskInput(
            dueDateMillis = TaskDateUtils.toMillis(dueDate),
            estimatedMinutes = estimatedMinutes
        )
    }
}

private data class ParsedTaskInput(
    val dueDateMillis: Long,
    val estimatedMinutes: Int
)
