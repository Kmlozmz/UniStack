package com.unistack.app.feature_terms.data

import com.unistack.app.feature_terms.data.local.AcademicTermDao
import com.unistack.app.feature_terms.data.local.toDomain
import com.unistack.app.feature_terms.data.local.toEntity
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserRepository
import java.time.LocalDate
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class RoomAcademicTermRepository(
    private val dao: AcademicTermDao,
    private val userRepository: UserRepository
) : AcademicTermRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val userId get() = UserIds.normalize(userRepository.currentUser.value.userId)
    private val userIds get() = UserIds.storageIdsFor(userId)

    override val terms: StateFlow<List<AcademicTerm>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids -> dao.observeForUsers(ids).map { rows -> rows.map { it.toDomain() } } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /*
     * Solo puede haber uno activo, y si hubiera varios manda el que empezo despues.
     *
     * `create` ya impide abrir un segundo con uno en curso, asi que esto no deberia darse.
     * Pero cursar dos programas a la vez esta previsto para mas adelante, y cuando llegue este
     * `firstOrNull` sera lo primero que haya que cambiar: prefiero que quede escrito aqui a
     * que la app elija uno al azar el dia que existan dos.
     */
    override val activeTerm: StateFlow<AcademicTerm?> = terms
        .map { lista -> lista.firstOrNull { it.isActive } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override suspend fun create(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?
    ): Result<AcademicTerm> = runCatching {
        require(name.isNotBlank()) { "El periodo necesita un nombre." }
        require(plannedEnd == null || plannedEnd.isAfter(start)) {
            "El periodo no puede acabar antes de empezar."
        }
        // Se pregunta a la base, no al flujo en cache: ver `activeFor`.
        check(dao.activeFor(userIds) == null) {
            "Ya hay un periodo en curso. Ciérralo antes de empezar otro."
        }
        val now = System.currentTimeMillis()
        val term = AcademicTerm(
            id = UUID.randomUUID().toString(),
            userId = userId,
            name = name.trim(),
            type = type,
            startEpochDay = start.toEpochDay(),
            plannedEndEpochDay = plannedEnd?.toEpochDay(),
            closedEpochDay = null,
            status = AcademicTermStatus.ACTIVE,
            createdAt = now,
            updatedAt = now
        )
        dao.upsert(term.toEntity())
        term
    }

    override suspend fun update(term: AcademicTerm): Result<Unit> = runCatching {
        require(term.isValid) { "Ese periodo no es válido." }
        dao.upsert(term.copy(updatedAt = System.currentTimeMillis()).toEntity())
    }

    override suspend fun close(termId: String, closedOn: LocalDate): Result<Unit> = runCatching {
        val term = dao.byId(termId, userIds)?.toDomain()
            ?: error("Ese periodo ya no existe.")
        check(term.isActive) { "Ese periodo ya estaba cerrado." }
        require(!closedOn.isBefore(term.start)) {
            "No se puede cerrar un periodo antes del día en que empezó."
        }
        val cambiadas = dao.close(
            termId = termId,
            closedEpochDay = closedOn.toEpochDay(),
            updatedAt = System.currentTimeMillis(),
            userIds = userIds
        )
        // Cero filas significa que alguien lo cerro entremedias; la primera fecha es la buena.
        check(cambiadas > 0) { "Ese periodo ya estaba cerrado." }
    }

    override suspend fun delete(termId: String): Result<Unit> = runCatching {
        dao.delete(termId, userIds)
    }
}
