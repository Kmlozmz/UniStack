@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_grades.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.drawText
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.unistack.app.core.utils.performSafely
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.GradeCalculator
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.feature_grades.domain.GradeItem
import com.unistack.app.feature_grades.domain.GradeType
import com.unistack.app.feature_user.domain.GradingCutScheme
import com.unistack.app.feature_user.domain.GradingScale

/**
 * Las estadísticas de una materia: cuatro preguntas que hoy no tienen respuesta en ningún sitio.
 *
 * No es un panel de adorno con los mismos datos de la pantalla anterior en otro formato. Cada
 * bloque responde algo concreto que el detalle de la materia **no puede** decir, porque un solo
 * número nunca dice ni de dónde viene ni hacia dónde va:
 *
 * - **Corte a corte** responde «¿en cuál me hundí?», que con cortes de distinto peso no se ve.
 * - **Cómo ha ido cambiando** responde «¿voy subiendo o bajando?».
 * - **De dónde sale la nota** responde «¿dónde me juego la materia?».
 * - **Qué pasa si…** responde la pregunta que se resuelve con la calculadora del teléfono al
 *   lado, y que la app ya tiene los datos para contestar sola.
 */
@Composable
fun SubjectStatsScreen(
    subjectId: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: GradesViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsStateWithLifecycle()
    val subject = subjects.firstOrNull { it.id == subjectId }
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()
    val scale = profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
    val maxGrade = profile?.let(GradingScaleUtils::maxGradeFor) ?: 5.0
    val passingGrade = profile?.passingGrade ?: (maxGrade * 0.6)

    if (subject == null) {
        MissingSubjectStats(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val cutScheme = subject.cutScheme ?: profile?.gradingCutScheme ?: GradingCutScheme.default()
    val calculation = GradeCalculator.calculateSubject(
        grades = subject.grades,
        cuts = cutScheme.cuts,
        targetAverage = subject.targetAverage,
        maxGrade = maxGrade,
        passingGrade = profile?.passingGrade
    )
    val cortes = cutScheme.cuts.sortedBy { it.order }

    /*
     * Los tres calculos van **aqui**, no dentro del `LazyColumn`.
     *
     * El cuerpo de una lista perezosa no es contexto composable: alli no se puede llamar a
     * `remember` ni a nada que lea el tema, asi que estos tres —que hacen las dos cosas— tenian
     * que salir. Y de paso solo se recalculan cuando cambian sus datos, en vez de cada vez que
     * la lista repinta un elemento.
     */
        val conNota = cortes.mapNotNull { cut ->
            val notas = subject.grades.filter { it.cutId == cut.id }
            GradeCalculator.calculateCutAverage(notas)?.let { media ->
                Triple(cutDisplayName(cut), cut.weight * 100, media)
            }
        }
        val serie = remember(subject.grades, cutScheme, maxGrade) {
            val ordenadas = subject.grades.sortedBy { it.recordedAt }
            ordenadas.indices.mapNotNull { hasta ->
                GradeCalculator.calculateSubject(
                    grades = ordenadas.take(hasta + 1),
                    cuts = cutScheme.cuts,
                    targetAverage = subject.targetAverage,
                    maxGrade = maxGrade
                ).currentAverage
            }
        }
        val porTipo = remember(subject.grades) {
            subject.grades.groupBy { it.type }
                .mapValues { (_, lista) -> lista.sumOf { it.percentage } * 100 }
                .toList()
                .sortedByDescending { it.second }
        }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 20.dp, top = 8.dp, end = 20.dp, bottom = scrollBottomRoom),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                CutHeader(
                    title = stringResource(R.string.subject_stats_title),
                    subtitle = subject.name,
                    onBackClick = onBackClick
                )
            }

            /*
             * ------------------------------------------------------- 1 · corte a corte
             *
             * **Cada bloque se calla cuando no tiene nada que decir.**
             *
             * Con un solo corte evaluado no hay comparacion posible, y dibujar tres columnas
             * —dos de ellas vacias— no informa de nada: ocupa el sitio de lo que si informa y
             * hace parecer que la pantalla esta rota. Callarse tambien es una respuesta.
             */
            if (conNota.size >= 2) {
                item {
                    val peor = conNota.minByOrNull { it.third }!!
                    BloqueDeEstadistica(
                        titulo = stringResource(R.string.subject_stats_cut_by_cut),
                        detalle = stringResource(R.string.subject_stats_cut_by_cut_hint),
                        veredicto = stringResource(
                            R.string.subject_stats_worst_cut,
                            peor.first,
                            formatPercent(peor.second)
                        )
                    ) {
                        CorteACorte(
                            cortes = conNota,
                            maxGrade = maxGrade,
                            scale = scale,
                            passingGrade = passingGrade
                        )
                    }
                }
            }

            // ------------------------------------------------------- 2 · el promedio nota a nota
            if (serie.size >= 3) {
                item {
                    val ultimas = serie.takeLast(3)
                    val baja = ultimas.last() < ultimas.first()
                    BloqueDeEstadistica(
                        titulo = stringResource(R.string.subject_stats_over_time),
                        detalle = stringResource(R.string.subject_stats_over_time_hint),
                        veredicto = stringResource(
                            if (baja) R.string.subject_stats_trend_down else R.string.subject_stats_trend_up,
                            GradingScaleUtils.formatGrade(ultimas.first(), scale),
                            GradingScaleUtils.formatGrade(ultimas.last(), scale)
                        )
                    ) {
                        LineaDeLaMateria(
                            serie = serie,
                            targetAverage = subject.targetAverage,
                            maxGrade = maxGrade
                        )
                    }
                }
            }

            // ------------------------------------------------------- 3 · el reparto
            if (porTipo.isNotEmpty()) {
                item {
                    val repartido = porTipo.sumOf { it.second }
                    val libre = (100.0 - repartido).coerceAtLeast(0.0)
                    BloqueDeEstadistica(
                        titulo = stringResource(R.string.subject_stats_where_from),
                        detalle = stringResource(R.string.subject_stats_where_from_hint),
                        veredicto = when {
                            libre >= 50 -> stringResource(R.string.subject_stats_mostly_open, formatPercent(libre))
                            libre > 0 -> stringResource(R.string.subject_stats_heaviest, porTipo.first().first.labelLocal())
                            else -> stringResource(R.string.subject_stats_all_allocated)
                        }
                    ) {
                        DeDondeSale(porTipo = porTipo, libre = libre)
                    }
                }
            }

            // ------------------------------------------------------- 4 · que pasa si
            if (calculation.currentAverage != null && !calculation.isFinished) {
                item {
                    QuePasaSi(
                        suelo = calculation.guaranteedMinimum ?: 0.0,
                        techo = calculation.bestPossible ?: maxGrade,
                        restante = calculation.remainingSemesterFraction * 100,
                        meta = subject.targetAverage,
                        maxGrade = maxGrade,
                        scale = scale,
                        passingGrade = passingGrade
                    )
                }
            }
        }
    }
}

