package com.unistack.app.feature_rooms.presentation

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Comment
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TableChart
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_notes.presentation.NoteRecorderSheet
import com.unistack.app.feature_rooms.data.RoomStoredFile
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.PartAttachment
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.RequestType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomPart
import com.unistack.app.feature_rooms.domain.RoomProduce
import com.unistack.app.feature_rooms.domain.SharedMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import kotlinx.coroutines.delay

/**
 * Mi parte — artifact «mi parte», cerrado el 23 sep: la franja de materia arriba, a la derecha la
 * revisión (el ojo con su punto de color), los comentarios y el ⋮; el texto como papel que se
 * abre en el editor limpio (de la C), los adjuntos en tarjetas y abajo sólo «Entregar mi parte».
 * La extensión es una guía: no se mide, sólo se cuentan las palabras.
 */
@Composable
fun PartScreen(room: WorkRoom, partId: String, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val p = room.part(partId)
    if (p == null) { LaunchedEffect(partId) { onBack() }; return }
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val today = vm.today()
    val mine = room.meId in p.ownerIds
    val delivered = p.state == PartState.DELIVERED
    val slides = room.produces == RoomProduce.SLIDES
    var editing by rememberSaveable { mutableStateOf(false) }
    var sheet by remember { mutableStateOf<String?>(null) }
    val subjects by vm.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == room.subjectId }
    val hasContent = if (slides) p.slides > 0 || p.file != null else p.hasContent
    val firstMsg = stringResource(R.string.rooms_first_write)
    val deliveredMsg = stringResource(R.string.rooms_delivered_toast)

    val upload = rememberRoomPicker(vm) { f ->
        if (slides) vm.uploadPartSlides(room.id, p.id, f, vm.files.countSlides(f.storedName, f.mimeType))
        else vm.uploadPartFile(room.id, p.id, f)
        editing = false
        context.roomToast(context.getString(if (slides) R.string.rooms_slides_uploaded else R.string.rooms_file_uploaded))
    }
    val images = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(30)) { uris ->
        val stored = uris.mapNotNull { vm.files.import(it) }
        if (stored.isNotEmpty()) {
            vm.uploadPartSlides(room.id, p.id, stored.first().copy(displayName = context.getString(R.string.rooms_images_n, stored.size)), stored.size)
            stored.drop(1).forEach { s -> vm.attachFile(room.id, p.id, MaterialType.PHOTO, s.displayName, fileSubtitle(s.mimeType, s.sizeBytes), s.storedName, s.mimeType) }
        }
    }
    val deliver = {
        if (!hasContent) context.roomToast(firstMsg) else { vm.deliver(room.id, p.id); editing = false; context.roomToast(deliveredMsg) }
    }

    if (editing && mine && !delivered && !slides) {
        PartEditor(room, p, vm, onDone = { editing = false }, onUpload = { upload.pickFile(DOC_TYPES) }, onSheet = { sheet = it }, onDeliver = deliver)
    } else Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UniBackButton(onClick = onBack)
                Text(stringResource(if (mine) R.string.rooms_my_part else R.string.rooms_the_part), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                ReviewButton(p) { if (!hasContent && p.review == null) context.roomToast(firstMsg) else sheet = "review" }
                IconCircle(Icons.AutoMirrored.Rounded.Comment, stringResource(R.string.rooms_comments), badge = p.comments.size) { sheet = "coms" }
                if (mine) IconCircle(Icons.Rounded.MoreVert, stringResource(R.string.rooms_more)) { sheet = "more" }
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 104.dp)) {
                PartHero(room, p, subjectColor(subject), subject?.name ?: stringResource(R.string.rooms_no_subject), today)
                RoomLabel(stringResource(if (slides) R.string.rooms_your_slides else R.string.rooms_your_text), trailing = if (p.ownerIds.size > 1) stringResource(R.string.rooms_shared_part) else null)
                if (p.ownerIds.size > 1) SharedPieces(room, p, vm, mine)
                if (slides) SlidesContent(room, p, vm, mine && !delivered, onUpload = { upload.pickFile(SLIDE_TYPES) }, onImages = {
                    images.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                })
                else DocContent(room, p, vm, mine && !delivered, onWrite = { editing = true }, onUpload = { upload.pickFile(DOC_TYPES) })
                ReviewBox(room, p, onFix = { editing = true }, onComments = { sheet = "coms" })
                RoomLabel(stringResource(R.string.rooms_attachments), trailing = if (p.attachments.isNotEmpty()) stringResource(R.string.rooms_attach_go_with, p.attachments.size) else stringResource(R.string.rooms_what_you_used))
                Attachments(room, p, vm, mine && !delivered) { sheet = "attach" }
            }
        }
        if (mine) Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) {
            FloatingBar {
                if (delivered) {
                    Row(Modifier.weight(1f).padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Rounded.Check, null, tint = RoomTone.VERDE.color, modifier = Modifier.size(18.dp))
                        Text(stringResource(R.string.rooms_delivered_group_sees), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = RoomTone.VERDE.color)
                    }
                    Pill(stringResource(R.string.rooms_edit_again), { vm.reopenPart(room.id, p.id) }, style = PillStyle.TONAL)
                } else Pill(stringResource(R.string.rooms_deliver_my_part), deliver, Modifier.weight(1f), icon = Icons.Rounded.Check, alpha = if (hasContent) 1f else 0.5f, big = true)
            }
        }
    }

    when (sheet) {
        "review" -> ReviewSheet(room, p, vm) { sheet = null }
        "coms" -> PartCommentsSheet(room, p, vm) { sheet = null }
        "more" -> PartMoreSheet(room, p, vm, go) { sheet = null }
        "attach" -> AttachSheet(room, p, vm) { sheet = null }
    }
}

