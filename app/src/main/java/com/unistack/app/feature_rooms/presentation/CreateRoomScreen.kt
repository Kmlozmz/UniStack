@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.feature_rooms.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Casino
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DoNotDisturbOn
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Photo
import androidx.compose.material.icons.rounded.Rule
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Slideshow
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material.icons.rounded.Videocam
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.R
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.unistack.app.core.utils.performSafely
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniDatePickerDialog
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_rooms.domain.ChangeRule
import com.unistack.app.feature_rooms.domain.LateRule
import com.unistack.app.feature_rooms.domain.MaterialType
import com.unistack.app.feature_rooms.domain.ReminderRule
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.RoomProduce
import com.unistack.app.feature_rooms.domain.RoomRules
import com.unistack.app.feature_rooms.domain.RoomType
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.StructureRule
import com.unistack.app.feature_rooms.domain.VisibilityRule
import com.unistack.app.feature_tasks.presentation.SubjectShapeIcon
import java.time.LocalDate
import kotlin.math.roundToInt

/*
 * Crear la sala — artifact «Crear una sala», cerrado el 23 sep (combinación A + D).
 * Cinco etapas con barra; dentro, preguntas de una en una que avanzan solas al elegir;
 * lo respondido queda arriba como resumen tocable. La app sugiere, el usuario decide.
 */

private sealed interface Step { val stage: Int }
private data class Q(override val stage: Int, val key: String) : Step
private data class Panel(override val stage: Int, val key: String) : Step

private val FLOW = listOf(
    Q(0, "name"), Q(0, "type"), Q(0, "produce"),
    Panel(1, "parts"),
    Q(2, "split"), Q(2, "capacity"), Panel(2, "mine"),
    Q(3, "due"), Panel(3, "dates"),
    Panel(4, "more")
)

@Composable
fun CreateRoomScreen(
    onBackClick: () -> Unit,
    onCreated: (String) -> Unit,
    viewModel: RoomsViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    var d by remember { mutableStateOf(RoomDraft()) }
    var step by remember { mutableIntStateOf(0) }
    var sheet by remember { mutableStateOf<String?>(null) }
    var openPart by remember { mutableStateOf<Long?>(null) }
    var openMore by remember { mutableStateOf<String?>(null) }
    var keySeq by remember { mutableLongStateOf(1L) }
    val today = viewModel.today()

    fun valid(i: Int): Boolean = (FLOW[i] as? Q)?.key != "capacity" || d.split == SplitMode.DRAW
    fun advance() { var n = step + 1; while (n < FLOW.size && !valid(n)) n++; step = n.coerceAtMost(FLOW.lastIndex) }
    fun back() { if (step == 0) onBackClick() else { var n = step - 1; while (n > 0 && !valid(n)) n--; step = n } }
    fun useType(t: RoomType) {
        val parts = RoomTemplates.parts(t).map { (n, e) -> DraftPart(keySeq++, n, e) }
        d = d.copy(type = t, produces = RoomTemplates.produces(t), parts = parts, usedSuggestions = emptySet(),
            tasks = RoomTemplates.tasks(t).map { it to false },
            title = d.title.ifBlank { if (t == RoomType.CERO) "" else RoomTemplates.typeName(t) })
    }
    fun create() { onCreated(viewModel.create(d).id) }

    BackHandler { back() }

    val stages = listOf(R.string.rooms_stage_basic, R.string.rooms_stage_parts, R.string.rooms_stage_split, R.string.rooms_stage_dates, R.string.rooms_stage_more)
    val cur = FLOW[step]
    val cs = MaterialTheme.colorScheme

    Column(Modifier.fillMaxSize().background(cs.background).statusBarsPadding().imePadding()) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UniBackButton(onClick = { back() })
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_new_room), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_stage_of, stringResource(stages[cur.stage]), cur.stage + 1, stages.size), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
            }
        }
        StretchDots(stages.size, cur.stage, Modifier.padding(start = 20.dp, top = 2.dp, bottom = 14.dp))
        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 16.dp, end = 16.dp, bottom = 110.dp)) {
                // Lo respondido en esta etapa se encoge y queda detrás de la pregunta, como una baraja.
                val behind = FLOW.take(step).withIndex().filter { (i, s) -> s is Q && s.stage == cur.stage && valid(i) }
                behind.forEachIndexed { j, (i, s) ->
                    androidx.compose.runtime.key(i) {
                        Behind(questionTitle((s as Q).key), answerOf(s.key, d, subjects, today), depth = behind.size - j, lift = LAP * j) { step = i }
                    }
                }
                when (cur) {
                    is Q -> QuestionCard(cur.key, d, subjects, today,
                        onChange = { d = it },
                        onType = { useType(it); advance() },
                        onPick = { d = it; advance() },
                        onMateria = { sheet = "subject" },
                        onCalendar = { sheet = "calendar" },
                        onNext = { advance() },
                        modifier = if (behind.isEmpty()) Modifier else Modifier.offset(y = -(LAP * behind.size) - 10.dp))
                    is Panel -> when (cur.key) {
                        "parts" -> PartsPanel(d, openPart, onOpen = { openPart = if (openPart == it) null else it }, onChange = { d = it },
                            newKey = { keySeq++ }, today = today)
                        "mine" -> MinePanel(d) { d = it }
                        "dates" -> DatesPanel(d, today) { d = it }
                        else -> MorePanel(d, subjects, today, openMore, onOpen = { openMore = if (openMore == it) null else it }, onChange = { d = it },
                            newKey = { keySeq++ }, onJump = { key -> step = FLOW.indexOfFirst { (it as? Q)?.key == key || (it as? Panel)?.key == key }.coerceAtLeast(0) })
                    }
                }
            }
            // pie
            val q = cur as? Q
            val showNext = cur is Panel || q?.key == "name"
            if (showNext) Row(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth().background(cs.background).navigationBarsPadding().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val panel = (cur as? Panel)?.key
                if (panel == "mine" || panel == "dates") Pill(stringResource(R.string.rooms_create_now), { create() }, Modifier.weight(1f), style = PillStyle.TONAL)
                if (panel == "more") Pill(stringResource(R.string.rooms_create_room_btn), { create() }, Modifier.weight(1f))
                else Pill(stringResource(R.string.rooms_next), { advance() }, Modifier.weight(1f), icon = null,
                    enabled = !(panel == "parts" && d.parts.isEmpty()))
            }
        }
    }

    when (sheet) {
        "subject" -> SubjectSheet(subjects, d.subjectId, onPick = { d = d.copy(subjectId = it); sheet = null }) { sheet = null }
        "calendar" -> UniDatePickerDialog(
            selectedDate = LocalDate.ofEpochDay(today + d.dueInDays),
            onDateSelected = { date ->
                val days = (date.toEpochDay() - today).toInt()
                if (days >= 1) { d = d.copy(dueInDays = days); sheet = null; if ((FLOW[step] as? Q)?.key == "due") advance() } else sheet = null
            },
            onDismiss = { sheet = null }
        )
    }
}

