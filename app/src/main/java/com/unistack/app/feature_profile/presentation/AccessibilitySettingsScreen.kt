@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Contrast
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.LayersClear
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.PanTool
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RecordVoiceOver
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.StayCurrentPortrait
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.EvaluationBar
import com.unistack.app.core.design.components.SettingsCustomRow
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsRow
import com.unistack.app.core.design.components.SettingsToggleRow
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.duracionDeDeshacer
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.ColorBlindPalette
import com.unistack.app.feature_user.domain.ContrastLevel
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.ReadingFont
import com.unistack.app.feature_user.domain.TouchTargetSize
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = spacing.screenHorizontal,
                end = spacing.screenHorizontal,
                top = 8.dp,
                bottom = scrollBottomRoom
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SettingsHeader(
                    title = "Accesibilidad",
                    subtitle = "Visión, interacción, asistencia y sistema",
                    onBackClick = onBackClick
                )
            }

            // ------------------------------------------------------------------ VISTA PREVIA
            item {
                LiveAccessibilityPreview(
                    contrast = a11y.contrast,
                    colorBlindPalette = a11y.colorBlindPalette,
                    shapesBesidesColor = a11y.shapesBesidesColor,
                    boldText = a11y.boldText,
                    readingFont = a11y.readingFont
                )
            }

            // ------------------------------------------------------------------ VISIÓN Y LECTURA
            item {
                SettingsGroup(label = "VISIÓN Y LECTURA", rowCount = 6) {
                    SettingsCustomRow(
                        icon = Icons.Rounded.Contrast,
                        title = "Contraste",
                        subtitle = "Separación entre el texto y su fondo en toda la app",
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
                        title = "Paleta para daltonismo",
                        subtitle = "Sustituye la combinación verde-rojo por tonos distinguibles",
                        iconColor = sections.onTrack
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            UniSegmentedControl(
                                selected = a11y.colorBlindPalette,
                                options = ColorBlindPalette.entries.map {
                                    UniSegmentedOption(value = it, label = it.etiqueta())
                                },
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
                        title = "Formas además del color",
                        subtitle = "● Círculo, ▲ triángulo y ■ cuadrado junto al estado de cada nota",
                        checked = a11y.shapesBesidesColor,
                        iconColor = sections.onTrack,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(shapesBesidesColor = valor) }
                        }
                    )

                    SettingsCustomRow(
                        icon = Icons.Rounded.Spellcheck,
                        title = "Tipografía de lectura",
                        subtitle = "Letra más abierta para facilitar la lectura continua",
                        iconColor = sections.schedule
                    ) {
                        UniSegmentedControl(
                            selected = a11y.readingFont,
                            options = ReadingFont.entries.map {
                                UniSegmentedOption(value = it, label = it.etiqueta())
                            },
                            onSelected = { valor ->
                                viewModel.updateAccessibility { it.copy(readingFont = valor) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsToggleRow(
                        icon = Icons.Rounded.FormatBold,
                        title = "Texto en negrita",
                        subtitle = "Aumenta el grosor y peso de todas las letras",
                        checked = a11y.boldText,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(boldText = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.LayersClear,
                        title = "Reducir transparencias",
                        subtitle = "Quita efectos de cristal y fondos translúcidos",
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
                SettingsGroup(label = "INTERACCIÓN Y MOVIMIENTO", rowCount = 4) {
                    SettingsCustomRow(
                        icon = Icons.Rounded.Animation,
                        title = "Movimiento",
                        subtitle = "Manda sobre Apariencia: si eliges «Nada», apaga toda animación",
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
                        icon = Icons.Rounded.TouchApp,
                        title = "Tamaño de los toques",
                        subtitle = "Área táctil mínima (48dp, 56dp o 64dp) para botones e iconos",
                        iconColor = sections.expenses
                    ) {
                        UniSegmentedControl(
                            selected = a11y.touchTargetSize,
                            options = TouchTargetSize.entries.map {
                                UniSegmentedOption(value = it, label = it.etiqueta())
                            },
                            onSelected = { valor ->
                                viewModel.updateAccessibility { it.copy(touchTargetSize = valor) }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    SettingsCustomRow(
                        icon = Icons.Rounded.History,
                        title = "Tiempo para deshacer",
                        subtitle = "Cuánto dura el aviso en pantalla tras borrar un elemento",
                        iconColor = sections.schedule,
                        trailingAction = {
                            val duration = duracionDeDeshacer()
                            val seconds = a11y.undoDuration.segundos
                            TextButton(
                                onClick = {
                                    testSnackbarJob?.cancel()
                                    testSnackbarJob = scope.launch {
                                        snackbarHostState.showSnackbar(
                                            message = "Aviso de prueba ($seconds s)",
                                            actionLabel = "Deshacer",
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
                                Text("Probar", style = MaterialTheme.typography.labelMedium)
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

                    SettingsToggleRow(
                        icon = Icons.Rounded.PanTool,
                        title = "Modo de una mano",
                        subtitle = "Baja las cabeceras para alcanzarlas cómodamente con el pulgar",
                        checked = a11y.oneHandedMode,
                        iconColor = MaterialTheme.colorScheme.tertiary,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(oneHandedMode = valor) }
                        }
                    )
                }
            }

            // ------------------------------------------------------------------ ASISTENCIA Y SISTEMA
            item {
                SettingsGroup(label = "ASISTENCIA Y SISTEMA", rowCount = 4) {
                    SettingsToggleRow(
                        icon = Icons.Rounded.RecordVoiceOver,
                        title = "Descripciones habladas",
                        subtitle = "TalkBack lee «tres coma cuatro sobre cinco» en vez de «3,4/5»",
                        checked = a11y.spokenDescriptions,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(spokenDescriptions = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.Warning,
                        title = "Confirmar acciones críticas",
                        subtitle = "Preguntar antes de borrar materias o cerrar periodos",
                        checked = a11y.confirmIrreversible,
                        iconColor = MaterialTheme.colorScheme.error,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(confirmIrreversible = valor) }
                        }
                    )

                    SettingsToggleRow(
                        icon = Icons.Rounded.StayCurrentPortrait,
                        title = "Mantener pantalla encendida",
                        subtitle = "Evita que el teléfono se suspenda mientras estudias con una nota abierta",
                        checked = a11y.keepScreenOn,
                        iconColor = sections.schedule,
                        onCheckedChange = { valor ->
                            viewModel.updateAccessibility { it.copy(keepScreenOn = valor) }
                        }
                    )

                    SettingsRow(
                        icon = Icons.Rounded.Translate,
                        title = "Idioma de la aplicación",
                        subtitle = "${a11y.appLanguage.etiqueta()} · ${a11y.appLanguage.estado()}",
                        iconColor = MaterialTheme.colorScheme.secondary,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
        }
    }

    if (showLanguageDialog) {
        LanguageSelectionDialog(
            current = a11y.appLanguage,
            onSelect = { selected ->
                viewModel.updateAccessibility { it.copy(appLanguage = selected) }
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
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
    modifier: Modifier = Modifier
) {
    val sections = LocalSectionColors.current
    val borderColor = when (contrast) {
        ContrastLevel.ESTANDAR -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
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
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(borderWidth, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "VISTA PREVIA EN VIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${contrast.etiqueta()} · ${colorBlindPalette.etiqueta()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Materia 1: Al día
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 36.dp)
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
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "4.2",
                            style = MaterialTheme.typography.titleMediumEmphasized
                        )
                        Text(
                            text = if (shapesBesidesColor) "● Al día" else "Al día",
                            style = MaterialTheme.typography.labelSmall,
                            color = sections.onTrack,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Materia 2: En riesgo
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = sections.atRiskContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 4.dp, height = 36.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(sections.atRisk)
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Física Mecánica",
                            style = MaterialTheme.typography.titleSmallEmphasized,
                            color = sections.onAtRiskContainer
                        )
                        Text(
                            text = "FIS-102 · Laboratorio 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = sections.onAtRiskContainer.copy(alpha = 0.8f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "2.8",
                            style = MaterialTheme.typography.titleMediumEmphasized,
                            color = sections.onAtRiskContainer
                        )
                        Text(
                            text = if (shapesBesidesColor) "▲ En riesgo" else "En riesgo",
                            style = MaterialTheme.typography.labelSmall,
                            color = sections.atRisk,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = "Esta vista previa adapta sus colores, contrastes, formas y tipografías en tiempo real.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
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
                text = "Idioma de la aplicación",
                style = MaterialTheme.typography.titleLargeEmphasized
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AppLanguage.entries.forEach { idioma ->
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
                                text = idioma.etiqueta(),
                                style = MaterialTheme.typography.titleSmallEmphasized
                            )
                            Text(
                                text = idioma.estado(),
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
                Text("Cerrar")
            }
        }
    )
}

// ------------------------------------------------------------------ extensiones de etiquetas

private fun AppLanguage.etiqueta() = when (this) {
    AppLanguage.SYSTEM -> "Seguir al sistema"
    AppLanguage.SPANISH -> "Español"
    AppLanguage.ENGLISH -> "English"
}

private fun AppLanguage.estado() = when (this) {
    AppLanguage.SYSTEM -> "El del teléfono, si está disponible"
    AppLanguage.SPANISH -> "Traducción completa"
    AppLanguage.ENGLISH -> "Traducción en curso"
}

private fun ContrastLevel.etiqueta() = when (this) {
    ContrastLevel.ESTANDAR -> "Estándar"
    ContrastLevel.ALTO -> "Alto"
    ContrastLevel.MAXIMO -> "Máximo"
}

private fun ColorBlindPalette.etiqueta() = when (this) {
    ColorBlindPalette.NINGUNA -> "Normal"
    ColorBlindPalette.DEUTERANOPIA -> "Deuteranopía"
    ColorBlindPalette.TRITANOPIA -> "Tritanopía"
}

private fun ReadingFont.etiqueta() = when (this) {
    ReadingFont.NORMAL -> "Normal"
    ReadingFont.DISLEXIA -> "Dislexia"
}

private fun TouchTargetSize.etiqueta() = when (this) {
    TouchTargetSize.ESTANDAR -> "Estándar"
    TouchTargetSize.GRANDE -> "Grande"
    TouchTargetSize.MAXIMO -> "Máximo"
}

private fun MotionPreference.etiquetaA11y() = when (this) {
    MotionPreference.FULL -> "Completo"
    MotionPreference.REDUCED -> "Reducido"
    MotionPreference.NONE -> "Nada"
}