/** Botón redondo de 40 con número (`.ib .bdg`). */
@Composable
fun IconCircle(icon: ImageVector, cd: String?, badge: Int = 0, dot: Color? = null, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box {
        Box(Modifier.size(40.dp).clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onClick), contentAlignment = Alignment.Center) {
            Icon(icon, cd, tint = cs.onSurface, modifier = Modifier.size(20.dp))
        }
        if (badge > 0) Box(Modifier.align(Alignment.TopEnd).offset(4.dp, (-4).dp).clip(CircleShape).background(cs.background).padding(2.dp).heightIn(min = 17.dp)
            .clip(CircleShape).background(cs.primary).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            Text("$badge", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = cs.onPrimary)
        }
        if (dot != null) Box(Modifier.align(Alignment.TopEnd).offset((-2).dp, 2.dp).size(11.dp).clip(CircleShape).background(cs.background).padding(2.dp).clip(CircleShape).background(dot))
    }
}

/** El ojo de revisión: sin punto = sin pedir · azul = la están leyendo · ámbar = cambios · verde = visto bueno. */
@Composable
private fun ReviewButton(p: RoomPart, onClick: () -> Unit) {
    val dot = when {
        p.state == PartState.REVIEW -> RoomTone.AZUL.color
        p.review?.ok == true -> RoomTone.VERDE.color
        p.review?.ok == false -> RoomTone.AMBAR.color
        else -> null
    }
    IconCircle(Icons.Rounded.Visibility, stringResource(R.string.rooms_review), dot = dot, onClick = onClick)
}

/** `.est`: el estado de la parte en mayúsculas. */
@Composable
fun PartStateChip(p: RoomPart, small: Boolean = false) {
    val cs = MaterialTheme.colorScheme
    val (label, c) = when (p.state) {
        PartState.PENDING -> if (p.hasContent) R.string.rooms_st_draft to RoomTone.AMBAR.color else R.string.rooms_st_pending to null
        PartState.DRAFT -> R.string.rooms_st_draft to RoomTone.AMBAR.color
        PartState.REVIEW -> R.string.rooms_st_review to RoomTone.AZUL.color
        PartState.DELIVERED -> R.string.rooms_st_delivered to RoomTone.VERDE.color
    }
    Text(stringResource(label).uppercase(), fontSize = if (small) 9.5.sp else 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, color = c ?: cs.onSurfaceVariant,
        modifier = Modifier.clip(CircleShape).background(if (c != null) mix(c, 0.2f, Color.Transparent) else cs.surfaceContainerHighest).padding(horizontal = if (small) 7.dp else 10.dp, vertical = if (small) 3.dp else 5.dp))
}

/** La franja de materia (`.h3`): materia y sala, el nombre, estado, entrega · extensión · quién, y lo que debe llevar. */
@Composable
private fun PartHero(room: WorkRoom, p: RoomPart, mat: Color, subjectName: String, today: Long) {
    val cs = MaterialTheme.colorScheme
    val others = p.ownerIds.filter { it != room.meId }
    Column(Modifier.padding(top = 4.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(cs.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().background(mix(mat, 0.22f, cs.surfaceContainerLow)).padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 14.dp)) {
            Text("$subjectName · ${room.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface.copy(alpha = 0.85f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(p.name, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, letterSpacing = (-0.4).sp, lineHeight = 30.sp, modifier = Modifier.padding(top = 4.dp))
            Row(Modifier.padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PartStateChip(p)
                Verdict(room, p)
            }
        }
        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min).padding(horizontal = 6.dp, vertical = 12.dp)) {
            HeroCell(stringResource(R.string.rooms_due_short), shortDate(RoomLogic.partDue(room, p)))
            HeroDivider()
            HeroCell(stringResource(R.string.rooms_extent), if (p.extent > 0) "~${p.extent} ${unitName(room.produces).lowercase()}" else "—")
            HeroDivider()
            HeroCell(stringResource(if (others.isEmpty()) R.string.rooms_who else R.string.rooms_with),
                if (others.isEmpty()) stringResource(if (p.ownerIds.isEmpty()) R.string.rooms_no_owner else R.string.rooms_only_you) else others.joinToString(", ") { room.nameOf(it) })
        }
        if (p.note.isNotBlank()) Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 12.dp).fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.background).padding(horizontal = 13.dp, vertical = 11.dp)) {
            Text(stringResource(R.string.rooms_what_to_include).uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.6.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(bottom = 2.dp))
            Text(p.note, fontSize = 13.sp, color = cs.onSurface, lineHeight = 19.sp)
        }
    }
    @Suppress("UNUSED_EXPRESSION") today
}

