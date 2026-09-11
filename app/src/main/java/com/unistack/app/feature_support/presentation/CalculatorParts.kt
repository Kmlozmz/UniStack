@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

import com.unistack.app.core.design.theme.LocalVividAccents
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.AnimatedContent
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.unistack.app.core.utils.performSafely
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import com.unistack.app.core.utils.Textos

/** El alto de una tecla, y de ahí sale el de las dos altas: dos teclas y el hueco. */
private val KeyHeight = 52.dp
private val KeyGap = 8.dp
private val TallKeyHeight = KeyHeight * 2 + KeyGap

/**
 * El teclado de la calculadora.
 *
 * Está aquí y no se usa el de Android por dos razones. Meter diez notas con campos de texto
 * son diez enfoques y diez veces el teclado subiendo y bajando, con media pantalla tapada
 * mientras escribes; y un teclado propio **sabe cuál es el máximo**, así que un 9 sobre una
 * escala de 0 a 5 no llega ni a escribirse.
 *
 * Cuando una tecla no cabe no se queda muda: manda [onBlocked] con el motivo. Una tecla que no
 * responde y no explica nada es el mismo fallo que un botón apagado sin decir por qué.
 *
 * [fresh] es lo que hace que al cambiar de casilla se empiece de cero. Sin eso, teclear sobre
 * un «60» daba «601», que no cabe, y la casilla parecía estropeada.
 */
@Composable
internal fun CalculatorKeypad(
    draft: String,
    max: Double,
    fresh: Boolean,
    blockedMessage: String,
    onDraft: (String) -> Unit,
    onClear: () -> Unit,
    onBlocked: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    fun type(symbol: String) {
        val base = if (fresh) "0" else draft
        if (symbol == "," && base.contains(',')) return
        val next = if (base == "0" && symbol != ",") symbol else base + symbol
        val value = next.replace(',', '.').toDoubleOrNull()
        if (value != null && value > max) {
            onBlocked(blockedMessage)
            return
        }
        onDraft(next)
    }

    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(KeyGap)
        ) {
            listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(",", "0", "00")
            ).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(KeyGap)) {
                    row.forEach { symbol ->
                        Key(
                            label = symbol,
                            modifier = Modifier.weight(1f),
                            muted = symbol == "," || symbol == "00",
                            onClick = { type(symbol) }
                        )
                    }
                }
            }
        }
        Column(
            modifier = Modifier.width(84.dp),
            verticalArrangement = Arrangement.spacedBy(KeyGap)
        ) {
            // `fillMaxWidth` no sobra: sin él la superficie se encoge a su contenido y las
            // dos teclas salían como tiras estrechas con el icono y el rótulo por fuera.
            Key(
                label = null,
                icon = Icons.AutoMirrored.Rounded.Backspace,
                contentDescription = stringResource(R.string.calculator_delete_digit),
                modifier = Modifier.fillMaxWidth(),
                height = TallKeyHeight,
                muted = true,
                onClick = {
                    val base = if (fresh) "0" else draft
                    onDraft(if (base.length > 1) base.dropLast(1) else "0")
                }
            )
            Key(
                label = Textos.get(R.string.tasks_filter_clear),
                modifier = Modifier.fillMaxWidth(),
                height = TallKeyHeight,
                small = true,
                muted = true,
                onClick = onClear
            )
        }
    }
}

