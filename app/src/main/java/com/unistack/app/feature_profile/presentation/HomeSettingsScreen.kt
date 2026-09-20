@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.R
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsChoiceRow
import com.unistack.app.core.design.components.SettingsGroup
import com.unistack.app.core.design.components.SettingsToggleRow
import com.unistack.app.core.design.components.rememberUniReorderState
import com.unistack.app.core.design.components.uniReorderHandle
import com.unistack.app.core.design.components.uniReorderableItem
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.feature_user.domain.AppModule
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.InitialTab

/**
 * Qué bloques salen en Inicio, en qué orden, y dónde arranca la app.
 *
 * Rehecha el 20 sep 2026 con lo que quedó del recorte de Apariencia. Arriba, una maqueta de
 * Inicio con una línea por bloque encendido; el saludo con sus dos interruptores; los ocho
 * bloques como lista segmentada **con asa** —el asa que aquí no funcionaba se arregló en
 * [uniReorderHandle], y las flechas que la sustituyeron se van—; y «Al abrir la app», que se
 * guardaba y se usaba sin ningún mando. Lo que contaba «Lo siguiente» ya no se elige: la
 * tarjeta mira siempre notas, entregas y gastos, y enseña lo más pronto.
 */
@Composable
fun HomeSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val appearance = current.appearancePreferences
    val orden = appearance.homeSectionOrder
    val encendidos = orden.count(appearance::showsSection)
    val modulos = current.enabledModules

    LargeTitleScaffold(
        title = stringResource(R.string.settings_home_title),
        subtitle = stringResource(R.string.settings_home_subtitle),
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item {
            MaquetaDeInicio(
                nombre = current.preferredName.takeIf { it.isNotBlank() } ?: stringResource(R.string.settings_profile_student),
                appearance = appearance
            )
        }

        item {
            SettingsGroup(label = stringResource(R.string.settings_home_greeting_sec), rowCount = 2) {
                SettingsToggleRow(
                    title = stringResource(R.string.settings_home_greeting_title),
                    subtitle = stringResource(R.string.settings_home_greeting_desc),
                    checked = appearance.showHomeGreeting,
                    onCheckedChange = { valor -> viewModel.updateAppearance { it.copy(showHomeGreeting = valor) } }
                )
                SettingsToggleRow(
                    title = stringResource(R.string.settings_home_greeting_time_title),
                    subtitle = stringResource(R.string.settings_home_greeting_time_desc),
                    checked = appearance.greetingWithTimeOfDay,
                    onCheckedChange = { valor -> viewModel.updateAppearance { it.copy(greetingWithTimeOfDay = valor) } }
                )
            }
        }

        item {
            ListaDeBloques(
                orden = orden,
                encendidos = encendidos,
                appearance = appearance,
                onCambio = { seccion, valor -> viewModel.updateAppearance { it.withSection(seccion, valor) } },
                onOrden = { nuevo -> viewModel.updateAppearance { it.copy(homeSectionOrder = nuevo) } }
            )
        }

        item {
            /*
             * Cuatro pestañas y no las cinco de la barra: Ajustes no es un sitio en el que
             * arrancar. Académico abre en Materias; lo guardado como Tareas antes del 20 sep
             * sigue abriendo Tareas y aquí se ve como Académico.
             */
            val academico = appearance.initialTab == InitialTab.GRADES || appearance.initialTab == InitialTab.TASKS
            val pestanas = buildList {
                add(Triple(InitialTab.HOME, R.string.settings_home_tab_home, appearance.initialTab == InitialTab.HOME))
                if (AppModule.GRADES in modulos || AppModule.TASKS in modulos) {
                    add(Triple(InitialTab.GRADES, R.string.settings_home_tab_academic, academico))
                }
                add(Triple(InitialTab.SCHEDULE, R.string.settings_home_tab_schedule, appearance.initialTab == InitialTab.SCHEDULE))
                if (AppModule.EXPENSES in modulos) {
                    add(Triple(InitialTab.EXPENSES, R.string.settings_home_tab_expenses, appearance.initialTab == InitialTab.EXPENSES))
                }
            }
            SettingsGroup(
                label = stringResource(R.string.settings_home_initial_tab_sec),
                labelColor = MaterialTheme.colorScheme.primary,
                rowCount = pestanas.size,
                explanation = stringResource(R.string.settings_home_initial_tab_desc)
            ) {
                pestanas.forEach { (pestana, rotulo, elegida) ->
                    SettingsChoiceRow(
                        title = stringResource(rotulo),
                        selected = elegida,
                        onClick = {
                            if (!elegida) viewModel.updateAppearance { it.copy(initialTab = pestana) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Los ocho bloques, en orden, cada uno con su asa, su interruptor y su línea.
 *
 * El orden mientras se arrastra vive aquí: cada permuta mueve la lista local para que la fila
 * siga al dedo sin esperar al disco, y al soltar se guarda de una vez.
 */
@Composable
private fun ListaDeBloques(
    orden: List<HomeSection>,
    encendidos: Int,
    appearance: AppearancePreferences,
    onCambio: (HomeSection, Boolean) -> Unit,
    onOrden: (List<HomeSection>) -> Unit
) {
    val reorder = rememberUniReorderState()
    var enPantalla by remember(orden) { mutableStateOf(orden) }

    SettingsGroup(
        label = stringResource(R.string.settings_home_blocks_order) + " · " +
            stringResource(R.string.settings_home_blocks_count, encendidos, orden.size),
        labelColor = MaterialTheme.colorScheme.primary,
        rowCount = enPantalla.size,
        explanation = stringResource(R.string.settings_home_blocks_drag)
    ) {
        enPantalla.forEachIndexed { indice, seccion ->
            SettingsToggleRow(
                title = seccion.label(),
                subtitle = seccion.detail(),
                checked = appearance.showsSection(seccion),
                onCheckedChange = { valor -> onCambio(seccion, valor) },
                modifier = Modifier.uniReorderableItem(reorder, seccion),
                leadingContent = {
                    Icon(
                        imageVector = Icons.Rounded.DragIndicator,
                        contentDescription = stringResource(R.string.settings_home_move, seccion.label()),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier
                            .size(24.dp)
                            .uniReorderHandle(
                                state = reorder,
                                key = seccion,
                                index = { enPantalla.indexOf(seccion) },
                                itemCount = { enPantalla.size },
                                onMove = { desde, hasta ->
                                    enPantalla = enPantalla.toMutableList().apply { add(hasta, removeAt(desde)) }
                                },
                                onSettle = { if (enPantalla != orden) onOrden(enPantalla) }
                            )
                    )
                }
            )
        }
    }
}

/**
 * Inicio en pequeño: el saludo y una línea por bloque encendido, en el orden elegido.
 *
 * Una línea y no una maqueta de cada tarjeta: lo que aquí se decide es qué sale y en qué
 * orden, y para eso basta con ver la lista. «Lo siguiente» va en el color del hero, que es
 * como se distingue en Inicio.
 */
@Composable
private fun MaquetaDeInicio(nombre: String, appearance: AppearancePreferences) {
    val esquema = MaterialTheme.colorScheme
    val visibles = appearance.homeSectionOrder.filter(appearance::showsSection)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = esquema.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (appearance.showHomeGreeting) {
                Column {
                    Text(
                        text = (if (appearance.greetingWithTimeOfDay) greetingForNow() else stringResource(R.string.home_header_greeting_default)).uppercase(),
                        style = SectionLabelStyle,
                        color = esquema.primary
                    )
                    Text(
                        text = nombre,
                        style = MaterialTheme.typography.titleLargeEmphasized,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            visibles.forEach { seccion ->
                val hero = seccion == HomeSection.HERO
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = if (hero) esquema.primaryContainer else esquema.surfaceContainerHigh,
                    contentColor = if (hero) esquema.onPrimaryContainer else esquema.onSurfaceVariant
                ) {
                    Text(
                        text = seccion.muestra(),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            }
            if (!appearance.showHomeGreeting && visibles.isEmpty()) {
                Text(
                    text = stringResource(R.string.settings_home_preview_empty),
                    style = MaterialTheme.typography.bodySmall,
                    color = esquema.onSurfaceVariant
                )
            }
        }
    }
}

/** La línea de muestra de cada bloque en la maqueta. */
@Composable
private fun HomeSection.muestra(): String = stringResource(
    when (this) {
        HomeSection.HERO -> R.string.settings_home_sample_hero
        HomeSection.AGENDA -> R.string.settings_home_sample_agenda
        HomeSection.SNAPSHOT -> R.string.settings_home_sample_snapshot
        HomeSection.SUBJECTS -> R.string.settings_home_sample_subjects
        HomeSection.WEEK -> R.string.settings_home_sample_week
        HomeSection.ATTENDANCE -> R.string.settings_home_sample_attendance
        HomeSection.EXPENSES -> R.string.settings_home_sample_expenses
        HomeSection.NOTES -> R.string.settings_home_sample_notes
    }
)
