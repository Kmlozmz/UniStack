@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_rooms.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.foundation.background
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.RoomMember
import com.unistack.app.feature_rooms.domain.WorkRoom

/*
 * Piezas de Trabajos, calcadas del artifact aprobado.
 *
 * Nombres del artifact → tokens de la app: s-low = surfaceContainerLow, s-cont =
 * surfaceContainer, s-high = surfaceContainerHigh, s-highest = surfaceContainerHighest,
 * s-pri = primary, s-pricont = primaryContainer, s-onvar = onSurfaceVariant, s-out =
 * outlineVariant y el fondo = background.
 */

/**
 * La letra de Trabajos como la de los artifacts. Material le pone a todo texto el `bodyLarge`:
 * 0,5 sp de tracking y 24 sp de renglón, y con letras de 12-14 sp eso la estiraba hacia los
 * lados (lo notó el 23 sep: «está todo muy ancho»). Aquí el tracking es 0 y el renglón 1,45 em,
 * como el CSS; los títulos grandes llevan su propio renglón.
 */
@Composable
fun RoomsText(content: @Composable () -> Unit) {
    val base = LocalTextStyle.current
    CompositionLocalProvider(
        LocalTextStyle provides base.copy(
            letterSpacing = 0.sp, lineHeight = 1.45.em,
            lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None)
        ),
        content = content
    )
}

/** color-mix(in srgb, c p%, base). */
fun mix(c: Color, p: Float, base: Color): Color = c.copy(alpha = p).compositeOver(base)

/** Esquinas de un grupo conectado: los extremos redondeados hacia fuera, lo de dentro casi recto. */
fun groupShape(index: Int, count: Int, outer: Dp = 20.dp, inner: Dp = 6.dp): Shape = when {
    count <= 1 -> RoundedCornerShape(outer)
    index == 0 -> RoundedCornerShape(outer, outer, inner, inner)
    index == count - 1 -> RoundedCornerShape(inner, inner, outer, outer)
    else -> RoundedCornerShape(inner)
}

/** Rótulo de sección (`.lbl`): 11sp, mayúsculas, espaciado, en primario; a la derecha un dato. */
@Composable
fun RoomLabel(text: String, modifier: Modifier = Modifier, trailing: String? = null, color: Color = MaterialTheme.colorScheme.primary) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = 4.dp, end = 4.dp, top = 18.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text.uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, modifier = Modifier.weight(1f))
        if (trailing != null) Text(trailing, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}

/** Una columna de filas conectadas (`.grupo`). */
@Composable
fun <T> RoomGroup(items: List<T>, modifier: Modifier = Modifier, outer: Dp = 20.dp, row: @Composable (item: T, shape: Shape) -> Unit) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEachIndexed { i, it -> row(it, groupShape(i, items.size, outer)) }
    }
}

/** Una fila base: fondo tonal, esquinas de grupo, contenido en fila. */
@Composable
fun RoomRow(
    shape: Shape,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    minHeight: Dp = 58.dp,
    padding: PaddingValues = PaddingValues(horizontal = 14.dp, vertical = 11.dp),
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(color)
            .then(if (onClick != null) Modifier.cleanClickable(onClick = onClick) else Modifier)
            .heightIn(min = minHeight)
            .padding(padding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

/** Título + línea de apoyo dentro de una fila. */
@Composable
fun RowScope.RowTexts(title: String, subtitle: String? = null, titleSize: TextUnit = 14.5.sp, titleWeight: FontWeight = FontWeight.Bold, subtitleLines: Int = 1) {
    Column(modifier = Modifier.weight(1f)) {
        Text(title, fontSize = titleSize, fontWeight = titleWeight, color = MaterialTheme.colorScheme.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = subtitleLines, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 1.dp))
    }
}

/** Mosaico de 40 con icono (`.tile.t40`): tono suave del color y el icono en el color. */
@Composable
fun Tile(icon: ImageVector, tint: Color, size: Dp = 40.dp, corner: Dp = 13.dp, filled: Boolean = false) {
    val bg = if (filled) tint else mix(tint, 0.16f, MaterialTheme.colorScheme.surfaceContainerHigh)
    Box(Modifier.size(size).clip(RoundedCornerShape(corner)).background(bg), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = if (filled) MaterialTheme.colorScheme.background else tint, modifier = Modifier.size(size * 0.5f))
    }
}

