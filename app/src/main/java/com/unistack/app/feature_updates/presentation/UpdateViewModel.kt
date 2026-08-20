package com.unistack.app.feature_updates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.UpdateInfo
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

    /** Las publicaciones recientes, de la más nueva a la más vieja. */
    val releases: StateFlow<List<UpdateInfo>> = updateRepository.releases

    val currentVersionName: String = BuildConfig.VERSION_NAME
    val currentVersionCode: Int = BuildConfig.VERSION_CODE

    /**
     * Los APK que quedan en el disco.
     *
     * Era un `get()` que la pantalla leía al componerse: leer eso no suscribe a nada, así que
     * al borrarlos la tarjeta de limpieza se quedaba puesta hasta salir y volver a entrar.
     */
    val pendingApks: StateFlow<Int> = updateRepository.pendingApks

    fun refreshPendingApks() {
        updateRepository.refreshPendingApks()
    }

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

    fun openInstallPermissionSettings() {
        updateRepository.openInstallPermissionSettings()
    }

    fun clearDownload() {
        updateRepository.clearDownload()
    }

    fun dismiss() {
        updateRepository.dismiss()
    }
}
