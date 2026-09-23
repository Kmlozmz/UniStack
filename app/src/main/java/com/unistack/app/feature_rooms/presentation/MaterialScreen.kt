package com.unistack.app.feature_rooms.presentation

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Sort
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomMaterial
import com.unistack.app.feature_rooms.domain.WorkRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Los nombres de grupo del material (en plural). */
fun materialGroupName(t: MaterialType): Int = when (t) {
    MaterialType.LINK -> R.string.rooms_mg_link
    MaterialType.DOC -> R.string.rooms_mg_doc
    MaterialType.PHOTO -> R.string.rooms_mg_photo
    MaterialType.VIDEO -> R.string.rooms_mg_video
    MaterialType.AUDIO -> R.string.rooms_mg_audio
    MaterialType.SOURCE -> R.string.rooms_mg_source
    MaterialType.NOTE -> R.string.rooms_mg_note
}

/**
 * Material de apoyo — artifact «material de apoyo», opción A hecha con piezas de la app y cerrada
 * el 23 sep: filtros (con «Para mí»), buscar y ordenar; avisos con la cara de quien etiquetó;
 * destacados; filas por tipo con su color (tres y «Ver los N») y la bibliografía que se arma sola.
 */
@Composable
fun MaterialScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit, go: (String, String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val today = vm.today()
    var filter by rememberSaveable { mutableStateOf<String?>(null) }
    var forMe by rememberSaveable { mutableStateOf(false) }
    var searching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var sort by rememberSaveable { mutableStateOf("new") }
    var expanded by remember { mutableStateOf(setOf<String>()) }
    var viewing by remember { mutableStateOf<String?>(null) }
    var sheet by remember { mutableStateOf<String?>(null) }
    var adding by remember { mutableStateOf(false) }
    val all = room.materials
    val you = stringResource(R.string.rooms_you)

    val filtered = all.filter { filter == null || it.type.name == filter }
        .filter { !forMe || RoomLogic.forMe(room, it) }
        .filter { m ->
            val q = query.trim().lowercase()
            q.isEmpty() || listOf(m.name, room.nameOf(m.byId), m.note, m.author).any { it.lowercase().contains(q) }
        }
        .let { l -> if (sort == "person") l.sortedBy { room.nameOf(it.byId).lowercase() } else l.sortedByDescending { it.createdAt } }
    val plain = query.isBlank() && filter == null && !forMe

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UniBackButton(onClick = onBack)
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(stringResource(R.string.rooms_material_title), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1)
                    Text("${room.title} · ${pluralText(R.plurals.rooms_things_n, all.size)}", fontSize = 11.5.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                RoundButton(Icons.Rounded.Search, stringResource(R.string.rooms_search), { searching = !searching; if (!searching) query = "" })
                RoundButton(Icons.Rounded.Sort, stringResource(R.string.rooms_sort), { sheet = "sort" })
            }
            if (searching) Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.weight(1f).clip(CircleShape).background(cs.surfaceContainer).padding(horizontal = 14.dp, vertical = 10.dp)) {
                    if (query.isEmpty()) Text(stringResource(R.string.rooms_search_material), fontSize = 13.5.sp, color = cs.onSurfaceVariant, maxLines = 1)
                    BasicTextField(query, { query = it }, singleLine = true, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 13.5.sp), modifier = Modifier.fillMaxWidth())
                }
                Text(stringResource(R.string.rooms_close), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.cleanClickable { searching = false; query = "" }.padding(horizontal = 2.dp, vertical = 6.dp))
            }
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                MatChip(stringResource(R.string.rooms_for_me), all.count { RoomLogic.forMe(room, it) }, forMe, tinted = true) { forMe = !forMe }
                MatChip(stringResource(R.string.rooms_all_items), all.size, filter == null) { filter = null }
                MATERIAL_ORDER.forEach { t ->
                    val n = all.count { it.type == t }
                    if (n > 0) MatChip(stringResource(materialGroupName(t)), n, filter == t.name) { filter = if (filter == t.name) null else t.name }
                }
            }
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 120.dp)) {
                if (plain) {
                    val avisos = all.filter { RoomLogic.forMe(room, it) && room.meId !in it.seenBy }
                    if (avisos.isNotEmpty()) item {
                        Column(Modifier.padding(top = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            avisos.take(2).forEach { m -> NoticeRow(room, m, onOpen = { viewing = m.id }, onDismiss = { vm.seeMaterial(room.id, m.id) }) }
                            if (avisos.size > 2) Text(stringResource(R.string.rooms_see_the_n, avisos.size), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary,
                                modifier = Modifier.padding(start = 6.dp).cleanClickable { forMe = true }.padding(vertical = 6.dp, horizontal = 2.dp))
                        }
                    }
                    val pinned = all.filter { it.pinned }
                    if (pinned.isNotEmpty()) {
                        item { RoomLabel(stringResource(R.string.rooms_pinned), trailing = stringResource(R.string.rooms_pinned_d)) }
                        item { RoomGroup(pinned) { m, shape -> MaterialRow(room, m, shape, today, forMe) { viewing = m.id } } }
                    }
                }
                val types = if (filter == null) MATERIAL_ORDER else MATERIAL_ORDER.filter { it.name == filter }
                types.forEach { t ->
                    val g = filtered.filter { it.type == t && !(plain && it.pinned) }
                    if (g.isNotEmpty()) {
                        val open = t.name in expanded || filter == t.name || g.size <= 3
                        val shown = if (open) g else g.take(3)
                        item { RoomLabel(stringResource(materialGroupName(t)), trailing = "${g.size}") }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                val count = shown.size + if (g.size > 3 && filter != t.name) 1 else 0
                                shown.forEachIndexed { i, m -> MaterialRow(room, m, groupShape(i, count), today, forMe) { viewing = m.id } }
                                if (g.size > 3 && filter != t.name) RoomRow(groupShape(count - 1, count), minHeight = 48.dp, onClick = { expanded = if (open) expanded - t.name else expanded + t.name }) {
                                    Box(Modifier.size(40.dp), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.primary, modifier = Modifier.size(20.dp).rotate(if (open) 180f else 0f)) }
                                    Text(if (open) stringResource(R.string.rooms_see_less) else stringResource(R.string.rooms_see_the_n, g.size), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.primary)
                                }
                            }
                        }
                    }
                }
                val sources = all.filter { it.type == MaterialType.SOURCE }
                if ((filter == null || filter == MaterialType.SOURCE.name) && sources.isNotEmpty() && !forMe) {
                    item { RoomLabel(stringResource(R.string.rooms_bibliography), trailing = stringResource(R.string.rooms_builds_itself)) }
                    item {
                        RoomRow(groupShape(0, 1), minHeight = 62.dp, onClick = { sheet = "bib" }) {
                            Tile(Icons.Rounded.Book, cs.primary)
                            Column(Modifier.weight(1f)) {
                                Text(stringResource(R.string.rooms_bib_in, room.bibliographyStyle), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                                Text(pluralText(R.plurals.rooms_n_sources, sources.size) + " · " + stringResource(R.string.rooms_bib_goes_end), fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                            Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(-90f))
                        }
                    }
                }
                if (filtered.isEmpty() && !(plain && all.any { it.pinned })) item {
                    Text(if (all.isEmpty()) stringResource(R.string.rooms_material_empty) else stringResource(R.string.rooms_nothing_filter), fontSize = 12.5.sp, color = cs.onSurfaceVariant,
                        textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(30.dp))
                }
            }
        }
        RoomDock(Modifier.align(Alignment.BottomCenter)) {
            Pill(stringResource(R.string.rooms_add_material), { adding = true }, Modifier.weight(1f), icon = Icons.Rounded.Add, big = true)
        }
    }

    if (adding) AddMaterialFlow(room, vm) { adding = false }
    viewing?.let { id -> MaterialViewer(room, id, vm) { viewing = null } }
    when (sheet) {
        "sort" -> RoomSheet({ sheet = null }, stringResource(R.string.rooms_sort)) {
            listOf("new" to R.string.rooms_sort_new, "type" to R.string.rooms_sort_type, "person" to R.string.rooms_sort_person).forEach { (k, l) ->
                SheetRow(stringResource(l), selected = sort == k, onClick = { sort = k; sheet = null })
            }
        }
        "bib" -> BibliographySheet(room, vm) { sheet = null }
    }
    @Suppress("UNUSED_EXPRESSION") you
    @Suppress("UNUSED_EXPRESSION") go
}

