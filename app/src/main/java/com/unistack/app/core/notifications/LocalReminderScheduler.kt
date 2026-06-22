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

private const val CHANNEL_ID = "unistack_reminders"
private const val CHANNEL_NAME = "Recordatorios UniStack"
private const val MAX_REMINDERS_PER_KIND = 8
private const val MAX_SMART_SUBJECT_REMINDERS = 3
private const val REQUEST_CODE_PREFS = "unistack_scheduled_notifications"
private const val REQUEST_CODE_SET = "request_codes"
private const val DAILY_DIGEST_REQUEST_CODE = 910060001
private const val EXTRA_TITLE = "title"
private const val EXTRA_BODY = "body"
private const val EXTRA_NOTIFICATION_ID = "notification_id"
private const val EXTRA_TARGET_ROUTE = "target_route"

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
                .take(MAX_REMINDERS_PER_KIND)
                .forEach { task ->
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = task.id.stableRequestCode("task-lead"),
                        triggerAtMillis = task.dueDateMillis - leadMillis,
                        title = "Tarea próxima",
                        body = "${task.title} ${TaskDateUtils.dueText(task.dueDateMillis)}.",
                        targetRoute = AppRoutes.editTask(task.id)
                    )
                    if (currentProfile.overdueRemindersEnabled) {
                        scheduleReminder(
                            profile = currentProfile,
                            requestCode = task.id.stableRequestCode("task-overdue"),
                            triggerAtMillis = task.dueDateMillis + 60L * 60L * 1000L,
                            title = "Tarea vencida",
                            body = "${task.title} ya venció. Revísala cuando puedas.",
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
                .take(MAX_REMINDERS_PER_KIND)
                .forEach { task ->
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = task.id.stableRequestCode("task-grade-pending"),
                        triggerAtMillis = requireNotNull(task.completedAt) + 24L * 60L * 60L * 1000L,
                        title = "¿Ya recibiste la nota?",
                        body = "La tarea ${task.title} sigue esperando resultado. Regístralo cuando lo conozcas.",
                        targetRoute = AppRoutes.Tasks
                    )
                }
        }

        if (AppModule.ACADEMIC_TEMPLATES in currentProfile.enabledModules && currentProfile.academicWorkRemindersEnabled) {
            works.filterNot { it.status == AcademicWorkStatus.SUBMITTED }
                .filter { it.dueDateMillis != null }
                .sortedBy { it.dueDateMillis ?: Long.MAX_VALUE }
                .take(MAX_REMINDERS_PER_KIND)
                .forEach { work ->
                    val dueDateMillis = work.dueDateMillis ?: return@forEach
                    scheduleReminder(
                        profile = currentProfile,
                        requestCode = work.id.stableRequestCode("work-lead"),
                        triggerAtMillis = dueDateMillis - leadMillis,
                        title = "Trabajo próximo",
                        body = "${work.title} ${TaskDateUtils.dueText(dueDateMillis)}.",
                        targetRoute = AppRoutes.AcademicTemplates
                    )
                    if (currentProfile.overdueRemindersEnabled) {
                        scheduleReminder(
                            profile = currentProfile,
                            requestCode = work.id.stableRequestCode("work-overdue"),
                            triggerAtMillis = dueDateMillis + 60L * 60L * 1000L,
                            title = "Trabajo vencido",
                            body = "${work.title} ya venció. Revisa su checklist.",
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

        if (currentProfile.taskRemindersEnabled ||
            currentProfile.academicWorkRemindersEnabled ||
            currentProfile.overdueRemindersEnabled ||
            currentProfile.gradeInsightRemindersEnabled ||
            currentProfile.pendingGradeRemindersEnabled
        ) {
            scheduleReminder(
                profile = currentProfile,
                requestCode = DAILY_DIGEST_REQUEST_CODE,
                triggerAtMillis = nextTriggerAt(hour = 7, minute = 30, daysFromNow = 1),
                title = "Resumen del dia",
                body = smartDigestBody(currentProfile, tasks, works, subjects),
                targetRoute = AppRoutes.Home
            )
        }

        persistScheduledRequestCodes()
    }

    private fun scheduleClassReminders(
        profile: UserProfile,
        subjects: List<Subject>,
        sessions: List<ClassSession>,
        occurrences: List<ClassOccurrence>
    ) {
        sessions
            .filter { it.reminderMinutes > 0 && it.isValid }
            .mapNotNull { session ->
                nextClassOccurrence(session)?.let { trigger ->
                    session to trigger.minusMinutes(session.reminderMinutes.toLong())
                }
            }
            .filter { (_, trigger) -> trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() > System.currentTimeMillis() }
            .sortedBy { (_, trigger) -> trigger }
            .take(MAX_REMINDERS_PER_KIND)
            .forEach { (session, trigger) ->
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: "Tu clase"
                val locationSuffix = session.location.takeIf(String::isNotBlank)?.let { " en $it" }.orEmpty()
                scheduleReminder(
                    profile = profile,
                    requestCode = session.id.stableRequestCode("class-reminder"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    title = "$subjectName empieza pronto",
                    body = "Tu clase comienza en ${session.reminderMinutes} min$locationSuffix.",
                    targetRoute = AppRoutes.Calendar
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
            .take(MAX_REMINDERS_PER_KIND)
            .forEach { (session, start, epochDay) ->
                val subjectName = subjects.firstOrNull { it.id == session.subjectId }?.name ?: "tu clase"
                val trigger = start.toLocalDate()
                    .atStartOfDay()
                    .plusMinutes(session.endMinute.toLong() + 10L)
                scheduleReminder(
                    profile = profile,
                    requestCode = "${session.id}:$epochDay".stableRequestCode("class-attendance"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    title = "¿Asististe a $subjectName?",
                    body = "Registra tu asistencia, modalidad o cualquier cambio de esta clase.",
                    targetRoute = AppRoutes.Calendar
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
            .take(MAX_REMINDERS_PER_KIND)
            .forEach { (event, trigger) ->
                scheduleReminder(
                    profile = profile,
                    requestCode = event.id.stableRequestCode("agenda-event"),
                    triggerAtMillis = trigger.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                    title = event.title,
                    body = event.location.takeIf(String::isNotBlank)?.let { "Próximamente en $it." }
                        ?: "Tienes un evento próximo en tu agenda.",
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
                    title = hint.notificationTitle(),
                    body = hint.message,
                    targetRoute = AppRoutes.subjectDetail(hint.subject.id)
                )
            }
    }

    private fun gradeNotificationHints(profile: UserProfile, subjects: List<Subject>): List<SubjectNotificationHint> {
        val maxGrade = GradingScaleUtils.maxGradeFor(profile)
        return subjects.mapNotNull { subject ->
            val periods = subject.periodScheme.periods
            val average = GradeCalculator.calculateProjectedAverageByPeriods(subject.grades, periods)
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
                    severity = 3,
                    message = "Falta ${if (missingPriorPeriods == 1) "un corte anterior" else "$missingPriorPeriods cortes anteriores"} en ${subject.name}. Complétalo para afinar tu meta."
                )
                unknownWeights > 0 -> SubjectNotificationHint(
                    subject = subject,
                    severity = 2,
                    message = "${subject.name} tiene $unknownWeights ${if (unknownWeights == 1) "nota sin porcentaje" else "notas sin porcentaje"}. La proyección seguirá provisional."
                )
                subject.grades.isEmpty() -> SubjectNotificationHint(
                    subject = subject,
                    severity = 1,
                    message = "Aún no tienes notas en ${subject.name}. Agrega la primera para activar tu promedio real."
                )
                average != null && average < profile.passingGrade -> SubjectNotificationHint(
                    subject = subject,
                    severity = 4,
                    message = "${subject.name} está bajo la nota mínima con ${GradingScaleUtils.formatGrade(average, profile.gradingScale)}. Revisa el siguiente corte."
                )
                neededGrade != null && neededGrade > maxGrade -> SubjectNotificationHint(
                    subject = subject,
                    severity = 3,
                    message = "La meta de ${subject.name} está difícil con lo restante. Ajusta estrategia o pesos de notas."
                )
                average != null && average < subject.targetAverage -> SubjectNotificationHint(
                    subject = subject,
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

    private fun scheduleReminder(
        profile: UserProfile,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        body: String,
        targetRoute: String? = null
    ) {
        val adjustedTrigger = adjustForQuietHours(profile, triggerAtMillis)
        if (adjustedTrigger <= System.currentTimeMillis()) return
        val intent = reminderIntent(requestCode, title, body, targetRoute)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, adjustedTrigger, pendingIntent)
        scheduledRequestCodes.add(requestCode)
    }

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
        targetRoute: String?
    ): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
            putExtra(EXTRA_TARGET_ROUTE, targetRoute)
        }
    }

    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT).apply {
            description = "Avisos inteligentes para tareas, trabajos y seguimiento académico."
            setShowBadge(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
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

    private fun adjustForQuietHours(profile: UserProfile, triggerAtMillis: Long): Long {
        if (!profile.quietHoursEnabled) return triggerAtMillis
        val start = profile.quietHoursStartHour ?: return triggerAtMillis
        val end = profile.quietHoursEndHour ?: return triggerAtMillis
        if (start == end) return triggerAtMillis

        val zone = ZoneId.systemDefault()
        val trigger = Instant.ofEpochMilli(triggerAtMillis).atZone(zone)
        val hour = trigger.hour
        val isQuiet = if (start < end) {
            hour in start until end
        } else {
            hour >= start || hour < end
        }
        if (!isQuiet) return triggerAtMillis

        val endDate = when {
            start < end -> trigger.toLocalDate()
            hour >= start -> trigger.toLocalDate().plusDays(1)
            else -> trigger.toLocalDate()
        }
        return endDate.atTime(end, 0).atZone(zone).toInstant().toEpochMilli()
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

    private fun String.stableRequestCode(kind: String): Int {
        return "$kind:$this".hashCode() and Int.MAX_VALUE
    }

    private data class SubjectNotificationHint(
        val subject: Subject,
        val severity: Int,
        val message: String
    ) {
        fun notificationTitle(): String {
            val normalized = message.lowercase()
            return when {
                "sin porcentaje" in normalized -> "Faltan porcentajes en ${subject.name}"
                "corte anterior" in normalized || "cortes anteriores" in normalized -> "Completa cortes de ${subject.name}"
                "no tienes notas" in normalized -> "Empieza ${subject.name}"
                severity >= 4 -> "${subject.name} necesita atencion"
                "meta" in normalized && "dif" in normalized -> "Meta dificil en ${subject.name}"
                "meta" in normalized -> "${subject.name} bajo tu meta"
                else -> "Revisa ${subject.name}"
            }
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
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
            notificationManager.notify(notificationId, notification)
        }
    }
}
