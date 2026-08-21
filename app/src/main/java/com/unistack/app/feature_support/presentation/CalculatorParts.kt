@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_support.presentation

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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Backspace
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
            Key(
                label = null,
                icon = Icons.Rounded.Backspace,
                contentDescription = "Borrar un dígito",
                height = TallKeyHeight,
                muted = true,
                onClick = {
                    val base = if (fresh) "0" else draft
                    onDraft(if (base.length > 1) base.dropLast(1) else "0")
                }
            )
            Key(
                label = "Limpiar",
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
    Surface(
        onClick = onClick,
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
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.weight(1f),
        shape = MaterialTheme.shapes.large,
        color = if (active) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        contentColor = if (active) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 13.dp, vertical = 8.dp)) {
            Text(text = label, style = SectionLabelStyleSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = if (active) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
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
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(54.dp),
        shape = MaterialTheme.shapes.large,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = if (enabled) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.outline
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                // Flecha mientras falta el segundo dato, más cuando ya guarda. Un «+» que en
                // realidad avanza promete guardar y no guarda.
                imageVector = if (isArrow) {
                    Icons.AutoMirrored.Rounded.ArrowForward
                } else {
                    Icons.Rounded.Add
                },
                contentDescription = if (isArrow) "Pasar al siguiente dato" else "Añadir",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * El nombre, escrito a mano. Es el único sitio de la calculadora donde sale el teclado de
 * texto, y solo si se toca.
 */
@Composable
internal fun NameField(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = { onValueChange(it.take(28)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleSmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth()
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
