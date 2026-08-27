package com.unistack.app.core.notifications

import android.content.Context
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

object ReminderCoordinator {
    private var job: Job? = null

    /* Lo que hace falta para reprogramar sin esperar a que cambie ningun dato. Se rellena en
       start() y lo usa el rearmado de madrugada: el combine de abajo solo reacciona a
       cambios, y a las tres de la manana no cambia nada. */
    private var rescheduleFromCurrentState: (() -> Unit)? = null

    /* BootReceiver necesita saber cuándo se han reprogramado las alarmas para
       soltar el broadcast. No vale con esperar «la primera pasada»: el combine
       emite ya con el perfil en null porque su StateFlow arranca así mientras
       DataStore lee, y schedule() con perfil nulo solo cancela. Se completa,
       entonces, en la primera pasada que de verdad reprograma: la que llega con
       perfil, o la que confirma que el perfil leído está vacío. */
    private val firstSchedule = CompletableDeferred<Unit>()

    suspend fun awaitFirstSchedule() {
        firstSchedule.await()
    }

    /**
     * Vuelve a programar con lo que hay ahora mismo, sin esperar a que cambie nada.
     *
     * Si el proceso acaba de nacer para atender el rearmado, esto todavia esta vacio y no hace
     * falta: `start()` corre en `onCreate()` de la Application y su primera pasada ya
     * reprograma. Con el proceso vivo, en cambio, es la unica via.
     */
    fun rescheduleNow() {
        rescheduleFromCurrentState?.invoke()
    }

    fun start(
        context: Context,
        userRepository: UserRepository,
        gradesRepository: GradesRepository,
        tasksRepository: TasksRepository,
        academicWorksRepository: AcademicWorksRepository,
        scheduleRepository: ScheduleRepository
    ) {
        if (job != null) return
        val scheduler = LocalReminderScheduler(context.applicationContext)
        rescheduleFromCurrentState = {
            scheduler.schedule(
                profile = userRepository.userProfile.value,
                tasks = tasksRepository.tasks.value,
                works = academicWorksRepository.works.value,
                subjects = gradesRepository.subjects.value,
                classSessions = scheduleRepository.sessions.value,
                classOccurrences = scheduleRepository.occurrences.value,
                agendaEvents = scheduleRepository.agendaEvents.value
            )
        }
        job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            val scheduleState = combine(
                scheduleRepository.sessions,
                scheduleRepository.occurrences,
                scheduleRepository.agendaEvents
            ) { sessions, occurrences, agendaEvents -> Triple(sessions, occurrences, agendaEvents) }
            combine(
                userRepository.userProfile,
                gradesRepository.subjects,
                tasksRepository.tasks,
                academicWorksRepository.works,
                scheduleState
            ) { profile, subjects, tasks, works, schedule ->
                scheduler.schedule(
                    profile = profile,
                    tasks = tasks,
                    works = works,
                    subjects = subjects,
                    classSessions = schedule.first,
                    classOccurrences = schedule.second,
                    agendaEvents = schedule.third
                )
                if (profile != null || userRepository.didLoad) {
                    firstSchedule.complete(Unit)
                }
            }.collect {}
        }
    }
}