// ---------------------------------------------------------------- preguntas

@Composable
private fun questionTitle(key: String): String = stringResource(
    when (key) {
        "name" -> R.string.rooms_q_name
        "type" -> R.string.rooms_q_type
        "produce" -> R.string.rooms_q_produce
        "split" -> R.string.rooms_q_split
        "capacity" -> R.string.rooms_q_capacity
        else -> R.string.rooms_q_due
    }
)

@Composable
private fun answerOf(key: String, d: RoomDraft, subjects: List<Subject>, today: Long): String = when (key) {
    "name" -> (d.title.ifBlank { "—" }) + (subjects.firstOrNull { it.id == d.subjectId }?.let { " · ${it.name}" } ?: "")
    "type" -> d.type?.let { stringResource(RoomTemplates.typeNameRes(it)) } ?: "—"
    "produce" -> stringResource(produceTitle(d.produces))
    "split" -> stringResource(splitTitle(d.split))
    "capacity" -> if (d.capacity >= 7) stringResource(R.string.rooms_cap_more_long) else "${d.capacity}"
    else -> shortDate(today + d.dueInDays)
}

fun produceTitle(p: RoomProduce) = when (p) {
    RoomProduce.DOC -> R.string.rooms_produce_doc
    RoomProduce.SLIDES -> R.string.rooms_produce_slides
    RoomProduce.BOTH -> R.string.rooms_produce_both
    RoomProduce.NOTHING -> R.string.rooms_produce_nothing
}
private fun produceSub(p: RoomProduce) = when (p) {
    RoomProduce.DOC -> R.string.rooms_produce_doc_d
    RoomProduce.SLIDES -> R.string.rooms_produce_slides_d
    RoomProduce.BOTH -> R.string.rooms_produce_both_d
    RoomProduce.NOTHING -> R.string.rooms_produce_nothing_d
}
private fun produceIcon(p: RoomProduce) = when (p) {
    RoomProduce.DOC -> Icons.Rounded.Description
    RoomProduce.SLIDES -> Icons.Rounded.Slideshow
    RoomProduce.BOTH -> Icons.Rounded.Book
    RoomProduce.NOTHING -> Icons.Rounded.CheckCircle
}
fun splitTitle(s: SplitMode) = when (s) {
    SplitMode.ASSIGN -> R.string.rooms_split_assign
    SplitMode.FREE -> R.string.rooms_split_free_t
    SplitMode.DRAW -> R.string.rooms_split_draw
    SplitMode.MIXED -> R.string.rooms_split_mixed
}
private fun splitSub(s: SplitMode) = when (s) {
    SplitMode.ASSIGN -> R.string.rooms_split_assign_d
    SplitMode.FREE -> R.string.rooms_split_free_d
    SplitMode.DRAW -> R.string.rooms_split_draw_d
    SplitMode.MIXED -> R.string.rooms_split_mixed_d
}
@Composable
private fun produceTone(p: RoomProduce) = when (p) {
    RoomProduce.DOC -> RoomTone.INDIGO
    RoomProduce.SLIDES -> RoomTone.NARANJA
    RoomProduce.BOTH -> RoomTone.VIOLETA
    RoomProduce.NOTHING -> RoomTone.VERDE
}.color
@Composable
private fun splitTone(s: SplitMode) = when (s) {
    SplitMode.ASSIGN -> RoomTone.AZUL
    SplitMode.FREE -> RoomTone.VERDE
    SplitMode.DRAW -> RoomTone.AMBAR
    SplitMode.MIXED -> RoomTone.ROSA
}.color
private fun splitIcon(s: SplitMode) = when (s) {
    SplitMode.ASSIGN -> Icons.Rounded.PanTool
    SplitMode.FREE -> Icons.Rounded.Groups
    SplitMode.DRAW -> Icons.Rounded.Casino
    SplitMode.MIXED -> Icons.Rounded.Shuffle
}

/** Lo que tapa la carta de encima: cada respondida asoma por arriba sólo esto. */
private val LAP = 14.dp

/**
 * Una pregunta ya respondida, encogida detrás de la actual: sólo asoma su respuesta. Cuanto más
 * atrás, más estrecha y más oscura; al tocarla vuelve al frente.
 */
