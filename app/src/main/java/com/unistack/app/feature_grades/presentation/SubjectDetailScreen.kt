@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.annotation.StringRes
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextDecoration
import com.unistack.app.feature_tasks.domain.StudentTask
import com.unistack.app.feature_tasks.domain.TaskGradingStatus
import com.unistack.app.feature_tasks.domain.formatTaskDueText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.PriorityHigh
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import com.unistack.app.core.design.components.barridoDeRecuperacion
import com.unistack.app.core.design.components.celebracionDelDia
import com.unistack.app.core.design.components.selloDeCorte
import com.unistack.app.core.design.components.numeroQueCuenta
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniDivider
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.FilaDeslizable
import com.unistack.app.core.design.components.EvaluationBar
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ArrowDownward
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.muelleDeMovimiento
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.tweenDeMovimiento
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.SubjectGradeCalculation
import com.unistack.app.core.utils.TargetOutlook
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_schedule.domain.ClassSession
import com.unistack.app.feature_user.domain.GradingCut
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.GradingScale
import java.util.Locale
import kotlin.math.round

import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.material.icons.rounded.Percent
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Flag
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.components.MetricCard
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Shape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
private val LargeCardShape: Shape
    @Composable
    @ReadOnlyComposable
    get() = MaterialTheme.shapes.medium

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onCutClick: (String, String) -> Unit,
    onEditSubjectClick: (String) -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    onCompleteHistoryClick: (String) -> Unit,
    /** Abre las estadisticas de la materia. La tarjeta de arriba entera lleva ahi. */
    onStatsClick: (String) -> Unit,
    onNewNoteClick: (String) -> Unit,
    onSubjectDeleted: () -> Unit,
    modifier: Modifier = Modifier,
    onTaskClick: ((String) -> Unit)? = null,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val allTasks by viewModel.tasks.collectAsStateWithLifecycle()
    val classSessions by viewModel.classSessions.collectAsStateWithLifecycle()
    val classSession = classSessions.firstOrNull { it.subjectId == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val cutScheme = subject?.cutScheme ?: profile?.gradingCutScheme ?: GradingCutScheme.default()
    val passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)
    var showSubjectMenu by remember { mutableStateOf(false) }
    var showDeleteSubjectDialog by remember { mutableStateOf(false) }

    if (subject == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    // Mientras el usuario no diga en qué corte va, la pantalla no lo supone: no marca ningún
    // corte como actual y no reclama el historial de los anteriores. Reclamarlo exige saber
    // que esos cortes ya pasaron, y eso solo lo sabe él.
    val chosenCut = subject.chosenCutId?.let { id ->
        cutScheme.cuts.firstOrNull { it.id == id }
    }
    val activeCutOrder = chosenCut?.order ?: 1
    val hasIncompletePriorHistory = chosenCut != null &&
        activeCutOrder > 1 &&
        cutScheme.cuts
            .filter { it.order < activeCutOrder }
            .any { cut ->
                subject.grades.none { it.cutId == cut.id } &&
                    cut.id !in subject.unknownCutIds
            }

    val cutSummaries = remember(subject, cutScheme) {
        cutScheme.cuts.map { cut ->
            val grades = subject.grades.filter { it.cutId == cut.id }
            cut.toSummary(grades)
        }
    }
    val orderedCutSummaries = remember(cutSummaries, subject.activeCutId) {
        cutSummaries.sortedWith(
            compareByDescending<CutSummary> { it.cut.id == subject.activeCutId }
                .thenBy { it.cut.order }
        )
    }
    // Los cortes cerrados se apartan: ni se pueden elegir como destino de notas nuevas ni
    // compiten por la atención con los que aún están en juego.
    /*
     * **Cerrado y completo no son lo mismo.**
     *
     * Completo quiere decir que ya no cabe otra nota: el 100 % del peso está repartido.
     * Cerrado quiere decir que tú lo has dado por terminado. Antes eran la misma cosa, y por
     * eso el corte se apartaba solo mientras estabas escribiendo en otra pantalla, con lo que
     * el sello no lo veía nadie. Ahora el corte lleno se queda arriba, diciendo que está listo
     * y con el botón puesto, hasta que lo cierres.
     */
    val cerrados = subject.closedCutIds
    val openCutSummaries = orderedCutSummaries.filter { it.cut.id !in cerrados }
    val completedCutSummaries = cutSummaries
        .filter { it.cut.id in cerrados }
        .sortedBy { it.cut.order }

    /*
     * La hoja de registrar una nota, encima de esta pantalla.
     *
     * Guarda **a qué corte** va, que es lo que antes viajaba en la ruta. Mientras está abierta
     * la materia sigue compuesta debajo: por eso, al cerrarse, la fila nueva entra animada y
     * el promedio cuenta desde el valor anterior. Con la pantalla aparte eso era imposible.
     */
    var hojaDeNota by rememberSaveable { mutableStateOf<String?>(null) }

    /*
     * **Qué corte se acaba de cerrar**, para estamparle el sello.
     *
     * Se compara con lo que había cerrado en la composición anterior y no con el estado a
     * secas: al abrir la materia, los cerrados lo estaban ya de antes, y sellarlos todos de
     * golpe convertiría el gesto en un adorno de bienvenida.
     */
    var cerradosVistos by remember { mutableStateOf<Set<String>?>(null) }
    var selloEn by remember { mutableStateOf<String?>(null) }
    // Reabrir suelta las notas de un corte que ya estaba decidido: pregunta antes, igual
    // que dentro del propio corte.
    var cortePorReabrir by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(cerrados) {
        val previos = cerradosVistos
        if (previos != null) (cerrados - previos).firstOrNull()?.let { selloEn = it }
        cerradosVistos = cerrados
    }

    // Una sola cuenta para toda la pantalla. Antes había tres: el promedio salía del
    // calculador, la proyección final se calculaba aquí a mano con otra fórmula —contaba un
    // corte apenas empezado con su peso completo— y lo necesario para la meta volvía a pasar
    // por el porcentaje ya redondeado. La tarjeta llegaba a enseñar dos cifras distintas
    // para lo mismo, una encima de la otra.
    val calculation = remember(subject.grades, cutScheme, subject.targetAverage, maxGrade, profile?.passingGrade) {
        GradeCalculator.calculateSubject(
            grades = subject.grades,
            cuts = cutScheme.cuts,
            targetAverage = subject.targetAverage,
            maxGrade = maxGrade,
            passingGrade = profile?.passingGrade
        )
    }
    val evaluatedSubjectPercentage = calculation.evaluatedSemesterFraction * 100.0
    val remainingSubjectPercentage = calculation.remainingSemesterFraction * 100.0

    val subjectTasks = remember(allTasks, subject.id) {
        allTasks.filter { it.subjectId == subject.id }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 22.dp,
                top = 8.dp,
                end = 22.dp,
                /*
                 * Ya no hay boton pegado abajo: la nota se agrega **desde dentro del corte**,
                 * que es donde se ve lo que pasa al guardarla. El de abajo abria la hoja sin
                 * que el corte estuviera desplegado, asi que al cerrarse no habia nada
                 * mirando: ni la fila entrando ni la cifra contando.
                 *
                 * Queda el hueco de la barra flotante, que sigue tapando el final de la lista.
                 */
                bottom = scrollBottomRoom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                SubjectHeader(
                    title = subject.name,
                    // «Materia activa» era una etiqueta fija que no distinguía nada: no
                    // existen materias inactivas. La meta sí dice algo y no se repite en
                    // ningún otro sitio de la cabecera.
                    subtitle = stringResource(R.string.subject_target_label, GradingScaleUtils.formatGrade(subject.targetAverage, scale)),
                    onBackClick = onBackClick,
                    showMenu = showSubjectMenu,
                    onMenuClick = { showSubjectMenu = true },
                    onDismissMenu = { showSubjectMenu = false },
                    onEditClick = {
                        showSubjectMenu = false
                        onEditSubjectClick(subject.id)
                    },
                    onCompleteHistoryClick = if (hasIncompletePriorHistory) {
                        {
                            showSubjectMenu = false
                            onCompleteHistoryClick(subject.id)
                        }
                    } else {
                        null
                    },
                    onNewNoteClick = {
                        showSubjectMenu = false
                        onNewNoteClick(subject.id)
                    },
                    onDeleteClick = {
                        showSubjectMenu = false
                        showDeleteSubjectDialog = true
                    }
                )
            }
            classSession?.let { session ->
                item { SubjectClassFacts(session) }
            }
            item {
                SubjectOverviewCard(
                    onClick = { onStatsClick(subject.id) },
                    calculation = calculation,
                    targetGrade = subject.targetAverage,
                    evaluated = evaluatedSubjectPercentage,
                    maxGrade = maxGrade,
                    scale = scale,
                    passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)
                )
            }
            item {
                SubjectMetricsBand(
                    calculation = calculation,
                    passingGrade = passingGrade,
                    targetGrade = subject.targetAverage,
                    remainingPercentage = remainingSubjectPercentage,
                    scale = scale
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(R.string.subject_semester_cuts),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                    // Sin «Corte N activo»: el selector de justo debajo ya lo marca y la
                    // tarjeta del corte lo repite otra vez. Eran tres formas de decir lo mismo
                    // seguidas.
                    Text(
                        stringResource(R.string.subject_cuts_count, cutScheme.cuts.size),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }
            item {
                CutChooser(
                    // Un corte con el 100% repartido ya no admite más notas, así que sale de
                    // la lista de destinos. Seguir ofreciéndolo era ofrecer un sitio donde
                    // cualquier peso nuevo iba a ser rechazado al guardar.
                    cuts = openCutSummaries.map { it.cut },
                    chosenCutId = subject.chosenCutId,
                    onChoose = { viewModel.setActiveCut(subject.id, it) }
                )
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    openCutSummaries.forEach { summary ->
                        val needsHistory = chosenCut != null &&
                            summary.cut.order < activeCutOrder &&
                            summary.grades.isEmpty() &&
                            summary.cut.id !in subject.unknownCutIds
                        CutCard(
                            summary = summary,
                            maxGrade = maxGrade,
                            scale = scale,
                            isActive = summary.cut.id == subject.chosenCutId,
                            needsHistory = needsHistory,
                            // Ni es el que cursas, ni tiene nada dentro, ni te reclama nada:
                            // no hay ningun dato suyo que merezca una tarjeta entera.
                            compacto = summary.cut.id != subject.chosenCutId &&
                                summary.grades.isEmpty() &&
                                !needsHistory,
                            onClick = {
                                if (needsHistory) {
                                    onCompleteHistoryClick(subject.id)
                                } else {
                                    onCutClick(subject.id, summary.cut.id)
                                }
                            }
                        )
                        /*
                         * **El corte lleno no se cierra solo: lo ofrece.**
                         *
                         * Aparece al repartir el 100 % y se queda ahí hasta que lo pulses, así
                         * que hay margen para corregir una nota antes de fijar el corte. Y el
                         * sello, que se estampa justo después, significa algo: lo pusiste tú.
                         */
                    }
                }
            }
            if (completedCutSummaries.isNotEmpty()) {
                item {
                    Text(
                        stringResource(R.string.subject_completed_cuts),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        completedCutSummaries.forEach { summary ->
                            // Siguen siendo tarjetas normales: se entra a consultarlas y, si
                            // hace falta corregir algo, a editar sus notas desde dentro.
                            CutCard(
                                summary = summary,
                                maxGrade = maxGrade,
                                scale = scale,
                                isActive = false,
                                needsHistory = false,
                                onClick = { onCutClick(subject.id, summary.cut.id) },
                                dimmed = true,
                                modifier = Modifier.selloDeCorte(
                                    disparado = summary.cut.id == selloEn,
                                    onTerminado = { selloEn = null },
                                    cerrado = true
                                )
                            )
                            // Reabrir tiene que existir y costar lo mismo que cerrar: si al
                            // cerrarlo ves que una nota estaba mal, el camino de vuelta no
                            // puede ser borrar el corte.
                            TextButton(
                                onClick = { cortePorReabrir = summary.cut.id },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    stringResource(R.string.subject_reopen_cut),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (subjectTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        stringResource(R.string.subject_tasks_section_title),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                items(subjectTasks, key = { it.id }) { task ->
                    SubjectTaskItem(
                        task = task,
                        onClick = { onTaskClick?.invoke(task.id) }
                    )
                }
            }
        }
    }

    cortePorReabrir?.let { cutId ->
        AlertDialog(
            onDismissRequest = { cortePorReabrir = null },
            title = {
                Text(
                    stringResource(R.string.subject_reopen_cut_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.subject_reopen_cut_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setCutClosed(subject.id, cutId, false)
                        cortePorReabrir = null
                    }
                ) {
                    Text(
                        stringResource(R.string.subject_reopen_cut),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { cortePorReabrir = null }) {
                    Text(
                        stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    if (showDeleteSubjectDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSubjectDialog = false },
            title = { Text(stringResource(R.string.subject_delete_title), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.subject_delete_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteSubjectDialog = false
                        if (viewModel.deleteSubject(subject.id)) onSubjectDeleted()
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSubjectDialog = false }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    /*
     * **La hoja de registrar una nota.**
     *
     * Va casi a pantalla completa porque el formulario es largo, pero no del todo: ese dedo de
     * materia que se ve arriba es lo que dice que no has cambiado de sitio, y es la diferencia
     * entre «guardo y vuelvo» y «guardo y lo veo pasar».
     *
     * `skipPartiallyExpanded` porque una hoja a media altura con un formulario dentro obliga a
     * arrastrarla antes de poder escribir.
     */
    hojaDeNota?.let { cutId ->
        val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { hojaDeNota = null },
            sheetState = estadoHoja,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            AddGradeScreen(
                subjectId = subject.id,
                initialCutId = cutId,
                onBackClick = { hojaDeNota = null },
                onCompleteHistoryClick = { id ->
                    hojaDeNota = null
                    onCompleteHistoryClick(id)
                },
                viewModel = viewModel,
                enHoja = true,
                /*
                 * **La hoja se aparta del teclado en vez de quedarse debajo.**
                 *
                 * Con targetSdk 36 la ventana ya no se redimensiona sola, asi que la hoja
                 * seguia midiendo el 92 % de la pantalla con el teclado encima: la barra de
                 * guardar quedaba tapada y entre ella y el teclado se abrian los huecos.
                 * `imePadding` es lo que ya hacen el editor de notas, la calculadora y la hoja
                 * de sugerencias; este formulario era el unico que no.
                 */
                /*
                 * **La hoja mide lo que mide su contenido.**
                 *
                 * Con el 92 % fijo, el primer paso —que son dos campos— dejaba media pantalla
                 * en blanco y el boton de guardar al fondo del todo, lejisimos del pulgar y
                 * despues de un vacio que no decia nada. Ajustandose, el boton queda justo
                 * debajo de lo ultimo que has rellenado; y el tope del 92 % sigue ahi para
                 * cuando el formulario si es largo.
                 */
                modifier = Modifier
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.92f)
                    .imePadding()
            )
        }
    }}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectCutDetailScreen(
    subjectId: String,
    cutId: String,
    onBackClick: () -> Unit,
    onEditGradeClick: (String, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val cutScheme = subject?.cutScheme ?: profile?.gradingCutScheme ?: GradingCutScheme.default()
    val cut = cutScheme.cuts.firstOrNull { it.id == cutId }
    var gradeIdPendingDelete by remember { mutableStateOf<String?>(null) }
    /*
     * **Cerrar y reabrir preguntan antes.**
     *
     * Cerrar fija las notas del corte y reabrir las suelta: las dos cambian el estado de
     * algo que ya estaba decidido, y las dos estaban a un toque sin red. El boton de
     * cerrar aparece justo cuando acabas de repartir el 100 %, que es cuando el dedo ya
     * viene bajando de registrar la ultima nota.
     */
    var confirmarCierre by remember { mutableStateOf(false) }
    var confirmarReapertura by remember { mutableStateOf(false) }
    // La misma hoja que en la materia: si esta pantalla se destruyera para escribir la nota,
    // la fila nueva volveria a aparecer sin entrar.
    var hojaDeNota by rememberSaveable { mutableStateOf(false) }

    if (subject == null || cut == null) {
        MissingSubjectState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val grades = subject.grades.filter { it.cutId == cut.id }
    val summary = cut.toSummary(grades)

    /*
     * **Nada se anima mientras la hoja tapa la pantalla.**
     *
     * La nota se guarda en cuanto pulsas, y la hoja tarda todavia un cuarto de segundo en
     * bajar: la fila entraba y el promedio contaba **detras de la hoja**, y para cuando se
     * apartaba ya estaba todo quieto. De ahi la sensacion de que guardar no hacia nada.
     *
     * Esto retiene el valor anterior hasta que la hoja se ha ido, y entonces lo suelta: la
     * cuenta, la flecha y la entrada de la fila arrancan con la pantalla ya despejada.
     */
    var listoParaAnimar by remember { mutableStateOf(true) }
    LaunchedEffect(hojaDeNota) {
        if (hojaDeNota) {
            listoParaAnimar = false
        } else {
            kotlinx.coroutines.delay(280)
            listoParaAnimar = true
        }
    }
    var resumenMostrado by remember { mutableStateOf(summary) }
    /*
     * **La lista tambien espera, y no solo el aviso de «cual es nueva».**
     *
     * Retener solo la marca no bastaba: la fila ya estaba puesta y a la vista desde que se
     * guardaba, y cuando la compuerta se abria el gesto arrancaba **desde cero** sobre una fila
     * que ya se veia. De ahi el parpadeo: aparecia, se apagaba y volvia a entrar.
     *
     * Reteniendo la lista entera, la fila no existe hasta que se la puede animar. Aparece una
     * sola vez, ya entrando, que es lo que se venia a ver.
     */
    var notasMostradas by remember { mutableStateOf(grades) }
    LaunchedEffect(summary, grades, listoParaAnimar) {
        if (listoParaAnimar) {
            resumenMostrado = summary
            notasMostradas = grades
        }
    }

    var saveBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    /*
     * **El sello tambien se estampa aqui.**
     *
     * «Cerrar corte» esta en las dos pantallas, pero el gesto solo lo disparaba la materia:
     * cerrando desde el detalle del corte —que es donde esta el boton que se ve al acabar de
     * repartir el 100 %— la tarjeta se limitaba a cambiar de estado y no pasaba nada. El
     * momento existia y quien lo provocaba no lo veia.
     *
     * Se dispara comparando con lo cerrado en la composicion anterior, igual que en la materia:
     * entrando a un corte que ya estaba cerrado no hay nada que celebrar, porque no acaba de
     * ocurrir.
     */
    val estaCerrado = cut.id in subject.closedCutIds
    var cierreVisto by remember { mutableStateOf<Boolean?>(null) }
    var sello by remember { mutableStateOf(false) }
    /*
     * **Cerrar dice como fue.**
     *
     * El sello caia y la pantalla se quedaba igual: el corte cambiaba de estado y nadie
     * decia si con esa nota ibas bien o mal. Cerrar un corte es un momento de balance
     * —como cerrar la ultima pendiente del dia, que se celebra— y el balance tiene que
     * decirse con palabras: cumpliste la meta, aprobaste por debajo de ella, o quedo en
     * rojo. Sale cuando el sello ya ha pegado, y si el corte no quedo en rojo lo acompana
     * la misma celebracion que la ultima tarea del dia: cerrar aprobado tambien se celebra,
     * que un corte cerrado es un corte menos.
     */
    var veredicto by remember { mutableStateOf(false) }
    var celebrar by remember { mutableStateOf(false) }
    val passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)
    val comoFue = veredictoDelCierre(summary.average, subject.targetAverage, passingGrade)
    val esperaDelSello = duracion(2200)
    LaunchedEffect(estaCerrado) {
        if (cierreVisto == false && estaCerrado) {
            sello = true
            veredicto = false
            kotlinx.coroutines.delay((esperaDelSello * 0.45f).toLong())
            veredicto = comoFue != null
            if (comoFue == Veredicto.META || comoFue == Veredicto.APROBADO) celebrar = true
        }
        if (!estaCerrado) veredicto = false
        cierreVisto = estaCerrado
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .celebracionDelDia(
                disparada = celebrar,
                onTerminada = { celebrar = false },
                mensaje = stringResource(R.string.celebration_cut_closed)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 8.dp,
                end = 20.dp,
                bottom = saveBarHeight + scrollBottomRoom
            ),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CutHeader(
                    title = cutDisplayName(cut),
                    subtitle = stringResource(R.string.subject_cut_weight_of_subject, subject.name, formatPercent(cut.weight * 100)),
                    onBackClick = onBackClick
                )
            }
            item {
                /*
                 * **El resumen del corte, arriba del todo.**
                 *
                 * Es la misma tarjeta rellena que abre la materia, con lo suyo: la nota del
                 * corte en grande, la barra de lo evaluado y el estado en una linea. Sin ella
                 * la pantalla empezaba directamente por la lista, y la cifra que resume el
                 * corte solo aparecia pequena, dentro de la cabecera de la tarjeta de abajo.
                 */
                HeroDelCorte(
                    summary = resumenMostrado,
                    maxGrade = maxGrade,
                    scale = scale,
                    passingGrade = passingGrade
                )
            }
            item {
                AnimatedVisibility(
                    visible = veredicto && comoFue != null,
                    enter = expandVertically(animationSpec = tweenDeMovimiento(360)) +
                        fadeIn(animationSpec = tweenDeMovimiento(360, 120)),
                    exit = shrinkVertically(animationSpec = tweenDeMovimiento(220)) +
                        fadeOut(animationSpec = tweenDeMovimiento(160))
                ) {
                    comoFue?.let {
                        TarjetaDeVeredicto(
                            veredicto = it,
                            corte = cutDisplayName(cut),
                            nota = summary.average?.let { n -> GradingScaleUtils.formatGrade(n, scale) }.orEmpty(),
                            meta = GradingScaleUtils.formatGrade(subject.targetAverage, scale),
                            aprobado = GradingScaleUtils.formatGrade(passingGrade, scale),
                            onCerrar = { veredicto = false }
                        )
                    }
                }
            }
            item {
                /*
                 * **Una sola tarjeta, la del prototipo.**
                 *
                 * Antes esta pantalla eran tres bloques apilados —el resumen tenido de arriba,
                 * el titulo «Notas del corte», y la caja de filas separadas por lineas— mas un
                 * boton pegado abajo. Cuatro sitios para contar una sola cosa.
                 *
                 * Ahora es un corte: su cabecera, sus notas y lo que puedes hacer con el, todo
                 * dentro del mismo recuadro y con la franja de color corriendo por el lado. Y
                 * como cada nota es su propia fila con fondo, la que llega **se abre hueco**
                 * entre las demas en vez de obligar a repintar la caja entera.
                 */
                TarjetaDelCorte(
                    summary = resumenMostrado,
                    notas = notasMostradas,
                    maxGrade = maxGrade,
                    scale = scale,
                    passingGrade = profile?.passingGrade ?: (maxGrade * 0.6),
                    cerrado = estaCerrado,
                    modifier = Modifier.selloDeCorte(
                        disparado = sello,
                        onTerminado = { sello = false },
                        cerrado = estaCerrado
                    ),
                    onAgregar = { hojaDeNota = true },
                    onCerrar = { confirmarCierre = true },
                    onReabrir = { confirmarReapertura = true },
                    onEditar = { onEditGradeClick(subject.id, it) },
                    onBorrar = { gradeIdPendingDelete = it }
                )
            }
        }
    }

    if (confirmarCierre) {
        AlertDialog(
            onDismissRequest = { confirmarCierre = false },
            title = {
                Text(
                    stringResource(R.string.subject_close_cut_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.subject_close_cut_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setCutClosed(subject.id, cut.id, true)
                        confirmarCierre = false
                    }
                ) {
                    Text(
                        stringResource(R.string.subject_close_cut_short),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarCierre = false }) {
                    Text(
                        stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    if (confirmarReapertura) {
        AlertDialog(
            onDismissRequest = { confirmarReapertura = false },
            title = {
                Text(
                    stringResource(R.string.subject_reopen_cut_title),
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    stringResource(R.string.subject_reopen_cut_message),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.setCutClosed(subject.id, cut.id, false)
                        confirmarReapertura = false
                    }
                ) {
                    Text(
                        stringResource(R.string.subject_reopen_cut),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarReapertura = false }) {
                    Text(
                        stringResource(R.string.action_cancel),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    gradeIdPendingDelete?.let { gradeId ->
        AlertDialog(
            onDismissRequest = { gradeIdPendingDelete = null },
            title = { Text(stringResource(R.string.subject_delete_grade_title), color = MaterialTheme.colorScheme.onSurface) },
            text = { Text(stringResource(R.string.subject_delete_grade_message), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteGrade(subject.id, gradeId)
                        gradeIdPendingDelete = null
                    }
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { gradeIdPendingDelete = null }) {
                    Text(stringResource(R.string.action_cancel), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    if (hojaDeNota) {
        val estadoHoja = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { hojaDeNota = false },
            sheetState = estadoHoja,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            shape = MaterialTheme.shapes.extraLarge
        ) {
            AddGradeScreen(
                subjectId = subject.id,
                initialCutId = cut.id,
                onBackClick = { hojaDeNota = false },
                viewModel = viewModel,
                enHoja = true,
                /*
                 * **La hoja se aparta del teclado en vez de quedarse debajo.**
                 *
                 * Con targetSdk 36 la ventana ya no se redimensiona sola, asi que la hoja
                 * seguia midiendo el 92 % de la pantalla con el teclado encima: la barra de
                 * guardar quedaba tapada y entre ella y el teclado se abrian los huecos.
                 * `imePadding` es lo que ya hacen el editor de notas, la calculadora y la hoja
                 * de sugerencias; este formulario era el unico que no.
                 */
                /*
                 * **La hoja mide lo que mide su contenido.**
                 *
                 * Con el 92 % fijo, el primer paso —que son dos campos— dejaba media pantalla
                 * en blanco y el boton de guardar al fondo del todo, lejisimos del pulgar y
                 * despues de un vacio que no decia nada. Ajustandose, el boton queda justo
                 * debajo de lo ultimo que has rellenado; y el tope del 92 % sigue ahi para
                 * cuando el formulario si es largo.
                 */
                modifier = Modifier
                    .heightIn(max = LocalConfiguration.current.screenHeightDp.dp * 0.92f)
                    .imePadding()
            )
        }
    }
}

@Composable
private fun MissingSubjectState(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp)
    ) {
        UniBackButton(onClick = onBackClick)
        Text(stringResource(R.string.subject_not_found), color = MaterialTheme.colorScheme.onSurface, fontSize = 18.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun SubjectHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit,
    showMenu: Boolean,
    onMenuClick: () -> Unit,
    onDismissMenu: () -> Unit,
    onEditClick: () -> Unit,
    onCompleteHistoryClick: (() -> Unit)?,
    onNewNoteClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UniBackButton(onClick = onBackClick)
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 2,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.BarChart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Text(
                    subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        Box {
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
            ) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.subject_options),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            UniDropdownMenu(
                expanded = showMenu,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.subject_edit), color = MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = onEditClick
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.subject_new_grade), color = MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(Icons.Rounded.EditNote, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = onNewNoteClick
                )
                onCompleteHistoryClick?.let { action ->
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.subject_complete_history), color = MaterialTheme.colorScheme.onSurface) },
                        leadingIcon = {
                            Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = LocalSectionColors.current.atRisk)
                        },
                        onClick = action
                    )
                }
                // Borrar una materia se lleva por delante sus notas: la línea la separa de
                // las opciones que solo abren otra pantalla.
                UniDivider(Modifier.padding(vertical = 4.dp))
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.subject_delete), color = MaterialTheme.colorScheme.error) },
                    leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                    onClick = onDeleteClick
                )
            }
        }
    }
}

@Composable
internal fun CutHeader(title: String, subtitle: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.action_back),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
        // Spacer to balance back button
        Box(modifier = Modifier.size(48.dp))
    }
}

/**
 * Profesor, aula y horario de la materia.
 *
 * Se piden al crear la materia y hasta ahora solo se veían en Horario, así que quien los
 * escribía no volvía a encontrarlos donde los busca: en la propia materia. Se omite lo que
 * esté vacío y la fila entera desaparece si no hay clase.
 */
@Composable
private fun SubjectClassFacts(session: ClassSession) {
    val facts = listOfNotNull(
        session.place.professor.takeIf { it.isNotBlank() }?.let { Icons.Rounded.Person to it },
        session.place.room.takeIf { it.isNotBlank() }?.let { Icons.Rounded.Place to it },
        session.daysAndTimeLabel().takeIf { it.isNotBlank() }?.let { Icons.Rounded.CalendarMonth to it }
    )
    if (facts.isEmpty()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        FlowRow(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            facts.forEach { (icon, text) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(text, color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/**
 * En qué corte va la materia.
 *
 * Mientras no haya elección, es una pregunta con todas las opciones apagadas y no un ajuste
 * con una ya marcada. La app venía dando por hecho el primer corte y presentándolo como
 * «Corte actual» en materias recién creadas; de ahí salía además que reclamara el historial
 * de unos cortes anteriores que el usuario nunca dijo haber cursado.
 */
@Composable
private fun CutChooser(
    cuts: List<GradingCut>,
    chosenCutId: String?,
    onChoose: (String) -> Unit
) {
    if (cuts.isEmpty()) {
        Text(
            stringResource(R.string.subject_all_cuts_completed_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        return
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            if (chosenCutId == null) stringResource(R.string.subject_which_cut_prompt) else stringResource(R.string.subject_new_grades_enter_in),
            color = if (chosenCutId == null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = if (chosenCutId == null) 15.sp else 13.sp,
            fontWeight = if (chosenCutId == null) FontWeight.ExtraBold else FontWeight.SemiBold
        )
        if (chosenCutId == null) {
            Text(
                stringResource(R.string.subject_choose_cut_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 17.sp
            )
        }
        // Elegir uno entre varios es un grupo conectado, igual que Horario/Calendario o
        // Materias/Tareas. Eran tres pastillas sueltas en una fila que rodaba.
        val ordered = cuts.sortedBy { it.order }
        UniSegmentedControl<String>(
            selected = chosenCutId.orEmpty(),
            options = ordered.map {
                UniSegmentedOption(value = it.id, label = cutDisplayName(it))
            },
            onSelected = onChoose,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Dónde está la materia y entre qué dos notas puede acabar.
 *
 * La segunda línea decía «con el rendimiento actual, terminarías con X» a partir de un
 * cálculo propio que contaba los cortes empezados con su peso entero: una sola nota del 5%
 * en un corte del 40% movía la cifra como si el corte estuviera cerrado. En su lugar van los
 * dos extremos reales —sacar 0 en lo que falta y sacarlo todo—, que no suponen nada.
 */
@Composable
private fun SubjectOverviewCard(
    onClick: () -> Unit,
    calculation: SubjectGradeCalculation,
    targetGrade: Double,
    evaluated: Double,
    maxGrade: Double,
    scale: GradingScale,
    /** Con cuanto se aprueba: la barra lo necesita para saber cuando sales del rojo. */
    passingGrade: Double
) {
    val average = calculation.currentAverage
    // La tarjeta de arriba va rellena con el color de la app, no en gris sobre gris.
    // Es lo primero que se mira al abrir una materia y era del mismo tono que todo lo
    // demás; ahora pesa lo que le toca, igual que el hero de Inicio y la próxima clase.
    UniCard(
        modifier = Modifier.fillMaxWidth().bounceClick(onClick),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f).padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    /*
                     * **El titular es la respuesta, no el promedio.**
                     *
                     * «Promedio de lo evaluado» ocupaba el tamano mas grande de la pantalla y
                     * describia el 21 % de la materia: el dato menos util puesto en el sitio
                     * mas visible. Y alrededor habia cinco cifras en la misma escala —lleva,
                     * meta, necesita, minimo, maximo— todas en negrita y sin jerarquia, asi que
                     * habia que resolver la tarjeta para entenderla.
                     *
                     * Ahora arriba va **lo que hay que hacer**, que es distinto en cada
                     * situacion: lo que necesitas de aqui en adelante si la meta sigue en
                     * juego, el suelo si ya no la puedes perder, y el techo si ya no llegas. El
                     * promedio baja a su pildora, con los otros dos.
                     */
                    val esFinal = calculation.isFinished
                    val yaAsegurada = calculation.outlook == TargetOutlook.SECURED && !esFinal
                    val yaNoLlega = calculation.outlook == TargetOutlook.UNREACHABLE
                    val titular = when {
                        average == null -> null
                        esFinal -> average
                        yaAsegurada -> calculation.guaranteedMinimum
                        yaNoLlega -> calculation.bestPossible
                        else -> calculation.neededForTarget
                    }
                    val etiqueta = when {
                        average == null -> stringResource(R.string.subject_evaluated_average)
                        esFinal -> stringResource(R.string.subject_final_grade)
                        yaAsegurada -> stringResource(R.string.subject_hero_secured_label)
                        yaNoLlega -> stringResource(R.string.subject_hero_ceiling_label)
                        else -> stringResource(R.string.subject_hero_needed_label)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            etiqueta.uppercase(Locale.forLanguageTag("es")),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = SectionLabelStyle,
                            modifier = Modifier.weight(1f).padding(end = 8.dp)
                        )
                        /*
                         * La tarjeta entera lleva a las estadisticas; esto lo dice.
                         *
                         * Con su pastilla y no como texto suelto: flotando en la esquina no se
                         * leia como algo que se pueda tocar, sino como una etiqueta mas de las
                         * muchas que ya hay en la tarjeta.
                         */
                        Row(
                            modifier = Modifier
                                // Pegada al borde de la tarjeta: los 20 dp de margen interior
                                // la dejaban flotando lejos de la esquina.
                                .offset(x = 8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.10f))
                                .padding(start = 12.dp, end = 7.dp, top = 6.dp, bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                stringResource(R.string.subject_see_detail),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                    if (titular == null) {
                        Text(
                            stringResource(R.string.subject_unevaluated),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.headlineSmallEmphasized
                        )
                    } else {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                GradingScaleUtils.formatGrade(titular, scale),
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.displaySmallEmphasized
                            )
                            Text(
                                " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                            )
                            // El cambio se marca sobre el promedio, que es lo que se mueve al
                            // registrar una nota, y por eso viaja con su pildora... salvo aqui,
                            // donde el titular es el promedio porque la materia ya acabo.
                            if (esFinal) FlechaDeCambio(average, scale)
                        }
                    }
                }
                /*
                 * **Sin el aro de «% evaluado» al lado del titular.**
                 *
                 * Era un segundo numero grande compitiendo con el titular, y encima medido en
                 * otra unidad: uno es una nota sobre 5 y el otro un porcentaje. Lo evaluado
                 * sigue estando —en su pildora, con los otros dos datos de contexto— donde no
                 * le disputa el sitio a la respuesta.
                 */
            }

            // La frase que explica el titular, pegada a el.
            if (calculation.outlook != TargetOutlook.NO_DATA) {
                SubjectInsightCard(
                    calculation = calculation,
                    targetGrade = targetGrade,
                    maxGrade = maxGrade,
                    scale = scale,
                    comoTarjeta = false
                )
            }

            /*
             * **La banda vuelve, con las marcas dichas.**
             *
             * La quite entera cuando el problema eran sus dos marcas —la raya de la meta y el
             * punto de donde vas— que no llevaban rotulo, y su cifra suelta cayendo justo bajo
             * un titular que significa otra cosa. Sin banda el hero se quedaba en dos frases y
             * nada que mirar, asi que el arreglo era rotularla, no retirarla.
             *
             * Ahora cada marca dice lo que es, debajo y en su sitio: «meta» bajo la raya y
             * «llevas» bajo el punto. Si las dos caen cerca, la de la meta se calla —dos
             * rotulos encimados no se leen ninguno— y queda la que estas mirando.
             */
            val floor = calculation.guaranteedMinimum
            val ceiling = calculation.bestPossible
            if (floor != null && ceiling != null && !calculation.isFinished) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.14f)
                )
                /*
                 * **Aqui vive «materia que se recupera».**
                 *
                 * La barra ya dibuja el viaje entero: va de rojo a verde sobre la escala
                 * completa, con el punto en donde vas. Salir del rojo **es** ese punto
                 * cruzando hacia la derecha, y el barrido lo acompana.
                 */
                OutcomeRangeBar(
                    target = targetGrade,
                    maxGrade = maxGrade,
                    actual = average,
                    scale = scale,
                    aprobado = passingGrade
                )
            }
        }
    }
}

/**
 * El rotulo de una marca de la banda, **centrado sobre su punto exacto**.
 *
 * Con `BiasAlignment` no quedaba centrado sino repartido: ese alineador no pone el centro del
 * hijo en la fraccion pedida, sino que lo interpola dentro del hueco que sobra —a sesgo 1 el
 * borde derecho toca el borde derecho—, asi que cuanto mas ancho el rotulo, mas lejos caia de
 * su marca. Con «llevas 4.70» al 94 % se veia claramente descolgado del punto.
 *
 * Aqui se mide el rotulo y se le resta su media anchura, que es lo que de verdad lo centra, y
 * se recorta a los bordes para que no se salga. Un `offset` en pixeles no supone nada: usa lo
 * que el texto mide de verdad.
 */
@Composable
private fun BoxScope.MarcaDeLaBanda(texto: String, fuerte: Boolean, fraccion: Float, anchoTotal: Int) {
    var ancho by remember { mutableStateOf(0) }
    Text(
        texto,
        color = if (fuerte) {
            MaterialTheme.colorScheme.onSurface
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
        },
        fontSize = if (fuerte) 11.5.sp else 10.5.sp,
        fontWeight = if (fuerte) FontWeight.ExtraBold else FontWeight.SemiBold,
        maxLines = 1,
        modifier = Modifier
            .onSizeChanged { ancho = it.width }
            .offset {
                val centro = (anchoTotal * fraccion).toInt() - ancho / 2
                IntOffset(centro.coerceIn(0, (anchoTotal - ancho).coerceAtLeast(0)), 0)
            }
    )
}

@Composable
private fun OutcomeRangeBar(
    target: Double,
    maxGrade: Double,
    /** Donde estas ahora mismo, para el punto. Nulo si todavia no hay nada evaluado. */
    actual: Double?,
    scale: GradingScale,
    /** Con cuanto se aprueba: es lo que decide si la materia acaba de salir del rojo. */
    aprobado: Double
) {
    if (maxGrade <= 0.0) return
    val targetAt = (target / maxGrade).coerceIn(0.0, 1.0).toFloat()
    val actualAt = actual?.let { (it / maxGrade).coerceIn(0.0, 1.0).toFloat() }

    /*
     * **El punto se mueve; no se teletransporta.**
     *
     * Registrar una nota cambiaba `actual` y el punto aparecia en su sitio nuevo en el mismo
     * fotograma: se quitaba de un lado y salia en otro, sin que nada dijera hacia donde ni
     * cuanto. Ahora va con muelle desde donde estaba, y la primera vez entra desde cero,
     * que es la unica forma de que se vea que la barra **mide** algo.
     *
     * El rotulo «Llevas» cuelga del valor animado, asi que viaja con el punto.
     */
    val muelle = muelleDeMovimiento<Float>()
    val conMovimiento = hayMovimiento()
    val punto = remember { Animatable(0f) }
    LaunchedEffect(actualAt, conMovimiento) {
        if (actualAt == null) return@LaunchedEffect
        if (conMovimiento) punto.animateTo(actualAt, muelle) else punto.snapTo(actualAt)
    }
    val metaAnimada by animateFloatAsState(
        targetValue = targetAt,
        animationSpec = if (conMovimiento) muelle else snap(),
        label = "meta"
    )
    val puntoEn = if (actualAt == null) null else punto.value

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            /*
             * 16 dp, que es lo que mide el punto. Con 14 el `size(16.dp)` del punto se
             * apretaba a lo que cabia y salia un ovalo de 16 por 14: el «circulo
             * compactado» que se veia. `size` cede ante lo que le dejen; la caja tiene
             * que dejarle sitio.
             */
            .height(16.dp)
            .barridoDeRecuperacion(recuperada = actual != null && actual >= aprobado)
    ) {
        val fullWidth = maxWidth
        /*
         * **La barra va llena de punta a punta.**
         *
         * Se pintaba solo el tramo alcanzable sobre una canaleta gris, y ese gris parecia un
         * hueco por rellenar en vez de «hasta aqui no llegas»: una barra horizontal con un
         * trozo de color se lee como progreso. Pintada entera es lo que es, la escala de 0 al
         * maximo, y encima van las dos marcas que hay que leer.
         *
         * El degradado va sobre la escala completa y no sobre el tramo: estirado al tramo, el
         * mismo verde caia en el 4,8 de una materia y en el 3,2 de otra.
         */
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(10.dp)
                .clip(CircleShape)
                .background(
                    /*
                     * Rojo, ambar y verde: los tres colores con los que la app ya dice como va
                     * una nota. Estaba con `atRisk` (ambar) en el extremo bajo y `tertiary`
                     * —el azul del tema, que no significa nada aqui— en el medio, asi que la
                     * barra iba de oliva a azul a verde y no se parecia a nada de la escala.
                     */
                    Brush.horizontalGradient(
                        listOf(
                            LocalSectionColors.current.expenses,
                            LocalSectionColors.current.atRisk,
                            LocalSectionColors.current.onTrack
                        )
                    )
                )
        )
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = (fullWidth * metaAnimada - 1.5.dp).coerceAtLeast(0.dp))
                .width(3.dp)
                .fillMaxHeight()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
        )
        // Donde estas: lo unico que la barra no puede decir sola.
        if (puntoEn != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = (fullWidth * puntoEn - 8.dp).coerceIn(0.dp, fullWidth - 16.dp))
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface)
                    .padding(3.5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
            )
        }
    }

    /*
     * **«Estas en» cuelga del punto**, no repartido en la fila.
     *
     * Centrado entre los extremos decia otra cosa —el punto medio del rango—, que no es lo que
     * significa. Colgando del propio punto, el numero y la marca son lo mismo.
     */
    /*
     * **Cada marca dice lo que es, debajo y en su sitio.**
     *
     * La raya y el punto no significaban nada por si solos: hacia falta adivinar que uno era la
     * meta y el otro donde vas. Un grafico con marcas sin etiquetar no es un grafico.
     *
     * Van colocados por proporcion con `BiasAlignment` —el -1 es el borde izquierdo, el 0 el
     * centro— y no con margenes calculados a mano, que fue lo que partio en dos el rotulo la
     * vez anterior: suponia un ancho de 68 dp y «Estas en 4.70» mide bastante mas.
     */
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
        val anchoTotal = constraints.maxWidth
        // Si las dos marcas caen cerca, se calla la de la meta: dos rotulos encimados no se
        // leen ninguno, y el que interesa es el de donde vas.
        // Se decide con los valores finales, no con los animados: si no, el rotulo de la
        // meta parpadearia mientras el punto pasa por delante.
        val juntas = actualAt != null && kotlin.math.abs(actualAt - targetAt) < 0.16f
        if (!juntas) {
            MarcaDeLaBanda(
                texto = stringResource(R.string.subject_mark_goal, GradingScaleUtils.formatGrade(target, scale)),
                fuerte = false,
                fraccion = metaAnimada,
                anchoTotal = anchoTotal
            )
        }
        if (puntoEn != null && actual != null) {
            MarcaDeLaBanda(
                texto = stringResource(R.string.subject_mark_you, GradingScaleUtils.formatGrade(actual, scale)),
                fuerte = true,
                fraccion = puntoEn,
                anchoTotal = anchoTotal
            )
        }
    }

    // La regla contra la que se leen todas las cifras: los puntos enteros de la escala.
    BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(14.dp)) {
        val ancho = maxWidth
        val topes = maxGrade.toInt()
        for (v in 0..topes) {
            val donde = (v / maxGrade).toFloat()
            Text(
                GradingScaleUtils.formatGrade(v.toDouble(), scale),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f),
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = (ancho * donde - 11.dp).coerceIn(0.dp, ancho - 22.dp))
            )
        }
    }
}

