package com.unistack.app.feature_rooms.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.BookmarkAdd
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Poll
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Segment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.data.RoomStoredFile
import com.unistack.app.feature_rooms.domain.ChatMessage
import com.unistack.app.feature_rooms.domain.MessageKind
import com.unistack.app.feature_rooms.domain.WorkRoom
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val HOUR = DateTimeFormatter.ofPattern("HH:mm")
fun hourOf(millis: Long): String = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).format(HOUR)

private const val CHAT_EMOJIS = "👍❤️😂🔥👀"
private val CHAT_REACTIONS = listOf("👍", "❤️", "😂", "🔥", "👀")

/**
 * Chat del grupo — artifact «chat del grupo y novedades», opción A: pantalla propia, con las 18
 * funciones (responder, reaccionar, fijar, guardar en material, @menciones, enlazar partes,
 * fotos, archivos, enlaces, encuestas y notas de voz manteniendo el micrófono). Avisos
 * automáticos, sólo entregas y cambios de fecha (opción c).
 */
@Composable
fun ChatScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit, prefill: String = "") {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var input by remember { mutableStateOf(if (prefill.isNotBlank()) "$prefill " else "") }
    var selected by remember { mutableStateOf<String?>(null) }
    var replyTo by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<String?>(null) }
    val subjects by vm.subjects.collectAsState()
    val mat = subjectColor(subjects.firstOrNull { it.id == room.subjectId })
    val list = rememberLazyListState()
    val msgs = room.messages.filter { m -> query.isBlank() || m.systemType != null || listOf(m.text, m.subtitle).any { it.contains(query, true) } || m.pollOptions.any { it.label.contains(query, true) } }

    LaunchedEffect(room.messages.size) {
        vm.markChatSeen(room.id)
        val n = list.layoutInfo.totalItemsCount
        if (n > 0) list.scrollToItem(n - 1)
    }

    val sendText = {
        val t = input.trim()
        if (t.isNotEmpty()) {
            vm.send(room.id, linkParts(room, t), replyTo)
            input = ""; replyTo = null
        }
    }
    val sendFile: (MessageKind, RoomStoredFile) -> Unit = { k, f -> vm.sendFile(room.id, k, f, fileSubtitle(f.mimeType, f.sizeBytes)) }
    val picker = rememberRoomPicker(vm) { f -> sendFile(if (f.mimeType.startsWith("image/")) MessageKind.PHOTO else MessageKind.FILE, f) }

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding().imePadding()) {
        Row(Modifier.fillMaxWidth().drawBehind {
            drawLine(cs.outlineVariant.copy(alpha = 0.35f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
        }.padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UniBackButton(onClick = onBack)
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(stringResource(R.string.rooms_group_chat), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1)
                    if (room.chatMuted) Icon(Icons.Rounded.NotificationsOff, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(15.dp))
                }
                Text(pluralText(R.plurals.rooms_n_people, room.activeMembers.size), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
            PlainIcon(Icons.Rounded.Search, stringResource(R.string.rooms_search)) { searching = !searching; query = "" }
            PlainIcon(Icons.Rounded.MoreVert, stringResource(R.string.rooms_more)) { sheet = "more" }
        }
        if (searching) Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.weight(1f).clip(CircleShape).background(cs.surfaceContainer).padding(horizontal = 14.dp, vertical = 10.dp)) {
                if (query.isEmpty()) Text(stringResource(R.string.rooms_search_chat), fontSize = 13.5.sp, color = cs.onSurfaceVariant)
                BasicTextField(query, { query = it }, singleLine = true, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 13.5.sp), modifier = Modifier.fillMaxWidth())
            }
            Text(stringResource(R.string.rooms_close), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.cleanClickable { searching = false; query = "" }.padding(vertical = 6.dp, horizontal = 2.dp))
        }
        room.messages.firstOrNull { it.id == room.pinnedMessageId }?.let { pin ->
            Row(Modifier.fillMaxWidth().background(cs.surfaceContainerLow).padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Rounded.PushPin, null, tint = cs.primary, modifier = Modifier.size(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.rooms_pinned_msg).uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, color = cs.primary)
                    Text(plainOf(pin), fontSize = 12.5.sp, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Box(Modifier.size(30.dp).clip(CircleShape).cleanClickable { vm.pin(room.id, null) }, contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Close, stringResource(R.string.rooms_unpin), tint = cs.onSurface, modifier = Modifier.size(16.dp))
                }
            }
        }
        selected?.let { id -> room.messages.firstOrNull { it.id == id } }?.let { m ->
            Row(Modifier.fillMaxWidth().background(cs.surfaceContainerHigh).padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    CHAT_REACTIONS.forEach { e -> Text(e, fontSize = 19.sp, lineHeight = 1.2.em, modifier = Modifier.clip(CircleShape).cleanClickable { vm.reactMessage(room.id, m.id, e); selected = null }.padding(horizontal = 5.dp, vertical = 4.dp)) }
                }
                PlainIcon(Icons.AutoMirrored.Rounded.Reply, stringResource(R.string.rooms_reply), 36) { replyTo = m.id; selected = null }
                PlainIcon(Icons.Rounded.PushPin, stringResource(R.string.rooms_pin), 36) { vm.pin(room.id, m.id); selected = null; context.roomToast(context.getString(R.string.rooms_pinned_for_all)) }
                if (m.kind == MessageKind.PHOTO || m.kind == MessageKind.FILE || m.kind == MessageKind.LINK) PlainIcon(Icons.Rounded.BookmarkAdd, stringResource(R.string.rooms_save_material), 36) {
                    vm.saveMessageToMaterial(room.id, m.id); selected = null; context.roomToast(context.getString(R.string.rooms_saved_material))
                }
                PlainIcon(Icons.Rounded.Close, stringResource(R.string.rooms_close), 36) { selected = null }
            }
        }
        if (room.chatMuted) Row(Modifier.fillMaxWidth().background(cs.surfaceContainerLow).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsOff, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.rooms_chat_muted), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
        }
        LazyColumn(Modifier.weight(1f), state = list, contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (msgs.isEmpty()) item {
                Text(stringResource(R.string.rooms_chat_empty), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(30.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            itemsIndexed(msgs, key = { _, m -> m.id }) { i, m ->
                val prev = msgs.getOrNull(i - 1)
                val day = epochDayOf(m.createdAt)
                if (prev == null || epochDayOf(prev.createdAt) != day) DaySeparator(day, vm.today())
                if (m.systemType != null) SystemNotice(room, m)
                else {
                    val cont = prev != null && prev.systemType == null && prev.byId == m.byId && epochDayOf(prev.createdAt) == day
                    MessageBubble(room, m, cont, m.id == selected, mat, vm, query,
                        onSelect = { selected = if (selected == m.id) null else m.id },
                        onPart = { pid -> go("parte", pid) })
                }
            }
            item { Box(Modifier.height(1.dp)) }
        }
        Composer(room, vm, input, { input = it }, replyTo?.let { id -> room.messages.firstOrNull { it.id == id } }, onCancelReply = { replyTo = null },
            onSend = sendText, onAttach = { sheet = "attach" }, onVoice = { f, secs -> vm.sendFile(room.id, MessageKind.VOICE, f, "", secs) })
    }

    when (sheet) {
        "attach" -> AttachToChatSheet(room, mat, onDismiss = { sheet = null }, onPhoto = { sheet = null; picker.pickImage() }, onFile = { sheet = null; picker.pickFile(ANY_TYPES) },
            onLink = { sheet = "link" }, onPart = { sheet = "part" }, onPoll = { sheet = "poll" }, onVoice = { sheet = null; context.roomToast(context.getString(R.string.rooms_hold_to_record)) })
        "part" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_link_part), stringResource(R.string.rooms_link_part_d)) {
            room.parts.forEach { p -> SheetRow(p.name, onClick = { vm.send(room.id, context.getString(R.string.rooms_look_part, "[[${p.name}]]")); sheet = null }) }
        }
        "link" -> {
            var url by remember { mutableStateOf("") }
            RoomSheet({ sheet = null }, stringResource(R.string.rooms_mt_link)) {
                RoomField(url, { url = it }, "https://", single = true)
                Pill(stringResource(R.string.rooms_send), {
                    if (url.isNotBlank()) { vm.send(room.id, url.trim(), kind = MessageKind.LINK, subtitle = hostOf(url.trim())); sheet = null }
                }, Modifier.fillMaxWidth().padding(top = 10.dp), enabled = url.isNotBlank())
            }
        }
        "poll" -> {
            var q by remember { mutableStateOf("") }
            var opts by remember { mutableStateOf(listOf("", "")) }
            RoomSheet({ sheet = null }, stringResource(R.string.rooms_quick_poll)) {
                RoomField(q, { q = it }, stringResource(R.string.rooms_poll_q_hint), single = true, fontSize = 14f)
                opts.forEachIndexed { i, o ->
                    Box(Modifier.height(6.dp))
                    RoomField(o, { v -> opts = opts.toMutableList().also { it[i] = v } }, stringResource(R.string.rooms_option_n, i + 1), single = true, fontSize = 14f)
                }
                if (opts.size < 6) Text(stringResource(R.string.rooms_add_option), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary,
                    modifier = Modifier.padding(top = 8.dp).cleanClickable { opts = opts + "" }.padding(vertical = 6.dp, horizontal = 2.dp))
                val ok = q.isNotBlank() && opts.count { it.isNotBlank() } >= 2
                Pill(stringResource(R.string.rooms_send_poll), { if (ok) { vm.send(room.id, q.trim(), kind = MessageKind.POLL, poll = opts.map { it.trim() }.filter { it.isNotBlank() }); sheet = null } },
                    Modifier.fillMaxWidth().padding(top = 6.dp), enabled = ok)
            }
        }
        "more" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_group_chat)) {
            SheetRow(stringResource(if (room.chatMuted) R.string.rooms_unmute_chat else R.string.rooms_mute_chat), stringResource(if (room.chatMuted) R.string.rooms_unmute_chat_d else R.string.rooms_mute_chat_d),
                Icons.Rounded.NotificationsOff, onClick = {
                    vm.update(room.id) { it.copy(chatMuted = !it.chatMuted) }; sheet = null
                    context.roomToast(context.getString(if (room.chatMuted) R.string.rooms_notifs_on else R.string.rooms_chat_muted_toast))
                })
            SheetRow(stringResource(R.string.rooms_search_chat), icon = Icons.Rounded.Search, onClick = { searching = true; sheet = null })
            SheetRow(stringResource(R.string.rooms_chat_media), stringResource(R.string.rooms_chat_media_d), Icons.Rounded.Photo, onClick = { sheet = "media" })
        }
        "media" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_chat_media), tall = true) {
            val media = room.messages.filter { it.kind == MessageKind.PHOTO || it.kind == MessageKind.FILE || it.kind == MessageKind.LINK || it.kind == MessageKind.VOICE }.reversed()
            if (media.isEmpty()) Text(stringResource(R.string.rooms_chat_media_empty), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(4.dp))
            media.forEach { m ->
                SheetRow(m.text, listOf(room.nameOf(m.byId), m.subtitle).filter { it.isNotBlank() }.joinToString(" · "), leading = { TypeIcon(kindIcon(m.kind), kindTone(m.kind)) }, onClick = {
                    when { m.file != null -> context.openRoomFile(vm, m.file, m.mime, m.text); m.kind == MessageKind.LINK -> context.openUrl(m.text) }
                })
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") CHAT_EMOJIS
}

fun kindIcon(k: MessageKind) = when (k) {
    MessageKind.PHOTO -> Icons.Rounded.Photo
    MessageKind.LINK -> Icons.Rounded.Link
    MessageKind.VOICE -> Icons.Rounded.Mic
    MessageKind.POLL -> Icons.Rounded.Poll
    else -> Icons.Rounded.Description
}

@Composable
fun kindTone(k: MessageKind) = when (k) {
    MessageKind.PHOTO -> RoomTone.ROSA.color
    MessageKind.LINK -> RoomTone.AZUL.color
    MessageKind.VOICE -> RoomTone.NARANJA.color
    MessageKind.POLL -> RoomTone.VERDE.color
    else -> RoomTone.INDIGO.color
}

/** Lo que dice un mensaje en una línea (para el fijado y la respuesta). */
@Composable
fun plainOf(m: ChatMessage): String = when (m.kind) {
    MessageKind.VOICE -> stringResource(R.string.rooms_voice_note)
    else -> m.text.replace("[[", "").replace("]]", "")
}

/** Escribe [[Parte]] donde el texto nombra una parte, para que salga como enlace. */
fun linkParts(room: WorkRoom, text: String): String {
    var t = text
    room.parts.sortedByDescending { it.name.length }.forEach { p ->
        if (p.name.length >= 3 && !t.contains("[[${p.name}]]")) t = t.replace(Regex("(?<!\\[\\[)" + Regex.escape(p.name) + "(?!\\]\\])", RegexOption.IGNORE_CASE), "[[${p.name}]]")
    }
    return t
}

@Composable
private fun PlainIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String?, size: Int = 40, onClick: () -> Unit) {
    Box(Modifier.size(size.dp).clip(CircleShape).cleanClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, cd, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun DaySeparator(day: Long, today: Long) {
    val cs = MaterialTheme.colorScheme
    val label = when (day) {
        today -> stringResource(R.string.rooms_day_today)
        today - 1 -> stringResource(R.string.rooms_day_yesterday)
        else -> shortDate(day)
    }
    Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 6.dp), contentAlignment = Alignment.Center) {
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.clip(CircleShape).background(cs.surfaceContainerLow).padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

/** `.aviso`: las entregas y los cambios de fecha, centrados y sin burbuja. */
@Composable
private fun SystemNotice(room: WorkRoom, m: ChatMessage) {
    val cs = MaterialTheme.colorScheme
    val who = whoText(room, m.byId, stringResource(R.string.rooms_you))
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(who) }
        if (m.systemType == "DUE") {
            append(" " + LocalContext.current.getString(R.string.rooms_moved_final_to) + " ")
            withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(m.text.toLongOrNull()?.let { shortDate(it) } ?: m.text) }
        } else {
            append(" " + LocalContext.current.getString(R.string.rooms_delivered_verb) + " ")
            withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(m.text) }
        }
        append(" · " + hourOf(m.createdAt))
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
        Icon(if (m.systemType == "DUE") Icons.Rounded.CalendarMonth else Icons.Rounded.Check, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
        Text(text, fontSize = 11.5.sp, color = cs.onSurfaceVariant)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageBubble(room: WorkRoom, m: ChatMessage, cont: Boolean, selected: Boolean, mat: Color, vm: RoomsViewModel, query: String, onSelect: () -> Unit, onPart: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val mine = m.byId == room.meId
    val bg = if (mine) cs.primary else cs.surfaceContainer
    val fg = if (mine) cs.onPrimary else cs.onSurface
    val shape = when {
        mine && cont -> RoundedCornerShape(18.dp, 6.dp, 6.dp, 18.dp)
        mine -> RoundedCornerShape(18.dp, 18.dp, 6.dp, 18.dp)
        cont -> RoundedCornerShape(6.dp, 18.dp, 18.dp, 6.dp)
        else -> RoundedCornerShape(18.dp, 18.dp, 18.dp, 6.dp)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
        Row(Modifier.fillMaxWidth(0.86f).then(Modifier), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start, verticalAlignment = Alignment.Bottom) {
            if (!mine) Box(Modifier.padding(bottom = 2.dp, end = 8.dp).alpha(if (cont) 0f else 1f)) { MemberFace(room, m.byId, 24.dp) }
            Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                if (!mine && !cont) Text(room.nameOf(m.byId), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = room.member(m.byId)?.let { MemberColors.of(it.colorIndex) } ?: cs.primary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp))
                Column(Modifier.clip(shape).then(if (selected) Modifier.border(2.dp, cs.primary, shape) else Modifier).background(bg).cleanClickable(onClick = onSelect).padding(horizontal = 12.dp, vertical = 8.dp)) {
                    m.replyToId?.let { rid -> room.messages.firstOrNull { it.id == rid } }?.let { q ->
                        Row(Modifier.padding(bottom = 5.dp).clip(RoundedCornerShape(4.dp)).background(fg.copy(alpha = 0.08f)).drawBehind {
                            drawRect(fg, size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height))
                        }.padding(start = 11.dp, end = 8.dp, top = 3.dp, bottom = 3.dp)) {
                            Column {
                                Text(room.nameOf(q.byId), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = fg.copy(alpha = 0.8f))
                                Text(plainOf(q), fontSize = 12.sp, color = fg.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    when (m.kind) {
                        MessageKind.PHOTO -> Box(Modifier.padding(bottom = 4.dp).size(210.dp, 130.dp).clip(RoundedCornerShape(12.dp)).background(cs.surfaceContainerHigh).cleanClickable {
                            m.file?.let { context.openRoomFile(vm, it, m.mime, m.text) }
                        }) { if (m.file != null) AsyncImage(vm.files.file(m.file), m.text, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                        MessageKind.FILE, MessageKind.LINK -> Row(Modifier.padding(bottom = 4.dp).widthIn(min = 200.dp).clip(RoundedCornerShape(12.dp)).background(fg.copy(alpha = 0.08f)).cleanClickable {
                            if (m.kind == MessageKind.LINK) context.openUrl(m.text) else m.file?.let { context.openRoomFile(vm, it, m.mime, m.text) }
                        }.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Icon(if (m.kind == MessageKind.LINK) Icons.Rounded.Link else Icons.Rounded.Description, null, tint = fg, modifier = Modifier.size(22.dp))
                            Column {
                                Text(if (m.kind == MessageKind.LINK) m.subtitle.ifBlank { m.text } else m.text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(if (m.kind == MessageKind.LINK) m.text else m.subtitle, fontSize = 11.sp, color = fg.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                        MessageKind.VOICE -> VoiceMessage(m, vm, fg)
                        MessageKind.POLL -> PollMessage(room, m, vm, fg)
                        MessageKind.TEXT -> Text(richText(room, m.text, mine, mat, query), fontSize = 14.sp, lineHeight = 20.sp, color = fg,
                            modifier = Modifier.pointerInput(m.text) { detectTapGestures(onTap = { onSelect() }) })
                    }
                    Text(hourOf(m.createdAt), fontSize = 10.sp, color = fg.copy(alpha = 0.65f), modifier = Modifier.align(Alignment.End).padding(top = 2.dp))
                }
                val reacts = m.reactions.filterValues { it.isNotEmpty() }
                if (reacts.isNotEmpty()) Row(Modifier.offset(y = (-6).dp).padding(horizontal = 6.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                    reacts.forEach { (e, who) ->
                        val yo = room.meId in who
                        Row(Modifier.clip(CircleShape).background(cs.background).padding(1.5.dp).clip(CircleShape).background(if (yo) mix(cs.primary, 0.25f, cs.surfaceContainerHigh) else cs.surfaceContainerHigh)
                            .cleanClickable { vm.reactMessage(room.id, m.id, e) }.padding(horizontal = 6.dp, vertical = 1.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(e, fontSize = 11.5.sp)
                            if (who.size > 1) Text("${who.size}", fontSize = 11.5.sp, color = cs.onSurface)
                        }
                    }
                }
                if (m.kind == MessageKind.PHOTO || m.kind == MessageKind.FILE || m.kind == MessageKind.LINK) {
                    if (m.savedToMaterial) Text("✓ " + stringResource(R.string.rooms_in_material), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoomTone.VERDE.color, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    else Text(stringResource(R.string.rooms_save_material), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.primary,
                        modifier = Modifier.cleanClickable { vm.saveMessageToMaterial(room.id, m.id); context.roomToast(context.getString(R.string.rooms_saved_material)) }.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") onPart
}

/** @menciones en negrita y [[partes]] como pastilla del color de la materia. */
@Composable
private fun richText(room: WorkRoom, text: String, mine: Boolean, mat: Color, query: String): AnnotatedString {
    val cs = MaterialTheme.colorScheme
    val tokens = Regex("\\[\\[(.+?)]]|@(\\w+)").findAll(text).toList()
    return buildAnnotatedString {
        var last = 0
        tokens.forEach { t ->
            appendHighlighted(text.substring(last, t.range.first), query, cs.primary)
            if (t.groupValues[1].isNotEmpty()) withStyle(SpanStyle(fontWeight = FontWeight.Bold, background = if (mine) cs.onPrimary.copy(alpha = 0.18f) else mix(mat, 0.22f, Color.Transparent))) { append(" ▸ ${t.groupValues[1]} ") }
            else withStyle(SpanStyle(fontWeight = FontWeight.ExtraBold, color = if (mine) cs.onPrimary else cs.primary, textDecoration = if (mine) TextDecoration.Underline else null)) { append(t.value) }
            last = t.range.last + 1
        }
        appendHighlighted(text.substring(last), query, cs.primary)
    }
}

private fun AnnotatedString.Builder.appendHighlighted(s: String, q: String, c: Color) {
    if (q.isBlank()) { append(s); return }
    var i = 0
    while (true) {
        val j = s.indexOf(q, i, ignoreCase = true)
        if (j < 0) { append(s.substring(i)); return }
        append(s.substring(i, j))
        withStyle(SpanStyle(background = c.copy(alpha = 0.3f))) { append(s.substring(j, j + q.length)) }
        i = j + q.length
    }
}

@Composable
private fun VoiceMessage(m: ChatMessage, vm: RoomsViewModel, fg: Color) {
    var playing by remember { mutableStateOf(false) }
    val player = remember(m.id) { android.media.MediaPlayer() }
    DisposableEffect(m.id) { onDispose { runCatching { player.release() } } }
    Row(Modifier.widthIn(min = 190.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(30.dp).clip(CircleShape).background(fg.copy(alpha = 0.18f)).cleanClickable {
            val f = m.file ?: return@cleanClickable
            runCatching {
                if (playing) { player.pause(); playing = false }
                else {
                    if (player.currentPosition == 0) { player.reset(); player.setDataSource(vm.files.file(f).path); player.prepare() }
                    player.setOnCompletionListener { playing = false; it.seekTo(0) }
                    player.start(); playing = true
                }
            }
        }, contentAlignment = Alignment.Center) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = fg, modifier = Modifier.size(16.dp)) }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(4, 9, 14, 7, 12, 18, 10, 6, 13, 8, 15, 5, 11, 7).forEach { h -> Box(Modifier.width(3.dp).height(h.dp).clip(RoundedCornerShape(2.dp)).background(fg.copy(alpha = 0.6f))) }
        }
        Text("%d:%02d".format(m.seconds / 60, m.seconds % 60), fontSize = 11.sp, color = fg)
    }
}

@Composable
private fun PollMessage(room: WorkRoom, m: ChatMessage, vm: RoomsViewModel, fg: Color) {
    val total = m.pollOptions.sumOf { it.voters.size }
    Column(Modifier.widthIn(min = 230.dp)) {
        Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Rounded.Poll, null, tint = fg, modifier = Modifier.size(15.dp))
            Text(m.text, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = fg)
        }
        m.pollOptions.forEachIndexed { i, o ->
            val yo = room.meId in o.voters
            val frac = if (total > 0) o.voters.size.toFloat() / total else 0f
            Box(Modifier.padding(bottom = 5.dp).fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(fg.copy(alpha = 0.08f)).cleanClickable { vm.vote(room.id, m.id, i) }) {
                Box(Modifier.matchParentSize()) { Box(Modifier.fillMaxHeight().fillMaxWidth(frac).clip(RoundedCornerShape(10.dp)).background(fg.copy(alpha = 0.16f))) }
                Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text((if (yo) "✓ " else "") + o.label, fontSize = 13.sp, fontWeight = if (yo) FontWeight.ExtraBold else FontWeight.SemiBold, color = fg)
                    Text("${o.voters.size}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = fg)
                }
            }
        }
        Text(pluralText(R.plurals.rooms_n_votes, total) + " · " + stringResource(R.string.rooms_tap_to_vote), fontSize = 11.sp, color = fg.copy(alpha = 0.7f))
    }
}

/** El compositor: respuesta, sugerencias de @, campo con clip y el botón (micrófono o enviar). */
@Composable
private fun Composer(
    room: WorkRoom, vm: RoomsViewModel, input: String, onInput: (String) -> Unit, replyTo: ChatMessage?, onCancelReply: () -> Unit,
    onSend: () -> Unit, onAttach: () -> Unit, onVoice: (RoomStoredFile, Int) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var recording by remember { mutableStateOf<Pair<String, MediaRecorder>?>(null) }
    var startedAt by remember { mutableStateOf(0L) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val onVoiceNow by rememberUpdatedState(onVoice)
    val mention = Regex("@(\\w*)$").find(input)
    val sug = mention?.let { mm -> room.activeMembers.filter { it.id != room.meId && room.nameOf(it.id).startsWith(mm.groupValues[1], true) } }.orEmpty()
    Column(Modifier.fillMaxWidth().background(cs.background).drawBehind {
        drawLine(cs.outlineVariant.copy(alpha = 0.35f), Offset.Zero, Offset(size.width, 0f), 1.dp.toPx())
    }.navigationBarsPadding().padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 12.dp)) {
        if (replyTo != null) Row(Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.AutoMirrored.Rounded.Reply, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(buildAnnotatedString {
                append(stringResource(R.string.rooms_replying_to) + " ")
                withStyle(SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) { append(room.nameOf(replyTo.byId)) }
                append(" · " + plainOf(replyTo))
            }, fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).cleanClickable(onClick = onCancelReply)) { Icon(Icons.Rounded.Close, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
        }
        if (sug.isNotEmpty()) FlowRow(Modifier.padding(start = 2.dp, end = 2.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            sug.forEach { mem ->
                Row(Modifier.clip(CircleShape).background(cs.surfaceContainerLow).cleanClickable { onInput(input.replace(Regex("@\\w*$"), "@" + room.nameOf(mem.id).substringBefore(' ') + " ")) }
                    .padding(start = 5.dp, end = 10.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MemberFace(room, mem.id, 22.dp)
                    Text(room.nameOf(mem.id), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (recording != null) {
                val pulse by rememberInfiniteTransition(label = "rec").animateFloat(1f, 0.3f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "p")
                Row(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainer).padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(Modifier.size(10.dp).alpha(pulse).clip(CircleShape).background(RoomTone.ROJO.color))
                    Text(stringResource(R.string.rooms_recording), fontSize = 13.sp, color = cs.onSurface)
                }
            } else Row(Modifier.weight(1f).clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainer).padding(start = 14.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.weight(1f).padding(vertical = 11.dp)) {
                    if (input.isEmpty()) Text(stringResource(R.string.rooms_write_group), fontSize = 14.sp, color = cs.onSurfaceVariant)
                    BasicTextField(input, onInput, cursorBrush = SolidColor(cs.primary), maxLines = 5, textStyle = TextStyle(color = cs.onSurface, fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
                }
                Box(Modifier.size(34.dp).clip(CircleShape).cleanClickable(onClick = onAttach), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.AttachFile, stringResource(R.string.rooms_attach), tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                }
            }
            val hasText = input.isNotBlank()
            val holdHint = stringResource(R.string.rooms_hold_to_record)
            Box(Modifier.size(44.dp).clip(CircleShape).background(if (recording != null) RoomTone.ROJO.color else cs.primary).pointerInput(hasText) {
                detectTapGestures(
                    onTap = { if (hasText) onSend() else context.roomToast(holdHint) },
                    onPress = {
                        if (hasText) return@detectTapGestures
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            permission.launch(Manifest.permission.RECORD_AUDIO); return@detectTapGestures
                        }
                        val (name, file) = vm.files.newFileFor("m4a")
                        val rec = startRecorder(context, file)
                        if (rec == null) { vm.files.delete(name); return@detectTapGestures }
                        recording = name to rec; startedAt = System.currentTimeMillis()
                        val released = tryAwaitRelease()
                        val secs = ((System.currentTimeMillis() - startedAt) / 1000).toInt()
                        val ok = runCatching { rec.stop() }.isSuccess
                        rec.release(); recording = null
                        if (released && ok && secs >= 1) onVoiceNow(RoomStoredFile(name, context.getString(R.string.rooms_voice_note), "audio/mp4", file.length()), secs)
                        else vm.files.delete(name)
                    }
                )
            }, contentAlignment = Alignment.Center) {
                Icon(if (hasText) Icons.AutoMirrored.Rounded.Send else Icons.Rounded.Mic, null, tint = cs.onPrimary, modifier = Modifier.size(21.dp))
            }
        }
    }
}

private fun startRecorder(context: android.content.Context, file: File): MediaRecorder? = runCatching {
    val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context) else @Suppress("DEPRECATION") MediaRecorder()
    r.setAudioSource(MediaRecorder.AudioSource.MIC)
    r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
    r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
    r.setAudioEncodingBitRate(96_000)
    r.setAudioSamplingRate(44_100)
    r.setOutputFile(file.absolutePath)
    r.prepare(); r.start(); r
}.getOrNull()

/** «Mandar al chat»: foto, archivo, enlace, una parte, encuesta o nota de voz. */
@Composable
private fun AttachToChatSheet(room: WorkRoom, mat: Color, onDismiss: () -> Unit, onPhoto: () -> Unit, onFile: () -> Unit, onLink: () -> Unit, onPart: () -> Unit, onPoll: () -> Unit, onVoice: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val tiles = listOf(
        Triple(R.string.rooms_mt_photo, Icons.Rounded.Photo, RoomTone.ROSA.color) to onPhoto,
        Triple(R.string.rooms_file, Icons.Rounded.Description, RoomTone.INDIGO.color) to onFile,
        Triple(R.string.rooms_mt_link, Icons.Rounded.Link, RoomTone.AZUL.color) to onLink,
        Triple(R.string.rooms_a_part, Icons.Rounded.Segment, mat) to onPart,
        Triple(R.string.rooms_poll, Icons.Rounded.Poll, RoomTone.VERDE.color) to onPoll,
        Triple(R.string.rooms_voice_note, Icons.Rounded.Mic, RoomTone.NARANJA.color) to onVoice
    )
    RoomSheet(onDismiss, stringResource(R.string.rooms_send_to_chat)) {
        tiles.chunked(3).forEach { row ->
            Row(Modifier.padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { (t, action) ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).cleanClickable(onClick = action).padding(horizontal = 6.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        TypeIcon(t.second, t.third)
                        Text(stringResource(t.first), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    }
                }
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") room
}
