@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsSoloRow
import com.unistack.app.feature_user.domain.offerableModules
import com.unistack.app.core.design.components.SettingsToggleRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.AssignmentTurnedIn
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppModule
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos

/**
 * Qué áreas de la app están encendidas.
 *
 * La lista es la misma del paso 4 del onboarding, con el mismo atenuado al apagar. Lo que
 * faltaba era decir qué significa apagar: la pestaña desaparece de la barra y nada más. Sin
 * esa frase, apagar «Gastos» se parece demasiado a borrar los gastos.
 */
@Composable
fun ModuleSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val sections = LocalSectionColors.current
    val current = profile ?: return
    var feedback by rememberSaveable { mutableStateOf<String?>(null) }

    val enabled = current.enabledModules
    val offerable = offerableModules()
    val off = offerable.count { it !in enabled }

    LargeTitleScaffold(
        title = stringResource(R.string.settings_modules_title),
        subtitle = stringResource(R.string.settings_modules_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 10.dp
    ) {
        items(offerable, key = { it.name }) { module ->
            val minWarning = stringResource(R.string.settings_modules_min_warning)
            ModuleCard(
                icon = module.icon(),
                title = module.moduleLabel(),
                detail = module.moduleDetail(),
                checked = module in enabled,
                iconColor = module.tone(sections.schedule, sections.expenses, sections.onTrack, MaterialTheme.colorScheme.tertiary)
            ) {
                feedback = if (viewModel.toggleModule(module)) null else minWarning
            }
        }
        item {
            ModuleNote(off = off)
        }
        feedback?.let { message ->
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
}

@Composable
private fun ModuleCard(
    icon: ImageVector,
    title: String,
    detail: String,
    checked: Boolean,
    iconColor: Color,
    onToggle: (Boolean) -> Unit
) {
    SettingsSoloRow {
        SettingsToggleRow(
            icon = icon,
            title = title,
            subtitle = detail,
            checked = checked,
            iconColor = iconColor,
            onCheckedChange = onToggle
        )
    }
}

/**
 * Lo que pasa al apagar, contado con el número de apagados delante.
 */
@Composable
private fun ModuleNote(off: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Text(
            text = if (off == 0) {
                stringResource(R.string.settings_modules_all_on)
            } else {
                stringResource(R.string.settings_modules_some_off, off)
            },
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

private fun AppModule.icon(): ImageVector = when (this) {
    AppModule.GRADES -> Icons.AutoMirrored.Rounded.MenuBook
    AppModule.TASKS -> Icons.Rounded.AssignmentTurnedIn
    AppModule.EXPENSES -> Icons.Rounded.AccountBalanceWallet
    AppModule.ACADEMIC_TEMPLATES -> Icons.Rounded.Description
}

private fun AppModule.moduleLabel(): String {
    return when (this) {
        AppModule.GRADES -> Textos.get(R.string.settings_module_grades_title)
        AppModule.TASKS -> Textos.get(R.string.setup_mod_tasks_title)
        AppModule.EXPENSES -> Textos.get(R.string.setup_mod_expenses_title)
        AppModule.ACADEMIC_TEMPLATES -> Textos.get(R.string.settings_backup_works)
    }
}

private fun AppModule.moduleDetail(): String {
    return when (this) {
        AppModule.GRADES -> Textos.get(R.string.settings_module_grades_desc)
        AppModule.TASKS -> Textos.get(R.string.settings_module_tasks_desc)
        AppModule.EXPENSES -> Textos.get(R.string.settings_module_expenses_desc)
        AppModule.ACADEMIC_TEMPLATES -> Textos.get(R.string.settings_module_templates_desc)
    }
}

private fun AppModule.tone(schedule: Color, expenses: Color, onTrack: Color, tertiary: Color): Color =
    when (this) {
        AppModule.GRADES -> schedule
        AppModule.TASKS -> tertiary
        AppModule.EXPENSES -> expenses
        AppModule.ACADEMIC_TEMPLATES -> onTrack
    }