/**
 * Aprobación, objetivo y lo que queda por evaluar.
 *
 * Los dos primeros iban en verde fijo, dijeran lo que dijeran los datos: la nota de
 * aprobación se pintaba igual de verde estando ya perdida. Ahora cada una se colorea según
 * siga estando a tiro con lo que falta.
 */
@Composable
private fun SubjectMetricsBand(
    calculation: SubjectGradeCalculation,
    passingGrade: Double,
    targetGrade: Double,
    remainingPercentage: Double,
    scale: GradingScale
) {
    // Los cuatro tonos se leen del tema aqui, en contexto composable, y la lambda solo
    // elige entre ellos: leer el tema dentro de la lambda la haria composable a ella.
    val toneUnknown = MaterialTheme.colorScheme.onSurfaceVariant
    val toneSecured = LocalSectionColors.current.onTrack
    val toneUnreachable = MaterialTheme.colorScheme.error
    val toneAtRisk = LocalSectionColors.current.atRisk
    val reachTone: (Double) -> Color = { threshold ->
        val floor = calculation.guaranteedMinimum
        val ceiling = calculation.bestPossible
        when {
            floor == null || ceiling == null -> toneUnknown
            floor >= threshold - 0.0001 -> toneSecured
            ceiling < threshold - 0.0001 -> toneUnreachable
            else -> toneAtRisk
        }
    }
    /*
     * **La del medio dice lo que llevas, no la meta.**
     *
     * La meta ya esta escrita dos veces en la pantalla —bajo el nombre de la materia y dentro
     * de la frase—, asi que su pildora era la tercera. Y lo que faltaba era justo lo contrario:
     * el promedio que llevas ahora mismo no aparecia en ningun sitio con su nombre, porque el
     * titular de arriba es **lo que necesitas**, que es otra cosa. De ahi que un 4,70 suelto
     * bajo un 3,90 grande no se entendiera.
     */
    SubjectMetricsBandContent(
        passingGrade = GradingScaleUtils.formatGrade(passingGrade, scale),
        passingTone = reachTone(passingGrade),
        targetGrade = calculation.currentAverage
            ?.let { GradingScaleUtils.formatGrade(it, scale) }
            ?: stringResource(R.string.subject_no_data_short),
        targetTone = calculation.currentAverage?.let {
            if (it >= passingGrade) toneSecured else LocalSectionColors.current.expenses
        } ?: toneUnknown,
        remainingPercentage = "${formatPercent(remainingPercentage)}%"
    )
}

