@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.unistack.app.feature_support.presentation

import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import com.unistack.app.core.design.components.UniSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * Qué hace la pestaña en la que estás, con un ejemplo con números.
 *
 * Un ejemplo y no una definición: «promedio ponderado por el peso de cada evaluación» describe
 * la cuenta a quien ya la sabe. «Sacaste 4,2 en el parcial, que vale el 30 %» la enseña.
 */
@Composable
internal fun CalculatorHelpSheet(
    tab: CalculatorTab,
    maxGrade: Double,
    scale: GradingScale,
    onDismiss: () -> Unit
) {
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val top = GradingScaleUtils.formatGrade(maxGrade, scale)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = state) {
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 34.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = tab.label,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when (tab) {
                    CalculatorTab.SUBJECT -> stringResource(R.string.calculator_sheet_course_desc)
                    CalculatorTab.SEMESTER -> stringResource(R.string.calculator_sheet_semester_desc)
                    CalculatorTab.NEEDED -> stringResource(R.string.calculator_sheet_target_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.calculator_sheet_example_header), style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
                    Text(
                        text = when (tab) {
                            CalculatorTab.SUBJECT -> stringResource(R.string.calculator_sheet_example_course)
                            CalculatorTab.SEMESTER -> stringResource(R.string.calculator_sheet_example_semester)
                            CalculatorTab.NEEDED -> stringResource(R.string.calculator_sheet_example_target, top)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Text(
                text = when (tab) {
                    CalculatorTab.SUBJECT -> stringResource(R.string.calculator_sheet_footer_percentages)
                    CalculatorTab.SEMESTER -> stringResource(R.string.calculator_sheet_footer_credits)
                    CalculatorTab.NEEDED -> stringResource(R.string.calculator_sheet_footer_safe)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Cuáles de tus materias entran en la cuenta, y si vienen con sus notas.
 *
 * Es una ventana y no una hoja que sube desde abajo: una hoja tapa la pantalla entera y aquí
 * se está eligiendo sobre una cuenta que conviene seguir viendo. Y la elección es corta —marcar
 * dos o tres nombres—, no un formulario que pida toda la altura.
 *
 * Elegir de una lista y no traerlas todas: quien calcula el semestre puede querer solo las de
 * un corte, o dejar fuera la que va a repetir. Y el interruptor de las notas existe porque las
 * dos preguntas son válidas —«cómo voy» y «cómo acabaría si sacara esto»— y la segunda pide la
 * materia sin su nota.
 */
@Composable
internal fun SubjectPickerSheet(
    subjects: List<CalculatorSubject>,
    scale: GradingScale,
    onDismiss: () -> Unit,
    onConfirm: (List<CalculatorSubject>, Boolean) -> Unit
) {
    var withGrades by remember { mutableStateOf(true) }
    var picked by remember { mutableStateOf(setOf<String>()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        title = {
            Text(
                text = stringResource(R.string.calc_traer_mis_materias),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.calculator_picker_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (subjects.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest
                    ) {
                        Text(
                            text = stringResource(R.string.calculator_picker_empty),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Surface(
                        onClick = { withGrades = !withGrades },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        color = if (withGrades) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 15.dp, end = 11.dp, top = 10.dp, bottom = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stringResource(R.string.calculator_picker_import_grades),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (withGrades) {
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.onSurface
                                    }
                                )
                                Text(
                                    text = if (withGrades) stringResource(R.string.calculator_picker_with_gpa) else stringResource(R.string.calculator_picker_blank_grade),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (withGrades) {
                                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                            UniSwitch(checked = withGrades, onCheckedChange = { withGrades = it })
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.heightIn(max = 280.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(subjects.size, key = { subjects[it].id }) { index ->
                            val subject = subjects[index]
                            val on = subject.id in picked
                            Surface(
                                onClick = {
                                    picked = if (on) picked - subject.id else picked + subject.id
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = MaterialTheme.shapes.large,
                                color = if (on) {
                                    MaterialTheme.colorScheme.secondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLowest
                                },
                                contentColor = if (on) {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(22.dp)
                                            .clip(MaterialTheme.shapes.extraSmall)
                                            .background(
                                                if (on) MaterialTheme.colorScheme.primary else Color.Transparent
                                            )
                                            .border(
                                                2.dp,
                                                if (on) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.outline
                                                },
                                                MaterialTheme.shapes.extraSmall
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (on) {
                                            Icon(
                                                Icons.Rounded.Check,
                                                contentDescription = null,
                                                modifier = Modifier.size(14.dp),
                                                tint = MaterialTheme.colorScheme.onPrimary
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = when {
                                                subject.average == null -> (stringResource(R.string.calc_sin_notas_registradas))
                                                withGrades -> stringResource(R.string.calculator_picker_enters_with) + GradingScaleUtils.formatGrade(subject.average, scale)
                                                else -> (stringResource(R.string.calc_llevas)) + GradingScaleUtils.formatGrade(subject.average, scale)
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = stringResource(R.string.calculator_picker_credits_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(subjects.filter { it.id in picked }, withGrades) },
                enabled = picked.isNotEmpty()
            ) {
                Text(
                    text = when (picked.size) {
                        0 -> (stringResource(R.string.calc_traer))
                        1 -> (stringResource(R.string.calc_traer_1))
                        else -> (stringResource(R.string.calc_traer_2)) + picked.size
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    )
}
