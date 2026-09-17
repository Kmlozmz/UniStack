package com.unistack.app.feature_grades.presentation

import androidx.compose.ui.res.stringResource
import com.unistack.app.R

import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.scrollBottomRoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.HistoryEdu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingScale

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
    val activeOrder = subject?.cutScheme?.cuts
        ?.firstOrNull { it.id == subject.activeCutId }
        ?.order
        ?: 1
    val previousCuts = subject?.cutScheme?.cuts
        ?.filter { it.order < activeOrder }
        ?.sortedBy { it.order }
        .orEmpty()
    var cutForFinalResult by remember { mutableStateOf<GradingCut?>(null) }

    LaunchedEffect(subject?.grades, subject?.unknownCutIds) {
        if (subject != null) viewModel.refreshHistoryCompletion(subject.id)
    }

    LargeTitleScaffold(
        title = stringResource(R.string.prior_history_title),
        subtitle = subject?.name.orEmpty(),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = 20.dp,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 14.dp
    ) {
        item {
            Surface(
                shape = MaterialTheme.shapes.medium,
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
                        stringResource(R.string.prior_history_desc),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
        if (subject == null || previousCuts.isEmpty()) {
            item {
                Text(
                    stringResource(R.string.prior_history_none_pending),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 28.dp)
                )
            }
        } else {
            previousCuts.forEach { cut ->
                item(key = cut.id) {
                    val grades = subject.grades.filter { it.cutId == cut.id }
                    val calculation = GradeCalculator.calculateCut(grades)
                    val unknown = cut.id in subject.unknownCutIds
                    HistoryCutCard(
                        cut = cut,
                        resultLabel = when {
                            calculation.usesOfficialResult ->
                                stringResource(R.string.prior_history_final_grade_label, GradingScaleUtils.formatGrade(calculation.average, profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE))
                            grades.isNotEmpty() ->
                                if (grades.size == 1) stringResource(R.string.prior_history_grades_count_single)
                                else stringResource(R.string.prior_history_grades_count_multiple, grades.size)
                            unknown -> stringResource(R.string.prior_history_marked_no_info)
                            else -> stringResource(R.string.prior_history_no_info)
                        },
                        resolved = calculation.average != null || unknown,
                        onFinalResultClick = { cutForFinalResult = cut },
                        onActivitiesClick = { onAddActivitiesClick(subject.id, cut.id) },
                        onUnknownClick = {
                            if (unknown) {
                                viewModel.clearCutUnknown(subject.id, cut.id)
                            } else {
                                viewModel.markCutUnknown(subject.id, cut.id)
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

    cutForFinalResult?.let { cut ->
        val finalResultName = stringResource(R.string.grade_final_result, cut.name)
        FinalCutGradeDialog(
            cut = cut,
            maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0,
            onDismiss = { cutForFinalResult = null },
            onSave = { value ->
                val outcome = viewModel.saveGrade(
                    subjectId = subjectId,
                    name = finalResultName,
                    value = value,
                    percentageInput = 100.0,
                    cutId = cut.id,
                    source = GradeSource.PERIOD_FINAL
                )
                if (outcome.saved) {
                    viewModel.clearCutUnknown(subjectId, cut.id)
                    viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                    cutForFinalResult = null
                }
                outcome.saved
            }
        )
    }
}

@Composable
private fun HistoryCutCard(
    cut: GradingCut,
    resultLabel: String,
    resolved: Boolean,
    onFinalResultClick: () -> Unit,
    onActivitiesClick: () -> Unit,
    onUnknownClick: () -> Unit,
    unknown: Boolean
) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (resolved) Icons.Rounded.CheckCircle else Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = null,
                    tint = if (resolved) LocalSectionColors.current.onTrack else MaterialTheme.colorScheme.primary
                )
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    Text(
                        cut.name,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(resultLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            // Las tres van al wrapper, y con la jerarquía que les toca: registrar la nota es
            // la acción principal, desglosarla en actividades es la alternativa, y decir que
            // no tienes el dato es salir sin registrar nada.
            UniStackButton(
                text = stringResource(R.string.prior_history_record_final_grade),
                onClick = onFinalResultClick,
                variant = UniStackButtonVariant.Filled
            )
            UniStackButton(
                text = stringResource(R.string.prior_history_record_activities),
                onClick = onActivitiesClick,
                variant = UniStackButtonVariant.Tonal
            )
            UniStackButton(
                text = if (unknown) stringResource(R.string.prior_history_redo_cut) else stringResource(R.string.prior_history_dont_have_info),
                onClick = onUnknownClick,
                variant = UniStackButtonVariant.Outlined
            )
        }
    }
}

@Composable
private fun FinalCutGradeDialog(
    cut: GradingCut,
    maxGrade: Double,
    onDismiss: () -> Unit,
    onSave: (Double) -> Boolean
) {
    var value by remember(cut.id) { mutableStateOf("") }
    var error by remember(cut.id) { mutableStateOf<String?>(null) }
    val rangeErrorMsg = stringResource(R.string.prior_history_grade_range_error, maxGrade.toString().removeSuffix(".0"))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.prior_history_final_grade_cut, cut.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.prior_history_final_grade_desc))
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    label = { Text(stringResource(R.string.grade_obtained)) },
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
                        error = rangeErrorMsg
                    }
                }
            ) { Text(stringResource(R.string.action_save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.action_cancel)) }
        }
    )
}
