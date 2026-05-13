package com.unistack.app.core

import android.content.Context
import com.unistack.app.core.datastore.UserPreferencesDataSource
import com.unistack.app.feature_billing.data.PlayBillingRepository
import com.unistack.app.feature_billing.domain.BillingRepository
import com.unistack.app.feature_expenses.data.RoomExpensesRepository
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.data.RoomGradesRepository
import com.unistack.app.feature_grades.data.local.UniStackDatabase
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_sync.data.FirebaseCloudBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_tasks.data.RoomTasksRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.data.DataStoreUserRepository
import com.unistack.app.feature_user.data.FirebaseGoogleAuthService
import com.unistack.app.feature_user.domain.AccountAuthService
import com.unistack.app.feature_user.domain.UserRepository

object AppContainer {
    lateinit var userRepository: UserRepository
        private set

    lateinit var gradesRepository: GradesRepository
        private set

    lateinit var tasksRepository: TasksRepository
        private set

    lateinit var expensesRepository: ExpensesRepository
        private set

    lateinit var accountAuthService: AccountAuthService
        private set

    lateinit var cloudBackupRepository: CloudBackupRepository
        private set

    lateinit var billingRepository: BillingRepository
        private set

    fun init(context: Context) {
        val appContext = context.applicationContext
        val dataSource = UserPreferencesDataSource(appContext)
        userRepository = DataStoreUserRepository(dataSource)

        accountAuthService = FirebaseGoogleAuthService()
        billingRepository = PlayBillingRepository(appContext)

        val database = UniStackDatabase.getInstance(appContext)
        gradesRepository = RoomGradesRepository(
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao(),
            userRepository = userRepository
        )
        tasksRepository = RoomTasksRepository(
            taskDao = database.taskDao(),
            userRepository = userRepository
        )
        expensesRepository = RoomExpensesRepository(
            expenseDao = database.expenseDao(),
            userRepository = userRepository
        )
        cloudBackupRepository = FirebaseCloudBackupRepository(
            context = appContext,
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository
        )
    }
}
