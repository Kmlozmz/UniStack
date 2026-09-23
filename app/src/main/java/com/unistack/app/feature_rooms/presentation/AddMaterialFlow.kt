package com.unistack.app.feature_rooms.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.Videocam
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_notes.presentation.NoteRecorderSheet
import com.unistack.app.feature_rooms.data.RoomStoredFile
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.WorkRoom

/**
 * «Añadir material» — artifact de Material de apoyo, cerrado el 23 sep: primero el tipo (una
 * fila con su color y para qué sirve) y después el detalle en hoja alta: de dónde sale, los
 * campos agrupados (la fuente con su cita), para qué parte y a quién avisar.
 */
@Composable
fun AddMaterialFlow(room: WorkRoom, vm: RoomsViewModel, presetPart: String? = null, onDismiss: () -> Unit) {
    var type by remember { mutableStateOf<MaterialType?>(null) }
    val t = type
    if (t == null) TypePickerSheet(room, presetPart, onPick = { type = it }, onDismiss = onDismiss)
    else MaterialDetailSheet(room, vm, t, presetPart, onBack = { type = null }, onDismiss = onDismiss)
}

fun materialDesc(t: MaterialType): Int = when (t) {
    MaterialType.LINK -> R.string.rooms_md_link
    MaterialType.DOC -> R.string.rooms_md_doc
    MaterialType.PHOTO -> R.string.rooms_md_photo
    MaterialType.VIDEO -> R.string.rooms_md_video
    MaterialType.AUDIO -> R.string.rooms_md_audio
    MaterialType.SOURCE -> R.string.rooms_md_source
    MaterialType.NOTE -> R.string.rooms_md_note
}

@Composable
private fun TypePickerSheet(room: WorkRoom, presetPart: String?, onPick: (MaterialType) -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val part = room.part(presetPart)
    RoomSheet(onDismiss, if (part != null) stringResource(R.string.rooms_add_to_x, part.name) else stringResource(R.string.rooms_add_material), stringResource(R.string.rooms_what_add)) {
        RoomGroup(MATERIAL_ORDER) { t, shape ->
            RoomRow(shape, minHeight = 62.dp, padding = androidx.compose.foundation.layout.PaddingValues(start = 13.dp, end = 12.dp, top = 12.dp, bottom = 12.dp), onClick = { onPick(t) }) {
                Tile(materialIcon(t), t.tone.color)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(materialName(t)), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(stringResource(materialDesc(t)), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 1.dp))
                }
                Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(-90f))
            }
        }
    }
}

private data class Origin(val label: Int, val icon: ImageVector, val action: String)

private fun originsOf(t: MaterialType): List<Origin> = when (t) {
    MaterialType.PHOTO -> listOf(Origin(R.string.rooms_from_camera, Icons.Rounded.PhotoCamera, "camera"), Origin(R.string.rooms_from_gallery, Icons.Rounded.Photo, "image"))
    MaterialType.DOC -> listOf(Origin(R.string.rooms_from_files, Icons.Rounded.Folder, "doc"), Origin(R.string.rooms_from_drive, Icons.Rounded.Description, "any"))
    MaterialType.VIDEO -> listOf(Origin(R.string.rooms_from_gallery, Icons.Rounded.Videocam, "video"), Origin(R.string.rooms_paste_link, Icons.Rounded.Link, "link"))
    MaterialType.AUDIO -> listOf(Origin(R.string.rooms_record_now, Icons.Rounded.Mic, "record"), Origin(R.string.rooms_upload_file, Icons.Rounded.Upload, "audio"))
    else -> emptyList()
}

