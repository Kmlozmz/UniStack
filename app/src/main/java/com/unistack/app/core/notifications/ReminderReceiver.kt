package com.unistack.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Recibe tanto los avisos como el rearmado de madrugada.
 *
 * El rearmado no lleva nada que enseñar: solo existe para que la ventana de programación
 * deslice sin depender de que el usuario abra la app. Si el proceso estaba muerto, el simple
 * hecho de entrar aquí construye la Application y `ReminderCoordinator.start()` reprograma
 * todo por su cuenta; si estaba vivo, hay que pedírselo, porque el flujo que vigila los datos
 * solo reacciona a cambios y de madrugada no cambia nada.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.getBooleanExtra(EXTRA_REARM, false)) {
            ReminderCoordinator.rescheduleNow()
            return
        }
        LocalReminderScheduler.showNotification(context, intent)
    }
}
