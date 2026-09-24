package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.NotifyMode
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.QrCode
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.WorkRoom

/** El texto que se manda al invitar: el nombre de la sala y el código, con dónde escribirlo. */
fun inviteText(context: android.content.Context, room: WorkRoom) = context.getString(R.string.rooms_invite_text, room.title, room.code)

/**
 * Grupo — artifact «grupo y ajustes», opción C cerrada el 23 sep: las caras arriba con cuántos
 * caben y «Invitar»; debajo dos pestañas, Personas y Tú en la sala. El código lo ven todos.
 */
@Composable
fun GroupScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var tab by rememberSaveable { mutableStateOf(0) }
    var sheet by remember { mutableStateOf<String?>(null) }
    var person by remember { mutableStateOf<String?>(null) }
    val people = room.activeMembers
    val free = (room.capacity - people.size).coerceAtLeast(0)
    val pokeMsg = stringResource(R.string.rooms_poke_sent)

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UniBackButton(onClick = onBack)
            Column(Modifier.weight(1f).padding(start = 6.dp)) {
                Text(stringResource(R.string.rooms_group), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_n_of_cap, people.size, room.capacity) + " · " + room.title, fontSize = 11.5.sp, color = cs.onSurfaceVariant, maxLines = 1)
            }
        }
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 14.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy((-12).dp)) {
                people.forEach { m -> Box(Modifier.clip(CircleShape).background(cs.background).padding(3.dp)) { MemberFace(room, m.id, 52.dp) } }
                repeat(minOf(free, 2)) { Box(Modifier.padding(3.dp)) { DashedFace(52.dp) } }
            }
            Text(pluralText(R.plurals.rooms_n_people, people.size) + if (free > 0) " · " + pluralText(R.plurals.rooms_spots_left, free) else "",
                fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
            Pill(stringResource(R.string.rooms_invite), { sheet = "invite" }, icon = Icons.Rounded.Share, small = true)
        }
        GroupTabs(listOf(stringResource(R.string.rooms_people), stringResource(R.string.rooms_you_in_room)), tab) { tab = it }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 40.dp)) {
            if (tab == 0) people.forEach { m ->
                val mine = room.partsOf(m.id)
                val ok = mine.count { it.state == PartState.DELIVERED }
                val late = mine.count { RoomLogic.status(room, it, vm.today()) == RoomLogic.Status.OVERDUE }
                SheetRow(
                    title = room.nameOf(m.id) + if (m.id == room.meId) " " + stringResource(R.string.rooms_you_paren) else "",
                    subtitle = null,
                    leading = { MemberFace(room, m.id, 34.dp) },
                    onClick = { person = m.id },
                    trailing = {
                        if (room.isLeader && late > 0 && m.id != room.meId) RapButton(stringResource(R.string.rooms_poke), RapKind.POKE) { context.roomToast(String.format(pokeMsg, room.nameOf(m.id))) }
                        else Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(-90f))
                    },
                    titleExtra = { if (m.id == room.leaderId) LeaderTag() },
                    subtitleRich = buildAnnotatedString {
                        append(pluralText(R.plurals.rooms_parts_n, mine.size) + " · " + pluralText(R.plurals.rooms_n_delivered, ok))
                        if (late > 0) { append(" · "); withStyle(SpanStyle(color = RoomTone.ROJO.color, fontWeight = FontWeight.Bold)) { append(pluralText(R.plurals.rooms_n_overdue, late)) } }
                    }
                )
            } else YouInRoom(room) { sheet = it }
        }
    }

    person?.let { id -> PersonSheet(room, id, vm, go, onDismiss = { person = null }) }
    when (sheet) {
        "invite" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_invite), stringResource(R.string.rooms_all_can_invite)) { InviteCard(room, vm) { sheet = "qr" } }
        "qr" -> QrSheet(room) { sheet = null }
        "name" -> {
            var name by remember { mutableStateOf(room.nameOf(room.meId)) }
            val changed = stringResource(R.string.rooms_name_changed)
            RoomSheet({ sheet = null }, stringResource(R.string.rooms_your_name_here), stringResource(R.string.rooms_your_name_here_d)) {
                RoomField(name, { name = it.take(30) }, single = true, fontSize = 15f)
                Pill(stringResource(R.string.rooms_save), { vm.setAlias(room.id, name); context.roomToast(changed); sheet = null }, Modifier.fillMaxWidth().padding(top = 10.dp))
            }
        }
        "notify" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_your_notifs), stringResource(R.string.rooms_only_this_room)) {
            listOf(Triple(NotifyMode.ALL, R.string.rooms_notify_all, R.string.rooms_notify_all_d), Triple(NotifyMode.MINE, R.string.rooms_notify_mine, R.string.rooms_notify_mine_d),
                Triple(NotifyMode.NONE, R.string.rooms_notify_none, R.string.rooms_notify_none_d)).forEach { (m, t, d) ->
                SheetRow(stringResource(t), stringResource(d), selected = room.notifyMode == m, onClick = { vm.setNotify(room.id, m); sheet = null })
            }
        }
        "leave" -> LeaveSheet(room, vm, onDismiss = { sheet = null }, onLeft = {}) // la ruta, al quedarse sin sala, lleva a Trabajos
    }
}

