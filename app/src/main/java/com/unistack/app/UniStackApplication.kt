package com.unistack.app

import android.app.Application
import com.unistack.app.core.notifications.ReminderCoordinator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class UniStackApplication : Application() {

    @Inject lateinit var userRepository: UserRepository
    @Inject lateinit var gradesRepository: GradesRepository
    @Inject lateinit var tasksRepository: TasksRepository
    @Inject lateinit var academicWorksRepository: AcademicWorksRepository
    @Inject lateinit var scheduleRepository: ScheduleRepository

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
    }
}
