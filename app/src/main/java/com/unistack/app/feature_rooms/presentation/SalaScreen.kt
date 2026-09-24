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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.WatchLater
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomComment
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import com.unistack.app.feature_tasks.presentation.SubjectShapeIcon

val REACTION_EMOJIS = listOf("👍", "🔥", "❤️", "👀", "✅")

val MATERIAL_ORDER = listOf(MaterialType.LINK, MaterialType.DOC, MaterialType.PHOTO, MaterialType.VIDEO, MaterialType.AUDIO, MaterialType.SOURCE, MaterialType.NOTE)

/**
 * Dentro de la sala — artifacts «Dentro de la sala» y «el líder en la sala», cerrados el 23 sep:
 * una columna con las secciones por estado (A2), la barra de arriba con los iconos E, el
 * material de apoyo al final y, abajo, «Añadir al trabajo» (y «Crear trabajo» al líder cuando
 * todo está entregado).
 */
@Composable
fun SalaScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val subjects by vm.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == room.subjectId }
    val today = vm.today()
    var open by remember { mutableStateOf(setOf<String>()) }
    var comsOpen by remember { mutableStateOf(setOf<String>()) }
    var addFor by remember { mutableStateOf<String?>(null) }
    var assignSheet by remember { mutableStateOf<String?>(null) }
    var askSheet by remember { mutableStateOf<String?>(null) }
    var giveSheet by remember { mutableStateOf<String?>(null) }
    val pokeMsg = stringResource(R.string.rooms_poke_sent)
    val poke: (String) -> Unit = { name -> context.roomToast(String.format(pokeMsg, name)) }
    val tookMsg = stringResource(R.string.rooms_is_yours)
    val take: (RoomPart) -> Unit = { p -> vm.take(room.id, p.id); context.roomToast(String.format(tookMsg, p.name)) }

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp)) { UniBackButton(onClick = onBack) }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)) {
                item { SalaShortcuts(room, go) }
                item { SalaCover(room, subject, today, go, onTake = take, onPoke = poke) }
                val joiner = RoomLogic.pendingEntry(room)
                if (joiner != null) item { EntryNotice(room, joiner.id) { giveSheet = joiner.id } }
                val mine = room.requests.lastOrNull { it.fromId == room.meId && it.resolution == null }
                if (!room.isLeader && mine != null) item { MyRequestNotice(room, mine.type) }
                partGroups(room, today).forEach { (label, parts) ->
                    item { RoomLabel(stringResource(label), trailing = "${parts.size}", modifier = Modifier.padding(top = 2.dp)) }
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            parts.forEachIndexed { i, p ->
                                PartRow(
                                    room, p, today, groupShape(i, parts.size, 22.dp), p.id in open, p.id in comsOpen, vm,
                                    onToggle = { open = if (p.id in open) open - p.id else open + p.id },
                                    onComs = { comsOpen = if (p.id in comsOpen) comsOpen - p.id else comsOpen + p.id },
                                    go = go, onAdd = { addFor = p.id }, onAssign = { assignSheet = p.id }, onAsk = { askSheet = p.id },
                                    onTake = { take(p) }, onPoke = poke
                                )
                            }
                        }
                    }
                }
                item { MaterialEntry(room) { go("material", "-") } }
            }
        }
        val canCreate = room.isLeader && room.allDelivered
        RoomDock(Modifier.align(Alignment.BottomCenter)) {
            Pill(stringResource(R.string.rooms_add_to_work), { addFor = "" }, Modifier.weight(1f), icon = Icons.Rounded.Add,
                style = if (canCreate) PillStyle.TONAL else PillStyle.FILL, big = true)
            if (canCreate) Pill(stringResource(R.string.rooms_create_work), { go("crear", "-") }, Modifier.weight(1f), icon = Icons.Rounded.Build, big = true)
        }
    }

    addFor?.let { target -> AddMaterialFlow(room, vm, presetPart = target.ifBlank { null }) { addFor = null } }
    assignSheet?.let { pid -> AssignSheet(room, pid, vm) { assignSheet = null } }
    giveSheet?.let { mid -> GivePartSheet(room, mid, vm) { giveSheet = null } }
    askSheet?.let { pid -> AskLeaderSheet(room, pid, vm) { askSheet = null } }
}