@Composable
private fun RowScope.HeroCell(k: String, v: String) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.weight(1f).padding(horizontal = 4.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(k.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp, color = cs.onSurfaceVariant, maxLines = 1)
        Text(v, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun HeroDivider() = Box(Modifier.width(1.dp).fillMaxHeight().background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)))

/** `.vb`: «Visto bueno de X» / «X pidió cambios». */
@Composable
private fun Verdict(room: WorkRoom, p: RoomPart) {
    val r = p.review ?: return
    val c = if (r.ok) RoomTone.VERDE.color else RoomTone.AMBAR.color
    Row(Modifier.clip(CircleShape).background(mix(c, if (r.ok) 0.16f else 0.18f, Color.Transparent)).padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Icon(if (r.ok) Icons.Rounded.Check else Icons.Rounded.Edit, null, tint = c, modifier = Modifier.size(14.dp))
        Text(stringResource(if (r.ok) R.string.rooms_ok_from else R.string.rooms_changes_from, room.nameOf(r.byId)), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = c)
    }
}

/** Parte compartida: cada uno su trozo o uno escribe y otro revisa. */
@Composable
private fun SharedPieces(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, mine: Boolean) {
    val cs = MaterialTheme.colorScheme
    SegmG(listOf(stringResource(R.string.rooms_pieces), stringResource(R.string.rooms_one_writes)), if (p.sharedMode == SharedMode.PIECES) 0 else 1,
        { i -> if (mine) vm.updatePart(room.id, p.id) { it.copy(sharedMode = if (i == 0) SharedMode.PIECES else SharedMode.ONE_WRITES) } }, background = cs.surfaceContainerLow)
    Column(Modifier.padding(top = 8.dp, bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (p.sharedMode == SharedMode.PIECES) p.ownerIds.forEachIndexed { i, id ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberFace(room, id, 22.dp)
                Column(Modifier.weight(1f)) {
                    Text(whoText(room, id, stringResource(R.string.rooms_you)), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Text(stringResource(R.string.rooms_piece_n, i + 1, p.ownerIds.size), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
                }
                PartStateChip(p, small = true)
            }
        } else {
            val writer = p.ownerIds.first()
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MemberFace(room, writer, 22.dp)
                Column(Modifier.weight(1f)) {
                    Text(if (writer == room.meId) stringResource(R.string.rooms_you_write) else stringResource(R.string.rooms_x_writes, room.nameOf(writer)), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    Text(stringResource(R.string.rooms_others_review, p.ownerIds.drop(1).joinToString(", ") { whoText(room, it, "") }.ifBlank { "—" }), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
                }
            }
        }
    }
}

/** El texto: los dos botones para empezar, o el papel que se abre en el editor. */
@Composable
private fun DocContent(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, editable: Boolean, onWrite: () -> Unit, onUpload: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    if (!p.hasContent) {
        if (!editable) { Text(stringResource(R.string.rooms_nothing_yet), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp)); return }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StartButton(Icons.Rounded.Edit, stringResource(R.string.rooms_write_here), stringResource(R.string.rooms_write_here_d), primary = true, onClick = onWrite)
            StartButton(Icons.Rounded.Upload, stringResource(R.string.rooms_upload_file), stringResource(R.string.rooms_upload_file_d), primary = false, onClick = onUpload)
        }
        return
    }
    val deliveredMsg = stringResource(R.string.rooms_delivered_edit_hint)
    val fromFile = p.file != null
    Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(6.dp, 22.dp, 22.dp, 22.dp)).background(cs.surfaceContainerHigh).cleanClickable {
        when {
            p.text.isBlank() && p.file != null -> context.openRoomFile(vm, p.file, p.fileMime, p.fileName ?: p.name)
            editable -> onWrite()
            p.state == PartState.DELIVERED -> context.roomToast(deliveredMsg)
        }
    }.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 16.dp)) {
        Column {
            Row(Modifier.padding(bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(if (fromFile) Icons.Rounded.Description else Icons.Rounded.Edit, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(if (fromFile) stringResource(if (p.text.isNotBlank()) R.string.rooms_file_text_extracted else R.string.rooms_file_as_is, p.fileName.orEmpty()) else stringResource(R.string.rooms_written_in_app),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(end = 70.dp))
            }
            Text(p.name, fontFamily = FontFamily.Serif, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.padding(bottom = 6.dp))
            Text(p.text.ifBlank { stringResource(R.string.rooms_tap_to_open_file) }, fontFamily = FontFamily.Serif, fontSize = 13.5.sp, lineHeight = 23.sp, color = cs.onSurface.copy(alpha = 0.9f), maxLines = 6, overflow = TextOverflow.Ellipsis)
        }
        if (editable && p.text.isNotBlank()) Row(Modifier.align(Alignment.TopEnd).offset(6.dp, (-4).dp).clip(CircleShape).background(cs.background).padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Edit, null, tint = cs.primary, modifier = Modifier.size(13.dp))
            Text(stringResource(R.string.rooms_edit), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.primary)
        }
    }
    Row(Modifier.padding(start = 6.dp, end = 6.dp, top = 8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(if (p.text.isNotBlank()) pluralText(R.plurals.rooms_words, p.words) + " · " + stringResource(R.string.rooms_saved_moment) else stringResource(R.string.rooms_file_kept),
            fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (fromFile && editable) Row(Modifier.cleanClickable(onClick = onUpload).padding(vertical = 6.dp, horizontal = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
            Icon(Icons.Rounded.Upload, null, tint = cs.primary, modifier = Modifier.size(16.dp))
            Text(stringResource(R.string.rooms_upload_other_version), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary)
        }
    }
}

/** `.eB`: el botón grande para empezar (el primero, en primaryContainer). */
@Composable
private fun RowScope.StartButton(icon: ImageVector, title: String, sub: String, primary: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.weight(1f).heightIn(min = 130.dp).clip(RoundedCornerShape(22.dp)).background(if (primary) cs.primaryContainer else cs.surfaceContainerLow)
        .cleanClickable(onClick = onClick).padding(horizontal = 12.dp, vertical = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(if (primary) cs.primary else cs.surfaceContainerHighest), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = if (primary) cs.onPrimary else cs.onSurface, modifier = Modifier.size(21.dp))
        }
        Box(Modifier.weight(1f, fill = false).heightIn(min = 8.dp))
        Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = if (primary) cs.onPrimaryContainer else cs.onSurface, modifier = Modifier.padding(top = 12.dp))
        Text(sub, fontSize = 11.5.sp, color = if (primary) cs.onPrimaryContainer else cs.onSurfaceVariant, lineHeight = 15.sp)
    }
}

