package com.unistack.app.feature_home.presentation

import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.DailyPriorityEngine
import com.unistack.app.feature_home.domain.DailyFocusItem
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomePriorityAction
import com.unistack.app.feature_home.domain.HomePrioritySummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.HomeTimelineKind
import com.unistack.app.feature_home.domain.HomeTimelineState
import com.unistack.app.feature_home.domain.HomeTimelineSummary
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import com.unistack.app.feature_home.domain.HomePriorityTimeframe

internal object HomeSummaryFactory {
    fun create(
        content: HomeContent,
        profile: UserProfile?,
        user: AppUser
    ): HomeSummary {
        val subjects = content.subjects
        val tasks = content.tasks
        val expenses = content.expenses
        val works = content.works
        val classSessions = content.classSessions
        val pendingTasks = tasks.filterNot { it.completed }
        val completedTasks = tasks.count { it.completed }
        val overdueTasks = pendingTasks.count {
            TaskDateUtils.fromMillis(it.dueDateMillis).isBefore(TaskDateUtils.today())
        }
        val nextTask = pendingTasks.nextTaskSummary()
        val weeklyExpenses = weeklyExpenseSummary(expenses)
        val weeklyExpenseTotal = weeklyExpenses?.total ?: 0
        val nextAcademicWork = works.nextAcademicWorkSummary()
        val openAcademicWorks = works.count { it.status != AcademicWorkStatus.SUBMITTED }
        val gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
        val enabledModules = profile?.enabledModules ?: setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        val appearance = profile?.appearancePreferences
        val heroSubjects = if (appearance?.heroShowsGrades != false) subjects else emptyList()
        val heroTasks = if (appearance?.heroShowsTasks != false) pendingTasks else emptyList()
        val heroWorks = if (appearance?.heroShowsTasks != false) works else emptyList()
        val heroExpenseTotal = if (appearance?.heroShowsExpenses != false) weeklyExpenseTotal else 0
        val riskSubject = subjectRiskSummary(
            subjects = heroSubjects,
            profile = profile,
            gradingScale = gradingScale
        )
        val academicFocus = academicFocusSummary(
            subjects = heroSubjects,
            gradingScale = gradingScale
        )
        val academicDataPriority = academicDataPriority(
            subjects = heroSubjects,
            tasks = heroTasks
        )
        val schedulePriority = schedulePrioritySummary(
            sessions = classSessions,
            subjects = subjects
        )
        val todayItems = todayTimelineItems(
            tasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            subjectNameById = subjects.associate { it.id to it.name }
        )
        val priority = academicDataPriority ?: schedulePriority ?: prioritySummary(
            subjects = heroSubjects,
            pendingTasks = heroTasks,
            works = heroWorks,
            riskSubject = riskSubject,
            weeklyExpenseTotal = heroExpenseTotal,
            profile = profile,
            enabledModules = enabledModules,
            academicFocus = academicFocus
        )
        val generatedFocusItems = DailyPriorityEngine.dailyFocusPlan(
            subjectsCount = subjects.size,
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        )
        val dailyFocusItems = when {
            academicDataPriority != null -> listOf(academicDataPriority.toDailyFocusItem()) +
                generatedFocusItems
                    .filterNot { it.action == academicDataPriority.action && it.subjectId == academicDataPriority.subjectId }
                    .take(2)
            generatedFocusItems.isGenericCalmPlan() && academicFocus != null -> academicFocus.toDailyFocusItems()
            else -> generatedFocusItems
        }

        return HomeSummary(
            userName = profile?.preferredName?.takeIf { it.isNotBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                ?: "Estudiante",
            avatarPhotoUrl = user.photoUrl,
            dashboardMessage = dashboardMessage(
                hasSubjects = subjects.isNotEmpty(),
                overdueTasks = overdueTasks,
                riskSubject = riskSubject,
                nextTask = nextTask,
                nextAcademicWork = nextAcademicWork,
                weeklyExpenseTotal = weeklyExpenseTotal
            ),
            priority = priority,
            dailyFocusItems = dailyFocusItems,
            generalAverage = generalAverage(subjects),
            subjectsCount = subjects.size,
            tasksToday = pendingTasks.count { TaskDateUtils.isToday(it.dueDateMillis) },
            overdueTasks = overdueTasks,
            pendingTasks = pendingTasks.size,
            openAcademicWorks = openAcademicWorks,
            subjects = subjects.take(3).map(::subjectSummary),
            riskSubject = riskSubject,
            neededGrade = neededGradeSummary(subjects, profile),
            nextTask = nextTask,
            nextAcademicWork = nextAcademicWork,
            todayItems = todayItems,
            weeklyExpenses = weeklyExpenses,
            weeklyExpenseTotal = weeklyExpenseTotal,
            productivitySummary = productivitySummary(completedTasks, pendingTasks.size, overdueTasks),
            companionInsight = companionInsight(
                userName = profile?.preferredName?.takeIf { it.isNotBlank() }
                    ?: user.displayName?.takeIf { it.isNotBlank() }
                    ?: "Estudiante",
                priority = priority,
                pendingTasks = pendingTasks.size,
                overdueTasks = overdueTasks,
                todayItems = todayItems
            ),
            gradingScale = gradingScale,
            enabledModules = enabledModules
        )
    }

    private fun subjectSummary(subject: Subject): SubjectSummary {
        val periods = subject.periodScheme.periods
        val evaluatedPercentage =
            GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, periods) / 100.0
        return SubjectSummary(
            id = subject.id,
            name = subject.name,
            average = GradeCalculator.calculateCurrentAverageByPeriods(subject.grades, periods),
            targetAverage = subject.targetAverage,
            progress = evaluatedPercentage.toFloat(),
            type = subject.visualType
        )
    }