@Composable
private fun Key(
    label: String?,
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    contentDescription: String? = null,
    height: androidx.compose.ui.unit.Dp = KeyHeight,
    muted: Boolean = false,
    small: Boolean = false,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Surface(
        onClick = {
            // El mismo toque que da el teclado del sistema: sin él, teclear en una superficie
            // de cristal no acusa recibo y se acaba mirando la cifra a cada pulsación.
            haptics.performSafely(HapticFeedbackType.KeyboardTap)
            onClick()
        },
        modifier = modifier.height(height),
        shape = MaterialTheme.shapes.large,
        color = if (muted) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        contentColor = if (muted) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            MaterialTheme.colorScheme.onSurface
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (icon != null) {
                Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(22.dp))
            } else if (label != null) {
                Text(
                    text = label,
                    style = if (small) {
                        MaterialTheme.typography.labelLarge
                    } else {
                        MaterialTheme.typography.headlineSmall
                    },
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Una casilla donde cae lo que se teclea.
 *
 * La activa va rellena: con dos o tres casillas y un solo teclado, lo único que dice dónde va
 * a caer el número es cuál está encendida.
 */
@Composable
internal fun RowScope.NumberSlot(
    label: String,
    value: String,
    active: Boolean,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    /*
     * La casilla activa cede el fondo.
     *
     * El relleno de la que está activa lo pinta [SlotRow] por detrás, en un solo rectángulo que
     * viaja de una a otra. Si la casilla pintara además el suyo, se verían dos: el que llega y
     * el que ya estaba. Aquí queda el fondo de la casilla apagada, que se desvanece justo cuando
     * el foco entra.
     */
    val container by animateColorAsState(
        targetValue = if (active) Color.Transparent else MaterialTheme.colorScheme.surfaceContainer,
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "fondo"
    )
    val ink by animateColorAsState(
        targetValue = if (active) {
            LocalVividAccents.current.let { it.inkOn(it.violet) }
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "tinta"
    )
    val valueInk by animateColorAsState(
        targetValue = if (active) {
            LocalVividAccents.current.let { it.inkOn(it.violet) }
        } else {
            MaterialTheme.colorScheme.onSurface
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "tinta del valor"
    )

    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        // Menos redondas que el resto: con el radio grande, «VALE · queda 100 %» quedaba
        // apretado contra la curva y se leía torcido.
        shape = MaterialTheme.shapes.medium,
        color = container,
        contentColor = ink
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
            Text(text = label, style = SectionLabelStyleSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = valueInk,
                maxLines = 1
            )
        }
    }
}

/** El botón que avanza o guarda, según lo que toque. */
@Composable
internal fun SlotAction(
    isArrow: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    /*
     * La forma cuenta el cambio antes que el icono.
     *
     * Al llegar al estado en que ya puede guardar, el botón se redondea del todo: de esquina de
     * 16 a círculo. Es el mismo gesto que hace M3 Expressive con los botones que cambian de
     * papel, y se nota con el rabillo del ojo sin tener que mirar qué icono hay dentro.
     */
    val corner by animateDpAsState(
        targetValue = if (isArrow) 16.dp else 27.dp,
        animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "esquina"
    )
    val container by animateColorAsState(
        targetValue = if (enabled) {
            LocalVividAccents.current.violet
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "fondo del botón"
    )

    Surface(
        onClick = {
            // Al guardar, un toque distinto del de las teclas: es el momento en el que la nota
            // entra en la cuenta, no una pulsación más.
            haptics.performSafely(
                if (isArrow) HapticFeedbackType.KeyboardTap else HapticFeedbackType.Confirm
            )
            onClick()
        },
        enabled = enabled,
        modifier = Modifier.size(54.dp),
        shape = RoundedCornerShape(corner),
        color = container,
        contentColor = if (enabled) {
            LocalVividAccents.current.let { it.inkOn(it.violet) }
        } else {
            MaterialTheme.colorScheme.outline
        }
    ) {
        Box(contentAlignment = Alignment.Center) {
            AnimatedContent(
                targetState = isArrow,
                transitionSpec = {
                    // La flecha sale por donde apuntaba y el visto entra desde el centro: el
                    // cruce cuenta que una cosa dio paso a la otra, no que se cambió un dibujo.
                    (scaleIn(initialScale = 0.6f) + fadeIn()) togetherWith
                        (scaleOut(targetScale = 1.3f) + fadeOut())
                },
                label = "icono de la acción"
            ) { arrow ->
                Icon(
                    // Flecha mientras falta el segundo dato, visto cuando ya guarda. Un «+»
                    // promete sumar algo a una lista, y lo que pasa es que la nota queda
                    // registrada en la cuenta.
                    imageVector = if (arrow) {
                        Icons.AutoMirrored.Rounded.ArrowForward
                    } else {
                        Icons.Rounded.Check
                    },
                    contentDescription = if (arrow) stringResource(R.string.calculator_next_data) else stringResource(R.string.calculator_add),
                    modifier = Modifier.size(if (arrow) 24.dp else 26.dp)
                )
            }
        }
    }
}

/** El aviso de por qué una tecla no hizo nada. */
@Composable
internal fun CalculatorToast(message: String, onDismiss: () -> Unit) {
    Surface(
        onClick = onDismiss,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.inverseSurface,
        contentColor = MaterialTheme.colorScheme.inverseOnSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(Icons.Rounded.Info, contentDescription = null, modifier = Modifier.size(18.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private val SectionLabelStyleSmall
    @Composable get() = MaterialTheme.typography.labelSmall.copy(
        fontWeight = FontWeight.Bold,
        letterSpacing = androidx.compose.ui.unit.TextUnit(0.8f, androidx.compose.ui.unit.TextUnitType.Sp)
    )

/** Un texto en gris bajo un bloque, para las notas al pie de cada pestaña. */
@Composable
internal fun CalculatorFootnote(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start
    )
}