/** `.tabsG`: pestañas con subrayado de 3 y el número en rojo. */
@Composable
fun GroupTabs(labels: List<String>, selected: Int, counts: List<Int> = emptyList(), onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().drawBehind { drawLine(cs.outlineVariant.copy(alpha = 0.5f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        labels.forEachIndexed { i, l ->
            val on = i == selected
            Box(Modifier.weight(1f).cleanClickable { onSelect(i) }.drawBehind {
                if (on) drawRoundRect(cs.primary, topLeft = Offset(size.width * 0.2f, size.height - 3.dp.toPx()), size = Size(size.width * 0.6f, 3.dp.toPx()),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx(), 3.dp.toPx()))
            }.padding(vertical = 11.dp), contentAlignment = Alignment.Center) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(l, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) cs.primary else cs.onSurfaceVariant, maxLines = 1)
                    val n = counts.getOrElse(i) { 0 }
                    if (n > 0) Box(Modifier.padding(start = 4.dp).clip(CircleShape).background(cs.error).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
                        Text("$n", fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.background)
                    }
                }
            }
        }
    }
}

@Composable
fun DashedFace(size: Dp) {
    val cs = MaterialTheme.colorScheme
    Canvas(Modifier.size(size)) {
        drawCircle(cs.outlineVariant, radius = this.size.minDimension / 2 - 1.dp.toPx(),
            style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
    }
}

enum class RapKind { POKE, READY, ASSIGN }

/** `.rap`: el botoncito de acción rápida de las filas (Toque · ✓ Lista · Asignar). */
@Composable
fun RapButton(text: String, kind: RapKind, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = when (kind) {
        RapKind.POKE -> cs.errorContainer to cs.onErrorContainer
        RapKind.READY -> mix(RoomTone.VERDE.color, 0.2f, Color.Transparent) to RoomTone.VERDE.color
        RapKind.ASSIGN -> cs.primary to cs.onPrimary
    }
    Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = fg, maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(bg).cleanClickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 6.dp))
}

@Composable
private fun YouInRoom(room: WorkRoom, onSheet: (String) -> Unit) {
    SheetRow(stringResource(R.string.rooms_your_name_here), room.nameOf(room.meId), Icons.Rounded.Edit, onClick = { onSheet("name") })
    SheetRow(stringResource(R.string.rooms_your_notifs), stringResource(when (room.notifyMode) {
        NotifyMode.ALL -> R.string.rooms_notify_all; NotifyMode.MINE -> R.string.rooms_notify_mine_short; NotifyMode.NONE -> R.string.rooms_notify_muted
    }), RoomIcons.Bell, onClick = { onSheet("notify") })
    SheetRow(stringResource(R.string.rooms_leave_room), stringResource(if (room.isLeader) R.string.rooms_leave_leader_d else R.string.rooms_leave_d),
        Icons.AutoMirrored.Rounded.ExitToApp, danger = true, onClick = { onSheet("leave") })
}