@Composable
private fun MaterialDetailSheet(room: WorkRoom, vm: RoomsViewModel, t: MaterialType, presetPart: String?, onBack: () -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val origins = originsOf(t)
    var origin by remember { mutableStateOf(-1) }
    var picked by remember { mutableStateOf<RoomStoredFile?>(null) }
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var partId by remember { mutableStateOf(presetPart) }
    var partsOpen by remember { mutableStateOf(false) }
    var tagged by remember { mutableStateOf(listOf<String>()) }
    var query by remember { mutableStateOf("") }
    var recording by remember { mutableStateOf(false) }
    val picker = rememberRoomPicker(vm) { f -> picked = f; if (name.isBlank() || name == picked?.displayName) name = f.displayName }
    val singular = stringResource(materialName(t))
    val isLinkMode = t == MaterialType.LINK || (t == MaterialType.VIDEO && origins.getOrNull(origin)?.action == "link")
    val ready = when (t) {
        MaterialType.LINK -> url.isNotBlank()
        MaterialType.SOURCE -> name.isNotBlank()
        MaterialType.NOTE -> name.isNotBlank() || note.isNotBlank()
        MaterialType.VIDEO -> if (isLinkMode) url.isNotBlank() else picked != null
        else -> picked != null
    }
    val added = stringResource(if (partId != null) R.string.rooms_added_to_part else R.string.rooms_added_to_material)
    val save = {
        val f = picked
        val sub = when {
            t == MaterialType.SOURCE -> listOf(publisher, year).filter { it.isNotBlank() }.joinToString(" · ")
            isLinkMode -> hostOf(url.trim())
            f != null -> fileSubtitle(f.mimeType, f.sizeBytes)
            t == MaterialType.NOTE -> context.getString(R.string.rooms_mt_note)
            else -> ""
        }
        val finalName = when {
            t == MaterialType.LINK && name.isBlank() -> hostOf(url.trim())
            isLinkMode && name.isBlank() -> hostOf(url.trim())
            t == MaterialType.NOTE && name.isBlank() -> note.trim().take(60)
            else -> name.trim().ifBlank { f?.displayName ?: singular }
        }
        vm.addMaterial(room.id, vm.newMaterial(room, t, finalName, sub, note.trim(), partId, tagged, author.trim(), year.trim(), publisher.trim(),
            file = if (isLinkMode) null else f?.storedName, mime = if (isLinkMode) null else f?.mimeType, url = if (isLinkMode) url.trim() else ""))
        context.roomToast(added)
        onDismiss()
    }

    RoomSheet(
        onDismiss, tall = true,
        header = {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.size(40.dp).clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onBack), contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.rooms_back), tint = cs.onSurface, modifier = Modifier.size(20.dp))
                }
                Tile(materialIcon(t), t.tone.color)
                Column(Modifier.weight(1f)) {
                    Text(singular, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(stringResource(R.string.rooms_seen_by_all), fontSize = 12.sp, color = cs.onSurfaceVariant)
                }
            }
        },
        footer = {
            Column(Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(cs.outlineVariant.copy(alpha = 0.35f)))
                Pill(stringResource(R.string.rooms_add_x, singular.lowercase()), save, Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 16.dp), enabled = ready, big = true)
            }
        }
    ) {
        if (origins.isNotEmpty()) {
            RoomLabel(stringResource(R.string.rooms_from_where), Modifier.padding(top = 0.dp))
            RoomGroup(origins) { o, shape ->
                val i = origins.indexOf(o)
                val on = origin == i
                RoomRow(shape, color = if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, minHeight = 52.dp, onClick = {
                    origin = i
                    when (o.action) {
                        "camera" -> picker.takePhoto()
                        "image" -> picker.pickImage()
                        "video" -> picker.pickVideo()
                        "doc" -> picker.pickFile(DOC_TYPES)
                        "any" -> picker.pickFile(ANY_TYPES)
                        "audio" -> picker.pickFile(AUDIO_TYPES)
                        "record" -> recording = true
                    }
                }) {
                    Tile(o.icon, cs.onSurfaceVariant)
                    Text(if (on && picked != null && o.action != "link") picked!!.displayName else stringResource(o.label), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface,
                        maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    RadioDot(on)
                }
            }
        }
        RoomLabel(stringResource(if (t == MaterialType.SOURCE) R.string.rooms_source_data else R.string.rooms_name))
        Fields {
            if (t == MaterialType.SOURCE) {
                FieldLine(stringResource(R.string.rooms_author), author, { author = it }, stringResource(R.string.rooms_author_hint))
                FieldDivider()
                FieldLine(stringResource(R.string.rooms_book_title), name, { name = it })
                FieldDivider()
                Row(Modifier.height(androidx.compose.foundation.layout.IntrinsicSize.Min)) {
                    Box(Modifier.weight(1f)) { FieldLine(stringResource(R.string.rooms_year), year, { year = it.filter { c -> c.isDigit() }.take(4) }, keyboard = KeyboardType.Number) }
                    Box(Modifier.width(1.dp).fillMaxHeight().background(cs.background))
                    Box(Modifier.weight(2f)) { FieldLine(stringResource(R.string.rooms_publisher), publisher, { publisher = it }) }
                }
            } else {
                if (isLinkMode) {
                    FieldLine(stringResource(R.string.rooms_mt_link), url, { url = it }, "https://", keyboard = KeyboardType.Uri)
                    FieldDivider()
                    FieldLine(stringResource(R.string.rooms_name), name, { name = it }, stringResource(R.string.rooms_optional_hint), optional = true)
                } else FieldLine(stringResource(R.string.rooms_name), name, { name = it }, stringResource(R.string.rooms_name_hint))
                FieldDivider()
                FieldLine(stringResource(if (t == MaterialType.NOTE) R.string.rooms_note_text else R.string.rooms_note_field), note, { note = it },
                    stringResource(R.string.rooms_note_placeholder), optional = t != MaterialType.NOTE, multi = true)
            }
        }
        if (t == MaterialType.SOURCE) {
            Row(Modifier.padding(start = 4.dp, end = 4.dp, top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.Book, null, tint = RoomTone.VERDE.color, modifier = Modifier.padding(top = 1.dp).size(16.dp))
                Text(buildAnnotatedString {
                    append(stringResource(R.string.rooms_cite_preview) + " ")
                    val a = author.ifBlank { stringResource(R.string.rooms_author) }
                    val y = year.ifBlank { "s. f." }
                    val ttl = name.ifBlank { stringResource(R.string.rooms_book_title) }
                    if (room.bibliographyStyle == "MLA") {
                        append("$a "); withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(ttl) }; append(". ${publisher.ifBlank { "—" }}, $y.")
                    } else {
                        append("$a ($y). "); withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(ttl) }; append(". ${publisher.ifBlank { "—" }}.")
                    }
                }, fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp)
            }
        }
        RoomLabel(stringResource(R.string.rooms_for_which_part))
        PartPicker(room, partId, partsOpen, onToggle = { partsOpen = !partsOpen }) { partId = it; partsOpen = false }
        RoomLabel(stringResource(R.string.rooms_notify), trailing = stringResource(R.string.rooms_notify_d))
        PeoplePicker(room, partId, tagged, query, onQuery = { query = it }) { tagged = it }
    }

    if (recording) NoteRecorderSheet(
        createFile = { ext -> vm.files.newFileFor(ext) },
        onDiscard = { vm.files.delete(it) },
        onSaved = { stored, millis ->
            val f = vm.files.file(stored)
            val secs = (millis / 1000).toInt()
            picked = RoomStoredFile(stored, context.getString(R.string.rooms_voice_note), "audio/mp4", f.length())
            if (name.isBlank()) name = context.getString(R.string.rooms_voice_note_len, secs / 60, secs % 60)
            recording = false
        },
        onDismiss = { recording = false }
    )
}

