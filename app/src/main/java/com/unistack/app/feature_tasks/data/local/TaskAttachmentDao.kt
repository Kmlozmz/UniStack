package com.unistack.app.feature_tasks.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskAttachmentDao {
    @Query(
        """
        SELECT * FROM task_attachments
        WHERE userId IN (:userIds)
        ORDER BY createdAt ASC
        """
    )
    fun observeAttachmentsForUsers(userIds: List<String>): Flow<List<TaskAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: TaskAttachmentEntity)

    @Query("SELECT storedName FROM task_attachments WHERE id = :attachmentId AND userId IN (:userIds)")
    suspend fun storedNameOf(attachmentId: String, userIds: List<String>): String?

    @Query("SELECT storedName FROM task_attachments WHERE taskId = :taskId AND userId IN (:userIds)")
    suspend fun storedNamesOfTask(taskId: String, userIds: List<String>): List<String>

    @Query("DELETE FROM task_attachments WHERE id = :attachmentId AND userId IN (:userIds)")
    suspend fun deleteAttachmentById(attachmentId: String, userIds: List<String>)

    @Query("DELETE FROM task_attachments WHERE taskId = :taskId AND userId IN (:userIds)")
    suspend fun deleteAttachmentsOfTask(taskId: String, userIds: List<String>)
}
