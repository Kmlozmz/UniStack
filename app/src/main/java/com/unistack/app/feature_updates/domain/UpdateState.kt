package com.unistack.app.feature_updates.domain

import android.net.Uri

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Int) : UpdateState
    data class ReadyToInstall(val info: UpdateInfo, val apkUri: Uri) : UpdateState

    /** Lo instalado y lo último del canal son la misma versión. */
    data object UpToDate : UpdateState

    /**
     * Lo último publicado en este canal es **anterior** a lo que hay instalado.
     *
     * Pasa en cuanto se cambia de canal: con una alpha puesta y el canal beta elegido, la
     * última beta es más vieja que lo que llevas. No hay nada que instalar, pero decir «al
     * día» era mentira —lo que tienes no es lo que ese canal publica— y dejaba pensando que
     * la beta que estabas esperando ya la tenías.
     */
    data class Ahead(val info: UpdateInfo) : UpdateState

    /**
     * El canal no tiene ninguna versión publicada que ofrecer.
     *
     * En pantalla se cuenta como lo que es para quien lo lee —no hay nada más nuevo que
     * instalar—, no como un fallo: «no se encontró ninguna publicación» sonaba a avería.
     */
    data object NoReleases : UpdateState

    data class Error(val message: String) : UpdateState

    companion object {
        /** Descargando sin saber cuánto pesa: la barra se mueve sola en vez de mentir. */
        const val UNKNOWN_PROGRESS = -1
    }
}

/**
 * Qué contar después de mirar el canal.
 *
 * La regla es una sola: **al día solo si lo instalado coincide con lo último del canal**. Antes
 * bastaba con que lo publicado no fuera más nuevo, así que cualquier cosa por debajo —una beta
 * vieja mirada desde una alpha, o un canal sin publicar nada— se anunciaba como «al día».
 */
fun resolveUpdateState(latest: UpdateInfo?, installed: String): UpdateState = when {
    latest == null -> UpdateState.NoReleases
    ReleaseVersion.isNewer(latest.versionName, installed) -> UpdateState.Available(latest)
    ReleaseVersion.isSame(latest.versionName, installed) -> UpdateState.UpToDate
    else -> UpdateState.Ahead(latest)
}