@Composable
private fun Behind(q: String, v: String, depth: Int, lift: Dp, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val seen = remember { MutableTransitionState(false).apply { targetState = true } }
    AnimatedVisibility(seen, enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn()) {
        Row(
            Modifier.offset(y = -lift).padding(horizontal = 8.dp * depth.coerceAtMost(3)).fillMaxWidth()
                .clip(RoundedCornerShape(22.dp)).background(mix(cs.background, (0.22f * depth).coerceAtMost(0.55f), cs.primaryContainer))
                .cleanClickable { haptic.performSafely(HapticFeedbackType.SegmentTick); onClick() }
                .padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 10.dp + LAP),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(q, fontSize = 12.sp, color = cs.onSurface.copy(alpha = 0.6f), modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(v, fontSize = 13.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuestionCard(
    key: String, d: RoomDraft, subjects: List<Subject>, today: Long,
    onChange: (RoomDraft) -> Unit, onType: (RoomType) -> Unit, onPick: (RoomDraft) -> Unit,
    onMateria: () -> Unit, onCalendar: () -> Unit, onNext: () -> Unit, modifier: Modifier = Modifier
) {
    val cs = MaterialTheme.colorScheme
    val on = cs.onSurface
    val tile = mix(on, 0.06f, cs.primaryContainer)
    val haptic = LocalHapticFeedback.current
    Column(modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(cs.primaryContainer).padding(horizontal = 16.dp, vertical = 18.dp)) {
        Text(questionTitle(key), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = on, lineHeight = 24.sp)
        val sub = when (key) {
            "name" -> R.string.rooms_q_name_d
            "type" -> R.string.rooms_q_type_d
            "produce" -> R.string.rooms_q_produce_d
            "split" -> R.string.rooms_q_split_d
            "capacity" -> R.string.rooms_q_capacity_d
            else -> null
        }
        if (sub != null) Text(stringResource(sub), fontSize = 12.5.sp, color = on.copy(alpha = 0.85f), lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)) else VSpace(10.dp)
        when (key) {
            "name" -> {
                Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(mix(on, 0.10f, cs.primaryContainer)).padding(horizontal = 13.dp, vertical = 11.dp)) {
                    if (d.title.isEmpty()) Text(stringResource(R.string.rooms_name_hint), color = on.copy(alpha = 0.5f), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    BasicTextField(d.title, { onChange(d.copy(title = it)) }, singleLine = true, cursorBrush = SolidColor(on),
                        textStyle = TextStyle(color = on, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold), modifier = Modifier.fillMaxWidth())
                }
                VSpace(10.dp)
                val s = subjects.firstOrNull { it.id == d.subjectId }
                if (s != null) Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(mix(on, 0.14f, cs.primaryContainer)).cleanClickable(onClick = onMateria).padding(start = 12.dp, end = 6.dp, top = 6.dp, bottom = 6.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubjectShapeIcon(subjectColor(s), s.id, size = 14.dp)
                    Text(s.name, color = on, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Box(Modifier.size(22.dp).clip(CircleShape).cleanClickable { onChange(d.copy(subjectId = null)) }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, null, tint = on, modifier = Modifier.size(15.dp)) }
                } else Row(
                    Modifier.clip(RoundedCornerShape(12.dp)).border(1.5.dp, on.copy(alpha = 0.4f), RoundedCornerShape(12.dp)).cleanClickable(onClick = onMateria).padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Add, null, tint = on, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.rooms_add_subject), color = on, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
            "type" -> {
                val rows = RoomTemplates.order.chunked(3)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    rows.forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            row.forEach { t ->
                                val sel = d.type == t
                                val tone = RoomTemplates.tone(t)
                                // Todas iguales: alto fijo y el nombre en dos líneas como mucho.
                                Column(
                                    Modifier.weight(1f).height(112.dp).clip(RoundedCornerShape(18.dp)).background(if (sel) mix(tone, 0.20f, tile) else tile)
                                        .then(if (sel) Modifier.border(2.dp, tone, RoundedCornerShape(18.dp)) else Modifier)
                                        .cleanClickable { haptic.performSafely(HapticFeedbackType.SegmentTick); onType(t) }.padding(horizontal = 8.dp, vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                                ) {
                                    Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(if (sel) tone else mix(tone, 0.22f, cs.surfaceContainerHigh)), contentAlignment = Alignment.Center) {
                                        Icon(RoomTemplates.icon(t), null, tint = if (sel) cs.background else tone, modifier = Modifier.size(21.dp))
                                    }
                                    Text(stringResource(RoomTemplates.typeNameRes(t)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = on, textAlign = TextAlign.Center,
                                        lineHeight = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
                Text(stringResource(R.string.rooms_type_none), color = on.copy(alpha = 0.8f), fontSize = 12.5.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 10.dp).cleanClickable { haptic.performSafely(HapticFeedbackType.SegmentTick); onType(RoomType.CERO) }.padding(vertical = 6.dp, horizontal = 2.dp))
            }
            "produce" -> OptionRows(RoomProduce.entries, { "produce" in d.answered && it == d.produces }, tile, { produceIcon(it) }, { produceTone(it) },
                { stringResource(produceTitle(it)) }, { stringResource(produceSub(it)) }) { onPick(d.copy(produces = it, answered = d.answered + "produce")) }
            "split" -> OptionRows(SplitMode.entries, { "split" in d.answered && it == d.split }, tile, { splitIcon(it) }, { splitTone(it) },
                { stringResource(splitTitle(it)) }, { stringResource(splitSub(it)) }) { onPick(d.copy(split = it, answered = d.answered + "split")) }
            "capacity" -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                (2..7).forEach { n ->
                    HeroChip(if (n == 7) stringResource(R.string.rooms_cap_more) else "$n", "capacity" in d.answered && d.capacity == n, big = true) {
                        onPick(d.copy(capacity = n, answered = d.answered + "capacity"))
                    }
                }
            }
            else -> FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(7 to R.string.rooms_due_1w, 14 to R.string.rooms_due_2w, 21 to R.string.rooms_due_3w, 30 to R.string.rooms_due_1m).forEach { (n, r) ->
                    HeroChip(stringResource(r), d.dueInDays == n) { onPick(d.copy(dueInDays = n)) }
                }
                val other = d.dueInDays !in listOf(7, 14, 21, 30)
                HeroChip(if (other) shortDate(today + d.dueInDays) else stringResource(R.string.rooms_pick_date), other, icon = Icons.Rounded.CalendarMonth, onClick = onCalendar)
            }
        }
    }
}

@Composable
private fun HeroChip(text: String, on: Boolean, big: Boolean = false, icon: ImageVector? = null, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val fg = cs.onSurface
    Row(
        Modifier.clip(RoundedCornerShape(10.dp)).background(if (on) cs.primary else mix(fg, 0.10f, cs.primaryContainer)).cleanClickable { haptic.performSafely(HapticFeedbackType.SegmentTick); onClick() }
            .heightIn(min = if (big) 44.dp else 36.dp).padding(horizontal = if (big) 16.dp else 11.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        if (icon != null) Icon(icon, null, tint = if (on) cs.onPrimary else fg, modifier = Modifier.size(15.dp))
        Text(text, fontSize = if (big) 15.sp else 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else fg)
    }
}

@Composable
private fun <T> OptionRows(items: List<T>, sel: (T) -> Boolean, tile: Color, icon: (T) -> ImageVector, tone: @Composable (T) -> Color, title: @Composable (T) -> String, sub: @Composable (T) -> String, onPick: (T) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val on = cs.onSurface
    val haptic = LocalHapticFeedback.current
    RoomGroup(items) { it, shape ->
        val s = sel(it)
        val c = tone(it)
        RoomRow(shape, color = if (s) mix(c, 0.16f, tile) else tile, onClick = { haptic.performSafely(HapticFeedbackType.SegmentTick); onPick(it) }) {
            Box(Modifier.size(38.dp).clip(RoundedCornerShape(12.dp)).background(if (s) c else mix(c, 0.22f, cs.surfaceContainerHigh)), contentAlignment = Alignment.Center) {
                Icon(icon(it), null, tint = if (s) cs.background else c, modifier = Modifier.size(20.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title(it), fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = on)
                Text(sub(it), fontSize = 12.sp, color = on.copy(alpha = 0.75f), lineHeight = 16.sp)
            }
            RadioDot(s)
        }
    }
}

// ---------------------------------------------------------------- las partes

@Composable
private fun PartsPanel(d: RoomDraft, open: Long?, onOpen: (Long) -> Unit, onChange: (RoomDraft) -> Unit, newKey: () -> Long, today: Long) {
    val cs = MaterialTheme.colorScheme
    val unit = unitName(d.produces)
    Text(stringResource(R.string.rooms_parts_title), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp))
    Text((d.type?.takeIf { it != RoomType.CERO }?.let { stringResource(R.string.rooms_parts_hint_type, stringResource(RoomTemplates.typeNameRes(it)).lowercase()) } ?: "") + stringResource(R.string.rooms_parts_hint),
        fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp), lineHeight = 18.sp)
    val total = d.parts.sumOf { it.extent }
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 12.dp)) {
        SummaryChip("${d.parts.size}", stringResource(R.string.rooms_parts_word))
        if (d.produces != RoomProduce.NOTHING && total > 0) SummaryChip("~$total", stringResource(R.string.rooms_parts_total, unit))
        val sh = d.parts.count { it.sharers != 1 }
        if (sh > 0) SummaryChip("$sh", stringResource(R.string.rooms_parts_shared))
    }
    ReorderableParts(d, open, onOpen, onChange, unit)
    if (d.parts.isEmpty()) Text(stringResource(R.string.rooms_parts_empty), fontSize = 12.5.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(18.dp))
    Row(
        Modifier.padding(top = 8.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).border(1.5.dp, cs.outlineVariant, RoundedCornerShape(20.dp))
            .cleanClickable { val k = newKey(); onChange(d.copy(parts = d.parts + DraftPart(k, ""))); onOpen(k) }.padding(15.dp),
        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.Add, null, tint = cs.primary, modifier = Modifier.size(18.dp)); HSpace(7.dp)
        Text(stringResource(R.string.rooms_add_part), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
    }
    val t = d.type ?: RoomType.CERO
    val sug = RoomTemplates.suggestions(t).filter { it !in d.usedSuggestions && d.parts.none { p -> p.name == it } }
    if (sug.isNotEmpty()) {
        Text(stringResource(R.string.rooms_suggestions, stringResource(RoomTemplates.typeNameRes(t)).lowercase()), fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, top = 14.dp))
        SuggestionChips(sug) { s -> onChange(d.copy(parts = d.parts + DraftPart(newKey(), s), usedSuggestions = d.usedSuggestions + s)) }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SuggestionChips(items: List<String>, onPick: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    FlowRow(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { s ->
            Row(
                Modifier.clip(RoundedCornerShape(10.dp)).border(1.5.dp, cs.outlineVariant, RoundedCornerShape(10.dp)).cleanClickable { onPick(s) }.padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Rounded.Add, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
                Text(s, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SummaryChip(b: String, t: String) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainerLow).padding(horizontal = 10.dp, vertical = 6.dp)) {
        Text(b, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface); HSpace(4.dp)
        Text(t, fontSize = 12.sp, color = cs.onSurfaceVariant)
    }
}

@Composable
fun unitName(p: RoomProduce): String = stringResource(if (p == RoomProduce.SLIDES) R.string.rooms_unit_slide else R.string.rooms_unit_page)

/** Tarjetas de parte con asa: se arrastran para ordenar; al tocar se abre su editor. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReorderableParts(d: RoomDraft, open: Long?, onOpen: (Long) -> Unit, onChange: (RoomDraft) -> Unit, unit: String) {
    val cs = MaterialTheme.colorScheme
    var dragKey by remember { mutableStateOf<Long?>(null) }
    var dragDy by remember { mutableFloatStateOf(0f) }
    val heights = remember { mutableMapOf<Long, Int>() }
    val cur by androidx.compose.runtime.rememberUpdatedState(d)
    val change by androidx.compose.runtime.rememberUpdatedState(onChange)
    val haptic = LocalHapticFeedback.current
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        d.parts.forEachIndexed { i, p -> androidx.compose.runtime.key(p.key) {
            val dragging = dragKey == p.key
            val ab = open == p.key
            val c = partTone(p.key)
            Column(
                Modifier.zIndex(if (dragging) 1f else 0f)
                    .offset { IntOffset(0, if (dragging) dragDy.roundToInt() else 0) }
                    .then(if (dragging) Modifier.shadow(12.dp, RoundedCornerShape(20.dp)) else Modifier)
                    .fillMaxWidth().clip(RoundedCornerShape(20.dp))
                    .background(mix(c, if (dragging) 0.22f else if (ab) 0.16f else 0.11f, if (dragging) cs.surfaceContainerHighest else cs.surfaceContainerLow))
                    .onSizeChanged { heights[p.key] = it.height }
            ) {
                Row(Modifier.fillMaxWidth().heightIn(min = 62.dp).padding(start = 2.dp, end = 6.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.width(34.dp).height(44.dp).pointerInput(p.key) {
                            detectVerticalDragGestures(
                                onDragStart = { dragKey = p.key; dragDy = 0f; haptic.performSafely(HapticFeedbackType.GestureThresholdActivate) },
                                onDragEnd = { dragKey = null; dragDy = 0f },
                                onDragCancel = { dragKey = null; dragDy = 0f },
                                onVerticalDrag = { ch, dy ->
                                    ch.consume()
                                    dragDy += dy
                                    val idx = cur.parts.indexOfFirst { it.key == p.key }
                                    val gap = 6.dp.toPx()
                                    if (dragDy > 0 && idx < cur.parts.lastIndex) {
                                        val h = (heights[cur.parts[idx + 1].key] ?: 0) + gap
                                        if (dragDy > h / 2) { change(cur.copy(parts = cur.parts.toMutableList().apply { add(idx + 1, removeAt(idx)) })); dragDy -= h; haptic.performSafely(HapticFeedbackType.SegmentFrequentTick) }
                                    } else if (dragDy < 0 && idx > 0) {
                                        val h = (heights[cur.parts[idx - 1].key] ?: 0) + gap
                                        if (-dragDy > h / 2) { change(cur.copy(parts = cur.parts.toMutableList().apply { add(idx - 1, removeAt(idx)) })); dragDy += h; haptic.performSafely(HapticFeedbackType.SegmentFrequentTick) }
                                    }
                                }
                            )
                        },
                        contentAlignment = Alignment.Center
                    ) { Icon(Icons.Rounded.DragIndicator, stringResource(R.string.rooms_drag), tint = c.copy(alpha = 0.55f), modifier = Modifier.size(20.dp)) }
                    Row(Modifier.weight(1f).cleanClickable { onOpen(p.key) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                        Box(Modifier.size(36.dp).clip(RoundedCornerShape(11.dp)).background(c), contentAlignment = Alignment.Center) {
                            Text("${i + 1}", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.background)
                        }
                        Column(Modifier.weight(1f)) {
                            Text(p.name.ifBlank { stringResource(R.string.rooms_part_unnamed) }, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            FlowRow(Modifier.padding(top = 5.dp), horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                partTags(p, d.produces, unit).forEach { t ->
                                    Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = c, maxLines = 1,
                                        modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(c.copy(alpha = 0.18f)).padding(horizontal = 7.dp, vertical = 2.dp))
                                }
                            }
                        }
                    }
                    Box(Modifier.size(38.dp).clip(CircleShape).cleanClickable { onOpen(p.key) }, contentAlignment = Alignment.Center) { Chevron(ab) }
                }
                AnimatedVisibility(ab) { PartEditor(p, d, unit, c, onChange) { onOpen(p.key) } }
            }
        } }
    }
}

/** Cada parte con su color, que la sigue al arrastrarla (va por su clave, no por su puesto). */
@Composable
private fun partTone(key: Long): Color = PART_TONES[((key - 1).coerceAtLeast(0) % PART_TONES.size).toInt()].color

private val PART_TONES = listOf(RoomTone.ROSA, RoomTone.AZUL, RoomTone.VERDE, RoomTone.AMBAR, RoomTone.VIOLETA, RoomTone.CIAN, RoomTone.NARANJA, RoomTone.INDIGO)

/** Lo que se ve de la parte sin abrirla: extensión, cuántos la hacen y si es tuya o lleva nota. */
@Composable
private fun partTags(p: DraftPart, produces: RoomProduce, unit: String): List<String> {
    val b = mutableListOf<String>()
    if (produces != RoomProduce.NOTHING && p.extent > 0) b += "~${p.extent} $unit${if (p.extent > 1) "s" else ""}"
    b += stringResource(when (p.sharers) { 2 -> R.string.rooms_two; -1 -> R.string.rooms_all; else -> R.string.rooms_one })
    if (p.mine) b += stringResource(R.string.rooms_part_yours)
    if (p.note.isNotBlank()) b += stringResource(R.string.rooms_part_has_note)
    return b
}

@Composable
private fun PartEditor(p: DraftPart, d: RoomDraft, unit: String, tone: Color, onChange: (RoomDraft) -> Unit, onDone: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    fun set(f: (DraftPart) -> DraftPart) = onChange(d.copy(parts = d.parts.map { if (it.key == p.key) f(it) else it }))
    Column(Modifier.padding(start = 14.dp, end = 14.dp, bottom = 14.dp, top = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.rooms_field_name), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
        Box(Modifier.offset(y = (-6).dp).fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(cs.background).padding(horizontal = 12.dp, vertical = 11.dp)) {
            BasicTextField(p.name, { v -> set { it.copy(name = v) } }, singleLine = true, cursorBrush = SolidColor(cs.primary),
                textStyle = TextStyle(color = cs.onSurface, fontSize = 15.sp, fontWeight = FontWeight.Bold), modifier = Modifier.fillMaxWidth())
        }
        if (d.produces != RoomProduce.NOTHING) Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.rooms_field_extent), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Row(Modifier.clip(CircleShape).background(cs.background).padding(3.dp), verticalAlignment = Alignment.CenterVertically) {
                StepBtn("−", p.extent > 0, tone) { set { it.copy(extent = (it.extent - 1).coerceAtLeast(0)) } }
                Text(if (p.extent > 0) "~${p.extent} $unit${if (p.extent > 1) "s" else ""}" else stringResource(R.string.rooms_extent_free),
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.width(78.dp))
                StepBtn("+", true, tone) { set { it.copy(extent = it.extent + 1) } }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.rooms_field_howmany), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Segmented(listOf(1 to R.string.rooms_one, 2 to R.string.rooms_two, -1 to R.string.rooms_all), p.sharers, tone = tone) { v -> set { it.copy(sharers = v) } }
        }
        if (d.split == SplitMode.ASSIGN || d.split == SplitMode.MIXED) Row(Modifier.cleanClickable { set { it.copy(mine = !it.mine) } }, verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.rooms_field_mine), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Switchy(p.mine, tone)
        }
        Text(stringResource(R.string.rooms_field_note), fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
        Box(Modifier.offset(y = (-6).dp).fillMaxWidth().heightIn(min = 70.dp).clip(RoundedCornerShape(14.dp)).background(cs.background).padding(12.dp)) {
            if (p.note.isEmpty()) Text(stringResource(R.string.rooms_note_hint), color = cs.onSurfaceVariant, fontSize = 13.5.sp)
            BasicTextField(p.note, { v -> set { it.copy(note = v) } }, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 13.5.sp), modifier = Modifier.fillMaxWidth())
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).cleanClickable { onChange(d.copy(parts = d.parts.filterNot { it.key == p.key })) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                Icon(Icons.Rounded.Delete, null, tint = cs.error, modifier = Modifier.size(17.dp))
                Text(stringResource(R.string.rooms_remove_part), color = cs.error, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
            }
            Pill(stringResource(R.string.rooms_done), onDone, small = true)
        }
    }
}

@Composable
fun StepBtn(t: String, enabled: Boolean, tint: Color? = null, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.size(32.dp).clip(CircleShape).background(if (tint != null) mix(tint, 0.26f, cs.surfaceContainer) else cs.surfaceContainer).then(if (enabled) Modifier.cleanClickable(onClick = onClick) else Modifier), contentAlignment = Alignment.Center) {
        Text(t, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = cs.onSurface.copy(alpha = if (enabled) 1f else 0.35f))
    }
}

@Composable
fun <T> Segmented(items: List<Pair<T, Int>>, sel: T, full: Boolean = false, tone: Color? = null, onPick: (T) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    Row(Modifier.then(if (full) Modifier.fillMaxWidth() else Modifier).clip(CircleShape).background(cs.background).padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEach { (v, r) ->
            val on = v == sel
            Text(stringResource(r), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = if (on) (if (tone != null) cs.background else cs.onPrimary) else cs.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1,
                modifier = Modifier.then(if (full) Modifier.weight(1f) else Modifier).clip(CircleShape).background(if (on) tone ?: cs.primary else Color.Transparent).cleanClickable { if (!on) haptic.performSafely(HapticFeedbackType.SegmentTick); onPick(v) }.padding(horizontal = 12.dp, vertical = 7.dp))
        }
    }
}

@Composable
fun Switchy(on: Boolean, tone: Color? = null) {
    val cs = MaterialTheme.colorScheme
    Box(Modifier.width(44.dp).height(26.dp).clip(CircleShape).background(if (on) tone ?: cs.primary else cs.surfaceContainerHighest).padding(4.dp), contentAlignment = if (on) Alignment.CenterEnd else Alignment.CenterStart) {
        Box(Modifier.size(18.dp).clip(CircleShape).background(if (on) (if (tone != null) cs.background else cs.onPrimary) else cs.onSurfaceVariant))
    }
}

// ---------------------------------------------------------------- el reparto

@Composable
private fun MinePanel(d: RoomDraft, onChange: (RoomDraft) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val withMine = d.split == SplitMode.ASSIGN || d.split == SplitMode.MIXED
    Text(stringResource(if (withMine) R.string.rooms_mine_title else if (d.split == SplitMode.DRAW) R.string.rooms_draw_title else R.string.rooms_free_title),
        fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp))
    val cap = if (d.capacity >= 7) "6+" else "${d.capacity}"
    Text(when {
        withMine -> stringResource(R.string.rooms_mine_hint) + " " + stringResource(if (d.split == SplitMode.ASSIGN) R.string.rooms_mine_hint_assign else R.string.rooms_mine_hint_mixed)
        d.split == SplitMode.DRAW -> stringResource(R.string.rooms_draw_hint, cap, d.parts.size)
        else -> stringResource(R.string.rooms_free_hint)
    }, fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp))
    if (withMine) RoomGroup(d.parts) { p, shape ->
        RoomRow(shape, color = if (p.mine) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, onClick = {
            onChange(d.copy(parts = d.parts.map { if (it.key == p.key) it.copy(mine = !it.mine) else it }))
        }) {
            RowTexts(p.name.ifBlank { stringResource(R.string.rooms_part_unnamed) }, when (p.sharers) { 2 -> stringResource(R.string.rooms_shared_two); -1 -> stringResource(R.string.rooms_shared_all); else -> null })
            CheckBox(p.mine)
        }
    } else Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLow).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(if (d.split == SplitMode.DRAW) Icons.Rounded.Casino else Icons.Rounded.Groups, null, tint = cs.primary, modifier = Modifier.size(40.dp))
        Column {
            if (d.split == SplitMode.DRAW) {
                Text(stringResource(R.string.rooms_draw_ilus, d.parts.size, cap), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_draw_each, (d.parts.size + minOf(d.capacity, 6) - 1) / minOf(d.capacity, 6)), fontSize = 12.5.sp, color = cs.onSurfaceVariant)
            } else {
                Text(stringResource(R.string.rooms_free_ilus, d.parts.size), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_free_first), fontSize = 12.5.sp, color = cs.onSurfaceVariant)
            }
        }
    }
}

