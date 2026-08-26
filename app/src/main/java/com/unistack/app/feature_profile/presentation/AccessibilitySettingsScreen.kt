@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.components.SettingsSoloRow
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsToggleRow
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
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.TextScalePreference

/**
 * Accesibilidad: tres interruptores y dos escalas.
 *
 * Los tres interruptores vivían cada uno bajo su propio encabezado de una línea —«Formatos»,
 * «Lectura», «Movimiento»— para un ajuste cada uno. Tres títulos para tres filas es un índice
 * de un libro de tres páginas: cada fila ya dice lo suyo, y los encabezados sobraban.
 */
@Composable
fun AccessibilitySettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val sections = LocalSectionColors.current
    val current = profile ?: return
    val accessibility = current.accessibilityPreferences

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
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SettingsHeader(
                title = "Accesibilidad",
                subtitle = "Lectura, movimiento y formatos",
                onBackClick = onBackClick
            )
        }
        item {
            AccessibilityToggleCard(
                icon = Icons.Rounded.Schedule,
                title = "Formato de 24 horas",
                detail = if (accessibility.use24HourTime) "13:00 en vez de 1:00 p. m." else "1:00 p. m. en vez de 13:00",
                checked = accessibility.use24HourTime,
                iconColor = sections.schedule
            ) { enabled ->
                viewModel.updateAccessibility { it.copy(use24HourTime = enabled) }
            }
        }
        item {
            AccessibilityToggleCard(
                icon = Icons.Rounded.Contrast,
                title = "Contraste reforzado",
                detail = "Texto secundario y bordes más visibles",
                checked = accessibility.highContrastEnabled,
                iconColor = MaterialTheme.colorScheme.tertiary
            ) { enabled ->
                viewModel.updateAccessibility { it.copy(highContrastEnabled = enabled) }
            }
        }
        item {
            AccessibilityToggleCard(
                icon = Icons.Rounded.Animation,
                title = "Animación del hero",
                detail = "Movimiento de fondo en la tarjeta principal",
                checked = accessibility.heroAnimationEnabled,
                iconColor = sections.onTrack
            ) { enabled ->
                viewModel.updateAccessibility { it.copy(heroAnimationEnabled = enabled) }
            }
        }
        /*
         * Las dos escalas se quedan aquí, y no en Apariencia.
         *
         * La maqueta dejaba esta pantalla en tres interruptores, con la idea de que del
         * tamaño de letra y del movimiento ya se encarga Android. Se encarga a medias: lo
         * de Android se aplica, y encima UniStack multiplica su propia escala —×1,10 en
         * «Grande»— y su propia duración de animación. Quitar los controles no habría
         * devuelto el mando a Android; habría dejado a quien tuviera «Grande» o «Sin
         * movimiento» sin forma de volver atrás.
         *
         * Y siguen en Accesibilidad, no en Apariencia, porque es donde se buscan.
         */
        item {
            AccessibilityScaleGroup(label = "TAMAÑO DEL TEXTO") {
                UniSegmentedControl(
                    selected = accessibility.textScale,
                    options = TextScalePreference.entries.map { option ->
                        UniSegmentedOption(value = option, label = option.label())
                    },
                    onSelected = { value ->
                        viewModel.updateAccessibility { it.copy(textScale = value) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            AccessibilityScaleGroup(label = "MOVIMIENTO") {
                UniSegmentedControl(
                    selected = accessibility.motionPreference,
                    options = MotionPreference.entries.map { option ->
                        UniSegmentedOption(value = option, label = option.label())
                    },
                    onSelected = { value ->
                        viewModel.updateAccessibility { it.copy(motionPreference = value) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            AccessibilityNote()
        }
    }
}

/**
 * Un ajuste de accesibilidad: su tarjeta, su icono de color y su interruptor.
 */
@Composable
private fun AccessibilityToggleCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    checked: Boolean,
    iconColor: androidx.compose.ui.graphics.Color,
    onCheckedChange: (Boolean) -> Unit
) {
    SettingsSoloRow {
        SettingsToggleRow(
            icon = icon,
            title = title,
            subtitle = detail,
            checked = checked,
            iconColor = iconColor,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun AccessibilityScaleGroup(label: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = label,
            style = SectionLabelStyle,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 9.dp)
        )
        content()
    }
}

/**
 * Qué de esto lo manda Android y qué la app: lo que faltaba decir.
 */
@Composable
private fun AccessibilityNote() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(19.dp)
            )
            Text(
                text = "El tamaño de letra y las animaciones de Android se aplican igual. " +
                    "Lo de aquí se suma a eso, solo dentro de UniStack.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun TextScalePreference.label() = when (this) {
    TextScalePreference.STANDARD -> "Normal"
    TextScalePreference.LARGE -> "Grande"
}

private fun MotionPreference.label() = when (this) {
    MotionPreference.FULL -> "Completo"
    MotionPreference.REDUCED -> "Reducido"
    MotionPreference.NONE -> "Sin movimiento"
}
