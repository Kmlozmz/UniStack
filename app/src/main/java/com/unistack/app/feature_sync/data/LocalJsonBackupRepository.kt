package com.unistack.app.feature_sync.data

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
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
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
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
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.AccessibilityPreferences
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.feature_user.domain.VisualPreference
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class LocalJsonBackupRepository(
    private val userRepository: UserRepository,
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository,
    private val scheduleRepository: ScheduleRepository
) : LocalBackupRepository {

    override fun exportBackupJson(): String {
        return JSONObject()
            .put("schemaVersion", SCHEMA_VERSION)
            .put("exportedAt", System.currentTimeMillis())
            .put("profile", profileJson(userRepository.userProfile.value))
            .put("subjects", JSONArray(gradesRepository.subjects.value.map(::subjectJson)))
            .put("tasks", JSONArray(tasksRepository.tasks.value.map(::taskJson)))
            .put("expenses", JSONArray(expensesRepository.expenses.value.map(::expenseJson)))
            .put("academicWorks", JSONArray(academicWorksRepository.works.value.map(::academicWorkJson)))
            .put("classSessions", JSONArray(scheduleRepository.sessions.value.map(::classSessionJson)))
            .put("classOccurrences", JSONArray(scheduleRepository.occurrences.value.map(::classOccurrenceJson)))
            .put("agendaEvents", JSONArray(scheduleRepository.agendaEvents.value.map(::agendaEventJson)))
            .toString(2)
    }

    override fun previewBackupJson(json: String): Result<LocalBackupPreview> = runCatching {
        val root = JSONObject(json)
        check(root.optInt("schemaVersion") in 1..SCHEMA_VERSION) { "Versión de backup no soportada." }
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
            agendaEvents = root.optJSONArray("agendaEvents")?.length() ?: 0
        )
    }

    override fun restoreBackupJson(json: String): Result<LocalBackupPreview> = runCatching {
        val preview = previewBackupJson(json).getOrThrow()
        val root = JSONObject(json)
        restoreProfile(root.optJSONObject("profile"))
        parseSubjects(root.optJSONArray("subjects")).forEach { subject ->
            val existing = gradesRepository.subjects.value.firstOrNull { it.id == subject.id }
            if (existing == null) gradesRepository.addSubject(subject.copy(grades = emptyList())) else gradesRepository.updateSubject(subject.copy(grades = existing.grades))
            subject.grades.forEach { grade ->
                if (existing?.grades?.any { it.id == grade.id } == true) {
                    gradesRepository.updateGrade(subject.id, grade)
                } else {
                    gradesRepository.addGrade(subject.id, grade)
                }
            }
        }
        parseTasks(root.optJSONArray("tasks")).forEach { task ->
            if (tasksRepository.tasks.value.any { it.id == task.id }) tasksRepository.updateTask(task) else tasksRepository.addTask(task)
        }
        parseExpenses(root.optJSONArray("expenses")).forEach { expense ->
            if (expensesRepository.expenses.value.any { it.id == expense.id }) expensesRepository.updateExpense(expense) else expensesRepository.addExpense(expense)
        }
        parseAcademicWorks(root.optJSONArray("academicWorks")).forEach { work ->
            if (academicWorksRepository.works.value.any { it.id == work.id }) academicWorksRepository.updateWork(work) else academicWorksRepository.addWork(work)
        }
        parseClassSessions(root.optJSONArray("classSessions")).forEach(scheduleRepository::saveSession)
        parseClassOccurrences(root.optJSONArray("classOccurrences")).forEach(scheduleRepository::saveOccurrence)
        parseAgendaEvents(root.optJSONArray("agendaEvents")).forEach(scheduleRepository::saveAgendaEvent)
        preview
    }

    override fun exportAcademicReport(): String {
        val profile = userRepository.userProfile.value
        val scale = profile?.gradingScale
        return buildString {
            appendLine("Reporte académico UniStack")
            appendLine("Estudiante: ${profile?.preferredName?.takeIf { it.isNotBlank() } ?: "Estudiante"}")
            appendLine()
            gradesRepository.subjects.value.forEach { subject ->
                val average = GradeCalculator.calculateCurrentAverageByPeriods(
                    subject.grades,
                    subject.periodScheme.periods
                )
                appendLine("${subject.name} · Promedio ${GradingScaleUtils.formatGrade(average, scale ?: profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE)}")
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
        page.canvas.drawText("Reporte académico UniStack", 40f, y, titlePaint)
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

    private fun profileJson(profile: UserProfile?): JSONObject {
        return JSONObject()
            .put("preferredName", profile?.preferredName)
            .put("gradingScale", profile?.gradingScale?.name)
            .put("customGradeMax", profile?.customGradeMax ?: 100.0)
            .put("passingGrade", profile?.passingGrade ?: 3.0)
            .put("targetAverage", profile?.targetAverage ?: 4.0)
            .put("visualPreference", profile?.visualPreference?.name ?: VisualPreference.SYSTEM.name)
            .put(
                "appearancePreferences",
                appearanceJson(profile?.appearancePreferences ?: AppearancePreferences.defaults())
            )
            .put(
                "accessibilityPreferences",
                accessibilityJson(profile?.accessibilityPreferences ?: AccessibilityPreferences())
            )
            .put("academicPeriodScheme", profile?.academicPeriodScheme?.toJsonObject() ?: AcademicPeriodScheme.default().toJsonObject())
            .put("taskRemindersEnabled", profile?.taskRemindersEnabled ?: true)
            .put("academicWorkRemindersEnabled", profile?.academicWorkRemindersEnabled ?: true)
            .put("overdueRemindersEnabled", profile?.overdueRemindersEnabled ?: true)
            .put("gradeInsightRemindersEnabled", profile?.gradeInsightRemindersEnabled ?: true)
            .put("pendingGradeRemindersEnabled", profile?.pendingGradeRemindersEnabled ?: true)
            .put("reminderLeadHours", profile?.reminderLeadHours ?: 24)
            .put("dailyDigestEnabled", profile?.dailyDigestEnabled ?: true)
            .put("dailyDigestHour", profile?.dailyDigestHour ?: 7)
            .put("dailyDigestMinute", profile?.dailyDigestMinute ?: 30)
            .put("quietHoursEnabled", profile?.quietHoursEnabled ?: false)
            .put("quietHoursStartHour", profile?.quietHoursStartHour)
            .put("quietHoursEndHour", profile?.quietHoursEndHour)
            .put("weeklyBudget", profile?.weeklyBudget ?: 0)
            .put("monthlyBudget", profile?.monthlyBudget ?: 0)
            .put("expenseAlertThresholdPercent", profile?.expenseAlertThresholdPercent ?: 80)
            .put("enabledExpenseCategories", JSONArray(profile?.enabledExpenseCategories?.map { it.name }.orEmpty()))
            .put("enabledModules", JSONArray(profile?.enabledModules?.map { it.name }.orEmpty()))
    }

    private fun restoreProfile(profileJson: JSONObject?) {
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
                gradingScale = profileJson.optString("gradingScale").toGradingScaleOrNull() ?: current.gradingScale,
                customGradeMax = profileJson.optDouble("customGradeMax", current.customGradeMax).coerceIn(1.0, 100.0),
                passingGrade = profileJson.optDouble("passingGrade", current.passingGrade),
                targetAverage = profileJson.optDouble("targetAverage", current.targetAverage),
                visualPreference = profileJson.optString("visualPreference")
                    .toEnum(current.visualPreference),
                appearancePreferences = parseAppearance(
                    profileJson.optJSONObject("appearancePreferences"),
                    current.appearancePreferences
                ),
                accessibilityPreferences = parseAccessibility(
                    profileJson.optJSONObject("accessibilityPreferences"),
                    current.accessibilityPreferences
                ),
                academicPeriodScheme = profileJson.optJSONObject("academicPeriodScheme").toAcademicPeriodSchemeOrNull()
                    ?: current.academicPeriodScheme,
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
                weeklyBudget = profileJson.optInt("weeklyBudget", current.weeklyBudget),
                monthlyBudget = profileJson.optInt("monthlyBudget", current.monthlyBudget),
                expenseAlertThresholdPercent = profileJson.optInt("expenseAlertThresholdPercent", current.expenseAlertThresholdPercent),
                enabledExpenseCategories = expenseCategories,
                enabledModules = modules,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun appearanceJson(value: AppearancePreferences): JSONObject = JSONObject()
        .put("backgroundStyle", value.backgroundStyle.name)
        .put("customBackgroundColor", value.customBackgroundColor)
        .put("customThemeBase", value.customThemeBase.name)
        .put("accentStyle", value.accentStyle.name)
        .put("customAccentColor", value.customAccentColor)
        .put("accentIntensity", value.accentIntensity.name)
        .put("surfaceStyle", value.surfaceStyle.name)
        .put("cornerStyle", value.cornerStyle.name)
        .put("interfaceDensity", value.interfaceDensity.name)
        .put("motionPreference", value.motionPreference.name)
        .put("textScale", value.textScale.name)
        .put("typographyStyle", value.typographyStyle.name)
        .put("decimalPlaces", value.decimalPlaces)
        .put("bottomBarStyle", value.bottomBarStyle.name)
        .put("academicIndicatorStyle", value.academicIndicatorStyle.name)
        .put("showHomeGreeting", value.showHomeGreeting)
        .put("showHomeHero", value.showHomeHero)
        .put("showHomeAgenda", value.showHomeAgenda)
        .put("showHomeSnapshot", value.showHomeSnapshot)
        .put("homeSectionOrder", JSONArray(value.homeSectionOrder.map { it.name }))
        .put("heroAutoRotate", value.heroAutoRotate)
        .put("heroShowsGrades", value.heroShowsGrades)
        .put("heroShowsTasks", value.heroShowsTasks)
        .put("heroShowsExpenses", value.heroShowsExpenses)
        .put("initialTab", value.initialTab.name)
        .put("visualPreset", value.visualPreset.name)

    private fun accessibilityJson(value: AccessibilityPreferences): JSONObject = JSONObject()
        .put("appLanguage", value.appLanguage.name)
        .put("highContrastEnabled", value.highContrastEnabled)
        .put("use24HourTime", value.use24HourTime)
        .put("textScale", value.textScale.name)
        .put("motionPreference", value.motionPreference.name)
        .put("heroAnimationEnabled", value.heroAnimationEnabled)

    private fun parseAccessibility(
        json: JSONObject?,
        current: AccessibilityPreferences
    ): AccessibilityPreferences {
        if (json == null) return current
        return AccessibilityPreferences(
            appLanguage = json.optString("appLanguage").toEnum(current.appLanguage),
            highContrastEnabled = json.optBoolean("highContrastEnabled", current.highContrastEnabled),
            use24HourTime = json.optBoolean("use24HourTime", current.use24HourTime),
            textScale = json.optString("textScale").toEnum(current.textScale),
            motionPreference = json.optString("motionPreference").toEnum(current.motionPreference),
            heroAnimationEnabled = json.optBoolean("heroAnimationEnabled", current.heroAnimationEnabled)
        )
    }

    private fun parseAppearance(
        json: JSONObject?,
        current: AppearancePreferences
    ): AppearancePreferences {
        if (json == null) return current
        return AppearancePreferences(
            backgroundStyle = json.optString("backgroundStyle").toEnum(current.backgroundStyle),
            customBackgroundColor = json.optIntOrNull("customBackgroundColor"),
            customThemeBase = json.optString("customThemeBase").toEnum(current.customThemeBase),
            accentStyle = json.optString("accentStyle").toEnum(current.accentStyle),
            customAccentColor = json.optIntOrNull("customAccentColor"),
            accentIntensity = json.optString("accentIntensity").toEnum(current.accentIntensity),
            surfaceStyle = json.optString("surfaceStyle").toEnum(current.surfaceStyle),
            cornerStyle = json.optString("cornerStyle").toEnum(current.cornerStyle),
            interfaceDensity = json.optString("interfaceDensity").toEnum(current.interfaceDensity),
            motionPreference = json.optString("motionPreference").toEnum(current.motionPreference),
            textScale = json.optString("textScale").toEnum(current.textScale),
            typographyStyle = json.optString("typographyStyle").toEnum(current.typographyStyle),
            decimalPlaces = json.optInt("decimalPlaces", current.decimalPlaces),
            bottomBarStyle = json.optString("bottomBarStyle").toEnum(current.bottomBarStyle),
            academicIndicatorStyle = json.optString("academicIndicatorStyle")
                .toEnum(current.academicIndicatorStyle),
            showHomeGreeting = json.optBoolean("showHomeGreeting", current.showHomeGreeting),
            showHomeHero = json.optBoolean("showHomeHero", current.showHomeHero),
            showHomeAgenda = json.optBoolean("showHomeAgenda", current.showHomeAgenda),
            showHomeSnapshot = json.optBoolean("showHomeSnapshot", current.showHomeSnapshot),
            homeSectionOrder = json.optJSONArray("homeSectionOrder")
                .strings()
                .mapNotNull { it.toEnumOrNull<HomeSection>() }
                .ifEmpty { current.homeSectionOrder },
            heroAutoRotate = json.optBoolean("heroAutoRotate", current.heroAutoRotate),
            heroShowsGrades = json.optBoolean("heroShowsGrades", current.heroShowsGrades),
            heroShowsTasks = json.optBoolean("heroShowsTasks", current.heroShowsTasks),
            heroShowsExpenses = json.optBoolean("heroShowsExpenses", current.heroShowsExpenses),
            initialTab = json.optString("initialTab").toEnum(current.initialTab),
            visualPreset = json.optString("visualPreset").toEnum(current.visualPreset)
        ).normalized()
    }

    private fun AcademicPeriodScheme.toJsonObject(): JSONObject {
        return JSONObject()
            .put(
                "periods",
                JSONArray(
                    periods.sortedBy { it.order }.map { period ->
                        JSONObject()
                            .put("id", period.id)
                            .put("name", period.name)
                            .put("weight", period.weight)
                            .put("order", period.order)
                    }
                )
            )
    }

    private fun JSONObject?.toAcademicPeriodSchemeOrNull(): AcademicPeriodScheme? {
        val root = this ?: return null
        val periodsArray = root.optJSONArray("periods") ?: return null
        val periods = periodsArray.objects()
            .mapIndexedNotNull { index, item ->
                val order = item.optInt("order", index + 1)
                val weight = item.optDouble("weight", 0.0)
                if (weight <= 0.0) return@mapIndexedNotNull null
                AcademicPeriod(
                    id = item.optString("id", "period-$order"),
                    name = item.optString("name", "${Corte.Singular} $order"),
                    weight = weight,
                    order = order
                )
            }
            .sortedBy { it.order }
        return AcademicPeriodScheme(periods = periods).takeIf { it.isValid }
    }

    private fun String.toGradingScaleOrNull(): GradingScale? {
        return when (this) {
            "ZERO_TO_ONE_HUNDRED" -> GradingScale.ZERO_TO_HUNDRED
            "ZERO_TO_TEN", "LETTERS" -> GradingScale.CUSTOM
            else -> runCatching { GradingScale.valueOf(this) }.getOrNull()
        }
    }

    private fun subjectJson(subject: Subject): JSONObject = JSONObject()
        .put("id", subject.id)
        .put("name", subject.name)
        .put("targetAverage", subject.targetAverage)
        .put("visualType", subject.visualType.name)
        .put("customColor", subject.customColor)
        .put("periodScheme", subject.periodScheme.toJsonObject())
        .put("activePeriodId", subject.activePeriodId)
        .put("historyPromptStatus", subject.historyPromptStatus.name)
        .put("unknownPeriodIds", JSONArray(subject.unknownPeriodIds.toList()))
        .put("grades", JSONArray(subject.grades.map(::gradeJson)))

    private fun gradeJson(grade: GradeItem): JSONObject = JSONObject()
        .put("id", grade.id)
        .put("name", grade.name)
        .put("value", grade.value)
        .put("percentage", grade.percentage)
        .put("type", grade.type.name)
        .put("periodId", grade.periodId)
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
        .put("periodId", task.periodId)
        .put("gradingStatus", task.gradingStatus.name)
        .put("linkedGradeId", task.linkedGradeId)
        .put("completedAt", task.completedAt)
        .put("createdAt", task.createdAt)
        .put("updatedAt", task.updatedAt)

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

    private fun parseSubjects(array: JSONArray?): List<Subject> = array.objects().mapNotNull { item ->
        Subject(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            targetAverage = item.optDouble("targetAverage"),
            visualType = item.optString("visualType").toEnum(SubjectVisualType.TEAL),
            customColor = if (item.isNull("customColor")) null else item.optInt("customColor"),
            grades = parseGrades(item.optJSONArray("grades")),
            periodScheme = item.optJSONObject("periodScheme").toAcademicPeriodSchemeOrNull()
                ?: AcademicPeriodScheme.default(),
            activePeriodId = item.optString("activePeriodId", ""),
            historyPromptStatus = item.optString("historyPromptStatus")
                .toEnum(PriorHistoryPromptStatus.NOT_SHOWN),
            unknownPeriodIds = item.optJSONArray("unknownPeriodIds").strings().toSet()
        )
    }

    private fun parseGrades(array: JSONArray?): List<GradeItem> = array.objects().mapNotNull { item ->
        GradeItem(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            name = item.optString("name").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            value = item.optDouble("value"),
            percentage = item.optDouble("percentage"),
            type = item.optString("type").toEnum(GradeType.WORKSHOP),
            periodId = item.optString("periodId", "period-1").ifBlank { "period-1" },
            source = item.optString("source").toEnum(GradeSource.ACTIVITY),
            weightStatus = item.optString("weightStatus").toEnum(GradeWeightStatus.KNOWN),
            taskId = item.optNullableString("taskId"),
            recordedAt = item.optLong("recordedAt", System.currentTimeMillis())
        )
    }

    private fun parseTasks(array: JSONArray?): List<StudentTask> = array.objects().mapNotNull { item ->
        StudentTask(
            id = item.optString("id").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            title = item.optString("title").takeIf { it.isNotBlank() } ?: return@mapNotNull null,
            description = item.optString("description", ""),
            subjectId = item.optNullableString("subjectId"),
            type = item.optString("type").toEnum(TaskType.WORKSHOP),
            dueDateMillis = item.optLong("dueDateMillis"),
            difficulty = item.optString("difficulty").toEnum(TaskDifficulty.MEDIUM),
            estimatedMinutes = item.optInt("estimatedMinutes"),
            completed = item.optBoolean("completed"),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
            periodId = item.optNullableString("periodId"),
            gradingStatus = item.optString("gradingStatus").toEnum(TaskGradingStatus.UNDECIDED),
            linkedGradeId = item.optNullableString("linkedGradeId"),
            completedAt = if (item.isNull("completedAt")) null else item.optLong("completedAt")
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

    private inline fun <reified T : Enum<T>> String.toEnum(default: T): T = runCatching { enumValueOf<T>(this) }.getOrDefault(default)
    private inline fun <reified T : Enum<T>> String.toEnumOrNull(): T? = runCatching { enumValueOf<T>(this) }.getOrNull()
    private fun JSONObject.optNullableString(key: String): String? = if (isNull(key)) null else optString(key).takeIf { it.isNotBlank() }
    private fun JSONObject.optIntOrNull(key: String): Int? = if (isNull(key) || !has(key)) null else optInt(key)
    private fun String.csvEscape(): String = "\"${replace("\"", "\"\"")}\""

    private companion object {
        const val SCHEMA_VERSION = 10
    }
}
