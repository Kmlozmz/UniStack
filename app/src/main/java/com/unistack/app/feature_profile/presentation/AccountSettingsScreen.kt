@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.runtime.LaunchedEffect
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRowIcon
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.CookieCorner
import com.unistack.app.core.design.components.UniStackButtonDefaults
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.TextValidators
import com.unistack.app.feature_profile.domain.FeatureGate
import com.unistack.app.feature_profile.domain.UserPlan

/**
 * Cuenta y perfil: quién eres y a qué cuenta está atado esto.
 *
 * El retrato manda y el lápiz va encima de él, no en una fila aparte que había que buscar.
 * Lo que se fue de aquí es «Tu semestre»: la escala, la meta y los cortes se contaban también
 * en el centro de ajustes y otra vez en la pantalla académica, tres veces el mismo dato en
 * tres sitios que se desincronizaban entre sí.
 */
@Composable
fun AccountSettingsScreen(
    onBackClick: () -> Unit,
    onOpenProClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val billingState by viewModel.billingState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val context = LocalContext.current
    val current = profile ?: return

    // El estado del plan hay que ir a pedirlo: vivía en el ProfileScreen que se partió en
    // cinco, y al desaparecer aquel se quedó sin nadie que lo refrescara. Sin esto, quien
    // acabara de comprar Pro seguía viendo el plan gratuito hasta reinstalar.
    LaunchedEffect(FeatureGate.PRO_FEATURES_ENABLED) {
        if (FeatureGate.PRO_FEATURES_ENABLED) {
            viewModel.refreshBilling()
        }
    }

    var editingName by rememberSaveable { mutableStateOf(false) }
    var nameInput by rememberSaveable(current.userId) { mutableStateOf(current.preferredName) }
    var showUnlinkDialog by rememberSaveable { mutableStateOf(false) }
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SettingsHeader(
                title = "Cuenta y perfil",
                subtitle = "Nombre, foto y sincronización",
                onBackClick = onBackClick
            )
        }
        item {
            AccountPortrait(
                name = current.preferredName.takeIf { it.isNotBlank() } ?: "Estudiante",
                detail = current.educationSummary(),
                photoUrl = current.accountPhotoUrl,
                onEditClick = {
                    nameInput = current.preferredName
                    editingName = true
                }
            )
        }
        item {
            SettingsGroup(label = "CUENTA") {
                AccountLinkRow(
                    linked = currentUser.isLinked,
                    title = if (currentUser.isLinked) currentUser.accountLabel() else "Sin cuenta vinculada",
                    detail = currentUser.email ?: "Vincula una para respaldar en la nube",
                    isBusy = actionState.isAccountBusy,
                    onClick = {
                        if (currentUser.isLinked) showUnlinkDialog = true else viewModel.connectGoogle(context)
                    }
                )
            }
        }
        if (FeatureGate.PRO_FEATURES_ENABLED) {
            item {
                Column {
                    Text(
                        text = "TU PLAN",
                        style = SectionLabelStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 9.dp)
                    )
                    PlanCard(
                        plan = FeatureGate.planFor(billingState.isPro),
                        onOpenProClick = onOpenProClick
                    )
                }
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (message.startsWith("Revisa")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        LocalSectionColors.current.onTrack
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }
        actionState.errorMessage?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
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
                    shape = MaterialTheme.shapes.large,
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
                        nameInput = current.preferredName
                        editingName = false
                    }
                ) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
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
                    Text("Desvincular", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showUnlinkDialog = false }) {
                    Text("Cancelar")
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}

/**
 * El retrato, el lápiz y el nombre, centrados.
 */
@Composable
private fun AccountPortrait(
    name: String,
    detail: String,
    photoUrl: String?,
    onEditClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box {
            AccountAvatar(
                photoUrl = photoUrl,
                contentDescription = "Foto de perfil",
                initial = name.first().uppercase(),
                modifier = Modifier.size(96.dp)
            )
            Surface(
                onClick = onEditClick,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 2.dp, y = 2.dp)
                    .size(32.dp)
                    // El borde del color del fondo separa el lápiz del retrato: sin él, dos
                    // círculos pegados se leen como una sola mancha con un bulto.
                    .border(3.dp, MaterialTheme.colorScheme.background, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Rounded.Edit,
                        contentDescription = "Editar nombre",
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = name,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * La cuenta: en qué estado está y el botón que la cambia, en la misma fila.
 */
@Composable
private fun AccountLinkRow(
    linked: Boolean,
    title: String,
    detail: String,
    isBusy: Boolean,
    onClick: () -> Unit
) {
    val sections = LocalSectionColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 15.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        SettingsRowIcon(
            icon = if (linked) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
            color = if (linked) sections.onTrack else sections.schedule
        )
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(1.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
        Surface(
            onClick = onClick,
            enabled = !isBusy,
            shape = CircleShape,
            color = if (linked) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.primary,
            contentColor = if (linked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
        ) {
            Text(
                text = when {
                    isBusy -> "..."
                    linked -> "Desvincular"
                    else -> "Vincular"
                },
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * El plan, con la forma de la marca detrás.
 */
@Composable
private fun PlanCard(plan: UserPlan, onOpenProClick: () -> Unit) {
    val ink = MaterialTheme.colorScheme.onPrimaryContainer
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = ink
    ) {
        Box {
            CookieCorner(color = ink, size = 140.dp, offsetX = 240.dp, offsetY = (-46).dp)
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (plan.isPro) "PLAN PRO" else "PLAN ESTUDIANTE",
                    style = SectionLabelStyle
                )
                Text(
                    text = if (plan.hasSubjectLimit) "Hasta ${plan.maxSubjects} materias" else "Materias ilimitadas",
                    modifier = Modifier.padding(top = 6.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (plan.isPro) {
                        "Gracias por sostener la app."
                    } else {
                        "Pro las quita y añade respaldo en la nube."
                    },
                    modifier = Modifier.padding(top = 3.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = ink.copy(alpha = 0.88f)
                )
                Surface(
                    onClick = onOpenProClick,
                    modifier = Modifier.padding(top = 13.dp),
                    shape = CircleShape,
                    color = ink,
                    contentColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = if (plan.isPro) "Ver tu plan" else "Ver UniStack Pro",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
