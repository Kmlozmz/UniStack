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
import com.unistack.app.MainActivity
import com.unistack.app.R
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskDateUtils
import com.unistack.app.feature_templates.domain.AcademicWork
import com.unistack.app.feature_templates.domain.AcademicWorkStatus
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.UserProfile

private const val CHANNEL_ID = "unistack_reminders"
private const val CHANNEL_NAME = "Recordatorios UniStack"
private const val EXTRA_TITLE = "title"
private const val EXTRA_BODY = "body"
private const val EXTRA_NOTIFICATION_ID = "notification_id"

class LocalReminderScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val scheduledRequestCodes = mutableSetOf<Int>()

    fun schedule(profile: UserProfile?, tasks: List<StudentTask>, works: List<AcademicWork>) {
        createChannel()
        cancelPrevious()
        val currentProfile = profile ?: return
        val leadMillis = currentProfile.reminderLeadHours.coerceIn(1, 168) * 60L * 60L * 1000L

        if (AppModule.TASKS in currentProfile.enabledModules && currentProfile.taskRemindersEnabled) {
            tasks.filterNot { it.completed }.forEach { task ->
                scheduleReminder(
                    requestCode = task.id.stableRequestCode("task-lead"),
                    triggerAtMillis = task.dueDateMillis - leadMillis,
                    title = "Tarea próxima",
                    body = "${task.title} ${TaskDateUtils.dueText(task.dueDateMillis)}."
                )
                if (currentProfile.overdueRemindersEnabled) {
                    scheduleReminder(
                        requestCode = task.id.stableRequestCode("task-overdue"),
                        triggerAtMillis = task.dueDateMillis + 9L * 60L * 60L * 1000L,
                        title = "Tarea vencida",
                        body = "${task.title} ya venció. Revísala cuando puedas."
                    )
                }
            }
        }

        if (AppModule.ACADEMIC_TEMPLATES in currentProfile.enabledModules && currentProfile.academicWorkRemindersEnabled) {
            works.filterNot { it.status == AcademicWorkStatus.SUBMITTED }
                .filter { it.dueDateMillis != null }
                .forEach { work ->
                    val dueDateMillis = work.dueDateMillis ?: return@forEach
                    scheduleReminder(
                        requestCode = work.id.stableRequestCode("work-lead"),
                        triggerAtMillis = dueDateMillis - leadMillis,
                        title = "Trabajo próximo",
                        body = "${work.title} ${TaskDateUtils.dueText(dueDateMillis)}."
                    )
                    if (currentProfile.overdueRemindersEnabled) {
                        scheduleReminder(
                            requestCode = work.id.stableRequestCode("work-overdue"),
                            triggerAtMillis = dueDateMillis + 9L * 60L * 60L * 1000L,
                            title = "Trabajo vencido",
                            body = "${work.title} ya venció. Revisa su checklist."
                        )
                    }
                }
        }
    }

    private fun scheduleReminder(
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        body: String
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return
        val intent = reminderIntent(requestCode, title, body)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        scheduledRequestCodes.add(requestCode)
    }

    private fun cancelPrevious() {
        scheduledRequestCodes.toList().forEach(::cancel)
        scheduledRequestCodes.clear()
    }

    private fun cancel(requestCode: Int) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            reminderIntent(requestCode, "", ""),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun reminderIntent(requestCode: Int, title: String, body: String): Intent {
        return Intent(context, ReminderReceiver::class.java).apply {
            putExtra(EXTRA_TITLE, title)
            putExtra(EXTRA_BODY, body)
            putExtra(EXTRA_NOTIFICATION_ID, requestCode)
        }
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun String.stableRequestCode(kind: String): Int {
        return "$kind:$this".hashCode() and Int.MAX_VALUE
    }

    companion object {
        fun showNotification(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val launchIntent = Intent(context, MainActivity::class.java)
            val contentIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            val body = intent.getStringExtra(EXTRA_BODY).orEmpty()
            val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, title.hashCode() and Int.MAX_VALUE)
            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setContentIntent(contentIntent)
                .setAutoCancel(true)
                .build()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
        }
    }
}
