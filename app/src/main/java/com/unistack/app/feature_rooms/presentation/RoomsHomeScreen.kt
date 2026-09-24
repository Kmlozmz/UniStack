@file:OptIn(ExperimentalMaterial3Api::class)

package com.unistack.app.feature_rooms.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import com.unistack.app.core.utils.performSafely
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.lerp
import kotlinx.coroutines.delay
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.QuestionMark
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.R
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.feature_grades.domain.Subject
import com.unistack.app.feature_rooms.domain.PartState
import com.unistack.app.feature_rooms.domain.RoomLogic
import com.unistack.app.feature_rooms.domain.SplitMode
import com.unistack.app.feature_rooms.domain.WorkRoom
import com.unistack.app.feature_tasks.presentation.SubjectShapeIcon
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private enum class Sort { URGENCY, DATE, SUBJECT, MINE }
private enum class StateFilter { ALL, ACTIVE, DONE }

/**
 * La pantalla principal de Trabajos (artifact «Trabajos · pantalla principal», cerrado el 22 sep):
 * vacía B1, recién creada, activos con hero inteligente y tarjeta 0, y terminados por periodo.
 */
@Composable
fun RoomsHomeScreen(
    onBackClick: () -> Unit,
    onOpenRoom: (String) -> Unit,
    onCreateRoom: () -> Unit,
    viewModel: RoomsViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    val today = viewModel.today()
    fun subjectOf(r: WorkRoom) = subjects.firstOrNull { it.id == r.subjectId }

    var sort by rememberSaveable { mutableStateOf(Sort.URGENCY) }
    var sortOpen by remember { mutableStateOf(false) }
    var stateFilter by rememberSaveable { mutableStateOf(StateFilter.ALL) }
    var subjectFilter by rememberSaveable { mutableStateOf<String?>(null) }
    var filterSheet by remember { mutableStateOf(false) }
    var searchOpen by remember { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var guide by remember { mutableStateOf(false) }
    var newMenu by remember { mutableStateOf(false) }
    var joinSheet by remember { mutableStateOf(false) }
    var closedRoomId by remember { mutableStateOf<String?>(null) }
    val askedReopen = remember { mutableStateListOf<String>() }
    var doneFolded by rememberSaveable { mutableStateOf(false) }
    val openPeriods = remember { mutableStateListOf<String>() }
    val openFolders = remember { mutableStateListOf<String>() }
    var periodsInit by remember { mutableStateOf(false) }

    fun visible(list: List<WorkRoom>): List<WorkRoom> {
        var l = subjectFilter?.let { f -> list.filter { it.subjectId == f } } ?: list
        val q = query.trim().lowercase()
        if (q.isNotEmpty()) l = l.filter { it.title.lowercase().contains(q) || (subjectOf(it)?.name?.lowercase()?.contains(q) == true) }
        return l
    }
    val active = if (stateFilter == StateFilter.DONE) emptyList() else rooms.filter { !it.isClosed }
    val closed = if (stateFilter == StateFilter.ACTIVE) emptyList() else rooms.filter { it.isClosed }

    val groups = activeGroups(visible(active), sort, today, ::subjectOf)
    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().statusBarsPadding(),
            contentPadding = PaddingValues(start = 18.dp, end = 18.dp, bottom = 110.dp)
        ) {
            /*
             * Cabecera en una sola fila: atrás, el título con su línea debajo y la ayuda en la
             * esquina. Antes el título iba en otra fila bajo el botón y la ayuda flotaba a media
             * altura (23 sep: «encájalo arriba al lado del back»).
             */
            item {
                Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    UniBackButton(onClick = onBackClick)
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.rooms_title), fontSize = 22.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface, letterSpacing = (-0.3).sp, maxLines = 1)
                        val sub = if (rooms.isEmpty()) stringResource(R.string.rooms_home_subtitle_empty) else {
                            val a = rooms.count { !it.isClosed }; val d = rooms.count { it.isClosed }
                            (if (a == 1) stringResource(R.string.rooms_home_counts_one_active) else stringResource(R.string.rooms_home_counts_active, a)) +
                                (if (d == 0) "" else if (d == 1) stringResource(R.string.rooms_home_counts_one_done) else stringResource(R.string.rooms_home_counts_done, d))
                        }
                        Text(sub, fontSize = 12.5.sp, lineHeight = 1.3.em, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                    HelpButton { guide = true }
                }
            }
            if (rooms.isEmpty()) {
                item { EmptyState(onCreate = onCreateRoom, onJoin = { joinSheet = true }) }
            } else {
                item { SmartHero(rooms, today, ::subjectOf, onOpenRoom) }
                if (rooms.count { !it.isClosed } >= 2 || rooms.any { it.isClosed }) item {
                    Tools(
                        sort = sort, sortOpen = sortOpen, onSortToggle = { sortOpen = !sortOpen }, onSort = { sort = it; sortOpen = false },
                        stateFilter = stateFilter, subject = subjects.firstOrNull { it.id == subjectFilter },
                        onFilter = { filterSheet = true; sortOpen = false },
                        searchOpen = searchOpen, query = query, onQuery = { query = it },
                        onSearch = { searchOpen = true; sortOpen = false }, onCloseSearch = { searchOpen = false; query = "" }
                    )
                }
                val v = visible(active)
                if (v.isNotEmpty()) {
                    groups.forEach { (label, color, list) ->
                        item(key = "g-$label") { RoomLabel(label, trailing = "${list.size}", color = color ?: MaterialTheme.colorScheme.primary, modifier = Modifier.padding(top = 0.dp)) }
                        items(list, key = { it.id }) { r -> RoomCard(r, subjectOf(r), today, onClick = { onOpenRoom(r.id) }); VSpace(12.dp) }
                    }
                }
                val vc = visible(closed)
                if (vc.isNotEmpty()) {
                    val byPeriod = vc.sortedByDescending { it.closedAt }.groupBy { periodOf(it) }
                    if (!periodsInit) { byPeriod.keys.firstOrNull()?.let { openPeriods.add(it) }; periodsInit = true }
                    item { DoneHeader(vc.size, byPeriod.size, !doneFolded) { doneFolded = !doneFolded } }
                    if (!doneFolded) {
                        byPeriod.entries.forEachIndexed { i, (per, ws) ->
                            item(key = "per-$per") {
                                PeriodBlock(per, i == 0, ws, subjects, per in openPeriods, openFolders,
                                    onToggle = { if (per in openPeriods) openPeriods.remove(per) else openPeriods.add(per) },
                                    onFolder = { k -> if (k in openFolders) openFolders.remove(k) else openFolders.add(k) },
                                    onRoom = { closedRoomId = it.id })
                            }
                        }
                    }
                }
                if ((subjectFilter != null || query.isNotBlank()) && v.isEmpty() && vc.isEmpty()) item {
                    Text(stringResource(R.string.rooms_nothing_here), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(vertical = 26.dp))
                }
            }
        }
        if (rooms.isNotEmpty()) {
            Row(
                Modifier.align(Alignment.BottomEnd).navigationBarsPadding().padding(end = 18.dp, bottom = 24.dp)
                    .clip(RoundedCornerShape(20.dp)).background(MaterialTheme.colorScheme.primary).cleanClickable { newMenu = true }
                    .padding(horizontal = 20.dp, vertical = 15.dp),
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(19.dp))
                Text(stringResource(R.string.rooms_new), color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (guide) GuideSheet { guide = false }
    if (newMenu) NewMenuSheet(onDismiss = { newMenu = false }, onCreate = { newMenu = false; onCreateRoom() }, onJoin = { newMenu = false; joinSheet = true })
    if (joinSheet) JoinSheet { joinSheet = false }
    if (filterSheet) FilterSheet(rooms, subjects, stateFilter, subjectFilter, onState = { stateFilter = it }, onSubject = { subjectFilter = it }) { filterSheet = false }
    val homeContext = LocalContext.current
    closedRoomId?.let { id ->
        rooms.firstOrNull { it.id == id }?.let { r ->
            ClosedDialog(r, id in askedReopen, onDismiss = { closedRoomId = null },
                onReopen = { viewModel.reopenRoom(id); closedRoomId = null },
                onAsk = { askedReopen.add(id) },
                onFinal = {
                    closedRoomId = null
                    val f = r.finalFile
                    val fmt = WorkFormat.entries.firstOrNull { it.key == r.finalFormat }
                    if (f != null && fmt != null) homeContext.openRoomFile(viewModel, f, fmt.mime, fileTitle(r, fmt)) else onOpenRoom(id)
                })
        }
    }
}

fun periodOf(r: WorkRoom): String {
    r.periodLabel?.let { return it }
    val d = Instant.ofEpochMilli(r.closedAt ?: r.updatedAt).atZone(ZoneId.systemDefault()).toLocalDate()
    return "${d.year}-${if (d.monthValue <= 6) 1 else 2}"
}

// ---------------------------------------------------------------- lo que te toca

private fun myOpenParts(r: WorkRoom) = r.parts.filter { r.meId in it.ownerIds && it.state != PartState.DELIVERED }
private fun urgencyOf(r: WorkRoom, today: Long): Long {
    val mine = myOpenParts(r)
    return if (mine.isNotEmpty()) mine.minOf { RoomLogic.partDue(r, it) - today } else (r.dueEpochDay - today) + 100
}

@Composable
private fun activeGroups(v: List<WorkRoom>, sort: Sort, today: Long, subjectOf: (WorkRoom) -> Subject?): List<Triple<String, Color?, List<WorkRoom>>> {
    val out = mutableListOf<Triple<String, Color?, List<WorkRoom>>>()
    when (sort) {
        Sort.URGENCY -> out += Triple(stringResource(R.string.rooms_group_active), null, v.sortedBy { urgencyOf(it, today) })
        Sort.DATE -> {
            val u = v.sortedBy { urgencyOf(it, today) }
            u.filter { urgencyOf(it, today) <= 7 }.takeIf { it.isNotEmpty() }?.let { out += Triple(stringResource(R.string.rooms_group_week), null, it) }
            u.filter { urgencyOf(it, today) in 8..14 }.takeIf { it.isNotEmpty() }?.let { out += Triple(stringResource(R.string.rooms_group_two), null, it) }
            u.filter { urgencyOf(it, today) > 14 }.takeIf { it.isNotEmpty() }?.let { out += Triple(stringResource(R.string.rooms_group_later), null, it) }
        }
        Sort.SUBJECT -> v.groupBy { it.subjectId }.forEach { (_, l) ->
            val s = subjectOf(l.first())
            out += Triple(s?.name ?: stringResource(R.string.rooms_no_subject), subjectColor(s), l)
        }
        Sort.MINE -> {
            val mine = v.filter { myOpenParts(it).isNotEmpty() }.sortedBy { urgencyOf(it, today) }
            if (mine.isNotEmpty()) out += Triple(stringResource(R.string.rooms_group_mine), null, mine)
            val rest = v - mine.toSet()
            if (rest.isNotEmpty()) out += Triple(stringResource(R.string.rooms_group_rest), null, rest)
        }
    }
    return out
}

private data class Hero(val score: Long, val label: String, val title: String, val body: String, val cta: String?, val roomId: String?, val alert: Boolean = false, val calm: Boolean = false)

@Composable
private fun SmartHero(rooms: List<WorkRoom>, today: Long, subjectOf: (WorkRoom) -> Subject?, onOpen: (String) -> Unit) {
    val active = rooms.filter { !it.isClosed }
    val cands = mutableListOf<Hero>()
    val noSubject = stringResource(R.string.rooms_no_subject)
    fun sname(r: WorkRoom) = subjectOf(r)?.name ?: noSubject
    active.forEach { r ->
        myOpenParts(r).forEach { p ->
            val d = RoomLogic.partDue(r, p) - today
            val late = d < 0
            cands += Hero(d, stringResource(if (late) R.string.rooms_hero_late else R.string.rooms_hero_yours), p.name,
                stringResource(if (late) R.string.rooms_hero_was else R.string.rooms_hero_for, sname(r), r.title, relDay(d)),
                stringResource(R.string.rooms_hero_upload), r.id, alert = late)
        }
    }
    active.firstOrNull { it.isLeader && it.activeMembers.size == 1 && it.parts.any { p -> p.state != PartState.DELIVERED } }?.let { r ->
        cands += Hero(-1, stringResource(R.string.rooms_hero_no_group), stringResource(R.string.rooms_hero_no_group_t),
            stringResource(R.string.rooms_hero_no_group_d, sname(r)), stringResource(R.string.rooms_hero_open_invite), r.id)
    }
    active.firstOrNull { it.split == SplitMode.FREE && it.activeMembers.size > 1 && it.parts.any { p -> p.isFree } && it.parts.none { p -> it.meId in p.ownerIds } }?.let { r ->
        cands += Hero(4, stringResource(R.string.rooms_hero_free), stringResource(R.string.rooms_hero_free_t, r.parts.count { it.isFree }, sname(r)),
            stringResource(R.string.rooms_hero_free_d, r.title), stringResource(R.string.rooms_hero_pick), r.id)
    }
    active.firstOrNull { it.split != SplitMode.FREE && it.activeMembers.size > 1 && it.parts.none { p -> it.meId in p.ownerIds } }?.let { r ->
        cands += Hero(6, stringResource(R.string.rooms_hero_no_part), stringResource(R.string.rooms_hero_no_part_t),
            stringResource(R.string.rooms_hero_no_part_d, sname(r), r.parts.count { it.isFree }), stringResource(R.string.rooms_hero_enter_pick), r.id)
    }
    active.firstOrNull { it.isLeader && it.split != SplitMode.FREE && it.activeMembers.size > 1 && it.parts.any { p -> p.isFree } }?.let { r ->
        cands += Hero(12, stringResource(R.string.rooms_hero_half), stringResource(R.string.rooms_hero_half_t, r.parts.count { it.isFree }),
            "${sname(r)} · ${r.title}", stringResource(R.string.rooms_hero_finish_split), r.id)
    }
    run {
        for (r in active.filter { it.isLeader }) {
            val o = r.parts.firstOrNull { it.state != PartState.DELIVERED && RoomLogic.partDue(r, it) < today && it.ownerIds.isNotEmpty() && r.meId !in it.ownerIds }
            if (o != null) {
                cands += Hero(18, stringResource(R.string.rooms_hero_other_late, r.nameOf(o.ownerIds.first())), o.name,
                    stringResource(R.string.rooms_hero_was_for, sname(r), relDay(RoomLogic.partDue(r, o) - today)), stringResource(R.string.rooms_hero_see), r.id)
                break
            }
        }
    }
    val h = cands.minByOrNull { it.score } ?: Hero(0, stringResource(R.string.rooms_hero_calm),
        stringResource(if (active.isNotEmpty()) R.string.rooms_hero_calm_t else R.string.rooms_hero_calm_t0),
        if (active.isNotEmpty()) stringResource(R.string.rooms_hero_calm_d, active.size) else stringResource(R.string.rooms_hero_calm_d0), null, null, calm = true)
    val cs = MaterialTheme.colorScheme
    val bg = when { h.alert -> cs.errorContainer; h.calm -> cs.surfaceContainer; else -> cs.primaryContainer }
    val fg = when { h.alert -> cs.onErrorContainer; else -> cs.onSurface }
    Block(bg, RoundedCornerShape(26.dp), Modifier.padding(bottom = 14.dp), PaddingValues(17.dp)) {
        Text(h.label.uppercase(), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp, color = if (h.calm) cs.onSurfaceVariant else fg.copy(alpha = 0.8f))
        Text(h.title, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold, color = fg, modifier = Modifier.padding(top = 6.dp), lineHeight = 24.sp)
        Text(h.body, fontSize = 13.sp, color = fg.copy(alpha = 0.85f), modifier = Modifier.padding(top = 5.dp), lineHeight = 19.sp)
        if (h.cta != null && h.roomId != null) {
            VSpace(14.dp)
            Pill(h.cta, { onOpen(h.roomId) }, icon = Icons.Rounded.PanTool, style = PillStyle.HERO,
                heroBase = if (h.alert) cs.onErrorContainer else if (h.calm) cs.primary else cs.onPrimaryContainer,
                heroOn = if (h.alert) cs.errorContainer else if (h.calm) cs.onPrimary else cs.primaryContainer)
        }
    }
}

// ---------------------------------------------------------------- herramientas

/**
 * Ordenar, filtrar y buscar. La lupa **se estira hasta ser la barra**: nace en su sitio, crece
 * hacia la izquierda por encima de los chips (que se apagan) y al cerrar se recoge en el botón.
 * Antes la fila entera se deslizaba a la izquierda y la barra entraba después (23 sep).
 *
 * El filtro vive con «Ordenar» en su propio tramo: repartía el hueco con el separador y se
 * quedaba en «…» (23 sep).
 */
@Composable
private fun Tools(
    sort: Sort, sortOpen: Boolean, onSortToggle: () -> Unit, onSort: (Sort) -> Unit,
    stateFilter: StateFilter, subject: Subject?, onFilter: () -> Unit,
    searchOpen: Boolean, query: String, onQuery: (String) -> Unit, onSearch: () -> Unit, onCloseSearch: () -> Unit
) {
    val cs = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val focus = remember { FocusRequester() }
    val sortLabels = mapOf(Sort.URGENCY to R.string.rooms_sort_urgency, Sort.DATE to R.string.rooms_sort_date, Sort.SUBJECT to R.string.rooms_sort_subject, Sort.MINE to R.string.rooms_sort_mine)
    val open by animateFloatAsState(if (searchOpen) 1f else 0f, spring(dampingRatio = 0.8f, stiffness = Spring.StiffnessMediumLow), label = "lupa")
    LaunchedEffect(searchOpen) { if (searchOpen) { delay(160); runCatching { focus.requestFocus() } } }
    Column(Modifier.padding(bottom = 10.dp)) {
        BoxWithConstraints(Modifier.fillMaxWidth().height(42.dp)) {
            val full = maxWidth
            Row(
                Modifier.fillMaxSize().graphicsLayer { alpha = (1f - open * 1.6f).coerceIn(0f, 1f) },
                verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    ToolChip(Icons.AutoMirrored.Rounded.Sort, stringResource(sortLabels.getValue(sort)), onClick = { if (!searchOpen) onSortToggle() })
                    val parts = listOfNotNull(
                        when (stateFilter) { StateFilter.ACTIVE -> stringResource(R.string.rooms_filter_active); StateFilter.DONE -> stringResource(R.string.rooms_filter_done); else -> null },
                        subject?.name
                    )
                    val on = parts.isNotEmpty()
                    val fc = if (subject != null) subjectColor(subject) else cs.primary
                    ToolChip(Icons.Rounded.FilterList, if (on) parts.joinToString(" · ") else stringResource(R.string.rooms_filter), onClick = { if (!searchOpen) onFilter() },
                        bg = if (on) mix(fc, 0.2f, cs.background) else cs.surfaceContainer, fg = if (on) fc else cs.onSurface,
                        modifier = Modifier.weight(1f, fill = false))
                }
                Box(Modifier.size(42.dp))
            }
            Row(
                Modifier.align(Alignment.CenterEnd).width(lerp(42.dp, full, open.coerceAtLeast(0f))).fillMaxHeight()
                    .clip(CircleShape).background(cs.surfaceContainer)
                    .then(if (!searchOpen) Modifier.cleanClickable(onClick = onSearch) else Modifier),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.Search, stringResource(R.string.rooms_search), tint = lerp(cs.onSurface, cs.onSurfaceVariant, open.coerceIn(0f, 1f)), modifier = Modifier.size(19.dp))
                }
                if (open > 0.02f) {
                    val late = ((open - 0.45f) / 0.55f).coerceIn(0f, 1f)
                    Box(Modifier.weight(1f).graphicsLayer { alpha = late }) {
                        if (query.isEmpty()) Text(stringResource(R.string.rooms_search_hint), color = cs.onSurfaceVariant, fontSize = 14.5.sp, maxLines = 1)
                        BasicTextField(query, onQuery, singleLine = true, textStyle = TextStyle(color = cs.onSurface, fontSize = 14.5.sp, fontWeight = FontWeight.Medium), cursorBrush = SolidColor(cs.primary), modifier = Modifier.fillMaxWidth().focusRequester(focus))
                    }
                    Box(Modifier.padding(start = 6.dp, end = 8.dp).size(26.dp).graphicsLayer { alpha = late; scaleX = 0.6f + 0.4f * late; scaleY = 0.6f + 0.4f * late }
                        .clip(CircleShape).background(cs.surfaceContainerHighest).cleanClickable { focusManager.clearFocus(); onCloseSearch() }, contentAlignment = Alignment.Center) {
                        Icon(Icons.Rounded.Close, stringResource(R.string.rooms_close), tint = cs.onSurfaceVariant, modifier = Modifier.size(13.dp))
                    }
                }
            }
        }
        AnimatedVisibility(sortOpen && !searchOpen, enter = fadeIn() + expandVertically(), exit = fadeOut() + shrinkVertically()) {
            Column(Modifier.padding(top = 6.dp).fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(cs.surfaceContainerLow).padding(7.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Sort.entries.forEach { s ->
                    val on = s == sort
                    Row(
                        Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(if (on) mix(cs.primary, 0.1f, cs.surfaceContainerLow) else Color.Transparent)
                            .cleanClickable { onSort(s) }.padding(13.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        RadioDot(on)
                        Text(stringResource(sortLabels.getValue(s)), fontSize = 14.5.sp, fontWeight = if (on) FontWeight.Bold else FontWeight.SemiBold, color = if (on) cs.primary else cs.onSurface)
                    }
                }
            }
        }
    }
}

