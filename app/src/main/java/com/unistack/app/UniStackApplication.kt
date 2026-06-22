package com.unistack.app

import android.app.Application
import com.unistack.app.core.AppContainer
import com.unistack.app.core.notifications.ReminderCoordinator

class UniStackApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        ReminderCoordinator.start(
            context = this,
            userRepository = AppContainer.userRepository,
            gradesRepository = AppContainer.gradesRepository,
            tasksRepository = AppContainer.tasksRepository,
            academicWorksRepository = AppContainer.academicWorksRepository,
            scheduleRepository = AppContainer.scheduleRepository
        )
    }
}
