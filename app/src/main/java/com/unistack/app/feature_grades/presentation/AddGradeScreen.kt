package com.unistack.app.feature_grades.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.components.SquishyButton

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.components.rememberLeaveGuard
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_user.domain.AcademicPeriod
import com.unistack.app.feature_user.domain.GradingScale

import com.unistack.app.core.design.theme.LocalIsDarkTheme
private val FormCardShape = AppShapes.SmallCard
private val FormFieldColor: Color
    @Composable get() = if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceContainerLow

private val DisabledButtonColor: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

/**
 * Los colores de los campos, en un sitio.
 *
 * Estaban copiados tres veces con catorce líneas cada uno, así que cualquier ajuste había que
 * hacerlo tres veces y el del peso ya se había quedado sin el color de etiqueta.
 */
@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FormFieldColor,
    unfocusedContainerColor = FormFieldColor,
    disabledContainerColor = FormFieldColor,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)

/** Los tipos de actividad y su rótulo, para no repetir la lista en dos sitios. */
private val ActivityTypeLabels = listOf(
    "Taller" to GradeType.WORKSHOP,
    "Exposición" to GradeType.PRESENTATION,
    "Quiz" to GradeType.QUIZ,
    "Parcial" to GradeType.EXAM,
    "Proyecto" to GradeType.PROJECT,
    "Investigación" to GradeType.RESEARCH,
    "Práctica" to GradeType.PRACTICE,
    "Otra" to GradeType.OTHER
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGradeScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    gradeId: String? = null,
    initialPeriodId: String? = null,
    onCompleteHistoryClick: (String) -> Unit = {}
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val grade = gradeId?.let { id -> subject?.grades?.firstOrNull { it.id == id } }
    val isEditing = gradeId != null
    var name by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }
    var percentage by remember { mutableStateOf("") }
    // Nulo de entrada: el bloque se titula «(opcional)» y llegaba con «Taller» marcado, así
    // que quien no tocaba nada guardaba un taller sin haberlo dicho.
    var selectedType by remember { mutableStateOf<GradeType?>(null) }
    var selectedPeriodId by remember { mutableStateOf(initialPeriodId.orEmpty()) }
    var selectedSource by remember { mutableStateOf(GradeSource.ACTIVITY) }
    var weightUnknown by remember { mutableStateOf(false) }
    var initialized by remember(subjectId, gradeId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showHistorySuggestion by remember { mutableStateOf(false) }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: GradingScaleUtils.maxGradeFor(scale)
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val periodScheme = subject?.periodScheme
        ?: profile?.academicPeriodScheme
        ?: com.unistack.app.feature_user.domain.AcademicPeriodScheme.default()
    val lockedPeriod = initialPeriodId?.let { id -> periodScheme.periods.firstOrNull { it.id == id } }
    val selectedPeriod = periodScheme.periods.firstOrNull { it.id == selectedPeriodId }
        ?: lockedPeriod
        ?: periodScheme.periods.firstOrNull { it.id == subject?.activePeriodId }
        ?: periodScheme.periods.first()

    val hasUnsavedChanges = if (grade == null) {
        name.isNotBlank() || value.isNotBlank() || percentage.isNotBlank() || selectedType != null
    } else {
        name != grade.name ||
            value != GradingScaleUtils.formatGrade(grade.value, scale) ||
            selectedType != grade.type
    }
    val requestLeave = rememberLeaveGuard(
        hasUnsavedChanges = hasUnsavedChanges,
        onLeave = onBackClick,
        message = if (grade == null) {
            "La nota no se ha registrado todavía."
        } else {
            "Los cambios de esta nota se van a perder."
        }
    )

    val gradeValue = value.toDoubleOrNull()
    val percentageValue = percentage.toDoubleOrNull()
    val currentPercentage = subject?.grades
        ?.filterNot { it.id == gradeId }
        ?.filter {
            it.periodId == selectedPeriod.id &&
                it.source == GradeSource.ACTIVITY &&
                it.weightStatus == GradeWeightStatus.KNOWN
        }
        ?.sumOf { it.percentage } ?: 0.0
    val totalPercentage = currentPercentage +
        if (selectedSource == GradeSource.ACTIVITY && !weightUnknown) {
            (percentageValue ?: 0.0) / 100.0
        } else {
            0.0
        }

    val nameValidation = TextValidators.validateActivityName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val isGradeValid = gradeValue != null && gradeValue in 0.0..maxGrade
    val isPercentageValid = selectedSource == GradeSource.PERIOD_FINAL ||
        weightUnknown ||
        (percentageValue != null && percentageValue > 0.0 && totalPercentage <= 1.00001)

    val isValid = subject != null &&
        (!isEditing || grade != null) &&
        name.isNotBlank() &&
        nameValidation.isValid &&
        isGradeValid &&
        isPercentageValid

    LaunchedEffect(grade?.id, subjectId, gradeId) {
        if (initialized) return@LaunchedEffect
        if (grade != null) {
            name = grade.name
            value = GradingScaleUtils.formatGrade(grade.value, scale)
            percentage = String.format(java.util.Locale.US, "%.0f", grade.percentage * 100)
            selectedType = grade.type
            selectedPeriodId = grade.periodId
            selectedSource = grade.source
            weightUnknown = grade.weightStatus == GradeWeightStatus.UNKNOWN
            initialized = true
        } else if (!isEditing) {
            selectedPeriodId = lockedPeriod?.id ?: subject?.chosenPeriodId ?: periodScheme.periods.first().id
            initialized = true
        }
    }

    val remainingWeight = ((1.0 - currentPercentage) * 100.0).coerceAtLeast(0.0)
    // El nombre que se pone solo cuando registras la nota final del corte. Se compara
    // con lo escrito para saber si sigue siendo automático o si el usuario lo cambió.
    val periodFinalName = "Resultado final ${periodDisplayName(selectedPeriod)}"
    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .dismissKeyboardOnTapOutside()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 10.dp, bottom = saveBarHeight + scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                IconButton(
                    onClick = requestLeave,
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.52f), AppShapes.Pill)
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = if (isEditing) "Editar nota" else "Nueva nota",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    // En secundario y no en verde: aquel verde no significaba nada, era el
                    // color que había a mano.
                    text = listOfNotNull(
                        subject?.name,
                        periodDisplayName(selectedPeriod),
                        "${formatPercent(selectedPeriod.weight * 100)}% de la materia"
                    ).joinToString("  ·  "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            FormBlock(
                title = "Qué registras",
                icon = Icons.AutoMirrored.Rounded.Assignment,
                accent = MaterialTheme.colorScheme.primary
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityChip(
                        label = "Una actividad",
                        isSelected = selectedSource == GradeSource.ACTIVITY,
                        onClick = {
                            selectedSource = GradeSource.ACTIVITY
                            // Al volver aquí se retira el nombre que puso la otra opción. Se
                            // quedaba puesto, así que la actividad nacía llamándose
                            // «Resultado final Corte 1» sin que nadie lo hubiera escrito.
                            if (name == periodFinalName) name = ""
                            error = null
                        }
                    )
                    ActivityChip(
                        label = "La nota final del corte",
                        isSelected = selectedSource == GradeSource.PERIOD_FINAL,
                        onClick = {
                            selectedSource = GradeSource.PERIOD_FINAL
                            weightUnknown = false
                            percentage = "100"
                            if (name.isBlank()) name = periodFinalName
                            error = null
                        }
                    )
                }
                Text(
                    if (selectedSource == GradeSource.PERIOD_FINAL) {
                        "Sustituye el cálculo del corte por la nota que puso el profesor."
                    } else {
                        "Se combina con las demás según el peso que tenga dentro del corte."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
                if (!isEditing && lockedPeriod == null) {
                    Text(
                        text = "Corte",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        periodScheme.periods.sortedBy { it.order }.forEach { period ->
                            ActivityChip(
                                label = periodDisplayName(period),
                                isSelected = selectedPeriod.id == period.id,
                                onClick = {
                                    selectedPeriodId = period.id
                                    error = null
                                }
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(50)
                        error = null
                    },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej. Taller 2, Parcial de mitad…") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormCardShape,
                    isError = !isNameValid,
                    colors = formFieldColors(),
                    supportingText = {
                        if (!isNameValid) {
                            Text(
                                nameValidation.errorMessage ?: "Ingresa un nombre de actividad válido",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                if (selectedSource == GradeSource.ACTIVITY) {
                    Text(
                        text = "Tipo (opcional)",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ActivityTypeLabels.forEach { (label, type) ->
                            ActivityChip(
                                label = label,
                                isSelected = selectedType == type,
                                onClick = {
                                    // El tipo ya no pisa el nombre escrito. Antes lo
                                    // sobreescribía siempre: escribías «Parcial 2», tocabas
                                    // «Quiz» y la actividad pasaba a llamarse «Quiz».
                                    if (name.isBlank() || ActivityTypeLabels.any { it.first == name }) {
                                        name = label
                                    }
                                    selectedType = type
                                    error = null
                                }
                            )
                        }
                    }
                }
            }

            FormBlock(
                title = "Cuánto vale",
                icon = Icons.Rounded.BarChart,
                accent = MaterialTheme.colorScheme.primary
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        error = null
                    },
                    label = { Text("Nota obtenida") },
                    placeholder = { Text("0") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = FormCardShape,
                    isError = value.isNotBlank() && !isGradeValid,
                    trailingIcon = {
                        Text(
                            text = "/ $maxGradeLabel",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    colors = formFieldColors()
                )

                if (selectedSource == GradeSource.ACTIVITY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "No conozco el peso",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "La nota queda registrada y no entra en el cálculo hasta que le pongas peso.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Switch(
                            checked = weightUnknown,
                            onCheckedChange = {
                                weightUnknown = it
                                error = null
                            }
                        )
                    }
                }

                if (selectedSource == GradeSource.ACTIVITY && !weightUnknown) {
                    OutlinedTextField(
                        value = percentage,
                        onValueChange = {
                            percentage = it
                            error = null
                        },
                        label = { Text("Peso dentro de ${periodDisplayName(selectedPeriod)}") },
                        placeholder = { Text("0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = FormCardShape,
                        isError = percentage.isNotBlank() && !isPercentageValid,
                        trailingIcon = {
                            Text(
                                text = "%",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                        },
                        supportingText = {
                            // Antes decía «la suma de pesos debe ser 100%», que es la regla
                            // pero no el dato: la pantalla sabe cuánto queda libre y no lo
                            // decía, así que había que ir a mirarlo a otra parte.
                            Text(
                                text = when {
                                    percentage.isNotBlank() && !isPercentageValid ->
                                        "Te pasas del 100%: en ${periodDisplayName(selectedPeriod)} solo queda " +
                                            "${formatPercent(remainingWeight)}% por repartir."
                                    remainingWeight <= 0.05 ->
                                        "${periodDisplayName(selectedPeriod)} ya tiene repartido el 100%."
                                    else ->
                                        "Queda ${formatPercent(remainingWeight)}% por repartir en " +
                                            "${periodDisplayName(selectedPeriod)}."
                                },
                                color = if (percentage.isNotBlank() && !isPercentageValid) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        colors = formFieldColors()
                    )
                }
            }

            error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        // Anclada, como en el resto de formularios de la app. Antes se desplazaba con el
        // contenido y sin margen de teclado: con targetSdk 36 la ventana ya no se
        // redimensiona, así que al escribir el peso el botón quedaba debajo del teclado.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { saveBarHeight = with(density) { it.height.toDp() } },
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            SquishyButton(
                onClick = {
                    val editingGradeId = gradeId
                    var shouldShowHistory = false
                    val saved = if (editingGradeId != null) {
                        viewModel.updateGrade(
                            subjectId = subjectId,
                            gradeId = editingGradeId,
                            name = TextValidators.normalizeText(name),
                            value = gradeValue ?: 0.0,
                            percentageInput = percentageValue ?: 0.0,
                            type = selectedType ?: GradeType.OTHER,
                            periodId = selectedPeriod.id,
                            source = selectedSource,
                            weightStatus = if (weightUnknown) {
                                GradeWeightStatus.UNKNOWN
                            } else {
                                GradeWeightStatus.KNOWN
                            }
                        )
                    } else {
                        val outcome = viewModel.saveGrade(
                            subjectId = subjectId,
                            name = TextValidators.normalizeText(name),
                            value = gradeValue ?: 0.0,
                            percentageInput = percentageValue ?: 0.0,
                            type = selectedType ?: GradeType.OTHER,
                            periodId = selectedPeriod.id,
                            source = selectedSource,
                            weightStatus = if (weightUnknown) {
                                GradeWeightStatus.UNKNOWN
                            } else {
                                GradeWeightStatus.KNOWN
                            }
                        )
                        if (outcome.saved && outcome.suggestPriorHistory) {
                            shouldShowHistory = true
                        }
                        outcome.saved
                    }
                    if (saved) {
                        if (shouldShowHistory) {
                            showHistorySuggestion = true
                        } else {
                            onBackClick()
                        }
                    } else {
                        error = "Revisa que la nota esté entre 0 y $maxGradeLabel y que el peso acumulado no supere 100% en ${periodDisplayName(selectedPeriod)}."
                    }
                },
                enabled = isValid,
                shape = AppShapes.LargeCard,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = DisabledButtonColor,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = if (isEditing) "Guardar cambios" else "Guardar nota",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isValid) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }


    if (showHistorySuggestion) {
        AlertDialog(
            onDismissRequest = {
                viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                showHistorySuggestion = false
                onBackClick()
            },
            title = { Text("Completa tu historial cuando puedas") },
            text = {
                Text(
                    "La nota ya quedó guardada. Agregar los cortes anteriores hará más precisas tus metas y proyecciones."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        showHistorySuggestion = false
                        onCompleteHistoryClick(subjectId)
                    }
                ) { Text("Completar historial") }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        showHistorySuggestion = false
                        onBackClick()
                    }
                ) { Text("Más tarde") }
            }
        )
    }
}

@Composable
private fun ActivityChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .bounceClick(onClick)
            .background(
                color = if (isSelected) MaterialTheme.colorScheme.primary else FormFieldColor,
                shape = FormCardShape
            )
            .border(
                width = 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                shape = FormCardShape
            )
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

private fun periodDisplayName(period: AcademicPeriod): String = "Corte ${period.order}"

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.0f", value)
