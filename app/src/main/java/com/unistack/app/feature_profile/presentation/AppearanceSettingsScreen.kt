package com.unistack.app.feature_profile.presentation

import com.unistack.app.core.design.theme.AppShapes
import com.unistack.app.core.design.theme.scrollBottomRoom

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShapeLine
import androidx.compose.material.icons.rounded.SpaceDashboard
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Widgets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.UniStackColors
import com.unistack.app.feature_user.domain.AccentIntensity
import com.unistack.app.feature_user.domain.AccentStyle
import com.unistack.app.feature_user.domain.AcademicIndicatorStyle
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BackgroundStyle
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.CustomThemeBase
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.InitialTab
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.ScreenTransition
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.TextScalePreference
import com.unistack.app.feature_user.domain.TypographyStyle
import com.unistack.app.feature_user.domain.VisualPreference
import com.unistack.app.feature_user.domain.VisualPreset

@Composable
fun AppearanceSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.section)
    ) {
        item {
            SettingsHeader(
                title = "Apariencia",
                subtitle = "Personaliza cómo se ve y se siente UniStack",
                onBackClick = onBackClick
            )
        }
        if (current == null) {
            item {
                UniCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Cargando preferencias...", color = UniStackColors.TextSecondary)
                }
            }
            return@LazyColumn
        }

        val appearance = current.appearancePreferences
        item {
            AppearancePreview(
                name = current.preferredName,
                appearance = appearance
            )
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.AutoAwesome,
                title = "Perfiles visuales"
            ) {
                ChoiceGrid(
                    entries = VisualPreset.entries.filterNot { it == VisualPreset.CUSTOM },
                    selected = appearance.visualPreset,
                    label = VisualPreset::label,
                    onSelected = viewModel::applyVisualPreset
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.DarkMode,
                title = "Tema y fondo"
            ) {
                SectionLabel("Modo")
                ChoiceGrid(
                    entries = VisualPreference.entries,
                    selected = current.visualPreference,
                    label = VisualPreference::label,
                    onSelected = viewModel::updateVisualPreference
                )
                if (current.visualPreference == VisualPreference.CUSTOM) {
                    SectionLabel("Base del tema")
                    ChoiceGrid(
                        entries = CustomThemeBase.entries,
                        selected = appearance.customThemeBase,
                        label = CustomThemeBase::label,
                        columns = 3,
                        onSelected = { base ->
                            viewModel.updateAppearance { it.copy(customThemeBase = base) }
                        }
                    )
                    SectionLabel("Fondo")
                    BackgroundChoices(
                        selected = appearance.backgroundStyle,
                        customColor = appearance.customBackgroundColor,
                        onSelected = { style ->
                            viewModel.updateAppearance { it.copy(backgroundStyle = style) }
                        },
                        onCustomColor = { color ->
                            viewModel.updateAppearance {
                                it.copy(
                                    backgroundStyle = BackgroundStyle.CUSTOM,
                                    customBackgroundColor = color
                                )
                            }
                        }
                    )
                } else {
                    Text(
                        text = current.visualPreference.themeDescription(),
                        color = UniStackColors.TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.ColorLens,
                title = "Colores"
            ) {
                AccentChoices(
                    selected = appearance.accentStyle,
                    customColor = appearance.customAccentColor,
                    onSelected = { style ->
                        viewModel.updateAppearance { it.copy(accentStyle = style) }
                    },
                    onCustomColor = { color ->
                        viewModel.updateAppearance {
                            it.copy(
                                accentStyle = AccentStyle.CUSTOM,
                                customAccentColor = color
                            )
                        }
                    }
                )
                SectionLabel("Intensidad")
                ChoiceGrid(
                    entries = AccentIntensity.entries,
                    selected = appearance.accentIntensity,
                    label = AccentIntensity::label,
                    columns = 3,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(accentIntensity = value) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.Layers,
                title = "Superficies y formas"
            ) {
                SectionLabel("Tarjetas")
                ChoiceGrid(
                    entries = SurfaceStyle.entries,
                    selected = appearance.surfaceStyle,
                    label = SurfaceStyle::label,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(surfaceStyle = value) }
                    }
                )
                SectionLabel("Esquinas")
                ChoiceGrid(
                    entries = CornerStyle.entries,
                    selected = appearance.cornerStyle,
                    label = CornerStyle::label,
                    columns = 3,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(cornerStyle = value) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.SpaceDashboard,
                title = "Tamaño y densidad"
            ) {
                SectionLabel("Densidad")
                ChoiceGrid(
                    entries = InterfaceDensity.entries,
                    selected = appearance.interfaceDensity,
                    label = InterfaceDensity::label,
                    columns = 3,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(interfaceDensity = value) }
                    }
                )
                SectionLabel("Tipografía")
                ChoiceGrid(
                    entries = TypographyStyle.entries,
                    selected = appearance.typographyStyle,
                    label = TypographyStyle::label,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(typographyStyle = value) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.Home,
                title = "Inicio"
            ) {
                PreferenceSwitch(
                    title = "Saludo",
                    subtitle = "Encabezado con nombre y momento del día",
                    checked = appearance.showHomeGreeting,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(showHomeGreeting = enabled) }
                    }
                )
                PreferenceSwitch(
                    title = "Hero inteligente",
                    subtitle = "Prioridad académica principal",
                    checked = appearance.showHomeHero,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(showHomeHero = enabled) }
                    }
                )
                PreferenceSwitch(
                    title = "Agenda",
                    subtitle = "Actividades y vencimientos cercanos",
                    checked = appearance.showHomeAgenda,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(showHomeAgenda = enabled) }
                    }
                )
                PreferenceSwitch(
                    title = "Tablero",
                    subtitle = "Resumen académico y financiero",
                    checked = appearance.showHomeSnapshot,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(showHomeSnapshot = enabled) }
                    }
                )
                SectionLabel("Orden")
                HomeSectionOrderEditor(
                    order = appearance.homeSectionOrder,
                    onMove = { section, direction ->
                        viewModel.updateAppearance {
                            it.copy(homeSectionOrder = it.homeSectionOrder.move(section, direction))
                        }
                    }
                )
                SectionLabel("Información del hero")
                PreferenceSwitch(
                    title = "Notas",
                    checked = appearance.heroShowsGrades,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(heroShowsGrades = enabled) }
                    }
                )
                PreferenceSwitch(
                    title = "Tareas",
                    checked = appearance.heroShowsTasks,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(heroShowsTasks = enabled) }
                    }
                )
                PreferenceSwitch(
                    title = "Gastos",
                    checked = appearance.heroShowsExpenses,
                    onCheckedChange = { enabled ->
                        viewModel.updateAppearance { it.copy(heroShowsExpenses = enabled) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.Dashboard,
                title = "Navegación e indicadores"
            ) {
                SectionLabel("Barra inferior")
                ChoiceGrid(
                    entries = BottomBarStyle.entries,
                    selected = appearance.bottomBarStyle,
                    label = BottomBarStyle::label,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(bottomBarStyle = value) }
                    }
                )
                SectionLabel("Cambio de pantalla")
                ChoiceGrid(
                    entries = ScreenTransition.entries,
                    selected = appearance.screenTransition,
                    label = ScreenTransition::label,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(screenTransition = value) }
                    }
                )
                SectionLabel("Pestaña inicial")
                ChoiceGrid(
                    entries = InitialTab.entries,
                    selected = appearance.initialTab,
                    label = InitialTab::label,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(initialTab = value) }
                    }
                )
                SectionLabel("Progreso académico")
                ChoiceGrid(
                    entries = AcademicIndicatorStyle.entries,
                    selected = appearance.academicIndicatorStyle,
                    label = AcademicIndicatorStyle::label,
                    columns = 3,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(academicIndicatorStyle = value) }
                    }
                )
                SectionLabel("Decimales")
                ChoiceGrid(
                    entries = listOf(0, 1, 2),
                    selected = appearance.decimalPlaces,
                    label = { value -> value.toString() },
                    columns = 3,
                    onSelected = { value ->
                        viewModel.updateAppearance { it.copy(decimalPlaces = value) }
                    }
                )
            }
        }
        item {
            TextButton(
                onClick = viewModel::resetAppearance,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.RestartAlt, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Restablecer apariencia")
            }
        }
    }
}

