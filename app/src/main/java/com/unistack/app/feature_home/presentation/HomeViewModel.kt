package com.unistack.app.feature_home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.feature_expenses.domain.Expense
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val gradesRepository: GradesRepository,
    private val tasksRepository: TasksRepository,
    private val expensesRepository: ExpensesRepository,
    private val academicWorksRepository: AcademicWorksRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    private val homeContent = combine(
        gradesRepository.subjects,
        tasksRepository.tasks,
        expensesRepository.expenses,
        academicWorksRepository.works,
        scheduleRepository.sessions
    ) { subjects, tasks, expenses, works, classSessions ->
        HomeContent(
            subjects = subjects,
            tasks = tasks,
            expenses = expenses,
            works = works,
            classSessions = classSessions
        )
    }

    val uiState: StateFlow<HomeUiState> = combine(
        homeContent,
        userRepository.userProfile,
        userRepository.currentUser
    ) { content, profile, user ->
        HomeUiState(
            summary = HomeSummaryFactory.create(
                content = content,
                profile = profile,
                user = user
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )
}

internal data class HomeContent(
    val subjects: List<Subject>,
    val tasks: List<StudentTask>,
    val expenses: List<Expense>,
    val works: List<AcademicWork>,
    val classSessions: List<ClassSession> = emptyList()
)
