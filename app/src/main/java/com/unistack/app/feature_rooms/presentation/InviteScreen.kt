package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.QrCode2
import androidx.compose.material.icons.rounded.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom

/**
 * «¡La sala está lista!» — el final de Crear sala (artifact cerrado el 23 sep): el código grande,
 * copiar y compartir, los atajos (WhatsApp, Telegram, correo y QR), quién ya entró y qué pasa con
 * las partes según cómo se repartan.
 */
@Composable
fun InviteScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, onGoRoom: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var qr by remember { mutableStateOf(false) }
    val copied = stringResource(R.string.rooms_code_copied)
    val text = inviteText(context, room)
    val waiting = if (room.split == SplitMode.DRAW) (minOf(room.capacity, 6) - room.activeMembers.size).coerceAtLeast(0) else 0

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 110.dp)) {
            Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(mix(RoomTone.VERDE.color, 0.2f, Color.Transparent)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = RoomTone.VERDE.color, modifier = Modifier.size(30.dp))
                }
                Text(stringResource(R.string.rooms_ready_title), fontSize = 21.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                Text(room.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.primary, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
                Text(stringResource(R.string.rooms_ready_sub), fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(cs.primaryContainer).padding(horizontal = 16.dp, vertical = 20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.rooms_room_code), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface.copy(alpha = 0.85f))
                Text(room.code, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, color = cs.onSurface, modifier = Modifier.padding(top = 6.dp, bottom = 4.dp))
                Text(stringResource(R.string.rooms_code_where), fontSize = 12.5.sp, color = cs.onSurface.copy(alpha = 0.8f), textAlign = TextAlign.Center)
                Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pill(stringResource(R.string.rooms_copy_invite), { context.copyText("invite", text); context.roomToast(copied) }, icon = Icons.Rounded.ContentCopy, style = PillStyle.TONAL, small = true)
                    Pill(stringResource(R.string.rooms_share), { context.shareText(text) }, icon = Icons.Rounded.Share, small = true)
                }
            }
            Row(Modifier.padding(top = 14.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ShareShortcut("WhatsApp", "W", FormatColors.whatsapp) { context.shareText(text, "com.whatsapp") }
                ShareShortcut("Telegram", "T", FormatColors.telegram) { context.shareText(text, "org.telegram.messenger") }
                ShareShortcut(stringResource(R.string.rooms_email), stringResource(R.string.rooms_email).take(1), FormatColors.mail) { context.emailText(room.title, text) }
                ShareShortcut("QR", null, FormatColors.text) { qr = true }
            }
            RoomLabel(stringResource(R.string.rooms_who_joined), trailing = if (room.split == SplitMode.DRAW) stringResource(R.string.rooms_x_of_y, room.activeMembers.size, room.capacity) else "${room.activeMembers.size}")
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val count = room.activeMembers.size + if (waiting > 0) 1 else 0
                room.activeMembers.forEachIndexed { i, m ->
                    RoomRow(groupShape(i, count), minHeight = 52.dp) {
                        MemberFace(room, m.id, 34.dp)
                        RowTexts(room.nameOf(m.id) + if (m.id == room.meId) " " + stringResource(R.string.rooms_you_paren) else "",
                            if (m.id == room.leaderId) stringResource(R.string.rooms_leader_word) else stringResource(R.string.rooms_just_joined), titleSize = 14.5.sp)
                    }
                }
                if (waiting > 0) RoomRow(groupShape(count - 1, count), minHeight = 52.dp) {
                    DashedFace(34.dp)
                    Text(pluralText(R.plurals.rooms_waiting_n, waiting), fontSize = 12.sp, color = cs.onSurfaceVariant)
                }
            }
            Text(stringResource(when (room.split) {
                SplitMode.ASSIGN -> R.string.rooms_hint_assign
                SplitMode.DRAW -> if (waiting > 0) R.string.rooms_hint_draw else R.string.rooms_hint_draw_done
                SplitMode.MIXED -> R.string.rooms_hint_mixed
                SplitMode.FREE -> R.string.rooms_hint_free
            }), fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 19.sp, modifier = Modifier.padding(start = 4.dp, end = 4.dp, top = 12.dp))
        }
        RoomDock(Modifier.align(Alignment.BottomCenter)) {
            Pill(stringResource(R.string.rooms_go_to_room), onGoRoom, Modifier.weight(1f), big = true)
        }
    }
    if (qr) QrSheet(room) { qr = false }
    @Suppress("UNUSED_EXPRESSION") vm
    @Suppress("UNUSED_EXPRESSION") onBack
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.ShareShortcut(label: String, letter: String?, color: Color, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.weight(1f).cleanClickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(48.dp).clip(CircleShape).background(color), contentAlignment = Alignment.Center) {
            if (letter != null) Text(letter, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = FormatColors.onLogo)
            else Icon(Icons.Rounded.QrCode2, null, tint = FormatColors.onLogo, modifier = Modifier.size(22.dp))
        }
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface, modifier = Modifier.heightIn(min = 14.dp))
    }
}
