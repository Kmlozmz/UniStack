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
import org.json.JSONArray

/**
 * @param activeTermId de qué periodo son las materias que se creen ahora.
 *
 * Llega como función y no como repositorio porque es lo único que hace falta de él, y así el
 * almacén de notas no queda atado a cuándo se construye el de periodos.
 */
class RoomGradesRepository(
    private val subjectDao: SubjectDao,
    private val gradeDao: GradeDao,
    private val userRepository: UserRepository,
    private val activeTermId: () -> String? = { null }
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

    /**
     * Una materia nace en el periodo que se está cursando.
     *
     * Se estampa aquí y no en cada pantalla porque hay varias puertas —el formulario, las
     * plantillas, el onboarding— y la que se olvidara dejaría materias sueltas que el
     * histórico no sabría de quién son. Si el [Subject] ya trae periodo se respeta: eso es lo
     * que pasa al restaurar una copia de seguridad, donde el dato ya viene decidido.
     */
    override fun addSubject(subject: Subject) {
        scope.launch {
            val conPeriodo = if (subject.termId == null) {
                subject.copy(termId = activeTermId())
            } else {
                subject
            }
            subjectDao.insertSubject(conPeriodo.toEntity(userId))
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
                customColor = subject.customColor,
                periodSchemeJson = subject.toEntity(userId).periodSchemeJson,
                activeCutId = subject.activeCutId,
                historyPromptStatus = subject.historyPromptStatus.name,
                unknownPeriodIdsJson = JSONArray(subject.unknownCutIds.toList()).toString(),
                termId = subject.termId,
                repeatedFromSubjectId = subject.repeatedFromSubjectId,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun stampTerm(termId: String) {
        subjectDao.stampMissingTerm(termId, System.currentTimeMillis(), userIds)
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
                percentage = grade.percentage,
                type = grade.type.name,
                cutId = grade.cutId,
                source = grade.source.name,
                weightStatus = grade.weightStatus.name,
                taskId = grade.taskId,
                recordedAt = grade.recordedAt
            )
        }
    }

    override fun deleteGrade(subjectId: String, gradeId: String) {
        scope.launch {
            gradeDao.deleteGradeById(gradeId)
        }
    }

    override fun clearGrades(subjectId: String) {
        scope.launch {
            gradeDao.deleteGradesForSubject(subjectId)
        }
    }
}
