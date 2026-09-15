package com.unistack.app.feature_tasks.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniIconButtonVariant

/**
 * El «⋮» de editar/duplicar/eliminar, igual en la hoja de detalle y en la pantalla completa.
 *
 * Antes esos tres vivían como iconos sueltos pegados al botón de marcar hecha: fácil tocar el
 * que no era. Colapsados aquí, sólo hay dos zonas de toque abajo (marcar + este menú), y
 * Eliminar va separado por línea y en rojo — la misma regla de `UniDropdownMenu` de siempre.
 */
@Composable
fun TaskActionsOverflowMenu(
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    variant: UniIconButtonVariant = UniIconButtonVariant.Standard
) {
    var expanded by remember { mutableStateOf(false) }
    UniIconButton(
        icon = Icons.Rounded.MoreVert,
        contentDescription = stringResource(R.string.tasks_more_actions),
        onClick = { expanded = true },
        variant = variant
    )
    UniDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_edit)) },
            leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
            onClick = { expanded = false; onEdit() }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.tasks_action_duplicate)) },
            leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = null) },
            onClick = { expanded = false; onDuplicate() }
        )
        androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
        DropdownMenuItem(
            text = { Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = { expanded = false; onDelete() }
        )
    }
}
