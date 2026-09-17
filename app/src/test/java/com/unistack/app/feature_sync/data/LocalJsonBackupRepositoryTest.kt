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
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskAttachment
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskSubtask
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.data.InMemoryUserRepository
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.ExpenseChartStyle
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.SavedGradeScenario
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
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
    fun exportPreviewAndRestoreKeepsStableIds() = runBlocking<Unit> {
        val telefono = Telefono()
        telefono.user.saveUserProfile(testProfile())
        telefono.grades.addSubject(
            Subject(
                id = "subject-1",
                name = "Fisica",
                targetAverage = 4.0,
                visualType = SubjectVisualType.BLUE,
                grades = emptyList(),
                activeCutId = "period-2",
                historyPromptStatus = PriorHistoryPromptStatus.SNOOZED,
                unknownCutIds = setOf("period-3")
            )
        )
        telefono.grades.addGrade(
            "subject-1",
            GradeItem(
                id = "grade-1",
                name = "Resultado Corte 1",
                value = 4.5,
                percentage = 1.0,
                cutId = "period-1",
                source = GradeSource.PERIOD_FINAL,
                taskId = "task-1",
                recordedAt = 1234
            )
        )
        telefono.notes.addNote(testNote())
        telefono.notes.addAttachment(testAttachment())
        telefono.tasks.addTask(testTask())
        telefono.expenses.addExpense(testExpense())
        telefono.works.addWork(testWork())
        telefono.schedule.saveSession(testSession())

        val json = telefono.backup.exportBackupJson()
        val preview = telefono.backup.previewBackupJson(json).getOrThrow()
        telefono.backup.restoreBackupJson(json).getOrThrow()

        assertEquals(1, preview.subjects)
        assertEquals(1, preview.grades)
        assertTrue(json.contains("\"schemaVersion\""))
        val subject = telefono.grades.subjects.value.single()
        assertEquals(1, subject.grades.size)
        assertEquals("period-2", subject.activeCutId)
        assertEquals(PriorHistoryPromptStatus.SNOOZED, subject.historyPromptStatus)
        assertEquals(setOf("period-3"), subject.unknownCutIds)
        assertEquals(GradeSource.PERIOD_FINAL, subject.grades.single().source)
        assertEquals("task-1", subject.grades.single().taskId)
        assertEquals(1, telefono.tasks.tasks.value.size)
        assertEquals(TaskGradingStatus.GRADED, telefono.tasks.tasks.value.single().gradingStatus)
        assertEquals("grade-1", telefono.tasks.tasks.value.single().linkedGradeId)
        assertTrue(telefono.user.userProfile.value?.quietHoursEnabled == true)
        assertEquals(22, telefono.user.userProfile.value?.quietHoursStartHour)
        assertEquals(7, telefono.user.userProfile.value?.quietHoursEndHour)
        assertEquals(1, telefono.expenses.expenses.value.size)
        assertEquals(1, telefono.works.works.value.size)
        assertEquals(1, telefono.schedule.sessions.value.size)
        /*
         * Los apuntes viajan en la copia, con su materia y su formato.
         *
         * Cuando eran una hoja en las preferencias del telefono no entraban: restaurar un
         * respaldo devolvia todo lo demas y dejaba las notas donde estaban, que en un telefono
         * nuevo es en ninguna parte.
         */
        assertEquals(1, preview.notes)
        val note = telefono.notes.notes.value.single()
        assertEquals("subject-1", note.subjectId)
        assertEquals(NoteFormat.MARKDOWN, note.format)
        assertTrue(note.pinned)
        assertEquals(NotesLayout.CUADERNO, telefono.user.userProfile.value?.notesLayout)
        /*
         * De los adjuntos, el JSON lleva la ficha; el archivo va aparte, en el zip.
         *
         * Lo que tiene que sobrevivir aquí es saber que la nota llevaba una foto, para que al
         * restaurar sin el archivo la nota lo diga en vez de que la fila desaparezca sin más.
         */
        val adjunto = telefono.notes.attachments.value.single()
        assertEquals("Pizarra.jpg", adjunto.displayName)
        assertEquals("note-1", adjunto.noteId)
        assertEquals(AttachmentKind.IMAGE, adjunto.kind)
    }

    /*
     * El caso para el que existe la copia: desinstalar, instalar de nuevo y restaurar.
     *
     * El teléfono nuevo ya pasó por el onboarding, así que tiene su propio perfil y su propio
     * periodo activo. Después de restaurar tiene que quedar lo de la copia y nada más.
     */
    @Test
    fun restoringOnANewPhoneBringsBackEverythingThatMatters() = runBlocking<Unit> {
        val gesto = MotionCatalog.gestures.first()
        val otraVariante = gesto.options.first { it.id != gesto.read(MotionPreferences.defaults()).id }
        val origen = Telefono()
        origen.user.saveUserProfile(
            testProfile().copy(
                careerOrProgram = "Ingenieria de sistemas",
                institutionName = "Universidad del Norte",
                currentSemester = 6,
                totalSemesters = 10,
                expenseChartStyle = ExpenseChartStyle.RING,
                gradeScenarios = listOf(
                    SavedGradeScenario("esc-1", "subject-1", "Fisica", "Para pasar", 3.0, 2.8, 5)
                ),
                appearancePreferences = AppearancePreferences.defaults().copy(
                    themeId = "oceano",
                    motion = gesto.write(MotionPreferences.defaults(), otraVariante)
                ),
                accessibilityPreferences = AccessibilityPreferences(
                    appLanguage = AppLanguage.ENGLISH,
                    boldText = true
                )
            )
        )
        origen.terms.update(term("term-1", "2026-1", start = LocalDate.of(2026, 1, 20), closed = LocalDate.of(2026, 6, 10)))
        origen.terms.update(term("term-2", "2026-2", start = LocalDate.of(2026, 7, 27)))
        origen.breaks.save("break-1", "Semana de receso", LocalDate.of(2026, 10, 5), LocalDate.of(2026, 10, 9))
        origen.grades.addSubject(
            Subject(
                id = "subject-1",
                name = "Fisica",
                targetAverage = 4.0,
                grades = emptyList(),
                termId = "term-2",
                closedCutIds = setOf("period-1")
            )
        )
        origen.tasks.addTask(
            testTask().copy(
                subtasks = listOf(
                    TaskSubtask("sub-1", "task-1", "Leer el capitulo", isCompleted = true, position = 0),
                    TaskSubtask("sub-2", "task-1", "Hacer el resumen", isCompleted = false, position = 1)
                )
            )
        )
        origen.tasks.addAttachment(
            TaskAttachment(
                id = "tatt-1",
                taskId = "task-1",
                kind = AttachmentKind.AUDIO,
                displayName = "Explicacion.m4a",
                storedName = "task-abc.m4a",
                mimeType = "audio/mp4",
                sizeBytes = 48_000,
                durationMillis = 5_000,
                createdAt = 5
            )
        )
        // Una foto sin una sola palabra: la nota más común, y la que restaurar se saltaba.
        origen.notes.addNote(QuickNote(id = "note-foto", body = "", createdAt = 1, updatedAt = 1))
        origen.notes.addAttachment(testAttachment().copy(id = "att-foto", noteId = "note-foto"))

        val json = origen.backup.exportBackupJson()

        val nuevo = Telefono()
        nuevo.user.saveUserProfile(testProfile())
        nuevo.terms.update(term("term-onboarding", "2026-2", start = LocalDate.of(2026, 8, 3)))

        val preview = nuevo.backup.restoreBackupJson(json).getOrThrow()

        assertEquals(2, preview.terms)
        assertEquals(AppLanguage.ENGLISH, preview.appLanguage)
        // El periodo del onboarding se va: dos activos a la vez no pueden existir.
        assertEquals(setOf("term-1", "term-2"), nuevo.terms.terms.value.map { it.id }.toSet())
        assertEquals("term-2", nuevo.terms.activeTerm.value?.id)
        assertEquals(LocalDate.of(2026, 6, 10), nuevo.terms.terms.value.first { it.id == "term-1" }.closedEpochDay?.let(LocalDate::ofEpochDay))
        assertEquals(listOf("break-1"), nuevo.breaks.breaks.value.map { it.id })
        assertEquals(LocalDate.of(2026, 10, 9), nuevo.breaks.breaks.value.single().end)

        val subject = nuevo.grades.subjects.value.single()
        assertEquals("term-2", subject.termId)
        assertEquals(setOf("period-1"), subject.closedCutIds)

        val task = nuevo.tasks.tasks.value.single()
        assertEquals(listOf("sub-1", "sub-2"), task.subtasks.map { it.id })
        assertTrue(task.subtasks.first().isCompleted)
        assertEquals("Explicacion.m4a", nuevo.tasks.attachments.value.single().displayName)
        assertEquals("task-1", nuevo.tasks.attachments.value.single().taskId)

        assertEquals("note-foto", nuevo.notes.notes.value.single().id)
        assertEquals("note-foto", nuevo.notes.attachments.value.single().noteId)

        val perfil = nuevo.user.userProfile.value!!
        assertEquals("Ingenieria de sistemas", perfil.careerOrProgram)
        assertEquals("Universidad del Norte", perfil.institutionName)
        assertEquals(6, perfil.currentSemester)
        assertEquals(10, perfil.totalSemesters)
        assertEquals(ExpenseChartStyle.RING, perfil.expenseChartStyle)
        assertEquals(listOf("esc-1"), perfil.gradeScenarios.map { it.id })
        assertEquals("oceano", perfil.appearancePreferences.themeId)
        assertEquals(otraVariante.id, gesto.read(perfil.appearancePreferences.motion).id)
        assertTrue(perfil.accessibilityPreferences.boldText)
        assertEquals(AppLanguage.ENGLISH, perfil.accessibilityPreferences.appLanguage)
    }

    /*
     * Restaurar reemplaza, como dice el diálogo antes de hacerlo.
     *
     * Antes mezclaba: lo que hubiera en el teléfono y no estuviera en la copia se quedaba, así
     * que el «Quedaría» del diálogo mentía en cuanto había algo.
     */
    @Test
    fun restoreReplacesInsteadOfMerging() = runBlocking<Unit> {
        val origen = Telefono()
        origen.user.saveUserProfile(testProfile())
        origen.expenses.addExpense(testExpense())
        val json = origen.backup.exportBackupJson()

        val telefono = Telefono()
        telefono.user.saveUserProfile(testProfile())
        telefono.expenses.addExpense(testExpense().copy(id = "expense-local"))
        telefono.grades.addSubject(Subject(id = "subject-local", name = "Local", targetAverage = 4.0, grades = emptyList()))
        telefono.notes.addNote(testNote().copy(id = "note-local"))
        telefono.tasks.addTask(testTask().copy(id = "task-local"))
        telefono.breaks.save("break-local", "Festivo", LocalDate.of(2026, 11, 2), LocalDate.of(2026, 11, 2))

        telefono.backup.restoreBackupJson(json).getOrThrow()

        assertEquals(listOf("expense-1"), telefono.expenses.expenses.value.map { it.id })
        assertTrue(telefono.grades.subjects.value.isEmpty())
        assertTrue(telefono.notes.notes.value.isEmpty())
        assertTrue(telefono.tasks.tasks.value.isEmpty())
        assertTrue(telefono.breaks.breaks.value.isEmpty())
    }

    /*
     * Una copia antigua no borra lo que su versión no sabía guardar.
     *
     * Que no traiga periodos no quiere decir que no los hubiera: quiere decir que entonces no
     * se guardaban. Lo mismo con las subtareas de una tarea que sí trae.
     */
    @Test
    fun anOldBackupLeavesAloneWhatItCouldNotSave() = runBlocking<Unit> {
        val telefono = Telefono()
        telefono.user.saveUserProfile(testProfile())
        telefono.terms.update(term("term-local", "2026-2", start = LocalDate.of(2026, 7, 27)))
        telefono.tasks.addTask(
            testTask().copy(subtasks = listOf(TaskSubtask("sub-1", "task-1", "Leer", isCompleted = false, position = 0)))
        )
        val antigua = JSONObject()
            .put("schemaVersion", 11)
            .put(
                "tasks",
                JSONArray().put(
                    JSONObject()
                        .put("id", "task-1")
                        .put("title", "Entrega")
                        .put("type", "WORKSHOP")
                        .put("dueDateMillis", 1_800_000_000_000)
                )
            )
            .toString()

        telefono.backup.restoreBackupJson(antigua).getOrThrow()

        assertEquals(listOf("term-local"), telefono.terms.terms.value.map { it.id })
        assertEquals(listOf("sub-1"), telefono.tasks.tasks.value.single().subtasks.map { it.id })
    }

    @Test
    fun previewRejectsCorruptBackup() {
        assertTrue(Telefono().backup.previewBackupJson("{bad json").isFailure)
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
            notesLayout = NotesLayout.CUADERNO,
            quietHoursEnabled = true,
            quietHoursStartHour = 22,
            quietHoursEndHour = 7,
            setupCompleted = true,
            createdAt = 10,
            updatedAt = 10
        )
    }

    private fun term(id: String, name: String, start: LocalDate, closed: LocalDate? = null) = AcademicTerm(
        id = id,
        userId = UserIds.LOCAL,
        name = name,
        type = AcademicTermType.SEMESTER,
        startEpochDay = start.toEpochDay(),
        plannedEndEpochDay = start.plusWeeks(16).toEpochDay(),
        closedEpochDay = closed?.toEpochDay(),
        status = if (closed == null) AcademicTermStatus.ACTIVE else AcademicTermStatus.CLOSED,
        createdAt = 1,
        updatedAt = 1,
        cutScheme = closed?.let { GradingCutScheme.default() }
    )

    private fun testNote() = QuickNote(
        id = "note-1",
        body = "Parcial 2 el martes, salon 302",
        subjectId = "subject-1",
        format = NoteFormat.MARKDOWN,
        pinned = true,
        createdAt = 20,
        updatedAt = 30
    )

    private fun testAttachment() = NoteAttachment(
        id = "att-1",
        noteId = "note-1",
        kind = AttachmentKind.IMAGE,
        displayName = "Pizarra.jpg",
        storedName = "note-abc.jpg",
        mimeType = "image/jpeg",
        sizeBytes = 350_000,
        durationMillis = null,
        createdAt = 25
    )

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
        cutId = "period-1",
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

