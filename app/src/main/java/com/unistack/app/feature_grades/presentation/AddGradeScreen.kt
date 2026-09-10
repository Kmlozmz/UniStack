@file:OptIn(
    ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)

package com.unistack.app.feature_grades.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

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
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.unistack.app.core.utils.performSafely
import androidx.compose.foundation.BorderStroke
import com.unistack.app.core.design.components.avisoDeError
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniSwitch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import java.time.LocalDate
import androidx.compose.runtime.saveable.rememberSaveable
import com.unistack.app.core.design.components.UniCard
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniDivider
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.dismissKeyboardOnTapOutside
import com.unistack.app.core.design.components.rememberLeaveGuard
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.PriorHistoryPromptStatus
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingScale

import com.unistack.app.core.design.theme.LocalIsDarkTheme
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
private val FormCardShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.medium
/*
 * **El campo tiene que verse contra lo que hay detras.**
 *
 * En la hoja, el fondo es `surfaceContainerLow`; el campo usaba `surfaceContainerHigh` en
 * oscuro, que a un paso de distancia son el mismo gris, y el borde iba en `outlineVariant`, que
 * en oscuro es practicamente invisible. Resultado: un campo vacio no parecia un campo, parecia
 * un hueco con una etiqueta encima.
 *
 * Dos escalones de separacion y un borde que existe. En claro el problema no se daba, asi que
 * ahi se queda como estaba.
 */
private val FormFieldColor: Color
    @Composable get() = if (LocalIsDarkTheme.current) MaterialTheme.colorScheme.surfaceContainerHighest else MaterialTheme.colorScheme.surfaceContainerLow

private val DisabledButtonColor: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh

/**
 * Los colores de los campos, en un sitio.
 *
 * Estaban copiados tres veces con catorce líneas cada uno, así que cualquier ajuste había que
 * hacerlo tres veces y el del peso ya se había quedado sin el color de etiqueta.
 */
/**
 * Una cifra que se compone tocando, no escribiendo.
 *
 * Parece un campo y se comporta como tal —tiene su etiqueta, se ilumina cuando esta activa y se
 * pone en rojo si el valor no vale— pero no pide el teclado del sistema: lo que la rellena es el
 * teclado de la propia hoja, justo debajo.
 */
@Composable
private fun CifraDeLaHoja(
    valor: String,
    sufijo: String,
    etiqueta: String,
    activo: Boolean,
    error: Boolean,
    onClick: () -> Unit
) {
    val borde = when {
        error -> MaterialTheme.colorScheme.error
        activo -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outline
    }
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = FormCardShape,
        color = FormFieldColor,
        border = BorderStroke(if (activo) 2.dp else 1.dp, borde)
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 11.dp)) {
            Text(
                etiqueta,
                color = if (activo) borde else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    valor.ifBlank { "0" },
                    color = if (valor.isBlank()) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    sufijo,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 3.dp)
                )
            }
        }
    }
}

/**
 * El teclado de la hoja: diez digitos, una coma y un borrado.
 *
 * Nada mas, porque nada mas se puede escribir aqui. El del sistema traia tabulador, idioma y
 * sugerencias —tres teclas que en un numero de dos cifras no hacen nada— y encima tapaba media
 * pantalla, que era el problema de verdad: el boton de guardar quedaba debajo y habia que
 * desplazarse a ciegas para llegar a el.
 */
