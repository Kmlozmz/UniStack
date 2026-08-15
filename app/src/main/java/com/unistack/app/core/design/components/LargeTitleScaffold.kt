package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors

/** La barra de arriba: lo justo para la flecha y el título en pequeño. */
private val BarHeight = 52.dp

/** Cuánto hay que desplazar para que el relevo termine. */
private const val HANDOVER_DISTANCE_DP = 44f

/**
 * El título grande se va con el contenido y su sitio lo ocupa el mismo título en pequeño.
 *
 * El título grande **es contenido**: el primer elemento de la lista, y se desplaza como
 * cualquier otro. Lo que se queda arriba es una barra fina con la flecha, y dentro de ella
 * aparece el título en pequeño justo cuando el grande termina de irse, con una línea que separa
 * la barra de lo que pasa por debajo.
 *
 * Esa es la diferencia con lo que había antes: el intento anterior usaba la barra grande de
 * Material, que lleva **dos** títulos y funde uno con otro, y por eso se leía como un salto en
 * vez de como un relevo. Aquí el grande se va de verdad, y el pequeño solo aparece cuando ya no
 * hay grande que mirar.
 */
@Composable
fun LargeTitleScaffold(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    horizontalPadding: Dp = 18.dp,
    bottomPadding: Dp = 0.dp,
    itemSpacing: Dp = 12.dp,
    actions: @Composable () -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    val handoverPx = with(density) { HANDOVER_DISTANCE_DP.dp.toPx() }

    // El relevo se mide sobre el primer elemento, que es el título grande: en cuanto se ha ido
    // del todo —o la lista va por elementos posteriores— el pequeño está puesto.
    val handover by remember(handoverPx) {
        derivedStateOf {
            if (listState.firstVisibleItemIndex > 0) {
                1f
            } else {
                (listState.firstVisibleItemScrollOffset / handoverPx).coerceIn(0f, 1f)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = BarHeight + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing)
        ) {
            item(key = "titulo-grande") {
                Column(modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) {
                    Text(
                        text = title,
                        color = UniStackColors.TextPrimary,
                        fontSize = 28.sp,
                        lineHeight = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            color = UniStackColors.TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            content()
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopStart)
                .background(UniStackColors.Background)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(BarHeight)
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick, modifier = Modifier.size(44.dp)) {
                    Icon(
                        Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "Volver",
                        tint = UniStackColors.TextPrimary
                    )
                }
                Text(
                    text = title,
                    modifier = Modifier
                        .weight(1f)
                        .alpha(handover),
                    color = UniStackColors.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                // Reserva el mismo ancho que la flecha para que el título quede centrado de
                // verdad y no desplazado hacia la derecha.
                Box(modifier = Modifier.size(44.dp)) { actions() }
            }
            // La línea aparece con el título pequeño: es lo que dice que hay contenido pasando
            // por debajo de la barra.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .alpha(handover)
                    .background(UniStackColors.SoftOutline)
            )
        }
    }
}
