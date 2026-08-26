package com.unistack.app.feature_grades.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.unistack.app.core.design.components.UniCard
import com.unistack.app.core.design.theme.LocalInterfaceSpacing
import com.unistack.app.core.design.theme.LocalMotionDurationScale
import kotlin.math.roundToInt

/**
 * Uno de los tres bloques del formulario: identidad, cuándo y académico.
 *
 * La cabecera va dentro de la tarjeta y no fuera para que al plegarse quede una sola pieza y
 * no un título huérfano sobre un hueco. Cuando está plegado, el resumen de la derecha dice
 * qué guarda dentro: plegar esconde los controles, nunca la información.
 */
@Composable
internal fun FormBlock(
    title: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier,
    summary: String? = null,
    expanded: Boolean = true,
    tinted: Boolean = false,
    onHeaderClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val spacing = LocalInterfaceSpacing.current
    val motionScale = LocalMotionDurationScale.current
    val expandSpec = tween<IntSize>((200 * motionScale).roundToInt().coerceAtLeast(1))
    val fadeSpec = tween<Float>((160 * motionScale).roundToInt().coerceAtLeast(1))

    UniCard(
        modifier = modifier.fillMaxWidth(),
        // Relleno del sistema y sin contorno. El bloque se separaba del fondo con un pelo
        // de medio punto y un tinte al 38 %, que es como se dibujaba una tarjeta antes de
        // que hubiera contenedores con su propio color.
        color = if (tinted) {
            accent.copy(alpha = 0.10f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        shape = MaterialTheme.shapes.large,
        tonalElevation = 0.dp,
        contentPadding = PaddingValues(spacing.cardPadding)
    ) {
        // El espaciado no se condiciona a [expanded]: mientras el bloque se pliega su
        // contenido sigue en el árbol, y quitarle el hueco de golpe da un salto al empezar
        // la animación. Plegado del todo, AnimatedVisibility no deja nodo y no hay hueco.
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.large)
                    .then(
                        if (onHeaderClick != null) {
                            Modifier.clickable(
                                onClickLabel = if (expanded) "Plegar $title" else "Desplegar $title",
                                onClick = onHeaderClick
                            )
                        } else {
                            Modifier
                        }
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accent.copy(alpha = 0.16f), MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
                }
                Text(
                    text = title,
                    modifier = Modifier.padding(start = 12.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
                // El resumen se lleva el hueco sobrante en vez de medirse a su antojo: «L M X
                // J V S D · 08:00» junto a «Académico» no cabe en una pantalla estrecha, y sin
                // peso empujaría al resto fuera del borde en vez de recortarse.
                if (summary != null && !expanded) {
                    Text(
                        text = summary,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.End,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Spacer(Modifier.weight(1f))
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
                if (onHeaderClick != null) {
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 6.dp)
                    )
                }
            }
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = expandSpec) + fadeIn(animationSpec = fadeSpec),
                exit = shrinkVertically(animationSpec = expandSpec) + fadeOut(animationSpec = fadeSpec)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp), content = content)
            }
        }
    }
}
