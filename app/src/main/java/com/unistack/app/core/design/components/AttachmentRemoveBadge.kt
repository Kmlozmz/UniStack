package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.unistack.app.R

/**
 * La equis redonda para quitar un adjunto antes de guardar.
 *
 * La misma en notas y en tareas; estaba escrita dos veces, igual, una en cada pantalla.
 */
@Composable
fun AttachmentRemoveBadge(onRemove: () -> Unit, modifier: Modifier = Modifier) {
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
