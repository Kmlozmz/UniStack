package com.unistack.app.feature_home.presentation

import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.VisualPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeSummaryFactoryTest {

    @Test
    fun createUsesLinkedUserNameWhenProfileHasNoPreferredName() {
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = emptyList(),
                tasks = emptyList(),
                expenses = emptyList(),
                works = emptyList()
            ),
            profile = null,
            user = appUser(displayName = "Laura")
        )

        assertEquals("Laura", summary.userName)
        assertEquals("Crea tus materias para ver un tablero real del semestre.", summary.dashboardMessage)
        assertNull(summary.generalAverage)
    }

    @Test
    fun createPrioritizesOverdueTasksAndSubjectRisk() {
        val overdueTask = task(
            id = "task-1",
            dueDateMillis = TaskDateUtils.toMillis(TaskDateUtils.today().minusDays(1))
        )
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = listOf(
                    subject(
                        id = "math",
                        name = "Matemáticas",
                        targetAverage = 4.0,
                        grades = listOf(grade(value = 2.5, percentage = 1.0))
                    )
                ),
                tasks = listOf(overdueTask),
                expenses = emptyList(),
                works = emptyList()
            ),
            profile = profile(),
            user = appUser()
        )

        assertEquals(1, summary.overdueTasks)
        assertTrue(summary.dashboardMessage.contains("vencida"))
        assertEquals(HomePriorityAction.TASKS, summary.priority.action)
        assertTrue(summary.priority.title.contains("vencida"))
        assertEquals(HomePriorityAction.TASKS, summary.dailyFocusItems.first().action)
        assertEquals("Ahora", summary.dailyFocusItems.first().slotLabel)
        assertEquals("Matemáticas", summary.riskSubject?.subjectName)
        assertEquals("Promedio bajo la nota mínima: 2.5.", summary.riskSubject?.detail)
    }

    @Test
    fun createSurfacesExpensePriorityWhenWeeklyBudgetThresholdIsReached() {
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = listOf(subject(id = "history", name = "Historia")),
                tasks = emptyList(),
                expenses = listOf(expense(amount = 8_000)),
                works = emptyList()
            ),
            profile = profile().copy(
                weeklyBudget = 10_000,
                expenseAlertThresholdPercent = 70
            ),
            user = appUser()
        )

        assertEquals(HomePriorityAction.EXPENSES, summary.priority.action)
        assertEquals("Gastos cerca del límite", summary.priority.title)
    }

    @Test
    fun createSelectsNextAcademicWorkIgnoringSubmittedItems() {
        val submittedSoon = academicWork(
            id = "submitted",
            title = "Entregado",
            dueDateMillis = TaskDateUtils.toMillis(TaskDateUtils.today()),
            status = AcademicWorkStatus.SUBMITTED
        )
        val activeLater = academicWork(
            id = "active",
            title = "Ensayo",
            dueDateMillis = TaskDateUtils.toMillis(TaskDateUtils.today().plusDays(2)),
            status = AcademicWorkStatus.DRAFT
        )

        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = listOf(subject(id = "history", name = "Historia")),
                tasks = emptyList(),
                expenses = listOf(expense(amount = 12_000)),
                works = listOf(submittedSoon, activeLater)
            ),
            profile = profile(),
            user = appUser()
        )

        assertEquals("active", summary.nextAcademicWork?.id)
        assertEquals("Ensayo", summary.nextAcademicWork?.title)
        assertEquals(12_000, summary.weeklyExpenseTotal)
    }

    private fun appUser(displayName: String? = "Estudiante UniStack"): AppUser {
        return AppUser(
            userId = "local-user",
            displayName = displayName,
            email = null,
            photoUrl = null,
            authProvider = AuthProvider.LOCAL,
            providerUserId = null,
            syncStatus = SyncStatus.LOCAL_ONLY
        )
    }

    private fun profile(): UserProfile {
        return UserProfile(
            userId = "local-user",
            preferredName = "",
            educationLevel = EducationLevel.UNIVERSITY,
            careerOrProgram = "Ingeniería",
            studyArea = StudyArea.ENGINEERING_TECHNOLOGY,
            gradeLevel = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES),
            visualPreference = VisualPreference.SYSTEM,
            setupCompleted = true,
            createdAt = 1L,
            updatedAt = 1L
        )
    }

    private fun subject(
        id: String,
        name: String,
        targetAverage: Double = 4.0,
        grades: List<GradeItem> = emptyList()
    ): Subject {
        return Subject(
            id = id,
            name = name,
            targetAverage = targetAverage,
            grades = grades
        )
    }

    private fun grade(value: Double, percentage: Double): GradeItem {
        return GradeItem(
            id = "grade-$value-$percentage",
            name = "Parcial",
            value = value,
            percentage = percentage
        )
    }

    private fun task(id: String, dueDateMillis: Long): StudentTask {
        return StudentTask(
            id = id,
            title = "Resolver taller",
            description = "",
            subjectId = null,
            type = TaskType.WORKSHOP,
            dueDateMillis = dueDateMillis,
            difficulty = TaskDifficulty.MEDIUM,
            estimatedMinutes = 60,
            completed = false,
            createdAt = 1L,
            updatedAt = 1L
        )
    }

    private fun expense(amount: Int): Expense {
        return Expense(
            id = "expense-$amount",
            category = ExpenseCategory.FOOD,
            amount = amount,
            dateMillis = TaskDateUtils.toMillis(TaskDateUtils.today()),
            createdAt = 1L,
            updatedAt = 1L
        )
    }

    private fun academicWork(
        id: String,
        title: String,
        dueDateMillis: Long,
        status: AcademicWorkStatus
    ): AcademicWork {
        return AcademicWork(
            id = id,
            templateId = "essay",
            title = title,
            subjectId = null,
            dueDateMillis = dueDateMillis,
            status = status,
            priority = AcademicWorkPriority.MEDIUM,
            completedChecklistIds = emptySet(),
            thesis = "",
            outline = "",
            sources = "",
            notes = "",
            createdAt = 1L,
            updatedAt = 1L
        )
    }
}
