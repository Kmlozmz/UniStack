package com.unistack.app.feature_schedule.data

import com.unistack.app.feature_schedule.data.local.ClassSessionDao
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceDao
import com.unistack.app.feature_schedule.data.local.AgendaEventDao
import com.unistack.app.feature_schedule.data.local.toDomain
import com.unistack.app.feature_schedule.data.local.toEntity
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomScheduleRepository(
    private val dao: ClassSessionDao,
    private val occurrenceDao: ClassOccurrenceDao,
    private val agendaEventDao: AgendaEventDao,
    private val userRepository: UserRepository
) : ScheduleRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val userId get() = UserIds.normalize(userRepository.currentUser.value.userId)
    private val userIds get() = UserIds.storageIdsFor(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val sessions: StateFlow<List<ClassSession>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids -> dao.observeForUsers(ids).map { rows -> rows.map { it.toDomain() } } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val occurrences: StateFlow<List<ClassOccurrence>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            occurrenceDao.observeForUsers(ids).map { rows -> rows.map { it.toDomain() } }
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    override val agendaEvents: StateFlow<List<AgendaEvent>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids -> agendaEventDao.observeForUsers(ids).map { rows -> rows.map { it.toDomain() } } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    override fun saveSession(session: ClassSession) {
        if (!session.isValid) return
        scope.launch { dao.insert(session.copy(updatedAt = System.currentTimeMillis()).toEntity(userId)) }
    }

    override fun deleteSession(sessionId: String) {
        scope.launch { dao.delete(sessionId, userIds) }
    }

    override fun saveOccurrence(occurrence: ClassOccurrence) {
        if (!occurrence.isValid) return
        scope.launch {
            occurrenceDao.insert(
                occurrence.copy(updatedAt = System.currentTimeMillis()).toEntity(userId)
            )
        }
    }

    override fun deleteOccurrence(occurrenceId: String) {
        scope.launch { occurrenceDao.delete(occurrenceId, userIds) }
    }

    override fun saveAgendaEvent(event: AgendaEvent) {
        if (!event.isValid) return
        scope.launch { agendaEventDao.insert(event.copy(updatedAt = System.currentTimeMillis()).toEntity(userId)) }
    }

    override fun deleteAgendaEvent(eventId: String) {
        scope.launch { agendaEventDao.delete(eventId, userIds) }
    }
}
