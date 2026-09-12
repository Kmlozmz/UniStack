@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.ui.geometry.Offset
import androidx.compose.material3.SliderDefaults
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.Canvas
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
import com.unistack.app.core.design.components.LargeTitleScaffold
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BadgeShape
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.ButtonShapeStyle
import com.unistack.app.feature_user.domain.ButtonSizeStyle
import com.unistack.app.feature_user.domain.ChipStyle
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.FirstDayOfWeek
import com.unistack.app.feature_user.domain.IconStyle
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.LineHeightStyle
import com.unistack.app.feature_user.domain.OutlineWeight
import com.unistack.app.feature_user.domain.ProgressShape
import com.unistack.app.feature_user.domain.ShadowIntensity
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.SwitchIconStyle
import com.unistack.app.feature_user.domain.TextFieldStyle
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

// ------------------------------------------------------------------ forma y superficie

/**
 * Todo lo que decide **qué forma tienen las cosas**: tarjetas, botones, campos y distintivos.
 *
 * `surfaceStyle` llevaba aquí desde el principio, guardado y viajando en la copia de
 * seguridad, **sin pintar nada**: elegir «plana» o «con sombra» daba exactamente el mismo
 * resultado. Ahora decide de verdad, y los dos ajustes que lo afinan —cuánta sombra, qué
 * grosor de filete— solo aparecen cuando el estilo elegido los usa: un selector de sombra bajo
 * una superficie plana es un mando desconectado.
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

        Rotulo(stringResource(R.string.settings_surface_sec_surface), arriba = true)
        UniSegmentedControl(
            selected = appearance.surfaceStyle,
            options = SurfaceStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(surfaceStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(appearance.surfaceStyle.explicacion())

        // Solo con «Sombra»: no hay opción «nada» porque una sombra de cero es una superficie
        // plana, y plana ya es una de las cuatro de arriba.
        if (appearance.surfaceStyle == SurfaceStyle.ELEVATED) {
            Rotulo(stringResource(R.string.settings_surface_sec_shadow))
            UniSegmentedControl(
                selected = appearance.shadowIntensity,
                options = ShadowIntensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(shadowIntensity = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (appearance.surfaceStyle == SurfaceStyle.OUTLINED) {
            Rotulo(stringResource(R.string.settings_surface_sec_outline))
            UniSegmentedControl(
                selected = appearance.outlineWeight,
                options = OutlineWeight.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(outlineWeight = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }

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

        Rotulo(stringResource(R.string.settings_surface_sec_button_shape), arriba = true)
        UniSegmentedControl(
            selected = appearance.buttonShape,
            options = ButtonShapeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeBotones()

        Rotulo(stringResource(R.string.settings_surface_sec_field_shape), arriba = true)
        UniSegmentedControl(
            selected = appearance.textFieldStyle,
            options = TextFieldStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(textFieldStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeCampo()

        Rotulo(stringResource(R.string.settings_surface_sec_chip_shape), arriba = true)
        UniSegmentedControl(
            selected = appearance.chipStyle,
            options = ChipStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(chipStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeChips()

        Rotulo(stringResource(R.string.settings_surface_sec_badge_shape), arriba = true)
        UniSegmentedControl(
            selected = appearance.badgeShape,
            options = BadgeShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(badgeShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeDistintivos()
        Explicacion(
            if (appearance.badgeShape == BadgeShape.ALEATORIO) {
                stringResource(R.string.settings_surface_badge_desc_unique)
            } else {
                stringResource(R.string.settings_surface_badge_desc_same)
            }
        )
    }
}

// ------------------------------------------------------------------ tipografía

@Composable
fun TypographySettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = stringResource(R.string.settings_typo_title),
        subtitulo = stringResource(R.string.settings_typo_subtitle),
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        MuestraDeLetra()

        Rotulo(stringResource(R.string.settings_typo_sec_family), arriba = true)
        // Seis familias en dos filas: en una sola de seis, «Redondeada» y «Estrecha» se
        // parten por la mitad y no se leen.
        UniSegmentedControl(
            selected = appearance.typographyStyle,
            options = TypographyStyle.entries.take(3).map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(typographyStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        UniSegmentedControl(
            selected = appearance.typographyStyle,
            options = TypographyStyle.entries.drop(3).map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(typographyStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(appearance.typographyStyle.explicacion())

        Rotulo(stringResource(R.string.settings_typo_sec_size), arriba = true)
        DeslizadorDeTamano(
            porcentaje = appearance.textScalePercent,
            onSoltar = { valor -> viewModel.updateAppearance { it.copy(textScalePercent = valor) } }
        )

        Rotulo(stringResource(R.string.settings_typo_sec_line_height), arriba = true)
        UniSegmentedControl(
            selected = appearance.lineHeightStyle,
            options = LineHeightStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(lineHeightStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = stringResource(R.string.settings_typo_line_height_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

/** Un trozo de materia con cifras: donde se nota una familia de letra y no en el abecedario. */
@Composable
private fun MuestraDeLetra() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(stringResource(R.string.settings_typo_sample_title), style = MaterialTheme.typography.headlineSmallEmphasized)
            Text(stringResource(R.string.settings_typo_sample_sub), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(R.string.settings_typo_sample_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * El deslizador del tamaño de texto, que **arrastra de verdad**.
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

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.appearance_text_scale_note, arrastre.toInt()))
    }
}

