@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import com.unistack.app.core.design.components.cookieCorner
import com.unistack.app.core.design.theme.SectionLabelStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import com.unistack.app.core.design.components.UniStackButtonDefaults
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos
private val BackupLocale = Locale.forLanguageTag("es")

/** Lo que se ha elegido restaurar, mientras se decide. */
private data class PendingRestore(
    val name: String,
    val json: String,
    val incoming: LocalBackupPreview?,
    val current: LocalBackupPreview?
)

/**
 * Datos y respaldos.
 *
 * Tres cosas distintas que antes iban revueltas en una tarjeta: la copia completa —la que sirve
 * para volver a tener la app como estaba—, la copia en la nube y las exportaciones para leer
 * fuera. Cada una en su bloque, y todas terminan en un archivo: lo que se copiaba al
 * portapapeles no se podía guardar en ninguna parte.
 */
@Composable
internal fun BackupSection(
    viewModel: ProfileViewModel,
    cloudLinked: Boolean,
    cloudAvailable: Boolean,
    cloudBusy: Boolean,
    cloudStatus: String?,
    dataSummary: String,
    /** El mensaje y si es un error: el color lo decide quien lo pinta, sin leer el texto. */
    onFeedback: (String, Boolean) -> Unit
) {
    val context = LocalContext.current
    var lastBackup by remember { mutableStateOf(BackupFiles.lastBackupAt(context)) }
    var pendingRestore by remember { mutableStateOf<PendingRestore?>(null) }

    val saveBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(context, uri, viewModel.exportLocalBackup())
            .onSuccess {
                BackupFiles.rememberBackupDone(context)
                lastBackup = BackupFiles.lastBackupAt(context)
                onFeedback(Textos.get(R.string.backup_saved), false)
            }
            .onFailure { onFeedback(Textos.get(R.string.backup_save_failed), true) }
    }

    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.readText(context, uri)
            .onSuccess { json ->
                // Se lee y se enseña qué trae antes de tocar nada: restaurar reemplaza lo que
                // hay, y esa es una puerta de una sola dirección.
                pendingRestore = PendingRestore(
                    name = BackupFiles.displayName(context, uri),
                    json = json,
                    incoming = viewModel.inspectLocalBackup(json),
                    current = viewModel.currentContents()
                )
            }
            .onFailure { onFeedback(Textos.get(R.string.backup_read_failed), true) }
    }

    val saveTasksCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(context, uri, viewModel.exportTasksCsv())
            .onSuccess { onFeedback(Textos.get(R.string.backup_csv_tasks_saved), false) }
            .onFailure { onFeedback(Textos.get(R.string.backup_csv_failed), true) }
    }

    val saveExpensesCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(context, uri, viewModel.exportExpensesCsv())
            .onSuccess { onFeedback(Textos.get(R.string.backup_csv_expenses_saved), false) }
            .onFailure { onFeedback(Textos.get(R.string.backup_csv_failed), true) }
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        /*
         * La copia manda arriba, con cuándo se hizo y qué lleva dentro.
         *
         * Antes lo primero era un párrafo explicando qué es una copia de seguridad, y el dato
         * que uno viene a comprobar —si la última es de hoy o de hace tres meses— estaba
         * enterrado dentro de un recuadro gris a media tarjeta.
         */
        // La tarjeta de la copia flota sobre el resto: es lo único de la pantalla que se toca
        // a diario. La sombra va teñida de su propio color porque sobre el fondo oscuro de la
        // app una sombra negra no se ve, y sin sombra la tarjeta quedaba pegada al papel.
        val floteShape = MaterialTheme.shapes.extraLarge
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 14.dp,
                    shape = floteShape,
                    ambientColor = LocalSectionColors.current.schedule,
                    spotColor = LocalSectionColors.current.schedule
                )
                .cookieCorner(
                    color = LocalSectionColors.current.onScheduleContainer,
                    shape = floteShape,
                    size = 150.dp,
                    offsetX = 250.dp,
                    offsetY = (-50).dp,
                    alpha = 0.16f
                ),
            shape = floteShape,
            color = LocalSectionColors.current.scheduleContainer,
            contentColor = LocalSectionColors.current.onScheduleContainer
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(stringResource(R.string.settings_backup_sec_last), style = SectionLabelStyle)
                Text(
                    text = lastBackup?.let { formatBackupDate(it) } ?: stringResource(R.string.settings_backup_none_yet),
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = dataSummary,
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall
                )
                Row(
                    modifier = Modifier.padding(top = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Surface(
                        onClick = { saveBackup.launch(BackupFiles.suggestedName("unistack-copia", "json")) },
                        shape = CircleShape,
                        color = LocalSectionColors.current.schedule,
                        contentColor = LocalSectionColors.current.scheduleContainer
                    ) {
                        Text(
                            stringResource(R.string.settings_backup_btn_save),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        onClick = {
                            BackupFiles.shareText(
                                context = context,
                                fileName = BackupFiles.suggestedName("unistack-copia", "json"),
                                mimeType = "application/json",
                                text = viewModel.exportLocalBackup()
                            )
                                .onSuccess {
                                    BackupFiles.rememberBackupDone(context)
                                    lastBackup = BackupFiles.lastBackupAt(context)
                                }
                                .onFailure { onFeedback(Textos.get(R.string.backup_share_failed), true) }
                        },
                        shape = CircleShape,
                        color = Color.Transparent
                    ) {
                        Text(
                            stringResource(R.string.settings_backup_btn_share),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        
        }

        SettingsGroup(label = stringResource(R.string.settings_backup_sec_backups), rowCount = 3) {
            SettingsRow(
                icon = Icons.Rounded.Backup,
                title = stringResource(R.string.settings_backup_restore_title),
                subtitle = stringResource(R.string.settings_backup_restore_subtitle),
                iconColor = LocalSectionColors.current.schedule,
                onClick = { openBackup.launch(arrayOf("application/json", "text/plain", "*/*")) }
            )
            SettingsRow(
                icon = Icons.AutoMirrored.Rounded.ListAlt,
                title = stringResource(R.string.settings_backup_csv_tasks_title),
                subtitle = stringResource(R.string.settings_backup_csv_tasks_subtitle),
                iconColor = MaterialTheme.colorScheme.tertiary,
                onClick = { saveTasksCsv.launch(BackupFiles.suggestedName("unistack-tareas", "csv")) }
            )
            SettingsRow(
                icon = Icons.Rounded.AccountBalanceWallet,
                title = stringResource(R.string.settings_backup_csv_expenses_title),
                subtitle = stringResource(R.string.settings_backup_csv_expenses_subtitle),
                iconColor = LocalSectionColors.current.expenses,
                onClick = { saveExpensesCsv.launch(BackupFiles.suggestedName("unistack-gastos", "csv")) }
            )
        }

        // El aviso no se va: por ahora la copia la guardas tú, y eso hay que decirlo antes de
        // que alguien confíe en que la nube ya lo está haciendo sola.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.shapes.large)
                .background(LocalSectionColors.current.atRisk.copy(alpha = 0.16f))
                .padding(14.dp)
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = LocalSectionColors.current.atRisk,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(11.dp))
            Text(
                stringResource(R.string.settings_backup_cloud_desc),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall
            )
        }

        // Apagada al 45% mientras no se pueda usar: un bloque a plena luz con los botones
        // desactivados parece un fallo, y bajarle la opacidad entera lo lee como lo que es,
        // algo que todavía no está.
        UniCard(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (cloudAvailable) 1f else 0.45f),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackupBlockTitle(Icons.Rounded.CloudUpload, stringResource(R.string.settings_backup_cloud_title))
                    if (!cloudAvailable) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(CircleShape)
                                .background(LocalSectionColors.current.atRisk.copy(alpha = 0.22f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(stringResource(R.string.settings_account_soon), color = LocalSectionColors.current.atRisk, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(
                    when {
                        !cloudAvailable -> stringResource(R.string.settings_backup_cloud_inactive)
                        cloudLinked -> cloudStatus ?: stringResource(R.string.settings_backup_cloud_ready)
                        else -> stringResource(R.string.settings_backup_cloud_connect_google)
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupButton(
                        label = if (cloudBusy) stringResource(R.string.settings_backup_processing) else stringResource(R.string.settings_backup_btn_backup),
                        icon = Icons.Rounded.CloudUpload,
                        modifier = Modifier.weight(1f),
                        primary = true,
                        enabled = cloudAvailable && cloudLinked && !cloudBusy,
                        onClick = viewModel::backupToCloud
                    )
                    BackupButton(
                        label = stringResource(R.string.settings_backup_btn_restore),
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        enabled = cloudAvailable && cloudLinked && !cloudBusy,
                        onClick = viewModel::restoreFromCloud
                    )
                }
            }
        }

        UniCard(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.extraLarge) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BackupBlockTitle(Icons.Rounded.Description, stringResource(R.string.settings_backup_sec_export))
                Text(
                    stringResource(R.string.settings_backup_export_desc),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                BackupButton(
                    label = stringResource(R.string.settings_backup_pdf_report),
                    icon = Icons.Rounded.Description,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val file = viewModel.academicPdfFile(context)
                        if (file == null) {
                            onFeedback(Textos.get(R.string.backup_pdf_failed), true)
                        } else {
                            BackupFiles.shareFile(context, file, "application/pdf")
                                .onFailure { onFeedback(Textos.get(R.string.backup_pdf_open_failed), true) }
                        }
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupButton(
                        label = stringResource(R.string.backup_tasks_csv_label),
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        onClick = { saveTasksCsv.launch(BackupFiles.suggestedName("unistack-tareas", "csv")) }
                    )
                    BackupButton(
                        label = stringResource(R.string.backup_expenses_csv_label),
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        onClick = { saveExpensesCsv.launch(BackupFiles.suggestedName("unistack-gastos", "csv")) }
                    )
                }
            }
        }
    }

    pendingRestore?.let { pending ->
        val incoming = pending.incoming
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(if (incoming == null) stringResource(R.string.settings_backup_dialog_invalid) else stringResource(R.string.settings_backup_dialog_confirm)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        pending.name,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (incoming == null) {
                        Text(
                            stringResource(R.string.settings_backup_invalid_desc),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    } else {
                        /*
                         * Antes decía «v10 · 1 materias · 0 notas…» en una línea.
                         *
                         * Ese «v10» es el número de esquema de la base, que no significa nada
                         * fuera del código, y una lista de cifras sin contra qué compararlas no
                         * responde la única pregunta que importa aquí: qué pierdo y qué gano.
                         * Ahora cada fila enseña lo que hay ahora y lo que quedaría.
                         */
                        Text(
                            stringResource(R.string.settings_backup_changes_preview),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            RestoreComparisonHeader()
                            RestoreComparisonRow(stringResource(R.string.settings_backup_subjects), pending.current?.subjects, incoming.subjects)
                            RestoreComparisonRow(stringResource(R.string.settings_backup_grades), pending.current?.grades, incoming.grades)
                            RestoreComparisonRow(stringResource(R.string.settings_backup_tasks), pending.current?.tasks, incoming.tasks)
                            RestoreComparisonRow(stringResource(R.string.settings_backup_expenses), pending.current?.expenses, incoming.expenses)
                            RestoreComparisonRow(stringResource(R.string.settings_backup_works), pending.current?.academicWorks, incoming.academicWorks)
                            RestoreComparisonRow(stringResource(R.string.settings_backup_events), pending.current?.agendaEvents, incoming.agendaEvents)
                        }
                        Text(
                            stringResource(R.string.settings_backup_warning),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (incoming != null) {
                    TextButton(
                        onClick = {
                            val restored = viewModel.restoreLocalBackup(pending.json)
                            pendingRestore = null
                            onFeedback(if (restored) Textos.get(R.string.backup_restored) else Textos.get(R.string.backup_restore_failed), !restored)
                        }
                    ) {
                        Text(stringResource(R.string.settings_backup_btn_restore), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(if (incoming == null) stringResource(R.string.settings_backup_btn_understood) else stringResource(R.string.common_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

@Composable
private fun RestoreComparisonHeader() {
    Row {
        Spacer(Modifier.weight(1f))
        Text(
            stringResource(R.string.settings_backup_now),
            modifier = Modifier.width(58.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            stringResource(R.string.settings_backup_would_be),
            modifier = Modifier.width(72.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RestoreComparisonRow(label: String, current: Int?, incoming: Int) {
    // Lo que baja se marca: perder notas es distinto de ganarlas, y el color lo dice antes de
    // que nadie compare los dos números.
    val losing = current != null && incoming < current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(
            current?.toString() ?: "—",
            modifier = Modifier.width(58.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp,
            textAlign = TextAlign.End
        )
        Text(
            incoming.toString(),
            modifier = Modifier.width(72.dp),
            color = if (losing) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun BackupBlockTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(34.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun BackupButton(
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    primary: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        shapes = UniStackButtonDefaults.shapes,
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = if (primary) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(7.dp))
        Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatBackupDate(millis: Long): String = Instant.ofEpochMilli(millis)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("d 'de' MMMM, HH:mm", BackupLocale))
