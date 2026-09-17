@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.feature_terms.presentation.AvisosDelPeriodoEnConfiguracion
import com.unistack.app.core.design.theme.tonosDeAjustes
import com.unistack.app.feature_terms.presentation.HojaDelAvisoParaEmpezar
import com.unistack.app.feature_terms.presentation.TermsViewModel
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.EventBusy
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.ScaleZoneBar
import com.unistack.app.core.design.components.fechaLargaDeCorte
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_user.domain.Corte
import com.unistack.app.feature_user.domain.CutDateProblem
import com.unistack.app.feature_user.domain.GradingScale
import androidx.compose.runtime.saveable.listSaver
import java.time.LocalDate
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos

/**
 * Tu semestre, en puertas: el periodo, la escala, los cortes, las faltas y los días sin clase.
 *
 * Era una sola pantalla con las cuatro primeras cosas amontonadas y tres botones de «Guardar»
 * sueltos en medio del scroll -- cada uno guardaba solo su pedazo, y nada decía cuál faltaba
 * pulsar. Aquí es un hub, igual que **Ajustes** mismo lo es un nivel más arriba: cada fila dice
 * ahora mismo qué hay guardado y abre su propia pantalla, con un solo botón.
 *
 * «Tu periodo» solo aparece con un periodo activo: cerrado, no hay nada que corregir aquí, y
 * abrir uno nuevo es «Sin periodo activo» en Inicio, no esta pantalla.
 *
 * La franja de arriba se queda visible aunque la escala se mude a su pantalla: es la única
 * parte de esta sección que vale la pena ver de un vistazo, sin entrar a nada.
 */
@Composable
fun AcademicSettingsScreen(
    onBackClick: () -> Unit,
    onScaleClick: () -> Unit,
    onCutsClick: () -> Unit,
    onAbsenceClick: () -> Unit,
    onBreaksClick: () -> Unit,
    onTermClick: () -> Unit,
    modifier: Modifier = Modifier,
    onNewTermClick: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel(),
    termsViewModel: TermsViewModel = hiltViewModel()
) {
    val termsState by termsViewModel.uiState.collectAsStateWithLifecycle()
    var eligiendoDia by rememberSaveable { mutableStateOf(false) }
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val breaks by viewModel.academicBreaks.collectAsStateWithLifecycle()
    val activeTerm by viewModel.activeTerm.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val sections = LocalSectionColors.current
    val current = profile ?: return

    val maxGrade = if (current.gradingScale == GradingScale.CUSTOM) {
        current.customGradeMax.coerceIn(1.0, 100.0)
    } else {
        GradingScaleUtils.maxGradeFor(current.gradingScale)
    }
    val cuts = current.gradingCutScheme.cuts.sortedBy { it.order }

    LargeTitleScaffold(
        title = stringResource(R.string.settings_academic_title),
        subtitle = stringResource(R.string.settings_academic_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item {
            ScaleZoneBar(max = maxGrade, passing = current.passingGrade, target = current.targetAverage)
        }
        item {
            SettingsGroup(label = stringResource(R.string.settings_academic_sections), rowCount = if (activeTerm != null) 5 else 4) {
                activeTerm?.let { term ->
                    SettingsRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = stringResource(R.string.settings_academic_term_title),
                        subtitle = term.name + " · " + term.type.label +
                            (term.plannedEnd?.let { stringResource(R.string.settings_academic_term_until, fechaLargaDeCorte(it)) } ?: ""),
                        iconColor = tonosDeAjustes.azul,
                        onClick = onTermClick
                    )
                }
                SettingsRow(
                    icon = Icons.Rounded.BarChart,
                    title = stringResource(R.string.settings_academic_scale_title),
                    subtitle = stringResource(
                        R.string.settings_academic_scale_sub,
                        academicGradeInput(maxGrade, current.gradingScale),
                        academicGradeInput(current.passingGrade, current.gradingScale),
                        academicGradeInput(current.targetAverage, current.gradingScale)
                    ),
                    iconColor = tonosDeAjustes.indigo,
                    onClick = onScaleClick
                )
                SettingsRow(
                    icon = Icons.Rounded.PieChart,
                    title = stringResource(R.string.settings_academic_cuts_title),
                    subtitle = cuts.size.toString() + " " +
                        (if (cuts.size == 1) Corte.Singular else Corte.Plural).lowercase() +
                        " · " + cuts.joinToString("/") { academicPercentInput(it.weight) } + "%",
                    iconColor = tonosDeAjustes.violeta,
                    onClick = onCutsClick
                )
                SettingsRow(
                    icon = Icons.Rounded.PersonOff,
                    title = stringResource(R.string.settings_academic_absences_title),
                    subtitle = current.absenceLimit?.let { stringResource(R.string.settings_academic_absences_limit, it) } ?: stringResource(R.string.settings_academic_absences_none),
                    iconColor = tonosDeAjustes.ambar,
                    onClick = onAbsenceClick
                )
                SettingsRow(
                    icon = Icons.Rounded.EventBusy,
                    title = stringResource(R.string.settings_academic_breaks_title),
                    subtitle = if (breaks.isEmpty()) {
                        stringResource(R.string.settings_academic_breaks_none)
                    } else if (breaks.size == 1) {
                        stringResource(R.string.settings_academic_breaks_single)
                    } else {
                        stringResource(R.string.settings_academic_breaks_multiple, breaks.size)
                    },
                    iconColor = tonosDeAjustes.turquesa,
                    onClick = onBreaksClick
                )
            }
        }
        if (activeTerm == null) {
            item {
                SettingsGroup(label = stringResource(R.string.hist_tu_periodo), rowCount = 1) {
                    SettingsRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = stringResource(R.string.hist_sin_periodo_activo_titulo),
                        subtitle = termsState.lastClosed?.let { stringResource(R.string.hist_el_ultimo_fue, it.nombre) }
                            ?: stringResource(R.string.hist_empezar_uno),
                        iconColor = tonosDeAjustes.azul,
                        onClick = onNewTermClick
                    )
                }
            }
        }
        item {
            AcademicGroupLabel(stringResource(R.string.hist_avisos_del_periodo))
            AvisosDelPeriodoEnConfiguracion(
                estado = termsState,
                onAlAcabar = termsViewModel::setAvisoAlAcabar,
                onParaEmpezar = termsViewModel::setAvisoParaEmpezar,
                onElegirDia = { eligiendoDia = true }
            )
        }
    }

    if (eligiendoDia) {
        HojaDelAvisoParaEmpezar(
            estado = termsState,
            onDismiss = { eligiendoDia = false },
            onActivo = termsViewModel::setAvisoParaEmpezar,
            onOpcion = termsViewModel::setDiaDelAviso,
            onAbrirConfiguracion = null
        )
    }
}