/** `.chM`: chip de filtro con su número. */
@Composable
private fun MatChip(text: String, count: Int, on: Boolean, tinted: Boolean = false, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val bg = when { on -> cs.primary; tinted -> mix(cs.primary, 0.16f, cs.surfaceContainer); else -> cs.surfaceContainer }
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(bg).cleanClickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else cs.onSurface, maxLines = 1)
        Text("$count", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary.copy(alpha = 0.8f) else cs.onSurfaceVariant)
    }
}

/** `.avisoM`: «Sam te etiquetó en…» con la cara y un distintivo del tipo. */
@Composable
private fun NoticeRow(room: WorkRoom, m: RoomMaterial, onOpen: () -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).cleanClickable(onClick = onOpen).padding(start = 10.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
        Box {
            MemberFace(room, m.byId, 34.dp)
            Box(Modifier.align(Alignment.BottomEnd).offset(5.dp, 5.dp).size(20.dp).clip(RoundedCornerShape(7.dp)).background(cs.surfaceContainerLow).padding(2.dp)
                .clip(RoundedCornerShape(5.dp)).background(mix(m.type.tone.color, 0.3f, cs.surfaceContainerHigh)), contentAlignment = Alignment.Center) {
                Icon(materialIcon(m.type), null, tint = m.type.tone.color, modifier = Modifier.size(11.dp))
            }
        }
        val tagged = room.meId in m.tagged
        Text(buildAnnotatedString {
            if (tagged) {
                withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(room.nameOf(m.byId)) }
                append(" " + LocalContext.current.getString(R.string.rooms_tagged_you_in) + " ")
                withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(m.name) }
            } else {
                append(LocalContext.current.getString(R.string.rooms_new_for) + " ")
                withStyle(SpanStyle(color = cs.onSurface, fontWeight = FontWeight.Bold)) { append(room.part(m.partId)?.name.orEmpty()) }
                append(": ${m.name}")
            }
        }, fontSize = 13.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp, modifier = Modifier.weight(1f))
        Box(Modifier.size(30.dp).clip(CircleShape).cleanClickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.Close, stringResource(R.string.rooms_dismiss), tint = cs.onSurfaceVariant, modifier = Modifier.size(16.dp))
        }
    }
}

