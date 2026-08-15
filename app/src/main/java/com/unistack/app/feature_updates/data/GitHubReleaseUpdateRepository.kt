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
import com.unistack.app.feature_updates.domain.ChannelAccess
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
private const val KEY_ACCESS_FINGERPRINTS = "access_fingerprints"
private const val KEY_ACCESS_CHANNELS = "access_channels"

/** La lista de códigos vive junto a los APK, en el repositorio público de publicaciones. */
private const val KEY_ACCESS_VERIFIED_AT = "access_verified_at"

/**
 * Cuánto puede vivir un acceso sin poder confirmarse contra la lista.
 *
 * Sin este tope, quedarse sin conexión conservaba el canal para siempre, y basta con cortarle
 * el paso a un dominio para no perderlo nunca: la revocación se esquivaba sola. Con él, un
 * corte normal no molesta a nadie y un bloqueo deliberado caduca.
 */
private const val ACCESS_GRACE_MILLIS = 7L * 24 * 60 * 60 * 1000

private const val ACCESS_LIST_PATH = "https://raw.githubusercontent.com/%s/main/canales.json"
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

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _channel = MutableStateFlow(readStoredChannel())
    override val channel: StateFlow<UpdateChannel> = _channel.asStateFlow()

    private fun readStoredChannel(): UpdateChannel {
        val stored = prefs.getString(KEY_CHANNEL, null) ?: return UpdateChannel.STABLE
        return runCatching { UpdateChannel.valueOf(stored) }.getOrDefault(UpdateChannel.STABLE)
    }

    // Un conjunto y no un nivel: se pueden tener varios codigos a la vez, y tener el de alpha
    // no da el de beta.
    private val _unlockedChannels = MutableStateFlow(readStoredChannels())
    override val unlockedChannels: StateFlow<Set<UpdateChannel>> = _unlockedChannels.asStateFlow()

    private fun storedFingerprints(): Set<String> =
        prefs.getStringSet(KEY_ACCESS_FINGERPRINTS, emptySet()).orEmpty()

    private fun readStoredChannels(): Set<UpdateChannel> {
        val stored = prefs.getString(KEY_ACCESS_CHANNELS, null) ?: return setOf(UpdateChannel.STABLE)
        val channels = stored.split(",")
            .mapNotNull { name -> runCatching { UpdateChannel.valueOf(name) }.getOrNull() }
        return channels.toSet() + UpdateChannel.STABLE
    }

    override fun setChannel(channel: UpdateChannel) {
        // Solo lo concedido: el selector ya no ofrece lo demas, pero esto es lo que lo impide
        // si el estado llegara por otro camino.
        val allowed = if (channel in _unlockedChannels.value) channel else UpdateChannel.STABLE
        prefs.edit { putString(KEY_CHANNEL, allowed.name) }
        _channel.value = allowed
    }

    override suspend fun redeemAccessCode(code: String, channel: UpdateChannel): UpdateChannel? =
        withContext(Dispatchers.IO) {
        val fingerprint = ChannelAccess.fingerprint(code)
        // El código tiene que ser el del canal que se está abriendo. Antes valía cualquiera de
        // la lista: metías el de alpha en la casilla de beta y te abría alpha, que no es lo que
        // pediste ni lo que esperas al pulsar «Beta».
        val granted = ChannelAccess.grantFor(fingerprint, channel, fetchAccessEntries())
            ?: return@withContext null
        prefs.edit {
            putStringSet(KEY_ACCESS_FINGERPRINTS, storedFingerprints() + fingerprint)
            putLong(KEY_ACCESS_VERIFIED_AT, System.currentTimeMillis())
        }
        applyAccess(_unlockedChannels.value + granted)
        granted
    }

    /**
     * Vuelve a contrastar el código guardado con la lista publicada.
     *
     * Es lo que hace que retirar una línea de la lista revoque de verdad: sin esto, quien
     * canjeó una vez se quedaba con el canal abierto para siempre. Si la lista no se puede
     * consultar no se toca nada, porque quedarse sin cobertura no es lo mismo que perder el
     * permiso.
     */
    private suspend fun refreshAccess() {
        val stored = storedFingerprints()
        if (stored.isEmpty()) return
        val entries = runCatching { fetchAccessEntries() }.getOrNull()

        if (entries == null) {
            // No se pudo consultar. Un corte puntual no revoca nada, pero el permiso no puede
            // sobrevivir indefinidamente sin confirmarse: si no, mantenerlo es tan facil como
            // impedir que la app llegue a la lista.
            val verifiedAt = prefs.getLong(KEY_ACCESS_VERIFIED_AT, 0L)
            if (verifiedAt > 0L && System.currentTimeMillis() - verifiedAt > ACCESS_GRACE_MILLIS) {
                prefs.edit { remove(KEY_ACCESS_FINGERPRINTS) }
                applyAccess(setOf(UpdateChannel.STABLE))
            }
            return
        }

        // Se conservan solo las huellas que siguen en la lista: asi una revocacion se lleva su
        // canal y deja intactos los demas codigos que tenga esa persona.
        val alive = stored.filter { ChannelAccess.channelFor(it, entries) != null }.toSet()
        val granted = alive.mapNotNull { ChannelAccess.channelFor(it, entries) }.toSet()
        prefs.edit {
            putStringSet(KEY_ACCESS_FINGERPRINTS, alive)
            putLong(KEY_ACCESS_VERIFIED_AT, System.currentTimeMillis())
        }
        applyAccess(granted + UpdateChannel.STABLE)
    }

    private fun applyAccess(granted: Set<UpdateChannel>) {
        val channels = granted + UpdateChannel.STABLE
        if (channels == _unlockedChannels.value) return
        prefs.edit { putString(KEY_ACCESS_CHANNELS, channels.joinToString(",") { it.name }) }
        _unlockedChannels.value = channels
        if (_channel.value !in channels) setChannel(UpdateChannel.STABLE)
    }

    private fun fetchAccessEntries(): List<ChannelAccess.AccessEntry> {
        val url = URL(String.format(ACCESS_LIST_PATH, BuildConfig.GITHUB_REPO))
        val connection = url.openConnection() as HttpURLConnection
        connection.connectTimeout = 10_000
        connection.readTimeout = 10_000
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("No se pudo consultar la lista de códigos (${connection.responseCode}).")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val codes = JSONObject(body).optJSONArray("codigos") ?: return emptyList()
            return (0 until codes.length()).mapNotNull { index ->
                val entry = codes.optJSONObject(index) ?: return@mapNotNull null
                val channel = runCatching {
                    UpdateChannel.valueOf(entry.optString("canal").uppercase())
                }.getOrNull() ?: return@mapNotNull null
                val fingerprint = entry.optString("huella").takeIf { it.isNotBlank() }
                    ?: return@mapNotNull null
                ChannelAccess.AccessEntry(channel, fingerprint)
            }
        } finally {
            connection.disconnect()
        }
    }

    /**
     * Comprobación pedida por el usuario: **no notifica**.
     *
     * La notificación existe para enterarte de algo que no estabas mirando. Al pulsar
     * «Verificar» o cambiar de canal ya estás delante de la pantalla, y aun así te llegaba el
     * aviso a la barra de estado contando lo que tenías en la mano.
     */
    override suspend fun checkForUpdates() = runCheck(notify = false)

    override suspend fun checkForUpdatesIfDue() {
        val lastCheckedAt = prefs.getLong(KEY_LAST_CHECKED_AT, 0L)
        val now = System.currentTimeMillis()
        if (now - lastCheckedAt < AUTO_CHECK_INTERVAL_MILLIS) return
        prefs.edit { putLong(KEY_LAST_CHECKED_AT, now) }
        // Esta sí: pasa por su cuenta y en segundo plano, así que es la única que tiene algo
        // que contar.
        runCheck(notify = true)
    }

    private suspend fun runCheck(notify: Boolean) {
        _state.value = UpdateState.Checking
        refreshAccess()
        runCatching { fetchLatestRelease() }
            .onSuccess { info ->
                if (info != null && ReleaseVersion.isNewer(info.versionName, BuildConfig.VERSION_NAME)) {
                    _state.value = UpdateState.Available(info)
                    if (notify) notificationManager.showUpdateAvailableNotification()
                } else {
                    _state.value = UpdateState.UpToDate
                    notificationManager.dismissNotification()
                }
            }
            .onFailure { error ->
                _state.value = UpdateState.Error(error.message ?: "No se pudo verificar actualizaciones.")
            }
    }

    /**
     * Devuelve la última versión publicada, o lanza explicando por qué no pudo saberlo.
     *
     * Se consulta la lista y no `/releases/latest`, que **excluye los preestrenos**: con una
     * alpha publicada, ese endpoint devolvía 404 y la app decía que no había ninguna
     * publicación. Se toma la primera que no sea borrador, que es la más reciente.
     */
    private suspend fun fetchLatestRelease(): UpdateInfo? = withContext(Dispatchers.IO) {
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
            // La más reciente que acepte el canal, no la más reciente a secas: con el canal
            // estable, una alpha publicada después de la definitiva no es una actualización.
            val current = _channel.value
            val published = (0 until releases.length())
                .mapNotNull(releases::optJSONObject)
                .filterNot { it.optBoolean("draft", false) }
                .firstOrNull { current.accepts(it.optString("tag_name").removePrefix("v").removePrefix("V")) }
                // Sin publicaciones para tu canal no hay error que dar: no tienes nada que
                // instalar, que es justo lo que significa estar al día. Decir «no se encontró
                // ninguna publicación» sonaba a avería, y con solo alphas publicadas era lo
                // que veía todo el que estuviera en el canal estable, o sea todo el mundo.
                ?: return@withContext null
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
                            _state.value = UpdateState.ReadyToInstall(info, apkUri())
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
                            if (canInstallPackages()) installUpdate()
                        }
                        DownloadManager.STATUS_FAILED -> {
                            _state.value = UpdateState.Error("La descarga falló. Intenta de nuevo.")
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
