package com.unistack.app.feature_tasks.presentation

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.unistack.app.TextosDePrueba
import com.unistack.app.core.MainDispatcherRule
import com.unistack.app.feature_grades.data.InMemoryGradesRepository
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_tasks.data.InMemoryTasksRepository
import com.unistack.app.feature_tasks.data.TaskAttachmentStore
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_user.data.InMemoryUserRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [34])
class TasksViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var tasksRepo: InMemoryTasksRepository
    private lateinit var gradesRepo: InMemoryGradesRepository
    private lateinit var userRepo: InMemoryUserRepository
    private lateinit var viewModel: TasksViewModel

    private val tomorrowInput: String = LocalDate.now().plusDays(1).toString()

    @Before
    fun setUp() {
        java.util.Locale.setDefault(java.util.Locale("es"))
        TextosDePrueba.instalar()
        tasksRepo = InMemoryTasksRepository()
        gradesRepo = InMemoryGradesRepository()
        userRepo = InMemoryUserRepository()
        viewModel = TasksViewModel(
            tasksRepository = tasksRepo,
            gradesRepository = gradesRepo,
            userRepository = userRepo,
            attachmentStore = TaskAttachmentStore(ApplicationProvider.getApplicationContext<Context>())
        )
    }

    @Test
    fun `addTask con inputs validos crea la tarea`() {
        val added = viewModel.addTask(
            title = "Resolver taller",
            description = "Ejercicios 1 al 10",
            subjectId = null,
            type = TaskType.WORKSHOP,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "60",
            difficulty = TaskDifficulty.MEDIUM
        )

        assertTrue(added)
        assertEquals(1, viewModel.tasks.value.size)
        assertEquals("Resolver taller", viewModel.tasks.value.first().title)
    }

    @Test
    fun `addTask con título vacío retorna false`() {
        val added = viewModel.addTask(
            title = "",
            description = "",
            subjectId = null,
            type = TaskType.WORKSHOP,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "60",
            difficulty = TaskDifficulty.EASY
        )

        assertFalse(added)
        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `addTask con fecha inválida retorna false`() {
        val added = viewModel.addTask(
            title = "Leer capítulo",
            description = "",
            subjectId = null,
            type = TaskType.READING,
            dueDateInput = "no-es-fecha",
            dueTimeInput = "",
            estimatedMinutesInput = "30",
            difficulty = TaskDifficulty.EASY
        )

        assertFalse(added)
    }

    @Test
    fun `addTask con estimatedMinutes fuera de rango retorna false`() {
        val added = viewModel.addTask(
            title = "Investigar",
            description = "",
            subjectId = null,
            type = TaskType.RESEARCH,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "0",
            difficulty = TaskDifficulty.HARD
        )

        assertFalse(added)
    }

    @Test
    fun `deleteTask retorna true y elimina la tarea`() {
        viewModel.addTask(
            title = "Ensayo final",
            description = "",
            subjectId = null,
            type = TaskType.ESSAY,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "120",
            difficulty = TaskDifficulty.HARD
        )
        val taskId = viewModel.tasks.value.first().id

        val deleted = viewModel.deleteTask(taskId)

        assertTrue(deleted)
        assertTrue(viewModel.tasks.value.isEmpty())
    }

    @Test
    fun `deleteTask retorna false para id inexistente`() {
        val deleted = viewModel.deleteTask("id-falso")

        assertFalse(deleted)
    }

    @Test
    fun `setTaskCompleted devuelve prompt cuando tarea tiene materia y estado UNDECIDED`() {
        val subject = com.unistack.app.feature_grades.domain.Subject(
            id = "sub-1",
            name = "Cálculo",
            targetAverage = 4.0,
            grades = emptyList(),
            visualType = SubjectVisualType.TEAL
        ).also { gradesRepo.addSubject(it) }

        viewModel.addTask(
            title = "Parcial 1",
            description = "",
            subjectId = subject.id,
            type = TaskType.EXAM,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "90",
            difficulty = TaskDifficulty.HARD,
            gradingStatus = TaskGradingStatus.UNDECIDED
        )
        val taskId = viewModel.tasks.value.first().id

        val prompt = viewModel.setTaskCompleted(taskId, completed = true)

        assertNotNull(prompt)
        assertEquals(taskId, prompt!!.task.id)
        assertTrue(prompt.task.completed)
    }

    @Test
    fun `setTaskCompleted sin materia no genera prompt`() {
        viewModel.addTask(
            title = "Leer artículo",
            description = "",
            subjectId = null,
            type = TaskType.READING,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "45",
            difficulty = TaskDifficulty.EASY
        )
        val taskId = viewModel.tasks.value.first().id

        val prompt = viewModel.setTaskCompleted(taskId, completed = true)

        assertNull(prompt)
        assertTrue(viewModel.tasks.value.first().completed)
    }

    @Test
    fun `duplicateTask crea copia con título modificado`() {
        viewModel.addTask(
            title = "Presentación oral",
            description = "",
            subjectId = null,
            type = TaskType.PRESENTATION,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "60",
            difficulty = TaskDifficulty.MEDIUM
        )
        val taskId = viewModel.tasks.value.first().id

        val duplicated = viewModel.duplicateTask(taskId)

        assertNotNull(duplicated)
        assertEquals(2, viewModel.tasks.value.size)
        val copy = viewModel.tasks.value.first { it.id != taskId }
        assertTrue(copy.title.contains("copia"))
        assertFalse(copy.completed)
    }

    @Test
    fun `subtasks operations work correctly`() {
        viewModel.addTask(
            title = "Ensayo final",
            description = "",
            subjectId = null,
            type = TaskType.ESSAY,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "120",
            difficulty = TaskDifficulty.HARD
        )
        val taskId = viewModel.tasks.value.first().id

        // Añadir subtareas
        assertTrue(viewModel.addSubtask(taskId, "Escribir introducción"))
        assertTrue(viewModel.addSubtask(taskId, "Desarrollar argumentos"))
        assertEquals(2, viewModel.tasks.value.first().subtasks.size)

        val firstSubId = viewModel.tasks.value.first().subtasks[0].id
        assertFalse(viewModel.tasks.value.first().subtasks[0].isCompleted)

        // Alternar subtarea
        viewModel.toggleSubtask(taskId, firstSubId)
        assertTrue(viewModel.tasks.value.first().subtasks[0].isCompleted)

        // Borrar subtarea
        assertTrue(viewModel.deleteSubtask(taskId, firstSubId))
        assertEquals(1, viewModel.tasks.value.first().subtasks.size)
        assertEquals("Desarrollar argumentos", viewModel.tasks.value.first().subtasks[0].title)
    }

    @Test
    fun `postponeTaskToTomorrow updates due date`() {
        viewModel.addTask(
            title = "Taller",
            description = "",
            subjectId = null,
            type = TaskType.WORKSHOP,
            dueDateInput = tomorrowInput,
            dueTimeInput = "14:00",
            estimatedMinutesInput = "60",
            difficulty = TaskDifficulty.MEDIUM
        )
        val taskId = viewModel.tasks.value.first().id
        val originalDue = viewModel.tasks.value.first().dueDateMillis

        val newDue = viewModel.postponeTaskToTomorrow(taskId)
        assertNotNull(newDue)
        assertTrue(newDue!! > originalDue)
    }

    @Test
    fun `markTaskAsDone sets completion and grading status`() {
        viewModel.addTask(
            title = "Examen",
            description = "",
            subjectId = null,
            type = TaskType.EXAM,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "90",
            difficulty = TaskDifficulty.HARD
        )
        val taskId = viewModel.tasks.value.first().id

        viewModel.markTaskAsDone(taskId, willBeGraded = true)
        val task = viewModel.tasks.value.first()
        assertTrue(task.completed)
        assertEquals(TaskGradingStatus.AWAITING_GRADE, task.gradingStatus)
    }

    @Test
    fun `completeTaskFromEditor con materia UNDECIDED marca AWAITING_GRADE`() {
        val subject = com.unistack.app.feature_grades.domain.Subject(
            id = "sub-2",
            name = "Física",
            targetAverage = 4.0,
            grades = emptyList(),
            visualType = SubjectVisualType.PURPLE
        )
        gradesRepo.addSubject(subject)

        viewModel.addTask(
            title = "Laboratorio",
            description = "",
            subjectId = subject.id,
            type = TaskType.PRACTICE,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "180",
            difficulty = TaskDifficulty.MEDIUM,
            gradingStatus = TaskGradingStatus.UNDECIDED
        )
        val taskId = viewModel.tasks.value.first().id

        val result = viewModel.completeTaskFromEditor(taskId)

        assertTrue(result)
        val task = viewModel.tasks.value.first { it.id == taskId }
        assertTrue(task.completed)
        assertEquals(TaskGradingStatus.AWAITING_GRADE, task.gradingStatus)
    }

    @Test
    fun `markTaskAsNotGraded cambia el estado y elimina la nota vinculada`() {
        viewModel.addTask(
            title = "Quiz",
            description = "",
            subjectId = null,
            type = TaskType.TEST,
            dueDateInput = tomorrowInput,
            dueTimeInput = "",
            estimatedMinutesInput = "30",
            difficulty = TaskDifficulty.EASY,
            gradingStatus = TaskGradingStatus.AWAITING_GRADE
        )
        val taskId = viewModel.tasks.value.first().id

        val result = viewModel.markTaskAsNotGraded(taskId)

        assertTrue(result)
        val task = viewModel.tasks.value.first { it.id == taskId }
        assertEquals(TaskGradingStatus.NOT_GRADED, task.gradingStatus)
        assertNull(task.linkedGradeId)
    }
}
