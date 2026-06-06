package com.unistack.app.feature_home.presentation

import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.DailyPriorityEngine
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
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import java.time.LocalTime
import java.time.temporal.ChronoUnit

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
        val riskSubject = subjectRiskSummary(
            subjects = subjects,
            profile = profile,
            gradingScale = gradingScale
        )
        val todayItems = todayTimelineItems(
            tasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            subjectNameById = subjects.associate { it.id to it.name }
        )
        val priority = prioritySummary(
            subjects = subjects,
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        )
        val dailyFocusItems = DailyPriorityEngine.dailyFocusPlan(
            subjectsCount = subjects.size,
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        )

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
        val evaluatedPercentage = subject.grades.sumOf { it.percentage }.coerceIn(0.0, 1.0)
        return SubjectSummary(
            id = subject.id,
            name = subject.name,
            average = GradeCalculator.calculateCurrentAverage(subject.grades),
            targetAverage = subject.targetAverage,
            progress = evaluatedPercentage.toFloat(),
            type = subject.visualType
        )
    }

    private fun generalAverage(subjects: List<Subject>): Double? {
        val subjectsWithGrades = subjects.filter { it.grades.isNotEmpty() }
        if (subjectsWithGrades.isEmpty()) return null

        val validGrades = subjectsWithGrades.mapNotNull { subject ->
            GradeCalculator.calculateCurrentAverage(subject.grades)?.let { average ->
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
        val currentWeightedPoints = GradeCalculator.calculateWeightedPoints(focusSubject.grades)
        val remainingPercentage = remainingPercentage(focusSubject)
        val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
        val needed = GradeCalculator.calculateNeededGrade(
            currentWeightedPoints = currentWeightedPoints,
            remainingPercentage = remainingPercentage,
            targetAverage = focusSubject.targetAverage,
            maxGrade = maxGrade
        )

        return needed
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
        enabledModules: Set<AppModule>
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
                fullDescription = "Todavía no tienes materias registradas. Cuando agregues tus cursos, UniStack podrá ordenar tareas, notas y alertas según tu semestre real.",
                suggestion = "${heroActionPrefix()}: crear tu primera materia para empezar con una agenda útil.",
                action = HomePriorityAction.SUBJECTS
            )
        }

        return calmPrioritySummary(
            subjects = subjects,
            pendingTasks = pendingTasks,
            works = works,
            weeklyExpenseTotal = weeklyExpenseTotal,
            enabledModules = enabledModules
        )
    }

    private fun calmPrioritySummary(
        subjects: List<Subject>,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        weeklyExpenseTotal: Int,
        enabledModules: Set<AppModule>
    ): HomePrioritySummary {
        val index = TaskDateUtils.today().dayOfYear % 4
        val canUseExpenses = AppModule.EXPENSES in enabledModules && weeklyExpenseTotal > 0
        val hasOpenWorks = works.any { it.status != AcademicWorkStatus.SUBMITTED }
        return when {
            canUseExpenses && index == 1 -> HomePrioritySummary(
                title = "Gastos bajo control",
                shortDescription = "Buen momento para revisar si tu semana sigue en ritmo.",
                fullDescription = "No hay urgencias académicas fuertes ahora. Como ya registraste gastos esta semana, puedes hacer una revisión rápida sin convertirlo en preocupación.",
                suggestion = "${heroActionPrefix()}: revisar tus gastos 2 minutos y seguir con el día.",
                action = HomePriorityAction.EXPENSES
            )
            hasOpenWorks && index == 2 -> HomePrioritySummary(
                title = "Espacio para avanzar",
                shortDescription = "Aprovecha un bloque corto para mover un trabajo.",
                fullDescription = "No tienes una urgencia clara ahora. Este es un buen momento para avanzar un trabajo abierto antes de que se acerque la fecha.",
                suggestion = "${heroActionPrefix()}: escoger un trabajo y avanzar 15 minutos.",
                action = HomePriorityAction.TEMPLATES
            )
            pendingTasks.isNotEmpty() && index == 3 -> HomePrioritySummary(
                title = "Buen ritmo",
                shortDescription = "Ordena una tarea pequeña y deja el día más liviano.",
                fullDescription = "No hay vencimientos cercanos fuertes. Aun así, tienes tareas pendientes que puedes organizar para evitar presión después.",
                suggestion = "${heroActionPrefix()}: elegir una tarea simple y dejarla encaminada.",
                action = HomePriorityAction.TASKS
            )
            else -> HomePrioritySummary(
                title = "Día despejado",
                shortDescription = "Aprovecha para repasar o preparar tus próximas notas.",
                fullDescription = "No tienes vencimientos cercanos por ahora. Es un buen momento para repasar, avanzar en tus materias o dejar listas tus próximas actividades.",
                suggestion = "${heroActionPrefix()}: dedica 15 minutos a repasar hoy para mantener el ritmo.",
                action = if (subjects.isNotEmpty()) HomePriorityAction.SUBJECTS else HomePriorityAction.TASKS
            )
        }
    }

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
            val average = GradeCalculator.calculateCurrentAverage(subject.grades) ?: return@mapNotNull null
            val remainingPercentage = remainingPercentage(subject)
            val needed = GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades),
                remainingPercentage = remainingPercentage,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
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

    private fun remainingPercentage(subject: Subject): Double {
        return (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0)
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
        return when {
            days < 0 -> "Vencida"
            days == 0L -> "Hoy"
            days == 1L -> "Mañana"
            else -> "En $days días"
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
        return when {
            days < 0 -> "Vencido"
            days == 0L -> "Hoy"
            days == 1L -> "Mañana"
            else -> "En $days días"
        }
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
}