@Composable
private fun SubjectMetricsBandContent(
    passingGrade: String,
    passingTone: Color,
    targetGrade: String,
    targetTone: Color,
    remainingPercentage: String
) {
    // Las mismas tarjetas que cuentan Materias, Hoy y Semana en Horario. Eran tres columnas
    // dentro de una banda separadas por dos líneas verticales, un recurso que no usa ninguna
    // otra pantalla; con la tarjeta, tres cifras se leen igual en toda la app.
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.CheckCircle,
            iconColor = passingTone,
            value = passingGrade,
            label = stringResource(R.string.subject_approval)
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Flag,
            iconColor = targetTone,
            value = targetGrade,
            label = stringResource(R.string.subject_you_carry)
        )
        MetricCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Rounded.Percent,
            iconColor = MaterialTheme.colorScheme.tertiary,
            value = remainingPercentage,
            label = stringResource(R.string.subject_to_evaluate)
        )
    }
}

@Composable
private fun SubjectInsightCard(
    calculation: SubjectGradeCalculation,
    targetGrade: Double,
    maxGrade: Double,
    scale: GradingScale,
    /**
     * Dentro del hero no lleva tarjeta propia.
     *
     * Como bloque suelto debajo era una tercera caja diciendo lo mismo que el titular: en el
     * prototipo la frase va **pegada al numero que explica**, y ahi es donde se lee sin tener
     * que relacionar dos cosas separadas por un hueco.
     */
    comoTarjeta: Boolean = true,
    modifier: Modifier = Modifier
) {
    // El color sale del mismo sitio que el mensaje. Antes la tarjeta era verde siempre,
    // así que «tu meta está en riesgo» se leía sobre fondo verde y con el número
    // resaltado también en verde: el color decía lo contrario que el texto.
    val tone = when (calculation.outlook) {
        TargetOutlook.NO_DATA -> MaterialTheme.colorScheme.onSurfaceVariant
        TargetOutlook.SECURED -> LocalSectionColors.current.onTrack
        TargetOutlook.ON_TRACK -> MaterialTheme.colorScheme.tertiary
        TargetOutlook.AT_RISK -> LocalSectionColors.current.atRisk
        TargetOutlook.UNREACHABLE -> MaterialTheme.colorScheme.error
    }

    val target = GradingScaleUtils.formatGrade(targetGrade, scale)
    val maxGradeFormatted = GradingScaleUtils.formatGrade(maxGrade, scale)
    val remainingPct = formatPercent(calculation.remainingSemesterFraction * 100)
    val insightsPrompt = stringResource(R.string.subject_insights_prompt)
    val closedWithPrefix = stringResource(R.string.subject_closed_with_prefix)
    val goalMetSuffix = stringResource(R.string.subject_goal_met_suffix, target)
    val goalSecuredPrefix = stringResource(R.string.subject_goal_secured_prefix)
    val goalSecuredMid = stringResource(R.string.subject_goal_secured_mid)
    val onTrackPrefix = stringResource(R.string.subject_on_track_prefix, target)
    val inRemainingPct = stringResource(R.string.subject_in_remaining_pct, remainingPct)
    val toStayThere = stringResource(R.string.subject_to_stay_there)
    val atRiskPrefix = stringResource(R.string.subject_at_risk_prefix)
    val toReachGoal = stringResource(R.string.subject_to_reach_goal, target)
    val closedBelowGoal = stringResource(R.string.subject_closed_below_goal, target)
    val unreachablePrefix = stringResource(R.string.subject_unreachable_prefix, target)
    val unreachableMid = stringResource(R.string.subject_unreachable_mid, maxGradeFormatted)

    val annotatedText = remember(
        calculation, targetGrade, maxGrade, scale, tone,
        insightsPrompt, closedWithPrefix, goalMetSuffix, goalSecuredPrefix,
        goalSecuredMid, onTrackPrefix, inRemainingPct, toStayThere,
        atRiskPrefix, toReachGoal, closedBelowGoal, unreachablePrefix, unreachableMid
    ) {
        val bold = SpanStyle(color = tone, fontWeight = FontWeight.Bold)
        buildAnnotatedString {
            when (calculation.outlook) {
                TargetOutlook.NO_DATA ->
                    append(insightsPrompt)

                TargetOutlook.SECURED -> {
                    // Con la materia cerrada, el mensaje habla en pasado. Antes «lo que falta
                    // para la meta» era null tanto sin notas como con todo evaluado, así que
                    // una materia terminada pedía registrar notas.
                    if (calculation.isFinished) {
                        append(closedWithPrefix)
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(goalMetSuffix)
                    } else {
                        append(goalSecuredPrefix)
                        withStyle(bold) { append(target) }
                        append(goalSecuredMid)
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(".")
                    }
                }

                TargetOutlook.ON_TRACK -> {
                    append(onTrackPrefix)
                    withStyle(bold) {
                        append(GradingScaleUtils.formatGrade(calculation.neededForTarget, scale))
                    }
                    append(inRemainingPct)
                    append(toStayThere)
                }

                TargetOutlook.AT_RISK -> {
                    append(atRiskPrefix)
                    withStyle(bold) {
                        append(GradingScaleUtils.formatGrade(calculation.neededForTarget, scale))
                    }
                    // «en los cortes restantes» era inexacto: lo que falta suele ser parte de
                    // un corte ya empezado, no cortes enteros.
                    append(inRemainingPct)
                    append(toReachGoal)
                }

                TargetOutlook.UNREACHABLE -> {
                    if (calculation.isFinished) {
                        append(closedWithPrefix)
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.guaranteedMinimum, scale))
                        }
                        append(closedBelowGoal)
                    } else {
                        append(unreachablePrefix)
                        append(unreachableMid)
                        withStyle(bold) {
                            append(GradingScaleUtils.formatGrade(calculation.bestPossible, scale))
                        }
                        append(".")
                    }
                }
            }
        }
    }

    val fila: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                // El icono también sigue al estado: una flecha al alza junto a un aviso
                // de meta en riesgo contradecía lo que decía el texto.
                when (calculation.outlook) {
                    TargetOutlook.SECURED -> Icons.Rounded.CheckCircle
                    TargetOutlook.AT_RISK, TargetOutlook.UNREACHABLE -> Icons.Rounded.PriorityHigh
                    else -> Icons.AutoMirrored.Rounded.TrendingUp
                },
                contentDescription = null,
                tint = if (comoTarjeta) tone else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = annotatedText,
                color = if (comoTarjeta) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onPrimaryContainer
                },
                fontSize = 13.sp,
                lineHeight = 18.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (comoTarjeta) {
        UniCard(
            modifier = modifier.fillMaxWidth(),
            color = tone.copy(alpha = 0.08f),
            shape = LargeCardShape,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
        ) { fila() }
    } else {
        Box(modifier = modifier.fillMaxWidth()) { fila() }
    }
}

