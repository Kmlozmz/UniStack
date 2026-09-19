package com.unistack.app.feature_updates.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.edit
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.CheckInterval
import com.unistack.app.feature_updates.domain.HistorialDeVersiones
import com.unistack.app.feature_updates.domain.InstalledVersion
import com.unistack.app.feature_updates.domain.ReleaseVersion
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateSettings
import com.unistack.app.feature_updates.domain.UpdateState
import com.unistack.app.feature_updates.domain.resolveUpdateState
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

private const val APK_FILE_NAME = "unistack-update.apk"
private const val PREFS_NAME = "unistack_update_checker"
private const val KEY_LAST_CHECKED_AT = "last_checked_at"
private const val KEY_AUTO_DOWNLOAD_WIFI = "auto_download_wifi"
private const val KEY_NOTIFY_READY = "notify_ready"
private const val KEY_CHECK_INTERVAL = "check_interval"
private const val KEY_SHEET_SEEN = "sheet_seen_version"
private const val KEY_DOWNLOAD_ID = "download_id"
private const val KEY_DOWNLOAD_INFO = "download_info"
private const val KEY_INSTALL_HISTORY = "install_history"

/**
 * Cada cuánto se deja consultar por su cuenta.
 *
 * Eran doce horas, que con la comprobación atada al arranque del proceso significaba enterarse
 * al día siguiente. Consultar es una petición diminuta; lo que hay que evitar es repetirla en
 * cada vuelta a la app, no espaciarla medio día. Con «cada día» elegido, se espacia de verdad.
 */
private const val AUTO_CHECK_INTERVAL_MILLIS = 45 * 60 * 1000L
private const val DAILY_CHECK_INTERVAL_MILLIS = 20 * 60 * 60 * 1000L

