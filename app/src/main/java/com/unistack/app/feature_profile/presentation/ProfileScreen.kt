package com.unistack.app.feature_profile.presentation

import android.Manifest
import android.content.ClipData
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.utils.GradingScaleUtils
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.core.utils.bounceClick
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan
import com.unistack.app.feature_user.domain.AcademicPeriodLabel
import com.unistack.app.feature_user.domain.AppUser
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.EducationLevel
import com.unistack.app.feature_user.domain.GradingScale
import com.unistack.app.feature_user.domain.AuthProvider
import com.unistack.app.feature_user.domain.SyncStatus
import com.unistack.app.feature_user.domain.UserProfile
import kotlinx.coroutines.launch

enum class ProfileScreenMode {
    PROFILE,
    ACADEMIC,
    MODULES,
    NOTIFICATIONS,
    DATA
}

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
    onOpenProClick: () -> Unit = {},
    onOpenSettingsClick: () -> Unit = {},
    mode: ProfileScreenMode = ProfileScreenMode.PROFILE,
    onBackClick: () -> Unit = {}
) {
    if (mode != ProfileScreenMode.PROFILE) {
        BackHandler(onBack = onBackClick)
    }
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val cloudBackupState by viewModel.cloudBackupState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()
    fun copyToClipboard(text: String) {
        coroutineScope.launch {
            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText("UniStack", text)))
        }
    }
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
    var quietHoursStartInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.quietHoursStartHour?.toString().orEmpty())
    }
    var quietHoursEndInput by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.quietHoursEndHour?.toString().orEmpty())
    }
    var academicPeriodLabel by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.academicPeriodScheme?.label ?: AcademicPeriodLabel.CORTE)
    }
    var academicPeriodWeights by rememberSaveable(currentProfile?.userId) {
        mutableStateOf(currentProfile?.academicPeriodScheme?.periods?.map { percentInput(it.weight) } ?: listOf("30", "40", "30"))
    }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var localBackupInput by rememberSaveable { mutableStateOf("") }
    var localBackupPreview by rememberSaveable { mutableStateOf<String?>(null) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var pendingReminderUpdate by remember { mutableStateOf<(() -> Boolean)?>(null) }
    var notificationPermissionGranted by remember {
        mutableStateOf(context.hasNotificationPermission())
    }
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted || context.hasNotificationPermission()
        val update = pendingReminderUpdate
        pendingReminderUpdate = null
        feedback = when {
            notificationPermissionGranted && update?.invoke() == true -> "Recordatorios activados."
            notificationPermissionGranted -> "Revisa las horas de anticipación."
            else -> "Activa el permiso de notificaciones para recibir recordatorios."
        }
    }
    val runReminderUpdate: (Boolean, () -> Boolean) -> Unit = { enablingReminder, update ->
        val requiresPermission = enablingReminder && !context.hasNotificationPermission()
        if (requiresPermission) {
            pendingReminderUpdate = update
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            feedback = if (update()) {
                notificationPermissionGranted = context.hasNotificationPermission()
                "Recordatorios actualizados."
            } else {
                "Revisa las horas de anticipación."
            }
        }
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                notificationPermissionGranted = context.hasNotificationPermission()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(FeatureGate.PRO_FEATURES_ENABLED) {
        if (FeatureGate.PRO_FEATURES_ENABLED) {
            viewModel.refreshBilling()
        }
    }

    LaunchedEffect(profile?.updatedAt, profile?.userId) {
        val current = profile ?: return@LaunchedEffect
        val scale = current.gradingScale
        nameInput = current.preferredName
        selectedScale = scale
        passingGradeInput = GradingScaleUtils.formatGrade(current.passingGrade, scale)
        targetAverageInput = GradingScaleUtils.formatGrade(current.targetAverage, scale)
        reminderLeadInput = current.reminderLeadHours.toString()
        quietHoursStartInput = current.quietHoursStartHour?.toString().orEmpty()
        quietHoursEndInput = current.quietHoursEndHour?.toString().orEmpty()
        academicPeriodLabel = current.academicPeriodScheme.label
        academicPeriodWeights = current.academicPeriodScheme.periods.map { percentInput(it.weight) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background),
        contentPadding = PaddingValues(start = 20.dp, top = 58.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (mode != ProfileScreenMode.PROFILE) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = UniStackColors.TextPrimary
                        )
                    }
                }
                Text(
                    when (mode) {
                        ProfileScreenMode.PROFILE -> "Perfil"
                        ProfileScreenMode.ACADEMIC -> "Configuración académica"
                        ProfileScreenMode.MODULES -> "Módulos"
                        ProfileScreenMode.NOTIFICATIONS -> "Notificaciones"
                        ProfileScreenMode.DATA -> "Datos y respaldos"
                    },
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.weight(1f)
                )
                if (mode == ProfileScreenMode.PROFILE) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(UniStackColors.SurfaceVariant)
                            .bounceClick(onOpenSettingsClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = "Abrir configuración",
                            tint = UniStackColors.Primary
                        )
                    }
                }
            }
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
            if (mode == ProfileScreenMode.PROFILE) {
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
                if (FeatureGate.PRO_FEATURES_ENABLED) {
                    item {
                        val billingState by viewModel.billingState.collectAsStateWithLifecycle()
                        PlanStatusCard(
                            plan = FeatureGate.planFor(billingState.isPro),
                            onOpenProClick = onOpenProClick
                        )
                    }
                }
            }

            if (mode == ProfileScreenMode.ACADEMIC) {
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
                    AcademicPeriodsSettingsCard(
                        label = academicPeriodLabel,
                        weights = academicPeriodWeights,
                        onLabelSelected = {
                            academicPeriodLabel = it
                            feedback = null
                        },
                        onCountSelected = { count ->
                            academicPeriodWeights = when (count) {
                                2 -> listOf("50", "50")
                                3 -> listOf("30", "40", "30")
                                4 -> listOf("25", "25", "25", "25")
                                else -> academicPeriodWeights
                            }
                            feedback = null
                        },
                        onWeightChange = { index, value ->
                            academicPeriodWeights = academicPeriodWeights.mapIndexed { currentIndex, currentValue ->
                                if (currentIndex == index) value.filter { char -> char.isDigit() || char == '.' }.take(5) else currentValue
                            }
                            feedback = null
                        },
                        onSaveClick = {
                            feedback = if (viewModel.updateAcademicPeriodSettings(academicPeriodLabel, academicPeriodWeights)) {
                                "Periodos académicos actualizados."
                            } else {
                                "Revisa que los pesos sumen 100%."
                            }
                        }
                    )
                }
            }

            if (mode == ProfileScreenMode.MODULES) {
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
            }

            if (mode == ProfileScreenMode.NOTIFICATIONS) {
                item {
                    NotificationSettingsCard(
                        taskRemindersEnabled = current.taskRemindersEnabled,
                        academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                        overdueRemindersEnabled = current.overdueRemindersEnabled,
                        gradeInsightRemindersEnabled = current.gradeInsightRemindersEnabled,
                        pendingGradeRemindersEnabled = current.pendingGradeRemindersEnabled,
                        reminderLeadInput = reminderLeadInput,
                        quietHoursEnabled = current.quietHoursEnabled,
                        quietHoursStartInput = quietHoursStartInput,
                        quietHoursEndInput = quietHoursEndInput,
                        notificationPermissionGranted = notificationPermissionGranted,
                        onTaskToggle = {
                            runReminderUpdate(!current.taskRemindersEnabled) {
                                viewModel.updateReminderSettings(
                                    taskRemindersEnabled = !current.taskRemindersEnabled,
                                    academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                                    overdueRemindersEnabled = current.overdueRemindersEnabled,
                                    reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                                )
                            }
                        },
                        onAcademicWorkToggle = {
                            runReminderUpdate(!current.academicWorkRemindersEnabled) {
                                viewModel.updateReminderSettings(
                                    taskRemindersEnabled = current.taskRemindersEnabled,
                                    academicWorkRemindersEnabled = !current.academicWorkRemindersEnabled,
                                    overdueRemindersEnabled = current.overdueRemindersEnabled,
                                    reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                                )
                            }
                        },
                        onOverdueToggle = {
                            runReminderUpdate(!current.overdueRemindersEnabled) {
                                viewModel.updateReminderSettings(
                                    taskRemindersEnabled = current.taskRemindersEnabled,
                                    academicWorkRemindersEnabled = current.academicWorkRemindersEnabled,
                                    overdueRemindersEnabled = !current.overdueRemindersEnabled,
                                    reminderLeadHours = reminderLeadInput.toIntOrNull() ?: current.reminderLeadHours
                                )
                            }
                        },
                        onGradeInsightsToggle = {
                            runReminderUpdate(!current.gradeInsightRemindersEnabled) {
                                viewModel.updateAcademicReminderSettings(
                                    gradeInsightRemindersEnabled = !current.gradeInsightRemindersEnabled,
                                    pendingGradeRemindersEnabled = current.pendingGradeRemindersEnabled
                                )
                            }
                        },
                        onPendingGradesToggle = {
                            runReminderUpdate(!current.pendingGradeRemindersEnabled) {
                                viewModel.updateAcademicReminderSettings(
                                    gradeInsightRemindersEnabled = current.gradeInsightRemindersEnabled,
                                    pendingGradeRemindersEnabled = !current.pendingGradeRemindersEnabled
                                )
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
                        },
                        onQuietHoursToggle = {
                            feedback = if (viewModel.updateQuietHours(
                                    enabled = !current.quietHoursEnabled,
                                    startHour = quietHoursStartInput.toIntOrNull(),
                                    endHour = quietHoursEndInput.toIntOrNull()
                                )
                            ) {
                                "Horario silencioso actualizado."
                            } else {
                                "Define dos horas distintas entre 0 y 23."
                            }
                        },
                        onQuietHoursStartChange = {
                            quietHoursStartInput = it.filter(Char::isDigit).take(2)
                            feedback = null
                        },
                        onQuietHoursEndChange = {
                            quietHoursEndInput = it.filter(Char::isDigit).take(2)
                            feedback = null
                        },
                        onSaveQuietHours = {
                            feedback = if (viewModel.updateQuietHours(
                                    enabled = current.quietHoursEnabled,
                                    startHour = quietHoursStartInput.toIntOrNull(),
                                    endHour = quietHoursEndInput.toIntOrNull()
                                )
                            ) {
                                "Horario silencioso guardado."
                            } else {
                                "Define dos horas distintas entre 0 y 23."
                            }
                        }
                    )
                }
            }

            if (mode == ProfileScreenMode.DATA) {
                item {
                    DataManagementCard(
                        cloudEnabled = currentUser.isLinked,
                        cloudStatus = cloudBackupState.message ?: cloudBackupState.errorMessage,
                        cloudBusy = cloudBackupState.inProgress,
                        onCloudBackupClick = viewModel::backupToCloud,
                        onCloudRestoreClick = viewModel::restoreFromCloud,
                        dataSummary = viewModel.localDataSummary(),
                        backupInput = localBackupInput,
                        backupPreview = localBackupPreview,
                        onBackupInputChange = {
                            localBackupInput = it
                            localBackupPreview = null
                        },
                        onCopyBackupClick = {
                            copyToClipboard(viewModel.exportLocalBackup())
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
                            copyToClipboard(viewModel.exportAcademicReport())
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
                            copyToClipboard(viewModel.exportTasksCsv())
                            feedback = "CSV de tareas copiado."
                        },
                        onCopyExpensesCsvClick = {
                            copyToClipboard(viewModel.exportExpensesCsv())
                            feedback = "CSV de gastos copiado."
                        }
                    )
                }
                item {
                    ResetOnboardingCard(onRestartClick = { showRestartDialog = true })
                }
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
private fun DataManagementCard(
    cloudEnabled: Boolean,
    cloudStatus: String?,
    cloudBusy: Boolean,
    onCloudBackupClick: () -> Unit,
    onCloudRestoreClick: () -> Unit,
    dataSummary: String,
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
        Text("Sincronización con Google", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
        Text(
            if (cloudEnabled) {
                cloudStatus ?: "Cuenta lista para respaldar o recuperar tus datos."
            } else {
                "Conecta una cuenta de Google desde Perfil para activar la nube."
            },
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onCloudBackupClick,
                enabled = cloudEnabled && !cloudBusy,
                modifier = Modifier.weight(1f),
                shape = AppShapes.Pill
            ) {
                Text(if (cloudBusy) "Procesando..." else "Respaldar")
            }
            Button(
                onClick = onCloudRestoreClick,
                enabled = cloudEnabled && !cloudBusy,
                modifier = Modifier.weight(1f),
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(
                    containerColor = UniStackColors.SurfaceVariant,
                    contentColor = UniStackColors.TextPrimary
                )
            ) {
                Text("Recuperar")
            }
        }
        DataStatusStrip(summary = dataSummary)
        Text("Exportaciones locales", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
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
            Button(
                onClick = onCopyExpensesCsvClick,
                shape = AppShapes.Pill,
                colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.SurfaceVariant, contentColor = UniStackColors.TextPrimary),
                modifier = Modifier.weight(1f)
            ) {
                Text("Gastos CSV")
            }
        }
        Text("Restauración", color = UniStackColors.TextPrimary, fontWeight = FontWeight.ExtraBold)
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
private fun DataStatusStrip(summary: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.MediumCard)
            .background(UniStackColors.PrimaryLight)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Backup actual", color = UniStackColors.Primary, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
            Text(summary, color = UniStackColors.TextPrimary, fontSize = 12.sp, lineHeight = 16.sp)
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
                contentColor = if (currentUser.isLinked) UniStackColors.TextPrimary else UniStackColors.OnPrimary
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
                SolidColor(UniStackColors.PrimaryLight)
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
        color = UniStackColors.PrimaryLight,
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
        color = UniStackColors.PrimaryLight,
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
                GradingScale.ZERO_TO_HUNDRED,
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
private fun AcademicPeriodsSettingsCard(
    label: AcademicPeriodLabel,
    weights: List<String>,
    onLabelSelected: (AcademicPeriodLabel) -> Unit,
    onCountSelected: (Int) -> Unit,
    onWeightChange: (Int, String) -> Unit,
    onSaveClick: () -> Unit
) {
    val total = weights.sumOf { it.toDoubleOrNull() ?: 0.0 }
    val isValid = weights.isNotEmpty() &&
        weights.all { (it.toDoubleOrNull() ?: 0.0) > 0.0 } &&
        kotlin.math.abs(total - 100.0) <= 0.01

    SettingsCard(title = "${label.plural} del semestre") {
        Text(
            "Esta estructura se usa en todas las materias nuevas y existentes.",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AcademicPeriodLabel.entries.forEach { option ->
                SelectionPill(
                    text = option.singular,
                    selected = label == option,
                    onClick = { onLabelSelected(option) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4).forEach { count ->
                SelectionPill(
                    text = count.toString(),
                    selected = weights.size == count,
                    onClick = { onCountSelected(count) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        weights.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { onWeightChange(index, it) },
                label = { Text("${label.singular} ${index + 1} (%)") },
                singleLine = true,
                shape = AppShapes.MediumCard,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Text(
            "Total: ${String.format(java.util.Locale.US, "%.0f", total)}%",
            color = if (isValid) UniStackColors.Green else UniStackColors.Coral,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Button(
            onClick = onSaveClick,
            enabled = isValid,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(containerColor = UniStackColors.Primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar ${label.plural.lowercase()}")
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
    gradeInsightRemindersEnabled: Boolean,
    pendingGradeRemindersEnabled: Boolean,
    reminderLeadInput: String,
    quietHoursEnabled: Boolean,
    quietHoursStartInput: String,
    quietHoursEndInput: String,
    notificationPermissionGranted: Boolean,
    onTaskToggle: () -> Unit,
    onAcademicWorkToggle: () -> Unit,
    onOverdueToggle: () -> Unit,
    onGradeInsightsToggle: () -> Unit,
    onPendingGradesToggle: () -> Unit,
    onLeadChange: (String) -> Unit,
    onSaveLead: () -> Unit,
    onQuietHoursToggle: () -> Unit,
    onQuietHoursStartChange: (String) -> Unit,
    onQuietHoursEndChange: (String) -> Unit,
    onSaveQuietHours: () -> Unit
) {
    SettingsCard(title = "Recordatorios") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            NotificationPermissionStatus(notificationPermissionGranted)
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
            ReminderToggleRow(
                title = "Notas y cortes",
                description = "Alertas sobre metas, proyecciones e historial incompleto.",
                checked = gradeInsightRemindersEnabled,
                onToggle = onGradeInsightsToggle
            )
            ReminderToggleRow(
                title = "Resultados pendientes",
                description = "Recuerda registrar la nota de tareas ya completadas.",
                checked = pendingGradeRemindersEnabled,
                onToggle = onPendingGradesToggle
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
            ReminderToggleRow(
                title = "Horario silencioso",
                description = "Mueve los avisos fuera del intervalo que elijas.",
                checked = quietHoursEnabled,
                onToggle = onQuietHoursToggle
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = quietHoursStartInput,
                    onValueChange = onQuietHoursStartChange,
                    label = { Text("Desde") },
                    supportingText = { Text("0-23") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.MediumCard
                )
                OutlinedTextField(
                    value = quietHoursEndInput,
                    onValueChange = onQuietHoursEndChange,
                    label = { Text("Hasta") },
                    supportingText = { Text("0-23") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                    shape = AppShapes.MediumCard
                )
            }
            TextButton(
                onClick = onSaveQuietHours,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Guardar horario", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun NotificationPermissionStatus(granted: Boolean) {
    val statusColor = if (granted) UniStackColors.Green else UniStackColors.Coral
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.MediumCard)
            .background(statusColor.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (granted) {
                    "Permiso de notificaciones activo"
                } else {
                    "Permiso de notificaciones pendiente"
                },
                color = statusColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (granted) {
                    "UniStack puede enviarte avisos inteligentes según tus tareas, trabajos y materias."
                } else {
                    "Actívalo para recibir recordatorios y alertas académicas basadas en tu información."
                },
                color = UniStackColors.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
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

private fun percentInput(weight: Double): String {
    val percent = weight * 100.0
    return if (percent % 1.0 == 0.0) {
        percent.toInt().toString()
    } else {
        String.format(java.util.Locale.US, "%.1f", percent)
    }
}

private fun GradingScale.label(): String {
    return when (this) {
        GradingScale.ZERO_TO_FIVE -> "0-5"
        GradingScale.ZERO_TO_HUNDRED -> "0-100"
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

private fun Context.hasNotificationPermission(): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
}
