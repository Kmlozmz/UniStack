package com.unistack.app.feature_updates.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.unistack.app.MainActivity
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import java.util.Locale

/**
 * Los avisos de actualización, que son **uno solo que va cambiando**.
 *
 * Decidido el 19 sep 2026: llega «Hay una nueva versión de UniStack disponible», sin versión ni
 * botones; si se descarga, ese mismo aviso pasa a ser la barra de progreso, y al acabar dice
 * «lista para instalar» con su botón. Un aviso por versión, con el mismo número, que se va
 * reescribiendo en vez de acumular tres en la bandeja.
 *
 * El primero suena (canal alto); el progreso y el «lista» van por un canal **sin sonido**: son
 * consecuencia de algo que el usuario ya sabe, no una noticia.
 */
class UpdateNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "unistack_updates"
        private const val PROGRESS_CHANNEL_ID = "unistack_updates_progress"
        private const val NOTIFICATION_ID = 1001

        const val EXTRA_NAVIGATE_TO = "navigate_to"
        const val EXTRA_INSTALL = "install_update"
        const val NAVIGATE_UPDATES = "updates"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                Textos.get(R.string.update_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = Textos.get(R.string.update_channel_desc)
                enableLights(true)
                enableVibration(true)
            }
        )
        manager.createNotificationChannel(
            NotificationChannel(
                PROGRESS_CHANNEL_ID,
                Textos.get(R.string.update_progress_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = Textos.get(R.string.update_progress_channel_desc)
            }
        )
    }

    /** Hay algo nuevo. Tocarlo abre Actualizaciones con la ficha de la versión. */
    fun showUpdateAvailableNotification() {
        val notification = builder(CHANNEL_ID)
            .setContentTitle(Textos.get(R.string.update_notif_title))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager().notify(NOTIFICATION_ID, notification)
    }

    /**
     * Descargando: el mismo aviso, ahora con barra.
     *
     * @param progress de 0 a 100, o negativo si no se sabe cuánto pesa (la barra se mueve sola).
     */
    fun showDownloadingNotification(versionName: String, progress: Int, sizeMb: Double) {
        val unknown = progress < 0
        val body = if (unknown) {
            Textos.get(R.string.update_notif_downloading_unknown)
        } else {
            Textos.get(R.string.update_notif_downloading_body, progress, String.format(Locale.US, "%.1f", sizeMb))
        }
        val notification = builder(PROGRESS_CHANNEL_ID)
            .setContentTitle(Textos.get(R.string.update_notif_downloading_title, versionName))
            .setContentText(body)
            .setProgress(100, progress.coerceAtLeast(0), unknown)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        manager().notify(NOTIFICATION_ID, notification)
    }

    /** Descargada: tocarlo abre Actualizaciones; «Instalar» abre además el instalador. */
    fun showReadyNotification(versionName: String) {
        val install = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_UPDATES)
            putExtra(EXTRA_INSTALL, true)
        }
        val installIntent = PendingIntent.getActivity(
            context,
            1,
            install,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notification = builder(PROGRESS_CHANNEL_ID)
            .setContentTitle(Textos.get(R.string.update_notif_ready_title, versionName))
            .setContentText(Textos.get(R.string.update_notif_ready_body))
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, Textos.get(R.string.updates_btn_install), installIntent)
            .build()
        manager().notify(NOTIFICATION_ID, notification)
    }

    fun dismissNotification() {
        manager().cancel(NOTIFICATION_ID)
    }

    private fun builder(channel: String): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NAVIGATE_TO, NAVIGATE_UPDATES)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_unistack)
            .setContentIntent(pendingIntent)
    }

    private fun manager(): NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
}
