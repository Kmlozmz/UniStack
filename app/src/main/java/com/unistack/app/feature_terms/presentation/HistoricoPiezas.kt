@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)

package com.unistack.app.feature_terms.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.stringResource
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.components.bottomActionInsets
import com.unistack.app.core.design.theme.CategoricalSubjectAccents
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.duracion
import com.unistack.app.core.design.theme.hayMovimiento
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.NO_DATA
import com.unistack.app.core.utils.Textos
import com.unistack.app.feature_schedule.presentation.AmbarPalido
import com.unistack.app.feature_schedule.presentation.AmbarProfundo
import com.unistack.app.feature_schedule.presentation.AttendanceAbsent
import com.unistack.app.feature_schedule.presentation.AttendanceAttended
import com.unistack.app.feature_schedule.presentation.AttendanceCancelled
import com.unistack.app.feature_schedule.presentation.RojoPalido
import com.unistack.app.feature_schedule.presentation.RojoProfundo
import com.unistack.app.feature_schedule.presentation.VerdePalido
import com.unistack.app.feature_schedule.presentation.VerdeProfundo
import com.unistack.app.feature_schedule.presentation.tonoDeAsistencia
import com.unistack.app.feature_user.domain.GradingScale
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.material.icons.rounded.KeyboardDoubleArrowRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import com.unistack.app.core.design.components.celebracionDelDia
import com.unistack.app.core.design.theme.muelleDeMovimiento
import com.unistack.app.core.utils.performSafely
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.unistack.app.core.navigation.TransicionDePantalla
import com.unistack.app.core.design.components.galletaViva

/*
 * Las piezas del histórico, copiadas de la simulación aprobada.
 *
 * La simulación se dibujó a 390 dp, el ancho de un teléfono corriente, así que sus medidas pasan
 * tal cual: un píxel de la maqueta es un dp aquí. Los colores no se copian en hexadecimal; cada
 * uno es el rol del tema que la maqueta imitaba, para que el histórico siga a los veintiocho
 * temas como el resto de la app. Los únicos fijos son los de asistencia, que por decisión no
 * siguen al tema.
 */

// ================================================================== tonos

/** Los colores de la simulación, por el rol del tema que imitaban. */
internal object Paleta {
    val fondo: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.background
    val bajo: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerLow
    val superficie: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainer
    val alto: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHigh
    val tope: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.surfaceContainerHighest
    val tinta: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurface
    val apagado: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onSurfaceVariant
    val tenue: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline
    val linea: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outlineVariant
    val acento: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
    val sobreAcento: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimary
    val acentoContenedor: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
    val acentoSuave: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
}

internal enum class Tono { Neutro, Acento, Ok, Mal, Ambar }

internal data class Par(val contenedor: Color, val sobre: Color)

@Composable
@ReadOnlyComposable
internal fun Tono.par(): Par {
    val oscuro = LocalIsDarkTheme.current
    return when (this) {
        Tono.Neutro -> Par(Paleta.alto, Paleta.tinta)
        Tono.Acento -> Par(Paleta.acentoSuave, Paleta.acento)
        Tono.Ok -> tonoDeAsistencia(VerdeProfundo, VerdePalido, oscuro).let { Par(it.contenedor, it.sobre) }
        Tono.Mal -> tonoDeAsistencia(RojoProfundo, RojoPalido, oscuro).let { Par(it.contenedor, it.sobre) }
        Tono.Ambar -> tonoDeAsistencia(AmbarProfundo, AmbarPalido, oscuro).let { Par(it.contenedor, it.sobre) }
    }
}

/** El fondo y la tinta de una nota: roja si no llega, verde si va sobrada y neutra en medio. */
@Composable
@ReadOnlyComposable
internal fun tonoDeNota(nota: Double?, aprobado: Double, maxima: Double): Par = when {
    nota == null -> Par(Paleta.alto, Paleta.apagado)
    nota < aprobado - 0.0001 -> Tono.Mal.par()
    nota >= maxima * 0.8 - 0.0001 -> Tono.Ok.par()
    else -> Par(Paleta.tope, Paleta.tinta)
}

/** Un color por corte: azul del horario, el acento y rosa, y después la paleta de materias. */
@Composable
@ReadOnlyComposable
internal fun colorDeCorte(indice: Int): Color = when (indice) {
    0 -> LocalSectionColors.current.schedule
    1 -> Paleta.acento
    2 -> CategoricalSubjectAccents.Rose
    3 -> CategoricalSubjectAccents.Orange
    4 -> CategoricalSubjectAccents.Cyan
    else -> CategoricalSubjectAccents.Lime
}

// ================================================================== letra

/**
 * Un estilo de texto hecho desde cero.
 *
 * Los de Material traen su propio renglón y espaciado, y la maqueta no: heredarlos descuadra
 * cada fila unos dp. La familia sí se toma del tema, para respetar la letra que el usuario eligió.
 */
