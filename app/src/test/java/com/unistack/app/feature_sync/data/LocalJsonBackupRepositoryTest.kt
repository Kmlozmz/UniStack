package com.unistack.app.feature_sync.data

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.data.InMemoryGradesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
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
                    grades = emptyList()
                )
            )
            repository.addGrade("subject-1", GradeItem(id = "grade-1", name = "Parcial", value = 4.5, percentage = 0.5))
        }
        val tasksRepository = FakeTasksRepository()
        val expensesRepository = FakeExpensesRepository()
        val worksRepository = FakeAcademicWorksRepository()
        tasksRepository.addTask(testTask())
        expensesRepository.addExpense(testExpense())
        worksRepository.addWork(testWork())

        val repository = LocalJsonBackupRepository(
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository,
            academicWorksRepository = worksRepository
        )

        val json = repository.exportBackupJson()
        val preview = repository.previewBackupJson(json).getOrThrow()
        repository.restoreBackupJson(json).getOrThrow()

        assertEquals(1, preview.subjects)
        assertEquals(1, preview.grades)
        assertTrue(json.contains("\"schemaVersion\""))
        assertEquals(1, gradesRepository.subjects.value.size)
        assertEquals(1, gradesRepository.subjects.value.single().grades.size)
        assertEquals(1, tasksRepository.tasks.value.size)
        assertEquals(1, expensesRepository.expenses.value.size)
        assertEquals(1, worksRepository.works.value.size)
    }

    @Test
    fun previewRejectsCorruptBackup() {
        val repository = LocalJsonBackupRepository(
            userRepository = InMemoryUserRepository(),
            gradesRepository = InMemoryGradesRepository(),
            tasksRepository = FakeTasksRepository(),
            expensesRepository = FakeExpensesRepository(),
            academicWorksRepository = FakeAcademicWorksRepository()
        )

        assertTrue(repository.previewBackupJson("{bad json").isFailure)
    }

    private fun testProfile(): UserProfile {
        return UserProfile(
            userId = UserIds.LOCAL,
            preferredName = "Tester",
            educationLevel = EducationLevel.UNIVERSITY,
            careerOrProgram = "Ingenieria",
            studyArea = null,
            gradeLevel = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES),
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
        completed = false,
        createdAt = 10,
        updatedAt = 10
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
