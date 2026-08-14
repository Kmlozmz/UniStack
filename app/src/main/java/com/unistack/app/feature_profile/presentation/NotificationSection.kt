package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Assignment
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.utils.BuildStage
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_user.domain.UserProfile

/** Las anticipaciones que se ofrecen. Cubren de «el mismo día» a «tres días antes». */
private val LeadChoices = listOf(1, 3, 6, 12, 24, 48, 72)

/**
 * Notificaciones.
 *
 * Era una sola tarjeta con cinco interruptores, un campo de texto para las horas con su botón
 * de guardar y otros dos campos de 0 a 23 con otro botón. Tres cosas distintas revueltas, y la
 * mitad pidiendo teclado para escribir un número que solo puede ser uno de siete.
 *
 * Ahora son tres bloques: si el sistema deja avisar, qué se avisa y cuándo. Nada pide teclado
 * ni botón de guardar: cada toque guarda, que es lo que hace el resto de la app.
 */
@Composable
internal fun NotificationSection(
    profile: UserProfile,
    permissionGranted: Boolean,
    onRequestPermission: () -> Unit,
    onReminderToggle: (taskReminders: Boolean, works: Boolean, overdue: Boolean, leadHours: Int) -> Unit,
    onAcademicToggle: (gradeInsights: Boolean, pendingGrades: Boolean) -> Unit,
    onQuietHoursChange: (enabled: Boolean, startHour: Int?, endHour: Int?) -> Unit
) {
    val lead = profile.reminderLeadHours
    val quietStart = profile.quietHoursStartHour ?: 22
    val quietEnd = profile.quietHoursEndHour ?: 7

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        PermissionCard(granted = permissionGranted, onRequestPermission = onRequestPermission)

        // Los avisos se apagan visualmente sin el permiso: los interruptores siguen ahí y se
        // pueden dejar preparados, pero no llega nada hasta que el sistema lo permita.
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.alpha(if (permissionGranted) 1f else 0.55f)
        ) {
            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionTitle(Icons.Rounded.NotificationsActive, "Qué te avisa")
                    ReminderRow(
                        icon = Icons.AutoMirrored.Rounded.Assignment,
                        title = "Tareas",
                        description = "Antes de que venza una tarea",
                        checked = profile.taskRemindersEnabled,
                        onToggle = {
                            onReminderToggle(
                                !profile.taskRemindersEnabled,
                                profile.academicWorkRemindersEnabled,
                                profile.overdueRemindersEnabled,
                                lead
                            )
                        }
                    )
                    // El aviso de Trabajos solo se ofrece donde Trabajos se puede abrir.
                    if (BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished) {
                        ReminderRow(
                            icon = Icons.Rounded.Description,
                            title = "Trabajos",
                            description = "Antes de una entrega académica",
                            checked = profile.academicWorkRemindersEnabled,
                            onToggle = {
                                onReminderToggle(
                                    profile.taskRemindersEnabled,
                                    !profile.academicWorkRemindersEnabled,
                                    profile.overdueRemindersEnabled,
                                    lead
                                )
                            }
                        )
                    }
                    ReminderRow(
                        icon = Icons.Rounded.WarningAmber,
                        title = "Vencidos",
                        description = "Cuando algo pasó de fecha sin entregar",
                        checked = profile.overdueRemindersEnabled,
                        onToggle = {
                            onReminderToggle(
                                profile.taskRemindersEnabled,
                                profile.academicWorkRemindersEnabled,
                                !profile.overdueRemindersEnabled,
                                lead
                            )
                        }
                    )
                    ReminderRow(
                        icon = Icons.Rounded.School,
                        title = "Notas y cortes",
                        description = "Si una meta deja de estar a tu alcance",
                        checked = profile.gradeInsightRemindersEnabled,
                        onToggle = {
                            onAcademicToggle(
                                !profile.gradeInsightRemindersEnabled,
                                profile.pendingGradeRemindersEnabled
                            )
                        }
                    )
                    ReminderRow(
                        icon = Icons.Rounded.Check,
                        title = "Resultados pendientes",
                        description = "Para registrar la nota de lo ya entregado",
                        checked = profile.pendingGradeRemindersEnabled,
                        onToggle = {
                            onAcademicToggle(
                                profile.gradeInsightRemindersEnabled,
                                !profile.pendingGradeRemindersEnabled
                            )
                        }
                    )
                }
            }

            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle(Icons.Rounded.Schedule, "Con cuánta anticipación")
                    Text(
                        "Los avisos de tareas y trabajos llegan " + leadLabel(lead) + " de la fecha.",
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    // Siete opciones en botones en vez de un campo de texto con su botón de
                    // guardar: la anticipación se elige, no se redacta.
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        LeadChoices.forEach { hours ->
                            LeadChip(
                                hours = hours,
                                selected = hours == lead,
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onReminderToggle(
                                        profile.taskRemindersEnabled,
                                        profile.academicWorkRemindersEnabled,
                                        profile.overdueRemindersEnabled,
                                        hours
                                    )
                                }
                            )
                        }
                    }
                }
            }

            UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.weight(1f)) {
                            SectionTitle(Icons.Rounded.Bedtime, "Horario silencioso")
                        }
                        Switch(
                            checked = profile.quietHoursEnabled,
                            onCheckedChange = {
                                onQuietHoursChange(!profile.quietHoursEnabled, quietStart, quietEnd)
                            },
                            colors = switchColors(),
                            modifier = Modifier.semantics {
                                contentDescription = "Horario silencioso"
                                stateDescription = if (profile.quietHoursEnabled) "Activo" else "Inactivo"
                                role = Role.Switch
                            }
                        )
                    }
                    Text(
                        if (profile.quietHoursEnabled) {
                            "Nada suena de " + hourLabel(quietStart) + " a " + hourLabel(quietEnd) +
                                ". Lo que caiga dentro se avisa al terminar."
                        } else {
                            "Los avisos pueden llegar a cualquier hora."
                        },
                        color = UniStackColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        HourStepper(
                            label = "Desde",
                            hour = quietStart,
                            enabled = profile.quietHoursEnabled,
                            modifier = Modifier.weight(1f),
                            onChange = { onQuietHoursChange(true, it, quietEnd) }
                        )
                        HourStepper(
                            label = "Hasta",
                            hour = quietEnd,
                            enabled = profile.quietHoursEnabled,
                            modifier = Modifier.weight(1f),
                            onChange = { onQuietHoursChange(true, quietStart, it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionCard(granted: Boolean, onRequestPermission: () -> Unit) {
    val accent = if (granted) UniStackColors.Green else UniStackColors.Coral
    UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(AppShapes.Small)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (granted) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    if (granted) "El sistema deja avisarte" else "El sistema no deja avisarte",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    if (granted) {
                        "Lo que enciendas abajo te va a llegar."
                    } else {
                        "Sin el permiso, nada de lo de abajo llega."
                    },
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            if (!granted) {
                Spacer(Modifier.width(10.dp))
                SquishyButton(
                    onClick = onRequestPermission,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
                ) {
                    Text("Activar", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UniStackColors.Primary, modifier = Modifier.size(19.dp))
        Spacer(Modifier.width(9.dp))
        Text(title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun ReminderRow(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = UniStackColors.TextSecondary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(11.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(title, color = UniStackColors.TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(description, color = UniStackColors.TextSecondary, fontSize = 11.sp, lineHeight = 15.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = switchColors(),
            modifier = Modifier.semantics {
                contentDescription = title
                stateDescription = if (checked) "Activo" else "Inactivo"
                role = Role.Switch
            }
        )
    }
}

@Composable
private fun LeadChip(
    hours: Int,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(AppShapes.Pill)
            .background(if (selected) UniStackColors.Primary else UniStackColors.SurfaceVariant)
            .border(
                width = 1.dp,
                color = if (selected) UniStackColors.Primary else UniStackColors.SoftOutline,
                shape = AppShapes.Pill
            )
            .semantics {
                contentDescription = "Anticipación " + leadLabel(hours)
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (hours < 24) "${hours}h" else "${hours / 24}d",
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 9.dp),
            color = if (selected) UniStackColors.OnPrimary else UniStackColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
    Column(
        modifier = modifier
            .clip(AppShapes.MediumCard)
            .background(UniStackColors.SurfaceVariant)
            .padding(horizontal = 6.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(label, color = UniStackColors.TextSecondary, fontSize = 11.sp)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onChange((hour + 23) % 24) },
                enabled = enabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = "Una hora menos en $label",
                    tint = UniStackColors.TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
            Text(
                hourLabel(hour),
                modifier = Modifier.width(58.dp),
                color = UniStackColors.TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            IconButton(
                onClick = { onChange((hour + 1) % 24) },
                enabled = enabled,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Una hora más en $label",
                    tint = UniStackColors.TextSecondary,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = UniStackColors.OnPrimary,
    checkedTrackColor = UniStackColors.Primary,
    uncheckedThumbColor = UniStackColors.TextSecondary,
    uncheckedTrackColor = UniStackColors.SurfaceVariant
)

private fun hourLabel(hour: Int): String = "%02d:00".format(hour)

private fun leadLabel(hours: Int): String = when {
    hours < 24 -> "$hours horas antes"
    hours == 24 -> "un día antes"
    else -> "${hours / 24} días antes"
}
