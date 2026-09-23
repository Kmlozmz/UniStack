package com.unistack.app.feature_rooms.presentation

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mail
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.AddToDrive
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_rooms.data.RoomStoredFile
import com.unistack.app.feature_rooms.data.WorkDoc
import com.unistack.app.feature_rooms.data.WorkExport
import com.unistack.app.feature_rooms.data.WorkSection
import com.unistack.app.feature_rooms.domain.MessageKind
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.WorkRoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/** Los seis formatos, con su logo (letra y color de la marca) y lo que se genera de verdad. */
enum class WorkFormat(val key: String, val ext: String, val mime: String, val letter: String, val title: Int, val desc: Int, val slides: Boolean) {
    DOCX("docx", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "W", R.string.rooms_fmt_word, R.string.rooms_fmt_word_d, false),
    PDF("pdf", "pdf", "application/pdf", "PDF", R.string.rooms_fmt_pdf, R.string.rooms_fmt_pdf_d, false),
    PPTX("pptx", "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "P", R.string.rooms_fmt_ppt, R.string.rooms_fmt_ppt_d, true),
    GDOC("gdoc", "docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "D", R.string.rooms_fmt_gdoc, R.string.rooms_fmt_gdoc_d, false),
    GSLIDES("gslides", "pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "S", R.string.rooms_fmt_gslides, R.string.rooms_fmt_gslides_d, true),
    TXT("txt", "txt", "text/plain", "T", R.string.rooms_fmt_txt, R.string.rooms_fmt_txt_d, false);

    val color: Color get() = when (this) {
        DOCX -> FormatColors.word; PDF -> FormatColors.pdf; PPTX -> FormatColors.ppt
        GDOC -> FormatColors.gdoc; GSLIDES -> FormatColors.gslides; TXT -> FormatColors.text
    }
}

private const val DRIVE = "com.google.android.apps.docs"

/**
 * Crear trabajo — artifact cerrado el 23 sep: una pantalla con tres pasos. Formato → vista previa
 * obligatoria (se aprueba o se vuelve a corregir) → listo, la sala se cierra y se comparte.
 */
@Composable
fun CreateWorkScreen(room: WorkRoom, vm: RoomsViewModel, onBack: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var step by rememberSaveable { mutableStateOf(if (room.isClosed && room.finalFile != null) 3 else 1) }
    var format by rememberSaveable { mutableStateOf(WorkFormat.entries.firstOrNull { it.key == room.finalFormat } ?: if (room.produces == com.unistack.app.feature_rooms.domain.RoomProduce.SLIDES) WorkFormat.PPTX else WorkFormat.DOCX) }
    var pages by remember { mutableStateOf<List<ImageBitmap>?>(null) }
    var plain by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }
    val subjects by vm.subjects.collectAsState()
    val subject = subjects.firstOrNull { it.id == room.subjectId }
    val doc = buildWorkDoc(room, subject?.name, subjectColor(subject))
    val back = { if (step == 2) step = 1 else if (step == 3) onBack() else onBack() }
    BackHandler(onBack = back)

    val save = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(format.mime)) { uri ->
        val f = room.finalFile
        if (uri != null && f != null) {
            runCatching { context.contentResolver.openOutputStream(uri)?.use { out -> vm.files.file(f).inputStream().use { it.copyTo(out) } } }
            vm.addDestination(room.id, "save"); context.roomToast(context.getString(R.string.rooms_saved_downloads))
        }
    }

    LaunchedEffect(step, format) {
        if (step != 2) return@LaunchedEffect
        pages = null; plain = null
        withContext(Dispatchers.IO) {
            val dir = File(context.cacheDir, "exports").apply { mkdirs() }
            if (format == WorkFormat.TXT) {
                val f = File(dir, "preview.txt"); WorkExport.writeText(doc, f); plain = f.readText()
            } else {
                val f = File(dir, "preview.pdf")
                if (format.slides) WorkExport.writePdfSlides(doc, f) else WorkExport.writePdfDocument(doc, f)
                pages = renderPages(f)
            }
        }
    }

    Box(Modifier.fillMaxSize().background(cs.background)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                UniBackButton(onClick = back)
                Column(Modifier.weight(1f).padding(start = 6.dp)) {
                    Text(stringResource(R.string.rooms_create_work), fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(room.title, fontSize = 11.5.sp, color = cs.onSurfaceVariant, maxLines = 1)
                }
            }
            Steps(step)
            when (step) {
                1 -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 110.dp)) {
                    Text(stringResource(R.string.rooms_choose_format_lead), fontSize = 13.sp, color = cs.onSurfaceVariant, lineHeight = 19.sp, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 14.dp))
                    RoomGroup(WorkFormat.entries) { f, shape ->
                        val on = f == format
                        RoomRow(shape, color = if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, onClick = { format = f }) {
                            FormatLogo(f, 36.dp)
                            RowTexts(stringResource(f.title), stringResource(f.desc))
                            RadioDot(on)
                        }
                    }
                }
                2 -> Column(Modifier.weight(1f).padding(bottom = 84.dp)) {
                    val list = rememberLazyListState()
                    val count = pages?.size ?: 1
                    val current = (list.firstVisibleItemIndex + if (list.firstVisibleItemScrollOffset > 200) 1 else 0).coerceAtMost(count - 1)
                    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FormatLogo(format, 24.dp)
                        Text(stringResource(format.title), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                        if (pages != null) Text(stringResource(if (format.slides) R.string.rooms_slide_i_of else R.string.rooms_page_i_of, current + 1, count), fontSize = 12.5.sp, color = cs.onSurfaceVariant)
                        Box(Modifier.weight(1f))
                        Text(stringResource(R.string.rooms_change_format), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.cleanClickable { step = 1 }.padding(vertical = 6.dp, horizontal = 2.dp))
                    }
                    Box(Modifier.weight(1f).fillMaxWidth().background(cs.surfaceContainerHighest)) {
                        val pg = pages
                        when {
                            format == WorkFormat.TXT && plain != null -> Text(plain!!, fontFamily = FontFamily.Monospace, fontSize = 10.5.sp, lineHeight = 17.sp, color = FormatColors.ink,
                                modifier = Modifier.padding(16.dp).fillMaxSize().shadow(8.dp, RoundedCornerShape(4.dp)).clip(RoundedCornerShape(4.dp)).background(FormatColors.paper)
                                    .verticalScroll(rememberScrollState()).padding(18.dp))
                            pg == null -> CircularProgressIndicator(Modifier.align(Alignment.Center), color = cs.primary)
                            else -> LazyColumn(Modifier.fillMaxSize(), state = list, contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp),
                                flingBehavior = rememberSnapFlingBehavior(list)) {
                                itemsIndexed(pg) { _, b ->
                                    Image(b, null, contentScale = ContentScale.FillWidth, modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(if (format.slides) 6.dp else 4.dp))
                                        .clip(RoundedCornerShape(if (format.slides) 6.dp else 4.dp)).background(FormatColors.paper).aspectRatio(b.width.toFloat() / b.height))
                                }
                            }
                        }
                    }
                    if (pages != null && count > 1) Row(Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterHorizontally)) {
                        repeat(count.coerceAtMost(20)) { i ->
                            Box(Modifier.size(if (i == current) 18.dp else 6.dp, 6.dp).clip(RoundedCornerShape(3.dp)).background(if (i == current) cs.primary else cs.outlineVariant)
                                .cleanClickable { scope.launch { list.animateScrollToItem(i) } })
                        }
                    }
                }
                else -> Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 110.dp)) {
                    DoneCard(room, format, doc)
                    RoomLabel(stringResource(R.string.rooms_share))
                    ShareGrid(room, vm, format, onSave = { save.launch(fileTitle(room, format)) })
                    RoomLabel(stringResource(R.string.rooms_also))
                    RoomGroup(listOf(0, 1)) { i, shape ->
                        RoomRow(shape, onClick = {
                            if (i == 0) room.finalFile?.let { context.openRoomFile(vm, it, format.mime, fileTitle(room, format)) }
                            else step = 2
                        }) {
                            Icon(if (i == 0) Icons.Rounded.Description else Icons.Rounded.Edit, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(22.dp))
                            RowTexts(stringResource(if (i == 0) R.string.rooms_open_file else R.string.rooms_back_to_preview))
                        }
                    }
                }
            }
        }
        RoomDock(Modifier.align(Alignment.BottomCenter)) {
            when (step) {
                1 -> Pill(stringResource(R.string.rooms_see_preview), { step = 2 }, Modifier.weight(1f), icon = null, big = true, trailingIcon = Icons.AutoMirrored.Rounded.ArrowForward)
                2 -> {
                    Pill(stringResource(R.string.rooms_fix_something), onBack, Modifier.weight(1f), style = PillStyle.TONAL, big = true)
                    Pill(stringResource(R.string.rooms_approve), {
                        if (busy) return@Pill
                        busy = true
                        scope.launch {
                            val stored = withContext(Dispatchers.IO) { generate(vm, room, doc, format) }
                            busy = false
                            if (stored == null) context.roomToast(context.getString(R.string.rooms_file_error))
                            else { vm.close(room.id, format.key, stored.storedName); step = 3 }
                        }
                    }, Modifier.weight(1f), icon = Icons.Rounded.Check, big = true, enabled = !busy && (pages != null || plain != null))
                }
                else -> Pill(stringResource(R.string.rooms_done_back_room), onBack, Modifier.weight(1f), big = true)
            }
        }
    }
}

