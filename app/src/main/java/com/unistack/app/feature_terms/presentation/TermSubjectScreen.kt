@file:OptIn(ExperimentalLayoutApi::class)

package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Straighten
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.UniStackButtonVariant
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.NO_DATA
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_schedule.domain.ClassAttendanceStatus
import com.unistack.app.feature_schedule.presentation.AttendanceAbsent
import com.unistack.app.feature_schedule.presentation.AttendanceAttended
import com.unistack.app.feature_schedule.presentation.AttendanceCancelled
import com.unistack.app.feature_terms.domain.CalculoDelHistorico
import com.unistack.app.feature_terms.domain.HistorialAcademico
import com.unistack.app.feature_terms.domain.MateriaDelPeriodo
import com.unistack.app.feature_terms.domain.PeriodoDelHistorico
import com.unistack.app.feature_terms.domain.media
import com.unistack.app.feature_user.domain.GradingScale
import java.time.Instant
import java.time.ZoneId
import java.util.Locale

private enum class Pestana { Notas, Asistencia, Metricas }

/**
 * Una materia de un periodo: la B de la propuesta.
 *
 * Tres pestañas —notas por corte, asistencia semana a semana y métricas— con la nota final
 * arriba. Si es repetida, enlaza con el intento que se perdió.
 */
@Composable
fun TermSubjectScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onEditGrades: (String) -> Unit,
    onSubjectClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val historial = estado.historial
    val encontrada = historial?.materia(subjectId)
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val tope = estado.profile?.absenceLimit
    var pestana by rememberSaveable { mutableStateOf(Pestana.Notas) }

    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = scrollBottomRoom)
        ) {
            CabeceraDelHistorico(onBack = onBackClick) {
                UniIconButton(
                    icon = Icons.Rounded.Edit,
                    contentDescription = stringResource(R.string.hist_editar_notas),
                    onClick = { onEditGrades(subjectId) }
                )
            }
            if (historial == null || encontrada == null) return@Column
            val (periodo, materia) = encontrada
            TituloDelHistorico(
                titulo = materia.nombre,
                subtitulo = if (materia.esRepetida) stringResource(R.string.hist_materia_sub_repetida, periodo.nombre) else periodo.nombre
            )
            Pila {
                CabeceraDeMateria(materia, historial, escala, onSubjectClick)
                UniSegmentedControl(
                    selected = pestana,
                    options = listOf(
                        UniSegmentedOption(Pestana.Notas, stringResource(R.string.hist_pestana_notas)),
                        UniSegmentedOption(Pestana.Asistencia, stringResource(R.string.hist_pestana_asistencia)),
                        UniSegmentedOption(Pestana.Metricas, stringResource(R.string.hist_pestana_metricas))
                    ),
                    onSelected = { pestana = it }
                )
                when (pestana) {
                    Pestana.Notas -> {
                        PestanaDeNotas(materia, historial, escala)
                        UniStackButton(
                            text = stringResource(R.string.hist_editar_notas),
                            onClick = { onEditGrades(subjectId) },
                            variant = UniStackButtonVariant.Outlined,
                            leadingIcon = Icons.Rounded.Edit,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Pestana.Asistencia -> PestanaDeAsistencia(periodo, materia, tope)
                    Pestana.Metricas -> PestanaDeMetricas(periodo, materia, historial, escala, tope)
                }
            }
        }
        AvisosDelHistorico()
    }
}