/**
 * La ayuda de la esquina: una pastilla «Guía» con el libro en el acento, que abre «Cómo
 * funciona». Sustituye al (?) redondo, que no le convencía; elegida la B de cuatro (23 sep).
 */
@Composable
private fun HelpButton(onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Row(
        Modifier.height(40.dp).clip(CircleShape).background(cs.surfaceContainer).cleanClickable(onClick = onClick).padding(start = 11.dp, end = 15.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(Icons.AutoMirrored.Rounded.MenuBook, stringResource(R.string.rooms_help), tint = cs.primary, modifier = Modifier.size(19.dp))
        Text(stringResource(R.string.rooms_guide_short), fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1)
    }
}

@Composable
private fun ToolChip(icon: ImageVector, text: String, onClick: () -> Unit, modifier: Modifier = Modifier, bg: Color = MaterialTheme.colorScheme.surfaceContainer, fg: Color = MaterialTheme.colorScheme.onSurface) {
    Row(
        modifier.clip(CircleShape).background(bg).cleanClickable(onClick = onClick).padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Icon(icon, null, tint = fg, modifier = Modifier.size(16.dp))
        Text(text, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
        Icon(Icons.Rounded.ExpandMore, null, tint = fg, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun FilterSheet(rooms: List<WorkRoom>, subjects: List<Subject>, state: StateFilter, subject: String?, onState: (StateFilter) -> Unit, onSubject: (String?) -> Unit, onDismiss: () -> Unit) {
    fun count(st: StateFilter, sub: String?) = rooms.count { (st == StateFilter.ALL || (st == StateFilter.ACTIVE) == !it.isClosed) && (sub == null || it.subjectId == sub) }
    val cs = MaterialTheme.colorScheme
    val mats = rooms.mapNotNull { it.subjectId }.distinct().mapNotNull { id -> subjects.firstOrNull { it.id == id } }
    RoomSheet(onDismiss, stringResource(R.string.rooms_filter), stringResource(R.string.rooms_filter_count, count(state, subject)),
        trailing = { RoundButton(Icons.Rounded.Close, stringResource(R.string.rooms_close), onDismiss, tint = cs.onSurfaceVariant) }, showClose = false) {
        RoomLabel(stringResource(R.string.rooms_filter_state), Modifier.padding(top = 0.dp))
        val states = listOf(Triple(StateFilter.ALL, R.string.rooms_filter_all, Icons.Rounded.Layers), Triple(StateFilter.ACTIVE, R.string.rooms_filter_active, Icons.Rounded.DonutLarge), Triple(StateFilter.DONE, R.string.rooms_filter_done, Icons.Rounded.CheckCircle))
        RoomGroup(states, outer = 18.dp) { (st, label, icon), shape ->
            FilterRow(shape, state == st, { Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) { Icon(icon, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(17.dp)) } },
                stringResource(label), count(st, subject)) { onState(st) }
        }
        RoomLabel(stringResource(R.string.rooms_filter_subject))
        RoomGroup(listOf<Subject?>(null) + mats, outer = 18.dp) { s, shape ->
            FilterRow(shape, subject == s?.id, {
                if (s == null) Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Folder, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(17.dp)) }
                else SubjectShapeIcon(subjectColor(s), s.id, size = 32.dp)
            }, s?.name ?: stringResource(R.string.rooms_filter_all_subjects), count(state, s?.id)) { onSubject(s?.id) }
        }
    }
}

@Composable
private fun FilterRow(shape: androidx.compose.ui.graphics.Shape, on: Boolean, lead: @Composable () -> Unit, name: String, count: Int, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomRow(shape, color = if (on) mix(cs.primary, 0.12f, cs.surfaceContainerLow) else cs.surfaceContainerLow, minHeight = 50.dp, padding = PaddingValues(horizontal = 13.dp, vertical = 9.dp), onClick = onClick) {
        lead()
        Text(name, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurface, modifier = Modifier.weight(1f))
        Text("$count", fontSize = 12.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant)
        RadioDot(on)
    }
}

// ---------------------------------------------------------------- tarjeta 0

@Composable
fun RoomCard(r: WorkRoom, subject: Subject?, today: Long, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val mat = subjectColor(subject)
    val base = mix(mat, 0.07f, cs.surfaceContainerLow)
    val delivered = r.deliveredCount
    val mine = myOpenParts(r)
    val myLate = mine.firstOrNull { RoomLogic.partDue(r, it) < today }
    val free = r.parts.count { it.isFree }
    val othersLate = r.parts.filter { it.state != PartState.DELIVERED && RoomLogic.partDue(r, it) < today && it.ownerIds.isNotEmpty() && r.meId !in it.ownerIds }
    val alone = r.activeMembers.size == 1
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(base).cleanClickable(onClick = onClick).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(mat), contentAlignment = Alignment.Center) {
                Icon(RoomTemplates.icon(r.type), null, tint = cs.background, modifier = Modifier.size(22.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(subject?.name ?: stringResource(R.string.rooms_no_subject), fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = mat, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(stringResource(RoomTemplates.typeNameRes(r.type)), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
            }
            FaceStack(r, r.activeMembers, base)
        }
        Text(r.title, fontSize = 17.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, lineHeight = 22.sp)
        SegmentBar(r.parts.map { p ->
            when {
                p.state == PartState.DELIVERED -> RoomTone.VERDE.color
                RoomLogic.partDue(r, p) < today -> cs.error
                r.meId in p.ownerIds -> mat
                else -> cs.surfaceContainerHighest
            }
        }, Modifier.padding(top = 13.dp, bottom = 7.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rooms_parts_of, delivered, r.parts.size), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(stringResource(if (r.split == SplitMode.FREE) R.string.rooms_split_free else R.string.rooms_split_assigned), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
        }
        data class Say(val t1: String, val t2: String, val tone: Color, val icon: ImageVector, val red: Boolean = false)
        val say = when {
            myLate != null -> Say(stringResource(R.string.rooms_card_your_part, myLate.name), stringResource(R.string.rooms_card_was_for, relDay(RoomLogic.partDue(r, myLate) - today)), cs.error, Icons.Rounded.Schedule, true)
            mine.isNotEmpty() -> Say(stringResource(R.string.rooms_card_your_part, mine.first().name),
                stringResource(R.string.rooms_card_for, relDay(RoomLogic.partDue(r, mine.first()) - today)) + (if (mine.size > 1) stringResource(R.string.rooms_card_and_more, mine.size - 1) else ""), mat, Icons.Rounded.PanTool)
            alone && r.isLeader -> Say(stringResource(R.string.rooms_card_invite), stringResource(R.string.rooms_card_invite_d), RoomTone.INDIGO.color, Icons.Rounded.Key)
            r.split == SplitMode.FREE && free > 0 -> Say(stringResource(R.string.rooms_card_take), stringResource(R.string.rooms_card_take_d, free), RoomTone.CIAN.color, Icons.Rounded.PanTool)
            free > 0 -> Say(stringResource(R.string.rooms_card_no_part), stringResource(R.string.rooms_card_no_part_d, free), RoomTone.CIAN.color, Icons.Rounded.PanTool)
            othersLate.isNotEmpty() -> Say(stringResource(R.string.rooms_card_late, othersLate.flatMap { it.ownerIds }.distinct().joinToString(" · ") { r.nameOf(it) }),
                stringResource(R.string.rooms_card_late_d, othersLate.first().name, relDay(RoomLogic.partDue(r, othersLate.first()) - today)), RoomTone.AMBAR.color, Icons.Rounded.NotificationsActive)
            else -> Say(stringResource(R.string.rooms_card_all_good), stringResource(R.string.rooms_card_delivered_of, delivered, r.parts.size), RoomTone.VERDE.color, Icons.Rounded.Check)
        }
        Row(
            Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(if (say.red) cs.errorContainer else cs.surfaceContainer).padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(say.tone), contentAlignment = Alignment.Center) { Icon(say.icon, null, tint = cs.background, modifier = Modifier.size(17.dp)) }
            Column(Modifier.weight(1f)) {
                Text(say.t1, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (say.red) cs.onErrorContainer else cs.onSurface, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(say.t2, fontSize = 11.5.sp, color = if (say.red) cs.onErrorContainer else cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp))
            }
        }
        val late = myLate != null || othersLate.isNotEmpty()
        val (estado, tone) = when {
            late -> stringResource(R.string.rooms_state_late) to cs.error
            r.allDelivered -> stringResource(R.string.rooms_state_complete) to RoomTone.VERDE.color
            else -> stringResource(R.string.rooms_state_active) to RoomTone.GRIS.color
        }
        val dueTxt = relDay(r.dueEpochDay - today)
        val foot = if (myLate == null && othersLate.isNotEmpty())
            stringResource(R.string.rooms_foot_late, othersLate.flatMap { it.ownerIds }.distinct().joinToString(" · ") { r.nameOf(it) }, dueTxt)
        else stringResource(R.string.rooms_foot_due, dueTxt)
        Row(Modifier.padding(top = 13.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Rounded.Schedule, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(14.dp))
            Text(foot, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
            StatusChip(estado, tone)
        }
    }
}

// ---------------------------------------------------------------- vacía B1

@Composable
private fun EmptyState(onCreate: () -> Unit, onJoin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column {
        Text(stringResource(R.string.rooms_empty_title), fontSize = 21.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.padding(top = 6.dp, bottom = 8.dp))
        Text(stringResource(R.string.rooms_empty_body), fontSize = 13.5.sp, color = cs.onSurfaceVariant, lineHeight = 21.sp, modifier = Modifier.padding(bottom = 14.dp))
        Box {
            ExampleCard()
            Box(Modifier.matchParentSize().clip(RoundedCornerShape(26.dp)).background(Brush.verticalGradient(0.3f to Color.Transparent, 0.96f to cs.background)))
        }
        RoomLabel(stringResource(R.string.rooms_how_label))
        Row(verticalAlignment = Alignment.Top) {
            FlowStep(Icons.Rounded.Add, MemberColors.of(0), stringResource(R.string.rooms_flow_create), Modifier.weight(1f))
            FlowArrow()
            FlowStep(Icons.Rounded.Key, MemberColors.of(2), stringResource(R.string.rooms_flow_join), Modifier.weight(1f))
            FlowArrow()
            FlowStep(Icons.Rounded.PanTool, MemberColors.of(10), stringResource(R.string.rooms_flow_start), Modifier.weight(1f))
        }
        VSpace(18.dp)
        Pill(stringResource(R.string.rooms_create_room), onCreate, Modifier.fillMaxWidth(), icon = Icons.Rounded.Add, big = true)
        VSpace(10.dp)
        Pill(stringResource(R.string.rooms_have_code), onJoin, Modifier.fillMaxWidth(), icon = Icons.Rounded.Key, style = PillStyle.TONAL, big = true)
    }
}

@Composable
private fun FlowStep(icon: ImageVector, color: Color, text: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(40.dp).clip(RoundedCornerShape(13.dp)).background(color), contentAlignment = Alignment.Center) { Icon(icon, null, tint = MaterialTheme.colorScheme.background, modifier = Modifier.size(20.dp)) }
        Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 7.dp), lineHeight = 15.sp)
    }
}

