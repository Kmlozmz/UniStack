package com.unistack.app.feature_templates.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicWorkDao {
    @Query(
        """
        SELECT * FROM academic_works
        WHERE userId IN (:userIds)
        ORDER BY status = 'SUBMITTED' ASC,
                 dueDateMillis IS NULL ASC,
                 dueDateMillis ASC,
                 updatedAt DESC
        """
    )
    fun observeWorksForUsers(userIds: List<String>): Flow<List<AcademicWorkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWork(work: AcademicWorkEntity)

    @Query(
        """
        UPDATE academic_works
        SET templateId = :templateId,
            title = :title,
            subjectId = :subjectId,
            dueDateMillis = :dueDateMillis,
            status = :status,
            priority = :priority,
            completedChecklistIdsJson = :completedChecklistIdsJson,
            thesis = :thesis,
            outline = :outline,
            sources = :sources,
            notes = :notes,
            updatedAt = :updatedAt
        WHERE id = :workId AND userId IN (:userIds)
        """
    )
    suspend fun updateWorkFields(
        workId: String,
        userIds: List<String>,
        templateId: String,
        title: String,
        subjectId: String?,
        dueDateMillis: Long?,
        status: String,
        priority: String,
        completedChecklistIdsJson: String,
        thesis: String,
        outline: String,
        sources: String,
        notes: String,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE academic_works
        SET completedChecklistIdsJson = :completedChecklistIdsJson,
            updatedAt = :updatedAt
        WHERE id = :workId AND userId IN (:userIds)
        """
    )
    suspend fun updateChecklist(
        workId: String,
        userIds: List<String>,
        completedChecklistIdsJson: String,
        updatedAt: Long
    )

    @Query(
        """
        UPDATE academic_works
        SET status = :status,
            updatedAt = :updatedAt
        WHERE id = :workId AND userId IN (:userIds)
        """
    )
    suspend fun updateStatus(
        workId: String,
        userIds: List<String>,
        status: String,
        updatedAt: Long
    )

    @Query("DELETE FROM academic_works WHERE id = :workId AND userId IN (:userIds)")
    suspend fun deleteWorkById(workId: String, userIds: List<String>)
}
