@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircleOutline
import androidx.compose.material.icons.rounded.Animation
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Dashboard
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.FormatLineSpacing
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.SwipeLeft
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.HapticStrength
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.MotionChoice
import com.unistack.app.feature_user.domain.MotionGesture
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.MotionPreferences
import com.unistack.app.feature_user.domain.MotionToggle

/**
 * Movimiento, con la forma exacta del diseño aprobado.
 *
 * La estructura no es libre: sale del artifact medida a medida —tarjeta de 16dp con 12 de
 * relleno, icono de 32 en su cuadrado teñido al 13%, rótulo de grupo con el recuento a la
 * derecha, y la rejilla de variantes **a dos columnas** con borde de 2dp y caja de 66dp de
 * alto—. Se había construido a tres columnas y con las tarjetas del sistema de ajustes, y por
 * eso no se parecía por mucho que las animaciones fueran las mismas.
 *
 * Tres formas de elegir, y cada una es la que le toca a su tipo de ajuste:
 *
 * - **Base**: un segmentado, porque «Rápida / Normal / Lenta» es una escala y una escala se
 *   lee mejor en fila que en rejilla. Debajo, un demo que se abre al pulsar.
 * - **Los veinte gestos**: la rejilla de variantes animadas, que es donde se compara.
 * - **Otros**: un interruptor y un demo que enseña las dos caras a la vez, porque ahí no hay
 *   variantes que comparar sino tenerlo o no tenerlo.
 */