/** Las tres secciones de la A2: lo que necesita atención, lo que va en curso y lo entregado. */
fun partGroups(room: WorkRoom, today: Long): List<Pair<Int, List<RoomPart>>> = listOf(
    R.string.rooms_needs_attention to room.parts.filter { val s = RoomLogic.status(room, it, today); s == RoomLogic.Status.OVERDUE || s == RoomLogic.Status.FREE },
    R.string.rooms_in_progress to room.parts.filter { val s = RoomLogic.status(room, it, today); s == RoomLogic.Status.IN_PROGRESS || s == RoomLogic.Status.PENDING },
    R.string.rooms_delivered_group to room.parts.filter { RoomLogic.status(room, it, today) == RoomLogic.Status.DELIVERED }
).filter { it.second.isNotEmpty() }

/** `.dock`: pie anclado sobre un degradado al fondo. */
@Composable
fun RoomDock(modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit) {
    val bg = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxWidth().background(Brush.verticalGradient(0f to bg.copy(alpha = 0f), 0.3f to bg, 1f to bg))) {
        Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), content = content)
    }
}

/**
 * Los accesos de la sala, cuatro botones iguales en fila: Chat, Grupo, Novedades y, al líder,
 * Gestionar en el acento (elegidos de la opción B y en ese orden, 24 sep). Antes eran pastillas
 * sueltas apretadas a la derecha de la barra.
 */
@Composable
private fun SalaShortcuts(room: WorkRoom, go: (String, String) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Shortcut(RoomIcons.Chat, stringResource(R.string.rooms_chat), RoomLogic.unseenChat(room)) { go("chat", "-") }
        Shortcut(RoomIcons.People, stringResource(R.string.rooms_group), 0) { go("grupo", "-") }
        Shortcut(RoomIcons.Bell, stringResource(R.string.rooms_news), RoomLogic.unseenEvents(room)) { go("nov", "-") }
        if (room.isLeader) Shortcut(Icons.Rounded.Tune, stringResource(R.string.rooms_manage), RoomLogic.pendingCount(room), accent = true) { go("gest", "-") }
    }
}

@Composable
private fun RowScope.Shortcut(icon: ImageVector, label: String, count: Int, accent: Boolean = false, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.weight(1f)) {
        Column(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (accent) cs.primary else cs.surfaceContainerLow)
                .cleanClickable(onClick = onClick).padding(top = 12.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(icon, null, tint = if (accent) cs.onPrimary else cs.onSurface, modifier = Modifier.size(22.dp))
            Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (accent) cs.onPrimary else cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (count > 0) CountBadge(count, if (accent) cs.error else cs.primary, Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 8.dp))
    }
}

/**
 * La portada de la sala (opción A, 24 sep): una tarjeta con el color de la materia que junta
 * materia y tipo, la entrega, el título, quiénes están, el avance y, dentro, «lo que te toca».
 */
