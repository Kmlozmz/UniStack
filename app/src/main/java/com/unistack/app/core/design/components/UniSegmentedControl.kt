package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors

data class UniSegmentedOption<T>(
    val value: T,
    val label: String,
    val icon: ImageVector
)

@Composable
fun <T> UniSegmentedControl(
    selected: T,
    options: List<UniSegmentedOption<T>>,
    onSelected: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.height(LocalInterfaceSpacing.current.controlHeight),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = AppShapes.MediumCard,
        tonalElevation = 0.dp,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(3.dp)
    ) {
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            options.forEach { option ->
                val isSelected = selected == option.value
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(AppShapes.SmallCard)
                        .background(if (isSelected) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
                        .clickable { onSelected(option.value) },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            option.icon,
                            contentDescription = null,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(9.dp))
                        Text(
                            option.label,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
