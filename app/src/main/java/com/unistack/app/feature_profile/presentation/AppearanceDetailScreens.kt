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
import androidx.compose.ui.Alignment
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
        titulo = "Forma y superficie",
        subtitulo = "Tarjetas, botones, campos y distintivos",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        VistaPreviaDeTarjeta()

        Rotulo("SUPERFICIE", arriba = true)
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
            Rotulo("CUÁNTA SOMBRA")
            UniSegmentedControl(
                selected = appearance.shadowIntensity,
                options = ShadowIntensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(shadowIntensity = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (appearance.surfaceStyle == SurfaceStyle.OUTLINED) {
            Rotulo("GROSOR DEL FILETE")
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
        Explicacion("Afecta a tarjetas, botones y hojas, en toda la app. Mira la tarjeta de arriba.")

        Rotulo("DENSIDAD", arriba = true)
        UniSegmentedControl(
            selected = appearance.interfaceDensity,
            options = InterfaceDensity.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(interfaceDensity = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Cuánto separa la app una cosa de la siguiente, y cuánto aire hay dentro de una tarjeta.")

        Rotulo("FORMA DE LOS BOTONES", arriba = true)
        UniSegmentedControl(
            selected = appearance.buttonShape,
            options = ButtonShapeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeBotones()

        Rotulo("FORMA DE LOS CAMPOS", arriba = true)
        UniSegmentedControl(
            selected = appearance.textFieldStyle,
            options = TextFieldStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(textFieldStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeCampo()

        Rotulo("FORMA DE LOS CHIPS", arriba = true)
        UniSegmentedControl(
            selected = appearance.chipStyle,
            options = ChipStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(chipStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeChips()

        Rotulo("FORMA DE LOS DISTINTIVOS", arriba = true)
        UniSegmentedControl(
            selected = appearance.badgeShape,
            options = BadgeShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(badgeShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeDistintivos()
        Explicacion(
            if (appearance.badgeShape == BadgeShape.ALEATORIO) {
                "Cada materia se queda con la suya, siempre la misma: dos del mismo color ya no se confunden."
            } else {
                "Todas las materias con la misma forma. Se distinguen solo por el color."
            }
        )
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
        MuestraDeLetra()

        Rotulo("FAMILIA", arriba = true)
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

        Rotulo("TAMAÑO", arriba = true)
        DeslizadorDeTamano(
            porcentaje = appearance.textScalePercent,
            onSoltar = { valor -> viewModel.updateAppearance { it.copy(textScalePercent = valor) } }
        )

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

/** Un trozo de materia con cifras: donde se nota una familia de letra y no en el abecedario. */
@Composable
private fun MuestraDeLetra() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("Cálculo III", style = MaterialTheme.typography.headlineSmallEmphasized)
            Text("Promedio 4,25 · 3 cortes · 128 h", style = MaterialTheme.typography.titleMedium)
            Text(
                "Con 3,10 en el tercer corte cierras en 4,00. El segundo corte pesa el 35%.",
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
@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun DeslizadorDeTamano(porcentaje: Int, onSoltar: (Int) -> Unit) {
    // `key` sobre el valor guardado: si cambia desde fuera —restablecer apariencia, una copia
    // de seguridad— el deslizador se entera. Mientras se arrastra, manda lo local.
    var arrastre by remember(porcentaje) { mutableFloatStateOf(porcentaje.toFloat()) }
    val esquema = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Slider(
            value = arrastre,
            onValueChange = { arrastre = it },
            onValueChangeFinished = { onSoltar(arrastre.toInt()) },
            valueRange = 85f..135f,
            /*
             * Los puntos vuelven, **sin volver a enganchar el arrastre**.
             *
             * `steps` hace las dos cosas a la vez —dibuja las marcas y obliga a saltar de una a
             * otra— y por eso al quitarlo para poder arrastrar libre se fueron tambien los
             * puntos. Aqui la pista se pinta a mano: el carril de Material debajo, y las once
             * marcas encima, una cada cinco por ciento. Sirven de referencia y no de reja.
             */
            track = { estado ->
                Box(contentAlignment = Alignment.Center) {
                    SliderDefaults.Track(sliderState = estado, modifier = Modifier.fillMaxWidth())
                    Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
                        val marcas = 11
                        val fraccion = (estado.value - 85f) / 50f
                        repeat(marcas) { indice ->
                            val x = size.width * indice / (marcas - 1f)
                            // La marca ya recorrida va sobre el relleno y la que falta sobre el
                            // carril: cada una necesita el color que contrasta con lo que tiene
                            // detras, o desaparece justo al pasar por encima.
                            val pasada = indice / (marcas - 1f) <= fraccion
                            drawCircle(
                                color = if (pasada) esquema.onPrimary else esquema.primary,
                                radius = 2.2.dp.toPx(),
                                center = Offset(x.coerceIn(2.2.dp.toPx(), size.width - 2.2.dp.toPx()), size.height / 2f)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("Al ${arrastre.toInt()}%. Vale para toda la app, no solo para esta pantalla.")
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
        titulo = "Componentes",
        subtitulo = "Tamaños, barra, progreso e interruptores",
        onBackClick = onBackClick,
        modifier = modifier
    ) { appearance ->
        Rotulo("TAMAÑO DE LOS BOTONES")
        UniSegmentedControl(
            selected = appearance.buttonSize,
            options = ButtonSizeStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(buttonSize = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        Explicacion("La forma se elige en Forma y superficie.")

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
        // La barra de abajo está a la vista mientras se elige: una muestra suya aquí sería la
        // misma cosa dos veces en la misma pantalla.
        Explicacion("Míralos en la barra de abajo mientras eliges: cambia al momento.")

        Rotulo("BARRAS DE PROGRESO", arriba = true)
        UniSegmentedControl(
            selected = appearance.academicProgressShape,
            options = ProgressShape.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(academicProgressShape = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        BarraDeProgresoReal(progreso = 0.68f)
        Explicacion(
            "Es la onda de Material 3 Expressive, y se ve moviéndose porque quieta apenas se " +
                "distingue de una recta. Las de descarga se quedan onduladas siempre."
        )

        Rotulo("INTERRUPTORES", arriba = true)
        UniSegmentedControl(
            selected = appearance.switchIconStyle,
            options = SwitchIconStyle.entries.map { UniSegmentedOption(value = it, label = it.label()) },
            onSelected = { valor -> viewModel.updateAppearance { it.copy(switchIconStyle = valor) } },
            modifier = Modifier.fillMaxWidth()
        )
        VistaPreviaDeInterruptores()

        Rotulo("DETALLES", arriba = true)
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                FilaDeInterruptor(
                    titulo = "Separadores en las listas",
                    detalle = "La línea fina entre una fila y la siguiente.",
                    marcado = appearance.listDividers,
                    onCambio = { valor -> viewModel.updateAppearance { it.copy(listDividers = valor) } }
                )
                FilaDeInterruptor(
                    titulo = "Colores por sección",
                    detalle = "Verde, ámbar y rojo en las notas. Apagado, todo va con el acento.",
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
