@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.feature_notes.presentation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.res.stringResource
import com.unistack.app.R
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.theme.LocalAccessibilityPreferences
import com.unistack.app.feature_user.domain.MotionPreference

/**
 * «Estás en Cálculo II». La app se da cuenta y lo ofrece.
 *
 * Sugiere, no impone: fue la condición. Sale un atajo para vincular y un aspa para quitarlo de
 * en medio, y quitarlo se recuerda mientras dure la nota —insistir con algo que acaban de
 * rechazar es la forma más rápida de que estorbe—.
 *
 * El borde es lo que él pidió: «una especie de borde brillante que va remarcándose tipo RGB,
 * pero me refiero al efecto». Gira y respira, y va **solo en la píldora**, no en el editor
 * entero: eso fue una corrección suya después de verlo puesto en todo el recuadro.
 *
 * Con el movimiento reducido, el borde se queda quieto. La sugerencia sigue viéndose —el color
 * no depende de que gire—, pero deja de moverse mientras alguien intenta escribir.
 */
@Composable
fun NoteSuggestionPill(
    subjectName: String,
    accent: Color,
    onLink: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val motion = LocalAccessibilityPreferences.current.motionPreference
    val anima = motion == MotionPreference.FULL

    val transicion = rememberInfiniteTransition(label = "sugerencia")
    val giro by transicion.animateFloat(
        initialValue = 0f,
        targetValue = if (anima) 1f else 0f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing)),
        label = "giro"
    )
    val respiracion by transicion.animateFloat(
        initialValue = if (anima) 0.45f else 0.85f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "respiracion"
    )

    val terciario = MaterialTheme.colorScheme.tertiary
    val primario = MaterialTheme.colorScheme.primary
    val ciclo = remember(accent, terciario, primario) {
        listOf(accent, terciario, primario, accent)
    }
    val brillo = remember(ciclo, giro, respiracion) {
        sweepRotado(ciclo.map { it.copy(alpha = respiracion) }, giro)
    }

    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.border(width = 1.5.dp, brush = brillo, shape = CircleShape)
    ) {
        Row(
            modifier = Modifier.padding(start = 11.dp, end = 5.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Icon(
                Icons.Rounded.AutoAwesome,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = stringResource(R.string.notes_suggestion_in_subject, subjectName),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Surface(
                onClick = onLink,
                shape = CircleShape,
                color = accent.copy(alpha = 0.16f),
                contentColor = accent
            ) {
                Text(
                    stringResource(R.string.notes_suggestion_link),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                )
            }
            Surface(
                onClick = onDismiss,
                shape = CircleShape,
                color = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.notes_suggestion_dismiss),
                    modifier = Modifier.padding(4.dp).size(14.dp)
                )
            }
        }
    }
}

/**
 * El degradado cónico, girado sin girar la figura.
 *
 * Rotar el lienzo giraría también la píldora. Lo que gira aquí son las **paradas de color**: la
 * lista es cíclica —empieza y acaba en el mismo color— y en el corte se pone el color exacto que
 * la rotación deja ahí, para que no se vea la costura.
 */
private fun sweepRotado(colores: List<Color>, t: Float): Brush {
    val paradas = ArrayList<Pair<Float, Color>>(colores.size + 2)
    val ultimo = colores.size - 1
    for (i in colores.indices) {
        val base = if (ultimo == 0) 0f else i / ultimo.toFloat()
        paradas += (((base + t) % 1f) + 1f) % 1f to colores[i]
    }
    paradas.sortBy { it.first }
    val corte = colorEn(colores, 1f - t)
    paradas.add(0, 0f to corte)
    paradas += 1f to corte
    return Brush.sweepGradient(colorStops = paradas.toTypedArray(), center = Offset.Unspecified)
}

private fun colorEn(colores: List<Color>, f: Float): Color {
    if (colores.size < 2) return colores.firstOrNull() ?: Color.Transparent
    val tramos = colores.size - 1
    val x = ((f % 1f) + 1f) % 1f * tramos
    val i = x.toInt().coerceIn(0, tramos - 1)
    return lerp(colores[i], colores[i + 1], x - i)
}