/**
 * Las notas del corte, dentro de su tarjeta.
 *
 * Cada nota es su propia fila con fondo, como en el prototipo, y no filas separadas por lineas
 * dentro de una caja: asi la que llega **se abre hueco** entre las demas en vez de repintarse
 * la caja entera.
 *
 * Ese hueco es lo que hacia falta. Al insertarse una fila arriba, las de debajo saltaban de
 * golpe a su sitio nuevo y no habia nada que uniera el antes con el despues: se veia una lista
 * distinta, no una lista que ha cambiado. Aqui la fila nueva nace con altura cero y crece,
 * asi que las de abajo bajan **con ella** y el movimiento cuenta lo que ha pasado.
 */
/**
 * Un corte entero en una tarjeta: lo que lleva, lo que tiene dentro y lo que puedes hacer.
 *
 * Es la forma del prototipo. La franja de color corre por el lado de todo el conjunto —de la
 * cabecera y de las notas— porque es **un** corte, no una cabecera con una lista pegada
 * debajo; y cada nota es su propia fila con fondo, que es lo que deja que la nueva se abra
 * hueco entre las demas en vez de repintarse la caja.
 */
/**
 * El resumen del corte: la nota que lleva, cuanto se ha evaluado y en que estado esta.
 *
 * La misma forma que el hero de la materia —tarjeta rellena, cifra grande, una linea de
 * estado— para que las dos pantallas se lean igual. La cifra cuenta y lleva su flecha, porque
 * es el numero que cambia al registrar una nota y es donde hay que ver que ha cambiado.
 */
