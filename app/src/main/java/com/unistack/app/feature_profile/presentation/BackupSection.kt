package com.unistack.app.feature_profile.presentation

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.CloudUpload
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Info
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.feature_sync.domain.LocalBackupPreview
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

import androidx.compose.material3.MaterialTheme
import com.unistack.app.core.design.theme.LocalSectionColors
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
                pendingRestore = PendingRestore(
                    name = BackupFiles.displayName(context, uri),
                    json = json,
                    incoming = viewModel.inspectLocalBackup(json),
                    current = viewModel.currentContents()
                )
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
        // salvo, y tiene que saber antes de empezar que por ahora la copia la guarda él. Con
        // icono, borde y fondo propios, porque como párrafo suelto se leía como decoración.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.MediumCard)
                .background(LocalSectionColors.current.atRisk.copy(alpha = 0.16f))
                .border(1.dp, LocalSectionColors.current.atRisk.copy(alpha = 0.55f), AppShapes.MediumCard)
                .padding(14.dp)
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = LocalSectionColors.current.atRisk,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(11.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "Esto es temporal",
                    color = LocalSectionColors.current.atRisk,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Por ahora la copia la guardas tú, en un archivo. Pronto vas a poder " +
                        "vincular tu cuenta de Google y que se haga sola, sin que tengas que " +
                        "acordarte.",
                    color = MaterialTheme.colorScheme.onSurface,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(AppShapes.MediumCard)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Contenido", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        Text(
                            dataSummary,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                        Text(
                            lastBackup?.let { "Última copia: ${formatBackupDate(it)}" }
                                ?: "Todavía no has guardado ninguna copia.",
                            color = if (lastBackup == null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
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

        // Apagada al 45% mientras no se pueda usar: un bloque a plena luz con los botones
        // desactivados parece un fallo, y bajarle la opacidad entera lo lee como lo que es,
        // algo que todavía no está.
        UniCard(
            modifier = Modifier
                .fillMaxWidth()
                .alpha(if (cloudAvailable) 1f else 0.45f),
            shape = AppShapes.LargeCard
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    BackupBlockTitle(Icons.Rounded.CloudUpload, "Copia en la nube")
                    if (!cloudAvailable) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            Modifier
                                .clip(AppShapes.Pill)
                                .background(LocalSectionColors.current.atRisk.copy(alpha = 0.22f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text("Pronto", color = LocalSectionColors.current.atRisk, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
        val incoming = pending.incoming
        AlertDialog(
            onDismissRequest = { pendingRestore = null },
            title = { Text(if (incoming == null) "Ese archivo no sirve" else "¿Restaurar esta copia?") },
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
                            "No es una copia de UniStack, o está incompleto. Elige el archivo " +
                                "que guardaste desde «Guardar».",
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
                            "Esto es lo que cambiaría:",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                            RestoreComparisonHeader()
                            RestoreComparisonRow("Materias", pending.current?.subjects, incoming.subjects)
                            RestoreComparisonRow("Notas", pending.current?.grades, incoming.grades)
                            RestoreComparisonRow("Tareas", pending.current?.tasks, incoming.tasks)
                            RestoreComparisonRow("Gastos", pending.current?.expenses, incoming.expenses)
                            RestoreComparisonRow("Trabajos", pending.current?.academicWorks, incoming.academicWorks)
                            RestoreComparisonRow("Eventos", pending.current?.agendaEvents, incoming.agendaEvents)
                        }
                        Text(
                            "Se reemplaza todo, no se mezcla, y no se puede deshacer. Si dudas, " +
                                "guarda antes una copia de lo que tienes ahora.",
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
                            onFeedback(if (restored) "Copia restaurada." else "No se pudo restaurar el archivo.")
                        }
                    ) {
                        Text("Restaurar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingRestore = null }) {
                    Text(if (incoming == null) "Entendido" else "Cancelar")
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
            "Ahora",
            modifier = Modifier.width(58.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End
        )
        Text(
            "Quedaría",
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
                .clip(AppShapes.Small)
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
    SquishyButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
        shape = AppShapes.Pill,
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
