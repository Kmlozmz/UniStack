package com.unistack.app.core.design.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import com.unistack.app.core.design.theme.UniStackColors

/** La barra recogida: la altura de una fila con su botón de atrás. */
private val CollapsedHeight = 56.dp

/** Desplegada: esa fila, más el sitio del título grande debajo. */
private val ExpandedHeight = 112.dp

/** A cuánto queda el título cuando termina de encogerse. */
private const val COLLAPSED_SCALE = 0.62f

/** Lo que se corre a la derecha para colocarse al lado de la flecha. */
private val TitleShift = 32.dp

/**
 * Una pantalla cuyo título se encoge hasta quedarse en la barra de arriba.
 *
 * Está escrito a mano y no con `LargeTopAppBar` porque esa barra **no encoge el título**: lleva
 * dos, uno grande abajo y otro pequeño arriba, y al desplazar funde el primero con el segundo.
 * De ahí venía la sensación de que el título «desaparecía y aparecía» en vez de recogerse. Y
 * meterle un subtítulo dentro descolocaba la flecha, porque su ranura de título espera una sola
 * línea.
 *
 * Aquí el título es **uno solo**: se reduce al 62% desde su borde izquierdo y se desliza hasta
 * el hueco que queda a la derecha de la flecha, que no se mueve en todo el recorrido. Como es el
 * mismo elemento de principio a fin, no hay relevo que notar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingScaffold(
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
    val appBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(appBarState)
    val density = LocalDensity.current

    // El tope lo fija normalmente la barra de Material; como no la usamos, se pone aquí, que
    // es lo que hace que `collapsedFraction` signifique algo.
    SideEffect {
        appBarState.heightOffsetLimit = -with(density) { (ExpandedHeight - CollapsedHeight).toPx() }
    }

    val fraction = appBarState.collapsedFraction
    val headerHeight = lerp(ExpandedHeight, CollapsedHeight, fraction)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(UniStackColors.Background)
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                // El contenido arranca por debajo de la barra desplegada, y desde ahí la barra
                // se recoge sobre él.
                top = ExpandedHeight + statusBarHeight(),
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(headerHeight)
                .background(UniStackColors.Background)
        ) {
            // La flecha no se mueve en todo el recorrido: es el punto fijo contra el que se lee
            // que lo que encoge es el título.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CollapsedHeight)
                    .align(Alignment.TopStart)
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
                Box(modifier = Modifier.weight(1f))
                actions()
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 20.dp, end = 20.dp)
                    .padding(bottom = lerp(16.dp, 12.dp, fraction))
                    .graphicsLayer {
                        val scale = lerp(1f, COLLAPSED_SCALE, fraction)
                        scaleX = scale
                        scaleY = scale
                        // Desde el borde izquierdo: encoger desde el centro haría que el título
                        // se moviera hacia la izquierda mientras se hace pequeño.
                        transformOrigin = TransformOrigin(0f, 0.5f)
                        translationX = TitleShift.toPx() * fraction
                    }
            ) {
                Text(
                    text = title,
                    color = UniStackColors.TextPrimary,
                    fontSize = 26.sp,
                    lineHeight = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        color = UniStackColors.TextSecondary.copy(
                            // Se va antes que el título: recogido no hay sitio para dos líneas.
                            alpha = (1f - fraction * 2f).coerceIn(0f, 1f)
                        ),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** El alto de la barra de estado, para que el contenido arranque por debajo de la cabecera. */
@Composable
private fun statusBarHeight(): Dp =
    WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
