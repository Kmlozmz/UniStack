package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.components.duracionDeDeshacer
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.MateriaDelPeriodo
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_user.domain.GradingScale

/**
 * Un periodo: la opción A, con lo de compartir y editar de la C.
 *
 * Cerrado, el promedio frente al anterior, tres cifras y justo debajo las dos acciones del
 * boletín. En curso, el promedio es un rango —piso y techo con lo que falta— y, si ya terminó,
 * el botón para cerrarlo. Cada materia enseña sus cortes y abre su detalle.
 */
@Composable
fun TermDetailScreen(
    termId: String,
    onBackClick: () -> Unit,
    onSubjectClick: (String) -> Unit,
    onBulletinClick: (String) -> Unit,
    onEditGrades: (String) -> Unit,
    onCloseTermClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val historial = estado.historial
    val periodo = historial?.periodo(termId)
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    var eligiendo by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = scrollBottomRoom)
        ) {
            CabeceraDelHistorico(onBack = onBackClick) {
                if (periodo != null && periodo.cerrado) {
                    UniIconButton(
                        icon = Icons.Rounded.Share,
                        contentDescription = stringResource(R.string.hist_compartir_boletin),
                        onClick = { onBulletinClick(periodo.id) }
                    )
                }
            }
            if (historial == null || periodo == null) return@Column
            if (periodo.cerrado) {
                PeriodoCerrado(
                    periodo = periodo,
                    historial = historial,
                    escala = escala,
                    onSubjectClick = onSubjectClick,
                    onCompartir = { onBulletinClick(periodo.id) },
                    onEditar = { eligiendo = true }
                )
            } else {
                PeriodoEnCurso(
                    periodo = periodo,
                    historial = historial,
                    escala = escala,
                    onSubjectClick = onSubjectClick,
                    onCerrar = onCloseTermClick
                )
            }
        }
        AvisosDelHistorico()
    }

    if (eligiendo && periodo != null) {
        HojaDelHistorico(onDismiss = { eligiendo = false }) {
            TituloDeHoja(
                titulo = stringResource(R.string.hist_que_materia_editas),
                intro = stringResource(R.string.hist_que_materia_editas_intro, periodo.nombre)
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                periodo.materias.forEachIndexed { i, materia ->
                    FilaDeGrupo(primera = i == 0, ultima = i == periodo.materias.lastIndex, onClick = {
                        eligiendo = false
                        onEditGrades(materia.id)
                    }) {
                        Columna(
                            titulo = materia.nombre,
                            sub = stringResource(R.string.hist_final_de, Formato.nota(materia.final, escala))
                        )
                        Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, null, tint = Paleta.apagado, modifier = Modifier.size(22.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PeriodoCerrado(
    periodo: PeriodoDelHistorico,
    historial: HistorialAcademico,
    escala: GradingScale,
    onSubjectClick: (String) -> Unit,
    onCompartir: () -> Unit,
    onEditar: () -> Unit
) {
    val anterior = historial.anteriorA(periodo)
    val cerro = periodo.term.closedEpochDay?.let(java.time.LocalDate::ofEpochDay)
    TituloDelHistorico(
        titulo = periodo.nombre,
        subtitulo = stringResource(
            R.string.hist_periodo_cerrado_sub,
            stringResource(R.string.hist_rango_fechas, Formato.diaMes(periodo.inicio), Formato.diaMes(periodo.fin)),
            cerro?.let { Formato.diaMes(it) }.orEmpty(),
            cerro?.year ?: periodo.fin.year
        )
    )
    Pila {
        Tarjeta(color = Paleta.acentoContenedor) {
            Fila(separacion = 16.dp) {
                GalletaConCifra(88.dp, Paleta.acento, Formato.promedio(periodo.promedio, historial.notaMaxima), Paleta.sobreAcento, 22f)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Rotulo(stringResource(R.string.hist_promedio_del_periodo), conMargen = false)
                    if (anterior != null) {
                        PastillaDeDiferencia(periodo.promedio, anterior.promedio, anterior.nombre, historial.notaMaxima)
                    } else {
                        Text(stringResource(R.string.hist_tu_primer_periodo), style = Letra.sub, color = Paleta.apagado)
                    }
                }
            }
        }
        FilaDeMetricas {
            Metrica(Icons.Rounded.EventAvailable, Formato.porcentaje(periodo.asistencia), stringResource(R.string.hist_asistencia), Modifier.weight(1f).fillMaxHeight())
            Metrica(Icons.AutoMirrored.Rounded.MenuBook, periodo.materias.size.toString(), stringResource(R.string.hist_materias), Modifier.weight(1f).fillMaxHeight())
            Metrica(Icons.Rounded.Cancel, periodo.perdidas.size.toString(), stringResource(R.string.hist_perdidas), Modifier.weight(1f).fillMaxHeight())
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UniStackButton(
                text = stringResource(R.string.hist_compartir),
                onClick = onCompartir,
                variant = UniStackButtonVariant.Tonal,
                leadingIcon = Icons.Rounded.Share,
                modifier = Modifier.weight(1f)
            )
            UniStackButton(
                text = stringResource(R.string.hist_editar),
                onClick = onEditar,
                variant = UniStackButtonVariant.Outlined,
                leadingIcon = Icons.Rounded.Edit,
                modifier = Modifier.weight(1f)
            )
        }
        Rotulo(stringResource(R.string.hist_materias_cortes_al, periodo.materias.firstOrNull()?.cortes?.joinToString(" / ") { it.pesoPorcentaje.toString() } ?: ""))
        periodo.materias.forEach { materia ->
            FilaDeMateria(periodo, materia, historial, escala, onClick = { onSubjectClick(materia.id) })
        }
        Text(
            text = stringResource(R.string.hist_toca_una_materia),
            style = Letra.sub,
            color = Paleta.apagado,
            modifier = Modifier.padding(4.dp)
        )
    }
}

@Composable
private fun PeriodoEnCurso(
    periodo: PeriodoDelHistorico,
    historial: HistorialAcademico,
    escala: GradingScale,
    onSubjectClick: (String) -> Unit,
    onCerrar: () -> Unit
) {
    TituloDelHistorico(
        titulo = periodo.nombre,
        subtitulo = when {
            periodo.porEmpezar -> stringResource(R.string.hist_empieza_el, Formato.corta(periodo.inicio))
            periodo.terminado -> stringResource(R.string.hist_termino_sin_cerrar, Formato.corta(periodo.fin))
            else -> stringResource(R.string.hist_en_curso_frase)
        }
    )
    Pila {
        if (periodo.porEmpezar) {
            Tarjeta(color = Paleta.acentoContenedor) {
                Text(stringResource(R.string.hist_todavia_no_ha_empezado), style = Letra.nombre, color = Paleta.tinta)
                Text(
                    stringResource(R.string.hist_todavia_no_ha_empezado_sub),
                    style = Letra.sub,
                    color = Paleta.apagado,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        } else {
            val rango = historial.rangoDelPeriodo(periodo)
            val unico = rango == null || kotlin.math.abs(rango.second - rango.first) < 0.005
            Tarjeta(color = Paleta.acentoContenedor, separacion = 6.dp) {
                Rotulo(stringResource(R.string.hist_tu_promedio_del_periodo), conMargen = false)
                Text(
                    text = when {
                        rango == null -> Formato.promedio(null, historial.notaMaxima)
                        unico -> Formato.promedio(rango.first, historial.notaMaxima)
                        else -> "${Formato.promedio(rango.first, historial.notaMaxima)} – ${Formato.promedio(rango.second, historial.notaMaxima)}"
                    },
                    style = Letra.cifraMedia,
                    color = Paleta.tinta
                )
                Text(
                    stringResource(if (unico) R.string.hist_con_todas_las_notas else R.string.hist_piso_y_techo),
                    style = Letra.sub,
                    color = Paleta.apagado
                )
            }
            FilaDeMetricas {
                Metrica(Icons.Rounded.EventAvailable, Formato.porcentaje(periodo.asistencia), stringResource(R.string.hist_asistencia), Modifier.weight(1f).fillMaxHeight())
                Metrica(Icons.Rounded.AssignmentTurnedIn, "${periodo.completas}/${periodo.materias.size}", stringResource(R.string.hist_completas), Modifier.weight(1f).fillMaxHeight())
                Metrica(Icons.AutoMirrored.Rounded.MenuBook, periodo.materias.size.toString(), stringResource(R.string.hist_materias), Modifier.weight(1f).fillMaxHeight())
            }
            if (periodo.terminado) {
                UniStackButton(
                    text = stringResource(R.string.hist_revisar_y_cerrar, periodo.nombre),
                    onClick = onCerrar,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Rotulo(stringResource(R.string.hist_materias_rotulo))
        periodo.materias.forEach { materia ->
            FilaDeMateria(periodo, materia, historial, escala, onClick = { onSubjectClick(materia.id) })
        }
    }
}

/** Una materia en la lista de su periodo: su rueda, su final o lo que falta, y sus cortes. */
@Composable
internal fun FilaDeMateria(
    periodo: PeriodoDelHistorico,
    materia: MateriaDelPeriodo,
    historial: HistorialAcademico,
    escala: GradingScale,
    onClick: () -> Unit
) {
    val falta = materia.primerCorteQueFalta
    Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 14.dp), onClick = onClick) {
        Fila(separacion = 12.dp) {
            Rueda(ruedaDeNota(materia.final, historial.aprobado))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(materia.nombre, style = Letra.t15, color = Paleta.tinta)
                if (materia.esRepetida && !periodo.porEmpezar) {
                    Text(stringResource(R.string.hist_repetida), style = Letra.sub, color = Paleta.apagado)
                }
            }
            when {
                periodo.porEmpezar -> Pastilla(
                    stringResource(if (materia.esRepetida) R.string.hist_repetida else R.string.hist_nueva),
                    Tono.Neutro
                )
                falta != null -> Pastilla(stringResource(R.string.hist_falta_corte, falta + 1), Tono.Ambar)
                else -> Text(Formato.nota(materia.final, escala), style = Letra.numero(18f), color = Paleta.tinta)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            materia.cortes.forEach { corte ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Barrita(((corte.nota ?: 0.0) / historial.notaMaxima).toFloat())
                    Text(
                        stringResource(R.string.hist_corte_con_nota, corte.numero, Formato.nota(corte.nota, escala)),
                        style = Letra.corte,
                        color = Paleta.apagado,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/** Una fila de lista agrupada, con las esquinas de fuera redondeadas y las de dentro casi rectas. */
@Composable
internal fun FilaDeGrupo(
    primera: Boolean,
    ultima: Boolean,
    onClick: () -> Unit,
    contenido: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val arriba = if (primera) 24.dp else 6.dp
    val abajo = if (ultima) 24.dp else 6.dp
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Paleta.bajo, RoundedCornerShape(arriba, arriba, abajo, abajo))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = contenido
    )
}

/**
 * Los avisos del histórico en esta pantalla: guardados, cierres y deshacer.
 *
 * El mensaje se consume en cuanto se enseña, así que solo lo pinta la primera pantalla que lo ve.
 */
@Composable
internal fun BoxScope.AvisosDelHistorico(
    margenAbajo: Dp = 16.dp,
    onDeshacerCierre: ((String, String) -> Unit)? = null
) {
    val host = remember { SnackbarHostState() }
    val mensaje by MensajesDelHistorico.mensaje.collectAsStateWithLifecycle()
    val deshacer = stringResource(R.string.hist_deshacer)
    val duracionConDeshacer = duracionDeDeshacer()
    LaunchedEffect(mensaje?.id) {
        val actual = mensaje ?: return@LaunchedEffect
        MensajesDelHistorico.consumir(actual.id)
        val conDeshacer = actual.deshacerCierre != null && onDeshacerCierre != null
        val resultado = host.showSnackbar(
            message = actual.texto,
            actionLabel = if (conDeshacer) deshacer else null,
            duration = if (conDeshacer) duracionConDeshacer else SnackbarDuration.Short
        )
        if (resultado == SnackbarResult.ActionPerformed) {
            actual.deshacerCierre?.let { (id, nombre) -> onDeshacerCierre?.invoke(id, nombre) }
        }
    }
    SnackbarHost(
        hostState = host,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .padding(bottom = margenAbajo)
    )
}
