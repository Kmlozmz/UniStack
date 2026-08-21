@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniStackButtonDefaults
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Elegir qué parte de la foto se ve.
 *
 * El retrato se pinta redondo y recortado al centro, así que una foto entera casi nunca queda
 * como quien la eligió esperaba: la cabeza arriba, el encuadre a un lado, medio cuerpo fuera.
 * Recortar antes de guardar convierte esa lotería en una decisión.
 *
 * Se ve dentro del círculo definitivo, no en un rectángulo con guías: lo que hay que decidir es
 * cómo va a quedar el avatar, y cualquier otra forma obliga a imaginárselo.
 */
@Composable
internal fun ProfilePhotoEditor(
    source: Uri,
    onCancel: () -> Unit,
    onSave: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var bitmap by remember(source) { mutableStateOf<Bitmap?>(null) }
    var failed by remember(source) { mutableStateOf(false) }
    var saving by remember(source) { mutableStateOf(false) }
    var zoom by remember(source) { mutableFloatStateOf(1f) }
    var offset by remember(source) { mutableStateOf(Offset.Zero) }
    var frame by remember(source) { mutableStateOf(0) }

    LaunchedEffect(source) {
        val loaded = withContext(Dispatchers.IO) { ProfilePhotoFiles.decodeForEditing(context, source) }
        if (loaded == null) failed = true else bitmap = loaded
    }

    // El desplazamiento no puede sacar la foto del círculo: el margen que sobra a cada lado es
    // la mitad de lo que la imagen excede al cuadro, y más allá solo habría hueco vacío.
    fun clamp(candidate: Offset, image: Bitmap, side: Int): Offset {
        if (side == 0) return Offset.Zero
        val base = max(side.toFloat() / image.width, side.toFloat() / image.height)
        val shown = base * zoom
        val slackX = max(0f, (image.width * shown - side) / 2f)
        val slackY = max(0f, (image.height * shown - side) / 2f)
        return Offset(
            candidate.x.coerceIn(-slackX, slackX),
            candidate.y.coerceIn(-slackY, slackY)
        )
    }

    AlertDialog(
        onDismissRequest = { if (!saving) onCancel() },
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                "Ajusta tu foto",
                style = MaterialTheme.typography.headlineSmallEmphasized,
                fontWeight = FontWeight.ExtraBold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    "Arrastra para mover y pellizca para acercar. Así se verá en toda la app.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .onSizeChanged { frame = min(it.width, it.height) },
                    contentAlignment = Alignment.Center
                ) {
                    val image = bitmap
                    when {
                        failed -> Text(
                            "No se pudo abrir esa imagen.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        image == null -> CircularWavyProgressIndicator()
                        else -> Image(
                            bitmap = image.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .graphicsLayer {
                                    scaleX = zoom
                                    scaleY = zoom
                                    translationX = offset.x
                                    translationY = offset.y
                                }
                                .pointerInput(image, frame) {
                                    detectTransformGestures { _, pan, gestureZoom, _ ->
                                        zoom = (zoom * gestureZoom).coerceIn(1f, 4f)
                                        offset = clamp(offset + pan, image, frame)
                                    }
                                }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Slider(
                        value = zoom,
                        onValueChange = { value ->
                            zoom = value
                            bitmap?.let { offset = clamp(offset, it, frame) }
                        },
                        valueRange = 1f..4f,
                        modifier = Modifier.weight(1f),
                        enabled = bitmap != null && !saving
                    )
                    TextButton(
                        onClick = {
                            zoom = 1f
                            offset = Offset.Zero
                        },
                        enabled = bitmap != null && !saving
                    ) {
                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel, enabled = !saving) { Text("Cancelar") }
        },
        confirmButton = {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    val image = bitmap ?: return@Button
                    saving = true
                    scope.launch {
                        val path = ProfilePhotoFiles.saveCrop(
                            context = context,
                            source = source,
                            image = image,
                            frame = frame,
                            zoom = zoom,
                            offset = offset
                        )
                        saving = false
                        if (path != null) onSave(path) else onCancel()
                    }
                },
                enabled = bitmap != null && !saving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (saving) "Guardando…" else "Guardar", fontWeight = FontWeight.Bold)
            }
        }
    )
}