/** Todos los almacenes de un teléfono, en memoria, con su copia de seguridad encima. */
private class Telefono {
    val user = InMemoryUserRepository()
    val grades = InMemoryGradesRepository()
    val tasks = FakeTasksRepository()
    val expenses = FakeExpensesRepository()
    val works = FakeAcademicWorksRepository()
    val schedule = FakeScheduleRepository()
    val notes = FakeNotesRepository()
    val terms = FakeTermRepository()
    val breaks = FakeBreakRepository()
    val backup = LocalJsonBackupRepository(
        userRepository = user,
        gradesRepository = grades,
        tasksRepository = tasks,
        expensesRepository = expenses,
        academicWorksRepository = works,
        scheduleRepository = schedule,
        notesRepository = notes,
        termRepository = terms,
        breakRepository = breaks
    )
}

private class FakeTermRepository : AcademicTermRepository {
    private val state = MutableStateFlow<List<AcademicTerm>>(emptyList())
    private val activo = MutableStateFlow<AcademicTerm?>(null)
    override val terms: StateFlow<List<AcademicTerm>> = state
    override val activeTerm: StateFlow<AcademicTerm?> = activo

    private fun publicar(lista: List<AcademicTerm>) {
        state.value = lista.sortedByDescending { it.startEpochDay }
        activo.value = state.value.firstOrNull { it.isActive }
    }

