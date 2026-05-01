package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun GradeSimulatorScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Text(
            text = "Nota necesaria",
            color = UniStackColors.TextPrimary,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold
        )
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.YellowLight,
            shape = AppShapes.LargeCard,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = UniStackColors.Yellow)
                Text("El simulador usará tus materias reales para calcular la nota que necesitas.", color = UniStackColors.TextPrimary)
            }
        }
    }
}
