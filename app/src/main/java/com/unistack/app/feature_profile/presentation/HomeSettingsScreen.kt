@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

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
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsGroupCard
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.components.rememberUniReorderState
import com.unistack.app.core.design.components.uniReorderHandle
import com.unistack.app.core.design.components.uniReorderableItem
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.HomeSection

/**
 * Qué sale en Inicio, en qué orden, y qué cuenta la tarjeta grande.
 *
 * Las dos listas van juntas porque son la misma pregunta a dos niveles: primero qué bloques
 * quieres, y después —si «Lo siguiente» está encendido— de qué te avisa. Repartidas entre dos
 * grupos del hub viejo, apagar el bloque dejaba debajo tres interruptores que ya no mandaban
 * en nada y nada lo decía. Aquí, si el bloque está apagado, su grupo no sale.
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
                title = "Tu inicio",
                subtitle = "Qué bloques salen, en qué orden y qué cuentan",
                onBackClick = onBackClick
            )
        }
        item {
            Column {
                Text(
                    text = "BLOQUES",
                    style = SectionLabelStyle,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 12.dp, top = 8.dp, bottom = 9.dp)
                )
                HomeBlocksCard(
                    appearance = appearance,
                    onToggleGreeting = { enabled ->
                        viewModel.updateAppearance { it.copy(showHomeGreeting = enabled) }
                    },
                    onToggleSection = { section, enabled ->
                        viewModel.updateAppearance { preferences ->
                            when (section) {
                                HomeSection.HERO -> preferences.copy(showHomeHero = enabled)
                                HomeSection.AGENDA -> preferences.copy(showHomeAgenda = enabled)
                                HomeSection.SNAPSHOT -> preferences.copy(showHomeSnapshot = enabled)
                            }
                        }
                    },
                    onReorder = { order ->
                        viewModel.updateAppearance { it.copy(homeSectionOrder = order) }
                    }
                )
                Text(
                    text = "Arrastra por el asa para cambiar el orden.",
                    modifier = Modifier.padding(top = 10.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        if (appearance.showHomeHero) {
            item {
                SettingsGroupCard(label = "QUÉ ENSEÑA LO SIGUIENTE") {
                    HomeToggleRow(
                        title = "Notas",
                        detail = "Promedios y materias en riesgo",
                        checked = appearance.heroShowsGrades,
                        draggable = false,
                        onCheckedChange = { enabled ->
                            viewModel.updateAppearance { it.copy(heroShowsGrades = enabled) }
                        }
                    )
                    HomeToggleRow(
                        title = "Tareas",
                        detail = "Entregas que vencen pronto",
                        checked = appearance.heroShowsTasks,
                        draggable = false,
                        onCheckedChange = { enabled ->
                            viewModel.updateAppearance { it.copy(heroShowsTasks = enabled) }
                        }
                    )
                    HomeToggleRow(
                        title = "Gastos",
                        detail = "Cuánto llevas gastado",
                        checked = appearance.heroShowsExpenses,
                        draggable = false,
                        onCheckedChange = { enabled ->
                            viewModel.updateAppearance { it.copy(heroShowsExpenses = enabled) }
                        }
                    )
                }
            }
        }
    }
}

/**
 * Los cuatro bloques de inicio: encender, apagar y ordenar en la misma lista.
 *
 * Antes eran dos cosas separadas —cuatro interruptores arriba y, más abajo, un editor de orden
 * con flechas que repetía tres de esos mismos nombres—. Que un bloque saliera dos veces con
 * dos controles distintos obligaba a leer las dos listas para saber cómo iba a quedar el
 * inicio. Ahora cada bloque aparece una vez, con su interruptor y su asa.
 *
 * El saludo no lleva asa: no está en el orden porque siempre encabeza, y un asa que no mueve
 * nada es peor que no tenerla.
 */
@Composable
private fun HomeBlocksCard(
    appearance: AppearancePreferences,
    onToggleGreeting: (Boolean) -> Unit,
    onToggleSection: (HomeSection, Boolean) -> Unit,
    onReorder: (List<HomeSection>) -> Unit
) {
    val order by rememberUpdatedState(appearance.homeSectionOrder)
    val reorder by rememberUpdatedState(onReorder)
    val dragState = rememberUniReorderState()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column {
            HomeToggleRow(
                title = "Saludo",
                detail = "Tu nombre y el momento del día",
                checked = appearance.showHomeGreeting,
                draggable = false,
                onCheckedChange = onToggleGreeting
            )
            order.forEach { section ->
                HomeToggleRow(
                    modifier = Modifier.uniReorderableItem(dragState, section),
                    title = section.label(),
                    detail = section.detail(),
                    checked = appearance.showsSection(section),
                    draggable = true,
                    handleModifier = Modifier.uniReorderHandle(
                        state = dragState,
                        key = section,
                        index = { order.indexOf(section) },
                        itemCount = { order.size },
                        onMove = { from, to ->
                            reorder(order.toMutableList().apply { add(to, removeAt(from)) })
                        }
                    ),
                    onCheckedChange = { enabled -> onToggleSection(section, enabled) }
                )
            }
        }
    }
}

/**
 * Una fila de bloque: asa opcional, nombre, apoyo e interruptor.
 */
@Composable
private fun HomeToggleRow(
    title: String,
    detail: String,
    checked: Boolean,
    draggable: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    handleModifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .cleanClickable { onCheckedChange(!checked) }
            .padding(horizontal = 15.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        if (draggable) {
            Icon(
                Icons.Rounded.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = handleModifier
                    .size(20.dp)
                    .semantics { contentDescription = "Arrastra para reordenar $title" }
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmallEmphasized,
                // Apagado, el nombre se atenúa: es lo que deja contar de un vistazo cuántos
                // bloques quedan encendidos sin leer los interruptores uno a uno.
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (checked) 1f else 0.55f)
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        UniSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
