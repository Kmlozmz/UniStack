package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unistack.app.core.design.theme.UniStackColors

/**
 * Una pantalla con título que se encoge y se queda arriba.
 *
 * El título empieza grande, y al desplazar se recoge hasta quedarse como una barra fina fijada
 * en la parte de arriba. Antes la cabecera era un elemento más de la lista: al bajar se iba con
 * el resto y dejaba de saber dónde estabas, que es justo lo que un título tiene que decir.
 *
 * La barra reaparece en cuanto se desplaza hacia arriba, sin tener que volver al principio de
 * la lista.
 *
 * El contenido va en una lista perezosa con el rebote elástico puesto, así que las dos cosas
 * —el título fijo y el rebote— llegan juntas a cada pantalla que use esto.
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

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = UniStackColors.Background,
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            title,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        // El subtítulo solo acompaña al título grande: encogido no cabe, y
                        // recortarlo a puntos suspensivos no aporta nada.
                        if (subtitle != null && appBarState.collapsedFraction < 0.5f) {
                            Text(
                                subtitle,
                                color = UniStackColors.TextSecondary,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = UniStackColors.TextPrimary
                        )
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = UniStackColors.Background,
                    scrolledContainerColor = UniStackColors.SurfaceVariant,
                    titleContentColor = UniStackColors.TextPrimary,
                    navigationIconContentColor = UniStackColors.TextPrimary
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .elasticScroll(),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                top = innerPadding.calculateTopPadding(),
                bottom = innerPadding.calculateBottomPadding() + bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content
        )
    }
}
