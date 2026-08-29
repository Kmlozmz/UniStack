package com.unistack.app.feature_notes.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteAttachmentDao {
    @Query(
        """
        SELECT * FROM note_attachments
        WHERE userId IN (:userIds)
        ORDER BY createdAt ASC
        """
    )
    fun observeAttachmentsForUsers(userIds: List<String>): Flow<List<NoteAttachmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttachment(attachment: NoteAttachmentEntity)

    @Query("SELECT storedName FROM note_attachments WHERE id = :attachmentId AND userId IN (:userIds)")
    suspend fun storedNameOf(attachmentId: String, userIds: List<String>): String?

    @Query("SELECT storedName FROM note_attachments WHERE noteId = :noteId AND userId IN (:userIds)")
    suspend fun storedNamesOfNote(noteId: String, userIds: List<String>): List<String>

    @Query("DELETE FROM note_attachments WHERE id = :attachmentId AND userId IN (:userIds)")
    suspend fun deleteAttachmentById(attachmentId: String, userIds: List<String>)

    @Query("DELETE FROM note_attachments WHERE noteId = :noteId AND userId IN (:userIds)")
    suspend fun deleteAttachmentsOfNote(noteId: String, userIds: List<String>)
}
