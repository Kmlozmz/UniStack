package com.unistack.app.feature_home.domain

import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AppModule

data class HomeSummary(
    val userName: String,
    // En qué está estudiando, para la cabecera del panel lateral: «Universidad · Ing. de
    // Sistemas». Vacío mientras no haya perfil, y entonces la línea no se pinta.
    val educationLine: String = "",
    val avatarPhotoUrl: String?,
    val dashboardMessage: String,
    val priority: HomePrioritySummary,
    val dailyFocusItems: List<DailyFocusItem>,
    val generalAverage: Double?,
    val subjectsCount: Int,
    val tasksToday: Int,
    val overdueTasks: Int,
    val pendingTasks: Int,
    val openAcademicWorks: Int,
    val subjects: List<SubjectSummary>,
    val riskSubject: SubjectRiskSummary?,
    val neededGrade: NeededGradeSummary?,
    val nextTask: TaskSummary?,
    val nextAcademicWork: AcademicWorkSummary?,
    val todayItems: List<HomeTimelineSummary>,
    /**
     * Lo que viene despues de hoy, para cuando hoy no hay nada.
     *
     * La tarjeta de «Hoy» se quedaba en blanco con un mensaje de calma y nada mas. Esto es
     * lo que la llena: las proximas paradas de los siguientes dias -- clases, entregas y
     * trabajos --, que existen en los datos pero no tenian donde salir. El hero solo
     * anuncia **una**, y siempre la primera.
     */
    val upcomingItems: List<HomeUpcomingItem> = emptyList(),
    val weeklyExpenses: ExpenseSummary?,
    val weeklyExpenseTotal: Int,
    /**
     * Lo de la semana anterior, solo para comparar.
     *
     * El pie de la casilla decia «en 1 dia», que suena a reproche y ademas no ayuda a
     * decidir nada. Con la semana pasada al lado, la cifra pasa a significar algo: no es
     * lo mismo llevar 22.400 subiendo que bajando.
     */
    val previousWeekExpenseTotal: Int = 0,
    val productivitySummary: String,
    val companionInsight: String,
    val gradingScale: GradingScale = GradingScale.ZERO_TO_FIVE,
    val enabledModules: Set<AppModule> = setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
)

data class HomePrioritySummary(
    val title: String,
    val shortDescription: String,
    val action: HomePriorityAction,
    val subjectId: String? = null,
    /**
     * Cuándo es lo que anuncia.
     *
     * El rótulo del hero decía «lo primero de hoy» pasara lo que pasara, y la próxima clase
     * puede caer dentro de dos días: se anunciaba como cosa de hoy algo que no lo era. Lo
     * decide quien construye la prioridad, que es el único que sabe la fecha.
     */
    val timeframe: HomePriorityTimeframe = HomePriorityTimeframe.TODAY
)

/** Para cuándo es la prioridad que enseña el hero. */
enum class HomePriorityTimeframe {
    TODAY,
    TOMORROW,
    LATER
}

enum class HomePriorityAction {
    SUBJECT,
    SUBJECTS,
    TASKS,
    EXPENSES,
    TEMPLATES,
    SCHEDULE
}

data class DailyFocusItem(
    val slotLabel: String,
    val title: String,
    val detail: String,
    val minutesText: String,
    val actionLabel: String,
    val action: HomePriorityAction,
    val subjectId: String? = null
)

data class NeededGradeSummary(
    val subjectName: String,
    val targetAverage: Double,
    val neededGrade: Double
)

data class SubjectRiskSummary(
    val subjectId: String,
    val subjectName: String,
    val detail: String,
    val severity: SubjectRiskSeverity
)

enum class SubjectRiskSeverity {
    STABLE,
    ATTENTION,
    CRITICAL
}

data class AcademicWorkSummary(
    val id: String,
    val subjectId: String?,
    val title: String,
    val dueText: String,
    val progress: Float
)

/** Una parada de los proximos dias: el dia, la hora y que es. */
data class HomeUpcomingItem(
    val dayLabel: String,
    val timeText: String,
    val title: String,
    val subtitle: String,
    val kind: HomeTimelineKind
)

data class HomeTimelineSummary(
    val timeText: String,
    val title: String,
    val subtitle: String,
    val kind: HomeTimelineKind,
    val state: HomeTimelineState
)

enum class HomeTimelineKind {
    CLASS,
    TASK,
    WORK,
    EXAM,
    FOCUS
}

enum class HomeTimelineState {
    CURRENT,
    PENDING,
    DONE
}
