package com.unistack.app.feature_profile.presentation

import android.net.Uri
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
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val BackupLocale = Locale.forLanguageTag("es")

/** Lo que se ha elegido restaurar, mientras se decide. */
private data class PendingRestore(val uri: Uri, val json: String, val preview: String)

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
    onFeedback: (String) -> Unit
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
                onFeedback("Copia guardada.")
            }
            .onFailure { onFeedback("No se pudo guardar la copia.") }
    }

    val openBackup = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.readText(context, uri)
            .onSuccess { json ->
                // Se lee y se enseña qué trae antes de tocar nada: restaurar reemplaza lo que
                // hay, y esa es una puerta de una sola dirección.
                pendingRestore = PendingRestore(uri, json, viewModel.previewLocalBackup(json))
            }
            .onFailure { onFeedback("No se pudo leer el archivo.") }
    }

    val saveTasksCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(context, uri, viewModel.exportTasksCsv())
            .onSuccess { onFeedback("CSV de tareas guardado.") }
            .onFailure { onFeedback("No se pudo guardar el CSV.") }
    }

    val saveExpensesCsv = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        BackupFiles.writeText(context, uri, viewModel.exportExpensesCsv())
            .onSuccess { onFeedback("CSV de gastos guardado.") }
            .onFailure { onFeedback("No se pudo guardar el CSV.") }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // El aviso va arriba del todo y no al pie: quien entra aquí viene a poner sus datos a
        // salvo, y tiene que saber antes de empezar que por ahora la copia la guarda él.
        Box(
            Modifier
                .fillMaxWidth()
                .clip(AppShapes.MediumCard)
                .background(UniStackColors.Primary.copy(alpha = 0.12f))
                .padding(13.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Esto es temporal",
                    color = UniStackColors.Primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Por ahora las copias las guardas tú, en un archivo. Pronto vas a poder " +
                        "vincular tu cuenta de Google y que se hagan solas.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BackupBlockTitle(Icons.Rounded.Backup, "Copia de seguridad")
                Text(
                    "Un archivo con todo lo que tienes registrado. Guárdalo donde quieras y " +
                        "úsalo para volver a dejar la app como estaba.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.MediumCard)
                        .background(UniStackColors.SurfaceVariant)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Contenido", color = UniStackColors.TextSecondary, fontSize = 11.sp)
                        Text(
                            dataSummary,
                            color = UniStackColors.TextPrimary,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Text(
                            lastBackup?.let { "Última copia: ${formatBackupDate(it)}" }
                                ?: "Todavía no has guardado ninguna copia.",
                            color = if (lastBackup == null) UniStackColors.Coral else UniStackColors.TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupButton(
                        label = "Guardar",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        primary = true,
                        onClick = { saveBackup.launch(BackupFiles.suggestedName("unistack-copia", "json")) }
                    )
                    BackupButton(
                        label = "Compartir",
                        icon = Icons.Rounded.Share,
                        modifier = Modifier.weight(1f),
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
                                .onFailure { onFeedback("No se pudo compartir la copia.") }
                        }
                    )
                }
                BackupButton(
                    label = "Restaurar desde un archivo",
                    icon = Icons.Rounded.Backup,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { openBackup.launch(arrayOf("application/json", "text/plain", "*/*")) }
                )
            }
        }

        UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackupBlockTitle(Icons.Rounded.CloudUpload, "Copia en la nube")
                    if (!cloudAvailable) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(AppShapes.Pill)
                                .background(UniStackColors.Primary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Pronto", color = UniStackColors.Primary, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text(
                    when {
                        // Sin proyecto configurado los botones fallaban al pulsarlos con un
                        // error de configuración, que es un problema de quien compila y no algo
                        // que quien usa la app pueda resolver.
                        !cloudAvailable -> "Todavía no está activa. Cuando lo esté, tu cuenta de " +
                            "Google guardará la copia sola y podrás recuperarla en otro teléfono."
                        cloudLinked -> cloudStatus ?: "Tu cuenta está lista para respaldar y recuperar."
                        else -> "Conecta una cuenta de Google desde tu perfil para usar la nube."
                    },
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupButton(
                        label = if (cloudBusy) "Procesando..." else "Respaldar",
                        icon = Icons.Rounded.CloudUpload,
                        modifier = Modifier.weight(1f),
                        primary = true,
                        enabled = cloudAvailable && cloudLinked && !cloudBusy,
                        onClick = viewModel::backupToCloud
                    )
                    BackupButton(
                        label = "Recuperar",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        enabled = cloudAvailable && cloudLinked && !cloudBusy,
                        onClick = viewModel::restoreFromCloud
                    )
                }
            }
        }

        UniCard(modifier = Modifier.fillMaxWidth(), shape = AppShapes.LargeCard) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                BackupBlockTitle(Icons.Rounded.Description, "Exportar para leer fuera")
                Text(
                    "Formatos para abrir en otro sitio. No sirven para restaurar: para eso está " +
                        "la copia de seguridad.",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                BackupButton(
                    label = "Reporte de notas en PDF",
                    icon = Icons.Rounded.Description,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val file = viewModel.academicPdfFile(context)
                        if (file == null) {
                            onFeedback("No se pudo crear el PDF.")
                        } else {
                            BackupFiles.shareFile(context, file, "application/pdf")
                                .onFailure { onFeedback("No se pudo abrir el PDF.") }
                        }
                    }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BackupButton(
                        label = "Tareas CSV",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        onClick = { saveTasksCsv.launch(BackupFiles.suggestedName("unistack-tareas", "csv")) }
                    )
                    BackupButton(
                        label = "Gastos CSV",
                        icon = Icons.Rounded.Download,
                        modifier = Modifier.weight(1f),
                        onClick = { saveExpensesCsv.launch(BackupFiles.suggestedName("unistack-gastos", "csv")) }
                    )
                }
            }
        }
    }

    pendingRestore?.let { pending ->
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text("¿Restaurar esta copia?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("El archivo contiene:", color = UniStackColors.TextSecondary, fontSize = 13.sp)
                    Text(pending.preview, color = UniStackColors.TextPrimary, fontSize = 13.sp)
                    Text(
                        "Lo que tengas ahora en la app se reemplaza por esto y no se puede " +
                            "deshacer. Si dudas, guarda antes una copia de lo actual.",
                        color = UniStackColors.Coral,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val restored = viewModel.restoreLocalBackup(pending.json)
                        pendingRestore = null
                        onFeedback(if (restored) "Copia restaurada." else "Revisa el archivo: no es una copia válida.")
                    }
                ) {
                    Text("Restaurar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) { Text("Cancelar") }
            },
            containerColor = UniStackColors.Background
        )
    }
}

@Composable
private fun BackupBlockTitle(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(34.dp)
                .clip(AppShapes.Small)
                .background(UniStackColors.Primary.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = UniStackColors.Primary, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(10.dp))
        Text(title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
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
    SquishyButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = AppShapes.Pill,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (primary) UniStackColors.Primary else UniStackColors.SurfaceVariant,
            contentColor = if (primary) UniStackColors.OnPrimary else UniStackColors.TextPrimary,
            disabledContainerColor = UniStackColors.SurfaceVariant,
            disabledContentColor = UniStackColors.TextSecondary
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