// ---------------------------------------------------------------- fechas

@Composable
private fun DatesPanel(d: RoomDraft, today: Long, onChange: (RoomDraft) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val due = today + d.dueInDays
    Text(stringResource(R.string.rooms_dates_title), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp))
    Text(stringResource(R.string.rooms_dates_hint), fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp, modifier = Modifier.padding(start = 4.dp, end = 4.dp, bottom = 12.dp))
    RoomRow(RoundedCornerShape(20.dp), onClick = { onChange(d.copy(internalDates = !d.internalDates)) }) {
        RowTexts(stringResource(R.string.rooms_dates_toggle), stringResource(R.string.rooms_dates_toggle_d, shortDate(due)), subtitleLines = 2)
        Switchy(d.internalDates)
    }
    if (d.internalDates && d.parts.isNotEmpty()) {
        RoomLabel(stringResource(R.string.rooms_dates_result))
        val rows = d.parts.mapIndexed { i, p -> Triple("${i + 1}", p.name, shortDate(RoomLogic.internalDue(i, d.parts.size, today, due))) } + Triple("★", stringResource(R.string.rooms_final_due), shortDate(due))
        RoomGroup(rows) { (n, name, date), shape ->
            RoomRow(shape, minHeight = 48.dp) {
                val star = n == "★"
                Box(Modifier.size(26.dp).clip(CircleShape).background(if (star) cs.primary else cs.surfaceContainerHighest), contentAlignment = Alignment.Center) {
                    Text(n, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (star) cs.onPrimary else cs.onSurfaceVariant)
                }
                Text(name.ifBlank { stringResource(R.string.rooms_part_unnamed) }, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, modifier = Modifier.weight(1f))
                Text(date, fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary)
            }
        }
    }
}