/** El marco común de los bloques: un título, una línea de qué contesta, y el dibujo. */
@Composable
private fun BloqueDeEstadistica(
    titulo: String,
    detalle: String,
    /**
     * La conclusion, en una linea.
     *
     * Dibujar es la mitad del trabajo: un grafico que no puede escribir lo que ha visto deja
     * el ultimo paso —el que cuesta— a quien lo mira. Si un bloque no tiene veredicto que
     * escribir, o le faltan datos o no hacia falta el bloque.
     */
    veredicto: String,
    contenido: @Composable () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    titulo,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    detalle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
            contenido()
            Text(
                veredicto,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/**
 * Un corte por columna: la altura es la nota y el ancho, el peso.
 *
 * Las dos cosas a la vez, porque por separado enganan. Un 2,0 en un corte del 10 % y un 2,0 en
 * uno del 60 % se escriben igual y no significan lo mismo, y una lista de cifras no lo dice.
 *
 * Solo llegan aqui los cortes **con nota**: una columna vacia no es un dato, es un hueco.
 */
@Composable
private fun CorteACorte(
    cortes: List<Triple<String, Double, Double>>,
    maxGrade: Double,
    scale: GradingScale,
    passingGrade: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(126.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        cortes.forEach { (nombre, peso, nota) ->
            val tono = if (nota >= passingGrade) {
                LocalSectionColors.current.onTrack
            } else {
                LocalSectionColors.current.expenses
            }
            val alto by animateFloatAsState(
                targetValue = (nota / maxGrade).coerceIn(0.0, 1.0).toFloat(),
                label = "columnaDelCorte"
            )
            Column(
                modifier = Modifier.weight(peso.toFloat().coerceAtLeast(1f)).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    GradingScaleUtils.formatGrade(nota, scale),
                    color = tono,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(alto)
                            .clip(RoundedCornerShape(9.dp))
                            .background(tono)
                    )
                }
                Spacer(Modifier.height(5.dp))
                Text(
                    nombre,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    "${formatPercent(peso)}%",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    fontSize = 9.5.sp
                )
            }
        }
    }
}

