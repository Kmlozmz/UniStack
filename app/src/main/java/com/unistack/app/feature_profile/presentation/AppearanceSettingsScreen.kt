@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import com.unistack.app.feature_user.domain.portraitUrl
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.SettingsGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.utils.greetingForNow
import com.unistack.app.feature_user.domain.AccentStyle
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.HomeSection
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.TypographyStyle
import com.unistack.app.feature_user.domain.VisualPreference

/**
 * Apariencia: la pantalla más grande de ajustes, y la única con vista previa.
 *
 * La maqueta va arriba del todo y no al final porque es la razón de estar aquí: se cambia una
 * cosa y se mira qué pasa. Debajo, cada decisión es un grupo conectado en vez de una rejilla
 * de dos columnas con contorno —cuatro modos en dos filas de dos no se leían como cuatro
 * opciones de lo mismo, sino como dos parejas.
 */
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
                title = "Apariencia",
                subtitle = "Tema, color, densidad y tu inicio",
                onBackClick = onBackClick
            )
        }
        if (current == null) {
            item {
                Text(
                    "Cargando preferencias...",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@LazyColumn
        }

        val appearance = current.appearancePreferences
        item {
            HomePreviewCard(
                name = current.preferredName,
                photoUrl = current.portraitUrl,
                appearance = appearance
            )
        }
        item {
            SettingsGroupBare(label = "MODO") {
                UniSegmentedControl(
                    // El modo personalizado no entra: no hay forma de configurarlo desde
                    // ninguna pantalla, así que como quinto segmento sería un botón que
                    // no lleva a nada. Quien lo tuviera guardado ve «Sistema» marcado y
                    // puede salir de ahí eligiendo cualquiera de los cuatro.
                    selected = current.visualPreference.orSystem(),
                    options = VisiblePreferences.map { option ->
                        UniSegmentedOption(value = option, label = option.label())
                    },
                    onSelected = viewModel::updateVisualPreference,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = current.visualPreference.themeDescription(),
                    modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
        item {
            SettingsGroupBare(label = "COLOR") {
                /*
                 * Dos opciones, que son las dos que la app sabe entregar de verdad.
                 *
                 * Antes se ofrecían siete acentos y uno personalizado, y de ahí se derivaba
                 * el resto de la paleta mezclando colores. Ahora el color es un esquema
                 * tonal completo: o el que Material saca del fondo de pantalla, o el de la
                 * marca. Ofrecer una opción que no cambia nada es peor que no ofrecerla.
                 */
                val dynamic = appearance.accentStyle == AccentStyle.DYNAMIC
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AccentChoiceCard(
                        title = "Del fondo",
                        detail = "Toma el color de tu pantalla",
                        dot = MaterialTheme.colorScheme.tertiary,
                        selected = dynamic,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.updateAppearance { it.copy(accentStyle = AccentStyle.DYNAMIC) } }
                    AccentChoiceCard(
                        title = "Violeta UniStack",
                        detail = "El de la marca, siempre",
                        dot = MaterialTheme.colorScheme.primary,
                        selected = !dynamic,
                        modifier = Modifier.weight(1f)
                    ) { viewModel.updateAppearance { it.copy(accentStyle = AccentStyle.VIOLET) } }
                }
            }
        }
        item {
            SettingsGroupBare(label = "DENSIDAD Y LETRA") {
                Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    UniSegmentedControl(
                        selected = appearance.interfaceDensity,
                        options = InterfaceDensity.entries.map { option ->
                            UniSegmentedOption(value = option, label = option.label())
                        },
                        onSelected = { value ->
                            viewModel.updateAppearance { it.copy(interfaceDensity = value) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    UniSegmentedControl(
                        selected = appearance.typographyStyle,
                        options = TypographyStyle.entries.map { option ->
                            UniSegmentedOption(value = option, label = option.label())
                        },
                        onSelected = { value ->
                            viewModel.updateAppearance { it.copy(typographyStyle = value) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        item {
            SettingsGroupBare(label = "TARJETAS DE INICIO") {
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
        item {
            SettingsGroup(label = "QUÉ ENSEÑA LO SIGUIENTE") {
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

/** Los cuatro modos que se pueden elegir. */
private val VisiblePreferences = listOf(
    VisualPreference.SYSTEM,
    VisualPreference.LIGHT,
    VisualPreference.DARK,
    VisualPreference.OLED
)

private fun VisualPreference.orSystem(): VisualPreference =
    if (this in VisiblePreferences) this else VisualPreference.SYSTEM

/**
 * Un rótulo de sección con su contenido suelto debajo, sin contenedor.
 *
 * [SettingsGroup] mete lo suyo dentro de una tarjeta, que es lo que quieren las listas de
 * filas. Aquí abajo van controles que ya traen su propio fondo —los grupos conectados, las
 * tarjetas de color— y meterlos en otra tarjeta sería una caja dentro de otra caja.
 */
@Composable
private fun SettingsGroupBare(label: String, content: @Composable () -> Unit) {
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
 * Tu inicio, en pequeño y de verdad.
 *
 * Lo que había antes eran tres rectángulos grises y una línea de texto que resumía los ajustes
 * con palabras. No enseñaba nada: para saber cómo iba a quedar el inicio había que salir de
 * aquí e ir a mirarlo. Esta maqueta enciende y apaga las mismas piezas que los interruptores
 * de abajo, y el color y la letra son los del tema que esté puesto en ese momento.
 */
@Composable
private fun HomePreviewCard(
    name: String,
    photoUrl: String?,
    appearance: AppearancePreferences
) {
    val shown = name.takeIf { it.isNotBlank() } ?: "Estudiante"

    Column {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Surface(
                modifier = Modifier.padding(10.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(13.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // La maqueta enseña tu retrato, no una inicial genérica: es una vista
                        // previa de tu Inicio, y el avatar es lo primero que se ve en él.
                        AccountAvatar(
                            photoUrl = photoUrl,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            initial = shown.first().uppercase()
                        )
                        Row(
                            modifier = Modifier.weight(1f),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                "Uni",
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Stack",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Spacer(Modifier.width(22.dp))
                    }
                    if (appearance.showHomeGreeting) {
                        Text(
                            text = greetingForNow().uppercase(),
                            modifier = Modifier.padding(top = 10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            style = SectionLabelStyle
                        )
                        Text(
                            text = shown,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleLargeEmphasized,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                    if (appearance.showHomeHero) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 9.dp),
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text("LO SIGUIENTE", style = SectionLabelStyle)
                                Text(
                                    "Cálculo III a las 10:00",
                                    modifier = Modifier.padding(top = 2.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
        Text(
            text = "Así queda tu inicio con lo que elijas.",
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

/**
 * Una de las dos formas de elegir color: el punto enseña de qué color se habla.
 */
@Composable
private fun AccentChoiceCard(
    title: String,
    detail: String,
    dot: androidx.compose.ui.graphics.Color,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(dot)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(
                    detail,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 2
                )
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
    var dragged by remember { mutableStateOf<HomeSection?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    var rowHeight by remember { mutableIntStateOf(0) }
    val order by rememberUpdatedState(appearance.homeSectionOrder)
    val reorder by rememberUpdatedState(onReorder)

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
                val isDragged = dragged == section
                HomeToggleRow(
                    modifier = Modifier
                        .zIndex(if (isDragged) 1f else 0f)
                        .graphicsLayer { translationY = if (isDragged) dragOffset else 0f }
                        .onSizeChanged { rowHeight = it.height },
                    title = section.label(),
                    detail = section.detail(),
                    checked = appearance.showsSection(section),
                    draggable = true,
                    // La clave del gesto es solo la sección: si dependiera del orden, la
                    // primera permuta reiniciaría el detector y el dedo se quedaría a
                    // medias con la fila pegada al sitio nuevo.
                    handleModifier = Modifier.pointerInput(section) {
                        detectDragGestures(
                            onDragStart = {
                                dragged = section
                                dragOffset = 0f
                            },
                            onDragEnd = {
                                dragged = null
                                dragOffset = 0f
                            },
                            onDragCancel = {
                                dragged = null
                                dragOffset = 0f
                            },
                            onDrag = { change, amount ->
                                change.consume()
                                dragOffset += amount.y
                                val height = rowHeight
                                if (height <= 0) return@detectDragGestures
                                val index = order.indexOf(section)
                                val step = when {
                                    dragOffset > height / 2f && index < order.lastIndex -> 1
                                    dragOffset < -height / 2f && index > 0 -> -1
                                    else -> 0
                                }
                                if (step != 0) {
                                    // Al permutar, la fila ya salta un hueco entero por sí
                                    // sola: se le descuenta esa altura al arrastre para que
                                    // siga justo debajo del dedo y no se adelante.
                                    dragOffset -= step * height
                                    reorder(
                                        order.toMutableList().apply {
                                            add(index + step, removeAt(index))
                                        }
                                    )
                                }
                            }
                        )
                    },
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

private fun AppearancePreferences.showsSection(section: HomeSection): Boolean = when (section) {
    HomeSection.HERO -> showHomeHero
    HomeSection.AGENDA -> showHomeAgenda
    HomeSection.SNAPSHOT -> showHomeSnapshot
}

private fun VisualPreference.label() = when (this) {
    VisualPreference.SYSTEM -> "Sistema"
    VisualPreference.LIGHT -> "Claro"
    VisualPreference.DARK -> "Oscuro"
    VisualPreference.OLED -> "OLED"
    VisualPreference.CUSTOM -> "Personalizado"
}

private fun VisualPreference.themeDescription() = when (this) {
    VisualPreference.SYSTEM -> "Sigue el tema de Android."
    VisualPreference.LIGHT -> "Siempre claro, aunque Android esté oscuro."
    VisualPreference.DARK -> "Siempre oscuro, aunque Android esté claro."
    VisualPreference.OLED -> "Negro puro: en pantallas OLED gasta menos batería."
    VisualPreference.CUSTOM -> ""
}

private fun InterfaceDensity.label() = when (this) {
    InterfaceDensity.COMPACT -> "Compacta"
    InterfaceDensity.BALANCED -> "Equilibrada"
    InterfaceDensity.COMFORTABLE -> "Cómoda"
}

private fun TypographyStyle.label() = when (this) {
    TypographyStyle.UNISTACK -> "Letra UniStack"
    TypographyStyle.SYSTEM -> "La del sistema"
}

private fun HomeSection.label() = when (this) {
    HomeSection.HERO -> "Lo siguiente"
    HomeSection.AGENDA -> "Hoy"
    HomeSection.SNAPSHOT -> "Cifras"
}

private fun HomeSection.detail() = when (this) {
    HomeSection.HERO -> "La tarjeta con lo más urgente"
    HomeSection.AGENDA -> "Clases y entregas del día"
    HomeSection.SNAPSHOT -> "Promedio, pendientes y gasto"
}
