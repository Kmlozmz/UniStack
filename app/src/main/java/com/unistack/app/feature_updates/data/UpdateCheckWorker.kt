package com.unistack.app.feature_updates.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
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
 * Crea su propio repositorio en vez de recibirlo inyectado: [GitHubReleaseUpdateRepository] solo
 * necesita un `Context`, y la marca de la última consulta la comparten por preferencias, así que
 * ni se pisan ni consultan de más.
 */
class UpdateCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return runCatching { GitHubReleaseUpdateRepository(applicationContext).checkForUpdatesIfDue() }
            .fold(
                onSuccess = { Result.success() },
                // Sin red o con GitHub caído se reintenta; no es un fallo del trabajo.
                onFailure = { Result.retry() }
            )
    }

    companion object {
        private const val WORK_NAME = "unistack-update-check"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<UpdateCheckWorker>(2, TimeUnit.HOURS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            // KEEP y no UPDATE: reprogramar en cada arranque reiniciaría el contador, y la app
            // que se abre a menudo no llegaría nunca a ejecutarlo.
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }
    }
}
