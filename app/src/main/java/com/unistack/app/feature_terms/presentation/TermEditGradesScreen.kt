package com.unistack.app.feature_terms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.UniStackButton
import com.unistack.app.core.design.components.rememberLeaveGuard
import com.unistack.app.feature_grades.domain.GradeSource
import com.unistack.app.feature_grades.domain.GradeWeightStatus
import com.unistack.app.feature_user.domain.GradingScale
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

/**
 * Editar las notas de una materia, a pantalla completa como los formularios de la app.
 *
 * Mientras se escribe se recalculan el corte, la final y el promedio del periodo. Un corte sin
 * ninguna nota ofrece la del corte entera, que es lo que suele llegar tarde de un semestre
 * cerrado. Salir con cambios pregunta antes de descartar.
 */
@Composable
fun TermEditGradesScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TermsViewModel = hiltViewModel()
) {
    val estado by viewModel.uiState.collectAsStateWithLifecycle()
    val encontrada = estado.historial?.materia(subjectId)
    val escala = estado.profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxima = estado.historial?.notaMaxima ?: 5.0

    /* Lo escrito, por id de nota; y para los cortes vacíos, por id de corte con «corte:» delante. */
    var textos by rememberSaveable { mutableStateOf(HashMap<String, String>()) }
    var iniciado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(encontrada != null) {
        val materia = encontrada?.second ?: return@LaunchedEffect
        if (iniciado) return@LaunchedEffect
        textos = HashMap(materia.subject.grades.associate { it.id to Formato.nota(it.value, escala) })
        iniciado = true
    }

    val materia = encontrada?.second
    val periodo = encontrada?.first
    val leidos = textos.mapValues { (_, texto) -> leerNota(texto, maxima) }
    val originales = materia?.subject?.grades?.associate { it.id to it.value }.orEmpty()
    val valores = leidos.filterKeys { !it.startsWith(PREFIJO_CORTE) }
        .mapNotNull { (id, v) -> if (v == null || v.isNaN()) null else id to v }.toMap()
    val nuevas = leidos.filterKeys { it.startsWith(PREFIJO_CORTE) }
        .mapNotNull { (id, v) -> if (v == null || v.isNaN()) null else id.removePrefix(PREFIJO_CORTE) to v }.toMap()
    val invalido = leidos.any { (id, v) -> (v != null && v.isNaN()) || (!id.startsWith(PREFIJO_CORTE) && v == null) }
    val hayCambios = valores.any { (id, v) -> originales[id]?.let { abs(it - v) > 0.0001 } == true } || nuevas.isNotEmpty()
    val vista = remember(valores, nuevas, estado) { viewModel.vistaPrevia(subjectId, valores, nuevas) }

    val salir = rememberLeaveGuard(
        hasUnsavedChanges = hayCambios,
        onLeave = onBackClick,
        title = stringResource(R.string.hist_descartar_titulo),
        message = stringResource(R.string.hist_descartar_mensaje)
    )

    Box(modifier = modifier.fillMaxSize().background(Paleta.fondo)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 132.dp)
        ) {
            CabeceraDelHistorico(onBack = salir, cerrar = true)
            if (materia == null || periodo == null) return@Column
            TituloDelHistorico(
                titulo = stringResource(R.string.hist_editar_notas),
                subtitulo = stringResource(R.string.hist_materia_y_periodo, materia.nombre, periodo.nombre)
            )
            val zona = ZoneId.systemDefault()
            Pila {
                materia.cortes.forEachIndexed { k, corte ->
                    Rotulo(stringResource(R.string.hist_corte_vale, corte.cut.name, corte.pesoPorcentaje))
                    Tarjeta(relleno = PaddingValues(horizontal = 18.dp, vertical = 6.dp)) {
                        if (corte.notas.isEmpty()) {
                            val clave = PREFIJO_CORTE + corte.cut.id
                            Fila(modifier = Modifier.padding(vertical = 10.dp)) {
                                Columna(titulo = stringResource(R.string.hist_nota_del_corte), sub = stringResource(R.string.hist_nota_del_corte_sub))
                                EntradaDeNota(
                                    valor = textos[clave].orEmpty(),
                                    onValor = { nuevo -> textos = HashMap(textos).apply { put(clave, nuevo) } },
                                    invalida = leidos[clave]?.isNaN() == true,
                                    descripcion = stringResource(R.string.hist_nota_del_corte)
                                )
                            }
                        }
                        corte.notas.forEachIndexed { i, nota ->
                            val fecha = nota.recordedAt.takeIf { it > 0L }?.let { Formato.corta(Instant.ofEpochMilli(it).atZone(zona).toLocalDate()) }
                            val peso = when {
                                nota.source == GradeSource.PERIOD_FINAL -> stringResource(R.string.hist_nota_del_corte)
                                nota.weightStatus == GradeWeightStatus.UNKNOWN -> stringResource(R.string.hist_peso_sin_saber)
                                else -> stringResource(R.string.hist_del_corte, Math.round(nota.percentage * 100))
                            }
                            Fila(modifier = Modifier.lineaArriba(i > 0).padding(vertical = 10.dp)) {
                                Columna(titulo = nota.name, sub = if (fecha != null) "$peso · $fecha" else peso)
                                EntradaDeNota(
                                    valor = textos[nota.id].orEmpty(),
                                    onValor = { nuevo -> textos = HashMap(textos).apply { put(nota.id, nuevo) } },
                                    invalida = leidos[nota.id].let { it == null || it.isNaN() } && iniciado,
                                    descripcion = nota.name
                                )
                            }
                        }
                        Fila(modifier = Modifier.lineaArriba(true).padding(vertical = 10.dp)) {
                            Text(stringResource(R.string.hist_nota_del_corte), style = Letra.sub, color = Paleta.apagado, modifier = Modifier.weight(1f))
                            Text(
                                Formato.nota(vista?.cortes?.getOrNull(k), escala),
                                style = estilo(14f, 20f, FontWeight.Bold, cifras = true),
                                color = Paleta.tinta
                            )
                        }
                    }
                }
                Tarjeta(color = Paleta.acentoContenedor, separacion = 4.dp) {
                    Fila {
                        Text(stringResource(R.string.hist_final), style = Letra.nombre, color = Paleta.tinta, modifier = Modifier.weight(1f))
                        Text(Formato.nota(vista?.final, escala), style = Letra.numero(24f), color = Paleta.tinta)
                    }
                    val antes = vista?.promedioAntes
                    val despues = vista?.promedioDespues
                    Text(
                        text = when {
                            despues == null -> stringResource(R.string.hist_efecto_sin_promedio, periodo.nombre)
                            antes == null || abs(despues - antes) < 0.0005 ->
                                stringResource(R.string.hist_efecto_se_queda, periodo.nombre, Formato.promedio(despues, maxima))
                            else -> stringResource(R.string.hist_efecto_pasaria, periodo.nombre, Formato.promedio(antes, maxima), Formato.promedio(despues, maxima))
                        },
                        style = Letra.sub,
                        color = Paleta.apagado
                    )
                }
                Text(
                    text = stringResource(R.string.hist_escribe_con_punto, notaMaximaTexto(maxima).removeSuffix(".0")),
                    style = Letra.sub,
                    color = Paleta.apagado,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
        Muelle {
            UniStackButton(
                text = stringResource(R.string.hist_guardar),
                onClick = {
                    viewModel.guardarNotas(subjectId, valores, nuevas)
                    onSaved()
                },
                enabled = hayCambios && !invalido,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

private const val PREFIJO_CORTE = "corte:"
