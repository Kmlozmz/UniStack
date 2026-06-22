package com.unistack.app.feature_schedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassSessionDao {
    @Query(
        """
        SELECT * FROM class_sessions
        WHERE userId IN (:userIds)
        ORDER BY startMinute ASC, createdAt ASC
        """
    )
    fun observeForUsers(userIds: List<String>): Flow<List<ClassSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ClassSessionEntity)

    @Query("DELETE FROM class_sessions WHERE id = :sessionId AND userId IN (:userIds)")
    suspend fun delete(sessionId: String, userIds: List<String>)
}