@Composable
private fun FlowArrow() = Box(Modifier.padding(top = 13.dp, start = 2.dp, end = 2.dp)) {
    Icon(Icons.AutoMirrored.Rounded.ArrowForward, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(15.dp))
}

@Composable
private fun ExampleCard() {
    val cs = MaterialTheme.colorScheme
    val mat = MemberColors.of(6)
    val base = mix(mat, 0.07f, cs.surfaceContainerLow)
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(26.dp)).background(base).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            Box(Modifier.size(42.dp).clip(RoundedCornerShape(13.dp)).background(mat), contentAlignment = Alignment.Center) { Icon(RoomTemplates.icon(com.unistack.app.feature_rooms.domain.RoomType.ENSAYO), null, tint = cs.background, modifier = Modifier.size(22.dp)) }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_example_subject), fontSize = 14.5.sp, fontWeight = FontWeight.ExtraBold, color = mat)
                Text(stringResource(R.string.rooms_type_ensayo), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
            }
            Row { listOf("A" to 0, "S" to 1, "N" to 2).forEachIndexed { i, (n, c) -> Box(Modifier.padding(start = if (i == 0) 0.dp else 0.dp)) { Face(n, MemberColors.of(c), 24.dp, base) } } }
        }
        Text(stringResource(R.string.rooms_example_title), fontSize = 17.5.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
        SegmentBar(listOf(RoomTone.VERDE.color, RoomTone.VERDE.color, cs.error, RoomTone.VERDE.color, mat, cs.surfaceContainerHighest), Modifier.padding(top = 13.dp, bottom = 7.dp))
        Row(Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.rooms_example_parts), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(stringResource(R.string.rooms_split_assigned), fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
        }
        Row(Modifier.padding(top = 12.dp).fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(cs.surfaceContainer).padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Box(Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(mat), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.PanTool, null, tint = cs.background, modifier = Modifier.size(17.dp)) }
            Column { Text(stringResource(R.string.rooms_example_mine), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = cs.onSurface); Text(stringResource(R.string.rooms_tomorrow), fontSize = 11.5.sp, color = cs.onSurfaceVariant) }
        }
    }
}

