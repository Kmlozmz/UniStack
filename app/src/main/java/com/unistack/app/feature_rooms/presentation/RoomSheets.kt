package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.ChangeRule
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.WorkRoom
import java.time.LocalDate

/*
 * Hojas y piezas pequeñas que comparten la sala, Gestionar, Grupo y Mi parte. Todas calcadas de
 * los artifacts: `.fila`, `.opt`, `.chL`, `.notaL`, `.inG` y `.segmG`.
 */

/** `.fila`: la fila suelta de las hojas (18 de radio, 6 de separación). */
@Composable
fun SheetRow(
    title: String,
    subtitle: String? = null,
    icon: ImageVector? = null,
    danger: Boolean = false,
    selected: Boolean? = null,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable RowScope.() -> Unit)? = null,
    titleExtra: (@Composable () -> Unit)? = null,
    subtitleRich: androidx.compose.ui.text.AnnotatedString? = null,
    onClick: (() -> Unit)? = null
) {
    val cs = MaterialTheme.colorScheme
    val on = selected == true
    Row(
        Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(if (on) cs.primaryContainer else cs.surfaceContainerLow)
            .then(if (onClick != null) Modifier.cleanClickable(onClick = onClick) else Modifier).padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leading?.invoke()
        if (icon != null) Icon(icon, null, tint = if (danger) cs.error else cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = if (danger) cs.error else cs.onSurface, modifier = Modifier.weight(1f, fill = false))
                titleExtra?.invoke()
            }
            if (subtitleRich != null) Text(subtitleRich, fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
            else if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
        }
        trailing?.invoke(this)
        if (selected != null) Box(Modifier.size(20.dp).clip(CircleShape).border(if (on) 6.dp else 2.dp, if (on) cs.primary else cs.onSurfaceVariant, CircleShape))
    }
}

/** `.opt`: opción con radio; la elegida en primaryContainer. */
@Composable
fun OptRow(title: String, subtitle: String? = null, on: Boolean, onClick: () -> Unit, radio: Boolean = true, leading: (@Composable () -> Unit)? = null) {
    val cs = MaterialTheme.colorScheme
    val fg = if (on) cs.onPrimaryContainer else cs.onSurface
    Row(
        Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (on) cs.primaryContainer else cs.surfaceContainerLow)
            .cleanClickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        leading?.invoke()
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = fg)
            if (!subtitle.isNullOrBlank()) Text(subtitle, fontSize = 12.sp, color = fg.copy(alpha = 0.75f))
        }
        if (radio) Box(Modifier.size(20.dp).clip(CircleShape).border(if (on) 6.dp else 2.dp, if (on) cs.onPrimaryContainer else cs.onSurfaceVariant, CircleShape))
    }
}

/** `.chL`: chip de 10 de radio. */
@Composable
fun ChipL(text: String, on: Boolean = false, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else cs.onSurface,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(if (on) cs.primary else cs.surfaceContainer).cleanClickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 9.dp))
}

/** `.notaL`/`.inG`: campo de la app sin bordes, fondo bajo. */
@Composable
fun RoomField(
    value: String, onChange: (String) -> Unit, placeholder: String = "", modifier: Modifier = Modifier,
    single: Boolean = false, minHeight: Dp = 0.dp, fontSize: Float = 13.5f, corner: Dp = 14.dp, background: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    italic: Boolean = false, serif: Boolean = false
) {
    val cs = MaterialTheme.colorScheme
    Box(modifier.fillMaxWidth().heightIn(min = minHeight).clip(RoundedCornerShape(corner)).background(background).padding(horizontal = if (single) 14.dp else 12.dp, vertical = 12.dp)) {
        if (value.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, fontSize = fontSize.sp, color = cs.onSurfaceVariant.copy(alpha = 0.8f), fontFamily = if (serif) FontFamily.Serif else null)
        BasicTextField(value, onChange, singleLine = single, cursorBrush = SolidColor(cs.primary),
            textStyle = TextStyle(color = cs.onSurface, fontSize = fontSize.sp, lineHeight = (fontSize * 1.55f).sp, fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
                fontFamily = if (serif) FontFamily.Serif else null),
            modifier = Modifier.fillMaxWidth())
    }
}

/** `.segmG`: segmentado en pastilla sobre el fondo. */
@Composable
fun SegmG(options: List<String>, selected: Int, onSelect: (Int) -> Unit, background: Color = MaterialTheme.colorScheme.background, fontSize: Float = 12f) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(CircleShape).background(background).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEachIndexed { i, o ->
            Text(o, fontSize = fontSize.sp, fontWeight = FontWeight.Bold, color = if (i == selected) cs.onPrimary else cs.onSurfaceVariant, maxLines = 1,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f).clip(CircleShape).background(if (i == selected) cs.primary else Color.Transparent).cleanClickable { onSelect(i) }.padding(vertical = 7.dp, horizontal = 4.dp))
        }
    }
}

