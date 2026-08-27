package com.unistack.app.core.di

import android.content.Context
import com.unistack.app.core.datastore.UserPreferencesDataSource
import com.unistack.app.feature_expenses.data.RoomExpensesRepository
import com.unistack.app.feature_expenses.data.local.ExpenseDao
import com.unistack.app.feature_expenses.domain.ExpensesRepository
import com.unistack.app.feature_grades.data.RoomGradesRepository
import com.unistack.app.feature_grades.data.local.GradeDao
import com.unistack.app.feature_grades.data.local.SubjectDao
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.data.RoomScheduleRepository
import com.unistack.app.feature_schedule.data.local.AgendaEventDao
import com.unistack.app.feature_terms.data.RoomAcademicTermRepository
import com.unistack.app.feature_terms.data.local.AcademicTermDao
import com.unistack.app.feature_terms.domain.AcademicTermRepository
import com.unistack.app.feature_schedule.data.local.ClassOccurrenceDao
import com.unistack.app.feature_schedule.data.local.ClassSessionDao
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_sync.data.FirebaseCloudBackupRepository
import com.unistack.app.feature_sync.data.LocalJsonBackupRepository
import com.unistack.app.feature_sync.domain.CloudBackupRepository
import com.unistack.app.feature_sync.domain.LocalBackupRepository
import com.unistack.app.feature_tasks.data.RoomTasksRepository
import com.unistack.app.feature_tasks.data.local.TaskDao
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.data.RoomAcademicWorksRepository
import com.unistack.app.feature_templates.data.local.AcademicWorkDao
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_user.data.DataStoreUserRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Provides
    @Singleton
    fun provideUserRepository(dataSource: UserPreferencesDataSource): UserRepository =
        DataStoreUserRepository(dataSource)

    @Provides
    @Singleton
    fun provideGradesRepository(
        subjectDao: SubjectDao,
        gradeDao: GradeDao,
        userRepository: UserRepository
    ): GradesRepository = RoomGradesRepository(
        subjectDao = subjectDao,
        gradeDao = gradeDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideTasksRepository(
        taskDao: TaskDao,
        userRepository: UserRepository
    ): TasksRepository = RoomTasksRepository(
        taskDao = taskDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideExpensesRepository(
        expenseDao: ExpenseDao,
        userRepository: UserRepository
    ): ExpensesRepository = RoomExpensesRepository(
        expenseDao = expenseDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideAcademicWorksRepository(
        academicWorkDao: AcademicWorkDao,
        userRepository: UserRepository
    ): AcademicWorksRepository = RoomAcademicWorksRepository(
        academicWorkDao = academicWorkDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideAcademicTermRepository(
        academicTermDao: AcademicTermDao,
        userRepository: UserRepository
    ): AcademicTermRepository = RoomAcademicTermRepository(
        dao = academicTermDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideScheduleRepository(
        classSessionDao: ClassSessionDao,
        classOccurrenceDao: ClassOccurrenceDao,
        agendaEventDao: AgendaEventDao,
        userRepository: UserRepository
    ): ScheduleRepository = RoomScheduleRepository(
        dao = classSessionDao,
        occurrenceDao = classOccurrenceDao,
        agendaEventDao = agendaEventDao,
        userRepository = userRepository
    )

    @Provides
    @Singleton
    fun provideCloudBackupRepository(
        @ApplicationContext context: Context,
        userRepository: UserRepository,
        gradesRepository: GradesRepository,
        tasksRepository: TasksRepository,
        expensesRepository: ExpensesRepository,
        academicWorksRepository: AcademicWorksRepository,
        scheduleRepository: ScheduleRepository
    ): CloudBackupRepository = FirebaseCloudBackupRepository(
        context = context,
        userRepository = userRepository,
        gradesRepository = gradesRepository,
        tasksRepository = tasksRepository,
        expensesRepository = expensesRepository,
        academicWorksRepository = academicWorksRepository,
        scheduleRepository = scheduleRepository
    )

    @Provides
    @Singleton
    fun provideLocalBackupRepository(
        userRepository: UserRepository,
        gradesRepository: GradesRepository,
        tasksRepository: TasksRepository,
        expensesRepository: ExpensesRepository,
        academicWorksRepository: AcademicWorksRepository,
        scheduleRepository: ScheduleRepository
    ): LocalBackupRepository = LocalJsonBackupRepository(
        userRepository = userRepository,
        gradesRepository = gradesRepository,
        tasksRepository = tasksRepository,
        expensesRepository = expensesRepository,
        academicWorksRepository = academicWorksRepository,
        scheduleRepository = scheduleRepository
    )
}
