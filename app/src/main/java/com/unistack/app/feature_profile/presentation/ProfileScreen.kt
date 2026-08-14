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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.SystemUpdate
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material.icons.rounded.School
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
import com.unistack.app.core.design.components.SquishyButton
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.core.design.theme.LocalBottomBarOverlay
import com.unistack.app.BuildConfig
import com.unistack.app.core.utils.BuildStage
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
    onOpenAcademicClick: () -> Unit = {},
    onOpenNotificationsClick: () -> Unit = {},
    onOpenModulesClick: () -> Unit = {},
    onOpenAppearanceClick: () -> Unit = {},
    onOpenDataClick: () -> Unit = {},
    onOpenUpdatesClick: () -> Unit = {},
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
    var editingName by rememberSaveable { mutableStateOf(false) }
    val academicSnapshot by viewModel.academicSnapshot.collectAsStateWithLifecycle()
    var showRestartDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var pendingScaleChange by remember { mutableStateOf<GradingScaleChangeImpact?>(null) }
    var confirmingScaleChange by remember { mutableStateOf<GradingScaleChangeImpact?>(null) }
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
        // Se suma lo que tape la barra flotante, que se dibuja encima del contenido. Con
        // la barra acoplada el valor es cero y esto queda igual que antes.
        contentPadding = PaddingValues(
            start = 20.dp,
            top = 58.dp,
            end = 20.dp,
            bottom = 20.dp + LocalBottomBarOverlay.current
        ),
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
                    // Con rótulo. Un engranaje suelto obliga a deducir a dónde lleva, y la
                    // deducción falla: el mismo icono abre ajustes, preferencias o edición
                    // según la app.
                    Row(
                        modifier = Modifier
                            .clip(AppShapes.Pill)
                            .background(UniStackColors.SurfaceVariant)
                            .bounceClick(onOpenSettingsClick)
                            .padding(start = 12.dp, end = 14.dp, top = 9.dp, bottom = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Settings,
                            contentDescription = null,
                            tint = UniStackColors.Primary,
                            modifier = Modifier.size(19.dp)
                        )
                        Spacer(Modifier.width(7.dp))
                        Text(
                            "Configuración",
                            color = UniStackColors.Primary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
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
                item {
                    ProfileHeaderCard(
                        profile = current,
                        onEditNameClick = {
                            nameInput = current.preferredName
                            editingName = true
                        }
                    )
                }
                item {
                    AcademicSnapshotCard(
                        snapshot = academicSnapshot,
                        profile = current,
                        onOpenAcademicClick = onOpenAcademicClick
                    )
                }
                item {
                    AccountSyncCard(
                        currentUser = currentUser,
                        isBusy = actionState.isAccountBusy,
                        onGoogleClick = { viewModel.connectGoogle(context) },
                        onUnlinkClick = { showUnlinkDialog = true }
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
                            val impact = viewModel.gradingScaleChangeImpact()
                            // El aviso solo aparece si de verdad hay algo que perder. Sacarlo
                            // siempre —lo normal es cambiar de escala recién salido del setup,
                            // sin una sola nota— enseña a cerrar diálogos sin leerlos, y
                            // entonces deja de servir el día que sí importa.
                            if (selectedScale != current.gradingScale && impact.isDestructive) {
                                pendingScaleChange = impact
                            } else {
                                feedback = if (viewModel.updateGradingSettings(selectedScale, passingGradeInput, targetAverageInput)) {
                                    "Configuración académica actualizada."
                                } else {
                                    "Revisa que las notas estén dentro de la escala."
                                }
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
                    NotificationSection(
                        profile = current,
                        permissionGranted = notificationPermissionGranted,
                        onRequestPermission = {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        },
                        onReminderToggle = { tasks, works, overdue, leadHours ->
                            // El permiso se pide solo cuando se enciende algo, no al entrar:
                            // preguntar antes de que haya nada que avisar es lo que enseña a
                            // decir que no.
                            val turningOn = (tasks && !current.taskRemindersEnabled) ||
                                (works && !current.academicWorkRemindersEnabled) ||
                                (overdue && !current.overdueRemindersEnabled)
                            runReminderUpdate(turningOn) {
                                viewModel.updateReminderSettings(tasks, works, overdue, leadHours)
                            }
                        },
                        onAcademicToggle = { insights, pending ->
                            val turningOn = (insights && !current.gradeInsightRemindersEnabled) ||
                                (pending && !current.pendingGradeRemindersEnabled)
                            runReminderUpdate(turningOn) {
                                viewModel.updateAcademicReminderSettings(insights, pending)
                            }
                        },
                        onQuietHoursChange = { enabled, start, end ->
                            feedback = if (viewModel.updateQuietHours(enabled, start, end)) {
                                null
                            } else {
                                "El horario silencioso necesita dos horas distintas."
                            }
                        }
                    )
                }
            }

            if (mode == ProfileScreenMode.DATA) {
                item {
                    BackupSection(
                        viewModel = viewModel,
                        cloudLinked = currentUser.isLinked,
                        cloudAvailable = viewModel.cloudAvailable,
                        cloudBusy = cloudBackupState.inProgress,
                        cloudStatus = cloudBackupState.message ?: cloudBackupState.errorMessage,
                        dataSummary = viewModel.localDataSummary(),
                        onFeedback = { feedback = it }
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

    pendingScaleChange?.let { impact ->
        AlertDialog(
            onDismissRequest = { pendingScaleChange = null },
            title = { Text("¿Cambiar la escala de notas?") },
            // El conteo va en el texto a propósito: «perderás tus notas» se descarta sin
            // leer, «borrará 23 notas en 4 materias» hace parar.
            text = {
                Text(
                    "Esto borrará ${impact.describe()}. Una nota registrada en otra escala " +
                        "no se puede reexpresar sin inventar el número, así que se elimina en " +
                        "vez de convertirse. Las metas de tus materias vuelven al valor del perfil."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingScaleChange = null
                        confirmingScaleChange = impact
                    }
                ) {
                    Text("Continuar", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingScaleChange = null }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = UniStackColors.Background
        )
    }

    // Segundo paso. El primero explica y da contexto; este solo pregunta si de verdad,
    // y es deliberadamente escueto: si repitiera el razonamiento se leería como el mismo
    // diálogo dos veces y se cerraría por inercia. Aquí lo único nuevo es que no hay vuelta atrás.
    confirmingScaleChange?.let { impact ->
        AlertDialog(
            onDismissRequest = { confirmingScaleChange = null },
            title = { Text("Esto no se puede deshacer") },
            text = {
                Text(
                    "Vas a borrar ${impact.describe()} de forma permanente. No hay copia " +
                        "de seguridad ni forma de recuperarlas después."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmingScaleChange = null
                        feedback = if (viewModel.updateGradingSettings(selectedScale, passingGradeInput, targetAverageInput)) {
                            "Escala actualizada. Se borraron ${impact.describe()}."
                        } else {
                            "Revisa que las notas estén dentro de la escala."
                        }
                    }
                ) {
                    Text("Sí, borrar definitivamente", color = UniStackColors.Coral, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmingScaleChange = null }) {
                    Text("Cancelar", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = UniStackColors.Background
        )
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

    if (editingName) {
        val validation = TextValidators.validateDisplayName(nameInput)
        AlertDialog(
            onDismissRequest = { editingName = false },
            title = { Text("Tu nombre") },
            text = {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it.take(30) },
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
            },
            confirmButton = {
                TextButton(
                    enabled = validation.isValid,
                    onClick = {
                        feedback = if (viewModel.updatePreferredName(nameInput)) {
                            editingName = false
                            "Nombre actualizado."
                        } else {
                            "Revisa el nombre antes de guardar."
                        }
                    }
                ) {
                    Text("Guardar", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        nameInput = currentProfile?.preferredName.orEmpty()
                        editingName = false
                    }
                ) {
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
        SquishyButton(
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
            SquishyButton(
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
private fun ProfileHeaderCard(profile: UserProfile, onEditNameClick: () -> Unit) {
    val name = profile.preferredName.takeIf { it.isNotBlank() } ?: "Estudiante"
    val scheme = profile.academicPeriodScheme
    val count = scheme.periods.size
    val periodLabel = if (count == 1) scheme.label.singular else scheme.label.plural

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.PrimaryLight,
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AccountAvatar(
                    photoUrl = profile.accountPhotoUrl,
                    contentDescription = "Foto de perfil",
                    modifier = Modifier.size(58.dp)
                )
                Column(
                    modifier = Modifier.padding(start = 14.dp).weight(1f),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        name,
                        color = UniStackColors.TextPrimary,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(profile.educationSummary(), color = UniStackColors.TextSecondary, fontSize = 13.sp)
                    profile.institutionName?.takeIf(String::isNotBlank)?.let { institution ->
                        Text(institution, color = UniStackColors.TextSecondary, fontSize = 12.sp)
                    }
                }
                // El nombre se edita desde aquí. Antes vivía en una tarjeta aparte con su campo
                // y su botón de guardar siempre a la vista: un formulario permanente para un dato
                // que se cambia una vez al año, ocupando un tercio de la pantalla.
                IconButton(onClick = onEditNameClick) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Editar nombre",
                        tint = UniStackColors.Primary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                ProfileChip("Meta " + GradingScaleUtils.formatGrade(profile.targetAverage, profile.gradingScale))
                ProfileChip(profileScaleLabel(profile))
                ProfileChip("$count $periodLabel")
            }
        }
    }
}

@Composable
private fun ProfileChip(text: String) {
    Box(
        modifier = Modifier
            .clip(AppShapes.Pill)
            .background(UniStackColors.Background.copy(alpha = 0.55f))
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(text, color = UniStackColors.TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

/**
 * Cómo va el semestre.
 *
 * El perfil decía cuál era tu meta y nada de si la estás cumpliendo, que es lo único que
 * convierte ese número en información. La barra compara el promedio de lo evaluado con la meta,
 * y debajo están las cifras que la explican: cuántas materias hay, cuántas van aprobando,
 * cuántas por debajo del mínimo y cuántas notas llevas registradas.
 */
@Composable
private fun AcademicSnapshotCard(
    snapshot: AcademicSnapshot,
    profile: UserProfile,
    onOpenAcademicClick: () -> Unit
) {
    val scale = profile.gradingScale
    val target = profile.targetAverage
    val averageText = snapshot.average?.let { GradingScaleUtils.formatGrade(it, scale) } ?: "—"
    val distance = snapshot.average?.minus(target)
    val progress = snapshot.average
        ?.let { (it / target).coerceIn(0.0, 1.0).toFloat() }
        ?: 0f
    val footer = when {
        snapshot.subjectCount == 0 -> "Todavía no has creado materias."
        distance == null -> "Aún no hay notas registradas."
        distance >= 0 -> "Vas por encima de tu meta."
        else -> "Te faltan " + GradingScaleUtils.formatGrade(-distance, scale) + " para tu meta."
    }

    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        shape = AppShapes.LargeCard
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = UniStackColors.Primary)
                Text(
                    "Tu semestre",
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(start = 10.dp).weight(1f)
                )
                TextButton(onClick = onOpenAcademicClick) {
                    Text("Ajustar", color = UniStackColors.Primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    averageText,
                    color = UniStackColors.TextPrimary,
                    fontSize = 34.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "  de meta " + GradingScaleUtils.formatGrade(target, scale),
                    color = UniStackColors.TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            // La barra se llena al llegar a la meta, no al llegar al máximo de la escala: la
            // referencia que importa es la que tú te pusiste.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(AppShapes.Pill)
                    .background(UniStackColors.SurfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(8.dp)
                        .clip(AppShapes.Pill)
                        .background(if (progress >= 1f) UniStackColors.Green else UniStackColors.Primary)
                )
            }
            Text(footer, color = UniStackColors.TextSecondary, fontSize = 12.sp)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SnapshotCell(
                    modifier = Modifier.weight(1f),
                    value = snapshot.subjectCount.toString(),
                    label = if (snapshot.subjectCount == 1) "Materia" else "Materias"
                )
                SnapshotCell(
                    modifier = Modifier.weight(1f),
                    value = snapshot.passing.toString(),
                    label = "Aprobando"
                )
                SnapshotCell(
                    modifier = Modifier.weight(1f),
                    value = snapshot.atRisk.toString(),
                    label = "En riesgo",
                    valueColor = if (snapshot.atRisk > 0) UniStackColors.Coral else UniStackColors.TextPrimary
                )
                SnapshotCell(
                    modifier = Modifier.weight(1f),
                    value = snapshot.gradeCount.toString(),
                    label = "Notas"
                )
            }
        }
    }
}

@Composable
private fun SnapshotCell(
    modifier: Modifier,
    value: String,
    label: String,
    valueColor: Color = UniStackColors.TextPrimary
) {
    Column(
        modifier = modifier
            .clip(AppShapes.MediumCard)
            .background(UniStackColors.SurfaceVariant)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(value, color = valueColor, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
        Text(
            label,
            color = UniStackColors.TextSecondary,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center
        )
    }
}

/** La escala en corto: «0 a 5», «0 a 100», o el máximo que se haya puesto a mano. */
private fun profileScaleLabel(profile: UserProfile): String = when (profile.gradingScale) {
    GradingScale.ZERO_TO_FIVE -> "Escala 0 a 5"
    GradingScale.ZERO_TO_HUNDRED -> "Escala 0 a 100"
    GradingScale.CUSTOM -> "Escala 0 a " + GradingScaleUtils.formatGrade(profile.customGradeMax, profile.gradingScale)
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
        SquishyButton(
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
        SquishyButton(
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
    // Trabajos no se puede abrir fuera de dev y alpha, así que tampoco se ofrece encender:
    // un interruptor que enciende una sección a la que no se entra es peor que no tenerlo.
    val visibleModules = AppModule.entries.filter { module ->
        module != AppModule.ACADEMIC_TEMPLATES ||
            BuildStage.of(BuildConfig.VERSION_NAME).allowsUnfinished
    }
    SettingsCard(title = "Módulos activos") {
        visibleModules.forEach { module: AppModule ->
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
            SquishyButton(
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