/** Cara de una persona: círculo con su color y la inicial. */
@Composable
fun Face(name: String, color: Color, size: Dp = 24.dp, border: Color? = null) {
    Box(
        Modifier.size(size).clip(CircleShape).background(color)
            .then(if (border != null) Modifier.border(2.dp, border, CircleShape) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(name.take(1).uppercase(), color = MemberColors.onFace, fontSize = (size.value * 0.42f).sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
fun MemberFace(room: WorkRoom, memberId: String?, size: Dp = 24.dp, border: Color? = null) {
    val m = room.member(memberId)
    if (m == null) {
        Box(Modifier.size(size).clip(CircleShape).border(1.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape))
    } else Face(room.nameOf(m.id), MemberColors.of(m.colorIndex), size, border)
}

/** Caras apiladas (`.avs`): 3 y un «+n». */
@Composable
fun FaceStack(room: WorkRoom, members: List<RoomMember>, ring: Color, size: Dp = 24.dp, max: Int = 3) {
    Row {
        members.take(max).forEachIndexed { i, m ->
            Box(Modifier.offset(x = (-7 * i).dp)) { Face(room.nameOf(m.id), MemberColors.of(m.colorIndex), size, ring) }
        }
        if (members.size > max) {
            Box(
                Modifier.offset(x = (-7 * max).dp).size(size).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest).border(2.dp, ring, CircleShape),
                contentAlignment = Alignment.Center
            ) { Text("+${members.size - max}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    }
}

enum class PillStyle { FILL, TONAL, HERO, ERR, OK, GHOST }

/** La pastilla de acción (`.pill`). */
@Composable
fun Pill(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    style: PillStyle = PillStyle.FILL,
    small: Boolean = false,
    enabled: Boolean = true,
    heroBase: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    heroOn: Color = MaterialTheme.colorScheme.primaryContainer,
    big: Boolean = false,
    alpha: Float = 1f,
    trailingIcon: ImageVector? = null
) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = when (style) {
        PillStyle.FILL -> cs.primary to cs.onPrimary
        PillStyle.TONAL -> cs.surfaceContainer to cs.onSurface
        PillStyle.HERO -> heroBase to heroOn
        PillStyle.ERR -> cs.errorContainer to cs.onErrorContainer
        PillStyle.OK -> RoomTone.VERDE.color to cs.background
        PillStyle.GHOST -> Color.Transparent to cs.primary
    }
    Row(
        modifier = modifier
            .graphicsLayer { this.alpha = alpha }
            .clip(CircleShape)
            .background(bg.copy(alpha = if (enabled) bg.alpha else bg.alpha * 0.5f))
            .then(if (enabled) Modifier.cleanClickable(onClick = onClick) else Modifier)
            .padding(horizontal = if (small) 12.dp else if (big) 14.dp else 17.dp, vertical = if (small) 7.dp else if (big) 14.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterHorizontally)
    ) {
        if (icon != null) Icon(icon, null, tint = fg, modifier = Modifier.size(if (small) 15.dp else 17.dp))
        Text(text, color = fg.copy(alpha = if (enabled) 1f else 0.6f), fontSize = if (small) 12.sp else 13.5.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (trailingIcon != null) Icon(trailingIcon, null, tint = fg, modifier = Modifier.size(if (small) 15.dp else 17.dp))
    }
}

/** Etiqueta de estado (`.chip`/`.estado`): mayúsculas, tono suave. */
@Composable
fun StatusChip(text: String, color: Color, filled: Boolean = false) {
    Text(
        text.uppercase(),
        color = if (filled) MaterialTheme.colorScheme.background else color,
        fontSize = 10.5.sp,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 0.3.sp,
        maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(if (filled) color else mix(color, 0.18f, Color.Transparent)).padding(horizontal = 9.dp, vertical = 4.dp)
    )
}

/** Botón redondo de 40 (`.ib`). */
@Composable
fun RoundButton(icon: ImageVector, contentDescription: String?, onClick: () -> Unit, badge: Int = 0, badgeColor: Color = MaterialTheme.colorScheme.primary, background: Color = MaterialTheme.colorScheme.surfaceContainer, tint: Color = MaterialTheme.colorScheme.onSurface) {
    Box {
        Box(Modifier.size(40.dp).clip(CircleShape).background(background).cleanClickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription, tint = tint, modifier = Modifier.size(20.dp))
        }
        if (badge > 0) CountBadge(badge, badgeColor, Modifier.align(Alignment.TopEnd).offset(x = 2.dp, y = (-2).dp))
    }
}

@Composable
fun CountBadge(n: Int, color: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier) {
    val on = if (color == MaterialTheme.colorScheme.primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.background
    Box(
        modifier.heightIn(min = 17.dp).clip(CircleShape).background(MaterialTheme.colorScheme.background).padding(2.dp)
            .clip(CircleShape).background(color).padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) { Text("$n", color = on, fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold) }
}

/**
 * La hoja de la app (`.sheet`): fondo de pantalla, esquinas de 28, el asa de 36×4, título de 19
 * y la X redonda a la derecha. `tall` es la hoja alta (`.sheet.alta`), que llega casi arriba.
 */
@Composable
fun RoomSheet(
    onDismiss: () -> Unit,
    title: String? = null,
    subtitle: String? = null,
    tall: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
    showClose: Boolean = true,
    header: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cs = MaterialTheme.colorScheme
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = cs.background,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = { Box(Modifier.padding(top = 10.dp, bottom = 8.dp).size(36.dp, 4.dp).clip(RoundedCornerShape(2.dp)).background(cs.outlineVariant)) },
        contentWindowInsets = { androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0) }
    ) {
        Column(Modifier.fillMaxWidth().then(if (tall) Modifier.fillMaxHeight(0.93f) else Modifier).navigationBarsPadding().imePadding()) {
            if (header != null) header()
            else if (title != null) {
                Row(Modifier.fillMaxWidth().padding(start = 18.dp, end = 18.dp, top = 2.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text(title, fontSize = 19.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                        if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant)
                    }
                    trailing?.invoke()
                    if (showClose) Box(Modifier.size(40.dp).clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Close, null, tint = cs.onSurface, modifier = Modifier.size(20.dp))
                    }
                }
            }
            /*
             * **Sin estirón al llegar al final.** Lanzando el contenido con fuerza, el desplazamiento
             * se para en el borde y el estirado llega a destiempo, porque la hoja también se arrastra.
             * Igual que en las hojas de Notas y Tareas: sin efecto de borde dentro de la hoja, y lo que
             * sobra del gesto se lo queda el contenido en vez de pasárselo a la hoja.
             */
            CompositionLocalProvider(LocalOverscrollFactory provides null) {
                Column(
                    Modifier.fillMaxWidth().then(if (tall) Modifier.weight(1f) else Modifier.weight(1f, fill = false))
                        .nestedScroll(SoloElContenido).verticalScroll(rememberScrollState()).padding(start = 18.dp, end = 18.dp, bottom = 22.dp),
                    content = content
                )
            }
            footer?.invoke()
        }
    }
}

/** Se queda con el desplazamiento y la inercia que sobran, para que no lleguen a la hoja. */
private val SoloElContenido = object : NestedScrollConnection {
    override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset = available
    override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity = available
}

/** Barra de secciones partida (`.barra`/`.partes`). */
@Composable
fun SegmentBar(colors: List<Color>, modifier: Modifier = Modifier, height: Dp = 7.dp) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        colors.forEach { c -> Box(Modifier.weight(1f).height(height).clip(RoundedCornerShape(3.dp)).background(c)) }
    }
}

/** Radio de las filas elegibles (`.rd`). */
@Composable
fun RadioDot(on: Boolean) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.size(20.dp).clip(CircleShape)
            .border(if (on) 6.dp else 2.dp, if (on) cs.primary else cs.onSurfaceVariant, CircleShape)
    )
}

