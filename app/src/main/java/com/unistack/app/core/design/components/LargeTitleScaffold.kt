@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.unistack.app.core.design.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement

/**
 * Pantalla con título grande que se recoge al desplazar.
 *
 * Ahora es un [Scaffold] con [LargeFlexibleTopAppBar], la barra de dos filas de Material 3
 * Expressive. Antes era una `LazyColumn` con el título como primer elemento y una barra
 * dibujada encima, midiendo a mano el desplazamiento para relevar un título por otro.
 *
 * Ese apaño existía por un motivo real: la `LargeTopAppBar` de Material 3 1.3 llevaba **dos**
 * títulos y fundía uno con otro, y el cruce se leía como un salto. La barra flexible de
 * Expressive encoge el mismo título en vez de intercambiar dos, que es lo que se quería, así
 * que el motivo desapareció y con él las ciento setenta líneas de medir el scroll.
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
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            LargeFlexibleTopAppBar(
                title = {
                    Text(text = title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                },
                subtitle = subtitle?.let {
                    { Text(text = it, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = { actions() },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                start = horizontalPadding,
                end = horizontalPadding,
                bottom = bottomPadding
            ),
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            content = content
        )
    }
}