class GitHubReleaseUpdateRepository(
    private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val notificationManager = UpdateNotificationManager(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private var downloadId: Long = prefs.getLong(KEY_DOWNLOAD_ID, -1L)
    private var downloadJob: Job? = null

    /** Si la descarga en curso la pidió el usuario desde la app: al acabar se abre el instalador. */
    private var downloadStartedByUser = false

    /**
     * Cuántos APK descargados quedan ocupando espacio.
     *
     * Era una función que la pantalla llamaba al componerse, así que no se enteraba de nada:
     * borrabas el archivo y la tarjeta de limpieza seguía ahí hasta salir y volver a entrar.
     * Como flujo, la lista se entera en el momento.
     */
    private val _pendingApks = MutableStateFlow(apkFiles().size)
    override val pendingApks: StateFlow<Int> = _pendingApks.asStateFlow()

    private val _releases = MutableStateFlow<List<UpdateInfo>>(emptyList())
    override val releases: StateFlow<List<UpdateInfo>> = _releases.asStateFlow()

    private val _lastCheckedAt = MutableStateFlow(prefs.getLong(KEY_LAST_CHECKED_AT, 0L))
    override val lastCheckedAt: StateFlow<Long> = _lastCheckedAt.asStateFlow()

    private val _settings = MutableStateFlow(readSettings())
    override val settings: StateFlow<UpdateSettings> = _settings.asStateFlow()

    private val _installHistory = MutableStateFlow(readHistory())
    override val installHistory: StateFlow<List<InstalledVersion>> = _installHistory.asStateFlow()

    private val _sheetSeenVersion = MutableStateFlow(prefs.getString(KEY_SHEET_SEEN, null))
    override val sheetSeenVersion: StateFlow<String?> = _sheetSeenVersion.asStateFlow()

    init {
        recordInstalledVersion()
        resumeDownloadIfAny()
    }

    // ------------------------------------------------------------------ comprobar

    override suspend fun checkForUpdates() {
        runCheck(automatic = false)
    }

    /**
     * La comprobación que hace la app sola: al arrancar y desde el trabajo en segundo plano.
     *
     * Respeta «cada cuánto» del menú: con «solo a mano» no mira nunca por su cuenta. Es la única
     * que avisa y la única que descarga sola, porque es la única que pasa sin nadie delante.
     */
    override suspend fun checkForUpdatesIfDue() {
        val interval = _settings.value.checkInterval
        if (interval == CheckInterval.MANUAL) return
        val lastCheckedAt = prefs.getLong(KEY_LAST_CHECKED_AT, 0L)
        val now = System.currentTimeMillis()
        val gap = if (interval == CheckInterval.DAILY) DAILY_CHECK_INTERVAL_MILLIS else AUTO_CHECK_INTERVAL_MILLIS
        if (now - lastCheckedAt < gap) return
        if (!runCheck(automatic = true)) {
            // Solo una comprobación real cuenta como comprobación. Si GitHub o la red fallan,
            // WorkManager recibe el fallo y puede reintentar antes del siguiente ciclo.
            throw IOException(Textos.get(R.string.update_check_failed))
        }
    }

    /**
     * @param automatic si la comprobación la hizo la app sola. Solo entonces avisa y descarga
     *   por su cuenta, y solo si nadie está mirando: con la app delante lo cuenta la hoja de
     *   Inicio, y un aviso encima sería contarlo dos veces.
     */
    private suspend fun runCheck(automatic: Boolean): Boolean {
        val sinNadieDelante = automatic && !appEnPrimerPlano()
        // Una descarga en marcha o ya lista no se pisa con «hay una versión disponible»: lo
        // que hay que contar es que ya está bajando, o que ya está.
        val current = _state.value
        if (current is UpdateState.Downloading) return true
        if (current !is UpdateState.ReadyToInstall) _state.value = UpdateState.Checking
        return runCatching { fetchReleases() }
            .onSuccess { published ->
                _releases.value = published
                val now = System.currentTimeMillis()
                prefs.edit { putLong(KEY_LAST_CHECKED_AT, now) }
                _lastCheckedAt.value = now
                val info = published.firstOrNull()
                val resolved = resolveUpdateState(info, BuildConfig.VERSION_NAME)
                val ready = _state.value as? UpdateState.ReadyToInstall
                if (ready != null && resolved is UpdateState.Available && resolved.info.versionName == ready.info.versionName) {
                    return@onSuccess
                }
                _state.value = resolved
                if (resolved is UpdateState.Available) {
                    if (sinNadieDelante) {
                        notificationManager.showUpdateAvailableNotification()
                        if (_settings.value.autoDownloadOnWifi && isOnUnmeteredNetwork()) {
                            startDownload(resolved.info, byUser = false)
                        }
                    }
                } else {
                    notificationManager.dismissNotification()
                }
            }
            .onFailure { error ->
                if (_state.value !is UpdateState.ReadyToInstall) {
                    _state.value = UpdateState.Error(error.message ?: Textos.get(R.string.update_check_failed))
                }
            }
            .isSuccess
    }

    /**
     * Las publicaciones recientes, de la más nueva a la más vieja.
     *
     * Se consulta la lista y no `/releases/latest`, que **excluye los preestrenos**: con una
     * beta publicada, ese endpoint devolvía 404 y la app decía que no había ninguna
     * publicación. Aquí no se filtra nada más que los borradores; la primera que quede es la
     * última publicada, sea preestreno o definitiva.
     */
    private suspend fun fetchReleases(): List<UpdateInfo> = withContext(Dispatchers.IO) {
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
                    Textos.get(R.string.update_err_not_found, BuildConfig.GITHUB_REPO)
                )
                HttpURLConnection.HTTP_FORBIDDEN -> throw IOException(
                    Textos.get(R.string.update_err_forbidden)
                )
                else -> throw IOException(Textos.get(R.string.update_err_code, code))
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val releases = JSONArray(body)
            (0 until releases.length())
                .mapNotNull(releases::optJSONObject)
                .filterNot { it.optBoolean("draft", false) }
                .mapNotNull(::parseRelease)
                // Las alphas no se ofrecen: se prueban por el bot, con quien toque, y no
                // tienen por qué llegarle a nadie que abra la app. Betas y definitivas sí,
                // que es lo que se está publicando mientras la app madura.
                .filterNot { BuildStage.of(it.versionName) == BuildStage.ALPHA }
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
            releaseNotes = json.optString("body").trim().ifBlank { Textos.get(R.string.update_no_notes) },
            releaseDate = json.optString("published_at").take(10),
            downloadUrl = downloadUrl,
            sizeMb = sizeBytes / 1024.0 / 1024.0
        )
    }

    // ------------------------------------------------------------------ descargar

    override fun downloadUpdate() {
        val info = (_state.value as? UpdateState.Available)?.info ?: return
        startDownload(info, byUser = true)
    }

    private fun startDownload(info: UpdateInfo, byUser: Boolean) {
        apkFile().delete()

        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, APK_FILE_NAME)
            .setTitle("UniStack ${info.versionName}")
            // El progreso lo cuenta el aviso de la app, que es el mismo que dijo que había
            // versión nueva; el del gestor del sistema sería un segundo aviso para lo mismo.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
            .setAllowedOverMetered(byUser || !_settings.value.autoDownloadOnWifi)

        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadId = downloadManager.enqueue(request)
        downloadStartedByUser = byUser
        prefs.edit {
            putLong(KEY_DOWNLOAD_ID, downloadId)
            putString(KEY_DOWNLOAD_INFO, info.toJson().toString())
        }
        refreshPendingApks()
        _state.value = UpdateState.Downloading(info, 0)
        notificationManager.showDownloadingNotification(info.versionName, 0, info.sizeMb)
        observeDownload(downloadManager, info)
    }

    /**
     * Si el proceso murió con una descarga a medias —o con una ya terminada que nadie
     * llegó a ver—, al arrancar se retoma desde el gestor del sistema en vez de olvidarla.
     */
    private fun resumeDownloadIfAny() {
        val info = prefs.getString(KEY_DOWNLOAD_INFO, null)?.let(::infoFromJson) ?: return
        if (!ReleaseVersion.isNewer(info.versionName, BuildConfig.VERSION_NAME)) {
            forgetDownload()
            return
        }
        if (downloadId >= 0) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val status = downloadManager.statusOf(downloadId)
            when (status) {
                DownloadManager.STATUS_SUCCESSFUL -> finishDownload(info)
                DownloadManager.STATUS_FAILED, null -> forgetDownload()
                else -> {
                    _state.value = UpdateState.Downloading(info, 0)
                    observeDownload(downloadManager, info)
                }
            }
            return
        }
        // Sin descarga en marcha pero con el archivo ya bajado y firmado: sigue lista.
        if (isSignedByThisApp(apkFile())) {
            _state.value = UpdateState.ReadyToInstall(info, apkUri())
        } else {
            forgetDownload()
        }
    }

    /**
     * Aviso del sistema de que una descarga acabó, aunque la app no estuviera abierta.
     *
     * El sondeo de [observeDownload] muere con el proceso; esto es lo que garantiza que el
     * aviso pase a «lista para instalar» cuando la descarga la empezó el trabajo de fondo.
     */
    fun onDownloadCompleted(id: Long) {
        if (id != downloadId) return
        if (_state.value is UpdateState.ReadyToInstall) return
        val info = prefs.getString(KEY_DOWNLOAD_INFO, null)?.let(::infoFromJson) ?: return
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        when (downloadManager.statusOf(id)) {
            DownloadManager.STATUS_SUCCESSFUL -> finishDownload(info)
            DownloadManager.STATUS_FAILED -> failDownload()
            else -> Unit
        }
    }

    private fun DownloadManager.statusOf(id: Long): Int? {
        val cursor = query(DownloadManager.Query().setFilterById(id)) ?: return null
        return cursor.use { if (it.moveToFirst()) it.intOrNull(DownloadManager.COLUMN_STATUS) else null }
    }

    /** El entero de una columna, o nulo si esa columna no viene en el cursor. */
    private fun android.database.Cursor.intOrNull(column: String): Int? =
        getColumnIndex(column).takeIf { it >= 0 }?.let(::getInt)

    /** El largo de una columna, o cero si esa columna no viene en el cursor. */
    private fun android.database.Cursor.longOrZero(column: String): Long =
        getColumnIndex(column).takeIf { it >= 0 }?.let(::getLong) ?: 0L

    private fun observeDownload(downloadManager: DownloadManager, info: UpdateInfo) {
        downloadJob?.cancel()
        downloadJob = scope.launch {
            val ritmo = RitmoDeDescarga()
            var lastNotified = -1
            while (isActive) {
                val query = DownloadManager.Query().setFilterById(downloadId)
                val cursor = downloadManager.query(query) ?: break
                var shouldStop = false
                cursor.use {
                    if (!it.moveToFirst()) return@use
                    // `getColumnIndex` devuelve -1 si la columna no está, y leer por -1 no
                    // devuelve un cero: revienta. Aquí eso sería un cierre de la app en mitad
                    // de la descarga de una actualización, que es de lo peor que puede pasar.
                    val status = it.intOrNull(DownloadManager.COLUMN_STATUS) ?: return@use
                    when (status) {
                        DownloadManager.STATUS_SUCCESSFUL -> {
                            finishDownload(info)
                            shouldStop = true
                        }
                        DownloadManager.STATUS_FAILED -> {
                            failDownload()
                            shouldStop = true
                        }
                        else -> {
                            val downloaded = it.longOrZero(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                            val total = it.longOrZero(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)
                            /*
                             * Mientras el servidor no diga cuánto pesa, la columna vale -1.
                             * Dividiendo por eso salía un porcentaje inventado; ahora se
                             * manda [UNKNOWN_PROGRESS] y la barra se mueve sola en vez de
                             * quedarse clavada en un número que no significa nada.
                             */
                            val progress = if (total <= 0L) {
                                UpdateState.UNKNOWN_PROGRESS
                            } else {
                                ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                            }
                            val secondsLeft = ritmo.segundosQueFaltan(
                                ahora = System.currentTimeMillis(),
                                bajados = downloaded,
                                total = total
                            )
                            _state.value = UpdateState.Downloading(info, progress, secondsLeft)
                            // El aviso se reescribe solo cuando cambia el número: cien
                            // notificaciones por segundo tampoco las pinta el sistema.
                            if (progress != lastNotified) {
                                lastNotified = progress
                                notificationManager.showDownloadingNotification(info.versionName, progress, info.sizeMb)
                            }
                        }
                    }
                }
                if (shouldStop) return@launch
                delay(200)
            }
        }
    }

    private fun finishDownload(info: UpdateInfo) {
        prefs.edit { putLong(KEY_DOWNLOAD_ID, -1L) }
        downloadId = -1L
        if (isSignedByThisApp(apkFile())) {
            _state.value = UpdateState.ReadyToInstall(info, apkUri())
            refreshPendingApks()
            if (_settings.value.notifyWhenReady) {
                notificationManager.showReadyNotification(info.versionName)
            } else {
                notificationManager.dismissNotification()
            }
            /*
             * Si la pidió el usuario desde la app, en cuanto está descargada se abre el
             * instalador: quien pulsó descargar ya dijo que sí. Bajada sola en segundo plano,
             * no: abrir el instalador sin que nadie lo haya pedido es un susto.
             *
             * Si falta el permiso de instalar, tampoco: [installUpdate] llevaría a los
             * ajustes del sistema por su cuenta, y eso sí es un desvío que conviene que el
             * usuario empiece a propósito.
             */
            if (downloadStartedByUser && canInstallPackages()) installUpdate()
        } else {
            apkFile().delete()
            refreshPendingApks()
            forgetDownload()
            notificationManager.dismissNotification()
            _state.value = UpdateState.Error(Textos.get(R.string.update_err_unsigned))
        }
        downloadStartedByUser = false
    }

    private fun failDownload() {
        forgetDownload()
        notificationManager.dismissNotification()
        _state.value = UpdateState.Error(Textos.get(R.string.update_err_download))
    }

    private fun forgetDownload() {
        downloadId = -1L
        prefs.edit {
            remove(KEY_DOWNLOAD_ID)
            remove(KEY_DOWNLOAD_INFO)
        }
    }

    private fun appEnPrimerPlano(): Boolean =
        ProcessLifecycleOwner.get().lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)

    private fun isOnUnmeteredNetwork(): Boolean {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        return !connectivity.isActiveNetworkMetered
    }

    // ------------------------------------------------------------------ instalar

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

    /**
     * Se borra **todo** APK del directorio, no solo el del nombre fijo.
     *
     * Una descarga interrumpida deja el archivo con un sufijo del gestor del sistema
     * (`unistack-update-1.apk`), y a ese no lo borraba nadie: la limpieza se llevaba uno y
     * dejaba el resto ocupando sitio sin que nada lo dijera.
     */
    override fun clearDownload() {
        downloadJob?.cancel()
        // Una descarga a medias se cancela de verdad: si no, el gestor la termina por su
        // cuenta y el archivo reaparece con la tarjeta ya limpia.
        if (downloadId >= 0) {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            runCatching { downloadManager.remove(downloadId) }
        }
        apkFiles().forEach { it.delete() }
        refreshPendingApks()
        forgetDownload()
        notificationManager.dismissNotification()
        // Sin el archivo, lo que había listo vuelve a ser una versión disponible.
        val info = (_state.value as? UpdateState.ReadyToInstall)?.info
            ?: (_state.value as? UpdateState.Downloading)?.info
        _state.value = if (info != null) UpdateState.Available(info) else _state.value
    }

    override fun refreshPendingApks() {
        _pendingApks.value = apkFiles().size
    }

    private fun apkFiles(): List<File> =
        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            ?.listFiles()
            ?.filter { it.isFile && it.name.endsWith(".apk", ignoreCase = true) }
            .orEmpty()

    private fun apkFile(): File =
        File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), APK_FILE_NAME)

    /**
     * HTTPS protege el trayecto, pero no basta si se publica un APK ajeno por error o tras un
     * compromiso de la cuenta de releases. Antes de abrir el instalador, el archivo tiene que
     * llevar uno de los certificados con los que está firmada la aplicación instalada.
     */
    private fun isSignedByThisApp(apk: File): Boolean {
        if (!apk.isFile || apk.length() == 0L) return false
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            PackageManager.GET_SIGNING_CERTIFICATES
        } else {
            @Suppress("DEPRECATION") PackageManager.GET_SIGNATURES
        }
        val packageManager = context.packageManager
        val installed = packageManager.getPackageInfo(context.packageName, flags)
        val archive = packageManager.getPackageArchiveInfo(apk.absolutePath, flags) ?: return false
        val installedCertificates = certificatesOf(installed)
            ?.map { it.toSha256() }
            ?.toSet()
            .orEmpty()
        val archiveCertificates = certificatesOf(archive)
            ?.map { it.toSha256() }
            ?.toSet()
            .orEmpty()
        return installedCertificates.isNotEmpty() && archiveCertificates == installedCertificates
    }

    @Suppress("DEPRECATION")
    private fun certificatesOf(packageInfo: android.content.pm.PackageInfo): Array<android.content.pm.Signature>? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.signingInfo?.apkContentsSigners
        } else {
            packageInfo.signatures
        }

    private fun android.content.pm.Signature.toSha256(): String =
        MessageDigest.getInstance("SHA-256")
            .digest(toByteArray())
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }

    private fun apkUri(): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", apkFile())

    // ------------------------------------------------------------------ ajustes y memoria

    override fun updateSettings(transform: (UpdateSettings) -> UpdateSettings) {
        val before = _settings.value
        val after = transform(before)
        if (after == before) return
        prefs.edit {
            putBoolean(KEY_AUTO_DOWNLOAD_WIFI, after.autoDownloadOnWifi)
            putBoolean(KEY_NOTIFY_READY, after.notifyWhenReady)
            putString(KEY_CHECK_INTERVAL, after.checkInterval.name)
        }
        _settings.value = after
        if (after.checkInterval != before.checkInterval) {
            UpdateCheckWorker.schedule(context, after.checkInterval, replace = true)
        }
    }

    private fun readSettings(): UpdateSettings {
        val defaults = UpdateSettings()
        return UpdateSettings(
            autoDownloadOnWifi = prefs.getBoolean(KEY_AUTO_DOWNLOAD_WIFI, defaults.autoDownloadOnWifi),
            notifyWhenReady = prefs.getBoolean(KEY_NOTIFY_READY, defaults.notifyWhenReady),
            checkInterval = prefs.getString(KEY_CHECK_INTERVAL, null)
                ?.let { name -> CheckInterval.entries.firstOrNull { it.name == name } }
                ?: defaults.checkInterval
        )
    }

    override fun markSheetSeen(versionName: String) {
        prefs.edit { putString(KEY_SHEET_SEEN, versionName) }
        _sheetSeenVersion.value = versionName
    }

    /**
     * Apunta la versión que arranca ahora en el historial.
     *
     * La hora es la de la instalación según el sistema y no «ahora»: entre instalar y abrir
     * pueden pasar días, y el historial cuenta cuánto estuvo puesta cada una.
     */
    private fun recordInstalledVersion() {
        val instaladaEn = runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        }.getOrDefault(System.currentTimeMillis())
        val updated = HistorialDeVersiones.registrar(_installHistory.value, BuildConfig.VERSION_NAME, instaladaEn)
        if (updated == _installHistory.value) return
        prefs.edit { putString(KEY_INSTALL_HISTORY, historyToJson(updated).toString()) }
        _installHistory.value = updated
    }

    private fun readHistory(): List<InstalledVersion> {
        val raw = prefs.getString(KEY_INSTALL_HISTORY, null) ?: return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            (0 until array.length()).mapNotNull { index ->
                val item = array.optJSONObject(index) ?: return@mapNotNull null
                val version = item.optString("v")
                if (version.isBlank()) null else InstalledVersion(version, item.optLong("at"))
            }
        }.getOrDefault(emptyList())
    }

    private fun historyToJson(history: List<InstalledVersion>): JSONArray =
        JSONArray().apply {
            history.forEach { put(JSONObject().put("v", it.versionName).put("at", it.installedAtMillis)) }
        }

    private fun UpdateInfo.toJson(): JSONObject = JSONObject()
        .put("versionName", versionName)
        .put("releaseNotes", releaseNotes)
        .put("releaseDate", releaseDate)
        .put("downloadUrl", downloadUrl)
        .put("sizeMb", sizeMb)

    private fun infoFromJson(raw: String): UpdateInfo? = runCatching {
        val json = JSONObject(raw)
        UpdateInfo(
            versionName = json.getString("versionName"),
            releaseNotes = json.optString("releaseNotes"),
            releaseDate = json.optString("releaseDate"),
            downloadUrl = json.getString("downloadUrl"),
            sizeMb = json.optDouble("sizeMb", 0.0)
        )
    }.getOrNull()

    // ------------------------------------------------------------------ banco de pruebas

    override fun simulate(state: UpdateState?) {
        downloadJob?.cancel()
        if (state == null) {
            _state.value = UpdateState.Idle
            notificationManager.dismissNotification()
            scope.launch { checkForUpdates() }
            return
        }
        _state.value = state
        when (state) {
            is UpdateState.Available -> {
                // Que la hoja vuelva a salir aunque esa versión fingida ya se hubiera visto.
                prefs.edit { remove(KEY_SHEET_SEEN) }
                _sheetSeenVersion.value = null
                notificationManager.showUpdateAvailableNotification()
            }
            is UpdateState.Downloading -> notificationManager.showDownloadingNotification(
                state.info.versionName, state.progress, state.info.sizeMb
            )
            is UpdateState.ReadyToInstall -> notificationManager.showReadyNotification(state.info.versionName)
            else -> notificationManager.dismissNotification()
        }
    }
}

/**
 * Lo que falta de descarga, al ritmo de los últimos segundos.
 *
 * Se mide contra una muestra de hace al menos un segundo y no contra la anterior (doscientos
 * milisegundos): a ese paso el ritmo saltaba con cada paquete y el número bailaba.
 */
internal class RitmoDeDescarga {
    private var conMuestra = false
    private var muestraEn = 0L
    private var muestraBytes = 0L
    private var bytesPorSegundo = 0.0

    fun segundosQueFaltan(ahora: Long, bajados: Long, total: Long): Int? {
        if (!conMuestra) {
            conMuestra = true
            muestraEn = ahora
            muestraBytes = bajados
            return null
        }
        val transcurrido = ahora - muestraEn
        if (transcurrido >= 1000L) {
            bytesPorSegundo = (bajados - muestraBytes) * 1000.0 / transcurrido
            muestraEn = ahora
            muestraBytes = bajados
        }
        if (total <= 0L || bytesPorSegundo <= 0.0) return null
        return ((total - bajados) / bytesPorSegundo).toInt().coerceAtLeast(0)
    }
}
