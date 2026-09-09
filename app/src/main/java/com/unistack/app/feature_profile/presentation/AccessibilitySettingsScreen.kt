@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.SettingsCustomRow
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsRow
import com.unistack.app.core.design.components.SettingsToggleRow
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniBackButton
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.duracionDeDeshacer
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.ColorBlindPalette
import com.unistack.app.feature_user.domain.ContrastLevel
import com.unistack.app.feature_user.domain.CurrencyPreference
import com.unistack.app.feature_user.domain.DateFormatPreference
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.ReadingFont
import com.unistack.app.feature_user.domain.UndoDuration
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/**
 * Accesibilidad: visión, interacción, asistencia y sistema bajo Material 3 Expressive.
 *
 * Agrupada en tres bloques segmentados con preview interactiva en tiempo real y componentes
 * consistentes de diseño (SettingsGroup, SettingsToggleRow, SettingsCustomRow, SettingsRow).
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
    val a11y = current.accessibilityPreferences

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var testSnackbarJob by remember { mutableStateOf<Job?>(null) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showDateFormatDialog by remember { mutableStateOf(false) }
    var pendingDateFormat by remember { mutableStateOf<DateFormatPreference?>(null) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.a11y_title),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                subtitle = {
                    Text(
                        text = stringResource(R.string.a11y_subtitle),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    UniBackButton(onClick = onBackClick)
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = 8.dp,
                bottom = scrollBottomRoom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ------------------------------------------------------------------ VISTA PREVIA
            item {
                LiveAccessibilityPreview(
                    contrast = a11y.contrast,
                    colorBlindPalette = a11y.colorBlindPalette,
                    shapesBesidesColor = a11y.shapesBesidesColor,
                    boldText = a11y.boldText,
                    readingFont = a11y.readingFont,
                    reduceTransparency = a11y.reduceTransparency
                )
            }

            // ------------------------------------------------------------------ VISIÓN Y LECTURA
            item {
                SettingsGroup(label = stringResource(R.string.a11y_section_vision), rowCount = 5) {
                    SettingsCustomRow(
                        icon = Icons.Rounded.Contrast,
                        title = stringResource(R.string.a11y_contrast_title),
                        subtitle = stringResource(R.string.a11y_contrast_subtitle),
                        iconColor = MaterialTheme.colorScheme.primary
                    ) {
                        UniSegmentedControl(
                            selected = a11y.contrast,
                            options = ContrastLevel.entries.map {
                                UniSegmentedOption(value = it, label = it.etiqueta())
                            },
                            onSelected = { valor ->
                                viewModel.updateAccessibility {
                                    it.copy(
                                        contrast = valor,
                                        highContrastEnabled = (valor != ContrastLevel.ESTANDAR)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsCustomRow(
                        icon = Icons.Rounded.Palette,
                        title = stringResource(R.string.a11y_colorblind_title),
                        subtitle = stringResource(R.string.a11y_colorblind_subtitle),
                        iconColor = sections.onTrack
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            UniSegmentedControl(
                                selected = a11y.colorBlindPalette,
                                options = listOf(
                                    UniSegmentedOption(
                                        value = ColorBlindPalette.NINGUNA,
                                        label = ColorBlindPalette.NINGUNA.etiqueta(),
                                        weight = 0.85f
                                    ),
                                    UniSegmentedOption(
                                        value = ColorBlindPalette.DEUTERANOPIA,
                                        label = ColorBlindPalette.DEUTERANOPIA.etiqueta(),
                                        weight = 1.35f
                                    ),
                                    UniSegmentedOption(
                                        value = ColorBlindPalette.TRITANOPIA,
                                        label = ColorBlindPalette.TRITANOPIA.etiqueta(),
                                        weight = 1.05f
                                    )
                                ),
                                onSelected = { valor ->
                                    viewModel.updateAccessibility { it.copy(colorBlindPalette = valor) }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                            MuestraDePaleta(a11y.colorBlindPalette)
                        }
                    }

                    SettingsToggleRow(
                        icon = Icons.Rounded.Category,
                        title = stringResource(R.string.a11y_shapes_besides_color_title),
                        subtitle = stringResource(R.string.a11y_shapes_besides_color_subtitle),
                        checked = a11y.shapesBesidesColor,
                        iconColor = sections.onTrack,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(shapesBesidesColor = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.Spellcheck,
                        title = stringResource(R.string.a11y_reading_font_title),
                        subtitle = stringResource(R.string.a11y_reading_font_subtitle),
                        checked = a11y.readingFont == ReadingFont.DISLEXIA,
                        iconColor = sections.schedule,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility {
                                it.copy(readingFont = if (valor) ReadingFont.DISLEXIA else ReadingFont.NORMAL)
                            }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.FormatBold,
                        title = stringResource(R.string.a11y_bold_text_title),
                        subtitle = stringResource(R.string.a11y_bold_text_subtitle),
                        checked = a11y.boldText,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(boldText = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.LayersClear,
                        title = stringResource(R.string.a11y_reduce_transparency_title),
                        subtitle = stringResource(R.string.a11y_reduce_transparency_subtitle),
                        checked = a11y.reduceTransparency,
                        iconColor = MaterialTheme.colorScheme.outline,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(reduceTransparency = valor) }
                        }
                    )
                }
            }

            // ------------------------------------------------------------------ INTERACCIÓN Y MOVIMIENTO
            item {
                SettingsGroup(label = stringResource(R.string.a11y_section_interaction), rowCount = 2) {
                    SettingsCustomRow(
                        icon = Icons.Rounded.Animation,
                        title = stringResource(R.string.a11y_motion_title),
                        subtitle = stringResource(R.string.a11y_motion_subtitle),
                        iconColor = MaterialTheme.colorScheme.primary
                    ) {
                        UniSegmentedControl(
                            selected = a11y.motionPreference,
                            options = MotionPreference.entries.map {
                                UniSegmentedOption(value = it, label = it.etiquetaA11y())
                            },
                            onSelected = { valor ->
                                viewModel.updateAccessibility { it.copy(motionPreference = valor) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsCustomRow(
                        icon = Icons.Rounded.History,
                        title = stringResource(R.string.a11y_undo_title),
                        subtitle = stringResource(R.string.a11y_undo_subtitle),
                        iconColor = sections.schedule,
                        trailingAction = {
                            val duration = duracionDeDeshacer()
                            val seconds = a11y.undoDuration.segundos
                            val testMsg = stringResource(R.string.a11y_test_snackbar_message)
                            val undoLabel = stringResource(R.string.action_undo)
                            TextButton(
                                onClick = {
                                    testSnackbarJob?.cancel()
                                    testSnackbarJob = scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "$testMsg ($seconds s)",
                                            actionLabel = undoLabel,
                                            withDismissAction = true,
                                            duration = duration
                                        )
                                    }
                                }
                            ) {
                                Icon(
                                    Icons.Rounded.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(stringResource(R.string.a11y_test_snackbar), style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    ) {
                        UniSegmentedControl(
                            selected = a11y.undoDuration,
                            options = UndoDuration.entries.map {
                                UniSegmentedOption(value = it, label = "${it.segundos} s")
                            },
                            onSelected = { valor ->
                                viewModel.updateAccessibility { it.copy(undoDuration = valor) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // ------------------------------------------------------------------ FORMATOS E IDIOMA
            item {
                SettingsGroup(label = stringResource(R.string.a11y_section_formats), rowCount = 4) {
                    val langLabel = when (a11y.appLanguage) {
                        AppLanguage.SYSTEM -> stringResource(R.string.a11y_lang_system)
                        AppLanguage.SPANISH -> stringResource(R.string.a11y_lang_spanish)
                        AppLanguage.ENGLISH -> stringResource(R.string.a11y_lang_english)
                    }
                    SettingsRow(
                        icon = Icons.Rounded.Translate,
                        title = stringResource(R.string.a11y_language_title),
                        subtitle = langLabel,
                        iconColor = MaterialTheme.colorScheme.secondary,
                        onClick = { showLanguageDialog = true }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.AccessTime,
                        title = stringResource(R.string.a11y_time_format_title),
                        subtitle = if (a11y.use24HourTime) stringResource(R.string.a11y_time_format_24) else stringResource(R.string.a11y_time_format_12),
                        checked = a11y.use24HourTime,
                        iconColor = sections.schedule,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(use24HourTime = valor) }
                        }
                    )

                    SettingsRow(
                        icon = Icons.Rounded.CalendarMonth,
                        title = stringResource(R.string.a11y_date_format_title),
                        subtitle = "${a11y.dateFormat.label} · ${a11y.dateFormat.previewDate}",
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = { showDateFormatDialog = true }
                    )

                    SettingsRow(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        title = stringResource(R.string.a11y_currency_title),
                        subtitle = "${a11y.currency.label} · ${a11y.currency.preview}",
                        iconColor = sections.onTrack,
                        onClick = { showCurrencyDialog = true }
                    )
                }
            }

            // ------------------------------------------------------------------ ASISTENCIA Y SISTEMA
            item {
                SettingsGroup(label = stringResource(R.string.a11y_section_system), rowCount = 3) {
                    SettingsToggleRow(
                        icon = Icons.Rounded.RecordVoiceOver,
                        title = stringResource(R.string.a11y_spoken_descriptions_title),
                        subtitle = stringResource(R.string.a11y_spoken_descriptions_subtitle),
                        checked = a11y.spokenDescriptions,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(spokenDescriptions = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.Warning,
                        title = stringResource(R.string.a11y_confirm_irreversible_title),
                        subtitle = stringResource(R.string.a11y_confirm_irreversible_subtitle),
                        checked = a11y.confirmIrreversible,
                        iconColor = MaterialTheme.colorScheme.error,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(confirmIrreversible = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.StayCurrentPortrait,
                        title = stringResource(R.string.a11y_keep_screen_on_title),
                        subtitle = stringResource(R.string.a11y_keep_screen_on_subtitle),
                        checked = a11y.keepScreenOn,
                        iconColor = sections.schedule,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(keepScreenOn = valor) }
                        }
                    )
                }
            }
        }
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            current = a11y.appLanguage,
            onSelect = { selected ->
                if (selected != a11y.appLanguage) {
                    viewModel.updateAccessibility { it.copy(appLanguage = selected) }
                    com.unistack.app.core.utils.LocaleHelper.persistLanguage(context, selected)
                    (context as? android.app.Activity)?.recreate()
                }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }

    if (showDateFormatDialog) {
        DateFormatSelectionDialog(
            current = a11y.dateFormat,
            onSelect = { selected ->
                showDateFormatDialog = false
                if (selected != a11y.dateFormat) {
                    pendingDateFormat = selected
                }
            },
            onDismiss = { showDateFormatDialog = false }
        )
    }

    pendingDateFormat?.let { pending ->
        DateFormatWarningDialog(
            currentFormat = a11y.dateFormat,
            newFormat = pending,
            onConfirm = {
                viewModel.updateAccessibility { it.copy(dateFormat = pending) }
                pendingDateFormat = null
            },
            onDismiss = { pendingDateFormat = null }
        )
    }

    if (showCurrencyDialog) {
        CurrencySelectionDialog(
            current = a11y.currency,
            onSelect = { selected ->
                viewModel.updateAccessibility { it.copy(currency = selected) }
                showCurrencyDialog = false
            },
            onDismiss = { showCurrencyDialog = false }
        )
    }
}

// ------------------------------------------------------------------ componentes visuales

@Composable
private fun LiveAccessibilityPreview(
    contrast: ContrastLevel,
    colorBlindPalette: ColorBlindPalette,
    shapesBesidesColor: Boolean,
    boldText: Boolean,
    readingFont: ReadingFont,
    reduceTransparency: Boolean,
    modifier: Modifier = Modifier
) {
    val sections = LocalSectionColors.current
    val borderColor = when (contrast) {
        ContrastLevel.ESTANDAR -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (reduceTransparency) 1f else 0.4f)
        ContrastLevel.ALTO -> MaterialTheme.colorScheme.outline
        ContrastLevel.MAXIMO -> MaterialTheme.colorScheme.primary
    }
    val borderWidth = when (contrast) {
        ContrastLevel.ESTANDAR -> 1.dp
        ContrastLevel.ALTO -> 1.5.dp
        ContrastLevel.MAXIMO -> 2.dp
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "VISTA PREVIA",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
            )

            // Contenedor interior estilizado
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (reduceTransparency) 0.8f else 0.35f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Materia 1: Al día
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(sections.schedule)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Cálculo Multivariable",
                                    style = MaterialTheme.typography.titleSmallEmphasized
                                )
                                Text(
                                    text = "MAT-201 · Aula 302B",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "4.2",
                                    style = MaterialTheme.typography.titleMediumEmphasized
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = sections.onTrackContainer.copy(alpha = if (reduceTransparency) 1f else 0.5f)
                                ) {
                                    Text(
                                        text = if (shapesBesidesColor) "● Al día" else "Al día",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = sections.onTrack,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Materia 2: En riesgo
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceContainerLow,
                        border = BorderStroke(1.dp, sections.atRisk.copy(alpha = if (reduceTransparency) 0.6f else 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = 4.dp, height = 32.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(sections.atRisk)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Física Mecánica",
                                    style = MaterialTheme.typography.titleSmallEmphasized
                                )
                                Text(
                                    text = "FIS-102 · Laboratorio 3",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(
                                    text = "2.8",
                                    style = MaterialTheme.typography.titleMediumEmphasized
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = sections.atRiskContainer.copy(alpha = if (reduceTransparency) 1f else 0.5f)
                                ) {
                                    Text(
                                        text = if (shapesBesidesColor) "▲ En riesgo" else "En riesgo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = sections.atRisk,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Los tres estados con la paleta puesta, y con su forma.
 */
