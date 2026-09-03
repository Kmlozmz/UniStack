@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsGroupCard
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalSectionColors
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.AppLanguage
import com.unistack.app.feature_user.domain.ColorBlindPalette
import com.unistack.app.feature_user.domain.ContrastLevel
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.ReadingFont
import com.unistack.app.feature_user.domain.TextScalePreference
import com.unistack.app.feature_user.domain.TouchTargetSize
import com.unistack.app.feature_user.domain.UndoDuration

/**
 * Accesibilidad: idioma, ver, tocar y controlar, voz y seguridad.
 *
 * Eran seis ajustes sueltos —idioma, contraste, reloj de 24 horas, tamaño de texto, movimiento
 * y la animación del hero— apilados sin agrupar. Cubrían la vista y poco más: nada decía qué
 * hacer si no distingues el verde del rojo, si el pulgar no llega arriba, o si cinco segundos
 * no bastan para pulsar «Deshacer».
 *
 * Ahora son diecisiete en cuatro grupos, y cada grupo responde a una pregunta distinta: qué
 * idioma hablo, qué veo, cómo lo toco, y qué me lee la app en voz alta.
 */
@Composable
fun AccessibilitySettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val a11y = current.accessibilityPreferences

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
        item {
            SettingsHeader(
                title = "Accesibilidad",
                subtitle = "Idioma, lectura, color y control",
                onBackClick = onBackClick
            )
        }

        // ------------------------------------------------------------------ idioma
        item { Rotulo("IDIOMA") }
        item {
            SettingsGroupCard(label = "") {
                AppLanguage.entries.forEach { idioma ->
                    FilaDeRadio(
                        titulo = idioma.etiqueta(),
                        detalle = idioma.estado(),
                        elegido = a11y.appLanguage == idioma,
                        onClick = { viewModel.updateAccessibility { it.copy(appLanguage = idioma) } }
                    )
                }
            }
        }
        item {
            Explicacion(
                "El idioma cambia también las fechas y los importes: «31 de agosto» pasa a " +
                    "«August 31», y «$60.500» a «COP 60,500»."
            )
        }

        // ------------------------------------------------------------------ ver
        item { Rotulo("VER", arriba = true) }
        item {
            Etiqueta("Contraste")
            UniSegmentedControl(
                selected = a11y.contrast,
                options = ContrastLevel.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(contrast = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Etiqueta("Paleta para daltonismo")
            UniSegmentedControl(
                selected = a11y.colorBlindPalette,
                options = ColorBlindPalette.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(colorBlindPalette = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
            MuestraDePaleta(a11y.colorBlindPalette)
            Explicacion(
                "La app dice «bien o mal» con verde y rojo. Estas dos paletas no dependen de ese par."
            )
        }
        item {
            Etiqueta("Tamaño del texto")
            UniSegmentedControl(
                selected = a11y.textScale,
                options = TextScalePreference.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(textScale = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
            Explicacion("Para afinarlo al porcentaje, el deslizador está en Apariencia › Tipografía.")
        }
        item {
            Etiqueta("Tipografía de lectura")
            UniSegmentedControl(
                selected = a11y.readingFont,
                options = ReadingFont.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(readingFont = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            SettingsGroupCard(label = "") {
                FilaDeInterruptorA11y(
                    "Alto contraste",
                    "Sube la separación entre el texto y su fondo en toda la app.",
                    a11y.highContrastEnabled
                ) { valor -> viewModel.updateAccessibility { it.copy(highContrastEnabled = valor) } }
                FilaDeInterruptorA11y(
                    "Formas además del color",
                    "Círculo, triángulo y cuadrado junto a cada nota.",
                    a11y.shapesBesidesColor
                ) { valor -> viewModel.updateAccessibility { it.copy(shapesBesidesColor = valor) } }
                FilaDeInterruptorA11y(
                    "Texto en negrita",
                    "Sube el peso de todas las letras.",
                    a11y.boldText
                ) { valor -> viewModel.updateAccessibility { it.copy(boldText = valor) } }
            }
        }

        // ------------------------------------------------------------------ tocar y controlar
        item { Rotulo("TOCAR Y CONTROLAR", arriba = true) }
        item {
            Etiqueta("Tamaño de los toques")
            UniSegmentedControl(
                selected = a11y.touchTargetSize,
                options = TouchTargetSize.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(touchTargetSize = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
            Explicacion("Agranda el área que responde al dedo, sin cambiar cómo se ve.")
        }
        item {
            Etiqueta("Movimiento")
            UniSegmentedControl(
                selected = a11y.motionPreference,
                options = MotionPreference.entries.map { UniSegmentedOption(value = it, label = it.etiquetaA11y()) },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(motionPreference = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
            // Los dos ajustes existen a la vez y no se pisan: este apaga, el de Apariencia
            // elige. Decirlo aquí evita que parezca que uno de los dos no hace nada.
            Explicacion("Manda sobre lo elegido en Apariencia › Movimiento: si aquí pone «nada», nada se mueve.")
        }
        item {
            Etiqueta("Tiempo para deshacer")
            UniSegmentedControl(
                selected = a11y.undoDuration,
                options = UndoDuration.entries.map { UniSegmentedOption(value = it, label = "${it.segundos} s") },
                onSelected = { valor -> viewModel.updateAccessibility { it.copy(undoDuration = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
            Explicacion("Cuánto dura el aviso al borrar algo.")
        }
        item {
            SettingsGroupCard(label = "") {
                FilaDeInterruptorA11y(
                    "Reducir transparencias",
                    "Quita el cristal y los desenfoques.",
                    a11y.reduceTransparency
                ) { valor -> viewModel.updateAccessibility { it.copy(reduceTransparency = valor) } }
                FilaDeInterruptorA11y(
                    "Modo de una mano",
                    "Baja el contenido para alcanzarlo con el pulgar.",
                    a11y.oneHandedMode
                ) { valor -> viewModel.updateAccessibility { it.copy(oneHandedMode = valor) } }
                FilaDeInterruptorA11y(
                    "Reloj de 24 horas",
                    "«14:30» en vez de «2:30 p. m.».",
                    a11y.use24HourTime
                ) { valor -> viewModel.updateAccessibility { it.copy(use24HourTime = valor) } }
                FilaDeInterruptorA11y(
                    "Animación del saludo",
                    "El movimiento de la tarjeta grande de Inicio.",
                    a11y.heroAnimationEnabled
                ) { valor -> viewModel.updateAccessibility { it.copy(heroAnimationEnabled = valor) } }
            }
        }

        // ------------------------------------------------------------------ voz y seguridad
        item { Rotulo("VOZ Y SEGURIDAD", arriba = true) }
        item {
            SettingsGroupCard(label = "") {
                FilaDeInterruptorA11y(
                    "Descripciones habladas",
                    "TalkBack lee «tres coma cuatro sobre cinco» en vez de «3,4/5».",
                    a11y.spokenDescriptions
                ) { valor -> viewModel.updateAccessibility { it.copy(spokenDescriptions = valor) } }
                FilaDeInterruptorA11y(
                    "Confirmar lo irreversible",
                    "Preguntar antes de borrar o de cerrar un periodo.",
                    a11y.confirmIrreversible
                ) { valor -> viewModel.updateAccessibility { it.copy(confirmIrreversible = valor) } }
                FilaDeInterruptorA11y(
                    "Mantener la pantalla encendida",
                    "Mientras estudias con una nota abierta.",
                    a11y.keepScreenOn
                ) { valor -> viewModel.updateAccessibility { it.copy(keepScreenOn = valor) } }
            }
        }
    }
}

// ------------------------------------------------------------------ piezas

@Composable
private fun Rotulo(texto: String, arriba: Boolean = false) {
    Text(
        text = texto,
        style = SectionLabelStyle,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp, top = if (arriba) 8.dp else 0.dp)
    )
}

@Composable
private fun Etiqueta(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
    )
}

@Composable
private fun Explicacion(texto: String) {
    Text(
        text = texto,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
    )
}

/** Una opción de radio: se usa solo para el idioma, donde las opciones no son comparables. */
@Composable
private fun FilaDeRadio(titulo: String, detalle: String, elegido: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RadioButton(selected = elegido, onClick = onClick)
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmallEmphasized)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FilaDeInterruptorA11y(
    titulo: String,
    detalle: String,
    marcado: Boolean,
    onCambio: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .cleanClickable { onCambio(!marcado) }
            .padding(horizontal = 15.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, style = MaterialTheme.typography.titleSmallEmphasized)
            Text(
                detalle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        UniSwitch(checked = marcado, onCheckedChange = onCambio)
    }
}

/**
 * Los tres estados con la paleta puesta, y con su forma.
 *
 * Enseñar el par de colores no basta para elegir: lo que hay que ver es si «al día» y «en
 * riesgo» se distinguen **entre sí** con esa paleta, y eso solo se juzga poniéndolos juntos.
 */
@Composable
private fun MuestraDePaleta(paleta: ColorBlindPalette) {
    val secciones = LocalSectionColors.current
    val colores = when (paleta) {
        ColorBlindPalette.NINGUNA -> listOf(secciones.onTrack, secciones.atRisk, MaterialTheme.colorScheme.error)
        // Sin canal verde, el par que más se separa es azul-naranja.
        ColorBlindPalette.DEUTERANOPIA -> listOf(Color(0xFF3A7DE0), Color(0xFFE0A63C), Color(0xFF8C4BD1))
        // Sin canal azul, el verde se mueve hacia el magenta y el rojo se queda.
        ColorBlindPalette.TRITANOPIA -> listOf(Color(0xFF12B0A0), Color(0xFFE0567F), Color(0xFF7A2E4C))
    }
    val formas = listOf(CircleShape, CutCornerShape(percent = 50), RoundedCornerShape(4.dp))
    val nombres = listOf("Al día", "En riesgo", "Suspenso")
    Row(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        nombres.forEachIndexed { indice, nombre ->
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(formas[indice])
                        .background(colores[indice])
                )
                Text(nombre, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
            }
        }
    }
}

// ------------------------------------------------------------------ rótulos

private fun AppLanguage.etiqueta() = when (this) {
    AppLanguage.SYSTEM -> "Seguir al sistema"
    AppLanguage.SPANISH -> "Español"
    AppLanguage.ENGLISH -> "English"
}

/**
 * Lo que hay traducido de verdad.
 *
 * Se dice porque el ajuste existe desde hace tiempo y los textos siguen viviendo en el código:
 * elegir «English» hoy cambia la preferencia y deja la app en español. Un ajuste que promete
 * algo que no cumple es peor que uno que avisa.
 */
private fun AppLanguage.estado() = when (this) {
    AppLanguage.SYSTEM -> "El de tu teléfono, si está disponible"
    AppLanguage.SPANISH -> "Traducción completa"
    AppLanguage.ENGLISH -> "Traducción en curso"
}

private fun ContrastLevel.etiqueta() = when (this) {
    ContrastLevel.ESTANDAR -> "Estándar"
    ContrastLevel.ALTO -> "Alto"
    ContrastLevel.MAXIMO -> "Máximo"
}

private fun ColorBlindPalette.etiqueta() = when (this) {
    ColorBlindPalette.NINGUNA -> "Normal"
    ColorBlindPalette.DEUTERANOPIA -> "Deuteranopía"
    ColorBlindPalette.TRITANOPIA -> "Tritanopía"
}

private fun ReadingFont.etiqueta() = when (this) {
    ReadingFont.NORMAL -> "Normal"
    ReadingFont.DISLEXIA -> "Dislexia"
}

private fun TouchTargetSize.etiqueta() = when (this) {
    TouchTargetSize.ESTANDAR -> "Estándar"
    TouchTargetSize.GRANDE -> "Grande"
    TouchTargetSize.MAXIMO -> "Máximo"
}

private fun TextScalePreference.etiqueta() = when (this) {
    TextScalePreference.STANDARD -> "Normal"
    TextScalePreference.LARGE -> "Grande"
}

private fun MotionPreference.etiquetaA11y() = when (this) {
    MotionPreference.FULL -> "Completo"
    MotionPreference.REDUCED -> "Reducido"
    MotionPreference.NONE -> "Nada"
}