// ---------------------------------------------------------------- lo demás

private val MAT_TYPES = listOf(MaterialType.DOC, MaterialType.LINK, MaterialType.PHOTO, MaterialType.VIDEO, MaterialType.AUDIO, MaterialType.NOTE)

fun materialIcon(t: MaterialType): ImageVector = when (t) {
    MaterialType.LINK -> Icons.Rounded.Link
    MaterialType.DOC -> Icons.Rounded.Description
    MaterialType.PHOTO -> Icons.Rounded.Photo
    MaterialType.VIDEO -> Icons.Rounded.Videocam
    MaterialType.AUDIO -> Icons.Rounded.Mic
    MaterialType.SOURCE -> Icons.Rounded.Book
    MaterialType.NOTE -> Icons.Rounded.StickyNote2
}

fun materialName(t: MaterialType): Int = when (t) {
    MaterialType.LINK -> R.string.rooms_mt_link
    MaterialType.DOC -> R.string.rooms_mt_doc
    MaterialType.PHOTO -> R.string.rooms_mt_photo
    MaterialType.VIDEO -> R.string.rooms_mt_video
    MaterialType.AUDIO -> R.string.rooms_mt_audio
    MaterialType.SOURCE -> R.string.rooms_mt_source
    MaterialType.NOTE -> R.string.rooms_mt_note
}

