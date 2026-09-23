package com.unistack.app.feature_rooms.domain

import kotlinx.coroutines.flow.StateFlow

interface WorkRoomsRepository {
    val rooms: StateFlow<List<WorkRoom>>
    fun save(room: WorkRoom)
    fun update(roomId: String, transform: (WorkRoom) -> WorkRoom)
    fun delete(roomId: String)
    fun deleteAll()
}
