package com.unistack.app.feature_tasks.domain

import kotlinx.coroutines.flow.StateFlow

interface TasksRepository {
    val tasks: StateFlow<List<StudentTask>>

    fun addTask(task: StudentTask)
    fun updateTask(task: StudentTask)
    fun deleteTask(taskId: String)
    fun setTaskCompleted(taskId: String, completed: Boolean)
    fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean)
    fun postponeTask(taskId: String, newDueDateMillis: Long)
    fun setGradingStatus(taskId: String, status: TaskGradingStatus, linkedGradeId: String? = null)
}