/**
 * Que cambia segun haya fechas o no, dicho antes de tocarlas.
 *
 * Sin esto la seccion es una lista de dias sin consecuencia visible. Lo que se gana es
 * concreto: cada nota se va sola a su corte por la fecha en vez de elegirse a mano.
 */
@Composable
internal fun CutDatesExplainer(hasDates: Boolean) {
    Text(
        text = if (hasDates) {
            stringResource(R.string.settings_academic_cut_dates_note_dates, Corte.Singular.lowercase())
        } else {
            stringResource(R.string.settings_academic_cut_dates_note_nodates, Corte.Singular.lowercase())
        },
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

/** Por que estas fechas no se pueden guardar, en una linea. */
@Composable
internal fun CutDatesProblemNote(problem: CutDateProblem) {
    val corte = Corte.Singular.lowercase()
    Text(
        text = when (problem) {
            CutDateProblem.INCOMPLETAS ->
                stringResource(R.string.academic_faltan_fechas_o_estan_todas_o)
            CutDateProblem.DESORDENADAS ->
                stringResource(R.string.academic_cada_tiene_que_acabar_despues_del, corte)
            CutDateProblem.ANTES_DEL_INICIO ->
                stringResource(R.string.academic_el_primer_no_puede_acabar_antes, corte)
            CutDateProblem.DESPUES_DEL_FINAL ->
                stringResource(R.string.academic_el_ultimo_se_queda_sin_dias, corte)
        },
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Bold
    )
}

/** Las fechas sobreviven a un giro de pantalla; el `Bundle` solo entiende texto. */
internal val CutDatesSaver = listSaver<List<LocalDate?>, String>(
    save = { fechas -> fechas.map { it?.toString() ?: "" } },
    restore = { textos -> textos.map { texto -> texto.takeIf { it.isNotEmpty() }?.let(LocalDate::parse) } }
)

@Composable
internal fun AcademicGroupLabel(text: String) {
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
internal fun ScaleWarningNote() {
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
    append(stringResource(R.string.settings_academic_scale_warning_pre))
    pushStyle(
        androidx.compose.ui.text.SpanStyle(
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
    )
    append(stringResource(R.string.settings_academic_scale_warning_bold))
    pop()
    append(stringResource(R.string.settings_academic_scale_warning_post))
}

internal fun GradingScale.shortLabel(): String {
    return when (this) {
        GradingScale.ZERO_TO_FIVE -> "0 - 5"
        GradingScale.ZERO_TO_HUNDRED -> "0 - 100"
        GradingScale.CUSTOM -> Textos.get(R.string.setup_scale_range_other)
    }
}

internal fun academicGradeInput(value: Double, scale: GradingScale): String =
    GradingScaleUtils.formatGrade(value, scale)

internal fun academicPercentInput(weight: Double): String {
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
internal fun academicWeightsFor(count: Int, current: List<String>): List<String> {
    if (count <= 0) return current
    if (count <= current.size) return current.take(count)
    val even = 100.0 / count
    return List(count) { academicPercentInput(even / 100.0) }
}
