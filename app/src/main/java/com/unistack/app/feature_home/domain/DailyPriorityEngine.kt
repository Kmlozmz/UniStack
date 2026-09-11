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
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

object DailyPriorityEngine {
    /**
     * Lo mas urgente de todo, o nulo si no hay nada que reclame.
     *
     * [extra] son candidatos que el motor no sabe calcular —la clase en curso o la
     * siguiente, que salen del horario— con su puntuacion ya puesta, para que compitan en
     * la misma mesa que las tareas y las materias. Antes la clase ganaba **siempre**, por
     * delante de una tarea vencida o de una materia en rojo, y el hero era el horario con
     * otro nombre.
     */
    fun primaryPriority(
        subjectsCount: Int,
        pendingTasks: List<StudentTask>,
        works: List<AcademicWork>,
        riskSubject: SubjectRiskSummary?,
        weeklyExpenseTotal: Int,
        profile: UserProfile?,
        enabledModules: Set<AppModule>,
        extra: List<Pair<Int, HomePrioritySummary>> = emptyList()
    ): HomePrioritySummary? {
        if (subjectsCount == 0) return null
        val candidatos = rankedCandidates(
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules
        ) + extra.map { (puntos, resumen) ->
            PriorityCandidate(score = puntos, title = resumen.title, minutes = 5, summary = resumen)
        }
        return candidatos.maxByOrNull { it.score }?.summary
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
                    slotLabel = Textos.get(R.string.settings_backup_now),
                    title = Textos.get(R.string.home_crear_primera_materia),
                    detail = Textos.get(R.string.home_activa_notas_tareas_y_prioridades_reales),
                    minutesText = "3 min",
                    actionLabel = Textos.get(R.string.settings_backup_subjects),
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
                    slotLabel = Textos.get(R.string.settings_backup_now),
                    title = Textos.get(R.string.home_repaso_breve),
                    detail = Textos.get(R.string.home_manten_una_materia_caliente_antes_de),
                    minutesText = "15 min",
                    actionLabel = Textos.get(R.string.settings_backup_subjects),
                    action = HomePriorityAction.SUBJECTS
                ),
                DailyFocusItem(
                    slotLabel = Textos.get(R.string.home_luego),
                    title = Textos.get(R.string.home_ordenar_pendientes),
                    detail = Textos.get(R.string.home_revisa_si_hay_una_tarea_pequena),
                    minutesText = "10 min",
                    actionLabel = Textos.get(R.string.setup_mod_tasks_title),
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
                    Textos.get(R.string.home_necesita_atencion, risk.subjectName)
                } else {
                    Textos.get(R.string.home_esta_cerca_de_la_meta, risk.subjectName)
                },
                shortDescription = if (critical) {
                    Textos.get(R.string.home_repasa_esta_materia_antes_de_abrir)
                } else {
                    Textos.get(R.string.home_vigila_esta_materia_con_un_repaso)
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
                Textos.get(R.string.home_gastos_sobre_el_limite)
            } else {
                Textos.get(R.string.home_gastos_cerca_del_limite)
            },
            minutes = 8,
            summary = HomePrioritySummary(
                title = if (overBudget) {
                    Textos.get(R.string.home_gastos_sobre_el_limite)
                } else {
                    Textos.get(R.string.home_gastos_cerca_del_limite)
                },
                shortDescription = Textos.get(R.string.home_revisa_tu_semana_antes_de_registrar),
                action = HomePriorityAction.EXPENSES
            )
        )
    }

    private fun StudentTask.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = TaskDateUtils.dueText(dueDateMillis)
        val timeText = TaskDateUtils.estimatedTimeText(estimatedMinutes)
        val isExamLike = type == TaskType.EXAM || type == TaskType.TEST
        val titleText = when {
            days < 0 -> Textos.get(R.string.home_esta_vencida, title)
            days == 0L && isExamLike -> Textos.get(R.string.home_es_hoy, title)
            days == 0L -> Textos.get(R.string.home_vence_hoy, title)
            days == 1L -> Textos.get(R.string.home_es_manana, title)
            else -> Textos.get(R.string.home_es_lo_siguiente, title)
        }
        val actionVerb = if (isExamLike) (Textos.get(R.string.home_repasar)) else (Textos.get(R.string.home_avanzar))
        return HomePrioritySummary(
            title = titleText,
            shortDescription = when {
                days < 0 -> Textos.get(R.string.home_cierra_esta_pendiente_antes_de_abrir)
                days == 0L -> Textos.get(R.string.home_atiendela_hoy_para_mantener_el_dia)
                else -> Textos.get(R.string.home_reserva_un_bloque_corto_antes_de)
            },
            action = HomePriorityAction.TASKS
        )
    }

    private fun AcademicWork.prioritySummary(days: Long): HomePrioritySummary {
        val dueText = dueDateMillis?.let(TaskDateUtils::dueText) ?: Textos.get(R.string.home_sin_fecha)
        return HomePrioritySummary(
            title = when {
                days < 0 -> Textos.get(R.string.home_esta_vencido, title)
                days == 0L -> Textos.get(R.string.home_vence_hoy, title)
                days == 1L -> Textos.get(R.string.home_es_manana, title)
                else -> Textos.get(R.string.home_es_lo_siguiente, title)
            },
            shortDescription = when {
                days <= 0 -> Textos.get(R.string.home_dale_prioridad_antes_de_sumar_nuevas)
                else -> Textos.get(R.string.home_avanza_un_poco_antes_de_que)
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
            HomePriorityAction.SUBJECT -> Textos.get(R.string.notif_btn_open)
            HomePriorityAction.SUBJECTS -> Textos.get(R.string.settings_backup_subjects)
            HomePriorityAction.TASKS -> Textos.get(R.string.setup_mod_tasks_title)
            HomePriorityAction.EXPENSES -> Textos.get(R.string.setup_mod_expenses_title)
            HomePriorityAction.TEMPLATES -> Textos.get(R.string.home_nav_assignments)
            HomePriorityAction.SCHEDULE -> Textos.get(R.string.home_action_schedule_short)
        }
    }

    private fun slotLabel(index: Int): String {
        return when (index) {
            0 -> Textos.get(R.string.settings_backup_now)
            1 -> Textos.get(R.string.home_luego)
            else -> Textos.get(R.string.home_si_tienes_30_min)
        }
    }

    private fun heroActionPrefix(): String {
        return when (LocalTime.now().hour) {
            in 5..11 -> Textos.get(R.string.home_arranca_con)
            in 18..23 -> Textos.get(R.string.home_deja_listo)
            else -> Textos.get(R.string.notif_next_step)
        }
    }

    private data class PriorityCandidate(
        val score: Int,
        val title: String,
        val minutes: Int,
        val summary: HomePrioritySummary
    )
}
