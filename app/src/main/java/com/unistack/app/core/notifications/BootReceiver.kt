package com.unistack.app.core.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

/* Android borra TODAS las alarmas de una app al reiniciar el teléfono y al
   reinstalarla, así que sin este receptor los avisos no vuelven hasta que el
   usuario abre la app a mano.

   Aquí no hace falta reprogramar nada: arrancar cualquier componente de la app
   —incluido este receptor— crea la Application, y UniStackApplication.onCreate()
   ya llama a ReminderCoordinator.start(), que recolecta los repositorios y
   vuelve a programar todo. El receptor solo existe para provocar ese arranque y
   para sostener el proceso mientras la primera pasada termina. */
private const val RESCHEDULE_TIMEOUT_MS = 8_000L

private val HANDLED_ACTIONS = setOf(
    Intent.ACTION_BOOT_COMPLETED,
    Intent.ACTION_MY_PACKAGE_REPLACED
)

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action !in HANDLED_ACTIONS) return
        /* Al volver de onReceive() el proceso queda sin componentes vivos y el
           sistema puede matarlo en cualquier momento —justo después de un
           arranque, con toda la cola de apps despertando, es probable—, pero los
           flows todavía están leyendo de Room y DataStore. goAsync() lo mantiene
           en pie hasta que la reprogramación real ocurre.

           El margen es de segundos, no de minutos: se corta muy por debajo del
           ANR del broadcast (10 s en el caso más estricto). Leer cinco flows
           locales tarda bastante menos, y si el arranque va tan cargado que no
           cabe, se acaba el plazo y se suelta el broadcast igualmente. */
        val pendingResult = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
            try {
                withTimeoutOrNull(RESCHEDULE_TIMEOUT_MS) {
                    ReminderCoordinator.awaitFirstSchedule()
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