@Composable
private fun MuestraDePaleta(paleta: ColorBlindPalette) {
    val secciones = LocalSectionColors.current
    val colores = when (paleta) {
        ColorBlindPalette.NINGUNA -> listOf(secciones.onTrack, secciones.atRisk, MaterialTheme.colorScheme.error)
        ColorBlindPalette.DEUTERANOPIA -> listOf(Color(0xFF3A7DE0), Color(0xFFE0A63C), Color(0xFF8C4BD1))
        ColorBlindPalette.TRITANOPIA -> listOf(Color(0xFF12B0A0), Color(0xFFE0567F), Color(0xFF7A2E4C))
    }
    val formas = listOf(CircleShape, CutCornerShape(percent = 50), RoundedCornerShape(4.dp))
    val nombres = listOf("Al día", "En riesgo", "Suspenso")
    Row(
        modifier = Modifier.padding(top = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        nombres.forEachIndexed { indice, nombre ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(formas[indice])
                        .background(colores[indice])
                )
                Text(
                    text = nombre,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun LanguageSelectionDialog(
    current: AppLanguage,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.a11y_language_title),
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLanguage.entries.forEach { idioma ->
                    val (title, subtitle) = when (idioma) {
                        AppLanguage.SYSTEM -> Pair(
                            stringResource(R.string.a11y_lang_system),
                            stringResource(R.string.a11y_lang_system_desc)
                        )
                        AppLanguage.SPANISH -> Pair(
                            stringResource(R.string.a11y_lang_spanish),
                            stringResource(R.string.a11y_lang_spanish_desc)
                        )
                        AppLanguage.ENGLISH -> Pair(
                            stringResource(R.string.a11y_lang_english),
                            stringResource(R.string.a11y_lang_english_desc)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cleanClickable { onSelect(idioma) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = current == idioma,
                            onClick = { onSelect(idioma) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmallEmphasized
                            )
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun DateFormatSelectionDialog(
    current: DateFormatPreference,
    onSelect: (DateFormatPreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.a11y_date_format_title),
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DateFormatPreference.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cleanClickable { onSelect(format) }
                            .padding(vertical = 10.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = current == format,
                            onClick = { onSelect(format) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = format.label,
                                style = MaterialTheme.typography.titleSmallEmphasized
                            )
                            Text(
                                text = stringResource(R.string.a11y_date_preview_sample, format.previewDate, format.pattern),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

@Composable
private fun DateFormatWarningDialog(
    currentFormat: DateFormatPreference,
    newFormat: DateFormatPreference,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Rounded.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.a11y_date_warning_title),
                style = MaterialTheme.typography.titleLargeEmphasized,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = stringResource(R.string.a11y_date_warning_message),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.a11y_date_warning_current),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = currentFormat.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                text = currentFormat.previewDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.a11y_date_warning_new),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = newFormat.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = newFormat.previewDate,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.action_apply_format))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun CurrencySelectionDialog(
    current: CurrencyPreference,
    onSelect: (CurrencyPreference) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.a11y_currency_title),
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                CurrencyPreference.entries.forEach { divisa ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .cleanClickable { onSelect(divisa) }
                            .padding(vertical = 8.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = current == divisa,
                            onClick = { onSelect(divisa) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = divisa.label,
                                style = MaterialTheme.typography.titleSmallEmphasized
                            )
                            Text(
                                text = stringResource(R.string.a11y_currency_preview_sample, divisa.preview, divisa.symbol),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_close))
            }
        }
    )
}

// ------------------------------------------------------------------ extensiones de etiquetas

@Composable
private fun ContrastLevel.etiqueta() = when (this) {
    ContrastLevel.ESTANDAR -> stringResource(R.string.a11y_contrast_standard)
    ContrastLevel.ALTO -> stringResource(R.string.a11y_contrast_high)
    ContrastLevel.MAXIMO -> stringResource(R.string.a11y_contrast_max)
}

@Composable
private fun ColorBlindPalette.etiqueta() = when (this) {
    ColorBlindPalette.NINGUNA -> stringResource(R.string.a11y_colorblind_normal)
    ColorBlindPalette.DEUTERANOPIA -> stringResource(R.string.a11y_colorblind_deuteranopia)
    ColorBlindPalette.TRITANOPIA -> stringResource(R.string.a11y_colorblind_tritanopia)
}

@Composable
private fun ReadingFont.etiqueta() = when (this) {
    ReadingFont.NORMAL -> stringResource(R.string.a11y_colorblind_normal)
    ReadingFont.DISLEXIA -> stringResource(R.string.a11y_reading_font_dyslexia)
}

@Composable
private fun MotionPreference.etiquetaA11y() = when (this) {
    MotionPreference.FULL -> stringResource(R.string.a11y_motion_normal)
    MotionPreference.REDUCED -> stringResource(R.string.a11y_motion_reduced)
    MotionPreference.NONE -> stringResource(R.string.a11y_motion_none)
}