@Composable
fun MotionSettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsStateWithLifecycle()
    val spacing = LocalInterfaceSpacing.current
    val current = profile ?: return
    val appearance = current.appearancePreferences
    val motion = appearance.motion
    val activo = appearance.motionPreference == MotionPreference.FULL
    val haptica = LocalHapticFeedback.current

    val base = MotionCatalog.gestures.filter { it.group == MotionCatalog.GROUP_BASE }
    val porGrupo = MotionCatalog.grouped().filter { it.first != MotionCatalog.GROUP_BASE }

    LazyColumn(
        modifier = modifier.fillMaxSize().statusBarsPadding(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = 8.dp,
            bottom = scrollBottomRoom
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        item {
            SettingsHeader(
                title = "Movimiento",
                subtitle = "Cada gesto con sus variantes, comparables de un vistazo",
                onBackClick = onBackClick
            )
        }

        item { InterruptorMaestro(appearance.motionPreference, activo, viewModel) }

        item { RotuloDeGrupo("BASE", "${base.size} ajustes") }
        items(base.size, key = { base[it].id }) { indice ->
            val gesto = base[indice]
            TarjetaDeAjuste(
                icono = iconoDe(gesto.id),
                color = colorDe(gesto.id),
                nombre = gesto.name,
                detalle = gesto.detail
            ) {
                // Escala en fila y no en rejilla: «Nada / Rápida / Normal / Lenta» se lee de
                // izquierda a derecha, y en rejilla ese orden se pierde.
                UniSegmentedControl(
                    selected = gesto.read(motion).id,
                    options = gesto.options.map { UniSegmentedOption(value = it.id, label = it.label) },
                    onSelected = { id ->
                        gesto.options.firstOrNull { it.id == id }?.let { opcion ->
                            viewModel.updateAppearance { p -> p.copy(motion = gesto.write(p.motion, opcion)) }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                DemoDesplegable(gestoId = gesto.id, variante = gesto.read(motion).id, animar = activo)
            }
        }

        porGrupo.forEach { (grupo, gestos) ->
            item(key = "rotulo-$grupo") {
                RotuloDeGrupo(
                    grupo,
                    "${gestos.size} gestos · ${gestos.sumOf { it.options.size }} variantes"
                )
            }
            items(gestos.size, key = { gestos[it].id }) { indice ->
                val gesto = gestos[indice]
                TarjetaDeAjuste(
                    icono = iconoDe(gesto.id),
                    color = colorDe(gesto.id),
                    nombre = gesto.name,
                    detalle = gesto.detail
                ) {
                    RejillaDeVariantes(
                        gesto = gesto,
                        motion = motion,
                        animar = activo,
                        onElegir = { opcion ->
                            if (gesto.id == "haptica") (opcion as? HapticStrength)?.avisar(haptica)
                            viewModel.updateAppearance { p -> p.copy(motion = gesto.write(p.motion, opcion)) }
                        }
                    )
                }
            }
        }

        item { RotuloDeGrupo("OTROS", "${MotionCatalog.toggles.size} ajustes") }
        items(MotionCatalog.toggles.size, key = { MotionCatalog.toggles[it].id }) { indice ->
            val toggle = MotionCatalog.toggles[indice]
            TarjetaDeInterruptor(
                toggle = toggle,
                motion = motion,
                animar = activo,
                onCambio = { valor ->
                    viewModel.updateAppearance { p -> p.copy(motion = toggle.write(p.motion, valor)) }
                }
            )
        }
    }
}

// ------------------------------------------------------------------ piezas del diseño

/**
 * El rótulo de grupo: nombre a la izquierda, recuento a la derecha.
 *
 * Nueve píxeles, ochocientos de peso y setenta centésimas de espaciado entre letras, que son
 * las medidas del diseño. El recuento a la derecha no es adorno: dice cuánto hay debajo antes
 * de bajar a mirarlo.
 */
@Composable
private fun RotuloDeGrupo(nombre: String, recuento: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(start = 2.dp, end = 2.dp, top = 16.dp, bottom = 7.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = nombre,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = recuento,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 0.7.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

/**
 * La tarjeta de un ajuste: icono teñido, nombre, descripción y lo que venga debajo.
 *
 * El cuadrado del icono va relleno al trece por ciento de su color y el icono a plena
 * intensidad, que es lo que hace que veinticinco tarjetas seguidas se distingan de un vistazo
 * sin leer ni un nombre.
 */
@Composable
private fun TarjetaDeAjuste(
    icono: ImageVector,
    color: Color,
    nombre: String,
    detalle: String,
    contenido: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icono, contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        nombre,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        detalle,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            contenido()
        }
    }
}

/**
 * La rejilla de variantes: **dos columnas**, como en el diseño.
 *
 * Estuvo a tres, y a tres la caja de cada variante mide sesenta y pocos píxeles de ancho: una
 * transición de pantalla dentro de eso es una mancha. A dos, la caja tiene sitio para que se
 * entienda qué entra y qué sale, que es de lo que va la pantalla.
 */
@Composable
private fun RejillaDeVariantes(
    gesto: MotionGesture,
    motion: MotionPreferences,
    animar: Boolean,
    onElegir: (MotionChoice) -> Unit
) {
    val elegida = gesto.read(motion)
    Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        gesto.options.chunked(2).forEach { fila ->
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                fila.forEach { opcion ->
                    CajaDeVariante(
                        gestoId = gesto.id,
                        opcion = opcion,
                        elegida = opcion.id == elegida.id,
                        animar = animar,
                        modifier = Modifier.weight(1f),
                        onClick = { onElegir(opcion) }
                    )
                }
                // Una fila impar deja el hueco en vez de estirar la última al doble.
                if (fila.size == 1) Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

/** Una variante: borde de 2dp, caja de 66dp con la animación, y el nombre debajo. */
@Composable
private fun CajaDeVariante(
    gestoId: String,
    opcion: MotionChoice,
    elegida: Boolean,
    animar: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tinta = tintaDemo()
    val esquema = MaterialTheme.colorScheme
    val t = bucle(duracionMs = duracionDe(gestoId), etiqueta = gestoId + opcion.id)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(13.dp))
            .background(if (elegida) esquema.primary.copy(alpha = 0.12f) else Color.Transparent)
            .border(
                width = 2.dp,
                color = if (elegida) esquema.primary else esquema.outlineVariant,
                shape = RoundedCornerShape(13.dp)
            )
            .cleanClickable(onClick = onClick)
            .padding(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(esquema.surfaceContainerHighest)
                .padding(7.dp)
        ) {
            LienzoDemo(t = if (animar) t else 0.55f) { reloj ->
                pintarVariante(gestoId, opcion.id, reloj, tinta)
            }
        }
        Text(
            text = if (elegida) "✓ " + opcion.label else opcion.label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (elegida) esquema.primary else esquema.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 5.dp)
        )
    }
}

/**
 * El interruptor de «Otros», con su demo comparativo.
 *
 * No lleva rejilla porque aquí no hay variantes que comparar: hay tenerlo o no tenerlo, y eso
 * se juzga viendo las dos caras a la vez. Por eso el demo enseña las dos, encendida y apagada,
 * una al lado de la otra.
 */
@Composable
private fun TarjetaDeInterruptor(
    toggle: MotionToggle,
    motion: MotionPreferences,
    animar: Boolean,
    onCambio: (Boolean) -> Unit
) {
    val marcado = toggle.read(motion)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                val color = colorDe(toggle.id)
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(color.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconoDe(toggle.id), contentDescription = null, tint = color, modifier = Modifier.size(17.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        toggle.name,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        toggle.detail,
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                UniSwitch(checked = marcado, onCheckedChange = onCambio)
            }
            DemoComparativo(toggle.id, marcado, animar)
        }
    }
}

/**
 * El demo que se abre al pulsar, como el «▶ Ver» del diseño.
 *
 * Cerrado por defecto y no siempre abierto: con veinticinco tarjetas, cuatro demos permanentes
 * arriba del todo empujan la lista media pantalla hacia abajo antes de haber elegido nada.
 */
@Composable
private fun DemoDesplegable(gestoId: String, variante: String, animar: Boolean) {
    var abierto by rememberSaveable(gestoId) { mutableStateOf(false) }
    val tinta = tintaDemo()
    val t = bucle(duracionMs = duracionDe(gestoId), etiqueta = "demo$gestoId")

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        BotonDeDemo(abierto = abierto, texto = "Ver") { abierto = !abierto }
        AnimatedVisibility(
            visible = abierto,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .padding(12.dp)
            ) {
                LienzoDemo(t = if (animar) t else 0.55f) { reloj ->
                    pintarVariante(gestoId, variante, reloj, tinta)
                }
            }
        }
    }
}

