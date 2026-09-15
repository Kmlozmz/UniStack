@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_tasks.presentation

import android.media.MediaPlayer
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments
import com.unistack.app.feature_tasks.domain.TaskAttachment
import java.io.File

/**
 * Lo que cuelga de una tarea, calcado de `NoteAttachmentStrip` pero tipado a [TaskAttachment].
 * Misma razón para el carrusel de fotos aparte: una foto recortada se sigue reconociendo, un
 * nombre de archivo a medias no dice nada.
 */
@Composable
fun TaskAttachmentStrip(
    attachments: List<TaskAttachment>,
    pathFor: (TaskAttachment) -> String,
    existsFor: (TaskAttachment) -> Boolean,
    onOpen: (TaskAttachment) -> Unit,
    onRemove: (TaskAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return

    val fotos = attachments.filter { it.kind == AttachmentKind.IMAGE && existsFor(it) }
    val resto = attachments - fotos.toSet()

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        when {
            fotos.size == 1 -> SingleTaskAttachment(fotos.first(), pathFor, existsFor, onOpen, onRemove)
            fotos.size > 1 -> TaskPhotoCarousel(
                fotos = fotos,
                pathFor = pathFor,
                onOpen = onOpen,
                onRemove = onRemove
            )
        }
        resto.forEach { adjunto ->
            SingleTaskAttachment(adjunto, pathFor, existsFor, onOpen, onRemove)
        }
    }
}

/**
 * El visor a pantalla completa: la foto o el vídeo, sin salir de la app.
 *
 * Fondo negro y nada más que lo que se mira. La foto se deja acercar con dos dedos y arrastrar
 * mientras esté acercada —una foto de un enunciado escrito a mano no se lee al tamaño de la
 * pantalla—, y vuelve sola a su sitio al soltarla en el mínimo. El vídeo usa el reproductor del
 * sistema con sus propios controles, que es lo que ya sabe hacer pausa, barra y volumen.
 */
@Composable
fun TaskAttachmentViewer(
    attachment: TaskAttachment,
    path: String,
    onOpenExternally: () -> Unit,
    onDismiss: () -> Unit
) {
    val esVideo = attachment.mimeType.startsWith("video/")
    var escala by remember { mutableFloatStateOf(1f) }
    var desplazamiento by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // design-tokens-ok: un visor de fotos se mira sobre negro, no sobre el tema.
                .background(Color.Black)
        ) {
            if (esVideo) {
                AndroidView(
                    factory = { contexto ->
                        VideoView(contexto).apply {
                            setVideoPath(path)
                            setMediaController(MediaController(contexto).also { it.setAnchorView(this) })
                            setOnPreparedListener { it.isLooping = false; start() }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().align(Alignment.Center)
                )
            } else {
                val gestos = rememberTransformableState { cambioEscala, cambioPan, _ ->
                    escala = (escala * cambioEscala).coerceIn(1f, 5f)
                    desplazamiento = if (escala <= 1f) Offset.Zero else desplazamiento + cambioPan
                }
                AsyncImage(
                    model = File(path),
                    contentDescription = attachment.displayName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = escala,
                            scaleY = escala,
                            translationX = desplazamiento.x,
                            translationY = desplazamiento.y
                        )
                        .transformable(gestos)
                )
            }

            // La barra flota encima y no ocupa sitio: lo que se mira manda.
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisorBoton(icon = Icons.Rounded.Close, description = stringResource(R.string.action_close), onClick = onDismiss)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = attachment.displayName,
                    modifier = Modifier.weight(1f),
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                VisorBoton(
                    icon = Icons.Rounded.OpenInNew,
                    description = stringResource(R.string.notes_attachment_open),
                    onClick = onOpenExternally
                )
            }
        }
    }
}

@Composable
private fun VisorBoton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        // design-tokens-ok: va sobre el negro del visor, no sobre una superficie del tema.
        color = Color.White.copy(alpha = 0.16f),
        contentColor = Color.White,
        modifier = Modifier.size(40.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun TaskPhotoCarousel(
    fotos: List<TaskAttachment>,
    pathFor: (TaskAttachment) -> String,
    onOpen: (TaskAttachment) -> Unit,
    onRemove: (TaskAttachment) -> Unit
) {
    val anchoItem = 232.dp
    val carousel = rememberCarouselState { fotos.size }
    HorizontalMultiBrowseCarousel(
        state = carousel,
        preferredItemWidth = anchoItem,
        itemSpacing = 8.dp,
        modifier = Modifier.fillMaxWidth().height(196.dp)
    ) { indice ->
        val foto = fotos[indice]
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .maskClip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .cleanClickable { onOpen(foto) }
        ) {
            AsyncImage(
                model = File(pathFor(foto)),
                contentDescription = foto.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(anchoItem)
                    .fillMaxHeight()
                    .align(Alignment.Center)
            )
            TaskAttachmentRemoveBadge(
                onRemove = { onRemove(foto) },
                modifier = Modifier.align(Alignment.TopEnd).padding(7.dp)
            )
        }
    }
}

