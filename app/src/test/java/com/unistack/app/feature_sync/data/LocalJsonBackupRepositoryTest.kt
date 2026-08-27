package com.unistack.app.feature_sync.data

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.data.InMemoryGradesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class LocalJsonBackupRepositoryTest {
    @Test
    fun exportPreviewAndRestoreKeepsStableIds() {
        val userRepository = InMemoryUserRepository().also { it.saveUserProfile(testProfile()) }
        val gradesRepository = InMemoryGradesRepository().also { repository ->
            repository.addSubject(
                Subject(
                    id = "subject-1",
                    name = "Fisica",
                    targetAverage = 4.0,
                    visualType = SubjectVisualType.BLUE,
                    grades = emptyList(),
                    activePeriodId = "period-2",
                    historyPromptStatus = PriorHistoryPromptStatus.SNOOZED,
                    unknownPeriodIds = setOf("period-3")
                )
            )
            repository.addGrade(
                "subject-1",
                GradeItem(
                    id = "grade-1",
                    name = "Resultado Corte 1",
                    value = 4.5,
                    percentage = 1.0,
                    periodId = "period-1",
                    source = GradeSource.PERIOD_FINAL,
                    taskId = "task-1",
                    recordedAt = 1234
                )
            )
        }
        val tasksRepository = FakeTasksRepository()
        val expensesRepository = FakeExpensesRepository()
        val worksRepository = FakeAcademicWorksRepository()
        val scheduleRepository = FakeScheduleRepository()
        tasksRepository.addTask(testTask())
        expensesRepository.addExpense(testExpense())
        worksRepository.addWork(testWork())
        scheduleRepository.saveSession(testSession())

        val repository = LocalJsonBackupRepository(
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository,
            academicWorksRepository = worksRepository,
            scheduleRepository = scheduleRepository
        )

        val json = repository.exportBackupJson()
        val preview = repository.previewBackupJson(json).getOrThrow()
        repository.restoreBackupJson(json).getOrThrow()

        assertEquals(1, preview.subjects)
        assertEquals(1, preview.grades)
        assertTrue(json.contains("\"schemaVersion\""))
        assertEquals(1, gradesRepository.subjects.value.size)
        assertEquals(1, gradesRepository.subjects.value.single().grades.size)
        assertEquals("period-2", gradesRepository.subjects.value.single().activePeriodId)
        assertEquals(PriorHistoryPromptStatus.SNOOZED, gradesRepository.subjects.value.single().historyPromptStatus)
        assertEquals(setOf("period-3"), gradesRepository.subjects.value.single().unknownPeriodIds)
        assertEquals(GradeSource.PERIOD_FINAL, gradesRepository.subjects.value.single().grades.single().source)
        assertEquals("task-1", gradesRepository.subjects.value.single().grades.single().taskId)
        assertEquals(1, tasksRepository.tasks.value.size)
        assertEquals(TaskGradingStatus.GRADED, tasksRepository.tasks.value.single().gradingStatus)
        assertEquals("grade-1", tasksRepository.tasks.value.single().linkedGradeId)
        assertTrue(userRepository.userProfile.value?.quietHoursEnabled == true)
        assertEquals(22, userRepository.userProfile.value?.quietHoursStartHour)
        assertEquals(7, userRepository.userProfile.value?.quietHoursEndHour)
        assertEquals(1, expensesRepository.expenses.value.size)
        assertEquals(1, worksRepository.works.value.size)
        assertEquals(1, scheduleRepository.sessions.value.size)
    }

    @Test
    fun previewRejectsCorruptBackup() {
        val repository = LocalJsonBackupRepository(
            userRepository = InMemoryUserRepository(),
            gradesRepository = InMemoryGradesRepository(),
            tasksRepository = FakeTasksRepository(),
            expensesRepository = FakeExpensesRepository(),
            academicWorksRepository = FakeAcademicWorksRepository(),
            scheduleRepository = FakeScheduleRepository()
        )

        assertTrue(repository.previewBackupJson("{bad json").isFailure)
    }

    private fun testProfile(): UserProfile {
        return UserProfile(
            userId = UserIds.LOCAL,
            preferredName = "Tester",
            careerOrProgram = "Ingenieria",
            studyArea = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES),
            quietHoursEnabled = true,
            quietHoursStartHour = 22,
            quietHoursEndHour = 7,
            setupCompleted = true,
            createdAt = 10,
            updatedAt = 10
        )
    }

    private fun testTask() = StudentTask(
        id = "task-1",
        title = "Entrega",
        description = "",
        subjectId = "subject-1",
        type = TaskType.WORKSHOP,
        dueDateMillis = 1_800_000_000_000,
        difficulty = TaskDifficulty.MEDIUM,
        estimatedMinutes = 60,
        completed = true,
        createdAt = 10,
        updatedAt = 10,
        periodId = "period-1",
        gradingStatus = TaskGradingStatus.GRADED,
        linkedGradeId = "grade-1",
        completedAt = 11
    )

    private fun testExpense() = Expense(
        id = "expense-1",
        category = ExpenseCategory.FOOD,
        amount = 12_000,
        dateMillis = 1_800_000_000_000,
        createdAt = 10,
        updatedAt = 10
    )

    private fun testWork() = AcademicWork(
        id = "work-1",
        templateId = "argumentative",
        title = "Ensayo",
        subjectId = "subject-1",
        dueDateMillis = 1_800_000_000_000,
        status = AcademicWorkStatus.DRAFT,
        priority = AcademicWorkPriority.HIGH,
        completedChecklistIds = setOf("topic"),
        thesis = "Tesis",
        outline = "Esquema",
        sources = "Fuente",
        notes = "Notas",
        createdAt = 10,
        updatedAt = 10
    )

    private fun testSession() = ClassSession(
        id = "session-1",
        subjectId = "subject-1",
        daysOfWeek = setOf(1, 3),
        startMinute = 480,
        endMinute = 570,
        location = "Aula 204",
        reminderMinutes = 15,
        createdAt = 10,
        updatedAt = 10
    )
}