/** `.campos`: un bloque con las líneas separadas por el fondo. */
@Composable
private fun Fields(content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLow)) { content() }
}

@Composable
private fun FieldDivider() = Box(Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.background))

@Composable
private fun FieldLine(label: String, value: String, onChange: (String) -> Unit, placeholder: String = "", optional: Boolean = false, multi: Boolean = false, keyboard: KeyboardType = KeyboardType.Text) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(buildAnnotatedString {
            append(label)
            if (optional) withStyle(SpanStyle(fontWeight = FontWeight.Medium, color = cs.onSurfaceVariant.copy(alpha = 0.8f))) { append(" " + LocalContext.current.getString(R.string.rooms_optional_word)) }
        }, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, letterSpacing = 0.3.sp)
        Box(Modifier.fillMaxWidth().then(if (multi) Modifier.heightIn(min = 52.dp) else Modifier)) {
            if (value.isEmpty() && placeholder.isNotEmpty()) Text(placeholder, fontSize = if (multi) 14.sp else 15.sp, color = cs.onSurfaceVariant.copy(alpha = 0.6f))
            BasicTextField(value, onChange, singleLine = !multi, cursorBrush = SolidColor(cs.primary), keyboardOptions = KeyboardOptions(keyboardType = keyboard),
                textStyle = TextStyle(color = cs.onSurface, fontSize = if (multi) 14.sp else 15.sp, fontWeight = if (multi) FontWeight.Medium else FontWeight.SemiBold),
                modifier = Modifier.fillMaxWidth())
        }
    }
}

