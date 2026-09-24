package com.unistack.app.feature_rooms.presentation

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.data.RoomFileStore
import com.unistack.app.feature_rooms.data.RoomStoredFile
import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.WorkRoom
import java.io.File
import java.text.DecimalFormat
import kotlin.math.ln
import kotlin.math.roundToInt

/*
 * Las notas de voz del chat, como WhatsApp (24 sep): se graban manteniendo el micrófono (a la
 * izquierda cancela, hacia arriba se queda grabando sola, y un toque también la deja sola) y se
 * escuchan en la burbuja con su onda de verdad, la bolita que avanza, saltar tocando la onda y la
 * velocidad 1× · 1,5× · 2×.
 */

fun mmss(seconds: Int): String = "%d:%02d".format(seconds / 60, seconds % 60)

/** Una onda de relleno para las notas grabadas antes de guardar la de verdad. */
fun fakeWave(id: String, n: Int = 40): List<Int> {
    var h = id.hashCode()
    return List(n) { i ->
        h = h * 1103515245 + 12345
        val v = (h ushr 8) % 100
        (18 + v * 0.7 * (0.55 + 0.45 * kotlin.math.sin(i / 3.0))).toInt().coerceIn(10, 100)
    }
}

/** Una sola nota suena a la vez; quien la escucha es la pantalla del chat. */
class VoicePlayer {
    var playingId by mutableStateOf<String?>(null); private set
    var paused by mutableStateOf(true); private set
    var position by mutableIntStateOf(0); private set
    var duration by mutableIntStateOf(1); private set
    var speed by mutableFloatStateOf(1f); private set
    private var mp: MediaPlayer? = null

    fun isOn(id: String) = playingId == id

    fun toggle(id: String, path: String) {
        val p = mp
        if (playingId == id && p != null) {
            if (p.isPlaying) { p.pause(); paused = true } else { p.start(); applySpeed(); paused = false }
        } else start(id, path, 0f)
    }

    fun seek(id: String, path: String, frac: Float) {
        val p = mp
        if (playingId == id && p != null) { p.seekTo((duration * frac.coerceIn(0f, 1f)).toInt()); position = p.currentPosition }
        else start(id, path, frac)
    }

    private fun start(id: String, path: String, frac: Float) {
        release()
        val p = runCatching { MediaPlayer().apply { setDataSource(path); prepare() } }.getOrNull() ?: return
        mp = p; playingId = id; duration = p.duration.coerceAtLeast(1)
        p.seekTo((duration * frac.coerceIn(0f, 1f)).toInt()); position = p.currentPosition
        p.setOnCompletionListener { release() }
        p.start(); applySpeed(); paused = false
    }

    fun cycleSpeed() { speed = when (speed) { 1f -> 1.5f; 1.5f -> 2f; else -> 1f }; applySpeed() }

    private fun applySpeed() {
        val p = mp ?: return
        runCatching {
            val was = p.isPlaying
            p.playbackParams = p.playbackParams.setSpeed(speed)
            if (!was) p.pause()
        }
    }

    fun tick() { mp?.let { if (it.isPlaying) position = it.currentPosition } }

    fun release() {
        runCatching { mp?.release() }
        mp = null; playingId = null; paused = true; position = 0
    }
}

enum class RecMode { IDLE, HOLD, LOCKED }

/** El grabador: tiempo sin contar las pausas y el nivel de la voz cada ~90 ms para la onda. */
class VoiceRecorder(private val context: Context, private val files: RoomFileStore) {
    class Result(val file: RoomStoredFile, val seconds: Int, val wave: List<Int>)

    var active by mutableStateOf(false); private set
    var paused by mutableStateOf(false); private set
    var elapsedMs by mutableLongStateOf(0L); private set
    val levels = mutableStateListOf<Int>()
    private var rec: MediaRecorder? = null
    private var name: String? = null
    private var file: File? = null
    private var startedAt = 0L
    private var banked = 0L

