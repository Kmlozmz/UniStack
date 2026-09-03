@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_profile.presentation

import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.unistack.app.core.design.components.SettingsHeader
import com.unistack.app.core.design.components.UniSegmentedControl
import com.unistack.app.core.design.components.UniSegmentedOption
import com.unistack.app.core.design.components.UniSwitch
import com.unistack.app.core.design.components.cleanClickable
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.SectionLabelStyle
import com.unistack.app.core.design.theme.scrollBottomRoom
import com.unistack.app.feature_user.domain.MotionCatalog
import com.unistack.app.feature_user.domain.MotionChoice
import com.unistack.app.feature_user.domain.MotionGesture
import com.unistack.app.feature_user.domain.MotionPreference
import com.unistack.app.feature_user.domain.MotionPreferences

/**
 * Movimiento: veinte gestos, ciento once variantes, y todas corriendo a la vez.
 *
 * **Elegir movimiento leyendo nombres no funciona.** «Trazo» y «Barrido» son dos palabras que
 * no dicen qué va a pasar al marcar asistencia; «Escalonada» y «Cascada» suenan igual y son
 * cosas distintas. Por eso cada variante se pinta animada en su propia caja: se comparan de un
 * vistazo, en la misma pantalla, sin tener que salir a probar.
 *
 * Arriba va el interruptor de siempre —completo, reducido o nada—, porque quien necesita quitar
 * el movimiento lo necesita de una vez y no gesto a gesto. Con él en «reducido» o «nada», todo
 * lo de abajo queda en pausa y la pantalla lo dice en vez de fingir que sigue mandando.
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
                title = "Movimiento",
                subtitle = "${MotionCatalog.gestures.size} gestos · ${MotionCatalog.variantCount} variantes",
                onBackClick = onBackClick
            )
        }

        item {
            Text("CUÁNTO MOVIMIENTO", style = SectionLabelStyle, color = MaterialTheme.colorScheme.primary)
        }
        item {
            UniSegmentedControl(
                selected = appearance.motionPreference,
                options = MotionPreference.entries.map {
                    UniSegmentedOption(value = it, label = it.etiqueta())
                },
                onSelected = { valor ->
                    viewModel.updateAppearance { it.copy(motionPreference = valor) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                text = if (activo) {
                    "Cada gesto de abajo se elige por separado."
                } else {
                    "Con el movimiento así, lo de abajo queda guardado pero en pausa."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        MotionCatalog.grouped().forEach { (grupo, gestos) ->
            item(key = "rotulo-$grupo") {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = grupo,
                        style = SectionLabelStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "${gestos.sumOf { it.options.size }} variantes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            gestos.forEach { gesto ->
                item(key = gesto.id) {
                    GestureCard(
                        gesto = gesto,
                        motion = motion,
                        activo = activo,
                        onElegir = { opcion ->
                            viewModel.updateAppearance { prefs ->
                                prefs.copy(motion = gesto.write(prefs.motion, opcion))
                            }
                        }
                    )
                }
            }
        }

        item {
            Text(
                "OTROS",
                style = SectionLabelStyle,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        MotionCatalog.toggles.forEach { toggle ->
            item(key = toggle.id) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        modifier = Modifier
                            .cleanClickable {
                                viewModel.updateAppearance { prefs ->
                                    prefs.copy(motion = toggle.write(prefs.motion, !toggle.read(prefs.motion)))
                                }
                            }
                            .padding(horizontal = 15.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(13.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(toggle.name, style = MaterialTheme.typography.titleSmallEmphasized)
                            Text(
                                toggle.detail,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        UniSwitch(
                            checked = toggle.read(motion),
                            onCheckedChange = { valor ->
                                viewModel.updateAppearance { prefs ->
                                    prefs.copy(motion = toggle.write(prefs.motion, valor))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Un gesto con su rejilla de variantes.
 *
 * La rejilla es de tres en fondo fijo y no un carrusel: comparar pide verlas juntas, y en un
 * carrusel la de la izquierda desaparece justo cuando llega la de la derecha.
 */
@Composable
private fun GestureCard(
    gesto: MotionGesture,
    motion: MotionPreferences,
    activo: Boolean,
    onElegir: (MotionChoice) -> Unit
) {
    val elegida = gesto.read(motion)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Column {
                Text(gesto.name, style = MaterialTheme.typography.titleSmallEmphasized)
                Text(
                    gesto.detail,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // Tres por fila: con cuatro no cabe el nombre de «Máquina de escribir» sin partirse,
            // y el nombre es la mitad de lo que hace falta para elegir.
            gesto.options.chunked(3).forEach { fila ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    fila.forEach { opcion ->
                        VariantCard(
                            gestoId = gesto.id,
                            opcion = opcion,
                            elegida = opcion.id == elegida.id,
                            animar = activo,
                            modifier = Modifier.weight(1f),
                            onClick = { onElegir(opcion) }
                        )
                    }
                    // Rellena la fila incompleta para que las cajas no se estiren al doble.
                    repeat(3 - fila.size) { Box(modifier = Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun VariantCard(
    gestoId: String,
    opcion: MotionChoice,
    elegida: Boolean,
    animar: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val tinta = tintaDemo()
    // Cada gesto lleva su propio compás: el latido pide dos segundos y una transición, uno y
    // medio. Con un reloj común, la mitad de las variantes se veían a destiempo.
    val t = bucle(duracionMs = duracionDe(gestoId), etiqueta = gestoId + opcion.id)
    val grosor by animateDpAsState(if (elegida) 2.dp else 1.dp, label = "borde")

    Column(
        modifier = modifier.cleanClickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .border(
                    width = grosor,
                    color = if (elegida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp)
                )
        ) {
            LienzoDemo(t = if (animar) t else 0.55f) { reloj ->
                pintarVariante(gestoId, opcion.id, reloj, tinta)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            if (elegida) {
                Icon(
                    Icons.Rounded.Check,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = opcion.label,
                style = MaterialTheme.typography.labelSmall,
                color = if (elegida) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (elegida) FontWeight.Bold else FontWeight.Normal,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
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
