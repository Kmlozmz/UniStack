package com.unistack.app.feature_updates.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.unistack.app.core.di.UniStackEntryPoint
import com.unistack.app.feature_updates.domain.CheckInterval
import dagger.hilt.android.EntryPointAccessors
import java.util.concurrent.TimeUnit

/**
 * Busca actualizaciones cada pocas horas aunque la app esté cerrada.
 *
 * Antes solo se miraba al arrancar el proceso y como mucho una vez cada doce horas, así que
 * enterarse de una versión nueva dependía de cerrar la app del todo y volver a abrirla al día
 * siguiente. Quien la dejaba en segundo plano no se enteraba nunca.
 *
 * **Esto sigue siendo consultar cada tanto, no un aviso instantáneo.** Para que la novedad
 * llegue en el momento hace falta que el aviso venga de fuera —una notificación push—, y eso
 * exige un servicio de mensajería configurado; hoy no lo hay.
 *
 * Usa el mismo repositorio que la app y no uno propio: desde que la descarga puede empezar
 * aquí, con Wi-Fi y sin nadie delante, el estado tiene que ser uno solo, o la app abriría sin
 * saber que ya está bajando.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val repository = EntryPointAccessors
            .fromApplication(applicationContext, UniStackEntryPoint::class.java)
            .updateRepository()
        return runCatching { repository.checkForUpdatesIfDue() }
            .fold(
                onSuccess = { Result.success() },
                // Sin red o con GitHub caído se reintenta; no es un fallo del trabajo.
                onFailure = { Result.retry() }
            )
    }

    companion object {
        private const val WORK_NAME = "unistack-update-check"

        /**
         * @param replace al cambiar «cada cuánto» en el menú. Al arrancar va en falso: reprogramar
         *   en cada arranque reiniciaría el contador, y la app que se abre a menudo no llegaría
         *   nunca a ejecutarlo.
         */
        fun schedule(context: Context, interval: CheckInterval, replace: Boolean = false) {
            val workManager = WorkManager.getInstance(context)
            if (interval == CheckInterval.MANUAL) {
                workManager.cancelUniqueWork(WORK_NAME)
                return
            }
            val hours = if (interval == CheckInterval.DAILY) 24L else 2L
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(hours, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                if (replace) ExistingPeriodicWorkPolicy.UPDATE else ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
