package com.unistack.app.feature_rooms.data

import com.unistack.app.feature_rooms.data.local.WorkRoomDao
import com.unistack.app.feature_rooms.data.local.WorkRoomEntity
import com.unistack.app.feature_rooms.domain.WorkRoom
import com.unistack.app.feature_rooms.domain.WorkRoomsRepository
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Las salas en Room.
 *
 * El estado vive en memoria ([rooms]) y se escribe detrás: así un cambio en la sala se ve al
 * instante aunque la escritura tarde, y [update] siempre parte de la última versión.
 */
class RoomWorkRoomsRepository(
    private val dao: WorkRoomDao,
    private val userRepository: UserRepository
) : WorkRoomsRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val state = MutableStateFlow<List<WorkRoom>>(emptyList())
    override val rooms: StateFlow<List<WorkRoom>> = state.asStateFlow()

    private val userId get() = UserIds.normalize(userRepository.currentUser.value.userId)
    private val userIds get() = UserIds.storageIdsFor(userId)

    init {
        @OptIn(ExperimentalCoroutinesApi::class)
        scope.launch {
            userRepository.currentUser
                .map { UserIds.storageIdsFor(it.userId) }
                .flatMapLatest { ids -> dao.observeRooms(ids) }
                .collect { list ->
                    state.value = list.mapNotNull { runCatching { WorkRoomJson.decode(it.json) }.getOrNull() }
                        .sortedByDescending { it.updatedAt }
                }
        }
    }

    override fun save(room: WorkRoom) {
        val r = room.copy(updatedAt = System.currentTimeMillis())
        state.value = (listOf(r) + state.value.filterNot { it.id == r.id }).sortedByDescending { it.updatedAt }
        scope.launch { dao.upsert(WorkRoomEntity(r.id, userId, WorkRoomJson.encode(r), r.updatedAt)) }
    }

    override fun update(roomId: String, transform: (WorkRoom) -> WorkRoom) {
        val current = state.value.firstOrNull { it.id == roomId } ?: return
        save(transform(current))
    }

    override fun delete(roomId: String) {
        state.value = state.value.filterNot { it.id == roomId }
        scope.launch { dao.delete(roomId, userIds) }
    }

    override fun deleteAll() {
        state.value = emptyList()
        scope.launch { dao.deleteAll(userIds) }
    }
}