    private fun generalAverage(subjects: List<Subject>): Double? {
        val subjectsWithGrades = subjects.filter { it.grades.isNotEmpty() }
        if (subjectsWithGrades.isEmpty()) return null

        val validGrades = subjectsWithGrades.mapNotNull { subject ->
            GradeCalculator.calculateCurrentAverageByPeriods(
                subject.grades,
                subject.periodScheme.periods
            )?.let { average ->
                GradeItem(
                    id = subject.id,
                    name = subject.name,
                    value = average,
                    percentage = 1.0 / subjectsWithGrades.size
                )
            }
        }
        return validGrades.takeIf { it.isNotEmpty() }?.let(GradeCalculator::calculateCurrentAverage)
    }

    private fun neededGradeSummary(subjects: List<Subject>, profile: UserProfile?): NeededGradeSummary? {
        val focusSubject = subjects.firstOrNull { it.grades.isNotEmpty() } ?: return null
        val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
        val calculation = GradeCalculator.calculateSubject(
            grades = focusSubject.grades,
            periods = focusSubject.periodScheme.periods,
            targetAverage = focusSubject.targetAverage,
            maxGrade = maxGrade
        )

        return calculation.neededForTarget
            ?.takeIf { it > 0.0 && it <= maxGrade }
            ?.let {
                NeededGradeSummary(
                    subjectName = focusSubject.name,
                    targetAverage = focusSubject.targetAverage,
                    neededGrade = it
                )
            }
    }

    private fun List<StudentTask>.nextTaskSummary(): TaskSummary? {
        return minByOrNull { it.dueDateMillis }?.let { task ->
            TaskSummary(
                id = task.id,
                title = task.title,
                dueText = TaskDateUtils.dueText(task.dueDateMillis),
                estimatedTimeText = TaskDateUtils.estimatedTimeText(task.estimatedMinutes)
            )
        }
    }