@Composable
internal fun estilo(
    tamano: Float,
    alto: Float,
    peso: FontWeight = FontWeight.Normal,
    espaciado: Float = 0f,
    cifras: Boolean = false
): TextStyle {
    val familia = MaterialTheme.typography.bodyLarge.fontFamily
    return remember(familia, tamano, alto, peso, espaciado, cifras) {
        TextStyle(
            fontFamily = familia,
            fontSize = tamano.sp,
            lineHeight = alto.sp,
            fontWeight = peso,
            letterSpacing = espaciado.sp,
            fontFeatureSettings = if (cifras) "tnum" else null,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
            platformStyle = PlatformTextStyle(includeFontPadding = false)
        )
    }
}

internal object Letra {
    val titulo: TextStyle @Composable get() = estilo(28f, 36f, FontWeight.ExtraBold, -0.2f)
    val subtitulo: TextStyle @Composable get() = estilo(14f, 20f)
    val rotulo: TextStyle @Composable get() = estilo(11f, 16f, FontWeight.Bold, 1f)
    val nombre: TextStyle @Composable get() = estilo(16f, 24f, FontWeight.Bold)
    val sub: TextStyle @Composable get() = estilo(12.5f, 18f)
    val t15: TextStyle @Composable get() = estilo(15f, 22f, FontWeight.Medium)
    val cifra: TextStyle @Composable get() = estilo(45f, 52f, FontWeight.ExtraBold, -1f, cifras = true)
    val cifraMedia: TextStyle @Composable get() = estilo(36f, 44f, FontWeight.ExtraBold, -1f, cifras = true)
    val metricaValor: TextStyle @Composable get() = estilo(20f, 24f, FontWeight.ExtraBold, cifras = true)
    val metricaEtiqueta: TextStyle @Composable get() = estilo(11.5f, 15f)
    val pastilla: TextStyle @Composable get() = estilo(12f, 16f, FontWeight.SemiBold)
    val hojaTitulo: TextStyle @Composable get() = estilo(22f, 28f, FontWeight.ExtraBold)
    val hojaIntro: TextStyle @Composable get() = estilo(14f, 20f)
    val corte: TextStyle @Composable get() = estilo(11f, 14f, cifras = true)
    val leyenda: TextStyle @Composable get() = estilo(12f, 16f)
    val mini: TextStyle @Composable get() = estilo(13f, 18f, FontWeight.SemiBold)
    fun numero(tamano: Float, peso: FontWeight = FontWeight.ExtraBold): TextStyle = TextStyle(
        fontSize = tamano.sp,
        lineHeight = (tamano * 1.25f).sp,
        fontWeight = peso,
        fontFeatureSettings = "tnum",
        lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
        platformStyle = PlatformTextStyle(includeFontPadding = false)
    )
}

// ================================================================== formato

internal object Formato {
    fun nota(valor: Double?, escala: GradingScale): String = GradingScaleUtils.formatGrade(valor, escala)

    fun promedio(valor: Double?, maxima: Double): String = formatoDePromedio(valor, maxima)

    fun porcentaje(valor: Double?): String = valor?.let { "${it.roundToInt()}%" } ?: NO_DATA

    fun diferencia(valor: Double, maxima: Double): String =
        String.format(Locale.US, if (maxima <= 10.0) "%.2f" else "%.1f", abs(valor))

    private fun con(patron: Int, fecha: LocalDate): String =
        DateTimeFormatter.ofPattern(Textos.get(patron), Locale.getDefault()).format(fecha).replace(".", "")

    /** «lun 25 ene». */
    fun corta(fecha: LocalDate): String = con(R.string.hist_patron_corta, fecha)

    /** «25 ene». */
    fun diaMes(fecha: LocalDate): String = con(R.string.hist_patron_dia_mes, fecha)

    /** «lunes 25 de enero». */
    fun larga(fecha: LocalDate): String = con(R.string.hist_patron_larga, fecha)

    /** «enero». */
    fun mes(fecha: LocalDate): String = con(R.string.hist_patron_mes, fecha)

    fun conMayuscula(texto: String): String = texto.replaceFirstChar { it.titlecase(Locale.getDefault()) }

    /** «C1 · 30%». */
    fun etiquetaCorte(numero: Int, peso: Int): String = Textos.get(R.string.hist_corte_peso, numero, peso)

    /** «A, B y C». */
    fun lista(elementos: List<String>): String = when (elementos.size) {
        0 -> ""
        1 -> elementos.first()
        else -> Textos.get(R.string.hist_lista_y, elementos.dropLast(1).joinToString(", "), elementos.last())
    }

    /** «$214k», lo que cabe en una casilla pequeña. */
    fun dineroCorto(valor: Int): String = when {
        valor >= 1_000_000 -> "$" + String.format(Locale.US, "%.1fM", valor / 1_000_000.0).replace(".0M", "M")
        valor >= 1_000 -> "$" + (valor / 1_000) + "k"
        else -> "$$valor"
    }
}

// ================================================================== estructura

