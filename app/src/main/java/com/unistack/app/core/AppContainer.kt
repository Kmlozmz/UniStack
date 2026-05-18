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
import com.unistack.app.feature_sync.data.LocalJsonBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_templates.data.RoomAcademicWorksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_tasks.data.RoomTasksRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_user.data.DataStoreUserRepository
import com.unistack.app.feature_user.data.FirebaseGoogleAuthService
import com.unistack.app.feature_user.domain.AccountAuthService
import com.unistack.app.feature_user.domain.UserRepository

data class AppDependencies(
    val userRepository: UserRepository,
    val gradesRepository: GradesRepository,
    val tasksRepository: TasksRepository,
    val expensesRepository: ExpensesRepository,
    val academicWorksRepository: AcademicWorksRepository,
    val accountAuthService: AccountAuthService,
    val cloudBackupRepository: CloudBackupRepository,
    val localBackupRepository: LocalBackupRepository,
    val billingRepository: BillingRepository
)

object AppDependencyFactory {
    fun create(context: Context): AppDependencies {
        val appContext = context.applicationContext
        val dataSource = UserPreferencesDataSource(appContext)
        val userRepository = DataStoreUserRepository(dataSource)
        val accountAuthService = FirebaseGoogleAuthService()
        val billingRepository = PlayBillingRepository(appContext)
        val database = UniStackDatabase.getInstance(appContext)
        val gradesRepository = RoomGradesRepository(
            subjectDao = database.subjectDao(),
            gradeDao = database.gradeDao(),
            userRepository = userRepository
        )
        val tasksRepository = RoomTasksRepository(
            taskDao = database.taskDao(),
            userRepository = userRepository
        )
        val expensesRepository = RoomExpensesRepository(
            expenseDao = database.expenseDao(),
            userRepository = userRepository
        )
        val academicWorksRepository = RoomAcademicWorksRepository(
            academicWorkDao = database.academicWorkDao(),
            userRepository = userRepository
        )
        val cloudBackupRepository = FirebaseCloudBackupRepository(
            context = appContext,
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository,
            academicWorksRepository = academicWorksRepository
        )
        val localBackupRepository = LocalJsonBackupRepository(
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository,
            academicWorksRepository = academicWorksRepository
        )

        return AppDependencies(
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            expensesRepository = expensesRepository,
            academicWorksRepository = academicWorksRepository,
            accountAuthService = accountAuthService,
            cloudBackupRepository = cloudBackupRepository,
            localBackupRepository = localBackupRepository,
            billingRepository = billingRepository
        )
    }
}

object AppContainer {
    lateinit var userRepository: UserRepository
        private set

    lateinit var gradesRepository: GradesRepository
        private set

    lateinit var tasksRepository: TasksRepository
        private set

    lateinit var expensesRepository: ExpensesRepository
        private set

    lateinit var academicWorksRepository: AcademicWorksRepository
        private set

    lateinit var accountAuthService: AccountAuthService
        private set

    lateinit var cloudBackupRepository: CloudBackupRepository
        private set

    lateinit var localBackupRepository: LocalBackupRepository
        private set

    lateinit var billingRepository: BillingRepository
        private set

    fun init(context: Context) {
        install(AppDependencyFactory.create(context))
    }

    fun install(dependencies: AppDependencies) {
        userRepository = dependencies.userRepository
        gradesRepository = dependencies.gradesRepository
        tasksRepository = dependencies.tasksRepository
        expensesRepository = dependencies.expensesRepository
        academicWorksRepository = dependencies.academicWorksRepository
        accountAuthService = dependencies.accountAuthService
        cloudBackupRepository = dependencies.cloudBackupRepository
        localBackupRepository = dependencies.localBackupRepository
        billingRepository = dependencies.billingRepository
    }
}
