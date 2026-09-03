@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppearancePreferences
import com.unistack.app.feature_user.domain.BottomBarStyle
import com.unistack.app.feature_user.domain.CornerStyle
import com.unistack.app.feature_user.domain.InterfaceDensity
import com.unistack.app.feature_user.domain.ProgressShape
import com.unistack.app.feature_user.domain.SurfaceStyle
import com.unistack.app.feature_user.domain.SwitchIconStyle
import com.unistack.app.feature_user.domain.TypographyStyle

/**
 * Las puertas de Apariencia, cada una con lo suyo.
 *
 * Antes era una sola pantalla con seis grupos apilados -- modo, color, densidad y letra,
 * detalles de la interfaz, tarjetas de inicio y lo que enseña el hero -- que se recorria a
 * base de scroll y donde el color se elegia en dos sitios a la vez. Es el mismo arreglo que
 * se hizo en Configuracion academica: cada cosa en su puerta, y el hub diciendo que hay
 * detras de cada una.
 */

/** Un rotulo de grupo, para no repetirlo en cada pantalla. */
@Composable
private fun Rotulo(texto: String, arriba: Boolean = false) {
    Text(
        text = texto,
        style = SectionLabelStyle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = if (arriba) 8.dp else 0.dp)
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

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SettingsHeader(title = titulo, subtitle = subtitulo, onBackClick = onBackClick) }
        item { Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { contenido(current.appearancePreferences) } }
    }
}

// ------------------------------------------------------------------ forma y superficie

/**
 * Como se separa una tarjeta del fondo, y cuanto aire hay dentro.
 *
 * `surfaceStyle` llevaba aqui desde el principio, guardado y viajando en la copia de
 * seguridad, **sin pintar nada**: elegir «plana» o «con sombra» daba exactamente el mismo
 * resultado. Ahora decide de verdad.
 */
@Composable
fun SurfaceSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = "Forma y superficie",
        subtitulo = "Cómo se separan las tarjetas y cuánto aire hay",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo("SUPERFICIE")
        UniSegmentedControl(
            selected = appearance.surfaceStyle,
            options = SurfaceStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(surfaceStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion(appearance.surfaceStyle.explicacion())

        Rotulo("ESQUINAS", arriba = true)
        UniSegmentedControl(
            selected = appearance.cornerStyle,
            options = CornerStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(cornerStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Afecta a tarjetas, botones y hojas, en toda la app.")

        Rotulo("DENSIDAD", arriba = true)
        UniSegmentedControl(
            selected = appearance.interfaceDensity,
            options = InterfaceDensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(interfaceDensity = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Cuánto separa la app una cosa de la siguiente.")
    }
}

// ------------------------------------------------------------------ tipografía

@Composable
fun TypographySettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = "Tipografía",
        subtitulo = "La letra de la app y cuántos decimales lleva una nota",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo("FAMILIA")
        UniSegmentedControl(
            selected = appearance.typographyStyle,
            options = TypographyStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(typographyStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("El tamaño del texto se cambia en Accesibilidad, para toda la app a la vez.")

        Rotulo("DECIMALES EN LAS NOTAS", arriba = true)
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
        Explicacion("Cuántos decimales enseña un promedio.")
    }
}

// ------------------------------------------------------------------ componentes

@Composable
fun ComponentSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = "Componentes",
        subtitulo = "Barra, progreso e interruptores",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo("BARRA DE ABAJO")
        UniSegmentedControl(
            selected = appearance.bottomBarStyle,
            options = BottomBarStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(bottomBarStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )

        Rotulo("BARRAS DE PROGRESO", arriba = true)
        UniSegmentedControl(
            selected = appearance.academicProgressShape,
            options = ProgressShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(academicProgressShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Es la onda de Material 3 Expressive. Las de descarga se quedan onduladas siempre.")

        Rotulo("ICONO DENTRO DEL INTERRUPTOR", arriba = true)
        UniSegmentedControl(
            selected = appearance.switchIconStyle,
            options = SwitchIconStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(switchIconStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            UniSwitch(checked = true, onCheckedChange = {})
            Explicacion("Así queda un interruptor encendido con lo que elijas.")
        }
    }
}
