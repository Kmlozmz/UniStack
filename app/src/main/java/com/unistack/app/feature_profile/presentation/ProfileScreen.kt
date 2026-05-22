package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan
import com.unistack.app.feature_expenses.domain.ExpenseCategory
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import com.unistack.app.feature_user.domain.VisualPreference

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel(),
    onOpenProClick: () -> Unit = {}
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val plan = FeatureGate.planFor(billingState.isPro)
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val currentProfile = profile
    var nameInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.preferredName.orEmpty())
    }
    var selectedScale by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(initialSupportedScale(currentProfile))
    }
    var passingGradeInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(initialPassingGradeInput(currentProfile))
    }
    var targetAverageInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(initialTargetAverageInput(currentProfile))
    }
    var reminderLeadInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.reminderLeadHours?.toString() ?: "24")
    }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var localBackupInput by rememberSaveable { mutableStateOf("") }
    var localBackupPreview by rememberSaveable { mutableStateOf<String?>(null) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.refreshBilling()
    }

    LaunchedEffect(profile?.updatedAt, profile?.userId) {
        val current = profile ?: return@LaunchedEffect
        val scale = current.gradingScale
        nameInput = current.preferredName
        selectedScale = scale
        passingGradeInput = GradingScaleUtils.formatGrade(current.passingGrade, scale)
        targetAverageInput = GradingScaleUtils.formatGrade(current.targetAverage, scale)
        reminderLeadInput = current.reminderLeadHours.toString()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("Perfil", color = UniStackColors.TextPrimary, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
        }

        val loadedProfile = currentProfile
        if (loadedProfile == null) {
            item {
                UniCard(
                    modifier = Modifier.fillMaxWidth(),
                    color = UniStackColors.Card,
                    shape = AppShapes.LargeCard
                ) {
                    Text("Cargando perfil local...", color = UniStackColors.TextSecondary)
                }
            }
        } else {
            val current = loadedProfile
            item { ProfileHeaderCard(profile = current) }
            item {
                AccountSyncCard(
                    currentUser = currentUser,
                    isBusy = actionState.isAccountBusy,
                    onGoogleClick = { viewModel.connectGoogle(context) },
                    onUnlinkClick = { showUnlinkDialog = true }
                )
            }
            item {
                NameSettingsCard(
                    nameInput = nameInput,
                    onNameChange = {
                        nameInput = it.take(30)
                        feedback = null
                    },
                    onSaveClick = {
                        feedback = if (viewModel.updatePreferredName(nameInput)) {
                            "Nombre actualizado."
                        } else {
                            "Revisa el nombre antes de guardar."
                        }
                    }
                )
            }
            item {
                GradingSettingsCard(
                    selectedScale = selectedScale,
                    customGradeMax = current.customGradeMax,
                    passingGradeInput = passingGradeInput,
                    targetAverageInput = targetAverageInput,
                    onScaleSelected = { scale ->
                        selectedScale = scale
                        val maxGrade = if (scale == GradingScale.CUSTOM) current.customGradeMax else GradingScaleUtils.maxGradeFor(scale)
                        passingGradeInput = defaultGradeInput(maxGrade * 0.6)
                        targetAverageInput = defaultGradeInput(maxGrade * 0.8)
                        feedback = null
                    },
                    onPassingGradeChange = {
                        passingGradeInput = it.take(6)
                        feedback = null
                    },
                    onTargetAverageChange = {
                        targetAverageInput = it.take(6)
                        feedback = null
                    },
                    onSaveClick = {
                        feedback = if (viewModel.updateGradingSettings(selectedScale, passingGradeInput, targetAverageInput)) {
                            "Configuración académica actualizada."
                        } else {
                            "Revisa que las notas estén dentro de la escala."
                        }
                    }
                )
            }
            item {
                ModulesSettingsCard(
                    enabledModules = current.enabledModules,
                    onToggleModule = { module ->
                        feedback = if (viewModel.toggleModule(module)) {
                            "Módulos actualizados."
                        } else {
                            "Debe quedar al menos un módulo activo."
                        }
                    }
                )
            }
            item {
                NotificationSettingsCard(
                    taskRemindersEnabled = current.taskRemindersEnabled,
                    academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                    overdueRemindersEnabled = current.overdueRemindersEnabled,
                    reminderLeadInput = reminderLeadInput,
                    onTaskToggle = {
                        feedback = if (viewModel.updateReminderSettings(
                                taskRemindersEnabled = !current.taskRemindersEnabled,
                                academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                                overdueRemindersEnabled = current.overdueRemindersEnabled,
                                reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                            )
                        ) {
                            "Recordatorios actualizados."
                        } else {
                            "Revisa las horas de anticipación."
                        }
                    },
                    onAcademicWorkToggle = {
                        feedback = if (viewModel.updateReminderSettings(
                                taskRemindersEnabled = current.taskRemindersEnabled,
                                academicWorkRemindersEnabled = !current.academicWorkRemindersEnabled,
                                overdueRemindersEnabled = current.overdueRemindersEnabled,
                                reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                            )
                        ) {
                            "Recordatorios actualizados."
                        } else {
                            "Revisa las horas de anticipación."
                        }
                    },
                    onOverdueToggle = {
                        feedback = if (viewModel.updateReminderSettings(
                                taskRemindersEnabled = current.taskRemindersEnabled,
                                academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                                overdueRemindersEnabled = !current.overdueRemindersEnabled,
                                reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                            )
                        ) {
                            "Recordatorios actualizados."
                        } else {
                            "Revisa las horas de anticipación."
                        }
                    },
                    onLeadChange = {
                        reminderLeadInput = it.filter(Char::isDigit).take(3)
                        feedback = null
                    },
                    onSaveLead = {
                        feedback = if (viewModel.updateReminderSettings(
                                taskRemindersEnabled = current.taskRemindersEnabled,
                                academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                                overdueRemindersEnabled = current.overdueRemindersEnabled,
                                reminderLeadHours = reminderLeadInput.toIntOrNull() ?: -1
                            )
                        ) {
                            "Recordatorios actualizados."
                        } else {
                            "Revisa las horas de anticipación."
                        }
                    }
                )
            }
            item {
                ExpenseCategoriesSettingsCard(
                    enabledExpenseCategories = current.enabledExpenseCategories,
                    onToggleCategory = { category ->
                        feedback = if (viewModel.toggleExpenseCategory(category)) {
                            "Categorías actualizadas."
                        } else {
                            "Debe quedar al menos una categoría activa."
                        }
                    }
                )
            }
            item {
                VisualSettingsCard(
                    selected = current.visualPreference,
                    onSelected = { preference ->
                        viewModel.updateVisualPreference(preference)
                        feedback = "Preferencia visual actualizada."
                    }
                )
            }
            item {
                PlanStatusCard(
                    plan = plan,
                    onOpenProClick = onOpenProClick
                )
            }
            item {
                DataManagementCard(
                    backupInput = localBackupInput,
                    backupPreview = localBackupPreview,
                    onBackupInputChange = {
                        localBackupInput = it
                        localBackupPreview = null
                    },
                    onCopyBackupClick = {
                        clipboard.setText(AnnotatedString(viewModel.exportLocalBackup()))
                        feedback = "Backup JSON copiado."
                    },
                    onPreviewBackupClick = {
                        localBackupPreview = viewModel.previewLocalBackup(localBackupInput)
                    },
                    onRestoreBackupClick = {
                        feedback = if (viewModel.restoreLocalBackup(localBackupInput)) {
                            localBackupInput = ""
                            localBackupPreview = null
                            "Backup local restaurado."
                        } else {
                            "Revisa el JSON del backup."
                        }
                    },
                    onCopyAcademicReportClick = {
                        clipboard.setText(AnnotatedString(viewModel.exportAcademicReport()))
                        feedback = "Reporte académico copiado."
                    },
                    onCreateAcademicPdfClick = {
                        feedback = if (viewModel.exportAcademicPdf(context)) {
                            "PDF académico creado."
                        } else {
                            "No se pudo crear el PDF."
                        }
                    },
                    onCopyTasksCsvClick = {
                        clipboard.setText(AnnotatedString(viewModel.exportTasksCsv()))
                        feedback = "CSV de tareas copiado."
                    },
                    onCopyExpensesCsvClick = {
                        clipboard.setText(AnnotatedString(viewModel.exportExpensesCsv()))
                        feedback = "CSV de gastos copiado."
                    }
                )
            }
            item {
                ResetOnboardingCard(onRestartClick = { showRestartDialog = true })
            }
            feedback?.let { message ->
                item {
                    Text(
                        message,
                        color = if (message.startsWith("Revisa") || message.startsWith("Debe")) UniStackColors.Coral else UniStackColors.Green,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            actionState.message?.let { message ->
                item {
                    Text(message, color = UniStackColors.Green, fontWeight = FontWeight.Bold)
                }
            }
            actionState.errorMessage?.let { message ->
                item {
                    Text(message, color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text("¿Reiniciar onboarding?") },
            text = { Text("Volverás al flujo inicial para configurar tus datos. Tus materias, tareas y gastos locales no se eliminan.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        viewModel.restartOnboarding()
                    }
                ) {
                    Text("Reiniciar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = UniStackColors.Background
        )
    }

    if (showUnlinkDialog) {
        AlertDialog(
            onDismissRequest = { showUnlinkDialog = false },
            title = { Text("¿Desvincular cuenta?") },
            text = { Text("Tus datos locales se mantienen en este dispositivo.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showUnlinkDialog = false
                        viewModel.unlinkAccount()
                    }
                ) {
                    Text("Desvincular", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = UniStackColors.Background
        )
    }
}

@Composable
private fun ExpenseCategoriesSettingsCard(
    enabledExpenseCategories: Set<ExpenseCategory>,
    onToggleCategory: (ExpenseCategory) -> Unit
) {
    SettingsCard(title = "Categorías de gastos") {
        Text(
            "Elige qué categorías aparecen disponibles al registrar gastos.",
            color = UniStackColors.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
        ExpenseCategory.entries.chunked(2).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { category ->
                    SelectionPill(
                        text = category.profileLabel(),
                        selected = category in enabledExpenseCategories,
                        onClick = { onToggleCategory(category) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DataManagementCard(
    backupInput: String,
    backupPreview: String?,
    onBackupInputChange: (String) -> Unit,
    onCopyBackupClick: () -> Unit,
    onPreviewBackupClick: () -> Unit,
    onRestoreBackupClick: () -> Unit,
    onCopyAcademicReportClick: () -> Unit,
    onCreateAcademicPdfClick: () -> Unit,
    onCopyTasksCsvClick: () -> Unit,
    onCopyExpensesCsvClick: () -> Unit
) {
    SettingsCard(title = "Datos y exportación") {
        Button(
            onClick = onCopyBackupClick,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Copiar backup JSON")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCopyAcademicReportClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.SurfaceVariant, contentColor = UniStackColors.TextPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Notas")
            }
            Button(
                onClick = onCreateAcademicPdfClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.SurfaceVariant, contentColor = UniStackColors.TextPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("PDF")
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCopyTasksCsvClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.SurfaceVariant, contentColor = UniStackColors.TextPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Tareas CSV")
            }
        }
        Button(
            onClick = onCopyExpensesCsvClick,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.SurfaceVariant, contentColor = UniStackColors.TextPrimary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Gastos CSV")
        }
        OutlinedTextField(
            value = backupInput,
            onValueChange = onBackupInputChange,
            label = { Text("Pegar backup JSON") },
            minLines = 3,
            maxLines = 5,
            shape = AppShapes.MediumCard,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("profile-backup-json-input")
        )
        backupPreview?.let {
            Text(it, color = UniStackColors.TextSecondary, fontSize = 12.sp, lineHeight = 16.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPreviewBackupClick,
                enabled = backupInput.isNotBlank(),
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.PrimaryLight, contentColor = UniStackColors.Primary),
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile-backup-preview")
            ) {
                Text("Vista previa")
            }
            Button(
                onClick = onRestoreBackupClick,
                enabled = backupInput.isNotBlank(),
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Coral),
                modifier = Modifier
                    .weight(1f)
                    .testTag("profile-backup-restore")
            ) {
                Text("Restaurar")
            }
        }
    }
}

@Composable
private fun AccountSyncCard(
    currentUser: AppUser,
    isBusy: Boolean,
    onGoogleClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    SettingsCard(title = "Cuenta") {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccountAvatar(
                photoUrl = currentUser.photoUrl,
                contentDescription = if (currentUser.photoUrl.isNullOrBlank()) "Perfil" else "Foto de perfil",
                modifier = Modifier.size(52.dp)
            )
            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = currentUser.accountLabel(),
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = currentUser.email ?: "Sin cuenta vinculada",
                    color = UniStackColors.TextSecondary,
                    fontSize = 12.sp
                )
            }
            SyncStatusPill(status = currentUser.syncStatus)
        }
        Text(
            text = if (currentUser.isLinked) {
                "Tu perfil está vinculado a esta cuenta."
            } else {
                "Puedes usar la app localmente o vincular una cuenta de Google."
            },
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Button(
            onClick = if (currentUser.isLinked) onUnlinkClick else onGoogleClick,
            enabled = !isBusy,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentUser.isLinked) UniStackColors.SurfaceVariant else UniStackColors.Primary,
                contentColor = if (currentUser.isLinked) UniStackColors.TextPrimary else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                when {
                    isBusy -> "Procesando..."
                    currentUser.isLinked -> "Desvincular cuenta"
                    else -> "Conectar con Google"
                }
            )
        }
    }
}

@Composable
private fun AccountAvatar(
    photoUrl: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFFFFD8C8),
                        Color(0xFFE6E0FF),
                        Color(0xFFDDEBFF)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl.isNullOrBlank()) {
            Icon(Icons.Rounded.Person, contentDescription = contentDescription, tint = UniStackColors.PrimaryDark)
        } else {
            AsyncImage(
                model = photoUrl,
                contentDescription = contentDescription,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
private fun SyncStatusPill(status: SyncStatus) {
    val color = when (status) {
        SyncStatus.LOCAL_ONLY -> UniStackColors.SurfaceVariant
        SyncStatus.READY_FOR_BACKUP -> UniStackColors.PrimaryLight
        SyncStatus.SYNC_PENDING -> UniStackColors.YellowLight
        SyncStatus.SYNCED -> UniStackColors.GreenLight
        SyncStatus.SYNC_ERROR -> UniStackColors.CoralLight
    }
    val textColor = when (status) {
        SyncStatus.LOCAL_ONLY -> UniStackColors.TextSecondary
        SyncStatus.READY_FOR_BACKUP -> UniStackColors.Primary
        SyncStatus.SYNC_PENDING -> UniStackColors.Yellow
        SyncStatus.SYNCED -> UniStackColors.Green
        SyncStatus.SYNC_ERROR -> UniStackColors.Coral
    }
    UniCard(
        color = color,
        shape = AppShapes.Pill,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = status.label(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1
        )
    }
}

@Composable
private fun PlanStatusCard(
    plan: UserPlan,
    onOpenProClick: () -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, UniStackColors.Card)),
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = UniStackColors.Primary)
            Text("Plan actual: ${plan.name}", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(
                if (plan.hasSubjectLimit) "${plan.maxSubjects} materias disponibles" else "Materias ilimitadas",
                color = UniStackColors.TextSecondary
            )
            Button(
                onClick = onOpenProClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver UniStack Pro")
            }
        }
    }
}

@Composable
private fun ProfileHeaderCard(profile: UserProfile) {
    val name = profile.preferredName.takeIf { it.isNotBlank() } ?: "Estudiante"

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        brush = Brush.linearGradient(listOf(UniStackColors.PrimaryLight, UniStackColors.Card)),
        shape = AppShapes.LargeCard
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AccountAvatar(
                photoUrl = profile.accountPhotoUrl,
                contentDescription = "Foto de perfil",
                modifier = Modifier.size(54.dp)
            )
            Column(modifier = Modifier.padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(name, color = UniStackColors.TextPrimary, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                Text(profile.educationSummary(), color = UniStackColors.TextSecondary)
                Text(
                    "Meta ${GradingScaleUtils.formatGrade(profile.targetAverage, profile.gradingScale)}",
                    color = UniStackColors.Primary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun NameSettingsCard(
    nameInput: String,
    onNameChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val validation = TextValidators.validateDisplayName(nameInput)
    SettingsCard(title = "Datos personales") {
        OutlinedTextField(
            value = nameInput,
            onValueChange = onNameChange,
            label = { Text("Nombre preferido") },
            singleLine = true,
            isError = nameInput.isNotBlank() && !validation.isValid,
            supportingText = {
                if (nameInput.isNotBlank() && !validation.isValid) {
                    Text(validation.errorMessage ?: "Ingresa un nombre válido")
                }
            },
            shape = AppShapes.MediumCard,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = onSaveClick,
            enabled = validation.isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar nombre")
        }
    }
}

@Composable
private fun GradingSettingsCard(
    selectedScale: GradingScale,
    customGradeMax: Double,
    passingGradeInput: String,
    targetAverageInput: String,
    onScaleSelected: (GradingScale) -> Unit,
    onPassingGradeChange: (String) -> Unit,
    onTargetAverageChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val maxGrade = if (selectedScale == GradingScale.CUSTOM) {
        customGradeMax.coerceIn(1.0, 100.0)
    } else {
        GradingScaleUtils.maxGradeFor(selectedScale)
    }
    val passing = passingGradeInput.toDoubleOrNull()
    val target = targetAverageInput.toDoubleOrNull()
    val isValid = passing != null &&
        target != null &&
        passing in 0.0..maxGrade &&
        target in 0.0..maxGrade &&
        target >= passing

    SettingsCard(title = "Configuración académica") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf<GradingScale>(
                GradingScale.ZERO_TO_FIVE,
                GradingScale.CUSTOM
            ).forEach { scale ->
                SelectionPill(
                    text = scale.label(),
                    selected = selectedScale == scale,
                    onClick = { onScaleSelected(scale) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = passingGradeInput,
                onValueChange = onPassingGradeChange,
                label = { Text("Mínima") },
                singleLine = true,
                isError = passingGradeInput.isNotBlank() && (passing == null || passing !in 0.0..maxGrade),
                shape = AppShapes.MediumCard,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = targetAverageInput,
                onValueChange = onTargetAverageChange,
                label = { Text("Meta") },
                singleLine = true,
                isError = targetAverageInput.isNotBlank() && !isValid,
                shape = AppShapes.MediumCard,
                modifier = Modifier.weight(1f)
            )
        }
        Text(
            "Rango activo: 0 a ${GradingScaleUtils.formatGrade(maxGrade, selectedScale)}",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp
        )
        Button(
            onClick = onSaveClick,
            enabled = isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar escala")
        }
    }
}

@Composable
private fun ModulesSettingsCard(
    enabledModules: Set<AppModule>,
    onToggleModule: (AppModule) -> Unit
) {
    SettingsCard(title = "Módulos activos") {
        AppModule.entries.forEach { module: AppModule ->
            val enabled = module in enabledModules
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .toggleable(
                        value = enabled,
                        role = Role.Checkbox,
                        onValueChange = { onToggleModule(module) }
                    )
                    .semantics {
                        contentDescription = "Módulo ${module.label()}"
                        stateDescription = if (enabled) "Activo" else "Inactivo"
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(module.label(), color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(module.description(), color = UniStackColors.TextSecondary, fontSize = 12.sp)
                }
                Checkbox(
                    checked = enabled,
                    onCheckedChange = null,
                    modifier = Modifier.clearAndSetSemantics {}
                )
            }
        }
    }
}

@Composable
private fun NotificationSettingsCard(
    taskRemindersEnabled: Boolean,
    academicWorkRemindersEnabled: Boolean,
    overdueRemindersEnabled: Boolean,
    reminderLeadInput: String,
    onTaskToggle: () -> Unit,
    onAcademicWorkToggle: () -> Unit,
    onOverdueToggle: () -> Unit,
    onLeadChange: (String) -> Unit,
    onSaveLead: () -> Unit
) {
    SettingsCard(title = "Recordatorios") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ReminderToggleRow(
                title = "Tareas",
                description = "Avisos antes de tareas pendientes.",
                checked = taskRemindersEnabled,
                onToggle = onTaskToggle
            )
            ReminderToggleRow(
                title = "Trabajos",
                description = "Avisos antes de entregas académicas.",
                checked = academicWorkRemindersEnabled,
                onToggle = onAcademicWorkToggle
            )
            ReminderToggleRow(
                title = "Vencidos",
                description = "Avisos cuando una tarea o trabajo vence.",
                checked = overdueRemindersEnabled,
                onToggle = onOverdueToggle
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = reminderLeadInput,
                    onValueChange = onLeadChange,
                    label = { Text("Horas antes") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.MediumCard
                )
                Button(
                    onClick = onSaveLead,
                    shape = AppShapes.Pill,
                    colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary)
                ) {
                    Text("Guardar")
                }
            }
        }
    }
}

@Composable
private fun ReminderToggleRow(
    title: String,
    description: String,
    checked: Boolean,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(AppShapes.MediumCard)
            .toggleable(
                value = checked,
                role = Role.Checkbox,
                onValueChange = { onToggle() }
            )
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Column(modifier = Modifier.padding(start = 8.dp)) {
            Text(title, color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text(description, color = UniStackColors.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun VisualSettingsCard(
    selected: VisualPreference,
    onSelected: (VisualPreference) -> Unit
) {
    SettingsCard(title = "Preferencia visual") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            VisualPreference.entries.forEach { preference: VisualPreference ->
                SelectionPill(
                    text = preference.label(),
                    selected = selected == preference,
                    onClick = { onSelected(preference) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Text(
            "La preferencia se guarda en tu perfil local.",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun ResetOnboardingCard(onRestartClick: () -> Unit) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.CoralLight,
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Rounded.RestartAlt, contentDescription = null, tint = UniStackColors.Coral)
            Text("Onboarding", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
            Text("Puedes volver al flujo inicial sin borrar tus datos locales.", color = UniStackColors.TextSecondary)
            Button(
                onClick = onRestartClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Coral),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reiniciar onboarding")
            }
        }
    }
}

@Composable
private fun SettingsCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Settings, contentDescription = null, tint = UniStackColors.Primary)
                Text(
                    title,
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
            content()
        }
    }
}

@Composable
private fun SelectionPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    UniCard(
        modifier = modifier
            .heightIn(min = 48.dp)
            .semantics {
                stateDescription = if (selected) "Seleccionado" else "No seleccionado"
            }
            .bounceClick(onClick),
        color = if (selected) UniStackColors.PrimaryLight else UniStackColors.SurfaceVariant,
        shape = AppShapes.Pill,
        tonalElevation = if (selected) 5.dp else 0.dp,
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 9.dp)
    ) {
        Text(
            text,
            color = if (selected) UniStackColors.Primary else UniStackColors.TextPrimary,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = if (selected) "Seleccionado" else "No seleccionado"
                }
        )
    }
}

private fun UserProfile.educationSummary(): String {
    val level = when (educationLevel) {
        EducationLevel.PRIMARY -> "Primaria"
        EducationLevel.SECONDARY -> "Secundaria"
        EducationLevel.UNIVERSITY -> "Universidad"
        EducationLevel.OTHER -> "Otro"
    }
    val detail = gradeLevel ?: careerOrProgram
    return if (detail.isNullOrBlank()) level else "$level · $detail"
}

private fun initialSupportedScale(profile: UserProfile?): GradingScale {
    return profile?.gradingScale ?: GradingScale.ZERO_TO_FIVE
}

private fun initialPassingGradeInput(profile: UserProfile?): String {
    val scale = initialSupportedScale(profile)
    return profile?.let { GradingScaleUtils.formatGrade(it.passingGrade, scale) }.orEmpty()
}

private fun initialTargetAverageInput(profile: UserProfile?): String {
    val scale = initialSupportedScale(profile)
    return profile?.let { GradingScaleUtils.formatGrade(it.targetAverage, scale) }.orEmpty()
}

private fun defaultGradeInput(value: Double): String {
    return if (value % 1.0 == 0.0) {
        value.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", value)
    }
}

private fun GradingScale.label(): String {
    return when (this) {
        GradingScale.ZERO_TO_FIVE -> "0-5"
        GradingScale.CUSTOM -> "Personalizada"
    }
}

private fun AppModule.label(): String {
    return when (this) {
        AppModule.GRADES -> "Notas"
        AppModule.TASKS -> "Tareas"
        AppModule.EXPENSES -> "Gastos"
        AppModule.ACADEMIC_TEMPLATES -> "Trabajos"
    }
}

private fun AppModule.description(): String {
    return when (this) {
        AppModule.GRADES -> "Materias, notas y metas."
        AppModule.TASKS -> "Entregas y pendientes."
        AppModule.EXPENSES -> "Registro y resumen de gastos."
        AppModule.ACADEMIC_TEMPLATES -> "Checklist, ensayos y formato APA."
    }
}

private fun ExpenseCategory.profileLabel(): String {
    return when (this) {
        ExpenseCategory.TRANSPORT -> "Transporte"
        ExpenseCategory.FOOD -> "Comida"
        ExpenseCategory.COPIES -> "Copias"
        ExpenseCategory.MATERIALS -> "Materiales"
        ExpenseCategory.OUTINGS -> "Salidas"
        ExpenseCategory.OTHER -> "Otros"
    }
}

private fun VisualPreference.label(): String {
    return when (this) {
        VisualPreference.SYSTEM -> "Sistema"
        VisualPreference.LIGHT -> "Claro"
        VisualPreference.DARK -> "Oscuro"
    }
}

private fun AppUser.accountLabel(): String {
    return when (authProvider) {
        AuthProvider.LOCAL -> "Cuenta local"
        AuthProvider.GOOGLE -> "Google conectado"
    }
}

private fun SyncStatus.label(): String {
    return when (this) {
        SyncStatus.LOCAL_ONLY -> "Local"
        SyncStatus.READY_FOR_BACKUP -> "Backup listo"
        SyncStatus.SYNC_PENDING -> "Pendiente"
        SyncStatus.SYNCED -> "Sincronizado"
        SyncStatus.SYNC_ERROR -> "Revisar"
    }
}