@Composable
private fun MorePanel(d: RoomDraft, subjects: List<Subject>, today: Long, open: String?, onOpen: (String) -> Unit, onChange: (RoomDraft) -> Unit, newKey: () -> Long, onJump: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Text(stringResource(R.string.rooms_preview_title), fontSize = 23.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.padding(start = 4.dp, top = 6.dp, bottom = 4.dp))
    Text(stringResource(R.string.rooms_preview_hint), fontSize = 12.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 4.dp, bottom = 12.dp))
    RoomPreview(d, subjects.firstOrNull { it.id == d.subjectId }, today, onJump)
    RoomLabel(stringResource(R.string.rooms_add_if_you_want))
    val defaults = RoomRules()
    val changed = listOf(d.rules.change != defaults.change, d.rules.late != defaults.late, d.rules.structure != defaults.structure,
        d.rules.visibility != defaults.visibility, d.rules.reminders != defaults.reminders, !d.entryOpen).count { it }
    MoreBlock("tasks", Icons.Rounded.TaskAlt, RoomTone.VERDE.color, stringResource(R.string.rooms_tasks_title),
        if (d.tasks.isEmpty()) stringResource(R.string.rooms_tasks_hint) else d.tasks.joinToString(", ") { it.first }, open == "tasks", { onOpen("tasks") }) {
        TasksEditor(d.tasks) { onChange(d.copy(tasks = it)) }
    }
    MoreBlock("material", Icons.Rounded.AttachFile, RoomTone.INDIGO.color, stringResource(R.string.rooms_material_title),
        if (d.materials.isEmpty()) stringResource(R.string.rooms_material_hint) else stringResource(R.string.rooms_things, d.materials.size), open == "material", { onOpen("material") }) {
        MaterialDraftEditor(d.materials, newKey) { onChange(d.copy(materials = it)) }
    }
    MoreBlock("rules", Icons.Rounded.Rule, RoomTone.VIOLETA.color, stringResource(R.string.rooms_rules_title),
        if (changed == 0) stringResource(R.string.rooms_rules_default) else stringResource(R.string.rooms_rules_changed, changed), open == "rules", { onOpen("rules") }) {
        RulesEditor(d.rules, d.entryOpen, onRules = { onChange(d.copy(rules = it)) }, onEntry = { onChange(d.copy(entryOpen = it)) })
    }
}

@Composable
private fun MoreBlock(key: String, icon: ImageVector, tone: Color, title: String, sub: String, open: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(Modifier.padding(bottom = 8.dp).fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(if (open) cs.surfaceContainerHigh else cs.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().cleanClickable(onClick = onToggle).heightIn(min = 64.dp).padding(start = 14.dp, end = 12.dp, top = 12.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Tile(icon, tone, 36.dp, 11.dp)
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(sub, fontSize = 12.sp, color = cs.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
            }
            Chevron(open)
        }
        AnimatedVisibility(open) { Column(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 14.dp)) { content() } }
    }
}

