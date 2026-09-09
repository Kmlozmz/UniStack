@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.feature_user.domain.portraitUrl
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsGroup
import androidx.compose.material.icons.rounded.History
import com.unistack.app.core.design.components.SettingsRow
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.BuildConfig
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.UserProfile
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.unistack.app.R

/**
 * El centro de configuración: quién eres, y las ocho puertas.
 *
 * Arriba va tu ficha, que antes había que ir a buscar dentro de «Cuenta y perfil»: el sitio
 * donde se configura la app es el sitio donde tiene sentido ver de quién es.
 *
 * No hay tarjeta de plan ni nada que vender: la app va entera y sin límites.
 */
@Composable
fun SettingsHubScreen(
    onBackClick: (() -> Unit)?,
    onAppearanceClick: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAcademicClick: () -> Unit,
    onAcademicHistoryClick: () -> Unit,
    onModulesClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDataClick: () -> Unit,
    onUpdatesClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val sections = LocalSectionColors.current

    LargeTitleScaffold(
        title = stringResource(R.string.settings_title),
        subtitle = stringResource(R.string.settings_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item {
            SettingsIdentityCard(profile = profile, onClick = onProfileClick)
        }
        item {
            SettingsGroup(label = stringResource(R.string.settings_section_customization), rowCount = 2) {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_appearance),
                    subtitle = stringResource(R.string.settings_appearance_desc),
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onAppearanceClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Accessibility,
                    title = stringResource(R.string.settings_accessibility),
                    subtitle = stringResource(R.string.settings_accessibility_desc),
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onAccessibilityClick
                )
            }
        }
        item {
            SettingsGroup(label = stringResource(R.string.settings_section_semester), rowCount = 3) {
                SettingsRow(
                    icon = Icons.Rounded.School,
                    title = stringResource(R.string.settings_academic),
                    subtitle = stringResource(R.string.settings_academic_desc),
                    iconColor = sections.schedule,
                    onClick = onAcademicClick
                )
                /*
                 * El periodo vive aqui porque es literalmente el semestre.
                 *
                 * Desde esta fila se llega a las tres cosas que hacen falta: ver los periodos
                 * cerrados, entrar en uno, y cerrar el que esta en curso. Cerrar no tiene fila
                 * propia a proposito: no es un ajuste que se toque de paso.
                 */
                SettingsRow(
                    icon = Icons.Rounded.History,
                    title = stringResource(R.string.settings_academic_history),
                    subtitle = stringResource(R.string.settings_academic_history_desc),
                    iconColor = sections.onTrack,
                    onClick = onAcademicHistoryClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Widgets,
                    title = stringResource(R.string.settings_modules),
                    subtitle = stringResource(R.string.settings_modules_desc),
                    iconColor = sections.onTrack,
                    onClick = onModulesClick
                )
            }
        }
        item {
            SettingsGroup(label = stringResource(R.string.settings_section_app), rowCount = 3) {
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    title = stringResource(R.string.settings_notifications),
                    subtitle = stringResource(R.string.settings_notifications_desc),
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onNotificationsClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Backup,
                    title = stringResource(R.string.settings_data),
                    subtitle = stringResource(R.string.settings_data_desc),
                    iconColor = sections.schedule,
                    onClick = onDataClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Refresh,
                    title = stringResource(R.string.settings_updates),
                    subtitle = stringResource(R.string.settings_updates_desc),
                    iconColor = sections.expenses,
                    onClick = onUpdatesClick
                )
            }
        }
        item {
            // El pie dice la versión y dónde están tus datos. Lo segundo es lo que la gente
            // viene a comprobar a los ajustes de una app que le pide el nombre y las notas.
            Text(
                text = "UniStack " + BuildConfig.VERSION_NAME,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

/**
 * Tu ficha, arriba del todo: foto, nombre y en qué semestre estás.
 */
@Composable
private fun SettingsIdentityCard(profile: UserProfile?, onClick: () -> Unit) {
    val defaultName = stringResource(R.string.settings_student_default)
    val notConfigured = stringResource(R.string.settings_not_configured)
    val name = profile?.preferredName?.takeIf { it.isNotBlank() } ?: defaultName
    val detail = remember(profile, notConfigured) { profile?.let(::settingsIdentityDetail) ?: notConfigured }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AccountAvatar(
                photoUrl = profile?.portraitUrl,
                contentDescription = stringResource(R.string.profile_picture),
                initial = name.first().uppercase(),
                modifier = Modifier.size(52.dp)
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                // Dos líneas y con puntos suspensivos: a una sola, «Universidad del Atlántico»
                // se cortaba a media palabra y sin ni siquiera un «…» que lo dijera.
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline
            )
        }
    }
}

/**
 * Programa e institución, en una línea.
 *
 * Llevaba el número de cortes al final -- «3 cortes» -- y no decía nada útil aquí: es un dato
 * de Configuración académica, no una ficha de identidad. Se quitó a petición suya.
 */
private fun settingsIdentityDetail(profile: UserProfile): String = profile.educationSummary()
