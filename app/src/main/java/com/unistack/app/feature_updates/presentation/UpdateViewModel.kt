package com.unistack.app.feature_updates.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.unistack.app.BuildConfig
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

    fun clearDownload() {
        updateRepository.clearDownload()
    }

    fun dismiss() {
        updateRepository.dismiss()
    }
}
