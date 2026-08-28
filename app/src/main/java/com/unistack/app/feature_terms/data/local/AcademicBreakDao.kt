package com.unistack.app.feature_terms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AcademicBreakDao {
    /** Del más reciente al más antiguo, igual que el histórico de periodos. */
    @Query("SELECT * FROM academic_breaks WHERE userId IN (:userIds) ORDER BY startEpochDay DESC")
    fun observeForUsers(userIds: List<String>): Flow<List<AcademicBreakEntity>>

    @Query("SELECT * FROM academic_breaks WHERE id = :breakId AND userId IN (:userIds)")
    suspend fun byId(breakId: String, userIds: List<String>): AcademicBreakEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(item: AcademicBreakEntity)

    @Query("DELETE FROM academic_breaks WHERE id = :breakId AND userId IN (:userIds)")
    suspend fun delete(breakId: String, userIds: List<String>)
}