/** La barra de arriba: volver a la izquierda y las acciones a la derecha. 56 dp. */
@Composable
internal fun CabeceraDelHistorico(
    onBack: () -> Unit,
    cerrar: Boolean = false,
    acciones: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (cerrar) {
            UniBackButton(
                onClick = onBack,
                icon = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.hist_cerrar_sin_guardar)
            )
        } else {
            UniBackButton(onClick = onBack)
        }
        Spacer(Modifier.weight(1f))
        acciones()
    }
}

@Composable
internal fun TituloDelHistorico(titulo: String, subtitulo: String?) {
    Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 2.dp, bottom = 14.dp)) {
        Text(text = titulo, style = Letra.titulo, color = Paleta.tinta)
        if (subtitulo != null) {
            Text(
                text = subtitulo,
                style = Letra.subtitulo,
                color = Paleta.apagado,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

/** La columna de tarjetas: 20 dp a los lados y 10 entre una y otra. */
@Composable
internal fun Pila(modifier: Modifier = Modifier, contenido: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        content = contenido
    )
}

@Composable
internal fun Rotulo(texto: String, modifier: Modifier = Modifier, conMargen: Boolean = true, color: Color = Paleta.apagado) {
    Text(
        text = texto.uppercase(Locale.getDefault()),
        style = Letra.rotulo,
        color = color,
        modifier = if (conMargen) modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp) else modifier
    )
}

@Composable
internal fun Tarjeta(
    modifier: Modifier = Modifier,
    color: Color = Paleta.bajo,
    relleno: PaddingValues = PaddingValues(18.dp),
    separacion: Dp = 0.dp,
    onClick: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    UniCard(
        modifier = modifier.fillMaxWidth(),
        color = color,
        shape = MaterialTheme.shapes.large,
        contentPadding = relleno,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(separacion),
            content = contenido
        )
    }
}

/** La fila de siempre: centrada y con 14 dp entre piezas. */
@Composable
internal fun Fila(
    modifier: Modifier = Modifier,
    separacion: Dp = 14.dp,
    alineacion: Alignment.Vertical = Alignment.CenterVertically,
    contenido: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(separacion),
        verticalAlignment = alineacion,
        content = contenido
    )
}

/** Título y línea de apoyo, uno encima del otro. */
@Composable
internal fun RowScope.Columna(
    titulo: String?,
    sub: String?,
    estiloTitulo: TextStyle = Letra.t15,
    subArriba: Boolean = false,
    colorSub: Color = Paleta.apagado,
    colorTitulo: Color = Paleta.tinta
) {
    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        if (subArriba && sub != null) Text(sub, style = Letra.sub, color = colorSub)
        if (titulo != null) Text(titulo, style = estiloTitulo, color = colorTitulo)
        if (!subArriba && sub != null) Text(sub, style = Letra.sub, color = colorSub)
    }
}

/** La línea de 1 dp que separa filas dentro de una tarjeta. */
@Composable
internal fun Modifier.lineaArriba(visible: Boolean): Modifier {
    if (!visible) return this
    val color = Paleta.linea
    return this.drawBehind {
        val grosor = 1.dp.toPx()
        drawLine(color, Offset(0f, grosor / 2f), Offset(size.width, grosor / 2f), strokeWidth = grosor)
    }
}

// ================================================================== piezas pequeñas

@Composable
internal fun Pastilla(texto: String, tono: Tono, modifier: Modifier = Modifier) {
    val par = tono.par()
    Box(
        modifier = modifier
            .height(26.dp)
            .clip(CircleShape)
            .background(par.contenedor)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = texto, style = Letra.pastilla, color = par.sobre, maxLines = 1)
    }
}

/** «▲ 0.12 frente a 2025-2». Nada si falta cualquiera de los dos. */
@Composable
internal fun PastillaDeDiferencia(valor: Double?, frente: Double?, frenteNombre: String, maxima: Double) {
    if (valor == null || frente == null) return
    val d = valor - frente
    Pastilla(
        texto = stringResource(
            R.string.hist_frente_a,
            if (d >= 0) "▲" else "▼",
            Formato.diferencia(d, maxima),
            frenteNombre
        ),
        tono = if (d >= 0) Tono.Ok else Tono.Mal
    )
}

/** Una cifra con su icono arriba. 58 dp de alto como mínimo, como `MetricCard`. */
@Composable
internal fun Metrica(
    icono: ImageVector?,
    valor: String,
    etiqueta: String,
    modifier: Modifier = Modifier,
    fondo: Color = Paleta.bajo,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .heightIn(min = 58.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(fondo)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = if (icono != null) 12.dp else 14.dp, vertical = if (icono != null) 10.dp else 8.dp),
        verticalArrangement = if (icono != null) Arrangement.spacedBy(4.dp) else Arrangement.Center
    ) {
        if (icono != null) Icon(icono, contentDescription = null, tint = Paleta.acento, modifier = Modifier.size(18.dp))
        Column {
            Text(valor, style = Letra.metricaValor, color = Paleta.tinta, maxLines = 1)
            Text(etiqueta, style = Letra.metricaEtiqueta, color = Paleta.apagado)
        }
    }
}

/** Una fila de cifras que se reparten el ancho y comparten el alto de la más alta. */
@Composable
internal fun FilaDeMetricas(modifier: Modifier = Modifier, contenido: @Composable RowScope.() -> Unit) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}

