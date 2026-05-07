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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
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
import com.unistack.app.feature_profile.domain.UserPlan
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
    val profile by viewModel.profile.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val plan = viewModel.userPlan
    var nameInput by rememberSaveable { mutableStateOf("") }
    var selectedScale by rememberSaveable { mutableStateOf(GradingScale.ZERO_TO_FIVE) }
    var passingGradeInput by rememberSaveable { mutableStateOf("") }
    var targetAverageInput by rememberSaveable { mutableStateOf("") }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var showRestartDialog by remember { mutableStateOf(false) }
    var showGoogleDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }

    LaunchedEffect(profile?.updatedAt, profile?.userId) {
        val current = profile ?: return@LaunchedEffect
        nameInput = current.preferredName
        selectedScale = current.gradingScale.supportedNumericScale()
        passingGradeInput = GradingScaleUtils.formatGrade(current.passingGrade, selectedScale)
        targetAverageInput = GradingScaleUtils.formatGrade(current.targetAverage, selectedScale)
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

        if (profile == null) {
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
            val current = profile!!
            item { ProfileHeaderCard(profile = current) }
            item {
                AccountSyncCard(
                    currentUser = currentUser,
                    onGoogleClick = { showGoogleDialog = true },
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
                    passingGradeInput = passingGradeInput,
                    targetAverageInput = targetAverageInput,
                    onScaleSelected = { scale ->
                        selectedScale = scale
                        passingGradeInput = GradingScaleUtils.formatGrade(
                            GradingScaleUtils.defaultPassingGradeFor(scale),
                            scale
                        )
                        targetAverageInput = GradingScaleUtils.formatGrade(
                            GradingScaleUtils.defaultTargetAverageFor(scale),
                            scale
                        )
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
            containerColor = UniStackColors.Card
        )
    }

    if (showGoogleDialog) {
        AlertDialog(
            onDismissRequest = { showGoogleDialog = false },
            title = { Text("Google Sign-In preparado") },
            text = { Text("La app ya tiene la estructura de cuenta, foto y backup futuro. Falta configurar el proveedor OAuth para activar el inicio de sesión real.") },
            confirmButton = {
                TextButton(onClick = { showGoogleDialog = false }) {
                    Text("Entendido", color = UniStackColors.Primary, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = UniStackColors.Card
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
                        feedback = if (viewModel.unlinkAccount()) {
                            "Cuenta desvinculada."
                        } else {
                            "No hay cuenta vinculada."
                        }
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
            containerColor = UniStackColors.Card
        )
    }
}

@Composable
private fun AccountSyncCard(
    currentUser: AppUser,
    onGoogleClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    SettingsCard(title = "Cuenta y backup") {
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
            text = "Backup local listo para vinculación futura.",
            color = UniStackColors.TextSecondary,
            fontSize = 12.sp,
            lineHeight = 16.sp
        )
        Button(
            onClick = if (currentUser.isLinked) onUnlinkClick else onGoogleClick,
            shape = AppShapes.Pill,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (currentUser.isLinked) UniStackColors.SurfaceVariant else UniStackColors.Primary,
                contentColor = if (currentUser.isLinked) UniStackColors.TextPrimary else Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (currentUser.isLinked) "Desvincular cuenta" else "Conectar con Google")
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
                    "Meta ${GradingScaleUtils.formatGrade(profile.targetAverage, profile.gradingScale.supportedNumericScale())}",
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
    passingGradeInput: String,
    targetAverageInput: String,
    onScaleSelected: (GradingScale) -> Unit,
    onPassingGradeChange: (String) -> Unit,
    onTargetAverageChange: (String) -> Unit,
    onSaveClick: () -> Unit
) {
    val maxGrade = GradingScaleUtils.maxGradeFor(selectedScale)
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
                GradingScale.ZERO_TO_TEN,
                GradingScale.ZERO_TO_ONE_HUNDRED
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
        AppModule.values().forEach { module: AppModule ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(module.label(), color = UniStackColors.TextPrimary, fontWeight = FontWeight.Bold)
                    Text(module.description(), color = UniStackColors.TextSecondary, fontSize = 12.sp)
                }
                Checkbox(
                    checked = module in enabledModules,
                    onCheckedChange = { onToggleModule(module) }
                )
            }
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
            VisualPreference.values().forEach { preference: VisualPreference ->
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
        modifier = modifier.bounceClick(onClick),
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
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun UserProfile.educationSummary(): String {
    val level = when (educationLevel) {
        EducationLevel.SCHOOL -> "Colegio"
        EducationLevel.UNIVERSITY -> "Universidad"
        EducationLevel.TECHNICAL -> "Técnico / Tecnólogo"
        EducationLevel.INDEPENDENT_COURSE -> "Curso independiente"
        EducationLevel.OTHER -> "Otro"
    }
    val detail = gradeLevel ?: careerOrProgram
    return if (detail.isNullOrBlank()) level else "$level · $detail"
}

private fun GradingScale.supportedNumericScale(): GradingScale {
    return when (this) {
        GradingScale.ZERO_TO_FIVE,
        GradingScale.ZERO_TO_TEN,
        GradingScale.ZERO_TO_ONE_HUNDRED -> this
        GradingScale.LETTERS,
        GradingScale.CUSTOM -> GradingScale.ZERO_TO_FIVE
    }
}

private fun GradingScale.label(): String {
    return when (this) {
        GradingScale.ZERO_TO_FIVE -> "0-5"
        GradingScale.ZERO_TO_TEN -> "0-10"
        GradingScale.ZERO_TO_ONE_HUNDRED -> "0-100"
        GradingScale.LETTERS -> "Letras"
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
        AppModule.GRADES -> "Materias, notas y simulador."
        AppModule.TASKS -> "Entregas y pendientes."
        AppModule.EXPENSES -> "Registro y resumen de gastos."
        AppModule.ACADEMIC_TEMPLATES -> "Plantillas académicas futuras."
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