/** Diapositivas: subir el archivo o imágenes; quién presenta, cuánto dura y el guion. */
@Composable
private fun SlidesContent(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, editable: Boolean, onUpload: () -> Unit, onImages: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    if (p.file == null && p.slides == 0) {
        if (editable) Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StartButton(Icons.Rounded.Slideshow, stringResource(R.string.rooms_upload_ppt), stringResource(R.string.rooms_upload_ppt_d), primary = true, onClick = onUpload)
            StartButton(Icons.Rounded.Photo, stringResource(R.string.rooms_upload_images), stringResource(R.string.rooms_upload_images_d), primary = false, onClick = onImages)
        } else Text(stringResource(R.string.rooms_nothing_yet), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
    } else {
        val n = p.slides.coerceAtLeast(1)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            (1..n.coerceAtMost(12)).forEach { i ->
                Column(Modifier.width(104.dp).aspectRatio(16f / 10f).clip(RoundedCornerShape(10.dp)).background(FormatColors.paper)) {
                    Box(Modifier.fillMaxWidth().height(4.dp).background(RoomTone.NARANJA.color))
                    Column(Modifier.padding(6.dp)) {
                        Text(if (i == 1) p.name else "${p.name} · $i", fontSize = 8.5.sp, fontWeight = FontWeight.Bold, color = FormatColors.ink, maxLines = 2)
                        Text(stringResource(R.string.rooms_slide_n, i), fontSize = 8.sp, color = FormatColors.inkSoft, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }
        Row(Modifier.padding(top = 8.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).cleanClickable { p.file?.let { context.openRoomFile(vm, it, p.fileMime, p.fileName ?: p.name) } }
            .padding(start = 12.dp, end = 10.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            TypeIcon(Icons.Rounded.Slideshow, RoomTone.NARANJA.color)
            Column(Modifier.weight(1f)) {
                Text(p.fileName ?: p.name, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(pluralText(R.plurals.rooms_n_slides, p.slides), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
            if (editable) Pill(stringResource(R.string.rooms_replace), onUpload, icon = Icons.Rounded.Upload, style = PillStyle.TONAL, small = true)
        }
    }
    val presenter = p.presenterId ?: p.ownerIds.firstOrNull() ?: room.meId
    Row(Modifier.padding(top = 10.dp).height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).cleanClickable {
            if (editable) { val pool = p.ownerIds.ifEmpty { listOf(room.meId) }; val i = pool.indexOf(presenter); vm.updatePart(room.id, p.id) { it.copy(presenterId = pool[(i + 1) % pool.size]) } }
        }.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(stringResource(R.string.rooms_presents).uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, color = cs.onSurfaceVariant)
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MemberFace(room, presenter, 22.dp)
                Text(whoText(room, presenter, stringResource(R.string.rooms_you)), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(stringResource(R.string.rooms_lasts).uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, color = cs.onSurfaceVariant)
            Row(Modifier.padding(top = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MiniStep("−") { if (editable) vm.updatePart(room.id, p.id) { it.copy(minutes = (it.minutes - 1).coerceAtLeast(1)) } }
                Text(stringResource(R.string.rooms_n_min, p.minutes), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                MiniStep("+") { if (editable) vm.updatePart(room.id, p.id) { it.copy(minutes = it.minutes + 1) } }
            }
        }
    }
    RoomLabel(stringResource(R.string.rooms_script), trailing = stringResource(R.string.rooms_only_you_see))
    var script by remember(p.id) { mutableStateOf(p.script) }
    LaunchedEffect(script) { delay(600); if (script != p.script) vm.updatePart(room.id, p.id) { it.copy(script = script) } }
    RoomField(script, { script = it }, stringResource(R.string.rooms_script_hint), minHeight = 80.dp, fontSize = 13f)
}

@Composable
private fun MiniStep(t: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.size(24.dp).clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(t, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
    }
}

/** `.iIc`: el icono con el tono del tipo. */
@Composable
fun TypeIcon(icon: ImageVector, c: Color, size: androidx.compose.ui.unit.Dp = 36.dp) {
    Box(Modifier.size(size).clip(RoundedCornerShape(11.dp)).background(mix(c, 0.16f, Color.Transparent)), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = c, modifier = Modifier.size(size * 0.53f))
    }
}

/** `.revBox`: cómo va la revisión, debajo del texto. */
@Composable
private fun ReviewBox(room: WorkRoom, p: RoomPart, onFix: () -> Unit, onComments: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val r = p.review
    val (c, icon) = when {
        p.state == PartState.REVIEW -> RoomTone.AZUL.color to Icons.Rounded.Visibility
        r != null && !r.ok && p.state != PartState.DELIVERED -> RoomTone.AMBAR.color to Icons.Rounded.Edit
        r != null && r.ok && p.state != PartState.DELIVERED -> RoomTone.VERDE.color to Icons.Rounded.Check
        else -> return
    }
    Row(Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(mix(c, if (p.state == PartState.REVIEW) 0.12f else 0.14f, cs.surfaceContainerLow)).padding(horizontal = 12.dp, vertical = 11.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(icon, null, tint = c, modifier = Modifier.size(20.dp))
        Column(Modifier.weight(1f)) {
            when {
                p.state == PartState.REVIEW -> {
                    Text(stringResource(R.string.rooms_x_reading, p.reviewerId?.let { room.nameOf(it) } ?: stringResource(R.string.rooms_someone)), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(stringResource(R.string.rooms_reading_d), fontSize = 12.5.sp, color = cs.onSurface, lineHeight = 18.sp)
                }
                r != null && !r.ok -> {
                    Text(stringResource(R.string.rooms_x_asked_changes, room.nameOf(r.byId)), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text("«${r.note}»", fontSize = 12.5.sp, color = cs.onSurface, lineHeight = 18.sp)
                    Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Pill(stringResource(R.string.rooms_fix), onFix, small = true)
                        Pill(stringResource(R.string.rooms_see_in_comments), onComments, style = PillStyle.TONAL, small = true)
                    }
                }
                r != null -> {
                    Text(stringResource(R.string.rooms_x_gave_ok, room.nameOf(r.byId)), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(stringResource(R.string.rooms_can_deliver_now), fontSize = 12.5.sp, color = cs.onSurface)
                }
            }
        }
    }
}

fun attachmentIcon(a: PartAttachment): ImageVector =
    if (a.mime?.contains("sheet") == true || a.mime?.contains("excel") == true || a.mime?.contains("csv") == true) Icons.Rounded.TableChart else materialIcon(a.type)

@Composable
fun attachmentTone(a: PartAttachment): Color =
    if (a.mime?.contains("sheet") == true || a.mime?.contains("excel") == true || a.mime?.contains("csv") == true) RoomTone.CIAN.color else a.type.tone.color

/** `.adjs`: tarjetas de 132 en fila, la última para adjuntar. */
@Composable
private fun Attachments(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, editable: Boolean, onAdd: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(bottom = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        p.attachments.forEach { a ->
            Box(Modifier.width(132.dp).clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).cleanClickable {
                when { a.file != null -> context.openRoomFile(vm, a.file, a.mime, a.name); a.url.isNotBlank() -> context.openUrl(a.url) }
            }.padding(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    TypeIcon(attachmentIcon(a), attachmentTone(a))
                    Box(Modifier.height(5.dp))
                    Text(a.name, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                    Text(a.subtitle, fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (editable && a.byId == room.meId) Box(Modifier.align(Alignment.TopEnd).offset(6.dp, (-6).dp).size(24.dp).clip(CircleShape).cleanClickable {
                    a.file?.let { vm.files.delete(it) }; vm.removeAttachment(room.id, p.id, a.id)
                }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, stringResource(R.string.rooms_remove), tint = cs.onSurfaceVariant, modifier = Modifier.size(15.dp)) }
            }
        }
        if (editable) Column(Modifier.width(132.dp).clip(RoundedCornerShape(18.dp)).border(1.5.dp, cs.outlineVariant, RoundedCornerShape(18.dp)).cleanClickable(onClick = onAdd).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            TypeIcon(Icons.Rounded.Add, cs.primary)
            Box(Modifier.height(5.dp))
            Text(stringResource(R.string.rooms_attach), fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
            Text(stringResource(R.string.rooms_attach_d), fontSize = 11.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (!editable && p.attachments.isEmpty()) Text(stringResource(R.string.rooms_no_attachments), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp))
    }
}

/** `.barraH`: la barra flotante de abajo. */
@Composable
fun FloatingBar(content: @Composable RowScope.() -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().shadow(12.dp, RoundedCornerShape(22.dp)).clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainerHigh).padding(6.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp), content = content)
}

// ---------------------------------------------------------------- el editor (de la C)

@Composable
private fun PartEditor(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDone: () -> Unit, onUpload: () -> Unit, onSheet: (String) -> Unit, onDeliver: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var text by remember(p.id) { mutableStateOf(p.text) }
    var showNote by remember { mutableStateOf(false) }
    val save = { if (text != p.text) vm.write(room.id, p.id, text) }
    LaunchedEffect(text) { delay(700); save() }
    val done = { save(); onDone() }
    BackHandler(onBack = done)
    val words = text.trim().split(Regex("\\s+")).count { it.isNotBlank() }
    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                UniBackButton(onClick = done)
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(p.name, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    PartStateChip(p.copy(text = text), small = true)
                }
                Pill(stringResource(R.string.rooms_done), done, style = PillStyle.TONAL, small = true)
            }
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 96.dp)) {
                Row(Modifier.padding(top = 4.dp, bottom = 10.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).cleanClickable { showNote = !showNote }.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Rounded.Info, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.rooms_card_for, shortDate(RoomLogic.partDue(room, p))).replaceFirstChar { it.uppercase() } +
                            if (p.extent > 0) " · ~${p.extent} ${unitName(room.produces).lowercase()}" else "", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        Text(if (showNote) p.note.ifBlank { stringResource(R.string.rooms_no_indications) } else stringResource(R.string.rooms_tap_see_what), fontSize = 12.5.sp, color = cs.onSurfaceVariant)
                    }
                }
                Box(Modifier.fillMaxWidth().heightIn(min = 380.dp).clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLow).padding(16.dp)) {
                    if (text.isEmpty()) Text(stringResource(R.string.rooms_write_placeholder), fontFamily = FontFamily.Serif, fontSize = 14.5.sp, color = cs.onSurfaceVariant.copy(alpha = 0.7f))
                    BasicTextField(text, { text = it }, cursorBrush = SolidColor(cs.primary),
                        textStyle = TextStyle(color = cs.onSurface, fontFamily = FontFamily.Serif, fontSize = 14.5.sp, lineHeight = 24.5.sp), modifier = Modifier.fillMaxWidth().heightIn(min = 320.dp))
                }
                Row(Modifier.padding(start = 4.dp, end = 4.dp, top = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    Dot(RoomTone.VERDE.color, 7.dp)
                    Text(if (text.isNotBlank()) stringResource(R.string.rooms_saved_alone) + " · " + pluralText(R.plurals.rooms_words, words) else stringResource(R.string.rooms_write_here_d),
                        fontSize = 11.5.sp, color = cs.onSurfaceVariant)
                }
            }
        }
        Box(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().imePadding().padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) {
            FloatingBar {
                BarButton(Icons.Rounded.Upload, stringResource(R.string.rooms_upload), 0, onUpload)
                BarButton(Icons.Rounded.AttachFile, stringResource(R.string.rooms_attachments), p.attachments.size) { save(); onSheet("attach") }
                BarButton(Icons.AutoMirrored.Rounded.Comment, stringResource(R.string.rooms_notes), p.comments.size) { onSheet("coms") }
                BarButton(Icons.Rounded.Visibility, stringResource(R.string.rooms_review), 0) { save(); onSheet("review") }
                Pill(stringResource(R.string.rooms_deliver), { save(); onDeliver() }, Modifier.padding(start = 2.dp), big = true)
            }
        }
    }
}

@Composable
private fun RowScope.BarButton(icon: ImageVector, label: String, badge: Int, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.weight(1f)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).cleanClickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(icon, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
            Text(label, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1)
        }
        if (badge > 0) Box(Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 8.dp).heightIn(min = 15.dp).clip(CircleShape).background(cs.primary).padding(horizontal = 4.dp), contentAlignment = Alignment.Center) {
            Text("$badge", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = cs.onPrimary)
        }
    }
}

// ---------------------------------------------------------------- hojas de la parte

@Composable
private fun ReviewSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val mine = room.meId in p.ownerIds
    if (p.state == PartState.REVIEW && (mine || (p.reviewerId != null && p.reviewerId != room.meId))) {
        RoomSheet(onDismiss, stringResource(R.string.rooms_review), stringResource(R.string.rooms_x_reading, p.reviewerId?.let { room.nameOf(it) } ?: stringResource(R.string.rooms_someone))) {
            Text(stringResource(R.string.rooms_review_wait_d), fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 19.sp, modifier = Modifier.padding(horizontal = 2.dp))
        }
        return
    }
    if (!mine) {
        // Quien revisa: visto bueno o cambios con una nota.
        var note by remember { mutableStateOf("") }
        RoomSheet(onDismiss, stringResource(R.string.rooms_review), p.name) {
            RoomField(note, { note = it }, stringResource(R.string.rooms_review_note_hint), minHeight = 80.dp)
            Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Pill(stringResource(R.string.rooms_ask_changes), { if (note.isNotBlank()) { vm.review(room.id, p.id, false, note.trim()); onDismiss() } }, Modifier.weight(1f), style = PillStyle.TONAL)
                Pill(stringResource(R.string.rooms_give_ok), { vm.review(room.id, p.id, true, note.trim().ifBlank { context.getString(R.string.rooms_ok_default) }); onDismiss() }, Modifier.weight(1f), style = PillStyle.OK)
            }
        }
        return
    }
    val asked = stringResource(R.string.rooms_review_asked)
    RoomSheet(onDismiss, stringResource(if (p.review != null) R.string.rooms_ask_review_again else R.string.rooms_ask_review), stringResource(R.string.rooms_ask_review_d)) {
        Text(stringResource(R.string.rooms_ask_review_hint), fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 19.sp, modifier = Modifier.padding(start = 2.dp, end = 2.dp, bottom = 12.dp))
        room.activeMembers.filter { it.id != room.meId }.forEach { m ->
            val sub = when {
                m.id in p.ownerIds -> stringResource(R.string.rooms_shares_part)
                m.id == room.leaderId -> stringResource(R.string.rooms_builds_final)
                else -> room.partsOf(m.id).joinToString(", ") { it.name }.ifBlank { stringResource(R.string.rooms_no_part_yet) }
            }
            SheetRow(room.nameOf(m.id) + if (m.id == room.leaderId) " · " + stringResource(R.string.rooms_leader_word) else "", sub, leading = { MemberFace(room, m.id, 22.dp) },
                onClick = { vm.askReview(room.id, p.id, m.id); context.roomToast(String.format(asked, room.nameOf(m.id))); onDismiss() })
        }
        SheetRow(stringResource(R.string.rooms_anyone), stringResource(R.string.rooms_anyone_d), leading = { TypeIcon(RoomIcons.People, cs.primary) },
            onClick = { vm.askReview(room.id, p.id, null); context.roomToast(String.format(asked, context.getString(R.string.rooms_the_group))); onDismiss() })
    }
}