    override suspend fun create(
        name: String,
        type: AcademicTermType,
        start: LocalDate,
        plannedEnd: LocalDate?
    ): Result<AcademicTerm> = runCatching { error("No se usa en estas pruebas") }

    override suspend fun update(term: AcademicTerm): Result<Unit> = runCatching {
        require(term.isValid)
        publicar(state.value.filterNot { it.id == term.id } + term)
    }

    override suspend fun close(termId: String, closedOn: LocalDate, cutScheme: GradingCutScheme?): Result<Unit> =
        runCatching { error("No se usa en estas pruebas") }

    override suspend fun reopen(termId: String): Result<Unit> = runCatching { error("No se usa en estas pruebas") }

    override suspend fun delete(termId: String): Result<Unit> = runCatching {
        publicar(state.value.filterNot { it.id == termId })
    }
}

private class FakeBreakRepository : AcademicBreakRepository {
    private val state = MutableStateFlow<List<AcademicBreak>>(emptyList())
    override val breaks: StateFlow<List<AcademicBreak>> = state

    override suspend fun save(id: String?, name: String, start: LocalDate, end: LocalDate): Result<AcademicBreak> =
        runCatching {
            val tramo = AcademicBreak(
                id = id ?: "break-${state.value.size + 1}",
                userId = UserIds.LOCAL,
                name = name,
                startEpochDay = start.toEpochDay(),
                endEpochDay = end.toEpochDay(),
                createdAt = 1,
                updatedAt = 1
            )
            state.value = state.value.filterNot { it.id == tramo.id } + tramo
            tramo
        }