    private fun List<AcademicWork>.nextAcademicWorkSummary(): AcademicWorkSummary? {
        return filterNot { it.status == AcademicWorkStatus.SUBMITTED }
            .minWithOrNull(compareBy<AcademicWork> { it.dueDateMillis ?: Long.MAX_VALUE }.thenByDescending { it.priority.ordinal })
            ?.let { work ->
                AcademicWorkSummary(
                    id = work.id,
                    subjectId = work.subjectId,
                    title = work.title,
                    dueText = work.dueDateMillis?.let(TaskDateUtils::dueText) ?: "sin fecha",
                    progress = work.checklistProgress
                )
            }
    }

    private fun prioritySummary(
        subjects: List<Subject>,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>,
        academicFocus: AcademicFocusSummary?
    ): HomePrioritySummary {
        DailyPriorityEngine.primaryPriority(
            subjectsCount = subjects.size,
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        )?.let { return it }

        if (subjects.isEmpty()) {
            return HomePrioritySummary(
                title = "Prepara tu semestre",
                shortDescription = "Agrega tus materias para activar prioridades reales.",
                action = HomePriorityAction.SUBJECTS
            )
        }

        return calmPrioritySummary(
            subjects = subjects,
            pendingTasks = pendingTasks,
            works = works,
            weeklyExpenseTotal = weeklyExpenseTotal,
            enabledModules = enabledModules,
            academicFocus = academicFocus
        )
    }

    private fun academicDataPriority(
        subjects: List<Subject>,
        tasks: List<StudentTask>
    ): HomePrioritySummary? {
        val waitingResults = tasks.filter {
            it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
        }
        if (waitingResults.isNotEmpty()) {
            val next = waitingResults.maxByOrNull { it.completedAt ?: it.updatedAt }
            return HomePrioritySummary(
                title = if (waitingResults.size == 1) {
                    "${next?.title.orEmpty()} espera su nota"
                } else {
                    "${waitingResults.size} resultados esperan registro"
                },
                shortDescription = "Registra la calificación o indica que la actividad no tuvo nota.",
                action = HomePriorityAction.TASKS,
                subjectId = next?.subjectId
            )
        }

        val incompleteHistory = subjects.firstOrNull { subject ->
            val activeOrder = subject.periodScheme.periods
                .firstOrNull { it.id == subject.activePeriodId }
                ?.order
                ?: 1
            activeOrder > 1 &&
                subject.historyPromptStatus != com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus.COMPLETED &&
                subject.historyPromptStatus != com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus.DISMISSED &&
                subject.periodScheme.periods
                    .filter { it.order < activeOrder }
                    .any { period ->
                        period.id !in subject.unknownPeriodIds &&
                            subject.grades.none { it.periodId == period.id }
                    }
        }
        if (incompleteHistory != null) {
            return HomePrioritySummary(
                title = "Completa el historial de ${incompleteHistory.name}",
                shortDescription = "Faltan datos de cortes anteriores para calcular una proyección fiable.",
                action = HomePriorityAction.SUBJECT,
                subjectId = incompleteHistory.id
            )
        }

        val subjectWithUnknownWeights = subjects.firstOrNull { subject ->
            subject.grades.any {
                it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
            }
        }
        if (subjectWithUnknownWeights != null) {
            val count = subjectWithUnknownWeights.grades.count {
                it.source == GradeSource.ACTIVITY && it.weightStatus == GradeWeightStatus.UNKNOWN
            }
            return HomePrioritySummary(
                title = "Ajusta ${subjectWithUnknownWeights.name}",
                shortDescription = if (count == 1) {
                    "Hay una nota sin porcentaje; la proyección todavía es provisional."
                } else {
                    "Hay $count notas sin porcentaje; la proyección todavía es provisional."
                },
                action = HomePriorityAction.SUBJECT,
                subjectId = subjectWithUnknownWeights.id
            )
        }

        return null
    }