@Composable
private fun PartCommentsSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val today = vm.today()
    RoomSheet(onDismiss, stringResource(R.string.rooms_comments), stringResource(R.string.rooms_about_part_group, p.name)) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (p.comments.isEmpty()) Text(stringResource(R.string.rooms_no_comments), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 2.dp, vertical = 4.dp))
            p.comments.forEach { c ->
                Row(Modifier.padding(horizontal = 2.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    MemberFace(room, c.byId, 22.dp)
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)).background(if (c.isReview) mix(RoomTone.AZUL.color, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow)
                        .padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(whoText(room, c.byId, stringResource(R.string.rooms_you)) + (if (c.isReview) " · " + stringResource(R.string.rooms_review_word) else "") + " · " + relDay(epochDayOf(c.createdAt) - today),
                            fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
                        Text(c.text, fontSize = 13.sp, color = cs.onSurface, lineHeight = 19.sp)
                    }
                }
            }
            Box(Modifier.height(0.dp))
            CommentInput(stringResource(R.string.rooms_comment_your_part)) { vm.comment(room.id, p.id, it) }
        }
    }
}

@Composable
private fun PartMoreSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, go: (String, String) -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var assign by remember { mutableStateOf(false) }
    var confirmDrop by remember { mutableStateOf(false) }
    if (assign) return AssignSheet(room, p.id, vm) { assign = false; onDismiss() }
    val helpSent = stringResource(R.string.rooms_help_sent)
    val sentLeader = stringResource(R.string.rooms_sent_to, room.nameOf(room.leaderId))
    RoomSheet(onDismiss, p.name) {
        SheetRow(stringResource(R.string.rooms_ask_help), stringResource(R.string.rooms_ask_help_group_d), leading = { TypeIcon(Icons.Rounded.PanTool, cs.primary) }, onClick = {
            vm.send(room.id, context.getString(R.string.rooms_help_chat, p.name))
            if (!room.isLeader) vm.request(room.id, RequestType.HELP, p.id)
            context.roomToast(helpSent); onDismiss()
        })
        SheetRow(stringResource(R.string.rooms_ask_swap), stringResource(if (room.isLeader) R.string.rooms_swap_leader_d else R.string.rooms_swap_rule_d), leading = { TypeIcon(Icons.Rounded.SwapHoriz, cs.primary) }, onClick = {
            if (room.isLeader) assign = true else { vm.request(room.id, RequestType.SWAP, p.id); context.roomToast(sentLeader); onDismiss() }
        })
        SheetRow(if (confirmDrop) stringResource(R.string.rooms_drop_confirm) else stringResource(R.string.rooms_drop_part), stringResource(R.string.rooms_drop_part_d),
            leading = { TypeIcon(Icons.Rounded.Close, cs.error) }, danger = confirmDrop, onClick = {
                if (!confirmDrop) confirmDrop = true
                else if (room.isLeader) { vm.updatePart(room.id, p.id) { it.copy(ownerIds = it.ownerIds - room.meId) }; onDismiss(); go("sala", "-") }
                else { vm.request(room.id, RequestType.DROP, p.id); context.roomToast(sentLeader); onDismiss() }
            })
    }
}

