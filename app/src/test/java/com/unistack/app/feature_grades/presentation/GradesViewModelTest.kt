package com.unistack.app.feature_grades.presentation

import android.app.Activity
import com.unistack.app.core.MainDispatcherRule
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_billing.domain.BillingState
import com.unistack.app.feature_grades.data.InMemoryGradesRepository
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.data.InMemoryTasksRepository
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.data.InMemoryUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GradesViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var gradesRepo: InMemoryGradesRepository
    private lateinit var userRepo: InMemoryUserRepository
    private lateinit var tasksRepo: InMemoryTasksRepository
    private lateinit var scheduleRepo: FakeScheduleRepository
    private lateinit var billingRepo: FakeBillingRepository
    private lateinit var worksRepo: FakeAcademicWorksRepository
    private lateinit var viewModel: GradesViewModel

    @Before
    fun setUp() {
        gradesRepo = InMemoryGradesRepository()
        userRepo = InMemoryUserRepository()
        tasksRepo = InMemoryTasksRepository()
        scheduleRepo = FakeScheduleRepository()
        billingRepo = FakeBillingRepository()
        worksRepo = FakeAcademicWorksRepository()
        viewModel = GradesViewModel(
            repository = gradesRepo,
            userRepository = userRepo,
            tasksRepository = tasksRepo,
            scheduleRepository = scheduleRepo,
            billingRepository = billingRepo,
            academicWorksRepository = worksRepo
        )
    }

    @Test
    fun `addSubject con nombre valido crea la materia`() {
        val subject = viewModel.addSubject(
            name = "Cálculo",
            targetAverage = 4.0,
            visualType = SubjectVisualType.TEAL
        )

        assertNotNull(subject)
        assertEquals("Cálculo", subject!!.name)
        assertEquals(1, viewModel.subjects.value.size)
    }

    @Test
    fun `addSubject con nombre vacío retorna null`() {
        val subject = viewModel.addSubject(
            name = "",
            targetAverage = 4.0,
            visualType = SubjectVisualType.TEAL
        )

        assertNull(subject)
        assertTrue(viewModel.subjects.value.isEmpty())
    }

    @Test
    fun `addSubject con nombre de un carácter retorna null`() {
        val subject = viewModel.addSubject(
            name = "A",
            targetAverage = 4.0,
            visualType = SubjectVisualType.TEAL
        )

        assertNull(subject)
    }

    @Test
    fun `addSubject con targetAverage negativo retorna null`() {
        val subject = viewModel.addSubject(
            name = "Física",
            targetAverage = -1.0,
            visualType = SubjectVisualType.BLUE
        )

        assertNull(subject)
    }

    @Test
    fun `addGrade con datos válidos retorna true y guarda la nota`() {
        val subject = viewModel.addSubject("Álgebra", 4.0, SubjectVisualType.TEAL)!!

        val saved = viewModel.addGrade(
            subjectId = subject.id,
            name = "Parcial 1",
            value = 4.2,
            percentageInput = 30.0,
            periodId = subject.activePeriodId
        )

        assertTrue(saved)
        val updated = viewModel.subjects.value.first { it.id == subject.id }
        assertEquals(1, updated.grades.size)
        assertEquals(4.2, updated.grades.first().value, 0.001)
    }

    @Test
    fun `addGrade con valor fuera del rango retorna false`() {
        val subject = viewModel.addSubject("Química", 4.0, SubjectVisualType.CORAL)!!

        val saved = viewModel.addGrade(
            subjectId = subject.id,
            name = "Examen",
            value = 6.0,
            percentageInput = 30.0,
            periodId = subject.activePeriodId
        )

        assertFalse(saved)
        val updated = viewModel.subjects.value.first { it.id == subject.id }
        assertTrue(updated.grades.isEmpty())
    }

    @Test
    fun `addGrade no permite exceder 100 porciento de peso acumulado`() {
        val subject = viewModel.addSubject("Derecho", 4.0, SubjectVisualType.TEAL)!!

        viewModel.addGrade(
            subjectId = subject.id,
            name = "Parcial 1",
            value = 3.5,
            percentageInput = 70.0,
            periodId = subject.activePeriodId
        )
        val secondSaved = viewModel.addGrade(
            subjectId = subject.id,
            name = "Parcial 2",
            value = 4.0,
            percentageInput = 50.0,
            periodId = subject.activePeriodId
        )

        assertFalse(secondSaved)
        val updated = viewModel.subjects.value.first { it.id == subject.id }
        assertEquals(1, updated.grades.size)
    }

    @Test
    fun `deleteSubject elimina la materia y desvincula tareas relacionadas`() {
        val subject = viewModel.addSubject("Historia", 3.5, SubjectVisualType.TEAL)!!
        val now = System.currentTimeMillis()
        tasksRepo.addTask(
            com.unistack.app.feature_tasks.domain.StudentTask(
                id = "task-1",
                title = "Ensayo",
                description = "",
                subjectId = subject.id,
                type = TaskType.ESSAY,
                dueDateMillis = now + 86_400_000,
                difficulty = TaskDifficulty.MEDIUM,
                estimatedMinutes = 90,
                completed = false,
                createdAt = now,
                updatedAt = now,
                gradingStatus = TaskGradingStatus.UNDECIDED
            )
        )

        val deleted = viewModel.deleteSubject(subject.id)

        assertTrue(deleted)
        assertTrue(viewModel.subjects.value.none { it.id == subject.id })
        val task = tasksRepo.tasks.value.first { it.id == "task-1" }
        assertNull(task.subjectId)
    }

    @Test
    fun `deleteSubject retorna false para id inexistente`() {
        val deleted = viewModel.deleteSubject("no-existe")

        assertFalse(deleted)
    }

    @Test
    fun `updateSubject actualiza el nombre correctamente`() {
        val subject = viewModel.addSubject("Matemáticas", 4.0, SubjectVisualType.TEAL)!!

        val updated = viewModel.updateSubject(
            subjectId = subject.id,
            name = "Matemáticas Avanzadas",
            targetAverage = 4.5,
            visualType = SubjectVisualType.PURPLE
        )

        assertTrue(updated)
        val stored = viewModel.subjects.value.first { it.id == subject.id }
        assertEquals("Matemáticas Avanzadas", stored.name)
        assertEquals(4.5, stored.targetAverage, 0.001)
    }

    @Test
    fun `setActivePeriod cambia el período activo`() {
        val subject = viewModel.addSubject("Inglés", 4.0, SubjectVisualType.TEAL)!!
        val secondPeriod = subject.periodScheme.periods.getOrNull(1)

        if (secondPeriod != null) {
            val result = viewModel.setActivePeriod(subject.id, secondPeriod.id)
            assertTrue(result)
            val stored = viewModel.subjects.value.first { it.id == subject.id }
            assertEquals(secondPeriod.id, stored.activePeriodId)
        }
    }

    @Test
    fun `saveGrade sugiere historial cuando se agrega nota en período no inicial`() {
        val subject = viewModel.addSubject("Economía", 4.0, SubjectVisualType.TEAL)!!
        val periods = subject.periodScheme.periods
        if (periods.size < 2) return

        // Set active period to the last period so history suggestion triggers
        val laterPeriod = periods.maxByOrNull { it.order }!!
        viewModel.setActivePeriod(subject.id, laterPeriod.id)

        // Save grade in second period (order=2 > 1) while grades are still empty
        val secondPeriod = periods.sortedBy { it.order }[1]
        val outcome = viewModel.saveGrade(
            subjectId = subject.id,
            name = "Parcial histórico",
            value = 3.5,
            percentageInput = 100.0,
            periodId = secondPeriod.id,
            source = GradeSource.ACTIVITY,
            weightStatus = GradeWeightStatus.KNOWN
        )

        assertTrue(outcome.saved)
        assertTrue(outcome.suggestPriorHistory)
    }

    @Test
    fun `updateHistoryPromptStatus actualiza el estado del banner`() {
        val subject = viewModel.addSubject("Biología", 4.0, SubjectVisualType.TEAL)!!

        val result = viewModel.updateHistoryPromptStatus(subject.id, PriorHistoryPromptStatus.SNOOZED)

        assertTrue(result)
        val stored = viewModel.subjects.value.first { it.id == subject.id }
        assertEquals(PriorHistoryPromptStatus.SNOOZED, stored.historyPromptStatus)
    }
}

