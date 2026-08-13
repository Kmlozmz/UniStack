package com.unistack.app.feature_updates.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.ReleaseVersion
import com.unistack.app.feature_updates.domain.UpdateChannel
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateState
import java.io.File
import java.io.IOException
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
import org.json.JSONArray
import org.json.JSONObject

private const val APK_FILE_NAME = "unistack-update.apk"
private const val PREFS_NAME = "unistack_update_checker"
private const val KEY_LAST_CHECKED_AT = "last_checked_at"
private const val KEY_CHANNEL = "update_channel"
private const val AUTO_CHECK_INTERVAL_MILLIS = 12 * 60 * 60 * 1000L

class GitHubReleaseUpdateRepository(
    private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val notificationManager = UpdateNotificationManager(context)
    private var downloadId: Long = -1L

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _channel = MutableStateFlow(readStoredChannel())
    override val channel: StateFlow<UpdateChannel> = _channel.asStateFlow()

    private fun readStoredChannel(): UpdateChannel {
        val stored = prefs.getString(KEY_CHANNEL, null) ?: return UpdateChannel.STABLE
        return runCatching { UpdateChannel.valueOf(stored) }.getOrDefault(UpdateChannel.STABLE)
    }

    override fun setChannel(channel: UpdateChannel) {
        prefs.edit { putString(KEY_CHANNEL, channel.name) }
        _channel.value = channel
    }

    override suspend fun checkForUpdates() {
        _state.value = UpdateState.Checking
        runCatching { fetchLatestRelease() }
            .onSuccess { info ->
                if (ReleaseVersion.isNewer(info.versionName, BuildConfig.VERSION_NAME)) {
                    _state.value = UpdateState.Available(info)
                    notificationManager.showUpdateAvailableNotification(info.versionName)
                } else {
                    _state.value = UpdateState.UpToDate
                    notificationManager.dismissNotification()
                }
            }
            .onFailure { error ->
                _state.value = UpdateState.Error(error.message ?: "No se pudo verificar actualizaciones.")
            }
    }

    override suspend fun checkForUpdatesIfDue() {
        val lastCheckedAt = prefs.getLong(KEY_LAST_CHECKED_AT, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheckedAt < AUTO_CHECK_INTERVAL_MILLIS) return
        prefs.edit { putLong(KEY_LAST_CHECKED_AT, now) }
        checkForUpdates()
    }

    /**
     * Devuelve la última versión publicada, o lanza explicando por qué no pudo saberlo.
     *
     * Se consulta la lista y no `/releases/latest`, que **excluye los preestrenos**: con una
     * alpha publicada, ese endpoint devolvía 404 y la app decía que no había ninguna
     * publicación. Se toma la primera que no sea borrador, que es la más reciente.
     */
    private suspend fun fetchLatestRelease(): UpdateInfo = withContext(Dispatchers.IO) {
        val url = URL("https://api.github.com/repos/${BuildConfig.GITHUB_REPO}/releases?per_page=10")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        connection.setRequestProperty("Accept", "application/vnd.github+json")
        try {
            /* Antes cualquier respuesta que no fuera 200 se convertía en null, y el
               llamador entiende null como «estás al día». Es decir: sin conexión, con la
               API caída, con el repositorio privado o con el límite de peticiones agotado,
               la app afirmaba que todo estaba en orden. Ahora cada caso se lanza con su
               motivo para que la pantalla pueda decir que no pudo comprobarlo, que es
               distinto de no tener nada que instalar. */
            when (val code = connection.responseCode) {
                HttpURLConnection.HTTP_OK -> Unit
                HttpURLConnection.HTTP_NOT_FOUND -> throw IOException(
                    "No se encontró ninguna publicación en ${BuildConfig.GITHUB_REPO}. " +
                        "Si el repositorio es privado, sus publicaciones no son visibles sin iniciar sesión."
                )
                HttpURLConnection.HTTP_FORBIDDEN -> throw IOException(
                    "GitHub rechazó la consulta, probablemente por exceso de peticiones. Inténtalo más tarde."
                )
                else -> throw IOException("GitHub respondió $code al consultar la última versión.")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val releases = JSONArray(body)
            if (releases.length() == 0) {
                throw IOException("Todavía no hay ninguna publicación en ${BuildConfig.GITHUB_REPO}.")
            }
            // La más reciente que acepte el canal, no la más reciente a secas: con el canal
            // estable, una alpha publicada después de la definitiva no es una actualización.
            val current = _channel.value
            val published = (0 until releases.length())
                .mapNotNull(releases::optJSONObject)
                .filterNot { it.optBoolean("draft", false) }
                .firstOrNull { current.accepts(it.optString("tag_name").removePrefix("v").removePrefix("V")) }
                ?: throw IOException(
                    "No hay ninguna versión publicada para el canal ${current.label}."
                )
            parseRelease(published)
                ?: throw IOException(
                    "La última publicación no trae ningún APK adjunto, así que no hay nada que descargar."
                )
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

    /**
     * Desde Android 8 instalar un APK exige que el usuario autorice a esta app como origen,
     * y esa autorización se concede en una pantalla de Ajustes, no en un diálogo.
     */
    override fun canInstallPackages(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    override fun openInstallPermissionSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun installUpdate() {
        val readyState = _state.value as? UpdateState.ReadyToInstall ?: return
        // Sin el permiso, lanzar el instalador dejaba al usuario en un desvío del sistema
        // sin contexto. Se le lleva directo al interruptor que necesita.
        if (!canInstallPackages()) {
            openInstallPermissionSettings()
            return
        }
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
