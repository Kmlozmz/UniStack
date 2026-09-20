package com.unistack.app.feature_home.domain

import com.unistack.app.TextosDePrueba
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.VisualPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeSummaryFactoryTest {

    @Before
    fun instalarTextos() {
        TextosDePrueba.instalar()
    }

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

    @Test
    fun weekDeliveriesKeepsOnlyTheNextSevenDaysWithoutClasses() {
        val hoy = TaskDateUtils.today()
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = listOf(subject(id = "calc", name = "Cálculo")),
                tasks = listOf(
                    task(id = "ayer", dueDateMillis = TaskDateUtils.toMillis(hoy.minusDays(1))),
                    task(id = "hoy", dueDateMillis = TaskDateUtils.toMillis(hoy)),
                    task(id = "en-7", dueDateMillis = TaskDateUtils.toMillis(hoy.plusDays(7))),
                    task(id = "en-8", dueDateMillis = TaskDateUtils.toMillis(hoy.plusDays(8)))
                ),
                expenses = emptyList(),
                works = listOf(
                    academicWork(
                        id = "ensayo",
                        title = "Ensayo",
                        dueDateMillis = TaskDateUtils.toMillis(hoy.plusDays(3)),
                        status = AcademicWorkStatus.DRAFT
                    )
                ),
                classSessions = listOf(
                    ClassSession(
                        id = "s1", subjectId = "calc", daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
                        startMinute = 8 * 60, endMinute = 10 * 60, createdAt = 1L, updatedAt = 1L
                    )
                )
            ),
            profile = profile(),
            user = appUser()
        )

        // Hoy, el ensayo a tres días y la de dentro de siete; ni la vencida ni la de ocho, y
        // ninguna clase aunque el horario tenga una diaria.
        assertEquals(listOf("Resolver taller", "Ensayo", "Resolver taller"), summary.weekDeliveries.map { it.title })
        assertTrue(summary.weekDeliveries.none { it.kind == HomeTimelineKind.CLASS })
    }

    @Test
    fun attendanceLinesCountAbsencesPerSubjectAndPutTheTightestFirst() {
        val sesionCalc = ClassSession(
            id = "s-calc", subjectId = "calc", daysOfWeek = setOf(1),
            startMinute = 8 * 60, endMinute = 10 * 60, createdAt = 1L, updatedAt = 1L
        )
        val sesionFis = ClassSession(
            id = "s-fis", subjectId = "fis", daysOfWeek = setOf(2),
            startMinute = 8 * 60, endMinute = 10 * 60, createdAt = 1L, updatedAt = 1L
        )
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = listOf(
                    subject(id = "calc", name = "Cálculo"),
                    subject(id = "fis", name = "Física"),
                    subject(id = "sin-horario", name = "Ética")
                ),
                tasks = emptyList(),
                expenses = emptyList(),
                works = emptyList(),
                classSessions = listOf(sesionCalc, sesionFis),
                occurrences = listOf(
                    ClassOccurrence(id = "o1", sessionId = "s-calc", dateEpochDay = 1L, status = ClassAttendanceStatus.ABSENT, updatedAt = 1L),
                    ClassOccurrence(id = "o2", sessionId = "s-fis", dateEpochDay = 2L, status = ClassAttendanceStatus.ABSENT, updatedAt = 1L),
                    ClassOccurrence(id = "o3", sessionId = "s-fis", dateEpochDay = 3L, status = ClassAttendanceStatus.ABSENT, updatedAt = 1L),
                    ClassOccurrence(id = "o4", sessionId = "s-fis", dateEpochDay = 4L, status = ClassAttendanceStatus.ATTENDED, updatedAt = 1L)
                )
            ),
            profile = profile().copy(absenceLimit = 3),
            user = appUser()
        )

        // Ética no tiene horario y no sale; Física va primero porque le queda una sola falta.
        assertEquals(listOf("Física", "Cálculo"), summary.attendanceLines.map { it.subjectName })
        assertEquals(listOf(2, 1), summary.attendanceLines.map { it.absent })
        assertEquals(listOf(1, 2), summary.attendanceLines.map { it.remaining })
    }

    @Test
    fun pinnedNotesTakeTheirTitleFromTheBodyWhenTheyHaveNone() {
        val summary = HomeSummaryFactory.create(
            content = HomeContent(
                subjects = emptyList(),
                tasks = emptyList(),
                expenses = emptyList(),
                works = emptyList(),
                notes = listOf(
                    QuickNote(id = "n1", body = "Llevar el informe\nY el cable", pinned = true, createdAt = 1L, updatedAt = 1L),
                    QuickNote(id = "n2", title = "Con título", body = "Cuerpo", pinned = true, createdAt = 2L, updatedAt = 2L),
                    QuickNote(id = "n3", body = "Sin fijar", pinned = false, createdAt = 3L, updatedAt = 3L)
                )
            ),
            profile = profile(),
            user = appUser()
        )

        assertEquals(setOf("Llevar el informe", "Con título"), summary.pinnedNotes.map { it.title }.toSet())
        assertEquals("Y el cable", summary.pinnedNotes.first { it.id == "n1" }.preview)
        assertEquals("Cuerpo", summary.pinnedNotes.first { it.id == "n2" }.preview)
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
            careerOrProgram = "Ingeniería",
            studyArea = StudyArea.ENGINEERING_TECHNOLOGY,
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