private class FakeScheduleRepository : ScheduleRepository {
    private val _sessions = MutableStateFlow<List<ClassSession>>(emptyList())
    private val _occurrences = MutableStateFlow<List<ClassOccurrence>>(emptyList())
    private val _agendaEvents = MutableStateFlow<List<AgendaEvent>>(emptyList())

    override val sessions: StateFlow<List<ClassSession>> = _sessions.asStateFlow()
    override val occurrences: StateFlow<List<ClassOccurrence>> = _occurrences.asStateFlow()
    override val agendaEvents: StateFlow<List<AgendaEvent>> = _agendaEvents.asStateFlow()

    override fun saveSession(session: ClassSession) {
        _sessions.value = _sessions.value.filterNot { it.id == session.id } + session
    }

    override fun deleteSession(sessionId: String) {
        _sessions.value = _sessions.value.filterNot { it.id == sessionId }
    }

    override fun saveOccurrence(occurrence: ClassOccurrence) {
        _occurrences.value = _occurrences.value.filterNot { it.id == occurrence.id } + occurrence
    }

    override fun deleteOccurrence(occurrenceId: String) {
        _occurrences.value = _occurrences.value.filterNot { it.id == occurrenceId }
    }

    override fun saveAgendaEvent(event: AgendaEvent) {
        _agendaEvents.value = _agendaEvents.value.filterNot { it.id == event.id } + event
    }

    override fun deleteAgendaEvent(eventId: String) {
        _agendaEvents.value = _agendaEvents.value.filterNot { it.id == eventId }
    }
}

private class FakeBillingRepository : BillingRepository {
    private val _state = MutableStateFlow(BillingState(isLoading = false, isBillingAvailable = false))
    override val state: StateFlow<BillingState> = _state.asStateFlow()

    override fun start() {}
    override fun refreshPurchases() {}
    override fun launchPurchase(activity: Activity, productId: String) {}
    override fun end() {}
}

private class FakeAcademicWorksRepository : AcademicWorksRepository {
    private val _works = MutableStateFlow<List<AcademicWork>>(emptyList())
    override val works: StateFlow<List<AcademicWork>> = _works.asStateFlow()

    override fun addWork(work: AcademicWork) {
        _works.value = _works.value + work
    }

    override fun updateWork(work: AcademicWork) {
        _works.value = _works.value.map { if (it.id == work.id) work else it }
    }

    override fun deleteWork(workId: String) {
        _works.value = _works.value.filterNot { it.id == workId }
    }

    override fun setChecklistItem(workId: String, checklistItemId: String, completed: Boolean) {}

    override fun setStatus(workId: String, status: AcademicWorkStatus) {
        _works.value = _works.value.map { work ->
            if (work.id == workId) work.copy(status = status) else work
        }
    }
}
