package com.unistack.app.feature_grades.data

import com.unistack.app.feature_grades.data.local.GradeDao
import com.unistack.app.feature_grades.data.local.SubjectDao
import com.unistack.app.feature_grades.data.local.toDomain
import com.unistack.app.feature_grades.data.local.toEntity
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.UserIds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RoomGradesRepository(
    private val subjectDao: SubjectDao,
    private val gradeDao: GradeDao,
    private val userRepository: UserRepository
) : GradesRepository {

    @OptIn(ExperimentalCoroutinesApi::class)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    private val userId: String
        get() = UserIds.normalize(userRepository.currentUser.value.userId)

    private val userIds: List<String>
        get() = UserIds.storageIdsFor(userId)

    /**
     * Combines all subjects for the current user with their respective grades.
     * Uses flatMapLatest on the user flow so that if the userId ever changes,
     * subjects automatically refresh.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    override val subjects: StateFlow<List<Subject>> = userRepository.currentUser
        .map { UserIds.storageIdsFor(it.userId) }
        .flatMapLatest { ids ->
            combine(
                subjectDao.observeSubjectsForUsers(ids),
                gradeDao.observeAllGrades()
            ) { subjectEntities, allGrades ->
                val gradesBySubject = allGrades.groupBy { it.subjectId }
                subjectEntities.map { entity ->
                    val grades = gradesBySubject[entity.id]
                        ?.map { it.toDomain() }
                        ?: emptyList()
                    entity.toDomain(grades)
                }
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    override fun addSubject(subject: Subject) {
        scope.launch {
            subjectDao.insertSubject(subject.toEntity(userId))
        }
    }

    override fun updateSubject(subject: Subject) {
        scope.launch {
            subjectDao.updateSubjectFields(
                subjectId = subject.id,
                userIds = userIds,
                name = subject.name,
                targetAverage = subject.targetAverage,
                visualType = subject.visualType.name,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override fun deleteSubject(subjectId: String) {
        scope.launch {
            // Grades are cascade-deleted by the foreign key, but we also
            // delete explicitly for safety.
            gradeDao.deleteGradesForSubject(subjectId)
            subjectDao.deleteSubjectById(subjectId)
        }
    }

    override fun addGrade(subjectId: String, grade: GradeItem) {
        scope.launch {
            gradeDao.insertGrade(grade.toEntity(subjectId))
        }
    }

    override fun updateGrade(subjectId: String, grade: GradeItem) {
        scope.launch {
            gradeDao.updateGradeFields(
                subjectId = subjectId,
                gradeId = grade.id,
                name = grade.name,
                value = grade.value,
                percentage = grade.percentage
            )
        }
    }

    override fun deleteGrade(subjectId: String, gradeId: String) {
        scope.launch {
            gradeDao.deleteGradeById(gradeId)
        }
    }
}
