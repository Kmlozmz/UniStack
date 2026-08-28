package com.unistack.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus

internal const val ACTION_MARK_ATTENDANCE = "com.unistack.app.MARK_ATTENDANCE"
internal const val EXTRA_ATTENDANCE_SESSION_ID = "attendance_session_id"
internal const val EXTRA_ATTENDANCE_EPOCH_DAY = "attendance_epoch_day"
internal const val EXTRA_ATTENDANCE_STATUS = "attendance_status"

/**
 * Marcar la asistencia desde la propia notificación, sin abrir la app.
 *
 * El aviso preguntaba «¿asististe?» y, para contestarlo, había que abrir la app, ir al
 * calendario, encontrar la clase y entrar en su panel. Cuatro pasos para una respuesta de sí o
 * no: se descarta desde la pantalla de bloqueo y la asistencia se queda sin registrar, que es
 * justo lo que el aviso venía a evitar.
 *
 * Se escribe por [ReminderCoordinator] y no inyectando aquí el repositorio porque este proceso
 * puede estar recién levantado por el propio broadcast: el coordinador ya guarda lo que hace
 * falta para operar sin una pantalla viva delante.
 */
class AttendanceActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_MARK_ATTENDANCE) return

        val sessionId = intent.getStringExtra(EXTRA_ATTENDANCE_SESSION_ID) ?: return
        val epochDay = intent.getLongExtra(EXTRA_ATTENDANCE_EPOCH_DAY, -1L)
        if (epochDay < 0L) return
        val estado = runCatching {
            ClassAttendanceStatus.valueOf(intent.getStringExtra(EXTRA_ATTENDANCE_STATUS).orEmpty())
        }.getOrNull() ?: return

        ReminderCoordinator.markAttendance(sessionId, epochDay, estado)

        // El aviso ya no tiene nada que preguntar, así que se retira solo.
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)
        if (notificationId >= 0) {
            NotificationManagerCompat.from(context).cancel(notificationId)
        }
    }
}