    fun start(): Boolean {
        if (active) return true
        val (n, f) = files.newFileFor("m4a")
        val r = runCatching {
            val m = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
            m.setAudioSource(MediaRecorder.AudioSource.MIC)
            m.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            m.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            m.setAudioEncodingBitRate(96_000)
            m.setAudioSamplingRate(44_100)
            m.setOutputFile(f.absolutePath)
            m.prepare(); m.start(); m
        }.getOrNull()
        if (r == null) { files.delete(n); return false }
        rec = r; name = n; file = f; levels.clear(); banked = 0; startedAt = SystemClock.elapsedRealtime(); elapsedMs = 0
        paused = false; active = true
        return true
    }

    fun sample() {
        val r = rec ?: return
        if (paused) return
        elapsedMs = banked + (SystemClock.elapsedRealtime() - startedAt)
        val amp = runCatching { r.maxAmplitude }.getOrDefault(0)
        levels.add(((ln(1.0 + amp) / ln(32768.0)) * 100).roundToInt().coerceIn(4, 100))
    }

    fun pause() {
        if (paused || rec == null) return
        runCatching { rec?.pause() }.onSuccess { banked += SystemClock.elapsedRealtime() - startedAt; paused = true }
    }

    fun resume() {
        if (!paused || rec == null) return
        runCatching { rec?.resume() }.onSuccess { startedAt = SystemClock.elapsedRealtime(); paused = false }
    }

    fun stop(label: String): Result? {
        val r = rec ?: return null
        if (!paused) elapsedMs = banked + (SystemClock.elapsedRealtime() - startedAt)
        val ok = runCatching { r.stop() }.isSuccess
        r.release(); rec = null; active = false; paused = false
        val n = name ?: return null
        val f = file ?: return null
        val secs = (elapsedMs / 1000).toInt()
        if (!ok || secs < 1) { files.delete(n); return null }
        return Result(RoomStoredFile(n, label, "audio/mp4", f.length()), secs, squeeze(levels.toList(), 40))
    }

    fun cancel() {
        rec?.let { r -> runCatching { r.stop() }; r.release() }
        rec = null; active = false; paused = false
        name?.let { files.delete(it) }
    }

    private fun squeeze(xs: List<Int>, n: Int): List<Int> {
        if (xs.isEmpty()) return emptyList()
        if (xs.size <= n) return xs
        return List(n) { i -> val a = i * xs.size / n; val b = ((i + 1) * xs.size / n).coerceAtLeast(a + 1); xs.subList(a, b).average().roundToInt() }
    }
}

fun speedLabel(s: Float): String = DecimalFormat("0.#").format(s) + "×"

/** La onda: barras de lo escuchado en `played`, el resto apagado y la bolita donde va. */
@Composable
fun WaveBar(wave: List<Int>, frac: Float, played: Color, rest: Color, knob: Color?, modifier: Modifier) {
    Canvas(modifier) {
        val n = wave.size.coerceAtLeast(1)
        val bw = 3.dp.toPx()
        val step = size.width / n
        val cut = (frac * n).toInt()
        wave.forEachIndexed { i, v ->
            val h = (size.height * 0.9f * v / 100f).coerceAtLeast(3.dp.toPx())
            drawRoundRect(if (i < cut) played else rest, Offset(i * step + (step - bw) / 2, (size.height - h) / 2), Size(bw, h), CornerRadius(bw / 2))
        }
        if (knob != null) drawCircle(knob, 6.dp.toPx(), Offset((frac * size.width).coerceIn(6.dp.toPx(), size.width - 6.dp.toPx()), size.height / 2))
    }
}

