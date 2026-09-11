package com.unistack.app.feature_terms.data

import com.unistack.app.feature_terms.data.local.AcademicBreakDao
import com.unistack.app.feature_terms.data.local.toDomain
import com.unistack.app.feature_terms.data.local.toEntity
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
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
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

@OptIn(ExperimentalCoroutinesApi::class)
class RoomAcademicBreakRepository(
    private val dao: AcademicBreakDao,
    private val userRepository: UserRepository
) : AcademicBreakRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))
    private val userId get() = UserIds.normalize(userRepository.currentUser.value.userId)
    private val userIds get() = UserIds.storageIdsFor(userId)

    override val breaks: StateFlow<List<AcademicBreak>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids -> dao.observeForUsers(ids).map { rows -> rows.map { it.toDomain() } } }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    /**
     * Crea o edita, según venga [id].
     *
     * No se comprueba que dos tramos no se solapen: solaparse no rompe nada —un día sin clase
     * dos veces sigue siendo un día sin clase— y prohibirlo obligaría a explicar por qué no se
     * puede apuntar un festivo que cae dentro de la semana de receso.
     */
    override suspend fun save(
        id: String?,
        name: String,
        start: LocalDate,
        end: LocalDate
    ): Result<AcademicBreak> = runCatching {
        val limpio = name.trim()
        require(limpio.isNotBlank()) { Textos.get(R.string.break_err_name) }
        require(!end.isBefore(start)) { Textos.get(R.string.break_err_order) }

        val existente = id?.let { dao.byId(it, userIds) }
        val now = System.currentTimeMillis()
        val item = AcademicBreak(
            id = existente?.id ?: id ?: UUID.randomUUID().toString(),
            userId = existente?.userId ?: userId,
            name = limpio.take(60),
            startEpochDay = start.toEpochDay(),
            endEpochDay = end.toEpochDay(),
            createdAt = existente?.createdAt ?: now,
            updatedAt = now
        )
        dao.upsert(item.toEntity())
        item
    }

    override suspend fun delete(breakId: String): Result<Unit> = runCatching {
        dao.delete(breakId, userIds)
    }
}
