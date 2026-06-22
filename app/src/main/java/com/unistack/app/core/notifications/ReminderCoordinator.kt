package com.unistack.app.core.notifications

import android.content.Context
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object ReminderCoordinator {
    private var job: Job? = null

    fun start(
        context: Context,
        userRepository: UserRepository,
        gradesRepository: GradesRepository,
        tasksRepository: TasksRepository,
        academicWorksRepository: AcademicWorksRepository,
        scheduleRepository: ScheduleRepository
    ) {
        if (job != null) return
        val scheduler = LocalReminderScheduler(context.applicationContext)
        job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val scheduleState = combine(
                scheduleRepository.sessions,
                scheduleRepository.occurrences
            ) { sessions, occurrences -> sessions to occurrences }
            combine(
                userRepository.userProfile,
                gradesRepository.subjects,
                tasksRepository.tasks,
                academicWorksRepository.works,
                scheduleState
            ) { profile, subjects, tasks, works, schedule ->
                scheduler.schedule(
                    profile = profile,
                    tasks = tasks,
                    works = works,
                    subjects = subjects,
                    classSessions = schedule.first,
                    classOccurrences = schedule.second
                )
            }.collect {}
        }
    }
}