// ---------------------------------------------------------------- terminados

@Composable
private fun DoneHeader(n: Int, periods: Int, open: Boolean, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    Column {
        Box(Modifier.padding(start = 4.dp, end = 4.dp, top = 20.dp, bottom = 4.dp).fillMaxWidth().height(1.dp).background(cs.outlineVariant.copy(alpha = 0.5f)))
        Row(Modifier.fillMaxWidth().cleanClickable(onClick = onClick).padding(start = 4.dp, end = 4.dp, top = 10.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(36.dp).clip(RoundedCornerShape(12.dp)).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.CheckCircle, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(19.dp)) }
            Column(Modifier.weight(1f)) {
                Text(stringResource(R.string.rooms_done_title), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(if (periods == 1) stringResource(R.string.rooms_done_sub1, n) else stringResource(R.string.rooms_done_sub, n, periods), fontSize = 12.sp, color = cs.onSurfaceVariant)
            }
            Chevron(open)
        }
    }
}

@Composable
fun Chevron(open: Boolean, size: androidx.compose.ui.unit.Dp = 22.dp) {
    val rot by animateFloatAsState(if (open) 180f else 0f, label = "chev")
    Icon(Icons.Rounded.ExpandMore, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(size).rotate(rot))
}

@Composable
private fun PeriodBlock(per: String, current: Boolean, ws: List<WorkRoom>, subjects: List<Subject>, open: Boolean, openFolders: List<String>, onToggle: () -> Unit, onFolder: (String) -> Unit, onRoom: (WorkRoom) -> Unit) {
    val cs = MaterialTheme.colorScheme
    val (year, sem) = per.split("-").let { it[0] to it.getOrElse(1) { "1" } }
    val graded = ws.mapNotNull { it.finalGrade }
    val avg = if (graded.isEmpty()) null else gradeText(graded.average())
    val mats = ws.map { it.subjectId }.distinct()
    Column(Modifier.padding(top = 10.dp).fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(if (current) mix(cs.primary, 0.07f, cs.surfaceContainerLow) else cs.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().cleanClickable(onClick = onToggle).padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.width(52.dp).height(56.dp).clip(RoundedCornerShape(16.dp)).background(if (current) cs.primary else cs.surfaceContainerHighest), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                Text(year, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (current) cs.onPrimary else cs.onSurfaceVariant, letterSpacing = 0.5.sp)
                Text(sem, fontSize = 24.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = if (current) cs.onPrimary else cs.onSurface)
            }
            /*
             * «En curso» va encima del nombre, en letra pequeña del acento. Al lado del nombre no
             * cabía con el promedio y la flecha, y se partía en sílabas hacia abajo (23 sep).
             */
            Column(Modifier.weight(1f)) {
                if (current) Text(stringResource(R.string.rooms_period_now).uppercase(), fontSize = 10.sp, lineHeight = 1.3.em, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.8.sp, color = cs.primary, maxLines = 1, softWrap = false)
                Text(stringResource(if (sem == "1") R.string.rooms_period_first else R.string.rooms_period_second), fontSize = 15.5.sp, lineHeight = 1.25.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(Modifier.padding(top = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    mats.take(4).forEach { id -> val s = subjects.firstOrNull { it.id == id }; Box(Modifier.padding(end = 3.dp)) { SubjectShapeIcon(subjectColor(s), id ?: "none", size = 17.dp) } }
                    Text(pluralText(R.plurals.rooms_n_works, ws.size), fontSize = 12.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(start = 5.dp).weight(1f, fill = false), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            if (avg != null) Column(horizontalAlignment = Alignment.End) {
                Text(avg, fontSize = 21.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                Text(stringResource(R.string.rooms_average), fontSize = 10.5.sp, fontWeight = FontWeight.SemiBold, color = cs.onSurfaceVariant)
            }
            Box(Modifier.size(32.dp).clip(CircleShape).background(cs.surfaceContainer), contentAlignment = Alignment.Center) { Chevron(open, 20.dp) }
        }
        AnimatedVisibility(open) {
            Column(Modifier.padding(start = 10.dp, end = 10.dp, bottom = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                mats.forEach { id ->
                    val s = subjects.firstOrNull { it.id == id }
                    val c = subjectColor(s)
                    val wm = ws.filter { it.subjectId == id }
                    val key = "$per|$id"
                    val ab = key in openFolders
                    val pm = wm.mapNotNull { it.finalGrade }.takeIf { it.isNotEmpty() }?.average()?.let { gradeText(it) }
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(if (ab) mix(c, 0.08f, cs.background) else cs.background)) {
                        Row(Modifier.fillMaxWidth().cleanClickable { onFolder(key) }.padding(horizontal = 14.dp, vertical = 13.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SubjectShapeIcon(c, id ?: "none", size = 34.dp)
                            Column(Modifier.weight(1f)) {
                                Text(s?.name ?: stringResource(R.string.rooms_no_subject), fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface)
                                Text(pluralText(R.plurals.rooms_n_works, wm.size) + (pm?.let { stringResource(R.string.rooms_folder_avg, it) } ?: ""), fontSize = 12.sp, color = cs.onSurfaceVariant)
                            }
                            Chevron(ab)
                        }
                        AnimatedVisibility(ab) {
                            Column(Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                wm.forEachIndexed { i, w -> Receipt(w, s, groupShape(i, wm.size, 18.dp, 4.dp)) { onRoom(w) } }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Receipt(w: WorkRoom, s: Subject?, shape: androidx.compose.ui.graphics.Shape, onClick: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomRow(shape, color = cs.surfaceContainerLow, minHeight = 60.dp, padding = PaddingValues(horizontal = 13.dp, vertical = 12.dp), onClick = onClick) {
        Box(Modifier.size(40.dp).alpha(0.7f), contentAlignment = Alignment.Center) {
            SubjectShapeIcon(subjectColor(s).copy(alpha = 0.6f), w.subjectId ?: "none", size = 40.dp)
            Icon(RoomTemplates.icon(w.type), null, tint = cs.background, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(w.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = cs.onSurface, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(listOfNotNull(s?.name ?: stringResource(R.string.rooms_no_subject), w.closedAt?.let { shortDate(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()) }, w.finalFormat).joinToString(" · "),
                fontSize = 11.5.sp, color = cs.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp), maxLines = 1)
        }
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(5.dp)) {
            if (w.finalGrade != null) Text(gradeText(w.finalGrade), fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, modifier = Modifier.clip(CircleShape).background(cs.surfaceContainerHighest).padding(horizontal = 9.dp, vertical = 3.dp))
            else Text(stringResource(R.string.rooms_no_grade), fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = cs.onSurfaceVariant,
                modifier = Modifier.clip(CircleShape).background(Color.Transparent).padding(horizontal = 9.dp, vertical = 3.dp))
            FaceStack(w, w.activeMembers, cs.surfaceContainerLow, size = 20.dp)
        }
    }
}

// ---------------------------------------------------------------- hojas y ventanas

/**
 * «Cómo funciona una sala» en carrusel (artifact «Crear sala, segunda vuelta», opción A): un paso
 * por página con su icono grande de color; se pasa deslizando o con Siguiente hasta Entendido.
 */
@Composable
private fun GuideSheet(onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val ts = stringArrayResource(R.array.rooms_guide_t)
    val ds = stringArrayResource(R.array.rooms_guide_d)
    val icons = listOf(Icons.Rounded.Flag, Icons.Rounded.Add, Icons.Rounded.Key, Icons.Rounded.PanTool, Icons.Rounded.Upload, Icons.Rounded.Build, Icons.Rounded.Share)
    val tones = listOf(RoomTone.VIOLETA, RoomTone.VIOLETA, RoomTone.CIAN, RoomTone.CIAN, RoomTone.VERDE, RoomTone.VERDE, RoomTone.AMBAR).map { it.color }
    val pager = rememberPagerState { ts.size }
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(pager) { snapshotFlow { pager.currentPage }.drop(1).collect { haptic.performSafely(HapticFeedbackType.SegmentTick) } }
    val last = pager.currentPage == ts.lastIndex
    RoomSheet(onDismiss, stringResource(R.string.rooms_guide_title), stringResource(R.string.rooms_guide_sub),
        trailing = { RoundButton(Icons.Rounded.Close, stringResource(R.string.rooms_close), onDismiss, tint = cs.onSurfaceVariant) }, showClose = false) {
        HorizontalPager(pager, Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) { i ->
            val c = tones.getOrElse(i) { cs.primary }
            Column(Modifier.fillMaxWidth().heightIn(min = 300.dp).padding(horizontal = 10.dp, vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.padding(top = 8.dp, bottom = 16.dp).size(96.dp).clip(RoundedCornerShape(30.dp)).background(mix(c, 0.2f, cs.surfaceContainerHigh)), contentAlignment = Alignment.Center) {
                    Icon(icons.getOrElse(i) { Icons.Rounded.QuestionMark }, null, tint = c, modifier = Modifier.size(46.dp))
                }
                Text(stringResource(R.string.rooms_guide_step, i + 1, ts.size), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp, color = cs.onSurfaceVariant)
                Text(ts[i], fontSize = 21.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, color = cs.onSurface, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
                Text(ds.getOrElse(i) { "" }, fontSize = 14.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 21.sp)
            }
        }
        StretchDots(ts.size, pager.currentPage, Modifier.align(Alignment.CenterHorizontally).padding(top = 18.dp, bottom = 14.dp), dot = 6.dp, long = 22.dp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (pager.currentPage > 0) Pill(stringResource(R.string.rooms_back), { scope.launch { pager.animateScrollToPage(pager.currentPage - 1) } }, Modifier.weight(1f), style = PillStyle.TONAL, big = true)
            Pill(stringResource(if (last) R.string.rooms_guide_ok else R.string.rooms_next), {
                if (last) onDismiss() else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
            }, Modifier.weight(2f), big = true)
        }
    }
}

@Composable
private fun NewMenuSheet(onDismiss: () -> Unit, onCreate: () -> Unit, onJoin: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    RoomSheet(onDismiss, showClose = false) {
        val opts = listOf(
            Triple(Icons.Rounded.Add, RoomTone.VIOLETA.color, Triple(R.string.rooms_create_room, R.string.rooms_menu_create_d, onCreate)),
            Triple(Icons.Rounded.Key, RoomTone.CIAN.color, Triple(R.string.rooms_menu_join, R.string.rooms_menu_join_d, onJoin))
        )
        RoomGroup(opts, outer = 18.dp) { (icon, tone, t), shape ->
            RoomRow(shape, padding = PaddingValues(15.dp), onClick = t.third) {
                Box(Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tone), contentAlignment = Alignment.Center) { Icon(icon, null, tint = cs.background, modifier = Modifier.size(21.dp)) }
                RowTexts(stringResource(t.first), stringResource(t.second), titleSize = 15.sp, titleWeight = FontWeight.SemiBold)
            }
        }
        VSpace(8.dp)
        Pill(stringResource(R.string.rooms_cancel), onDismiss, Modifier.fillMaxWidth(), style = PillStyle.TONAL)
    }
}

@Composable
private fun JoinSheet(onDismiss: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current
    val msg = stringResource(R.string.rooms_needs_cloud)
    RoomSheet(onDismiss, stringResource(R.string.rooms_join_title), stringResource(R.string.rooms_join_sub)) {
        TextField(code, { code = it.uppercase().filter { c -> c.isLetterOrDigit() }.take(6) }, singleLine = true,
            textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 8.sp, textAlign = TextAlign.Center, color = cs.onSurface),
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp),
            colors = TextFieldDefaults.colors(focusedContainerColor = cs.surfaceContainerLow, unfocusedContainerColor = cs.surfaceContainerLow, focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent))
        VSpace(12.dp)
        Pill(stringResource(R.string.rooms_join_go), { android.widget.Toast.makeText(context, msg, android.widget.Toast.LENGTH_LONG).show(); onDismiss() }, Modifier.fillMaxWidth(), enabled = code.length == 6)
    }
}

@Composable
private fun ClosedDialog(r: WorkRoom, asked: Boolean, onDismiss: () -> Unit, onReopen: () -> Unit, onAsk: () -> Unit, onFinal: () -> Unit) {
    val cs = MaterialTheme.colorScheme
    val leader = r.nameOf(r.leaderId)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = cs.background,
        shape = RoundedCornerShape(28.dp),
        icon = { Box(Modifier.size(52.dp).clip(RoundedCornerShape(17.dp)).background(cs.surfaceContainerHighest), contentAlignment = Alignment.Center) { Icon(Icons.Rounded.Lock, null, tint = cs.onSurfaceVariant, modifier = Modifier.size(26.dp)) } },
        title = { Text(stringResource(R.string.rooms_closed_title), fontSize = 19.sp, lineHeight = 1.2.em, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
        text = {
            Column {
                Text(stringResource(R.string.rooms_closed_body, r.title), fontSize = 13.sp, color = cs.onSurfaceVariant, textAlign = TextAlign.Center, lineHeight = 20.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp))
                Text(if (r.isLeader) stringResource(R.string.rooms_closed_leader) else stringResource(R.string.rooms_closed_member, leader), fontSize = 12.5.sp, color = cs.onSurface, lineHeight = 19.sp,
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(cs.surfaceContainer).padding(horizontal = 13.dp, vertical = 11.dp))
                VSpace(12.dp)
                if (r.isLeader) Pill(stringResource(R.string.rooms_reopen), onReopen, Modifier.fillMaxWidth())
                else Pill(if (asked) stringResource(R.string.rooms_asked_reopen, leader) else stringResource(R.string.rooms_ask_reopen, leader), onAsk, Modifier.fillMaxWidth(), enabled = !asked)
                VSpace(8.dp)
                Pill(stringResource(R.string.rooms_see_final), onFinal, Modifier.fillMaxWidth(), style = PillStyle.TONAL)
                VSpace(8.dp)
                Pill(stringResource(R.string.rooms_close), onDismiss, Modifier.fillMaxWidth(), style = PillStyle.TONAL)
            }
        },
        confirmButton = {}
    )
}

