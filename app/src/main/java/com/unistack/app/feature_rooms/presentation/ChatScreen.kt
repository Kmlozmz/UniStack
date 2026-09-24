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
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntOffset
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.utils.performSafely
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
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
    val focusManager = LocalFocusManager.current
    var input by remember { mutableStateOf(if (prefill.isNotBlank()) "$prefill " else "") }
    var selected by remember { mutableStateOf<String?>(null) }
    var selectedBounds by remember { mutableStateOf<Rect?>(null) }
    var replyTo by remember { mutableStateOf<String?>(null) }
    var searching by remember { mutableStateOf(false) }
    var query by remember { mutableStateOf("") }
    var sheet by remember { mutableStateOf<String?>(null) }
    var menu by remember { mutableStateOf(false) }
    val composerFocus = remember { FocusRequester() }
    val subjects by vm.subjects.collectAsState()
    val mat = subjectColor(subjects.firstOrNull { it.id == room.subjectId })
    val list = rememberLazyListState()
    val msgs = room.messages.filter { m -> query.isBlank() || m.systemType != null || listOf(m.text, m.subtitle).any { it.contains(query, true) } || m.pollOptions.any { it.label.contains(query, true) } }

    /*
     * La lista va al revés, con lo último abajo y anclado al borde: cuando sube el teclado y la
     * pantalla se encoge, lo que se esconde es lo de arriba y el último mensaje sube con el
     * teclado, como en cualquier chat. Al derecho se quedaba debajo del teclado (23 sep).
     */
    LaunchedEffect(room.messages.size) {
        vm.markChatSeen(room.id)
        if (list.layoutInfo.totalItemsCount > 0) list.scrollToItem(0)
    }

    var editingId by remember { mutableStateOf<String?>(null) }
    var confirmDelete by remember { mutableStateOf<String?>(null) }
    val player = remember { VoicePlayer() }
    val recorder = remember { VoiceRecorder(context, vm.files) }
    var recMode by remember { mutableStateOf(RecMode.IDLE) }
    DisposableEffect(Unit) { onDispose { player.release(); if (recorder.active) recorder.cancel() } }
    LaunchedEffect(player.playingId, player.paused) { while (player.playingId != null && !player.paused) { player.tick(); delay(50) } }
    LaunchedEffect(recorder.active) { while (recorder.active) { recorder.sample(); delay(90) } }
    LaunchedEffect(recMode) { if (recMode != RecMode.IDLE) player.release() }
    val sendVoice: (VoiceRecorder.Result) -> Unit = { res -> vm.sendFile(room.id, MessageKind.VOICE, res.file, "", res.seconds, res.wave) }
    val micPermission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { ok -> if (ok && recorder.start()) recMode = RecMode.LOCKED }

    val sendText = {
        val t = input.trim()
        if (t.isNotEmpty()) {
            val e = editingId
            if (e != null) vm.editMessage(room.id, e, linkParts(room, t)) else vm.send(room.id, linkParts(room, t), replyTo)
            input = ""; replyTo = null; editingId = null
        }
    }
    val replyWith: (String) -> Unit = { id -> replyTo = id; selected = null; selectedBounds = null; runCatching { composerFocus.requestFocus() } }
    val sendFile: (MessageKind, RoomStoredFile) -> Unit = { k, f -> vm.sendFile(room.id, k, f, fileSubtitle(f.mimeType, f.sizeBytes)) }
    val picker = rememberRoomPicker(vm) { f -> sendFile(if (f.mimeType.startsWith("image/")) MessageKind.PHOTO else MessageKind.FILE, f) }

    Box(Modifier.fillMaxSize().background(cs.background)) {
    Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
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
            // Los tres puntos abren un menú que cuelga del botón, no una hoja (23 sep: «estamos abusando de los sheets»).
            Box {
                PlainIcon(Icons.Rounded.MoreVert, stringResource(R.string.rooms_more)) { menu = true }
                UniDropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(
                        text = { Text(stringResource(if (room.chatMuted) R.string.rooms_unmute_chat else R.string.rooms_mute_chat)) },
                        leadingIcon = { Icon(Icons.Rounded.NotificationsOff, null) },
                        onClick = {
                            menu = false
                            vm.update(room.id) { it.copy(chatMuted = !it.chatMuted) }
                            context.roomToast(context.getString(if (room.chatMuted) R.string.rooms_notifs_on else R.string.rooms_chat_muted_toast))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rooms_search_chat)) },
                        leadingIcon = { Icon(Icons.Rounded.Search, null) },
                        onClick = { menu = false; searching = true }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rooms_chat_media)) },
                        leadingIcon = { Icon(Icons.Rounded.Photo, null) },
                        onClick = { menu = false; sheet = "media" }
                    )
                }
            }
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
        if (room.chatMuted) Row(Modifier.fillMaxWidth().background(cs.surfaceContainerLow).padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.NotificationsOff, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.rooms_chat_muted), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
        }
        LazyColumn(
            Modifier.weight(1f), state = list, reverseLayout = true,
            contentPadding = PaddingValues(start = 14.dp, end = 14.dp, top = 6.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp, Alignment.Bottom)
        ) {
            if (msgs.isEmpty()) item {
                Text(stringResource(R.string.rooms_chat_empty), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(30.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            // Al revés: el índice 0 es el último mensaje, que queda abajo.
            items(msgs.size, key = { msgs[msgs.lastIndex - it].id }) { r ->
                val i = msgs.lastIndex - r
                val m = msgs[i]
                val prev = msgs.getOrNull(i - 1)
                val day = epochDayOf(m.createdAt)
                Column {
                    if (prev == null || epochDayOf(prev.createdAt) != day) DaySeparator(day, vm.today())
                    if (m.systemType != null) SystemNotice(room, m)
                    else {
                        val cont = prev != null && prev.systemType == null && prev.byId == m.byId && epochDayOf(prev.createdAt) == day
                        MessageBubble(room, m, cont, m.id == selected, mat, vm, query, player,
                            onSelect = {
                                selectedBounds = null
                                selected = if (selected == m.id) null else m.id
                                focusManager.clearFocus()
                            },
                            onReply = { replyWith(m.id) },
                            onBounds = { selectedBounds = it },
                            onPart = { pid -> go("parte", pid) })
                    }
                }
            }
        }
        Composer(room, input, { input = it }, replyTo?.let { id -> room.messages.firstOrNull { it.id == id } }, onCancelReply = { replyTo = null },
            editing = editingId?.let { id -> room.messages.firstOrNull { it.id == id } }, onCancelEdit = { editingId = null; input = "" },
            onSend = sendText, onAttach = { sheet = "attach" }, onCamera = { picker.takePhoto() }, recorder = recorder, recMode = recMode, onRecMode = { recMode = it }, onVoice = sendVoice, focus = composerFocus)
    }
    selected?.let { id -> room.messages.firstOrNull { it.id == id } }?.let { m ->
        selectedBounds?.let { b ->
            MessageMenu(room, m, b,
                onReact = { e -> vm.reactMessage(room.id, m.id, e); selected = null; selectedBounds = null },
                onReply = { replyWith(m.id) },
                onCopy = { context.copyText(context.getString(R.string.rooms_group_chat), m.text.replace("[[", "").replace("]]", "")); context.roomToast(context.getString(R.string.rooms_copied)); selected = null },
                onPin = {
                    val pinning = room.pinnedMessageId != m.id
                    vm.pin(room.id, if (pinning) m.id else null); selected = null
                    if (pinning) context.roomToast(context.getString(R.string.rooms_pinned_for_all))
                },
                onSave = { vm.saveMessageToMaterial(room.id, m.id); selected = null; context.roomToast(context.getString(R.string.rooms_saved_material)) },
                onEdit = if (m.byId == room.meId && m.kind == MessageKind.TEXT) { {
                    editingId = m.id; replyTo = null; input = m.text.replace("[[", "").replace("]]", ""); selected = null; selectedBounds = null
                    runCatching { composerFocus.requestFocus() }
                } } else null,
                onDelete = if (m.byId == room.meId) { { confirmDelete = m.id; selected = null; selectedBounds = null } } else null,
                onDismiss = { selected = null; selectedBounds = null })
        }
    }
    confirmDelete?.let { id ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null }, modifier = Modifier.popIn(), containerColor = cs.background, shape = RoundedCornerShape(28.dp),
            title = { Text(stringResource(R.string.rooms_delete_msg_q), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold) },
            text = { Text(stringResource(R.string.rooms_delete_msg_d), fontSize = 13.5.sp, color = cs.onSurfaceVariant) },
            confirmButton = { Pill(stringResource(R.string.rooms_delete), { vm.deleteMessage(room.id, id); if (editingId == id) { editingId = null; input = "" }; confirmDelete = null }, style = PillStyle.ERR) },
            dismissButton = { Pill(stringResource(R.string.rooms_cancel), { confirmDelete = null }, style = PillStyle.TONAL) }
        )
    }
    }

    when (sheet) {
        "attach" -> AttachToChatSheet(room, mat, onDismiss = { sheet = null }, onPhoto = { sheet = null; picker.pickImage() }, onFile = { sheet = null; picker.pickFile(ANY_TYPES) },
            onLink = { sheet = "link" }, onPart = { sheet = "part" }, onPoll = { sheet = "poll" }, onVoice = {
                // la nota desde «Mandar al chat» se queda grabando sola, con su panel
                sheet = null
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) micPermission.launch(Manifest.permission.RECORD_AUDIO)
                else if (recorder.start()) recMode = RecMode.LOCKED
            })
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
        "media" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_chat_media), tall = true) {
            val media = room.messages.filter { !it.deleted }.filter { it.kind == MessageKind.PHOTO || it.kind == MessageKind.FILE || it.kind == MessageKind.LINK || it.kind == MessageKind.VOICE }.reversed()
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
fun plainOf(m: ChatMessage): String = when {
    m.deleted -> stringResource(R.string.rooms_msg_deleted_other)
    m.kind == MessageKind.VOICE -> stringResource(R.string.rooms_voice_note)
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

/**
 * Un mensaje. Se arrastra hacia la derecha para responderlo, como en WhatsApp: asoma la flecha,
 * al pasar el umbral vibra y al soltar vuelve a su sitio con el mensaje ya citado abajo.
 *
 * La cara va pegada al pie de la burbuja, no al de las reacciones: con reacciones la burbuja
 * quedaba más alta que la cara y las demás no (lo notó el 23 sep). Las reacciones montan medio
 * cuerpo sobre el borde de la burbuja y no empujan al mensaje siguiente.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MessageBubble(
    room: WorkRoom, m: ChatMessage, cont: Boolean, selected: Boolean, mat: Color, vm: RoomsViewModel, query: String, player: VoicePlayer,
    onSelect: () -> Unit, onReply: () -> Unit, onBounds: (Rect) -> Unit, onPart: (String) -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val mine = m.byId == room.meId
    val bg = if (mine) cs.primary else cs.surfaceContainer
    val fg = if (mine) cs.onPrimary else cs.onSurface
    /*
     * Como WhatsApp (captura del 24 sep): esquinas de 10 y, en el primero de una tanda, la esquina de
     * arriba en punta con su colita hacia fuera; el nombre va dentro y la cara arriba a la izquierda.
     */
    val r10 = 10.dp
    val shape = when {
        cont -> RoundedCornerShape(r10)
        mine -> RoundedCornerShape(r10, 0.dp, r10, r10)
        else -> RoundedCornerShape(0.dp, r10, r10, r10)
    }
    val time = (if (m.edited && !m.deleted) stringResource(R.string.rooms_edited) + " " else "") + hourOf(m.createdAt)
    var dx by remember { mutableFloatStateOf(0f) }
    var armed by remember { mutableStateOf(false) }
    var settle by remember { mutableStateOf<Job?>(null) }
    val face = 38.dp
    val triggerPx = with(androidx.compose.ui.platform.LocalDensity.current) { 56.dp.toPx() }
    Box(Modifier.fillMaxWidth().pointerInput(m.id) {
        val trigger = 56.dp.toPx()
        val limit = 84.dp.toPx()
        val release = {
            if (dx >= trigger && !m.deleted) onReply()
            armed = false
            settle = scope.launch { animate(dx, 0f, animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow)) { v, _ -> dx = v } }
        }
        detectHorizontalDragGestures(
            onDragStart = { settle?.cancel() },
            onDragEnd = { release() },
            onDragCancel = { release() },
            onHorizontalDrag = { change, amount ->
                // Pasado el umbral cuesta más estirarlo: se nota que ya «enganchó».
                val next = (dx + amount * (if (dx > trigger) 0.3f else 1f)).coerceIn(0f, limit)
                if (next != dx) change.consume()
                dx = next
                val now = dx >= trigger
                if (now && !armed) haptic.performSafely(HapticFeedbackType.GestureThresholdActivate)
                armed = now
            }
        )
    }) {
        val p = (dx / triggerPx).coerceIn(0f, 1f)
        Box(
            Modifier.align(Alignment.CenterStart).padding(start = 2.dp).size(30.dp)
                .graphicsLayer { alpha = p; scaleX = 0.5f + 0.5f * p; scaleY = 0.5f + 0.5f * p }
                .clip(CircleShape).background(if (armed) mix(cs.primary, 0.22f, cs.surfaceContainerHigh) else cs.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) { Icon(Icons.AutoMirrored.Rounded.Reply, null, tint = if (armed) cs.primary else cs.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
        Row(Modifier.fillMaxWidth().offset { IntOffset(dx.roundToInt(), 0) }, horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
            Column(Modifier.fillMaxWidth(0.86f), horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
                Row(verticalAlignment = Alignment.Top) {
                    if (!mine) Box(Modifier.padding(end = 8.dp).size(30.dp)) { if (!cont) MemberFace(room, m.byId, 30.dp) }
                    Column(
                        Modifier.onGloballyPositioned { if (selected) onBounds(it.boundsInWindow()) }
                            .drawBehind {
                                if (!cont) {
                                    val w = 8.dp.toPx(); val h = 11.dp.toPx()
                                    val tail = androidx.compose.ui.graphics.Path().apply {
                                        if (mine) { moveTo(size.width - 1f, 0f); lineTo(size.width + w, 0f); lineTo(size.width - 1f, h) }
                                        else { moveTo(1f, 0f); lineTo(-w, 0f); lineTo(1f, h) }
                                        close()
                                    }
                                    drawPath(tail, bg)
                                }
                            }
                            .clip(shape).background(bg)
                            // sólo mantener pulsado abre las opciones, como en WhatsApp (las de lo tuyo incluyen editar y borrar)
                            .pointerInput(m.id, m.deleted) { detectTapGestures(onLongPress = { if (!m.deleted) onSelect() }) }
                            .padding(start = 10.dp, end = 10.dp, top = 6.dp, bottom = 6.dp)
                    ) {
                        if (!mine && !cont) Text(room.nameOf(m.byId), fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, color = room.member(m.byId)?.let { MemberColors.of(it.colorIndex) } ?: cs.primary,
                            modifier = Modifier.padding(bottom = 2.dp))
                        m.replyToId?.let { rid -> room.messages.firstOrNull { it.id == rid } }?.let { q ->
                            Row(Modifier.padding(top = 2.dp, bottom = 5.dp).clip(RoundedCornerShape(6.dp)).background(fg.copy(alpha = 0.08f)).drawBehind {
                                drawRect(fg, size = androidx.compose.ui.geometry.Size(3.dp.toPx(), size.height))
                            }.padding(start = 11.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)) {
                                Column {
                                    Text(room.nameOf(q.byId), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg.copy(alpha = 0.85f))
                                    Text(plainOf(q), fontSize = 12.5.sp, color = fg.copy(alpha = 0.8f), maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        val timeStyle = SpanStyle(fontSize = 11.sp, color = Color.Transparent)
                        when {
                            m.deleted -> Box {
                                Text(buildAnnotatedString {
                                    append(stringResource(if (mine) R.string.rooms_msg_deleted_mine else R.string.rooms_msg_deleted_other))
                                    withStyle(timeStyle) { append("\u2003\u2002" + time) }
                                }, fontSize = 14.sp, fontStyle = FontStyle.Italic, color = fg.copy(alpha = 0.7f), modifier = Modifier.padding(start = 20.dp))
                                Icon(Icons.Rounded.Block, null, tint = fg.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.TopStart).padding(top = 2.dp).size(15.dp))
                                Text(time, fontSize = 11.sp, color = fg.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomEnd))
                            }
                            m.kind == MessageKind.TEXT -> Box {
                                // la hora va en la última línea, como WhatsApp: el texto le guarda sitio con una copia invisible
                                Text(buildAnnotatedString {
                                    append(richText(room, m.text, mine, mat, query))
                                    withStyle(timeStyle) { append("\u2003\u2002" + time) }
                                }, fontSize = 14.5.sp, lineHeight = 20.sp, color = fg,
                                    modifier = Modifier.pointerInput(m.text) { detectTapGestures(onLongPress = { onSelect() }) })
                                Text(time, fontSize = 11.sp, color = fg.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.BottomEnd))
                            }
                            m.kind == MessageKind.VOICE -> VoiceBubble(room, m, mine, fg, player, m.file?.let { vm.files.file(it).path }, time)
                            else -> {
                                when (m.kind) {
                                    MessageKind.PHOTO -> Box(Modifier.padding(top = 2.dp, bottom = 4.dp).size(220.dp, 140.dp).clip(RoundedCornerShape(8.dp)).background(cs.surfaceContainerHigh).combinedClickable(interactionSource = null, indication = null, onLongClick = onSelect) {
                                        m.file?.let { context.openRoomFile(vm, it, m.mime, m.text) }
                                    }) { if (m.file != null) AsyncImage(vm.files.file(m.file), m.text, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize()) }
                                    MessageKind.POLL -> PollMessage(room, m, vm, fg)
                                    else -> Row(Modifier.padding(top = 2.dp, bottom = 4.dp).widthIn(min = 210.dp).clip(RoundedCornerShape(8.dp)).background(fg.copy(alpha = 0.08f)).combinedClickable(interactionSource = null, indication = null, onLongClick = onSelect) {
                                        if (m.kind == MessageKind.LINK) context.openUrl(m.text) else m.file?.let { context.openRoomFile(vm, it, m.mime, m.text) }
                                    }.padding(horizontal = 10.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        Icon(if (m.kind == MessageKind.LINK) Icons.Rounded.Link else Icons.Rounded.Description, null, tint = fg, modifier = Modifier.size(22.dp))
                                        Column {
                                            Text(if (m.kind == MessageKind.LINK) m.subtitle.ifBlank { m.text } else m.text, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = fg, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                            Text(if (m.kind == MessageKind.LINK) m.text else m.subtitle, fontSize = 11.sp, color = fg.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                    }
                                }
                                Text(time, fontSize = 11.sp, color = fg.copy(alpha = 0.6f), modifier = Modifier.align(Alignment.End))
                            }
                        }
                    }
                }
                val reacts = m.reactions.filterValues { it.isNotEmpty() }
                if (reacts.isNotEmpty() && !m.deleted) Row(
                    Modifier.padding(start = if (mine) 0.dp else face + 8.dp, end = if (mine) 8.dp else 0.dp).layout { measurable, c ->
                        val pl = measurable.measure(c)
                        val up = 7.dp.roundToPx()
                        layout(pl.width, (pl.height - up).coerceAtLeast(0)) { pl.place(0, -up) }
                    },
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    reacts.forEach { (e, who) ->
                        val yo = room.meId in who
                        Row(Modifier.clip(CircleShape).background(cs.background).padding(1.5.dp).clip(CircleShape).background(if (yo) mix(cs.primary, 0.25f, cs.surfaceContainerHigh) else cs.surfaceContainerHigh)
                            .cleanClickable { vm.reactMessage(room.id, m.id, e) }.padding(horizontal = 6.dp, vertical = 1.dp), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(e, fontSize = 11.5.sp)
                            if (who.size > 1) Text("${who.size}", fontSize = 11.5.sp, color = cs.onSurface)
                        }
                    }
                }
                if (!m.deleted && (m.kind == MessageKind.PHOTO || m.kind == MessageKind.FILE || m.kind == MessageKind.LINK)) {
                    val pad = Modifier.padding(start = if (mine) 6.dp else face + 6.dp, end = 6.dp, top = 2.dp, bottom = 2.dp)
                    if (m.savedToMaterial) Text("✓ " + stringResource(R.string.rooms_in_material), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = RoomTone.VERDE.color, modifier = pad)
                    else Text(stringResource(R.string.rooms_save_material), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.primary,
                        modifier = Modifier.cleanClickable { vm.saveMessageToMaterial(room.id, m.id); context.roomToast(context.getString(R.string.rooms_saved_material)) }.then(pad))
                }
            }
        }
    }
    @Suppress("UNUSED_EXPRESSION") onPart
}

/**
 * Lo que sale al tocar un mensaje, ahí mismo y flotando, como en Telegram o WhatsApp: el resto
 * del chat se apaga menos el mensaje, encima brotan las reacciones rápidas y debajo el menú
 * (responder, copiar, destacar, guardar en material). Si abajo no cabe, todo va encima.
 * Sustituye a la barra que se abría arriba del chat (23 sep).
 */
@Composable
private fun MessageMenu(
    room: WorkRoom, m: ChatMessage, bubble: Rect,
    onReact: (String) -> Unit, onReply: () -> Unit, onCopy: () -> Unit, onPin: () -> Unit, onSave: () -> Unit,
    onEdit: (() -> Unit)?, onDelete: (() -> Unit)?, onDismiss: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val mine = m.byId == room.meId
    var origin by remember { mutableStateOf(Offset.Zero) }
    val shown = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        haptic.performSafely(HapticFeedbackType.LongPress)
        shown.animateTo(1f, spring(dampingRatio = 0.62f, stiffness = Spring.StiffnessMediumLow))
    }
    val topInset = WindowInsets.statusBars.getTop(androidx.compose.ui.platform.LocalDensity.current)
    val bottomInset = WindowInsets.navigationBars.getBottom(androidx.compose.ui.platform.LocalDensity.current)
    val hole = bubble.translate(-origin)
    var menuAbove by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInWindow() }) {
        Canvas(Modifier.fillMaxSize().pointerInput(Unit) { detectTapGestures { onDismiss() } }) {
            val r = 20.dp.toPx()
            val path = Path().apply {
                fillType = PathFillType.EvenOdd
                addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size))
                addRoundRect(RoundRect(hole.inflate(3.dp.toPx()), CornerRadius(r, r)))
            }
            drawPath(path, Color.Black.copy(alpha = 0.55f * shown.value.coerceIn(0f, 1f)))
        }
        Layout(content = {
            // las reacciones rápidas, en una pastilla
            Row(
                Modifier.graphicsLayer {
                    val v = shown.value
                    alpha = v.coerceIn(0f, 1f); scaleX = 0.6f + 0.4f * v; scaleY = 0.6f + 0.4f * v
                    transformOrigin = TransformOrigin(if (mine) 1f else 0f, 1f)
                }.shadow(10.dp, CircleShape).clip(CircleShape).background(cs.surfaceContainerHigh).padding(horizontal = 6.dp, vertical = 5.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp), verticalAlignment = Alignment.CenterVertically
            ) {
                CHAT_REACTIONS.forEachIndexed { i, e ->
                    val pop = remember { Animatable(0f) }
                    LaunchedEffect(Unit) { delay(40L * i); pop.animateTo(1f, spring(dampingRatio = 0.45f, stiffness = Spring.StiffnessMedium)) }
                    val yo = room.meId in m.reactions[e].orEmpty()
                    Text(e, fontSize = 24.sp, lineHeight = 1.15.em, modifier = Modifier
                        .graphicsLayer { scaleX = pop.value; scaleY = pop.value }
                        .clip(CircleShape).background(if (yo) mix(cs.primary, 0.28f, cs.surfaceContainerHigh) else Color.Transparent)
                        .cleanClickable { onReact(e) }.padding(horizontal = 6.dp, vertical = 4.dp))
                }
            }
            // el menú
            Column(
                Modifier.width(220.dp).graphicsLayer {
                    val v = shown.value
                    alpha = v.coerceIn(0f, 1f); scaleX = 0.85f + 0.15f * v; scaleY = 0.85f + 0.15f * v
                    transformOrigin = TransformOrigin(if (mine) 1f else 0f, if (menuAbove) 1f else 0f)
                }.shadow(10.dp, RoundedCornerShape(18.dp)).clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerHigh).padding(vertical = 6.dp)
            ) {
                MenuRow(Icons.AutoMirrored.Rounded.Reply, stringResource(R.string.rooms_reply), onReply)
                if (m.kind == MessageKind.TEXT || m.kind == MessageKind.LINK || m.kind == MessageKind.POLL) MenuRow(Icons.Rounded.ContentCopy, stringResource(R.string.rooms_copy), onCopy)
                if (onEdit != null) MenuRow(Icons.Rounded.Edit, stringResource(R.string.rooms_edit), onEdit)
                MenuRow(Icons.Rounded.PushPin, stringResource(if (room.pinnedMessageId == m.id) R.string.rooms_unpin else R.string.rooms_pin), onPin)
                if ((m.kind == MessageKind.PHOTO || m.kind == MessageKind.FILE || m.kind == MessageKind.LINK) && !m.savedToMaterial)
                    MenuRow(Icons.Rounded.BookmarkAdd, stringResource(R.string.rooms_save_material), onSave)
                if (onDelete != null) MenuRow(Icons.Rounded.Delete, stringResource(R.string.rooms_delete), onDelete, danger = true)
            }
        }) { measurables, constraints ->
            val loose = constraints.copy(minWidth = 0, minHeight = 0)
            val pill = measurables[0].measure(loose)
            val box = measurables[1].measure(loose)
            val w = constraints.maxWidth; val h = constraints.maxHeight
            val gap = 8.dp.roundToPx(); val margin = 12.dp.roundToPx()
            val top = topInset + margin; val bottom = h - bottomInset - margin
            fun x(width: Int) = (if (mine) hole.right.roundToInt() - width else hole.left.roundToInt()).coerceIn(margin, (w - margin - width).coerceAtLeast(margin))
            val below = hole.bottom.roundToInt() + gap + box.height <= bottom
            menuAbove = !below
            val boxY: Int; val pillY: Int
            if (below) {
                boxY = hole.bottom.roundToInt() + gap
                pillY = (hole.top.roundToInt() - gap - pill.height).coerceAtLeast(top)
            } else {
                boxY = (hole.top.roundToInt() - gap - box.height).coerceAtLeast(top + pill.height + gap)
                pillY = (boxY - gap - pill.height).coerceAtLeast(top)
            }
            layout(w, h) {
                pill.place(x(pill.width), pillY)
                box.place(x(box.width), boxY)
            }
        }
    }
}

@Composable
private fun MenuRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, onClick: () -> Unit, danger: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().cleanClickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = if (danger) cs.error else cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(text, fontSize = 14.5.sp, fontWeight = FontWeight.Medium, color = if (danger) cs.error else cs.onSurface, maxLines = 1)
    }
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

/**
 * El compositor, como la barra de WhatsApp (captura del 24 sep): una pastilla de 48 con la
 * pegatina a la izquierda, el campo, el clip y la cámara (que se va al escribir), y aparte el botón
 * redondo gris claro. Sin texto el botón es el micrófono: mantener graba (a la izquierda cancela,
 * hacia arriba se queda grabando sola), un toque también la deja grabando sola, y entonces el
 * compositor es el panel de WhatsApp con el tiempo, la onda, borrar, pausa y enviar.
 * La pegatina cambia el teclado por el panel de emojis, que mide lo mismo que el teclado: mientras
 * uno baja el otro sube, así el chat no salta.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Composer(
    room: WorkRoom, input: String, onInput: (String) -> Unit, replyTo: ChatMessage?, onCancelReply: () -> Unit,
    editing: ChatMessage?, onCancelEdit: () -> Unit,
    onSend: () -> Unit, onAttach: () -> Unit, onCamera: () -> Unit, recorder: VoiceRecorder, recMode: RecMode, onRecMode: (RecMode) -> Unit,
    onVoice: (VoiceRecorder.Result) -> Unit, focus: FocusRequester
) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    val mode by rememberUpdatedState(recMode)
    val setMode by rememberUpdatedState(onRecMode)
    val sendVoice by rememberUpdatedState(onVoice)
    val sendNow by rememberUpdatedState(onSend)
    val label = stringResource(R.string.rooms_voice_note)
    var drag by remember { mutableStateOf(Offset.Zero) }
    val cancelPx = with(density) { 110.dp.toPx() }
    val lockPx = with(density) { 90.dp.toPx() }
    val grow by animateFloatAsState(if (recMode == RecMode.HOLD) 1.5f else 1f, spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessMediumLow), label = "micro")
    val mention = Regex("@(" + "[" + "a-zA-Z0-9_" + "]*)$").find(input)
    val sug = mention?.let { mm -> room.activeMembers.filter { it.id != room.meId && room.nameOf(it.id).startsWith(mm.groupValues[1], true) } }.orEmpty()
    val keyboard = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var emoji by remember { mutableStateOf(false) }
    var keyboardPx by rememberSaveable { mutableIntStateOf(0) }
    val imeNow = WindowInsets.ime.getBottom(density)
    val imeTarget = WindowInsets.imeAnimationTarget.getBottom(density)
    val navNow = WindowInsets.navigationBars.getBottom(density)
    // con el teclado ya arriba del todo se aprende su alto, y si el panel seguía abierto se va
    LaunchedEffect(imeNow, imeTarget) {
        if (imeTarget > 0 && imeNow == imeTarget) { keyboardPx = (imeNow - navNow).coerceAtLeast(0); emoji = false }
    }
    LaunchedEffect(recMode) { if (recMode != RecMode.IDLE) emoji = false }
    BackHandler(emoji) { emoji = false }
    val panelPx = if (!emoji) 0 else ((if (keyboardPx > 0) keyboardPx else with(density) { 290.dp.roundToPx() }) - (imeNow - navNow).coerceAtLeast(0)).coerceAtLeast(0)
    val toggleEmoji: () -> Unit = {
        if (emoji) {
            runCatching { focus.requestFocus() }
            keyboard?.show()
            scope.launch { delay(700); emoji = false }
        } else { emoji = true; keyboard?.hide() }
    }
    if (recMode == RecMode.LOCKED) {
        Box(Modifier.fillMaxWidth().background(cs.background).navigationBarsPadding()) {
            LockedRecordingPanel(recorder,
                onDiscard = { recorder.cancel(); onRecMode(RecMode.IDLE); haptic.performSafely(HapticFeedbackType.Reject) },
                onSend = { recorder.stop(label)?.let(onVoice); onRecMode(RecMode.IDLE) })
        }
        return
    }
    Column(Modifier.fillMaxWidth().background(cs.background).navigationBarsPadding()) {
    Column(Modifier.padding(start = 8.dp, end = 8.dp, top = 5.dp, bottom = 6.dp)) {
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
        if (editing != null) Row(Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.Edit, null, tint = cs.primary, modifier = Modifier.size(16.dp))
            Text(buildAnnotatedString {
                withStyle(SpanStyle(color = cs.primary, fontWeight = FontWeight.Bold)) { append(stringResource(R.string.rooms_editing)) }
                append(" · " + plainOf(editing))
            }, fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
            Box(Modifier.clip(CircleShape).cleanClickable(onClick = onCancelEdit)) { Icon(Icons.Rounded.Close, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp)) }
        }
        if (sug.isNotEmpty()) FlowRow(Modifier.padding(start = 2.dp, end = 2.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            sug.forEach { mem ->
                Row(Modifier.clip(CircleShape).background(cs.surfaceContainerLow).cleanClickable { onInput(input.substring(0, mention!!.range.first) + "@" + room.nameOf(mem.id).substringBefore(' ') + " ") }
                    .padding(start = 5.dp, end = 10.dp, top = 5.dp, bottom = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    MemberFace(room, mem.id, 22.dp)
                    Text(room.nameOf(mem.id), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                }
            }
        }
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (recMode == RecMode.HOLD) HoldRecordingBar(recorder, drag.x, cancelPx, Modifier.weight(1f).heightIn(min = 48.dp))
            else Row(Modifier.weight(1f).heightIn(min = 48.dp).clip(RoundedCornerShape(24.dp)).background(cs.surfaceContainer), verticalAlignment = Alignment.Bottom) {
                PillIcon(if (emoji) Icons.Outlined.Keyboard else StickerSmile, stringResource(if (emoji) R.string.rooms_keyboard else R.string.rooms_emoji), onClick = toggleEmoji)
                Box(Modifier.weight(1f).padding(vertical = 13.dp)) {
                    if (input.isEmpty()) Text(stringResource(R.string.rooms_message_hint), fontSize = 17.sp, lineHeight = 22.sp, color = cs.onSurfaceVariant, maxLines = 1)
                    BasicTextField(input, onInput, cursorBrush = SolidColor(cs.onSurface), maxLines = 6,
                        textStyle = TextStyle(color = cs.onSurface, fontSize = 17.sp, lineHeight = 22.sp), modifier = Modifier.fillMaxWidth().focusRequester(focus))
                }
                PillIcon(Icons.Rounded.AttachFile, stringResource(R.string.rooms_attach), onClick = onAttach)
                // la cámara se aparta en cuanto hay texto, como en WhatsApp
                AnimatedVisibility(input.isEmpty(), enter = fadeIn() + expandHorizontally(), exit = fadeOut() + shrinkHorizontally()) {
                    PillIcon(Icons.Outlined.PhotoCamera, stringResource(R.string.rooms_camera), onClick = onCamera)
                }
            }
            val hasText = input.isNotBlank()
            Box(Modifier.size(48.dp)) {
                // el candado encima mientras se mantiene: subir hasta él deja la nota grabando sola
                if (recMode == RecMode.HOLD) Column(
                    Modifier.align(Alignment.BottomCenter).offset { IntOffset(0, (-(68.dp.toPx()) + drag.y.coerceIn(-lockPx, 0f) / 3).roundToInt()) }
                        .width(44.dp).clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainer).padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Rounded.Lock, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    Icon(Icons.Rounded.KeyboardArrowUp, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
                Box(
                    Modifier.matchParentSize().pointerInput(hasText) {
                        if (hasText) detectTapGestures(onTap = { sendNow() })
                        else awaitEachGesture {
                            val down = awaitFirstDown()
                            if (mode != RecMode.IDLE) return@awaitEachGesture
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                                permission.launch(Manifest.permission.RECORD_AUDIO); return@awaitEachGesture
                            }
                            if (!recorder.start()) return@awaitEachGesture
                            haptic.performSafely(HapticFeedbackType.LongPress)
                            setMode(RecMode.HOLD)
                            var outcome = 0 // 0 enviar, 1 cancelar, 2 dejarla grabando sola
                            while (true) {
                                val c = awaitPointerEvent().changes.firstOrNull { it.id == down.id } ?: break
                                if (!c.pressed) {
                                    val quick = c.uptimeMillis - down.uptimeMillis < 350 && drag.getDistance() < 24.dp.toPx()
                                    outcome = if (quick) 2 else 0
                                    break
                                }
                                drag = c.position - down.position
                                c.consume()
                                if (drag.x < -cancelPx) { outcome = 1; break }
                                if (drag.y < -lockPx) { outcome = 2; break }
                            }
                            drag = Offset.Zero
                            when (outcome) {
                                1 -> { recorder.cancel(); setMode(RecMode.IDLE); haptic.performSafely(HapticFeedbackType.Reject) }
                                2 -> { setMode(RecMode.LOCKED); haptic.performSafely(HapticFeedbackType.GestureThresholdActivate) }
                                else -> { recorder.stop(label)?.let { sendVoice(it) }; setMode(RecMode.IDLE) }
                            }
                        }
                    }.graphicsLayer {
                        scaleX = grow; scaleY = grow
                        translationX = drag.x.coerceIn(-cancelPx, 0f); translationY = drag.y.coerceIn(-lockPx, 0f)
                    }.clip(CircleShape).background(mix(cs.onSurface, 0.8f, cs.background)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(if (hasText) (if (editing != null) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.Send) else Icons.Rounded.Mic, null, tint = cs.background, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
        if (panelPx > 0) EmojiPanel(with(density) { panelPx.toDp() }, onPick = { onInput(input + it) }, onBackspace = { onInput(input.dropLastGrapheme()) })
    }
}

/** Un icono de la pastilla: 24 dp en gris dentro de un toque de 48, como en la barra de WhatsApp. */
@Composable
private fun PillIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, cd: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clip(CircleShape).cleanClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, cd, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
    }
}

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