/** La nota en la burbuja (opción A · como WhatsApp). */
@Composable
fun VoiceBubble(room: WorkRoom, m: ChatMessage, mine: Boolean, fg: Color, bubble: Color, player: VoicePlayer, path: String?) {
    val cs = MaterialTheme.colorScheme
    val on = player.isOn(m.id)
    val playing = on && !player.paused
    val frac = if (on) player.position / player.duration.toFloat() else 0f
    val accent = if (mine) fg else cs.primary
    val wave = m.wave.ifEmpty { fakeWave(m.id) }
    Column(Modifier.widthIn(min = 236.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).cleanClickable { path?.let { player.toggle(m.id, it) } }, contentAlignment = Alignment.Center) {
                Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = fg, modifier = Modifier.size(30.dp))
            }
            WaveBar(wave, frac, accent, fg.copy(alpha = 0.35f), accent, Modifier.weight(1f).height(30.dp)
                .pointerInput(m.id, path) { detectTapGestures { o -> path?.let { player.seek(m.id, it, o.x / size.width) } } }
                .pointerInput(m.id, path) { detectHorizontalDragGestures { c, _ -> c.consume(); path?.let { player.seek(m.id, it, c.position.x / size.width) } } })
            if (on) Text(speedLabel(player.speed), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = if (mine) cs.primary else cs.onSurface, textAlign = TextAlign.Center,
                modifier = Modifier.width(44.dp).clip(CircleShape).background(if (mine) cs.onPrimary else fg.copy(alpha = 0.16f)).cleanClickable { player.cycleSpeed() }.padding(vertical = 4.dp))
            else Box(Modifier.size(40.dp)) {
                MemberFace(room, m.byId, 38.dp)
                Box(Modifier.align(Alignment.BottomEnd).offset(x = 3.dp, y = 3.dp).size(18.dp).clip(CircleShape).background(bubble), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Mic, null, tint = accent, modifier = Modifier.size(13.dp))
                }
            }
        }
        Text(mmss(if (on) player.position / 1000 else m.seconds), fontSize = 11.sp, color = fg.copy(alpha = 0.75f), modifier = Modifier.padding(start = 42.dp))
    }
}

/** Grabando con el dedo puesto: el punto rojo, el tiempo y «‹ Desliza para cancelar» que sigue al dedo. */
@Composable
fun HoldRecordingBar(recorder: VoiceRecorder, dragX: Float, cancelPx: Float, modifier: Modifier) {
    val cs = MaterialTheme.colorScheme
    Row(modifier.clip(CircleShape).background(cs.surfaceContainer).padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        RecDot(recorder.paused)
        Text(mmss((recorder.elapsedMs / 1000).toInt()), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
        val k = (-dragX / cancelPx).coerceIn(0f, 1f)
        Text("‹ " + stringResource(R.string.rooms_slide_cancel), fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1,
            modifier = Modifier.weight(1f).offset { androidx.compose.ui.unit.IntOffset((dragX * 0.5f).roundToInt(), 0) }.alpha(1f - k))
    }
}

/** El panel de cuando se queda grabando solo (como WhatsApp): tiempo, onda en vivo, borrar, pausa y enviar. */
@Composable
fun LockedRecordingPanel(recorder: VoiceRecorder, onDiscard: () -> Unit, onSend: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp, top = 6.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(mmss((recorder.elapsedMs / 1000).toInt()), fontSize = 24.sp, fontWeight = FontWeight.Medium, color = cs.onSurface)
            Box(Modifier.weight(1f))
            RecDot(recorder.paused)
            val tail = recorder.levels.takeLast(26)
            WaveBar(List(26 - tail.size) { 6 } + tail, 1f, cs.onSurfaceVariant, cs.onSurfaceVariant, null, Modifier.width(110.dp).height(26.dp))
        }
        Row(Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(mix(cs.error, 0.18f, cs.background)).cleanClickable(onClick = onDiscard), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.Delete, stringResource(R.string.rooms_delete), tint = cs.error, modifier = Modifier.size(24.dp))
            }
            Row(Modifier.weight(1f).height(52.dp).clip(CircleShape).background(cs.surfaceContainerHigh).cleanClickable { if (recorder.paused) recorder.resume() else recorder.pause() },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
                Icon(if (recorder.paused) Icons.Rounded.Mic else Icons.Rounded.Pause, null, tint = cs.onSurface, modifier = Modifier.size(22.dp))
                Text(stringResource(if (recorder.paused) R.string.rooms_resume else R.string.rooms_pause), fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
            }
            Box(Modifier.size(52.dp).clip(CircleShape).background(cs.primary).cleanClickable(onClick = onSend), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.rooms_send), tint = cs.onPrimary, modifier = Modifier.size(22.dp))
            }
        }
    }
}

@Composable
private fun RecDot(paused: Boolean) {
    val blink by rememberInfiniteTransition(label = "grabando").animateFloat(1f, 0.25f, infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "punto")
    Box(Modifier.size(10.dp).alpha(if (paused) 1f else blink).clip(CircleShape).background(if (paused) MaterialTheme.colorScheme.onSurfaceVariant else RoomTone.ROJO.color))
}
