package com.unistack.app.feature_updates.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateState
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

private const val APK_FILE_NAME = "unistack-update.apk"
private const val MIN_FETCH_INTERVAL_SECONDS = 43_200L

class RemoteConfigUpdateRepository(
    private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var downloadId: Long = -1L

    private val remoteConfig: FirebaseRemoteConfig by lazy {
        ensureFirebaseConfigured()
        Firebase.remoteConfig.apply {
            setConfigSettingsAsync(
                remoteConfigSettings { minimumFetchIntervalInSeconds = MIN_FETCH_INTERVAL_SECONDS }
            )
        }
    }

    override suspend fun checkForUpdates() {
        _state.value = UpdateState.Checking
        runCatching {
            remoteConfig.fetchAndActivate().await()
            UpdateInfo(
                versionCode = remoteConfig.getLong("latest_version_code"),
                versionName = remoteConfig.getString("latest_version_name"),
                releaseNotes = remoteConfig.getString("release_notes"),
                releaseDate = remoteConfig.getString("release_date"),
                downloadUrl = remoteConfig.getString("download_url"),
                sizeMb = remoteConfig.getDouble("apk_size_mb"),
                isForced = BuildConfig.VERSION_CODE < remoteConfig.getLong("min_version_code")
            )
        }.onSuccess { info ->
            _state.value = if (info.versionCode > BuildConfig.VERSION_CODE && info.downloadUrl.isNotBlank()) {
                UpdateState.Available(info)
            } else {
                UpdateState.UpToDate
            }
        }.onFailure { error ->
            _state.value = UpdateState.Error(error.message ?: "No se pudo verificar actualizaciones.")
        }
    }

    override fun downloadUpdate() {
        val info = (_state.value as? UpdateState.Available)?.info ?: return
        val destination = apkFile()
        destination.delete()

        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
            .setTitle("UniStack ${info.versionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setAllowedOverMetered(true)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
        _state.value = UpdateState.Downloading(info, 0)
        observeDownload(downloadManager, info)
    }

    private fun observeDownload(downloadManager: DownloadManager, info: UpdateInfo) {
        scope.launch {
            while (isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query) ?: break
                var shouldStop = false
                cursor.use {
                    if (!it.moveToFirst()) return@use
                    val status = it.getInt(it.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            _state.value = UpdateState.ReadyToInstall(info, apkUri())
                            shouldStop = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _state.value = UpdateState.Error("La descarga falló. Intenta de nuevo.")
                            shouldStop = true
                        }
                        else -> {
                            val downloaded = it.getLong(it.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR))
                            val total = it.getLong(it.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)).coerceAtLeast(1)
                            val progress = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                            _state.value = UpdateState.Downloading(info, progress)
                        }
                    }
                }
                if (shouldStop) return@launch
                delay(500)
            }
        }
    }

    override fun installUpdate() {
        val readyState = _state.value as? UpdateState.ReadyToInstall ?: return
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(readyState.apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override fun clearDownload() {
        apkFile().delete()
        _state.value = UpdateState.Idle
    }

    override fun dismiss() {
        _state.value = UpdateState.Idle
    }

    override fun hasPendingDownload(): Boolean = apkFile().exists()

    private fun apkFile(): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)

    private fun apkUri(): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile())

    private fun ensureFirebaseConfigured() {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
        check(FirebaseApp.getApps(context).isNotEmpty()) {
            "Falta app/google-services.json para inicializar Firebase."
        }
    }
}
