@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroupCard
import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.unistack.app.core.design.components.UniSwitch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.contentColorOn
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.feature_user.domain.UserProfile
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos

private val LeadChoices = listOf(1, 3, 6, 12, 24, 48, 72)

/**
 * Notificaciones: primero si Android lo permite, y después qué avisar.
 *
 * El permiso del sistema encabeza la pantalla porque sin él nada de lo de abajo funciona, y
 * antes no se decía en ninguna parte: se podían encender cinco avisos y no llegaba ninguno.
 * Con el permiso denegado, todo lo demás queda atenuado y bloqueado —dejarlo editable «para
 * dejarlo preparado» se lee como que ya está configurado y va a sonar.
 */
@Composable
fun NotificationSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val context = LocalContext.current
    val current = profile ?: return

    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    // Si lo de abajo es un aviso de error o una confirmacion: antes se adivinaba leyendo
    // el texto («Sin el permiso...»), que en ingles ya no empieza asi.
    var feedbackEsError by rememberSaveable { mutableStateOf(false) }
    var granted by remember { mutableStateOf(context.hasNotificationPermission()) }
    var alreadyAsked by rememberSaveable { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { result ->
        granted = result || context.hasNotificationPermission()
        feedbackEsError = !granted
        feedback = if (granted) {
            context.getString(R.string.settings_notif_ready)
        } else {
            context.getString(R.string.settings_notif_no_perm)
        }
    }
    val askForPermission: () -> Unit = {
        if (context.canAskForNotificationPermission(alreadyAsked)) {
            alreadyAsked = true
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            // Ya no hay diálogo que enseñar: al volver de ajustes, el observador del ciclo de
            // vida vuelve a mirar si quedó activado.
            context.openNotificationSettings()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = context.hasNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val lead = current.reminderLeadHours
    val quietStart = current.quietHoursStartHour ?: 22
    val quietEnd = current.quietHoursEndHour ?: 7

    fun setReminders(tasks: Boolean, works: Boolean, overdue: Boolean, hours: Int) {
        granted = context.hasNotificationPermission()
        feedbackEsError = true
        feedback = if (viewModel.updateReminderSettings(tasks, works, overdue, hours)) {
            null
        } else {
            context.getString(R.string.settings_notif_review_lead_time)
        }
    }

    LargeTitleScaffold(
        title = stringResource(R.string.settings_notif_title),
        subtitle = stringResource(R.string.settings_notif_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        item {
            NotificationPermissionCard(
                granted = granted,
                actionLabel = if (context.canAskForNotificationPermission(alreadyAsked)) stringResource(R.string.settings_notif_btn_activate) else stringResource(R.string.settings_notif_btn_settings),
                onRequestPermission = askForPermission
            )
        }
        item {
            Column(modifier = Modifier.alpha(if (granted) 1f else 0.45f)) {
                SettingsGroupCard(label = stringResource(R.string.settings_notif_sec_alerts)) {
                    AlertRow(
                        title = stringResource(R.string.settings_notif_tasks_title),
                        detail = stringResource(R.string.settings_notif_tasks_desc),
                        checked = current.taskRemindersEnabled,
                        enabled = granted
                    ) {
                        setReminders(!current.taskRemindersEnabled, current.academicWorkRemindersEnabled, current.overdueRemindersEnabled, lead)
                    }
                    // El aviso de Trabajos solo se ofrece donde Trabajos se puede abrir.
                    if (BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished) {
                        AlertRow(
                            title = stringResource(R.string.settings_notif_works_title),
                            detail = stringResource(R.string.settings_notif_works_desc),
                            checked = current.academicWorkRemindersEnabled,
                            enabled = granted
                        ) {
                            setReminders(current.taskRemindersEnabled, !current.academicWorkRemindersEnabled, current.overdueRemindersEnabled, lead)
                        }
                    }
                    AlertRow(
                        title = stringResource(R.string.settings_notif_overdue_title),
                        detail = stringResource(R.string.settings_notif_overdue_desc),
                        checked = current.overdueRemindersEnabled,
                        enabled = granted
                    ) {
                        setReminders(current.taskRemindersEnabled, current.academicWorkRemindersEnabled, !current.overdueRemindersEnabled, lead)
                    }
                    AlertRow(
                        title = stringResource(R.string.settings_notif_grades_cuts_title),
                        detail = stringResource(R.string.settings_notif_grades_cuts_desc),
                        checked = current.gradeInsightRemindersEnabled,
                        enabled = granted
                    ) {
                        granted = context.hasNotificationPermission()
                        viewModel.updateAcademicReminderSettings(
                            !current.gradeInsightRemindersEnabled,
                            current.pendingGradeRemindersEnabled
                        )
                    }
                    AlertRow(
                        title = stringResource(R.string.settings_notif_pending_results_title),
                        detail = stringResource(R.string.settings_notif_pending_results_desc),
                        checked = current.pendingGradeRemindersEnabled,
                        enabled = granted
                    ) {
                        granted = context.hasNotificationPermission()
                        viewModel.updateAcademicReminderSettings(
                            current.gradeInsightRemindersEnabled,
                            !current.pendingGradeRemindersEnabled
                        )
                    }
                }
            }
        }
        item {
            Column(modifier = Modifier.alpha(if (granted) 1f else 0.45f)) {
                SettingsGroupLabel(stringResource(R.string.settings_notif_sec_lead_time))
                // Siete opciones en botones y no un campo de texto con su botón de guardar:
                // la anticipación se elige, no se redacta.
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    LeadChoices.forEach { hours ->
                        LeadChip(
                            hours = hours,
                            selected = hours == lead,
                            enabled = granted,
                            modifier = Modifier.weight(1f)
                        ) {
                            setReminders(
                                current.taskRemindersEnabled,
                                current.academicWorkRemindersEnabled,
                                current.overdueRemindersEnabled,
                                hours
                            )
                        }
                    }
                }
                Text(
                    text = stringResource(R.string.settings_notif_lead_time_desc, leadLabel(lead)),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            Column(modifier = Modifier.alpha(if (granted) 1f else 0.45f)) {
                SettingsGroupCard(label = stringResource(R.string.settings_notif_sec_daily_summary)) {
                    DailyDigestRow(
                        profile = current,
                        enabled = granted,
                        onChange = { on, hour, minute ->
                            granted = context.hasNotificationPermission()
                            viewModel.updateDailyDigest(on, hour, minute)
                        }
                    )
                }
            }
        }
        item {
            val dndHoursWarning = stringResource(R.string.settings_notif_dnd_different_hours)
            Column(modifier = Modifier.alpha(if (granted) 1f else 0.45f)) {
                SettingsGroupCard(label = stringResource(R.string.settings_notif_sec_dnd)) {
                    QuietHoursRow(
                        profile = current,
                        enabled = granted,
                        start = quietStart,
                        end = quietEnd,
                        onChange = { on, from, to ->
                            feedbackEsError = true
                            feedback = if (viewModel.updateQuietHours(on, from, to)) {
                                null
                            } else {
                                dndHoursWarning
                            }
                        }
                    )
                }
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (feedbackEsError) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SettingsGroupLabel(text: String) {
    Text(
        text = text,
        style = com.unistack.app.core.design.theme.SectionLabelStyle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 9.dp)
    )
}

/**
 * El permiso del sistema, con el color diciendo en qué estado está.
 */
@Composable
private fun NotificationPermissionCard(
    granted: Boolean,
    actionLabel: String,
    onRequestPermission: () -> Unit
) {
    val sections = LocalSectionColors.current
    val container = if (granted) sections.onTrackContainer else MaterialTheme.colorScheme.errorContainer
    val ink = contentColorOn(container)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = container,
        contentColor = ink
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.small,
                color = if (granted) sections.onTrack else MaterialTheme.colorScheme.error,
                contentColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (granted) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                        contentDescription = null,
                        modifier = Modifier.size(21.dp)
                    )
                }
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
                Text(
                    text = if (granted) stringResource(R.string.settings_notif_perm_enabled) else stringResource(R.string.settings_notif_perm_disabled),
                    style = MaterialTheme.typography.titleMediumEmphasized
                )
                Text(
                    text = if (granted) {
                        stringResource(R.string.settings_notif_perm_msg_enabled)
                    } else {
                        stringResource(R.string.settings_notif_perm_msg_disabled)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = 0.85f)
                )
            }
            if (!granted) {
                Surface(
                    onClick = onRequestPermission,
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ) {
                    Text(
                        text = actionLabel,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Un aviso: su nombre, cuándo llega y su interruptor.
 */
@Composable
private fun AlertRow(
    title: String,
    detail: String,
    checked: Boolean,
    enabled: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (enabled) Modifier.cleanClickable(onClick = onToggle) else Modifier)
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (checked) 1f else 0.55f)
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        UniSwitch(
            checked = checked,
            enabled = enabled,
            onCheckedChange = { onToggle() }
        )
    }
}

/**
 * El silencio, y las dos horas que solo salen cuando está encendido.
 *
 * El título va atado al estado. Cuando era fijo —«Nada suena de noche»— y el interruptor
 * estaba apagado, la fila prometía silencio justo encima de una línea que decía que los
 * avisos suenan a cualquier hora.
 */
@Composable
private fun DailyDigestRow(
    profile: UserProfile,
    enabled: Boolean,
    onChange: (Boolean, Int, Int) -> Unit
) {
    val on = profile.dailyDigestEnabled
    val hour = profile.dailyDigestHour
    val minute = profile.dailyDigestMinute
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (enabled) Modifier.cleanClickable { onChange(!on, hour, minute) } else Modifier)
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.settings_notif_morning_summary_title),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (on) {
                        stringResource(R.string.settings_notif_morning_summary_time, hour, minute)
                    } else {
                        stringResource(R.string.settings_notif_morning_turn_on)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            UniSwitch(
                checked = on,
                enabled = enabled,
                onCheckedChange = { onChange(it, hour, minute) }
            )
        }
        if (on) {
            Row(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HourStepper(
                    label = "Hora",
                    hour = hour,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) { onChange(true, it, minute) }
                MinuteStepper(
                    label = "Minutos",
                    minute = minute,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) { onChange(true, hour, it) }
            }
        }
    }
}

@Composable
private fun QuietHoursRow(
    profile: UserProfile,
    enabled: Boolean,
    start: Int,
    end: Int,
    onChange: (Boolean, Int, Int) -> Unit
) {
    val on = profile.quietHoursEnabled
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (enabled) Modifier.cleanClickable { onChange(!on, start, end) } else Modifier)
                .padding(horizontal = 15.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (on) stringResource(R.string.settings_notif_dnd_active_desc) else stringResource(R.string.settings_notif_dnd_inactive_desc),
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (on) {
                        stringResource(R.string.settings_notif_dnd_range, hourLabel(start), hourLabel(end))
                    } else {
                        stringResource(R.string.settings_notif_dnd_turn_on)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            UniSwitch(
                checked = on,
                enabled = enabled,
                onCheckedChange = { onChange(it, start, end) }
            )
        }
        if (on) {
            Row(
                modifier = Modifier.padding(start = 15.dp, end = 15.dp, bottom = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HourStepper(
                    label = "Desde",
                    hour = start,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) { onChange(true, it, end) }
                HourStepper(
                    label = "Hasta",
                    hour = end,
                    enabled = enabled,
                    modifier = Modifier.weight(1f)
                ) { onChange(true, start, it) }
            }
        }
    }
}

@Composable
private fun LeadChip(
    hours: Int,
    selected: Boolean,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        Text(
            text = leadChipLabel(hours),
            modifier = Modifier.padding(vertical = 12.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun HourStepper(
    label: String,
    hour: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit
) {
    ValueStepper(
        label = label,
        value = hourLabel(hour),
        enabled = enabled,
        lessDescription = stringResource(R.string.settings_notif_one_hour_less, label),
        moreDescription = stringResource(R.string.settings_notif_one_hour_more, label),
        modifier = modifier,
        onLess = { onChange((hour + 23) % 24) },
        onMore = { onChange((hour + 1) % 24) }
    )
}

/**
 * Los minutos van de cinco en cinco.
 *
 * De uno en uno serían sesenta pulsaciones para cruzar la hora, y nadie necesita que su
 * resumen salga a las 7:37 en vez de a las 7:35.
 */
@Composable
private fun MinuteStepper(
    label: String,
    minute: Int,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onChange: (Int) -> Unit
) {
    ValueStepper(
        label = label,
        value = "%02d".format(minute),
        enabled = enabled,
        lessDescription = stringResource(R.string.settings_notif_five_min_less, label),
        moreDescription = stringResource(R.string.settings_notif_five_min_more, label),
        modifier = modifier,
        onLess = { onChange((minute + 55) % 60) },
        onMore = { onChange((minute + 5) % 60) }
    )
}

/** El menos, el número y el más. Lo comparten las horas y los minutos. */
@Composable
private fun ValueStepper(
    label: String,
    value: String,
    enabled: Boolean,
    lessDescription: String,
    moreDescription: String,
    modifier: Modifier = Modifier,
    onLess: () -> Unit,
    onMore: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onLess,
                enabled = enabled,
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = lessDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                value,
                modifier = Modifier.width(58.dp),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            IconButton(
                onClick = onMore,
                enabled = enabled,
                modifier = Modifier.size(IconButtonDefaults.smallContainerSize())
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = moreDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

private fun hourLabel(hour: Int): String = "%02d:00".format(hour)

private fun leadChipLabel(hours: Int): String = when {
    hours < 24 -> "${hours}h"
    hours == 24 -> "1d"
    else -> "${hours / 24}d"
}

private fun leadLabel(hours: Int): String {
    return when {
        hours < 24 -> Textos.get(R.string.notif_horas_antes, hours)
        hours == 24 -> Textos.get(R.string.settings_notif_one_day_before)
        else -> Textos.get(R.string.notif_dias_antes, hours / 24)
    }
}
