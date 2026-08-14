package com.unistack.app.feature_notifications.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.notifications.NotificationHistoryItem
import com.unistack.app.core.notifications.NotificationHistoryStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private enum class NotificationFilter(val label: String) {
    ALL("Todas"),
    ACTIONS("Acción"),
    ACADEMIC("Académico"),
    TASKS("Tareas"),
    CLASSES("Clases"),
    UNREAD("No leídas"),
    READ("Vistas");

    fun matches(item: NotificationHistoryItem): Boolean {
        val category = item.category()
        return when (this) {
            ALL -> true
            ACTIONS -> category.requiresAction
            ACADEMIC -> category.kind == NotificationKind.ACADEMIC
            TASKS -> category.kind == NotificationKind.TASK
            CLASSES -> category.kind == NotificationKind.CLASS
            UNREAD -> !item.read
            READ -> item.read
        }
    }
}

private val NotificationSurface: Color
    @Composable get() = UniStackColors.Background
private val NotificationCard: Color
    @Composable get() = UniStackColors.Card
private val NotificationFilterSurface: Color
    @Composable get() = UniStackColors.SurfaceVariant
private val NotificationBorder: Color
    @Composable get() = UniStackColors.SoftOutline
private val NotificationPrimary: Color
    @Composable get() = UniStackColors.Primary
private val NotificationAccentText: Color
    @Composable get() = UniStackColors.PrimaryDark
private val NotificationText: Color
    @Composable get() = UniStackColors.TextPrimary
private val NotificationBody: Color
    @Composable get() = UniStackColors.TextSecondary
private val NotificationMuted: Color
    @Composable get() = UniStackColors.TextSecondary
private val NotificationHeroBrush: Brush
    @Composable get() = SolidColor(UniStackColors.PrimaryLight)

