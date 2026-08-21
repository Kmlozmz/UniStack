@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.ui.graphics.Path
import com.unistack.app.core.design.theme.LocalVividAccents
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.rounded.HelpOutline
import androidx.compose.ui.draw.alpha
import com.unistack.app.core.design.theme.SectionLabelStyle
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material.icons.rounded.Balance
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import kotlin.math.roundToInt
import com.unistack.app.core.design.theme.LocalSectionColors

/*
 * Las piezas con las que se configura lo académico: la franja de la escala, las notas con
 * pasos, la rueda del reparto y sus atajos.
 *
 * Nacieron dentro del onboarding y ahora las usan dos sitios —los pasos 3 y 5 de la
 * configuración inicial, y la pantalla de ajustes académicos—, que enseñan lo mismo y tienen
 * que enseñarlo igual. Copiarlas habría dejado dos escalas que se separan a la primera
 * corrección; aquí solo hay una.
 */

/**
 * La escala partida en sus tres tramos, a lo ancho.
 *
 * Cada zona ocupa lo que le toca del rango, así que la anchura misma dice cuánto margen hay
 * entre aprobar y llegar a la meta. Con la meta pegada al máximo la zona verde se estrecha
 * hasta desaparecer, que es exactamente lo que significa ponerse una meta así.
 */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun ScaleZoneBar(
    max: Double,
    passing: Double?,
    target: Double?,
    /** Una nota concreta señalada sobre la franja, para ver en qué tramo cae. */
    marker: Double? = null
) {
    if (max <= 0.0) return
    val pass = (passing ?: 0.0).coerceIn(0.0, max)
    val goal = (target ?: max).coerceIn(pass, max)
    var explaining by rememberSaveable { mutableStateOf(false) }
    if (explaining) {
        ScaleZoneExplainer(
            max = max,
            passing = pass,
            target = goal,
            onDismiss = { explaining = false }
        )
    }
    Surface(
        onClick = { explaining = true },
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
    Column(modifier = Modifier.padding(16.dp)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Text(
            text = "TU ESCALA, DE UN VISTAZO",
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary,
            style = SectionLabelStyle
        )
        Text(
            text = "0 a ${formatGradeValue(max, max)}",
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Rounded.HelpOutline,
            contentDescription = "Qué significa esta franja",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
    // Los tres tramos van con el color pleno, no con su contenedor.
    //
    // En claro los contenedores son casi blancos: los tres salían de un pastel lavado en el
    // que había que leer el rótulo para saber cuál era cuál, y la franja está justo para no
    // tener que leerla. El texto va en el color del fondo de la app, que contra el relleno
    // sólido es el que más contrasta en los dos temas.
    /*
     * Los tres tramos, en color vivo.
     *
     * Iban con los tonos de seccion, que son colores de contenido: en tema oscuro salen lavados
     * porque tienen que leerse como texto. Aqui son rellenos con un rotulo corto encima, asi que
     * pueden llevar la saturacion que hace falta para que rojo, ambar y verde se distingan de un
     * vistazo y no parezcan tres pasteles del mismo cuadro.
     */
    val vivid = LocalVividAccents.current
    val zones = listOf(
        Triple("Reprobado", (pass / max).toFloat(), vivid.coral to vivid.inkOn(vivid.coral)),
        Triple("Aprobado", ((goal - pass) / max).toFloat(), vivid.amber to vivid.inkOn(vivid.amber)),
        Triple("Meta", ((max - goal) / max).toFloat(), vivid.green to vivid.inkOn(vivid.green))
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(46.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            zones.forEach { (label, share, colors) ->
                if (share <= 0.001f) return@forEach
                Box(
                    modifier = Modifier
                        .weight(share)
                        .fillMaxHeight()
                        .clip(MaterialTheme.shapes.small)
                        .background(colors.first),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = colors.second,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        if (marker != null && max > 0.0) {
            // La marca de una nota concreta. Va debajo de los tramos y no encima: encima
            // taparía el rótulo del tramo en el que cae, que es justo lo que se viene a leer.
            // La marca viaja a su sitio en vez de aparecer en el nuevo: es la unica cosa de la
            // franja que se mueve, y saltando no se veia de donde a donde.
            val marca = MaterialTheme.colorScheme.onSurface
            val share by animateFloatAsState(
                targetValue = (marker / max).toFloat().coerceIn(0f, 1f),
                animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
                label = "marca"
            )
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(share.coerceAtLeast(0.001f))
                        .height(8.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    // Una punta que apunta al tramo, no una rayita.
                    //
                    // Era una barra de 3 x 6 dp del color del texto, debajo de tres bloques de
                    // color: se perdia. Un triangulo apuntando hacia arriba senala el tramo en
                    // vez de quedarse al lado de el, y el rotulo de abajo dice en cual cae, que
                    // es la pregunta de verdad: si apruebo o no.
                    Canvas(modifier = Modifier.size(width = 13.dp, height = 8.dp)) {
                        val punta = Path().apply {
                            moveTo(size.width / 2f, 0f)
                            lineTo(size.width, size.height)
                            lineTo(0f, size.height)
                            close()
                        }
                        drawPath(punta, marca)
                    }
                }
            }
            /*
             * Y la respuesta escrita, debajo.
             *
             * La punta dice donde cae; esto dice que significa. Sin ello hay que mirar el
             * triangulo, deducir sobre que bloque esta y traducirlo, que es justo el trabajo
             * que la franja viene a ahorrar.
             */
            val zona = when {
                marker < pass -> "Reprobado" to vivid.coral
                marker < goal -> "Aprobado" to vivid.amber
                else -> "Meta" to vivid.green
            }
            Row(
                modifier = Modifier.padding(top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = formatGradeValue(marker, max),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold
                )
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = zona.second,
                    contentColor = vivid.inkOn(zona.second)
                ) {
                    Text(
                        text = zona.first.uppercase(),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
        // Los cuatro números del reparto, no solo los extremos: la gracia de la franja es
        // ver dónde caen la nota de aprobar y la meta dentro del rango.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf(0.0, pass, goal, max).forEach { value ->
                Text(
                    text = formatGradeValue(value, max),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    }
    }
}

/**
 * Qué es la franja y cómo se lee, cuando se toca.
 *
 * La franja resume tres cosas de golpe y, sin haberla visto antes, un bloque de color no
 * dice qué mide ni por qué se estrecha. Se explica aquí en vez de en un pie de texto fijo
 * porque solo hace falta la primera vez.
 */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun ScaleZoneExplainer(
    max: Double,
    passing: Double,
    target: Double,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Insights,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = { Text("Cómo leer la franja") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "La barra es tu escala completa, de 0 a ${formatGradeValue(max, max)}, " +
                        "partida en los tres tramos en los que puede caer una nota.",
                    style = MaterialTheme.typography.bodyMedium
                )
                ScaleZoneLegendRow(
                    color = MaterialTheme.colorScheme.errorContainer,
                    title = "Reprobado",
                    detail = "Por debajo de ${formatGradeValue(passing, max)}, la nota mínima que pusiste."
                )
                ScaleZoneLegendRow(
                    color = LocalSectionColors.current.atRiskContainer,
                    title = "Aprobado",
                    detail = "De ${formatGradeValue(passing, max)} a ${formatGradeValue(target, max)}: " +
                        "pasas la materia, pero aún no llegas a tu meta."
                )
                ScaleZoneLegendRow(
                    color = LocalSectionColors.current.onTrackContainer,
                    title = "Meta",
                    detail = "De ${formatGradeValue(target, max)} en adelante: el promedio que te propusiste."
                )
                Text(
                    text = "El ancho de cada tramo es el sitio que ocupa en la escala. Si subes " +
                        "la meta, el tramo verde se estrecha: te dejas menos margen.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Entendido") }
        },
        shape = MaterialTheme.shapes.extraLarge
    )
}

@Composable
internal fun ScaleZoneLegendRow(color: Color, title: String, detail: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 3.dp)
                .size(width = 16.dp, height = 16.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .background(color)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = detail,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Una nota que se mueve con dos botones, no con el teclado.
 *
 * Escribirla a mano abría el teclado sobre media pantalla y dejaba entrar cualquier cosa
 * —vacío, texto, un número fuera de la escala—, que es de donde salía el aviso rojo de
 * validación. Con pasos, la cifra no puede salirse ni cruzarse con la otra.
 */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun GradeStepperRow(
    label: String,
    value: String,
    max: Double,
    floorValue: Double,
    ceilingValue: Double,
    filled: Boolean,
    onValueChange: (String) -> Unit
) {
    val step = if (max > 10.0) 5.0 else 0.1
    val current = gradeValueOf(value) ?: floorValue
    val container = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow
    val content = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val stepContainer = if (filled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.surfaceContainerHigh
    val stepContent = if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = container,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    color = content.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = value.ifBlank { formatGradeValue(current, max) },
                    color = content,
                    style = MaterialTheme.typography.headlineSmallEmphasized
                )
            }
            GradeStepperButton("−", stepContainer, stepContent) {
                onValueChange(formatGradeValue((current - step).coerceAtLeast(floorValue), max))
            }
            Spacer(modifier = Modifier.width(8.dp))
            GradeStepperButton("+", stepContainer, stepContent) {
                onValueChange(formatGradeValue((current + step).coerceAtMost(ceilingValue), max))
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun GradeStepperButton(
    symbol: String,
    container: Color,
    content: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(38.dp),
        shape = CircleShape,
        color = container,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = content,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * La rueda del reparto, con un peso por sector y sus pasos al lado.
 *
 * El agujero del centro lleva el total: cerrada y verde cuando cuadra, abierta y roja
 * cuando no. Es el mismo dato que había en una línea de texto al final de la tarjeta, pero
 * aquí no hay que buscarlo ni sumar de cabeza para entenderlo.
 */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun PeriodWheelCard(
    label: AcademicPeriodLabel,
    weights: List<String>,
    total: Double,
    isValid: Boolean,
    onWeightChange: (Int, String) -> Unit
) {
    // El orden importa más que la lista.
    //
    // Estaba morado, azul, verde…, y morado y azul son vecinos: con tres cortes, los dos
    // primeros salían casi iguales. Ahora la secuencia salta de tono en cada paso —morado,
    // verde, ámbar, azul, rosa, rojo— así que dos cortes seguidos nunca caen cerca.
    //
    // Y va en este orden fijo, no al azar: un color aleatorio puede salir pegado al
    // anterior, que es justo lo que hay que evitar.
    val palette = listOf(
        MaterialTheme.colorScheme.primary,
        LocalSectionColors.current.onTrack,
        LocalSectionColors.current.atRisk,
        LocalSectionColors.current.schedule,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error
    )
    // Los sectores viajan hasta su tamaño nuevo en vez de saltar: con un salto seco, subir
    // cinco puntos y bajarlos se ve igual, y no se sigue de dónde sale lo que se mueve.
    val values = weights.mapIndexed { index, weight ->
        val target = setupPercentValue(weight)
        val animated by animateFloatAsState(
            targetValue = target.toFloat(),
            animationSpec = MaterialTheme.motionScheme.slowSpatialSpec(),
            label = "period-slice-$index"
        )
        animated.toDouble()
    }
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val totalColor = if (isValid) LocalSectionColors.current.onTrack else MaterialTheme.colorScheme.error

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(112.dp)) {
                    val stroke = 15.dp.toPx()
                    val inset = stroke / 2f
                    val arcSize = Size(size.width - stroke, size.height - stroke)
                    val topLeft = Offset(inset, inset)
                    drawArc(
                        color = track,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = stroke, cap = StrokeCap.Butt)
                    )
                    var start = -90f
                    values.forEachIndexed { index, value ->
                        val sweep = (value / 100.0 * 360.0).toFloat()
                        if (sweep <= 0f) return@forEachIndexed
                        drawArc(
                            color = palette[index % palette.size],
                            startAngle = start,
                            sweepAngle = sweep.coerceAtMost(360f - (start + 90f)),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = stroke, cap = StrokeCap.Butt)
                        )
                        start += sweep
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${formatSetupPercent(total)}%",
                        color = totalColor,
                        style = MaterialTheme.typography.titleLargeEmphasized
                    )
                    Text(
                        text = "repartido",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                weights.forEachIndexed { index, weight ->
                    val value = setupPercentValue(weight)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(palette[index % palette.size])
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Text(
                            text = "${label.singular} ${index + 1}",
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        PeriodStepButton("−") {
                            onWeightChange(index, formatSetupPercent((value - 5.0).coerceAtLeast(0.0)))
                        }
                        Text(
                            text = "${formatSetupPercent(value)}%",
                            modifier = Modifier.width(46.dp),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        PeriodStepButton("+") {
                            onWeightChange(index, formatSetupPercent((value + 5.0).coerceAtMost(100.0)))
                        }
                    }
                }
            }
        }
    }
}

/** Cuánto falta o cuánto sobra, dicho con palabras y con el color del estado. */
@Composable
internal fun PeriodBalanceNotice(total: Double, remaining: Double, isValid: Boolean) {
    val over = total > 100.0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isValid) {
            LocalSectionColors.current.onTrackContainer
        } else {
            MaterialTheme.colorScheme.errorContainer
        },
        tonalElevation = 0.dp
    ) {
        Text(
            text = when {
                isValid -> "Cuadra: el 100 % está repartido."
                over -> "Te pasas ${formatSetupPercent(total - 100.0)} puntos. Baja alguno."
                else -> "Te faltan ${formatSetupPercent(remaining)} puntos por repartir."
            },
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            color = if (isValid) {
                LocalSectionColors.current.onOnTrackContainer
            } else {
                MaterialTheme.colorScheme.onErrorContainer
            },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/** Cuántos cortes, en el grupo conectado que usa el resto de la app. */
@Composable
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun PeriodCountSection(
    label: AcademicPeriodLabel,
    count: Int,
    onCountSelected: (Int) -> Unit
) {
    // Dos, tres, cuatro… y «Otro», que abre los pasos para cualquier número.
    //
    // Con un grupo cerrado en 2-5, un plan de seis cortes no cabía en la pantalla, y esa
    // es justo la clase de caso que no se puede dejar fuera de la configuración inicial.
    val options = listOf(2, 3, 4)
    // Sin cantidad elegida todavía, `count` vale cero: comparándolo contra la lista salía
    // que era «otro» y el grupo arrancaba con esa opción marcada y un contador a cero.
    val custom = count > 0 && count !in options
    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        Text(
            text = "¿CUÁNTOS ${label.plural.uppercase(java.util.Locale.forLanguageTag("es"))}?",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = SectionLabelStyle
        )
        ButtonGroup(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            options.forEachIndexed { index, option ->
                val interactionSource = remember { MutableInteractionSource() }
                ToggleButton(
                    checked = !custom && count == option,
                    onCheckedChange = { onCountSelected(option) },
                    shapes = if (index == 0) {
                        ButtonGroupDefaults.connectedLeadingButtonShapes()
                    } else {
                        ButtonGroupDefaults.connectedMiddleButtonShapes()
                    },
                    interactionSource = interactionSource,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .animateWidth(interactionSource)
                ) {
                    Text(option.toString(), maxLines = 1, softWrap = false)
                }
            }
            val otherSource = remember { MutableInteractionSource() }
            ToggleButton(
                checked = custom,
                onCheckedChange = { if (!custom) onCountSelected(5) },
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                interactionSource = otherSource,
                contentPadding = PaddingValues(horizontal = 8.dp),
                modifier = Modifier
                    .weight(1.4f)
                    .defaultMinSize(minHeight = 48.dp)
                    .animateWidth(otherSource)
            ) {
                Text("Otro", maxLines = 1, softWrap = false)
            }
        }
        AnimatedVisibility(
            visible = custom,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label.plural.replaceFirstChar { it.uppercase() },
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    PeriodStepButton("−") { if (count > 2) onCountSelected(count - 1) }
                    Text(
                        text = count.toString(),
                        modifier = Modifier.width(46.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    PeriodStepButton("+") { if (count < 12) onCountSelected(count + 1) }
                }
            }
        }
    }
}

/** El atajo que casi todo el mundo quiere: partes iguales. */
@Composable
internal fun SetupEvenSplitAction(count: Int, onSplit: (List<String>) -> Unit) {
    Surface(
        onClick = {
            val base = 100 / count
            val even = List(count) { index ->
                if (index == count - 1) (100 - base * (count - 1)).toString() else base.toString()
            }
            onSplit(even)
        },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Rounded.Balance,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(9.dp))
            Text(
                text = "Repartir en partes iguales",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

internal fun gradeValueOf(text: String): Double? = text.trim().replace(',', '.').toDoubleOrNull()

internal fun formatGradeValue(value: Double, max: Double): String =
    if (max > 10.0) {
        kotlin.math.round(value).toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }

internal fun formatSetupPercent(value: Double): String {
    val normalized = if (kotlin.math.abs(value) < 0.0001) 0.0 else value
    return if (kotlin.math.abs(normalized - normalized.roundToInt()) < 0.0001) {
        normalized.roundToInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", normalized)
    }
}

internal fun setupPercentValue(text: String): Double =
    text.trim().replace(',', '.').toDoubleOrNull() ?: 0.0

@Composable
internal fun PeriodStepButton(symbol: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 0.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = symbol,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
