package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
fun AddTaskScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
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
        Text("Nueva tarea", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        UniCard(
            modifier = Modifier.fillMaxWidth(),
            color = UniStackColors.BlueLight,
            shape = AppShapes.LargeCard,
            contentPadding = PaddingValues(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.AutoMirrored.Rounded.EventNote, contentDescription = null, tint = UniStackColors.Blue)
                Text("Formulario preparado para título, materia, fecha límite y tiempo estimado.", color = UniStackColors.TextPrimary)
            }
        }
        Button(onClick = {}, shape = AppShapes.Pill, colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue)) {
            Text("Crear tarea")
        }
    }
}