/** La fila del material: mosaico del color del tipo, título, cara y «cuándo · parte», PARA TI o el punto de nuevo. */
@Composable
fun MaterialRow(room: WorkRoom, m: RoomMaterial, shape: androidx.compose.ui.graphics.Shape, today: Long, forMeFilter: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomRow(shape, minHeight = 62.dp, padding = PaddingValues(start = 13.dp, end = 12.dp, top = 12.dp, bottom = 12.dp), onClick = onClick) {
        Tile(materialIcon(m.type), m.type.tone.color)
        Column(Modifier.weight(1f)) {
            Text((if (m.author.isNotBlank()) "${m.author} — " else "") + m.name, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(Modifier.padding(top = 1.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                MemberFace(room, m.byId, 20.dp)
                Text(listOfNotNull(relDay(epochDayOf(m.createdAt) - today), room.part(m.partId)?.name).joinToString(" · "), fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        when {
            RoomLogic.forMe(room, m) && !forMeFilter -> Text(stringResource(R.string.rooms_for_you_chip), fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.3.sp,
                color = cs.onPrimaryContainer, modifier = Modifier.clip(CircleShape).background(cs.primaryContainer).padding(horizontal = 9.dp, vertical = 4.dp))
            room.meId !in m.seenBy -> Dot(cs.primary)
        }
        Icon(Icons.Rounded.ArrowDropDown, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(20.dp).rotate(-90f))
    }
}

// ---------------------------------------------------------------- bibliografía

@Composable
fun BibliographySheet(room: WorkRoom, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val sources = room.materials.filter { it.type == MaterialType.SOURCE }.sortedBy { it.author.lowercase() }
    val copied = stringResource(R.string.rooms_bib_copied)
    RoomSheet(onDismiss, stringResource(R.string.rooms_bibliography), stringResource(R.string.rooms_bib_sheet_d)) {
        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(cs.surfaceContainerLow).padding(14.dp)) {
            Row(Modifier.padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(pluralText(R.plurals.rooms_bib_builds_n, sources.size), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Row(Modifier.clip(CircleShape).background(cs.background).padding(3.dp)) {
                    listOf("APA", "MLA").forEach { st ->
                        val on = room.bibliographyStyle == st
                        Text(st, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (on) cs.onPrimary else cs.onSurfaceVariant,
                            modifier = Modifier.clip(CircleShape).background(if (on) cs.primary else Color.Transparent).cleanClickable { vm.setBibliographyStyle(room.id, st) }.padding(horizontal = 12.dp, vertical = 6.dp))
                    }
                }
            }
            sources.forEach { m ->
                Text(citation(m, room.bibliographyStyle), fontFamily = FontFamily.Serif, fontSize = 13.sp, lineHeight = 20.sp, color = cs.onSurface,
                    modifier = Modifier.padding(bottom = 8.dp, start = 18.dp).offset(x = (-18).dp).fillMaxWidth())
            }
            Row(Modifier.padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.rooms_bib_pasted), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
                Pill(stringResource(R.string.rooms_copy), {
                    context.copyText("bib", RoomLogic.bibliography(room).joinToString("\n")); context.roomToast(copied)
                }, icon = Icons.Rounded.ContentCopy, style = PillStyle.TONAL, small = true)
            }
        }
    }
}

fun citation(m: RoomMaterial, style: String) = buildAnnotatedString {
    val year = m.year.ifBlank { "s. f." }
    if (style == "MLA") {
        append("${m.author} "); withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.name) }; append(". ${m.publisher}, $year.")
    } else {
        append("${m.author} ($year). "); withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(m.name) }; append(". ${m.publisher}.")
    }
}

// ---------------------------------------------------------------- el visor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MaterialViewer(room: WorkRoom, id: String, vm: RoomsViewModel, onDismiss: () -> Unit) {
    val m = room.materials.firstOrNull { it.id == id } ?: return onDismiss()
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val today = vm.today()
    var sub by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(id) { vm.seeMaterial(room.id, id) }
    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(m.mime ?: "*/*")) { uri ->
        val f = m.file
        if (uri != null && f != null) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { out -> vm.files.file(f).inputStream().use { it.copyTo(out) } } }
            context.roomToast(context.getString(R.string.rooms_downloaded))
        }
    }
    if (sub == "part") {
        RoomSheet({ sub = null }, stringResource(R.string.rooms_which_part_q), stringResource(R.string.rooms_which_part_d)) {
            (listOf<String?>(null) + room.parts.map { it.id }).forEach { pid ->
                SheetRow(room.part(pid)?.name ?: stringResource(R.string.rooms_whole_work), selected = m.partId == pid, onClick = { vm.updateMaterial(room.id, m.id) { it.copy(partId = pid) }; sub = null })
            }
        }
        return
    }
    if (sub == "tag") {
        var sel by remember { mutableStateOf(m.tagged) }
        RoomSheet({ sub = null }, stringResource(R.string.rooms_who_helps_q), stringResource(R.string.rooms_notify_d)) {
            room.activeMembers.filter { it.id != m.byId }.forEach { mem ->
                SheetRow(whoText(room, mem.id, stringResource(R.string.rooms_you)), selected = mem.id in sel, leading = { MemberFace(room, mem.id, 20.dp) },
                    onClick = { sel = if (mem.id in sel) sel - mem.id else sel + mem.id })
            }
            Pill(stringResource(R.string.rooms_done), {
                val added = sel - m.tagged.toSet()
                vm.update(room.id) { r -> RoomLogic.updateMaterial(r, m.id) { it.copy(tagged = sel) }.let { rr -> rr.copy(events = rr.events + added.map { RoomLogic.event(com.unistack.app.feature_rooms.domain.EventType.TAGGED, rr.meId, m.name, now = System.currentTimeMillis()) }) } }
                sub = null
            }, Modifier.fillMaxWidth().padding(top = 2.dp))
        }
        return
    }
    val typeName = stringResource(materialName(m.type))
    RoomSheet(onDismiss, (if (m.author.isNotBlank()) "${m.author} — " else "") + m.name,
        "$typeName · ${whoText(room, m.byId, stringResource(R.string.rooms_you))} · ${relDay(epochDayOf(m.createdAt) - today)}", tall = true) {
        MaterialPreview(m, vm)
        if (m.note.isNotBlank() && m.type != MaterialType.NOTE) Text(buildAnnotatedString {
            append("«${m.note}» ")
            withStyle(SpanStyle(fontStyle = FontStyle.Normal, color = cs.onSurfaceVariant, fontSize = 12.sp)) { append("— " + room.nameOf(m.byId)) }
        }, fontSize = 13.5.sp, fontStyle = FontStyle.Italic, color = cs.onSurface, lineHeight = 20.sp, modifier = Modifier.padding(top = 12.dp))
        ViewerLine(stringResource(R.string.rooms_for_part)) {
            MatChip2((room.part(m.partId)?.name ?: stringResource(R.string.rooms_whole_work)) + " ▾") { sub = "part" }
        }
        ViewerLine(stringResource(R.string.rooms_helps)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                m.tagged.forEach { t ->
                    Row(Modifier.clip(CircleShape).background(cs.surfaceContainerLow).padding(start = 3.dp, end = 10.dp, top = 3.dp, bottom = 3.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        MemberFace(room, t, 20.dp)
                        Text(if (t == room.meId) stringResource(R.string.rooms_you_obj_cap) else room.nameOf(t), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    }
                }
                MatChip2(stringResource(R.string.rooms_tag)) { sub = "tag" }
            }
        }
        ViewerLine(stringResource(R.string.rooms_reactions)) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                m.reactions.filterValues { it.isNotEmpty() }.forEach { (e, who) ->
                    val yo = room.meId in who
                    Row(Modifier.clip(CircleShape).background(if (yo) mix(cs.primary, 0.22f, cs.surfaceContainer) else cs.surfaceContainer)
                        .then(if (yo) Modifier.border2(cs.primary) else Modifier).cleanClickable { vm.reactMaterial(room.id, m.id, e) }.padding(horizontal = 8.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text(e, fontSize = 12.sp); Text("${who.size}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface) }
                }
                REACTION_EMOJIS.filter { m.reactions[it].isNullOrEmpty() }.take(3).forEach { e ->
                    Text(e, fontSize = 12.sp, modifier = Modifier.clip(CircleShape).background(cs.surfaceContainer).cleanClickable { vm.reactMaterial(room.id, m.id, e) }.padding(horizontal = 8.dp, vertical = 3.dp))
                }
            }
        }
        ViewerLine(stringResource(R.string.rooms_seen_by)) {
            FaceStack(room, m.seenBy.mapNotNull { room.member(it) }, cs.background, size = 20.dp, max = 6)
            Text(stringResource(R.string.rooms_x_of_y, m.seenBy.size, room.activeMembers.size), fontSize = 12.sp, color = cs.onSurfaceVariant)
        }
        RoomLabel(stringResource(R.string.rooms_comments), trailing = "${m.comments.size}")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            m.comments.forEach { c ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MemberFace(room, c.byId, 20.dp)
                    Text(buildAnnotatedString { withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(room.nameOf(c.byId)) }; append(" ${c.text}") },
                        fontSize = 12.5.sp, color = cs.onSurface, lineHeight = 18.sp, modifier = Modifier.weight(1f))
                }
            }
            CommentInput(stringResource(R.string.rooms_comment_hint2)) { vm.commentMaterial(room.id, m.id, it) }
        }
        FlowRow(Modifier.padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (m.file != null) Pill(stringResource(R.string.rooms_download), { save.launch(m.name) }, icon = Icons.Rounded.Download, style = PillStyle.TONAL, small = true)
            if (m.url.isNotBlank()) Pill(stringResource(R.string.rooms_open), { context.openUrl(m.url) }, icon = Icons.Rounded.OpenInNew, style = PillStyle.TONAL, small = true)
            if (room.isLeader) Pill(stringResource(if (m.pinned) R.string.rooms_unpin else R.string.rooms_pin), { vm.updateMaterial(room.id, m.id) { it.copy(pinned = !it.pinned) } },
                icon = Icons.Rounded.PushPin, style = PillStyle.TONAL, small = true)
            if (m.byId == room.meId || room.isLeader) Pill(stringResource(R.string.rooms_delete), {
                m.file?.let { vm.files.delete(it) }; vm.deleteMaterial(room.id, m.id); onDismiss()
            }, icon = Icons.Rounded.Delete, style = PillStyle.ERR, small = true)
        }
    }
}

