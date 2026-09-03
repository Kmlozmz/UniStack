@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.formaDeDistintivo
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniStackButton
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

/**
 * Las puertas de Apariencia, cada una con lo suyo.
 *
 * Antes era una sola pantalla con seis grupos apilados —modo, color, densidad y letra,
 * detalles de la interfaz, tarjetas de inicio y lo que enseña el hero— que se recorría a
 * base de scroll y donde el color se elegía en dos sitios a la vez. Es el mismo arreglo que
 * se hizo en Configuración académica: cada cosa en su puerta, y el hub diciendo qué hay
 * detrás de cada una.
 *
 * **Cada pantalla enseña lo que hace.** No hay un solo ajuste aquí sin algo que se mueva al
 * tocarlo: el estilo de superficie pinta tres tarjetas, el de botón pinta un botón, el
 * distintivo pinta cinco materias. Es lo que separa esto de la versión anterior, donde media
 * docena de segmentos se guardaban sin cambiar un píxel.
 */

/** Un rótulo de grupo, para no repetirlo en cada pantalla. */
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
 * Cómo se separa una tarjeta del fondo, y cuánto aire hay dentro.
 *
 * `surfaceStyle` llevaba aquí desde el principio, guardado y viajando en la copia de
 * seguridad, **sin pintar nada**: elegir «plana» o «con sombra» daba exactamente el mismo
 * resultado. Ahora decide de verdad, y los dos ajustes que lo afinan —cuánta sombra, qué
 * grosor de filete— solo aparecen cuando el estilo elegido los usa: un deslizador de sombra
 * bajo una superficie plana es un mando desconectado.
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
        MuestraDeSuperficie(appearance)

        // Solo con «Sombra»: no hay opción «nada» porque una sombra de cero es una superficie
        // plana, y plana ya es una de las cuatro de arriba.
        if (appearance.surfaceStyle == SurfaceStyle.ELEVATED) {
            Rotulo("CUÁNTA SOMBRA", arriba = true)
            UniSegmentedControl(
                selected = appearance.shadowIntensity,
                options = ShadowIntensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(shadowIntensity = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (appearance.surfaceStyle == SurfaceStyle.OUTLINED) {
            Rotulo("GROSOR DEL FILETE", arriba = true)
            UniSegmentedControl(
                selected = appearance.outlineWeight,
                options = OutlineWeight.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(outlineWeight = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }

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

/** Tres tarjetas con el estilo puesto: es la única forma de comparar cuatro superficies. */
@Composable
private fun MuestraDeSuperficie(appearance: AppearancePreferences) {
    val esquema = MaterialTheme.colorScheme
    val elevacion = when (appearance.shadowIntensity) {
        ShadowIntensity.SUAVE -> 2.dp
        ShadowIntensity.MEDIA -> 6.dp
        ShadowIntensity.FUERTE -> 12.dp
    }
    val grosor = when (appearance.outlineWeight) {
        OutlineWeight.FINO -> 1.dp
        OutlineWeight.MEDIO -> 1.5.dp
        OutlineWeight.GRUESO -> 2.5.dp
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .then(
                        if (appearance.surfaceStyle == SurfaceStyle.ELEVATED) {
                            Modifier.shadow(elevacion, MaterialTheme.shapes.large)
                        } else {
                            Modifier
                        }
                    )
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        if (appearance.surfaceStyle == SurfaceStyle.TRANSLUCENT) {
                            esquema.surfaceContainer.copy(alpha = 0.55f)
                        } else {
                            esquema.surfaceContainer
                        }
                    )
                    .then(
                        if (appearance.surfaceStyle == SurfaceStyle.OUTLINED) {
                            Modifier.border(grosor, esquema.outlineVariant, MaterialTheme.shapes.large)
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

// ------------------------------------------------------------------ tipografía

@Composable
fun TypographySettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = "Tipografía",
        subtitulo = "La letra de la app, su tamaño y su aire",
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
        MuestraDeLetra()

        Rotulo("TAMAÑO", arriba = true)
        // Un deslizador y no tres segmentos: entre el 100% y el 110% hay gente que quiere el
        // 105, y con tres posiciones había que dar el salto entero o quedarse.
        Slider(
            value = appearance.textScalePercent.toFloat(),
            onValueChange = { valor ->
                viewModel.updateAppearance { it.copy(textScalePercent = valor.toInt()) }
            },
            valueRange = 85f..135f,
            steps = 9,
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Al ${appearance.textScalePercent}%. Vale para toda la app, no solo para esta pantalla.")

        Rotulo("INTERLINEADO", arriba = true)
        UniSegmentedControl(
            selected = appearance.lineHeightStyle,
            options = LineHeightStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(lineHeightStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "El aire entre renglones. Se nota en textos largos como este, " +
                "y no tanto en una lista de materias donde cada fila lleva una línea.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

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

/** El abecedario y unas cifras: lo justo para ver de qué familia se habla. */
@Composable
private fun MuestraDeLetra() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text("Cálculo III", style = MaterialTheme.typography.titleMediumEmphasized)
            Text(
                "Promedio 4,25 · 3 cortes · 128 h",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ------------------------------------------------------------------ componentes

/**
 * Botones, campos, chips, barra y distintivos.
 *
 * Cada grupo lleva su muestra debajo porque «Filete» y «Relleno» no dicen nada como palabras:
 * lo que separa un chip de otro es cómo se ve, y esta pantalla existe para verlo.
 */
@Composable
fun ComponentSettingsScreen(onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = hiltViewModel()
    PantallaDeAjustes(
        titulo = "Componentes",
        subtitulo = "Botones, campos, barra y distintivos",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo("BOTONES")
        UniSegmentedControl(
            selected = appearance.buttonShape,
            options = ButtonShapeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        UniSegmentedControl(
            selected = appearance.buttonSize,
            options = ButtonSizeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonSize = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        UniStackButton(text = "Guardar", onClick = {}, modifier = Modifier.fillMaxWidth())

        Rotulo("CAMPOS DE TEXTO", arriba = true)
        UniSegmentedControl(
            selected = appearance.textFieldStyle,
            options = TextFieldStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(textFieldStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        MuestraDeCampo(appearance.textFieldStyle)

        Rotulo("CHIPS Y FILTROS", arriba = true)
        UniSegmentedControl(
            selected = appearance.chipStyle,
            options = ChipStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(chipStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        MuestraDeChips(appearance.chipStyle)

        Rotulo("BARRA DE ABAJO", arriba = true)
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
        Explicacion("Míralos en la barra de abajo mientras eliges.")

        Rotulo("DISTINTIVOS DE MATERIA", arriba = true)
        UniSegmentedControl(
            selected = appearance.badgeShape,
            options = BadgeShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(badgeShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        MuestraDeDistintivos(appearance.badgeShape)
        Explicacion(
            if (appearance.badgeShape == BadgeShape.ALEATORIO) {
                "Cada materia se queda con la suya, siempre la misma: dos del mismo color ya no se confunden."
            } else {
                "El punto de color de cada materia, en toda la app."
            }
        )

        Rotulo("BARRAS DE PROGRESO", arriba = true)
        UniSegmentedControl(
            selected = appearance.academicProgressShape,
            options = ProgressShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(academicProgressShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Es la onda de Material 3 Expressive. Las de descarga se quedan onduladas siempre.")

        Rotulo("INTERRUPTORES", arriba = true)
        UniSegmentedControl(
            selected = appearance.switchIconStyle,
            options = SwitchIconStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(switchIconStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            UniSwitch(checked = true, onCheckedChange = {})
            UniSwitch(checked = false, onCheckedChange = {})
            Explicacion("Encendido y apagado, con lo que elijas.")
        }

        Rotulo("DETALLES", arriba = true)
        FilaDeInterruptor(
            titulo = "Separadores en las listas",
            detalle = "La línea fina entre una fila y la siguiente.",
            marcado = appearance.listDividers,
            onCambio = { valor -> viewModel.updateAppearance { it.copy(listDividers = valor) } }
        )
        if (appearance.listDividers) HorizontalDivider()
        FilaDeInterruptor(
            titulo = "Colores por sección",
            detalle = "Verde, ámbar y rojo en las notas. Apagado, todo va con el acento.",
            marcado = appearance.sectionColorsEnabled,
            onCambio = { valor -> viewModel.updateAppearance { it.copy(sectionColorsEnabled = valor) } }
        )

        Rotulo("PRIMER DÍA DE LA SEMANA", arriba = true)
        UniSegmentedControl(
            selected = appearance.firstDayOfWeek,
            options = FirstDayOfWeek.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(firstDayOfWeek = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        MuestraDeSemana(appearance.firstDayOfWeek)
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
        modifier = Modifier.fillMaxWidth(),
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

@Composable
private fun MuestraDeCampo(estilo: TextFieldStyle) {
    val esquema = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .then(
                when (estilo) {
                    TextFieldStyle.RELLENO -> Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .background(esquema.surfaceContainerHighest)
                    TextFieldStyle.FILETE -> Modifier
                        .clip(MaterialTheme.shapes.medium)
                        .border(1.5.dp, esquema.outline, MaterialTheme.shapes.medium)
                    // El subrayado no lleva forma: solo la línea de abajo, que es lo que lo
                    // hace distinto de los otros dos y no una variante del filete.
                    TextFieldStyle.SUBRAYADO -> Modifier
                }
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            "Nombre de la materia",
            modifier = Modifier.padding(horizontal = 14.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = esquema.onSurfaceVariant
        )
        if (estilo == TextFieldStyle.SUBRAYADO) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.5.dp)
                    .align(Alignment.BottomStart)
                    .background(esquema.outline)
            )
        }
    }
}

@Composable
private fun MuestraDeChips(estilo: ChipStyle) {
    val esquema = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf("Vencidas" to true, "Hoy" to false, "Sin materia" to false).forEach { (texto, activo) ->
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(percent = 50))
                    .then(
                        when (estilo) {
                            ChipStyle.RELLENO -> Modifier.background(
                                if (activo) esquema.primary else esquema.surfaceContainerHighest
                            )
                            ChipStyle.FILETE -> Modifier
                                .background(if (activo) esquema.primaryContainer else esquema.surface)
                                .border(1.dp, esquema.outlineVariant, RoundedCornerShape(percent = 50))
                            ChipStyle.TEXTO -> Modifier
                        }
                    )
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Text(
                    texto,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (activo) FontWeight.Bold else FontWeight.Normal,
                    color = when {
                        estilo == ChipStyle.RELLENO && activo -> esquema.onPrimary
                        estilo == ChipStyle.TEXTO && activo -> esquema.primary
                        else -> esquema.onSurface
                    }
                )
            }
        }
    }
}

@Composable
private fun MuestraDeDistintivos(forma: BadgeShape) {
    val colores = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.error,
        MaterialTheme.colorScheme.primary
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
        // Cinco identificadores fijos: con «Aleatorio» puesto, el reparto se ve estable y no
        // como algo que cambia cada vez que se pinta la pantalla.
        listOf("cal", "fis", "prog", "est", "ing").forEachIndexed { indice, id ->
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(formaDeDistintivo(forma, id))
                    .background(colores[indice])
            )
        }
    }
}

@Composable
private fun MuestraDeSemana(primerDia: FirstDayOfWeek) {
    val dias = if (primerDia == FirstDayOfWeek.LUNES) {
        listOf("L", "M", "X", "J", "V", "S", "D")
    } else {
        listOf("D", "L", "M", "X", "J", "V", "S")
    }
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        dias.forEach { dia ->
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center
            ) {
                Text(dia, style = MaterialTheme.typography.labelMedium, fontSize = 12.sp)
            }
        }
    }
}

// ------------------------------------------------------------------ rótulos

internal fun ShadowIntensity.label() = when (this) {
    ShadowIntensity.SUAVE -> "Suave"
    ShadowIntensity.MEDIA -> "Media"
    ShadowIntensity.FUERTE -> "Fuerte"
}

internal fun OutlineWeight.label() = when (this) {
    OutlineWeight.FINO -> "Fino"
    OutlineWeight.MEDIO -> "Medio"
    OutlineWeight.GRUESO -> "Grueso"
}

internal fun LineHeightStyle.label() = when (this) {
    LineHeightStyle.COMPACTO -> "Compacto"
    LineHeightStyle.NORMAL -> "Normal"
    LineHeightStyle.AMPLIO -> "Amplio"
}

internal fun ButtonShapeStyle.label() = when (this) {
    ButtonShapeStyle.RECTO -> "Rectos"
    ButtonShapeStyle.MEDIO -> "Medios"
    ButtonShapeStyle.PASTILLA -> "Pastilla"
}

internal fun ButtonSizeStyle.label() = when (this) {
    ButtonSizeStyle.PEQUENO -> "Pequeño"
    ButtonSizeStyle.MEDIO -> "Medio"
    ButtonSizeStyle.GRANDE -> "Grande"
}

internal fun TextFieldStyle.label() = when (this) {
    TextFieldStyle.RELLENO -> "Relleno"
    TextFieldStyle.FILETE -> "Filete"
    TextFieldStyle.SUBRAYADO -> "Subrayado"
}

internal fun ChipStyle.label() = when (this) {
    ChipStyle.FILETE -> "Filete"
    ChipStyle.RELLENO -> "Relleno"
    ChipStyle.TEXTO -> "Solo texto"
}

internal fun IconStyle.label() = when (this) {
    IconStyle.REDONDEADO -> "Redondeado"
    IconStyle.LINEAL -> "Lineal"
    IconStyle.RELLENO -> "Relleno"
}

internal fun BadgeShape.label() = when (this) {
    BadgeShape.CIRCULO -> "Círculo"
    BadgeShape.GALLETA -> "Galleta"
    BadgeShape.TREBOL -> "Trébol"
    BadgeShape.SOL -> "Sol"
    BadgeShape.ROMBO -> "Rombo"
    BadgeShape.ALEATORIO -> "Aleatorio"
}

internal fun FirstDayOfWeek.label() = when (this) {
    FirstDayOfWeek.LUNES -> "Lunes"
    FirstDayOfWeek.DOMINGO -> "Domingo"
}
