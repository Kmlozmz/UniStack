package com.unistack.app.feature_updates.data

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.FileProvider
import androidx.core.content.edit
import com.unistack.app.BuildConfig
import com.unistack.app.feature_updates.domain.UpdateInfo
import com.unistack.app.feature_updates.domain.UpdateRepository
import com.unistack.app.feature_updates.domain.UpdateState
import com.unistack.app.feature_updates.domain.resolveUpdateState
import java.io.File
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
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
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.utils.Textos
import com.unistack.app.R

private const val APK_FILE_NAME = "unistack-update.apk"
private const val PREFS_NAME = "unistack_update_checker"
private const val KEY_LAST_CHECKED_AT = "last_checked_at"



/**
 * Cada cuánto se deja consultar por su cuenta.
 *
 * Eran doce horas, que con la comprobación atada al arranque del proceso significaba enterarse
 * al día siguiente. Consultar es una petición diminuta; lo que hay que evitar es repetirla en
 * cada vuelta a la app, no espaciarla medio día.
 */
private const val AUTO_CHECK_INTERVAL_MILLIS = 45 * 60 * 1000L

class GitHubReleaseUpdateRepository(
    private val context: Context
) : UpdateRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    override val state: StateFlow<UpdateState> = _state.asStateFlow()

    private val notificationManager = UpdateNotificationManager(context)
    private var downloadId: Long = -1L

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

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun checkForUpdates() {
        runCheck(notify = false)
    }

    override suspend fun checkForUpdatesIfDue() {
        val lastCheckedAt = prefs.getLong(KEY_LAST_CHECKED_AT, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheckedAt < AUTO_CHECK_INTERVAL_MILLIS) return
        // Esta sí: pasa por su cuenta y en segundo plano, así que es la única que tiene algo
        // que contar.
        if (runCheck(notify = true)) {
            // Solo una comprobación real cuenta como comprobación. Si GitHub o la red fallan,
            // WorkManager recibe el fallo y puede reintentar antes del siguiente ciclo.
            prefs.edit { putLong(KEY_LAST_CHECKED_AT, now) }
        } else {
            throw IOException(Textos.get(R.string.update_check_failed))
        }
    }

    private suspend fun runCheck(notify: Boolean): Boolean {
        _state.value = UpdateState.Checking
        return runCatching { fetchReleases() }
            .onSuccess { published ->
                _releases.value = published
                val info = published.firstOrNull()
                val resolved = resolveUpdateState(info, BuildConfig.VERSION_NAME)
                _state.value = resolved
                if (resolved is UpdateState.Available) {
                    if (notify) notificationManager.showUpdateAvailableNotification()
                } else {
                    notificationManager.dismissNotification()
                }
            }
            .onFailure { error ->
                _state.value = UpdateState.Error(error.message ?: Textos.get(R.string.update_check_failed))
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
        refreshPendingApks()
        _state.value = UpdateState.Downloading(info, 0)
        observeDownload(downloadManager, info)
    }

    /** El entero de una columna, o nulo si esa columna no viene en el cursor. */
    private fun android.database.Cursor.intOrNull(column: String): Int? =
        getColumnIndex(column).takeIf { it >= 0 }?.let(::getInt)

    /** El largo de una columna, o cero si esa columna no viene en el cursor. */
    private fun android.database.Cursor.longOrZero(column: String): Long =
        getColumnIndex(column).takeIf { it >= 0 }?.let(::getLong) ?: 0L

    private fun observeDownload(downloadManager: DownloadManager, info: UpdateInfo) {
        scope.launch {
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
                            if (isSignedByThisApp(apkFile())) {
                                _state.value = UpdateState.ReadyToInstall(info, apkUri())
                                refreshPendingApks()
                            } else {
                                apkFile().delete()
                                refreshPendingApks()
                                _state.value = UpdateState.Error(
                                    Textos.get(R.string.update_err_unsigned)
                                )
                            }
                            shouldStop = true
                            /*
                             * En cuanto está descargada se abre el instalador de Android.
                             * Antes había que volver a pulsar «Instalar» sobre una descarga
                             * que ya estaba lista: un paso que no decide nada, porque quien
                             * pulsó descargar ya dijo que sí.
                             *
                             * Si falta el permiso de instalar, no se lanza: [installUpdate]
                             * llevaría a los ajustes del sistema por su cuenta, y eso sí es
                             * un desvío que conviene que el usuario empiece a propósito.
                             */
                            if (_state.value is UpdateState.ReadyToInstall && canInstallPackages()) installUpdate()
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _state.value = UpdateState.Error(Textos.get(R.string.update_err_download))
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
                            _state.value = UpdateState.Downloading(info, progress)
                        }
                    }
                }
                if (shouldStop) return@launch
                delay(200)
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

    /**
     * Se borra **todo** APK del directorio, no solo el del nombre fijo.
     *
     * Una descarga interrumpida deja el archivo con un sufijo del gestor del sistema
     * (`unistack-update-1.apk`), y a ese no lo borraba nadie: la limpieza se llevaba uno y
     * dejaba el resto ocupando sitio sin que nada lo dijera.
     */
    override fun clearDownload() {
        apkFiles().forEach { it.delete() }
        refreshPendingApks()
        _state.value = UpdateState.Idle
    }

    override fun dismiss() {
        _state.value = UpdateState.Idle
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
}
