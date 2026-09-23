package com.unistack.app.feature_rooms.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Rule
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Login
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.ChangeRule
import com.unistack.app.feature_rooms.domain.LateRule
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.ReminderRule
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.RoomRequest
import com.unistack.app.feature_rooms.domain.StructureRule
import com.unistack.app.feature_rooms.domain.VisibilityRule
import com.unistack.app.feature_rooms.domain.WorkRoom

/**
 * Gestionar — artifact «el líder en la sala», G1 + G4 cerrado el 23 sep: la franja con «Tienes N
 * cosas por atender» y los cuatro números queda fija arriba; debajo, pestañas Pendientes · Partes
 * · Grupo · Ajustes. Tocar un número lleva a Pendientes con sólo ese bloque abierto.
 */
@Composable
fun ManageScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val today = vm.today()
    if (!room.isLeader) { androidx.compose.runtime.LaunchedEffect(Unit) { onBack() }; return }
    var tab by rememberSaveable { mutableStateOf(0) }
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var openBlocks by remember { mutableStateOf(setOf<String>()) }
    var partSheet by remember { mutableStateOf<String?>(null) }
    var assign by remember { mutableStateOf<String?>(null) }
    var giveTo by remember { mutableStateOf<String?>(null) }
    var sheet by remember { mutableStateOf<String?>(null) }
    val subjects by vm.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == room.subjectId }
    val pokeMsg = stringResource(R.string.rooms_poke_sent)
    val poke: (String) -> Unit = { n -> context.roomToast(String.format(pokeMsg, n)) }

    val requests = RoomLogic.openRequests(room)
    val entry = RoomLogic.pendingEntry(room)
    val overdue = RoomLogic.overdue(room, today)
    val free = RoomLogic.free(room)
    val toApprove = RoomLogic.toApprove(room)
    val pedidos = requests.size + if (entry != null) 1 else 0
    val total = pedidos + overdue.size + free.size + toApprove.size

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UniBackButton(onClick = onBack)
            Column(Modifier.weight(1f).padding(start = 6.dp)) {
                Text(stringResource(R.string.rooms_manage), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_only_you_leader), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
        }
        // La franja fija con los cuatro números.
        Column(Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(mix(subjectColor(subject), 0.20f, cs.surfaceContainerLow)).padding(16.dp)) {
            Text("${room.title} · ${stringResource(R.string.rooms_due_on, shortDate(room.dueEpochDay))}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(buildAnnotatedString {
                if (total == 0) append(stringResource(R.string.rooms_all_up_to_date)) else {
                    append(stringResource(R.string.rooms_you_have) + " ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append("$total") }
                    append(" " + pluralText(R.plurals.rooms_things_to_attend, total))
                }
            }, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, lineHeight = 22.sp, modifier = Modifier.padding(top = 3.dp, bottom = 12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(Triple("req", R.string.rooms_requests, pedidos to RoomTone.AZUL.color), Triple("late", R.string.rooms_overdue_pl, overdue.size to RoomTone.ROJO.color),
                    Triple("free", R.string.rooms_free_pl, free.size to cs.onSurfaceVariant), Triple("ok", R.string.rooms_to_approve, toApprove.size to RoomTone.VERDE.color)).forEach { (k, l, nc) ->
                    val on = filter == k
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(cs.background).then(if (on) Modifier.border(2.dp, nc.second, RoundedCornerShape(16.dp)) else Modifier)
                        .cleanClickable { filter = if (on) null else k; tab = 0; if (!on) openBlocks = openBlocks + k }.padding(horizontal = 4.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("${nc.first}", fontSize = 21.sp, lineHeight = 1.2.em, fontWeight = FontWeight.Black, color = nc.second)
                        Text(stringResource(l), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, maxLines = 1)
                    }
                }
            }
        }
        GroupTabs(listOf(stringResource(R.string.rooms_pending), stringResource(R.string.rooms_parts), stringResource(R.string.rooms_group), stringResource(R.string.rooms_settings)), tab,
            counts = listOf(total, 0, 0, 0)) { tab = it; if (it != 0) filter = null }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 40.dp)) {
            when (tab) {
                0 -> {
                    if (filter == null && entry != null) EntryNotice(room, entry.id) { giveTo = entry.id }
                    val vis: (String) -> Boolean = { filter == null || filter == it }
                    val isOpen: (String) -> Boolean = { it in openBlocks || filter == it }
                    val toggle: (String) -> Unit = { k -> openBlocks = if (k in openBlocks) openBlocks - k else openBlocks + k }
                    if (vis("req")) ManageBlock(stringResource(R.string.rooms_requests), requests.size, isOpen("req"), { toggle("req") }) {
                        if (requests.isEmpty() && room.requests.none { it.resolution != null }) Empty(stringResource(R.string.rooms_no_requests))
                        RequestList(room, vm, go)
                    }
                    if (vis("late")) ManageBlock(stringResource(R.string.rooms_overdue_pl), overdue.size, isOpen("late"), { toggle("late") }) {
                        if (overdue.isEmpty()) Empty(stringResource(R.string.rooms_nothing_overdue)) else ManageRows(room, overdue, today, onRow = { partSheet = it }, onPoke = poke, onAssign = { assign = it }, onReady = { vm.approve(room.id, it) })
                    }
                    if (vis("free")) ManageBlock(stringResource(R.string.rooms_free_parts), free.size, isOpen("free"), { toggle("free") }) {
                        if (free.isEmpty()) Empty(stringResource(R.string.rooms_all_have_owner)) else {
                            ManageRows(room, free, today, onRow = { partSheet = it }, onPoke = poke, onAssign = { assign = it }, onReady = { vm.approve(room.id, it) })
                            Pill(stringResource(R.string.rooms_draw_least), { vm.drawFree(room.id); context.roomToast(context.getString(R.string.rooms_drawn)) },
                                Modifier.fillMaxWidth().padding(top = 8.dp), icon = Icons.Rounded.Casino, style = PillStyle.TONAL)
                        }
                    }
                    if (vis("ok")) ManageBlock(stringResource(R.string.rooms_to_approve), toApprove.size, isOpen("ok"), { toggle("ok") }) {
                        if (toApprove.isEmpty()) Empty(stringResource(R.string.rooms_nothing_to_approve)) else ManageRows(room, toApprove, today, onRow = { partSheet = it }, onPoke = poke, onAssign = { assign = it },
                            onReady = { vm.approve(room.id, it); context.roomToast(context.getString(R.string.rooms_ready_to_join, room.part(it)?.name.orEmpty())) })
                    }
                    if (filter != null) Pill(stringResource(R.string.rooms_see_all_pending), { filter = null }, Modifier.fillMaxWidth().padding(top = 8.dp), style = PillStyle.TONAL)
                }
                1 -> {
                    Text(stringResource(R.string.rooms_tap_part_options), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 10.dp))
                    partGroups(room, today).forEach { (label, parts) ->
                        RoomLabel(stringResource(label), trailing = "${parts.size}")
                        ManageRows(room, parts, today, onRow = { partSheet = it }, onPoke = poke, onAssign = { assign = it }, onReady = { vm.approve(room.id, it) }, onBackground = false)
                    }
                }
                2 -> {
                    if (entry != null) EntryNotice(room, entry.id) { giveTo = entry.id }
                    RoomLabel(stringResource(R.string.rooms_load_each), trailing = stringResource(R.string.rooms_load_legend))
                    LoadChart(room, today)
                }
                else -> RoomSettings(room, vm, subjects, onSheet = { sheet = it })
            }
        }
    }

    partSheet?.let { id -> PartOptionsSheet(room, id, vm, onDismiss = { partSheet = null }, onPoke = poke) }
    assign?.let { id -> AssignSheet(room, id, vm) { assign = null } }
    giveTo?.let { id -> GivePartSheet(room, id, vm) { giveTo = null } }
    when (sheet) {
        "final" -> FinalDueSheet(room, vm) { sheet = null }
        "pass" -> PassLeadershipSheet(room, vm, { sheet = null })
        "rename" -> RenameSheet(room, vm, subjects) { sheet = null }
        "code" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_new_code_q), stringResource(R.string.rooms_new_code_d)) {
            Pill(stringResource(R.string.rooms_create_new_code), { vm.newCode(room.id); sheet = null; context.roomToast(context.getString(R.string.rooms_new_code_done)) }, Modifier.fillMaxWidth())
        }
        "rules" -> RulesSheet(room, vm) { sheet = null }
        "delete" -> DeleteRoomSheet(room, vm, onDismiss = { sheet = null }, onDeleted = onBack)
    }
}

