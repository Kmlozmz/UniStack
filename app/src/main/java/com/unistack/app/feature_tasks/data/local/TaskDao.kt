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
        WHERE userId = :userId
        ORDER BY completed ASC, dueDateMillis ASC, createdAt DESC
        """
    )
    fun observeTasksForUser(userId: String): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity)

    @Query(
        """
        UPDATE tasks
        SET title = :title,
            subjectId = :subjectId,
            dueDateMillis = :dueDateMillis,
            difficulty = :difficulty,
            estimatedMinutes = :estimatedMinutes,
            updatedAt = :updatedAt
        WHERE id = :taskId AND userId = :userId
        """
    )
    suspend fun updateTaskFields(
        taskId: String,
        userId: String,
        title: String,
        subjectId: String?,
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
        WHERE id = :taskId AND userId = :userId
        """
    )
    suspend fun updateTaskCompleted(
        taskId: String,
        userId: String,
        completed: Boolean,
        updatedAt: Long
    )

    @Query("DELETE FROM tasks WHERE id = :taskId AND userId = :userId")
    suspend fun deleteTaskById(taskId: String, userId: String)
}