/**
 * El promedio despues de cada nota, con la meta de fondo.
 *
 * Es lo unico que dice si vas subiendo o bajando. La cifra del detalle es una foto: aqui esta
 * la pelicula, y una materia que va en 3,5 bajando no es la misma que una que va en 3,5
 * subiendo.
 *
 * Con su eje a la izquierda: sin el, una caida de tres decimas y una de tres puntos se dibujan
 * exactamente igual, porque la linea se estira sola para llenar el alto que tenga.
 */
@Composable
private fun LineaDeLaMateria(
    serie: List<Double>,
    targetAverage: Double,
    maxGrade: Double
) {
    val acento = MaterialTheme.colorScheme.primary
    val rejilla = MaterialTheme.colorScheme.outlineVariant
    val tintaMeta = MaterialTheme.colorScheme.onSurfaceVariant
    val etiqueta = MaterialTheme.colorScheme.onSurfaceVariant
    val medidor = androidx.compose.ui.text.rememberTextMeasurer()
    val avance by animateFloatAsState(
        targetValue = 1f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 900),
        label = "lineaDeLaMateria"
    )
    val estiloEje = androidx.compose.ui.text.TextStyle(
        color = etiqueta,
        fontSize = 9.sp,
        fontWeight = FontWeight.SemiBold
    )

    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxWidth().height(126.dp)) {
        val margen = 26f
        val ancho = size.width - margen
        fun x(i: Int) = margen + ancho * i / (serie.size - 1).toFloat()
        fun y(v: Double) = size.height - (v / maxGrade).coerceIn(0.0, 1.0).toFloat() * size.height

        for (g in 0..maxGrade.toInt()) {
            val alturaY = y(g.toDouble())
            drawLine(rejilla, Offset(margen, alturaY), Offset(size.width, alturaY), 1f)
            if (g % 2 == 0) {
                val texto = medidor.measure(g.toString(), estiloEje)
                drawText(
                    textLayoutResult = texto,
                    topLeft = Offset(0f, alturaY - texto.size.height / 2f)
                )
            }
        }
        drawLine(
            tintaMeta.copy(alpha = 0.6f),
            Offset(margen, y(targetAverage)),
            Offset(size.width, y(targetAverage)),
            2.5f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(11f, 9f))
        )

        val visibles = (serie.size * avance).toInt().coerceAtLeast(2).coerceAtMost(serie.size)
        val camino = Path().apply {
            moveTo(x(0), y(serie[0]))
            for (k in 1 until visibles) lineTo(x(k), y(serie[k]))
        }
        drawPath(camino, acento, style = Stroke(width = 6f))
        for (k in 0 until visibles) {
            drawCircle(acento, radius = 8f, center = Offset(x(k), y(serie[k])))
        }
    }
}

/** Que parte del 100 % pone cada tipo, y cuanto sigue sin repartir. */
@Composable
private fun DeDondeSale(porTipo: List<Pair<GradeType, Double>>, libre: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().height(14.dp).clip(CircleShape),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            porTipo.forEach { (tipo, pct) ->
                Box(
                    modifier = Modifier
                        .weight(pct.toFloat().coerceAtLeast(0.5f))
                        .fillMaxHeight()
                        .background(tipo.colorLocal())
                )
            }
            /*
             * **Lo que falta por repartir tambien es una parte.**
             *
             * Sin ese hueco, la barra se llenaba con lo poco que hubiera y parecia que la
             * materia ya estaba decidida: con dos notas del 10 % se veia media barra verde y
             * media ambar, cuando lo cierto es que el 80 % esta todavia en juego.
             */
            if (libre > 0) {
                Box(
                    modifier = Modifier
                        .weight(libre.toFloat())
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                )
            }
        }
        porTipo.forEach { (tipo, pct) ->
            FilaDeReparto(tipo.colorLocal(), tipo.labelLocal(), pct, false)
        }
        if (libre > 0) {
            FilaDeReparto(
                MaterialTheme.colorScheme.surfaceContainerHighest,
                stringResource(R.string.subject_stats_unallocated),
                libre,
                true
            )
        }
    }
}

