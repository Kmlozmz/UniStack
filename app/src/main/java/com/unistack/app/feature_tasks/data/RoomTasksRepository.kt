package com.unistack.app.feature_tasks.data

import com.unistack.app.feature_tasks.data.local.TaskAttachmentDao
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.data.local.toDomain
import com.unistack.app.feature_tasks.data.local.toEntity
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskAttachment
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
    private val attachmentDao: TaskAttachmentDao,
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
            taskDao.observeTasksWithSubtasks(ids).map { entities ->
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    override val attachments: StateFlow<List<TaskAttachment>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            attachmentDao.observeAttachmentsForUsers(ids).map { rows -> rows.map { it.toDomain() } }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override fun addAttachment(attachment: TaskAttachment) {
        scope.launch { attachmentDao.insertAttachment(attachment.toEntity(userId)) }
    }

    override fun deleteAttachment(attachmentId: String) {
        scope.launch { attachmentDao.deleteAttachmentById(attachmentId, userIds) }
    }

    override fun addTask(task: StudentTask) {
        scope.launch {
            val subtaskEntities = task.subtasks.map { it.toEntity() }
            taskDao.insertTaskWithSubtasks(task.toEntity(userId), subtaskEntities)
        }
    }

    override fun updateTask(task: StudentTask) {
        scope.launch {
            taskDao.updateTaskFields(
                taskId = task.id,
                userIds = userIds,
                title = task.title,
                description = task.description,
                subjectId = task.subjectId,
                type = task.type.name,
                dueDateMillis = task.dueDateMillis,
                difficulty = task.difficulty.name,
                estimatedMinutes = task.estimatedMinutes,
                completed = task.completed,
                cutId = task.cutId,
                gradingStatus = task.gradingStatus.name,
                linkedGradeId = task.linkedGradeId,
                completedAt = task.completedAt,
                updatedAt = System.currentTimeMillis()
            )
            val subtaskEntities = task.subtasks.map { it.toEntity() }
            taskDao.deleteSubtasksByTaskId(task.id)
            if (subtaskEntities.isNotEmpty()) {
                taskDao.insertSubtasks(subtaskEntities)
            }
        }
    }

    override fun deleteTask(taskId: String) {
        scope.launch {
            taskDao.deleteTaskById(taskId, userIds)
            attachmentDao.deleteAttachmentsOfTask(taskId, userIds)
        }
    }

    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        scope.launch {
            taskDao.updateTaskCompleted(
                taskId = taskId,
                userIds = userIds,
                completed = completed,
                completedAt = if (completed) System.currentTimeMillis() else null,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean) {
        scope.launch {
            taskDao.updateSubtaskCompleted(subtaskId, completed)
        }
    }

    override fun postponeTask(taskId: String, newDueDateMillis: Long) {
        scope.launch {
            taskDao.updateDueDate(taskId, userIds, newDueDateMillis, System.currentTimeMillis())
        }
    }

    override fun setGradingStatus(taskId: String, status: com.unistack.app.feature_tasks.domain.TaskGradingStatus, linkedGradeId: String?) {
        scope.launch {
            taskDao.updateGradingStatus(taskId, userIds, status.name, linkedGradeId, System.currentTimeMillis())
        }
    }
}
