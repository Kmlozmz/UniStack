package com.unistack.app

import android.app.Application
import com.unistack.app.core.notifications.ReminderCoordinator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@HiltAndroidApp
class UniStackApplication : Application() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var gradesRepository: GradesRepository
    @Inject lateinit var tasksRepository: TasksRepository
    @Inject lateinit var academicWorksRepository: AcademicWorksRepository
    @Inject lateinit var scheduleRepository: ScheduleRepository
    @Inject lateinit var updateRepository: UpdateRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        ReminderCoordinator.start(
            context = this,
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            academicWorksRepository = academicWorksRepository,
            scheduleRepository = scheduleRepository
        )
        appScope.launch {
            updateRepository.checkForUpdatesIfDue()
        }
    }
}
