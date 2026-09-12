package com.unistack.app.feature_tasks.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class TaskWithSubtasks(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<TaskSubtaskEntity> = emptyList()
)

@Dao
interface TaskDao {
    @Transaction
    @Query(
        """
        SELECT * FROM tasks
        WHERE userId IN (:userIds)
        ORDER BY completed ASC, dueDateMillis ASC, createdAt DESC
        """
    )
    fun observeTasksWithSubtasks(userIds: List<String>): Flow<List<TaskWithSubtasks>>

    @Query(
        """
        SELECT * FROM tasks
        WHERE userId IN (:userIds)
        ORDER BY completed ASC, dueDateMillis ASC, createdAt DESC
        """
    )
    fun observeTasksForUsers(userIds: List<String>): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtasks(subtasks: List<TaskSubtaskEntity>)

    @Query("DELETE FROM task_subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksByTaskId(taskId: String)

    @Query("UPDATE task_subtasks SET isCompleted = :isCompleted WHERE id = :subtaskId")
    suspend fun updateSubtaskCompleted(subtaskId: String, isCompleted: Boolean)

    @Transaction
    suspend fun insertTaskWithSubtasks(task: TaskEntity, subtasks: List<TaskSubtaskEntity>) {
        insertTask(task)
        deleteSubtasksByTaskId(task.id)
        if (subtasks.isNotEmpty()) {
            insertSubtasks(subtasks)
        }
    }

    @Query(
        """
        UPDATE tasks
        SET title = :title,
            description = :description,
            subjectId = :subjectId,
            type = :type,
            dueDateMillis = :dueDateMillis,
            difficulty = :difficulty,
            estimatedMinutes = :estimatedMinutes,
            completed = :completed,
            periodId = :cutId,
            gradingStatus = :gradingStatus,
            linkedGradeId = :linkedGradeId,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId IN (:userIds)
        """
    )
    suspend fun updateTaskFields(
        taskId: String,
        userIds: List<String>,
        title: String,
        description: String,
        subjectId: String?,
        type: String,
        dueDateMillis: Long,
        difficulty: String,
        estimatedMinutes: Int,
        completed: Boolean,
        cutId: String?,
        gradingStatus: String,
        linkedGradeId: String?,
        completedAt: Long?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE tasks
        SET completed = :completed,
            completedAt = :completedAt,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId IN (:userIds)
        """
    )
    suspend fun updateTaskCompleted(
        taskId: String,
        userIds: List<String>,
        completed: Boolean,
        completedAt: Long?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE tasks
        SET gradingStatus = :gradingStatus,
            linkedGradeId = :linkedGradeId,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId IN (:userIds)
        """
    )
    suspend fun updateGradingStatus(
        taskId: String,
        userIds: List<String>,
        gradingStatus: String,
        linkedGradeId: String?,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE tasks
        SET dueDateMillis = :newDueDateMillis,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId IN (:userIds)
        """
    )
    suspend fun updateDueDate(
        taskId: String,
        userIds: List<String>,
        newDueDateMillis: Long,
        updatedAt: Long
    )

    @Query("DELETE FROM tasks WHERE id = :taskId AND userId IN (:userIds)")
    suspend fun deleteTaskById(taskId: String, userIds: List<String>)
}
