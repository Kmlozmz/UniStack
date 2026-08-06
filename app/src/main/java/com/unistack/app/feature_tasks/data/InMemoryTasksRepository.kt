package com.unistack.app.feature_tasks.data

import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryTasksRepository : TasksRepository {
    private val _tasks = MutableStateFlow<List<StudentTask>>(emptyList())
    override val tasks: StateFlow<List<StudentTask>> = _tasks.asStateFlow()

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
    }

    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        _tasks.update { current ->
            current.map { task ->
                if (task.id == taskId) task.copy(completed = completed) else task
            }
        }
    }
}
