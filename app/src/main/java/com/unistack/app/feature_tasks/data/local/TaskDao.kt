package com.unistack.app.feature_tasks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
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
        updatedAt: Long
    )

    @Query(
        """
        UPDATE tasks
        SET completed = :completed,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId IN (:userIds)
        """
    )
    suspend fun updateTaskCompleted(
        taskId: String,
        userIds: List<String>,
        completed: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM tasks WHERE id = :taskId AND userId IN (:userIds)")
    suspend fun deleteTaskById(taskId: String, userIds: List<String>)
}
