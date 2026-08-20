@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.unistack.app.core.utils.toColorIntOrNull
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SubjectColorPalette
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.components.rememberLeaveGuard
import com.unistack.app.core.utils.NO_DATA
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.SubjectVisualType
import com.unistack.app.feature_user.domain.AcademicPeriodScheme
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate

import com.unistack.app.core.design.theme.contentColorOn
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
/**
 * De dónde se entra al formulario de materia.
 *
 * Es la misma pantalla en los dos casos: lo único que cambia es qué bloque llega abierto y si
 * el horario se puede apagar. Antes eran dos formularios distintos para la misma entidad
 * —«Agregar materia» desde Académico y el diálogo «Nueva materia» desde Horario— y divergían
 * en todo lo que nadie sincronizaba a mano: la paleta, las horas por defecto, el sitio del
 * botón de guardar y el límite del plan gratis, que uno comprobaba y el otro no.
 */
enum class SubjectFormMode {
    /** Desde Académico: la materia es el asunto, y el bloque académico llega abierto. */
    ACADEMIC,

    /** Desde Horario: la clase es el asunto. El bloque académico llega plegado. */
    SCHEDULE
}

@Composable
fun SubjectFormScreen(
    onBackClick: () -> Unit,
    onSubjectSaved: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    subjectId: String? = null,
    mode: SubjectFormMode = SubjectFormMode.ACADEMIC,
    onUpgradeClick: () -> Unit = {}
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: com.unistack.app.feature_user.domain.GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val defaultAverage = profile?.targetAverage ?: 4.0
    val isEditing = subjectId != null
    val subject = subjectId?.let { id -> subjects.firstOrNull { it.id == id } }
    val subjectSchedule = subjectId?.let { id -> classSessions.firstOrNull { it.subjectId == id } }
    val defaultPeriodScheme = subject?.periodScheme ?: profile?.academicPeriodScheme ?: AcademicPeriodScheme.default()
    val userPlan = FeatureGate.planFor(isPro = false)
    val freeLimitReached = !isEditing && !FeatureGate.canCreateSubject(userPlan, subjects.size)
    // Desde Horario no tiene sentido guardar una materia sin clase: es justo lo que se venía
    // a crear. El interruptor solo aparece en la ruta académica.
    val scheduleIsOptional = mode == SubjectFormMode.ACADEMIC

    // El acento de cada tipo se resuelve aqui, en contexto composable. Dentro de
    // remember o LaunchedEffect no se puede leer el tema, y antes se leia porque el
    // color no venia del tema sino de un objeto global.
    val accentArgbByType = SubjectVisualType.entries.associateWith { subjectAccent(it).toArgb() }

    var name by remember { mutableStateOf("") }
    var targetAverage by remember { mutableStateOf("") }
    var visualType by remember { mutableStateOf(SubjectVisualType.TEAL) }
    var customColor by remember { mutableStateOf<Int?>(accentArgbByType.getValue(SubjectVisualType.TEAL)) }
    // Vacío mientras nadie lo elija. En edición se carga el que ya tuviera la materia,
    // para no borrar una elección hecha desde su pantalla.
    var activePeriodId by remember { mutableStateOf("") }
    var scheduleDraft by remember(subjectId) { mutableStateOf(defaultSubjectScheduleDraft()) }
    var scheduleInitialized by remember(subjectId) { mutableStateOf(false) }
    var initialized by remember(subjectId) { mutableStateOf(false) }
    var academicExpanded by rememberSaveable(mode) { mutableStateOf(mode == SubjectFormMode.ACADEMIC) }
    var error by remember { mutableStateOf<String?>(null) }
    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val formScrollState = rememberScrollState()
    val spacing = LocalInterfaceSpacing.current
    // Al crear, cualquier dato escrito cuenta. Al editar, solo lo que se aparte de la materia
    // guardada: el color y el horario se cargan solos y preguntarían por cambios de nadie.
    val hasUnsavedChanges = if (subject == null) {
        name.isNotBlank() || targetAverage.isNotBlank() ||
            scheduleDraft.professor.isNotBlank() || scheduleDraft.room.isNotBlank() ||
            scheduleDraft.daysOfWeek.isNotEmpty()
    } else {
        name != subject.name ||
            targetAverage != GradingScaleUtils.formatGrade(subject.targetAverage, scale) ||
            customColor != subject.customColor
    }
    val requestLeave = rememberLeaveGuard(
        hasUnsavedChanges = hasUnsavedChanges,
        onLeave = onBackClick,
        message = if (subject == null) {
            "La materia no se ha creado todavía."
        } else {
            "Los cambios de esta materia se van a perder."
        }
    )

    val targetValue = targetAverage.toDoubleOrNull()
    val nameValidation = TextValidators.validateSubjectName(name)
    val isNameValid = name.isBlank() || nameValidation.isValid
    val canEditLoadedSubject = !isEditing || subject != null
    val accent = customColor?.let(::Color) ?: subjectAccent(visualType)
    // La meta bloquea el guardado, así que el bloque académico no puede quedarse plegado
    // escondiéndola: el botón se apagaría sin que se vea por qué. Se condiciona a que el
    // formulario ya esté cargado para que no se despliegue en el primer fotograma, cuando el
    // campo todavía está vacío porque nadie lo ha rellenado aún.
    val targetBlocksSave = initialized && (targetValue == null || targetValue !in 0.0..maxGrade)
    val isValid = canEditLoadedSubject &&
        !freeLimitReached &&
        nameValidation.isValid &&
        targetValue != null &&
        targetValue in 0.0..maxGrade &&
        scheduleDraft.isValid

    LaunchedEffect(subject?.id, defaultAverage, scale, subjectId) {
        if (initialized) return@LaunchedEffect

        if (subject != null) {
            name = subject.name
            targetAverage = GradingScaleUtils.formatGrade(subject.targetAverage, scale)
            visualType = subject.visualType
            customColor = subject.customColor ?: accentArgbByType.getValue(subject.visualType)
            activePeriodId = subject.activePeriodId
            initialized = true
        } else if (!isEditing) {
            targetAverage = GradingScaleUtils.formatGrade(defaultAverage, scale)
            customColor = accentArgbByType.getValue(visualType)
            activePeriodId = ""
            initialized = true
        }
    }

    LaunchedEffect(subject?.id, subjectSchedule?.id, subjectId) {
        when {
            subjectSchedule != null -> {
                scheduleDraft = subjectSchedule.toSubjectScheduleDraft()
                scheduleInitialized = true
            }
            !scheduleInitialized && isEditing && subject != null -> {
                scheduleDraft = defaultSubjectScheduleDraft().copy(enabled = !scheduleIsOptional)
                scheduleInitialized = true
            }
            !scheduleInitialized && !isEditing -> {
                scheduleDraft = defaultSubjectScheduleDraft()
                scheduleInitialized = true
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .dismissKeyboardOnTapOutside()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(formScrollState)
                .statusBarsPadding()
                .padding(horizontal = spacing.screenHorizontal)
                .padding(top = 10.dp, bottom = saveBarHeight + scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(spacing.section)
        ) {
            SubjectFormHeader(
                title = if (isEditing) "Editar materia" else "Agregar materia",
                subtitle = when {
                    name.isNotBlank() -> name
                    mode == SubjectFormMode.SCHEDULE -> "Se añadirá a tu horario"
                    else -> "Configura tu materia"
                },
                accent = accent,
                initial = name.trim().firstOrNull()?.uppercaseChar(),
                onBackClick = requestLeave
            )
            if (isEditing && subject == null) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = MaterialTheme.shapes.large,
                    tonalElevation = 0.dp,
                ) {
                    Text("Materia no encontrada.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (!isEditing && FeatureGate.PRO_FEATURES_ENABLED) {
                PlanBanner(
                    plan = userPlan,
                    currentSubjectCount = subjects.size,
                    onUpgradeClick = onUpgradeClick
                )
            }

            FormBlock(
                title = "Identidad",
                icon = Icons.Rounded.School,
                accent = accent,
                // El único bloque teñido con el color de la materia: es el que lo elige, y
                // así el acento se ve aplicado antes de guardar.
                tinted = true
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it.take(40)
                        error = null
                    },
                    label = { Text("Nombre") },
                    placeholder = { Text("Ej: Estadística inferencial") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    isError = !isNameValid,
                    supportingText = {
                        if (!isNameValid) {
                            Text(nameValidation.errorMessage ?: "Ingresa un nombre de materia válido")
                        }
                    }
                )
                SubjectColorField(
                    selectedColor = customColor ?: subjectAccent(visualType).toArgb(),
                    onSelected = { color ->
                        customColor = color
                        visualType = closestVisualType(Color(color), accentArgbByType)
                    }
                )
                // El profesor se guarda dentro del bloque de clase, que es donde lo aloja el
                // modelo: sin horario no hay dónde ponerlo. Se avisa en vez de perderlo en
                // silencio.
                val professorHint: (@Composable () -> Unit)? =
                    if (!scheduleDraft.enabled && scheduleDraft.professor.isNotBlank()) {
                        { Text("Sin clases en el horario no se guarda el profesor.") }
                    } else {
                        null
                    }
                OutlinedTextField(
                    value = scheduleDraft.professor,
                    onValueChange = { scheduleDraft = scheduleDraft.copy(professor = it.take(60)) },
                    label = { Text("Profesor") },
                    placeholder = { Text("Prof. Pérez") },
                    leadingIcon = { Icon(Icons.Rounded.Person, null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = professorHint
                )
            }

            FormBlock(
                title = "Cuándo",
                icon = Icons.Rounded.CalendarMonth,
                accent = accent,
                summary = scheduleDraft.whenSummary(),
                expanded = scheduleDraft.enabled,
                trailing = if (scheduleIsOptional) {
                    {
                        Switch(
                            checked = scheduleDraft.enabled,
                            onCheckedChange = { scheduleDraft = scheduleDraft.copy(enabled = it) }
                        )
                    }
                } else {
                    null
                }
            ) {
                SubjectWhenFields(
                    draft = scheduleDraft,
                    onDraftChange = {
                        scheduleDraft = it
                        error = null
                    }
                )
            }

            FormBlock(
                title = "Académico",
                icon = Icons.Rounded.AutoAwesome,
                accent = accent,
                summary = academicSummary(
                    targetAverage = targetAverage,
                    periodScheme = defaultPeriodScheme,
                    activePeriodId = activePeriodId,
                    reminderMinutes = scheduleDraft.reminderMinutes
                ),
                expanded = academicExpanded || targetBlocksSave,
                onHeaderClick = { academicExpanded = !academicExpanded }
            ) {
                OutlinedTextField(
                    value = targetAverage,
                    onValueChange = {
                        targetAverage = it
                        error = null
                    },
                    label = { Text("Meta de promedio (0 a $maxGradeLabel)") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                    // También en blanco: sin meta no se puede guardar, y marcarlo solo cuando
                    // hay algo escrito dejaba el campo vacío con aspecto de correcto.
                    isError = targetBlocksSave
                )
                // El corte en el que va la materia se pregunta en su pantalla, no aquí. Al
                // crearla no hay con qué juzgarlo —ni cortes con notas ni nada que mirar— y
                // el selector llegaba con el primero ya marcado, así que la app elegía por el
                // usuario y luego le reclamaba el historial de unos cortes que él nunca dijo
                // haber cursado.
                if (scheduleDraft.enabled) {
                    SubjectReminderField(
                        draft = scheduleDraft,
                        onDraftChange = { scheduleDraft = it }
                    )
                }
            }

            error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
        }
        // La barra llega hasta el borde inferior de la pantalla y el margen del sistema va
        // por dentro. Con navigationBarsPadding() por fuera se levantaba entera y dejaba
        // una franja transparente debajo por la que se veía pasar el formulario al
        // desplazarse: eso era lo que se veía cortado.
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                // El hueco que el formulario deja al final sale de medir la barra, no de un
                // número a mano: la barra crece con el teclado y con la barra de gestos, y
                // los 112dp fijos de antes se quedaban cortos o sobraban según el móvil.
                .onSizeChanged { saveBarHeight = with(density) { it.height.toDp() } },
            // Opaca del todo. Al 98% el contenido se traslucía por debajo y parecía que la
            // barra estaba superpuesta sobre todo.
            color = MaterialTheme.colorScheme.background,
            shadowElevation = 8.dp
        ) {
            UniStackButton(
                text = if (isEditing) "Guardar cambios" else "Guardar materia",
                enabled = isValid,
                onClick = {
                    if (freeLimitReached) {
                        error = "Alcanzaste el límite gratis de ${userPlan.maxSubjects} materias."
                        return@UniStackButton
                    }

                    val editingSubjectId = subjectId
                    val savedSubjectId = if (editingSubjectId != null) {
                        val saved = viewModel.updateSubject(
                            subjectId = editingSubjectId,
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType,
                            customColor = customColor,
                            activePeriodId = activePeriodId
                        )
                        if (saved) editingSubjectId else null
                    } else {
                        viewModel.addSubject(
                            name = TextValidators.normalizeText(name),
                            targetAverage = targetValue ?: defaultAverage,
                            visualType = visualType,
                            customColor = customColor,
                            activePeriodId = activePeriodId
                        )?.id
                    }

                    val scheduleSaved = savedSubjectId?.let { id ->
                        viewModel.saveSubjectSchedule(
                            subjectId = id,
                            draft = scheduleDraft.copy(
                                recurrenceStartEpochDay = scheduleDraft.recurrenceStartEpochDay
                                    .takeIf { it > 0L }
                                    ?: LocalDate.now().toEpochDay()
                            )
                        )
                    } ?: false

                    when {
                        savedSubjectId == null -> {
                            error = "Revisa el nombre y la meta antes de guardar."
                        }
                        !scheduleSaved -> {
                            error = "La materia se guardó, pero revisa la configuración del horario."
                        }
                        else -> {
                            scope.launch {
                                launch {
                                    snackbarHostState.showSnackbar(
                                        if (isEditing) "Materia y horario actualizados" else "Materia y horario creados"
                                    )
                                }
                                delay(650)
                                onSubjectSaved(savedSubjectId)
                            }
                        }
                    }
                },
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = spacing.screenHorizontal, vertical = 12.dp)
                    .fillMaxWidth()
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 84.dp)
        )
    }
}


/** Resumen del bloque académico, para leerlo de un vistazo cuando llega plegado. */
private fun academicSummary(
    targetAverage: String,
    periodScheme: AcademicPeriodScheme,
    activePeriodId: String,
    reminderMinutes: Int
): String {
    val period = periodScheme.periods.firstOrNull { it.id == activePeriodId }
    return buildList {
        add("Meta ${targetAverage.ifBlank { NO_DATA }}")
        if (periodScheme.periods.size > 1 && period != null) add("Corte ${period.order}")
        if (reminderMinutes > 0) add("Aviso $reminderMinutes min")
    }.joinToString("  ·  ")
}

@Composable
private fun SubjectFormHeader(
    title: String,
    subtitle: String,
    accent: Color,
    initial: Char?,
    onBackClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.52f), CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Relleno con el color elegido y no teñido al 14%: es la muestra grande de cómo
            // se verá la materia en el resto de la app.
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accent, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                if (initial != null) {
                    Text(
                        initial.toString(),
                        color = contentColorOn(accent),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                } else {
                    Icon(
                        Icons.Rounded.School,
                        contentDescription = null,
                        tint = contentColorOn(accent)
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlanBanner(
    plan: UserPlan,
    currentSubjectCount: Int,
    onUpgradeClick: () -> Unit
) {
    val remaining = FeatureGate.remainingSubjects(plan, currentSubjectCount) ?: Int.MAX_VALUE
    val limitReached = remaining == 0

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        borderColor = if (limitReached) MaterialTheme.colorScheme.error.copy(alpha = 0.36f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.14f),
        borderWidth = 0.5.dp,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (limitReached) MaterialTheme.colorScheme.error.copy(alpha = 0.18f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                        MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = if (limitReached) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (limitReached) "Límite gratis alcanzado" else "Plan ${plan.name}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = if (limitReached) {
                        "${plan.maxSubjects} materias incluidas. Pro desbloqueará materias ilimitadas."
                    } else {
                        "$currentSubjectCount de ${plan.maxSubjects} materias usadas."
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
            if (limitReached) {
                Button(
                    shapes = UniStackButtonDefaults.shapes,
                    onClick = onUpgradeClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Ver Pro", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * El color de la materia, dentro del bloque de identidad.
 *
 * Es lo que identifica la materia en toda la app —en la rejilla del horario, en las tarjetas
 * de notas, en el calendario—, así que va con las muestras a la vista y no escondido en una
 * fila diminuta al lado del profesor, que es donde estaba en la ruta de Horario.
 */
@Composable
private fun SubjectColorField(
    selectedColor: Int,
    onSelected: (Int) -> Unit
) {
    val selected = Color(selectedColor)
    var showEditor by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Color",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Text(
                selected.toHexString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
        val accentArgbByType = SubjectVisualType.entries.associateWith { subjectAccent(it).toArgb() }
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(SubjectColorPalette, key = { it.toArgb() }) { color ->
                ColorSwatch(
                    color = color,
                    label = closestVisualType(color, accentArgbByType).accessibilityLabel(),
                    selected = color.toArgb() == selectedColor,
                    onClick = { onSelected(color.toArgb()) }
                )
            }
            item {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.28f), CircleShape)
                        .clickable(
                            onClickLabel = "Abrir editor de color",
                            onClick = { showEditor = true }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.ColorLens,
                        contentDescription = "Color personalizado",
                        tint = selected,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }

    if (showEditor) {
        CustomSubjectColorDialog(
            initialColor = selectedColor,
            onDismiss = { showEditor = false },
            onApply = {
                onSelected(it)
                showEditor = false
            }
        )
    }
}

@Composable
private fun CustomSubjectColorDialog(
    initialColor: Int,
    onDismiss: () -> Unit,
    onApply: (Int) -> Unit
) {
    /*
     * El tono, la saturación y el valor son el estado; el color se deriva de ellos.
     *
     * Antes el estado era el color y el HSV se recalculaba a partir de él en cada cambio. Ese
     * viaje de ida y vuelta no es exacto —el color se guarda en enteros de 0 a 255—, así que
     * arrastrar por el lienzo movía un poco el tono, y como el gesto estaba atado al tono, se
     * cancelaba solo a mitad del arrastre: el selector se quedaba pegado. Con el HSV como
     * fuente, arrastrar la saturación no puede tocar el tono.
     */
    val initialHsv = remember(initialColor) {
        FloatArray(3).also { android.graphics.Color.colorToHSV(initialColor, it) }
    }
    var hue by remember(initialColor) { mutableStateOf(initialHsv[0]) }
    var saturation by remember(initialColor) { mutableStateOf(initialHsv[1]) }
    var value by remember(initialColor) { mutableStateOf(initialHsv[2]) }

    val workingColor = remember(hue, saturation, value) {
        android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    }
    val selected = Color(workingColor)
    var hexInput by remember(initialColor) { mutableStateOf(selected.toHexString()) }

    LaunchedEffect(workingColor) {
        hexInput = selected.toHexString()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Cancelar")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Color personalizado",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            "Arrastra el selector para ajustar el tono con precisión.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    shape = MaterialTheme.shapes.large,
                    color = selected
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 18.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            selected.toHexString(),
                            color = contentColorOn(selected),
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                SaturationValuePicker(
                    hue = hue,
                    saturation = saturation,
                    value = value,
                    onSelected = { newSaturation, newValue ->
                        saturation = newSaturation
                        value = newValue
                    }
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        "Tono",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Slider(
                        value = hue,
                        onValueChange = { hue = it },
                        valueRange = 0f..360f,
                        colors = SliderDefaults.colors(
                            thumbColor = selected,
                            activeTrackColor = selected,
                            inactiveTrackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.18f)
                        )
                    )
                }

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { input ->
                        val normalized = input.uppercase()
                            .filter { it == '#' || it in '0'..'9' || it in 'A'..'F' }
                            .take(7)
                        hexInput = normalized
                        normalized.toColorIntOrNull()?.let { typed ->
                            val parsed = FloatArray(3)
                            android.graphics.Color.colorToHSV(typed, parsed)
                            hue = parsed[0]
                            saturation = parsed[1]
                            value = parsed[2]
                        }
                    },
                    label = { Text("Hexadecimal") },
                    placeholder = { Text("#6750F5") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.weight(1f))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        shapes = UniStackButtonDefaults.shapes,
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        shapes = UniStackButtonDefaults.shapes,
                        onClick = { onApply(workingColor) },
                        colors = ButtonDefaults.buttonColors(containerColor = selected),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Aplicar",
                            color = contentColorOn(selected)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SaturationValuePicker(
    hue: Float,
    saturation: Float,
    value: Float,
    onSelected: (Float, Float) -> Unit
) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(MaterialTheme.shapes.medium)
            // Sin el tono como clave: reiniciar el bloque a mitad de un arrastre cancela el
            // gesto, y el lienzo dejaba de seguir el dedo.
            .pointerInput(Unit) {
                fun update(offset: Offset) {
                    onSelected(
                        (offset.x / size.width).coerceIn(0f, 1f),
                        (1f - offset.y / size.height).coerceIn(0f, 1f)
                    )
                }

                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = false)
                    update(down.position)
                    down.consume()

                    do {
                        val event = awaitPointerEvent()
                        event.changes.forEach { change ->
                            if (change.pressed) {
                                update(change.position)
                            }
                            change.consume()
                        }
                    } while (event.changes.any { it.pressed })
                }
            }
    ) {
        // design-tokens-ok-begin: lienzo saturación/valor del selector HSV. El blanco y el
        // negro son los ejes del espacio de color, no decisiones de marca; el cursor va en
        // blanco con contorno oscuro para verse sobre cualquier punto del lienzo.
        drawRect(
            Brush.horizontalGradient(
                listOf(Color.White, Color.hsv(hue, 1f, 1f))
            )
        )
        drawRect(Brush.verticalGradient(listOf(Color.Transparent, Color.Black)))
        val center = Offset(saturation * size.width, (1f - value) * size.height)
        drawCircle(Color.White, radius = 7.dp.toPx(), center = center, style = Stroke(2.dp.toPx()))
        drawCircle(Color.Black.copy(alpha = 0.45f), radius = 9.dp.toPx(), center = center, style = Stroke(1.dp.toPx()))
        // design-tokens-ok-end
    }
}

private fun Color.toHexString(): String = "#%06X".format(toArgb() and 0xFFFFFF)

@Composable
private fun ColorSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = if (selected) 1f else 0.82f))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f) else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                onClickLabel = "Seleccionar color $label",
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics {
                contentDescription = "Color $label"
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = contentColorOn(color))
        }
    }
}

