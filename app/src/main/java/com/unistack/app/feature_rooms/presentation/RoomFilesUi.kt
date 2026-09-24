package com.unistack.app.feature_rooms.presentation

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.graphics.drawable.toBitmap
import androidx.core.content.FileProvider
import com.unistack.app.R
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_rooms.data.RoomStoredFile

fun Context.roomToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

/** Lo que hay copiado, como texto (null si no hay nada). */
fun Context.pastedText(): String? {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return null
    return cm.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(this)?.toString()
}

fun Context.copyText(label: String, text: String) {
    val cm = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    cm.setPrimaryClip(ClipData.newPlainText(label, text))
}

/** Compartir texto: a una app concreta si está instalada; si no, el selector de siempre. */
fun Context.shareText(text: String, pkg: String? = null) {
    val send = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, text); if (pkg != null) setPackage(pkg) }
    val ok = runCatching { startActivity(if (pkg == null) Intent.createChooser(send, null) else send) }.isSuccess
    if (!ok) runCatching { startActivity(Intent.createChooser(send.setPackage(null), null)) }
}

/** Una app a la que se comparte directo, con el icono que tiene en el teléfono. */
class ShareApp(val label: String, val icon: ImageBitmap, val open: () -> Unit)

/**
 * WhatsApp, Telegram y la app de correo, sólo las que estén instaladas y con su icono de verdad
 * (sacado del sistema, no dibujado): así no hay logos inventados ni atajos que no llevan a nada.
 */
@Composable
fun rememberShareApps(subject: String, text: String): List<ShareApp> {
    val context = LocalContext.current
    val mail = stringResource(R.string.rooms_email)
    return remember(subject, text) {
        val pm = context.packageManager
        fun icon(pkg: String) = runCatching { pm.getApplicationIcon(pkg).toBitmap(144, 144).asImageBitmap() }.getOrNull()
        fun first(vararg pkgs: String) = pkgs.firstNotNullOfOrNull { p -> icon(p)?.let { p to it } }
        val mailto = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
        val mailPkg = runCatching {
            pm.resolveActivity(mailto, 0)?.activityInfo?.packageName?.takeIf { it != "android" }
                ?: pm.queryIntentActivities(mailto, 0).firstOrNull()?.activityInfo?.packageName
        }.getOrNull()
        buildList {
            first("com.whatsapp", "com.whatsapp.w4b")?.let { (p, i) -> add(ShareApp("WhatsApp", i) { context.shareText(text, p) }) }
            first("org.telegram.messenger", "org.telegram.messenger.web", "org.thunderdog.challegram")?.let { (p, i) -> add(ShareApp("Telegram", i) { context.shareText(text, p) }) }
            mailPkg?.let { icon(it) }?.let { i -> add(ShareApp(mail, i) { context.emailText(subject, text) }) }
        }
    }
}

fun Context.emailText(subject: String, text: String) {
    val mail = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:")).apply { putExtra(Intent.EXTRA_SUBJECT, subject); putExtra(Intent.EXTRA_TEXT, text) }
    runCatching { startActivity(mail) }.onFailure { shareText(text) }
}

