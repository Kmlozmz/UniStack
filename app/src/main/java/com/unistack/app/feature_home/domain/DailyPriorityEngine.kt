package com.unistack.app.feature_home.domain

import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskDifficulty
import com.unistack.app.feature_tasks.domain.TaskType
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkPriority
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.UserProfile
import java.time.LocalTime
import java.time.temporal.ChronoUnit

object DailyPriorityEngine {
    private val isEnglish: Boolean get() = java.util.Locale.getDefault().language == "en"
    fun primaryPriority(
        subjectsCount: Int,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>
    ): HomePrioritySummary? {
        if (subjectsCount == 0) return null
        return rankedCandidates(
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        ).firstOrNull()?.summary
    }

    fun dailyFocusPlan(
        subjectsCount: Int,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>
    ): List<DailyFocusItem> {
        if (subjectsCount == 0) {
            return listOf(
                DailyFocusItem(
                    slotLabel = if (isEnglish) "Now" else "Ahora",
                    title = if (isEnglish) "Create first subject" else "Crear primera materia",
                    detail = if (isEnglish) "Enables grades, tasks, and real priorities." else "Activa notas, tareas y prioridades reales.",
                    minutesText = "3 min",
                    actionLabel = if (isEnglish) "Subjects" else "Materias",
                    action = HomePriorityAction.SUBJECTS
                )
            )
        }

        val candidates = rankedCandidates(
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        )

        val focused = candidates
            .distinctBy { Triple(it.summary.action, it.summary.subjectId, it.title) }
            .take(3)
            .mapIndexed { index, candidate -> candidate.toFocusItem(slotLabel(index)) }

        return focused.ifEmpty {
            listOf(
                DailyFocusItem(
                    slotLabel = if (isEnglish) "Now" else "Ahora",
                    title = if (isEnglish) "Quick review" else "Repaso breve",
                    detail = if (isEnglish) "Keep a subject fresh before an emergency comes up." else "Mantén una materia caliente antes de que aparezca una urgencia.",
                    minutesText = "15 min",
                    actionLabel = if (isEnglish) "Subjects" else "Materias",
                    action = HomePriorityAction.SUBJECTS
                ),
                DailyFocusItem(
                    slotLabel = if (isEnglish) "Later" else "Luego",
                    title = if (isEnglish) "Sort out pending tasks" else "Ordenar pendientes",
                    detail = if (isEnglish) "Check if there's a small task you can finish today." else "Revisa si hay una tarea pequeña que puedas cerrar hoy.",
                    minutesText = "10 min",
                    actionLabel = if (isEnglish) "Tasks" else "Tareas",
                    action = HomePriorityAction.TASKS
                )
            )
        }
    }

    private fun rankedCandidates(
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>
    ): List<PriorityCandidate> {
        return buildList {
            addAll(taskCandidates(pendingTasks))
            addAll(academicWorkCandidates(works))
            subjectRiskCandidate(riskSubject)?.let(::add)
            expenseCandidate(
                weeklyExpenseTotal = weeklyExpenseTotal,
                profile = profile,
                enabledModules = enabledModules
            )?.let(::add)
        }.sortedByDescending { it.score }
    }

    private fun taskCandidates(tasks: List<StudentTask>): List<PriorityCandidate> {
        val today = TaskDateUtils.today()
        return tasks.mapNotNull { task ->
            val days = ChronoUnit.DAYS.between(today, TaskDateUtils.fromMillis(task.dueDateMillis))
            val baseScore = when {
                days < 0 -> 940
                days == 0L -> 900
                days == 1L -> 760
                days in 2..3 -> 650
                else -> return@mapNotNull null
            }
            val typeBoost = when (task.type) {
                TaskType.EXAM,
                TaskType.TEST -> 45
                TaskType.PROJECT -> 30
                TaskType.ESSAY,
                TaskType.PRESENTATION,
                TaskType.RESEARCH -> 20
                else -> 0
            }
            val difficultyBoost = when (task.difficulty) {
                TaskDifficulty.HARD -> 25
                TaskDifficulty.MEDIUM -> 12
                TaskDifficulty.EASY -> 0
            }
            PriorityCandidate(
                score = baseScore + typeBoost + difficultyBoost,
                title = task.title,
                minutes = task.estimatedMinutes.coerceAtLeast(15),
                summary = task.prioritySummary(days)
            )
        }
    }