private fun Modifier.border2(c: Color) = this.border(1.5.dp, c, CircleShape)

@Composable
private fun MatChip2(text: String, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(text, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1,
        modifier = Modifier.clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainer).cleanClickable(onClick = onClick).padding(horizontal = 11.dp, vertical = 8.dp))
}

/** `.vFila`: rótulo de 84 a la izquierda y el valor al lado. */
@Composable
private fun ViewerLine(label: String, content: @Composable () -> Unit) {
    Row(Modifier.padding(top = 12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(84.dp))
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { content() }
    }
}

/** Lo de arriba del visor: la foto, la primera página del PDF, el audio que suena… */
@Composable
fun MaterialPreview(m: RoomMaterial, vm: RoomsViewModel) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val open = { m.file?.let { context.openRoomFile(vm, it, m.mime, m.name) } ?: if (m.url.isNotBlank()) context.openUrl(m.url) else Unit }
    when (m.type) {
        MaterialType.PHOTO -> Box(Modifier.fillMaxWidth().height(210.dp).clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerHigh).cleanClickable { open() }) {
            if (m.file != null) AsyncImage(vm.files.file(m.file), m.name, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else Box(Modifier.fillMaxSize().background(Brush.linearGradient(listOf(mix(RoomTone.ROSA.color, 0.55f, cs.surfaceContainerHigh), mix(RoomTone.VIOLETA.color, 0.45f, cs.surfaceContainerLow)))))
        }
        MaterialType.DOC -> {
            val pdf = m.file != null && m.mime?.contains("pdf") == true
            val page by produceState<Pair<ImageBitmap?, Int>>(null to 0, m.file) {
                if (pdf) value = withContext(Dispatchers.IO) { renderFirstPage(vm.files.file(m.file!!)) }
            }
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerHighest).cleanClickable { open() }.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(Modifier.width(150.dp).aspectRatio(1f / 1.35f).clip(RoundedCornerShape(4.dp)).background(FormatColors.paper)) {
                    val bmp = page.first
                    if (bmp != null) Image(bmp, null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                    else Column(Modifier.padding(horizontal = 12.dp, vertical = 14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(m.name.substringBeforeLast('.'), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = FormatColors.ink, maxLines = 2, modifier = Modifier.padding(bottom = 4.dp))
                        listOf(1f, 1f, 0.7f, 1f, 0.55f).forEach { w -> Box(Modifier.fillMaxWidth(w).height(5.dp).clip(RoundedCornerShape(2.dp)).background(FormatColors.rule)) }
                    }
                }
                Text(if (page.second > 0) stringResource(R.string.rooms_page_1_of, page.second) else m.subtitle, fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
        }
        MaterialType.AUDIO -> AudioPlayerBox(m, vm)
        MaterialType.VIDEO -> Column(Modifier.fillMaxWidth().height(190.dp).clip(RoundedCornerShape(18.dp)).background(FormatColors.qrDark).cleanClickable { open() }.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically)) {
            Box(Modifier.size(52.dp).clip(CircleShape).background(FormatColors.onLogo.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.PlayArrow, null, tint = FormatColors.onLogo, modifier = Modifier.size(26.dp))
            }
            Text(m.name, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = FormatColors.onLogo, textAlign = TextAlign.Center, maxLines = 2)
        }
        MaterialType.LINK -> Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).cleanClickable { open() }) {
            Box(Modifier.fillMaxWidth().height(110.dp).background(Brush.linearGradient(listOf(mix(RoomTone.AZUL.color, 0.4f, cs.surfaceContainerHigh), cs.surfaceContainerLow))))
            Column(Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(m.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                Text(m.url.ifBlank { m.subtitle }, fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        MaterialType.SOURCE -> Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            val cells = listOf(R.string.rooms_author to m.author, R.string.rooms_book_title to m.name, R.string.rooms_year to m.year, R.string.rooms_publisher to m.publisher)
            cells.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { (l, v) ->
                        Column(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainerLow).padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(stringResource(l).uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, letterSpacing = 0.4.sp)
                            Text(v.ifBlank { "—" }, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        }
                    }
                }
            }
        }
        MaterialType.NOTE -> Text(m.note.ifBlank { m.name }, fontFamily = FontFamily.Serif, fontSize = 14.sp, lineHeight = 22.sp, color = cs.onSurface,
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).padding(14.dp))
    }
}