@Composable
private fun TasksEditor(tasks: List<Pair<String, Boolean>>, onChange: (List<Pair<String, Boolean>>) -> Unit) {
    val cs = MaterialTheme.colorScheme
    var input by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        tasks.forEachIndexed { i, (n, mine) ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.background).padding(start = 10.dp, end = 6.dp, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Tile(Icons.Rounded.TaskAlt, RoomTone.VERDE.color, 36.dp, 11.dp)
                Column(Modifier.weight(1f)) {
                    Text(n, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(stringResource(if (mine) R.string.rooms_task_yours else R.string.rooms_task_later), fontSize = 11.5.sp, color = cs.onSurfaceVariant)
                }
                Text(stringResource(R.string.rooms_me_short), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = if (mine) cs.onPrimary else cs.onSurfaceVariant,
                    modifier = Modifier.clip(CircleShape).background(if (mine) cs.primary else Color.Transparent).border(1.5.dp, if (mine) cs.primary else cs.outlineVariant, CircleShape)
                        .cleanClickable { onChange(tasks.mapIndexed { j, t -> if (j == i) t.first to !t.second else t }) }.padding(horizontal = 11.dp, vertical = 5.dp))
                Box(Modifier.size(32.dp).clip(CircleShape).cleanClickable { onChange(tasks.filterIndexed { j, _ -> j != i }) }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            }
        }
        Row(Modifier.padding(top = 2.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).background(cs.background).padding(horizontal = 12.dp, vertical = 10.dp)) {
                if (input.isEmpty()) Text(stringResource(R.string.rooms_task_hint), color = cs.onSurfaceVariant, fontSize = 13.5.sp, maxLines = 1)
                BasicTextField(input, { input = it }, singleLine = true, cursorBrush = SolidColor(cs.primary), textStyle = TextStyle(color = cs.onSurface, fontSize = 13.5.sp), modifier = Modifier.fillMaxWidth())
            }
            Pill("", { if (input.isNotBlank()) { onChange(tasks + (input.trim() to false)); input = "" } }, icon = Icons.Rounded.Add, small = true)
        }
        val sug = listOf(R.string.rooms_task_spell, R.string.rooms_task_print, R.string.rooms_task_rehearse_short).map { stringResource(it) }.filter { s -> tasks.none { it.first == s } }
        if (sug.isNotEmpty()) SuggestionChips(sug) { onChange(tasks + (it to false)) }
    }
}

@Composable
private fun MaterialDraftEditor(items: List<DraftMaterial>, newKey: () -> Long, onChange: (List<DraftMaterial>) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val samples = mapOf(
        MaterialType.DOC to (R.string.rooms_sample_rubric to R.string.rooms_sample_rubric_d),
        MaterialType.LINK to (R.string.rooms_sample_link to R.string.rooms_sample_link_d),
        MaterialType.PHOTO to (R.string.rooms_sample_photo to R.string.rooms_sample_photo_d),
        MaterialType.VIDEO to (R.string.rooms_sample_video to R.string.rooms_sample_video_d),
        MaterialType.AUDIO to (R.string.rooms_sample_audio to R.string.rooms_sample_audio_d),
        MaterialType.NOTE to (R.string.rooms_sample_note to R.string.rooms_sample_note_d)
    ).mapValues { (_, v) -> stringResource(v.first) to stringResource(v.second) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.forEach { m ->
            Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.background).padding(start = 10.dp, end = 6.dp, top = 9.dp, bottom = 9.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Tile(materialIcon(m.type), m.type.tone.color, 36.dp, 11.dp)
                Column(Modifier.weight(1f)) {
                    Text(m.name, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(m.subtitle, fontSize = 11.5.sp, color = cs.onSurfaceVariant)
                }
                Box(Modifier.size(32.dp).clip(CircleShape).cleanClickable { onChange(items.filterNot { it.key == m.key }) }, contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Close, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(18.dp)) }
            }
        }
        MAT_TYPES.chunked(3).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { t ->
                    Column(Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(cs.background).cleanClickable {
                        val (n, s) = samples.getValue(t); onChange(items + DraftMaterial(newKey(), t, n, s))
                    }.padding(vertical = 12.dp, horizontal = 6.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        Tile(materialIcon(t), t.tone.color, 36.dp, 11.dp)
                        Text(stringResource(materialName(t)), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = cs.onSurface)
                    }
                }
            }
        }
        Text(stringResource(R.string.rooms_material_create_hint), fontSize = 12.5.sp, color = cs.onSurfaceVariant, lineHeight = 18.sp, modifier = Modifier.padding(top = 4.dp, start = 2.dp, end = 2.dp))
    }
}

@Composable
fun RulesEditor(rules: RoomRules, entryOpen: Boolean, onRules: (RoomRules) -> Unit, onEntry: ((Boolean) -> Unit)?) {
    val cs = MaterialTheme.colorScheme
    @Composable
    fun <T> Rule(icon: ImageVector, title: Int, value: T, opts: List<Pair<T, Int>>, desc: Map<T, Int>, onPick: (T) -> Unit) {
        Column(Modifier.padding(bottom = 6.dp).fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(cs.background).padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(bottom = 10.dp)) {
                Tile(icon, cs.primary, 36.dp, 11.dp)
                Column(Modifier.weight(1f)) {
                    Text(stringResource(title), fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                    Text(stringResource(desc.getValue(value)), fontSize = 12.sp, color = cs.onSurfaceVariant, lineHeight = 17.sp, modifier = Modifier.padding(top = 2.dp).heightIn(min = 34.dp))
                }
            }
            Box(Modifier.clip(CircleShape).background(cs.surfaceContainerLow)) { SegmentedFull(opts, value, onPick) }
        }
    }
    Rule(Icons.Rounded.SwapHoriz, R.string.rooms_rule_change, rules.change, listOf(ChangeRule.NO to R.string.rooms_rule_no, ChangeRule.ASK to R.string.rooms_rule_asking, ChangeRule.FREE to R.string.rooms_rule_free),
        mapOf(ChangeRule.NO to R.string.rooms_rule_change_no, ChangeRule.ASK to R.string.rooms_rule_change_ask, ChangeRule.FREE to R.string.rooms_rule_change_free)) { onRules(rules.copy(change = it)) }
    Rule(Icons.Rounded.Schedule, R.string.rooms_rule_late, rules.late, listOf(LateRule.NO to R.string.rooms_rule_no, LateRule.MARKED to R.string.rooms_rule_marked, LateRule.GRACE to R.string.rooms_rule_grace),
        mapOf(LateRule.NO to R.string.rooms_rule_late_no, LateRule.MARKED to R.string.rooms_rule_late_marked, LateRule.GRACE to R.string.rooms_rule_late_grace)) { onRules(rules.copy(late = it)) }
    Rule(Icons.Rounded.Edit, R.string.rooms_rule_structure, rules.structure, listOf(StructureRule.LEADER to R.string.rooms_rule_only_you, StructureRule.ALL to R.string.rooms_rule_everyone),
        mapOf(StructureRule.LEADER to R.string.rooms_rule_structure_leader, StructureRule.ALL to R.string.rooms_rule_structure_all)) { onRules(rules.copy(structure = it)) }
    Rule(Icons.Rounded.Visibility, R.string.rooms_rule_visibility, rules.visibility, listOf(VisibilityRule.ALWAYS to R.string.rooms_rule_always, VisibilityRule.ON_SUBMIT to R.string.rooms_rule_on_submit),
        mapOf(VisibilityRule.ALWAYS to R.string.rooms_rule_visibility_always, VisibilityRule.ON_SUBMIT to R.string.rooms_rule_visibility_submit)) { onRules(rules.copy(visibility = it)) }
    Rule(Icons.Rounded.Notifications, R.string.rooms_rule_reminders, rules.reminders, listOf(ReminderRule.NONE to R.string.rooms_rule_no, ReminderRule.ONE_DAY to R.string.rooms_rule_1day, ReminderRule.THREE_DAYS to R.string.rooms_rule_3days),
        mapOf(ReminderRule.NONE to R.string.rooms_rule_rem_none, ReminderRule.ONE_DAY to R.string.rooms_rule_rem_1, ReminderRule.THREE_DAYS to R.string.rooms_rule_rem_3)) { onRules(rules.copy(reminders = it)) }
    if (onEntry != null) Rule(Icons.Rounded.Lock, R.string.rooms_rule_entry, entryOpen, listOf(true to R.string.rooms_rule_with_code, false to R.string.rooms_rule_you_approve),
        mapOf(true to R.string.rooms_rule_entry_code, false to R.string.rooms_rule_entry_approve)) { onEntry(it) }
}

