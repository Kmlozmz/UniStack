@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue

/**
 * Datos y respaldos: dónde está tu copia y qué se puede sacar de aquí.
 *
 * Lo irreversible va abajo, agrupado bajo su propio rótulo rojo. Antes «Reiniciar onboarding»
 * iba en una tarjeta más, entre exportaciones y con el mismo peso visual que guardar un CSV,
 * y las cosas que no se pueden deshacer no deberían parecerse a las que sí.
 */
@Composable
fun DataSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val cloudBackupState by viewModel.cloudBackupState.collectAsStateWithLifecycle()
    val actionState by viewModel.actionState.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current

    var feedback by rememberSaveable { mutableStateOf<String?>(null) }
    var showRestartDialog by rememberSaveable { mutableStateOf(false) }

    LargeTitleScaffold(
        title = stringResource(R.string.settings_data_title),
        subtitle = stringResource(R.string.settings_data_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
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
            SettingsGroup(label = stringResource(R.string.settings_data_reset_section), labelColor = MaterialTheme.colorScheme.error, rowCount = 1) {
                SettingsRow(
                    icon = Icons.Rounded.RestartAlt,
                    title = stringResource(R.string.settings_data_repeat_setup_title),
                    subtitle = stringResource(R.string.settings_data_repeat_setup_subtitle),
                    iconColor = MaterialTheme.colorScheme.error,
                    onClick = { showRestartDialog = true }
                )
            }
        }
        feedback?.let { message ->
            item {
                Text(
                    message,
                    modifier = Modifier.padding(horizontal = 4.dp),
                    color = if (message.startsWith("No se pudo") || message.startsWith("Could not") || message.startsWith("Failed")) {
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

    if (showRestartDialog) {
        AlertDialog(
            onDismissRequest = { showRestartDialog = false },
            title = { Text(stringResource(R.string.settings_data_repeat_setup_dialog_title)) },
            text = {
                Text(stringResource(R.string.settings_data_repeat_setup_dialog_msg))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRestartDialog = false
                        viewModel.restartOnboarding()
                    }
                ) {
                    Text(stringResource(R.string.settings_data_repeat_setup_confirm), color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestartDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        )
    }
}