private class FakeTasksRepository : TasksRepository {
    private val state = MutableStateFlow<List<StudentTask>>(emptyList())
    override val tasks: StateFlow<List<StudentTask>> = state
    override fun addTask(task: StudentTask) {
        state.value = if (state.value.any { it.id == task.id }) state.value else state.value + task
    }
    override fun updateTask(task: StudentTask) {
        state.value = state.value.map { if (it.id == task.id) task else it }
    }
    override fun deleteTask(taskId: String) {
        state.value = state.value.filterNot { it.id == taskId }
    }
    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        state.value = state.value.map { if (it.id == taskId) it.copy(completed = completed) else it }
    }
}

private class FakeExpensesRepository : ExpensesRepository {
    private val state = MutableStateFlow<List<Expense>>(emptyList())
    override val expenses: StateFlow<List<Expense>> = state
    override fun addExpense(expense: Expense) {
        state.value = if (state.value.any { it.id == expense.id }) state.value else state.value + expense
    }
    override fun updateExpense(expense: Expense) {
        state.value = state.value.map { if (it.id == expense.id) expense else it }
    }
    override fun deleteExpense(expenseId: String) {
        state.value = state.value.filterNot { it.id == expenseId }
    }
}

private class FakeAcademicWorksRepository : AcademicWorksRepository {
    private val state = MutableStateFlow<List<AcademicWork>>(emptyList())
    override val works: StateFlow<List<AcademicWork>> = state
    override fun addWork(work: AcademicWork) {
        state.value = if (state.value.any { it.id == work.id }) state.value else state.value + work
    }
    override fun updateWork(work: AcademicWork) {
        state.value = state.value.map { if (it.id == work.id) work else it }
    }
    override fun deleteWork(workId: String) {
        state.value = state.value.filterNot { it.id == workId }
    }
    override fun setChecklistItem(workId: String, checklistItemId: String, completed: Boolean) = Unit
    override fun setStatus(workId: String, status: AcademicWorkStatus) {
        state.value = state.value.map { if (it.id == workId) it.copy(status = status) else it }
    }
}

private class FakeScheduleRepository : ScheduleRepository {
    private val state = MutableStateFlow<List<ClassSession>>(emptyList())
    override val sessions: StateFlow<List<ClassSession>> = state
    private val occurrenceState = MutableStateFlow<List<com.unistack.app.feature_schedule.domain.ClassOccurrence>>(emptyList())
    override val occurrences: StateFlow<List<com.unistack.app.feature_schedule.domain.ClassOccurrence>> = occurrenceState
    private val agendaState = MutableStateFlow<List<com.unistack.app.feature_schedule.domain.AgendaEvent>>(emptyList())
    override val agendaEvents: StateFlow<List<com.unistack.app.feature_schedule.domain.AgendaEvent>> = agendaState

    override fun saveSession(session: ClassSession) {
        state.value = state.value.filterNot { it.id == session.id } + session
    }

    override fun deleteSession(sessionId: String) {
        state.value = state.value.filterNot { it.id == sessionId }
    }

    override fun saveOccurrence(occurrence: com.unistack.app.feature_schedule.domain.ClassOccurrence) {
        occurrenceState.value = occurrenceState.value.filterNot { it.id == occurrence.id } + occurrence
    }

    override fun deleteOccurrence(occurrenceId: String) {
        occurrenceState.value = occurrenceState.value.filterNot { it.id == occurrenceId }
    }

    override fun saveAgendaEvent(event: com.unistack.app.feature_schedule.domain.AgendaEvent) {
        agendaState.value = agendaState.value.filterNot { it.id == event.id } + event
    }

    override fun deleteAgendaEvent(eventId: String) {
        agendaState.value = agendaState.value.filterNot { it.id == eventId }
    }
}