fun fileTitle(room: WorkRoom, f: WorkFormat) = room.title.replace(Regex("[\\\\/:*?\"<>|]"), "").trim().ifBlank { "Trabajo" } + "." + f.ext

/** Escribe el archivo de verdad y lo guarda con los de la sala. */
private fun generate(vm: RoomsViewModel, room: WorkRoom, doc: WorkDoc, f: WorkFormat): RoomStoredFile? = runCatching {
    val (name, file) = vm.files.newFileFor(f.ext)
    when (f) {
        WorkFormat.DOCX, WorkFormat.GDOC -> WorkExport.writeDocx(doc, file)
        WorkFormat.PDF -> WorkExport.writePdfDocument(doc, file)
        WorkFormat.PPTX, WorkFormat.GSLIDES -> WorkExport.writePptx(doc, file)
        WorkFormat.TXT -> WorkExport.writeText(doc, file)
    }
    RoomStoredFile(name, fileTitle(room, f), f.mime, file.length())
}.getOrNull()

private fun renderPages(pdf: File): List<ImageBitmap> = runCatching {
    ParcelFileDescriptor.open(pdf, ParcelFileDescriptor.MODE_READ_ONLY).use { fd ->
        PdfRenderer(fd).use { r ->
            (0 until minOf(r.pageCount, 40)).map { i ->
                r.openPage(i).use { p ->
                    val scale = 1080f / p.width
                    val bmp = Bitmap.createBitmap((p.width * scale).toInt(), (p.height * scale).toInt(), Bitmap.Config.ARGB_8888)
                    bmp.eraseColor(android.graphics.Color.WHITE)
                    p.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bmp.asImageBitmap()
                }
            }
        }
    }
}.getOrDefault(emptyList())

