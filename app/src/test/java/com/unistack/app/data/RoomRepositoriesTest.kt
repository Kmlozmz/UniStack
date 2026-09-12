package com.unistack.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.feature_expenses.data.RoomExpensesRepository
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_grades.data.RoomGradesRepository
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_templates.data.RoomAcademicWorksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_tasks.data.RoomTasksRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.unistack.app.TextosDePrueba

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class RoomRepositoriesTest {
    private lateinit var database: UniStackDatabase
    private lateinit var userRepository: InMemoryUserRepository

    @Before
    fun setUp() {
        TextosDePrueba.instalar()
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UniStackDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        userRepository = InMemoryUserRepository().also { repository ->
            repository.saveUserProfile(testProfile())
        }
    }

    @After
    fun tearDown() {
        database.close()
    }

    /**
     * Los campos de la materia tienen que llegar a la base.
     *
     * `termId` existia como columna y el mapeador la leia, pero la consulta de actualizacion
     * no la escribia, asi que se perdia en cuanto el flujo volvia a leer. El tope de faltas
     * tenia el mismo fallo; despues resulto ser uno solo y se fue al perfil. Esta prueba pasa
     * por el mismo camino que la app —`updateSubject`— a proposito.
     */
    @Test
    fun elPeriodoDeLaMateriaSobreviveAGuardar() {
        runBlocking {
            val repository = RoomGradesRepository(
                subjectDao = database.subjectDao(),
                gradeDao = database.gradeDao(),
                userRepository = userRepository
            )
            val subject = Subject(
                id = "subject-tope",
                name = "Estadistica",
                targetAverage = 4.0,
                grades = emptyList(),
                visualType = SubjectVisualType.BLUE,
                termId = "term-1"
            )
            repository.addSubject(subject)
            repository.subjects.awaitValue { it.any { materia -> materia.id == subject.id } }

            repository.updateSubject(subject.copy(name = "Estadistica II"))

            val guardada = repository.subjects.awaitValue { lista ->
                lista.firstOrNull { it.id == subject.id }?.name == "Estadistica II"
            }.first { it.id == subject.id }
            assertEquals("term-1", guardada.termId)

            // Mover una materia de periodo tambien tiene que llegar.
            repository.updateSubject(guardada.copy(termId = "term-2"))
            val movida = repository.subjects.awaitValue { lista ->
                lista.firstOrNull { it.id == subject.id }?.termId == "term-2"
            }.first { it.id == subject.id }
            assertEquals("term-2", movida.termId)
        }
    }

    /**
     * Al cerrar un periodo, las materias sueltas se quedan con el.
     *
     * Son las de antes de que existieran los periodos. Sin estampar, el historico las perderia
     * justo en el momento en que empieza a haber historico.
     */
    @Test
    fun cerrarUnPeriodoEstampaLasMateriasQueNoLoTienen() {
        runBlocking {
            val repository = RoomGradesRepository(
                subjectDao = database.subjectDao(),
                gradeDao = database.gradeDao(),
                userRepository = userRepository
            )
            repository.addSubject(
                Subject(
                    id = "sin-periodo",
                    name = "Historia",
                    targetAverage = 4.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE
                )
            )
            repository.addSubject(
                Subject(
                    id = "con-periodo",
                    name = "Algebra",
                    targetAverage = 4.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE,
                    termId = "term-viejo"
                )
            )
            repository.subjects.awaitValue { it.size == 2 }

            repository.stampTerm("term-que-cierra")

            val despues = repository.subjects.awaitValue { lista ->
                lista.firstOrNull { it.id == "sin-periodo" }?.termId == "term-que-cierra"
            }
            assertEquals("term-que-cierra", despues.first { it.id == "sin-periodo" }.termId)
            // La que ya tenia periodo no se toca: pertenece a otro semestre.
            assertEquals("term-viejo", despues.first { it.id == "con-periodo" }.termId)
        }
    }

    /** Una materia nueva nace en el periodo que se esta cursando. */
    @Test
    fun laMateriaNuevaSeEstampaConElPeriodoActivo() {
        runBlocking {
            val repository = RoomGradesRepository(
                subjectDao = database.subjectDao(),
                gradeDao = database.gradeDao(),
                userRepository = userRepository,
                activeTermId = { "term-activo" }
            )
            repository.addSubject(
                Subject(
                    id = "subject-nueva",
                    name = "Calculo II",
                    targetAverage = 4.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE
                )
            )
            val creada = repository.subjects.awaitValue { lista ->
                lista.any { it.id == "subject-nueva" }
            }.first { it.id == "subject-nueva" }
            assertEquals("term-activo", creada.termId)
        }
    }

    /** Restaurar una copia no reescribe el periodo: ese dato ya venia decidido. */
    @Test
    fun elPeriodoQueYaTraeLaMateriaSeRespeta() {
        runBlocking {
            val repository = RoomGradesRepository(
                subjectDao = database.subjectDao(),
                gradeDao = database.gradeDao(),
                userRepository = userRepository,
                activeTermId = { "term-activo" }
            )
            repository.addSubject(
                Subject(
                    id = "subject-vieja",
                    name = "Fisica I",
                    targetAverage = 4.0,
                    grades = emptyList(),
                    visualType = SubjectVisualType.BLUE,
                    termId = "term-de-2025"
                )
            )
            val creada = repository.subjects.awaitValue { lista ->
                lista.any { it.id == "subject-vieja" }
            }.first { it.id == "subject-vieja" }
            assertEquals("term-de-2025", creada.termId)
        }
    }

    @Test
    fun gradesRepositoryPersistsSubjectsGradesAndCascadeDelete() {
        runBlocking {
        val repository = RoomGradesRepository(
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao(),
            userRepository = userRepository
        )
        val subject = Subject(
            id = "subject-1",
            name = "Fisica",
            targetAverage = 4.0,
            grades = emptyList(),
            visualType = SubjectVisualType.BLUE
        )
        val grade = GradeItem(
            id = "grade-1",
            name = "Parcial",
            value = 4.5,
            percentage = 0.4
        )

        repository.addSubject(subject)
        repository.addGrade(subject.id, grade)

        val withGrade = repository.subjects.awaitValue { subjects ->
            subjects.singleOrNull()?.grades?.singleOrNull()?.id == grade.id
        }.single()
        assertEquals("Fisica", withGrade.name)
        assertEquals(SubjectVisualType.BLUE, withGrade.visualType)
        assertEquals(4.5, withGrade.grades.single().value, 0.0)

        repository.updateSubject(subject.copy(name = "Fisica avanzada", targetAverage = 4.2))
        repository.updateGrade(subject.id, grade.copy(value = 4.8, percentage = 0.5))

        val updated = repository.subjects.awaitValue { subjects ->
            subjects.singleOrNull()?.name == "Fisica avanzada" &&
                subjects.single().grades.singleOrNull()?.value == 4.8
        }.single()
        assertEquals(4.2, updated.targetAverage, 0.0)
        assertEquals(0.5, updated.grades.single().percentage, 0.0)

        repository.deleteSubject(subject.id)

        repository.subjects.awaitValue { it.isEmpty() }
        val orphanGrades = database.gradeDao().observeAllGrades().first()
        assertTrue(orphanGrades.isEmpty())
        }
    }

    @Test
    fun tasksRepositoryPersistsUpdatesCompletionAndDelete() {
        runBlocking {
        val repository = RoomTasksRepository(
            taskDao = database.taskDao(),
            userRepository = userRepository
        )
        val task = StudentTask(
            id = "task-1",
            title = "Entrega ensayo",
            description = "",
            subjectId = null,
            type = TaskType.ESSAY,
            dueDateMillis = 1_800_000_000_000,
            difficulty = TaskDifficulty.MEDIUM,
            estimatedMinutes = 90,
            completed = false,
            createdAt = 10,
            updatedAt = 10
        )

        repository.addTask(task)
        assertEquals("Entrega ensayo", repository.tasks.awaitValue { it.size == 1 }.single().title)

        repository.updateTask(
            task.copy(
                title = "Entrega final",
                difficulty = TaskDifficulty.HARD,
                estimatedMinutes = 120,
                completed = true,
                completedAt = 20,
                gradingStatus = TaskGradingStatus.GRADED,
                linkedGradeId = "grade-1"
            )
        )
        val updated = repository.tasks.awaitValue { tasks ->
            tasks.singleOrNull()?.title == "Entrega final" &&
                tasks.single().difficulty == TaskDifficulty.HARD &&
                tasks.single().completed
        }.single()
        assertEquals(120, updated.estimatedMinutes)
        assertEquals(TaskGradingStatus.GRADED, updated.gradingStatus)
        assertEquals("grade-1", updated.linkedGradeId)
        assertEquals(20L, updated.completedAt)

        repository.setTaskCompleted(task.id, completed = false)
        assertTrue(repository.tasks.awaitValue { it.singleOrNull()?.completed == false }.single().completed.not())

        repository.deleteTask(task.id)
        repository.tasks.awaitValue { it.isEmpty() }
        }
    }

    @Test
    fun expensesRepositoryPersistsUpdatesAndDelete() {
        runBlocking {
        val repository = RoomExpensesRepository(
            expenseDao = database.expenseDao(),
            userRepository = userRepository
        )
        val expense = Expense(
            id = "expense-1",
            category = ExpenseCategory.FOOD,
            amount = 12_000,
            dateMillis = 1_800_000_000_000,
            createdAt = 10,
            updatedAt = 10
        )

        repository.addExpense(expense)
        assertEquals(12_000, repository.expenses.awaitValue { it.size == 1 }.single().amount)

        repository.updateExpense(expense.copy(category = ExpenseCategory.MATERIALS, amount = 25_000))
        val updated = repository.expenses.awaitValue { expenses ->
            expenses.singleOrNull()?.category == ExpenseCategory.MATERIALS
        }.single()
        assertEquals(25_000, updated.amount)

        repository.deleteExpense(expense.id)
        repository.expenses.awaitValue { it.isEmpty() }
        }
    }

    @Test
    fun academicWorksRepositoryPersistsChecklistStatusAndDelete() {
        runBlocking {
            val repository = RoomAcademicWorksRepository(
                academicWorkDao = database.academicWorkDao(),
                userRepository = userRepository
            )
            val work = AcademicWork(
                id = "work-1",
                templateId = "argumentative",
                title = "Ensayo final",
                subjectId = "subject-1",
                dueDateMillis = 1_800_000_000_000,
                status = AcademicWorkStatus.DRAFT,
                priority = AcademicWorkPriority.HIGH,
                completedChecklistIds = emptySet(),
                thesis = "Tesis",
                outline = "Esquema",
                sources = "Fuente",
                notes = "Notas",
                createdAt = 10,
                updatedAt = 10
            )

            repository.addWork(work)
            assertEquals("Ensayo final", repository.works.awaitValue { it.size == 1 }.single().title)

            repository.setChecklistItem(work.id, "topic", completed = true)
            assertTrue(repository.works.awaitValue { it.singleOrNull()?.completedChecklistIds?.contains("topic") == true }.single().completedChecklistIds.contains("topic"))

            repository.setStatus(work.id, AcademicWorkStatus.SUBMITTED)
            assertEquals(AcademicWorkStatus.SUBMITTED, repository.works.awaitValue { it.singleOrNull()?.status == AcademicWorkStatus.SUBMITTED }.single().status)

            repository.updateWork(work.copy(title = "Ensayo corregido", priority = AcademicWorkPriority.MEDIUM))
            val updated = repository.works.awaitValue { it.singleOrNull()?.title == "Ensayo corregido" }.single()
            assertEquals(AcademicWorkPriority.MEDIUM, updated.priority)

            repository.deleteWork(work.id)
            repository.works.awaitValue { it.isEmpty() }
        }
    }

    private suspend fun <T> StateFlow<T>.awaitValue(predicate: (T) -> Boolean): T {
        if (predicate(value)) return value
        return withTimeout(3_000) {
            filter(predicate).first()
        }
    }

    private fun testProfile(): UserProfile {
        val now = System.currentTimeMillis()
        return UserProfile(
            userId = UserIds.LOCAL,
            preferredName = "Tester",
            careerOrProgram = "Ingenieria",
            studyArea = null,
            gradingScale = GradingScale.ZERO_TO_FIVE,
            passingGrade = 3.0,
            targetAverage = 4.0,
            enabledModules = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES, AppModule.ACADEMIC_TEMPLATES),
            setupCompleted = true,
            createdAt = now,
            updatedAt = now
        )
    }
}
