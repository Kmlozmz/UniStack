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
import com.unistack.app.feature_home.domain.HomeUpcomingItem
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
import com.unistack.app.feature_profile.presentation.educationSummary
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.portraitUrl
import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
import java.time.LocalDate
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import com.unistack.app.feature_home.domain.HomePriorityTimeframe
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

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
        val previousWeekExpenseTotal = previousWeekTotal(expenses)
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
        val clase = classPriorityCandidate(
            sessions = classSessions,
            subjects = subjects
        )
        val upcomingItems = upcomingItems(
            sessions = classSessions,
            tasks = pendingTasks,
            works = works,
            subjectNameById = subjects.associate { it.id to it.name }
        )
        val todayItems = todayTimelineItems(
            tasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            subjectNameById = subjects.associate { it.id to it.name }
        )
        /*
         * **La clase compite; no manda.**
         *
         * Estaba `academicDataPriority ?: schedulePriority ?: prioritySummary(...)`, o sea:
         * habiendo cualquier clase en los proximos siete dias, el hero era esa clase. Una
         * tarea vencida, una materia en rojo o el presupuesto pasado no llegaban a
         * ensenarse nunca mientras hubiera horario, que es siempre. Y encima la clase
         * elegida ignoraba la que **esta pasando**: en plena clase decia «manana».
         *
         * Ahora la clase entra en la misma clasificacion que lo demas con una puntuacion
         * segun lo cerca que este: la que esta en curso por encima de todo, la de dentro
         * de una hora por debajo de lo que vence hoy, la de manana por debajo de casi
         * todo.
         */
        val priority = academicDataPriority ?: prioritySummary(
            subjects = heroSubjects,
            pendingTasks = heroTasks,
            works = heroWorks,
            riskSubject = riskSubject,
            weeklyExpenseTotal = heroExpenseTotal,
            profile = profile,
            enabledModules = enabledModules,
            academicFocus = academicFocus,
            clase = clase
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
                ?: Textos.get(R.string.settings_profile_student),
            educationLine = profile?.educationSummary().orEmpty(),
            avatarPhotoUrl = profile?.portraitUrl ?: user.photoUrl,
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
            upcomingItems = upcomingItems,
            weeklyExpenses = weeklyExpenses,
            weeklyExpenseTotal = weeklyExpenseTotal,
            previousWeekExpenseTotal = previousWeekExpenseTotal,
            productivitySummary = productivitySummary(completedTasks, pendingTasks.size, overdueTasks),
            companionInsight = companionInsight(
                userName = profile?.preferredName?.takeIf { it.isNotBlank() }
                    ?: user.displayName?.takeIf { it.isNotBlank() }
                    ?: Textos.get(R.string.settings_profile_student),
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
        val cuts = subject.cutScheme.cuts
        val evaluatedPercentage =
            GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, cuts) / 100.0
        return SubjectSummary(
            id = subject.id,
            name = subject.name,
            average = GradeCalculator.calculateCurrentAverageByCuts(subject.grades, cuts),
            targetAverage = subject.targetAverage,
            progress = evaluatedPercentage.toFloat(),
            type = subject.visualType
        )
    }

    private fun generalAverage(subjects: List<Subject>): Double? {
        val subjectsWithGrades = subjects.filter { it.grades.isNotEmpty() }
        if (subjectsWithGrades.isEmpty()) return null

        val validGrades = subjectsWithGrades.mapNotNull { subject ->
            GradeCalculator.calculateCurrentAverageByCuts(
                subject.grades,
                subject.cutScheme.cuts
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
            cuts = focusSubject.cutScheme.cuts,
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
                    dueText = work.dueDateMillis?.let(TaskDateUtils::dueText) ?: Textos.get(R.string.home_sin_fecha),
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
        academicFocus: AcademicFocusSummary?,
        clase: Pair<Int, HomePrioritySummary>? = null
    ): HomePrioritySummary {
        // Con los tres interruptores del hero apagados `subjects` llega vacio y el motor
        // no puntua nada: entonces se queda con la clase, que es lo que promete el ajuste.
        if (subjects.isEmpty() && clase != null) return clase.second
        DailyPriorityEngine.primaryPriority(
            subjectsCount = subjects.size,
            pendingTasks = pendingTasks,
            works = works,
            riskSubject = riskSubject,
            weeklyExpenseTotal = weeklyExpenseTotal,
            profile = profile,
            enabledModules = enabledModules,
            extra = listOfNotNull(clase)
        )?.let { return it }

        if (subjects.isEmpty()) {
            return HomePrioritySummary(
                title = Textos.get(R.string.home_prepara_tu_semestre),
                shortDescription = Textos.get(R.string.home_agrega_tus_materias_para_activar_prioridades),
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
                    Textos.get(R.string.home_espera_su_nota, next?.title.orEmpty())
                } else {
                    Textos.get(R.string.home_resultados_esperan_registro, waitingResults.size)
                },
                shortDescription = Textos.get(R.string.home_registra_la_calificacion_o_indica_que),
                action = HomePriorityAction.TASKS,
                subjectId = next?.subjectId
            )
        }

        val incompleteHistory = subjects.firstOrNull { subject ->
            val activeOrder = subject.cutScheme.cuts
                .firstOrNull { it.id == subject.activeCutId }
                ?.order
                ?: 1
            activeOrder > 1 &&
                subject.historyPromptStatus != com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus.COMPLETED &&
                subject.historyPromptStatus != com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus.DISMISSED &&
                subject.cutScheme.cuts
                    .filter { it.order < activeOrder }
                    .any { cut ->
                        cut.id !in subject.unknownCutIds &&
                            subject.grades.none { it.cutId == cut.id }
                    }
        }
        if (incompleteHistory != null) {
            return HomePrioritySummary(
                title = Textos.get(R.string.home_completa_el_historial_de, incompleteHistory.name),
                shortDescription = Textos.get(R.string.home_faltan_datos_de_cortes_anteriores_para),
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
                title = Textos.get(R.string.home_ajusta, subjectWithUnknownWeights.name),
                shortDescription = if (count == 1) {
                    Textos.get(R.string.home_hay_una_nota_sin_porcentaje_la)
                } else {
                    Textos.get(R.string.home_hay_notas_sin_porcentaje_la_proyeccion, count)
                },
                action = HomePriorityAction.SUBJECT,
                subjectId = subjectWithUnknownWeights.id
            )
        }

        return null
    }

    /**
     * La clase de ahora mismo, o la siguiente, puntuada para competir con lo demas.
     *
     * **El texto dice algo.** Decia «Materia a las 15:30» y debajo «Tienes clase manana.
     * Revisa aula, asistencia y recordatorio», que es relleno: no hay nada ahi que no se
     * supiera ya. Ahora el titulo es la materia y la linea de abajo es lo que hace falta
     * para actuar: «Hasta las 21:30 · 505D» si estas dentro, «Empieza en 40 min · 103F» si
     * viene, «Manana a las 15:30» si es manana.
     *
     * La puntuacion es lo que la sienta en la mesa con las tareas (vencida 940, hoy 900,
     * manana 760) y las materias (critica 870, cerca de la meta 680):
     * - en curso, 960: lo que esta pasando gana a lo que vence;
     * - dentro de una hora, 890: por debajo de lo que vence hoy, por encima de una materia
     *   en rojo;
     * - mas tarde hoy, 720; manana, 560; despues, 450.
     */
    private fun classPriorityCandidate(
        sessions: List<ClassSession>,
        subjects: List<Subject>
    ): Pair<Int, HomePrioritySummary>? {
        if (sessions.isEmpty()) return null
        val today = LocalDate.now()
        val nowMinute = LocalTime.now().let { it.hour * 60 + it.minute }

        fun nombre(s: ClassSession): String = subjects.firstOrNull { it.id == s.subjectId }?.name
            ?: Textos.get(R.string.home_tu_proxima_clase)
        // El aula va delante del profesor en `location`, separados por el punto medio.
        fun conAula(texto: String, s: ClassSession): String {
            val aula = s.location.split('•', limit = 2).first().trim()
            return if (aula.isBlank()) texto else "$texto · $aula"
        }

        val enCurso = sessions
            .filter { it.occursOn(today.toEpochDay(), today.dayOfWeek.value) }
            .filter { nowMinute >= it.startMinute && nowMinute < it.endMinute }
            .minByOrNull { it.endMinute }
        if (enCurso != null) {
            val hasta = formatClassMinute(enCurso.endMinute)
            return 960 to HomePrioritySummary(
                title = nombre(enCurso),
                shortDescription = conAula(Textos.get(R.string.home_hasta_las, hasta), enCurso),
                action = HomePriorityAction.SCHEDULE,
                subjectId = enCurso.subjectId,
                timeframe = HomePriorityTimeframe.NOW
            )
        }

        val next = (0..7)
            .flatMap { offset ->
                val date = today.plusDays(offset.toLong())
                sessions
                    .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                    .filter { offset > 0 || it.startMinute >= nowMinute }
                    .map { date to it }
            }
            .sortedWith(compareBy<Pair<LocalDate, ClassSession>> { it.first }.thenBy { it.second.startMinute })
            .firstOrNull() ?: return null
        val (date, session) = next
        val hora = formatClassMinute(session.startMinute)
        val faltan = session.startMinute - nowMinute
        val diaDeLaSemana = date.dayOfWeek.getDisplayName(
            java.time.format.TextStyle.FULL,
            java.util.Locale.getDefault()
        )
        val (puntos, texto, cuando) = when {
            date == today && faltan <= 0 ->
                Triple(890, Textos.get(R.string.home_empieza_ahora_mismo), HomePriorityTimeframe.TODAY)
            date == today && faltan <= 60 ->
                Triple(890, Textos.get(R.string.home_empieza_en_min, faltan), HomePriorityTimeframe.TODAY)
            date == today ->
                Triple(720, Textos.get(R.string.home_hoy_a_las, hora), HomePriorityTimeframe.TODAY)
            date == today.plusDays(1) ->
                Triple(560, Textos.get(R.string.home_manana_a_las, hora), HomePriorityTimeframe.TOMORROW)
            else ->
                Triple(450, Textos.get(R.string.home_el_a_las, diaDeLaSemana, hora), HomePriorityTimeframe.LATER)
        }
        return puntos to HomePrioritySummary(
            title = nombre(session),
            shortDescription = conAula(texto, session),
            action = HomePriorityAction.SCHEDULE,
            subjectId = session.subjectId,
            timeframe = cuando
        )
    }

    private fun formatClassMinute(minute: Int): String = TaskDateUtils.formatTimeInput(
        LocalTime.of((minute / 60).coerceIn(0, 23), (minute % 60).coerceIn(0, 59))
    )
    private fun HomePrioritySummary.toDailyFocusItem(): DailyFocusItem {
        return DailyFocusItem(
            slotLabel = Textos.get(R.string.settings_backup_now),
            title = title,
            detail = shortDescription,
            minutesText = when (action) {
                HomePriorityAction.TASKS -> "3 min"
                HomePriorityAction.SUBJECT -> "5 min"
                else -> "5 min"
            },
            actionLabel = when (action) {
                HomePriorityAction.SUBJECT -> Textos.get(R.string.notif_btn_open)
                HomePriorityAction.SUBJECTS -> Textos.get(R.string.settings_backup_subjects)
                HomePriorityAction.TASKS -> Textos.get(R.string.tasks_review)
                HomePriorityAction.EXPENSES -> Textos.get(R.string.setup_mod_expenses_title)
                HomePriorityAction.TEMPLATES -> Textos.get(R.string.home_nav_assignments)
                HomePriorityAction.SCHEDULE -> Textos.get(R.string.home_action_schedule_short)
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
                title = Textos.get(R.string.home_gastos_bajo_control),
                shortDescription = Textos.get(R.string.home_buen_momento_para_revisar_si_tu),
                action = HomePriorityAction.EXPENSES
            )
            hasOpenWorks && index == 2 -> HomePrioritySummary(
                title = Textos.get(R.string.home_espacio_para_avanzar),
                shortDescription = Textos.get(R.string.home_aprovecha_un_bloque_corto_para_mover),
                action = HomePriorityAction.TEMPLATES
            )
            pendingTasks.isNotEmpty() && index == 3 -> HomePrioritySummary(
                title = Textos.get(R.string.home_buen_ritmo),
                shortDescription = Textos.get(R.string.home_ordena_una_tarea_pequena_y_deja),
                action = HomePriorityAction.TASKS
            )
            academicFocus != null -> academicFocus.toPrioritySummary()
            else -> HomePrioritySummary(
                title = Textos.get(R.string.home_dia_despejado),
                shortDescription = Textos.get(R.string.home_aprovecha_para_repasar_o_preparar_tus),
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
                val cuts = subject.cutScheme.cuts
                val average = GradeCalculator.calculateCurrentAverageByCuts(subject.grades, cuts)
                val evaluated = GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, cuts)
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
        return any { it.title == "Repaso breve" || it.title == "Ordenar pendientes" || it.title == "Quick review" || it.title == "Sort out pending tasks" }
    }

    private fun AcademicFocusSummary.toPrioritySummary(): HomePrioritySummary {
        val averageText = average?.let { GradingScaleUtils.formatGrade(it, gradingScale) }
        return HomePrioritySummary(
            title = if (averageText != null) {
                Textos.get(R.string.home_va_en, subjectName, averageText)
            } else {
                Textos.get(R.string.home_espera_su_primera_nota, subjectName)
            },
            shortDescription = if (averageText != null) {
                Textos.get(R.string.home_evaluado_falta_registrar, evaluatedPercentage.roundPercent(), remainingPercentage.roundPercent())
            } else {
                Textos.get(R.string.home_agrega_una_nota_para_activar_proyeccion)
            },
            action = HomePriorityAction.SUBJECT,
            subjectId = subjectId
        )
    }

    private fun AcademicFocusSummary.toDailyFocusItems(): List<DailyFocusItem> {
        val hasGrade = average != null
        return listOf(
            DailyFocusItem(
                slotLabel = Textos.get(R.string.settings_backup_now),
                title = if (hasGrade) {
                    Textos.get(R.string.home_actualizar, subjectName)
                } else {
                    Textos.get(R.string.home_agregar_primera_nota)
                },
                detail = if (hasGrade) {
                    Textos.get(R.string.home_registra_la_proxima_nota_o_revisa, remainingPercentage.roundPercent())
                } else {
                    Textos.get(R.string.home_convierte_esta_materia_en_un_tablero)
                },
                minutesText = if (hasGrade) "5 min" else "3 min",
                actionLabel = Textos.get(R.string.notif_btn_open),
                action = HomePriorityAction.SUBJECT,
                subjectId = subjectId
            )
        )
    }

    private fun Double.roundPercent(): String = "%.0f".format(this)

    private fun heroActionPrefix(): String {
        return when (LocalTime.now().hour) {
            in 5..11 -> Textos.get(R.string.home_arranca_con)
            in 18..23 -> Textos.get(R.string.home_deja_listo)
            else -> Textos.get(R.string.notif_next_step)
        }
    }

    /**
     * Las proximas paradas, de mañana en adelante.
     *
     * Mira siete dias hacia delante y mezcla las tres cosas que ocupan un dia: las clases que
     * toquen por su regla de repeticion, las tareas con fecha y los trabajos sin entregar. Hoy
     * queda fuera a proposito -- de hoy ya se encarga [todayTimelineItems], y repetirlo aqui
     * haria que la tarjeta dijera dos veces lo mismo.
     */
    private fun upcomingItems(
        sessions: List<ClassSession>,
        tasks: List<StudentTask>,
        works: List<AcademicWork>,
        subjectNameById: Map<String, String>
    ): List<HomeUpcomingItem> {
        val today = LocalDate.now()
        val dias = (1..7).map { today.plusDays(it.toLong()) }
        // Cada candidata guarda su dia y su minuto para poder ordenarlas entre si; lo que se
        // devuelve es solo la ficha.
        val candidatas = mutableListOf<Triple<LocalDate, Int, HomeUpcomingItem>>()

        dias.forEach { date ->
            sessions
                .filter { it.occursOn(date.toEpochDay(), date.dayOfWeek.value) }
                .forEach { session ->
                    candidatas += Triple(
                        date,
                        session.startMinute,
                        HomeUpcomingItem(
                            dayLabel = upcomingDayLabel(date, today),
                            timeText = formatClassMinute(session.startMinute),
                            title = subjectNameById[session.subjectId] ?: Textos.get(R.string.notif_cat_class),
                            subtitle = session.place.room.takeIf(String::isNotBlank)?.let { Textos.get(R.string.home_aula, it) }.orEmpty(),
                            kind = HomeTimelineKind.CLASS
                        )
                    )
                }
        }

        tasks.forEach { task ->
            val date = TaskDateUtils.fromMillis(task.dueDateMillis)
            if (date in dias) {
                candidatas += Triple(
                    date,
                    // Las entregas van al final de su dia: una clase de las 8 se hace antes
                    // que algo que solo tiene fecha.
                    24 * 60,
                    HomeUpcomingItem(
                        dayLabel = upcomingDayLabel(date, today),
                        timeText = Textos.get(R.string.home_entrega),
                        title = task.title,
                        subtitle = task.type.label(),
                        kind = task.type.timelineKind()
                    )
                )
            }
        }

        works
            .filterNot { it.status == AcademicWorkStatus.SUBMITTED }
            .forEach { work ->
                val millis = work.dueDateMillis ?: return@forEach
                val date = TaskDateUtils.fromMillis(millis)
                if (date in dias) {
                    candidatas += Triple(
                        date,
                        24 * 60,
                        HomeUpcomingItem(
                            dayLabel = upcomingDayLabel(date, today),
                            timeText = Textos.get(R.string.home_entrega),
                            title = work.title,
                            subtitle = work.subjectId?.let(subjectNameById::get).orEmpty(),
                            kind = HomeTimelineKind.WORK
                        )
                    )
                }
            }

        return candidatas
            .sortedWith(compareBy({ it.first }, { it.second }))
            .map { it.third }
            .take(3)
    }

    private fun upcomingDayLabel(date: LocalDate, today: LocalDate): String = when (date) {
        today.plusDays(1) -> Textos.get(R.string.schedule_identity_tomorrow)
        else -> date.dayOfWeek
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
            .replaceFirstChar { it.uppercase(java.util.Locale.getDefault()) }
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
                    subtitle = listOfNotNull(subject, Textos.get(R.string.home_listo, progress)).joinToString(" · "),
                    kind = HomeTimelineKind.WORK,
                    state = if (work.isDueTodayOrOverdue()) HomeTimelineState.CURRENT else HomeTimelineState.PENDING
                )
            }

        val focusItem = riskSubject
            ?.takeIf { it.severity != SubjectRiskSeverity.STABLE }
            ?.let {
                HomeTimelineSummary(
                    timeText = Textos.get(R.string.home_enfoque),
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

    /** Lo gastado en los siete dias anteriores al lunes de esta semana. */
    private fun previousWeekTotal(expenses: List<Expense>): Int {
        val inicio = ExpenseDateUtils.startOfWeek().minusDays(7)
        val fin = inicio.plusDays(6)
        return expenses
            .filter { ExpenseDateUtils.fromMillis(it.dateMillis) in inicio..fin }
            .sumOf { it.amount }
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
                cuts = subject.cutScheme.cuts,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade,
                passingGrade = passingGrade
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
                Textos.get(R.string.home_promedio_bajo_la_nota_minima, GradingScaleUtils.formatGrade(average, gradingScale))
            neededGrade != null && neededGrade in 0.0..maxGrade && remainingPercentage > 0.0 ->
                Textos.get(R.string.home_necesitas_en_lo_restante, GradingScaleUtils.formatGrade(neededGrade, gradingScale))
            average >= targetAverage ->
                Textos.get(R.string.home_va_sobre_la_meta_con, GradingScaleUtils.formatGrade(average, gradingScale))
            else ->
                Textos.get(R.string.home_revisa_los_proximos_porcentajes_para_recuper)
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
        if (!hasSubjects) return Textos.get(R.string.home_crea_tus_materias_para_ver_un)
        if (overdueTasks == 1) return Textos.get(R.string.home_overdue_one_first)
        if (overdueTasks > 1) return Textos.get(R.string.home_overdue_many_first, overdueTasks)
        if (riskSubject?.severity == SubjectRiskSeverity.CRITICAL) return Textos.get(R.string.home_necesita_atencion_academica_hoy, riskSubject.subjectName)
        if (riskSubject?.severity == SubjectRiskSeverity.ATTENTION) return Textos.get(R.string.home_esta_cerca_de_la_meta_pero, riskSubject.subjectName)
        if (nextAcademicWork != null) return Textos.get(R.string.home_tu_proximo_trabajo_es, nextAcademicWork.title)
        if (nextTask != null) return Textos.get(R.string.home_tu_proxima_accion_clara_es, nextTask.title)
        if (weeklyExpenseTotal > 0) return Textos.get(R.string.home_esta_semana_ya_tienes_gastos_registrados)
        return Textos.get(R.string.home_todo_esta_bajo_control_manten_notas)
    }

    private fun productivitySummary(completedTasks: Int, pendingTasks: Int, overdueTasks: Int): String {
        return when {
            completedTasks == 0 && pendingTasks == 0 -> Textos.get(R.string.home_sin_tareas_todavia)
            overdueTasks > 0 -> Textos.get(R.string.home_completadas_vencidas, completedTasks, overdueTasks)
            else -> Textos.get(R.string.home_completadas_pendientes, completedTasks, pendingTasks)
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
            overdueTasks > 0 -> Textos.get(R.string.home_cierra_una_pendiente_primero_despues_el, shortName)
            todayItems.any { it.state == HomeTimelineState.CURRENT } ->
                Textos.get(R.string.home_hoy_conviene_enfocarte_en_antes_de, todayItems.first { it.state == HomeTimelineState.CURRENT }.title)
            pendingTasks == 0 -> Textos.get(R.string.home_dia_tranquilo_perfecto_para_repasar_o, shortName)
            priority.action == HomePriorityAction.TEMPLATES ->
                Textos.get(R.string.home_small_progress_saves_pressure, priority.title.substringBefore(Textos.get(R.string.home_title_verb_separator)))
            else -> Textos.get(R.string.home_vas_bien_prioriza_una_cosa_importante, shortName)
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
            days < 0 -> Textos.get(R.string.home_vencida, time)
            days == 0L -> Textos.get(R.string.home_hoy, time)
            days == 1L -> Textos.get(R.string.home_manana, time)
            else -> Textos.get(R.string.home_en_dias, days, time)
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
        val due = dueDateMillis ?: return Textos.get(R.string.home_sin_fecha_2)
        val days = ChronoUnit.DAYS.between(TaskDateUtils.today(), TaskDateUtils.fromMillis(due))
        val time = due.timelineTimeSuffix()
        return when {
            days < 0 -> Textos.get(R.string.home_vencido, time)
            days == 0L -> Textos.get(R.string.home_hoy, time)
            days == 1L -> Textos.get(R.string.home_manana, time)
            else -> Textos.get(R.string.home_en_dias, days, time)
        }
    }

    private fun Long.timelineTimeSuffix(): String {
        val time = TaskDateUtils.timeFromMillis(this)
        return if (time == LocalTime.MIDNIGHT) "" else " ${TaskDateUtils.formatTimeInput(time)}"
    }

    private fun TaskType.label(): String {
        return when (this) {
            TaskType.WORKSHOP -> Textos.get(R.string.agenda_academic_type_workshop)
            TaskType.EXAM -> Textos.get(R.string.tasks_type_exam)
            TaskType.ESSAY -> Textos.get(R.string.agenda_academic_type_essay)
            TaskType.PRESENTATION -> Textos.get(R.string.agenda_academic_type_presentation)
            TaskType.RESEARCH -> Textos.get(R.string.agenda_academic_type_research)
            TaskType.TEST -> Textos.get(R.string.grade_type_quiz)
            TaskType.PRACTICE -> Textos.get(R.string.grade_type_practice)
            TaskType.PROJECT -> Textos.get(R.string.agenda_academic_type_project)
            TaskType.READING -> Textos.get(R.string.agenda_academic_type_reading)
            TaskType.OTHER -> Textos.get(R.string.home_tarea)
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