/*
 * **El texto del hero es blanco, no verde.**
 *
 * Iba en `onPrimaryContainer`, el verde que Material asigna al fondo verde de la tarjeta.
 * Se leia, pero dejaba la tarjeta entera en un solo tono y la cifra —lo unico que se viene
 * a mirar— no destacaba de su propio rotulo.
 *
 * `onSurface` es el mismo blanco del resto de la pantalla y sirve en los dos temas: casi
 * negro sobre el verde claro del tema claro, casi blanco sobre el verde oscuro.
 *
 * Lo que conserva su color es [FlechaDeCambio]: ahi el verde y el rojo **significan** subir
 * o bajar, no son decoracion del contenedor.
 */
/** Como quedo el corte al cerrarse, medido contra la meta y el aprobado. */
private enum class Veredicto { META, APROBADO, ROJO }

/** Nulo si no hay nota: un corte cerrado vacio no tiene balance que dar. */
private fun veredictoDelCierre(nota: Double?, meta: Double, aprobado: Double): Veredicto? = when {
    nota == null -> null
    nota >= meta -> Veredicto.META
    nota >= aprobado -> Veredicto.APROBADO
    else -> Veredicto.ROJO
}

/**
 * El balance del corte que se acaba de cerrar, en una tarjeta que se puede quitar.
 *
 * Misma forma que los avisos de la app —icono, titulo, un parrafo— y el color del estado:
 * verde si se cumplio la meta, ambar si se aprobo por debajo, rojo si quedo en rojo. Se puede
 * cerrar porque es un momento, no un dato: al volver a entrar ya no esta, y el estado del corte
 * lo siguen diciendo el sello y la cabecera.
 */
