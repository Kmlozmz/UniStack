package com.unistack.app.feature_tasks.data

import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.toDomain
import com.unistack.app.feature_tasks.data.local.toEntity
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomTasksRepository(
    private val taskDao: TaskDao,
    private val userRepository: UserRepository
) : TasksRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val userId: String
        get() = userRepository.currentUser.value.userId.ifBlank { "local_user" }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val tasks: StateFlow<List<StudentTask>> = userRepository.currentUser
        .map { it.userId.ifBlank { "local_user" } }
        .flatMapLatest { uid ->
            taskDao.observeTasksForUser(uid).map { entities ->
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override fun addTask(task: StudentTask) {
        scope.launch {
            taskDao.insertTask(task.toEntity(userId))
        }
    }

    override fun updateTask(task: StudentTask) {
        scope.launch {
            taskDao.updateTaskFields(
                taskId = task.id,
                userId = userId,
                title = task.title,
                subjectId = task.subjectId,
                dueDateMillis = task.dueDateMillis,
                difficulty = task.difficulty.name,
                estimatedMinutes = task.estimatedMinutes,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteTask(taskId: String) {
        scope.launch {
            taskDao.deleteTaskById(taskId, userId)
        }
    }

    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        scope.launch {
            taskDao.updateTaskCompleted(
                taskId = taskId,
                userId = userId,
                completed = completed,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