    private fun schedulePrioritySummary(
        sessions: List<ClassSession>,
        subjects: List<Subject>
    ): HomePrioritySummary? {
        if (sessions.isEmpty()) return null
        val today = LocalDate.now()
        val nowMinute = LocalTime.now().hour * 60 + LocalTime.now().minute
        val next = (0..7)
            .flatMap { offset ->
                val date = today.plusDays(offset.toLong())
                sessions
                    .filter { date.dayOfWeek.value in it.daysOfWeek }
                    .filter { offset > 0 || it.startMinute >= nowMinute }
                    .map { date to it }
            }
            .sortedWith(compareBy<Pair<LocalDate, ClassSession>> { it.first }.thenBy { it.second.startMinute })
            .firstOrNull() ?: return null
        val (date, session) = next
        val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: "Tu proxima clase"
        val dayText = when (date) {
            today -> "hoy"
            today.plusDays(1) -> "mañana"
            else -> "el " + date.dayOfWeek.getDisplayName(
                java.time.format.TextStyle.FULL,
                java.util.Locale.forLanguageTag("es")
            )
        }
        val timeframe = when (date) {
            today -> HomePriorityTimeframe.TODAY
            today.plusDays(1) -> HomePriorityTimeframe.TOMORROW
            else -> HomePriorityTimeframe.LATER
        }
        val timeText = formatClassMinute(session.startMinute)
        return HomePrioritySummary(
            title = "$subjectName a las $timeText",
            shortDescription = "Tienes clase $dayText. Revisa aula, asistencia y recordatorio.",
            action = HomePriorityAction.SCHEDULE,
            subjectId = session.subjectId,
            timeframe = timeframe
        )
    }

    private fun formatClassMinute(minute: Int): String = TaskDateUtils.formatTimeInput(
        LocalTime.of((minute / 60).coerceIn(0, 23), (minute % 60).coerceIn(0, 59))
    )
    private fun HomePrioritySummary.toDailyFocusItem(): DailyFocusItem {
        return DailyFocusItem(
            slotLabel = "Ahora",
            title = title,
            detail = shortDescription,
            minutesText = when (action) {
                HomePriorityAction.TASKS -> "3 min"
                HomePriorityAction.SUBJECT -> "5 min"
                else -> "5 min"
            },
            actionLabel = when (action) {
                HomePriorityAction.SUBJECT -> "Abrir"
                HomePriorityAction.SUBJECTS -> "Materias"
                HomePriorityAction.TASKS -> "Revisar"
                HomePriorityAction.EXPENSES -> "Gastos"
                HomePriorityAction.TEMPLATES -> "Trabajos"
                HomePriorityAction.SCHEDULE -> "Horario"
            },
            action = action,
            subjectId = subjectId
        )
    }

    private fun calmPrioritySummary(
        subjects: List<Subject>,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        weeklyExpenseTotal: Int,
        enabledModules: Set<AppModule>,
        academicFocus: AcademicFocusSummary?
    ): HomePrioritySummary {
        val index = TaskDateUtils.today().dayOfYear % 4
        val canUseExpenses = AppModule.EXPENSES in enabledModules && weeklyExpenseTotal > 0
        // Trabajos está apagado fuera de dev y alpha, así que tampoco se propone desde Inicio:
        // una sugerencia que lleva a una pantalla cerrada es peor que ninguna sugerencia.
        val hasOpenWorks = BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished &&
            works.any { it.status != AcademicWorkStatus.SUBMITTED }
        return when {
            canUseExpenses && index == 1 -> HomePrioritySummary(
                title = "Gastos bajo control",
                shortDescription = "Buen momento para revisar si tu semana sigue en ritmo.",
                action = HomePriorityAction.EXPENSES
            )
            hasOpenWorks && index == 2 -> HomePrioritySummary(
                title = "Espacio para avanzar",
                shortDescription = "Aprovecha un bloque corto para mover un trabajo.",
                action = HomePriorityAction.TEMPLATES
            )
            pendingTasks.isNotEmpty() && index == 3 -> HomePrioritySummary(
                title = "Buen ritmo",
                shortDescription = "Ordena una tarea pequeña y deja el día más liviano.",
                action = HomePriorityAction.TASKS
            )
            academicFocus != null -> academicFocus.toPrioritySummary()
            else -> HomePrioritySummary(
                title = "Día despejado",
                shortDescription = "Aprovecha para repasar o preparar tus próximas notas.",
                action = if (subjects.isNotEmpty()) HomePriorityAction.SUBJECTS else HomePriorityAction.TASKS
            )
        }
    }