@Composable
private fun <T> SegmentedFull(items: List<Pair<T, Int>>, sel: T, onPick: (T) -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(Modifier.fillMaxWidth().padding(3.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        items.forEach { (v, r) ->
            val on = v == sel
            Text(stringResource(r), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (on) cs.onPrimary else cs.onSurfaceVariant, textAlign = TextAlign.Center, maxLines = 1,
                modifier = Modifier.weight(1f).clip(CircleShape).background(if (on) cs.primary else Color.Transparent).cleanClickable { onPick(v) }.padding(horizontal = 4.dp, vertical = 8.dp))
        }
    }
}

/** «Así queda tu sala»: la tarjeta de la sala, sin degradados. */
@Composable
private fun RoomPreview(d: RoomDraft, s: Subject?, today: Long, onJump: (String) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mc = s?.let { subjectColor(it) } ?: cs.primary
    val t = d.type ?: RoomType.CERO
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(cs.surfaceContainerLow).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(46.dp).clip(RoundedCornerShape(15.dp)).background(mc), contentAlignment = Alignment.Center) { Icon(RoomTemplates.icon(t), null, tint = cs.background, modifier = Modifier.size(24.dp)) }
            Column(Modifier.weight(1f)) {
                Text("${s?.name ?: stringResource(R.string.rooms_no_subject)} · ${stringResource(RoomTemplates.typeNameRes(t))}".uppercase(), fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.4.sp, color = cs.onSurfaceVariant)
                Text(d.title.ifBlank { stringResource(R.string.rooms_untitled) }, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, lineHeight = 23.sp, modifier = Modifier.padding(top = 2.dp))
            }
        }
        SegmentBar(d.parts.map { if (it.mine) cs.primary else cs.surfaceContainerHighest }, Modifier.padding(top = 16.dp, bottom = 10.dp))
        @OptIn(ExperimentalLayoutApi::class)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(5.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            d.parts.take(6).forEach { p ->
                Text(p.name.ifBlank { "—" }, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = if (p.mine) cs.primary else cs.onSurfaceVariant,
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (p.mine) mix(cs.primary, 0.14f, cs.background) else cs.background).padding(horizontal = 8.dp, vertical = 4.dp))
            }
            if (d.parts.size > 6) Text("+${d.parts.size - 6}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant,
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(cs.background).padding(horizontal = 8.dp, vertical = 4.dp))
        }
        Row(Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            StatBox(stringResource(R.string.rooms_stat_due), shortDate(today + d.dueInDays), Modifier.weight(1f)) { onJump("due") }
            StatBox(stringResource(R.string.rooms_stat_split), stringResource(splitTitle(d.split)), Modifier.weight(1f)) { onJump("split") }
            StatBox(stringResource(R.string.rooms_stat_delivers), stringResource(when (d.produces) {
                RoomProduce.DOC -> R.string.rooms_short_doc; RoomProduce.SLIDES -> R.string.rooms_short_slides; RoomProduce.BOTH -> R.string.rooms_short_both; RoomProduce.NOTHING -> R.string.rooms_short_nothing
            }), Modifier.weight(1f)) { onJump("produce") }
        }
        Row(Modifier.padding(top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            val mine = d.parts.count { it.mine }
            Text(listOfNotNull(stringResource(R.string.rooms_n_parts, d.parts.size), if (mine > 0) stringResource(R.string.rooms_n_yours, mine) else null,
                if (d.tasks.isNotEmpty()) stringResource(R.string.rooms_n_tasks, d.tasks.size) else null,
                if (d.materials.isNotEmpty()) stringResource(R.string.rooms_n_material, d.materials.size) else null).joinToString(" · "),
                fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.rooms_edit_parts), fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.primary, modifier = Modifier.cleanClickable { onJump("parts") }.padding(4.dp))
        }
    }
}

@Composable
private fun StatBox(k: String, v: String, modifier: Modifier, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column(modifier.clip(RoundedCornerShape(14.dp)).background(cs.background).cleanClickable(onClick = onClick).padding(10.dp)) {
        Text(k.uppercase(), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.4.sp, color = cs.onSurfaceVariant, maxLines = 1)
        Text(v, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(top = 2.dp))
    }
}

// ---------------------------------------------------------------- materia

@Composable
fun SubjectSheet(subjects: List<Subject>, selected: String?, onPick: (String?) -> Unit, onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomSheet(onDismiss, stringResource(R.string.rooms_add_subject), stringResource(R.string.rooms_subject_sheet_sub)) {
        Column {
            RoomGroup(subjects.map<Subject, Subject?> { it } + listOf<Subject?>(null)) { s, shape ->
                val on = (s?.id) == selected
                RoomRow(shape, color = if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, onClick = { onPick(s?.id) }) {
                    if (s != null) SubjectShapeIcon(subjectColor(s), s.id, size = 20.dp)
                    RowTexts(s?.name ?: stringResource(R.string.rooms_no_subject), if (s == null) stringResource(R.string.rooms_no_subject_d) else null)
                    RadioDot(on)
                }
            }
        }
    }
}

