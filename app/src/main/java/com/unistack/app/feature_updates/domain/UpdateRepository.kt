package com.unistack.app.feature_updates.domain

import kotlinx.coroutines.flow.StateFlow

interface UpdateRepository {
    val state: StateFlow<UpdateState>
    suspend fun checkForUpdates()
    fun downloadUpdate()
    fun installUpdate()
    fun clearDownload()
    fun dismiss()
    fun hasPendingDownload(): Boolean
}