@Composable
private fun CabeceraDeMateria(
    materia: MateriaDelPeriodo,
    historial: HistorialAcademico,
    escala: GradingScale,
    onSubjectClick: (String) -> Unit
) {
    val final = materia.final
    val par = tonoDeNota(final, historial.aprobado, historial.notaMaxima)
    Tarjeta(color = Paleta.acentoContenedor) {
        Fila(separacion = 16.dp) {
            GalletaConCifra(
                tamano = 80.dp,
                fondo = if (final == null) Paleta.alto else par.contenedor,
                texto = Formato.nota(final, escala),
                tinta = if (final == null) Paleta.apagado else par.sobre,
                letra = 22f
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Rotulo(stringResource(R.string.hist_nota_final), conMargen = false)
                when {
                    final == null -> Pastilla(stringResource(R.string.hist_sin_nota_final), Tono.Neutro)
                    materia.aprobada == true && materia.esRepetida -> Pastilla(stringResource(R.string.hist_aprobada_al_repetir), Tono.Ok)
                    materia.aprobada == true -> Pastilla(stringResource(R.string.hist_aprobada), Tono.Ok)
                    else -> Pastilla(stringResource(R.string.hist_perdida), Tono.Mal)
                }
                materia.intentoAnterior?.let { intento ->
                    Text(
                        text = stringResource(R.string.hist_la_primera_vez, Formato.nota(intento.final, escala), intento.termName),
                        style = estilo(16f, 24f, FontWeight.SemiBold),
                        color = Paleta.acento,
                        modifier = Modifier
                            .heightIn(min = 32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSubjectClick(intento.subjectId) }
                            .padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ================================================================== notas

@Composable
private fun PestanaDeNotas(materia: MateriaDelPeriodo, historial: HistorialAcademico, escala: GradingScale) {
    val zona = ZoneId.systemDefault()
    materia.cortes.forEach { corte ->
        Tarjeta(separacion = 10.dp) {
            Fila {
                Columna(
                    titulo = corte.cut.name,
                    sub = if (corte.desde != null && corte.hasta != null) {
                        stringResource(R.string.hist_vale_con_fechas, corte.pesoPorcentaje, Formato.diaMes(corte.desde), Formato.diaMes(corte.hasta))
                    } else {
                        stringResource(R.string.hist_vale, corte.pesoPorcentaje)
                    },
                    estiloTitulo = Letra.nombre
                )
                if (corte.nota == null) {
                    Pastilla(stringResource(R.string.hist_falta), Tono.Ambar)
                } else {
                    Text(Formato.nota(corte.nota, escala), style = Letra.numero(24f), color = Paleta.tinta)
                }
            }
            if (corte.notas.isEmpty()) {
                Text(stringResource(R.string.hist_corte_sin_notas), style = Letra.sub, color = Paleta.apagado)
            }
            corte.notas.forEach { nota ->
                val fecha = nota.recordedAt.takeIf { it > 0L }?.let { Formato.corta(Instant.ofEpochMilli(it).atZone(zona).toLocalDate()) }
                val peso = when {
                    nota.source == GradeSource.PERIOD_FINAL -> stringResource(R.string.hist_nota_del_corte)
                    nota.weightStatus == GradeWeightStatus.UNKNOWN -> stringResource(R.string.hist_peso_sin_saber)
                    else -> stringResource(R.string.hist_del_corte, Math.round(nota.percentage * 100))
                }
                Column(
                    modifier = Modifier.fillMaxWidth().lineaArriba(true),
                    verticalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Fila(modifier = Modifier.padding(top = 10.dp), separacion = 10.dp) {
                        Columna(titulo = nota.name, sub = if (fecha != null) "$peso · $fecha" else peso)
                        Text(Formato.nota(nota.value, escala), style = Letra.numero(16f, FontWeight.Bold), color = Paleta.tinta)
                    }
                    Barrita((nota.value / historial.notaMaxima).toFloat())
                }
            }
        }
    }
}

// ================================================================== asistencia

@Composable
private fun PestanaDeAsistencia(periodo: PeriodoDelHistorico, materia: MateriaDelPeriodo, tope: Int?) {
    val asistencia = materia.asistencia
    if (asistencia.clases.isEmpty()) {
        Tarjeta {
            Text(stringResource(R.string.hist_sin_clases_todavia), style = Letra.sub, color = Paleta.apagado)
        }
        return
    }
    val permitidas = tope?.let { (it - 1).coerceAtLeast(0) }
    Tarjeta(separacion = 12.dp) {
        Fila {
            Text(Formato.porcentaje(asistencia.porcentaje), style = Letra.cifraMedia, color = Paleta.tinta)
            Text(
                text = if (asistencia.sinMarcar > 0) {
                    stringResource(R.string.hist_de_clases_sin_marcar, asistencia.programadas, asistencia.sinMarcar)
                } else {
                    stringResource(R.string.hist_de_clases, asistencia.programadas)
                },
                style = Letra.sub,
                color = Paleta.apagado,
                modifier = Modifier.weight(1f)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Fila {
                Text(
                    text = if (permitidas != null) {
                        stringResource(R.string.hist_faltaste_de_permitidas, asistencia.faltas, permitidas)
                    } else {
                        stringResource(R.string.hist_faltaste, asistencia.faltas)
                    },
                    style = Letra.t15,
                    color = Paleta.tinta,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = if (tope != null) stringResource(R.string.hist_pierdes_con, tope) else stringResource(R.string.hist_sin_tope),
                    style = Letra.sub,
                    color = Paleta.apagado
                )
            }
            if (permitidas != null && permitidas > 0) {
                val uso = asistencia.faltas.toFloat() / permitidas
                Barrita(uso, color = if (uso >= 0.7f) AttendanceCancelled else Paleta.acento)
            }
        }
    }
    Tarjeta(separacion = 10.dp) {
        Text(stringResource(R.string.hist_semana_a_semana), style = Letra.nombre, color = Paleta.tinta)
        MapaDeSemanas(asistencia.clases, periodo.inicio, periodo.semanas)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(stringResource(R.string.hist_semana_n, 1), style = Letra.sub, color = Paleta.apagado)
            Text(stringResource(R.string.hist_semana_n, periodo.semanas), style = Letra.sub, color = Paleta.apagado)
        }
        Leyenda(
            listOfNotNull(
                AttendanceAttended to stringResource(R.string.hist_fuiste),
                AttendanceAbsent to stringResource(R.string.hist_faltaste_leyenda),
                AttendanceCancelled to stringResource(R.string.hist_no_hubo),
                if (asistencia.sinMarcar > 0) Paleta.alto to stringResource(R.string.hist_sin_marcar) else null
            )
        )
    }
    val faltas = asistencia.clases.filter { it.status == ClassAttendanceStatus.ABSENT }
    if (faltas.isNotEmpty()) {
        Rotulo(stringResource(R.string.hist_rotulo_faltaste))
        Fechas(faltas.map { Formato.corta(it.date) })
    }
    val noHubo = asistencia.clases.filter { it.status == ClassAttendanceStatus.CANCELLED || it.status == ClassAttendanceStatus.RESCHEDULED }
    if (noHubo.isNotEmpty()) {
        Rotulo(stringResource(R.string.hist_rotulo_no_hubo))
        Fechas(noHubo.map { Formato.corta(it.date) })
    }
}

@Composable
private fun Fechas(fechas: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        fechas.forEach { fecha ->
            Box(
                modifier = Modifier
                    .height(28.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Paleta.alto)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(fecha, style = Letra.sub, color = Paleta.tinta)
            }
        }
    }
}

// ================================================================== métricas

@Composable
private fun CabeceraDeTarjeta(icono: ImageVector, titulo: String, sub: String, tono: Tono = Tono.Acento) {
    Fila(separacion = 12.dp) {
        Azulejo(icono, tono)
        Columna(titulo = titulo, sub = sub, estiloTitulo = Letra.nombre)
    }
}

@Composable
private fun PestanaDeMetricas(
    periodo: PeriodoDelHistorico,
    materia: MateriaDelPeriodo,
    historial: HistorialAcademico,
    escala: GradingScale,
    tope: Int?
) {
    val evaluaciones = CalculoDelHistorico.evaluaciones(materia)
    if (evaluaciones.isEmpty()) {
        Tarjeta {
            Fila(separacion = 12.dp) {
                Azulejo(Icons.Rounded.BarChart, Tono.Acento)
                Text(stringResource(R.string.hist_estadisticas_con_notas), style = Letra.sub, color = Paleta.apagado, modifier = Modifier.weight(1f))
            }
        }
        return
    }
    val maxima = historial.notaMaxima
    val mejor = evaluaciones.maxBy { it.nota.value }
    val peor = evaluaciones.minBy { it.nota.value }
    val constancia = CalculoDelHistorico.constancia(evaluaciones.map { it.nota.value })

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        FilaDeMetricas {
            Metrica(Icons.Rounded.Star, Formato.nota(mejor.nota.value, escala), stringResource(R.string.hist_mejor_de, mejor.nota.name), Modifier.weight(1f).fillMaxHeight())
            Metrica(Icons.AutoMirrored.Rounded.TrendingDown, Formato.nota(peor.nota.value, escala), stringResource(R.string.hist_peor_de, peor.nota.name), Modifier.weight(1f).fillMaxHeight())
        }
        FilaDeMetricas {
            Metrica(
                Icons.Rounded.BarChart,
                constancia?.let { "±" + Formato.diferencia(it, maxima) } ?: NO_DATA,
                stringResource(R.string.hist_constancia),
                Modifier.weight(1f).fillMaxHeight()
            )
            Metrica(
                Icons.Rounded.AssignmentTurnedIn,
                if (materia.tareasTotal > 0) stringResource(R.string.hist_de, materia.tareasHechas, materia.tareasTotal) else NO_DATA,
                stringResource(R.string.hist_tareas),
                Modifier.weight(1f).fillMaxHeight()
            )
        }
    }

    // Tus notas en el tiempo
    val conFecha = evaluaciones.mapNotNull { e -> e.fecha?.let { NotaEnElTiempo(it, e.nota.value, e.corte.numero - 1) } }
    Tarjeta(separacion = 10.dp) {
        CabeceraDeTarjeta(
            Icons.AutoMirrored.Rounded.TrendingUp,
            stringResource(R.string.hist_notas_en_el_tiempo),
            stringResource(R.string.hist_notas_en_el_tiempo_sub, cuenta(evaluaciones.size, R.string.hist_evaluacion_una, R.string.hist_evaluacion_varias))
        )
        GraficaDeNotasEnElTiempo(conFecha, periodo.inicio, periodo.fin, materia.final, historial.aprobado, maxima)
        Leyenda(materia.cortes.mapIndexed { i, corte -> colorDeCorte(i) to corte.cut.name })
    }

    // Cortes frente al periodo
    val etiquetaCorte: (Int, Int) -> String = { n, peso -> Formato.etiquetaCorte(n, peso) }
    Tarjeta(separacion = 10.dp) {
        CabeceraDeTarjeta(
            Icons.Rounded.BarChart,
            stringResource(R.string.hist_cortes_frente_al_periodo),
            stringResource(R.string.hist_cortes_frente_al_periodo_sub, periodo.materias.size)
        )
        GraficaDeCortesFrenteAlPeriodo(
            mias = materia.cortes.map { it.nota },
            medias = materia.cortes.indices.map { k -> periodo.materias.mapNotNull { it.cortes.getOrNull(k)?.nota }.media() },
            pesos = materia.cortes.map { it.pesoPorcentaje },
            aprobado = historial.aprobado,
            maxima = maxima,
            etiquetaCorte = etiquetaCorte,
            formato = { Formato.nota(it, escala) }
        )
    }

    // Lo que aportó cada corte
    Tarjeta(separacion = 10.dp) {
        CabeceraDeTarjeta(
            Icons.Rounded.Straighten,
            stringResource(R.string.hist_lo_que_aporto),
            if (materia.final == null) {
                stringResource(R.string.hist_falta_nota_para_final)
            } else {
                stringResource(R.string.hist_suman_de, Formato.nota(materia.final, escala), notaMaximaTexto(maxima))
            }
        )
        GraficaDeAporte(
            aportes = materia.cortes.map { c -> c.nota?.let { it * c.cut.weight } },
            aprobado = historial.aprobado,
            maxima = maxima,
            textoAprobado = stringResource(R.string.hist_aprobado_minus)
        )
    }

    // Todas las evaluaciones
    Tarjeta(separacion = 10.dp) {
        CabeceraDeTarjeta(
            Icons.Rounded.AssignmentTurnedIn,
            stringResource(R.string.hist_todas_las_evaluaciones),
            stringResource(R.string.hist_todas_las_evaluaciones_sub)
        )
        TablaDeEvaluaciones(materia, evaluaciones, escala, maxima)
    }

    // Asistencia acumulada
    if (materia.asistencia.clases.isNotEmpty()) {
        val puntos = CalculoDelHistorico.asistenciaPorSemana(materia, periodo.inicio, periodo.semanas)
        val programadas = materia.asistencia.programadas
        val minimo = tope?.takeIf { programadas > 0 }?.let { ((programadas - (it - 1)).coerceAtLeast(0) * 100.0 / programadas).coerceIn(0.0, 100.0) }
        Tarjeta(separacion = 10.dp) {
            CabeceraDeTarjeta(
                Icons.Rounded.EventAvailable,
                stringResource(R.string.hist_asistencia_acumulada),
                buildString {
                    append(
                        stringResource(
                            if (periodo.cerrado) R.string.hist_acabo_en else R.string.hist_va_en,
                            Formato.porcentaje(materia.asistencia.porcentaje)
                        )
                    )
                    if (minimo != null) append(stringResource(R.string.hist_tope_deja_bajar, Formato.porcentaje(minimo)))
                },
                Tono.Ok
            )
            GraficaDeAsistenciaAcumulada(
                puntos = puntos,
                minimo = minimo,
                textoMinimo = minimo?.let { stringResource(R.string.hist_minimo, Formato.porcentaje(it)) },
                textoSemanaUno = stringResource(R.string.hist_semana_n_minus, 1),
                textoUltimaSemana = stringResource(R.string.hist_semana_n_minus, periodo.semanas)
            )
        }
    }

    // Frente a tus otras materias
    val otras = periodo.materias.filter { it.final != null }.sortedByDescending { it.final }
    Tarjeta(separacion = 10.dp) {
        CabeceraDeTarjeta(
            Icons.AutoMirrored.Rounded.MenuBook,
            stringResource(R.string.hist_frente_a_tus_materias, periodo.nombre),
            if (materia.final != null) {
                stringResource(R.string.hist_puesto_de, otras.indexOfFirst { it.id == materia.id } + 1, otras.size)
            } else {
                stringResource(R.string.hist_sin_final_todavia)
            }
        )
        otras.forEach { otra ->
            val yo = otra.id == materia.id
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Fila(separacion = 8.dp) {
                    Text(
                        otra.nombre,
                        style = if (yo) Letra.sub.copy(fontWeight = FontWeight.Bold) else Letra.sub,
                        color = if (yo) Paleta.tinta else Paleta.apagado,
                        modifier = Modifier.weight(1f)
                    )
                    Text(Formato.nota(otra.final, escala), style = Letra.numero(13f), color = if (yo) Paleta.tinta else Paleta.apagado)
                }
                Barrita(((otra.final ?: 0.0) / maxima).toFloat(), color = if (yo) Paleta.acento else Paleta.tope)
            }
        }
    }

    // Intentos
    materia.intentoAnterior?.let { intento ->
        Tarjeta(separacion = 10.dp) {
            CabeceraDeTarjeta(
                Icons.Rounded.Replay,
                stringResource(R.string.hist_intentos),
                stringResource(R.string.hist_intentos_sub, intento.termName),
                Tono.Mal
            )
            Fila(separacion = 10.dp) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(intento.termName, style = Letra.sub, color = Paleta.apagado)
                    Text(Formato.nota(intento.final, escala), style = Letra.numero(22f), color = AttendanceAbsent)
                }
                Text("→", style = estilo(22f, 28f), color = Paleta.apagado)
                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                    Text(periodo.nombre, style = Letra.sub, color = Paleta.apagado)
                    Text(
                        Formato.nota(materia.final, escala),
                        style = Letra.numero(22f),
                        color = if (materia.aprobada == true) AttendanceAttended else Paleta.tinta
                    )
                }
            }
        }
    }
}

