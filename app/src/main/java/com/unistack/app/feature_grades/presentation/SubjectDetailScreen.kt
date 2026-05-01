package com.unistack.app.feature_grades.presentation

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors

@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddGradeClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    val subjects by viewModel.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == subjectId }

    if (subject == null) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(UniStackColors.Background)
                .padding(20.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text("Materia no encontrada", color = UniStackColors.TextPrimary)
        }
        return
    }

    val average = viewModel.currentAverage(subject)
    val evaluated = viewModel.evaluatedPercentage(subject)
    val needed = viewModel.neededGrade(subject)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text(subject.name, color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = subjectBackground(subject.visualType),
                shape = AppShapes.LargeCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Promedio actual", color = UniStackColors.TextSecondary)
                        Text(String.format("%.1f", average), color = UniStackColors.TextPrimary, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("${String.format("%.0f", evaluated)}% evaluado", color = subjectAccent(subject.visualType), fontWeight = FontWeight.ExtraBold)
                }
            }
        }
        item {
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.YellowLight,
                shape = AppShapes.MediumCard
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.TrackChanges, contentDescription = null, tint = UniStackColors.Yellow)
                    Text(
                        text = if (needed.isNaN()) "Ya no queda porcentaje disponible." else "Para terminar con ${String.format("%.1f", subject.targetAverage)} necesitas ${String.format("%.1f", needed)}.",
                        color = UniStackColors.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(start = 10.dp)
                    )
                }
            }
        }
        if (subject.grades.isEmpty()) {
            item {
                UniCard(modifier = Modifier.fillMaxWidth(), color = UniStackColors.Card, shape = AppShapes.MediumCard) {
                    Text("Aún no tienes notas en esta materia.", color = UniStackColors.TextSecondary)
                }
            }
        } else {
            items(subject.grades, key = { it.id }) { grade ->
                UniCard(modifier = Modifier.fillMaxWidth(), color = UniStackColors.Card, shape = AppShapes.MediumCard) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(grade.name, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                            Text("${String.format("%.0f", grade.percentage * 100)}%", color = UniStackColors.TextSecondary)
                        }
                        Text(String.format("%.1f", grade.value), color = UniStackColors.Primary, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                    }
                }
            }
        }
        item {
            Button(
                onClick = { onAddGradeClick(subject.id) },
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null)
                Spacer(modifier = Modifier.padding(3.dp))
                Text("Agregar nota")
            }
        }
    }
}
