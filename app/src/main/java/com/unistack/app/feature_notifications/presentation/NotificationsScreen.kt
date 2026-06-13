package com.unistack.app.feature_notifications.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Settings
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    UNREAD("No leídas"),
    READ("Vistas")
}

private val NotificationSurface: Color
    @Composable get() = UniStackColors.Background
private val NotificationCard: Color
    @Composable get() = UniStackColors.Card
private val NotificationFilterSurface: Color
    @Composable get() = UniStackColors.SurfaceVariant
private val NotificationBorder: Color
    @Composable get() = UniStackColors.SoftOutline
private val NotificationPurple: Color
    @Composable get() = UniStackColors.Primary
private val NotificationAccentText: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFDDB7FF) else UniStackColors.PrimaryDark
private val NotificationText: Color
    @Composable get() = UniStackColors.TextPrimary
private val NotificationBody: Color
    @Composable get() = UniStackColors.TextSecondary
private val NotificationMuted: Color
    @Composable get() = if (UniStackColors.IsDarkTheme) Color(0xFFA8ADBC) else UniStackColors.TextSecondary
private val NotificationHeroBrush: Brush
    @Composable get() = if (UniStackColors.IsDarkTheme) {
        Brush.linearGradient(listOf(Color(0xFF130A2C), Color(0xFF25104F)))
    } else {
        Brush.linearGradient(listOf(Color(0xFFF7F2FF), Color(0xFFE9DDFF)))
    }

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
        notifications.filter { item ->
            when (selectedFilter) {
                NotificationFilter.ALL -> true
                NotificationFilter.UNREAD -> !item.read
                NotificationFilter.READ -> item.read
            }
        }
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
        contentPadding = PaddingValues(start = 18.dp, top = 8.dp, end = 18.dp, bottom = 28.dp),
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
        item {
            NotificationFilterBar(
                selected = selectedFilter,
                onSelected = { selectedFilter = it }
            )
        }
        if (grouped.isEmpty()) {
            item {
                EmptyNotifications(filter = selectedFilter)
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
                text = "Historial de notificaciones",
                color = NotificationText,
                fontSize = 21.sp,
                lineHeight = 25.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = when (unreadCount) {
                    0 -> "Consulta los avisos que recibió tu teléfono"
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
                Icon(Icons.Rounded.MoreVert, contentDescription = "Más opciones", tint = NotificationAccentText)
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
                            tint = if (canMarkAllRead) NotificationPurple else NotificationMuted.copy(alpha = 0.55f)
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
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = NotificationPurple)
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
private fun NotificationFilterBar(
    selected: NotificationFilter,
    onSelected: (NotificationFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.Pill)
            .background(NotificationFilterSurface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        NotificationFilter.entries.forEach { filter ->
            val isSelected = selected == filter
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onSelected(filter) },
                shape = AppShapes.Pill,
                color = if (isSelected) NotificationPurple else Color.Transparent,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = filter.label,
                    color = if (isSelected) Color.White else NotificationMuted,
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 9.dp)
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
    val visual = item.visual()
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!item.read) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(NotificationPurple)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(visual.color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.color,
                    modifier = Modifier.size(21.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = item.title,
                    color = NotificationText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = if (!item.read) FontWeight.Bold else FontWeight.SemiBold,
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NotificationStatusPill(item = item, compact = true)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.timeLabel(),
                        color = NotificationMuted,
                        fontSize = 10.sp,
                        lineHeight = 13.sp
                    )
                }
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = NotificationMuted,
                modifier = Modifier.size(18.dp)
            )
        }
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
            contentPadding = PaddingValues(start = 18.dp, top = 4.dp, end = 18.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                NotificationHero()
            }
            item {
                NotificationDetailCopy(notification)
            }
            item {
                NotificationScheduleSummary(notification)
            }
            item {
                NotificationHintStrip(notification)
            }
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
            .height(52.dp)
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver", tint = NotificationAccentText)
        }
        Text(
            text = "Detalle del aviso",
            color = NotificationText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun NotificationHero() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(164.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(NotificationHeroBrush),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.notification_bell_cutout),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(154.dp)
        )
    }
}

@Composable
private fun NotificationDetailCopy(item: NotificationHistoryItem) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        NotificationStatusPill(item = item)
        Text(
            text = item.title,
            color = NotificationText,
            fontSize = 23.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            text = item.body,
            color = NotificationBody,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
private fun NotificationScheduleSummary(item: NotificationHistoryItem) {
    val zoned = Instant.ofEpochMilli(item.timestampMillis).atZone(ZoneId.systemDefault())
    val date = zoned.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-CO")))
    val time = zoned.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NotificationPurple.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Campaign,
                    contentDescription = null,
                    tint = NotificationAccentText,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = "Recibida",
                    color = NotificationMuted,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
                Text(
                    text = "$date · $time",
                    color = NotificationText,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Icon(Icons.Rounded.AccessTime, contentDescription = null, tint = NotificationMuted, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun NotificationHintStrip(item: NotificationHistoryItem) {
    val hint = when {
        "nota" in item.body.lowercase() -> "Registrar tus notas activa proyecciones y alertas personalizadas."
        "tarea" in item.body.lowercase() -> "Asignar una hora permite que el recordatorio llegue en el momento adecuado."
        "gasto" in item.body.lowercase() -> "Registrar gastos con frecuencia mejora el resumen semanal."
        else -> "Mantener tus datos al día ayuda a UniStack a priorizar mejor."
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(NotificationPurple.copy(alpha = if (UniStackColors.IsDarkTheme) 0.10f else 0.07f))
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Rounded.Lightbulb,
            contentDescription = null,
            tint = NotificationAccentText,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(11.dp))
        Text(
            text = hint,
            color = NotificationBody,
            fontSize = 12.sp,
            lineHeight = 17.sp,
            fontWeight = FontWeight.Normal
        )
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
                .padding(horizontal = 18.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDelete) {
                Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = UniStackColors.Coral)
                Spacer(modifier = Modifier.width(7.dp))
                Text(
                    text = "Eliminar",
                    color = UniStackColors.Coral,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            if (onOpenRelated != null) {
                TextButton(onClick = onOpenRelated) {
                    Text(
                        text = "Abrir contenido",
                        color = NotificationAccentText,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Icon(
                        Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        tint = NotificationAccentText
                    )
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
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
private fun EmptyNotifications(filter: NotificationFilter) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 72.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(CircleShape)
                .background(NotificationPurple.copy(alpha = 0.11f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.NotificationsNone,
                contentDescription = null,
                tint = NotificationPurple,
                modifier = Modifier.size(28.dp)
            )
        }
        Text("Todo tranquilo", color = NotificationText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = when (filter) {
                NotificationFilter.ALL -> "Las notificaciones que recibas quedarán guardadas aquí."
                NotificationFilter.UNREAD -> "No tienes avisos pendientes por revisar."
                NotificationFilter.READ -> "Todavía no has revisado ningún aviso."
            },
            color = NotificationMuted,
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )
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
        Text("Este aviso ya no está disponible.", color = NotificationText, fontWeight = FontWeight.SemiBold)
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

private fun NotificationHistoryItem.visual(): NotificationVisual {
    return when {
        read -> NotificationVisual("Vista", Color(0xFF3FAE67), Icons.Rounded.CheckCircle)
        else -> NotificationVisual("Nueva", Color(0xFF317FE8), Icons.Rounded.Campaign)
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
