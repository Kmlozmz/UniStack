package com.unistack.app.feature_updates.domain

import android.net.Uri

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo, val progress: Int) : UpdateState
    data class ReadyToInstall(val info: UpdateInfo, val apkUri: Uri) : UpdateState
    data object UpToDate : UpdateState
    data class Error(val message: String) : UpdateState
}