@Composable
fun NotificationHistoryScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    val notifications by remember(context) {
        NotificationHistoryStore.observe(context)
    }.collectAsStateWithLifecycle()
    var selectedFilter by rememberSaveable { mutableStateOf(NotificationFilter.ALL) }
    val filtered = remember(notifications, selectedFilter) {
        notifications.filter(selectedFilter::matches)
    }
    val grouped = remember(filtered) {
        filtered
            .sortedByDescending { it.timestampMillis }
            .groupBy { it.dateSectionLabel() }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NotificationSurface)
            .statusBarsPadding(),
        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = scrollBottomRoom),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            NotificationHistoryHeader(
                unreadCount = notifications.count { !it.read },
                canMarkAllRead = notifications.any { !it.read },
                onBackClick = onBackClick,
                onMarkAllRead = { NotificationHistoryStore.markAllRead(context) },
                onSettingsClick = onSettingsClick
            )
        }
        /*
         * El resumen y los filtros solo salen si hay algo que resumir o filtrar.
         *
         * Con la bandeja vacía había tres capas diciendo lo mismo: la cabecera («Historial de
         * avisos recibidos»), una tarjeta repitiéndolo («Historial de avisos · Todo está
         * revisado») y un vacío debajo. Y unos filtros para elegir entre ninguna cosa y
         * ninguna otra.
         */
        if (notifications.isNotEmpty()) {
            item {
                NotificationInboxSummary(
                    unreadCount = notifications.count { !it.read },
                    actionCount = notifications.count { it.category().requiresAction }
                )
            }
            item {
                NotificationFilterBar(
                    selected = selectedFilter,
                    onSelected = { selectedFilter = it }
                )
            }
        }
        if (grouped.isEmpty()) {
            item {
                EmptyNotifications(
                    filter = selectedFilter,
                    inboxIsEmpty = notifications.isEmpty(),
                    onSettingsClick = onSettingsClick
                )
            }
        } else {
            grouped.forEach { (section, items) ->
                item {
                    Text(
                        text = section,
                        color = NotificationText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 5.dp, start = 2.dp)
                    )
                }
                items.forEach { notification ->
                    item(key = notification.id) {
                        NotificationHistoryCard(
                            item = notification,
                            onClick = { onNotificationClick(notification.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationHistoryHeader(
    unreadCount: Int,
    canMarkAllRead: Boolean,
    onBackClick: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = NotificationAccentText
            )
        }
        Spacer(modifier = Modifier.width(5.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = "Notificaciones",
                color = NotificationText,
                fontSize = 28.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (unreadCount) {
                    0 -> "Historial de avisos recibidos"
                    1 -> "1 aviso sin revisar"
                    else -> "$unreadCount avisos sin revisar"
                },
                color = NotificationMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Box {
            IconButton(onClick = { menuExpanded = true }, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.MoreVert, contentDescription = "Mas opciones", tint = NotificationAccentText)
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
                containerColor = NotificationCard
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            "Marcar todo como visto",
                            color = if (canMarkAllRead) NotificationText else NotificationMuted.copy(alpha = 0.55f)
                        )
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.DoneAll,
                            contentDescription = null,
                            tint = if (canMarkAllRead) NotificationPrimary else NotificationMuted.copy(alpha = 0.55f)
                        )
                    },
                    enabled = canMarkAllRead,
                    onClick = {
                        menuExpanded = false
                        onMarkAllRead()
                    }
                )
                DropdownMenuItem(
                    text = { Text("Configurar recordatorios", color = NotificationText) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = NotificationPrimary)
                    },
                    onClick = {
                        menuExpanded = false
                        onSettingsClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun NotificationInboxSummary(
    unreadCount: Int,
    actionCount: Int
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(NotificationPrimary.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.NotificationsNone, contentDescription = null, tint = NotificationPrimary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (unreadCount == 0) "Todo revisado" else "$unreadCount sin revisar",
                    color = NotificationText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (unreadCount == 0) {
                        "Aquí queda lo que te ha llegado."
                    } else {
                        "Toca uno para abrir lo que lo provocó."
                    },
                    color = NotificationMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            if (actionCount > 0) {
                Surface(
                    shape = AppShapes.Pill,
                    color = UniStackColors.Coral.copy(alpha = 0.13f)
                ) {
                    Text(
                        "$actionCount con acción",
                        color = UniStackColors.Coral,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationSummaryStat(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = AppShapes.SmallCard,
        color = NotificationCard.copy(alpha = if (UniStackColors.IsDarkTheme) 0.62f else 0.82f)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value.toString(), color = color, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = NotificationMuted, fontSize = 10.sp, lineHeight = 12.sp, maxLines = 1)
        }
    }
}

@Composable
private fun NotificationFilterBar(
    selected: NotificationFilter,
    onSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.SmallCard)
            .background(NotificationFilterSurface.copy(alpha = 0.72f))
            .horizontalScroll(rememberScrollState())
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NotificationFilter.entries.filterNot { it == NotificationFilter.READ }.forEach { filter ->
            val isSelected = selected == filter
            Surface(
                onClick = { onSelected(filter) },
                shape = AppShapes.Pill,
                color = if (isSelected) NotificationPrimary else Color.Transparent,
                border = if (isSelected) null else BorderStroke(1.dp, NotificationBorder.copy(alpha = 0.45f)),
                tonalElevation = 0.dp
            ) {
                Text(
                    text = filter.label,
                    color = if (isSelected) UniStackColors.OnPrimary else NotificationMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationHistoryCard(
    item: NotificationHistoryItem,
    onClick: () -> Unit
) {
    val category = item.category()
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = NotificationCard,
        border = BorderStroke(
            1.dp,
            if (category.requiresAction && !item.read) category.color.copy(alpha = 0.36f) else NotificationBorder
        )
    ) {
        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(AppShapes.Small)
                        .background(category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.color,
                        modifier = Modifier.size(23.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NotificationCategoryPill(category)
                        Spacer(Modifier.width(7.dp))
                        NotificationStatusPill(item = item, compact = true)
                    }
                    Text(
                        text = item.title,
                        color = NotificationText,
                        fontSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = if (!item.read) FontWeight.ExtraBold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.body,
                        color = NotificationBody,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = NotificationMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (category.requiresAction) {
                    Text(
                        "Requiere accion",
                        color = category.color,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.width(10.dp))
                }
                Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = NotificationMuted, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = item.timeLabel(),
                    color = NotificationMuted,
                    fontSize = 11.sp,
                    lineHeight = 13.sp
                )
            }
        }
    }
}

@Composable
private fun NotificationCategoryPill(category: NotificationCategory) {
    Surface(
        shape = AppShapes.Pill,
        color = category.color.copy(alpha = 0.13f)
    ) {
        Text(
            category.label,
            color = category.color,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun NotificationDetailScreen(
    notificationId: Int,
    onBackClick: () -> Unit,
    onOpenRelated: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)
    val context = LocalContext.current
    val notifications by remember(context) {
        NotificationHistoryStore.observe(context)
    }.collectAsStateWithLifecycle()
    val notification = notifications.firstOrNull { it.id == notificationId }

    LaunchedEffect(notificationId) {
        NotificationHistoryStore.markRead(context, notificationId)
    }

    if (notification == null) {
        EmptyNotificationDetail(onBackClick = onBackClick, modifier = modifier)
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NotificationSurface)
            .statusBarsPadding()
    ) {
        NotificationDetailHeader(onBackClick = onBackClick)
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 18.dp, top = 6.dp, end = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { NotificationDetailHero(notification) }
            item { NotificationMetaCard(notification) }
            item { NotificationHintCard(notification) }
        }
        NotificationDetailActions(
            item = notification,
            onOpenRelated = notification.targetRoute?.let { route ->
                { onOpenRelated(route) }
            },
            onDelete = {
                NotificationHistoryStore.delete(context, notification.id)
                onBackClick()
            }
        )
    }
}

@Composable
private fun NotificationDetailHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = NotificationAccentText)
        }
        Text(
            text = "Detalle del aviso",
            color = NotificationText,
            fontSize = 19.sp,
            lineHeight = 23.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun NotificationDetailHero(item: NotificationHistoryItem) {
    val category = item.category()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Column(
            modifier = Modifier
                .background(NotificationHeroBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(AppShapes.SmallCard)
                        .background(category.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), verticalAlignment = Alignment.CenterVertically) {
                        NotificationCategoryPill(category)
                        NotificationStatusPill(item = item, compact = true)
                    }
                    Text(
                        text = if (item.read) "Aviso revisado" else "Aviso nuevo",
                        color = NotificationMuted,
                        fontSize = 12.sp,
                        lineHeight = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = item.title,
                    color = NotificationText,
                    fontSize = 24.sp,
                    lineHeight = 29.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.body,
                    color = NotificationBody,
                    fontSize = 14.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailMessage(item: NotificationHistoryItem) {
    val category = item.category()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Column(
            modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(category.color.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(category.icon, contentDescription = null, tint = category.color, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = item.title,
                    color = NotificationText,
                    fontSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            Text(
                text = item.body,
                color = NotificationBody,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
private fun NotificationMetaCard(item: NotificationHistoryItem) {
    val zoned = Instant.ofEpochMilli(item.timestampMillis).atZone(ZoneId.systemDefault())
    val date = zoned.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("es-CO")))
    val time = zoned.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)) {
            NotificationMetaRow(
                icon = Icons.Rounded.Campaign,
                label = "Recibida",
                value = date
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(NotificationBorder)
            )
            NotificationMetaRow(
                icon = Icons.Rounded.AccessTime,
                label = "Hora",
                value = time
            )
        }
    }
}

@Composable
private fun NotificationMetaRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(NotificationPrimary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = NotificationAccentText, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = label,
            color = NotificationMuted,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            color = NotificationText,
            fontSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun NotificationHintCard(item: NotificationHistoryItem) {
    val category = item.category()
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Small,
        color = category.color.copy(alpha = if (UniStackColors.IsDarkTheme) 0.12f else 0.09f),
        border = BorderStroke(1.dp, category.color.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(category.color.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Rounded.Lightbulb, contentDescription = null, tint = category.color, modifier = Modifier.size(21.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "Siguiente paso",
                    color = category.color,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = category.hint,
                    color = NotificationBody,
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NotificationDetailActions(
    item: NotificationHistoryItem,
    onOpenRelated: (() -> Unit)?,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NotificationSurface,
        border = BorderStroke(1.dp, NotificationBorder.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = AppShapes.Pill,
                color = UniStackColors.Coral.copy(alpha = if (UniStackColors.IsDarkTheme) 0.14f else 0.12f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = UniStackColors.Coral, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Eliminar", color = UniStackColors.Coral, fontWeight = FontWeight.SemiBold)
                }
            }
            if (onOpenRelated != null) {
                Surface(
                    onClick = onOpenRelated,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp),
                    shape = AppShapes.Pill,
                    color = NotificationPrimary
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Abrir", color = UniStackColors.OnPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(7.dp))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = UniStackColors.OnPrimary, modifier = Modifier.size(19.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.weight(1.25f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = UniStackColors.Green, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text("Vista", color = NotificationMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun NotificationStatusPill(
    item: NotificationHistoryItem,
    compact: Boolean = false
) {
    val visual = item.visual()
    Surface(
        shape = AppShapes.Pill,
        color = visual.color.copy(alpha = 0.13f)
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 10.dp,
                vertical = if (compact) 3.dp else 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                visual.icon,
                contentDescription = null,
                tint = visual.color,
                modifier = Modifier.size(if (compact) 12.dp else 14.dp)
            )
            Text(
                visual.status,
                color = visual.color,
                fontSize = if (compact) 9.sp else 11.sp,
                lineHeight = if (compact) 11.sp else 14.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun EmptyNotifications(
    filter: NotificationFilter,
    inboxIsEmpty: Boolean,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 56.dp, bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(NotificationPrimary.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.NotificationsNone,
                contentDescription = null,
                tint = NotificationPrimary,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            if (inboxIsEmpty) "Todavía no te ha llegado nada" else "Nada con este filtro",
            color = NotificationText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (inboxIsEmpty) {
                "Cuando la app te avise de una entrega, una clase o una nota, el aviso queda " +
                    "guardado aquí para que puedas volver a leerlo."
            } else {
                when (filter) {
                    NotificationFilter.ACTIONS -> "Ningún aviso pide que hagas algo ahora mismo."
                    NotificationFilter.ACADEMIC -> "No hay avisos de notas ni de cortes."
                    NotificationFilter.TASKS -> "No hay avisos de tareas ni de entregas."
                    NotificationFilter.CLASSES -> "No hay avisos de clases ni de asistencia."
                    NotificationFilter.UNREAD -> "No te queda ninguno por revisar."
                    NotificationFilter.READ -> "Todavía no has revisado ninguno."
                    NotificationFilter.ALL -> "No hay avisos guardados."
                }
            },
            color = NotificationMuted,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        // Desde el vacío se llega a lo único que se puede hacer aquí: decidir qué quieres que
        // te avise. Antes era una pantalla en blanco sin salida.
        if (inboxIsEmpty) {
            Spacer(Modifier.height(4.dp))
            Surface(
                onClick = onSettingsClick,
                shape = AppShapes.Pill,
                color = NotificationPrimary.copy(alpha = 0.14f)
            ) {
                Text(
                    "Elegir qué te avisa",
                    color = NotificationPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyNotificationDetail(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NotificationSurface)
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Este aviso ya no esta disponible.", color = NotificationText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onBackClick) {
            Text("Volver")
        }
    }
}

private data class NotificationVisual(
    val status: String,
    val color: Color,
    val icon: ImageVector
)

private enum class NotificationKind {
    ACADEMIC,
    TASK,
    CLASS,
    SUMMARY,
    SYSTEM
}

private data class NotificationCategory(
    val label: String,
    val kind: NotificationKind,
    val color: Color,
    val icon: ImageVector,
    val hint: String,
    val requiresAction: Boolean
)

private fun NotificationHistoryItem.visual(): NotificationVisual {
    return when {
        read -> NotificationVisual("Vista", UniStackColors.Green, Icons.Rounded.CheckCircle)
        else -> NotificationVisual("Nueva", UniStackColors.Blue, Icons.Rounded.Campaign)
    }
}

private fun NotificationHistoryItem.category(): NotificationCategory {
    val text = "$title $body".lowercase(Locale.ROOT)
    return when {
        "resumen" in text || "dia despejado" in text || "día despejado" in text -> NotificationCategory(
            label = "Resumen",
            kind = NotificationKind.SUMMARY,
            color = UniStackColors.Blue,
            icon = Icons.Rounded.Event,
            hint = "Revisa tu agenda y decide el siguiente movimiento del dia.",
            requiresAction = false
        )
        "clase" in text || "asististe" in text -> NotificationCategory(
            label = "Clase",
            kind = NotificationKind.CLASS,
            color = UniStackColors.Teal,
            icon = Icons.Rounded.School,
            hint = "Registra asistencia, modalidad o cambios para mantener tu horario al dia.",
            requiresAction = "asististe" in text || "asistencia" in text
        )
        "tarea" in text || "trabajo" in text || "entrega" in text -> NotificationCategory(
            label = "Entrega",
            kind = NotificationKind.TASK,
            color = UniStackColors.Yellow,
            icon = Icons.Rounded.TaskAlt,
            hint = "Abre la actividad para actualizar estado, hora limite o nota obtenida.",
            requiresAction = true
        )
        "nota" in text || "promedio" in text || "corte" in text || "materia" in text -> NotificationCategory(
            label = "Academico",
            kind = NotificationKind.ACADEMIC,
            color = UniStackColors.Primary,
            icon = Icons.Rounded.School,
            hint = "Completa notas, pesos o cortes anteriores para mejorar la proyeccion.",
            requiresAction = true
        )
        else -> NotificationCategory(
            label = "Aviso",
            kind = NotificationKind.SYSTEM,
            color = UniStackColors.Primary,
            icon = Icons.Rounded.NotificationsNone,
            hint = "Mantener tus datos al dia ayuda a UniStack a priorizar mejor.",
            requiresAction = false
        )
    }
}

private fun NotificationHistoryItem.timeLabel(): String {
    return Instant.ofEpochMilli(timestampMillis)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
}

private fun NotificationHistoryItem.dateSectionLabel(): String {
    val date = Instant.ofEpochMilli(timestampMillis).atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    return when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO")))
    }
}