/** «Adjuntar a tu parte»: fuente, enlace, foto, tabla, audio o documento. */
@Composable
private fun AttachSheet(room: WorkRoom, p: RoomPart, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    var form by remember { mutableStateOf<String?>(null) }
    var recording by remember { mutableStateOf(false) }
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var c by remember { mutableStateOf("") }
    val attached = stringResource(R.string.rooms_attached)
    val onFile: (MaterialType) -> (RoomStoredFile) -> Unit = { t -> { f ->
        vm.attachFile(room.id, p.id, t, f.displayName, fileSubtitle(f.mimeType, f.sizeBytes), f.storedName, f.mimeType)
        context.roomToast(attached); onDismiss()
    } }
    var pendingType by remember { mutableStateOf(MaterialType.DOC) }
    val picker = rememberRoomPicker(vm) { f -> onFile(pendingType)(f) }
    if (recording) {
        NoteRecorderSheet(createFile = { vm.files.newFileFor(it) }, onDiscard = { vm.files.delete(it) }, onSaved = { stored, millis ->
            val secs = (millis / 1000).toInt()
            vm.attachFile(room.id, p.id, MaterialType.AUDIO, context.getString(R.string.rooms_voice_note), context.getString(R.string.rooms_voice_note_len, secs / 60, secs % 60), stored, "audio/mp4")
            recording = false; context.roomToast(attached); onDismiss()
        }, onDismiss = { recording = false })
        return
    }
    when (form) {
        "link" -> RoomSheet({ form = null }, stringResource(R.string.rooms_mt_link), stringResource(R.string.rooms_attach_to_part, p.name)) {
            RoomField(a, { a = it }, "https://", single = true)
            Box(Modifier.height(6.dp))
            RoomField(b, { b = it }, stringResource(R.string.rooms_name_optional), single = true)
            Pill(stringResource(R.string.rooms_attach), { if (a.isNotBlank()) {
                vm.attachFile(room.id, p.id, MaterialType.LINK, b.trim().ifBlank { hostOf(a.trim()) }, hostOf(a.trim()), null, null, a.trim()); context.roomToast(attached); onDismiss()
            } }, Modifier.fillMaxWidth().padding(top = 10.dp), enabled = a.isNotBlank())
        }
        "source" -> RoomSheet({ form = null }, stringResource(R.string.rooms_mt_source), stringResource(R.string.rooms_attach_to_part, p.name)) {
            RoomField(a, { a = it }, stringResource(R.string.rooms_author_hint), single = true)
            Box(Modifier.height(6.dp))
            RoomField(b, { b = it }, stringResource(R.string.rooms_book_title), single = true)
            Box(Modifier.height(6.dp))
            RoomField(c, { c = it.filter { ch -> ch.isDigit() }.take(4) }, stringResource(R.string.rooms_year), single = true)
            Pill(stringResource(R.string.rooms_attach), { if (b.isNotBlank()) {
                vm.attachFile(room.id, p.id, MaterialType.SOURCE, listOf(a.trim(), b.trim()).filter { it.isNotBlank() }.joinToString(" — "), c.trim(), null, null); context.roomToast(attached); onDismiss()
            } }, Modifier.fillMaxWidth().padding(top = 10.dp), enabled = b.isNotBlank())
        }
        else -> RoomSheet(onDismiss, stringResource(R.string.rooms_attach_to_your_part), stringResource(R.string.rooms_what_you_used_d)) {
            val tiles = listOf(
                Triple(R.string.rooms_mt_source, Icons.Rounded.Book, RoomTone.VERDE.color) to { form = "source" },
                Triple(R.string.rooms_mt_link, Icons.Rounded.Link, RoomTone.AZUL.color) to { form = "link" },
                Triple(R.string.rooms_mt_photo, Icons.Rounded.Photo, RoomTone.ROSA.color) to { pendingType = MaterialType.PHOTO; picker.pickImage() },
                Triple(R.string.rooms_mt_table, Icons.Rounded.TableChart, RoomTone.CIAN.color) to { pendingType = MaterialType.DOC; picker.pickFile(arrayOf("application/vnd.ms-excel", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "text/csv", "text/comma-separated-values")) },
                Triple(R.string.rooms_mt_audio, Icons.Rounded.Mic, RoomTone.NARANJA.color) to { recording = true },
                Triple(R.string.rooms_mt_doc, Icons.Rounded.Description, RoomTone.INDIGO.color) to { pendingType = MaterialType.DOC; picker.pickFile(DOC_TYPES) }
            )
            tiles.chunked(3).forEach { row ->
                Row(Modifier.padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (t, action) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainerLow).cleanClickable(onClick = action).padding(horizontal = 6.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                            TypeIcon(t.second, t.third)
                            Text(stringResource(t.first), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}
