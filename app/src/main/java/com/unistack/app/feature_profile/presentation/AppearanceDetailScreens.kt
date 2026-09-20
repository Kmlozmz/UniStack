@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material.icons.rounded.Circle
import com.unistack.app.core.design.components.SettingsSoloRow
import com.unistack.app.core.design.components.SettingsToggleRow
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import com.unistack.app.core.design.theme.familia
import java.util.Locale
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import com.unistack.app.core.utils.performSafely
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.School
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.SettingsRowIcon
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.core.design.theme.tonosDeAjustes
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.ButtonSizeStyle
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.SettingsIconColor
import com.unistack.app.feature_user.domain.SubjectsLayout
import com.unistack.app.feature_user.domain.FirstDayOfWeek
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.LineHeightStyle
import com.unistack.app.feature_user.domain.TypographyStyle
import com.unistack.app.core.utils.Textos

/**
 * Las puertas de Apariencia, cada una con lo suyo.
 *
 * **Cada ajuste enseña lo que hace, con una pieza de la app y no con un rectángulo.** La
 * versión anterior ponía tres cajas grises al elegir superficie y una fila de puntos al elegir
 * distintivo, y con eso «plana» y «filete» se veían idénticas: las muestras abstractas no
 * enseñan un contraste, solo un color. Ahora la superficie se juzga en una tarjeta de materia
 * de verdad, los chips en los de Tareas, y el progreso animándose, porque la onda de Material
 * 3 Expressive se reconoce por cómo se mueve y no por su silueta quieta.
 *
 * El reparto entre las dos pantallas también cambió: **lo que es forma va en «Forma y
 * superficie»** —la de las tarjetas, la de los botones, la de los campos, la de los
 * distintivos— y en «Componentes» queda lo que no es forma sino comportamiento: tamaños,
 * barra, iconos, progreso e interruptores. Antes la forma de un botón estaba en una pantalla y
 * la de una tarjeta en la otra, sin nada que lo explicara.
 */

/** Un rótulo de grupo, para no repetirlo en cada pantalla. */
@Composable
private fun Rotulo(texto: String, arriba: Boolean = false) {
    Text(
        text = texto,
        style = SectionLabelStyle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = if (arriba) 10.dp else 0.dp)
    )
}

@Composable
private fun Explicacion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun PantallaDeAjustes(
    titulo: String,
    subtitulo: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable (AppearancePreferences) -> Unit
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return

    LargeTitleScaffold(
        title = titulo,
        subtitle = subtitulo,
        onBackClick = onBackClick,
        modifier = modifier,
        horizontalPadding = spacing.screenHorizontal,
        topPadding = 8.dp,
        bottomPadding = scrollBottomRoom,
        itemSpacing = 12.dp
    ) {
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { contenido(current.appearancePreferences) } }
    }
}

// ------------------------------------------------------------------ forma

/**
 * Lo que decide **qué forma tienen las cosas**, con lo que cambia de una persona a otra.
 *
 * Fue «Forma y superficie», con nueve mandos. El 20 sep 2026 se quedó con cuatro: esquinas y
 * densidad, el tamaño de los botones (que vivía en Componentes y es de dedos, no de diseño) y
 * las materias como tarjetas o como lista. Superficie, forma de botones, campos, chips y
 * distintivos eran decisiones de diseño puestas delante del usuario, y van con lo suyo: plana,
 * pastilla, filete, filete y cada materia con la suya.
 */
@Composable
fun SurfaceSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = stringResource(R.string.settings_surface_title),
        subtitulo = stringResource(R.string.settings_surface_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        VistaPreviaDeTarjeta()

        Rotulo(stringResource(R.string.settings_surface_sec_corners), arriba = true)
        UniSegmentedControl(
            selected = appearance.cornerStyle,
            options = CornerStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(cornerStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_surface_corners_desc))

        Rotulo(stringResource(R.string.settings_surface_sec_density), arriba = true)
        UniSegmentedControl(
            selected = appearance.interfaceDensity,
            options = InterfaceDensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(interfaceDensity = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_surface_density_desc))

        Rotulo(stringResource(R.string.settings_components_sec_button_size), arriba = true)
        UniSegmentedControl(
            selected = appearance.buttonSize,
            options = ButtonSizeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonSize = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeBotones()
        Explicacion(stringResource(R.string.settings_surface_button_size_desc))

        Rotulo(stringResource(R.string.settings_surface_sec_subjects), arriba = true)
        UniSegmentedControl(
            selected = appearance.subjectsLayout,
            options = SubjectsLayout.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(subjectsLayout = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_surface_subjects_desc))
    }
}