private fun renderFirstPage(file: java.io.File): Pair<ImageBitmap?, Int> = runCatching {
    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { r ->
            val count = r.pageCount
            r.openPage(0).use { p ->
                val bmp = Bitmap.createBitmap(p.width * 2, p.height * 2, Bitmap.Config.ARGB_8888)
                bmp.eraseColor(android.graphics.Color.WHITE)
                p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bmp.asImageBitmap() to count
            }
        }
    }
}.getOrDefault(null to 0)

/** `.vAud`: botón de reproducir, la onda y lo que dura; suena de verdad. */
@Composable
fun AudioPlayerBox(m: RoomMaterial, vm: RoomsViewModel) {
    val cs = MaterialTheme.colorScheme
    var playing by remember { mutableStateOf(false) }
    val player = remember(m.file) { MediaPlayer() }
    val duration by produceState(0, m.file) {
        value = withContext(Dispatchers.IO) {
            runCatching { MediaMetadataRetriever().run { setDataSource(vm.files.file(m.file!!).path); val d = extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toInt() ?: 0; release(); d } }.getOrDefault(0)
        }
    }
    DisposableEffect(m.file) { onDispose { runCatching { player.release() } } }
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.surfaceContainerLow).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Box(Modifier.size(42.dp).clip(CircleShape).background(cs.primary).cleanClickable {
            val f = m.file ?: return@cleanClickable
            runCatching {
                if (playing) { player.pause(); playing = false }
                else {
                    if (player.currentPosition == 0) { player.reset(); player.setDataSource(vm.files.file(f).path); player.prepare() }
                    player.setOnCompletionListener { playing = false; it.seekTo(0) }
                    player.start(); playing = true
                }
            }
        }, contentAlignment = Alignment.Center) { Icon(if (playing) Icons.Rounded.Pause else Icons.Rounded.PlayArrow, null, tint = cs.onPrimary, modifier = Modifier.size(20.dp)) }
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
            listOf(6, 12, 18, 9, 14, 20, 11, 7, 15, 10, 17, 8, 13, 6, 16).forEach { h -> Box(Modifier.width(3.dp).height(h.dp).clip(RoundedCornerShape(2.dp)).background(cs.primary.copy(alpha = 0.7f))) }
        }
        Text(if (duration > 0) "%d:%02d".format(duration / 60000, (duration / 1000) % 60) else m.subtitle, fontSize = 12.sp, color = cs.onSurfaceVariant)
    }
}
