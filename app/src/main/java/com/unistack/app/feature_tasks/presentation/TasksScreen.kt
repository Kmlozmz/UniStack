package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun TasksScreen(onNewTaskClick: () -> Unit, modifier: Modifier = Modifier) {
    val tasks = listOf(
        "Primera entrega" to "Materia personalizada · vence mañana · 2 h",
        "Preparar evaluación" to "Tu materia · viernes · 3 h",
        "Repasar apuntes" to "General · lunes · 45 min"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Tareas", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                Text("Organiza entregas, parciales y actividades académicas.", color = UniStackColors.TextSecondary)
            }
        }
        items(tasks) { task ->
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Card,
                shape = AppShapes.MediumCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AssignmentTurnedIn, contentDescription = null, tint = UniStackColors.Green)
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(task.first, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                        Text(task.second, color = UniStackColors.TextSecondary, fontSize = 13.sp)
                    }
                }
            }
        }
        item {
            Button(
                onClick = onNewTaskClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue)
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Nueva tarea")
            }
        }
    }
}
