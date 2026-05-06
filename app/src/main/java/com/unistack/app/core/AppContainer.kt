package com.unistack.app.core

import android.content.Context
import com.unistack.app.core.datastore.UserPreferencesDataSource
import com.unistack.app.feature_grades.data.RoomGradesRepository
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_tasks.data.RoomTasksRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.data.DataStoreUserRepository
import com.unistack.app.feature_user.domain.UserRepository

object AppContainer {
    lateinit var userRepository: UserRepository
        private set

    lateinit var gradesRepository: GradesRepository
        private set

    lateinit var tasksRepository: TasksRepository
        private set

    fun init(context: Context) {
        val dataSource = UserPreferencesDataSource(context.applicationContext)
        userRepository = DataStoreUserRepository(dataSource)

        val database = UniStackDatabase.getInstance(context.applicationContext)
        gradesRepository = RoomGradesRepository(
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao(),
            userRepository = userRepository
        )
        tasksRepository = RoomTasksRepository(
            taskDao = database.taskDao(),
            userRepository = userRepository
        )
    }
}
