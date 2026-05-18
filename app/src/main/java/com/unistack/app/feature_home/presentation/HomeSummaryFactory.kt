package com.unistack.app.feature_home.presentation

import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_home.domain.AcademicWorkSummary
import com.unistack.app.feature_home.domain.ExpenseSummary
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectRiskSeverity
import com.unistack.app.feature_home.domain.SubjectRiskSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile

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
        val gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
        val riskSubject = subjectRiskSummary(
            subjects = subjects,
            profile = profile,
            gradingScale = gradingScale
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
            generalAverage = generalAverage(subjects),
            subjectsCount = subjects.size,
            tasksToday = pendingTasks.count { TaskDateUtils.isToday(it.dueDateMillis) },
            overdueTasks = overdueTasks,
            subjects = subjects.take(3).map(::subjectSummary),
            riskSubject = riskSubject,
            neededGrade = neededGradeSummary(subjects, profile),
            nextTask = nextTask,
            nextAcademicWork = nextAcademicWork,
            weeklyExpenses = weeklyExpenses,
            weeklyExpenseTotal = weeklyExpenseTotal,
            productivitySummary = productivitySummary(completedTasks, pendingTasks.size, overdueTasks),
            gradingScale = gradingScale,
            enabledModules = profile?.enabledModules ?: setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )
    }

    private fun subjectSummary(subject: Subject): SubjectSummary {
        val evaluatedPercentage = subject.grades.sumOf { it.percentage }.coerceIn(0.0, 1.0)
        return SubjectSummary(
            id = subject.id,
            name = subject.name,
            average = GradeCalculator.calculateCurrentAverage(subject.grades),
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
}