/** Casilla (`.ck`). */
@Composable
fun CheckBox(on: Boolean) {
    val cs = MaterialTheme.colorScheme
    Box(
        Modifier.size(22.dp).clip(RoundedCornerShape(7.dp))
            .background(if (on) cs.primary else Color.Transparent)
            .border(2.dp, if (on) cs.primary else cs.outlineVariant, RoundedCornerShape(7.dp)),
        contentAlignment = Alignment.Center
    ) { if (on) Icon(Icons.Rounded.Check, null, tint = cs.onPrimary, modifier = Modifier.size(15.dp)) }
}

@Composable
fun VSpace(h: Dp) = Spacer(Modifier.height(h))

@Composable
fun HSpace(w: Dp) = Spacer(Modifier.width(w))

/** Un chip de elección (`.chT`/`.chM`). */
@Composable
fun ChoiceChip(text: String, on: Boolean, onClick: () -> Unit, icon: ImageVector? = null, leading: (@Composable () -> Unit)? = null, count: Int? = null) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (on) cs.primary else cs.surfaceContainer).cleanClickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        leading?.invoke()
        if (icon != null) Icon(icon, null, tint = if (on) cs.onPrimary else cs.onSurface, modifier = Modifier.size(15.dp))
        Text(text, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else cs.onSurface, maxLines = 1)
        if (count != null) Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = (if (on) cs.onPrimary else cs.onSurfaceVariant).copy(alpha = 0.8f))
    }
}

/** Surface simple con color y forma, para bloques como el recuadro «Lo que te toca». */
@Composable
fun Block(color: Color, shape: Shape = RoundedCornerShape(24.dp), modifier: Modifier = Modifier, padding: PaddingValues = PaddingValues(16.dp), onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxWidth().clip(shape).background(color).then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(padding),
        content = content
    )
}

@Composable
fun Dot(color: Color, size: Dp = 8.dp) = Box(Modifier.size(size).clip(CircleShape).background(color))

/**
 * Los pasos como puntos: el actual se estira en píldora y, al avanzar, se encoge para que se
 * estire el siguiente, con un poco de muelle (artifact «Crear sala, segunda vuelta», opción A).
 */
@Composable
fun StretchDots(count: Int, current: Int, modifier: Modifier = Modifier, dot: Dp = 8.dp, long: Dp = 28.dp) {
    val cs = MaterialTheme.colorScheme
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        repeat(count) { i ->
            val on = i == current
            val w by animateDpAsState(if (on) long else dot, spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMediumLow), label = "paso")
            val c by animateColorAsState(if (on) cs.primary else cs.outlineVariant, label = "paso-color")
            Box(Modifier.width(w).height(dot).clip(CircleShape).background(c))
        }
    }
}

@Suppress("unused")
@Composable
private fun keepSurface() = Surface {}
