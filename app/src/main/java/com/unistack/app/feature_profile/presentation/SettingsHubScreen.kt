@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.feature_user.domain.portraitUrl
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsGroup
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
import androidx.compose.material.icons.rounded.Person
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

/**
 * El centro de configuración: quién eres, y las ocho puertas.
 *
 * Arriba va tu ficha, que antes había que ir a buscar dentro de «Cuenta y perfil»: el sitio
 * donde se configura la app es el sitio donde tiene sentido ver de quién es.
 *
 * Aquí no hay tarjeta de Pro. La hubo, y sobraba por dos motivos: mientras Pro esté apagado no
 * hay nada que ofrecer —la app va sin límites—, y aunque estuviera encendido, lo que se compra
 * y el plan que se tiene son asunto de «Cuenta y perfil», no del índice de ajustes.
 */
@Composable
fun SettingsHubScreen(
    onBackClick: (() -> Unit)?,
    onAppearanceClick: () -> Unit,
    onAccessibilityClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAcademicClick: () -> Unit,
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
                title = "Configuración",
                subtitle = "Tu experiencia, tus datos y tu semestre",
                onBackClick = onBackClick
            )
        }
        item {
            SettingsIdentityCard(profile = profile, onClick = onProfileClick)
        }
        item {
            SettingsGroup(label = "PERSONALIZACIÓN", rowCount = 2) {
                SettingsRow(
                    icon = Icons.Rounded.Palette,
                    title = "Apariencia",
                    subtitle = "Tema, color, densidad y tu inicio",
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = onAppearanceClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Accessibility,
                    title = "Accesibilidad",
                    subtitle = "Lectura, movimiento y formatos",
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onAccessibilityClick
                )
            }
        }
        item {
            SettingsGroup(label = "TU SEMESTRE", rowCount = 3) {
                SettingsRow(
                    icon = Icons.Rounded.School,
                    title = "Configuración académica",
                    subtitle = "Escala, metas y cortes",
                    iconColor = sections.schedule,
                    onClick = onAcademicClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Widgets,
                    title = "Módulos",
                    subtitle = "Qué áreas usas",
                    iconColor = sections.onTrack,
                    onClick = onModulesClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Person,
                    title = "Cuenta y perfil",
                    subtitle = "Nombre, foto y sincronización",
                    iconColor = sections.atRisk,
                    onClick = onProfileClick
                )
            }
        }
        item {
            SettingsGroup(label = "LA APP", rowCount = 3) {
                SettingsRow(
                    icon = Icons.Rounded.Notifications,
                    title = "Notificaciones",
                    subtitle = "Avisos, permiso y silencio",
                    iconColor = MaterialTheme.colorScheme.tertiary,
                    onClick = onNotificationsClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Backup,
                    title = "Datos y respaldos",
                    subtitle = "Copias, exportar y restaurar",
                    iconColor = sections.schedule,
                    onClick = onDataClick
                )
                SettingsRow(
                    icon = Icons.Rounded.Refresh,
                    title = "Actualizaciones",
                    subtitle = "Comprueba y descarga",
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
    val name = profile?.preferredName?.takeIf { it.isNotBlank() } ?: "Estudiante"
    val detail = remember(profile) { profile?.let(::settingsIdentityDetail) ?: "Sin configurar" }

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
                contentDescription = "Foto de perfil",
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
                Text(
                    text = detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
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
 * Nivel, programa y cuántos cortes, en una línea.
 */
private fun settingsIdentityDetail(profile: UserProfile): String {
    val scheme = profile.academicPeriodScheme
    val count = scheme.periods.size
    val periodLabel = if (count == 1) scheme.label.singular else scheme.label.plural
    return profile.educationSummary() + " · " + count + " " + periodLabel.lowercase()
}
