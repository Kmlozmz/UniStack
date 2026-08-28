package com.unistack.app.feature_sync.data

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.firestore
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupState
import com.unistack.app.feature_notes.domain.NoteFormat
import com.unistack.app.feature_notes.domain.NotesLayout
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
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
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.GradingCutScheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await

class FirebaseCloudBackupRepository(
    private val context: Context,
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val notesRepository: NotesRepository
) : CloudBackupRepository {

    private val _state = MutableStateFlow(CloudBackupState())
    override val state: StateFlow<CloudBackupState> = _state

    override val isConfigured: Boolean
        get() = runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) FirebaseApp.initializeApp(context)
            FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)

    override suspend fun backupNow(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val now = System.currentTimeMillis()
        val payload = mapOf(
            "schemaVersion" to 7,
            "updatedAt" to now,
            "profile" to profileMap(),
            "subjects" to gradesRepository.subjects.value.map(::subjectMap),
            "tasks" to tasksRepository.tasks.value.map(::taskMap),
            "expenses" to expensesRepository.expenses.value.map(::expenseMap),
            "academicWorks" to academicWorksRepository.works.value.map(::academicWorkMap),
            "classSessions" to scheduleRepository.sessions.value.map(::classSessionMap),
            "classOccurrences" to scheduleRepository.occurrences.value.map(::classOccurrenceMap),
            "agendaEvents" to scheduleRepository.agendaEvents.value.map(::agendaEventMap),
            "notes" to notesRepository.notes.value.map(::noteMap)
        )

        Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("backups")
            .document("current")
            .set(payload)
            .await()

        _state.update {
            it.copy(
                inProgress = false,
                lastBackupAt = now,
                message = "Backup cloud actualizado.",
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: "No se pudo actualizar el backup."
            )
        }
    }

    override suspend fun restoreLatest(): Result<Unit> = runCatching {
        _state.update { it.copy(inProgress = true, message = null, errorMessage = null) }
        val userId = linkedUserId()
        ensureFirebaseConfigured()

        val snapshot = Firebase.firestore
            .collection("users")
            .document(userId)
            .collection("backups")
            .document("current")
            .get()
            .await()

        check(snapshot.exists()) { "No hay backup cloud para restaurar." }
        val data = snapshot.data.orEmpty()

        parseSubjects(data["subjects"]).forEach { subject ->
            gradesRepository.addSubject(subject.copy(grades = emptyList()))
            subject.grades.forEach { grade ->
                gradesRepository.addGrade(subject.id, grade)
            }
        }
        parseTasks(data["tasks"]).forEach(tasksRepository::addTask)
        parseExpenses(data["expenses"]).forEach(expensesRepository::addExpense)
        parseAcademicWorks(data["academicWorks"]).forEach(academicWorksRepository::addWork)
        parseClassSessions(data["classSessions"]).forEach(scheduleRepository::saveSession)
        parseClassOccurrences(data["classOccurrences"]).forEach(scheduleRepository::saveOccurrence)
        parseAgendaEvents(data["agendaEvents"]).forEach(scheduleRepository::saveAgendaEvent)
        parseNotes(data["notes"]).forEach(notesRepository::addNote)

        val now = System.currentTimeMillis()
        _state.update {
            it.copy(
                inProgress = false,
                lastRestoreAt = now,
                message = "Backup cloud restaurado en este dispositivo.",
                errorMessage = null
            )
        }
    }.onFailure { throwable ->
        _state.update {
            it.copy(
                inProgress = false,
                message = null,
                errorMessage = throwable.message ?: "No se pudo restaurar el backup."
            )
        }
    }

    private fun linkedUserId(): String {
        val user = userRepository.currentUser.value
        val providerUserId = user.providerUserId
        check(user.isLinked && !providerUserId.isNullOrBlank()) {
            "Conecta una cuenta de Google antes de usar backup cloud."
        }
        return providerUserId
    }

    private fun ensureFirebaseConfigured() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Falta app/google-services.json para inicializar Firebase."
        }
    }

    private fun profileMap(): Map<String, Any?> {
        val profile = userRepository.userProfile.value ?: return emptyMap()
        return mapOf(
            "preferredName" to profile.preferredName,
            "careerOrProgram" to profile.careerOrProgram,
            "studyArea" to profile.studyArea?.name,
            "gradingScale" to profile.gradingScale.name,
            "customGradeMax" to profile.customGradeMax,
            "passingGrade" to profile.passingGrade,
            "absenceLimit" to profile.absenceLimit,
            "targetAverage" to profile.targetAverage,
            "enabledModules" to profile.enabledModules.map { it.name },
            "notesLayout" to profile.notesLayout.name,
            "visualPreference" to profile.visualPreference.name,
            "appearancePreferences" to mapOf(
                "backgroundStyle" to profile.appearancePreferences.backgroundStyle.name,
                "customBackgroundColor" to profile.appearancePreferences.customBackgroundColor,
                "customThemeBase" to profile.appearancePreferences.customThemeBase.name,
                "accentStyle" to profile.appearancePreferences.accentStyle.name,
                "customAccentColor" to profile.appearancePreferences.customAccentColor,
                "accentIntensity" to profile.appearancePreferences.accentIntensity.name,
                "surfaceStyle" to profile.appearancePreferences.surfaceStyle.name,
                "cornerStyle" to profile.appearancePreferences.cornerStyle.name,
                "interfaceDensity" to profile.appearancePreferences.interfaceDensity.name,
                "motionPreference" to profile.appearancePreferences.motionPreference.name,
                "textScale" to profile.appearancePreferences.textScale.name,
                "typographyStyle" to profile.appearancePreferences.typographyStyle.name,
                "decimalPlaces" to profile.appearancePreferences.decimalPlaces,
                "bottomBarStyle" to profile.appearancePreferences.bottomBarStyle.name,
                "academicIndicatorStyle" to profile.appearancePreferences.academicIndicatorStyle.name,
                "showHomeGreeting" to profile.appearancePreferences.showHomeGreeting,
                "showHomeHero" to profile.appearancePreferences.showHomeHero,
                "showHomeAgenda" to profile.appearancePreferences.showHomeAgenda,
                "showHomeSnapshot" to profile.appearancePreferences.showHomeSnapshot,
                "homeSectionOrder" to profile.appearancePreferences.homeSectionOrder.map { it.name },
                "heroAutoRotate" to profile.appearancePreferences.heroAutoRotate,
                "heroShowsGrades" to profile.appearancePreferences.heroShowsGrades,
                "heroShowsTasks" to profile.appearancePreferences.heroShowsTasks,
                "heroShowsExpenses" to profile.appearancePreferences.heroShowsExpenses,
                "initialTab" to profile.appearancePreferences.initialTab.name,
                "visualPreset" to profile.appearancePreferences.visualPreset.name
            ),
            "accessibilityPreferences" to mapOf(
                "appLanguage" to profile.accessibilityPreferences.appLanguage.name,
                "highContrastEnabled" to profile.accessibilityPreferences.highContrastEnabled,
                "use24HourTime" to profile.accessibilityPreferences.use24HourTime,
                "textScale" to profile.accessibilityPreferences.textScale.name,
                "motionPreference" to profile.accessibilityPreferences.motionPreference.name,
                "heroAnimationEnabled" to profile.accessibilityPreferences.heroAnimationEnabled
            ),
            "taskRemindersEnabled" to profile.taskRemindersEnabled,
            "academicWorkRemindersEnabled" to profile.academicWorkRemindersEnabled,
            "overdueRemindersEnabled" to profile.overdueRemindersEnabled,
            "gradeInsightRemindersEnabled" to profile.gradeInsightRemindersEnabled,
            "pendingGradeRemindersEnabled" to profile.pendingGradeRemindersEnabled,
            "reminderLeadHours" to profile.reminderLeadHours,
            "dailyDigestEnabled" to profile.dailyDigestEnabled,
            "dailyDigestHour" to profile.dailyDigestHour,
            "dailyDigestMinute" to profile.dailyDigestMinute,
            "quietHoursEnabled" to profile.quietHoursEnabled,
            "quietHoursStartHour" to profile.quietHoursStartHour,
            "quietHoursEndHour" to profile.quietHoursEndHour,
            "weeklyBudget" to profile.weeklyBudget,
            "monthlyBudget" to profile.monthlyBudget,
            "expenseAlertThresholdPercent" to profile.expenseAlertThresholdPercent,
            "enabledExpenseCategories" to profile.enabledExpenseCategories.map { it.name },
            "gradeScenarios" to profile.gradeScenarios.map { scenario ->
                mapOf(
                    "id" to scenario.id,
                    "subjectId" to scenario.subjectId,
                    "subjectName" to scenario.subjectName,
                    "name" to scenario.name,
                    "targetAverage" to scenario.targetAverage,
                    "neededGrade" to scenario.neededGrade,
                    "createdAt" to scenario.createdAt
                )
            }
        )
    }

    private fun subjectMap(subject: Subject): Map<String, Any?> = mapOf(
        "id" to subject.id,
        "name" to subject.name,
        "targetAverage" to subject.targetAverage,
        "visualType" to subject.visualType.name,
        "customColor" to subject.customColor,
        "periodScheme" to cutSchemeMap(subject.cutScheme),
        "activePeriodId" to subject.activeCutId,
        "historyPromptStatus" to subject.historyPromptStatus.name,
        "unknownCutIds" to subject.unknownCutIds.toList(),
        // Sin estos, restaurar deja el historico sin poder colocar ninguna materia.
        "termId" to subject.termId,
        "repeatedFromSubjectId" to subject.repeatedFromSubjectId,
        "grades" to subject.grades.map(::gradeMap)
    )

    private fun gradeMap(grade: GradeItem): Map<String, Any?> = mapOf(
        "id" to grade.id,
        "name" to grade.name,
        "value" to grade.value,
        "percentage" to grade.percentage,
        "type" to grade.type.name,
        "periodId" to grade.cutId,
        "source" to grade.source.name,
        "weightStatus" to grade.weightStatus.name,
        "taskId" to grade.taskId,
        "recordedAt" to grade.recordedAt
    )

    private fun noteMap(note: QuickNote): Map<String, Any?> = mapOf(
        "id" to note.id,
        "body" to note.body,
        "subjectId" to note.subjectId,
        "format" to note.format.name,
        "pinned" to note.pinned,
        "createdAt" to note.createdAt,
        "updatedAt" to note.updatedAt
    )

    private fun parseNotes(value: Any?): List<QuickNote> {
        return asMapList(value).mapNotNull { map ->
            val body = map.string("body") ?: return@mapNotNull null
            if (body.isBlank()) return@mapNotNull null
            val created = map.long("createdAt") ?: System.currentTimeMillis()
            QuickNote(
                id = map.string("id") ?: return@mapNotNull null,
                body = body,
                subjectId = map.string("subjectId"),
                format = map.string("format")
                    ?.let { runCatching { NoteFormat.valueOf(it) }.getOrNull() }
                    ?: NoteFormat.PLAIN,
                pinned = map.boolean("pinned") ?: false,
                createdAt = created,
                updatedAt = map.long("updatedAt") ?: created
            )
        }
    }

    private fun taskMap(task: StudentTask): Map<String, Any?> = mapOf(
        "id" to task.id,
        "title" to task.title,
        "description" to task.description,
        "subjectId" to task.subjectId,
        "type" to task.type.name,
        "dueDateMillis" to task.dueDateMillis,
        "difficulty" to task.difficulty.name,
        "estimatedMinutes" to task.estimatedMinutes,
        "completed" to task.completed,
        "periodId" to task.cutId,
        "gradingStatus" to task.gradingStatus.name,
        "linkedGradeId" to task.linkedGradeId,
        "completedAt" to task.completedAt,
        "createdAt" to task.createdAt,
        "updatedAt" to task.updatedAt
    )

    private fun expenseMap(expense: Expense): Map<String, Any?> = mapOf(
        "id" to expense.id,
        "category" to expense.category.name,
        "amount" to expense.amount,
        "dateMillis" to expense.dateMillis,
        "createdAt" to expense.createdAt,
        "updatedAt" to expense.updatedAt
    )

    private fun academicWorkMap(work: AcademicWork): Map<String, Any?> = mapOf(
        "id" to work.id,
        "templateId" to work.templateId,
        "title" to work.title,
        "subjectId" to work.subjectId,
        "dueDateMillis" to work.dueDateMillis,
        "status" to work.status.name,
        "priority" to work.priority.name,
        "completedChecklistIds" to work.completedChecklistIds.toList(),
        "thesis" to work.thesis,
        "outline" to work.outline,
        "sources" to work.sources,
        "notes" to work.notes,
        "createdAt" to work.createdAt,
        "updatedAt" to work.updatedAt
    )

    private fun classSessionMap(session: ClassSession): Map<String, Any?> = mapOf(
        "id" to session.id,
        "subjectId" to session.subjectId,
        "daysOfWeek" to session.daysOfWeek.toList(),
        "startMinute" to session.startMinute,
        "endMinute" to session.endMinute,
        "location" to session.location,
        "reminderMinutes" to session.reminderMinutes,
        "repeatEveryWeeks" to session.repeatEveryWeeks,
        "recurrenceStartEpochDay" to session.recurrenceStartEpochDay,
        "createdAt" to session.createdAt,
        "updatedAt" to session.updatedAt
    )

    private fun classOccurrenceMap(occurrence: ClassOccurrence): Map<String, Any?> = mapOf(
        "id" to occurrence.id,
        "sessionId" to occurrence.sessionId,
        "dateEpochDay" to occurrence.dateEpochDay,
        "status" to occurrence.status.name,
        "modality" to occurrence.modality.name,
        "absenceReason" to occurrence.absenceReason?.name,
        "note" to occurrence.note,
        "overrideStartMinute" to occurrence.overrideStartMinute,
        "overrideEndMinute" to occurrence.overrideEndMinute,
        "overrideLocation" to occurrence.overrideLocation,
        "updatedAt" to occurrence.updatedAt
    )

    private fun agendaEventMap(event: AgendaEvent): Map<String, Any?> = mapOf(
        "id" to event.id,
        "title" to event.title,
        "notes" to event.notes,
        "kind" to event.kind.name,
        "startMillis" to event.startMillis,
        "endMillis" to event.endMillis,
        "allDay" to event.allDay,
        "location" to event.location,
        "reminderMinutes" to event.reminderMinutes,
        "recurrence" to event.recurrence.name,
        "recurrenceEndEpochDay" to event.recurrenceEndEpochDay,
        "colorArgb" to event.colorArgb,
        "createdAt" to event.createdAt,
        "updatedAt" to event.updatedAt
    )

    private fun parseSubjects(value: Any?): List<Subject> {
        return asMapList(value).mapNotNull { map ->
            val id = map.string("id") ?: return@mapNotNull null
            val name = map.string("name") ?: return@mapNotNull null
            val targetAverage = map.double("targetAverage") ?: return@mapNotNull null
            val visualType = map.string("visualType")
                ?.let { runCatching { SubjectVisualType.valueOf(it) }.getOrNull() }
                ?: SubjectVisualType.TEAL
            val customColor = (map["customColor"] as? Number)?.toInt()
            val grades = parseGrades(map["grades"])
            Subject(
                id = id,
                name = name,
                targetAverage = targetAverage,
                visualType = visualType,
                customColor = customColor,
                grades = grades,
                cutScheme = parseCutScheme(map["periodScheme"]),
                activeCutId = map.string("activePeriodId").orEmpty(),
                historyPromptStatus = map.string("historyPromptStatus")
                    ?.let { runCatching { PriorHistoryPromptStatus.valueOf(it) }.getOrNull() }
                    ?: PriorHistoryPromptStatus.NOT_SHOWN,
                unknownCutIds = (map["unknownCutIds"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    .orEmpty(),
                termId = map.string("termId"),
                repeatedFromSubjectId = map.string("repeatedFromSubjectId")
            )
        }
    }

    private fun parseGrades(value: Any?): List<GradeItem> {
        return asMapList(value).mapNotNull { map ->
            GradeItem(
                id = map.string("id") ?: return@mapNotNull null,
                name = map.string("name") ?: return@mapNotNull null,
                value = map.double("value") ?: return@mapNotNull null,
                percentage = map.double("percentage") ?: return@mapNotNull null,
                type = map.string("type")
                    ?.let { runCatching { GradeType.valueOf(it) }.getOrNull() }
                    ?: GradeType.WORKSHOP,
                cutId = map.string("periodId") ?: "period-1",
                source = map.string("source")
                    ?.let { runCatching { GradeSource.valueOf(it) }.getOrNull() }
                    ?: GradeSource.ACTIVITY,
                weightStatus = map.string("weightStatus")
                    ?.let { runCatching { GradeWeightStatus.valueOf(it) }.getOrNull() }
                    ?: GradeWeightStatus.KNOWN,
                taskId = map.string("taskId"),
                recordedAt = map.long("recordedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun parseTasks(value: Any?): List<StudentTask> {
        return asMapList(value).mapNotNull { map ->
            val difficulty = map.string("difficulty")
                ?.let { runCatching { TaskDifficulty.valueOf(it) }.getOrNull() }
                ?: TaskDifficulty.MEDIUM
            StudentTask(
                id = map.string("id") ?: return@mapNotNull null,
                title = map.string("title") ?: return@mapNotNull null,
                description = map.string("description") ?: "",
                subjectId = map.string("subjectId"),
                type = map.string("type")
                    ?.let { runCatching { TaskType.valueOf(it) }.getOrNull() }
                    ?: TaskType.WORKSHOP,
                dueDateMillis = map.long("dueDateMillis") ?: return@mapNotNull null,
                difficulty = difficulty,
                estimatedMinutes = map.int("estimatedMinutes") ?: return@mapNotNull null,
                completed = map.boolean("completed") ?: false,
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis(),
                cutId = map.string("periodId"),
                gradingStatus = map.string("gradingStatus")
                    ?.let { runCatching { TaskGradingStatus.valueOf(it) }.getOrNull() }
                    ?: TaskGradingStatus.UNDECIDED,
                linkedGradeId = map.string("linkedGradeId"),
                completedAt = map.long("completedAt")
            )
        }
    }

    private fun parseExpenses(value: Any?): List<Expense> {
        return asMapList(value).mapNotNull { map ->
            val category = map.string("category")
                ?.let { runCatching { ExpenseCategory.valueOf(it) }.getOrNull() }
                ?: ExpenseCategory.OTHER
            Expense(
                id = map.string("id") ?: return@mapNotNull null,
                category = category,
                amount = map.int("amount") ?: return@mapNotNull null,
                dateMillis = map.long("dateMillis") ?: return@mapNotNull null,
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun parseAcademicWorks(value: Any?): List<AcademicWork> {
        return asMapList(value).mapNotNull { map ->
            val status = map.string("status")
                ?.let { runCatching { AcademicWorkStatus.valueOf(it) }.getOrNull() }
                ?: AcademicWorkStatus.DRAFT
            val priority = map.string("priority")
                ?.let { runCatching { AcademicWorkPriority.valueOf(it) }.getOrNull() }
                ?: AcademicWorkPriority.MEDIUM
            AcademicWork(
                id = map.string("id") ?: return@mapNotNull null,
                templateId = map.string("templateId") ?: return@mapNotNull null,
                title = map.string("title") ?: return@mapNotNull null,
                subjectId = map.string("subjectId"),
                dueDateMillis = map.long("dueDateMillis"),
                status = status,
                priority = priority,
                completedChecklistIds = (map["completedChecklistIds"] as? List<*>)
                    ?.mapNotNull { it as? String }
                    ?.toSet()
                    .orEmpty(),
                thesis = map.string("thesis").orEmpty(),
                outline = map.string("outline").orEmpty(),
                sources = map.string("sources").orEmpty(),
                notes = map.string("notes").orEmpty(),
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            )
        }
    }

    private fun parseClassSessions(value: Any?): List<ClassSession> {
        return asMapList(value).mapNotNull { map ->
            ClassSession(
                id = map.string("id") ?: return@mapNotNull null,
                subjectId = map.string("subjectId") ?: return@mapNotNull null,
                daysOfWeek = (map["daysOfWeek"] as? List<*>)
                    ?.mapNotNull { (it as? Number)?.toInt() }
                    ?.filter { it in 1..7 }
                    ?.toSet()
                    .orEmpty(),
                startMinute = map.int("startMinute") ?: return@mapNotNull null,
                endMinute = map.int("endMinute") ?: return@mapNotNull null,
                location = map.string("location").orEmpty(),
                reminderMinutes = map.int("reminderMinutes") ?: 15,
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis(),
                repeatEveryWeeks = map.int("repeatEveryWeeks") ?: 1,
                recurrenceStartEpochDay = map.long("recurrenceStartEpochDay") ?: 0L
            ).takeIf { it.isValid }
        }
    }

    private fun parseClassOccurrences(value: Any?): List<ClassOccurrence> {
        return asMapList(value).mapNotNull { map ->
            val sessionId = map.string("sessionId") ?: return@mapNotNull null
            val dateEpochDay = map.long("dateEpochDay") ?: return@mapNotNull null
            ClassOccurrence(
                id = map.string("id") ?: ClassOccurrence.idFor(sessionId, dateEpochDay),
                sessionId = sessionId,
                dateEpochDay = dateEpochDay,
                status = map.string("status")
                    ?.let { runCatching { ClassAttendanceStatus.valueOf(it) }.getOrNull() }
                    ?: ClassAttendanceStatus.PENDING,
                modality = map.string("modality")
                    ?.let { runCatching { ClassModality.valueOf(it) }.getOrNull() }
                    ?: ClassModality.IN_PERSON,
                absenceReason = map.string("absenceReason")
                    ?.let { runCatching { ClassAbsenceReason.valueOf(it) }.getOrNull() },
                note = map.string("note").orEmpty(),
                overrideStartMinute = map.int("overrideStartMinute"),
                overrideEndMinute = map.int("overrideEndMinute"),
                overrideLocation = map.string("overrideLocation"),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            ).takeIf { it.isValid }
        }
    }

    private fun parseAgendaEvents(value: Any?): List<AgendaEvent> {
        return asMapList(value).mapNotNull { map ->
            AgendaEvent(
                id = map.string("id") ?: return@mapNotNull null,
                title = map.string("title").orEmpty(),
                notes = map.string("notes").orEmpty(),
                kind = map.string("kind")?.let { runCatching { AgendaEventKind.valueOf(it) }.getOrNull() }
                    ?: AgendaEventKind.CUSTOM,
                startMillis = map.long("startMillis") ?: return@mapNotNull null,
                endMillis = map.long("endMillis"),
                allDay = map.boolean("allDay") ?: false,
                location = map.string("location").orEmpty(),
                reminderMinutes = map.int("reminderMinutes") ?: 0,
                recurrence = map.string("recurrence")?.let { runCatching { AgendaRecurrence.valueOf(it) }.getOrNull() }
                    ?: AgendaRecurrence.NONE,
                recurrenceEndEpochDay = map.long("recurrenceEndEpochDay"),
                colorArgb = map.int("colorArgb"),
                createdAt = map.long("createdAt") ?: System.currentTimeMillis(),
                updatedAt = map.long("updatedAt") ?: System.currentTimeMillis()
            ).takeIf(AgendaEvent::isValid)
        }
    }

    private fun asMapList(value: Any?): List<Map<String, Any?>> {
        return (value as? List<*>)
            ?.mapNotNull { item ->
                @Suppress("UNCHECKED_CAST")
                item as? Map<String, Any?>
            }
            .orEmpty()
    }

    private fun cutSchemeMap(scheme: GradingCutScheme): Map<String, Any?> = mapOf(
        "periods" to scheme.cuts.map { cut ->
            mapOf(
                "id" to cut.id,
                "name" to cut.name,
                "weight" to cut.weight,
                "order" to cut.order,
                "endEpochDay" to cut.endEpochDay
            )
        }
    )

    private fun parseCutScheme(value: Any?): GradingCutScheme {
        @Suppress("UNCHECKED_CAST")
        val root = value as? Map<String, Any?> ?: return GradingCutScheme.default()
        val cuts = asMapList(root["periods"]).mapIndexedNotNull { index, item ->
            val weight = item.double("weight") ?: return@mapIndexedNotNull null
            GradingCut(
                id = item.string("id") ?: "period-${index + 1}",
                name = item.string("name") ?: "${Corte.Singular} ${index + 1}",
                weight = weight,
                order = item.int("order") ?: index + 1,
                endEpochDay = item.long("endEpochDay")
            )
        }
        return GradingCutScheme(cuts).takeIf { it.isValid }
            ?: GradingCutScheme.default()
    }

    private fun Map<String, Any?>.string(key: String): String? = this[key] as? String
    private fun Map<String, Any?>.boolean(key: String): Boolean? = this[key] as? Boolean
    private fun Map<String, Any?>.double(key: String): Double? = (this[key] as? Number)?.toDouble()
    private fun Map<String, Any?>.long(key: String): Long? = (this[key] as? Number)?.toLong()
    private fun Map<String, Any?>.int(key: String): Int? = (this[key] as? Number)?.toInt()
}
