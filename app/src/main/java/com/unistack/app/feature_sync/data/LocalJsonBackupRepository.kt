package com.unistack.app.feature_sync.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.unistack.app.core.datastore.AccessibilityPreferencesJson
import com.unistack.app.core.datastore.AppearancePreferencesJson
import com.unistack.app.core.datastore.GradeScenariosJson
import com.unistack.app.core.datastore.GradingCutSchemeJson
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesSort
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskAttachment
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskSubtask
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassModality
import com.unistack.app.feature_schedule.domain.ClassAbsenceReason
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.AgendaEventKind
import com.unistack.app.feature_schedule.domain.AgendaRecurrence
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_terms.domain.AcademicBreak
import com.unistack.app.feature_terms.domain.AcademicBreakRepository
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_terms.domain.AcademicTermStatus
import com.unistack.app.feature_terms.domain.AcademicTermType
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.StudyArea
import com.unistack.app.feature_user.domain.UserIds
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreference
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import com.unistack.app.core.utils.Textos
import com.unistack.app.R
import com.unistack.app.feature_user.domain.toGradingScaleOrNull

class LocalJsonBackupRepository(
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val notesRepository: NotesRepository,
    private val termRepository: AcademicTermRepository,
    private val breakRepository: AcademicBreakRepository
) : LocalBackupRepository {
    override fun exportBackupJson(): String {
        return JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("profile", profileJson(userRepository.userProfile.value))
            .put("terms", JSONArray(termRepository.terms.value.map(::termJson)))
            .put("academicBreaks", JSONArray(breakRepository.breaks.value.map(::breakJson)))
            .put("subjects", JSONArray(gradesRepository.subjects.value.map(::subjectJson)))
            .put("tasks", JSONArray(tasksRepository.tasks.value.map(::taskJson)))
            .put("expenses", JSONArray(expensesRepository.expenses.value.map(::expenseJson)))
            .put("academicWorks", JSONArray(academicWorksRepository.works.value.map(::academicWorkJson)))
            .put("classSessions", JSONArray(scheduleRepository.sessions.value.map(::classSessionJson)))
            .put("classOccurrences", JSONArray(scheduleRepository.occurrences.value.map(::classOccurrenceJson)))
            .put("agendaEvents", JSONArray(scheduleRepository.agendaEvents.value.map(::agendaEventJson)))
            .put("notes", JSONArray(notesRepository.notes.value.map(::noteJson)))
            .toString(2)
    }

    override fun previewBackupJson(json: String): Result<LocalBackupPreview> = runCatching {
        val root = JSONObject(json)
        check(root.optInt("schemaVersion") in 1..SCHEMA_VERSION) { Textos.get(R.string.backup_err_version) }
        val subjects = root.optJSONArray("subjects") ?: JSONArray()
        val grades = (0 until subjects.length()).sumOf { index ->
            subjects.optJSONObject(index)?.optJSONArray("grades")?.length() ?: 0
        }
        LocalBackupPreview(
            schemaVersion = root.optInt("schemaVersion"),
            subjects = subjects.length(),
            grades = grades,
            tasks = root.optJSONArray("tasks")?.length() ?: 0,
            expenses = root.optJSONArray("expenses")?.length() ?: 0,
            academicWorks = root.optJSONArray("academicWorks")?.length() ?: 0,
            agendaEvents = root.optJSONArray("agendaEvents")?.length() ?: 0,
            notes = root.optJSONArray("notes")?.length() ?: 0,
            terms = root.optJSONArray("terms")?.length() ?: 0,
            appLanguage = root.optJSONObject("profile")
                ?.optJSONObject("accessibilityPreferences")
                ?.optString("appLanguage")
                ?.toEnumOrNull<AppLanguage>()
        )
    }

    /*
     * El orden importa.
     *
     * Primero el perfil y los periodos: una materia sin periodo se apunta al activo en cuanto
     * se añade, así que el activo tiene que ser ya el de la copia. Luego las materias, antes
     * que lo que cuelga de ellas.
     *
     * Cada bloque solo se toca si la copia lo trae. Una copia de antes de que existieran los
     * periodos no dice nada de ellos, y eso no significa que haya que borrarlos.
     */
    override suspend fun restoreBackupJson(
        json: String,
        localPhotoUri: String?
    ): Result<LocalBackupPreview> = runCatching {
        val preview = previewBackupJson(json).getOrThrow()
        val root = JSONObject(json)
        restoreProfile(root.optJSONObject("profile"), localPhotoUri)
        root.optJSONArray("terms")?.let { restoreTerms(it) }
        root.optJSONArray("academicBreaks")?.let { restoreBreaks(it) }
        root.optJSONArray("subjects")?.let(::restoreSubjects)
        root.optJSONArray("tasks")?.let(::restoreTasks)
        root.optJSONArray("expenses")?.let { array ->
            reemplazar(
                actuales = expensesRepository.expenses.value.map { it.id },
                enLaCopia = array,
                nuevos = parseExpenses(array),
                id = Expense::id,
                borrar = expensesRepository::deleteExpense
            ) { expense, existe ->
                if (existe) expensesRepository.updateExpense(expense) else expensesRepository.addExpense(expense)
            }
        }
        root.optJSONArray("academicWorks")?.let { array ->
            reemplazar(
                actuales = academicWorksRepository.works.value.map { it.id },
                enLaCopia = array,
                nuevos = parseAcademicWorks(array),
                id = AcademicWork::id,
                borrar = academicWorksRepository::deleteWork
            ) { work, existe ->
                if (existe) academicWorksRepository.updateWork(work) else academicWorksRepository.addWork(work)
            }
        }
        root.optJSONArray("classSessions")?.let { array ->
            reemplazar(
                actuales = scheduleRepository.sessions.value.map { it.id },
                enLaCopia = array,
                nuevos = parseClassSessions(array),
                id = ClassSession::id,
                borrar = scheduleRepository::deleteSession
            ) { session, _ -> scheduleRepository.saveSession(session) }
        }
        root.optJSONArray("classOccurrences")?.let { array ->
            reemplazar(
                actuales = scheduleRepository.occurrences.value.map { it.id },
                enLaCopia = array,
                nuevos = parseClassOccurrences(array),
                id = ClassOccurrence::id,
                borrar = scheduleRepository::deleteOccurrence
            ) { occurrence, _ -> scheduleRepository.saveOccurrence(occurrence) }
        }
        root.optJSONArray("agendaEvents")?.let { array ->
            reemplazar(
                actuales = scheduleRepository.agendaEvents.value.map { it.id },
                enLaCopia = array,
                nuevos = parseAgendaEvents(array),
                id = AgendaEvent::id,
                borrar = scheduleRepository::deleteAgendaEvent
            ) { event, _ -> scheduleRepository.saveAgendaEvent(event) }
        }
        root.optJSONArray("notes")?.let(::restoreNotes)
        preview
    }

    override fun exportAcademicReport(): String {
        val profile = userRepository.userProfile.value
        val scale = profile?.gradingScale
        return buildString {
            appendLine(Textos.get(R.string.report_title))
            appendLine(Textos.get(R.string.report_student, profile?.preferredName?.takeIf { it.isNotBlank() } ?: Textos.get(R.string.settings_profile_student)))
            appendLine()
            gradesRepository.subjects.value.forEach { subject ->
                val average = GradeCalculator.calculateCurrentAverageByCuts(
                    subject.grades,
                    subject.cutScheme.cuts
                )
                appendLine("${subject.name} · ${Textos.get(R.string.backup_summary_average)} ${GradingScaleUtils.formatGrade(average, scale ?: GradingScale.ZERO_TO_FIVE)}")
                subject.grades.forEach { grade ->
                    appendLine("- ${grade.name}: ${grade.value} · ${(grade.percentage * 100).toInt()}%")
                }
                appendLine()
            }
        }.trim()
    }

    override fun exportAcademicPdf(context: Context): Result<String> = runCatching {
        val file = File(context.cacheDir, "unistack-academic-report.pdf")
        val document = PdfDocument()
        val paint = Paint().apply {
            textSize = 12f
            isAntiAlias = true
        }
        val titlePaint = Paint(paint).apply {
            textSize = 18f
            isFakeBoldText = true
        }
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = document.startPage(pageInfo)
        var y = 48f
        page.canvas.drawText(Textos.get(R.string.report_title), 40f, y, titlePaint)
        y += 28f
        exportAcademicReport().lineSequence().forEach { line ->
            if (y > 800f) {
                document.finishPage(page)
                page = document.startPage(pageInfo)
                y = 48f
            }
            page.canvas.drawText(line.take(92), 40f, y, paint)
            y += 18f
        }
        document.finishPage(page)
        file.outputStream().use { output -> document.writeTo(output) }
        document.close()
        file.absolutePath
    }

    override fun exportTasksCsv(): String {
        return buildCsv(
            header = listOf("id", "title", "description", "subjectId", "type", "dueDate", "difficulty", "estimatedMinutes", "completed"),
            rows = tasksRepository.tasks.value.map { task ->
                listOf(task.id, task.title, task.description, task.subjectId.orEmpty(), task.type.name, TaskDateUtils.fromMillis(task.dueDateMillis).toString(), task.difficulty.name, task.estimatedMinutes.toString(), task.completed.toString())
            }
        )
    }

    override fun exportExpensesCsv(): String {
        return buildCsv(
            header = listOf("id", "category", "amount", "date"),
            rows = expensesRepository.expenses.value.map { expense ->
                listOf(expense.id, expense.category.name, expense.amount.toString(), ExpenseDateUtils.fromMillis(expense.dateMillis).toString())
            }
        )
    }

    /**
     * Lo que hay y no está en la copia se borra; lo de la copia se escribe encima o se añade.
     *
     * Lo que se conserva se decide por los identificadores del JSON, no por lo que se pudo
     * leer: una fila que la copia trae pero que no se entiende —de una versión más nueva, o
     * a medias— no es motivo para borrar la que ya hay.
     */
    private inline fun <T> reemplazar(
        actuales: List<String>,
        enLaCopia: JSONArray,
        nuevos: List<T>,
        id: (T) -> String,
        borrar: (String) -> Unit,
        escribir: (T, Boolean) -> Unit
    ) {
        val quedan = enLaCopia.ids()
        actuales.filterNot(quedan::contains).forEach(borrar)
        val habia = actuales.toSet()
        nuevos.forEach { item -> escribir(item, id(item) in habia) }
    }

    private fun profileJson(profile: UserProfile?): JSONObject {
        return JSONObject()
            .put("preferredName", profile?.preferredName)
            // Quién es: sin esto, restaurar en un teléfono nuevo deja la carrera y la
            // universidad como las pusiera el onboarding de ese teléfono.
            .putNullable("careerOrProgram", profile?.careerOrProgram)
            .putNullable("studyArea", profile?.studyArea?.name)
            .putNullable("customStudyArea", profile?.customStudyArea)
            .putNullable("institutionName", profile?.institutionName)
            .putNullable("currentSemester", profile?.currentSemester)
            .putNullable("totalSemesters", profile?.totalSemesters)
            // Solo el nombre: el archivo viaja aparte, dentro del archivo de la copia.
            .putNullable("photoFile", profile?.localPhotoUri?.substringAfterLast('/'))
            .put("gradingScale", profile?.gradingScale?.name)
            .put("customGradeMax", profile?.customGradeMax ?: 100.0)
            .put("passingGrade", profile?.passingGrade ?: 3.0)
            .put("absenceLimit", profile?.absenceLimit)
            .put("targetAverage", profile?.targetAverage ?: 4.0)
            .put("visualPreference", profile?.visualPreference?.name ?: VisualPreference.SYSTEM.name)
            .apply {
                profile?.appearancePreferences?.let { put("appearancePreferences", AppearancePreferencesJson.encode(it)) }
                profile?.accessibilityPreferences?.let { put("accessibilityPreferences", AccessibilityPreferencesJson.encode(it)) }
            }
            .put("academicPeriodScheme", GradingCutSchemeJson.toJson(profile?.gradingCutScheme ?: GradingCutScheme.default()))
            .put("gradeScenarios", GradeScenariosJson.encode(profile?.gradeScenarios.orEmpty()))
            .put("taskRemindersEnabled", profile?.taskRemindersEnabled ?: true)
            .put("academicWorkRemindersEnabled", profile?.academicWorkRemindersEnabled ?: true)
            .put("overdueRemindersEnabled", profile?.overdueRemindersEnabled ?: true)
            .put("gradeInsightRemindersEnabled", profile?.gradeInsightRemindersEnabled ?: true)
            .put("pendingGradeRemindersEnabled", profile?.pendingGradeRemindersEnabled ?: true)
            .put("reminderLeadHours", profile?.reminderLeadHours ?: 24)
            .put("dailyDigestEnabled", profile?.dailyDigestEnabled ?: true)
            .put("dailyDigestHour", profile?.dailyDigestHour ?: 7)
            .put("dailyDigestMinute", profile?.dailyDigestMinute ?: 30)
            .put("termEndReminderEnabled", profile?.termEndReminderEnabled ?: true)
            .put("nextTermReminderEnabled", profile?.nextTermReminderEnabled ?: true)
            .put("nextTermReminderOffset", profile?.nextTermReminderOffset ?: 0)
            .put("quietHoursEnabled", profile?.quietHoursEnabled ?: false)
            .put("quietHoursStartHour", profile?.quietHoursStartHour)
            .put("quietHoursEndHour", profile?.quietHoursEndHour)
            .putNullable("expenseChartStyle", profile?.expenseChartStyle?.name)
            .put("weeklyBudget", profile?.weeklyBudget ?: 0)
            .put("monthlyBudget", profile?.monthlyBudget ?: 0)
            .put("expenseAlertThresholdPercent", profile?.expenseAlertThresholdPercent ?: 80)
            .put("enabledExpenseCategories", JSONArray(profile?.enabledExpenseCategories?.map { it.name }.orEmpty()))
            .put("enabledModules", JSONArray(profile?.enabledModules?.map { it.name }.orEmpty()))
            .put("notesLayout", (profile?.notesLayout ?: NotesLayout.CUADERNO).name)
            .put("noteFormatDefault", (profile?.noteFormatDefault ?: NoteFormat.PLAIN).name)
            .put("notesSort", (profile?.notesSort ?: NotesSort.MODIFICADA).name)
    }

    private fun restoreProfile(profileJson: JSONObject?, localPhotoUri: String?) {
        val current = userRepository.userProfile.value ?: return
        if (profileJson == null) return
        val modules = profileJson.optJSONArray("enabledModules")
            .strings()
            .mapNotNull { name -> runCatching { AppModule.valueOf(name) }.getOrNull() }
            .toSet()
            .ifEmpty { current.enabledModules }
        val expenseCategories = profileJson.optJSONArray("enabledExpenseCategories")
            .strings()
            .mapNotNull { name -> runCatching { ExpenseCategory.valueOf(name) }.getOrNull() }
            .toSet()
            .ifEmpty { current.enabledExpenseCategories }
        userRepository.saveUserProfile(
            current.copy(
                preferredName = profileJson.optString("preferredName", current.preferredName).takeIf { it.isNotBlank() } ?: current.preferredName,
                careerOrProgram = profileJson.nullableOr("careerOrProgram", current.careerOrProgram) { optString(it) },
                studyArea = profileJson.nullableOr("studyArea", current.studyArea) { optString(it).toEnumOrNull<StudyArea>() },
                customStudyArea = profileJson.nullableOr("customStudyArea", current.customStudyArea) { optString(it) },
                institutionName = profileJson.nullableOr("institutionName", current.institutionName) { optString(it) },
                currentSemester = profileJson.nullableOr("currentSemester", current.currentSemester) { optInt(it).takeIf { valor -> valor > 0 } },
                totalSemesters = profileJson.nullableOr("totalSemesters", current.totalSemesters) { optInt(it).takeIf { valor -> valor > 0 } },
                localPhotoUri = localPhotoUri ?: current.localPhotoUri,
                gradingScale = profileJson.optString("gradingScale").toGradingScaleOrNull() ?: current.gradingScale,
                customGradeMax = profileJson.optDouble("customGradeMax", current.customGradeMax).coerceIn(1.0, 100.0),
                passingGrade = profileJson.optDouble("passingGrade", current.passingGrade),
                absenceLimit = if (profileJson.isNull("absenceLimit")) {
                    null
                } else {
                    profileJson.optInt("absenceLimit").takeIf { it > 0 }
                },
                targetAverage = profileJson.optDouble("targetAverage", current.targetAverage),
                visualPreference = profileJson.optString("visualPreference")
                    .toEnum(current.visualPreference),
                appearancePreferences = profileJson.optJSONObject("appearancePreferences")
                    ?.let { AppearancePreferencesJson.decode(it, current.appearancePreferences) }
                    ?: current.appearancePreferences,
                accessibilityPreferences = profileJson.optJSONObject("accessibilityPreferences")
                    ?.let { AccessibilityPreferencesJson.decode(it, current.accessibilityPreferences) }
                    ?: current.accessibilityPreferences,
                gradingCutScheme = GradingCutSchemeJson.fromJson(profileJson.optJSONObject("academicPeriodScheme"))
                    ?: current.gradingCutScheme,
                gradeScenarios = profileJson.optJSONArray("gradeScenarios")
                    ?.let(GradeScenariosJson::decode)
                    ?: current.gradeScenarios,
                taskRemindersEnabled = profileJson.optBoolean("taskRemindersEnabled", current.taskRemindersEnabled),
                academicWorkRemindersEnabled = profileJson.optBoolean(
                    "academicWorkRemindersEnabled",
                    current.academicWorkRemindersEnabled
                ),
                overdueRemindersEnabled = profileJson.optBoolean(
                    "overdueRemindersEnabled",
                    current.overdueRemindersEnabled
                ),
                gradeInsightRemindersEnabled = profileJson.optBoolean(
                    "gradeInsightRemindersEnabled",
                    current.gradeInsightRemindersEnabled
                ),
                pendingGradeRemindersEnabled = profileJson.optBoolean(
                    "pendingGradeRemindersEnabled",
                    current.pendingGradeRemindersEnabled
                ),
                reminderLeadHours = profileJson.optInt("reminderLeadHours", current.reminderLeadHours),
                dailyDigestEnabled = profileJson.optBoolean(
                    "dailyDigestEnabled",
                    current.dailyDigestEnabled
                ),
                dailyDigestHour = profileJson.optInt("dailyDigestHour", current.dailyDigestHour),
                dailyDigestMinute = profileJson.optInt(
                    "dailyDigestMinute",
                    current.dailyDigestMinute
                ),
                termEndReminderEnabled = profileJson.optBoolean(
                    "termEndReminderEnabled",
                    current.termEndReminderEnabled
                ),
                nextTermReminderEnabled = profileJson.optBoolean(
                    "nextTermReminderEnabled",
                    current.nextTermReminderEnabled
                ),
                nextTermReminderOffset = profileJson.optInt(
                    "nextTermReminderOffset",
                    current.nextTermReminderOffset
                ).coerceIn(0, 2),
                quietHoursEnabled = profileJson.optBoolean("quietHoursEnabled", current.quietHoursEnabled),
                quietHoursStartHour = if (profileJson.has("quietHoursStartHour")) {
                    profileJson.optIntOrNull("quietHoursStartHour")
                } else {
                    current.quietHoursStartHour
                },
                quietHoursEndHour = if (profileJson.has("quietHoursEndHour")) {
                    profileJson.optIntOrNull("quietHoursEndHour")
                } else {
                    current.quietHoursEndHour
                },
                expenseChartStyle = profileJson.optString("expenseChartStyle").toEnum(current.expenseChartStyle),
                weeklyBudget = profileJson.optInt("weeklyBudget", current.weeklyBudget),
                monthlyBudget = profileJson.optInt("monthlyBudget", current.monthlyBudget),
                expenseAlertThresholdPercent = profileJson.optInt("expenseAlertThresholdPercent", current.expenseAlertThresholdPercent),
                enabledExpenseCategories = expenseCategories,
                enabledModules = modules,
                notesLayout = profileJson.optString("notesLayout")
                    .let { name -> runCatching { NotesLayout.valueOf(name) }.getOrNull() }
                    ?: current.notesLayout,
                noteFormatDefault = profileJson.optString("noteFormatDefault")
                    .let { name -> runCatching { NoteFormat.valueOf(name) }.getOrNull() }
                    ?: current.noteFormatDefault,
                notesSort = profileJson.optString("notesSort")
                    .let { name -> runCatching { NotesSort.valueOf(name) }.getOrNull() }
                    ?: current.notesSort,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /*
     * Los periodos, que es lo que el histórico necesita para colocar cada materia.
     *
     * Se guardan con el usuario de este teléfono y no con el de la copia: el almacén solo
     * devuelve las filas de quien está usando la app, y un periodo apuntado a otro usuario
     * estaría en la base sin aparecer en ningún sitio.
     */
    private suspend fun restoreTerms(array: JSONArray) {
        val userId = UserIds.normalize(userRepository.currentUser.value.userId)
        val quedan = array.ids()
        termRepository.terms.value
            .filterNot { it.id in quedan }
            .forEach { termRepository.delete(it.id) }
        val terms = array.objects().mapNotNull(::parseTerm).map { it.copy(userId = userId) }
        terms.forEach { term -> termRepository.update(term) }
        /*
         * Se espera a que el periodo activo sea ya el de la copia.
         *
         * Una materia sin periodo se apunta al activo al añadirse, y ese dato sale de un flujo
         * que tarda un momento en enterarse de lo que se acaba de escribir. Sin la espera, las
         * materias de antes de los periodos podían quedar colgadas del periodo del onboarding,
         * que la copia acaba de borrar.
         */
        val activo = terms.firstOrNull { it.isActive }?.id
        withTimeoutOrNull(ESPERA_DEL_PERIODO_MS) {
            termRepository.activeTerm.first { it?.id == activo }
        }
    }

    private suspend fun restoreBreaks(array: JSONArray) {
        val quedan = array.ids()
        breakRepository.breaks.value
            .filterNot { it.id in quedan }
            .forEach { breakRepository.delete(it.id) }
        array.objects().mapNotNull(::parseBreak).forEach { tramo ->
            breakRepository.save(tramo.id, tramo.name, tramo.start, tramo.end)
        }
    }

    private fun restoreSubjects(array: JSONArray) {
        val actuales = gradesRepository.subjects.value
        val quedan = array.ids()
        actuales.filterNot { it.id in quedan }.forEach { gradesRepository.deleteSubject(it.id) }
        array.objects().forEach { item ->
            val leida = parseSubject(item) ?: return@forEach
            val existing = actuales.firstOrNull { it.id == leida.id }
            // Las copias de antes no traían los cortes cerrados: sin la clave, se quedan los que hay.
            val subject = if (item.has("closedCutIds")) {
                leida
            } else {
                leida.copy(closedCutIds = existing?.closedCutIds.orEmpty())
            }
            if (existing == null) {
                gradesRepository.addSubject(subject.copy(grades = emptyList()))
            } else {
                gradesRepository.updateSubject(subject.copy(grades = existing.grades))
                val notasQueQuedan = item.optJSONArray("grades").ids()
                existing.grades
                    .filterNot { it.id in notasQueQuedan }
                    .forEach { gradesRepository.deleteGrade(subject.id, it.id) }
            }
            subject.grades.forEach { grade ->
                if (existing?.grades?.any { it.id == grade.id } == true) {
                    gradesRepository.updateGrade(subject.id, grade)
                } else {
                    gradesRepository.addGrade(subject.id, grade)
                }
            }
        }
    }

    private fun restoreTasks(array: JSONArray) {
        val actuales = tasksRepository.tasks.value
        val quedan = array.ids()
        actuales.filterNot { it.id in quedan }.forEach { tasksRepository.deleteTask(it.id) }
        array.objects().forEach { item ->
            val leida = parseTask(item) ?: return@forEach
            val existing = actuales.firstOrNull { it.id == leida.id }
            // Las copias de antes no traían subtareas: sin la clave, se quedan las que hay.
            val task = if (item.has("subtasks")) leida else leida.copy(subtasks = existing?.subtasks.orEmpty())
            if (existing == null) tasksRepository.addTask(task) else tasksRepository.updateTask(task)
            if (item.has("attachments")) {
                val adjuntos = item.optJSONArray("attachments")
                val habia = tasksRepository.attachments.value.filter { it.taskId == task.id }
                val siguen = adjuntos.ids()
                habia.filterNot { it.id in siguen }.forEach { tasksRepository.deleteAttachment(it.id) }
                parseTaskAttachments(task.id, adjuntos)
                    .filter { nuevo -> habia.none { it.id == nuevo.id } }
                    .forEach(tasksRepository::addAttachment)
            }
        }
    }

    private fun restoreNotes(array: JSONArray) {
        val actuales = notesRepository.notes.value
        val quedan = array.ids()
        actuales.filterNot { it.id in quedan }.forEach { notesRepository.deleteNote(it.id) }
        array.objects().forEach { item ->
            val note = parseNote(item) ?: return@forEach
            if (actuales.any { it.id == note.id }) notesRepository.updateNote(note) else notesRepository.addNote(note)
            if (item.has("attachments")) {
                val adjuntos = item.optJSONArray("attachments")
                val habia = notesRepository.attachments.value.filter { it.noteId == note.id }
                val siguen = adjuntos.ids()
                habia.filterNot { it.id in siguen }.forEach { notesRepository.deleteAttachment(it.id) }
                parseNoteAttachments(note.id, adjuntos)
                    .filter { nuevo -> habia.none { it.id == nuevo.id } }
                    .forEach(notesRepository::addAttachment)
            }
        }
    }

    private fun termJson(term: AcademicTerm): JSONObject = JSONObject()
        .put("id", term.id)
        .put("name", term.name)
        .put("type", term.type.name)
        .put("startEpochDay", term.startEpochDay)
        .putNullable("plannedEndEpochDay", term.plannedEndEpochDay)
        .putNullable("closedEpochDay", term.closedEpochDay)
        .put("status", term.status.name)
        .put("createdAt", term.createdAt)
        .put("updatedAt", term.updatedAt)
        .putNullable("cutScheme", term.cutScheme?.let(GradingCutSchemeJson::toJson))

    private fun parseTerm(item: JSONObject): AcademicTerm? {
        val created = item.optLong("createdAt", System.currentTimeMillis())
        return AcademicTerm(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return null,
            userId = "",
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return null,
            type = item.optString("type").toEnum(AcademicTermType.SEMESTER),
            startEpochDay = if (item.has("startEpochDay")) item.optLong("startEpochDay") else return null,
            plannedEndEpochDay = item.optLongOrNull("plannedEndEpochDay"),
            closedEpochDay = item.optLongOrNull("closedEpochDay"),
            status = item.optString("status").toEnumOrNull<AcademicTermStatus>() ?: return null,
            createdAt = created,
            updatedAt = item.optLong("updatedAt", created),
            cutScheme = GradingCutSchemeJson.fromJson(item.optJSONObject("cutScheme"))
        ).takeIf { it.isValid }
    }

    private fun breakJson(tramo: AcademicBreak): JSONObject = JSONObject()
        .put("id", tramo.id)
        .put("name", tramo.name)
        .put("startEpochDay", tramo.startEpochDay)
        .put("endEpochDay", tramo.endEpochDay)
        .put("createdAt", tramo.createdAt)
        .put("updatedAt", tramo.updatedAt)

    private fun parseBreak(item: JSONObject): AcademicBreak? {
        val created = item.optLong("createdAt", System.currentTimeMillis())
        return AcademicBreak(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return null,
            userId = "",
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return null,
            startEpochDay = if (item.has("startEpochDay")) item.optLong("startEpochDay") else return null,
            endEpochDay = if (item.has("endEpochDay")) item.optLong("endEpochDay") else return null,
            createdAt = created,
            updatedAt = item.optLong("updatedAt", created)
        ).takeIf { it.isValid }
    }

    private fun subjectJson(subject: Subject): JSONObject = JSONObject()
        .put("id", subject.id)
        .put("name", subject.name)
        .put("targetAverage", subject.targetAverage)
        .put("visualType", subject.visualType.name)
        .put("customColor", subject.customColor)
        .put("periodScheme", GradingCutSchemeJson.toJson(subject.cutScheme))
        .put("activePeriodId", subject.activeCutId)
        .put("historyPromptStatus", subject.historyPromptStatus.name)
        .put("unknownCutIds", JSONArray(subject.unknownCutIds.toList()))
        /*
         * Estos tres viajan porque si no, restaurar los pierde.
         *
         * `termId` es a qué periodo pertenece la materia: sin él, una copia restaurada deja
         * el histórico entero sin poder colocar nada. `closedCutIds` son los cortes que el
         * usuario dio por cerrados, y `repeatedFromSubjectId` la marca de que la está
         * repitiendo.
         */
        .put("termId", subject.termId)
        .put("closedCutIds", JSONArray(subject.closedCutIds.toList()))
        .put("repeatedFromSubjectId", subject.repeatedFromSubjectId)
        .put("grades", JSONArray(subject.grades.map(::gradeJson)))

    private fun gradeJson(grade: GradeItem): JSONObject = JSONObject()
        .put("id", grade.id)
        .put("name", grade.name)
        .put("value", grade.value)
        .put("percentage", grade.percentage)
        .put("type", grade.type.name)
        .put("periodId", grade.cutId)
        .put("source", grade.source.name)
        .put("weightStatus", grade.weightStatus.name)
        .put("taskId", grade.taskId)
        .put("recordedAt", grade.recordedAt)

    private fun taskJson(task: StudentTask): JSONObject = JSONObject()
        .put("id", task.id)
        .put("title", task.title)
        .put("description", task.description)
        .put("subjectId", task.subjectId)
        .put("type", task.type.name)
        .put("dueDateMillis", task.dueDateMillis)
        .put("difficulty", task.difficulty.name)
        .put("estimatedMinutes", task.estimatedMinutes)
        .put("completed", task.completed)
        .put("periodId", task.cutId)
        .put("gradingStatus", task.gradingStatus.name)
        .put("linkedGradeId", task.linkedGradeId)
        .put("completedAt", task.completedAt)
        .put("createdAt", task.createdAt)
        .put("updatedAt", task.updatedAt)
        .put(
            "subtasks",
            JSONArray(
                task.subtasks.sortedBy { it.position }.map { subtask ->
                    JSONObject()
                        .put("id", subtask.id)
                        .put("title", subtask.title)
                        .put("isCompleted", subtask.isCompleted)
                        .put("position", subtask.position)
                }
            )
        )
        .put(
            "attachments",
            JSONArray(
                tasksRepository.attachments.value
                    .filter { it.taskId == task.id }
                    .map(::taskAttachmentJson)
            )
        )

    // De los adjuntos de una tarea viaja lo mismo que de los de una nota: ver `attachmentJson`.
    private fun taskAttachmentJson(attachment: TaskAttachment): JSONObject = JSONObject()
        .put("id", attachment.id)
        .put("kind", attachment.kind.name)
        .put("displayName", attachment.displayName)
        .put("storedName", attachment.storedName)
        .put("mimeType", attachment.mimeType)
        .put("sizeBytes", attachment.sizeBytes)
        .put("durationMillis", attachment.durationMillis)
        .put("createdAt", attachment.createdAt)

    private fun classSessionJson(session: ClassSession): JSONObject = JSONObject()
        .put("id", session.id)
        .put("subjectId", session.subjectId)
        .put("daysOfWeek", JSONArray(session.daysOfWeek.sorted()))
        .put("startMinute", session.startMinute)
        .put("endMinute", session.endMinute)
        .put("location", session.location)
        .put("reminderMinutes", session.reminderMinutes)
        .put("repeatEveryWeeks", session.repeatEveryWeeks)
        .put("recurrenceStartEpochDay", session.recurrenceStartEpochDay)
        .put("createdAt", session.createdAt)
        .put("updatedAt", session.updatedAt)

    private fun classOccurrenceJson(occurrence: ClassOccurrence): JSONObject = JSONObject()
        .put("id", occurrence.id)
        .put("sessionId", occurrence.sessionId)
        .put("dateEpochDay", occurrence.dateEpochDay)
        .put("status", occurrence.status.name)
        .put("modality", occurrence.modality.name)
        .put("absenceReason", occurrence.absenceReason?.name)
        .put("note", occurrence.note)
        .put("overrideStartMinute", occurrence.overrideStartMinute)
        .put("overrideEndMinute", occurrence.overrideEndMinute)
        .put("overrideLocation", occurrence.overrideLocation)
        .put("updatedAt", occurrence.updatedAt)

    private fun agendaEventJson(event: AgendaEvent): JSONObject = JSONObject()
        .put("id", event.id)
        .put("title", event.title)
        .put("notes", event.notes)
        .put("kind", event.kind.name)
        .put("startMillis", event.startMillis)
        .put("endMillis", event.endMillis)
        .put("allDay", event.allDay)
        .put("location", event.location)
        .put("reminderMinutes", event.reminderMinutes)
        .put("recurrence", event.recurrence.name)
        .put("recurrenceEndEpochDay", event.recurrenceEndEpochDay)
        .put("colorArgb", event.colorArgb)
        .put("createdAt", event.createdAt)
        .put("updatedAt", event.updatedAt)

    /*
     * Los apuntes viajan enteros, con su materia y su formato.
     *
     * Cuando eran una hoja en las preferencias del telefono no entraban en la copia: restaurar
     * un respaldo devolvia las materias, las tareas y los gastos, y dejaba las notas donde
     * estaban, que en un telefono nuevo es en ninguna parte.
     */
    private fun noteJson(note: QuickNote): JSONObject = JSONObject()
        .put("id", note.id)
        .put("title", note.title)
        .put("body", note.body)
        .put("reminderAt", note.reminderAt)
        .put("colorArgb", note.colorArgb)
        .put("archived", note.archived)
        .put("deletedAt", note.deletedAt)
        .put("subjectId", note.subjectId)
        .put("format", note.format.name)
        .put("pinned", note.pinned)
        .put("createdAt", note.createdAt)
        .put("updatedAt", note.updatedAt)
        .put(
            "attachments",
            JSONArray(
                notesRepository.attachments.value
                    .filter { it.noteId == note.id }
                    .map(::attachmentJson)
            )
        )

    /*
     * En el JSON va la ficha del adjunto, no el archivo.
     *
     * Una sola foto de móvil son tres o cuatro megas, y la copia en la nube va a un documento
     * de Firestore que no admite más de uno. Los archivos de verdad viajan al lado, dentro del
     * archivo de la copia que se guarda o se comparte: ver `BackupArchive`. Si una copia llega
     * sin ellos —un JSON suelto, o la nube—, la nota dice que llevaba una foto llamada así en
     * vez de dejar un hueco.
     */
    private fun attachmentJson(attachment: NoteAttachment): JSONObject = JSONObject()
        .put("id", attachment.id)
        .put("kind", attachment.kind.name)
        .put("displayName", attachment.displayName)
        .put("storedName", attachment.storedName)
        .put("mimeType", attachment.mimeType)
        .put("sizeBytes", attachment.sizeBytes)
        .put("durationMillis", attachment.durationMillis)
        .put("createdAt", attachment.createdAt)

    private fun parseNoteAttachments(noteId: String, array: JSONArray?): List<NoteAttachment> =
        array.objects().mapNotNull { item ->
            val id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val storedName = item.optString("storedName").takeIf { it.isNotBlank() }
                ?: return@mapNotNull null
            NoteAttachment(
                id = id,
                noteId = noteId,
                kind = item.optString("kind").toEnum(AttachmentKind.FILE),
                displayName = item.optString("displayName").ifBlank { "Adjunto" },
                storedName = storedName,
                mimeType = item.optString("mimeType").ifBlank { "application/octet-stream" },
                sizeBytes = item.optLong("sizeBytes", 0L),
                durationMillis = if (item.isNull("durationMillis")) null else item.optLong("durationMillis"),
                createdAt = item.optLong("createdAt", System.currentTimeMillis())
            )
        }

    private fun parseTaskAttachments(taskId: String, array: JSONArray?): List<TaskAttachment> =
        parseNoteAttachments(taskId, array).map { ficha ->
            TaskAttachment(
                id = ficha.id,
                taskId = taskId,
                kind = ficha.kind,
                displayName = ficha.displayName,
                storedName = ficha.storedName,
                mimeType = ficha.mimeType,
                sizeBytes = ficha.sizeBytes,
                durationMillis = ficha.durationMillis,
                createdAt = ficha.createdAt
            )
        }

    /*
     * Una nota sin texto también vale si lleva algo colgado.
     *
     * Una foto de la pizarra sin una sola palabra es la nota más común. Leerlas exigía cuerpo,
     * así que restaurar se saltaba justo esas, y sus adjuntos se quedaban sin nota.
     */
    private fun parseNote(item: JSONObject): QuickNote? {
        val id = item.optString("id").takeIf { it.isNotBlank() } ?: return null
        val body = item.optString("body")
        val title = item.optString("title")
        val llevaAlgo = (item.optJSONArray("attachments")?.length() ?: 0) > 0
        if (body.isBlank() && title.isBlank() && !llevaAlgo) return null
        val created = item.optLong("createdAt", System.currentTimeMillis())
        return QuickNote(
            id = id,
            title = title,
            body = body,
            reminderAt = if (item.isNull("reminderAt")) null else item.optLong("reminderAt"),
            colorArgb = if (item.isNull("colorArgb")) null else item.optInt("colorArgb"),
            archived = item.optBoolean("archived", false),
            deletedAt = if (item.isNull("deletedAt")) null else item.optLong("deletedAt"),
            subjectId = item.optString("subjectId").takeIf { it.isNotBlank() },
            format = item.optString("format").toEnum(NoteFormat.PLAIN),
            pinned = item.optBoolean("pinned", false),
            createdAt = created,
            updatedAt = item.optLong("updatedAt", created)
        )
    }

    private fun expenseJson(expense: Expense): JSONObject = JSONObject()
        .put("id", expense.id)
        .put("category", expense.category.name)
        .put("amount", expense.amount)
        .put("dateMillis", expense.dateMillis)
        .put("createdAt", expense.createdAt)
        .put("updatedAt", expense.updatedAt)

    private fun academicWorkJson(work: AcademicWork): JSONObject = JSONObject()
        .put("id", work.id)
        .put("templateId", work.templateId)
        .put("title", work.title)
        .put("subjectId", work.subjectId)
        .put("dueDateMillis", work.dueDateMillis)
        .put("status", work.status.name)
        .put("priority", work.priority.name)
        .put("completedChecklistIds", JSONArray(work.completedChecklistIds.toList()))
        .put("thesis", work.thesis)
        .put("outline", work.outline)
        .put("sources", work.sources)
        .put("notes", work.notes)
        .put("createdAt", work.createdAt)
        .put("updatedAt", work.updatedAt)

    private fun parseSubject(item: JSONObject): Subject? {
        return Subject(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return null,
            targetAverage = item.optDouble("targetAverage"),
            visualType = item.optString("visualType").toEnum(SubjectVisualType.TEAL),
            customColor = if (item.isNull("customColor")) null else item.optInt("customColor"),
            grades = parseGrades(item.optJSONArray("grades")),
            cutScheme = GradingCutSchemeJson.fromJson(item.optJSONObject("periodScheme"))
                ?: GradingCutScheme.default(),
            activeCutId = item.optString("activePeriodId", ""),
            historyPromptStatus = item.optString("historyPromptStatus")
                .toEnum(PriorHistoryPromptStatus.NOT_SHOWN),
            unknownCutIds = item.optJSONArray("unknownCutIds").strings().toSet(),
            closedCutIds = item.optJSONArray("closedCutIds").strings().toSet(),
            // Ausentes en copias viejas: nulo es exactamente lo que significaban entonces.
            termId = if (item.isNull("termId")) null else item.optString("termId").takeIf { it.isNotBlank() },
            repeatedFromSubjectId = if (item.isNull("repeatedFromSubjectId")) {
                null
            } else {
                item.optString("repeatedFromSubjectId").takeIf { it.isNotBlank() }
            }
        )
    }

    private fun parseGrades(array: JSONArray?): List<GradeItem> = array.objects().mapNotNull { item ->
        GradeItem(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            value = item.optDouble("value"),
            percentage = item.optDouble("percentage"),
            type = item.optString("type").toEnum(GradeType.WORKSHOP),
            cutId = item.optString("periodId", "period-1").ifBlank { "period-1" },
            source = item.optString("source").toEnum(GradeSource.ACTIVITY),
            weightStatus = item.optString("weightStatus").toEnum(GradeWeightStatus.KNOWN),
            taskId = item.optNullableString("taskId"),
            recordedAt = item.optLong("recordedAt", System.currentTimeMillis())
        )
    }

    private fun parseTask(item: JSONObject): StudentTask? {
        val id = item.optString("id").takeIf { it.isNotBlank() } ?: return null
        return StudentTask(
            id = id,
            title = item.optString("title").takeIf { it.isNotBlank() } ?: return null,
            description = item.optString("description", ""),
            subjectId = item.optNullableString("subjectId"),
            type = item.optString("type").toEnum(TaskType.WORKSHOP),
            dueDateMillis = item.optLong("dueDateMillis"),
            difficulty = item.optString("difficulty").toEnum(TaskDifficulty.MEDIUM),
            estimatedMinutes = item.optInt("estimatedMinutes"),
            completed = item.optBoolean("completed"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
            cutId = item.optNullableString("periodId"),
            gradingStatus = item.optString("gradingStatus").toEnum(TaskGradingStatus.UNDECIDED),
            linkedGradeId = item.optNullableString("linkedGradeId"),
            completedAt = if (item.isNull("completedAt")) null else item.optLong("completedAt"),
            subtasks = item.optJSONArray("subtasks").objects().mapIndexedNotNull { index, subtask ->
                TaskSubtask(
                    id = subtask.optString("id").takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null,
                    taskId = id,
                    title = subtask.optString("title").takeIf { it.isNotBlank() } ?: return@mapIndexedNotNull null,
                    isCompleted = subtask.optBoolean("isCompleted"),
                    position = subtask.optInt("position", index)
                )
            }
        )
    }

    private fun parseClassSessions(array: JSONArray?): List<ClassSession> = array.objects().mapNotNull { item ->
        ClassSession(
            id = item.optString("id").takeIf(String::isNotBlank) ?: return@mapNotNull null,
            subjectId = item.optString("subjectId").takeIf(String::isNotBlank) ?: return@mapNotNull null,
            daysOfWeek = item.optJSONArray("daysOfWeek")
                ?.let { days -> (0 until days.length()).map { days.optInt(it) }.filter { it in 1..7 }.toSet() }
                .orEmpty(),
            startMinute = item.optInt("startMinute", -1),
            endMinute = item.optInt("endMinute", -1),
            location = item.optString("location"),
            reminderMinutes = item.optInt("reminderMinutes", 15),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
            repeatEveryWeeks = item.optInt("repeatEveryWeeks", 1),
            recurrenceStartEpochDay = item.optLong("recurrenceStartEpochDay", 0L)
        ).takeIf { it.isValid }
    }

    private fun parseClassOccurrences(array: JSONArray?): List<ClassOccurrence> =
        array.objects().mapNotNull { item ->
            ClassOccurrence(
                id = item.optString("id").takeIf(String::isNotBlank) ?: return@mapNotNull null,
                sessionId = item.optString("sessionId").takeIf(String::isNotBlank) ?: return@mapNotNull null,
                dateEpochDay = item.optLong("dateEpochDay"),
                status = item.optString("status").toEnum(ClassAttendanceStatus.PENDING),
                modality = item.optString("modality").toEnum(ClassModality.IN_PERSON),
                absenceReason = item.optString("absenceReason").toEnumOrNull<ClassAbsenceReason>(),
                note = item.optString("note"),
                overrideStartMinute = item.optIntOrNull("overrideStartMinute"),
                overrideEndMinute = item.optIntOrNull("overrideEndMinute"),
                overrideLocation = item.optNullableString("overrideLocation"),
                updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
            ).takeIf { it.isValid }
        }

    private fun parseAgendaEvents(array: JSONArray?): List<AgendaEvent> = array.objects().mapNotNull { item ->
        AgendaEvent(
            id = item.optString("id").takeIf(String::isNotBlank) ?: return@mapNotNull null,
            title = item.optString("title"),
            notes = item.optString("notes"),
            kind = item.optString("kind").toEnum(AgendaEventKind.CUSTOM),
            startMillis = item.optLong("startMillis"),
            endMillis = if (item.isNull("endMillis")) null else item.optLong("endMillis"),
            allDay = item.optBoolean("allDay"),
            location = item.optString("location"),
            reminderMinutes = item.optInt("reminderMinutes", 0),
            recurrence = item.optString("recurrence").toEnum(AgendaRecurrence.NONE),
            recurrenceEndEpochDay = if (item.isNull("recurrenceEndEpochDay")) null else item.optLong("recurrenceEndEpochDay"),
            colorArgb = if (item.isNull("colorArgb")) null else item.optInt("colorArgb"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        ).takeIf(AgendaEvent::isValid)
    }

    private fun parseExpenses(array: JSONArray?): List<Expense> = array.objects().mapNotNull { item ->
        Expense(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            category = item.optString("category").toEnum(ExpenseCategory.OTHER),
            amount = item.optInt("amount"),
            dateMillis = item.optLong("dateMillis"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun parseAcademicWorks(array: JSONArray?): List<AcademicWork> = array.objects().mapNotNull { item ->
        AcademicWork(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            templateId = item.optString("templateId").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            title = item.optString("title").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            subjectId = item.optNullableString("subjectId"),
            dueDateMillis = if (item.isNull("dueDateMillis")) null else item.optLong("dueDateMillis"),
            status = item.optString("status").toEnum(AcademicWorkStatus.DRAFT),
            priority = item.optString("priority").toEnum(AcademicWorkPriority.MEDIUM),
            completedChecklistIds = item.optJSONArray("completedChecklistIds").strings().toSet(),
            thesis = item.optString("thesis"),
            outline = item.optString("outline"),
            sources = item.optString("sources"),
            notes = item.optString("notes"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    private fun buildCsv(header: List<String>, rows: List<List<String>>): String {
        return (listOf(header) + rows).joinToString("\n") { row -> row.joinToString(",") { it.csvEscape() } }
    }

    private fun JSONArray?.objects(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optJSONObject(index) }
    }

    private fun JSONArray?.strings(): List<String> {
        if (this == null) return emptyList()
        return (0 until length()).mapNotNull { index -> optString(index).takeIf { it.isNotBlank() } }
    }

    /** Los identificadores que trae un bloque, se hayan podido leer sus filas o no. */
    private fun JSONArray?.ids(): Set<String> =
        objects().mapNotNull { item -> item.optString("id").takeIf { it.isNotBlank() } }.toSet()

    /**
     * Un nulo que se escribe, en vez de quitar la clave.
     *
     * `put` con nulo borra la clave, y entonces al leer no se distingue «no tenía» de «esta
     * copia es de cuando no se guardaba». Con el nulo escrito, lo que falta es de una copia
     * antigua y se deja lo que hay.
     */
    private fun JSONObject.putNullable(key: String, value: Any?): JSONObject = put(key, value ?: JSONObject.NULL)

    private inline fun <T> JSONObject.nullableOr(key: String, current: T?, read: JSONObject.(String) -> T?): T? = when {
        !has(key) -> current
        isNull(key) -> null
        else -> read(key)
    }

    private inline fun <reified T : Enum<T>> String.toEnum(default: T): T = runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? = runCatching { enumValueOf<T>(this) }.getOrNull()
    private fun JSONObject.optNullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    private fun JSONObject.optIntOrNull(key: String): Int? = if (isNull(key) || !has(key)) null else optInt(key)
    private fun JSONObject.optLongOrNull(key: String): Long? = if (isNull(key) || !has(key)) null else optLong(key)
    private fun String.csvEscape(): String = "\"${replace("\"", "\"\"")}\""

    private companion object {
        /*
         * 12: periodos, días sin clase, subtareas y adjuntos de tareas, cortes cerrados de cada
         * materia, y el perfil y las preferencias enteros.
         */
        const val SCHEMA_VERSION = 12

        const val ESPERA_DEL_PERIODO_MS = 3_000L
    }
}
