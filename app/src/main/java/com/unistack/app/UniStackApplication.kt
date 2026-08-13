package com.unistack.app

import android.app.Application
import androidx.work.Configuration
import com.unistack.app.core.notifications.ReminderCoordinator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_updates.data.UpdateCheckWorker
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_user.domain.UserRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Implementa [Configuration.Provider] para que WorkManager arranque **bajo demanda**, la primera
 * vez que alguien pide su instancia, en vez de con el inicializador automático del manifiesto.
 *
 * Con el automático, cualquier prueba que levante esta clase —y Robolectric levanta la
 * aplicación para todas— se encontraba WorkManager sin inicializar en cuanto se programaba el
 * trabajo, y reventaban hasta los tests de migración de la base de datos, que no tienen nada que
 * ver. Así la inicialización ocurre cuando hace falta y en cualquier entorno.
 */
@HiltAndroidApp
class UniStackApplication : Application(), Configuration.Provider {

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().build()


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
        // Y que siga mirando aunque la app no se abra: sin esto, enterarse de una versión nueva
        // dependía de cerrar el proceso y volver a arrancarlo.
        UpdateCheckWorker.schedule(this)
    }
}