// ------------------------------------------------------------------ tipografía

/**
 * La letra: familia, tamaño, interlineado y decimales, en ese orden, con una sola muestra.
 *
 * Los cuatro mandos son los de siempre; lo que cambió el 20 sep 2026 es el orden y el aire. La
 * familia va en una lista desplegable con cada nombre escrito en su letra —en dos filas de
 * fichas, «Redondeada» y «Estrecha» no cabían— y la muestra de arriba responde a los cuatro:
 * la letra, el tamaño, el aire entre renglones y el promedio con sus decimales.
 */
@Composable
fun TypographySettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = stringResource(R.string.settings_typo_title),
        subtitulo = stringResource(R.string.settings_typo_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        MuestraDeLetra(decimales = appearance.decimalPlaces)

        Rotulo(stringResource(R.string.settings_typo_sec_family), arriba = true)
        SelectorDeFamilia(
            elegida = appearance.typographyStyle,
            onElegir = { valor -> viewModel.updateAppearance { it.copy(typographyStyle = valor) } }
        )

        Rotulo(stringResource(R.string.settings_typo_sec_size), arriba = true)
        DeslizadorDeTamano(
            porcentaje = appearance.textScalePercent,
            onSoltar = { valor -> viewModel.updateAppearance { it.copy(textScalePercent = valor) } }
        )
        Explicacion(stringResource(R.string.settings_typo_size_desc))

        Rotulo(stringResource(R.string.settings_typo_sec_line_height), arriba = true)
        UniSegmentedControl(
            selected = appearance.lineHeightStyle,
            options = LineHeightStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(lineHeightStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_typo_line_height_desc))

        Rotulo(stringResource(R.string.settings_typo_sec_decimals), arriba = true)
        UniSegmentedControl(
            selected = appearance.decimalPlaces,
            options = listOf(
                UniSegmentedOption(value = 0, label = "3"),
                UniSegmentedOption(value = 1, label = "3,5"),
                UniSegmentedOption(value = 2, label = "3,50")
            ),
            onSelected = { valor -> viewModel.updateAppearance { it.copy(decimalPlaces = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_typo_decimals_desc))
    }
}

/**
 * Un trozo de materia con cifras: donde se nota una familia de letra y no en el abecedario.
 *
 * La familia, el tamaño y el interlineado le llegan solos por el tema; el promedio se escribe
 * aquí con los decimales elegidos, para que ese mando también se vea en la muestra.
 */
@Composable
private fun MuestraDeLetra(decimales: Int) {
    val promedio = String.format(Locale.getDefault(), "%.${decimales.coerceIn(0, 2)}f", 4.25)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(stringResource(R.string.settings_typo_sample_title), style = MaterialTheme.typography.headlineSmallEmphasized)
            Text(stringResource(R.string.settings_typo_sample_sub, promedio), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_typo_sample_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * La familia como lista desplegable: la fila enseña la elegida con su explicación y, al tocar,
 * se abren las seis, cada una escrita en su propia letra.
 *
 * La lista mide lo que la fila —se toma el ancho de la fila al medirla— y cuelga seis puntos
 * por debajo, que es como venía dibujada.
 */
@Composable
private fun SelectorDeFamilia(elegida: TypographyStyle, onElegir: (TypographyStyle) -> Unit) {
    var abierto by remember { mutableStateOf(false) }
    var anchoDeLaFila by remember { mutableIntStateOf(0) }
    val densidad = LocalDensity.current

    Box(modifier = Modifier.fillMaxWidth().onSizeChanged { anchoDeLaFila = it.width }) {
        Surface(
            onClick = { abierto = true },
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = elegida.label(),
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        fontFamily = elegida.familia()
                    )
                    Explicacion(elegida.explicacion())
                }
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        DropdownMenu(
            expanded = abierto,
            onDismissRequest = { abierto = false },
            shape = RoundedCornerShape(18.dp),
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            shadowElevation = 8.dp,
            offset = DpOffset(0.dp, 6.dp),
            modifier = Modifier.width(with(densidad) { anchoDeLaFila.toDp() })
        ) {
            Column(modifier = Modifier.padding(6.dp)) {
                TypographyStyle.entries.forEach { estilo ->
                    val esLaElegida = estilo == elegida
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                abierto = false
                                if (!esLaElegida) onElegir(estilo)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = estilo.label(),
                            style = MaterialTheme.typography.bodyLarge,
                            fontFamily = estilo.familia(),
                            color = if (esLaElegida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        if (esLaElegida) {
                            Icon(
                                imageVector = Icons.Rounded.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * El deslizador del tamaño de texto, que **arrastra de verdad**, con el porcentaje al lado.
 *
 * Quitar los `steps` no bastó: seguía enganchándose al primer movimiento. La causa era otra —
 * cada píxel del arrastre escribía en las preferencias, eso viaja al disco, y el valor volvía
 * uno o dos fotogramas tarde. El deslizador se pintaba entonces en la posición vieja, que
 * bajo el dedo se siente como si se hubiera trabado.
 *
 * Aquí el arrastre vive en estado local y solo se guarda **al levantar el dedo**. De paso, la
 * app deja de reconstruir su tipografía entera cincuenta veces por gesto.
 */
@Composable
private fun DeslizadorDeTamano(porcentaje: Int, onSoltar: (Int) -> Unit) {
    /*
     * Pasos de cinco, con las marcas que Material dibuja solo.
     *
     * Estuvo sin `steps` para poder arrastrar libre, y con eso pasaron dos cosas: se fueron las
     * marcas —`steps` las dibuja y hace saltar a la vez— y las que dibuje a mano quedaban
     * apinadas contra el borde derecho, porque tenia que recortarlas para que no se salieran.
     *
     * Con diez tramos de cinco por ciento no hace falta ninguna de las dos cosas: Material
     * reparte las marcas el solo, y de todas formas nadie necesita el 97% pudiendo elegir el
     * 95 o el 100.
     */
    val pasos = 9
    var arrastre by remember(porcentaje) { mutableFloatStateOf(porcentaje.toFloat()) }
    val haptica = LocalHapticFeedback.current
    // El aviso al tacto se dispara al cambiar de tramo, no en cada pixel del arrastre: sin
    // esto, un gesto de punta a punta daria cincuenta golpecitos seguidos.
    var ultimoTramo by remember(porcentaje) { mutableIntStateOf(porcentaje / 5) }

    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Slider(
            value = arrastre,
            onValueChange = { valor ->
                arrastre = valor
                val tramo = valor.toInt() / 5
                if (tramo != ultimoTramo) {
                    ultimoTramo = tramo
                    haptica.performSafely(HapticFeedbackType.SegmentTick)
                }
            },
            onValueChangeFinished = { onSoltar(arrastre.toInt()) },
            valueRange = 85f..135f,
            steps = pasos,
            modifier = Modifier.weight(1f)
        )
        // Ancho fijo para que «85 %» y «135 %» no muevan el deslizador al pasar de una a otra.
        Text(
            text = stringResource(R.string.settings_typo_size_value, arrastre.toInt()),
            style = MaterialTheme.typography.titleSmallEmphasized,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp)
        )
    }
}

// ------------------------------------------------------------------ componentes

/**
 * Las piezas: los iconos de Ajustes, la barra de abajo y dos detalles.
 *
 * Quedó así el 20 sep 2026. El tamaño de los botones se mudó a Forma; barras de progreso,
 * iconos de los interruptores, separadores y colores por sección eran decisiones de diseño y
 * van con lo suyo. Entran la forma de los iconos aparte del color, los puntos de aviso en las
 * pestañas y el primer día de la semana, que se guardaba y se usaba sin ningún mando.
 */
@Composable
fun ComponentSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = stringResource(R.string.settings_components_title),
        subtitulo = stringResource(R.string.settings_components_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo(stringResource(R.string.settings_components_sec_settings_icons))
        MuestraDeIconosDeAjustes()
        UniSegmentedControl(
            selected = appearance.settingsIconColor,
            options = SettingsIconColor.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(settingsIconColor = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        SettingsSoloRow {
            SettingsToggleRow(
                icon = Icons.Rounded.Circle,
                title = stringResource(R.string.settings_components_icons_round_title),
                subtitle = stringResource(R.string.settings_components_icons_round_desc),
                checked = appearance.settingsIconRound,
                iconColor = tonosDeAjustes.indigo,
                onCheckedChange = { valor -> viewModel.updateAppearance { it.copy(settingsIconRound = valor) } }
            )
        }

        Rotulo(stringResource(R.string.settings_components_sec_bottom_bar), arriba = true)
        UniSegmentedControl(
            selected = appearance.bottomBarStyle,
            options = BottomBarStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(bottomBarStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        SettingsSoloRow {
            SettingsToggleRow(
                icon = Icons.Rounded.Notifications,
                title = stringResource(R.string.settings_components_tab_badges_title),
                subtitle = stringResource(R.string.settings_components_tab_badges_desc),
                checked = appearance.tabBadges,
                iconColor = tonosDeAjustes.rojo,
                onCheckedChange = { valor -> viewModel.updateAppearance { it.copy(tabBadges = valor) } }
            )
        }

        Rotulo(stringResource(R.string.settings_components_sec_first_day), arriba = true)
        UniSegmentedControl(
            selected = appearance.firstDayOfWeek,
            options = FirstDayOfWeek.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(firstDayOfWeek = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_components_first_day_desc))
    }
}

// ------------------------------------------------------------------ rótulos



internal fun LineHeightStyle.label(): String {
    return when (this) {
        LineHeightStyle.COMPACTO -> Textos.get(R.string.appearance_compacto)
        LineHeightStyle.NORMAL -> Textos.get(R.string.a11y_motion_normal)
        LineHeightStyle.AMPLIO -> Textos.get(R.string.appearance_amplio)
    }
}


internal fun SubjectsLayout.label(): String = when (this) {
    SubjectsLayout.CARDS -> Textos.get(R.string.appearance_subjects_cards)
    SubjectsLayout.LIST -> Textos.get(R.string.appearance_subjects_list)
}

internal fun ButtonSizeStyle.label(): String {
    return when (this) {
        ButtonSizeStyle.PEQUENO -> Textos.get(R.string.appearance_pequeno)
        ButtonSizeStyle.MEDIO -> Textos.get(R.string.settings_accent_balanced)
        ButtonSizeStyle.GRANDE -> Textos.get(R.string.appearance_grande)
    }
}



internal fun SettingsIconColor.label(): String = when (this) {
    SettingsIconColor.COLORES -> Textos.get(R.string.appearance_iconos_de_colores)
    SettingsIconColor.ACENTO -> Textos.get(R.string.appearance_iconos_con_el_acento)
}

/**
 * Seis iconos de Ajustes con la forma elegida, que es donde se juzga.
 *
 * Ajustes no está a la vista mientras se elige, así que sin esto habría que salir y volver para
 * saber qué cambió. Son los de las puertas de verdad, con sus colores.
 */
@Composable
private fun MuestraDeIconosDeAjustes() {
    val tonos = tonosDeAjustes
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SettingsRowIcon(Icons.Rounded.Palette, tonos.rosa)
            SettingsRowIcon(Icons.Rounded.Accessibility, tonos.azul)
            SettingsRowIcon(Icons.Rounded.School, tonos.indigo)
            SettingsRowIcon(Icons.Rounded.History, tonos.turquesa)
            SettingsRowIcon(Icons.Rounded.Notifications, tonos.ambar)
            SettingsRowIcon(Icons.Rounded.Backup, tonos.cian)
        }
    }
}



internal fun FirstDayOfWeek.label(): String {
    return when (this) {
        FirstDayOfWeek.LUNES -> Textos.get(R.string.appearance_lunes)
        FirstDayOfWeek.DOMINGO -> Textos.get(R.string.appearance_domingo)
    }
}
