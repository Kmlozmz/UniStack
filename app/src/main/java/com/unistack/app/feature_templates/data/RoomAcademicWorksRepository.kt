package com.unistack.app.feature_templates.data

import com.unistack.app.feature_templates.data.local.AcademicWorkDao
import com.unistack.app.feature_templates.data.local.toDomain
import com.unistack.app.feature_templates.data.local.toEntity
import com.unistack.app.feature_templates.data.local.toJsonArrayString
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
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

class RoomAcademicWorksRepository(
    private val academicWorkDao: AcademicWorkDao,
    private val userRepository: UserRepository
) : AcademicWorksRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userId: String
        get() = UserIds.normalize(userRepository.currentUser.value.userId)

    private val userIds: List<String>
        get() = UserIds.storageIdsFor(userId)

    @OptIn(ExperimentalCoroutinesApi::class)
    override val works: StateFlow<List<AcademicWork>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            academicWorkDao.observeWorksForUsers(ids).map { entities ->
                entities.map { it.toDomain() }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override fun addWork(work: AcademicWork) {
        scope.launch {
            academicWorkDao.insertWork(work.toEntity(userId))
        }
    }

    override fun updateWork(work: AcademicWork) {
        scope.launch {
            academicWorkDao.updateWorkFields(
                workId = work.id,
                userIds = userIds,
                templateId = work.templateId,
                title = work.title,
                subjectId = work.subjectId,
                dueDateMillis = work.dueDateMillis,
                status = work.status.name,
                priority = work.priority.name,
                completedChecklistIdsJson = work.completedChecklistIds.toJsonArrayString(),
                thesis = work.thesis,
                outline = work.outline,
                sources = work.sources,
                notes = work.notes,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteWork(workId: String) {
        scope.launch {
            academicWorkDao.deleteWorkById(workId, userIds)
        }
    }

    override fun setChecklistItem(workId: String, checklistItemId: String, completed: Boolean) {
        val work = works.value.firstOrNull { it.id == workId } ?: return
        val updatedIds = if (completed) {
            work.completedChecklistIds + checklistItemId
        } else {
            work.completedChecklistIds - checklistItemId
        }
        scope.launch {
            academicWorkDao.updateChecklist(
                workId = workId,
                userIds = userIds,
                completedChecklistIdsJson = updatedIds.toJsonArrayString(),
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun setStatus(workId: String, status: AcademicWorkStatus) {
        scope.launch {
            academicWorkDao.updateStatus(
                workId = workId,
                userIds = userIds,
                status = status.name,
                updatedAt = System.currentTimeMillis()
            )
        }
    }
}
