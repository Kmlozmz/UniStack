@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.SettingsHeader
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.GradeStepperRow
import com.unistack.app.core.design.components.CutBalanceNotice
import com.unistack.app.core.design.components.CutCountSection
import com.unistack.app.core.design.components.CutWheelCard
import com.unistack.app.core.design.components.ScaleZoneBar
import com.unistack.app.core.design.components.SetupEvenSplitAction
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.components.setupPercentValue
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.GradingScale
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * Tu semestre: la escala con la que se califica y cómo se reparte el peso entre cortes.
 *
 * Es la misma información que pide el onboarding y ahora también las mismas piezas: la franja
 * de tres tramos, las notas con pasos y la rueda del reparto. Antes aquí había dos campos de
 * texto y una lista de porcentajes escritos a mano —el mismo dato, con el teclado encima de
 * media pantalla y un aviso rojo cada vez que se salía de rango.
 */
@Composable
fun AcademicSettingsScreen(
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
    var weights by rememberSaveable(current.userId) {
        mutableStateOf(current.gradingCutScheme.cuts.map { academicPercentInput(it.weight) })
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

    val total = weights.sumOf { setupPercentValue(it) }
    val weightsAreValid = kotlin.math.abs(total - 100.0) < 0.01

    fun saveScale() {
        val impact = viewModel.gradingScaleChangeImpact()
        // El aviso solo aparece si de verdad hay algo que perder. Sacarlo siempre —lo normal
        // es cambiar de escala recién salido del setup, sin una sola nota— enseña a cerrar
        // diálogos sin leerlos, y entonces deja de servir el día que sí importa.
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
                title = "Configuración académica",
                subtitle = "Escala, metas y cortes",
                onBackClick = onBackClick
            )
        }
        item {
            ScaleZoneBar(max = maxGrade, passing = passing, target = target)
        }
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
                onValueChange = {
                    passingInput = it
                    feedback = null
                }
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
                onValueChange = {
                    targetInput = it
                    feedback = null
                }
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
        item {
            ScaleWarningNote()
        }
        item {
            AcademicGroupLabel("TUS CORTES")
            CutCountSection(
                count = weights.size,
                onCountSelected = { count ->
                    weights = academicWeightsFor(count, weights)
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
        item {
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    feedback = if (viewModel.updateGradingCutSettings(weights)) {
                        "Cortes actualizados."
                    } else {
                        "Revisa que los pesos sumen 100%."
                    }
                },
                enabled = weightsAreValid,
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

    pendingScaleChange?.let { impact ->
        AlertDialog(
            onDismissRequest = { pendingScaleChange = null },
            title = { Text("¿Cambiar la escala de notas?") },
            // El conteo va en el texto a propósito: «perderás tus notas» se descarta sin
            // leer, «borrará 23 notas en 4 materias» hace parar.
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

    // Segundo paso. El primero explica y da contexto; este solo pregunta si de verdad, y es
    // deliberadamente escueto: si repitiera el razonamiento se leería como el mismo diálogo
    // dos veces y se cerraría por inercia. Aquí lo único nuevo es que no hay vuelta atrás.
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
private fun AcademicGroupLabel(text: String) {
    Text(
        text = text,
        style = SectionLabelStyle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 10.dp, bottom = 9.dp)
    )
}

/**
 * El aviso de la escala, como nota al pie.
 *
 * En bloque rojo relleno gritaba cada vez que se entraba, y lo que dice no es un error: es una
 * advertencia permanente sobre algo que solo pasa si tocas la escala. El icono y las tres
 * palabras que importan llevan el color; el resto, no.
 */
@Composable
private fun ScaleWarningNote() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(11.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Rounded.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(17.dp)
        )
        Text(
            text = buildScaleWarning(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun buildScaleWarning() = androidx.compose.ui.text.buildAnnotatedString {
    append("Cambiar la escala ")
    pushStyle(
        androidx.compose.ui.text.SpanStyle(
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
    )
    append("borra todas tus notas")
    pop()
    append(". Continua solo si estás seguro de lo que haces.")
}

private fun GradingScale.shortLabel(): String = when (this) {
    GradingScale.ZERO_TO_FIVE -> "0 a 5"
    GradingScale.ZERO_TO_HUNDRED -> "0 a 100"
    GradingScale.CUSTOM -> "Otra"
}

private fun academicGradeInput(value: Double, scale: GradingScale): String =
    GradingScaleUtils.formatGrade(value, scale)

private fun academicPercentInput(weight: Double): String {
    val percent = weight * 100.0
    return if (kotlin.math.abs(percent - kotlin.math.round(percent)) < 0.01) {
        kotlin.math.round(percent).toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", percent)
    }
}

/**
 * Los pesos al cambiar de cantidad de cortes.
 *
 * Al subir se reparte a partes iguales y al bajar se conservan los primeros: cambiar de tres
 * a cuatro y volver a tres no debería devolver un reparto distinto del que había.
 */
private fun academicWeightsFor(count: Int, current: List<String>): List<String> {
    if (count <= 0) return current
    if (count <= current.size) return current.take(count)
    val even = 100.0 / count
    return List(count) { academicPercentInput(even / 100.0) }
}
