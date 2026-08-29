@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_notes.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Context
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.unistack.app.feature_notes.domain.Attachments
import kotlinx.coroutines.delay
import java.io.File

/** Diez minutos. Más que eso deja de ser un apunte y empieza a ser una clase entera. */
private const val MAX_MILLIS = 10 * 60 * 1000

/**
 * Grabar dentro de una nota.
 *
 * El permiso se pide aquí y no al instalar: quien nunca grabe nada no tiene por qué conceder el
 * micrófono. Si lo niega, la hoja lo dice y se cierra sin dejar nada a medias.
 *
 * El archivo se escribe directamente dentro de UniStack, así que no hay copia posterior ni un
 * momento en el que la grabación viva fuera. Si se sale sin parar, se borra: media grabación no
 * le sirve a nadie y ocuparía igual.
 */
@Composable
fun NoteRecorderSheet(
    createFile: (String) -> Pair<String, File>,
    onDiscard: (String) -> Unit,
    onSaved: (storedName: String, durationMillis: Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var permitido by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var denegado by remember { mutableStateOf(false) }
    var grabando by remember { mutableStateOf(false) }
    var transcurrido by remember { mutableStateOf(0L) }
    var recorder by remember { mutableStateOf<MediaRecorder?>(null) }
    var archivo by remember { mutableStateOf<Pair<String, File>?>(null) }

    val pedirPermiso = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        permitido = concedido
        denegado = !concedido
    }

    LaunchedEffect(Unit) {
        if (!permitido) pedirPermiso.launch(Manifest.permission.RECORD_AUDIO)
    }

    // El reloj de la grabación. Se para solo al llegar al tope para que nadie se deje el
    // micrófono abierto sin darse cuenta.
    LaunchedEffect(grabando) {
        if (!grabando) return@LaunchedEffect
        val comienzo = SystemClock.elapsedRealtime()
        while (grabando && transcurrido < MAX_MILLIS) {
            transcurrido = SystemClock.elapsedRealtime() - comienzo
            delay(100)
        }
    }

    val detener: (Boolean) -> Unit = { guardar ->
        val actual = recorder
        val destino = archivo
        recorder = null
        archivo = null
        grabando = false
        val duracion = transcurrido
        val correcta = runCatching {
            actual?.stop()
            actual?.release()
        }.isSuccess
        if (destino != null) {
            if (guardar && correcta && destino.second.length() > 0) {
                onSaved(destino.first, duracion)
            } else {
                onDiscard(destino.first)
            }
        }
    }

    /*
     * Salir de la pantalla suelta el micrófono pase lo que pase.
     *
     * Sin esto, cerrar la hoja mientras graba deja el `MediaRecorder` vivo: el micrófono se
     * queda tomado para toda la app y la siguiente grabación falla sin decir por qué.
     */
    DisposableEffect(Unit) {
        onDispose {
            val actual = recorder
            val destino = archivo
            if (actual != null) {
                runCatching { actual.stop() }
                runCatching { actual.release() }
            }
            if (destino != null) onDiscard(destino.first)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            // Cerrar la hoja mientras graba guarda lo grabado: es lo que se estaba haciendo.
            if (grabando) detener(true)
            onDismiss()
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                if (grabando) "Grabando" else "Grabar una nota de voz",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )

            if (denegado && !permitido) {
                Text(
                    "Sin permiso de micrófono no se puede grabar. Se concede desde los ajustes " +
                        "del sistema, en los permisos de UniStack.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    Attachments.formatDuration(transcurrido),
                    color = if (grabando) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    style = MaterialTheme.typography.displaySmall,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                RecordButton(
                    grabando = grabando,
                    enabled = permitido,
                    onClick = {
                        if (grabando) {
                            detener(true)
                            onDismiss()
                        } else {
                            val nuevo = createFile("m4a")
                            val creado = crearRecorder(context, nuevo.second)
                            if (creado != null) {
                                recorder = creado
                                archivo = nuevo
                                transcurrido = 0L
                                grabando = true
                            } else {
                                onDiscard(nuevo.first)
                                denegado = true
                            }
                        }
                    }
                )

                Text(
                    if (grabando) {
                        "Toca otra vez para parar. Se guarda en la nota."
                    } else {
                        "Se guarda dentro de UniStack, con la nota. Máximo 10 minutos."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RecordButton(grabando: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val transicion = rememberInfiniteTransition(label = "latido")
    val latido by transicion.animateFloat(
        initialValue = 1f,
        targetValue = if (grabando) 1.08f else 1f,
        animationSpec = infiniteRepeatable(tween(760), RepeatMode.Reverse),
        label = "escala"
    )
    val escala by animateFloatAsState(if (grabando) latido else 1f, label = "pulso")

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (grabando) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            MaterialTheme.colorScheme.primaryContainer
        },
        contentColor = if (grabando) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onPrimaryContainer
        },
        modifier = Modifier.size(84.dp).scale(escala)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                if (grabando) Icons.Rounded.Stop else Icons.Rounded.Mic,
                contentDescription = if (grabando) "Parar" else "Empezar a grabar",
                modifier = Modifier.size(34.dp)
            )
        }
    }
}

/**
 * El grabador, con el formato que menos ocupa sin sonar mal.
 *
 * AAC a 64 kbps dentro de un MPEG-4: diez minutos de voz caben en cinco megas, y cualquier
 * teléfono y cualquier ordenador lo abren sin instalar nada.
 */
private fun crearRecorder(context: Context, destino: File): MediaRecorder? = runCatching {
    val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        MediaRecorder(context)
    } else {
        @Suppress("DEPRECATION")
        MediaRecorder()
    }
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    recorder.setAudioEncodingBitRate(64_000)
    recorder.setAudioSamplingRate(44_100)
    recorder.setMaxDuration(MAX_MILLIS)
    recorder.setOutputFile(destino.absolutePath)
    recorder.prepare()
    recorder.start()
    recorder
}.getOrNull()
