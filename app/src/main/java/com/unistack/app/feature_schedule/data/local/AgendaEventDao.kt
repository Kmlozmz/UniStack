package com.unistack.app.feature_schedule.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AgendaEventDao {
    @Query("SELECT * FROM agenda_events WHERE userId IN (:userIds) ORDER BY startMillis ASC")
    fun observeForUsers(userIds: List<String>): Flow<List<AgendaEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: AgendaEventEntity)

    @Query("DELETE FROM agenda_events WHERE id = :eventId AND userId IN (:userIds)")
    suspend fun delete(eventId: String, userIds: List<String>)
}
