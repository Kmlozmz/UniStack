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
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.UserProfile
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

/* El canal viejo nace con IMPORTANCE_DEFAULT, que en Android nunca muestra
   ventana emergente. La importancia de un canal ya creado no se puede subir
   por código —el sistema ignora el cambio para respetar al usuario—, así que
   la única salida es publicar en canales nuevos y borrar el anterior. */
private const val LEGACY_CHANNEL_ID = "unistack_reminders"

/* Lo que tiene hora y se puede perder: entregas, clases, vencidos. Asoma. */
private const val CHANNEL_ID_ALERTS = "unistack_alerts_v2"
private const val CHANNEL_NAME_ALERTS = "Entregas y clases"

/* Lo que solo informa: resumen del día, avisos de notas. No interrumpe. */
private const val CHANNEL_ID_DIGEST = "unistack_digest_v2"
private const val CHANNEL_NAME_DIGEST = "Resumen y seguimiento"

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
private const val EXTRA_TITLE = "title"
private const val EXTRA_BODY = "body"
private const val EXTRA_NOTIFICATION_ID = "notification_id"
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
        agendaEvents: List<AgendaEvent> = emptyList()
    ) {
        createChannel()
        cancelPrevious()
        val currentProfile = profile ?: run {
            persistScheduledRequestCodes()
            return
        }
        val leadMillis = currentProfile.reminderLeadHours.coerceIn(1, 168) * 60L * 60L * 1000L

        if (AppModule.TASKS in currentProfile.enabledModules && currentProfile.taskRemindersEnabled) {
            tasks
                .filter { !it.completed && TaskDateUtils.hasExplicitTime(it.dueDateMillis) }
                .sortedBy { it.dueDateMillis }
                .forEach { task ->
                    // El nombre de la materia va en el subtítulo, junto al de la
                    // app: se lee de un vistazo sin robarle sitio al título, que
                    // se reserva para lo único que identifica el aviso.
                    val subjectLabel = subjects.firstOrNull { it.id == task.subjectId }?.name ?: "Tarea"
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
                            title = "Venció: ${task.title}",
                            body = "Márcala como hecha o muévela de fecha.",
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
                        subText = "Nota pendiente",
                        title = "¿Ya te dieron la nota de ${task.title}?",
                        body = "Regístrala para que tu promedio deje de ser una proyección.",
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
                        subText = "Trabajo",
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
                            subText = "Trabajo",
                            title = "Venció: ${work.title}",
                            body = "Revisa su checklist y actualiza en qué estado quedó.",
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
                subText = "Resumen",
                title = "¡Buenos días!",
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
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: "Tu clase"
                scheduleReminder(
                    profile = profile,
                    requestCode = session.id.stableRequestCode("class-reminder"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    subText = "Clase",
                    // Los minutos van en el título: es el dato que decide si te
                    // levantas ya o no, y así se ve sin desplegar el aviso.
                    title = "$subjectName empieza en ${session.reminderMinutes} min",
                    body = session.location.takeIf(String::isNotBlank)?.let { "Nos vemos en $it." }
                        ?: "Alista lo que necesites antes de entrar.",
                    targetRoute = AppRoutes.Calendar,
                    eventAtMillis = comienzo.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    // Si sale con retraso, los minutos del titulo se cuentan de nuevo: el
                    // numero es justo el dato por el que se lee este aviso.
                    lateTitle = { minutos -> "$subjectName empieza en $minutos min" }
                )
            }

        sessions
            .filter(ClassSession::isValid)
            .mapNotNull { session ->
                nextClassOccurrence(session)?.let { start ->
                    val epochDay = start.toLocalDate().toEpochDay()
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
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: "tu clase"
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
                    subText = "Asistencia",
                    title = "¿Asististe a $subjectName?",
                    body = "Déjalo registrado para llevar la cuenta de tus faltas.",
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
                    subText = "Agenda",
                    title = event.title,
                    body = event.location.takeIf(String::isNotBlank)
                        ?.let { "Empieza en ${event.reminderMinutes} min, en $it." }
                        ?: "Empieza en ${event.reminderMinutes} min.",
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
            val periods = subject.periodScheme.periods
            val average = GradeCalculator.calculateCurrentAverageByPeriods(subject.grades, periods)
            val evaluatedPercentage = GradeCalculator.calculateEvaluatedSemesterPercentage(subject.grades, periods)
            val weightedPoints = GradeCalculator.calculateWeightedPointsByPeriods(subject.grades, periods)
            val remainingPercentage = (1.0 - evaluatedPercentage / 100.0).coerceAtLeast(0.0)
            val neededGrade = GradeCalculator.calculateNeededGrade(
                currentWeightedPoints = weightedPoints,
                remainingPercentage = remainingPercentage,
                targetAverage = subject.targetAverage,
                maxGrade = maxGrade
            )
            val activeOrder = periods.firstOrNull { it.id == subject.activePeriodId }?.order ?: 1
            val missingPriorPeriods = periods
                .filter { it.order < activeOrder }
                .count { period ->
                    subject.grades.none { it.periodId == period.id } &&
                        period.id !in subject.unknownPeriodIds
                }
            val unknownWeights = subject.grades.count {
                it.source == com.unistack.app.feature_grades.domain.GradeSource.ACTIVITY &&
                    it.weightStatus == com.unistack.app.feature_grades.domain.GradeWeightStatus.UNKNOWN
            }
            when {
                missingPriorPeriods > 0 -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.MISSING_PERIODS,
                    severity = 3,
                    message = "Falta ${if (missingPriorPeriods == 1) "un corte anterior" else "$missingPriorPeriods cortes anteriores"} en ${subject.name}. Complétalo para afinar tu meta."
                )
                unknownWeights > 0 -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.UNKNOWN_WEIGHTS,
                    severity = 2,
                    message = "${subject.name} tiene $unknownWeights ${if (unknownWeights == 1) "nota sin porcentaje" else "notas sin porcentaje"}. La proyección seguirá provisional."
                )
                subject.grades.isEmpty() -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.NO_GRADES,
                    severity = 1,
                    message = "Aún no tienes notas en ${subject.name}. Agrega la primera para activar tu promedio real."
                )
                average != null && average < profile.passingGrade -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.BELOW_PASSING,
                    severity = 4,
                    message = "${subject.name} está bajo la nota mínima con ${GradingScaleUtils.formatGrade(average, profile.gradingScale)}. Revisa el siguiente corte."
                )
                neededGrade != null && neededGrade > maxGrade -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.TARGET_UNREACHABLE,
                    severity = 3,
                    message = "La meta de ${subject.name} está difícil con lo restante. Ajusta estrategia o pesos de notas."
                )
                average != null && average < subject.targetAverage -> SubjectNotificationHint(
                    subject = subject,
                    kind = SubjectHintKind.BELOW_TARGET,
                    severity = 2,
                    message = "${subject.name} va en ${GradingScaleUtils.formatGrade(average, profile.gradingScale)}. Tu meta es ${GradingScaleUtils.formatGrade(subject.targetAverage, profile.gradingScale)}."
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
            overdueTotal > 0 -> "Tienes $overdueTotal pendiente${if (overdueTotal == 1) "" else "s"} vencido${if (overdueTotal == 1) "" else "s"}. Prioriza uno antes de seguir."
            dueTodayTotal > 0 -> "Hoy tienes $dueTodayTotal entrega${if (dueTodayTotal == 1) "" else "s"}. Mantén el ritmo y cierra lo urgente primero."
            pendingGradeResults > 0 && profile.pendingGradeRemindersEnabled ->
                "Tienes $pendingGradeResults ${if (pendingGradeResults == 1) "actividad esperando nota" else "actividades esperando nota"}. Actualízalas cuando recibas el resultado."
            risk != null -> risk.message
            subjects.isEmpty() -> "Crea tus materias para activar promedios, metas y avisos académicos inteligentes."
            else -> "Día despejado. Buen momento para repasar una materia o adelantar una tarea corta."
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
        lateTitle: ((minutosRestantes: Long) -> String)? = null
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
                reminderIntent(requestCode, tituloReal, body, targetRoute, subText, channelId)
            )
            rememberDeliveredLate(marca, now)
            return
        }

        val intent = reminderIntent(requestCode, title, body, targetRoute, subText, channelId)
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
        channelId: String = CHANNEL_ID_ALERTS
    ): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
            putExtra(EXTRA_SUBTEXT, subText)
            putExtra(EXTRA_CHANNEL_ID, channelId)
        }
    }

    private fun createChannel() {
        val manager = context.getSystemService(NotificationManager::class.java)

        val alerts = NotificationChannel(
            CHANNEL_ID_ALERTS,
            CHANNEL_NAME_ALERTS,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos con hora: entregas próximas o vencidas, clases y eventos de tu agenda."
            setShowBadge(true)
            enableVibration(true)
            enableLights(true)
            lightColor = BRAND_COLOR
        }

        val digest = NotificationChannel(
            CHANNEL_ID_DIGEST,
            CHANNEL_NAME_DIGEST,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Tu resumen de la mañana, notas pendientes de registrar y avisos sobre tus promedios."
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
            SubjectHintKind.MISSING_PERIODS -> "Te faltan cortes por registrar"
            SubjectHintKind.UNKNOWN_WEIGHTS -> "Faltan porcentajes"
            SubjectHintKind.NO_GRADES -> "Aún sin notas"
            SubjectHintKind.BELOW_PASSING -> "Vas por debajo de la nota mínima"
            SubjectHintKind.TARGET_UNREACHABLE -> "Tu meta está en riesgo"
            SubjectHintKind.BELOW_TARGET -> "Vas por debajo de tu meta"
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
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(
                    MainActivity.EXTRA_LAUNCH_ROUTE,
                    targetRoute ?: AppRoutes.notificationDetail(historyItem.id)
                )
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
            val notification = NotificationCompat.Builder(context, channelId)
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
                .build()
            notificationManager.notify(notificationId, notification)
        }
    }
}
