@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_notes.presentation

import android.media.MediaPlayer
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.NoteAttachment
import com.unistack.app.feature_notes.domain.Attachments
import java.io.File

/**
 * Lo que cuelga de una nota, debajo del texto.
 *
 * Las fotos van grandes y los archivos en fila: una foto de la pizarra se mira, y un PDF solo
 * hay que saber que está. Cada uno lleva su aspa para quitarlo, porque el gesto de adjuntar es
 * rápido y equivocarse también.
 */
@Composable
fun NoteAttachmentStrip(
    attachments: List<NoteAttachment>,
    pathFor: (NoteAttachment) -> String,
    existsFor: (NoteAttachment) -> Boolean,
    onOpen: (NoteAttachment) -> Unit,
    onRemove: (NoteAttachment) -> Unit,
    modifier: Modifier = Modifier
) {
    if (attachments.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        attachments.forEach { adjunto ->
            val presente = existsFor(adjunto)
            when {
                !presente -> MissingAttachmentRow(adjunto) { onRemove(adjunto) }
                adjunto.kind == AttachmentKind.IMAGE -> ImageAttachment(
                    attachment = adjunto,
                    path = pathFor(adjunto),
                    onOpen = { onOpen(adjunto) },
                    onRemove = { onRemove(adjunto) }
                )
                adjunto.kind == AttachmentKind.AUDIO -> AudioAttachment(
                    attachment = adjunto,
                    path = pathFor(adjunto),
                    onRemove = { onRemove(adjunto) }
                )
                else -> FileAttachment(
                    attachment = adjunto,
                    onOpen = { onOpen(adjunto) },
                    onRemove = { onRemove(adjunto) }
                )
            }
        }
    }
}

@Composable
private fun ImageAttachment(
    attachment: NoteAttachment,
    path: String,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        AsyncImage(
            model = File(path),
            contentDescription = attachment.displayName,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 260.dp)
                .clip(MaterialTheme.shapes.large)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
        )
        RemoveBadge(
            onRemove = onRemove,
            modifier = Modifier.align(Alignment.TopEnd).padding(7.dp)
        )
        Surface(
            onClick = onOpen,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.align(Alignment.BottomStart).padding(7.dp)
        ) {
            Text(
                "Abrir",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
            )
        }
    }
}

@Composable
private fun FileAttachment(
    attachment: NoteAttachment,
    onOpen: () -> Unit,
    onRemove: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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
            RemoveBadge(onRemove = onRemove)
        }
    }
}

/**
 * Una grabación con su botón de escuchar.
 *
 * El reproductor se suelta al salir de la pantalla. Sin eso el audio sigue sonando después de
 * cerrar la nota, y la única forma de callarlo sería cerrar la app.
 */
@Composable
private fun AudioAttachment(
    attachment: NoteAttachment,
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
        shape = MaterialTheme.shapes.large,
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
                    contentDescription = if (reproduciendo) "Pausar" else "Escuchar",
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
            RemoveBadge(onRemove = onRemove)
        }
    }
}

/**
 * Un adjunto cuya copia no está en este teléfono.
 *
 * Pasa al restaurar un respaldo en un móvil nuevo: la copia guarda que la nota llevaba una foto,
 * pero no la foto. Decirlo es mejor que enseñar un hueco gris o, peor, que la fila desaparezca
 * como si nunca hubiera existido.
 */
@Composable
private fun MissingAttachmentRow(attachment: NoteAttachment, onRemove: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
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
                    "No está en este teléfono",
                    color = MaterialTheme.colorScheme.outline,
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.width(8.dp))
            RemoveBadge(onRemove = onRemove)
        }
    }
}

@Composable
private fun RemoveBadge(onRemove: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onRemove,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
    ) {
        Icon(
            Icons.Rounded.Close,
            contentDescription = "Quitar",
            modifier = Modifier.padding(5.dp).size(15.dp)
        )
    }
}

/**
 * La foto de una nota, arriba de su tarjeta en la lista.
 *
 * Es lo que sostiene el mosaico: se eligió porque una nota suele empezar por una foto de la
 * pizarra, y en dos columnas es la foto la que dice de qué va cada tarjeta antes de leer nada.
 */
@Composable
fun NoteCardThumbnail(path: String, compact: Boolean, modifier: Modifier = Modifier) {
    AsyncImage(
        model = File(path),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .then(if (compact) Modifier.aspectRatio(1.35f) else Modifier.height(128.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    )
}

/** Un contador de lo que no cabe en la tarjeta: «2 archivos», «1 grabación». */
@Composable
fun NoteAttachmentSummary(attachments: List<NoteAttachment>, modifier: Modifier = Modifier) {
    val archivos = attachments.count { it.kind == AttachmentKind.FILE }
    val audios = attachments.count { it.kind == AttachmentKind.AUDIO }
    if (archivos == 0 && audios == 0) return

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (archivos > 0) {
            Icon(
                Icons.Rounded.Description,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(12.dp)
            )
            Text(
                archivos.toString(),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
        if (audios > 0) {
            Icon(
                Icons.Rounded.Mic,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(12.dp)
            )
            Text(
                audios.toString(),
                color = MaterialTheme.colorScheme.outline,
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
