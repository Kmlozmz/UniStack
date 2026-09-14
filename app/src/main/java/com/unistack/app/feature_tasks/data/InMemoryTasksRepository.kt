package com.unistack.app.feature_tasks.data

import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskAttachment
import com.unistack.app.feature_tasks.domain.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTasksRepository : TasksRepository {
    private val _tasks = MutableStateFlow<List<StudentTask>>(emptyList())
    override val tasks: StateFlow<List<StudentTask>> = _tasks.asStateFlow()

    private val _attachments = MutableStateFlow<List<TaskAttachment>>(emptyList())
    override val attachments: StateFlow<List<TaskAttachment>> = _attachments.asStateFlow()

    override fun addAttachment(attachment: TaskAttachment) {
        _attachments.update { current -> current + attachment }
    }

    override fun deleteAttachment(attachmentId: String) {
        _attachments.update { current -> current.filterNot { it.id == attachmentId } }
    }

    override fun addTask(task: StudentTask) {
        _tasks.update { current ->
            if (current.any { it.id == task.id }) current else current + task
        }
    }

    override fun updateTask(task: StudentTask) {
        _tasks.update { current ->
            current.map { existing -> if (existing.id == task.id) task else existing }
        }
    }

    override fun deleteTask(taskId: String) {
        _tasks.update { current -> current.filterNot { it.id == taskId } }
        _attachments.update { current -> current.filterNot { it.taskId == taskId } }
    }

    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        completed = completed,
                        completedAt = if (completed) System.currentTimeMillis() else null
                    )
                } else task
            }
        }
    }

    override fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) {
                    task.copy(
                        subtasks = task.subtasks.map { sub ->
                            if (sub.id == subtaskId) sub.copy(isCompleted = completed) else sub
                        }
                    )
                } else task
            }
        }
    }

    override fun postponeTask(taskId: String, newDueDateMillis: Long) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) task.copy(dueDateMillis = newDueDateMillis) else task
            }
        }
    }

    override fun setGradingStatus(taskId: String, status: com.unistack.app.feature_tasks.domain.TaskGradingStatus, linkedGradeId: String?) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) task.copy(gradingStatus = status, linkedGradeId = linkedGradeId) else task
            }
        }
    }
}
