@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.unistack.app.feature_tasks.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_notes.domain.AttachmentKind
import com.unistack.app.feature_notes.domain.Attachments

/** Lo que necesita cualquier pantalla que deje adjuntar a una tarea: hoja, pantalla completa o formulario. */
class TaskAttachController internal constructor(
    val menuOpen: Boolean,
    val recorderOpen: Boolean,
    val openMenu: () -> Unit,
    val dismissMenu: () -> Unit,
    val pickPhoto: () -> Unit,
    val takePhoto: () -> Unit,
    val pickFile: () -> Unit,
    val openRecorder: () -> Unit,
    val dismissRecorder: () -> Unit
)

/**
 * Junta los tres `rememberLauncherForActivityResult` (foto, archivo, cámara) y el grabador,
 * calcado del flujo de `NoteEditorScreen`. Una sola vez por pantalla para no repetirlo en la
 * hoja, la pantalla completa y el formulario.
 */
@Composable
fun rememberTaskAttachController(
    taskId: String,
    viewModel: TasksViewModel,
    onError: (String) -> Unit
): TaskAttachController {
    val context = LocalContext.current
    var menuOpen by remember { mutableStateOf(false) }
    var recorderOpen by remember { mutableStateOf(false) }
    var pendingPhoto by remember { mutableStateOf<String?>(null) }

    val maxLimit = Attachments.formatSize(Attachments.MAX_BYTES)
    val errPhoto = Textos.get(R.string.tasks_attachment_error_photo, maxLimit)
    val errFile = Textos.get(R.string.tasks_attachment_error_file, maxLimit)
    val errCamera = Textos.get(R.string.tasks_attachment_error_camera)
    val newPhotoName = Textos.get(R.string.tasks_attachment_new_photo)

    val elegirFoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null && !viewModel.attachTask(taskId, uri)) onError(errPhoto)
    }
    val elegirArchivo = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null && !viewModel.attachTask(taskId, uri)) onError(errFile)
    }
    val hacerFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { hecha ->
        val guardado = pendingPhoto
        pendingPhoto = null
        if (guardado == null) return@rememberLauncherForActivityResult
        if (!hecha) {
            viewModel.discardTaskStoredFile(guardado)
            return@rememberLauncherForActivityResult
        }
        if (!viewModel.attachTaskStoredFile(taskId, guardado, newPhotoName, "image/jpeg", AttachmentKind.IMAGE)) {
            onError(errPhoto)
        }
    }

    return TaskAttachController(
        menuOpen = menuOpen,
        recorderOpen = recorderOpen,
        openMenu = { menuOpen = true },
        dismissMenu = { menuOpen = false },
        pickPhoto = {
            menuOpen = false
            elegirFoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        },
        pickFile = {
            menuOpen = false
            elegirArchivo.launch(arrayOf("*/*"))
        },
        takePhoto = {
            menuOpen = false
            val (nombre, archivo) = viewModel.newTaskAttachmentFile("jpg")
            runCatching { archivo.createNewFile() }
            val uri = runCatching {
                androidx.core.content.FileProvider.getUriForFile(context, context.packageName + ".provider", archivo)
            }.getOrNull()
            if (uri == null) {
                viewModel.discardTaskStoredFile(nombre)
                onError(errCamera)
            } else {
                pendingPhoto = nombre
                hacerFoto.launch(uri)
            }
        },
        openRecorder = {
            menuOpen = false
            recorderOpen = true
        },
        dismissRecorder = { recorderOpen = false }
    )
}

/** La hoja de «Foto de galería / Tomar foto / Archivo / Grabar audio», igual en las tres pantallas. */
@Composable
fun TaskAttachMenuSheet(
    onDismiss: () -> Unit,
    onPickPhoto: () -> Unit,
    onTakePhoto: () -> Unit,
    onPickFile: () -> Unit,
    onRecordAudio: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.tasks_attachment_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
            )
            Text(
                text = stringResource(R.string.tasks_attachment_sheet_subtitle, Attachments.formatSize(Attachments.MAX_BYTES)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp, start = 2.dp)
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TaskAttachMenuTile(Modifier.weight(1f), Icons.Rounded.Image, stringResource(R.string.tasks_attachment_pick_photo), onPickPhoto)
                TaskAttachMenuTile(Modifier.weight(1f), Icons.Rounded.CameraAlt, stringResource(R.string.tasks_attachment_take_photo), onTakePhoto)
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TaskAttachMenuTile(Modifier.weight(1f), Icons.Rounded.Description, stringResource(R.string.tasks_attachment_pick_file), onPickFile)
                TaskAttachMenuTile(Modifier.weight(1f), Icons.Rounded.Mic, stringResource(R.string.tasks_attachment_record_audio), onRecordAudio)
            }
        }
    }
}

@Composable
private fun TaskAttachMenuTile(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    // Alto fijo, no `heightIn`: dentro de la hoja la restricción de alto llega suelta, así que
    // un mínimo con `fillMaxSize` dentro hacía que cada casilla se comiera la pantalla entera.
    Surface(
        modifier = modifier
            .height(96.dp)
            .cleanClickable(shape = RoundedCornerShape(18.dp), onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}
