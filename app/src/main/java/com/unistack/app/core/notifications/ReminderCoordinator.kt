package com.unistack.app.core.notifications

import android.content.Context
import com.unistack.app.feature_grades.domain.GradesRepository
import com.unistack.app.feature_notes.domain.NotesRepository
import com.unistack.app.feature_tasks.domain.TasksRepository
import com.unistack.app.feature_templates.domain.AcademicWorksRepository
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ScheduleRepository
import com.unistack.app.feature_user.domain.UserRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

/** Todo lo que se mira a la vez para decidir qué avisos hay que programar. */
private data class ScheduleSnapshot(
    val sessions: List<com.unistack.app.feature_schedule.domain.ClassSession>,
    val occurrences: List<ClassOccurrence>,
    val agendaEvents: List<com.unistack.app.feature_schedule.domain.AgendaEvent>,
    val notes: List<com.unistack.app.feature_notes.domain.QuickNote>,
    val terms: List<com.unistack.app.feature_terms.domain.AcademicTerm>
)

object ReminderCoordinator {
    private var job: Job? = null

    /* Lo que hace falta para reprogramar sin esperar a que cambie ningun dato. Se rellena en
       start() y lo usa el rearmado de madrugada: el combine de abajo solo reacciona a
       cambios, y a las tres de la manana no cambia nada. */
    private var rescheduleFromCurrentState: (() -> Unit)? = null

    /* Marcar la asistencia desde un boton del aviso, sin que haya ninguna pantalla viva. El
       broadcast puede levantar el proceso el, asi que no hay ViewModel al que pedirselo. */
    private var markFromNotification: ((String, Long, ClassAttendanceStatus) -> Unit)? = null

    /**
     * Guarda lo que se conteste en la notificacion.
     *
     * Silencioso si el proceso todavia no ha arrancado del todo: perder un toque es molesto,
     * pero reventar en un receptor se lleva por delante toda la app.
     */
    fun markAttendance(sessionId: String, epochDay: Long, status: ClassAttendanceStatus) {
        markFromNotification?.invoke(sessionId, epochDay, status)
    }

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
        scheduleRepository: ScheduleRepository,
        notesRepository: NotesRepository,
        termRepository: com.unistack.app.feature_terms.domain.AcademicTermRepository
    ) {
        if (job != null) return
        val scheduler = LocalReminderScheduler(context.applicationContext)
        markFromNotification = { sessionId, epochDay, status ->
            val existente = scheduleRepository.occurrences.value.firstOrNull {
                it.sessionId == sessionId && it.dateEpochDay == epochDay
            }
            scheduleRepository.saveOccurrence(
                (existente ?: ClassOccurrence(
                    id = "occurrence-$sessionId-$epochDay",
                    sessionId = sessionId,
                    dateEpochDay = epochDay,
                    updatedAt = System.currentTimeMillis()
                )).copy(status = status, updatedAt = System.currentTimeMillis())
            )
        }
        rescheduleFromCurrentState = {
            scheduler.schedule(
                profile = userRepository.userProfile.value,
                tasks = tasksRepository.tasks.value,
                works = academicWorksRepository.works.value,
                subjects = gradesRepository.subjects.value,
                classSessions = scheduleRepository.sessions.value,
                classOccurrences = scheduleRepository.occurrences.value,
                agendaEvents = scheduleRepository.agendaEvents.value,
                notes = notesRepository.notes.value,
                terms = termRepository.terms.value
            )
        }
        job = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            /*
             * Lo del calendario y las notas, en un solo flujo.
             *
             * `combine` admite cinco fuentes y ya iban cinco. Agrupar aqui lo que se mira junto
             * deja sitio sin tener que anidar otro combine dentro del de abajo.
             */
            val scheduleState = combine(
                scheduleRepository.sessions,
                scheduleRepository.occurrences,
                scheduleRepository.agendaEvents,
                notesRepository.notes,
                termRepository.terms
            ) { sessions, occurrences, agendaEvents, notes, terms ->
                ScheduleSnapshot(sessions, occurrences, agendaEvents, notes, terms)
            }
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
                    classSessions = schedule.sessions,
                    classOccurrences = schedule.occurrences,
                    agendaEvents = schedule.agendaEvents,
                    notes = schedule.notes,
                    terms = schedule.terms
                )
                if (profile != null || userRepository.didLoad) {
                    firstSchedule.complete(Unit)
                }
            }.collect {}
        }
    }
}