/** Un icono sobre un cuadrado redondeado de 40 dp. */
@Composable
internal fun Azulejo(icono: ImageVector, tono: Tono = Tono.Neutro, modifier: Modifier = Modifier) {
    val par = tono.par()
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (tono == Tono.Neutro) Paleta.alto else par.contenedor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icono,
            contentDescription = null,
            tint = if (tono == Tono.Neutro || tono == Tono.Acento) Paleta.acento else par.sobre,
            modifier = Modifier.size(22.dp)
        )
    }
}

internal enum class EstadoDeRueda { Ok, Mal, Ambar, Vacia }

/** La rueda de estado: verde con visto, roja con equis, ámbar con guion o un aro vacío. */
@Composable
internal fun Rueda(estado: EstadoDeRueda, modifier: Modifier = Modifier) {
    if (estado == EstadoDeRueda.Vacia) {
        Box(modifier = modifier.size(24.dp).border(2.dp, Paleta.tenue, CircleShape))
        return
    }
    val color = when (estado) {
        EstadoDeRueda.Ok -> AttendanceAttended
        EstadoDeRueda.Mal -> AttendanceAbsent
        else -> AttendanceCancelled
    }
    Box(
        modifier = modifier.size(24.dp).clip(CircleShape).background(color),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = when (estado) {
                EstadoDeRueda.Ok -> Icons.Rounded.Check
                EstadoDeRueda.Mal -> Icons.Rounded.Close
                else -> Icons.Rounded.Remove
            },
            contentDescription = null,
            tint = contentColorOn(color),
            modifier = Modifier.size(16.dp)
        )
    }
}

internal fun ruedaDeNota(nota: Double?, aprobado: Double): EstadoDeRueda = when {
    nota == null -> EstadoDeRueda.Vacia
    nota >= aprobado - 0.0001 -> EstadoDeRueda.Ok
    else -> EstadoDeRueda.Mal
}

/** La galleta de M3E, viva, con la cifra dentro. */
@Composable
internal fun Galleta(
    tamano: Dp,
    fondo: Color,
    modifier: Modifier = Modifier,
    contenido: @Composable BoxScope.() -> Unit
) {
    // La galleta se mueve —gira y respira— y lo de dentro se queda quieto.
    Box(
        modifier = modifier.size(tamano).galletaViva(fondo),
        contentAlignment = Alignment.Center,
        content = contenido
    )
}

@Composable
internal fun GalletaConCifra(tamano: Dp, fondo: Color, texto: String, tinta: Color, letra: Float, modifier: Modifier = Modifier) {
    Galleta(tamano = tamano, fondo = fondo, modifier = modifier) {
        Text(text = texto, style = Letra.numero(letra), color = tinta, maxLines = 1)
    }
}

/** La onda de M3E para lo que ya pasó del periodo. */
@Composable
internal fun Onda(fraccion: Float, modifier: Modifier = Modifier) {
    LinearWavyProgressIndicator(
        progress = { fraccion.coerceIn(0f, 1f) },
        modifier = modifier.fillMaxWidth(),
        color = Paleta.acento,
        trackColor = Paleta.acentoSuave
    )
}

@Composable
internal fun Barrita(fraccion: Float, modifier: Modifier = Modifier, color: Color = Paleta.acento) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(Paleta.alto)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
    }
}

/** Los pasos de un flujo: una raya por paso, encendidas hasta el actual. */
@Composable
internal fun PasosDelFlujo(actual: Int, total: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { indice ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(if (indice < actual) Paleta.acento else Paleta.alto)
            )
        }
    }
}

@Composable
internal fun MiniBoton(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    fondo: Color = Paleta.acentoSuave,
    tinta: Color = Paleta.acento
) {
    Box(
        modifier = modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(fondo)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(texto, style = Letra.mini, color = tinta, maxLines = 1)
    }
}

/** Una opción suelta en píldora: contorno y visto cuando está marcada. */
@Composable
internal fun Opcion(texto: String, marcada: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(20.dp)
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(forma)
            .background(if (marcada) Paleta.acento.copy(alpha = 0.08f) else Color.Transparent)
            .border(1.dp, if (marcada) Paleta.acento else Paleta.linea, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (marcada) Icon(Icons.Rounded.Check, contentDescription = null, tint = Paleta.acento, modifier = Modifier.size(18.dp))
        Text(texto, style = estilo(14f, 20f, FontWeight.Medium), color = if (marcada) Paleta.acento else Paleta.tinta, maxLines = 1)
    }
}

