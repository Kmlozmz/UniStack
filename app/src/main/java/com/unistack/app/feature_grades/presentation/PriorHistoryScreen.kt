package com.unistack.app.feature_grades.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.SquishyButton

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.GradingScale

@Composable
fun PriorHistoryScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onAddActivitiesClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val activeOrder = subject?.periodScheme?.periods
        ?.firstOrNull { it.id == subject.activePeriodId }
        ?.order
        ?: 1
    val previousPeriods = subject?.periodScheme?.periods
        ?.filter { it.order < activeOrder }
        ?.sortedBy { it.order }
        .orEmpty()
    var periodForFinalResult by remember { mutableStateOf<AcademicPeriod?>(null) }

    LaunchedEffect(subject?.grades, subject?.unknownPeriodIds) {
        if (subject != null) viewModel.refreshHistoryCompletion(subject.id)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = scrollBottomRoom),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = UniStackColors.TextPrimary
                    )
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        "Completar historial",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = UniStackColors.TextPrimary
                    )
                    Text(
                        subject?.name.orEmpty(),
                        color = UniStackColors.TextSecondary
                    )
                }
            }
        }
        item {
            Surface(
                shape = AppShapes.SmallCard,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Rounded.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        "Registra lo que recuerdes. Una nota final del corte es suficiente; no necesitas inventar actividades ni porcentajes.",
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        if (subject == null || previousPeriods.isEmpty()) {
            item {
                Text(
                    "No hay cortes anteriores pendientes.",
                    color = UniStackColors.TextSecondary,
                    modifier = Modifier.padding(vertical = 28.dp)
                )
            }
        } else {
            previousPeriods.forEach { period ->
                item(key = period.id) {
                    val grades = subject.grades.filter { it.periodId == period.id }
                    val calculation = GradeCalculator.calculatePeriod(grades)
                    val unknown = period.id in subject.unknownPeriodIds
                    HistoryPeriodCard(
                        period = period,
                        resultLabel = when {
                            calculation.usesOfficialResult ->
                                "Nota final: ${GradingScaleUtils.formatGrade(calculation.average, profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE)}"
                            grades.isNotEmpty() ->
                                "${grades.size} ${if (grades.size == 1) "actividad" else "actividades"} registradas"
                            unknown -> "Marcado como información no disponible"
                            else -> "Sin información"
                        },
                        resolved = calculation.average != null || unknown,
                        onFinalResultClick = { periodForFinalResult = period },
                        onActivitiesClick = { onAddActivitiesClick(subject.id, period.id) },
                        onUnknownClick = {
                            if (unknown) {
                                viewModel.clearPeriodUnknown(subject.id, period.id)
                            } else {
                                viewModel.markPeriodUnknown(subject.id, period.id)
                            }
                        },
                        unknown = unknown
                    )
                }
            }
        }
        item {
            Spacer(modifier = Modifier.navigationBarsPadding())
        }
    }

    periodForFinalResult?.let { period ->
        FinalPeriodGradeDialog(
            period = period,
            maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0,
            onDismiss = { periodForFinalResult = null },
            onSave = { value ->
                val outcome = viewModel.saveGrade(
                    subjectId = subjectId,
                    name = "Resultado final ${period.name}",
                    value = value,
                    percentageInput = 100.0,
                    periodId = period.id,
                    source = GradeSource.PERIOD_FINAL
                )
                if (outcome.saved) {
                    viewModel.clearPeriodUnknown(subjectId, period.id)
                    viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                    periodForFinalResult = null
                }
                outcome.saved
            }
        )
    }
}

@Composable
private fun HistoryPeriodCard(
    period: AcademicPeriod,
    resultLabel: String,
    resolved: Boolean,
    onFinalResultClick: () -> Unit,
    onActivitiesClick: () -> Unit,
    onUnknownClick: () -> Unit,
    unknown: Boolean
) {
    Surface(
        shape = AppShapes.SmallCard,
        color = UniStackColors.Card,
        border = androidx.compose.foundation.BorderStroke(1.dp, UniStackColors.SoftOutline)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (resolved) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = if (resolved) UniStackColors.Green else UniStackColors.Primary
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        period.name,
                        fontWeight = FontWeight.Bold,
                        color = UniStackColors.TextPrimary
                    )
                    Text(resultLabel, color = UniStackColors.TextSecondary)
                }
            }
            SquishyButton(onClick = onFinalResultClick, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar nota final del corte")
            }
            OutlinedButton(onClick = onActivitiesClick, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar actividades individuales")
            }
            TextButton(onClick = onUnknownClick, modifier = Modifier.fillMaxWidth()) {
                Text(if (unknown) "Volver a completar este corte" else "No tengo esta información")
            }
        }
    }
}

@Composable
private fun FinalPeriodGradeDialog(
    period: AcademicPeriod,
    maxGrade: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Boolean
) {
    var value by remember(period.id) { mutableStateOf("") }
    var error by remember(period.id) { mutableStateOf<String?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nota final de ${period.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Esta nota representará el corte completo y tendrá prioridad sobre sus actividades.")
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    label = { Text("Nota obtenida") },
                    suffix = { Text("/ ${maxGrade.toString().removeSuffix(".0")}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = value.toDoubleOrNull()
                    if (parsed == null || parsed !in 0.0..maxGrade || !onSave(parsed)) {
                        error = "Ingresa una nota válida entre 0 y $maxGrade."
                    }
                }
            ) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
