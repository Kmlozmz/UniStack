@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_notifications.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.UniDropdownMenu
import com.unistack.app.core.design.components.UniIconButton
import com.unistack.app.core.design.components.UniIconButtonVariant
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.notifications.NotificationHistoryItem
import com.unistack.app.core.notifications.NotificationHistoryStore
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.LocalIsDarkTheme
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos
private enum class NotificationFilter(val labelRes: Int) {
    ALL(R.string.notif_tab_all),
    ACTIONS(R.string.notif_tab_actions),
    ACADEMIC(R.string.notif_tab_academic),
    TASKS(R.string.notif_tab_tasks),
    CLASSES(R.string.notif_tab_classes),
    UNREAD(R.string.notif_tab_unread),
    READ(R.string.notif_tab_read);

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
    @Composable get() = MaterialTheme.colorScheme.background
private val NotificationCard: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerLow
private val NotificationFilterSurface: Color
    @Composable get() = MaterialTheme.colorScheme.surfaceContainerHigh
private val NotificationBorder: Color
    @Composable get() = MaterialTheme.colorScheme.outlineVariant
private val NotificationPrimary: Color
    @Composable get() = MaterialTheme.colorScheme.primary
private val NotificationAccentText: Color
    @Composable get() = MaterialTheme.colorScheme.onPrimaryContainer
private val NotificationText: Color
    @Composable get() = MaterialTheme.colorScheme.onSurface
private val NotificationBody: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val NotificationMuted: Color
    @Composable get() = MaterialTheme.colorScheme.onSurfaceVariant
private val NotificationHeroBrush: Brush
    @Composable get() = SolidColor(MaterialTheme.colorScheme.primaryContainer)

@Composable
fun NotificationHistoryScreen(
    onBackClick: () -> Unit,
    onNotificationClick: (Int) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        // Medidas tomadas de la réplica interactiva: 14 dp de margen y 8 dp entre avisos.
        contentPadding = PaddingValues(start = 14.dp, top = 4.dp, end = 14.dp, bottom = scrollBottomRoom),
        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                    notifications = notifications,
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
                        fontSize = 12.5.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 9.dp, start = 2.dp)
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
    /*
     * Los botones arriba, el título debajo — y no el título entre los dos.
     *
     * Con el título en medio de la fila, «Notificaciones» compite por el ancho con el atrás y
     * el «⋮», así que había que encogerlo. Puesto debajo cabe entero y a su tamaño, que es lo
     * que hace la réplica y el resto de cabeceras de la app.
     */
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UniBackButton(onClick = onBackClick)
            Spacer(modifier = Modifier.weight(1f))
            Box {
                UniIconButton(
                    icon = Icons.Rounded.MoreVert,
                    contentDescription = stringResource(R.string.notif_more_options),
                    variant = UniIconButtonVariant.Surface,
                    onClick = { menuExpanded = true }
                )
                NotificationHeaderMenu(
                    expanded = menuExpanded,
                    canMarkAllRead = canMarkAllRead,
                    onDismiss = { menuExpanded = false },
                    onMarkAllRead = onMarkAllRead,
                    onSettingsClick = onSettingsClick
                )
            }
        }
        Column(modifier = Modifier.padding(start = 2.dp, top = 2.dp)) {
            Text(
                text = stringResource(R.string.notif_title),
                color = NotificationText,
                fontSize = 23.sp,
                lineHeight = 27.sp,
                letterSpacing = (-0.02).em,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = when (unreadCount) {
                    0 -> stringResource(R.string.notif_subtitle)
                    1 -> stringResource(R.string.notif_unread_single)
                    else -> stringResource(R.string.notif_unread_multiple, unreadCount)
                },
                color = NotificationMuted,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun NotificationHeaderMenu(
    expanded: Boolean,
    canMarkAllRead: Boolean,
    onDismiss: () -> Unit,
    onMarkAllRead: () -> Unit,
    onSettingsClick: () -> Unit
) {
    UniDropdownMenu(
                expanded = expanded,
                onDismissRequest = onDismiss,
                containerColor = NotificationCard
            ) {
                DropdownMenuItem(
                    text = {
                        Text(
                            stringResource(R.string.notif_mark_all_read),
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
                        onDismiss()
                        onMarkAllRead()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.notif_configure_reminders), color = NotificationText) },
                    leadingIcon = {
                        Icon(Icons.Rounded.Settings, contentDescription = null, tint = NotificationPrimary)
                    },
                    onClick = {
                        onDismiss()
                        onSettingsClick()
                    }
                )
    }
}

@Composable
private fun NotificationInboxSummary(
    unreadCount: Int,
    actionCount: Int
) {
    // Sin contorno y más bajo: es un resumen, no una tarjeta más de la lista.
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = NotificationCard
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NotificationPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.NotificationsNone,
                    contentDescription = null,
                    tint = NotificationPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    if (unreadCount == 0) stringResource(R.string.notif_all_reviewed) else stringResource(R.string.notif_unreviewed_count, unreadCount),
                    color = NotificationText,
                    fontSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (unreadCount == 0) {
                        stringResource(R.string.notif_history_desc_1)
                    } else {
                        stringResource(R.string.notif_history_desc_2)
                    },
                    color = NotificationMuted,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            if (actionCount > 0) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.13f)
                ) {
                    Text(
                        stringResource(R.string.notif_with_action_count, actionCount),
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 10.5.sp,
                        lineHeight = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}


@Composable
private fun NotificationFilterBar(
    selected: NotificationFilter,
    notifications: List<NotificationHistoryItem>,
    onSelected: (NotificationFilter) -> Unit
) {
    // Sin bandeja detrás: los chips ya se separan solos con su contorno, y el recuadro gris
    // añadía un escalón de superficie más entre el fondo y las tarjetas.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        NotificationFilter.entries.filterNot { it == NotificationFilter.READ }.forEach { filter ->
            val isSelected = selected == filter
            // Cada filtro lleva su cuenta: sin ella, tocar «Clases» es tirar a ciegas y a veces
            // caer en una lista vacía que no avisaba de nada antes de tocarla.
            val count = notifications.count(filter::matches)
            Surface(
                onClick = { onSelected(filter) },
                shape = CircleShape,
                color = if (isSelected) NotificationPrimary else Color.Transparent,
                border = if (isSelected) null else BorderStroke(1.dp, NotificationBorder.copy(alpha = 0.45f)),
                tonalElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(filter.labelRes),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else NotificationMuted,
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = count.toString(),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        } else {
                            NotificationMuted.copy(alpha = 0.65f)
                        },
                        fontSize = 11.sp,
                        lineHeight = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
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
    /*
     * Una fila, una columna, y la hora dentro.
     *
     * Antes eran dos filas: arriba el aviso y abajo una tira con «requiere acción», un reloj y
     * la hora. Esa segunda tira repetía en palabras lo que ya decían el color y la flecha, y le
     * daba a cada aviso el alto de dos. Lo leído baja de opacidad en vez de cambiar de grosor:
     * quince filas del mismo peso no dejan ver por dónde vas.
     */
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .alpha(if (item.read) 0.58f else 1f),
        shape = RoundedCornerShape(16.dp),
        color = NotificationCard
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(category.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = category.color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(11.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    NotificationCategoryPill(category)
                    Spacer(Modifier.width(5.dp))
                    NotificationStatusPill(item = item, compact = true)
                }
                Spacer(Modifier.height(3.dp))
                Text(
                    text = item.title,
                    color = NotificationText,
                    fontSize = 13.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.body,
                    color = NotificationBody,
                    fontSize = 11.5.sp,
                    lineHeight = 15.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.timeLabel(),
                    color = NotificationMuted.copy(alpha = 0.72f),
                    fontSize = 9.5.sp,
                    lineHeight = 12.sp,
                    modifier = Modifier.padding(top = 5.dp)
                )
            }
            // La flecha sale sólo si hay a dónde ir. Estaba en todas, y doce de quince no
            // guardan destino: prometía una pantalla que no existe.
            if (item.targetRoute != null) {
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = NotificationMuted,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun NotificationCategoryPill(category: NotificationCategory) {
    // Rectángulo de esquina corta, no pastilla: dos pastillas juntas se leen como un control
    // partido en dos, y esto es un rótulo.
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = category.color.copy(alpha = 0.15f)
    ) {
        Text(
            category.label.uppercase(Locale.getDefault()),
            color = category.color,
            fontSize = 9.sp,
            lineHeight = 11.sp,
            letterSpacing = 0.04.em,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
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
        UniBackButton(onClick = onBackClick)
        Text(
            text = stringResource(R.string.notif_detail_title),
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
        shape = MaterialTheme.shapes.small,
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
                        .clip(MaterialTheme.shapes.medium)
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
                        text = if (item.read) stringResource(R.string.notif_reviewed_badge) else stringResource(R.string.notif_new_badge),
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
private fun NotificationMetaCard(item: NotificationHistoryItem) {
    val zoned = Instant.ofEpochMilli(item.timestampMillis).atZone(ZoneId.systemDefault())
    val datePattern = stringResource(R.string.notif_d_de_mmmm_yyyy)
    val date = zoned.format(DateTimeFormatter.ofPattern(datePattern, Locale.getDefault()))
    val time = zoned.format(DateTimeFormatter.ofPattern("HH:mm", Locale.US))
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.small,
        color = NotificationCard,
        border = BorderStroke(1.dp, NotificationBorder)
    ) {
        Column(modifier = Modifier.padding(horizontal = 15.dp, vertical = 4.dp)) {
            NotificationMetaRow(
                icon = Icons.Rounded.Campaign,
                label = stringResource(R.string.notif_received_label),
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
                label = stringResource(R.string.notif_time_label),
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
        shape = MaterialTheme.shapes.small,
        color = category.color.copy(alpha = if (LocalIsDarkTheme.current) 0.12f else 0.09f),
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
                    text = stringResource(R.string.notif_next_step),
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
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error.copy(alpha = if (LocalIsDarkTheme.current) 0.14f else 0.12f)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(19.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(stringResource(R.string.notif_btn_delete), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
                }
            }
            if (onOpenRelated != null) {
                Surface(
                    onClick = onOpenRelated,
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp),
                    shape = CircleShape,
                    color = NotificationPrimary
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.notif_btn_open), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(7.dp))
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(19.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.weight(1.25f),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = LocalSectionColors.current.onTrack, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(7.dp))
                    Text(stringResource(R.string.notif_seen_badge), color = NotificationMuted, fontSize = 13.sp, fontWeight = FontWeight.Medium)
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
    // En la fila es un rótulo gemelo del de categoría: misma esquina, mismo cuerpo y sin icono
    // —el icono a 12 dp junto a un texto de 9 no se distingue, sólo ensancha. En el detalle sí
    // se abre a pastilla con su icono, que ahí hay sitio y es el estado de la pantalla entera.
    Surface(
        shape = if (compact) RoundedCornerShape(6.dp) else CircleShape,
        color = visual.color.copy(alpha = if (compact) 0.15f else 0.13f)
    ) {
        if (compact) {
            Text(
                visual.status.uppercase(Locale.getDefault()),
                color = visual.color,
                fontSize = 9.sp,
                lineHeight = 11.sp,
                letterSpacing = 0.04.em,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
            )
        } else {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    visual.icon,
                    contentDescription = null,
                    tint = visual.color,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    visual.status,
                    color = visual.color,
                    fontSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
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
            if (inboxIsEmpty) stringResource(R.string.notif_empty_title_default) else stringResource(R.string.notif_empty_title_filtered),
            color = NotificationText,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (inboxIsEmpty) {
                stringResource(R.string.notif_empty_desc_default)
            } else {
                when (filter) {
                    NotificationFilter.ACTIONS -> stringResource(R.string.notif_empty_actions)
                    NotificationFilter.ACADEMIC -> stringResource(R.string.notif_empty_academic)
                    NotificationFilter.TASKS -> stringResource(R.string.notif_empty_tasks)
                    NotificationFilter.CLASSES -> stringResource(R.string.notif_empty_classes)
                    NotificationFilter.UNREAD -> stringResource(R.string.notif_empty_unread)
                    NotificationFilter.READ -> stringResource(R.string.notif_empty_read)
                    NotificationFilter.ALL -> stringResource(R.string.notif_empty_saved)
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
                shape = CircleShape,
                color = NotificationPrimary.copy(alpha = 0.14f)
            ) {
                Text(
                    stringResource(R.string.notif_choose_what_notifies),
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
        Text(stringResource(R.string.notif_unavailable), color = NotificationText, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))
        TextButton(onClick = onBackClick) {
            Text(stringResource(R.string.notif_back))
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
    val icon: ImageVector,
    val hint: String,
    val requiresAction: Boolean
)

/**
 * El color de una categoria, derivado de su tipo.
 *
 * Vive fuera de [NotificationCategory] a proposito: clasificar una notificacion es leer su
 * texto, y eso no depende del tema. Teniendo el color dentro, clasificar exigia contexto
 * composable y el filtro de la lista -que solo mira el tipo- no podia ejecutarse dentro de
 * un remember.
 */
private val NotificationCategory.color: Color
    @Composable
    @ReadOnlyComposable
    get() = when (kind) {
        NotificationKind.SUMMARY -> LocalSectionColors.current.schedule
        NotificationKind.CLASS -> MaterialTheme.colorScheme.tertiary
        NotificationKind.TASK -> LocalSectionColors.current.atRisk
        NotificationKind.ACADEMIC -> MaterialTheme.colorScheme.primary
        NotificationKind.SYSTEM -> MaterialTheme.colorScheme.primary
    }

@Composable
@ReadOnlyComposable
private fun NotificationHistoryItem.visual(): NotificationVisual {
    return when {
        read -> NotificationVisual(stringResource(R.string.notif_seen_badge), LocalSectionColors.current.onTrack, Icons.Rounded.CheckCircle)
        else -> NotificationVisual(stringResource(R.string.notif_new_badge_short), LocalSectionColors.current.schedule, Icons.Rounded.Campaign)
    }
}

private fun NotificationHistoryItem.category(): NotificationCategory {
    val text = "$title $body".lowercase(Locale.ROOT)
    return when {
        "resumen" in text || "summary" in text || "dia despejado" in text || "clear day" in text || "día despejado" in text -> NotificationCategory(
            label = Textos.get(R.string.notif_cat_summary),
            kind = NotificationKind.SUMMARY,
            icon = Icons.Rounded.Event,
            hint = Textos.get(R.string.notif_revisa_tu_agenda_y_decide_el),
            requiresAction = false
        )
        "clase" in text || "class" in text || "asististe" in text || "attend" in text -> NotificationCategory(
            label = Textos.get(R.string.notif_cat_class),
            kind = NotificationKind.CLASS,
            icon = Icons.Rounded.School,
            hint = Textos.get(R.string.notif_registra_asistencia_modalidad_o_cambios_para),
            requiresAction = "asististe" in text || "asistencia" in text || "attendance" in text
        )
        "tarea" in text || "task" in text || "trabajo" in text || "assignment" in text || "entrega" in text || "due" in text -> NotificationCategory(
            label = Textos.get(R.string.notif_cat_delivery),
            kind = NotificationKind.TASK,
            icon = Icons.Rounded.TaskAlt,
            hint = Textos.get(R.string.notif_abre_la_actividad_para_actualizar_estado),
            requiresAction = true
        )
        "nota" in text || "grade" in text || "promedio" in text || "gpa" in text || "corte" in text || "materia" in text || "subject" in text -> NotificationCategory(
            label = Textos.get(R.string.notif_academico),
            kind = NotificationKind.ACADEMIC,
            icon = Icons.Rounded.School,
            hint = Textos.get(R.string.notif_completa_notas_pesos_o_cortes_anteriores),
            requiresAction = true
        )
        else -> NotificationCategory(
            label = Textos.get(R.string.notif_cat_notice),
            kind = NotificationKind.SYSTEM,
            icon = Icons.Rounded.NotificationsNone,
            hint = Textos.get(R.string.notif_mantener_tus_datos_al_dia_ayuda),
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
    val isEn = Locale.getDefault().language == "en"
    return when (date) {
        today -> Textos.get(R.string.notif_time_today)
        today.minusDays(1) -> Textos.get(R.string.notif_time_yesterday)
        else -> date.format(
            if (isEn) DateTimeFormatter.ofPattern("MMMM d", Locale.ENGLISH)
            else DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es-CO"))
        )
    }
}