    override suspend fun delete(breakId: String): Result<Unit> = runCatching {
        state.value = state.value.filterNot { it.id == breakId }
    }
}

private class FakeNotesRepository : NotesRepository {
    private val state = MutableStateFlow<List<QuickNote>>(emptyList())
    private val adjuntos = MutableStateFlow<List<NoteAttachment>>(emptyList())
    override val notes: StateFlow<List<QuickNote>> = state
    override val attachments: StateFlow<List<NoteAttachment>> = adjuntos
    override fun addAttachment(attachment: NoteAttachment) {
        adjuntos.value = if (adjuntos.value.any { it.id == attachment.id }) {
            adjuntos.value
        } else {
            adjuntos.value + attachment
        }
    }
    override fun deleteAttachment(attachmentId: String) {
        adjuntos.value = adjuntos.value.filterNot { it.id == attachmentId }
    }
    override fun setState(noteId: String, archived: Boolean, deletedAt: Long?) {
        state.value = state.value.map {
            if (it.id == noteId) it.copy(archived = archived, deletedAt = deletedAt) else it
        }
    }
    override fun purgeTrash(olderThan: Long) {
        state.value = state.value.filterNot { it.deletedAt != null && it.deletedAt!! < olderThan }
    }
    override fun addNote(note: QuickNote) {
        state.value = if (state.value.any { it.id == note.id }) state.value else state.value + note
    }
    override fun updateNote(note: QuickNote) {
        state.value = state.value.map { if (it.id == note.id) note else it }
    }
    override fun deleteNote(noteId: String) {
        state.value = state.value.filterNot { it.id == noteId }
        adjuntos.value = adjuntos.value.filterNot { it.noteId == noteId }
    }
    override fun setPinned(noteId: String, pinned: Boolean) {
        state.value = state.value.map { if (it.id == noteId) it.copy(pinned = pinned) else it }
    }
}