@Composable
private fun TarjetaDeVeredicto(
    veredicto: Veredicto,
    corte: String,
    nota: String,
    meta: String,
    aprobado: String,
    onCerrar: () -> Unit
) {
    val tono = when (veredicto) {
        Veredicto.META -> LocalSectionColors.current.onTrack
        Veredicto.APROBADO -> LocalSectionColors.current.atRisk
        Veredicto.ROJO -> LocalSectionColors.current.expenses
    }
    val icono = when (veredicto) {
        Veredicto.META -> Icons.Rounded.EmojiEvents
        Veredicto.APROBADO -> Icons.Rounded.Flag
        Veredicto.ROJO -> Icons.Rounded.PriorityHigh
    }
    val titulo = when (veredicto) {
        Veredicto.META -> stringResource(R.string.subject_close_verdict_goal_title)
        Veredicto.APROBADO -> stringResource(R.string.subject_close_verdict_pass_title)
        Veredicto.ROJO -> stringResource(R.string.subject_close_verdict_fail_title)
    }
    val cuerpo = when (veredicto) {
        Veredicto.META -> stringResource(R.string.subject_close_verdict_goal_body, corte, nota, meta)
        Veredicto.APROBADO -> stringResource(R.string.subject_close_verdict_pass_body, corte, nota, meta)
        Veredicto.ROJO -> stringResource(R.string.subject_close_verdict_fail_body, corte, nota, aprobado)
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(tono.copy(alpha = 0.13f))
            .padding(start = 14.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(tono.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icono, contentDescription = null, tint = tono, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f).padding(top = 2.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                titulo,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                cuerpo,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                lineHeight = 18.sp
            )
        }
        IconButton(onClick = onCerrar) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.action_close),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun HeroDelCorte(
    summary: CutSummary,
    maxGrade: Double,
    scale: GradingScale,
    passingGrade: Double,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    stringResource(R.string.subject_cut_grade_header),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = SectionLabelStyle
                )
                StatusBadge(status = summary.status)
            }
            if (summary.average == null) {
                Text(
                    stringResource(R.string.subject_unevaluated),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.headlineSmallEmphasized
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        GradingScaleUtils.formatGrade(
                            numeroQueCuenta(summary.average.toFloat(), "heroDelCorte").toDouble(),
                            scale
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.displaySmallEmphasized
                    )
                    Text(
                        " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                    )
                    FlechaDeCambio(summary.average, scale)
                }
            }
            /*
             * **Una sola pieza, no una por nota.**
             *
             * Estuvo partida en un trozo por nota, para que el 30 % se leyera como «un
             * taller de 20 y un quiz de 10». La informacion era buena y la lectura no: sin
             * rotulos, los cortes en la barra no explican de que son, y quien no la hizo
             * ve una barra rota. Y ponerles rotulo era repetir las filas de debajo, que ya
             * dicen exactamente eso con sus nombres y sus porcentajes.
             *
             * La barra se queda con el acento: es lo unico del hero que no es texto, y
             * pintarla del mismo blanco la habria dejado sin decir a que corte pertenece.
             */
            EvaluationBar(
                fraction = (summary.evaluated / 100.0).coerceIn(0.0, 1.0),
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)
            )
            Text(
                stringResource(R.string.subject_percent_evaluated, formatPercent(summary.evaluated)) +
                    stringResource(R.string.subject_remaining_to_distribute, formatPercent(100.0 - summary.evaluated)),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.80f),
                fontSize = 12.5.sp
            )
        }
    }
}

@Composable
private fun TarjetaDelCorte(
    summary: CutSummary,
    notas: List<GradeItem>,
    maxGrade: Double,
    scale: GradingScale,
    passingGrade: Double,
    cerrado: Boolean,
    onAgregar: () -> Unit,
    onCerrar: () -> Unit,
    onReabrir: () -> Unit,
    onEditar: (String) -> Unit,
    onBorrar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (cerrado) LocalSectionColors.current.onTrack else summary.status.color
    /*
     * Cual es la nota recien registrada.
     *
     * Se recuerda lo que habia y se compara: la que no estaba es la nueva. Sin esa
     * comparacion, todas entrarian animadas cada vez que se abre el corte y la entrada dejaria
     * de significar «acaba de pasar algo».
     */
    val vistasAntes = remember { mutableStateOf(notas.map { it.id }.toSet()) }
    val recienLlegada = notas.map { it.id }.firstOrNull { it !in vistasAntes.value }
    LaunchedEffect(notas.size) {
        kotlinx.coroutines.delay(1400)
        vistasAntes.value = notas.map { it.id }.toSet()
    }

    /*
     * **Un corte cerrado se ve cerrado, no solo lo dice.**
     *
     * La tarjeta quedaba identica a la de un corte abierto: el unico aviso eran la
     * insignia y una linea de texto al pie, y se seguia leyendo como algo con lo que hay
     * cosas que hacer. Atenuada dice de un vistazo que ahi ya no toca nada, y es la misma
     * atenuacion que la lista de la materia ya aplica a sus cortes cerrados.
     *
     * Con contraste alto se queda entera: bajar la opacidad es justo lo que esa
     * preferencia viene a evitar.
     */
    val contrasteAlto = LocalAccessibilityPreferences.current.highContrastEnabled
    val atenuada = if (cerrado && !contrasteAlto) 0.62f else 1f
    UniCard(
        modifier = modifier.fillMaxWidth().alpha(atenuada),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 16.dp, top = 14.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
                /*
                 * ---------------------------------------------------------- cabecera
                 *
                 * La franja va **solo al lado del corte**, no por todo el recuadro. Corriendo
                 * de arriba abajo parecia el borde de la tarjeta —un adorno— en vez de la marca
                 * de estado del corte, que es lo que es; y ademas competia con las filas de las
                 * notas, que ya tienen su propio fondo.
                 */
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .width(4.dp)
                            .fillMaxHeight()
                            .clip(CircleShape)
                            .background(accent)
                    )
                    Column(
                        modifier = Modifier.weight(1f).padding(start = 12.dp, end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                cutDisplayName(summary.cut),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            StatusBadge(status = summary.status)
                        }
                        Text(
                            stringResource(R.string.subject_weight_of_subject_simple, formatPercent(summary.cut.weight * 100)) +
                                stringResource(R.string.subject_remaining_to_distribute, formatPercent(100.0 - summary.evaluated)),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.5.sp
                        )
                    }
                    /*
                     * La cifra del corte **no se repite aqui**: ya la dice el resumen de
                     * arriba, en grande y con su flecha. Dos veces el mismo numero en la misma
                     * pantalla es lo que hacia que esto pareciera tres bloques y no uno.
                     */
                }

                // ---------------------------------------------------------- las notas y la accion
                NotasDelCorte(
                    notas = notas,
                    scale = scale,
                    passingGrade = passingGrade,
                    recienLlegada = recienLlegada,
                    completo = summary.status == CutStatus.COMPLETED,
                    cerrado = cerrado,
                    onAgregar = onAgregar,
                    onCerrar = onCerrar,
                    onReabrir = onReabrir,
                    onEditar = onEditar,
                    onBorrar = onBorrar
                )
        }
    }
}