@Composable
private fun Empty(text: String) = Text(text, fontSize = 12.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp))

/** `.gBloque`: bloque plegable con su número (o ✓ si está vacío). */
@Composable
private fun ManageBlock(title: String, count: Int, open: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val rot by animateFloatAsState(if (open) 180f else 0f, label = "blk")
    Column(Modifier.padding(top = 8.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(if (open) cs.surfaceContainerHigh else cs.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().cleanClickable(onClick = onToggle).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.weight(1f))
            if (count > 0) Box(Modifier.heightIn(min = 24.dp).clip(RoundedCornerShape(12.dp)).background(cs.primary).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
                Text("$count", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = cs.onPrimary)
            } else Box(Modifier.size(24.dp).clip(CircleShape).background(mix(RoomTone.VERDE.color, 0.2f, Color.Transparent)), contentAlignment = Alignment.Center) {
                Text("✓", fontSize = 12.sp, fontWeight = FontWeight.Black, color = RoomTone.VERDE.color)
            }
            Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(22.dp).rotate(rot))
        }
        AnimatedVisibility(open) { Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 12.dp)) { content() } }
    }
}

/** Las filas de Gestionar: número, nombre, quién · cuándo y la acción rápida. */
@Composable
private fun ManageRows(room: WorkRoom, parts: List<RoomPart>, today: Long, onRow: (String) -> Unit, onPoke: (String) -> Unit, onAssign: (String) -> Unit, onReady: (String) -> Unit, onBackground: Boolean = true) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        parts.forEachIndexed { i, p ->
            val st = RoomLogic.status(room, p, today)
            Row(Modifier.fillMaxWidth().clip(groupShape(i, parts.size, 22.dp)).background(if (onBackground) cs.background else cs.surfaceContainerLow).cleanClickable { onRow(p.id) }
                .heightIn(min = 62.dp).padding(start = 13.dp, end = 12.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                PartNumber(room, p, st)
                Column(Modifier.weight(1f)) {
                    Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Row(Modifier.padding(top = 1.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        p.ownerIds.firstOrNull()?.let { MemberFace(room, it, 20.dp) }
                        Text(ownersText(room, p) + " · " + whenText(room, p, today), fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                when {
                    st == RoomLogic.Status.OVERDUE && room.meId !in p.ownerIds -> RapButton(stringResource(R.string.rooms_poke), RapKind.POKE) { onPoke(room.nameOf(p.ownerIds.first())) }
                    st == RoomLogic.Status.FREE -> RapButton(stringResource(R.string.rooms_assign), RapKind.ASSIGN) { onAssign(p.id) }
                    p.state == PartState.DELIVERED && !p.approved -> RapButton("✓ " + stringResource(R.string.rooms_ready), RapKind.READY) { onReady(p.id) }
                    p.approved -> StatusChip(stringResource(R.string.rooms_chip_ready), RoomTone.VERDE.color)
                }
            }
        }
    }
}

/** `.pd`: un pedido con su icono, la nota entre comillas y aceptar/rechazar. */
@Composable
private fun RequestList(room: WorkRoom, vm: RoomsViewModel, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val list = room.requests.sortedWith(compareBy<RoomRequest> { it.resolution != null }.thenByDescending { it.createdAt }).take(12)
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        list.forEachIndexed { i, q ->
            val (icon, tone) = requestIcon(q.type)
            Column(Modifier.fillMaxWidth().clip(groupShape(i, list.size, 20.dp)).background(cs.background).padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    TypeIcon(icon, tone, 34.dp)
                    Column(Modifier.weight(1f)) {
                        Text(requestText(room, q), fontSize = 13.5.sp, color = cs.onSurface, lineHeight = 19.sp)
                        if (q.note.isNotBlank()) Text("«${q.note}»", fontSize = 12.5.sp, fontStyle = FontStyle.Italic, color = cs.onSurface,
                            modifier = Modifier.padding(top = 6.dp).clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainerLow).padding(horizontal = 10.dp, vertical = 7.dp))
                    }
                }
                val name = room.nameOf(q.fromId)
                when (q.resolution) {
                    null -> Row(Modifier.padding(start = 44.dp, top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (q.type == RequestType.HELP) Pill(stringResource(R.string.rooms_answer_in_chat), { vm.resolve(room.id, q.id, true); go("chat", "@" + name.substringBefore(' ')) }, small = true)
                        else {
                            Pill(stringResource(R.string.rooms_accept), { vm.resolve(room.id, q.id, true); context.roomToast(context.getString(R.string.rooms_accepted_x, name)) }, small = true)
                            Pill(stringResource(R.string.rooms_reject), { vm.resolve(room.id, q.id, false); context.roomToast(context.getString(R.string.rooms_rejected_x, name)) }, style = PillStyle.TONAL, small = true)
                        }
                    }
                    true -> Text(if (q.type == RequestType.HELP) "✓ " + stringResource(R.string.rooms_answered_chat) else "✓ " + stringResource(R.string.rooms_accepted_x, name),
                        fontSize = 12.sp, fontWeight = FontWeight.Bold, color = RoomTone.VERDE.color, modifier = Modifier.padding(start = 44.dp, top = 8.dp))
                    false -> Text(stringResource(R.string.rooms_rejected), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 44.dp, top = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun requestIcon(t: RequestType): Pair<ImageVector, Color> = when (t) {
    RequestType.MORE_TIME -> Icons.Rounded.WatchLater to RoomTone.AMBAR.color
    RequestType.SWAP -> Icons.Rounded.SwapHoriz to RoomTone.AZUL.color
    RequestType.HELP -> Icons.Rounded.Help to RoomTone.VIOLETA.color
    RequestType.DROP -> Icons.Rounded.Close to RoomTone.ROJO.color
    RequestType.JOIN -> Icons.Rounded.Login to RoomTone.CIAN.color
}

@Composable
private fun requestText(room: WorkRoom, q: RoomRequest) = buildAnnotatedString {
    val ctx = LocalContext.current
    fun b(s: String) = withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(s) }
    b(room.nameOf(q.fromId)); append(" ")
    val part = room.part(q.partId)?.name.orEmpty()
    when (q.type) {
        RequestType.MORE_TIME -> { append(ctx.getString(R.string.rooms_rq_time_a) + " "); b(ctx.getString(R.string.rooms_rq_time_b)); append(" " + ctx.getString(R.string.rooms_rq_for) + " "); b(part) }
        RequestType.SWAP -> { append(ctx.getString(R.string.rooms_rq_swap_a) + " "); b(part); append(" " + ctx.getString(R.string.rooms_rq_swap_b) + " "); b(room.part(q.otherPartId)?.name ?: ctx.getString(R.string.rooms_rq_other)) }
        RequestType.HELP -> { append(ctx.getString(R.string.rooms_rq_help_a) + " "); b(ctx.getString(R.string.rooms_rq_help_b)); append(" " + ctx.getString(R.string.rooms_rq_with) + " "); b(part) }
        RequestType.DROP -> { append(ctx.getString(R.string.rooms_rq_drop) + " "); b(part) }
        RequestType.JOIN -> append(ctx.getString(R.string.rooms_rq_join))
    }
}

/** `.cargaB`: cuántas partes tiene cada uno, verde entregada y rojo vencida. */
@Composable
private fun LoadChart(room: WorkRoom, today: Long) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLow).padding(horizontal = 14.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(9.dp)) {
        room.activeMembers.forEach { m ->
            val parts = room.partsOf(m.id)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                MemberFace(room, m.id, 20.dp)
                Text(whoText(room, m.id, stringResource(R.string.rooms_you)), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.width(54.dp))
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    parts.forEach { p ->
                        val c = when (RoomLogic.status(room, p, today)) { RoomLogic.Status.DELIVERED -> RoomTone.VERDE.color; RoomLogic.Status.OVERDUE -> RoomTone.ROJO.color; else -> cs.primary }
                        Box(Modifier.size(24.dp, 9.dp).clip(RoundedCornerShape(4.dp)).background(c))
                    }
                }
                Text("${parts.size}", fontSize = 12.sp, color = cs.onSurfaceVariant)
            }
        }
    }
}

/** Ajustes de la sala: nombre y materia, cupo, entrada, código, reglas, fechas y liderazgo, borrar. */
@Composable
private fun RoomSettings(room: WorkRoom, vm: RoomsViewModel, subjects: List<com.unistack.app.feature_grades.domain.Subject>, onSheet: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val inside = room.activeMembers.size
    RoomLabel(stringResource(R.string.rooms_the_room))
    SheetRow(stringResource(R.string.rooms_name_and_subject), room.title + " · " + (subjects.firstOrNull { it.id == room.subjectId }?.name ?: stringResource(R.string.rooms_no_subject)), Icons.Rounded.Edit, onClick = { onSheet("rename") })
    SheetRow(stringResource(R.string.rooms_capacity), stringResource(R.string.rooms_capacity_d, inside, room.capacity), RoomIcons.People, trailing = {
        Row(Modifier.clip(CircleShape).background(cs.background).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
            StepCircle("−", room.capacity > maxOf(inside, 2)) { vm.setCapacity(room.id, room.capacity - 1) }
            Text("${room.capacity}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(30.dp))
            StepCircle("+", room.capacity < 12) { vm.setCapacity(room.id, room.capacity + 1) }
        }
    })
    RoomLabel(stringResource(R.string.rooms_entry))
    Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(true to (R.string.rooms_entry_free to R.string.rooms_entry_free_d), false to (R.string.rooms_entry_approval to R.string.rooms_entry_approval_d)).forEach { (open, t) ->
            val on = room.entryOpen == open
            Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(18.dp)).background(if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow)
                .border(2.dp, if (on) cs.primary else Color.Transparent, RoundedCornerShape(18.dp)).cleanClickable { vm.setEntryOpen(room.id, open) }.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(stringResource(t.first), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text(stringResource(t.second), fontSize = 11.5.sp, color = cs.onSurfaceVariant, lineHeight = 15.sp)
            }
        }
    }
    Box(Modifier.height(6.dp))
    SheetRow(stringResource(R.string.rooms_new_code), stringResource(R.string.rooms_new_code_now, room.code), Icons.Rounded.Key, onClick = { onSheet("code") })
    RoomLabel(stringResource(R.string.rooms_rules))
    SheetRow(stringResource(R.string.rooms_room_rules), rulesSummary(room), Icons.AutoMirrored.Rounded.Rule, onClick = { onSheet("rules") })
    RoomLabel(stringResource(R.string.rooms_dates_leadership))
    SheetRow(stringResource(R.string.rooms_final_due), stringResource(R.string.rooms_final_due_d, shortDate(room.dueEpochDay)), Icons.Rounded.CalendarMonth, onClick = { onSheet("final") })
    SheetRow(stringResource(R.string.rooms_pass_leadership), icon = Icons.Rounded.WorkspacePremium, onClick = { onSheet("pass") })
    RoomLabel(stringResource(R.string.rooms_danger_zone))
    SheetRow(stringResource(R.string.rooms_delete_room), stringResource(R.string.rooms_delete_room_d), Icons.Rounded.Delete, danger = true, onClick = { onSheet("delete") })
}

@Composable
private fun StepCircle(t: String, enabled: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.size(30.dp).clip(CircleShape).background(cs.surfaceContainer.copy(alpha = if (enabled) 1f else 0.35f)).then(if (enabled) Modifier.cleanClickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center) { Text(t, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface.copy(alpha = if (enabled) 1f else 0.35f)) }
}

@Composable
private fun rulesSummary(room: WorkRoom): String {
    val r = room.rules
    return listOf(
        stringResource(when (r.change) { ChangeRule.NO -> R.string.rooms_rs_change_no; ChangeRule.ASK -> R.string.rooms_rs_change_ask; ChangeRule.FREE -> R.string.rooms_rs_change_free }),
        stringResource(when (r.late) { LateRule.NO -> R.string.rooms_rs_late_no; LateRule.MARKED -> R.string.rooms_rs_late_marked; LateRule.GRACE -> R.string.rooms_rs_late_grace }),
        stringResource(when (r.reminders) { ReminderRule.NONE -> R.string.rooms_rs_rem_none; ReminderRule.ONE_DAY -> R.string.rooms_rs_rem_one; ReminderRule.THREE_DAYS -> R.string.rooms_rs_rem_three })
    ).joinToString(" · ")
}

/** Reglas de la sala: las mismas de cuando se creó, en segmentados. */
@Composable
private fun RulesSheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val r = room.rules
    RoomSheet(onDismiss, stringResource(R.string.rooms_room_rules), stringResource(R.string.rooms_rules_same)) {
        RuleBlock(stringResource(R.string.rooms_rule_change), listOf(R.string.rooms_opt_no, R.string.rooms_opt_asking, R.string.rooms_opt_free), r.change.ordinal) {
            vm.setRules(room.id, r.copy(change = ChangeRule.entries[it]))
        }
        RuleBlock(stringResource(R.string.rooms_rule_late), listOf(R.string.rooms_opt_no, R.string.rooms_opt_marked, R.string.rooms_opt_grace), r.late.ordinal) {
            vm.setRules(room.id, r.copy(late = LateRule.entries[it]))
        }
        RuleBlock(stringResource(R.string.rooms_rule_structure), listOf(R.string.rooms_opt_only_you, R.string.rooms_opt_everyone), r.structure.ordinal) {
            vm.setRules(room.id, r.copy(structure = StructureRule.entries[it]))
        }
        RuleBlock(stringResource(R.string.rooms_rule_visibility), listOf(R.string.rooms_opt_always, R.string.rooms_opt_on_submit), r.visibility.ordinal) {
            vm.setRules(room.id, r.copy(visibility = VisibilityRule.entries[it]))
        }
        RuleBlock(stringResource(R.string.rooms_rule_reminders), listOf(R.string.rooms_opt_no, R.string.rooms_opt_1day, R.string.rooms_opt_3days), r.reminders.ordinal) {
            vm.setRules(room.id, r.copy(reminders = ReminderRule.entries[it]))
        }
        Text(stringResource(R.string.rooms_rules_entry_note), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 10.dp, start = 2.dp))
    }
}

