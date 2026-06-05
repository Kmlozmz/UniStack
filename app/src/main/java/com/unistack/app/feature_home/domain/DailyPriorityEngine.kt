package com.unistack.app.feature_home.domain

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
                    slotLabel = "Ahora",
                    title = "Crear primera materia",
                    detail = "Activa notas, tareas y prioridades reales.",
                    minutesText = "3 min",
                    actionLabel = "Materias",
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
                    slotLabel = "Ahora",
                    title = "Repaso breve",
                    detail = "Mantén una materia caliente antes de que aparezca una urgencia.",
                    minutesText = "15 min",
                    actionLabel = "Materias",
                    action = HomePriorityAction.SUBJECTS
                ),
                DailyFocusItem(
                    slotLabel = "Luego",
                    title = "Ordenar pendientes",
                    detail = "Revisa si hay una tarea pequeña que puedas cerrar hoy.",
                    minutesText = "10 min",
                    actionLabel = "Tareas",
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

    private fun academicWorkCandidates(works: List<AcademicWork>): List<PriorityCandidate> {
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
                title = if (critical) "${risk.subjectName} necesita atención" else "${risk.subjectName} está cerca de la meta",
                shortDescription = if (critical) {
                    "Repasa esta materia antes de abrir más frentes."
                } else {
                    "Vigila esta materia con un repaso corto hoy."
                },
                fullDescription = "La elegí porque ${risk.subjectName} requiere seguimiento académico. ${risk.detail}",
                suggestion = "${heroActionPrefix()}: repasar ${risk.subjectName} 15 minutos y revisar qué evaluación pesa más.",
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
            title = if (overBudget) "Gastos sobre el límite" else "Gastos cerca del límite",
            minutes = 8,
            summary = HomePrioritySummary(
                title = if (overBudget) "Gastos sobre el límite" else "Gastos cerca del límite",
                shortDescription = "Revisa tu semana antes de registrar más gastos.",
                fullDescription = "La elegí porque ya usaste $usagePercent% de tu presupuesto semanal. Revisarlo ahora te ayuda a ajustar el resto de la semana.",
                suggestion = "${heroActionPrefix()}: mirar tus categorías y decidir si conviene pausar algún gasto.",
                action = HomePriorityAction.EXPENSES
            )
        )
    }

    private fun StudentTask.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = TaskDateUtils.dueText(dueDateMillis)
        val timeText = TaskDateUtils.estimatedTimeText(estimatedMinutes)
        val isExamLike = type == TaskType.EXAM || type == TaskType.TEST
        val titleText = when {
            days < 0 -> "$title está vencida"
            days == 0L && isExamLike -> "$title es hoy"
            days == 0L -> "$title vence hoy"
            days == 1L -> "$title es mañana"
            else -> "$title es lo siguiente"
        }
        val actionVerb = if (isExamLike) "repasar" else "avanzar"
        return HomePrioritySummary(
            title = titleText,
            shortDescription = when {
                days < 0 -> "Cierra esta pendiente antes de abrir más frentes."
                days == 0L -> "Atiéndela hoy para mantener el día bajo control."
                else -> "Reserva un bloque corto antes de que se acerque."
            },
            fullDescription = when {
                days < 0 -> "La elegí porque ya está vencida. $title aparece como la tarea que más conviene resolver primero."
                days == 0L -> "La elegí porque vence hoy. $title necesita atención para que el resto del día no se acumule."
                else -> "La elegí porque vence $dueText y tiene una prioridad suficiente para prepararla con calma."
            },
            suggestion = "${heroActionPrefix()}: $actionVerb $timeText y dejar un avance claro.",
            action = HomePriorityAction.TASKS
        )
    }

    private fun AcademicWork.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = dueDateMillis?.let(TaskDateUtils::dueText) ?: "sin fecha"
        return HomePrioritySummary(
            title = when {
                days < 0 -> "$title está vencido"
                days == 0L -> "$title vence hoy"
                days == 1L -> "$title es mañana"
                else -> "$title es lo siguiente"
            },
            shortDescription = when {
                days <= 0 -> "Dale prioridad antes de sumar nuevas tareas."
                else -> "Avanza un poco antes de que se acumule."
            },
            fullDescription = "La elegí porque este trabajo vence $dueText y todavía no está marcado como entregado. Un avance pequeño hoy reduce presión después.",
            suggestion = "${heroActionPrefix()}: avanzar 15 minutos y marcar un paso concreto.",
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
            HomePriorityAction.SUBJECT -> "Abrir"
            HomePriorityAction.SUBJECTS -> "Materias"
            HomePriorityAction.TASKS -> "Tareas"
            HomePriorityAction.EXPENSES -> "Gastos"
            HomePriorityAction.TEMPLATES -> "Trabajos"
        }
    }

    private fun slotLabel(index: Int): String {
        return when (index) {
            0 -> "Ahora"
            1 -> "Luego"
            else -> "Si tienes 30 min"
        }
    }

    private fun heroActionPrefix(): String {
        return when (LocalTime.now().hour) {
            in 5..11 -> "Arranca con"
            in 18..23 -> "Deja listo"
            else -> "Siguiente paso"
        }
    }

    private data class PriorityCandidate(
        val score: Int,
        val title: String,
        val minutes: Int,
        val summary: HomePrioritySummary
    )
}