/** Junta lo que el grupo entregó: la portada, las partes en orden y la bibliografía. */
@Composable
fun buildWorkDoc(room: WorkRoom, subjectName: String?, accent: Color): WorkDoc {
    val ctx = LocalContext.current
    val type = stringResource(RoomTemplates.typeNameRes(room.type))
    val authors = room.activeMembers.map { room.member(it.id)?.name ?: room.nameOf(it.id) }
    val due = LocalDate.ofEpochDay(room.dueEpochDay).format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
    return WorkDoc(
        title = room.title,
        kicker = listOfNotNull(subjectName, type).joinToString(" · "),
        kind = type,
        authors = authors,
        due = due,
        dueLabel = stringResource(R.string.rooms_due_label),
        sections = room.parts.map { p ->
            val text = p.text.ifBlank { if (p.fileName != null) ctx.getString(R.string.rooms_see_file, p.fileName) else "" }
            WorkSection(p.name, text, p.ownerIds.joinToString(", ") { room.member(it)?.name ?: room.nameOf(it) })
        },
        bibliographyTitle = stringResource(R.string.rooms_bibliography),
        bibliography = RoomLogic.bibliography(room),
        accent = accent.toArgb()
    )
}

@Composable
fun FormatLogo(f: WorkFormat, size: androidx.compose.ui.unit.Dp) {
    val big = size >= 60.dp
    Box(Modifier.size(size).clip(RoundedCornerShape(if (big) 18.dp else if (size < 30.dp) 7.dp else 10.dp)).background(f.color), contentAlignment = Alignment.Center) {
        Text(f.letter, fontSize = if (big) 20.sp else if (size < 30.dp) 9.5.sp else 12.sp, fontWeight = FontWeight.Black, color = FormatColors.onLogo, letterSpacing = (-0.3).sp)
    }
}

/** `.pasos`: Formato — Vista previa — Compartir. */
@Composable
private fun Steps(step: Int) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(R.string.rooms_step_format, R.string.rooms_step_preview, R.string.rooms_step_share).forEachIndexed { i, l ->
            if (i > 0) Box(Modifier.weight(1f).height(2.dp).clip(RoundedCornerShape(1.dp)).background(cs.surfaceContainerHighest))
            val on = step == i + 1
            val done = step > i + 1
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(22.dp).clip(CircleShape).background(when { on -> cs.primary; done -> RoomTone.VERDE.color; else -> cs.surfaceContainerHighest }), contentAlignment = Alignment.Center) {
                    Text(if (done) "✓" else "${i + 1}", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = when { on -> cs.onPrimary; done -> cs.background; else -> cs.onSurfaceVariant })
                }
                Text(stringResource(l), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onSurface else cs.onSurfaceVariant, maxLines = 1)
            }
        }
    }
}

