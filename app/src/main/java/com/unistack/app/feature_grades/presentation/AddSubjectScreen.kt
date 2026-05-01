package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_grades.domain.SubjectVisualType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AddSubjectScreen(
    onBackClick: () -> Unit,
    onSubjectCreated: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = viewModel()
) {
    BackHandler(onBack = onBackClick)
    var name by remember { mutableStateOf("") }
    var targetAverage by remember { mutableStateOf("4.0") }
    var visualType by remember { mutableStateOf(SubjectVisualType.TEAL) }
    var error by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val targetValue = targetAverage.toDoubleOrNull()
    val nameValidation = TextValidators.validateSubjectName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val isValid = nameValidation.isValid && targetValue != null && targetValue in 0.0..5.0

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = UniStackColors.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(UniStackColors.Background)
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
            Text(
                text = "Agregar materia",
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )
            UniCard(
                modifier = Modifier.fillMaxWidth(),
                color = UniStackColors.Card,
                shape = AppShapes.LargeCard,
                contentPadding = PaddingValues(18.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it.take(40)
                            error = null
                        },
                        label = { Text("Nombre") },
                        placeholder = { Text("Cálculo, Derecho civil, Biología...") },
                        singleLine = true,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth(),
                        isError = !isNameValid,
                        supportingText = {
                            if (!isNameValid) {
                                Text(nameValidation.errorMessage ?: "Ingresa un nombre de materia válido")
                            }
                        }
                    )
                    OutlinedTextField(
                        value = targetAverage,
                        onValueChange = {
                            targetAverage = it
                            error = null
                        },
                        label = { Text("Meta de promedio") },
                        singleLine = true,
                        shape = AppShapes.MediumCard,
                        modifier = Modifier.fillMaxWidth(),
                        isError = targetAverage.isNotBlank() && (targetValue == null || targetValue !in 0.0..5.0)
                    )
                    Text("Color", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
                    SubjectVisualType.values().toList().chunked(6).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { type ->
                                ColorSwatch(
                                    color = subjectAccent(type),
                                    selected = visualType == type,
                                    onClick = { visualType = type }
                                )
                            }
                        }
                    }
                    error?.let {
                        Text(it, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Button(
                onClick = {
                    val subject = viewModel.addSubject(TextValidators.normalizeText(name), targetValue ?: 4.0, visualType)
                    if (subject == null) {
                        error = "Revisa el nombre y la meta antes de guardar."
                    } else {
                        scope.launch {
                            launch {
                                snackbarHostState.showSnackbar("Materia creada correctamente")
                            }
                            delay(650)
                            onSubjectCreated(subject.id)
                        }
                    }
                },
                enabled = isValid,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar materia")
            }
        }
    }
}

@Composable
private fun ColorSwatch(color: Color, selected: Boolean, onClick: () -> Unit) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = if (selected) 1f else 0.22f))
            .clickable(onClick = onClick)
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = Color.White)
        }
    }
}
