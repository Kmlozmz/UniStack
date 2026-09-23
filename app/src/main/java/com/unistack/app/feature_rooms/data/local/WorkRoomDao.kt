package com.unistack.app.feature_rooms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkRoomDao {
    @Query("SELECT * FROM work_rooms WHERE userId IN (:userIds) ORDER BY updatedAt DESC")
    fun observeRooms(userIds: List<String>): Flow<List<WorkRoomEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(room: WorkRoomEntity)

    @Query("DELETE FROM work_rooms WHERE id = :id AND userId IN (:userIds)")
    suspend fun delete(id: String, userIds: List<String>)

    @Query("DELETE FROM work_rooms WHERE userId IN (:userIds)")
    suspend fun deleteAll(userIds: List<String>)
}
