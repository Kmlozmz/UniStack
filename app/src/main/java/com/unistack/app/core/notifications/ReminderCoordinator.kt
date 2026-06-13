package com.unistack.app.core.notifications

import android.content.Context
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
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
        academicWorksRepository: AcademicWorksRepository
    ) {
        if (job != null) return
        val scheduler = LocalReminderScheduler(context.applicationContext)
        job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            combine(
                userRepository.userProfile,
                gradesRepository.subjects,
                tasksRepository.tasks,
                academicWorksRepository.works
            ) { profile, subjects, tasks, works ->
                scheduler.schedule(profile, tasks, works, subjects)
            }.collect {}
        }
    }
}