@Composable
private fun NotasDelCorte(
    notas: List<GradeItem>,
    scale: GradingScale,
    passingGrade: Double,
    recienLlegada: String?,
    completo: Boolean,
    /** Ya lo diste por cerrado: no se agregan notas, se reabre. */
    cerrado: Boolean = false,
    onAgregar: () -> Unit,
    onCerrar: () -> Unit,
    onReabrir: () -> Unit = {},
    onEditar: (String) -> Unit,
    onBorrar: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        /*
         * **Lo que se ve no es exactamente lo que llega.**
         *
         * Una nota borrada desaparecia en el mismo fotograma en que se borraba y las
         * de abajo saltaban a su sitio nuevo. Se habia cuidado la llegada y se habia
         * dejado la marcha sin nada, que es justo lo que se notaba al deslizar una
         * fila: el dedo la sacaba con suavidad y el hueco se cerraba de golpe.
         *
         * Asi que la fila borrada se queda aqui mientras dura su salida y se retira
         * sola al acabarla. `notas` manda —quien esta y quien no—, y esta lista es lo
         * que hay pintado ahora mismo, que durante medio segundo es una fila mas.
         *
         * Por eso el «aqui no hay nada» mira esta lista y no `notas`: si mirara `notas`,
         * borrar la ultima nota cambiaria la fila por el vacio en el mismo fotograma, que
         * es justo el corte que se venia a quitar.
         */
        val visibles = remember { mutableStateListOf<GradeItem>().apply { addAll(notas) } }
        LaunchedEffect(notas) {
            notas.forEachIndexed { i, nota ->
                val donde = visibles.indexOfFirst { it.id == nota.id }
                when {
                    donde < 0 -> visibles.add(i.coerceAtMost(visibles.size), nota)
                    visibles[donde] != nota -> visibles[donde] = nota
                }
            }
        }
        if (visibles.isEmpty()) {
            EmptyCutNotesInline()
        } else {
            visibles.toList().forEach { grade ->
                val esNueva = grade.id == recienLlegada
                val sigue = notas.any { it.id == grade.id }
                /*
                 * **El estado de la transicion, no un `LaunchedEffect`.**
                 *
                 * Con `mutableStateOf(false)` mas un efecto que lo pone a `true`, el cambio
                 * llega **despues** de que la composicion se haya aplicado, y si algo mas la
                 * recompone en ese hueco la fila ya nace visible y no queda nada que animar:
                 * ahi es donde se perdia la entrada.
                 *
                 * `MutableTransitionState` lleva el «venia de false, va a true» dentro del
                 * propio estado, asi que la animacion arranca en la primera composicion pase lo
                 * que pase alrededor. Es la forma de animar una aparicion que no depende de que
                 * los tiempos cuadren.
                 */
                val estado = remember(grade.id) {
                    androidx.compose.animation.core.MutableTransitionState(!esNueva)
                }
                estado.targetState = sigue
                // Terminada la salida, la fila se quita a si misma de lo pintado.
                if (!sigue && estado.isIdle && !estado.currentState) {
                    SideEffect { visibles.removeAll { it.id == grade.id } }
                }
                /*
                 * **Un solo gesto, no dos encadenados.**
                 *
                 * El hueco se abria por un lado (320 ms) y la fila se deslizaba por otro, con
                 * su propio modificador y su propia duracion. Terminaba antes lo primero, asi
                 * que la nota **aparecia ya entera en su sitio** y despues se corria hacia la
                 * izquierda: no parecia que entrara, parecia que se acomodaba.
                 *
                 * Ahora el desplazamiento va dentro de la misma transicion que abre el hueco,
                 * asi que empiezan y acaban juntos: la fila llega **con** el hueco.
                 */
                AnimatedVisibility(
                    visibleState = estado,
                    enter = entradaDeNota(),
                    exit = salidaDeNota()
                ) {
                    /*
                     * **Deslizar para borrar, como en el resto de listas.**
                     *
                     * En Tareas y en Gastos una fila se va con el dedo; aqui habia que abrir un
                     * menu de tres puntos y elegir. Era la unica lista de la app que pedia dos
                     * toques para lo mismo, y quien ya conoce el gesto lo intenta igualmente y
                     * no pasa nada.
                     *
                     * El menu se queda: deslizar es el atajo de quien lo conoce, no el unico
                     * camino, y editar sigue sin tener gesto propio.
                     */
                    FilaDeslizable(
                        onBorrar = { onBorrar(grade.id) },
                        shape = MaterialTheme.shapes.medium,
                        /*
                         * El fondo de la tarjeta, debajo de la fila.
                         *
                         * La fila se pinta con un gris al 55 % —a proposito, para que se
                         * note que esta dentro del corte y no suelta— y sin nada opaco
                         * detras ese 55 % dejaba ver el rojo de borrar **a traves** de la
                         * nota mientras la arrastrabas.
                         */
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ) {
                        NotaDelCorte(
                            grade = grade,
                            scale = scale,
                            passingGrade = passingGrade,
                            onEditar = { onEditar(grade.id) },
                            onBorrar = { onBorrar(grade.id) }
                        )
                    }
                }
            }
        }

        if (cerrado) {
            Text(
                stringResource(R.string.subject_cut_closed_hint),
                color = LocalSectionColors.current.onTrack,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 3.dp)
            )
            TextButton(onClick = onReabrir, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.subject_reopen_cut), fontWeight = FontWeight.Bold)
            }
        } else if (completo) {
            /*
             * **El corte lleno no se cierra solo: lo ofrece.**
             *
             * Aparece al repartir el 100 % y se queda ahi hasta que lo pulses, asi que hay
             * margen para corregir una nota antes de fijar el corte. Y el sello, que se estampa
             * justo despues, significa algo: lo pusiste tu.
             */
            Text(
                stringResource(R.string.subject_cut_full_hint),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                modifier = Modifier.padding(top = 3.dp)
            )
            OutlinedButton(
                onClick = onCerrar,
                shapes = UniStackButtonDefaults.shapes,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = LocalSectionColors.current.onTrack
                ),
                border = BorderStroke(1.5.dp, LocalSectionColors.current.onTrack)
            ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.subject_close_cut_short), fontWeight = FontWeight.ExtraBold)
            }
        } else {
            UniStackButton(
                text = stringResource(R.string.subject_add_grade_short),
                onClick = onAgregar,
                leadingIcon = Icons.Rounded.Add,
                modifier = Modifier.fillMaxWidth().padding(top = 3.dp)
            )
        }
    }
}

/** Una nota, con su peso y su valor. La forma es la del prototipo: una fila con fondo propio. */
/**
 * Como entra una nota recien registrada, en **una sola** transicion.
 *
 * Lo importante es que el desplazamiento y la apertura del hueco viajen en la misma
 * `EnterTransition`, para que empiecen y terminen juntos; encadenar dos animaciones
 * distintas era lo que hacia que la fila apareciera ya puesta y **luego** se corriera.
 */
@Composable
private fun entradaDeNota(): androidx.compose.animation.EnterTransition {
    if (!hayMovimiento()) {
        return androidx.compose.animation.EnterTransition.None
    }
    // Cae y empuja: el hueco se abre y la fila baja a ocuparlo, en la misma transicion para
    // que empiecen y terminen juntos. Es la unica variante que quedo.
    val hueco = expandVertically(
        animationSpec = tween(durationMillis = 420),
        expandFrom = Alignment.Top
    )
    val aparece = fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 60))
    return hueco + aparece + slideInVertically(
        animationSpec = tween(durationMillis = 420)
    ) { alto -> -alto }
}

/**
 * Como se va una nota.
 *
 * No tiene variantes en Movimiento y no le hacen falta: la entrada es la que se elige y la
 * salida solo tiene que acompanarla. Dura mas que la entrada a proposito —irse tiene que
 * notarse menos que llegar— y cierra el hueco por arriba, en el mismo sentido en que la
 * entrada lo abre, para que la fila de abajo suba y no baje.
 */
@Composable
private fun salidaDeNota(): androidx.compose.animation.ExitTransition {
    if (!hayMovimiento()) {
        return androidx.compose.animation.ExitTransition.None
    }
    return shrinkVertically(
        animationSpec = tween(durationMillis = 520),
        shrinkTowards = Alignment.Top
    ) + fadeOut(animationSpec = tween(durationMillis = 300))
}

@Composable
private fun NotaDelCorte(
    grade: GradeItem,
    scale: GradingScale,
    passingGrade: Double,
    onEditar: () -> Unit,
    onBorrar: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.55f))
            .padding(horizontal = 13.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            grade.name,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.5.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Text(
            grade.contextLabel(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.5.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 10.dp)
        )
        Text(
            GradingScaleUtils.formatGrade(grade.value, scale),
            // Suspendida es roja, no ambar: el ambar es «ojo», no «esta por debajo».
            color = if (grade.value >= passingGrade) {
                LocalSectionColors.current.onTrack
            } else {
                LocalSectionColors.current.expenses
            },
            fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Box {
            UniIconButton(
                icon = Icons.Rounded.MoreVert,
                contentDescription = stringResource(R.string.subject_grade_options),
                onClick = { menu = true }
            )
            UniDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                // Con su icono, como en el resto de menus de la app: se perdieron al
                // reescribir la fila y el menu quedaba con dos textos sueltos.
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_edit)) },
                    leadingIcon = { Icon(Icons.Rounded.Edit, contentDescription = null) },
                    onClick = { menu = false; onEditar() }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.action_delete)) },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = { menu = false; onBorrar() }
                )
            }
        }
    }
    }
}

@Composable
private fun CutCard(
    summary: CutSummary,
    maxGrade: Double,
    scale: GradingScale,
    isActive: Boolean,
    needsHistory: Boolean,
    onClick: () -> Unit,
    dimmed: Boolean = false,
    /**
     * Un corte que todavia no ha empezado, en una linea.
     *
     * Con tres cortes en pantalla, dos de ellos «Sin evaluar · 0 %» y con su barra vacia, la
     * lista repetia tres veces la misma forma y el que estabas cursando no destacaba en nada.
     * Colapsado se sigue viendo que existe, cuanto pesa y que no ha empezado —que es todo lo
     * que hay que saber de el— y deja de ocupar el sitio del que si esta en juego.
     */
    compacto: Boolean = false,
    modifier: Modifier = Modifier
) {
    val progress = (summary.evaluated / 100.0).coerceIn(0.0, 1.0)
    val accent = when {
        needsHistory -> LocalSectionColors.current.atRisk
        isActive -> MaterialTheme.colorScheme.primary
        else -> summary.status.color
    }
    // Un corte cerrado se apaga un poco: sigue ahí para consultarlo, pero ya no compite por la
    // atención con los que están en juego. Con contraste alto no se atenúa nada, que es lo que
    // esa preferencia viene a pedir.
    val highContrast = LocalAccessibilityPreferences.current.highContrastEnabled
    val cardAlpha = if (dimmed && !highContrast) 0.62f else 1f
    UniCard(
        modifier = modifier
            .fillMaxWidth()
            .alpha(cardAlpha)
            .bounceClick(onClick),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = LargeCardShape,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(0.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left status indicator strip
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .fillMaxHeight()
                    .background(accent)
            )
            Column(modifier = Modifier.weight(1f)) {
            // Main content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left side: Texts and progress
                Column(
                    modifier = Modifier.weight(1f).padding(end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                cutDisplayName(summary.cut),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            // La insignia dice siempre el estado del corte. Antes, en el corte
                            // activo la ocupaba «Corte actual», así que de ese corte —el único
                            // que importa ahora mismo— no se podía saber si estaba pendiente,
                            // en curso o completado.
                            if (needsHistory) {
                                CustomStatusBadge(stringResource(R.string.subject_complete_history), LocalSectionColors.current.atRisk)
                            } else {
                                StatusBadge(status = summary.status)
                            }
                        }
                        Text(
                            buildString {
                                append(stringResource(R.string.subject_weight_of_subject_simple, formatPercent(summary.cut.weight * 100)))
                                if (isActive) append(stringResource(R.string.subject_current_cut_badge))
                            },
                            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                    if (!compacto) Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        if (summary.average == null) {
                            Text(
                                stringResource(R.string.subject_unevaluated),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 18.sp,
                                lineHeight = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        } else {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    GradingScaleUtils.formatGrade(summary.average, scale),
                                    // La cifra sigue al estado del corte, no a si es el corte
                                    // activo: ser el corte en curso no dice nada de la nota.
                                    color = summary.status.color,
                                    fontSize = 24.sp,
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    " / ${GradingScaleUtils.formatGrade(maxGrade, scale)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 2.dp, bottom = 3.dp)
                                )
                            }
                        }
                        Text(
                            stringResource(R.string.subject_percent_evaluated, formatPercent(summary.evaluated)),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        EvaluationBar(fraction = progress)
                    }
                }

                // Right side: Icon, notes count, chevron
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!compacto) Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(accent.copy(alpha = 0.15f), MaterialTheme.shapes.small),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                if (needsHistory) Icons.Rounded.Lightbulb else summary.status.icon(),
                                contentDescription = null,
                                tint = accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            gradeCountLabel(summary.grades.size),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            }
        }
    }
}

/**
 * El resumen del corte: qué nota lleva, en qué estado está y cuánto de su peso está repartido.
 *
 * Sin envoltorio teñido. La pantalla entera iba dentro de una tarjeta pintada con el color del
 * estado, así que un corte pendiente teñía de ámbar todo lo que había debajo y parecía un aviso.
 * El color del estado se queda donde significa algo: la insignia y la cifra.
 */
