package com.unistack.app.core.design.components

import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.ChipStyle
import com.unistack.app.core.design.theme.LocalAppearancePreferences
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
data class UniFilterOption<T>(
    val value: T,
    val label: String
)

@Composable
fun UniEmptyStateCard(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    iconColor: Color = MaterialTheme.colorScheme.primary
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = color,
        shape = MaterialTheme.shapes.large,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
            Text(body, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, lineHeight = 18.sp)
            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = onActionClick,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(actionText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun UniConfirmDeleteDialog(
    title: String,
    body: String,
    confirmText: String = stringResource(R.string.common_delete),
    dismissText: String = stringResource(R.string.common_cancel),
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        icon = {
            Icon(
                Icons.Rounded.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    )
}

@Composable
fun <T> UniFilterChipRow(
    options: List<UniFilterOption<T>>,
    selected: T,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            FilterChip(
                selected = option.value == selected,
                onClick = { onSelected(option.value) },
                label = {
                    Text(option.label, fontWeight = FontWeight.Bold)
                },
                shape = CircleShape,
                /*
                 * El estilo de chip sale de Apariencia, y estos son **los chips de la app**:
                 * los filtros de Tareas y de Gastos pasan todos por aqui.
                 *
                 * Estuvo escrito a mano —relleno tonal siempre— asi que «Filete» y «Solo
                 * texto» se elegian y no cambiaban nada. La diferencia esta en como se marca
                 * el activo: relleno lo tine, filete lo rodea, y solo texto deja el color de
                 * la letra como unica senal.
                 */
                colors = when (LocalAppearancePreferences.current.chipStyle) {
                    ChipStyle.RELLENO -> FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ChipStyle.FILETE -> FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ChipStyle.TEXTO -> FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color.Transparent,
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = Color.Transparent,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                border = when (LocalAppearancePreferences.current.chipStyle) {
                    ChipStyle.FILETE -> FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = option.value == selected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant,
                        selectedBorderColor = MaterialTheme.colorScheme.primary
                    )
                    // Sin contorno: en relleno lo dice el fondo y en texto, la letra.
                    else -> FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = option.value == selected,
                        borderColor = Color.Transparent,
                        selectedBorderColor = Color.Transparent
                    )
                }
            )
        }
    }
}
