package com.unistack.app.feature_schedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ClassOccurrenceDao {
    @Query(
        """
        SELECT * FROM class_occurrences
        WHERE userId IN (:userIds)
        ORDER BY dateEpochDay DESC, updatedAt DESC
        """
    )
    fun observeForUsers(userIds: List<String>): Flow<List<ClassOccurrenceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(occurrence: ClassOccurrenceEntity)

    @Query("DELETE FROM class_occurrences WHERE id = :occurrenceId AND userId IN (:userIds)")
    suspend fun delete(occurrenceId: String, userIds: List<String>)
}
