@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.SettingsHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.CutBalanceNotice
import com.unistack.app.core.design.components.CutCountSection
import com.unistack.app.core.design.components.CutDatesSection
import com.unistack.app.core.design.components.CutWheelCard
import com.unistack.app.core.design.components.GradeStepperRow
import com.unistack.app.core.design.components.ScaleZoneBar
import com.unistack.app.core.design.components.SetupEvenSplitAction
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.components.setupPercentValue
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.performSafely
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.CutDateRules
import com.unistack.app.feature_user.domain.GradingScale
import java.time.LocalDate
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * Las cuatro puertas del hub de Configuración académica.
 *
 * Cada pantalla es la sección de siempre, solo que sola: mismas piezas de [AcademicPieces],
 * mismo `ProfileViewModel`, un único botón de guardar cada una. Nada de esto cambia cómo se
 * guarda -- eso sigue en el `ViewModel`, intacto -- solo dónde vive mientras se edita.
 */

@Composable
fun AcademicScaleScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return

    var selectedScale by rememberSaveable(current.userId) { mutableStateOf(current.gradingScale) }
    var passingInput by rememberSaveable(current.userId) {
        mutableStateOf(academicGradeInput(current.passingGrade, current.gradingScale))
    }
    var targetInput by rememberSaveable(current.userId) {
        mutableStateOf(academicGradeInput(current.targetAverage, current.gradingScale))
    }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingScaleChange by rememberSaveable { mutableStateOf<GradingScaleChangeImpact?>(null) }
    var confirmingScaleChange by rememberSaveable { mutableStateOf<GradingScaleChangeImpact?>(null) }

    val maxGrade = if (selectedScale == GradingScale.CUSTOM) {
        current.customGradeMax.coerceIn(1.0, 100.0)
    } else {
        GradingScaleUtils.maxGradeFor(selectedScale)
    }
    val passing = passingInput.toDoubleOrNull()
    val target = targetInput.toDoubleOrNull()
    val scaleIsValid = passing != null && target != null &&
        passing in 0.0..maxGrade && target in 0.0..maxGrade && target >= passing

    fun saveScale() {
        val impact = viewModel.gradingScaleChangeImpact()
        if (selectedScale != current.gradingScale && impact.isDestructive) {
            pendingScaleChange = impact
        } else {
            feedback = if (viewModel.updateGradingSettings(selectedScale, passingInput, targetInput)) {
                "Escala actualizada."
            } else {
                "Revisa que las notas estén dentro de la escala."
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Escala y metas",
                subtitle = "Cómo se convierten tus notas",
                onBackClick = onBackClick
            )
        }
        item { ScaleZoneBar(max = maxGrade, passing = passing, target = target) }
        item {
            AcademicGroupLabel("LA ESCALA")
            UniSegmentedControl(
                selected = selectedScale,
                options = listOf(
                    GradingScale.ZERO_TO_FIVE,
                    GradingScale.ZERO_TO_HUNDRED,
                    GradingScale.CUSTOM
                ).map { scale -> UniSegmentedOption(value = scale, label = scale.shortLabel()) },
                onSelected = { scale ->
                    selectedScale = scale
                    val newMax = if (scale == GradingScale.CUSTOM) current.customGradeMax else GradingScaleUtils.maxGradeFor(scale)
                    passingInput = academicGradeInput(newMax * 0.6, scale)
                    targetInput = academicGradeInput(newMax * 0.8, scale)
                    feedback = null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            GradeStepperRow(
                label = "Apruebas con",
                value = passingInput,
                max = maxGrade,
                floorValue = 0.0,
                ceilingValue = target ?: maxGrade,
                filled = false,
                onValueChange = { passingInput = it; feedback = null }
            )
        }
        item {
            GradeStepperRow(
                label = "Tu meta",
                value = targetInput,
                max = maxGrade,
                floorValue = passing ?: 0.0,
                ceilingValue = maxGrade,
                filled = true,
                onValueChange = { targetInput = it; feedback = null }
            )
        }
        item {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = ::saveScale,
                enabled = scaleIsValid,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
            ) {
                Text("Guardar escala")
            }
        }
        item { ScaleWarningNote() }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (message.startsWith("Revisa")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    pendingScaleChange?.let { impact ->
        AlertDialog(
            onDismissRequest = { pendingScaleChange = null },
            title = { Text("¿Cambiar la escala de notas?") },
            text = {
                Text(
                    "Esto borrará ${impact.describe()}. Una nota registrada en otra escala " +
                        "no se puede reexpresar sin inventar el número, así que se elimina en " +
                        "vez de convertirse. Las metas de tus materias vuelven al valor del perfil."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingScaleChange = null
                        confirmingScaleChange = impact
                    }
                ) {
                    Text("Continuar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingScaleChange = null }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }

    confirmingScaleChange?.let { impact ->
        AlertDialog(
            onDismissRequest = { confirmingScaleChange = null },
            title = { Text("Esto no se puede deshacer") },
            text = {
                Text(
                    "Vas a borrar ${impact.describe()} de forma permanente. No hay copia " +
                        "de seguridad ni forma de recuperarlas después."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingScaleChange = null
                        feedback = if (viewModel.updateGradingSettings(selectedScale, passingInput, targetInput)) {
                            "Escala actualizada. Se borraron ${impact.describe()}."
                        } else {
                            "Revisa que las notas estén dentro de la escala."
                        }
                    }
                ) {
                    Text("Sí, borrar definitivamente", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingScaleChange = null }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
fun AcademicCutsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val term by viewModel.activeTerm.collectAsStateWithLifecycle()

    var weights by rememberSaveable(current.userId) {
        mutableStateOf(current.gradingCutScheme.cuts.map { academicPercentInput(it.weight) })
    }
    var cutDates by rememberSaveable(current.userId, stateSaver = CutDatesSaver) {
        mutableStateOf(
            CutDateRules.resize(
                current.gradingCutScheme.cuts.sortedBy { it.order }.map { corte ->
                    corte.endEpochDay?.let(LocalDate::ofEpochDay)
                },
                current.gradingCutScheme.cuts.size
            )
        )
    }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    val total = weights.sumOf { setupPercentValue(it) }
    val weightsAreValid = kotlin.math.abs(total - 100.0) < 0.01
    val dateProblem = CutDateRules.problemFor(
        cutEndDates = cutDates,
        cutCount = weights.size,
        termStart = term?.start,
        termPlannedEnd = term?.plannedEnd
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Tus cortes",
                subtitle = "Cómo se reparte el peso del semestre",
                onBackClick = onBackClick
            )
        }
        item {
            AcademicGroupLabel("TUS CORTES")
            CutCountSection(
                count = weights.size,
                onCountSelected = { count ->
                    weights = academicWeightsFor(count, weights)
                    cutDates = CutDateRules.resize(cutDates, count)
                    feedback = null
                }
            )
        }
        item {
            CutWheelCard(
                weights = weights,
                total = total,
                isValid = weightsAreValid,
                onWeightChange = { index, value ->
                    weights = weights.mapIndexed { position, currentValue ->
                        if (position == index) value else currentValue
                    }
                    feedback = null
                }
            )
        }
        item {
            CutBalanceNotice(
                total = total,
                remaining = (100.0 - total).coerceAtLeast(0.0),
                isValid = weightsAreValid
            )
        }
        item {
            SetupEvenSplitAction(count = weights.size) { split ->
                weights = split
                feedback = null
            }
        }
        if (weights.size > 1) {
            item {
                AcademicGroupLabel("CUÁNDO CIERRA CADA ${Corte.Singular.uppercase()}")
                CutDatesExplainer(hasDates = cutDates.any { it != null })
            }
            item {
                CutDatesSection(
                    termStart = term?.start,
                    termPlannedEnd = term?.plannedEnd,
                    cutWeights = weights,
                    cutEndDates = cutDates,
                    onCutDateChange = { indice, fecha ->
                        cutDates = cutDates.mapIndexed { posicion, actual ->
                            if (posicion == indice) fecha else actual
                        }
                        feedback = null
                    }
                )
            }
            dateProblem?.let { problema ->
                item { CutDatesProblemNote(problema) }
            }
            if (cutDates.any { it != null }) {
                item {
                    TextButton(
                        onClick = {
                            cutDates = cutDates.map { null }
                            feedback = null
                        }
                    ) {
                        Text("Quitar las fechas", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        item {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    feedback = if (viewModel.updateGradingCutSettings(weights, cutDates)) {
                        "Cortes actualizados."
                    } else {
                        "Revisa que los pesos sumen 100%."
                    }
                },
                enabled = weightsAreValid && dateProblem == null,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
            ) {
                Text("Guardar cortes")
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (message.startsWith("Revisa")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Un tope, dicho a lo grande.
 *
 * Era una tarjeta que abría un diálogo encima de todo. Aquí es su propia pantalla: mismo
 * stepper de siempre, pero sin el cuadro apretándolo por los bordes.
 */
@Composable
fun AcademicAbsenceScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val haptics = LocalHapticFeedback.current

    var valor by rememberSaveable(current.userId) { mutableStateOf(current.absenceLimit ?: 6) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Faltas",
                subtitle = "Tu tope de inasistencias",
                onBackClick = onBackClick
            )
        }
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = valor.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.displayLargeEmphasized
                )
                Text(
                    text = "faltas y pierdes la materia",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(26.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(28.dp), verticalAlignment = Alignment.CenterVertically) {
                    AbsenceStepButton(Icons.Rounded.Remove, "Una menos", valor > 1) {
                        haptics.performSafely(HapticFeedbackType.SegmentTick)
                        valor = (valor - 1).coerceAtLeast(1)
                        feedback = null
                    }
                    AbsenceStepButton(Icons.Rounded.Add, "Una más", valor < 40) {
                        haptics.performSafely(HapticFeedbackType.SegmentTick)
                        valor = (valor + 1).coerceAtMost(40)
                        feedback = null
                    }
                }
            }
        }
        item {
            Text(
                text = "El número a partir del cual pierdes una materia. Lo dice el reglamento " +
                    "de tu universidad, y vale para todas tus materias.",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        item {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    viewModel.setAbsenceLimit(valor)
                    feedback = "Tope actualizado."
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = UniStackButtonDefaults.PrimaryHeight)
            ) {
                Text("Guardar")
            }
        }
        if (current.absenceLimit != null) {
            item {
                TextButton(
                    onClick = {
                        viewModel.setAbsenceLimit(null)
                        valor = 6
                        feedback = "Quitaste el tope."
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Quitar el tope", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    color = LocalSectionColors.current.onTrack,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun AbsenceStepButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = active,
        modifier = Modifier.size(52.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = description,
                tint = if (active) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier.size(23.dp)
            )
        }
    }
}

@Composable
fun AcademicBreaksScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val breaks by viewModel.academicBreaks.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Días sin clase",
                subtitle = "Festivos y semanas sin clase",
                onBackClick = onBackClick
            )
        }
        item {
            AcademicBreaksSection(
                breaks = breaks,
                onSave = { id, nombre, desde, hasta -> viewModel.saveAcademicBreak(id, nombre, desde, hasta) },
                onDelete = viewModel::deleteAcademicBreak
            )
        }
    }
}