@Composable
private fun SalaCover(room: WorkRoom, subject: Subject?, today: Long, go: (String, String) -> Unit, onTake: (RoomPart) -> Unit, onPoke: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mat = subjectColor(subject)
    val base = mix(mat, 0.13f, cs.surfaceContainerLow)
    Column(Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(base).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            SubjectShapeIcon(mat, room.subjectId ?: "none", size = 16.dp)
            Text(subject?.name ?: stringResource(R.string.rooms_no_subject), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = mat, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
            Text("·", fontSize = 12.sp, color = cs.onSurfaceVariant)
            Text(stringResource(RoomTemplates.typeNameRes(room.type)).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurfaceVariant, letterSpacing = 0.8.sp, maxLines = 1)
            Box(Modifier.weight(1f))
            Text(stringResource(R.string.rooms_due_in, relDay(room.dueEpochDay - today)), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1,
                modifier = Modifier.clip(CircleShape).background(mix(cs.background, 0.55f, base)).padding(horizontal = 10.dp, vertical = 5.dp))
        }
        Text(room.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, lineHeight = 28.sp, letterSpacing = (-0.3).sp, modifier = Modifier.padding(top = 10.dp, bottom = 8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            FaceStack(room, room.activeMembers, base, size = 24.dp, max = 5)
            Text(buildAnnotatedString {
                append(pluralText(R.plurals.rooms_n_members_leader, room.activeMembers.size) + " ")
                withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(room.nameOf(room.leaderId)) }
            }, fontSize = 12.5.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        SegmentBar(room.parts.map { p ->
            when (RoomLogic.status(room, p, today)) {
                RoomLogic.Status.DELIVERED -> RoomTone.VERDE.color
                RoomLogic.Status.OVERDUE -> RoomTone.ROJO.color
                RoomLogic.Status.IN_PROGRESS -> if (room.meId in p.ownerIds) cs.primary else mix(RoomTone.VERDE.color, 0.4f, cs.surfaceContainerHighest)
                else -> if (room.meId in p.ownerIds) cs.primary else cs.surfaceContainerHighest
            }
        }, Modifier.padding(top = 14.dp, bottom = 6.dp), height = 7.dp)
        Text(stringResource(R.string.rooms_sections_delivered, room.deliveredCount, room.parts.size), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
        TurnStrip(room, today, mix(cs.background, 0.6f, base), go, onTake, onPoke)
    }
}

/** «X entró a la sala · Todavía no tiene parte · Darle una» (sólo cuando el líder reparte). */
@Composable
fun EntryNotice(room: WorkRoom, memberId: String, onGive: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.padding(top = 14.dp, bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(cs.primaryContainer).padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MemberFace(room, memberId, 34.dp)
        Column(Modifier.weight(1f)) {
            Text(stringResource(R.string.rooms_joined, room.nameOf(memberId)), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
            Text(stringResource(R.string.rooms_joined_no_part), fontSize = 12.5.sp, color = cs.onSurface.copy(alpha = 0.85f))
        }
        Pill(stringResource(R.string.rooms_give_one), onGive, style = PillStyle.HERO, small = true)
    }
}

@Composable
private fun MyRequestNotice(room: WorkRoom, type: RequestType) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(mix(RoomTone.AMBAR.color, 0.14f, cs.surfaceContainerLow)).padding(horizontal = 13.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(Icons.Rounded.WatchLater, null, tint = RoomTone.AMBAR.color, modifier = Modifier.size(18.dp))
        Text(buildAnnotatedString {
            append(stringResource(requestSentText(type)) + " · ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(stringResource(R.string.rooms_waiting_for, room.nameOf(room.leaderId))) }
        }, fontSize = 13.sp, color = cs.onSurface)
    }
}

fun requestSentText(t: RequestType) = when (t) {
    RequestType.MORE_TIME -> R.string.rooms_req_sent_time
    RequestType.SWAP -> R.string.rooms_req_sent_swap
    RequestType.HELP, RequestType.JOIN -> R.string.rooms_req_sent_help
    RequestType.DROP -> R.string.rooms_req_sent_drop
}

/**
 * «Lo que te toca», dentro de la portada: el mosaico con su icono, qué es y para cuándo, y la
 * acción. Subir tu parte va a la derecha; las demás acciones, debajo.
 */
@Composable
private fun TurnStrip(room: WorkRoom, today: Long, bg: Color, go: (String, String) -> Unit, onTake: (RoomPart) -> Unit, onPoke: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mine = room.parts.filter { room.meId in it.ownerIds && it.state != PartState.DELIVERED }.sortedBy { RoomLogic.partDue(room, it) }
    val free = room.parts.firstOrNull { it.isFree && it.state != PartState.DELIVERED }
    val lateOther = room.parts.firstOrNull { RoomLogic.status(room, it, today) == RoomLogic.Status.OVERDUE && room.meId !in it.ownerIds }
    val lateName = lateOther?.ownerIds?.firstOrNull()?.let { room.nameOf(it) }
    class Turn(val label: String, val title: String, val body: String, val icon: ImageVector, val tone: Color)
    val turn = when {
        room.allDelivered && room.isLeader -> Turn(stringResource(R.string.rooms_all_delivered), stringResource(R.string.rooms_can_create), stringResource(R.string.rooms_can_create_d, room.parts.size), Icons.Rounded.Build, RoomTone.VERDE.color)
        room.allDelivered -> Turn(stringResource(R.string.rooms_all_delivered), stringResource(R.string.rooms_nothing_left), stringResource(R.string.rooms_leader_must_create, room.nameOf(room.leaderId)), Icons.Rounded.HourglassTop, RoomTone.AMBAR.color)
        mine.isNotEmpty() -> {
            val p = mine.first()
            val extra = when {
                room.isLeader && lateOther != null && lateName != null -> stringResource(R.string.rooms_and_late, lateName, lateOther.name)
                p.hasContent -> stringResource(R.string.rooms_toca_draft)
                p.attachments.isNotEmpty() -> pluralText(R.plurals.rooms_toca_contribs, p.attachments.size)
                else -> stringResource(R.string.rooms_toca_unstarted)
            }
            Turn(stringResource(R.string.rooms_hero_yours), p.name, stringResource(R.string.rooms_card_for, relDay(RoomLogic.partDue(room, p) - today)) + " · " + extra, Icons.Rounded.EditNote, cs.primary)
        }
        free != null && room.split != SplitMode.ASSIGN -> Turn(stringResource(R.string.rooms_hero_no_part), stringResource(R.string.rooms_take_one_title), pluralText(R.plurals.rooms_free_parts_n, room.parts.count { it.isFree }), Icons.Rounded.PanTool, RoomTone.CIAN.color)
        else -> Turn(stringResource(R.string.rooms_hero_calm), if (room.partsOf(room.meId).isEmpty()) stringResource(R.string.rooms_no_part_yet) else stringResource(R.string.rooms_hero_calm_t),
            stringResource(R.string.rooms_card_delivered_of, room.deliveredCount, room.parts.size), Icons.Rounded.Check, RoomTone.VERDE.color)
    }
    Column(Modifier.padding(top = 14.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(bg).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(turn.icon, turn.tone, 42.dp, 13.dp, filled = true)
            Column(Modifier.weight(1f)) {
                Text(turn.label.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp, color = cs.onSurfaceVariant, maxLines = 1)
                Text(turn.title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(turn.body, fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
            }
            if (mine.isNotEmpty() && !room.allDelivered) Pill(stringResource(R.string.rooms_upload), { go("parte", mine.first().id) }, small = true)
        }
        val second: (@Composable () -> Unit)? = when {
            room.allDelivered && room.isLeader -> { { Pill(stringResource(R.string.rooms_create_work), { go("crear", "-") }, icon = Icons.Rounded.Build, small = true) } }
            mine.isNotEmpty() && room.isLeader && lateOther != null && lateName != null -> { { Pill(stringResource(R.string.rooms_poke_to, lateName), { onPoke(lateName) }, icon = Icons.Rounded.PanTool, style = PillStyle.RAISED, small = true) } }
            mine.isNotEmpty() && free != null && room.split != SplitMode.ASSIGN -> { { Pill(stringResource(R.string.rooms_take_x, free.name), { onTake(free) }, icon = Icons.Rounded.PanTool, style = PillStyle.RAISED, small = true) } }
            mine.isEmpty() && !room.allDelivered && free != null && room.split != SplitMode.ASSIGN -> { { Pill(stringResource(R.string.rooms_take_x, free.name), { onTake(free) }, icon = Icons.Rounded.PanTool, small = true) } }
            else -> null
        }
        if (second != null) Row(Modifier.padding(start = 54.dp, top = 10.dp)) { second() }
    }
}

@Composable
fun pluralText(id: Int, n: Int): String = LocalContext.current.resources.getQuantityString(id, n, n)

/** La etiqueta de estado de una sección (`.chip`). */
@Composable
fun PartChip(room: WorkRoom, p: RoomPart, today: Long) {
    if (p.approved) { StatusChip(stringResource(R.string.rooms_chip_ready), RoomTone.VERDE.color); return }
    when (RoomLogic.status(room, p, today)) {
        RoomLogic.Status.DELIVERED -> StatusChip(stringResource(R.string.rooms_chip_delivered), RoomTone.VERDE.color)
        RoomLogic.Status.OVERDUE -> StatusChip(stringResource(R.string.rooms_chip_overdue), RoomTone.ROJO.color)
        RoomLogic.Status.IN_PROGRESS -> StatusChip(stringResource(R.string.rooms_chip_progress), RoomTone.AMBAR.color)
        RoomLogic.Status.FREE -> NeutralChip(stringResource(R.string.rooms_chip_free))
        RoomLogic.Status.PENDING -> NeutralChip(stringResource(R.string.rooms_chip_pending))
    }
}

/** `.chip.libre`: fondo más alto y texto secundario. */
@Composable
fun NeutralChip(text: String) {
    val cs = MaterialTheme.colorScheme
    Text(text.uppercase(), color = cs.onSurfaceVariant, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp, maxLines = 1,
        modifier = Modifier.clip(CircleShape).background(cs.surfaceContainerHighest).padding(horizontal = 9.dp, vertical = 4.dp))
}

/** «entregó ayer» / «venció hace 2 días» / «para mañana». */
@Composable
fun whenText(room: WorkRoom, p: RoomPart, today: Long): String {
    val d = RoomLogic.partDue(room, p) - today
    return when (RoomLogic.status(room, p, today)) {
        RoomLogic.Status.DELIVERED -> stringResource(R.string.rooms_delivered_when, relDay(epochDayOf(p.deliveredAt ?: 0L) - today))
        RoomLogic.Status.OVERDUE -> stringResource(R.string.rooms_overdue_when, relDay(d))
        else -> stringResource(R.string.rooms_card_for, relDay(d))
    }
}

fun epochDayOf(millis: Long): Long = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()

/** «Tú», «Sam», «Sam, Nico» o «Sin dueño». */
@Composable
fun ownersText(room: WorkRoom, p: RoomPart): String {
    val you = stringResource(R.string.rooms_you)
    return if (p.ownerIds.isEmpty()) stringResource(R.string.rooms_no_owner) else p.ownerIds.joinToString(", ") { if (it == room.meId) you else room.nameOf(it) }
}

/** La fila de una sección: igual para todas; al abrirla, texto, aportes, pie y comentarios. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PartRow(
    room: WorkRoom, p: RoomPart, today: Long, shape: Shape, open: Boolean, comsOpen: Boolean, vm: RoomsViewModel,
    onToggle: () -> Unit, onComs: () -> Unit, go: (String, String) -> Unit, onAdd: () -> Unit, onAssign: () -> Unit, onAsk: () -> Unit,
    onTake: () -> Unit, onPoke: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val st = RoomLogic.status(room, p, today)
    val mine = room.meId in p.ownerIds
    Column(Modifier.fillMaxWidth().clip(shape).background(if (mine) mix(cs.primary, 0.10f, cs.surfaceContainerLow) else cs.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().cleanClickable(onClick = onToggle).heightIn(min = 62.dp).padding(start = 13.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            PartNumber(room, p, st)
            Column(Modifier.weight(1f)) {
                Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 1.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    p.ownerIds.firstOrNull()?.let { MemberFace(room, it, 20.dp) }
                    Text(ownersText(room, p) + " · " + whenText(room, p, today), fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            PartChip(room, p, today)
            val rot by animateFloatAsState(if (open) 180f else 0f, label = "chev")
            Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(rot))
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp)) {
                if (p.text.isNotBlank()) Text(p.text, fontSize = 13.sp, lineHeight = 20.sp, color = cs.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.background).padding(horizontal = 12.dp, vertical = 10.dp))
                val aportes = aportesOf(room, p)
                if (aportes.isNotEmpty()) Column(Modifier.padding(top = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    aportes.forEach { a -> AporteRow(room, a, today, vm, p.id) }
                } else Text(stringResource(R.string.rooms_no_contrib), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp, start = 2.dp))
                /*
                 * Botones que se ven como botones: dentro de la tarjeta van en el tonal alto (el tonal
                 * normal es del mismo color que la tarjeta y parecían texto suelto, 23 sep). El líder
                 * también puede quedarse una parte libre, y los comentarios dicen «Comentar».
                 */
                FlowRow(Modifier.padding(top = 10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    when {
                        p.isFree && room.isLeader -> {
                            Pill(stringResource(R.string.rooms_take_mine), onTake, icon = Icons.Rounded.PanTool, small = true)
                            Pill(stringResource(R.string.rooms_assign), onAssign, icon = Icons.Rounded.PersonAdd, style = PillStyle.RAISED, small = true)
                        }
                        p.isFree -> Pill(stringResource(R.string.rooms_take_this), onTake, icon = Icons.Rounded.PanTool, small = true)
                        st == RoomLogic.Status.OVERDUE && room.isLeader && !mine ->
                            Pill(stringResource(R.string.rooms_give_poke), { onPoke(room.nameOf(p.ownerIds.first())) }, icon = Icons.Rounded.PanTool, style = PillStyle.ERR, small = true)
                        mine && p.state != PartState.DELIVERED -> Pill(stringResource(R.string.rooms_hero_upload), { go("parte", p.id) }, icon = Icons.Rounded.Upload, small = true)
                        mine -> Pill(stringResource(R.string.rooms_open_part), { go("parte", p.id) }, style = PillStyle.RAISED, small = true)
                    }
                    Pill(stringResource(R.string.rooms_add_short), onAdd, icon = Icons.Rounded.Add, style = PillStyle.RAISED, small = true)
                    Pill(if (p.comments.isEmpty()) stringResource(R.string.rooms_comment_action) else stringResource(R.string.rooms_comments) + " · ${p.comments.size}", onComs,
                        icon = Icons.AutoMirrored.Rounded.Comment, style = if (comsOpen) PillStyle.HERO else PillStyle.RAISED, small = true)
                    if (!room.isLeader && mine && p.state != PartState.DELIVERED) Box(Modifier.size(32.dp).clip(CircleShape).background(cs.surfaceContainerHighest).cleanClickable(onClick = onAsk), contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.MoreVert, stringResource(R.string.rooms_ask_leader), tint = cs.onSurface, modifier = Modifier.size(16.dp))
                    }
                }
                AnimatedVisibility(comsOpen) { InlineComments(room, p.comments, stringResource(R.string.rooms_comment_in, p.name)) { vm.comment(room.id, p.id, it) } }
            }
        }
    }
}

/** `.secN`: el número (o ✓) de la sección. */
@Composable
fun PartNumber(room: WorkRoom, p: RoomPart, st: RoomLogic.Status, size: Dp = 28.dp) {
    val cs = MaterialTheme.colorScheme
    val (bg, fg) = when (st) {
        RoomLogic.Status.DELIVERED -> RoomTone.VERDE.color to cs.background
        RoomLogic.Status.OVERDUE -> RoomTone.ROJO.color to cs.background
        else -> cs.surfaceContainerHighest to cs.onSurfaceVariant
    }
    Box(Modifier.size(size).clip(RoundedCornerShape(9.dp)).background(bg), contentAlignment = Alignment.Center) {
        if (st == RoomLogic.Status.DELIVERED) Icon(Icons.Rounded.Check, null, tint = fg, modifier = Modifier.size(16.dp))
        else Text("${room.parts.indexOf(p) + 1}", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

/** Un aporte de una sección: lo adjunto a la parte y el material que se vinculó a ella. */
data class Aporte(val id: String, val type: MaterialType, val name: String, val byId: String, val createdAt: Long, val reactions: Map<String, List<String>>, val isMaterial: Boolean)

fun aportesOf(room: WorkRoom, p: RoomPart): List<Aporte> =
    (p.attachments.map { Aporte(it.id, it.type, it.name, it.byId, it.createdAt, it.reactions, false) } +
        room.materials.filter { it.partId == p.id }.map { Aporte(it.id, it.type, if (it.author.isNotBlank()) "${it.author} — ${it.name}" else it.name, it.byId, it.createdAt, it.reactions, true) })
        .sortedBy { it.createdAt }

@Composable
fun AporteRow(room: WorkRoom, a: Aporte, today: Long, vm: RoomsViewModel, partId: String) {
    val cs = MaterialTheme.colorScheme
    var picker by remember { mutableStateOf(false) }
    val react: (String) -> Unit = { e -> if (a.isMaterial) vm.reactMaterial(room.id, a.id, e) else vm.reactAttachment(room.id, partId, a.id, e); picker = false }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.background).padding(horizontal = 10.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            Icon(materialIcon(a.type), null, tint = cs.onSurfaceVariant, modifier = Modifier.size(17.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(a.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(whoText(room, a.byId, stringResource(R.string.rooms_you)) + " · " + relDay(epochDayOf(a.createdAt) - today), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
            ReactionRow(a.reactions, room.meId, picker, onPicker = { picker = !picker }, onReact = react)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReactionRow(reactions: Map<String, List<String>>, me: String, picker: Boolean, onPicker: () -> Unit, onReact: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    FlowRow(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        reactions.filterValues { it.isNotEmpty() }.forEach { (e, who) ->
            val yo = me in who
            Row(
                Modifier.clip(CircleShape).background(if (yo) mix(cs.primary, 0.22f, cs.surfaceContainer) else cs.surfaceContainer)
                    .then(if (yo) Modifier.border(1.5.dp, cs.primary, CircleShape) else Modifier).cleanClickable { onReact(e) }.padding(horizontal = 8.dp, vertical = 3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically
            ) { Text(e, fontSize = 12.sp); Text("${who.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface) }
        }
        Text("＋ ☺", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant,
            modifier = Modifier.clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onPicker).padding(horizontal = 8.dp, vertical = 3.dp))
    }
    AnimatedVisibility(picker) {
        Row(Modifier.padding(top = 6.dp).clip(CircleShape).background(cs.surfaceContainerHighest).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            REACTION_EMOJIS.forEach { e -> Text(e, fontSize = 17.sp, modifier = Modifier.clip(CircleShape).cleanClickable { onReact(e) }.padding(horizontal = 6.dp, vertical = 4.dp)) }
        }
    }
}

/** `.coms`: los comentarios dentro de la fila, con su campo. */
@Composable
fun InlineComments(room: WorkRoom, comments: List<RoomComment>, hint: String, onSend: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val today = epochDayOf(System.currentTimeMillis())
    val reviewWord = stringResource(R.string.rooms_review_word)
    val you = stringResource(R.string.rooms_you)
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(cs.outlineVariant.copy(alpha = 0.5f)))
        comments.forEach { c ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MemberFace(room, c.byId, 20.dp)
                Text(buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(whoText(room, c.byId, you)) }
                    if (c.isReview) withStyle(SpanStyle(color = RoomTone.AZUL.color, fontWeight = FontWeight.Bold)) { append(" · $reviewWord") }
                    append(" ${c.text} ")
                    withStyle(SpanStyle(color = cs.onSurfaceVariant, fontSize = 11.sp)) { append("· " + relDay(epochDayOf(c.createdAt) - today)) }
                }, fontSize = 12.5.sp, color = cs.onSurface, lineHeight = 18.sp, modifier = Modifier.weight(1f))
            }
        }
        CommentInput(hint, onSend)
    }
}

/** `.inp`: el campo en pastilla con el botón de enviar. */
@Composable
fun CommentInput(hint: String, onSend: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var input by remember { mutableStateOf("") }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.weight(1f).clip(CircleShape).background(cs.surfaceContainer).padding(horizontal = 14.dp, vertical = 10.dp)) {
            if (input.isEmpty()) Text(hint, fontSize = 13.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            BasicTextField(input, { input = it }, singleLine = true, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 13.sp), modifier = Modifier.fillMaxWidth())
        }
        Box(Modifier.size(38.dp).clip(CircleShape).background(cs.primary).cleanClickable { if (input.isNotBlank()) { onSend(input.trim()); input = "" } }, contentAlignment = Alignment.Center) {
            Icon(Icons.AutoMirrored.Rounded.Send, null, tint = cs.onPrimary, modifier = Modifier.size(18.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MaterialEntry(room: WorkRoom, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val all = room.materials
    RoomLabel(stringResource(R.string.rooms_material_title), trailing = pluralText(R.plurals.rooms_things_n, all.size), modifier = Modifier.padding(top = 2.dp))
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainerLow).cleanClickable(onClick = onClick).padding(14.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_material_entry_t), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_material_entry_d), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
            Box(Modifier.size(32.dp).clip(CircleShape).background(cs.surfaceContainer), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ChevronRight, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
        val counts = MATERIAL_ORDER.map { it to all.count { m -> m.type == it } }.filter { it.second > 0 }
        if (counts.isNotEmpty()) FlowRow(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            counts.forEach { (t, n) ->
                val c = t.tone.color
                Row(Modifier.clip(RoundedCornerShape(10.dp)).background(mix(c, 0.14f, Color.Transparent)).padding(start = 7.dp, end = 9.dp, top = 5.dp, bottom = 5.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(materialIcon(t), null, tint = c, modifier = Modifier.size(15.dp))
                    Text("$n", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = c)
                }
            }
        }
    }
}