@Composable
private fun FilaDeReparto(color: Color, nombre: String, pct: Double, apagado: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
        Spacer(Modifier.width(8.dp))
        Text(
            nombre,
            color = if (apagado) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${formatPercent(pct)}%",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

/**
 * «Si de aqui en adelante saco X, como termino?»
 *
 * Es la pregunta que la gente resuelve con la calculadora del telefono al lado, y la app ya
 * tiene los tres datos que hacen falta. Se interpola entre el suelo y el techo, que son los
 * mismos numeros que ensena la materia: recalcularlo aparte daria cifras que no cuadran con
 * las de la pantalla anterior.
 */
@Composable
private fun QuePasaSi(
    suelo: Double,
    techo: Double,
    restante: Double,
    meta: Double,
    maxGrade: Double,
    scale: GradingScale,
    passingGrade: Double
) {
    var supuesto by remember { mutableStateOf((maxGrade * 0.8).toFloat()) }
    val fin = suelo + (techo - suelo) * (supuesto / maxGrade)
    val tono = if (fin >= passingGrade) {
        LocalSectionColors.current.onTrack
    } else {
        LocalSectionColors.current.expenses
    }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(22.dp),
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(
                    Icons.Rounded.QueryStats,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(R.string.subject_stats_what_if),
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                stringResource(R.string.subject_stats_what_if_hint, formatPercent(restante)),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.5.sp,
                lineHeight = 17.sp
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        GradingScaleUtils.formatGrade(fin, scale),
                        color = tono,
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        stringResource(R.string.subject_stats_you_would_end),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        GradingScaleUtils.formatGrade(supuesto.toDouble(), scale),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        stringResource(R.string.subject_stats_if_you_get),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            DeslizadorConUmbrales(
                valor = supuesto,
                maxGrade = maxGrade,
                aprobar = passingGrade,
                meta = meta,
                onValor = { supuesto = it }
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    GradingScaleUtils.formatGrade(0.0, scale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.subject_stats_pass_mark, GradingScaleUtils.formatGrade(aprobarSeguro(passingGrade), scale)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    stringResource(R.string.subject_stats_goal_mark, GradingScaleUtils.formatGrade(meta, scale)),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    GradingScaleUtils.formatGrade(maxGrade, scale),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

private fun aprobarSeguro(v: Double) = v

/**
 * El deslizador, con los dos umbrales marcados **en la propia pista**.
 *
 * Aprobar y la meta son las dos unicas cifras que importan al mover esto: de que numero para
 * arriba pasas, y de cual para arriba llegas a donde querias. Escritas en una leyenda aparte
 * obligan a mirar dos sitios a la vez; dibujadas en la pista, el mango las cruza y ya esta.
 *
 * Y al cruzarlas **vibra**. Es el unico sitio de la app donde un tic dice algo que no dice la
 * pantalla: que acabas de pasar un limite. Sin mirar, se nota.
 */
@Composable
private fun DeslizadorConUmbrales(
    valor: Float,
    maxGrade: Double,
    aprobar: Double,
    meta: Double,
    onValor: (Float) -> Unit
) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    val marca = MaterialTheme.colorScheme.onSurface
    var cruzados by remember { mutableStateOf(umbralesBajo(valor.toDouble(), aprobar, meta)) }

    Box(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(horizontal = 10.dp)
        ) {
            val medio = size.height / 2f
            listOf(aprobar, meta).forEach { u ->
                val px = size.width * (u / maxGrade).coerceIn(0.0, 1.0).toFloat()
                drawRoundRect(
                    color = marca.copy(alpha = 0.45f),
                    topLeft = Offset(px - 1.5f, medio - 11f),
                    size = androidx.compose.ui.geometry.Size(3f, 22f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f)
                )
            }
        }
        Slider(
            value = valor,
            onValueChange = { nuevo ->
                val ahora = umbralesBajo(nuevo.toDouble(), aprobar, meta)
                if (ahora != cruzados) {
                    cruzados = ahora
                    haptics.performSafely(HapticFeedbackType.SegmentTick)
                }
                onValor(nuevo)
            },
            valueRange = 0f..maxGrade.toFloat(),
            steps = (maxGrade * 10).toInt() - 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** Cuantos umbrales quedan por debajo del valor: cambia justo al cruzar uno. */
private fun umbralesBajo(v: Double, aprobar: Double, meta: Double): Int =
    (if (v >= aprobar) 1 else 0) + (if (v >= meta) 1 else 0)

@Composable
private fun MissingSubjectStats(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CutHeader(
            title = stringResource(R.string.subject_stats_title),
            subtitle = "",
            onBackClick = onBackClick
        )
    }
}

/*
 * Copias propias de los dos ayudantes de formato.
 *
 * `formatPercent` y `cutDisplayName` ya existen, privados, en las otras dos pantallas de la
 * seccion. Compartirlos obligaba a hacerlos `internal`, y entonces cada fichero veia dos
 * candidatos con la misma firma —el suyo y el de al lado— y el compilador no sabia cual usar.
 * Dos lineas repetidas cuestan menos que ese enredo.
 */
private fun formatPercent(value: Double): String = String.format(java.util.Locale.US, "%.0f", value)

@Composable
@androidx.compose.runtime.ReadOnlyComposable
private fun cutDisplayName(cut: com.unistack.app.feature_user.domain.GradingCut): String =
    cut.name.takeIf { it.isNotBlank() } ?: stringResource(R.string.subject_cut_order, cut.order)
