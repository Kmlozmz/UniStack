package com.unistack.app

import android.app.Application
import androidx.work.Configuration
import com.unistack.app.core.notifications.ReminderCoordinator
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_updates.data.UpdateCheckWorker
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_user.domain.UserRepository
import com.unistack.app.core.utils.Textos
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
    @Inject lateinit var notesRepository: NotesRepository
    @Inject lateinit var updateRepository: UpdateRepository

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        // Antes que nada: lo primero que arranca son los avisos, y ya piden textos.
        Textos.desde(this)
        ReminderCoordinator.start(
            context = this,
            userRepository = userRepository,
            gradesRepository = gradesRepository,
            tasksRepository = tasksRepository,
            academicWorksRepository = academicWorksRepository,
            scheduleRepository = scheduleRepository,
            notesRepository = notesRepository
        )
        /*
         * **Que falle la comprobacion no puede cerrar la app.**
         *
         * `checkForUpdatesIfDue` lanza a proposito cuando GitHub o la red no responden: eso es
         * para WorkManager, que necesita el fallo para reintentar mas tarde. Pero aqui la
         * excepcion subia al `launch`, y una corrutina que revienta en un `CoroutineScope`
         * suelto acaba en el manejador por defecto del hilo — es decir, cierra el proceso.
         *
         * El sintoma era exacto: abrir la app sin red y que se fuera al «Send feedback» de
         * Android con un `IOException: No se pudo verificar actualizaciones`. Arrancar sin
         * conexion es lo mas normal del mundo y no es motivo para nada.
         *
         * El reintento no se pierde: el trabajo programado justo debajo sigue haciendo la
         * misma comprobacion, y ahi el fallo si sirve de algo.
         */
        appScope.launch {
            runCatching { updateRepository.checkForUpdatesIfDue() }
        }
        // Y que siga mirando aunque la app no se abra: sin esto, enterarse de una versión nueva
        // dependía de cerrar el proceso y volver a arrancarlo.
        UpdateCheckWorker.schedule(this)
    }
}
