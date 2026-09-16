package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.NO_DATA
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_user.domain.GradingScale
import java.time.temporal.ChronoUnit

/**
 * El histórico académico: la opción A de la propuesta.
 *
 * Arriba el acumulado con su evolución —tocar un punto abre ese periodo—, luego el periodo en
 * curso con lo que puede quedar tu acumulado al cerrarlo, y los cerrados agrupados por año.
 */
@Composable
fun AcademicHistoryScreen(
    onBackClick: () -> Unit,
    onTermClick: (String) -> Unit,
    onCloseTermClick: () -> Unit,
    onScheduleClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val historial = estado.historial
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE

    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = scrollBottomRoom)
        ) {
            CabeceraDelHistorico(onBack = onBackClick)
            if (historial == null) return@Column
            val primero = historial.periodos.minByOrNull { it.term.startEpochDay }
            TituloDelHistorico(
                titulo = stringResource(R.string.hist_titulo),
                subtitulo = primero?.let {
                    stringResource(
                        R.string.hist_subtitulo,
                        cuenta(historial.periodos.size, R.string.hist_periodo_uno, R.string.hist_periodo_varios),
                        Formato.mes(it.inicio),
                        it.inicio.year
                    )
                }
            )
            Pila {
                TarjetaDelAcumulado(historial = historial, onTermClick = onTermClick)
                historial.activo?.let { activo ->
                    Rotulo(stringResource(R.string.hist_en_curso))
                    TarjetaEnCurso(
                        periodo = activo,
                        historial = historial,
                        onClick = { onTermClick(activo.id) },
                        onCerrar = onCloseTermClick,
                        onHorario = onScheduleClick
                    )
                }
                historial.cerrados.groupBy { it.inicio.year }.toSortedMap(compareByDescending { it }).forEach { (anio, periodos) ->
                    Rotulo(anio.toString())
                    periodos.forEach { periodo ->
                        FilaDePeriodo(periodo = periodo, historial = historial, escala = escala, onClick = { onTermClick(periodo.id) })
                    }
                }
                Text(
                    text = stringResource(R.string.hist_cerrado_no_es_bloqueado),
                    style = Letra.sub,
                    color = Paleta.apagado,
                    modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun TarjetaDelAcumulado(historial: HistorialAcademico, onTermClick: (String) -> Unit) {
    Tarjeta(color = Paleta.acentoContenedor, separacion = 6.dp) {
        Rotulo(stringResource(R.string.hist_promedio_acumulado), conMargen = false)
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(Formato.promedio(historial.acumulado, historial.notaMaxima), style = Letra.cifra, color = Paleta.tinta)
            Text(
                text = stringResource(
                    R.string.hist_acumulado_detalle,
                    notaMaximaTexto(historial.notaMaxima),
                    cuenta(historial.cerrados.size, R.string.hist_cerrado_uno, R.string.hist_cerrado_varios),
                    cuenta(historial.notasFinales, R.string.hist_nota_final_una, R.string.hist_nota_final_varias)
                ),
                style = Letra.sub,
                color = Paleta.apagado,
                modifier = Modifier.padding(bottom = 9.dp)
            )
        }
        if (historial.cerrados.isNotEmpty()) {
            GraficaDelHistorico(
                serie = historial.serie,
                nombreActivo = historial.activo?.takeIf { !it.porEmpezar }?.nombre,
                rangoActivo = historial.activo?.takeIf { !it.porEmpezar }?.let { historial.rangoDelAcumulado(it) },
                aprobado = historial.aprobado,
                maxima = historial.notaMaxima,
                onPunto = onTermClick
            )
            val conRango = historial.activo?.let { !it.porEmpezar } == true
            Leyenda(
                elementos = listOfNotNull(
                    Paleta.acento to stringResource(R.string.hist_leyenda_del_periodo),
                    Paleta.apagado to stringResource(R.string.hist_leyenda_acumulado),
                    if (conRango) Paleta.acento.copy(alpha = 0.45f) to stringResource(R.string.hist_leyenda_puede_quedar) else null
                ),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** El periodo en curso: su avance, tres cifras y lo que puede quedar tu acumulado al cerrarlo. */
@Composable
internal fun TarjetaEnCurso(
    periodo: PeriodoDelHistorico,
    historial: HistorialAcademico,
    onClick: () -> Unit,
    onCerrar: () -> Unit,
    onHorario: () -> Unit
) {
    val hoy = historial.hoy
    val futuro = periodo.porEmpezar
    val termino = periodo.terminado
    val estadoTexto = when {
        futuro -> stringResource(R.string.hist_empieza_en, Formato.corta(periodo.inicio), ChronoUnit.DAYS.between(hoy, periodo.inicio))
        termino -> stringResource(R.string.hist_termino_hace, Formato.corta(periodo.fin), ChronoUnit.DAYS.between(periodo.fin, hoy))
        else -> stringResource(R.string.hist_rango_fechas, Formato.diaMes(periodo.inicio), Formato.diaMes(periodo.fin))
    }
    val fraccion = when {
        futuro -> 0f
        termino -> 1f
        else -> (ChronoUnit.DAYS.between(periodo.inicio, hoy).toFloat() / ChronoUnit.DAYS.between(periodo.inicio, periodo.fin).coerceAtLeast(1)).coerceIn(0f, 1f)
    }

    Tarjeta(color = Paleta.superficie, separacion = 12.dp, onClick = onClick) {
        Fila {
            Columna(titulo = periodo.nombre, sub = estadoTexto, estiloTitulo = Letra.nombre)
            when {
                futuro -> Pastilla(stringResource(R.string.hist_por_empezar), Tono.Neutro, Modifier.align(Alignment.Top))
                termino -> Pastilla(stringResource(R.string.hist_listo_para_cerrar), Tono.Acento, Modifier.align(Alignment.Top))
            }
        }
        Onda(fraccion)
        FilaDeMetricas {
            Metrica(
                icono = Icons.Rounded.AssignmentTurnedIn,
                valor = if (futuro) NO_DATA else "${periodo.completas}/${periodo.materias.size}",
                etiqueta = stringResource(if (futuro) R.string.hist_sin_notas else R.string.hist_completas),
                fondo = Paleta.alto,
                modifier = Modifier.weight(1f).fillMaxHeightCompat()
            )
            Metrica(
                icono = Icons.Rounded.EventAvailable,
                valor = if (futuro) NO_DATA else Formato.porcentaje(periodo.asistencia),
                etiqueta = stringResource(R.string.hist_asistencia),
                fondo = Paleta.alto,
                modifier = Modifier.weight(1f).fillMaxHeightCompat()
            )
            Metrica(
                icono = Icons.AutoMirrored.Rounded.MenuBook,
                valor = periodo.materias.size.toString(),
                etiqueta = stringResource(R.string.hist_materias),
                fondo = Paleta.alto,
                modifier = Modifier.weight(1f).fillMaxHeightCompat()
            )
        }
        if (!futuro) {
            historial.rangoDelAcumulado(periodo)?.let { (piso, techo) ->
                val bajo = Formato.promedio(piso, historial.notaMaxima)
                val alto = Formato.promedio(techo, historial.notaMaxima)
                val texto = if (kotlin.math.abs(techo - piso) < 0.005) {
                    val plantilla = stringResource(R.string.hist_acumulado_quedara_en, "\u0000")
                    negrita(plantilla, bajo)
                } else {
                    val plantilla = stringResource(R.string.hist_acumulado_quedara_entre, "\u0000", notaMaximaTexto(historial.notaMaxima))
                    negrita(plantilla, stringResource(R.string.hist_entre_y, bajo, alto))
                }
                Text(texto, style = Letra.sub, color = Paleta.tinta)
            }
        }
    }
    when {
        futuro -> UniStackButton(
            text = stringResource(R.string.hist_poner_horario),
            onClick = onHorario,
            variant = UniStackButtonVariant.Tonal,
            modifier = Modifier.fillMaxWidth()
        )
        termino -> UniStackButton(
            text = stringResource(R.string.hist_revisar_y_cerrar, periodo.nombre),
            onClick = onCerrar,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun FilaDePeriodo(periodo: PeriodoDelHistorico, historial: HistorialAcademico, escala: GradingScale, onClick: () -> Unit) {
    val par = tonoDeNota(periodo.promedio, historial.aprobado, historial.notaMaxima)
    Tarjeta(onClick = onClick) {
        Fila {
            GalletaConCifra(52.dp, par.contenedor, Formato.promedio(periodo.promedio, historial.notaMaxima), par.sobre, 14f)
            Columna(
                titulo = periodo.nombre,
                sub = stringResource(
                    R.string.hist_fila_periodo,
                    stringResource(R.string.hist_rango_fechas, Formato.diaMes(periodo.inicio), Formato.diaMes(periodo.fin)),
                    cuenta(periodo.materias.size, R.string.hist_materia_una, R.string.hist_materia_varias),
                    Formato.porcentaje(periodo.asistencia)
                ),
                estiloTitulo = Letra.nombre
            )
            if (periodo.perdidas.isNotEmpty()) {
                Pastilla(cuenta(periodo.perdidas.size, R.string.hist_perdida_una, R.string.hist_perdida_varias), Tono.Mal)
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = Paleta.apagado,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ================================================================== ayudas de texto

/** «1 periodo» o «4 periodos», con la cifra dentro de la cadena. */
@Composable
internal fun cuenta(n: Int, uno: Int, varios: Int): String =
    if (n == 1) stringResource(uno) else stringResource(varios, n)

/** «5.0», «100»: el máximo de la escala tal como se escribe. */
internal fun notaMaximaTexto(maxima: Double): String =
    if (maxima <= 10.0) String.format(java.util.Locale.US, "%.1f", maxima) else String.format(java.util.Locale.US, "%.0f", maxima)

/** Sustituye el marcador nulo de [plantilla] por [resaltado] en negrita. */
internal fun negrita(plantilla: String, resaltado: String) = buildAnnotatedString {
    val partes = plantilla.split("\u0000")
    append(partes.first())
    if (partes.size > 1) {
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(resaltado) }
        append(partes.drop(1).joinToString(""))
    }
}

/** Que las cifras de una fila compartan alto aunque una tenga dos líneas. */
internal fun Modifier.fillMaxHeightCompat(): Modifier = this.fillMaxHeight()