/** La etiqueta «LÍDER». */
@Composable
fun LeaderTag() {
    val cs = MaterialTheme.colorScheme
    Text(stringResource(R.string.rooms_leader_tag), fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.6.sp, color = cs.primary,
        modifier = Modifier.clip(CircleShape).background(mix(cs.primary, 0.15f, Color.Transparent)).padding(horizontal = 7.dp, vertical = 2.dp))
}

/** Cara vacía con borde discontinuo (una parte libre, un puesto por llenar). */
@Composable
fun EmptyFace(size: Dp) {
    val cs = MaterialTheme.colorScheme
    androidx.compose.foundation.Canvas(Modifier.size(size)) {
        drawCircle(cs.outlineVariant, radius = this.size.minDimension / 2 - 1.dp.toPx(),
            style = androidx.compose.ui.graphics.drawscope.Stroke(1.5.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(4f, 4f))))
    }
}

fun whoText(room: WorkRoom, id: String?, you: String): String = if (id == room.meId) you else room.nameOf(id)

// ---------------------------------------------------------------- asignar (Responsable y fecha)

@Composable
fun AssignSheet(room: WorkRoom, partId: String, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val p = room.part(partId) ?: return onDismiss()
    val today = vm.today()
    var picking by remember { mutableStateOf(false) }
    val max = if (p.sharers == -1) Int.MAX_VALUE else p.sharers.coerceAtLeast(1)
    RoomSheet(onDismiss, p.name, stringResource(R.string.rooms_owner_and_date)) {
        RoomLabel(stringResource(R.string.rooms_owner), Modifier.padding(top = 0.dp))
        room.activeMembers.forEach { m ->
            val on = m.id in p.ownerIds
            OptRow(if (m.id == room.meId) stringResource(R.string.rooms_you) else room.nameOf(m.id), on = on, onClick = {
                val next = when {
                    max == 1 -> listOf(m.id)
                    on -> p.ownerIds - m.id
                    p.ownerIds.size >= max -> p.ownerIds.drop(1) + m.id
                    else -> p.ownerIds + m.id
                }
                vm.assign(room.id, p.id, next)
            }, leading = { MemberFace(room, m.id, 20.dp) })
        }
        RoomLabel(stringResource(R.string.rooms_date))
        OptRow(stringResource(R.string.rooms_card_for, relDay(RoomLogic.partDue(room, p) - today)), stringResource(R.string.rooms_tap_to_change), on = false, radio = false,
            onClick = { picking = true }, leading = { Icon(Icons.Rounded.CalendarMonth, null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp)) })
    }
    if (picking) UniDatePickerDialog(
        selectedDate = LocalDate.ofEpochDay(RoomLogic.partDue(room, p)),
        onDismiss = { picking = false },
        onDateSelected = { d -> vm.updatePart(room.id, p.id) { it.copy(dueEpochDay = d.toEpochDay()) }; picking = false }
    )
}

// ---------------------------------------------------------------- darle una parte a quien entró

@Composable
fun GivePartSheet(room: WorkRoom, memberId: String, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val today = vm.today()
    val name = room.nameOf(memberId)
    val libres = room.parts.filter { it.isFree && it.state != PartState.DELIVERED }
    val sinEmpezar = room.parts.filter { !it.isFree && it.state == PartState.PENDING && !it.hasContent }
    val you = stringResource(R.string.rooms_you_obj)
    val msgIs = stringResource(R.string.rooms_part_is_of)
    val msgFrom = stringResource(R.string.rooms_part_from_to)
    val later = stringResource(R.string.rooms_enters_without_part, name)
    RoomSheet(onDismiss, stringResource(R.string.rooms_give_part_to, name), stringResource(R.string.rooms_give_part_d)) {
        if (libres.isNotEmpty()) {
            RoomLabel(stringResource(R.string.rooms_free_label), Modifier.padding(top = 0.dp))
            libres.forEach { p ->
                SheetRow(p.name, stringResource(R.string.rooms_card_for, relDay(RoomLogic.partDue(room, p) - today)), leading = { EmptyFace(20.dp) }, onClick = {
                    vm.giveEntry(room.id, memberId, p.id); context.roomToast(String.format(msgIs, p.name, name)); onDismiss()
                })
            }
        }
        if (sinEmpezar.isNotEmpty()) {
            RoomLabel(stringResource(R.string.rooms_others_unstarted))
            sinEmpezar.forEach { p ->
                val owner = p.ownerIds.first()
                SheetRow(p.name, stringResource(R.string.rooms_now_of, whoText(room, owner, you)), leading = { MemberFace(room, owner, 20.dp) }, onClick = {
                    vm.giveEntry(room.id, memberId, p.id); context.roomToast(String.format(msgFrom, p.name, room.nameOf(owner), name)); onDismiss()
                })
            }
        }
        SheetRow(stringResource(R.string.rooms_later), stringResource(R.string.rooms_later_d), onClick = { vm.handleEntry(room.id, memberId); context.roomToast(later); onDismiss() })
    }
}