    /**
     * Los trabajos solo compiten por el primer puesto donde Trabajos se puede abrir.
     *
     * Fuera de dev y alpha esa pantalla está cerrada, y proponer «avanza este trabajo» para
     * luego no dejar entrar es peor que no proponer nada.
     */
    private fun academicWorkCandidates(works: List<AcademicWork>): List<PriorityCandidate> {
        if (!BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished) return emptyList()
        val today = TaskDateUtils.today()
        return works
            .filterNot { it.status == AcademicWorkStatus.SUBMITTED }
            .mapNotNull { work ->
                val dueMillis = work.dueDateMillis ?: return@mapNotNull null
                val days = ChronoUnit.DAYS.between(today, TaskDateUtils.fromMillis(dueMillis))
                val baseScore = when {
                    days < 0 -> 900
                    days == 0L -> 840
                    days == 1L -> 740
                    days in 2..5 -> 610
                    else -> return@mapNotNull null
                }
                val priorityBoost = when (work.priority) {
                    AcademicWorkPriority.HIGH -> 35
                    AcademicWorkPriority.MEDIUM -> 18
                    AcademicWorkPriority.LOW -> 0
                }
                val minutes = when (work.priority) {
                    AcademicWorkPriority.HIGH -> 30
                    AcademicWorkPriority.MEDIUM -> 20
                    AcademicWorkPriority.LOW -> 15
                }
                PriorityCandidate(
                    score = baseScore + priorityBoost,
                    title = work.title,
                    minutes = minutes,
                    summary = work.prioritySummary(days)
                )
            }
    }

    private fun subjectRiskCandidate(riskSubject: SubjectRiskSummary?): PriorityCandidate? {
        val risk = riskSubject ?: return null
        if (risk.severity == SubjectRiskSeverity.STABLE) return null
        val critical = risk.severity == SubjectRiskSeverity.CRITICAL
        return PriorityCandidate(
            score = if (critical) 870 else 680,
            title = risk.subjectName,
            minutes = if (critical) 25 else 15,
            summary = HomePrioritySummary(
                title = if (critical) {
                    if (isEnglish) "${risk.subjectName} needs attention" else "${risk.subjectName} necesita atención"
                } else {
                    if (isEnglish) "${risk.subjectName} is close to target" else "${risk.subjectName} está cerca de la meta"
                },
                shortDescription = if (critical) {
                    if (isEnglish) "Review this subject before opening more fronts." else "Repasa esta materia antes de abrir más frentes."
                } else {
                    if (isEnglish) "Keep an eye on this subject with a short review today." else "Vigila esta materia con un repaso corto hoy."
                },
                action = HomePriorityAction.SUBJECT,
                subjectId = risk.subjectId
            )
        )
    }

    private fun expenseCandidate(
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>
    ): PriorityCandidate? {
        if (AppModule.EXPENSES !in enabledModules || weeklyExpenseTotal <= 0) return null

        val weeklyBudget = profile?.weeklyBudget ?: 0
        if (weeklyBudget <= 0) return null

        val threshold = (profile?.expenseAlertThresholdPercent ?: 80).coerceIn(1, 100)
        val usagePercent = (weeklyExpenseTotal * 100) / weeklyBudget
        if (usagePercent < threshold) return null

        val overBudget = weeklyExpenseTotal > weeklyBudget
        return PriorityCandidate(
            score = if (overBudget) 820 else 570,
            title = if (overBudget) {
                if (isEnglish) "Expenses over limit" else "Gastos sobre el límite"
            } else {
                if (isEnglish) "Expenses near limit" else "Gastos cerca del límite"
            },
            minutes = 8,
            summary = HomePrioritySummary(
                title = if (overBudget) {
                    if (isEnglish) "Expenses over limit" else "Gastos sobre el límite"
                } else {
                    if (isEnglish) "Expenses near limit" else "Gastos cerca del límite"
                },
                shortDescription = if (isEnglish) "Review your week before logging more expenses." else "Revisa tu semana antes de registrar más gastos.",
                action = HomePriorityAction.EXPENSES
            )
        )
    }