/** «Para qué parte»: una sola fila que se despliega con todas. */
@Composable
fun PartPicker(room: WorkRoom, partId: String?, open: Boolean, onToggle: () -> Unit, onPick: (String?) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val part = room.part(partId)
    val rot by animateFloatAsState(if (open) 180f else 0f, label = "pp")
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        val count = if (open) room.parts.size + 2 else 1
        RoomRow(groupShape(0, count), minHeight = 56.dp, onClick = onToggle) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(mix(cs.onSurfaceVariant, 0.16f, cs.surfaceContainerHigh)), contentAlignment = Alignment.Center) {
                if (part != null) Text("${room.parts.indexOf(part) + 1}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                else Icon(Icons.Rounded.Description, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(part?.name ?: stringResource(R.string.rooms_whole_work), fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(if (part != null) stringResource(R.string.rooms_of_x, part.ownerIds.firstOrNull()?.let { whoText(room, it, stringResource(R.string.rooms_you)) } ?: stringResource(R.string.rooms_nobody_yet))
                    else stringResource(R.string.rooms_not_a_part), fontSize = 12.sp, color = cs.onSurfaceVariant)
            }
            Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(rot))
        }
        AnimatedVisibility(open) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                val options = listOf<String?>(null) + room.parts.map { it.id }
                options.forEachIndexed { i, id ->
                    val p = room.part(id)
                    val on = id == partId
                    RoomRow(groupShape(i + 1, options.size + 1), color = if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, minHeight = 48.dp, onClick = { onPick(id) }) {
                        Box(Modifier.size(28.dp).clip(RoundedCornerShape(9.dp)).background(if (p != null) cs.surfaceContainerHighest else Color.Transparent), contentAlignment = Alignment.Center) {
                            if (p != null) Text("$i", fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurfaceVariant)
                        }
                        Text(p?.name ?: stringResource(R.string.rooms_whole_work), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.weight(1f))
                        p?.ownerIds?.firstOrNull()?.let { MemberFace(room, it, 20.dp) }
                        RadioDot(on)
                    }
                }
            }
        }
    }
}

/** «Avisar a»: buscador + caras en fila (Todos primero y quien tiene la parte, marcado). */
@Composable
fun PeoplePicker(room: WorkRoom, partId: String?, tagged: List<String>, query: String, onQuery: (String) -> Unit, onChange: (List<String>) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val everyone = room.activeMembers.filter { it.id != room.meId }.map { it.id }
    val sug = room.part(partId)?.ownerIds.orEmpty().filter { it != room.meId }
    val q = query.trim().lowercase()
    val list = if (q.isNotEmpty()) everyone.filter { room.nameOf(it).lowercase().contains(q) }
    else tagged + sug.filter { it !in tagged } + everyone.filter { it !in tagged && it !in sug }
    Row(Modifier.padding(bottom = 10.dp).fillMaxWidth().clip(CircleShape).background(cs.surfaceContainerLow).padding(start = 14.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.Search, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f).padding(vertical = 11.dp)) {
            if (query.isEmpty()) Text(stringResource(R.string.rooms_search_person, everyone.size), fontSize = 14.sp, color = cs.onSurfaceVariant.copy(alpha = 0.8f), maxLines = 1)
            BasicTextField(query, onQuery, singleLine = true, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 14.sp), modifier = Modifier.fillMaxWidth())
        }
        if (tagged.isNotEmpty()) Box(Modifier.heightIn(min = 24.dp).clip(RoundedCornerShape(12.dp)).background(cs.primary).padding(horizontal = 7.dp), contentAlignment = Alignment.Center) {
            Text("${tagged.size}", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = cs.onPrimary)
        }
    }
    if (everyone.isEmpty()) {
        Text(stringResource(R.string.rooms_nobody_else), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))
        return
    }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(top = 4.dp, bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        if (q.isEmpty()) {
            val all = tagged.size == everyone.size
            Column(Modifier.width(52.dp).cleanClickable { onChange(if (all) emptyList() else everyone) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(46.dp).then(if (all) Modifier.border(2.dp, cs.primary, CircleShape).padding(4.dp) else Modifier).clip(CircleShape).background(if (all) cs.primary else cs.surfaceContainer), contentAlignment = Alignment.Center) {
                    Icon(RoomIcons.People, null, tint = if (all) cs.onPrimary else cs.onSurface, modifier = Modifier.size(22.dp))
                }
                Text(stringResource(R.string.rooms_everyone), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (all) cs.onSurface else cs.onSurfaceVariant, maxLines = 1)
            }
        }
        if (list.isEmpty()) Text(stringResource(R.string.rooms_nobody_named), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(horizontal = 4.dp, vertical = 10.dp))
        list.forEach { id ->
            val on = id in tagged
            Column(Modifier.width(52.dp).cleanClickable { onChange(if (on) tagged - id else tagged + id) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Box(Modifier.size(46.dp).then(if (on) Modifier.border(2.dp, cs.primary, CircleShape).padding(4.dp) else Modifier.alpha(0.55f)), contentAlignment = Alignment.Center) {
                    MemberFace(room, id, if (on) 38.dp else 46.dp)
                }
                Text(room.nameOf(id), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onSurface else cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (id in sug && q.isEmpty()) Text(stringResource(R.string.rooms_their_part), fontSize = 9.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.primary, modifier = Modifier.padding(top = 0.dp))
            }
        }
    }
}