/** Una fila elegible con su marca redonda a la derecha. */
@Composable
internal fun FilaDeEleccion(
    elegida: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    centrada: Boolean = true,
    contenido: @Composable RowScope.() -> Unit
) {
    val forma = MaterialTheme.shapes.medium
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (elegida) Paleta.acento.copy(alpha = 0.08f) else Color.Transparent)
            .border(1.dp, if (elegida) Paleta.acento else Paleta.linea, forma)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = if (centrada) Alignment.CenterVertically else Alignment.Top
    ) {
        contenido()
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (elegida) Paleta.acento else Color.Transparent)
                .then(if (elegida) Modifier else Modifier.border(2.dp, Paleta.tenue, CircleShape)),
            contentAlignment = Alignment.Center
        ) {
            if (elegida) Icon(Icons.Rounded.Check, contentDescription = null, tint = Paleta.sobreAcento, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
internal fun Leyenda(elementos: List<Pair<Color, String>>, modifier: Modifier = Modifier) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        elementos.forEach { (color, texto) ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(color))
                Spacer(Modifier.width(5.dp))
                Text(texto, style = Letra.leyenda, color = Paleta.apagado)
            }
        }
    }
}

// ================================================================== hojas y muelle

@Composable
internal fun HojaDelHistorico(onDismiss: () -> Unit, contenido: @Composable ColumnScope.() -> Unit) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Paleta.fondo,
        shape = MaterialTheme.shapes.extraLarge
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, bottom = 36.dp)
                .navigationBarsPadding(),
            content = contenido
        )
    }
}

@Composable
internal fun TituloDeHoja(titulo: String, intro: String?) {
    Text(titulo, style = Letra.hojaTitulo, color = Paleta.tinta, modifier = Modifier.padding(bottom = 4.dp))
    if (intro != null) {
        Text(intro, style = Letra.hojaIntro, color = Paleta.apagado, modifier = Modifier.padding(bottom = 16.dp))
    }
}

/**
 * Lo que queda anclado abajo, sobre un degradado del fondo.
 *
 * El degradado va hasta el 76 % del alto: por encima se transparenta, para que lo último de la
 * lista no se corte en seco contra el botón.
 */
@Composable
internal fun BoxScope.Muelle(contenido: @Composable ColumnScope.() -> Unit) {
    val fondo = Paleta.fondo
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .background(Brush.verticalGradient(0f to fondo.copy(alpha = 0f), 0.24f to fondo, 1f to fondo))
            .bottomActionInsets()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        content = contenido
    )
}

/**
 * Lo del muelle cuando cambia de paso: se apaga lo que había y luego se enciende lo nuevo.
 *
 * No se desliza con la pantalla porque el muelle no se va, sigue abajo y solo cambia lo que
 * lleva. Primero se va lo de antes y después llega lo nuevo, para que nunca se vean dos botones
 * uno a través del otro.
 */
internal fun <S> AnimatedContentTransitionScope<S>.cambioDelMuelle(transicion: TransicionDePantalla?): ContentTransform =
    if (transicion == null) {
        EnterTransition.None togetherWith ExitTransition.None
    } else {
        fadeIn(tween(220, delayMillis = 90)) togetherWith fadeOut(tween(90))
    }

