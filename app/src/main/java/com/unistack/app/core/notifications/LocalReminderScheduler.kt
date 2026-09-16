package com.unistack.app.core.notifications

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.unistack.app.MainActivity
import com.unistack.app.R
import com.unistack.app.core.navigation.AppRoutes
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_schedule.domain.ClassOccurrence
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.domain.AgendaEvent
import com.unistack.app.feature_schedule.domain.SessionPlace
import com.unistack.app.feature_notes.domain.NoteMarkdown
import com.unistack.app.feature_notes.domain.NoteText
import com.unistack.app.feature_notes.domain.QuickNote
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_terms.domain.AcademicTerm
import com.unistack.app.feature_terms.domain.PropuestaDelSiguientePeriodo
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.unistack.app.core.utils.Textos

/* El canal viejo nace con IMPORTANCE_DEFAULT, que en Android nunca muestra
   ventana emergente. La importancia de un canal ya creado no se puede subir
   por código —el sistema ignora el cambio para respetar al usuario—, así que
   la única salida es publicar en canales nuevos y borrar el anterior. */
private const val LEGACY_CHANNEL_ID = "unistack_reminders"

/* Lo que tiene hora y se puede perder: entregas, clases, vencidos. Asoma. */
private const val CHANNEL_ID_ALERTS = "unistack_alerts_v2"

/* Lo que solo informa: resumen del día, avisos de notas. No interrumpe. */
private const val CHANNEL_ID_DIGEST = "unistack_digest_v2"

private const val BRAND_COLOR = 0xFF5B46E0.toInt()

/*
 * Se programa por tiempo, no por cantidad.
 *
 * Antes se cogian los 8 primeros de cada tipo y el resto no avisaba nunca: con horario cargado
 * te quedabas sin recordatorio de las clases del final del dia. Pero quitar el tope a secas
 * tampoco vale, porque un semestre entero son cientos de alarmas vivas y cada cambio de datos
 * las cancela y las vuelve a poner todas.
 *
 * La ventana son 48 horas y el rearmado es diario, asi que hay un dia entero de margen: si un
 * rearmado se pierde, el siguiente todavia llega a tiempo.
 */
private const val SCHEDULING_WINDOW_MILLIS = 48L * 60L * 60L * 1000L

/* Cuantas materias pueden darte consejo el mismo dia. Esto si es cantidad: son avisos que no
   se pierden por no darse hoy, y cinco seguidos serian ruido. */
private const val MAX_SMART_SUBJECT_REMINDERS = 3
private const val REQUEST_CODE_PREFS = "unistack_scheduled_notifications"
private const val REQUEST_CODE_SET = "request_codes"
private const val DELIVERED_LATE_SET = "delivered_late"
private const val DAILY_DIGEST_REQUEST_CODE = 910060001
private const val REARM_REQUEST_CODE = 910060002
/* De madrugada: a esa hora la ventana del dia siguiente ya esta completa y no compite con
   ningun aviso real. */
private const val REARM_HOUR = 3

/* Lo que se espera tras el final de una clase antes de preguntar si asististe. */
private const val ATTENDANCE_PROMPT_DELAY_MINUTES = 20L
private const val ATTENDANCE_CATCHUP_DELAY_MINUTES = 60L
private const val ATTENDANCE_CATCHUP_REQUEST_CODE = 910060003
private const val EXTRA_TITLE = "title"
private const val EXTRA_BODY = "body"
internal const val EXTRA_NOTIFICATION_ID = "notification_id"
private const val EXTRA_TARGET_ROUTE = "target_route"
private const val EXTRA_SUBTEXT = "subtext"
private const val EXTRA_CHANNEL_ID = "channel_id"
internal const val EXTRA_REARM = "rearm"

class LocalReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scheduledRequestCodes = mutableSetOf<Int>()
    private val prefs = context.getSharedPreferences(REQUEST_CODE_PREFS, Context.MODE_PRIVATE)

    fun schedule(
        profile: UserProfile?,
        tasks: List<StudentTask>,
        works: List<AcademicWork>,
        subjects: List<Subject> = emptyList(),
        classSessions: List<ClassSession> = emptyList(),
        classOccurrences: List<ClassOccurrence> = emptyList(),
        agendaEvents: List<AgendaEvent> = emptyList(),
        notes: List<QuickNote> = emptyList(),
        terms: List<AcademicTerm> = emptyList()
    ) {
        createChannel()
        cancelPrevious()
        val currentProfile = profile ?: run {
            persistScheduledRequestCodes()
            return
        }
        avisaSiTeQuedasSinFaltas(currentProfile, subjects, classSessions, classOccurrences)
        scheduleNoteReminders(currentProfile, notes, subjects)
        val leadMillis = currentProfile.reminderLeadHours.coerceIn(1, 168) * 60L * 60L * 1000L

        if (AppModule.TASKS in currentProfile.enabledModules && currentProfile.taskRemindersEnabled) {
            tasks
                .filter { !it.completed && TaskDateUtils.hasExplicitTime(it.dueDateMillis) }
                .sortedBy { it.dueDateMillis }
                .forEach { task ->
                    // El nombre de la materia va en el subtítulo, junto al de la
                    // app: se lee de un vistazo sin robarle sitio al título, que
                    // se reserva para lo único que identifica el aviso.
                    val subjectLabel = subjects.firstOrNull { it.id == task.subjectId }?.name ?: Textos.get(R.string.notif_sub_task)
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = task.id.stableRequestCode("task-lead"),
                        triggerAtMillis = task.dueDateMillis - leadMillis,
                        subText = subjectLabel,
                        title = task.title,
                        body = "${TaskDateUtils.dueText(task.dueDateMillis).sentenceCase()}.",
                        targetRoute = AppRoutes.editTask(task.id),
                        eventAtMillis = task.dueDateMillis
                    )
                    if (currentProfile.overdueRemindersEnabled) {
                        scheduleReminder(
                            profile = currentProfile,
                            requestCode = task.id.stableRequestCode("task-overdue"),
                            triggerAtMillis = task.dueDateMillis + 60L * 60L * 1000L,
                            subText = subjectLabel,
                            title = Textos.get(R.string.notif_overdue_title, task.title),
                            body = Textos.get(R.string.notif_task_overdue_body),
                            targetRoute = AppRoutes.editTask(task.id)
                        )
                    }
                }
        }

        if (AppModule.TASKS in currentProfile.enabledModules && currentProfile.pendingGradeRemindersEnabled) {
            tasks
                .filter {
                    it.completed &&
                        it.gradingStatus == TaskGradingStatus.AWAITING_GRADE &&
                        it.subjectId != null &&
                        it.completedAt != null
                }
                .sortedByDescending { it.completedAt }
                .forEach { task ->
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = task.id.stableRequestCode("task-grade-pending"),
                        triggerAtMillis = requireNotNull(task.completedAt) + 24L * 60L * 60L * 1000L,
                        subText = Textos.get(R.string.notif_sub_pending_grade),
                        title = Textos.get(R.string.notif_pending_grade_title, task.title),
                        body = Textos.get(R.string.notif_pending_grade_body),
                        targetRoute = AppRoutes.Tasks,
                        channelId = CHANNEL_ID_DIGEST
                    )
                }
        }

        if (AppModule.ACADEMIC_TEMPLATES in currentProfile.enabledModules && currentProfile.academicWorkRemindersEnabled) {
            works.filterNot { it.status == AcademicWorkStatus.SUBMITTED }
                .filter { it.dueDateMillis != null }
                .sortedBy { it.dueDateMillis ?: Long.MAX_VALUE }
                .forEach { work ->
                    val dueDateMillis = work.dueDateMillis ?: return@forEach
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = work.id.stableRequestCode("work-lead"),
                        triggerAtMillis = dueDateMillis - leadMillis,
                        subText = Textos.get(R.string.notif_sub_work),
                        title = work.title,
                        body = "${TaskDateUtils.dueText(dueDateMillis).sentenceCase()}.",
                        targetRoute = AppRoutes.AcademicTemplates,
                        eventAtMillis = dueDateMillis
                    )
                    if (currentProfile.overdueRemindersEnabled) {
                        scheduleReminder(
                            profile = currentProfile,
                            requestCode = work.id.stableRequestCode("work-overdue"),
                            triggerAtMillis = dueDateMillis + 60L * 60L * 1000L,
                            subText = Textos.get(R.string.notif_sub_work),
                            title = Textos.get(R.string.notif_overdue_title, work.title),
                            body = Textos.get(R.string.notif_work_overdue_body),
                            targetRoute = AppRoutes.AcademicTemplates
                        )
                    }
                }
        }

        if (AppModule.GRADES in currentProfile.enabledModules && currentProfile.gradeInsightRemindersEnabled) {
            scheduleSubjectInsights(currentProfile, subjects)
        }

        scheduleClassReminders(currentProfile, subjects, classSessions, classOccurrences)
        scheduleAgendaEventReminders(currentProfile, agendaEvents)
        scheduleTermReminders(currentProfile, terms)

        /*
         * El resumen se apaga solo, no arrastrado por los demas.
         *
         * Antes salia si estaba encendido cualquiera de los cinco tipos de aviso, asi que
         * quien queria los recordatorios de clase se comia el resumen sin poder evitarlo.
         * Y `daysFromNow = 1` lo mandaba siempre a manana: instalar la app a las seis de la
         * manana significaba no ver el primero hasta el dia siguiente. Con cero, nextTriggerAt
         * ya se encarga de saltar a manana solo si la hora de hoy ha pasado.
         */
        if (currentProfile.dailyDigestEnabled) {
            scheduleReminder(
                profile = currentProfile,
                requestCode = DAILY_DIGEST_REQUEST_CODE,
                triggerAtMillis = nextTriggerAt(
                    hour = currentProfile.dailyDigestHour.coerceIn(0, 23),
                    minute = currentProfile.dailyDigestMinute.coerceIn(0, 59),
                    daysFromNow = 0
                ),
                subText = Textos.get(R.string.notif_sub_digest),
                title = Textos.get(R.string.notif_digest_title),
                body = smartDigestBody(currentProfile, tasks, works, subjects),
                targetRoute = AppRoutes.Home,
                channelId = CHANNEL_ID_DIGEST
            )
        }

        scheduleRearm()
        persistScheduledRequestCodes()
    }

    /**
     * El despertador interno que hace deslizar la ventana.
     *
     * Sin esto, quien no abra la app ni reinicie el teléfono se queda sin avisos en cuanto
     * pasan las 48 horas ya programadas. No lleva notificación: su intent va marcado con
     * [EXTRA_REARM] y el receptor, al verlo, se limita a pedir un recálculo.
     *
     * Se rearma en cada pasada porque `cancelPrevious()` también lo cancela a él.
     */
    private fun scheduleRearm() {
        val intent = Intent(context, ReminderReceiver::class.java).putExtra(EXTRA_REARM, true)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cuando = nextTriggerAt(hour = REARM_HOUR, minute = 0, daysFromNow = 0)
        // Aproximada a propósito: no la ve nadie y da igual media hora arriba o abajo.
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cuando, pendingIntent)
        scheduledRequestCodes.add(REARM_REQUEST_CODE)
    }

    private fun scheduleClassReminders(
        profile: UserProfile,
        subjects: List<Subject>,
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>
    ) {
        /*
         * Aqui habia un filtro que tiraba los avisos cuya hora ya habia pasado, y era el que
         * remataba el fallo: ni siquiera llegaban a scheduleReminder, que es quien ahora sabe
         * distinguir entre un aviso que aun sirve y uno que ya no. Se quita a proposito; el
         * descarte lo decide alli, comparando con el comienzo real de la clase.
         */
        sessions
            .filter { it.reminderMinutes > 0 && it.isValid }
            .mapNotNull { session ->
                nextClassOccurrence(session)?.let { comienzo -> Triple(session, comienzo, comienzo.minusMinutes(session.reminderMinutes.toLong())) }
            }
            .sortedBy { (_, _, trigger) -> trigger }
            .forEach { (session, comienzo, trigger) ->
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: Textos.get(R.string.notif_your_class)
                scheduleReminder(
                    profile = profile,
                    requestCode = session.id.stableRequestCode("class-reminder"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    subText = Textos.get(R.string.notif_sub_class),
                    // Los minutos van en el título: es el dato que decide si te
                    // levantas ya o no, y así se ve sin desplegar el aviso.
                    title = Textos.get(R.string.notif_class_starts_in, subjectName, session.reminderMinutes),
                    // `location` es "aula•profesor" en crudo -- concatenarlo tal cual dejaba un
                    // punto suelto cuando faltaba el profesor ("Nos vemos en 103F•."). Se arma
                    // la frase a partir de `place`, que ya sabe cuál de los dos falta.
                    body = classReminderBody(session.place),
                    targetRoute = AppRoutes.Calendar,
                    eventAtMillis = comienzo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    // Si sale con retraso, los minutos del titulo se cuentan de nuevo: el
                    // numero es justo el dato por el que se lee este aviso.
                    lateTitle = { minutos -> Textos.get(R.string.notif_class_starts_in, subjectName, minutos) }
                )
            }

        sessions
            .filter(ClassSession::isValid)
            .mapNotNull { session ->
                nextAttendanceTarget(session)?.let { (start, epochDay) ->
                    val occurrence = occurrences.firstOrNull {
                        it.sessionId == session.id && it.dateEpochDay == epochDay
                    }
                    if (occurrence?.status != null && occurrence.status != ClassAttendanceStatus.PENDING) {
                        null
                    } else {
                        Triple(session, start, epochDay)
                    }
                }
            }
            .sortedBy { it.second }
            .forEach { (session, start, epochDay) ->
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: Textos.get(R.string.notif_your_class_lower)
                /*
                 * Veinte minutos despues de acabar, no diez.
                 *
                 * A los diez todavia se esta recogiendo o saliendo del aula, y el aviso llega
                 * cuando no se puede atender: se descarta sin leer y la asistencia se queda
                 * sin registrar, que es justo lo que este aviso venia a evitar.
                 */
                val trigger = start.toLocalDate()
                    .atStartOfDay()
                    .plusMinutes(session.endMinute.toLong() + ATTENDANCE_PROMPT_DELAY_MINUTES)
                scheduleReminder(
                    profile = profile,
                    requestCode = "${session.id}:$epochDay".stableRequestCode("class-attendance"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    subText = Textos.get(R.string.notif_sub_attendance),
                    title = Textos.get(R.string.notif_did_you_attend, subjectName),
                    body = Textos.get(R.string.notif_attend_body),
                    targetRoute = AppRoutes.Calendar,
                    channelId = CHANNEL_ID_DIGEST,
                    /*
                     * Con botones, contestar no obliga a abrir la app.
                     *
                     * Preguntaba y para responder habia que entrar, ir al calendario, dar con
                     * la clase y abrir su panel: cuatro pasos para un si o un no. Asi se
                     * contesta desde la pantalla de bloqueo, y tocar el aviso sigue abriendo
                     * el calendario para quien prefiera mirar.
                     */
                    attendanceOf = session.id to epochDay
                )
            }

        // ── Repaso de fin de jornada ─────────────────────────────────────────────
        // Si quedaron clases de hoy sin marcar, un aviso único después de la última.
        // No sustituye al aviso individual de cada clase: lo complementa para el caso
        // en que el usuario no atendió ninguno, y al final del día las preguntas se
        // acumularon sin respuesta.
        val ahora = LocalDateTime.now()
        val hoy = ahora.toLocalDate()
        val hoyEpochDay = hoy.toEpochDay()
        val hoyDow = hoy.dayOfWeek.value

        val sinMarcarHoy = sessions
            .filter { it.isValid && it.occursOn(hoyEpochDay, hoyDow) }
            .filter { session ->
                hoy.atStartOfDay().plusMinutes(session.endMinute.toLong()).isBefore(ahora)
            }
            .filter { session ->
                val occurrence = occurrences.firstOrNull {
                    it.sessionId == session.id && it.dateEpochDay == hoyEpochDay
                }
                occurrence?.status == null || occurrence.status == ClassAttendanceStatus.PENDING
            }

        if (sinMarcarHoy.isNotEmpty()) {
            val ultimaClase = sinMarcarHoy.maxOf { it.endMinute }
            val triggerCatchup = hoy.atStartOfDay()
                .plusMinutes(ultimaClase.toLong() + ATTENDANCE_CATCHUP_DELAY_MINUTES)

            val cantidad = sinMarcarHoy.size
            val nombres = sinMarcarHoy.mapNotNull { session ->
                subjects.firstOrNull { it.id == session.subjectId }?.name
            }
            scheduleReminder(
                profile = profile,
                requestCode = ATTENDANCE_CATCHUP_REQUEST_CODE,
                triggerAtMillis = triggerCatchup.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                subText = Textos.get(R.string.notif_sub_attendance),
                title = if (cantidad == 1) Textos.get(R.string.notif_unmarked_one_today)
                        else Textos.get(R.string.notif_unmarked_many_today, cantidad),
                body = when {
                    nombres.isEmpty() -> Textos.get(R.string.notif_unmarked_open_calendar)
                    nombres.size <= 2 -> Textos.get(R.string.notif_unmarked_tap_to_mark, nombres.joinToString(Textos.get(R.string.notif_joiner_and)))
                    else -> Textos.get(R.string.notif_unmarked_and_more, nombres.take(2).joinToString(", "), nombres.size - 2)
                },
                targetRoute = AppRoutes.Calendar,
                channelId = CHANNEL_ID_DIGEST
            )
        }
    }

    private fun scheduleAgendaEventReminders(profile: UserProfile, events: List<AgendaEvent>) {
        val now = LocalDateTime.now()
        events
            .filter { it.isValid && it.reminderMinutes > 0 }
            .mapNotNull { event ->
                (0L..366L).asSequence()
                    .map { now.toLocalDate().plusDays(it) }
                    .filter(event::occursOn)
                    .map { date ->
                        val originalTime = Instant.ofEpochMilli(event.startMillis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                        date.atTime(originalTime)
                    }
                    .firstOrNull { it.isAfter(now) }
                    ?.let { event to it.minusMinutes(event.reminderMinutes.toLong()) }
            }
            .filter { (_, trigger) -> trigger.isAfter(now) }
            .sortedBy { it.second }
            .forEach { (event, trigger) ->
                scheduleReminder(
                    profile = profile,
                    requestCode = event.id.stableRequestCode("agenda-event"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    subText = Textos.get(R.string.notif_sub_agenda),
                    title = event.title,
                    body = event.location.takeIf(String::isNotBlank)
                        ?.let { Textos.get(R.string.notif_event_starts_in_at, event.reminderMinutes, it) }
                        ?: Textos.get(R.string.notif_event_starts_in, event.reminderMinutes),
                    targetRoute = AppRoutes.Calendar
                )
            }
    }

    private fun nextClassOccurrence(session: ClassSession): LocalDateTime? {
        val now = LocalDateTime.now()
        return (0L..84L).asSequence()
            .map { now.toLocalDate().plusDays(it) }
            .filter { session.occursOn(it.toEpochDay(), it.dayOfWeek.value) }
            .map { it.atTime(session.startMinute / 60, session.startMinute % 60) }
            .firstOrNull { it.isAfter(now) }
    }

    /**
     * La siguiente fecha cuya ventana de asistencia no ha cerrado todavía.
     *
     * [nextClassOccurrence] busca la clase cuyo inicio esté en el futuro: una clase que ya
     * empezó la salta. Eso mataba la notificación de asistencia del día, porque la ventana
     * de asistencia se mide desde el **fin** de la clase, no desde su inicio. Una clase de
     * 8 a 10 sigue necesitando su pregunta de asistencia hasta las 10:20 — pero a las 8:01
     * [nextClassOccurrence] ya devolvía el día siguiente.
     */
    private fun nextAttendanceTarget(session: ClassSession): Pair<LocalDateTime, Long>? {
        val now = LocalDateTime.now()
        return (0L..84L).asSequence()
            .map { now.toLocalDate().plusDays(it) }
            .filter { session.occursOn(it.toEpochDay(), it.dayOfWeek.value) }
            .firstOrNull { date ->
                ReminderTiming.isAttendanceWindowOpen(
                    endMinuteOfDay = session.endMinute,
                    delayMinutes = ATTENDANCE_PROMPT_DELAY_MINUTES,
                    date = date,
                    now = now
                )
            }
            ?.let { date ->
                val start = date.atTime(session.startMinute / 60, session.startMinute % 60)
                start to date.toEpochDay()
            }
    }

    fun cancelScheduled(requestCode: Int) {
        cancel(requestCode)
        scheduledRequestCodes.remove(requestCode)
        val remaining = storedScheduledRequestCodes() - requestCode
        prefs.edit {
            putStringSet(REQUEST_CODE_SET, remaining.map(Int::toString).toSet())
        }
    }

    private fun scheduleSubjectInsights(profile: UserProfile, subjects: List<Subject>) {
        gradeNotificationHints(profile, subjects)
            .take(MAX_SMART_SUBJECT_REMINDERS)
            .forEachIndexed { index, hint ->
                scheduleReminder(
                    profile = profile,
                    requestCode = hint.subject.id.stableRequestCode("subject-insight"),
                    triggerAtMillis = nextTriggerAt(hour = 18, minute = index * 10, daysFromNow = 1),
                    subText = hint.subject.name,
                    title = hint.notificationTitle(),
                    body = hint.message,
                    targetRoute = AppRoutes.subjectDetail(hint.subject.id),
                    channelId = CHANNEL_ID_DIGEST
                )
            }
    }

    private fun gradeNotificationHints(profile: UserProfile, subjects: List<Subject>): List<SubjectNotificationHint> {
        val maxGrade = GradingScaleUtils.maxGradeFor(profile)
        return subjects.mapNotNull { subject ->
            val cuts = subject.cutScheme.cuts
            val average = GradeCalculator.calculateCurrentAverageByCuts(subject.grades, cuts)
            val evaluatedPercentage = GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, cuts)
            val weightedPoints = GradeCalculator.calculateWeightedPointsByCuts(subject.grades, cuts)
            val remainingPercentage = (1.0 - evaluatedPercentage / 100.0).coerceAtLeast(0.0)
            val neededGrade = GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = weightedPoints,
                remainingPercentage = remainingPercentage,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
            val activeOrder = cuts.firstOrNull { it.id == subject.activeCutId }?.order ?: 1
            val missingPriorCuts = cuts
                .filter { it.order < activeOrder }
                .count { cut ->
                    subject.grades.none { it.cutId == cut.id } &&
                        cut.id !in subject.unknownCutIds
                }
            val unknownWeights = subject.grades.count {
                it.source == com.unistack.app.feature_grades.domain.GradeSource.ACTIVITY &&
                    it.weightStatus == com.unistack.app.feature_grades.domain.GradeWeightStatus.UNKNOWN
            }
            when {
                missingPriorCuts > 0 -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.MISSING_PERIODS,
                    severity = 3,
                    message = if (missingPriorCuts == 1) Textos.get(R.string.notif_hint_missing_cut_one, subject.name) else Textos.get(R.string.notif_hint_missing_cut_many, missingPriorCuts, subject.name)
                )
                unknownWeights > 0 -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.UNKNOWN_WEIGHTS,
                    severity = 2,
                    message = if (unknownWeights == 1) Textos.get(R.string.notif_hint_unknown_weight_one, subject.name) else Textos.get(R.string.notif_hint_unknown_weight_many, subject.name, unknownWeights)
                )
                subject.grades.isEmpty() -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.NO_GRADES,
                    severity = 1,
                    message = Textos.get(R.string.notif_hint_no_grades, subject.name)
                )
                average != null && average < profile.passingGrade -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.BELOW_PASSING,
                    severity = 4,
                    message = Textos.get(R.string.notif_hint_below_passing, subject.name, GradingScaleUtils.formatGrade(average, profile.gradingScale))
                )
                neededGrade != null && neededGrade > maxGrade -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.TARGET_UNREACHABLE,
                    severity = 3,
                    message = Textos.get(R.string.notif_hint_target_unreachable, subject.name)
                )
                average != null && average < subject.targetAverage -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.BELOW_TARGET,
                    severity = 2,
                    message = Textos.get(R.string.notif_hint_below_target, subject.name, GradingScaleUtils.formatGrade(average, profile.gradingScale), GradingScaleUtils.formatGrade(subject.targetAverage, profile.gradingScale))
                )
                else -> null
            }
        }.sortedByDescending { it.severity }
    }

    private fun smartDigestBody(
        profile: UserProfile,
        tasks: List<StudentTask>,
        works: List<AcademicWork>,
        subjects: List<Subject>
    ): String {
        val now = System.currentTimeMillis()
        val overdueTasks = tasks.count { !it.completed && it.dueDateMillis < now }
        val dueTodayTasks = tasks.count { !it.completed && it.dueDateMillis >= now && it.dueDateMillis.isToday() }
        val overdueWorks = works.count {
            it.status != AcademicWorkStatus.SUBMITTED && (it.dueDateMillis ?: Long.MAX_VALUE) < now
        }
        val dueTodayWorks = works.count {
            it.status != AcademicWorkStatus.SUBMITTED &&
                it.dueDateMillis != null &&
                it.dueDateMillis >= now &&
                it.dueDateMillis.isToday()
        }
        val pendingGradeResults = tasks.count {
            it.completed && it.gradingStatus == TaskGradingStatus.AWAITING_GRADE
        }
        val risk = gradeNotificationHints(profile, subjects).firstOrNull { it.severity >= 2 }
        val overdueTotal = overdueTasks + overdueWorks
        val dueTodayTotal = dueTodayTasks + dueTodayWorks

        return when {
            overdueTotal > 0 -> if (overdueTotal == 1) Textos.get(R.string.notif_digest_overdue_one) else Textos.get(R.string.notif_digest_overdue_many, overdueTotal)
            dueTodayTotal > 0 -> if (dueTodayTotal == 1) Textos.get(R.string.notif_digest_due_one) else Textos.get(R.string.notif_digest_due_many, dueTodayTotal)
            pendingGradeResults > 0 && profile.pendingGradeRemindersEnabled ->
                if (pendingGradeResults == 1) Textos.get(R.string.notif_digest_pending_grade_one) else Textos.get(R.string.notif_digest_pending_grade_many, pendingGradeResults)
            risk != null -> risk.message
            subjects.isEmpty() -> Textos.get(R.string.notif_digest_no_subjects)
            else -> Textos.get(R.string.notif_digest_clear_day)
        }
    }

    /**
     * Deja puesto un aviso, lo manda ya si llega tarde, o lo aparca para el proximo rearmado.
     *
     * [eventAtMillis] es cuando ocurre lo que se anuncia —la clase, la entrega—, y no es lo
     * mismo que [triggerAtMillis], que es cuando toca avisar. La diferencia importa cuando el
     * momento del aviso ya ha pasado: un «empieza en 10 min» que sale con tres minutos de
     * retraso sigue sirviendo, y uno que sale cuando la clase lleva media hora es basura.
     *
     * Antes los dos casos acababan igual, en un `return` mudo, y por ahi se perdian avisos:
     * cualquier cambio de datos —marcar una tarea, mirar una nota— vuelve a pasar por aqui
     * cancelando primero todas las alarmas, asi que bastaba con que el usuario tocara la app
     * en el minuto equivocado para que su recordatorio desapareciera sin dejar rastro.
     */
    /**
     * Los recordatorios que el usuario le puso a sus notas.
     *
     * No dependen de ningun modulo ni de ningun ajuste de avisos: si alguien se toma la molestia
     * de poner una hora a una nota es porque quiere que suene, y apagarla desde otro sitio
     * convertiria la campana del editor en un boton que a veces no hace nada.
     *
     * El titulo del aviso es el de la nota, y si no tiene, su primera linea sin marcas. El cuerpo
     * lleva lo que sigue, que es lo que hace util un aviso a las siete de la manana.
     */
    /**
     * Los dos avisos del periodo: el dia en que acaba, para cerrarlo, y el dia elegido para
     * empezar el siguiente si todavia no existe.
     *
     * Salen a las 9:00, como los demas recordatorios de dia. Si la app se abre ese mismo dia mas
     * tarde, se envian al momento; al dia siguiente ya no. El del siguiente periodo cuenta desde
     * el inicio previsto y no desde el que se va corriendo con los dias, para que no vuelva a
     * sonar cada lunes.
     */
    private fun scheduleTermReminders(profile: UserProfile, terms: List<AcademicTerm>) {
        val zona = ZoneId.systemDefault()
        val activo = terms.firstOrNull { it.isActive }
        val fin = activo?.plannedEnd
        if (profile.termEndReminderEnabled && activo != null && fin != null) {
            scheduleReminder(
                profile = profile,
                requestCode = activo.id.stableRequestCode("term-end"),
                triggerAtMillis = fin.atTime(9, 0).atZone(zona).toInstant().toEpochMilli(),
                subText = Textos.get(R.string.hist_notif_sub),
                title = Textos.get(R.string.hist_notif_acabo_titulo, activo.name),
                body = Textos.get(R.string.hist_notif_acabo_cuerpo),
                targetRoute = AppRoutes.TermClose,
                channelId = CHANNEL_ID_DIGEST,
                eventAtMillis = fin.atTime(23, 59).atZone(zona).toInstant().toEpochMilli()
            )
        }
        if (profile.nextTermReminderEnabled && activo == null && terms.isNotEmpty()) {
            val propuesta = PropuestaDelSiguientePeriodo.build(terms, LocalDate.now())
            val dia = propuesta.diaDelAviso(profile.nextTermReminderOffset)
            scheduleReminder(
                profile = profile,
                requestCode = propuesta.nombre.stableRequestCode("term-next"),
                triggerAtMillis = dia.atTime(9, 0).atZone(zona).toInstant().toEpochMilli(),
                subText = Textos.get(R.string.hist_notif_sub),
                title = Textos.get(R.string.hist_notif_empezar_titulo, propuesta.nombre),
                body = Textos.get(R.string.hist_notif_empezar_cuerpo),
                targetRoute = AppRoutes.NewTerm,
                channelId = CHANNEL_ID_DIGEST,
                eventAtMillis = dia.atTime(23, 59).atZone(zona).toInstant().toEpochMilli()
            )
        }
    }

    private fun scheduleNoteReminders(
        profile: UserProfile,
        notes: List<QuickNote>,
        subjects: List<Subject>
    ) {
        notes.filter { it.reminderAt != null }.forEach { note ->
            val plano = NoteMarkdown.strip(note.body)
            val titulo = note.title.trim().ifBlank { NoteText.title(plano) }.ifBlank { Textos.get(R.string.notif_sub_note) }
            val cuerpo = NoteText.preview(plano, maxLines = 2)
                .replace(10.toChar(), ' ')
                .trim()
            scheduleReminder(
                profile = profile,
                requestCode = note.id.stableRequestCode("note-reminder"),
                triggerAtMillis = note.reminderAt!!,
                subText = subjects.firstOrNull { it.id == note.subjectId }?.name ?: Textos.get(R.string.notif_sub_note),
                title = titulo,
                body = cuerpo.ifBlank { Textos.get(R.string.notif_note_body_fallback) },
                targetRoute = AppRoutes.noteEditor(note.id),
                eventAtMillis = note.reminderAt
            )
        }
    }

    private fun scheduleReminder(
        profile: UserProfile,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        body: String,
        targetRoute: String? = null,
        subText: String? = null,
        channelId: String = CHANNEL_ID_ALERTS,
        eventAtMillis: Long? = null,
        lateTitle: ((minutosRestantes: Long) -> String)? = null,
        /** Clase y dia a los que responden los botones «Asisti» y «Falta», si los lleva. */
        attendanceOf: Pair<String, Long>? = null
    ) {
        val adjustedTrigger = ReminderTiming.adjustForQuietHours(profile, triggerAtMillis)
        val now = System.currentTimeMillis()
        val accion = ReminderTiming.decide(
            triggerAtMillis = adjustedTrigger,
            eventAtMillis = eventAtMillis,
            now = now,
            windowMillis = SCHEDULING_WINDOW_MILLIS
        )
        if (accion == ReminderAction.SKIP) return

        if (accion == ReminderAction.SEND_NOW) {
            /*
             * Sale con retraso, y se apunta que salio.
             *
             * Por aqui se vuelve a pasar en cada cambio de datos, asi que sin la marca el
             * mismo aviso se reenviaria una y otra vez durante todas las horas que queden
             * hasta la clase. La marca lleva la hora prevista dentro, de modo que si el
             * usuario mueve la clase el aviso nuevo cuenta como otro y si puede sonar.
             *
             * El titulo se rehace: el que venia dado decia «empieza en 10 min» porque asi se
             * calculo al programarlo, y publicarlo tal cual cuando quedan tres es repetir el
             * problema que este arreglo venia a quitar.
             */
            val marca = "$requestCode@$adjustedTrigger"
            if (marca in storedDeliveredLate()) return
            val tituloReal = if (lateTitle != null && eventAtMillis != null) {
                lateTitle(ReminderTiming.minutesUntil(eventAtMillis, now))
            } else {
                title
            }
            showNotification(
                context,
                reminderIntent(requestCode, tituloReal, body, targetRoute, subText, channelId, attendanceOf)
            )
            rememberDeliveredLate(marca, now)
            return
        }

        val intent = reminderIntent(requestCode, title, body, targetRoute, subText, channelId, attendanceOf)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        /*
         * Exacta cuando se puede, aproximada cuando no.
         *
         * `setAndAllowWhileIdle` respeta el reposo agrupando alarmas, y con el movil dormido
         * las suelta cada nueve o quince minutos: el aviso que dice «empieza en 10 min» podia
         * llegar cuando la clase ya habia empezado. El manifest declara USE_EXACT_ALARM, que
         * se concede sola al instalar, pero se comprueba igualmente: sin permiso
         * `setExactAndAllowWhileIdle` lanza SecurityException, y quedarse sin aviso es mejor
         * que tumbar la app.
         */
        if (canScheduleExact()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
        } else {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
        }
        scheduledRequestCodes.add(requestCode)
    }

    private fun canScheduleExact(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    private fun cancelPrevious() {
        (scheduledRequestCodes + storedScheduledRequestCodes()).forEach(::cancel)
        scheduledRequestCodes.clear()
        persistScheduledRequestCodes()
    }

    private fun cancel(requestCode: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            reminderIntent(requestCode, "", "", null),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun reminderIntent(
        requestCode: Int,
        title: String,
        body: String,
        targetRoute: String?,
        subText: String? = null,
        channelId: String = CHANNEL_ID_ALERTS,
        attendanceOf: Pair<String, Long>? = null
    ): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
            putExtra(EXTRA_SUBTEXT, subText)
            putExtra(EXTRA_CHANNEL_ID, channelId)
            attendanceOf?.let { (sessionId, epochDay) ->
                putExtra(EXTRA_ATTENDANCE_SESSION_ID, sessionId)
                putExtra(EXTRA_ATTENDANCE_EPOCH_DAY, epochDay)
            }
        }
    }

    private fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)

        val alerts = NotificationChannel(
            CHANNEL_ID_ALERTS,
            Textos.get(R.string.notif_channel_alerts_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = Textos.get(R.string.notif_channel_alerts_desc)
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
            lightColor = BRAND_COLOR
        }

        val digest = NotificationChannel(
            CHANNEL_ID_DIGEST,
            Textos.get(R.string.notif_channel_digest_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = Textos.get(R.string.notif_channel_digest_desc)
            setShowBadge(true)
        }

        manager.createNotificationChannel(alerts)
        manager.createNotificationChannel(digest)
        manager.deleteNotificationChannel(LEGACY_CHANNEL_ID)
    }

    private fun nextTriggerAt(hour: Int, minute: Int, daysFromNow: Long): Long {
        val zone = ZoneId.systemDefault()
        var trigger = LocalDate.now(zone)
            .plusDays(daysFromNow)
            .atTime(hour, minute)
            .atZone(zone)
        if (trigger.toInstant().toEpochMilli() <= System.currentTimeMillis()) {
            trigger = trigger.plusDays(1)
        }
        return trigger.toInstant().toEpochMilli()
    }

    private fun Long.isToday(): Boolean {
        val zone = ZoneId.systemDefault()
        return Instant.ofEpochMilli(this).atZone(zone).toLocalDate() == LocalDate.now(zone)
    }

    private fun storedScheduledRequestCodes(): Set<Int> {
        return prefs.getStringSet(REQUEST_CODE_SET, emptySet<String>())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()
    }

    private fun persistScheduledRequestCodes() {
        prefs.edit {
            putStringSet(REQUEST_CODE_SET, scheduledRequestCodes.map(Int::toString).toSet())
        }
    }

    /**
     * Avisa cuando queda una falta, o cuando ya no queda ninguna.
     *
     * Es el unico momento en que este dato cambia lo que haces, y llega tarde si tienes que
     * ir tu a mirarlo. Antes de eso no dice nada: un aviso por cada falta seria ruido.
     *
     * Las faltas se cuentan de lo marcado y no de la regla de repeticion, asi que aqui no
     * hace falta el periodo: una falta existe porque alguien la escribio.
     *
     * La marca lleva el numero de faltas dentro, de modo que el aviso sale una vez por falta
     * nueva y no en cada cambio de datos —por aqui se vuelve a pasar constantemente—.
     */
    private fun avisaSiTeQuedasSinFaltas(
        profile: UserProfile,
        subjects: List<Subject>,
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>
    ) {
        // Va con las notas: el tope de faltas es un dato academico y sin ese modulo no hay
        // materias que vigilar.
        if (AppModule.GRADES !in profile.enabledModules) return
        val ahora = System.currentTimeMillis()
        // Respeta las horas de silencio igual que el resto: `showNotification` no las mira.
        if (ReminderTiming.adjustForQuietHours(profile, ahora) != ahora) return
        // Uno para todas: sale del reglamento, no de la asignatura.
        val tope = profile.absenceLimit ?: return
        subjects.forEach { subject ->
            val suyas = sessions.filter { it.subjectId == subject.id }.map { it.id }.toSet()
            if (suyas.isEmpty()) return@forEach
            val faltas = occurrences.count {
                it.sessionId in suyas && it.status == ClassAttendanceStatus.ABSENT
            }
            val restantes = tope - faltas
            if (restantes > 1) return@forEach

            val marca = "limite:${subject.id}@$faltas"
            if (marca in storedDeliveredLate()) return@forEach
            showNotification(
                context,
                reminderIntent(
                    requestCode = marca.stableRequestCode("absence-limit"),
                    title = if (restantes <= 0) {
                        Textos.get(R.string.notif_limit_over_title, subject.name)
                    } else {
                        Textos.get(R.string.notif_limit_one_left_title, subject.name)
                    },
                    body = if (restantes <= 0) {
                        Textos.get(R.string.notif_limit_over_body, faltas, tope)
                    } else {
                        // «La siguiente ya no cabe» sonaba a error de formulario. Lo que
                        // hay que decir es la consecuencia: una mas y pierdes la materia.
                        Textos.get(R.string.notif_limit_one_left_body, faltas, tope)
                    },
                    targetRoute = AppRoutes.Calendar,
                    subText = Textos.get(R.string.notif_sub_attendance),
                    channelId = CHANNEL_ID_ALERTS
                )
            )
            rememberDeliveredLate(marca, ahora)
        }
    }

    private fun storedDeliveredLate(): Set<String> =
        prefs.getStringSet(DELIVERED_LATE_SET, emptySet<String>()).orEmpty()

    /**
     * Apunta un aviso ya enviado con retraso, y de paso barre los viejos.
     *
     * Sin la barrida el conjunto crecería para siempre. Se conserva lo de la última ventana:
     * pasado ese plazo la hora prevista ya no puede volver a salir en ningún cálculo, así que
     * la marca no protege de nada.
     */
    private fun rememberDeliveredLate(marca: String, now: Long) {
        val vigentes = (storedDeliveredLate() + marca).filter { entrada ->
            val previsto = entrada.substringAfterLast('@').toLongOrNull() ?: return@filter false
            now - previsto <= SCHEDULING_WINDOW_MILLIS
        }.toSet()
        prefs.edit { putStringSet(DELIVERED_LATE_SET, vigentes) }
    }

    private fun String.stableRequestCode(kind: String): Int {
        return "$kind:$this".hashCode() and Int.MAX_VALUE
    }

    /**
     * El cuerpo del aviso de clase, con solo lo que hay.
     *
     * `location` se guarda como `"aula•profesor"` y con cualquiera de los dos vacío la frase
     * quedaba con el separador suelto -- «Nos vemos en 103F•.» -- porque se concatenaba tal
     * cual en vez de mirar qué mitad faltaba.
     */
    private fun classReminderBody(place: SessionPlace): String {
        val room = place.room.takeIf(String::isNotBlank)
        val professor = place.professor.takeIf(String::isNotBlank)
        return when {
            room != null && professor != null -> Textos.get(R.string.notif_class_body_room_prof, room, professor)
            room != null -> Textos.get(R.string.notif_class_body_room, room)
            professor != null -> Textos.get(R.string.notif_class_body_prof, professor)
            else -> Textos.get(R.string.notif_class_body_plain)
        }
    }

    /* dueText() devuelve fragmentos pensados para ir dentro de una frase
       («vence mañana 15:00»). Aquí abren el cuerpo del aviso, así que hay que
       levantar la primera letra. */
    private fun String.sentenceCase(): String =
        replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }

    private enum class SubjectHintKind {
        MISSING_PERIODS,
        UNKNOWN_WEIGHTS,
        NO_GRADES,
        BELOW_PASSING,
        TARGET_UNREACHABLE,
        BELOW_TARGET
    }

    private data class SubjectNotificationHint(
        val subject: Subject,
        val kind: SubjectHintKind,
        val severity: Int,
        val message: String
    ) {
        /* El nombre de la materia ya viaja en el subtítulo del aviso, así que
           el título se queda solo con el qué: dicho de corrido se lee
           «Cálculo III · Vas por debajo de tu meta». */
        fun notificationTitle(): String = when (kind) {
            SubjectHintKind.MISSING_PERIODS -> Textos.get(R.string.notif_hint_title_missing_cuts)
            SubjectHintKind.UNKNOWN_WEIGHTS -> Textos.get(R.string.notif_hint_title_weights)
            SubjectHintKind.NO_GRADES -> Textos.get(R.string.notif_hint_title_no_grades)
            SubjectHintKind.BELOW_PASSING -> Textos.get(R.string.notif_hint_title_below_passing)
            SubjectHintKind.TARGET_UNREACHABLE -> Textos.get(R.string.notif_hint_title_target_risk)
            SubjectHintKind.BELOW_TARGET -> Textos.get(R.string.notif_hint_title_below_target)
        }
    }

    companion object {
        fun showNotification(context: Context, intent: Intent) {
            val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode() and Int.MAX_VALUE)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val notificationManager = NotificationManagerCompat.from(context)
            if (!notificationManager.areNotificationsEnabled()) return
            val targetRoute = intent.getStringExtra(EXTRA_TARGET_ROUTE)
            val historyItem = NotificationHistoryStore.recordDelivered(
                context = context,
                requestCode = notificationId,
                title = title,
                body = body,
                targetRoute = targetRoute
            ) ?: return
            val sessionId = intent.getStringExtra(EXTRA_ATTENDANCE_SESSION_ID)
            val epochDay = intent.getLongExtra(EXTRA_ATTENDANCE_EPOCH_DAY, -1L)
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(
                    MainActivity.EXTRA_LAUNCH_ROUTE,
                    targetRoute ?: AppRoutes.notificationDetail(historyItem.id)
                )
                /*
                 * Tocar el aviso abre la clase por la que pregunta.
                 *
                 * Dejaba en Horario y desde ahi habia que buscar el dia, dar con la clase y
                 * abrir su panel: tres pasos para contestar lo que el aviso acababa de
                 * preguntar. La ruta sigue siendo el calendario —es donde vive el panel— y
                 * estos dos datos le dicen cual abrir.
                 */
                if (sessionId != null && epochDay >= 0L) {
                    putExtra(EXTRA_ATTENDANCE_SESSION_ID, sessionId)
                    putExtra(EXTRA_ATTENDANCE_EPOCH_DAY, epochDay)
                }
            }
            val contentIntent = PendingIntent.getActivity(
                context,
                notificationId,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val channelId = intent.getStringExtra(EXTRA_CHANNEL_ID) ?: CHANNEL_ID_ALERTS
            val subText = intent.getStringExtra(EXTRA_SUBTEXT)
            val isAlert = channelId == CHANNEL_ID_ALERTS
            val builder = NotificationCompat.Builder(context, channelId)
                // Un mipmap de lanzador no sirve aquí: el sistema se queda solo
                // con su alfa y, al ser una imagen opaca de borde a borde, sale
                // un cuadro blanco. Hace falta una silueta monocroma de 24dp.
                .setSmallIcon(R.drawable.ic_stat_unistack)
                .setColor(BRAND_COLOR)
                .setContentTitle(title)
                .setContentText(body)
                .setSubText(subText)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .setCategory(if (isAlert) NotificationCompat.CATEGORY_REMINDER else NotificationCompat.CATEGORY_STATUS)
                // PRIORITY_* es lo que atiende Android 7 y anteriores; de Oreo
                // en adelante manda la importancia del canal. Se ponen los dos
                // para que la ventana emergente salga en todas las versiones.
                .setPriority(if (isAlert) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
                .setDefaults(if (isAlert) NotificationCompat.DEFAULT_ALL else 0)

            /*
             * Si el aviso pregunta por una clase, se contesta desde el propio aviso.
             *
             * Las dos acciones van a un receptor y no a la actividad: abrir la app para
             * guardar un si o un no es exactamente el rodeo que hacia que nadie contestase.
             */
            if (sessionId != null && epochDay >= 0L) {
                builder.addAction(
                    0,
                    Textos.get(R.string.schedule_status_attended),
                    accionDeAsistencia(
                        context, notificationId, sessionId, epochDay,
                        ClassAttendanceStatus.ATTENDED
                    )
                )
                builder.addAction(
                    0,
                    Textos.get(R.string.schedule_status_absent),
                    accionDeAsistencia(
                        context, notificationId, sessionId, epochDay,
                        ClassAttendanceStatus.ABSENT
                    )
                )
            }

            notificationManager.notify(notificationId, builder.build())
        }

        /**
         * El disparador de un boton del aviso.
         *
         * Cada estado necesita su propio `requestCode`: con el mismo, el segundo
         * `PendingIntent` reutilizaria los extras del primero y «Falta» guardaria una
         * asistencia.
         */
        private fun accionDeAsistencia(
            context: Context,
            notificationId: Int,
            sessionId: String,
            epochDay: Long,
            status: ClassAttendanceStatus
        ): PendingIntent {
            val intent = Intent(context, AttendanceActionReceiver::class.java).apply {
                action = ACTION_MARK_ATTENDANCE
                putExtra(EXTRA_ATTENDANCE_SESSION_ID, sessionId)
                putExtra(EXTRA_ATTENDANCE_EPOCH_DAY, epochDay)
                putExtra(EXTRA_ATTENDANCE_STATUS, status.name)
                putExtra(EXTRA_NOTIFICATION_ID, notificationId)
            }
            return PendingIntent.getBroadcast(
                context,
                notificationId * 2 + if (status == ClassAttendanceStatus.ATTENDED) 0 else 1,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }
    }
}