    private fun StudentTask.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = TaskDateUtils.dueText(dueDateMillis)
        val timeText = TaskDateUtils.estimatedTimeText(estimatedMinutes)
        val isExamLike = type == TaskType.EXAM || type == TaskType.TEST
        val titleText = when {
            days < 0 -> if (isEnglish) "$title is overdue" else "$title está vencida"
            days == 0L && isExamLike -> if (isEnglish) "$title is today" else "$title es hoy"
            days == 0L -> if (isEnglish) "$title is due today" else "$title vence hoy"
            days == 1L -> if (isEnglish) "$title is tomorrow" else "$title es mañana"
            else -> if (isEnglish) "$title is next" else "$title es lo siguiente"
        }
        val actionVerb = if (isExamLike) (if (isEnglish) "review" else "repasar") else (if (isEnglish) "make progress" else "avanzar")
        return HomePrioritySummary(
            title = titleText,
            shortDescription = when {
                days < 0 -> if (isEnglish) "Close this pending task before opening more fronts." else "Cierra esta pendiente antes de abrir más frentes."
                days == 0L -> if (isEnglish) "Handle it today to keep the day under control." else "Atiéndela hoy para mantener el día bajo control."
                else -> if (isEnglish) "Reserve a short block before it gets closer." else "Reserva un bloque corto antes de que se acerque."
            },
            action = HomePriorityAction.TASKS
        )
    }

    private fun AcademicWork.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = dueDateMillis?.let(TaskDateUtils::dueText) ?: if (isEnglish) "no due date" else "sin fecha"
        return HomePrioritySummary(
            title = when {
                days < 0 -> if (isEnglish) "$title is overdue" else "$title está vencido"
                days == 0L -> if (isEnglish) "$title is due today" else "$title vence hoy"
                days == 1L -> if (isEnglish) "$title is tomorrow" else "$title es mañana"
                else -> if (isEnglish) "$title is next" else "$title es lo siguiente"
            },
            shortDescription = when {
                days <= 0 -> if (isEnglish) "Give it priority before adding new tasks." else "Dale prioridad antes de sumar nuevas tareas."
                else -> if (isEnglish) "Make some progress before it piles up." else "Avanza un poco antes de que se acumule."
            },
            action = HomePriorityAction.TEMPLATES,
            subjectId = subjectId
        )
    }

    private fun PriorityCandidate.toFocusItem(slotLabel: String): DailyFocusItem {
        return DailyFocusItem(
            slotLabel = slotLabel,
            title = summary.title,
            detail = summary.shortDescription,
            minutesText = TaskDateUtils.estimatedTimeText(minutes),
            actionLabel = summary.action.focusActionLabel(),
            action = summary.action,
            subjectId = summary.subjectId
        )
    }

    private fun HomePriorityAction.focusActionLabel(): String {
        return when (this) {
            HomePriorityAction.SUBJECT -> if (isEnglish) "Open" else "Abrir"
            HomePriorityAction.SUBJECTS -> if (isEnglish) "Subjects" else "Materias"
            HomePriorityAction.TASKS -> if (isEnglish) "Tasks" else "Tareas"
            HomePriorityAction.EXPENSES -> if (isEnglish) "Expenses" else "Gastos"
            HomePriorityAction.TEMPLATES -> if (isEnglish) "Assignments" else "Trabajos"
            HomePriorityAction.SCHEDULE -> if (isEnglish) "Schedule" else "Horario"
        }
    }

    private fun slotLabel(index: Int): String {
        return when (index) {
            0 -> if (isEnglish) "Now" else "Ahora"
            1 -> if (isEnglish) "Later" else "Luego"
            else -> if (isEnglish) "If you have 30 min" else "Si tienes 30 min"
        }
    }

    private fun heroActionPrefix(): String {
        return when (LocalTime.now().hour) {
            in 5..11 -> if (isEnglish) "Start with" else "Arranca con"
            in 18..23 -> if (isEnglish) "Wrap up" else "Deja listo"
            else -> if (isEnglish) "Next step" else "Siguiente paso"
        }
    }

    private data class PriorityCandidate(
        val score: Int,
        val title: String,
        val minutes: Int,
        val summary: HomePrioritySummary
    )
}
