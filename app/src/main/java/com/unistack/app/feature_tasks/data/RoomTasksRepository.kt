package com.unistack.app.feature_tasks.data

import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.toDomain
import com.unistack.app.feature_tasks.data.local.toEntity
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
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

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userId: String
        get() = UserIds.normalize(userRepository.currentUser.value.userId)

    private val userIds: List<String>
        get() = UserIds.storageIdsFor(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val tasks: StateFlow<List<StudentTask>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            taskDao.observeTasksForUsers(ids).map { entities ->
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
                userIds = userIds,
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
            taskDao.deleteTaskById(taskId, userIds)
        }
    }

    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        scope.launch {
            taskDao.updateTaskCompleted(
                taskId = taskId,
                userIds = userIds,
                completed = completed,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
