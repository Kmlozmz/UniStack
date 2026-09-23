package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.EventType
import com.unistack.app.feature_rooms.domain.RoomEvent
import com.unistack.app.feature_rooms.domain.WorkRoom

/** Lo que es para mí: me mencionaron, comentaron lo mío, me asignaron o me etiquetaron. */
fun isForMe(room: WorkRoom, e: RoomEvent): Boolean = e.actorId != room.meId && (
    e.forMe || when (e.type) {
        EventType.MENTION -> room.nameOf(room.meId).substringBefore(' ').let { n -> n.isNotBlank() && e.text.contains("@$n", true) }
        EventType.COMMENT, EventType.COMMENT_ON_MINE, EventType.REVIEW_ASKED -> room.part(e.targetPartId)?.ownerIds?.contains(room.meId) == true
        EventType.ASSIGNED -> room.part(e.targetPartId)?.ownerIds?.contains(room.meId) == true
        EventType.TAGGED -> room.materials.any { it.name == e.text && room.meId in it.tagged }
        else -> false
    })

private fun groupOf(e: RoomEvent) = when (e.type) {
    EventType.DELIVERED -> "deliv"
    EventType.DUE_CHANGED, EventType.ASSIGNED -> "changes"
    else -> "other"
}

private fun iconOf(t: EventType): Pair<ImageVector, Int> = when (t) {
    EventType.MENTION -> Icons.Rounded.AlternateEmail to 0
    EventType.COMMENT_ON_MINE, EventType.REVIEW_ASKED -> Icons.AutoMirrored.Rounded.Comment to 1
    EventType.DUE_CHANGED -> Icons.Rounded.CalendarMonth to 2
    EventType.MATERIAL, EventType.TAGGED -> Icons.Rounded.Visibility to 3
    EventType.DELIVERED -> Icons.Rounded.Check to 4
    EventType.COMMENT -> Icons.AutoMirrored.Rounded.Comment to 5
    EventType.JOINED -> Icons.AutoMirrored.Rounded.Login to 6
    EventType.ASSIGNED -> Icons.Rounded.SwapHoriz to 2
}

/**
 * Novedades — misma opción A del chat: su propia pantalla. Arriba «Mientras no estabas», los
 * filtros (Todo · Para mí · Entregas · Cambios), lo que es para ti y el resto por días.
 */
@Composable
fun NewsScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val today = vm.today()
    var filter by rememberSaveable { mutableStateOf("all") }
    val seenAt = remember { room.lastSeenEventsAt }
    DisposableEffect(Unit) { onDispose { vm.markEventsSeen(room.id) } }
    val events = room.events.sortedByDescending { it.createdAt }
    val unseen = events.filter { it.createdAt > room.lastSeenEventsAt && it.actorId != room.meId }
    val pass: (RoomEvent) -> Boolean = { e ->
        when (filter) { "all" -> true; "mine" -> isForMe(room, e); else -> groupOf(e) == filter }
    }
    val forMe = events.filter { isForMe(room, it) && pass(it) }
    val rest = events.filter { !isForMe(room, it) && pass(it) }
    val tones = listOf(cs.primary, RoomTone.AZUL.color, RoomTone.AMBAR.color, RoomTone.ROSA.color, RoomTone.VERDE.color, RoomTone.GRIS.color, RoomTone.CIAN.color)

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding()) {
        Row(Modifier.fillMaxWidth().padding(start = 14.dp, end = 14.dp, top = 8.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            UniBackButton(onClick = onBack)
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_news), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(room.title, fontSize = 11.5.sp, color = cs.onSurfaceVariant, maxLines = 1)
            }
            if (unseen.isNotEmpty()) Text(stringResource(R.string.rooms_mark_all_seen), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary,
                modifier = Modifier.cleanClickable { vm.markEventsSeen(room.id) }.padding(vertical = 6.dp, horizontal = 2.dp))
            else Text(stringResource(R.string.rooms_all_seen), fontSize = 12.sp, color = cs.onSurfaceVariant)
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 24.dp)) {
            if (unseen.isNotEmpty()) item { WhileAway(room, unseen) }
            item {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 10.dp, bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("all" to R.string.rooms_all_items, "mine" to R.string.rooms_for_me, "deliv" to R.string.rooms_deliveries, "changes" to R.string.rooms_changes).forEach { (k, l) ->
                        val n = when (k) { "all" -> events.size; "mine" -> events.count { isForMe(room, it) }; else -> events.count { groupOf(it) == k } }
                        val on = filter == k
                        Row(Modifier.clip(RoundedCornerShape(10.dp)).background(if (on) cs.primary else cs.surfaceContainer).cleanClickable { filter = k }.padding(horizontal = 11.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(l), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else cs.onSurface)
                            Text("$n", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = (if (on) cs.onPrimary else cs.onSurface).copy(alpha = 0.7f))
                        }
                    }
                }
            }
            if (forMe.isNotEmpty()) {
                item { RoomLabel(stringResource(R.string.rooms_for_you), trailing = "${forMe.size}", modifier = Modifier.padding(top = 0.dp)) }
                item { NewsGroup(room, forMe, seenAt, tones, today, true, go) }
            }
            rest.groupBy { epochDayOf(it.createdAt) }.forEach { (day, list) ->
                item { RoomLabel(when (day) { today -> stringResource(R.string.rooms_day_today); today - 1 -> stringResource(R.string.rooms_day_yesterday); else -> shortDate(day) }) }
                item { NewsGroup(room, list, seenAt, tones, today, false, go) }
            }
            if (forMe.isEmpty() && rest.isEmpty()) item {
                Text(stringResource(if (events.isEmpty()) R.string.rooms_news_empty else R.string.rooms_nothing_filter), fontSize = 12.5.sp, color = cs.onSurfaceVariant,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(30.dp))
            }
        }
    }
}