@Composable
internal fun NotaDelMuelle(texto: String) {
    Text(
        text = texto,
        style = Letra.sub,
        color = Paleta.apagado,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

// ================================================================== entradas

/** Lee una nota escrita con punto o coma. Nulo si está vacía; `NaN` si no vale. */
internal fun leerNota(texto: String, maxima: Double): Double? {
    val limpio = texto.trim().replace(',', '.')
    if (limpio.isEmpty()) return null
    val valor = limpio.toDoubleOrNull() ?: return Double.NaN
    return if (valor in 0.0..maxima) Math.round(valor * 10.0) / 10.0 else Double.NaN
}

@Composable
internal fun EntradaDeNota(
    valor: String,
    onValor: (String) -> Unit,
    invalida: Boolean,
    descripcion: String,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(14.dp)
    val interaccion = remember { MutableInteractionSource() }
    val enfocada by interaccion.collectIsFocusedAsState()
    val estiloTexto = estilo(18f, 24f, FontWeight.SemiBold, cifras = true)
    BasicTextField(
        value = valor,
        onValueChange = { nuevo -> if (nuevo.length <= 5) onValor(nuevo) },
        singleLine = true,
        interactionSource = interaccion,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
        textStyle = estiloTexto.copy(color = Paleta.tinta, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(Paleta.acento),
        modifier = modifier
            .size(width = 76.dp, height = 48.dp)
            .clip(forma)
            .background(Paleta.alto)
            .border(
                width = 2.dp,
                color = when {
                    invalida -> MaterialTheme.colorScheme.error
                    enfocada -> Paleta.acento
                    else -> Color.Transparent
                },
                shape = forma
            )
            .semantics { contentDescription = descripcion },
        decorationBox = { campo ->
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (valor.isEmpty()) {
                    Text(NO_DATA, style = estilo(18f, 24f), color = Paleta.tenue)
                }
                campo()
            }
        }
    )
}

/** Un campo de texto con su etiqueta encima, dentro de un contorno. */
@Composable
internal fun CampoDelHistorico(etiqueta: String, valor: String, onValor: (String) -> Unit, modifier: Modifier = Modifier) {
    val forma = RoundedCornerShape(16.dp)
    val interaccion = remember { MutableInteractionSource() }
    val enfocado by interaccion.collectIsFocusedAsState()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(forma)
            .border(if (enfocado) 2.dp else 1.dp, if (enfocado) Paleta.acento else Paleta.linea, forma)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(etiqueta, style = estilo(12f, 16f), color = Paleta.apagado)
        BasicTextField(
            value = valor,
            onValueChange = onValor,
            singleLine = true,
            interactionSource = interaccion,
            textStyle = estilo(16f, 24f).copy(color = Paleta.tinta),
            cursorBrush = SolidColor(Paleta.acento),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ================================================================== deslizar para cerrar

/**
 * Desliza para confirmar: lo que cierra el periodo.
 *
 * El pulgar arrastra y deja un rastro del acento; soltarlo antes del final lo devuelve a su
 * sitio y llegar al final confirma sin esperar a soltar. Para quien usa lector de pantalla, la
 * acción también se ofrece como un toque.
 *
 * **Rehecho el 16 sep 2026**, porque se veía pobre: una pista lisa y un círculo sin nada que
 * tapaba el texto. Ahora cuenta lo que pasa en cada momento:
 *
 * - En reposo, la doble flecha del pulgar da un empujoncito hacia la derecha cada poco y un
 *   brillo recorre el texto. Se entiende «esto se desliza» antes de leer la frase.
 * - Al fondo espera el candado en un hueco punteado, que es a donde hay que llevarlo. Según se
 *   acerca el pulgar, el hueco se enciende; desde el 85 % el trazo se cierra y el candado pasa
 *   a visto, y el pulgar también.
 * - El rastro es un degradado que se intensifica hacia el pulgar, y el texto se apaga según
 *   queda tapado.
 * - Vibra al agarrar, con un tic suave cada décima del recorrido, con un golpe al entrar en la
 *   zona de confirmar, con la confirmación al llegar y con un rechazo si se suelta a medias.
 * - Al llegar, un fogonazo del acento recorre la pista antes de cerrar, para que se vea que
 *   entró.
 *
 * Sin movimiento no hay brillo, ni empujón, ni fogonazo; la vibración sigue lo que diga
 * Movimiento.
 */
@Composable
internal fun DeslizarParaConfirmar(texto: String, onConfirmado: () -> Unit, modifier: Modifier = Modifier) {
    val densidad = LocalDensity.current
    val haptica = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val movimiento = hayMovimiento()
    val confirmar by rememberUpdatedState(onConfirmado)

    val alto = 64.dp
    val aire = 4.dp
    val lado = alto - aire * 2
    val ladoPx = with(densidad) { lado.toPx() }
    val airePx = with(densidad) { aire.toPx() }

    var ancho by remember { mutableIntStateOf(0) }
    var hecho by remember { mutableStateOf(false) }
    var agarrado by remember { mutableStateOf(false) }
    var tramo by remember { mutableIntStateOf(0) }
    var enZona by remember { mutableStateOf(false) }
    // Dónde va el dedo, al instante. `posicion` se mueve en una corrutina, y leerla entre dos
    // eventos seguidos daba el valor de antes: el pulgar se quedaba atrás del dedo.
    val dedo = remember { floatArrayOf(0f) }
    val posicion = remember { Animatable(0f) }
    val fogonazo = remember { Animatable(0f) }
    val recorrido = (ancho - ladoPx - airePx * 2).coerceAtLeast(1f)
    val fraccion = (posicion.value / recorrido).coerceIn(0f, 1f)
    val listo = ((fraccion - 0.55f) / 0.30f).coerceIn(0f, 1f)

    val vuelta = muelleDeMovimiento<Float>()
    val llegada = duracion(140).coerceAtLeast(1)
    val destello = duracion(460)
    val escalaDelPulgar by animateFloatAsState(
        targetValue = if (agarrado || hecho) 1.06f else 1f,
        animationSpec = muelleDeMovimiento(),
        label = "pulgar"
    )
    val ciclo = if (movimiento) rememberInfiniteTransition(label = "deslizar") else null
    val brillo = ciclo?.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing)),
        label = "brillo"
    )
    val empujon = ciclo?.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing)),
        label = "empujon"
    )

    fun completar() {
        if (hecho) return
        hecho = true
        agarrado = false
        haptica.performSafely(HapticFeedbackType.Confirm)
        scope.launch {
            posicion.animateTo(recorrido, tween(llegada))
            if (destello > 0) fogonazo.animateTo(1f, tween(destello, easing = LinearOutSlowInEasing))
            confirmar()
            // Si a los seis segundos sigue aquí es que el cierre no salió —el aviso ya lo dice—:
            // el pulgar vuelve a su sitio para poder intentarlo otra vez.
            delay(6000)
            fogonazo.snapTo(0f)
            hecho = false
            tramo = 0
            enZona = false
            dedo[0] = 0f
            posicion.animateTo(0f, vuelta)
        }
    }

    fun soltar() {
        if (hecho) return
        agarrado = false
        // Un roce no es un intento: el rechazo solo vibra si de verdad se llegó a arrastrar.
        if (dedo[0] / recorrido > 0.12f) haptica.performSafely(HapticFeedbackType.Reject)
        tramo = 0
        enZona = false
        dedo[0] = 0f
        scope.launch { posicion.animateTo(0f, vuelta) }
    }

    val acento = Paleta.acento
    val sobre = Paleta.sobreAcento
    val tinta = Paleta.tinta
    // La luz del pulgar tiene que ser la más clara de las dos tintas: en oscuro el acento lleva
    // letra oscura encima y un brillo de ese color lo ensuciaría.
    val luz = if (LocalIsDarkTheme.current) tinta else sobre

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(CircleShape)
            .background(Paleta.acentoSuave)
            .border(1.dp, acento.copy(alpha = 0.22f), CircleShape)
            .onSizeChanged { ancho = it.width }
            .semantics {
                onClick(label = texto) {
                    completar()
                    true
                }
            }
            .pointerInput(recorrido) {
                detectHorizontalDragGestures(
                    onDragStart = {
                        if (!hecho) {
                            agarrado = true
                            dedo[0] = posicion.value
                            haptica.performSafely(HapticFeedbackType.SegmentTick)
                        }
                    },
                    onDragEnd = { soltar() },
                    onDragCancel = { soltar() },
                    onHorizontalDrag = { cambio, delta ->
                        if (hecho) return@detectHorizontalDragGestures
                        cambio.consume()
                        val nueva = (dedo[0] + delta).coerceIn(0f, recorrido)
                        dedo[0] = nueva
                        scope.launch { posicion.snapTo(nueva) }
                        val f = nueva / recorrido
                        val nuevoTramo = (f * 10f).toInt()
                        if (nuevoTramo != tramo) {
                            tramo = nuevoTramo
                            haptica.performSafely(HapticFeedbackType.SegmentFrequentTick)
                        }
                        if (!enZona && f >= 0.85f) {
                            enZona = true
                            haptica.performSafely(HapticFeedbackType.GestureThresholdActivate)
                        } else if (enZona && f < 0.75f) {
                            enZona = false
                        }
                        if (f >= 0.97f) completar()
                    }
                )
            }
    ) {
        // El rastro: del borde al pulgar, más intenso cuanto más se ha recorrido.
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(with(densidad) { (airePx * 2 + ladoPx + posicion.value).toDp() })
                .clip(CircleShape)
                .background(
                    Brush.horizontalGradient(
                        0f to acento.copy(alpha = 0.06f),
                        1f to acento.copy(alpha = 0.16f + 0.32f * fraccion)
                    )
                )
        )

        // El hueco del final, con su candado.
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = aire)
                .size(lado)
                .drawBehind {
                    val trazo = 1.5.dp.toPx()
                    val radio = size.minDimension / 2f - trazo
                    drawCircle(color = acento.copy(alpha = 0.08f + 0.20f * listo), radius = radio)
                    drawCircle(
                        color = acento.copy(alpha = 0.38f + 0.62f * listo),
                        radius = radio,
                        style = Stroke(
                            width = trazo,
                            pathEffect = if (listo < 1f) {
                                PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.5.dp.toPx()))
                            } else {
                                null
                            }
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (listo >= 1f) Icons.Rounded.Check else Icons.Rounded.Lock,
                contentDescription = null,
                tint = acento.copy(alpha = 0.55f + 0.45f * listo),
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = texto,
            style = estilo(15f, 22f, FontWeight.SemiBold),
            color = acento,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = alto)
                .graphicsLayer {
                    alpha = (1f - fraccion * 1.7f).coerceIn(0f, 1f)
                    compositingStrategy = CompositingStrategy.Offscreen
                }
                .drawWithContent {
                    drawContent()
                    val b = brillo?.value ?: return@drawWithContent
                    val banda = size.width * 0.28f
                    val x = -banda + (size.width + banda * 2f) * b
                    // `SrcAtop` pinta el brillo solo donde ya hay letra.
                    drawRect(
                        brush = Brush.horizontalGradient(
                            0f to Color.Transparent,
                            0.5f to tinta.copy(alpha = 0.9f),
                            1f to Color.Transparent,
                            startX = x - banda,
                            endX = x + banda
                        ),
                        blendMode = BlendMode.SrcAtop
                    )
                }
        )

        // El fogonazo de la llegada, detrás del pulgar.
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .drawBehind {
                    val e = fogonazo.value
                    if (e <= 0f || e >= 1f) return@drawBehind
                    val centro = Offset(airePx + posicion.value + ladoPx / 2f, size.height / 2f)
                    val radio = ladoPx * (0.6f + 4f * e)
                    drawCircle(
                        brush = Brush.radialGradient(
                            0f to acento.copy(alpha = 0.55f * (1f - e)),
                            1f to Color.Transparent,
                            center = centro,
                            radius = radio
                        ),
                        radius = radio,
                        center = centro
                    )
                }
        )

        Box(
            modifier = Modifier
                .offset { IntOffset((airePx + posicion.value).roundToInt(), airePx.roundToInt()) }
                .size(lado)
                .graphicsLayer {
                    scaleX = escalaDelPulgar
                    scaleY = escalaDelPulgar
                    shadowElevation = 6.dp.toPx()
                    shape = CircleShape
                    clip = true
                    ambientShadowColor = acento
                    spotShadowColor = acento
                }
                .background(acento)
                .drawBehind {
                    // Luz desde arriba, como sobre una canica: sin ella el pulgar se ve plano.
                    drawCircle(
                        brush = Brush.verticalGradient(
                            0f to luz.copy(alpha = 0.24f),
                            0.55f to Color.Transparent
                        ),
                        radius = size.minDimension / 2f
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            val visto = hecho || fraccion >= 0.85f
            Crossfade(targetState = visto, animationSpec = tween(duracion(160).coerceAtLeast(1)), label = "icono") { conVisto ->
                Icon(
                    imageVector = if (conVisto) Icons.Rounded.Check else Icons.Rounded.KeyboardDoubleArrowRight,
                    contentDescription = null,
                    tint = sobre,
                    modifier = Modifier
                        .size(26.dp)
                        .graphicsLayer {
                            val t = empujon?.value
                            translationX = if (conVisto || t == null || agarrado || posicion.value > 0f) {
                                0f
                            } else {
                                empujonDelPulgar(t) * 5.dp.toPx()
                            }
                        }
                )
            }
        }
    }
}

