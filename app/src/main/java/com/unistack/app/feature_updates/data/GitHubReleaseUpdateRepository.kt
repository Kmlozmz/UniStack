package com.unistack.app.feature_updates.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateState
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

private const val APK_FILE_NAME = "unistack-update.apk"
private const val PREFS_NAME = "unistack_update_checker"
private const val KEY_LAST_CHECKED_AT = "last_checked_at"
private const val AUTO_CHECK_INTERVAL_MILLIS = 12 * 60 * 60 * 1000L

class GitHubReleaseUpdateRepository(
    private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var downloadId: Long = -1L

    override suspend fun checkForUpdates() {
        _state.value = UpdateState.Checking
        runCatching { fetchLatestRelease() }
            .onSuccess { info ->
                _state.value = if (info != null && isNewerVersion(info.versionName, BuildConfig.VERSION_NAME)) {
                    UpdateState.Available(info)
                } else {
                    UpdateState.UpToDate
                }
            }
            .onFailure { error ->
                _state.value = UpdateState.Error(error.message ?: "No se pudo verificar actualizaciones.")
            }
    }

    override suspend fun checkForUpdatesIfDue() {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCheckedAt = prefs.getLong(KEY_LAST_CHECKED_AT, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheckedAt < AUTO_CHECK_INTERVAL_MILLIS) return
        prefs.edit { putLong(KEY_LAST_CHECKED_AT, now) }
        checkForUpdates()
    }

    private suspend fun fetchLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases/latest")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            parseRelease(JSONObject(body))
        } finally {
            connection.disconnect()
        }
    }

    private fun parseRelease(json: JSONObject): UpdateInfo? {
        val versionName = json.optString("tag_name").removePrefix("v").removePrefix("V")
        if (versionName.isBlank()) return null

        val assets = json.optJSONArray("assets") ?: return null
        var downloadUrl = ""
        var sizeBytes = 0L
        for (index in 0 until assets.length()) {
            val asset = assets.optJSONObject(index) ?: continue
            if (asset.optString("name").endsWith(".apk", ignoreCase = true)) {
                downloadUrl = asset.optString("browser_download_url")
                sizeBytes = asset.optLong("size")
                break
            }
        }
        if (downloadUrl.isBlank()) return null

        return UpdateInfo(
            versionName = versionName,
            releaseNotes = json.optString("body").trim().ifBlank { "Sin notas de la versión." },
            releaseDate = json.optString("published_at").take(10),
            downloadUrl = downloadUrl,
            sizeMb = sizeBytes / 1024.0 / 1024.0
        )
    }

    private fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = remote.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.toIntOrNull() }
        val length = maxOf(remoteParts.size, currentParts.size)
        for (index in 0 until length) {
            val remoteValue = remoteParts.getOrElse(index) { 0 }
            val currentValue = currentParts.getOrElse(index) { 0 }
            if (remoteValue != currentValue) return remoteValue > currentValue
        }
        return false
    }

    override fun downloadUpdate() {
        val info = (_state.value as? UpdateState.Available)?.info ?: return
        apkFile().delete()

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
}