/** La tarjeta del código (`.codigo`): puestos libres, copiar, compartir, QR y la entrada. */
@Composable
fun InviteCard(room: WorkRoom, vm: RoomsViewModel, onQr: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val free = (room.capacity - room.activeMembers.size).coerceAtLeast(0)
    val copied = stringResource(R.string.rooms_code_copied)
    val on = cs.onSurface
    Column(Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(cs.primaryContainer).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stringResource(R.string.rooms_room_code), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = on)
        Text(room.code, fontSize = 32.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, color = on, modifier = Modifier.padding(top = 6.dp, bottom = 10.dp))
        if (free > 0) Row(Modifier.padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            repeat(minOf(free, 6)) {
                Canvas(Modifier.size(30.dp)) {
                    drawCircle(on.copy(alpha = 0.55f), radius = size.minDimension / 2 - 1.dp.toPx(),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(2.dp.toPx(), pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 5f))))
                }
            }
        }
        Text(if (free > 0) context.resources.getQuantityString(R.plurals.rooms_spots_of, free, free, room.capacity) else stringResource(R.string.rooms_room_full), fontSize = 12.sp, color = on, modifier = Modifier.padding(bottom = 12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(stringResource(R.string.rooms_copy), { context.copyText("code", inviteText(context, room)); context.roomToast(copied) }, icon = Icons.Rounded.ContentCopy, style = PillStyle.HERO, small = true)
            Pill(stringResource(R.string.rooms_share), { context.shareText(inviteText(context, room)) }, icon = Icons.Rounded.Share, style = PillStyle.HERO, small = true)
            Pill("QR", onQr, icon = Icons.Rounded.QrCode2, style = PillStyle.HERO, small = true)
        }
        if (room.isLeader) {
            Box(Modifier.padding(top = 14.dp).fillMaxWidth().height(1.dp).background(on.copy(alpha = 0.2f)))
            Row(Modifier.fillMaxWidth().cleanClickable { vm.setEntryOpen(room.id, !room.entryOpen) }.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(if (room.entryOpen) R.string.rooms_entry_direct else R.string.rooms_entry_you_approve), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = on, modifier = Modifier.weight(1f))
                PrimarySwitch(!room.entryOpen)
            }
        } else Text(if (room.entryOpen) stringResource(R.string.rooms_entry_direct_all) else stringResource(R.string.rooms_entry_x_approves, room.nameOf(room.leaderId)),
            fontSize = 11.5.sp, color = on.copy(alpha = 0.8f), modifier = Modifier.padding(top = 10.dp))
    }
}

/** `.sw`: el interruptor sobre primaryContainer. */
@Composable
private fun PrimarySwitch(on: Boolean) {
    val c = MaterialTheme.colorScheme.onPrimaryContainer
    val bg = MaterialTheme.colorScheme.primaryContainer
    Box(Modifier.size(44.dp, 26.dp).clip(RoundedCornerShape(13.dp)).background(if (on) c else c.copy(alpha = 0.25f)).padding(4.dp), contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(Modifier.size(18.dp).clip(CircleShape).background(if (on) bg else c.copy(alpha = 0.7f)))
    }
}

/** El QR de verdad, en blanco y negro para que cualquier cámara lo lea. */
@Composable
fun QrView(text: String, size: Dp = 200.dp) {
    val qr = remember(text) { runCatching { QrCode.encode(text) }.getOrNull() } ?: return
    Box(Modifier.size(size).clip(RoundedCornerShape(14.dp)).background(FormatColors.paper).padding(10.dp)) {
        Canvas(Modifier.fillMaxSize()) {
            val quiet = 2
            val n = qr.size + quiet * 2
            val cell = this.size.minDimension / n
            for (y in 0 until qr.size) for (x in 0 until qr.size) if (qr.isDark(x, y)) {
                drawRect(FormatColors.qrDark, topLeft = Offset((x + quiet) * cell, (y + quiet) * cell), size = Size(cell + 0.5f, cell + 0.5f))
            }
        }
    }
}