/** El demo de «Otros»: encendido a la izquierda, apagado a la derecha, para comparar. */
@Composable
private fun DemoComparativo(toggleId: String, marcado: Boolean, animar: Boolean) {
    var abierto by rememberSaveable(toggleId) { mutableStateOf(false) }
    val tinta = tintaDemo()
    val t = bucle(duracionMs = 2400, etiqueta = "cmp$toggleId")

    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
        BotonDeDemo(abierto = abierto, texto = "Comparar") { abierto = !abierto }
        AnimatedVisibility(
            visible = abierto,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                listOf(true, false).forEach { encendido ->
                    Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(72.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                .border(
                                    width = if (encendido == marcado) 2.dp else 0.dp,
                                    color = if (encendido == marcado) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .padding(8.dp)
                        ) {
                            LienzoDemo(t = if (animar) t else 0.55f) { reloj ->
                                pintarInterruptor(toggleId, encendido, reloj, tinta)
                            }
                        }
                        Text(
                            text = if (encendido) "Encendido" else "Apagado",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (encendido == marcado) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.padding(top = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonDeDemo(abierto: Boolean, texto: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(18.dp))
            .cleanClickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            if (abierto) Icons.Rounded.Check else Icons.Rounded.PlayArrow,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            if (abierto) "Ocultar" else texto,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** El interruptor maestro, que manda sobre los veinticinco de abajo. */
@Composable
private fun InterruptorMaestro(
    preferencia: MotionPreference,
    activo: Boolean,
    viewModel: ProfileViewModel
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = if (activo) {
            MaterialTheme.colorScheme.surfaceContainerLow
        } else {
            // Apagado, la tarjeta se tiñe: se nota que lo de abajo está en pausa sin leer.
            MaterialTheme.colorScheme.primaryContainer
        }
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.13f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Animation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Cuánto movimiento", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (activo) {
                            "Manda sobre los ${MotionCatalog.gestures.size} gestos de abajo."
                        } else {
                            "Los gestos de abajo quedan guardados, pero en pausa."
                        },
                        fontSize = 10.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            UniSegmentedControl(
                selected = preferencia,
                options = MotionPreference.entries.map { UniSegmentedOption(value = it, label = it.etiqueta()) },
                onSelected = { valor -> viewModel.updateAppearance { it.copy(motionPreference = valor) } },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ------------------------------------------------------------------ iconos y colores

/**
 * El icono de cada ajuste, con el color que le da el diseño.
 *
 * Van aquí y no en [MotionCatalog] para que el catálogo siga sin depender de Compose: es
 * dominio, y el dominio no tiene por qué saber qué es un `ImageVector`.
 */
private fun iconoDe(id: String): ImageVector = when (id) {
    "velocidad", "latido", "presupuesto" -> Icons.Rounded.Bolt
    "rebote", "transicion", "deshacer", "fijar", "saludo", "parallax" -> Icons.Rounded.Animation
    "pulsacion" -> Icons.Rounded.TouchApp
    "carga", "refresco" -> Icons.Rounded.Refresh
    "listas" -> Icons.Rounded.FormatLineSpacing
    "notaNueva", "numeros" -> Icons.Rounded.Numbers
    "claseAhora" -> Icons.Rounded.Schedule
    "errorShake" -> Icons.Rounded.ErrorOutline
    "fabScroll" -> Icons.Rounded.AddCircleOutline
    "haptica" -> Icons.Rounded.Vibration
    "barraAnim" -> Icons.Rounded.Dashboard
    "gestos" -> Icons.Rounded.SwipeLeft
    else -> Icons.Rounded.AutoAwesome
}

/** Los colores del diseño, uno por ajuste. Son los que hacen la lista legible de un vistazo. */
private fun colorDe(id: String): Color = when (id) {
    "velocidad", "fabScroll" -> Color(0xFFE8693A)
    "rebote", "notaNueva", "numeros" -> Color(0xFF7F77DD)
    "pulsacion", "recupera", "saludo" -> Color(0xFF4FBFA6)
    "carga", "cierreSem", "celebracion" -> Color(0xFFE0A63C)
    "transicion", "sello", "deshacer" -> Color(0xFF3F8FE0)
    "listas", "asistencia", "tachar", "claseAhora", "gestos" -> Color(0xFF5FC96E)
    "refresco" -> Color(0xFF3FC7B4)
    "subeNota", "barraAnim" -> Color(0xFFE062A8)
    "latido", "presupuesto", "errorShake" -> Color(0xFFEA5A52)
    "guardado", "parallax" -> Color(0xFF8C93A8)
    "fijar", "haptica" -> Color(0xFFC08BE0)
    else -> Color(0xFF7F77DD)
}

/** Cuánto dura una vuelta del bucle, por gesto. */
private fun duracionDe(gestoId: String): Int = when (gestoId) {
    "latido", "claseAhora" -> 2200
    "carga", "refresco" -> 1800
    "haptica" -> 1600
    "velocidad", "rebote", "pulsacion" -> 1700
    "celebracion", "cierreSem", "saludo" -> 2600
    else -> 2200
}

private fun MotionPreference.etiqueta() = when (this) {
    MotionPreference.FULL -> "Completo"
    MotionPreference.REDUCED -> "Reducido"
    MotionPreference.NONE -> "Nada"
}

/**
 * El aviso que le toca a cada fuerza de vibración.
 *
 * Android no deja pedir «un poco de vibración»: lo que hay son tipos de aviso con intensidad
 * propia. `TextHandleMove` es el más leve, `LongPress` el que más se nota, y encadenar dos
 * seguidos es lo más cerca que se puede estar de un aviso «fuerte» sin permisos de vibrador.
 */
private fun HapticStrength.avisar(haptica: HapticFeedback) {
    when (this) {
        HapticStrength.NINGUNA -> Unit
        HapticStrength.SUAVE -> haptica.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        HapticStrength.MEDIA -> haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        HapticStrength.FUERTE -> {
            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
            haptica.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }
}