/**
 * Cuanto ha cambiado la nota, al lado de la nota.
 *
 * Antes esto se pintaba **dentro** del propio `Text` de la cifra, con `drawWithContent`: o caia
 * fuera de la caja del texto y lo recortaba la tarjeta, o caia encima del ultimo digito. Un
 * simbolo que acompana a un numero no es decoracion del numero: es otro elemento, y como tal se
 * coloca solo, en su hueco, sin pelearse con nada.
 *
 * Sube en verde y baja en rojo, y no sale cuando no ha cambiado —que es la unica noticia que no
 * es noticia—. Se retira sola a los pocos segundos: dice «acaba de pasar esto», no es un dato
 * permanente de la pantalla.
 */
@Composable
private fun FlechaDeCambio(
    promedio: Double?,
    scale: GradingScale,
    modifier: Modifier = Modifier
) {
    if (promedio == null) return
    var anterior by remember { mutableStateOf(promedio) }
    var delta by remember { mutableStateOf(0.0) }
    LaunchedEffect(promedio) {
        val cambio = promedio - anterior
        anterior = promedio
        // El umbral evita que un redondeo de milesimas dispare el gesto.
        if (kotlin.math.abs(cambio) >= 0.005) {
            delta = cambio
            kotlinx.coroutines.delay(2600)
            delta = 0.0
        }
    }

    val visible = delta != 0.0
    /*
     * **Lo que se pinta es el ultimo cambio de verdad, no el cero que lo apaga.**
     *
     * Al terminar se pone `delta = 0.0` para que la flecha se vaya, pero mientras dura la
     * salida el contenido se sigue dibujando: con el cero, `subio` pasaba a falso y la flecha
     * se despedia en rojo diciendo «−0,00». Un cambio de nada, marcado como una caida.
     *
     * Guardando aparte el ultimo valor distinto de cero, la flecha se va **como entro**: con su
     * signo, su color y su cifra.
     */
    var ultimo by remember { mutableStateOf(0.0) }
    if (delta != 0.0) ultimo = delta
    val subio = ultimo > 0
    /*
     * **Entra y sale animada; antes solo entraba.**
     *
     * Al terminar, el composable se retiraba de golpe: desaparecia entero en un fotograma y la
     * cifra de al lado **saltaba** a ocupar su hueco. Justo lo contrario de lo que la animacion
     * venia a contar, y lo que hacia que todo el gesto se sintiera cortado al final.
     *
     * Ahora la salida dura mas que la entrada —irse tiene que notarse menos que llegar— y el
     * hueco se cierra con ella, asi que el numero se desliza a su sitio en vez de dar el salto.
     */
    /*
     * Bajar se dice en **rojo**, no en ambar: `atRisk` es el ambar de «ojo con esto», y en una
     * caida se leia apagado. El rojo de la app es el de Gastos, el unico rojo de verdad de la
     * paleta —el `error` de Material tira a piel en oscuro y no dice peligro—.
     */
    val tinte = if (subio) LocalSectionColors.current.onTrack else LocalSectionColors.current.expenses

    /*
     * **Lo que hay que animar es el ANCHO, no la opacidad.**
     *
     * Ya se desvanecia al salir, pero la flecha y su cifra seguian ocupando su sitio entero
     * hasta el ultimo fotograma: cuando por fin desaparecian, el numero de al lado saltaba de
     * golpe los sesenta y pico pixeles que dejaban libres. De ahi el tiron del final, que es
     * justo lo que la salida venia a evitar.
     *
     * `expandHorizontally` / `shrinkHorizontally` animan **la medida**: el hueco se abre y se
     * cierra poco a poco y el numero se desliza acompanandolo. Y la salida dura mas que la
     * entrada, porque irse tiene que notarse menos que llegar.
     */
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = expandHorizontally(
            animationSpec = tween(durationMillis = 380),
            expandFrom = Alignment.Start
        ) + fadeIn(animationSpec = tween(durationMillis = 300, delayMillis = 80)) +
            slideInVertically(animationSpec = tween(durationMillis = 380)) { if (subio) it else -it },
        exit = shrinkHorizontally(
            animationSpec = tween(durationMillis = 560),
            shrinkTowards = Alignment.Start
        ) + fadeOut(animationSpec = tween(durationMillis = 380))
    ) {
    Row(
        modifier = Modifier.padding(start = 10.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            if (subio) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
            contentDescription = null,
            tint = tinte,
            modifier = Modifier.size(16.dp)
        )
        Text(
            (if (subio) "+" else "\u2212") +
                GradingScaleUtils.formatGrade(kotlin.math.abs(ultimo), scale),
            color = tinte,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
    }
}

// Inline non-card version for use inside a grouped card
@Composable
private fun GradeItem.contextLabel(): String {
    return when {
        source == GradeSource.PERIOD_FINAL -> stringResource(R.string.subject_official_cut_grade)
        weightStatus == GradeWeightStatus.UNKNOWN -> stringResource(R.string.subject_weight_pending)
        else -> stringResource(R.string.subject_weight_of_cut, formatPercent(percentage * 100))
    }
}

@Composable
private fun EmptyCutNotesInline() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(LargeCardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = if (LocalIsDarkTheme.current) 0.78f else 0.92f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, LargeCardShape)
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f), MaterialTheme.shapes.small),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.Assignment,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(stringResource(R.string.subject_no_grades_yet_title), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Text(
            stringResource(R.string.subject_no_grades_yet_desc),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            lineHeight = 17.sp
        )
    }
}

@Composable
private fun StatusBadge(status: CutStatus) {
    val statusColor = status.color
    Box(
        modifier = Modifier
            .background(statusColor, MaterialTheme.shapes.extraSmall)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            status.label.uppercase(Locale.forLanguageTag("es")),
            color = contentColorOn(statusColor),
            style = SectionLabelStyle.copy(fontSize = 9.sp, lineHeight = 12.sp, letterSpacing = 0.5.sp)
        )
    }
}

@Composable
private fun CustomStatusBadge(label: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), MaterialTheme.shapes.small)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

private data class CutSummary(
    val cut: GradingCut,
    val grades: List<GradeItem>,
    val average: Double?,
    val evaluated: Double,
    val status: CutStatus
)

private enum class CutStatus(@param:StringRes val labelRes: Int) {
    COMPLETED(R.string.subject_status_completed),
    IN_PROGRESS(R.string.subject_status_ongoing),
    PENDING(R.string.subject_status_pending);

    val label: String
        @Composable get() = stringResource(labelRes)
}

private val CutStatus.color: Color
    @Composable get() = when (this) {
        CutStatus.COMPLETED -> LocalSectionColors.current.onTrack
        CutStatus.IN_PROGRESS -> MaterialTheme.colorScheme.primary
        CutStatus.PENDING -> LocalSectionColors.current.atRisk
    }

private fun CutStatus.icon(): ImageVector {
    return when (this) {
        CutStatus.COMPLETED -> Icons.Rounded.CheckCircle
        CutStatus.IN_PROGRESS -> Icons.AutoMirrored.Rounded.Assignment
        CutStatus.PENDING -> Icons.AutoMirrored.Rounded.Assignment
    }
}

private fun GradingCut.toSummary(grades: List<GradeItem>): CutSummary {
    val calculation = GradeCalculator.calculateCut(grades)
    val evaluated = round(calculation.evaluatedFraction * 100.0 * 10.0) / 10.0
    val status = when {
        evaluated <= 0.001 -> CutStatus.PENDING
        evaluated >= 99.9 -> CutStatus.COMPLETED
        else -> CutStatus.IN_PROGRESS
    }
    return CutSummary(
        cut = this,
        grades = grades,
        average = calculation.average,
        evaluated = evaluated,
        status = status
    )
}

@Composable
private fun cutDisplayName(cut: GradingCut): String =
    cut.name.takeIf { it.isNotBlank() } ?: stringResource(R.string.subject_cut_order, cut.order)

@Composable
private fun gradeCountLabel(count: Int): String =
    if (count == 1) stringResource(R.string.subject_grade_count_single) else stringResource(R.string.subject_grade_count_multiple, count)

private fun formatPercent(value: Double): String = String.format(Locale.US, "%.0f", value)

private fun GradeType.icon(): ImageVector {
    return when (this) {
        GradeType.WORKSHOP,
        GradeType.PRACTICE -> Icons.AutoMirrored.Rounded.Assignment
        GradeType.PRESENTATION -> Icons.Rounded.School
        GradeType.EXAM,
        GradeType.QUIZ -> Icons.Rounded.CheckCircle
        GradeType.PROJECT,
        GradeType.RESEARCH,
        GradeType.OTHER -> Icons.Rounded.BarChart
    }
}

/** El nombre del tipo, el mismo que ofrece el formulario al elegirlo. */
@Composable
@ReadOnlyComposable
internal fun GradeType.labelLocal(): String = stringResource(
    when (this) {
        GradeType.WORKSHOP -> R.string.grade_type_workshop
        GradeType.PRESENTATION -> R.string.grade_type_presentation
        GradeType.QUIZ -> R.string.grade_type_quiz
        GradeType.EXAM -> R.string.grade_type_midterm
        GradeType.PROJECT -> R.string.grade_type_project
        GradeType.RESEARCH -> R.string.grade_type_research
        GradeType.PRACTICE -> R.string.grade_type_practice
        GradeType.OTHER -> R.string.grade_type_other
    }
)

@Composable
@ReadOnlyComposable
internal fun GradeType.colorLocal(): Color {
    return when (this) {
        GradeType.WORKSHOP,
        GradeType.PRACTICE -> LocalSectionColors.current.onTrack
        GradeType.PRESENTATION -> MaterialTheme.colorScheme.primary
        GradeType.EXAM,
        GradeType.QUIZ -> LocalSectionColors.current.atRisk
        GradeType.PROJECT,
        GradeType.RESEARCH -> LocalSectionColors.current.schedule
        GradeType.OTHER -> MaterialTheme.colorScheme.onSurfaceVariant
    }
}

@Composable
private fun SubjectTaskItem(
    task: StudentTask,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(
                        color = if (task.completed) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(6.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = if (task.completed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(6.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (task.completed) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (task.completed) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.completed) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                val subtaskProgress = if (task.subtasks.isNotEmpty()) {
                    " · ${task.subtasks.count { it.isCompleted }}/${task.subtasks.size}"
                } else ""
                Text(
                    text = "${task.type.name.lowercase().replaceFirstChar { it.uppercase() }} · ${task.estimatedMinutes} min$subtaskProgress",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            val dueText = formatTaskDueText(task.dueDateMillis)
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (!task.completed && task.dueDateMillis < System.currentTimeMillis()) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.surfaceContainerHigh
                }
            ) {
                Text(
                    text = if (task.gradingStatus == TaskGradingStatus.AWAITING_GRADE) {
                        stringResource(R.string.tasks_pill_awaiting_grade)
                    } else if (task.completed) {
                        stringResource(R.string.tasks_pill_done)
                    } else {
                        dueText
                    },
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                    color = if (!task.completed && task.dueDateMillis < System.currentTimeMillis()) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}
