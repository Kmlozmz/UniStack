package com.unistack.app.feature_home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.core.AppContainer
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_expenses.domain.ExpenseDateUtils
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_home.domain.NeededGradeSummary
import com.unistack.app.feature_home.domain.SubjectSummary
import com.unistack.app.feature_home.domain.TaskSummary
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeViewModel(
    private val gradesRepository: GradesRepository = AppContainer.gradesRepository,
    private val tasksRepository: TasksRepository = AppContainer.tasksRepository,
    private val expensesRepository: ExpensesRepository = AppContainer.expensesRepository,
    private val userRepository: UserRepository = AppContainer.userRepository
) : ViewModel() {
    val uiState: StateFlow<HomeUiState> = combine(
        gradesRepository.subjects,
        tasksRepository.tasks,
        expensesRepository.expenses,
        userRepository.userProfile,
        userRepository.currentUser
    ) { subjects, tasks, expenses, profile, user ->
        HomeUiState(summary = HomeUiState.emptySummary.copyFrom(subjects, tasks, expenses, profile, user))
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    private fun com.unistack.app.feature_home.domain.HomeSummary.copyFrom(
        subjects: List<Subject>,
        tasks: List<StudentTask>,
        expenses: List<Expense>,
        profile: UserProfile?,
        user: AppUser
    ): com.unistack.app.feature_home.domain.HomeSummary {
        val summaries = subjects.take(3).map { subject ->
            val average = GradeCalculator.calculateCurrentAverage(subject.grades)
            val evaluatedPercentage = subject.grades.sumOf { it.percentage }.coerceIn(0.0, 1.0)
            SubjectSummary(
                id = subject.id,
                name = subject.name,
                average = average,
                progress = evaluatedPercentage.toFloat(),
                type = subject.visualType
            )
        }

        val subjectsWithGrades = subjects.filter { it.grades.isNotEmpty() }
        val generalAverage = if (subjectsWithGrades.isEmpty()) {
            null
        } else {
            val validGrades = subjectsWithGrades.mapNotNull { sub ->
                GradeCalculator.calculateCurrentAverage(sub.grades)?.let { avg ->
                    GradeItem(
                        id = sub.id,
                        name = sub.name,
                        value = avg,
                        percentage = 1.0 / subjectsWithGrades.size
                    )
                }
            }
            if (validGrades.isEmpty()) null else GradeCalculator.calculateCurrentAverage(validGrades)
        }

        val focusSubject = subjects.firstOrNull { it.grades.isNotEmpty() }
        val neededGrade = focusSubject?.let { subject ->
            val currentWeightedPoints = GradeCalculator.calculateWeightedPoints(subject.grades)
            val remainingPercentage = (1.0 - subject.grades.sumOf { it.percentage }).coerceAtLeast(0.0)
            val maxGrade = profile?.let { GradingScaleUtils.maxGradeFor(it.gradingScale) } ?: 5.0
            val needed = GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = currentWeightedPoints,
                remainingPercentage = remainingPercentage,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
            if (needed == null || needed <= 0.0 || needed > maxGrade) {
                null
            } else {
                NeededGradeSummary(
                    subjectName = subject.name,
                    targetAverage = subject.targetAverage,
                    neededGrade = needed
                )
            }
        }
        val pendingTasks = tasks.filterNot { it.completed }
        val tasksToday = pendingTasks.count { TaskDateUtils.isToday(it.dueDateMillis) }
        val nextTask = pendingTasks.minByOrNull { it.dueDateMillis }?.let { task ->
            TaskSummary(
                title = task.title,
                dueText = TaskDateUtils.dueText(task.dueDateMillis),
                estimatedTimeText = TaskDateUtils.estimatedTimeText(task.estimatedMinutes)
            )
        }
        val weeklyExpenses = weeklyExpenseSummary(expenses)

        return copy(
            userName = profile?.preferredName?.takeIf { it.isNotBlank() }
                ?: user.displayName?.takeIf { it.isNotBlank() }
                ?: "Estudiante",
            avatarPhotoUrl = user.photoUrl,
            generalAverage = generalAverage,
            subjectsCount = subjects.size,
            tasksToday = tasksToday,
            subjects = summaries,
            neededGrade = neededGrade,
            nextTask = nextTask,
            weeklyExpenses = weeklyExpenses,
            gradingScale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE,
            enabledModules = profile?.enabledModules ?: setOf(AppModule.GRADES, AppModule.TASKS, AppModule.EXPENSES)
        )
    }

    private fun weeklyExpenseSummary(expenses: List<Expense>): com.unistack.app.feature_home.domain.ExpenseSummary? {
        val weekly = expenses.filter { ExpenseDateUtils.isInCurrentWeek(it.dateMillis) }
        if (weekly.isEmpty()) return null

        val start = ExpenseDateUtils.startOfWeek()
        val chartValues = (0..6).map { dayOffset ->
            val date = start.plusDays(dayOffset.toLong())
            weekly
                .filter { ExpenseDateUtils.fromMillis(it.dateMillis) == date }
                .sumOf { it.amount }
        }

        return com.unistack.app.feature_home.domain.ExpenseSummary(
            transport = weekly.filter { it.category == ExpenseCategory.TRANSPORT }.sumOf { it.amount },
            food = weekly.filter { it.category == ExpenseCategory.FOOD }.sumOf { it.amount },
            chartValues = chartValues
        )
    }
}