@Composable
private fun RuleBlock(title: String, options: List<Int>, selected: Int, onSelect: (Int) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).padding(12.dp)) {
        Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.padding(bottom = 8.dp))
        SegmG(options.map { stringResource(it) }, selected, onSelect)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenameSheet(room: WorkRoom, vm: RoomsViewModel, subjects: List<com.unistack.app.feature_grades.domain.Subject>, onDismiss: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(room.title) }
    var subject by remember { mutableStateOf(room.subjectId) }
    RoomSheet(onDismiss, stringResource(R.string.rooms_name_and_subject)) {
        RoomField(title, { title = it.take(80) }, single = true, fontSize = 15f)
        RoomLabel(stringResource(R.string.rooms_subject))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            subjects.forEach { s -> ChipL(s.name, subject == s.id) { subject = s.id } }
            ChipL(stringResource(R.string.rooms_no_subject), subject == null) { subject = null }
        }
        Pill(stringResource(R.string.rooms_save), { vm.rename(room.id, title, subject); context.roomToast(context.getString(R.string.rooms_saved)); onDismiss() }, Modifier.fillMaxWidth().padding(top = 12.dp))
    }
}

/** Borrar pide dos confirmaciones: se borra para todos y no se puede deshacer. */
@Composable
private fun DeleteRoomSheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    val context = LocalContext.current
    var sure by remember { mutableStateOf(false) }
    RoomSheet(onDismiss, stringResource(if (sure) R.string.rooms_delete_sure else R.string.rooms_delete_q),
        context.resources.getQuantityString(R.plurals.rooms_delete_d, room.activeMembers.size, room.activeMembers.size)) {
        Pill(stringResource(if (sure) R.string.rooms_delete_final else R.string.rooms_yes_delete), {
            if (!sure) sure = true else {
                room.materials.mapNotNull { it.file }.forEach { vm.files.delete(it) }
                vm.delete(room.id); context.roomToast(context.getString(R.string.rooms_deleted)); onDismiss(); onDeleted()
            }
        }, Modifier.fillMaxWidth(), style = PillStyle.ERR)
    }
}