private fun closestVisualType(color: Color, accents: Map<SubjectVisualType, Int>): SubjectVisualType {
    return SubjectVisualType.entries.minBy { type ->
        val candidate = Color(accents.getValue(type))
        val dr = candidate.red - color.red
        val dg = candidate.green - color.green
        val db = candidate.blue - color.blue
        dr * dr + dg * dg + db * db
    }
}

/**
 * Nombre en castellano del color más cercano de la paleta.
 *
 * El lector de pantalla decía «Color almohadilla 1 0 B 8 A C»: el hexadecimal es exacto y no
 * significa nada en voz alta.
 */
private fun SubjectVisualType.accessibilityLabel(): String = when (this) {
    SubjectVisualType.TEAL -> "turquesa"
    SubjectVisualType.BLUE -> "azul"
    SubjectVisualType.CORAL -> "coral"
    SubjectVisualType.PURPLE -> "morado"
    SubjectVisualType.GREEN -> "verde"
    SubjectVisualType.YELLOW -> "amarillo"
    SubjectVisualType.ROSE -> "rosa"
    SubjectVisualType.INDIGO -> "indigo"
    SubjectVisualType.ORANGE -> "naranja"
    SubjectVisualType.CYAN -> "cian"
    SubjectVisualType.LIME -> "lima"
    SubjectVisualType.SLATE -> "gris azulado"
}
