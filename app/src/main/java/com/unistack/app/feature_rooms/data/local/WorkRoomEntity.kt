package com.unistack.app.feature_rooms.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Una sala = una fila con su documento completo en [json]. */
@Entity(tableName = "work_rooms", indices = [Index("userId")])
data class WorkRoomEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val json: String,
    val updatedAt: Long
)