// ---------------------------------------------------------------- el compañero le pide algo al líder

@Composable
fun AskLeaderSheet(room: WorkRoom, partId: String, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val p = room.part(partId) ?: return onDismiss()
    val context = LocalContext.current
    val leader = room.nameOf(room.leaderId)
    var swapping by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    val sent = stringResource(R.string.rooms_sent_to, leader)
    val send: (RequestType, String?) -> Unit = { t, other ->
        vm.request(room.id, t, p.id, note.trim(), other)
        if (t == RequestType.HELP) vm.send(room.id, context.getString(R.string.rooms_help_chat, p.name) + if (note.isNotBlank()) " — ${note.trim()}" else "")
        context.roomToast(sent); onDismiss()
    }
    RoomSheet(onDismiss, p.name, if (swapping) stringResource(R.string.rooms_swap_with) else stringResource(R.string.rooms_ask_leader_d, leader)) {
        if (!swapping) {
            RoomField(note, { note = it }, stringResource(R.string.rooms_note_optional), Modifier.padding(bottom = 10.dp), minHeight = 0.dp)
            SheetRow(stringResource(R.string.rooms_ask_time), stringResource(R.string.rooms_ask_time_d, leader), Icons.Rounded.WatchLater) { send(RequestType.MORE_TIME, null) }
            if (room.rules.change != ChangeRule.NO) SheetRow(stringResource(R.string.rooms_ask_swap), stringResource(R.string.rooms_ask_swap_d), Icons.Rounded.SwapHoriz) { swapping = true }
            SheetRow(stringResource(R.string.rooms_ask_help), stringResource(R.string.rooms_ask_help_d, leader), Icons.Rounded.Help) { send(RequestType.HELP, null) }
            SheetRow(stringResource(R.string.rooms_drop_part), stringResource(R.string.rooms_drop_part_d), Icons.Rounded.Close, danger = true) { send(RequestType.DROP, null) }
        } else {
            room.parts.filter { it.id != p.id && it.state != PartState.DELIVERED }.forEach { o ->
                SheetRow(o.name, if (o.isFree) stringResource(R.string.rooms_no_owner) else stringResource(R.string.rooms_now_of, room.nameOf(o.ownerIds.first())),
                    leading = { if (o.isFree) EmptyFace(20.dp) else MemberFace(room, o.ownerIds.first(), 20.dp) }) { send(RequestType.SWAP, o.id) }
            }
        }
    }
}

// ---------------------------------------------------------------- opciones del líder sobre una parte

@Composable
fun PartOptionsSheet(room: WorkRoom, partId: String, vm: RoomsViewModel, onDismiss: () -> Unit, onPoke: (String) -> Unit) {
    val p = room.part(partId) ?: return onDismiss()
    var sub by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val owner = p.ownerIds.firstOrNull()
    val you = stringResource(R.string.rooms_you)
    when (sub) {
        "read" -> return ReadPartSheet(room, p) { sub = null; onDismiss() }
        "assign" -> return AssignSheet(room, p.id, vm) { sub = null; onDismiss() }
        "changes" -> return ChangesSheet(room, p, vm) { sub = null; onDismiss() }
        "due" -> return MoveDueSheet(room, p, vm) { sub = null; onDismiss() }
    }
    val ready = stringResource(R.string.rooms_ready_to_join, p.name)
    RoomSheet(onDismiss, p.name, when { owner == null -> stringResource(R.string.rooms_no_owner); owner == room.meId -> stringResource(R.string.rooms_yours); else -> stringResource(R.string.rooms_of_x, room.nameOf(owner)) }) {
        if (p.hasContent) SheetRow(stringResource(R.string.rooms_read_part), if (p.state == PartState.DELIVERED) stringResource(R.string.rooms_read_part_d1) else stringResource(R.string.rooms_read_part_d2), Icons.Rounded.Visibility) { sub = "read" }
        SheetRow(if (owner != null) stringResource(R.string.rooms_move_to) else stringResource(R.string.rooms_assign_to), if (owner != null) stringResource(R.string.rooms_move_to_d) else stringResource(R.string.rooms_assign_to_d), Icons.Rounded.SwapHoriz) { sub = "assign" }
        if (owner != null && p.state != PartState.DELIVERED && owner != room.meId)
            SheetRow(stringResource(R.string.rooms_poke), stringResource(R.string.rooms_poke_d, room.nameOf(owner)), Icons.Rounded.PanTool) { onPoke(room.nameOf(owner)); onDismiss() }
        if (owner != null && owner != room.meId && (p.hasContent || p.state == PartState.DELIVERED))
            SheetRow(stringResource(R.string.rooms_ask_changes), stringResource(R.string.rooms_ask_changes_d), Icons.Rounded.Edit) { sub = "changes" }
        if (p.state != PartState.DELIVERED) SheetRow(stringResource(R.string.rooms_change_due), stringResource(R.string.rooms_change_due_d), Icons.Rounded.CalendarMonth) { sub = "due" }
        if (p.state == PartState.DELIVERED && !p.approved) SheetRow(stringResource(R.string.rooms_mark_ready), stringResource(R.string.rooms_mark_ready_d), Icons.Rounded.Check) {
            vm.approve(room.id, p.id); context.roomToast(ready); onDismiss()
        }
    }
    @Suppress("UNUSED_EXPRESSION") you
}