    private fun academicFocusSummary(
        subjects: List<Subject>,
        gradingScale: GradingScale
    ): AcademicFocusSummary? {
        if (subjects.isEmpty()) return null
        return subjects
            .map { subject ->
                val periods = subject.periodScheme.periods
                val average = GradeCalculator.calculateCurrentAverageByPeriods(subject.grades, periods)
                val evaluated = GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, periods)
                AcademicFocusSummary(
                    subjectId = subject.id,
                    subjectName = subject.name,
                    average = average,
                    targetAverage = subject.targetAverage,
                    evaluatedPercentage = evaluated.coerceIn(0.0, 100.0),
                    gradingScale = gradingScale
                )
            }
            .maxWithOrNull(
                compareBy<AcademicFocusSummary> { it.average != null }
                    .thenBy { it.remainingPercentage }
                    .thenByDescending { it.average ?: 0.0 }
            )
    }

    private fun List<DailyFocusItem>.isGenericCalmPlan(): Boolean {
        return any { it.title == "Repaso breve" || it.title == "Ordenar pendientes" }
    }

    private fun AcademicFocusSummary.toPrioritySummary(): HomePrioritySummary {
        val averageText = average?.let { GradingScaleUtils.formatGrade(it, gradingScale) }
        return HomePrioritySummary(
            title = if (averageText != null) "$subjectName va en $averageText" else "$subjectName espera su primera nota",
            shortDescription = if (averageText != null) {
                "${evaluatedPercentage.roundPercent()}% evaluado. Falta registrar ${remainingPercentage.roundPercent()}%."
            } else {
                "Agrega una nota para activar proyección y seguimiento real."
            },
            action = HomePriorityAction.SUBJECT,
            subjectId = subjectId
        )
    }

    private fun AcademicFocusSummary.toDailyFocusItems(): List<DailyFocusItem> {
        val hasGrade = average != null
        return listOf(
            DailyFocusItem(
                slotLabel = "Ahora",
                title = if (hasGrade) "Actualizar $subjectName" else "Agregar primera nota",
                detail = if (hasGrade) {
                    "Registra la próxima nota o revisa el ${remainingPercentage.roundPercent()}% restante."
                } else {
                    "Convierte esta materia en un tablero con promedio real."
                },
                minutesText = if (hasGrade) "5 min" else "3 min",
                actionLabel = "Abrir",
                action = HomePriorityAction.SUBJECT,
                subjectId = subjectId
            )
        )
    }

    private fun Double.roundPercent(): String = "%.0f".format(this)

    private fun heroActionPrefix(): String {
        return when (LocalTime.now().hour) {
            in 5..11 -> "Arranca con"
            in 18..23 -> "Deja listo"
            else -> "Siguiente paso"
        }
    }

    private fun todayTimelineItems(
        tasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        subjectNameById: Map<String, String>
    ): List<HomeTimelineSummary> {
        val taskItems = tasks
            .filter { it.isRelevantForToday() }
            .sortedWith(compareBy<StudentTask> { it.dueDateMillis }.thenByDescending { it.difficulty.rank() })
            .take(3)
            .map { task ->
                HomeTimelineSummary(
                    timeText = task.timelineTimeText(),
                    title = task.title,
                    subtitle = "${task.type.label()} · ${TaskDateUtils.estimatedTimeText(task.estimatedMinutes)}",
                    kind = task.type.timelineKind(),
                    state = if (task.isDueTodayOrOverdue()) HomeTimelineState.CURRENT else HomeTimelineState.PENDING
                )
            }

        val workItems = works
            .filterNot { it.status == AcademicWorkStatus.SUBMITTED }
            .filter { it.isRelevantForToday() }
            .sortedWith(compareBy<AcademicWork> { it.dueDateMillis ?: Long.MAX_VALUE }.thenByDescending { it.priority.rank() })
            .take(3)
            .map { work ->
                val subject = work.subjectId?.let(subjectNameById::get)
                val progress = (work.checklistProgress * 100).toInt().coerceIn(0, 100)
                HomeTimelineSummary(
                    timeText = work.timelineTimeText(),
                    title = work.title,
                    subtitle = listOfNotNull(subject, "$progress% listo").joinToString(" · "),
                    kind = HomeTimelineKind.WORK,
                    state = if (work.isDueTodayOrOverdue()) HomeTimelineState.CURRENT else HomeTimelineState.PENDING
                )
            }

        val focusItem = riskSubject
            ?.takeIf { it.severity != SubjectRiskSeverity.STABLE }
            ?.let {
                HomeTimelineSummary(
                    timeText = "Enfoque",
                    title = it.subjectName,
                    subtitle = it.detail,
                    kind = HomeTimelineKind.FOCUS,
                    state = if (it.severity == SubjectRiskSeverity.CRITICAL) HomeTimelineState.CURRENT else HomeTimelineState.PENDING
                )
            }

        return (taskItems + workItems + listOfNotNull(focusItem))
            .distinctBy { it.kind to it.title }
            .sortedWith(
                compareBy<HomeTimelineSummary> { item ->
                    when (item.state) {
                        HomeTimelineState.CURRENT -> 0
                        HomeTimelineState.PENDING -> 1
                        HomeTimelineState.DONE -> 2
                    }
                }.thenBy { it.timeText }
            )
            .take(3)
    }

    private fun weeklyExpenseSummary(expenses: List<Expense>): ExpenseSummary? {
        val weekly = expenses.filter { ExpenseDateUtils.isInCurrentWeek(it.dateMillis) }
        if (weekly.isEmpty()) return null

        val start = ExpenseDateUtils.startOfWeek()
        val chartValues = (0..6).map { dayOffset ->
            val date = start.plusDays(dayOffset.toLong())
            weekly
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == date }
                .sumOf { it.amount }
        }

        return ExpenseSummary(
            transport = weekly.filter { it.category == ExpenseCategory.TRANSPORT }.sumOf { it.amount },
            food = weekly.filter { it.category == ExpenseCategory.FOOD }.sumOf { it.amount },
            total = weekly.sumOf { it.amount },
            chartValues = chartValues
        )
    }

    private fun subjectRiskSummary(
        subjects: List<Subject>,
        profile: UserProfile?,
        gradingScale: GradingScale
    ): SubjectRiskSummary? {
        val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: GradingScaleUtils.maxGradeFor(gradingScale)
        val passingGrade = profile?.passingGrade ?: maxGrade * 0.6
        val candidates = subjects.mapNotNull { subject ->
            val calculation = GradeCalculator.calculateSubject(
                grades = subject.grades,
                periods = subject.periodScheme.periods,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
            val average = calculation.currentAverage ?: return@mapNotNull null
            val remainingPercentage = calculation.remainingSemesterFraction
            val needed = calculation.neededForTarget
            val severity = riskSeverity(
                average = average,
                passingGrade = passingGrade,
                targetAverage = subject.targetAverage,
                remainingPercentage = remainingPercentage,
                neededGrade = needed,
                maxGrade = maxGrade
            )
            SubjectRiskSummary(
                subjectId = subject.id,
                subjectName = subject.name,
                detail = riskDetail(
                    severity = severity,
                    average = average,
                    passingGrade = passingGrade,
                    neededGrade = needed,
                    remainingPercentage = remainingPercentage,
                    targetAverage = subject.targetAverage,
                    maxGrade = maxGrade,
                    gradingScale = gradingScale
                ),
                severity = severity
            )
        }

        return candidates.minWithOrNull(
            compareBy<SubjectRiskSummary> { risk ->
                when (risk.severity) {
                    SubjectRiskSeverity.CRITICAL -> 0
                    SubjectRiskSeverity.ATTENTION -> 1
                    SubjectRiskSeverity.STABLE -> 2
                }
            }.thenBy { it.subjectName }
        )
    }

    private fun riskSeverity(
        average: Double,
        passingGrade: Double,
        targetAverage: Double,
        remainingPercentage: Double,
        neededGrade: Double?,
        maxGrade: Double
    ): SubjectRiskSeverity {
        return when {
            average < passingGrade -> SubjectRiskSeverity.CRITICAL
            remainingPercentage <= 0.0 && average < targetAverage -> SubjectRiskSeverity.CRITICAL
            neededGrade == null && average < targetAverage -> SubjectRiskSeverity.CRITICAL
            neededGrade != null && neededGrade > maxGrade -> SubjectRiskSeverity.CRITICAL
            average < targetAverage -> SubjectRiskSeverity.ATTENTION
            else -> SubjectRiskSeverity.STABLE
        }
    }

    private fun riskDetail(
        severity: SubjectRiskSeverity,
        average: Double,
        passingGrade: Double,
        neededGrade: Double?,
        remainingPercentage: Double,
        targetAverage: Double,
        maxGrade: Double,
        gradingScale: GradingScale
    ): String {
        return when {
            severity == SubjectRiskSeverity.CRITICAL && average < passingGrade ->
                "Promedio bajo la nota mínima: ${GradingScaleUtils.formatGrade(average, gradingScale)}."
            neededGrade != null && neededGrade in 0.0..maxGrade && remainingPercentage > 0.0 ->
                "Necesitas ${GradingScaleUtils.formatGrade(neededGrade, gradingScale)} en lo restante."
            average >= targetAverage ->
                "Va sobre la meta con ${GradingScaleUtils.formatGrade(average, gradingScale)}."
            else ->
                "Revisa los próximos porcentajes para recuperar la meta."
        }
    }

    private fun dashboardMessage(
        hasSubjects: Boolean,
        overdueTasks: Int,
        riskSubject: SubjectRiskSummary?,
        nextTask: TaskSummary?,
        nextAcademicWork: AcademicWorkSummary?,
        weeklyExpenseTotal: Int
    ): String {
        if (!hasSubjects) return "Crea tus materias para ver un tablero real del semestre."
        if (overdueTasks > 0) return "Hay $overdueTasks tarea${if (overdueTasks == 1) "" else "s"} vencida${if (overdueTasks == 1) "" else "s"} que conviene cerrar primero."
        if (riskSubject?.severity == SubjectRiskSeverity.CRITICAL) return "${riskSubject.subjectName} necesita atención académica hoy."
        if (riskSubject?.severity == SubjectRiskSeverity.ATTENTION) return "${riskSubject.subjectName} está cerca de la meta, pero vale la pena vigilarla."
        if (nextAcademicWork != null) return "Tu próximo trabajo es ${nextAcademicWork.title}."
        if (nextTask != null) return "Tu próxima acción clara es ${nextTask.title}."
        if (weeklyExpenseTotal > 0) return "Esta semana ya tienes gastos registrados; revisa si siguen dentro de tu plan."
        return "Todo está bajo control. Mantén notas y tareas actualizadas."
    }

    private fun productivitySummary(completedTasks: Int, pendingTasks: Int, overdueTasks: Int): String {
        return when {
            completedTasks == 0 && pendingTasks == 0 -> "Sin tareas todavía."
            overdueTasks > 0 -> "$completedTasks completadas · $overdueTasks vencidas"
            else -> "$completedTasks completadas · $pendingTasks pendientes"
        }
    }

    private fun companionInsight(
        userName: String,
        priority: HomePrioritySummary,
        pendingTasks: Int,
        overdueTasks: Int,
        todayItems: List<HomeTimelineSummary>
    ): String {
        val shortName = userName.substringBefore(' ').takeIf { it.isNotBlank() } ?: userName
        return when {
            overdueTasks > 0 -> "Cierra una pendiente primero, $shortName. Después el día se siente más ligero."
            todayItems.any { it.state == HomeTimelineState.CURRENT } ->
                "Hoy conviene enfocarte en ${todayItems.first { it.state == HomeTimelineState.CURRENT }.title} antes de abrir más frentes."
            pendingTasks == 0 -> "Día tranquilo, $shortName. Perfecto para repasar o dejar listas tus próximas notas."
            priority.action == HomePriorityAction.TEMPLATES ->
                "Un avance pequeño en ${priority.title.substringBefore(" es ")} hoy puede ahorrarte presión después."
            else -> "Vas bien, $shortName. Prioriza una cosa importante y deja el resto en orden."
        }
    }

    private fun StudentTask.isRelevantForToday(): Boolean {
        val days = ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(dueDateMillis))
        return days <= 3
    }

    private fun StudentTask.isDueTodayOrOverdue(): Boolean {
        return ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(dueDateMillis)) <= 0
    }

    private fun StudentTask.timelineTimeText(): String {
        val days = ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(dueDateMillis))
        val time = dueDateMillis.timelineTimeSuffix()
        return when {
            days < 0 -> "Vencida$time"
            days == 0L -> "Hoy$time"
            days == 1L -> "Mañana$time"
            else -> "En $days días$time"
        }
    }

    private fun AcademicWork.isRelevantForToday(): Boolean {
        val due = dueDateMillis ?: return false
        val days = ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(due))
        return days <= 5
    }

    private fun AcademicWork.isDueTodayOrOverdue(): Boolean {
        val due = dueDateMillis ?: return false
        return ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(due)) <= 0
    }

    private fun AcademicWork.timelineTimeText(): String {
        val due = dueDateMillis ?: return "Sin fecha"
        val days = ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(due))
        val time = due.timelineTimeSuffix()
        return when {
            days < 0 -> "Vencido$time"
            days == 0L -> "Hoy$time"
            days == 1L -> "Mañana$time"
            else -> "En $days días$time"
        }
    }

    private fun Long.timelineTimeSuffix(): String {
        val time = TaskDateUtils.timeFromMillis(this)
        return if (time == LocalTime.MIDNIGHT) "" else " ${TaskDateUtils.formatTimeInput(time)}"
    }

    private fun TaskType.label(): String {
        return when (this) {
            TaskType.WORKSHOP -> "Taller"
            TaskType.EXAM -> "Examen"
            TaskType.ESSAY -> "Ensayo"
            TaskType.PRESENTATION -> "Presentación"
            TaskType.RESEARCH -> "Investigación"
            TaskType.TEST -> "Quiz"
            TaskType.PRACTICE -> "Práctica"
            TaskType.PROJECT -> "Proyecto"
            TaskType.READING -> "Lectura"
            TaskType.OTHER -> "Tarea"
        }
    }

    private fun TaskType.timelineKind(): HomeTimelineKind {
        return when (this) {
            TaskType.EXAM,
            TaskType.TEST -> HomeTimelineKind.EXAM
            else -> HomeTimelineKind.TASK
        }
    }

    private fun TaskDifficulty.rank(): Int {
        return when (this) {
            TaskDifficulty.EASY -> 1
            TaskDifficulty.MEDIUM -> 2
            TaskDifficulty.HARD -> 3
        }
    }

    private fun AcademicWorkPriority.rank(): Int {
        return when (this) {
            AcademicWorkPriority.LOW -> 1
            AcademicWorkPriority.MEDIUM -> 2
            AcademicWorkPriority.HIGH -> 3
        }
    }

    private data class AcademicFocusSummary(
        val subjectId: String,
        val subjectName: String,
        val average: Double?,
        val targetAverage: Double,
        val evaluatedPercentage: Double,
        val gradingScale: GradingScale
    ) {
        val remainingPercentage: Double = (100.0 - evaluatedPercentage).coerceIn(0.0, 100.0)
    }
}