@Composable
private fun TecladoDeLaHoja(
    onDigito: (String) -> Unit,
    onComa: () -> Unit,
    comaActiva: Boolean,
    onBorrar: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    val filas = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf(",", "0", "\u232B")
    )
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        filas.forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { tecla ->
                    val apagada = tecla == "," && !comaActiva
                    Surface(
                        onClick = {
                            haptics.performSafely(HapticFeedbackType.SegmentTick)
                            when (tecla) {
                                "," -> onComa()
                                "\u232B" -> onBorrar()
                                else -> onDigito(tecla)
                            }
                        },
                        enabled = !apagada,
                        modifier = Modifier.weight(1f),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    ) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 13.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                tecla,
                                color = if (apagada) {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun formFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = FormFieldColor,
    unfocusedContainerColor = FormFieldColor,
    disabledContainerColor = FormFieldColor,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
    errorBorderColor = MaterialTheme.colorScheme.error,
    focusedTextColor = MaterialTheme.colorScheme.onSurface,
    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
    errorTextColor = MaterialTheme.colorScheme.onSurface,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
)

private data class ActivityTypeItem(@StringRes val labelRes: Int, val type: GradeType)

private val ActivityTypeItems = listOf(
    ActivityTypeItem(R.string.grade_type_workshop, GradeType.WORKSHOP),
    ActivityTypeItem(R.string.grade_type_presentation, GradeType.PRESENTATION),
    ActivityTypeItem(R.string.grade_type_quiz, GradeType.QUIZ),
    ActivityTypeItem(R.string.grade_type_midterm, GradeType.EXAM),
    ActivityTypeItem(R.string.grade_type_project, GradeType.PROJECT),
    ActivityTypeItem(R.string.grade_type_research, GradeType.RESEARCH),
    ActivityTypeItem(R.string.grade_type_practice, GradeType.PRACTICE),
    ActivityTypeItem(R.string.grade_type_other, GradeType.OTHER)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddGradeScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel(),
    gradeId: String? = null,
    initialCutId: String? = null,
    onCompleteHistoryClick: (String) -> Unit = {},
    /**
     * Si va dentro de una hoja en vez de ocupar la pantalla.
     *
     * **Es lo que hace que se vean los gestos de Académico.** Registrando la nota en una
     * pantalla aparte, la materia se destruye mientras escribes y al volver nace con la nota
     * ya puesta: no hay estado anterior contra el que animar, así que ni la fila entra, ni el
     * promedio sube, ni el corte se sella. Dentro de una hoja la materia sigue viva debajo, y
     * los tres momentos ocurren donde estás mirando.
     *
     * Lo único que cambia aquí es la envoltura: dentro de la hoja no hay barra de estado que
     * esquivar, ni fondo que pintar —lo pone la hoja—, ni botón de volver, porque se sale
     * tirando de ella hacia abajo.
     */
    enHoja: Boolean = false
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
    var selectedCutId by remember { mutableStateOf(initialCutId.orEmpty()) }
    var selectedSource by remember { mutableStateOf(GradeSource.ACTIVITY) }
    var weightUnknown by remember { mutableStateOf(false) }
    var initialized by remember(subjectId, gradeId) { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showHistorySuggestion by remember { mutableStateOf(false) }

    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: GradingScaleUtils.maxGradeFor(scale)
    val maxGradeLabel = GradingScaleUtils.formatGrade(maxGrade, scale)
    val cutScheme = subject?.cutScheme
        ?: profile?.gradingCutScheme
        ?: com.unistack.app.feature_user.domain.GradingCutScheme.default()
    val lockedCut = initialCutId?.let { id -> cutScheme.cuts.firstOrNull { it.id == id } }
    /*
     * El corte sale de la fecha, cuando el esquema tiene fechas.
     *
     * Es lo que compran las fechas de corte: un paso menos, y un dato que deja de depender de
     * que el usuario acierte al elegir. Solo aplica al crear —editando manda lo que ya se
     * guardo— y solo si nadie ha fijado el corte al abrir la pantalla.
     *
     * Nulo significa «pregunta a mano»: sin fechas, aqui sale el selector de siempre.
     */
    val cutByDate = if (!isEditing && lockedCut == null) {
        cutScheme.cutForDate(LocalDate.now())
    } else {
        null
    }
    /*
     * **El teclado ya esta puesto cuando la hoja termina de subir.**
     *
     * Registrar una nota es escribir una cifra: si al abrir hay que tocar el campo antes de
     * poder escribirla, el gesto son dos toques en vez de uno. Solo dentro de la hoja —a
     * pantalla completa la hoja no sube, y robar el foco nada mas entrar tapa el formulario
     * con el teclado sin que nadie lo haya pedido— y solo al crear: editando se viene a
     * cambiar cualquiera de los campos, no forzosamente la nota.
     */
    /*
     * **Que cifra esta recibiendo lo que se teclea.**
     *
     * Con un teclado propio hace falta decir a donde van las teclas, cosa que con el del
     * sistema resolvia el foco. Empieza en la nota, que es lo primero que se escribe, y se
     * cambia tocando la otra cifra.
     */
    var campoActivo by rememberSaveable { mutableStateOf(0) }

    /*
     * **Dos pasos, y el segundo se puede saltar.**
     *
     * Once controles a la vez para lo que son dos datos: eso es lo que hacia la hoja
     * inmanejable, no el orden ni el color. Lo obligatorio —la nota y su peso— cabe entero en
     * una pantalla con el teclado abierto; el nombre y el tipo se responden de memoria y son
     * otro momento, asi que van detras.
     *
     * Y los dos interruptores que cambian las reglas de lo de arriba —«no conozco el peso» y
     * «es la nota final del corte»— viven plegados: casi nadie los toca, y cuando se tocan
     * redefinen lo que hay encima, que es justo lo que no puede quedar debajo por accidente.
     */
    var paso by rememberSaveable { mutableStateOf(1) }
    var avanzado by rememberSaveable { mutableStateOf(false) }

    var changingCut by rememberSaveable { mutableStateOf(false) }
    val selectedPeriod = cutScheme.cuts.firstOrNull { it.id == selectedCutId }
        ?: lockedCut
        ?: cutScheme.cuts.firstOrNull { it.id == subject?.activeCutId }
        ?: cutScheme.cuts.first()

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
            stringResource(R.string.grade_form_leave_create)
        } else {
            stringResource(R.string.grade_form_leave_edit)
        }
    )

    val gradeValue = value.comoNumero()
    val percentageValue = percentage.comoNumero()
    // Las notas que ya ocupan sitio en este corte. Antes solo se sumaban; ahora la lista
    // tambien se usa para pintar una pieza por cada una en la barra de abajo.
    val notasDelCorte = subject?.grades.orEmpty()
        .filterNot { it.id == gradeId }
        .filter {
            it.cutId == selectedPeriod.id &&
                it.source == GradeSource.ACTIVITY &&
                it.weightStatus == GradeWeightStatus.KNOWN
        }
    val currentPercentage = notasDelCorte.sumOf { it.percentage }
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
            selectedCutId = grade.cutId
            selectedSource = grade.source
            weightUnknown = grade.weightStatus == GradeWeightStatus.UNKNOWN
            initialized = true
        } else if (!isEditing) {
            selectedCutId = lockedCut?.id
                ?: cutByDate?.id
                ?: subject?.chosenCutId
                ?: cutScheme.cuts.first().id
            initialized = true
        }
    }

    val remainingWeight = ((1.0 - currentPercentage) * 100.0).coerceAtLeast(0.0)
    val activityTypes = ActivityTypeItems.map { stringResource(it.labelRes) to it.type }
    val validationErrorMsg = stringResource(R.string.grade_validation_error, maxGradeLabel, cutDisplayName(selectedPeriod))
    // El nombre que se pone solo cuando registras la nota final del corte. Se compara
    // con lo escrito para saber si sigue siendo automático o si el usuario lo cambió.
    val cutFinalName = stringResource(R.string.grade_final_result, cutDisplayName(selectedPeriod))
    /*
     * **Desplazar el contenido no cierra la hoja.**
     *
     * Una hoja de Material se va cuando el dedo baja y lo que hay dentro ya no tiene mas
     * recorrido: el desplazamiento sobrante se lo queda ella y lo usa para bajarse. En una
     * lista es lo que se espera; en un formulario a medio rellenar es perder lo escrito por
     * un gesto que solo pretendia subir un poco, y aqui pasaba con casi cualquier arrastre
     * porque el contenido apenas tiene recorrido que gastar.
     *
     * Esta conexion se queda con lo que sobra antes de que llegue a la hoja. Cerrarla sigue
     * siendo cosa del asa de arriba, del gesto de volver y del boton: tres formas, todas
     * deliberadas, ninguna a medio teclear una nota.
     */
    val soloElContenido = remember {
        object : NestedScrollConnection {
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset = available

            override suspend fun onPostFling(
                consumed: Velocity,
                available: Velocity
            ): Velocity = available
        }
    }
    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    /*
     * **Dentro de la hoja no hay estiron al llegar al tope.**
     *
     * Lanzando el contenido con fuerza, el desplazamiento llega al final enseguida y ahi
     * se para a la vista, pero la animacion de inercia sigue viva decayendo y Compose no
     * le entrega la velocidad sobrante al borde hasta que esa animacion termina: de ahi
     * el paron seco y, un momento despues, el estiron.
     *
     * En una pantalla normal el estirado cae a tiempo y es la senal de «se acabo» que
     * Android usa en todas partes, asi que ahi se queda. En la hoja no, porque la hoja
     * tiene su propio arrastre: el gesto se reparte entre desplazar el contenido y mover
     * la hoja entera, y el efecto de borde llega siempre a destiempo. Es el mismo motivo
     * —y la misma solucion— que en la fila de fichas del compositor de agenda.
     */
    CompositionLocalProvider(
        LocalOverscrollFactory provides if (enHoja) null else LocalOverscrollFactory.current
    ) {
    Box(
        modifier = modifier
            .then(if (enHoja) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
            .dismissKeyboardOnTapOutside()
            .then(
                if (enHoja) Modifier else Modifier.background(MaterialTheme.colorScheme.background)
            )
    ) {
        Column(
            modifier = Modifier
                .then(if (enHoja) Modifier.fillMaxWidth() else Modifier.fillMaxSize())
                .then(if (enHoja) Modifier.nestedScroll(soloElContenido) else Modifier)
                .verticalScroll(rememberScrollState())
                .then(if (enHoja) Modifier else Modifier.statusBarsPadding())
                .padding(horizontal = 20.dp)
                /*
                 * En la hoja no hay barra de navegacion debajo que esquivar.
                 *
                 * `scrollBottomRoom` son los 28 dp que toda pantalla deja para que su
                 * ultimo elemento suba por encima de la barra inferior de la app. Dentro
                 * de una hoja esa barra no existe, asi que eran 28 dp de nada que hacian
                 * el contenido mas alto que la hoja: aparecia un desplazamiento minusculo
                 * que solo servia para dar el tiron al llegar al final.
                 */
                .padding(
                    top = 10.dp,
                    bottom = saveBarHeight + if (enHoja) 8.dp else scrollBottomRoom
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!enHoja) {
                    UniBackButton(onClick = requestLeave)
                }
                Text(
                    text = if (isEditing) stringResource(R.string.grade_form_edit_title) else stringResource(R.string.grade_form_new_title),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    // En secundario y no en verde: aquel verde no significaba nada, era el
                    // color que había a mano.
                    text = listOfNotNull(
                        subject?.name,
                        cutDisplayName(selectedPeriod),
                        stringResource(R.string.subject_weight_of_subject_simple, formatPercent(selectedPeriod.weight * 100))
                    ).joinToString("  ·  "),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }

            /*
             * **Las dos partes, arriba y como pestanas.**
             *
             * El acceso al segundo paso estuvo en el pie —debajo de guardar, estorbando al
             * pulgar cada vez que ibas a la accion principal— y despues al final del contenido,
             * donde se perdia. Arriba dice **de que va cada parte**, se ve que hay dos desde el
             * primer momento, y se salta entre ellas sin buscar nada.
             *
             * Editando no hay pestanas: se viene a cambiar un campo concreto, no a recorrer un
             * alta, y partir en dos algo que ya existe solo esconde la mitad.
             */
            if (!isEditing) {
                /*
                 * **El control segmentado de la app, no dos botones hechos aqui.**
                 *
                 * Estaban escritos a mano: dos superficies rellenas, una con rotulo de dos
                 * lineas y otra de una, con su propio alto y su propio tipo de letra. Al
                 * lado de los diecinueve sitios donde la app ya elige entre dos vistas
                 * —Materias o Tareas, Horario o Calendario— se veian de otra app.
                 *
                 * `UniSegmentedControl` los deja conectados, del alto de siempre y con el
                 * toque haptico al cambiar. Lo de «opcional» se va al titulo del bloque,
                 * que es donde se lee sin partir el rotulo de la pestana en dos.
                 */
                UniSegmentedControl(
                    selected = paso,
                    options = listOf(
                        UniSegmentedOption(1, stringResource(R.string.grade_tab_grade)),
                        UniSegmentedOption(2, stringResource(R.string.grade_tab_details))
                    ),
                    onSelected = { paso = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            /*
             * **«Solo la nota final» es la respuesta a un caso, no una opcion permanente.**
             *
             * Se usa cuando te pones al dia: instalas la app a mitad de semestre, el corte 1 ya
             * cerro, y meter sus notas una a una es inviable porque no las llevabas apuntadas.
             * Ese es **el unico** momento en que hace falta.
             *
             * Como interruptor fijo estaba mal por los dos lados: se ofrecia siempre —en cada
             * nota del corte que cursas, donde no significa nada— y a la vez estaba escondido
             * en un pliegue el dia que si hacia falta. Y activarlo hacia desaparecer el otro
             * interruptor, con lo que parecia que no habia vuelta atras.
             *
             * Aqui se pregunta una vez, arriba, y solo cuando el corte de destino ya quedo
             * atras y esta vacio. Las dos respuestas son botones, asi que siempre se puede
             * cambiar de idea.
             */
            val ordenDelActual = cutScheme.cuts
                .firstOrNull { it.id == subject?.chosenCutId }?.order ?: Int.MAX_VALUE
            val corteAtrasadoYVacio = !isEditing &&
                selectedPeriod.order < ordenDelActual &&
                subject?.grades.orEmpty().none { it.cutId == selectedPeriod.id }

            if (corteAtrasadoYVacio && paso == 1) {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    shape = MaterialTheme.shapes.large,
                    contentPadding = PaddingValues(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text(
                            stringResource(R.string.grade_cut_closed_question),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ActivityChip(
                                label = stringResource(R.string.grade_mode_one_by_one),
                                isSelected = selectedSource == GradeSource.ACTIVITY,
                                onClick = {
                                    selectedSource = GradeSource.ACTIVITY
                                    if (name == cutFinalName) name = ""
                                    percentage = ""
                                    error = null
                                }
                            )
                            ActivityChip(
                                label = stringResource(R.string.grade_mode_final_only),
                                isSelected = selectedSource == GradeSource.PERIOD_FINAL,
                                onClick = {
                                    selectedSource = GradeSource.PERIOD_FINAL
                                    weightUnknown = false
                                    percentage = "100"
                                    if (name.isBlank()) name = cutFinalName
                                    error = null
                                }
                            )
                        }
                    }
                }
            }

            if (paso == 1 || isEditing) FormBlock(
                title = stringResource(R.string.grade_value_section),
                icon = Icons.Rounded.BarChart,
                accent = MaterialTheme.colorScheme.primary
            ) {
                /*
                 * **La cifra se escribe con el teclado de la propia hoja.**
                 *
                 * El del sistema tapaba media pantalla, empujaba el formulario y obligaba a
                 * desplazarse a ciegas para llegar al boton; ademas trae una tecla de tabulador,
                 * otra de idioma y una barra de sugerencias que aqui no significan nada, porque
                 * lo unico que se puede escribir son diez digitos y una coma.
                 *
                 * Un teclado propio ocupa lo que ocupa, no se va ni vuelve, y sus teclas son
                 * exactamente las que hacen falta. La cifra deja de ser un campo de texto y
                 * pasa a ser lo que siempre fue: un numero grande que se compone tocando.
                 */
                CifraDeLaHoja(
                    valor = value,
                    sufijo = "/ $maxGradeLabel",
                    etiqueta = stringResource(R.string.grade_obtained),
                    activo = campoActivo == 0,
                    error = value.isNotBlank() && !isGradeValid,
                    onClick = { campoActivo = 0 }
                )

                if (selectedSource == GradeSource.ACTIVITY && !weightUnknown) {
                    CifraDeLaHoja(
                        valor = percentage,
                        sufijo = "%",
                        etiqueta = stringResource(R.string.grade_weight_within_cut, cutDisplayName(selectedPeriod)),
                        activo = campoActivo == 1,
                        error = percentage.isNotBlank() && !isPercentageValid,
                        onClick = { campoActivo = 1 }
                    )
                    Text(
                        text = when {
                            percentage.isNotBlank() && !isPercentageValid ->
                                stringResource(R.string.grade_weight_exceeds, cutDisplayName(selectedPeriod), formatPercent(remainingWeight))
                            remainingWeight <= 0.05 ->
                                stringResource(R.string.grade_weight_already_full, cutDisplayName(selectedPeriod))
                            else ->
                                stringResource(R.string.grade_weight_remaining, formatPercent(remainingWeight), cutDisplayName(selectedPeriod))
                        },
                        color = if (percentage.isNotBlank() && !isPercentageValid) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    /*
                     * **Cuanto del corte llevas repartido, viendolo.**
                     *
                     * El texto de abajo ya decia cuanto queda, pero en numero: para saber si
                     * un 40 % es mucho o poco en ese corte habia que restar de cabeza. La
                     * barra lo ensena de un vistazo, con la parte apagada siendo lo que ya
                     * estaba repartido y la viva lo que se lleva esta nota.
                     *
                     * Es la misma `EvaluationBar` de la materia y del corte, asi que respeta
                     * la forma —ondulada o recta— que este elegida en Apariencia.
                     */
                    val pesoEscrito = percentage.comoNumero() ?: 0.0
                    val yaRepartido = (100.0 - remainingWeight).coerceIn(0.0, 100.0)
                    val conEsta = (yaRepartido + pesoEscrito).coerceIn(0.0, 100.0)
                    EvaluationBar(
                        fraction = conEsta / 100.0,
                        modifier = Modifier.fillMaxWidth(),
                        color = if (percentage.isNotBlank() && !isPercentageValid) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }

                TecladoDeLaHoja(
                    onDigito = { d ->
                        error = null
                        if (campoActivo == 0) {
                            val t = value + d
                            // Una nota no pasa del maximo ni tiene mas de una cifra entera.
                            if ((t.comoNumero() ?: 0.0) <= maxGrade && t.replace(",", "").length <= 2) {
                                value = t
                            }
                        } else {
                            val t = percentage + d
                            if ((t.comoNumero() ?: 0.0) <= 100.0) percentage = t
                        }
                    },
                    onComa = {
                        // El peso es entero: no hay pesos con decimales en ningun corte.
                        if (campoActivo == 0 && value.isNotEmpty() && !value.contains(",")) {
                            value += ","
                        }
                    },
                    comaActiva = campoActivo == 0,
                    onBorrar = {
                        error = null
                        if (campoActivo == 0) value = value.dropLast(1) else percentage = percentage.dropLast(1)
                    }
                )

                /*
                 * Un solo toque para lo que casi nadie toca.
                 *
                 * Los dos interruptores de aqui dentro redefinen lo de arriba, asi que no
                 * pueden estar sueltos en el camino; pero tampoco escondidos del todo, porque
                 * cuando hacen falta hacen mucha falta. Un pliegue con su nombre es el termino
                 * medio: se ve que hay algo, y no estorba.
                 */
                TextButton(
                    onClick = { avanzado = !avanzado },
                    contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                ) {
                    Icon(
                        if (avanzado) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        stringResource(R.string.grade_more_options),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                }

                /*
                 * Los dos que cambian las reglas, juntos y detras del pliegue.
                 *
                 * Uno retira el peso del calculo y el otro lo pone al 100 % sustituyendo el
                 * corte entero: los dos redefinen el campo que hay justo encima. Sueltos en el
                 * camino se activan sin querer —y eso costaba entender por que la nota no
                 * contaba—; escondidos del todo no se encuentran cuando hacen falta. Un pliegue
                 * con su nombre es el termino medio.
                 */
                if (avanzado) {
                if (selectedSource == GradeSource.ACTIVITY) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.grade_unknown_weight),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = stringResource(R.string.grade_unknown_weight_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        UniSwitch(
                            checked = weightUnknown,
                            onCheckedChange = {
                                weightUnknown = it
                                error = null
                            }
                        )
                    }
                }
                }
            }

            if (paso == 2 || isEditing) FormBlock(
                title = if (isEditing) {
                    stringResource(R.string.grade_what_recording)
                } else {
                    stringResource(R.string.grade_what_recording) + " \u00b7 " +
                        stringResource(R.string.grade_tab_optional)
                },
                icon = Icons.AutoMirrored.Rounded.Assignment,
                accent = MaterialTheme.colorScheme.primary
            ) {
                if (!isEditing && lockedCut == null && cutByDate != null && !changingCut) {
                    // Lo eligio la fecha. Se dice cual y por que, y se deja salida a mano.
                    UniCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        borderColor = MaterialTheme.colorScheme.primary,
                        borderWidth = 1.dp
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = stringResource(R.string.grade_goes_to),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = cutDisplayName(cutByDate),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = stringResource(R.string.grade_chosen_today_hint),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                            TextButton(
                                onClick = { changingCut = true },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                            ) {
                                Text(stringResource(R.string.grade_change_cut))
                            }
                        }
                    }
                }
                if (!isEditing && lockedCut == null && (cutByDate == null || changingCut)) {
                    Text(
                        text = stringResource(R.string.tasks_field_cut),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        cutScheme.cuts.sortedBy { it.order }.forEach { cut ->
                            ActivityChip(
                                label = cutDisplayName(cut),
                                isSelected = selectedPeriod.id == cut.id,
                                onClick = {
                                    selectedCutId = cut.id
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
                    label = { Text(stringResource(R.string.grade_activity_name)) },
                    placeholder = { Text(stringResource(R.string.grade_activity_name_placeholder)) },
                    singleLine = true,
                    // El aviso de error de Movimiento: el rojo lo pone `isError`, y esto
                    // anade lo que el rojo no dice, que es que **acaba** de pasar.
                    modifier = Modifier.fillMaxWidth().avisoDeError(!isNameValid),
                    shape = FormCardShape,
                    isError = !isNameValid,
                    colors = formFieldColors(),
                    supportingText = {
                        if (!isNameValid) {
                            Text(
                                nameValidation.errorMessage ?: stringResource(R.string.grade_activity_name_error),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                )
                if (selectedSource == GradeSource.ACTIVITY) {
                    Text(
                        text = stringResource(R.string.grade_activity_type_optional),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activityTypes.forEach { (label, type) ->
                            ActivityChip(
                                label = label,
                                isSelected = selectedType == type,
                                onClick = {
                                    // El tipo ya no pisa el nombre escrito. Antes lo
                                    // sobreescribía siempre: escribías «Parcial 2», tocabas
                                    // «Quiz» y la actividad pasaba a llamarse «Quiz».
                                    if (name.isBlank() || activityTypes.any { it.first == name }) {
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
            // Dentro de la hoja el fondo es el de la hoja: con el de la pantalla, la barra de
            // guardar salia de otro tono que el resto del formulario.
            color = if (enHoja) {
                MaterialTheme.colorScheme.surfaceContainerLow
            } else {
                MaterialTheme.colorScheme.background
            },
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .bottomActionInsets()
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
            /*
             * **En el primer paso el boton lleva al segundo, no a guardar.**
             *
             * El nombre de la actividad es obligatorio y vive en la segunda pestana, asi que
             * desde la primera «Guardar nota» estaba siempre apagado: rellenabas la nota y el
             * peso, ibas al boton grande —que es a donde va el pulgar— y no pasaba nada, sin
             * decir por que. La pestana de arriba era el unico camino, y arriba no es donde se
             * busca «lo siguiente».
             *
             * Editando no hay pasos y el boton guarda, como siempre.
             */
            val enPasoUno = !isEditing && paso == 1
            val pasoUnoValido = subject != null && isGradeValid && isPercentageValid
            val botonActivo = if (enPasoUno) pasoUnoValido else isValid
            Button(
                shapes = UniStackButtonDefaults.shapes,
                onClick = {
                    if (enPasoUno) {
                        paso = 2
                        error = null
                        return@Button
                    }
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
                            cutId = selectedPeriod.id,
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
                            cutId = selectedPeriod.id,
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
                        error = validationErrorMsg
                    }
                },
                enabled = botonActivo,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = DisabledButtonColor,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    text = when {
                        enPasoUno -> stringResource(R.string.grade_next_step)
                        isEditing -> stringResource(R.string.grade_save_changes)
                        else -> stringResource(R.string.grade_save_grade)
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (botonActivo) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            }
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
            title = { Text(stringResource(R.string.grade_complete_history_title)) },
            text = {
                Text(stringResource(R.string.grade_complete_history_desc))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        showHistorySuggestion = false
                        onCompleteHistoryClick(subjectId)
                    }
                ) { Text(stringResource(R.string.grade_complete_history_button)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.updateHistoryPromptStatus(subjectId, PriorHistoryPromptStatus.SNOOZED)
                        showHistorySuggestion = false
                        onBackClick()
                    }
                ) { Text(stringResource(R.string.grade_complete_history_later)) }
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

@Composable
private fun cutDisplayName(cut: GradingCut): String = cut.name.takeIf { it.isNotBlank() } ?: stringResource(R.string.subject_cut_order, cut.order)

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.0f", value)

/*
 * **Un 5,0 es un 5,0.**
 *
 * `toDoubleOrNull` solo entiende el punto, asi que quien escribia la coma —que es lo que pone
 * el teclado en espanol, y lo que la propia app usa al mostrar la nota en varios sitios— veia
 * el campo en rojo y no sabia por que. Aqui se aceptan las dos y no se elige por el idioma del
 * telefono: en un campo de una sola cifra no hay separador de miles con el que confundirse.
 */
private fun String.comoNumero(): Double? = trim().replace(',', '.').toDoubleOrNull()
