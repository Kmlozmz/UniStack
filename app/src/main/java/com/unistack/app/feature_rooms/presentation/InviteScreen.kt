package com.unistack.app.feature_rooms.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.RoomType
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom

/**
 * «¡La sala está lista!» — el final de Crear sala (artifact cerrado el 23 sep): el código en un
 * boleto con copiar y compartir, los atajos con el icono real de cada app instalada (WhatsApp,
 * Telegram y el correo; el QR ya va en el boleto), quién ya entró y qué pasa con
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
    val subjects by vm.subjects.collectAsState()
    val stripe = subjects.firstOrNull { it.id == room.subjectId }?.let { subjectColor(it) } ?: RoomTemplates.tone(room.type)

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 110.dp)) {
            Column(Modifier.fillMaxWidth().padding(start = 10.dp, end = 10.dp, bottom = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(56.dp).clip(CircleShape).background(mix(RoomTone.VERDE.color, 0.2f, Color.Transparent)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = RoomTone.VERDE.color, modifier = Modifier.size(30.dp))
                }
                Text(stringResource(R.string.rooms_ready_title), fontSize = 21.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 10.dp))
                Text(room.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 3.dp))
                Text(stringResource(R.string.rooms_ready_sub), fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 19.sp, modifier = Modifier.padding(top = 4.dp))
            }
            InviteTicket(room, stripe, onCopy = { context.copyText("invite", text); context.roomToast(copied) }, onShare = { context.shareText(text) }, onQr = { qr = true })
            val apps = rememberShareApps(room.title, text)
            if (apps.isNotEmpty()) Row(Modifier.padding(top = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                apps.forEach { ShareShortcut(it) }
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
    @Suppress("UNUSED_EXPRESSION") onBack
}

/**
 * El código como un boleto (artifact «Crear sala, segunda vuelta», opción A): franja del color
 * de la materia, el código grande con su QR al lado, el corte con muescas y, abajo, copiar y compartir.
 */
@Composable
private fun InviteTicket(room: WorkRoom, stripe: Color, onCopy: () -> Unit, onShare: () -> Unit, onQr: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val on = cs.onSurface
    val meta = listOfNotNull(
        room.title,
        room.type.takeIf { it != RoomType.CERO }?.let { stringResource(RoomTemplates.typeNameRes(it)) },
        room.parts.size.takeIf { it > 0 }?.let { pluralText(R.plurals.rooms_parts_n, it) }
    ).joinToString(" · ")
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(cs.primaryContainer)) {
        Box(Modifier.fillMaxWidth().height(8.dp).background(stripe))
        Row(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_room_code).uppercase(), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = on.copy(alpha = 0.7f))
                Text(room.code, fontSize = 30.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, letterSpacing = 6.sp, color = on, maxLines = 1, modifier = Modifier.padding(top = 4.dp, bottom = 2.dp))
                Text(meta, fontSize = 12.sp, color = on.copy(alpha = 0.75f), lineHeight = 16.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Box(Modifier.clip(RoundedCornerShape(14.dp)).cleanClickable(onClick = onQr)) { QrView("unistack://sala/${room.code}", size = 84.dp) }
        }
        // el corte del boleto: línea de puntos con una muesca a cada lado
        Box(Modifier.fillMaxWidth().height(22.dp)) {
            val dash = on.copy(alpha = 0.25f)
            Canvas(Modifier.align(Alignment.Center).fillMaxWidth().padding(horizontal = 16.dp).height(2.dp)) {
                drawLine(dash, Offset(0f, size.height / 2), Offset(size.width, size.height / 2), strokeWidth = size.height,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx())))
            }
            Box(Modifier.align(Alignment.CenterStart).offset(x = (-11).dp).size(22.dp).clip(CircleShape).background(cs.background))
            Box(Modifier.align(Alignment.CenterEnd).offset(x = 11.dp).size(22.dp).clip(CircleShape).background(cs.background))
        }
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Pill(stringResource(R.string.rooms_copy), onCopy, Modifier.weight(1f), icon = Icons.Rounded.ContentCopy, style = PillStyle.TONAL, big = true)
            Pill(stringResource(R.string.rooms_share), onShare, Modifier.weight(1f), icon = Icons.Rounded.Share, big = true)
        }
    }
}

/** Atajo para compartir: el icono de la app tal como está en el teléfono y su nombre debajo. */
@Composable
private fun androidx.compose.foundation.layout.RowScope.ShareShortcut(app: ShareApp) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).cleanClickable(onClick = app.open).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Image(app.icon, app.label, Modifier.size(52.dp))
        Text(app.label, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface, maxLines = 1)
    }
}