@Composable
private fun SingleTaskAttachment(
    adjunto: TaskAttachment,
    pathFor: (TaskAttachment) -> String,
    existsFor: (TaskAttachment) -> Boolean,
    onOpen: (TaskAttachment) -> Unit,
    onRemove: (TaskAttachment) -> Unit
) {
    when {
        !existsFor(adjunto) -> MissingTaskAttachmentRow(adjunto) { onRemove(adjunto) }
        adjunto.kind == AttachmentKind.IMAGE -> TaskImageAttachment(
            attachment = adjunto,
            path = pathFor(adjunto),
            onOpen = { onOpen(adjunto) },
            onRemove = { onRemove(adjunto) }
        )
        adjunto.kind == AttachmentKind.AUDIO -> TaskAudioAttachment(
            attachment = adjunto,
            path = pathFor(adjunto),
            onRemove = { onRemove(adjunto) }
        )
        else -> TaskFileAttachment(
            attachment = adjunto,
            onOpen = { onOpen(adjunto) },
            onRemove = { onRemove(adjunto) }
        )
    }
}

@Composable
private fun TaskImageAttachment(
    attachment: TaskAttachment,
    path: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    // La foto entera abre. Antes sólo abría la pastilla de «Abrir» en una esquina: tocar una
    // foto para verla es el gesto que cualquiera prueba primero, y no pasaba nada.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .cleanClickable(shape = RoundedCornerShape(12.dp), onClick = onOpen)
    ) {
        AsyncImage(
            model = File(path),
            contentDescription = attachment.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        TaskAttachmentRemoveBadge(
            onRemove = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(7.dp)
        )
    }
}

@Composable
private fun TaskFileAttachment(
    attachment: TaskAttachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        onClick = onOpen,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    attachment.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    Attachments.formatSize(attachment.sizeBytes),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            TaskAttachmentRemoveBadge(onRemove = onRemove)
        }
    }
}

@Composable
private fun TaskAudioAttachment(
    attachment: TaskAttachment,
    path: String,
    onRemove: () -> Unit
) {
    var reproduciendo by remember { mutableStateOf(false) }
    val player = remember { MediaPlayer() }

    DisposableEffect(path) {
        onDispose {
            runCatching { player.release() }
        }
    }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                onClick = {
                    runCatching {
                        if (reproduciendo) {
                            player.pause()
                            reproduciendo = false
                        } else {
                            if (!player.isPlaying && player.currentPosition == 0) {
                                player.reset()
                                player.setDataSource(path)
                                player.prepare()
                                player.setOnCompletionListener { reproduciendo = false }
                            }
                            player.start()
                            reproduciendo = true
                        }
                    }.onFailure { reproduciendo = false }
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Icon(
                    if (reproduciendo) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                    contentDescription = if (reproduciendo) stringResource(R.string.notes_attachment_pause) else stringResource(R.string.notes_attachment_listen),
                    modifier = Modifier.padding(7.dp).size(20.dp)
                )
            }
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    attachment.displayName,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOfNotNull(
                        attachment.durationMillis?.let { Attachments.formatDuration(it) },
                        Attachments.formatSize(attachment.sizeBytes)
                    ).joinToString(" · "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            TaskAttachmentRemoveBadge(onRemove = onRemove)
        }
    }
}

@Composable
private fun MissingTaskAttachmentRow(attachment: TaskAttachment, onRemove: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        contentPadding = PaddingValues(horizontal = 13.dp, vertical = 11.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Rounded.BrokenImage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(11.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    attachment.displayName,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    stringResource(R.string.notes_attachment_missing_phone),
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            TaskAttachmentRemoveBadge(onRemove = onRemove)
        }
    }
}

@Composable
private fun TaskAttachmentRemoveBadge(onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onRemove,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Icon(
            Icons.Rounded.Close,
            contentDescription = stringResource(R.string.notes_attachment_remove),
            modifier = Modifier.padding(5.dp).size(15.dp)
        )
    }
}

/**
 * Miniatura cuadrada para usar como icono compacto (fila de tarea, chip de formulario).
 */
@Composable
fun TaskAttachmentThumb(attachment: TaskAttachment, path: String, modifier: Modifier = Modifier) {
    if (attachment.kind == AttachmentKind.IMAGE) {
        AsyncImage(
            model = File(path),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        )
    } else {
        Box(
            modifier = modifier
                .aspectRatio(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (attachment.kind == AttachmentKind.AUDIO) {
                    androidx.compose.material.icons.Icons.Rounded.PlayArrow
                } else {
                    Icons.Rounded.Description
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(15.dp)
            )
        }
    }
}