private class FakeTasksRepository : TasksRepository {
    private val state = MutableStateFlow<List<StudentTask>>(emptyList())
    override val tasks: StateFlow<List<StudentTask>> = state
    private val attachmentState = MutableStateFlow<List<TaskAttachment>>(emptyList())
    override val attachments: StateFlow<List<TaskAttachment>> = attachmentState
    override fun addAttachment(attachment: TaskAttachment) {
        attachmentState.value = attachmentState.value + attachment
    }
    override fun deleteAttachment(attachmentId: String) {
        attachmentState.value = attachmentState.value.filterNot { it.id == attachmentId }
    }
    override fun addTask(task: StudentTask) {
        state.value = if (state.value.any { it.id == task.id }) state.value else state.value + task
    }
    override fun updateTask(task: StudentTask) {
        state.value = state.value.map { if (it.id == task.id) task else it }
    }
    override fun deleteTask(taskId: String) {
        state.value = state.value.filterNot { it.id == taskId }
        attachmentState.value = attachmentState.value.filterNot { it.taskId == taskId }
    }
    override fun setTaskCompleted(taskId: String, completed: Boolean) {
        state.value = state.value.map { if (it.id == taskId) it.copy(completed = completed) else it }
    }
    override fun toggleSubtask(taskId: String, subtaskId: String, completed: Boolean) {
        state.value = state.value.map { task ->
            if (task.id == taskId) {
                task.copy(subtasks = task.subtasks.map { if (it.id == subtaskId) it.copy(isCompleted = completed) else it })
            } else task
        }
    }
    override fun postponeTask(taskId: String, newDueDateMillis: Long) {
        state.value = state.value.map { if (it.id == taskId) it.copy(dueDateMillis = newDueDateMillis) else it }
    }
    override fun setGradingStatus(taskId: String, status: TaskGradingStatus, linkedGradeId: String?) {
        state.value = state.value.map { if (it.id == taskId) it.copy(gradingStatus = status, linkedGradeId = linkedGradeId) else it }
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
    private val occurrenceState = MutableStateFlow<List<ClassOccurrence>>(emptyList())
    override val occurrences: StateFlow<List<ClassOccurrence>> = occurrenceState
    private val agendaState = MutableStateFlow<List<AgendaEvent>>(emptyList())
    override val agendaEvents: StateFlow<List<AgendaEvent>> = agendaState

    override fun saveSession(session: ClassSession) {
        state.value = state.value.filterNot { it.id == session.id } + session
    }

    override fun deleteSession(sessionId: String) {
        state.value = state.value.filterNot { it.id == sessionId }
    }

    override fun saveOccurrence(occurrence: ClassOccurrence) {
        occurrenceState.value = occurrenceState.value.filterNot { it.id == occurrence.id } + occurrence
    }

    override fun deleteOccurrence(occurrenceId: String) {
        occurrenceState.value = occurrenceState.value.filterNot { it.id == occurrenceId }
    }

    override fun saveAgendaEvent(event: AgendaEvent) {
        agendaState.value = agendaState.value.filterNot { it.id == event.id } + event
    }

    override fun deleteAgendaEvent(eventId: String) {
        agendaState.value = agendaState.value.filterNot { it.id == eventId }
    }
}