@Composable
fun AccessibilitySettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val accessibility = current.accessibilityPreferences

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(spacing.section)
    ) {
        item {
            SettingsHeader(
                title = "Accesibilidad",
                subtitle = "Lectura, movimiento y formatos",
                onBackClick = onBackClick
            )
        }
        item {
            // Aquí había un selector de idioma con Sistema / Español / English. La
            // preferencia se guardaba y hasta se sincronizaba, pero nadie la leía nunca:
            // no existen traducciones y la interfaz está solo en español. El propio texto
            // de ayuda lo admitía. Ofrecer un control que no hace nada es una promesa
            // incumplida, y en la pantalla de accesibilidad es donde peor sienta.
            //
            // AppLanguage y su persistencia siguen en pie para no romper los respaldos ya
            // guardados; lo que vuelve es el selector, cuando haya algo que seleccionar.
            AppearanceSection(
                icon = Icons.Rounded.Language,
                title = "Formatos"
            ) {
                PreferenceSwitch(
                    title = "Formato de 24 horas",
                    subtitle = if (accessibility.use24HourTime) "Ejemplo: 18:30" else "Ejemplo: 6:30 p. m.",
                    checked = accessibility.use24HourTime,
                    onCheckedChange = { enabled ->
                        viewModel.updateAccessibility { it.copy(use24HourTime = enabled) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.FormatSize,
                title = "Lectura"
            ) {
                SectionLabel("Tamaño del texto")
                ChoiceGrid(
                    entries = TextScalePreference.entries,
                    selected = accessibility.textScale,
                    label = TextScalePreference::label,
                    onSelected = { value ->
                        viewModel.updateAccessibility { it.copy(textScale = value) }
                    }
                )
                PreferenceSwitch(
                    title = "Contraste reforzado",
                    subtitle = "Texto secundario y bordes más visibles",
                    checked = accessibility.highContrastEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateAccessibility { it.copy(highContrastEnabled = enabled) }
                    }
                )
            }
        }
        item {
            AppearanceSection(
                icon = Icons.Rounded.Animation,
                title = "Movimiento"
            ) {
                ChoiceGrid(
                    entries = MotionPreference.entries,
                    selected = accessibility.motionPreference,
                    label = MotionPreference::label,
                    onSelected = { value ->
                        viewModel.updateAccessibility { it.copy(motionPreference = value) }
                    }
                )
                PreferenceSwitch(
                    title = "Animación del hero",
                    subtitle = "Movimiento ambiental en la tarjeta principal",
                    checked = accessibility.heroAnimationEnabled,
                    onCheckedChange = { enabled ->
                        viewModel.updateAccessibility { it.copy(heroAnimationEnabled = enabled) }
                    }
                )
            }
        }
    }
}

@Composable
private fun HomeSectionOrderEditor(
    order: List<HomeSection>,
    onMove: (HomeSection, Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        order.forEachIndexed { index, section ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(AppShapes.Small)
                    .background(UniStackColors.SurfaceVariant.copy(alpha = 0.58f))
                    .padding(start = 12.dp, end = 4.dp, top = 5.dp, bottom = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    section.label(),
                    modifier = Modifier.weight(1f),
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(
                    onClick = { onMove(section, -1) },
                    enabled = index > 0
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Subir ${section.label()}"
                    )
                }
                IconButton(
                    onClick = { onMove(section, 1) },
                    enabled = index < order.lastIndex
                ) {
                    Icon(
                        Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Bajar ${section.label()}"
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsHubScreen(
    onBackClick: () -> Unit,
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
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
            SettingsDestination(
                icon = Icons.Rounded.Palette,
                title = "Apariencia",
                subtitle = profile?.appearancePreferences?.summary() ?: "Tema y personalización",
                onClick = onAppearanceClick,
                highlighted = true
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Accessibility,
                title = "Accesibilidad",
                subtitle = "Texto, contraste, movimiento y formatos",
                onClick = onAccessibilityClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Person,
                title = "Cuenta y perfil",
                subtitle = "Nombre, cuenta vinculada y sincronización",
                onClick = onProfileClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.School,
                title = "Configuración académica",
                subtitle = "Escala, metas y estructura de cortes",
                onClick = onAcademicClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Widgets,
                title = "Módulos",
                subtitle = "Activa las áreas que quieres usar",
                onClick = onModulesClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Notifications,
                title = "Notificaciones",
                subtitle = "Recordatorios, permisos y horario silencioso",
                onClick = onNotificationsClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Backup,
                title = "Datos y respaldos",
                subtitle = "Exportar, restaurar y repetir configuración inicial",
                onClick = onDataClick
            )
        }
        item {
            SettingsDestination(
                icon = Icons.Rounded.Refresh,
                title = "Actualizaciones",
                subtitle = "Verifica y descarga la última versión",
                onClick = onUpdatesClick
            )
        }
    }
}

@Composable
private fun SettingsHeader(
    title: String,
    subtitle: String,
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Volver",
                tint = UniStackColors.TextPrimary
            )
        }
        Spacer(Modifier.width(4.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = UniStackColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = UniStackColors.TextSecondary
            )
        }
    }
}

@Composable
private fun AppearancePreview(
    name: String,
    appearance: AppearancePreferences
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card,
        borderColor = UniStackColors.Primary.copy(alpha = 0.24f),
        borderWidth = 1.dp
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(UniStackColors.Primary.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Palette,
                        contentDescription = null,
                        tint = UniStackColors.Primary
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Hola, ${name.ifBlank { "estudiante" }}",
                        style = MaterialTheme.typography.titleMedium,
                        color = UniStackColors.TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        appearance.summary(),
                        style = MaterialTheme.typography.bodySmall,
                        color = UniStackColors.TextSecondary
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(if (index == 0) 46.dp else 38.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .background(
                                if (index == 0) {
                                    UniStackColors.Primary.copy(alpha = 0.18f)
                                } else {
                                    UniStackColors.SurfaceVariant
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun AppearanceSection(
    icon: ImageVector,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = UniStackColors.Card
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(AppShapes.Small)
                        .background(UniStackColors.Primary.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = UniStackColors.Primary)
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    color = UniStackColors.TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}

@Composable
private fun <T> ChoiceGrid(
    entries: List<T>,
    selected: T,
    label: (T) -> String,
    columns: Int = 2,
    onSelected: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        entries.chunked(columns).forEach { rowEntries ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowEntries.forEach { entry ->
                    val isSelected = entry == selected
                    Surface(
                        onClick = { onSelected(entry) },
                        modifier = Modifier.weight(1f),
                        shape = AppShapes.Small,
                        color = if (isSelected) {
                            UniStackColors.Primary.copy(alpha = 0.14f)
                        } else {
                            UniStackColors.SurfaceVariant.copy(alpha = 0.62f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) UniStackColors.Primary else UniStackColors.SoftOutline.copy(alpha = 0.45f)
                        )
                    ) {
                        Text(
                            label(entry),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 11.dp),
                            color = if (isSelected) UniStackColors.Primary else UniStackColors.TextPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                repeat(columns - rowEntries.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun BackgroundChoices(
    selected: BackgroundStyle,
    customColor: Int?,
    onSelected: (BackgroundStyle) -> Unit,
    onCustomColor: (Int) -> Unit
) {
    // design-tokens-ok-begin: muestras del selector; enseñan el color literal que se va a
    // aplicar, así que no pueden derivar del tema actual sin dejar de representar la opción.
    val choices = listOf(
        BackgroundStyle.DEFAULT to UniStackColors.Background,
        BackgroundStyle.PURE to if (UniStackColors.IsDarkTheme) Color.Black else Color.White,
        BackgroundStyle.COOL to if (UniStackColors.IsDarkTheme) Color(0xFF050A13) else Color(0xFFF5F7FC),
        BackgroundStyle.VIOLET to if (UniStackColors.IsDarkTheme) Color(0xFF0D0818) else Color(0xFFFAF7FF)
    )
    // design-tokens-ok-end
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(choices) { (style, color) ->
            ColorChoice(
                color = color,
                selected = selected == style,
                label = style.label(),
                onClick = { onSelected(style) }
            )
        }
    }
    HexColorField(
        label = "Fondo personalizado",
        color = customColor ?: UniStackColors.Background.toArgb(),
        onColorChanged = onCustomColor
    )
}

@Composable
private fun AccentChoices(
    selected: AccentStyle,
    customColor: Int?,
    onSelected: (AccentStyle) -> Unit,
    onCustomColor: (Int) -> Unit
) {
    val choices = buildList {
        // Material You solo existe desde Android 12; en versiones previas no ofrecemos la opción.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            add(AccentStyle.DYNAMIC to UniStackColors.Primary)
        }
        // design-tokens-ok-begin: muestras de acento; cada una debe verse con su color real
        add(AccentStyle.VIOLET to Color(0xFF6750F5))
        add(AccentStyle.BLUE to Color(0xFF1E7BEA))
        add(AccentStyle.TEAL to Color(0xFF00AFA5))
        add(AccentStyle.GREEN to Color(0xFF3DBB68))
        add(AccentStyle.PINK to Color(0xFFD83D87))
        // design-tokens-ok-end
    }
    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(choices) { (style, color) ->
            ColorChoice(
                color = color,
                selected = selected == style,
                label = style.label(),
                onClick = { onSelected(style) }
            )
        }
    }
    HexColorField(
        label = "Acento personalizado",
        color = customColor ?: UniStackColors.Primary.toArgb(),
        onColorChanged = onCustomColor
    )
}

@Composable
private fun ColorChoice(
    color: Color,
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(66.dp)
            .clip(AppShapes.SmallCard)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(color)
                .then(
                    if (selected) {
                        // design-tokens-ok: velo de selección sobre la muestra de color
                        Modifier.background(Color.Black.copy(alpha = 0.08f))
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    tint = UniStackColors.contentColorOn(color)
                )
            }
        }
        Text(
            label,
            color = if (selected) UniStackColors.Primary else UniStackColors.TextSecondary,
            fontSize = 10.sp,
            maxLines = 1
        )
    }
}

@Composable
private fun HexColorField(
    label: String,
    color: Int,
    onColorChanged: (Int) -> Unit
) {
    var value by remember(color) { mutableStateOf(color.toHexString()) }
    OutlinedTextField(
        value = value,
        onValueChange = { input ->
            value = input.take(7)
            input.toColorIntOrNull()?.let(onColorChanged)
        },
        label = { Text(label) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(Color(color))
            )
        },
        singleLine = true,
        shape = AppShapes.Small,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PreferenceSwitch(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShapes.SmallCard)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = UniStackColors.TextPrimary,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        color = UniStackColors.TextSecondary,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsDestination(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    highlighted: Boolean = false
) {
    UniCard(
        modifier = Modifier.fillMaxWidth(),
        color = if (highlighted) {
            UniStackColors.Primary.copy(alpha = 0.08f)
        } else {
            UniStackColors.Card
        },
        borderColor = if (highlighted) UniStackColors.Primary.copy(alpha = 0.28f) else Color.Transparent,
        borderWidth = if (highlighted) 1.dp else 0.dp,
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(AppShapes.Small)
                    .background(UniStackColors.Primary.copy(alpha = 0.13f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = UniStackColors.Primary)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    color = UniStackColors.TextPrimary,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    color = UniStackColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

private fun AppearancePreferences.summary(): String =
    "${accentStyle.label()} · ${interfaceDensity.label()} · ${surfaceStyle.label()}"

private fun VisualPreference.label() = when (this) {
    VisualPreference.SYSTEM -> "Sistema"
    VisualPreference.LIGHT -> "Claro"
    VisualPreference.DARK -> "Oscuro"
    VisualPreference.OLED -> "OLED"
    VisualPreference.CUSTOM -> "Personalizado"
}

private fun VisualPreference.themeDescription() = when (this) {
    VisualPreference.SYSTEM -> "Sigue el tema del dispositivo y utiliza el fondo original."
    VisualPreference.LIGHT -> "Usa la apariencia clara con el fondo original."
    VisualPreference.DARK -> "Usa la apariencia oscura con el fondo original."
    VisualPreference.OLED -> "Usa negro puro para aprovechar pantallas OLED."
    VisualPreference.CUSTOM -> ""
}

private fun CustomThemeBase.label() = when (this) {
    CustomThemeBase.SYSTEM -> "Sistema"
    CustomThemeBase.LIGHT -> "Clara"
    CustomThemeBase.DARK -> "Oscura"
}

private fun VisualPreset.label() = when (this) {
    VisualPreset.DEFAULT -> "UniStack"
    VisualPreset.MINIMAL -> "Minimalista"
    VisualPreset.OLED -> "OLED"
    VisualPreset.FOCUS -> "Enfoque"
    VisualPreset.CUSTOM -> "Personal"
}

private fun BackgroundStyle.label() = when (this) {
    BackgroundStyle.DEFAULT -> "Original"
    BackgroundStyle.PURE -> "Puro"
    BackgroundStyle.COOL -> "Frío"
    BackgroundStyle.VIOLET -> "Lavanda"
    BackgroundStyle.CUSTOM -> "Personal"
}

private fun AccentStyle.label() = when (this) {
    AccentStyle.DYNAMIC -> "Del sistema"
    AccentStyle.VIOLET -> "Violeta"
    AccentStyle.BLUE -> "Azul"
    AccentStyle.TEAL -> "Turquesa"
    AccentStyle.GREEN -> "Verde"
    AccentStyle.PINK -> "Rosa"
    AccentStyle.CUSTOM -> "Personal"
}

private fun AccentIntensity.label() = when (this) {
    AccentIntensity.SOFT -> "Suave"
    AccentIntensity.BALANCED -> "Equilibrada"
    AccentIntensity.VIBRANT -> "Vibrante"
}

private fun SurfaceStyle.label() = when (this) {
    SurfaceStyle.FLAT -> "Planas"
    SurfaceStyle.OUTLINED -> "Bordes"
    SurfaceStyle.ELEVATED -> "Elevadas"
    SurfaceStyle.TRANSLUCENT -> "Suaves"
}

private fun CornerStyle.label() = when (this) {
    CornerStyle.COMPACT -> "Compactas"
    CornerStyle.BALANCED -> "Medias"
    CornerStyle.SOFT -> "Suaves"
}

private fun InterfaceDensity.label() = when (this) {
    InterfaceDensity.COMPACT -> "Compacta"
    InterfaceDensity.BALANCED -> "Equilibrada"
    InterfaceDensity.COMFORTABLE -> "Cómoda"
}

private fun MotionPreference.label() = when (this) {
    MotionPreference.FULL -> "Completo"
    MotionPreference.REDUCED -> "Reducido"
    MotionPreference.NONE -> "Sin movimiento"
}

private fun TextScalePreference.label() = when (this) {
    TextScalePreference.STANDARD -> "Estándar"
    TextScalePreference.LARGE -> "Grande"
}

private fun TypographyStyle.label() = when (this) {
    TypographyStyle.UNISTACK -> "UniStack"
    TypographyStyle.SYSTEM -> "Sistema"
}

private fun BottomBarStyle.label() = when (this) {
    BottomBarStyle.LABELED -> "Iconos y texto"
    BottomBarStyle.ICONS_ONLY -> "Solo iconos"
}

private fun ScreenTransition.label() = when (this) {
    ScreenTransition.PUSH -> "Empuje"
    ScreenTransition.FADE -> "Fundido"
    ScreenTransition.NONE -> "Sin animación"
}


private fun AcademicIndicatorStyle.label() = when (this) {
    AcademicIndicatorStyle.RINGS -> "Anillos"
    AcademicIndicatorStyle.BARS -> "Barras"
    AcademicIndicatorStyle.NUMBERS -> "Cifras"
}

private fun HomeSection.label() = when (this) {
    HomeSection.HERO -> "Hero inteligente"
    HomeSection.AGENDA -> "Agenda"
    HomeSection.SNAPSHOT -> "Tablero"
}

private fun InitialTab.label() = when (this) {
    InitialTab.HOME -> "Inicio"
    InitialTab.GRADES -> "Materias"
    InitialTab.TASKS -> "Tareas"
    InitialTab.EXPENSES -> "Gastos"
}

private fun List<HomeSection>.move(section: HomeSection, direction: Int): List<HomeSection> {
    val source = indexOf(section)
    if (source == -1) return this
    val target = (source + direction).coerceIn(indices)
    if (source == target) return this
    return toMutableList().apply {
        add(target, removeAt(source))
    }
}

private fun Int.toHexString(): String = String.format("#%06X", this and 0xFFFFFF)

private fun String.toColorIntOrNull(): Int? {
    val normalized = trim().removePrefix("#")
    if (normalized.length != 6 || normalized.any { it !in "0123456789abcdefABCDEF" }) return null
    return runCatching {
        (0xFF000000L or normalized.toLong(16)).toInt()
    }.getOrNull()
}

private fun Color.luminanceValue(): Float =
    0.299f * red + 0.587f * green + 0.114f * blue