// ------------------------------------------------------------------ componentes

/**
 * Lo que no es forma sino comportamiento: tamaños, barra, iconos, progreso e interruptores.
 *
 * Las formas se mudaron a «Forma y superficie», que es donde las buscaba cualquiera. Aquí
 * queda lo que decide cómo se comporta o cuánto ocupa un control, y cada grupo lleva su
 * muestra: la de progreso **animada**, porque la onda de M3E se reconoce por su movimiento.
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
        Rotulo(stringResource(R.string.settings_components_sec_button_size))
        UniSegmentedControl(
            selected = appearance.buttonSize,
            options = ButtonSizeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonSize = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(stringResource(R.string.settings_components_button_size_desc))

        Rotulo(stringResource(R.string.settings_components_sec_bottom_bar), arriba = true)
        UniSegmentedControl(
            selected = appearance.bottomBarStyle,
            options = BottomBarStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(bottomBarStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        UniSegmentedControl(
            selected = appearance.iconStyle,
            options = IconStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(iconStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        // La barra de abajo está a la vista mientras se elige: una muestra suya aquí sería la
        // misma cosa dos veces en la misma pantalla.
        Explicacion(stringResource(R.string.settings_components_bottom_bar_desc))

        Rotulo(stringResource(R.string.settings_components_sec_progress), arriba = true)
        UniSegmentedControl(
            selected = appearance.academicProgressShape,
            options = ProgressShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(academicProgressShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        BarraDeProgresoReal(progreso = 0.68f)
        Explicacion(stringResource(R.string.settings_components_progress_desc))

        Rotulo(stringResource(R.string.settings_components_sec_switches), arriba = true)
        UniSegmentedControl(
            selected = appearance.switchIconStyle,
            options = SwitchIconStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(switchIconStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeInterruptores()

        Rotulo(stringResource(R.string.settings_components_sec_details), arriba = true)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                FilaDeInterruptor(
                    titulo = stringResource(R.string.settings_components_list_dividers),
                    detalle = stringResource(R.string.settings_components_list_dividers_desc),
                    marcado = appearance.listDividers,
                    onCambio = { valor -> viewModel.updateAppearance { it.copy(listDividers = valor) } }
                )
                FilaDeInterruptor(
                    titulo = stringResource(R.string.settings_components_section_colors),
                    detalle = stringResource(R.string.settings_components_section_colors_desc),
                    marcado = appearance.sectionColorsEnabled,
                    onCambio = { valor -> viewModel.updateAppearance { it.copy(sectionColorsEnabled = valor) } }
                )
            }
        }
    }
}

@Composable
private fun FilaDeInterruptor(
    titulo: String,
    detalle: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmallEmphasized)
            Explicacion(detalle)
        }
        UniSwitch(checked = marcado, onCheckedChange = onCambio)
    }
}

// ------------------------------------------------------------------ rótulos

internal fun ShadowIntensity.label(): String {
    return when (this) {
        ShadowIntensity.SUAVE -> Textos.get(R.string.settings_accent_soft)
        ShadowIntensity.MEDIA -> Textos.get(R.string.templates_priority_medium)
        ShadowIntensity.FUERTE -> Textos.get(R.string.appearance_fuerte)
    }
}

internal fun OutlineWeight.label(): String {
    return when (this) {
        OutlineWeight.FINO -> Textos.get(R.string.appearance_fino)
        OutlineWeight.MEDIO -> Textos.get(R.string.settings_accent_balanced)
        OutlineWeight.GRUESO -> Textos.get(R.string.appearance_grueso)
    }
}

internal fun LineHeightStyle.label(): String {
    return when (this) {
        LineHeightStyle.COMPACTO -> Textos.get(R.string.appearance_compacto)
        LineHeightStyle.NORMAL -> Textos.get(R.string.a11y_motion_normal)
        LineHeightStyle.AMPLIO -> Textos.get(R.string.appearance_amplio)
    }
}

internal fun ButtonShapeStyle.label(): String {
    return when (this) {
        ButtonShapeStyle.RECTO -> Textos.get(R.string.appearance_rectos)
        ButtonShapeStyle.MEDIO -> Textos.get(R.string.appearance_medios)
        ButtonShapeStyle.PASTILLA -> Textos.get(R.string.appearance_pastilla)
    }
}

internal fun ButtonSizeStyle.label(): String {
    return when (this) {
        ButtonSizeStyle.PEQUENO -> Textos.get(R.string.appearance_pequeno)
        ButtonSizeStyle.MEDIO -> Textos.get(R.string.settings_accent_balanced)
        ButtonSizeStyle.GRANDE -> Textos.get(R.string.appearance_grande)
    }
}

internal fun TextFieldStyle.label(): String {
    return when (this) {
        TextFieldStyle.RELLENO -> Textos.get(R.string.appearance_relleno)
        TextFieldStyle.FILETE -> Textos.get(R.string.appearance_filete)
        TextFieldStyle.SUBRAYADO -> Textos.get(R.string.appearance_subrayado)
    }
}

internal fun ChipStyle.label(): String {
    return when (this) {
        ChipStyle.FILETE -> Textos.get(R.string.appearance_filete)
        ChipStyle.RELLENO -> Textos.get(R.string.appearance_relleno)
        ChipStyle.TEXTO -> Textos.get(R.string.appearance_solo_texto)
    }
}

internal fun IconStyle.label(): String {
    return when (this) {
        IconStyle.REDONDEADO -> Textos.get(R.string.appearance_redondeado)
        IconStyle.LINEAL -> Textos.get(R.string.appearance_lineal)
        IconStyle.RELLENO -> Textos.get(R.string.appearance_relleno)
    }
}

internal fun BadgeShape.label(): String {
    return when (this) {
        BadgeShape.CIRCULO -> Textos.get(R.string.appearance_circulo)
        BadgeShape.GALLETA -> Textos.get(R.string.appearance_galleta)
        BadgeShape.TREBOL -> Textos.get(R.string.appearance_trebol)
        BadgeShape.SOL -> Textos.get(R.string.appearance_sol)
        BadgeShape.ROMBO -> Textos.get(R.string.appearance_rombo)
        BadgeShape.ALEATORIO -> Textos.get(R.string.appearance_aleatorio)
    }
}

internal fun FirstDayOfWeek.label(): String {
    return when (this) {
        FirstDayOfWeek.LUNES -> Textos.get(R.string.appearance_lunes)
        FirstDayOfWeek.DOMINGO -> Textos.get(R.string.appearance_domingo)
    }
}