@Composable
fun ReadPartSheet(room: WorkRoom, p: RoomPart, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomSheet(onDismiss, p.name, stringResource(R.string.rooms_of_x, p.ownerIds.joinToString(", ") { room.nameOf(it) }), tall = true) {
        Text(p.text.ifBlank { p.fileName ?: "" }, fontFamily = FontFamily.Serif, fontSize = 14.sp, lineHeight = 21.sp, color = cs.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 10.dp))
    }
}

@Composable
fun ChangesSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var note by remember { mutableStateOf("") }
    val owner = p.ownerIds.firstOrNull()
    val asked = stringResource(R.string.rooms_changes_asked)
    val commented = stringResource(R.string.rooms_comment_sent)
    RoomSheet(onDismiss, stringResource(R.string.rooms_ask_changes), "${p.name} · ${stringResource(R.string.rooms_to_x, room.nameOf(owner))}") {
        RoomField(note, { note = it }, stringResource(R.string.rooms_changes_hint), minHeight = 80.dp)
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(stringResource(R.string.rooms_only_comment), { if (note.isNotBlank()) { vm.comment(room.id, p.id, note.trim()); context.roomToast(commented); onDismiss() } }, Modifier.weight(1f), style = PillStyle.TONAL)
            Pill(stringResource(R.string.rooms_ask_changes), {
                if (note.isNotBlank()) {
                    vm.review(room.id, p.id, false, note.trim())
                    if (p.state == PartState.DELIVERED) vm.reopenPart(room.id, p.id)
                    context.roomToast(asked); onDismiss()
                }
            }, Modifier.weight(1f))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MoveDueSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val today = vm.today()
    val days = listOf(1, 2, 3, 7)
    RoomSheet(onDismiss, stringResource(R.string.rooms_due_of, p.name), stringResource(R.string.rooms_due_now_only, relDay(RoomLogic.partDue(room, p) - today))) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            days.forEach { n ->
                val label = context.resources.getQuantityString(R.plurals.rooms_plus_days, n, n)
                ChipL(label) { vm.moveDue(room.id, p.id, n); context.roomToast("${p.name}: $label"); onDismiss() }
            }
        }
    }
}

/** Entrega final: fechas rápidas, mover también las internas y avisar al grupo. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FinalDueSheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var due by remember { mutableStateOf(room.dueEpochDay) }
    var alsoInternal by remember { mutableStateOf(true) }
    var picking by remember { mutableStateOf(false) }
    val saved = stringResource(R.string.rooms_due_saved)
    RoomSheet(onDismiss, stringResource(R.string.rooms_final_due), stringResource(R.string.rooms_now_x, shortDate(room.dueEpochDay))) {
        FlowRow(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(-2L, 0L, 2L, 5L).map { room.dueEpochDay + it }.forEach { d -> ChipL(shortDate(d), d == due) { due = d } }
            ChipL(stringResource(R.string.rooms_pick_date), false) { picking = true }
        }
        SheetRow(stringResource(R.string.rooms_move_internal), stringResource(R.string.rooms_move_internal_d), selected = alsoInternal, onClick = { alsoInternal = !alsoInternal })
        Pill(stringResource(R.string.rooms_save_and_tell), {
            if (due != room.dueEpochDay) vm.moveFinalDue(room.id, due, alsoInternal)
            context.roomToast(saved); onDismiss()
        }, Modifier.fillMaxWidth().padding(top = 2.dp))
    }
    if (picking) UniDatePickerDialog(selectedDate = LocalDate.ofEpochDay(due), onDismiss = { picking = false }, onDateSelected = { due = it.toEpochDay(); picking = false })
}
