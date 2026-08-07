package com.unistack.app.feature_setup.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

/**
 * Campo opcional para el nombre de la institución, con sugerencias de [InstitutionCatalog].
 *
 * Acepta siempre texto libre: las sugerencias son un atajo, no una restricción. Se explica
 * para qué sirve porque un campo opcional sin motivo se rellena peor y con menos cuidado.
 */
@Composable
fun InstitutionField(
    value: String,
    label: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var dismissedSuggestions by remember { mutableStateOf(false) }
    val suggestions = remember(value, dismissedSuggestions) {
        if (dismissedSuggestions) emptyList() else InstitutionCatalog.suggestionsFor(value)
    }
    val showSuggestions = suggestions.isNotEmpty() && suggestions.none { it == value }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = label,
                color = UniStackColors.Primary,
                fontSize = 14.sp
            )
            Text(
                text = "Opcional",
                color = UniStackColors.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .clip(AppShapes.Pill)
                    .background(UniStackColors.SurfaceVariant)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            )
        }
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = Color.Transparent)) {
            OutlinedTextField(
                value = value,
                onValueChange = {
                    dismissedSuggestions = false
                    onValueChange(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 58.dp),
                singleLine = true,
                placeholder = {
                    Text(
                        text = placeholder,
                        color = UniStackColors.TextSecondary.copy(alpha = 0.72f),
                        fontSize = 15.sp
                    )
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Rounded.Apartment,
                        contentDescription = null,
                        tint = UniStackColors.Primary,
                        modifier = Modifier.size(22.dp)
                    )
                },
                shape = AppShapes.SmallCard
            )
        }

        AnimatedVisibility(
            visible = showSuggestions,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                suggestions.forEach { suggestion ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(AppShapes.Small)
                            .background(UniStackColors.SurfaceVariant)
                            .clickable {
                                onValueChange(suggestion)
                                dismissedSuggestions = true
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Apartment,
                            contentDescription = null,
                            tint = UniStackColors.Primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = suggestion,
                            color = UniStackColors.TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}
