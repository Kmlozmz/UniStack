package com.unistack.app.core.design.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

data class UniFilterOption<T>(
    val value: T,
    val label: String
)

@Composable
fun UniScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                color = UniStackColors.TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (trailing != null) {
            Spacer(modifier = Modifier.padding(start = 12.dp))
            trailing()
        }
    }
}

@Composable
fun UniEmptyStateCard(
    title: String,
    body: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    color: Color = UniStackColors.Card,
    iconColor: Color = UniStackColors.Primary
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = color,
        shape = AppShapes.MediumCard,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = null, tint = iconColor)
            Text(title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(body, color = UniStackColors.TextSecondary, fontSize = 13.sp, lineHeight = 18.sp)
            if (actionText != null && onActionClick != null) {
                Button(
                    onClick = onActionClick,
                    shape = AppShapes.Pill,
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
    confirmText: String = "Eliminar",
    dismissText: String = "Cancelar",
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
                tint = UniStackColors.Coral
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(confirmText, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        },
        containerColor = UniStackColors.Background
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
                shape = AppShapes.Pill,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = UniStackColors.PrimaryLight,
                    selectedLabelColor = UniStackColors.PrimaryDark,
                    containerColor = UniStackColors.Card,
                    labelColor = UniStackColors.TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = option.value == selected,
                    borderColor = Color.Transparent,
                    selectedBorderColor = UniStackColors.Primary.copy(alpha = 0.25f)
                )
            )
        }
    }
}
