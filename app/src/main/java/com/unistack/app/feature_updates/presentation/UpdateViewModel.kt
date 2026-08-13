package com.unistack.app.feature_updates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.UpdateChannel
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateViewModel @Inject constructor(
    private val updateRepository: UpdateRepository
) : ViewModel() {

    val state: StateFlow<UpdateState> = updateRepository.state
    val channel: StateFlow<UpdateChannel> = updateRepository.channel
    val unlockedChannel: StateFlow<UpdateChannel> = updateRepository.unlockedChannel

    /** Devuelve el canal que abrio el codigo, o null si no vale. */
    suspend fun redeemAccessCode(code: String): UpdateChannel? =
        updateRepository.redeemAccessCode(code)

    /**
     * Cambiar de canal vuelve a consultar en el acto: si bajas de alpha a estable, lo que la
     * pantalla enseñaba puede haber dejado de ser una actualización para ti.
     */
    fun setChannel(channel: UpdateChannel) {
        updateRepository.setChannel(channel)
        viewModelScope.launch { updateRepository.checkForUpdates() }
    }

    val currentVersionName: String = BuildConfig.VERSION_NAME
    val currentVersionCode: Int = BuildConfig.VERSION_CODE
    val hasPendingDownload: Boolean get() = updateRepository.hasPendingDownload()

    fun checkForUpdates() {
        viewModelScope.launch {
            updateRepository.checkForUpdates()
        }
    }

    fun downloadUpdate() {
        updateRepository.downloadUpdate()
    }

    fun installUpdate() {
        updateRepository.installUpdate()
    }

    /**
     * Se consulta en cada composición y no se cachea: el usuario puede conceder el permiso
     * en Ajustes y volver, y el estado tiene que reflejarlo.
     */
    fun canInstallPackages(): Boolean = updateRepository.canInstallPackages()

    fun clearDownload() {
        updateRepository.clearDownload()
    }

    fun dismiss() {
        updateRepository.dismiss()
    }
}