/** `.listo`: el archivo aprobado y el aviso de que la sala quedó cerrada. */
@Composable
private fun DoneCard(room: WorkRoom, f: WorkFormat, doc: WorkDoc) {
    val cs = MaterialTheme.colorScheme
    val detail = when {
        f.slides -> pluralText(R.plurals.rooms_n_slides, doc.sections.size + 1 + if (doc.bibliography.isNotEmpty()) 1 else 0)
        f == WorkFormat.TXT -> pluralText(R.plurals.rooms_n_sections, doc.sections.size)
        else -> pluralText(R.plurals.rooms_n_sections, doc.sections.size)
    }
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(cs.surfaceContainerLow).padding(horizontal = 18.dp, vertical = 22.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        FormatLogo(f, 64.dp)
        Text(fileTitle(room, f), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 12.dp))
        Text("${stringResource(f.title)} · $detail · ${stringResource(R.string.rooms_approved)}", fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 3.dp))
        Row(Modifier.padding(top = 14.dp).clip(CircleShape).background(cs.background).padding(horizontal = 12.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(Icons.Rounded.Lock, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(stringResource(R.string.rooms_room_closed_note), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
        }
    }
}

/** `.shareGrid`: a dónde mandarlo, con ✓ en lo ya compartido. */
@Composable
private fun ShareGrid(room: WorkRoom, vm: RoomsViewModel, f: WorkFormat, onSave: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val context = LocalContext.current
    val file = room.finalFile
    val targets: List<Triple<String, Pair<Int, ImageVector>, () -> Unit>> = listOf(
        Triple("group", R.string.rooms_to_group to RoomIcons.People) {
            if (file != null) {
                val len = vm.files.file(file).length()
                vm.sendFile(room.id, MessageKind.FILE, RoomStoredFile(file, fileTitle(room, f), f.mime, len), fileSubtitle(f.mime, len))
                context.roomToast(context.getString(R.string.rooms_sent_to_group))
            }
        },
        Triple("drive", R.string.rooms_drive to Icons.Rounded.AddToDrive) { file?.let { context.shareRoomFile(vm, it, f.mime, DRIVE) } },
        Triple("canvas", R.string.rooms_canvas to Icons.Rounded.School) { file?.let { context.shareRoomFile(vm, it, f.mime, "com.instructure.candroid") } },
        Triple("classroom", R.string.rooms_classroom to Icons.Rounded.School) { file?.let { context.shareRoomFile(vm, it, f.mime, "com.google.android.apps.classroom") } },
        Triple("teams", R.string.rooms_teams to Icons.AutoMirrored.Rounded.Chat) { file?.let { context.shareRoomFile(vm, it, f.mime, "com.microsoft.teams") } },
        Triple("mail", R.string.rooms_email to Icons.Rounded.Mail) {
            file?.let {
                val uri = vm.files.shareUri(it)
                val send = Intent(Intent.ACTION_SEND).apply {
                    type = f.mime; putExtra(Intent.EXTRA_STREAM, uri); putExtra(Intent.EXTRA_SUBJECT, room.title); addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    selector = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:"))
                }
                runCatching { context.startActivity(send) }.onFailure { _ -> context.shareRoomFile(vm, it, f.mime) }
            }
        },
        Triple("whatsapp", R.string.rooms_whatsapp to Icons.AutoMirrored.Rounded.Send) { file?.let { context.shareRoomFile(vm, it, f.mime, "com.whatsapp") } },
        Triple("save", R.string.rooms_save_phone to Icons.Rounded.Download) { onSave() }
    )
    targets.chunked(4).forEach { row ->
        Row(Modifier.fillMaxWidth().padding(bottom = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            row.forEach { (key, meta, action) ->
                val done = key in room.finalDestinations
                Column(Modifier.weight(1f).cleanClickable { action(); if (key != "save") vm.addDestination(room.id, key) }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box {
                        Box(Modifier.size(54.dp).clip(CircleShape).background(if (done) mix(RoomTone.VERDE.color, 0.18f, cs.surfaceContainer) else cs.surfaceContainer), contentAlignment = Alignment.Center) {
                            Icon(meta.second, null, tint = cs.onSurface, modifier = Modifier.size(23.dp))
                        }
                        if (done) Box(Modifier.align(Alignment.BottomEnd).size(20.dp).clip(CircleShape).background(cs.background).padding(2.dp).clip(CircleShape).background(RoomTone.VERDE.color), contentAlignment = Alignment.Center) {
                            Text("✓", fontSize = 11.sp, fontWeight = FontWeight.Black, color = cs.background)
                        }
                    }
                    Text(stringResource(meta.first), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface, textAlign = TextAlign.Center, maxLines = 2)
                }
            }
        }
    }
}