/** «Mientras no estabas»: un resumen en una frase de lo nuevo. */
@Composable
private fun WhileAway(room: WorkRoom, unseen: List<RoomEvent>) {
    val cs = MaterialTheme.colorScheme
    val ctx = LocalContext.current
    val parts = buildList {
        unseen.count { it.type == EventType.DELIVERED }.takeIf { it > 0 }?.let { add(ctx.resources.getQuantityString(R.plurals.rooms_sum_deliveries, it, it)) }
        unseen.count { it.type == EventType.MATERIAL }.takeIf { it > 0 }?.let { add(ctx.resources.getQuantityString(R.plurals.rooms_sum_materials, it, it)) }
        unseen.count { it.type == EventType.COMMENT }.takeIf { it > 0 }?.let { add(ctx.resources.getQuantityString(R.plurals.rooms_sum_comments, it, it)) }
        unseen.count { it.type == EventType.JOINED }.takeIf { it > 0 }?.let { add(ctx.resources.getQuantityString(R.plurals.rooms_sum_joined, it, it)) }
    }
    val due = unseen.firstOrNull { it.type == EventType.DUE_CHANGED }?.text?.toLongOrNull()
    Column(Modifier.padding(top = 10.dp, bottom = 4.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(cs.primaryContainer).padding(horizontal = 16.dp, vertical = 14.dp)) {
        Text(stringResource(R.string.rooms_while_away).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = cs.onPrimaryContainer.copy(alpha = 0.8f))
        Text(buildAnnotatedString {
            parts.forEachIndexed { i, s ->
                if (i > 0) append(if (i == parts.lastIndex && due == null) " ${ctx.getString(R.string.rooms_and)} " else ", ")
                withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append(s) }
            }
            if (due != null) {
                if (parts.isNotEmpty()) append(" ${ctx.getString(R.string.rooms_and)} ")
                append(ctx.getString(R.string.rooms_sum_due) + " ")
                withStyle(SpanStyle(fontWeight = FontWeight.Black)) { append(shortDate(due)) }
            }
            if (parts.isEmpty() && due == null) append(ctx.resources.getQuantityString(R.plurals.rooms_sum_other, unseen.size, unseen.size))
            append(".")
            val mine = unseen.filter { isForMe(room, it) }
            if (mine.isNotEmpty()) append(" " + ctx.resources.getQuantityString(R.plurals.rooms_sum_for_you, mine.size, mine.size))
        }, fontSize = 15.5.sp, fontWeight = FontWeight.Bold, color = cs.onPrimaryContainer, lineHeight = 22.sp, modifier = Modifier.padding(top = 4.dp))
    }
}

