package com.unistack.app.feature_tasks.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.EventNote
import androidx.compose.material.icons.rounded.AddTask
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun TasksScreen(onNewTaskClick: () -> Unit, modifier: Modifier = Modifier) {
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
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.BlueLight,
                shape = AppShapes.LargeCard,
                contentPadding = PaddingValues(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.AutoMirrored.Rounded.EventNote, contentDescription = null, tint = UniStackColors.Blue)
                    Text("Aún no tienes tareas reales.", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Cuando implementemos tareas con Room, aquí verás tus entregas y pendientes guardados.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
        item {
            Button(
                onClick = onNewTaskClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Blue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AddTask, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Nueva tarea")
            }
        }
    }
}
