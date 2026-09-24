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
import androidx.compose.material.icons.outlined.Delete
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
        levels.add((kotlin.math.sqrt(amp / 32767.0) * 130).roundToInt().coerceIn(4, 100))
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

/**
 * La nota en la burbuja, como la captura de WhatsApp (24 sep): el triángulo sin fondo, la bolita
 * del acento sobre la onda, la cara grande a la derecha con el micro montado abajo a la izquierda
 * (o la velocidad mientras suena) y debajo la duración y la hora.
 */
@Composable
fun VoiceBubble(room: WorkRoom, m: ChatMessage, mine: Boolean, fg: Color, player: VoicePlayer, path: String?, time: String) {
    val cs = MaterialTheme.colorScheme
    val on = player.isOn(m.id)
    val playing = on && !player.paused
    val frac = if (on) player.position / player.duration.toFloat() else 0f
    val accent = if (mine) fg else cs.primary
    val wave = m.wave.ifEmpty { fakeWave(m.id) }
    Row(Modifier.widthIn(min = 250.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(36.dp).clip(CircleShape).cleanClickable { path?.let { player.toggle(m.id, it) } }, contentAlignment = Alignment.Center) {
                    Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = fg.copy(alpha = 0.75f), modifier = Modifier.size(34.dp))
                }
                WaveBar(wave, frac, accent, fg.copy(alpha = 0.32f), accent, Modifier.weight(1f).height(30.dp)
                    .pointerInput(m.id, path) { detectTapGestures { o -> path?.let { player.seek(m.id, it, o.x / size.width) } } }
                    .pointerInput(m.id, path) { detectHorizontalDragGestures { c, _ -> c.consume(); path?.let { player.seek(m.id, it, c.position.x / size.width) } } })
            }
            Row(Modifier.padding(start = 42.dp, top = 2.dp)) {
                Text(mmss(if (on) player.position / 1000 else m.seconds), fontSize = 12.sp, color = fg.copy(alpha = 0.6f), modifier = Modifier.weight(1f))
                Text(time, fontSize = 11.sp, color = fg.copy(alpha = 0.6f))
            }
        }
        if (on) Text(speedLabel(player.speed), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (mine) cs.primary else cs.onSurface, textAlign = TextAlign.Center,
            modifier = Modifier.width(48.dp).clip(CircleShape).background(if (mine) cs.onPrimary else fg.copy(alpha = 0.16f)).cleanClickable { player.cycleSpeed() }.padding(vertical = 5.dp))
        else Box(Modifier.size(50.dp)) {
            MemberFace(room, m.byId, 50.dp)
            Icon(Icons.Rounded.Mic, null, tint = accent, modifier = Modifier.align(Alignment.BottomStart).offset(x = (-8).dp, y = 2.dp).size(20.dp))
        }
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

/**
 * El panel de cuando se queda grabando sola, igual que el de WhatsApp (captura del 24 sep): una
 * tarjeta con las esquinas de arriba redondas; el tiempo grande a la izquierda y la onda en vivo a
 * la derecha (puntitos en silencio, barras al hablar, que entran por la derecha); abajo la papelera
 * roja, «Pausa» ancho con las barras huecas y enviar (gris hasta el primer segundo).
 */
@Composable
fun LockedRecordingPanel(recorder: VoiceRecorder, onDiscard: () -> Unit, onSend: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val secs = (recorder.elapsedMs / 1000).toInt()
    val ready = secs >= 1
    Column(Modifier.fillMaxWidth().clip(androidx.compose.foundation.shape.RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)).background(cs.surfaceContainerLow)
        .padding(start = 16.dp, end = 16.dp, top = 26.dp, bottom = 16.dp)) {
        Row(Modifier.fillMaxWidth().padding(start = 8.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(mmss(secs), fontSize = 28.sp, fontWeight = FontWeight.Normal, color = cs.onSurface, modifier = Modifier.weight(1f))
            LiveWave(recorder.levels, cs.onSurfaceVariant, Modifier.width(150.dp).height(28.dp))
        }
        Row(Modifier.padding(top = 26.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(56.dp).clip(CircleShape).background(mix(cs.error, 0.16f, cs.surfaceContainerLow)).cleanClickable(onClick = onDiscard), contentAlignment = Alignment.Center) {
                Icon(Icons.Outlined.Delete, stringResource(R.string.rooms_delete), tint = cs.error, modifier = Modifier.size(26.dp))
            }
            Row(Modifier.weight(1f).height(56.dp).clip(CircleShape).background(cs.surfaceContainerHighest).cleanClickable { if (recorder.paused) recorder.resume() else recorder.pause() },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally)) {
                if (recorder.paused) Icon(Icons.Rounded.Mic, null, tint = RoomTone.ROJO.color, modifier = Modifier.size(22.dp))
                else HollowPause(cs.onSurface)
                Text(stringResource(if (recorder.paused) R.string.rooms_resume else R.string.rooms_pause), fontSize = 16.sp, fontWeight = FontWeight.Medium, color = cs.onSurface)
            }
            val sendBg by androidx.compose.animation.animateColorAsState(if (ready) cs.primary else mix(cs.onSurface, 0.72f, cs.surfaceContainerLow), label = "enviar")
            Box(Modifier.size(56.dp).clip(CircleShape).background(sendBg).cleanClickable(enabled = ready, onClick = onSend), contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Rounded.Send, stringResource(R.string.rooms_send), tint = if (ready) cs.onPrimary else cs.surfaceContainerLow, modifier = Modifier.size(24.dp))
            }
        }
    }
}

/** Las dos barras huecas de la pausa de WhatsApp. */
@Composable
private fun HollowPause(color: Color) {
    Canvas(Modifier.size(width = 20.dp, height = 22.dp)) {
        val w = 6.dp.toPx(); val sw = 2.dp.toPx(); val gap = 4.dp.toPx()
        val st = androidx.compose.ui.graphics.drawscope.Stroke(width = sw)
        drawRoundRect(color, Offset(sw / 2, sw / 2), Size(w, size.height - sw), CornerRadius(w / 3), style = st)
        drawRoundRect(color, Offset(w + gap + sw / 2, sw / 2), Size(w, size.height - sw), CornerRadius(w / 3), style = st)
    }
}

/** La onda en vivo: lo último que se grabó entra por la derecha; el silencio son puntitos. */
@Composable
private fun LiveWave(levels: List<Int>, color: Color, modifier: Modifier) {
    Canvas(modifier) {
        val bw = 3.dp.toPx(); val step = 5.dp.toPx()
        val n = (size.width / step).toInt()
        val tail = levels.takeLast(n)
        tail.forEachIndexed { k, v ->
            val x = size.width - (tail.size - k) * step
            val h = if (v < 22) bw else (size.height * v / 100f).coerceAtLeast(bw)
            drawRoundRect(color, Offset(x, (size.height - h) / 2), Size(bw, h), CornerRadius(bw / 2))
        }
    }
}

@Composable
private fun RecDot(paused: Boolean) {
    val blink by rememberInfiniteTransition(label = "grabando").animateFloat(1f, 0.25f, infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "punto")
    Box(Modifier.size(10.dp).alpha(if (paused) 1f else blink).clip(CircleShape).background(if (paused) MaterialTheme.colorScheme.onSurfaceVariant else RoomTone.ROJO.color))
}