@Composable
fun QrSheet(room: WorkRoom, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomSheet(onDismiss, stringResource(R.string.rooms_qr_title), stringResource(R.string.rooms_qr_d)) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            QrView("unistack://sala/${room.code}")
            Text(room.code, fontSize = 22.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp, color = cs.onSurface, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun PersonSheet(room: WorkRoom, id: String, vm: RoomsViewModel, go: (String, String) -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val today = vm.today()
    val parts = room.partsOf(id)
    val contribs = room.parts.sumOf { p -> p.attachments.count { it.byId == id } } + room.materials.count { it.byId == id }
    var confirm by remember { mutableStateOf(false) }
    val removed = stringResource(R.string.rooms_removed_x, room.nameOf(id))
    val pokeMsg = stringResource(R.string.rooms_poke_sent)
    RoomSheet(onDismiss, room.nameOf(id) + if (id == room.meId) " " + stringResource(R.string.rooms_you_paren) else "",
        stringResource(if (id == room.leaderId) R.string.rooms_room_leader else R.string.rooms_member), tall = true) {
        RoomLabel(stringResource(R.string.rooms_their_parts), trailing = "${parts.size}", modifier = Modifier.padding(top = 0.dp))
        if (parts.isEmpty()) Text(stringResource(R.string.rooms_no_parts_yet), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
        parts.forEach { p ->
            SheetRow(p.name, whenText(room, p, today), leading = { PartNumber(room, p, RoomLogic.status(room, p, today), 26.dp) }, trailing = { PartChip(room, p, today) },
                onClick = { onDismiss(); go("parte", p.id) })
        }
        RoomLabel(stringResource(R.string.rooms_contributions), trailing = "$contribs")
        if (id != room.meId) {
            Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(stringResource(R.string.rooms_write_in_chat), { onDismiss(); go("chat", "@" + room.nameOf(id).substringBefore(' ')) }, Modifier.weight(1f), icon = RoomIcons.Chat, style = PillStyle.TONAL)
                if (room.isLeader && parts.any { it.state != PartState.DELIVERED })
                    Pill(stringResource(R.string.rooms_poke), { context.roomToast(String.format(pokeMsg, room.nameOf(id))) }, Modifier.weight(1f), icon = Icons.Rounded.PanTool, style = PillStyle.ERR)
            }
            if (room.isLeader && id != room.leaderId) Box(Modifier.padding(top = 14.dp)) {
                SheetRow(if (confirm) stringResource(R.string.rooms_remove_confirm) else stringResource(R.string.rooms_remove_member), stringResource(R.string.rooms_remove_member_d),
                    Icons.AutoMirrored.Rounded.ExitToApp, danger = true, onClick = {
                        if (!confirm) confirm = true else { vm.removeMember(room.id, id); context.roomToast(removed); onDismiss() }
                    })
            }
        }
    }
}

/** Salir: el líder primero pasa el liderazgo; los demás confirman y la sala sale de su lista. */
@Composable
private fun LeaveSheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit, onLeft: () -> Unit) {
    val context = LocalContext.current
    val others = room.activeMembers.filter { it.id != room.meId }
    if (room.isLeader && others.isNotEmpty()) {
        PassLeadershipSheet(room, vm, onDismiss, subtitle = stringResource(R.string.rooms_pass_before_leave))
        return
    }
    val left = stringResource(R.string.rooms_left_room)
    RoomSheet(onDismiss, stringResource(R.string.rooms_leave_q), stringResource(if (others.isEmpty()) R.string.rooms_leave_alone_d else R.string.rooms_leave_d)) {
        Pill(stringResource(R.string.rooms_yes_leave), { vm.delete(room.id); context.roomToast(left); onDismiss(); onLeft() }, Modifier.fillMaxWidth(), style = PillStyle.ERR)
    }
}

@Composable
fun PassLeadershipSheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit, subtitle: String? = null) {
    val context = LocalContext.current
    val passed = stringResource(R.string.rooms_leader_passed)
    RoomSheet(onDismiss, stringResource(R.string.rooms_pass_leadership), subtitle ?: stringResource(R.string.rooms_pass_leadership_d)) {
        val others = room.activeMembers.filter { it.id != room.meId }
        if (others.isEmpty()) Text(stringResource(R.string.rooms_nobody_else), fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(8.dp))
        others.forEach { m ->
            SheetRow(room.nameOf(m.id), pluralText(R.plurals.rooms_parts_n, room.partsOf(m.id).size), leading = { MemberFace(room, m.id, 22.dp) },
                onClick = { vm.passLeadership(room.id, m.id); context.roomToast(String.format(passed, room.nameOf(m.id))); onDismiss() })
        }
    }
}