fun Context.openUrl(url: String) {
    val u = if (url.startsWith("http")) url else "https://$url"
    runCatching { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(u)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure { roomToast(Textos.get(R.string.notes_error_no_app_to_open)) }
}

/** Abrir un archivo de la sala fuera, con quien sepa. */
fun Context.openRoomFile(vm: RoomsViewModel, stored: String, mime: String?, name: String) {
    val uri = vm.files.shareUri(stored) ?: return roomToast(Textos.get(R.string.notes_error_no_app_to_open))
    val ver = Intent(Intent.ACTION_VIEW).apply { setDataAndType(uri, mime ?: "*/*"); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) }
    runCatching { startActivity(Intent.createChooser(ver, name).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure { roomToast(Textos.get(R.string.notes_error_no_app_to_open)) }
}

/** Mandar un archivo de la sala a otra app (o a una concreta). */
fun Context.shareRoomFile(vm: RoomsViewModel, stored: String, mime: String?, pkg: String? = null) {
    val uri = vm.files.shareUri(stored) ?: return
    val send = Intent(Intent.ACTION_SEND).apply {
        type = mime ?: "*/*"; putExtra(Intent.EXTRA_STREAM, uri); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); if (pkg != null) setPackage(pkg)
    }
    val ok = runCatching { startActivity(if (pkg == null) Intent.createChooser(send, null) else send) }.isSuccess
    if (!ok) runCatching { startActivity(Intent.createChooser(send.setPackage(null), null)) }
}

/**
 * Los selectores del teléfono para una sala: foto, video, audio, cualquier archivo o la cámara.
 * Lo elegido se copia dentro antes de devolverlo, como los adjuntos de las tareas.
 */
class RoomPicker internal constructor(
    val pickImage: () -> Unit,
    val pickVideo: () -> Unit,
    val pickFile: (Array<String>) -> Unit,
    val takePhoto: () -> Unit
)

@Composable
fun rememberRoomPicker(vm: RoomsViewModel, onPicked: (RoomStoredFile) -> Unit): RoomPicker {
    val context = LocalContext.current
    var pendingPhoto by remember { mutableStateOf<String?>(null) }
    val tooBig = Textos.get(R.string.rooms_file_error)
    val take: (Uri?) -> Unit = { uri ->
        if (uri != null) {
            val s = vm.files.import(uri)
            if (s == null) context.roomToast(tooBig) else onPicked(s)
        }
    }
    val image = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia(), take)
    val file = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument(), take)
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        val stored = pendingPhoto
        pendingPhoto = null
        if (stored != null) {
            val f = vm.files.file(stored)
            if (ok && f.exists() && f.length() > 0) onPicked(RoomStoredFile(stored, Textos.get(R.string.rooms_photo_of_today), "image/jpeg", f.length()))
            else vm.files.delete(stored)
        }
    }
    return remember(vm) {
        RoomPicker(
            pickImage = { image.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            pickVideo = { image.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)) },
            pickFile = { types -> file.launch(types) },
            takePhoto = {
                val (name, f) = vm.files.newFileFor("jpg")
                runCatching { f.createNewFile() }
                val uri = runCatching { FileProvider.getUriForFile(context, context.packageName + ".provider", f) }.getOrNull()
                if (uri == null) vm.files.delete(name) else { pendingPhoto = name; camera.launch(uri) }
            }
        )
    }
}

val DOC_TYPES = arrayOf(
    "application/pdf", "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.google-apps.document", "text/plain", "text/markdown"
)
val SLIDE_TYPES = arrayOf(
    "application/vnd.ms-powerpoint", "application/vnd.openxmlformats-officedocument.presentationml.presentation",
    "application/vnd.google-apps.presentation", "application/pdf"
)
val AUDIO_TYPES = arrayOf("audio/*")
val ANY_TYPES = arrayOf("*/*")

/** «Word · 1,2 MB» para la línea de apoyo de un archivo. */
fun fileSubtitle(mime: String, bytes: Long): String {
    val kind = when {
        mime.contains("pdf") -> "PDF"
        mime.contains("word") || mime.contains("msword") -> "Word"
        mime.contains("presentation") || mime.contains("powerpoint") -> "PowerPoint"
        mime.contains("sheet") || mime.contains("excel") -> "Excel"
        mime.startsWith("image/") -> Textos.get(R.string.rooms_mt_photo)
        mime.startsWith("video/") -> Textos.get(R.string.rooms_mt_video)
        mime.startsWith("audio/") -> Textos.get(R.string.rooms_mt_audio)
        mime.startsWith("text/") -> Textos.get(R.string.rooms_fmt_txt)
        else -> Textos.get(R.string.rooms_mt_doc)
    }
    val size = if (bytes >= 1024 * 1024) String.format(java.util.Locale.getDefault(), "%.1f MB", bytes / 1048576.0) else "${(bytes / 1024).coerceAtLeast(1)} KB"
    return "$kind · $size"
}

fun hostOf(url: String): String = runCatching { Uri.parse(if (url.startsWith("http")) url else "https://$url").host.orEmpty().removePrefix("www.") }
    .getOrDefault("").ifBlank { url }
