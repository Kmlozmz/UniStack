package com.unistack.app.feature_grades.presentation

import androidx.lifecycle.ViewModel
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import java.util.Locale
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.SubjectGradeCalculation
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_schedule.domain.SubjectScheduleDraft
import kotlinx.coroutines.flow.StateFlow
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import java.util.UUID

@HiltViewModel
class GradesViewModel @Inject constructor(
    private val repository: GradesRepository,
    private val userRepository: UserRepository,
    private val tasksRepository: TasksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val academicWorksRepository: AcademicWorksRepository,
    private val termRepository: AcademicTermRepository
) : ViewModel() {
    val subjects: StateFlow<List<Subject>> = repository.subjects
    val userProfile: StateFlow<UserProfile?> = userRepository.userProfile
    val academicWorks: StateFlow<List<AcademicWork>> = academicWorksRepository.works
    val classSessions: StateFlow<List<ClassSession>> = scheduleRepository.sessions
    val tasks: StateFlow<List<StudentTask>> = tasksRepository.tasks

    private fun getMaxGrade(): Double {
        val profile = userProfile.value ?: return 5.0
        return GradingScaleUtils.maxGradeFor(profile)
    }

    /**
     * Guarda el orden en el que quieres ver tus materias.
     *
     * Se llama al soltar, no mientras arrastras: mientras el dedo se mueve, la lista permuta en
     * pantalla y escribir en disco en cada permuta sería una escritura por cada fila que cruzas.
     */
    fun saveSubjectOrder(ids: List<String>) {
        val profile = userProfile.value ?: return
        userRepository.saveUserProfile(
            profile.copy(
                appearancePreferences = profile.appearancePreferences.copy(subjectOrder = ids),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    fun addSubject(
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null,
        activeCutId: String? = null
    ): Subject? {
        if (!TextValidators.validateSubjectName(name).isValid) return null
        if (targetAverage !in 0.0..getMaxGrade()) return null
        val cutScheme = userProfile.value?.gradingCutScheme
            ?: com.unistack.app.feature_user.domain.GradingCutScheme.default()
        val subject = Subject(
            id = "subject-${UUID.randomUUID()}",
            name = TextValidators.normalizeText(name),
            targetAverage = targetAverage,
            grades = emptyList(),
            visualType = visualType,
            customColor = customColor,
            cutScheme = cutScheme,
            // Vacío si no llega uno válido: una materia nueva no tiene corte elegido hasta
            // que alguien lo elige.
            activeCutId = activeCutId
                ?.takeIf { id -> cutScheme.cuts.any { it.id == id } }
                .orEmpty()
        )
        repository.addSubject(subject)
        return subject
    }

    fun updateSubject(
        subjectId: String,
        name: String,
        targetAverage: Double,
        visualType: SubjectVisualType,
        customColor: Int? = null,
        activeCutId: String? = null
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        if (!TextValidators.validateSubjectName(name).isValid) return false
        if (targetAverage !in 0.0..getMaxGrade()) return false

        repository.updateSubject(
            subject.copy(
                name = TextValidators.normalizeText(name),
                targetAverage = targetAverage,
                visualType = visualType,
                customColor = customColor,
                activeCutId = activeCutId
                    ?.takeIf { id -> subject.cutScheme.cuts.any { it.id == id } }
                    ?: subject.activeCutId
            )
        )
        return true
    }

    /**
     * El horario de una materia, que puede ser **más de una franja**.
     *
     * Una materia que se da dos veces por semana a horas distintas —lunes 8-10 y miércoles
     * 14-16— es lo normal, no la excepción. La tabla siempre lo admitió: `ClassSession` lleva
     * `subjectId` y nada impide varias. Lo que lo impedía era esto: se guardaba la primera y
     * **se borraban las demás**, así que aunque existieran, editar la materia se las llevaba.
     *
     * Las franjas existentes se reaprovechan por orden para no romper lo que cuelgue de su id
     * —la asistencia ya registrada, sin ir más lejos—; sólo se borran las que sobran.
     */
    fun saveSubjectSchedule(subjectId: String, drafts: List<SubjectScheduleDraft>): Boolean {
        if (subjectId.isBlank()) return false
        val existing = classSessions.value.filter { it.subjectId == subjectId }
        val activos = drafts.filter { it.enabled }
        if (activos.any { !it.isValid }) return false

        if (activos.isEmpty()) {
            existing.forEach { scheduleRepository.deleteSession(it.id) }
            return true
        }

        val now = System.currentTimeMillis()
        activos.forEachIndexed { indice, draft ->
            val previa = existing.getOrNull(indice)
            scheduleRepository.saveSession(
                ClassSession(
                    id = previa?.id ?: "class-${UUID.randomUUID()}",
                    subjectId = subjectId,
                    daysOfWeek = draft.daysOfWeek,
                    startMinute = draft.startMinute,
                    endMinute = draft.endMinute,
                    location = draft.location,
                    reminderMinutes = draft.reminderMinutes,
                    createdAt = previa?.createdAt ?: now,
                    updatedAt = now,
                    repeatEveryWeeks = draft.repeatEveryWeeks,
                    recurrenceStartEpochDay = draft.recurrenceStartEpochDay
                )
            )
        }
        existing.drop(activos.size).forEach { scheduleRepository.deleteSession(it.id) }
        return true
    }

    /**
     * ¿Ya hay una materia con este nombre en el periodo activo?
     *
     * La red de seguridad de quien no vio el botón de añadir franja y va a crear la materia
     * dos veces. Compara sin acentos ni mayúsculas, y se queda en el periodo activo a propósito:
     * repetir el nombre entre semestres es legítimo y para eso está `repeatedFromSubjectId`.
     */
    private fun sinTildes(texto: String): String =
        java.text.Normalizer.normalize(TextValidators.normalizeText(texto), java.text.Normalizer.Form.NFD)
            .replace(Regex("""\p{Mn}+"""), "")
            .lowercase(Locale.ROOT)

    fun subjectWithSameName(name: String, excludingId: String? = null): Subject? {
        val buscado = TextValidators.normalizeText(name).lowercase(Locale.ROOT)
        if (buscado.isBlank()) return null
        val termId = termRepository.activeTerm.value?.id
        return subjects.value.firstOrNull { otra ->
            otra.id != excludingId &&
                (termId == null || otra.termId == null || otra.termId == termId) &&
                sinTildes(otra.name) == buscado
        }
    }

    fun deleteSubject(subjectId: String): Boolean {
        val exists = subjects.value.any { it.id == subjectId }
        if (!exists) return false
        tasksRepository.tasks.value
            .filter { it.subjectId == subjectId }
            .forEach { task ->
                tasksRepository.updateTask(
                    task.copy(
                        subjectId = null,
                        cutId = null,
                        gradingStatus = if (task.linkedGradeId != null) {
                            TaskGradingStatus.NOT_GRADED
                        } else {
                            task.gradingStatus
                        },
                        linkedGradeId = null,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        classSessions.value
            .filter { it.subjectId == subjectId }
            .forEach { scheduleRepository.deleteSession(it.id) }
        repository.deleteSubject(subjectId)
        return true
    }

    fun addGrade(
        subjectId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        cutId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN,
        taskId: String? = null
    ): Boolean {
        return saveGrade(
            subjectId = subjectId,
            name = name,
            value = value,
            percentageInput = percentageInput,
            type = type,
            cutId = cutId,
            source = source,
            weightStatus = weightStatus,
            taskId = taskId
        ).saved
    }

    fun saveGrade(
        subjectId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        cutId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN,
        taskId: String? = null
    ): GradeSaveOutcome {
        val subject = subjects.value.firstOrNull { it.id == subjectId }
            ?: return GradeSaveOutcome(false)
        if (!TextValidators.validateActivityName(name).isValid) return GradeSaveOutcome(false)
        val percentage = when {
            source == GradeSource.PERIOD_FINAL -> 1.0
            weightStatus == GradeWeightStatus.UNKNOWN -> 0.0
            else -> percentageInput / 100.0
        }
        val existingKnownWeight = subject.grades
            .filter { it.cutId == cutId }
            .filter { it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.KNOWN }
            .sumOf { it.percentage }
        val total = existingKnownWeight +
            if (source == GradeSource.ACTIVITY && weightStatus == GradeWeightStatus.KNOWN) percentage else 0.0
        val maxGrade = getMaxGrade()
        val validWeight = source == GradeSource.PERIOD_FINAL ||
            weightStatus == GradeWeightStatus.UNKNOWN ||
            percentage > 0.0
        if (value !in 0.0..maxGrade || !validWeight || total > 1.00001) {
            return GradeSaveOutcome(false)
        }

        if (source == GradeSource.PERIOD_FINAL) {
            subject.grades
                .filter { it.cutId == cutId && it.source == GradeSource.PERIOD_FINAL }
                .forEach { repository.deleteGrade(subjectId, it.id) }
        }

        val newGrade = GradeItem(
            id = "grade-${UUID.randomUUID()}",
            name = TextValidators.normalizeText(name),
            value = value,
            percentage = percentage,
            type = type,
            cutId = cutId,
            source = source,
            weightStatus = weightStatus,
            taskId = taskId,
            recordedAt = System.currentTimeMillis()
        )
        repository.addGrade(subjectId = subjectId, grade = newGrade)
        val shouldSuggestHistory = subject.grades.isEmpty() &&
            subject.historyPromptStatus == PriorHistoryPromptStatus.NOT_SHOWN &&
            subject.cutScheme.cuts.firstOrNull { it.id == cutId }?.order?.let { it > 1 } == true
        // Se calcula sobre la lista de aquí y no sobre el flujo del repositorio: con Room la
        // consulta puede no haber emitido todavía cuando volvemos de addGrade.
        val nextActive = cutAfterSaving(subject, subject.grades + newGrade, cutId)
        if (nextActive != subject.activeCutId) {
            repository.updateSubject(subject.copy(activeCutId = nextActive))
        }
        return GradeSaveOutcome(saved = true, suggestPriorHistory = shouldSuggestHistory)
    }


    /**
     * A qué corte pasa la materia después de guardar una nota.
     *
     * Al corte donde entró la nota, salvo que con ella quede repartido al 100%: entonces salta
     * al siguiente que aún tenga hueco. Un corte cerrado ya no admite más reparto, así que
     * dejarlo como destino de las notas nuevas obligaba a cambiarlo a mano cada vez.
     *
     * Solo hacia delante. Si por detrás quedó un corte a medias, saltar hacia atrás sería otra
     * suposición sobre en qué punto del semestre va el usuario, y eso lo decide él.
     */
    private fun cutAfterSaving(
        subject: Subject,
        gradesAfterSaving: List<GradeItem>,
        savedCutId: String
    ): String {
        val cuts = subject.cutScheme.cuts.sortedBy { it.order }
        val saved = cuts.firstOrNull { it.id == savedCutId } ?: return savedCutId
        fun isComplete(cut: GradingCut): Boolean =
            GradeCalculator.calculateCut(gradesAfterSaving.filter { it.cutId == cut.id }).isComplete

        if (!isComplete(saved)) return savedCutId
        return cuts.firstOrNull { it.order > saved.order && !isComplete(it) }?.id ?: savedCutId
    }

    fun updateGrade(
        subjectId: String,
        gradeId: String,
        name: String,
        value: Double,
        percentageInput: Double,
        type: GradeType = GradeType.WORKSHOP,
        cutId: String = "period-1",
        source: GradeSource = GradeSource.ACTIVITY,
        weightStatus: GradeWeightStatus = GradeWeightStatus.KNOWN
    ): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val existingGrade = subject.grades.firstOrNull { it.id == gradeId } ?: return false
        if (!TextValidators.validateActivityName(name).isValid) return false

        val percentage = when {
            source == GradeSource.PERIOD_FINAL -> 1.0
            weightStatus == GradeWeightStatus.UNKNOWN -> 0.0
            else -> percentageInput / 100.0
        }
        val existingKnownWeight = subject.grades
            .filterNot { it.id == gradeId }
            .filter { it.cutId == cutId }
            .filter { it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.KNOWN }
            .sumOf { it.percentage }
        val total = existingKnownWeight +
            if (source == GradeSource.ACTIVITY && weightStatus == GradeWeightStatus.KNOWN) percentage else 0.0
        val maxGrade = getMaxGrade()
        val validWeight = source == GradeSource.PERIOD_FINAL ||
            weightStatus == GradeWeightStatus.UNKNOWN ||
            percentage > 0.0
        if (value !in 0.0..maxGrade || !validWeight || total > 1.00001) return false

        if (source == GradeSource.PERIOD_FINAL) {
            subject.grades
                .filter {
                    it.id != gradeId &&
                        it.cutId == cutId &&
                        it.source == GradeSource.PERIOD_FINAL
                }
                .forEach { repository.deleteGrade(subjectId, it.id) }
        }

        repository.updateGrade(
            subjectId = subjectId,
            grade = existingGrade.copy(
                name = TextValidators.normalizeText(name),
                value = value,
                percentage = percentage,
                type = type,
                cutId = cutId,
                source = source,
                weightStatus = weightStatus,
                recordedAt = System.currentTimeMillis()
            )
        )
        existingGrade.taskId?.let { taskId ->
            tasksRepository.tasks.value.firstOrNull { it.id == taskId }?.let { task ->
                tasksRepository.updateTask(
                    task.copy(
                        cutId = cutId,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
        return true
    }

    fun deleteGrade(subjectId: String, gradeId: String): Boolean {
        val subject = subjects.value.firstOrNull { it.id == subjectId } ?: return false
        val grade = subject.grades.firstOrNull { it.id == gradeId } ?: return false
        repository.deleteGrade(subjectId, gradeId)
        val linkedTask = tasksRepository.tasks.value.firstOrNull {
            it.linkedGradeId == gradeId || it.id == grade.taskId
        }
        linkedTask?.let { task ->
            tasksRepository.updateTask(
                task.copy(
                    gradingStatus = TaskGradingStatus.AWAITING_GRADE,
                    linkedGradeId = null,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return true
    }

    fun subjectById(subjectId: String): Subject? {
        return subjects.value.firstOrNull { it.id == subjectId }
    }

    /**
     * Todo lo que hay que saber de una materia, en una sola cuenta.
     *
     * Cada pantalla sacaba sus cifras por su lado y acababan discrepando: el detalle de
     * materia llegó a enseñar dos proyecciones distintas en la misma tarjeta. Pasando por
     * aquí, promedio, suelo, techo y lo que falta para la meta salen todos de la misma
     * llamada y no pueden contradecirse.
     */
    fun calculationFor(subject: Subject): SubjectGradeCalculation {
        return GradeCalculator.calculateSubject(
            grades = subject.grades,
            cuts = subject.cutScheme.cuts,
            targetAverage = subject.targetAverage,
            maxGrade = getMaxGrade(),
            passingGrade = userProfile.value?.passingGrade
        )
    }

    fun currentAverage(subject: Subject): Double? = calculationFor(subject).currentAverage

    fun evaluatedPercentage(subject: Subject): Double =
        GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, subject.cutScheme.cuts)

    fun neededGrade(subject: Subject): Double? {
        if (subject.grades.isEmpty()) return null
        // El porcentaje restante no se recalcula a mano: la versión anterior lo sacaba
        // pasando por calculateEvaluatedSemesterPercentage, que redondea a un decimal y
        // multiplica por 100, para luego volver a dividir entre 100. Ese viaje de ida y
        // vuelta hacía que dos pantallas dieran cifras distintas para lo mismo.
        return calculationFor(subject).neededForTarget
    }

    fun setActiveCut(subjectId: String, cutId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        if (subject.cutScheme.cuts.none { it.id == cutId }) return false
        repository.updateSubject(subject.copy(activeCutId = cutId))
        return true
    }

    /**
     * Cierra un corte, o lo vuelve a abrir.
     *
     * Reabrir hace falta: si al cerrarlo te das cuenta de que una nota estaba mal, el camino
     * de vuelta tiene que existir y costar lo mismo que la ida.
     */
    fun setCutClosed(subjectId: String, cutId: String, closed: Boolean): Boolean {
        val subject = subjectById(subjectId) ?: return false
        if (subject.cutScheme.cuts.none { it.id == cutId }) return false
        val nuevos = if (closed) subject.closedCutIds + cutId else subject.closedCutIds - cutId
        if (nuevos == subject.closedCutIds) return true
        repository.updateSubject(subject.copy(closedCutIds = nuevos))
        return true
    }

    fun updateHistoryPromptStatus(
        subjectId: String,
        status: PriorHistoryPromptStatus
    ): Boolean {
        val subject = subjectById(subjectId) ?: return false
        repository.updateSubject(subject.copy(historyPromptStatus = status))
        return true
    }

    fun markCutUnknown(subjectId: String, cutId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        if (subject.cutScheme.cuts.none { it.id == cutId }) return false
        val unknown = subject.unknownCutIds + cutId
        val previousCuts = subject.previousCuts()
        val status = if (previousCuts.all { cut ->
                cut.id in unknown || subject.grades.any { it.cutId == cut.id }
            }
        ) PriorHistoryPromptStatus.COMPLETED else PriorHistoryPromptStatus.SNOOZED
        repository.updateSubject(
            subject.copy(
                unknownCutIds = unknown,
                historyPromptStatus = status
            )
        )
        return true
    }

    fun clearCutUnknown(subjectId: String, cutId: String): Boolean {
        val subject = subjectById(subjectId) ?: return false
        repository.updateSubject(subject.copy(unknownCutIds = subject.unknownCutIds - cutId))
        return true
    }

    fun refreshHistoryCompletion(subjectId: String) {
        val subject = subjectById(subjectId) ?: return
        val previousCuts = subject.previousCuts()
        if (previousCuts.isNotEmpty() && previousCuts.all { cut ->
                cut.id in subject.unknownCutIds || subject.grades.any { it.cutId == cut.id }
            }
        ) {
            repository.updateSubject(subject.copy(historyPromptStatus = PriorHistoryPromptStatus.COMPLETED))
        }
    }

    private fun Subject.previousCuts() = cutScheme.cuts
        .filter { cut ->
            val activeOrder = cutScheme.cuts.firstOrNull { it.id == activeCutId }?.order ?: 1
            cut.order < activeOrder
        }
}

data class GradeSaveOutcome(
    val saved: Boolean,
    val suggestPriorHistory: Boolean = false
)