@Composable
private fun TablaDeEvaluaciones(
    materia: MateriaDelPeriodo,
    evaluaciones: List<com.unistack.app.feature_terms.domain.EvaluacionDeMateria>,
    escala: GradingScale,
    maxima: Double
) {
    val cabecera = estilo(10.5f, 14f, FontWeight.Bold, 0.6f)
    val celda = estilo(13f, 18f, cifras = true)
    val fuerte = estilo(13f, 18f, FontWeight.ExtraBold, cifras = true)
    val decimales = if (maxima <= 10.0) "%.2f" else "%.1f"
    Column {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
            Text(stringResource(R.string.hist_tabla_evaluacion).uppercase(Locale.getDefault()), style = cabecera, color = Paleta.apagado, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.hist_tabla_peso).uppercase(Locale.getDefault()), style = cabecera, color = Paleta.apagado, textAlign = TextAlign.End, modifier = Modifier.width(52.dp))
            Text(stringResource(R.string.hist_tabla_nota).uppercase(Locale.getDefault()), style = cabecera, color = Paleta.apagado, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
            Text(stringResource(R.string.hist_tabla_aporta).uppercase(Locale.getDefault()), style = cabecera, color = Paleta.apagado, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
        }
        evaluaciones.forEach { e ->
            Row(
                modifier = Modifier.fillMaxWidth().lineaArriba(true).padding(vertical = 8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(8.dp).clip(RoundedCornerShape(2.dp)).background(colorDeCorte(e.corte.numero - 1)))
                        Spacer(Modifier.width(6.dp))
                        Text(e.nota.name, style = celda, color = Paleta.tinta)
                    }
                    e.fecha?.let { Text(Formato.corta(it), style = Letra.sub, color = Paleta.apagado) }
                }
                Text("${Math.round(e.pesoEnLaFinal)}%", style = celda, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(52.dp))
                Text(Formato.nota(e.nota.value, escala), style = fuerte, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                Text(String.format(Locale.US, decimales, e.aporte), style = celda, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
            }
        }
        materia.final?.let { final ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .lineaArribaFuerte()
                    .padding(vertical = 8.dp)
            ) {
                Text(stringResource(R.string.hist_final), style = fuerte, color = Paleta.tinta, modifier = Modifier.weight(1f))
                Text("100%", style = fuerte, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(52.dp))
                Text(Formato.nota(final, escala), style = fuerte, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(48.dp))
                Text(String.format(Locale.US, decimales, evaluaciones.sumOf { it.aporte }), style = fuerte, color = Paleta.tinta, textAlign = TextAlign.End, modifier = Modifier.width(60.dp))
            }
        }
    }
}

/** La raya más marcada que separa el total de la tabla. */
@Composable
private fun Modifier.lineaArribaFuerte(): Modifier {
    val color = Paleta.tope
    return this.drawBehind {
        val grosor = 1.dp.toPx()
        drawLine(color, Offset(0f, grosor / 2f), Offset(size.width, grosor / 2f), grosor)
    }
}