/** Cuánto sale la flecha en cada momento del ciclo: sale, vuelve y espera. */
private fun empujonDelPulgar(t: Float): Float = when {
    t < 0.16f -> FastOutSlowInEasing.transform(t / 0.16f)
    t < 0.38f -> 1f - FastOutSlowInEasing.transform((t - 0.16f) / 0.22f)
    else -> 0f
}

// ================================================================== celebración

/**
 * El momento después de cerrar o de crear: la galleta con el visto y lo que acaba de pasar.
 *
 * Dura lo que la celebración del resto de la app, 2,2 s, y se acorta sin movimiento. Mientras
 * está, atrás no hace nada: el cambio ya está guardado y no hay a dónde volver.
 *
 * **Al cerrar, lleva además el efecto de Apariencia.** El confeti, la onda, el sello o el
 * destello que se haya elegido para cerrar un corte salen también aquí, sumados a la galleta y
 * no en su lugar: se pidió así el 16 sep 2026, para el cierre. Van detrás de la galleta y del texto, para no tapar lo que se
 * lee, y la onda, el sello y los rayos nacen de la galleta y no del centro de la pantalla.
 */
@Composable
internal fun CelebracionDelHistorico(
    titulo: String,
    subtitulo: String,
    onTerminada: () -> Unit,
    /** El efecto de Apariencia para cerrar un corte. Solo al cerrar un periodo, no al crearlo. */
    conEfectoDeCierre: Boolean = false
) {
    val movimiento = hayMovimiento()
    val entrada = duracion(700).coerceAtLeast(1)
    val espera = if (movimiento) duracion(2200).coerceAtLeast(1200) else 1200
    val escala = remember { Animatable(if (movimiento) 0.2f else 1f) }
    val giro = remember { Animatable(if (movimiento) -40f else 0f) }
    val opacidad = remember { Animatable(if (movimiento) 0f else 1f) }
    val terminar by rememberUpdatedState(onTerminada)
    val curva = remember { CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) }
    // Empieza apagado y se enciende tras el primer fotograma: encendido desde el principio, la
    // animación del efecto nacería ya terminada y no se vería nada.
    var efecto by remember { mutableStateOf(false) }
    var origen by remember { mutableStateOf(Offset.Zero) }
    var centroDeLaGalleta by remember { mutableStateOf<Offset?>(null) }

    LaunchedEffect(Unit) {
        efecto = true
        launch { opacidad.animateTo(1f, tween(250)) }
        launch { escala.animateTo(1f, tween(entrada, easing = curva)) }
        launch { giro.animateTo(0f, tween(entrada, easing = curva)) }
        delay(espera.toLong())
        terminar()
    }
    BackHandler {}

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = opacidad.value }
            .background(Paleta.acentoContenedor)
            .pointerInput(Unit) { detectTapGestures { } }
            .onGloballyPositioned { origen = it.positionInRoot() }
    ) {
        if (conEfectoDeCierre) {
            Spacer(
                modifier = Modifier
                    .matchParentSize()
                    .celebracionDelDia(disparada = efecto, onTerminada = {}, centro = centroDeLaGalleta)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Galleta(
                tamano = 150.dp,
                fondo = Paleta.acento,
                modifier = Modifier
                    .onGloballyPositioned { centroDeLaGalleta = it.boundsInRoot().center - origen }
                    .graphicsLayer {
                        scaleX = escala.value
                        scaleY = escala.value
                        rotationZ = giro.value
                    }
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null, tint = Paleta.sobreAcento, modifier = Modifier.size(72.dp))
            }
            Text(titulo, style = estilo(32f, 40f, FontWeight.ExtraBold), color = Paleta.tinta, textAlign = TextAlign.Center)
            Text(subtitulo, style = estilo(16f, 24f), color = Paleta.apagado, textAlign = TextAlign.Center)
        }
    }
}
