@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.unistack.app.core.design.theme.tonosDeAjustes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import com.unistack.app.feature_home.domain.HomeSummary
import com.unistack.app.feature_home.presentation.AccionesDeInicio
import com.unistack.app.feature_home.presentation.HomeViewModel
import com.unistack.app.feature_home.presentation.bloquesDeInicio
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
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
    // Los datos de Inicio, los de verdad: la maqueta pinta las mismas tarjetas que Inicio.
    val homeViewModel: HomeViewModel = hiltViewModel()
    val inicio by homeViewModel.uiState.collectAsStateWithLifecycle()
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
                appearance = appearance,
                summary = inicio.summary
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
            // Con el icono y el color de cada pestaña, los de la barra de abajo.
            val tonos = tonosDeAjustes
            val pestanas = buildList {
                add(Pestana(InitialTab.HOME, R.string.settings_home_tab_home, appearance.initialTab == InitialTab.HOME, Icons.Rounded.Home, tonos.azul))
                if (AppModule.GRADES in modulos || AppModule.TASKS in modulos) {
                    add(Pestana(InitialTab.GRADES, R.string.settings_home_tab_academic, academico, Icons.AutoMirrored.Rounded.MenuBook, tonos.indigo))
                }
                add(Pestana(InitialTab.SCHEDULE, R.string.settings_home_tab_schedule, appearance.initialTab == InitialTab.SCHEDULE, Icons.Rounded.CalendarMonth, tonos.turquesa))
                if (AppModule.EXPENSES in modulos) {
                    add(Pestana(InitialTab.EXPENSES, R.string.settings_home_tab_expenses, appearance.initialTab == InitialTab.EXPENSES, Icons.Rounded.AccountBalanceWallet, tonos.rojo))
                }
            }
            SettingsGroup(
                label = stringResource(R.string.settings_home_initial_tab_sec),
                labelColor = MaterialTheme.colorScheme.primary,
                rowCount = pestanas.size,
                explanation = stringResource(R.string.settings_home_initial_tab_desc)
            ) {
                pestanas.forEach { pestana ->
                    SettingsChoiceRow(
                        title = stringResource(pestana.rotulo),
                        selected = pestana.elegida,
                        icon = pestana.icono,
                        iconColor = pestana.color,
                        onClick = {
                            if (!pestana.elegida) viewModel.updateAppearance { it.copy(initialTab = pestana.valor) }
                        }
                    )
                }
            }
        }
    }
}

private class Pestana(
    val valor: InitialTab,
    val rotulo: Int,
    val elegida: Boolean,
    val icono: ImageVector,
    val color: Color
)

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
        enPantalla.forEach { seccion ->
            // `key` es lo que hace que el asa siga a su bloque cuando la lista se permuta:
            // sin ella, la fila de arriba pasaba a ser otro bloque, el detector del asa se
            // reiniciaba con la clave nueva y el arrastre se quedaba pegado tras un salto.
            key(seccion) {
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
                                .sinToqueCorto()
                        )
                    }
                )
            }
        }
    }
}

/**
 * El asa se arrastra; un toque corto sobre ella no enciende ni apaga la fila.
 *
 * Va el último de la cadena, que es el primero en ver el dedo: se traga la bajada y la fila,
 * que la necesita sin tocar para contarla como toque, no hace nada. El arrastre tras mantener
 * pulsado no la necesita y sigue funcionando.
 */
private fun Modifier.sinToqueCorto(): Modifier = pointerInput(Unit) {
    awaitEachGesture {
        awaitFirstDown(requireUnconsumed = false).consume()
    }
}

/**
 * Inicio a escala, con las tarjetas de verdad y tus datos de verdad.
 *
 * Una línea por bloque no bastaba («ese preview no es muy accurate», 20 sep 2026). Aquí se
 * pintan los mismos bloques que Inicio, con [bloquesDeInicio] y el mismo resumen, a un 60 %
 * de densidad: todo —letra, aire, tarjetas— encoge junto, sin escalar un bitmap. Una capa
 * encima se traga los toques, que esto es para mirar.
 */
@Composable
private fun MaquetaDeInicio(nombre: String, appearance: AppearancePreferences, summary: HomeSummary) {
    val esquema = MaterialTheme.colorScheme
    val densidad = LocalDensity.current
    val visibles = appearance.homeSectionOrder.filter(appearance::showsSection)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(esquema.background)
            .border(1.dp, esquema.outlineVariant, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(esquema.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(esquema.error))
            Text(stringResource(R.string.settings_home_preview_window).uppercase(), style = SectionLabelStyle, color = esquema.onSurfaceVariant)
        }
        Box(modifier = Modifier.fillMaxWidth().heightIn(max = 440.dp).clipToBounds()) {
            CompositionLocalProvider(
                LocalDensity provides Density(densidad.density * ESCALA_DE_LA_MAQUETA, densidad.fontScale)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    if (appearance.showHomeGreeting) {
                        Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 14.dp, bottom = 10.dp)) {
                            Text(
                                text = (if (appearance.greetingWithTimeOfDay) greetingForNow() else stringResource(R.string.home_header_greeting_default)).uppercase(),
                                style = SectionLabelStyle,
                                color = esquema.primary
                            )
                            Text(
                                text = nombre,
                                style = MaterialTheme.typography.headlineLargeEmphasized,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    bloquesDeInicio(summary = summary, orden = visibles, acciones = AccionesDeInicio()).forEach { bloque ->
                        key(bloque.clave) { bloque.contenido() }
                    }
                    if (!appearance.showHomeGreeting && visibles.isEmpty()) {
                        Text(
                            text = stringResource(R.string.settings_home_preview_empty),
                            style = MaterialTheme.typography.bodyMedium,
                            color = esquema.onSurfaceVariant,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
            // Encima de todo y sin hacer nada: es lo que deja las tarjetas sin tocar. La lista
            // de fuera sigue desplazándose porque no se consume nada.
            Box(modifier = Modifier.matchParentSize().pointerInput(Unit) {})
        }
    }
}

private const val ESCALA_DE_LA_MAQUETA = 0.6f