@Composable
private fun NewsGroup(room: WorkRoom, list: List<RoomEvent>, seenAt: Long, tones: List<Color>, today: Long, forMe: Boolean, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        list.forEachIndexed { i, e ->
            val (icon, ti) = iconOf(e.type)
            val tone = tones[ti]
            val isNew = e.createdAt > seenAt && e.actorId != room.meId
            Box(Modifier.fillMaxWidth().clip(groupShape(i, list.size, 18.dp)).background(if (forMe) mix(cs.primary, 0.10f, cs.surfaceContainerLow) else cs.surfaceContainerLow)
                .cleanClickable { openEvent(room, e, go) }.padding(horizontal = 12.dp, vertical = 11.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                    Box {
                        Box(Modifier.size(34.dp).clip(RoundedCornerShape(11.dp)).background(mix(tone, 0.16f, Color.Transparent)), contentAlignment = Alignment.Center) {
                            Icon(icon, null, tint = tone, modifier = Modifier.size(18.dp))
                        }
                        Box(Modifier.align(Alignment.BottomEnd).offset(5.dp, 5.dp).clip(CircleShape).background(cs.surfaceContainerLow).padding(2.dp)) { MemberFace(room, e.actorId, 16.dp) }
                    }
                    Column(Modifier.weight(1f).padding(end = if (isNew) 14.dp else 0.dp)) {
                        Text(eventText(room, e), fontSize = 13.5.sp, color = cs.onSurface, lineHeight = 19.sp)
                        Text(eventDetail(room, e, today), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (isNew) Box(Modifier.align(Alignment.TopEnd).padding(top = 4.dp)) { Dot(cs.primary, 8.dp) }
            }
        }
    }
}

private fun openEvent(room: WorkRoom, e: RoomEvent, go: (String, String) -> Unit) = when (e.type) {
    EventType.MENTION -> go("chat", "-")
    EventType.MATERIAL, EventType.TAGGED -> go("material", "-")
    EventType.JOINED -> go("grupo", "-")
    EventType.DUE_CHANGED -> go("sala", "-")
    else -> if (e.targetPartId != null && room.part(e.targetPartId) != null) go("parte", e.targetPartId) else go("sala", "-")
}

@Composable
private fun eventText(room: WorkRoom, e: RoomEvent) = buildAnnotatedString {
    val ctx = LocalContext.current
    val cs = MaterialTheme.colorScheme
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(whoText(room, e.actorId, ctx.getString(R.string.rooms_you))) }
    append(" ")
    fun bold(s: String) = withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(s) }
    when (e.type) {
        EventType.DELIVERED -> { append(ctx.getString(R.string.rooms_ev_delivered) + " "); bold(e.text) }
        EventType.COMMENT, EventType.COMMENT_ON_MINE -> {
            append(ctx.getString(R.string.rooms_ev_commented) + " ")
            bold(if (room.part(e.targetPartId)?.ownerIds?.contains(room.meId) == true && e.actorId != room.meId) ctx.getString(R.string.rooms_your_part) else e.text)
        }
        EventType.MENTION -> append(ctx.getString(if (isForMe(room, e)) R.string.rooms_ev_mentioned_you else R.string.rooms_ev_mentioned))
        EventType.DUE_CHANGED -> { append(ctx.getString(R.string.rooms_moved_final_to) + " "); bold(e.text.toLongOrNull()?.let { shortDate(it) } ?: e.text) }
        EventType.MATERIAL -> {
            append(ctx.getString(R.string.rooms_ev_uploaded) + " ")
            val m = room.materials.firstOrNull { it.name == e.text }
            if (m?.partId != null) { append(ctx.getString(R.string.rooms_ev_for) + " "); bold(room.part(m.partId)?.name.orEmpty()) }
            else bold(ctx.getString(R.string.rooms_material_title))
        }
        EventType.TAGGED -> { append(ctx.getString(if (isForMe(room, e)) R.string.rooms_ev_tagged_you else R.string.rooms_ev_tagged) + " "); bold(e.text) }
        EventType.JOINED -> append(ctx.getString(R.string.rooms_ev_joined))
        EventType.ASSIGNED -> { append(ctx.getString(R.string.rooms_ev_assigned) + " "); bold(e.text) }
        EventType.REVIEW_ASKED -> { append(ctx.getString(R.string.rooms_ev_review_asked) + " "); bold(e.text) }
    }
    @Suppress("UNUSED_EXPRESSION") cs
}

@Composable
private fun eventDetail(room: WorkRoom, e: RoomEvent, today: Long): String {
    val ctx = LocalContext.current
    val day = epochDayOf(e.createdAt)
    val whenTxt = if (day == today) hourOf(e.createdAt) else relDay(day - today) + " · " + hourOf(e.createdAt)
    return when (e.type) {
        EventType.COMMENT, EventType.COMMENT_ON_MINE -> if (e.detail.isNotBlank()) "«${e.detail.take(60)}» · $whenTxt" else "${e.text} · $whenTxt"
        EventType.MENTION -> "«${e.text.take(60)}» · $whenTxt"
        EventType.DUE_CHANGED -> e.detail.toLongOrNull()?.let { ctx.getString(R.string.rooms_before_was, shortDate(it)) + " · " + whenTxt } ?: whenTxt
        EventType.MATERIAL -> "${e.text} · $whenTxt"
        else -> whenTxt
    }
}
